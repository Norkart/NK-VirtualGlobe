/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.view;

// External imports
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.action.IStatusLineManager;

import org.eclipse.swt.widgets.Display;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;

import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * An implementation of the URI progress listener for putting messages 
 * regarding the load status of a scene to the Eclipse status line and/or
 * to the error reporter as necessary.
 * <p>
 * The listener utilizes the Eclipse ProgressMonitor on the status line
 * whenever it is possible to determine file sizes and obtain updates on
 * the loading process. The status line must be enabled for this to function.
 * The status line may be enabled in the application's WorkbenchWindowAdvisor
 * subclass with the following:
 *
 * <blockquote>
 * <pre>
 *
 * public void preWindowOpen() {
 *		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
 *		configurer.setShowProgressIndicator(true); 
 *	}
 *
 * </pre>
 * </blockquote>
 * <p>
 * Additionally, the progress monitor provides an icon which will load
 * a View of the loading progess along with a cancel button. This view
 * may be included with the following addition to the plugin.xml file:
 *
 * <blockquote>
 * <pre>
 *
 * <extension point="org.eclipse.ui.views">
 *    <view
 *          class="org.eclipse.ui.ExtensionFactory:progressView"
 *          id="org.eclipse.ui.views.ProgressView"
 *          name="Progress View"/>
 * </extension>
 *
 * </pre>
 * </blockquote>
 * 
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class LoadProgressListener extends Job implements ProgressListener, Runnable {
    
    /** The progress monitor Job title */
    private static final String JOB_TITLE = "Loading";
    
    /** Main file loading in progress message */
    private static final String MAIN_FILE_LOAD_MSG = "Main file downloading";
    
    /** Main file loading complete message */
    private static final String MAIN_FILE_LOAD_COMPLETE_MSG = "Main file complete";
    
    /** The error reporter for logging messages */
    private ErrorReporter reporter;
    
    /** The status manager for reporting messages */
    private IStatusLineManager status;
    
    /** The last status message */
    private String msg;
    
    /** The current progress monitor associated with the Job */
    private IProgressMonitor monitor;
    
    /** The total size of the current content item being loaded */
    private int contentSize;
    
    /** The size of the current content item loaded so far */
    private int currentSize;
    
    /** The last 'published' size of the current content item */
    private int lastReportedSize;
    
    /** Flag indicating that a load is in progress */
    private boolean loadInProgress;
    
    /** Flag indicating that the main file is being loaded */
    private boolean loadingMainFile;
    
    /** The load status of the current content item */
    private IStatus loadStatus;
    
    /** The display object */
    private Display display;
    
    /**
     * Create a new listener that puts information regarding the load process
     * into the status line and error logger.
     * Assumes that all arguments are non-null.
     * 
     * @param display - The display object, used for updating the status bar
     * on the display thread.
     * @param slm - The status line manager instance
     * @param rep - The place for error messages
     */
    public LoadProgressListener(Display display, IStatusLineManager slm, ErrorReporter rep) {
        super(JOB_TITLE);
        this.display = display;
        status = slm;
        reporter = rep;
    }
    
    // ---------------------------------------------------------------
    // Methods defined by ProgressListener
    // ---------------------------------------------------------------
    
    /**
     * A connection to the resource has been established. At this point, no data
     * has yet been downloaded.
     * 
     * @param evt - The event that caused this method to be called.
     */
    public void connectionEstablished(ProgressEvent evt) {
        
        ResourceConnection conn = evt.getSource();
        if ( (URI.getScheme( conn.getURI( ).toExternalForm( )).equals( URI.FILE_SCHEME ) ) ) {
            
            // if the connection is to a local file, then we can safely request
            // it's length. requesting the length on an http connection may
            // cause an infinite recursive loop - re-establishing a connection
            // to get the file header - which generates this event - adnauseum...
            
            loadInProgress = true;
            contentSize = conn.getContentLength();
            currentSize = 0;
        } else {
            contentSize = -1;
        }
        msg = evt.getMessage();
        display.asyncExec(this);
    }
    
    /**
     * The header information reading and handshaking is taking place. Reading
     * and intepreting of the data (a download started event) should commence
     * shortly. When that begins, you will be given the appropriate event.
     * 
     * @param evt - The event that caused this method to be called.
     */
    public void handshakeInProgress(ProgressEvent evt) {
        msg = evt.getMessage();
        display.asyncExec(this);
    }
    
    /**
     * The download has started.
     * 
     * @param evt - The event that caused this method to be called.
     */
    public void downloadStarted(ProgressEvent evt) {
        
        ResourceConnection conn = evt.getSource();
        loadInProgress = true;
        contentSize = conn.getContentLength();
        currentSize = 0;
        schedule();
        
        msg = evt.getMessage();
        display.asyncExec(this);
    }
    
    /**
     * The download has updated its status.
     * 
     * @param evt - The event that caused this method to be called.
     */
    public void downloadUpdate(ProgressEvent evt) {
        
        ResourceConnection conn = evt.getSource();
        if (conn != null) {
            if (monitor != null) {
                currentSize = evt.getValue();
                wakeUp();
            } else {
                loadInProgress = true;
                contentSize = conn.getContentLength();
                currentSize = evt.getValue();
                schedule();
            }
            URI uri = conn.getURI();
            msg = uri.toExternalForm();
            display.asyncExec(this);
            
        } else {
            
            // apparently the only way we can get here is if the 
            // 'main file' is being loaded
            
            if ( loadingMainFile ) {
                if (monitor != null) {
                    currentSize = evt.getValue();
                    wakeUp(); 
                }
            } else {
                loadingMainFile = true;
                // TODO: Right now we don't know the filename
                msg = MAIN_FILE_LOAD_MSG;
                display.asyncExec(this);
                if ( contentSize != -1 ) {
                    currentSize = evt.getValue();
                    schedule( );
                }
            }
        }
    }
    
    /**
     * The download has ended.
     * 
     * @param evt - The event that caused this method to be called.
     */
    public void downloadEnded(ProgressEvent evt) {
        if (monitor != null) {
            loadInProgress = false;
            loadStatus = Status.OK_STATUS;
            wakeUp();
        }
        
        ResourceConnection conn = evt.getSource();
        if (conn != null) {
            URI uri = conn.getURI();
            msg = uri.toExternalForm() + " complete.";
            reporter.messageReport(msg);
            display.asyncExec(this);
        } else {
            loadingMainFile = false;
            msg = MAIN_FILE_LOAD_COMPLETE_MSG;
            display.asyncExec(this);
        }
    }
    
    /**
     * An error has occurred during the download.
     * 
     * @param evt - The event that caused this method to be called.
     */
    public void downloadError(ProgressEvent evt) {
        if (monitor != null) {
            loadInProgress = false;
            // ? should there be a different status ?
            loadStatus = Status.OK_STATUS;
            wakeUp();
        }
        msg = evt.getMessage();
        reporter.errorReport(msg, null);
        display.asyncExec(this);
    }
    
    // ---------------------------------------------------------------
    // Methods overridden in Job
    // ---------------------------------------------------------------
    
    /**
     * The Job for updating the progress monitor.
     *
     * @param progMon - The progress monitor instance to inform of our
     * status.
     */
    protected IStatus run(IProgressMonitor progMon) {
        monitor = progMon;
        lastReportedSize = 0;
        monitor.beginTask(JOB_TITLE, contentSize);
        while (loadInProgress) {
            if (monitor.isCanceled()) {
                return( Status.CANCEL_STATUS );
            }
            if (currentSize > lastReportedSize) {
                monitor.worked(currentSize - lastReportedSize);
                lastReportedSize = currentSize;
            }
            LoadProgressListener.this.sleep(250);
        }
        monitor.done();
        monitor = null;
        return(loadStatus);
    }
    
    // ---------------------------------------------------------
    // Methods defined by Runnable
    // ---------------------------------------------------------
    
    /**
     * Update the status message on the display thread.
     */
    public void run() {
        status.setMessage(msg);
    }
    
    /**
     * Put the calling thread to sleep for the requested time period
     *
     * @param millis - The number of milliseconds for this thread to sleep.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
