/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.Container;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;
import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * An implemenetion of the URI progress listener for putting messages to
 * a status label.
 * <P>
 *
 * @author  Justin Couch
 * @version $Revision: 1.4 $
 */
public class SwingProgressListener implements ProgressListener {

    /** Main file loading in progress message */
    private static final String MAIN_FILE_LOAD_MSG =
        "Main file downloading";

    /** Main file loading complete message */
    private static final String MAIN_FILE_LOAD_COMPLETE_MSG =
        "Main file complete";

    /** The status label to put transient messages on */
    private JLabel statusLabel;

    /** Error handler for more serious messages */
    private ErrorReporter reporter;

    /** A progress bar for main file loading */
    private JProgressBar progressBar;

    /** The size of the main file */
    private int mainSize;

    /** The container for the progress bar */
    private Container progressPanel;

    /** The layout location for the progress bar */
    private String progressLocation;

    /** Flag indicating that the current connection is to a local file */
    private boolean currentConnectionIsFile;

    /** Flag indicating that the main file is being loaded */
    private boolean loadingMainFile;

    /** Flag indicating that the orogress bar is active */
    private boolean progressBarIsActive;

    /**
     * Create a new listener that puts information in these diferent places.
     * Assumes that both references are non-null.
     *
     * @param status The status label to write to
     * @param rep The place for error messages
     */
    public SwingProgressListener(JLabel status, JProgressBar progress, Container panel,
        String location, ErrorReporter rep) {
        statusLabel = status;
        reporter = rep;
        progressBar = progress;
        progressPanel = panel;
        progressLocation = location;
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
        statusLabel.setText(evt.getMessage());

        ResourceConnection conn = evt.getSource();
        if ((URI.getScheme(conn.getURI().toExternalForm()).equals(URI.FILE_SCHEME))) {

            // if the connection is to a local file, then we can safely request
            // it's length. requesting the length on an http connection may
            // cause an infinite recursive loop - re-establishing a connection
            // to get the file header - which generates this event - adnauseum...

            currentConnectionIsFile = true;
            mainSize = (int)conn.getContentLength();
            progressBar.setMaximum(mainSize);

        } else {
            currentConnectionIsFile = false;
        }
    }

    /**
     * The header information reading and handshaking is taking place. Reading
     * and intepreting of the data (a download started event) should commence
     * shortly. When that begins, you will be given the appropriate event.
     *
     * @param evt The event that caused this method to be called.
     */
    public void handshakeInProgress(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
    }

    /**
     * The download has started.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadStarted(ProgressEvent evt) {
        ResourceConnection conn = evt.getSource();
        int maxSize = conn.getContentLength();

        progressBar.setMaximum(maxSize);

        statusLabel.setText(evt.getMessage());
    }

    /**
     * The download has updated its status.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadUpdate(ProgressEvent evt) {
        ResourceConnection conn = evt.getSource();

        String url = null;
        if (conn != null) {
            URI uri = conn.getURI();
            url = uri.toExternalForm();

            //StringBuffer buf = new StringBuffer(url);
            //buf.append(" (");
            //buf.append(evt.getValue());
            //buf.append(")");

            //statusLabel.setText(buf.toString());
            statusLabel.setText(url);

            if ( progressBarIsActive ) {
                progressBar.setValue(evt.getValue());
            } else {

                // this code path is depending upon the assumption
                // that a downloadStarted event has occured and that
                // the progress bar extent has been set there.

                addProgressBar();
                progressBarIsActive = true;
                progressBar.setValue(evt.getValue());
            }
        } else {

            // apparently the only way we can get here is if the
            // 'main file' is being loaded

            if ( loadingMainFile ) {
                if ( progressBarIsActive ) {
                    progressBar.setValue(evt.getValue());
                }
            } else {
                loadingMainFile = true;
                // TODO: Right now we don't know the filename
                statusLabel.setText(MAIN_FILE_LOAD_MSG);
                if ( currentConnectionIsFile ) {
                    addProgressBar();
                    progressBarIsActive = true;
                    progressBar.setValue(evt.getValue());
                }
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
            URI uri = conn.getURI();

            String msg = uri.toExternalForm() + " complete.";
            /*
            String msg2 = evt.getMessage();
            if (msg2 != null)
            msg = msg + msg2;
            */
            statusLabel.setText(msg);
            reporter.messageReport(msg);
        } else {
            statusLabel.setText(MAIN_FILE_LOAD_COMPLETE_MSG);
            loadingMainFile = false;
        }
        if ( progressBarIsActive ) {
            progressBarIsActive = false;
            removeProgressBar();
        }
    }

    /**
     * An error has occurred during the download.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadError(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
        reporter.errorReport(evt.getMessage(), null);

        if ( progressBarIsActive ) {
            progressBarIsActive = false;
            removeProgressBar();
        }
    }

    /**
     * Add the progress bar into the status bar.
     */
    private void addProgressBar() {
        progressPanel.add(progressBar, progressLocation);
    }

    /**
     * Remove the progress bar from the status bar.
     */
    private void removeProgressBar() {
        //progressPanel.remove(progressBar);

        progressBar.setMaximum(1);
        progressBar.setValue(1);
    }
}
