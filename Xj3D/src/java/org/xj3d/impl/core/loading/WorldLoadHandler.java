/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.loading;

// External imports
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

//import org.ietf.uri.URL;
import java.net.URL;
import org.ietf.uri.ResourceConnection;
import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.xj3d.io.ReadProgressListener;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.BlockingQueue;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.FileCache;
import org.xj3d.core.loading.LoadRequestHandler;
import org.xj3d.core.loading.WorldLoader;

/**
 * Independent thread used to load a world from a list of URLs and then
 * place it in the given node.
 * <p>
 *
 * This implementation is designed to work as both a loadURL() and
 * createVrmlFromUrl() call handler. The difference is defined by what data
 * is supplied to the thread. If the target node is specified, then we assume
 * that the caller wants us to put the results there. If it is null, then
 * assume that we're doing a loadURL call and replace the entire world.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class WorldLoadHandler
    implements LoadRequestHandler, ReadProgressListener {

    /** Error message when nothing loaded in a LoadURL */
    private static final String LOAD_URL_FAIL_MSG =
        "No valid URLs found for loadURL call.";

    /** Error message when nothing loaded in a createVrmlFromURL */
    private static final String CREATE_FAIL_MSG =
        "No valid URLs found for createVrmlFromURL call.";

    /** Error message when the setFieldValue fails */
    private static final String SET_FIELD_MSG =
        "Odd field error in createVrmlFromUrl";

    /** Flag to say that the world loading should be aborted now */
    private boolean terminateCurrent;

    /** The cache representation that this loader is using */
    private FileCache fileCache;

    /** The current progressListener */
    private ProgressListener progressListener;

    /** The final url of the current item */
    String final_url;

    /**
     * Create a new empty world loader. It does not start the thread running.
     * That is the job of the caller code.
     *
     * @param cache The file cache implementation to use for this handler
     */
    public WorldLoadHandler(FileCache cache) {
        fileCache = cache;
        terminateCurrent = false;
    }

    //---------------------------------------------------------------
    // Methods required by ReadProgressListener
    //---------------------------------------------------------------

    /**
     * Notification of where the stream is at.  The value is
     * dependent on the type, absolute or relative.
     *
     * @param value The new value
     */
    public void progressUpdate(long value) {
        if (progressListener != null) {

            // TODO: Can we avoid the garbage
            // TODO: can we get the current resource?
            ProgressEvent event = new ProgressEvent((ResourceConnection)null, ProgressEvent.DOWNLOAD_UPDATE,final_url,(int)value);
            progressListener.downloadUpdate(event);
        }
    }

    /**
     * The stream has closed.
     */
    public void streamClosed() {
        ProgressEvent event = new ProgressEvent((ResourceConnection)null, ProgressEvent.DOWNLOAD_END,final_url,0);
        progressListener.downloadEnded(event);
    }

    //----------------------------------------------------------
    // Methods defined by LoadRequestHandler
    //----------------------------------------------------------

    /**
     * Process this load request now.
     *
     * @param reporter The errorReporter to send all messages to
     * @param url The list of URLs to load
     * @param loadList The list of LoadDetails objects to sent the fulfilled
     *    requests to
     */
    public void processLoadRequest(ErrorReporter reporter,
                                   String[] url,
                                   Vector loadList) {
        VRMLScene scene = null;
        URL real_url;

        WorldLoadDetails details = (WorldLoadDetails)loadList.remove(0);
        WorldLoader loader = details.worldLoader.fetchLoader();

        for(int i = 0; i < url.length && !terminateCurrent; i++) {
            if(terminateCurrent) {
                details.worldLoader.releaseLoader(loader);
                continue;
            }

            try {
                real_url = new URL(url[i]);
                final_url = url[i];
            } catch(MalformedURLException mue) {
                try {
                    final_url = details.core.getWorldURL() + url[i];

                    real_url = new URL(final_url);
                } catch(MalformedURLException mue2) {
                    reporter.warningReport("Invalid URL: " + url[i],
                                                null);
                    final_url = "<NONE>";
                    continue;
                }
            }

            if(terminateCurrent) {
                details.worldLoader.releaseLoader(loader);
                return;
            }

            InputSource is = new InputSource(real_url);

            if (details.progressListener != null) {
                progressListener = details.progressListener;
                is.setProgressListener(details.progressListener);
                is.setReadProgressListener(this, 100000);
            }

            try {
                scene = loader.loadNow(details.core,
                                       is,
                                       false,
                                       details.majorVersion,
                                       details.minorVersion);
            } catch(IOException ioe) {
                reporter.warningReport("I/O Error loading " + final_url,
                                            ioe);
            } catch (VRMLParseException vpe) {
                reporter.warningReport("VRML Parse exception loading " + final_url, vpe);
            }

            progressListener = null;
            if(scene != null)
                break;
        }

        details.worldLoader.releaseLoader(loader);

        if(scene == null) {
            // Produce invalid URL notifications here
            String msg = details.isLoadURL ? LOAD_URL_FAIL_MSG : CREATE_FAIL_MSG;
            reporter.warningReport(msg, null);

            details.core.sendURLFailEvent(msg);
            return;
        }

        if(terminateCurrent)
            return;

        if(details.isLoadURL) {
            details.core.setScene(scene, null);
        } else {
            VRMLNode root = scene.getRootNode();
            VRMLWorldRootNodeType world = (VRMLWorldRootNodeType)root;

            // Get the children nodes and then force the world root to delete
            // them. This is because if we leave the nodes as part of the world
            // root, they have a Java3D parent. If we try to add them later on
            // to part of the live scene graph, they will generate multiple
            // parent exceptions. This avoids that problem.
            VRMLNodeType[] children = world.getChildren();
            world.setChildren((VRMLNodeType)null);

            // Since createVrmlFromUrl returns back to the space it was called
            // in, the route manager just adds a big pile of routes to the loaded
            // space.
            ArrayList list = scene.getRoutes();
            int size = list.size();
            for(int i = 0; i < size && !terminateCurrent; i++) {
                ROUTE r = (ROUTE)list.get(i);
                details.routeManager.addRoute(details.space, r);
            }

            if(terminateCurrent)
                return;

            try {
                details.node.setValue(details.fieldIndex,
                                      children,
                                      children.length);
            } catch(VRMLException ife) {
                reporter.errorReport(SET_FIELD_MSG, ife);
            }
        }
    }
    /**
     * Notification to abort loading the current resource. If there is one
     * loading, it will terminate the procedure immediately and start fetching
     * the next available URI. This will only work if we are currently
     * processing a file. If we are not processing a file then this is
     * ignored.
     */
    public void abortCurrentFile() {
        terminateCurrent = true;
    }

    /**
     * Notification to shut down the load process entirely for this thread.
     * It probably means we are about to close down the whole system. If we
     * are held in the queue, blocked waiting for input, the caller should
     * call {@link org.web3d.util.BlockingQueue#purge()} on the queue
     * <i>after</i> calling this method. That will force the block to exit
     * and this thread to end.
     */
    public void shutdown() {
        terminateCurrent = true;
    }
}
