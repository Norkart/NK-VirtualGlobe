/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.loading;

// External imports
import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;
import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.xj3d.core.loading.*;

import org.web3d.util.ErrorReporter;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.vrml.nodes.VRMLScene;

/**
 * Determines what the frame rate should be based on available information.
 *
 * @author  Alan Hudson
 * @version $Revision: 1.7 $
 */
public class FramerateThrottle extends Thread
    implements ProgressListener,
               BrowserCoreListener {

    /** The thread name */
    private static final String NAME = "Xj3D Framerate Throttle";

    /** What minimum frame cycle should we use during startup */
    private static final int FRAME_CYCLE_MINIMUM_STARTUP = 1000;

    /** What minimum frame cycle should we use during noloading operation */
    private int FRAME_CYCLE_MINIMUM_NOLOADING = 0;

    /**
     * What minimum frame cycle should we use during post startup loading
     * operation.
     */
    private static final int FRAME_CYCLE_MINIMUM_LOADING = 60;

    /** The script loader */
    private ScriptLoader scriptLoader;

    /** The load manager */
    private ContentLoadManager loadManager;

    /** Time to wait between queries in milleseconds*/
    private static final int WAIT_TIME = 50;

    /** Number of wait times till we assume no activity */
    /** Leave a little margin for setContent processing as well */
    private static final int LOAD_WAIT = 3;

    /** The number of cycles we've had no activity */
    private int noActivity;

    /** The universe to set the cycle interval on */
    private BrowserCore universe;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Should this throttle keep running */
    private boolean doRun;

    /** Is the main file loading */
    private boolean mainFileLoading;

    /** Is the initial load done */
    private boolean initialLoadDone;

    /** Display total load time */
    private static final boolean displayLoadTime = false;

    // TODO: These variables are to debug a problem here.  Remove when fixed.
    /** The last total.  Sometimes get stuck on some files? */
    private int lastTotal;

    /** The number of cycles we've had the same total */
    private int lastTotalTimes;

    /** Have we reported the error */
    private boolean reportedError;

    // End of debug variables

    /**
     * Construct a new instance of the throttle for the given core instance.
     *
     * @param core The core instance to work with
     */
    public FramerateThrottle(BrowserCore core, ErrorReporter reporter) {
        super(NAME);
        ResourceConnection.addGlobalProgressListener(this);
        universe = core;

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        universe.addCoreListener(this);

        doRun = true;

        start();
    }

    //---------------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
        mainFileLoading = false;
        initialLoadDone = false;

        startTime = System.currentTimeMillis();
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
    }

    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    public void browserShutdown() {
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
        ResourceConnection.removeGlobalProgressListener(this);
        doRun = false;
    }

    //---------------------------------------------------------------
    // Methods defined by ProgressListener
    //---------------------------------------------------------------

    /**
     * A connection to the resource has been established. At this point, no data
     * has yet been downloaded.
     *
     * @param evt The event that caused this method to be called.
     */
    public void connectionEstablished(ProgressEvent evt) {
        noActivity = LOAD_WAIT;
    }

    /**
     * The header information reading and handshaking is taking place. Reading
     * and intepreting of the data (a download started event) should commence
     * shortly. When that begins, you will be given the appropriate event.
     *
     * @param evt The event that caused this method to be called.
     */
    public void handshakeInProgress(ProgressEvent evt) {
    }

    /**
     * The download has started.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadStarted(ProgressEvent evt) {
    }

    /**
     * The download has updated its status.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadUpdate(ProgressEvent evt) {
        noActivity = LOAD_WAIT;
    }

    /**
     * The download has ended.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadEnded(ProgressEvent evt) {
    }

    /**
     * An error has occurred during the download.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadError(ProgressEvent evt) {
    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    private long startTime;

    /**
     * Run the contents of this thread now.
     */
    public void run() {
        startTime = System.currentTimeMillis();

        while (doRun) {
            int numScripts = 0;
            int numExternals = 0;

            if (scriptLoader != null)
                numScripts = scriptLoader.getNumberInProgress();

            if (loadManager != null)
                numExternals = loadManager.getNumberInProgress();

            int total = numExternals + numScripts;

            if (mainFileLoading) {
                universe.setMinimumFrameInterval(FRAME_CYCLE_MINIMUM_STARTUP,
                                                 false);
            } else if (total > 0) {
                if (total == lastTotal) {
                    lastTotalTimes++;

                    if (lastTotalTimes > 2000 / WAIT_TIME) {
                        if (!reportedError) {
                            reportedError = true;
                            initialLoadDone = true;
                            errorReporter.warningReport("Stuck on same queue count: scripts: " + numScripts + " textures: " + numExternals + " stopping throttle.", null);

                            universe.setMinimumFrameInterval(FRAME_CYCLE_MINIMUM_NOLOADING, false);
                        }
                    }
                } else {
                    lastTotalTimes = 0;

                    if (initialLoadDone) {
                        universe.setMinimumFrameInterval(FRAME_CYCLE_MINIMUM_LOADING, false);
                    } else {
                        universe.setMinimumFrameInterval(FRAME_CYCLE_MINIMUM_STARTUP, false);
                    }
                }

                lastTotal = total;
            } else if (noActivity > 0) {
                universe.setMinimumFrameInterval(FRAME_CYCLE_MINIMUM_LOADING,
                                                 false);
                noActivity--;
            } else {
                universe.setMinimumFrameInterval(FRAME_CYCLE_MINIMUM_NOLOADING,
                                                 false);

                if (displayLoadTime && initialLoadDone != true) {
                    System.out.println("***TotalLoadTime: " + (System.currentTimeMillis() - startTime));
                }

                initialLoadDone = true;
            }

            try {
                Thread.sleep(WAIT_TIME);
            } catch(Exception e) {
            }
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Has the initial load finished.  Somewhat subjective, but basically
     * have the items referenced in the main file finished loading.
     *
     * @return Is the initial loading finished.
     */
    public boolean isInitialLoadDone() {
        return initialLoadDone;
    }

    /**
     * Set the minimum frame cycle time when nothing is loading.
     *
     * @param val The minimum cycle time
     */
    public void setMinimumNoLoading(int val) {
        FRAME_CYCLE_MINIMUM_NOLOADING = val;
    }

    /**
     * Notify the throttle that a file load has started.  Won't be necessary
     * once the main loading use a content handler.
     */
    public void startedLoading() {
        mainFileLoading = true;
    }

    /**
     * Set the script loader to query.  Null is ok.
     *
     * @param loader The script loader
     */
    public void setScriptLoader(ScriptLoader loader) {
        scriptLoader = loader;
    }

    /**
     * Set the content load manager to query.  Null is ok.
     *
     * @param manager The content load manager
     */
    public void setLoadManager(ContentLoadManager manager) {
        loadManager = manager;
    }

    /**
     * Stop the execution of this thread.
     */
    public void shutdown() {
        doRun = false;
    }
}
