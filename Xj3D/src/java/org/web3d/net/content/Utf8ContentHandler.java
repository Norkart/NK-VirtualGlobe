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

package org.web3d.net.content;

// External imports
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.util.zip.GZIPInputStream;
import java.net.URLConnection;

import org.ietf.uri.ContentHandler;
import org.ietf.uri.ResourceConnection;
import org.ietf.uri.event.ProgressEvent;

// Local imports
import org.web3d.vrml.parser.*;

import org.web3d.browser.BrowserCore;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.sav.InputSource;

import org.xj3d.core.loading.WorldLoader;
import org.xj3d.core.loading.WorldLoaderManager;
import org.xj3d.io.ReadProgressListener;

/**
 * Content handler implementation for loading VRML UTF8 content from a URI
 * resource connection.
 * <P>
 *
 * The returned object type for this loader is a
 * {@link org.web3d.vrml.nodes.VRMLScene}. It does not load
 * any further content apart from the contents of this file. If this file
 * contains further content then the receiver of this object must organise to
 * have all the routes processed, external nodes loaded etc.
 * <p>
 * This implementation also loads everything in the incoming file. There is no
 * filtering of the content.
 * <p>
 *
 * For details on URIs see the IETF working group:
 * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
 * <P>
 *
 * @author  Justin Couch
 * @version $Revision: 1.17 $
 */
class Utf8ContentHandler extends ContentHandler
    implements ReadProgressListener {

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** Browser core for the loading */
    private BrowserCore core;

    /** The length of this stream */
    private long streamLength;

    /** The external form of the URL */
    private String extURL;

    /** The resource connection */
    private ResourceConnection resource;

    /**
     * Construct a new instance of the content handler.
     *
     * @param browser The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    Utf8ContentHandler(BrowserCore browser, WorldLoaderManager wlm) {
        core = browser;
        worldLoader = wlm;
    }

    /**
     * Given a fresh stream from a ResourceConnection, read and create an object
     * instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    public Object getContent(ResourceConnection resc)
        throws IOException {

        InputStream stream = resc.getInputStream();

        stream = new BufferedInputStream(stream);
        String encoding = resc.getContentEncoding();

        if (encoding != null && encoding.equals("x-gzip")) {
            stream = new GZIPInputStream(stream);
        }

        String full_url = resc.getURI().toExternalForm();
        if (full_url.startsWith("file:")) {
            full_url = full_url.replace('\\', '/');
            if (full_url.charAt(7) != '\\') {
                // Change file:/ to file:///
                full_url = "file:///" + full_url.substring(6);
            }
        }

        int index = full_url.lastIndexOf("/");
        String base_url = full_url.substring(0, index + 1);

        streamLength = resc.getContentLength();

        InputSource is = new InputSource(base_url, stream, full_url);
        is.setReadProgressListener(this, (int) (streamLength / 50));
        is.setContentType(resc.getContentType());

        WorldLoader ldr = worldLoader.fetchLoader();
        extURL = full_url;
        resource = resc;
        notifyDownloadStarted(resc, extURL);

        VRMLScene scene = ldr.loadNow(core, is);
        notifyDownloadFinished(resc, extURL);

        worldLoader.releaseLoader(ldr);

        resource = null;
        return scene;
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
        notifyDownloadProgress(resource, (int) value, extURL);
    }

    /**
     * The stream has closed.
     */
    public void streamClosed() {
    }
}
