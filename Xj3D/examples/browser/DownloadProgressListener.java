/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import javax.swing.JLabel;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URI;
import org.ietf.uri.event.ProgressEvent;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * An implemenetion of the URI progress listener for putting messages to
 * various parts of the screen.
 * <P>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class DownloadProgressListener implements ProgressListener
{
    /** The status label to put transient messages on */
    private JLabel statusLabel;

    /** Error handler for more serious messages */
    private ErrorReporter reporter;

    /**
     * Create a new listener that puts information in these diferent places.
     * Assumes that both references are non-null.
     *
     * @param status The status label to write to
     * @param rep The place for error messages
     */
    public DownloadProgressListener(JLabel status, ErrorReporter rep) {
        statusLabel = status;
        reporter = rep;
    }

    /**
     * A connection to the resource has been established. At this point, no data
     * has yet been downloaded.
     *
     * @param evt The event that caused this method to be called.
     */
    public void connectionEstablished(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
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
        statusLabel.setText(evt.getMessage());
    }

    /**
     * The download has updated its status.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadUpdate(ProgressEvent evt) {
        ResourceConnection conn = evt.getSource();
        URI uri = conn.getURI();

        StringBuffer buf = new StringBuffer(uri.toExternalForm());
        buf.append(" (");
        buf.append(evt.getValue());
        buf.append(")");

        statusLabel.setText(buf.toString());
    }

    /**
     * The download has ended.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadEnded(ProgressEvent evt) {
        String msg = evt.getMessage() + " complete";
        statusLabel.setText(msg);
        reporter.messageReport(msg);
    }

    /**
     * An error has occurred during the download.
     *
     * @param evt The event that caused this method to be called.
     */
    public void downloadError(ProgressEvent evt) {
        statusLabel.setText(evt.getMessage());
        reporter.errorReport(evt.getMessage(), null);
    }
}
