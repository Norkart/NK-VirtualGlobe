/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;
import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.ProfilingInfo;
import org.web3d.browser.ProfilingListener;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.sai.Xj3DStatusListener;

/**
 * An adapter class for providing browser internal info and state through
 * the <code>Xj3DStatusListener</code> interface.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class StatusAdapter implements ProfilingListener, ProgressListener {
	
    /** Load started progress message */
    private static final String LOAD_STARTED_MSG =
        "Load Started";

    /** Load started progress message */
    private static final String LOADING_MSG =
        "Loading";

    /** Load complete progress message */
    private static final String LOAD_COMPLETE_MSG =
        "Load Complete";

    /** The implementation of the browser core */
    private BrowserCore browserCore;

    /** The ErrorReporter to send errors and warnings to. */
    private ErrorReporter errorReporter;

    /** Multicaster for status event listeners. */
    private Xj3DStatusListener statusListener;

	/**
	 * Constructor
	 *
     * @param browserCore The BrowserCore to use as the implementation.
	 */
	StatusAdapter(BrowserCore core, ErrorReporter reporter) {
		browserCore = core;
		errorReporter = (reporter == null) ? 
			DefaultErrorReporter.getDefaultReporter() : reporter;
		core.addProfilingListener(this);
		ResourceConnection.addGlobalProgressListener(this);
	}
	
    /**
     * Add a listener for status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addStatusListener(Xj3DStatusListener l) {
        statusListener = StatusListenerMulticaster.add(statusListener, l);
    }

    /**
     * Remove a listener for status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeStatusListener(Xj3DStatusListener l) {
        statusListener = StatusListenerMulticaster.remove(statusListener, l);
    }
	
    //-------------------------------------------------------------------
    // Methods defined by ProfilingListener
    //-------------------------------------------------------------------

	/**
     * The profiling data has changed. This will happen at the end of each
	 * frame render.
     *
     * @param data The profiling data
     */
    public void profilingDataChanged(ProfilingInfo data) {
		// at present, the data being delivered in ProfilingInfo is....
		// incomplete. presumably we want to use that eventually, till
		// then however, return the core's notion of the frame rate.
		statusListener.updateFramesPerSecond(browserCore.getCurrentFrameRate());
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
		
        statusListener.updateStatusMessage(evt.getMessage());
    }

    /**
     * The header information reading and handshaking is taking place. Reading
     * and intepreting of the data (a download started event) should commence
     * shortly. When that begins, you will be given the appropriate event.
     *
     * @param evt The event that caused this method to be called.
     */
    public void handshakeInProgress(ProgressEvent evt) {
		
        statusListener.updateStatusMessage(evt.getMessage());
    }

    /**
     * The download has started.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadStarted(ProgressEvent evt) {
		
        ResourceConnection conn = evt.getSource();
        if (conn != null) {
            URI uri = conn.getURI();
            String url = uri.toExternalForm();
			statusListener.progressUpdate(url, LOAD_STARTED_MSG, 0);
		}
        statusListener.updateStatusMessage(evt.getMessage());
    }

    /**
     * The download has updated its status.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadUpdate(ProgressEvent evt) {
		
        ResourceConnection conn = evt.getSource();
        if (conn != null) {
            String url = conn.getURI().toExternalForm();
			if (isFile(conn)) {
				// it has been observed that getContentLength() does not
				// return useful information for network connections.
				// therefore, progress updates are only logged for local files
				float total = (float)conn.getContentLength();
				float progress = (float)evt.getValue();
				float perc = 100 * progress/total;
				statusListener.progressUpdate(url, LOADING_MSG, perc);
			}
        } 
    }

    /**
     * The download has ended.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadEnded(ProgressEvent evt) {
		
        ResourceConnection conn = evt.getSource();
        if (conn != null) {
			String url = conn.getURI().toExternalForm();
            String msg = url + " complete.";
            statusListener.updateStatusMessage(msg);
			statusListener.progressUpdate(url, LOAD_COMPLETE_MSG, 100);
            errorReporter.messageReport(msg);
        }
    }

    /**
     * An error has occurred during the download.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadError(ProgressEvent evt) {
        statusListener.updateStatusMessage(evt.getMessage());
        errorReporter.errorReport(evt.getMessage(), null);
    }

    //-------------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------------

    /**
     * Set the handler for error messages.
     *
     * @param reporter The error reporter instance to use
     */
    public void setErrorReporter(ErrorReporter reporter) {
		errorReporter = (reporter == null) ? 
			DefaultErrorReporter.getDefaultReporter() : reporter;
    }
	
	/**
	 * Shutdown and release resources
	 */
	public void shutdown() {
		ResourceConnection.removeGlobalProgressListener(this);
	}
	
	/**
	 * Check whether the connection represents a local file
	 *
	 * @param conn The connection 
	 * @return true if the connection is to a local file, false otherwise.
	 */
	private boolean isFile(ResourceConnection conn) {
		boolean isFile = false;
		if (conn != null) {
        	isFile = ((URI.getScheme(conn.getURI().toExternalForm()).equals(URI.FILE_SCHEME)));
		}
		return(isFile);
	}
}
