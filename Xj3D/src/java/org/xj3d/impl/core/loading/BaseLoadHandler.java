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
import java.io.FileNotFoundException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import org.ietf.uri.ResourceConnection;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * A abstract implementation of loader thread for common functionality between
 * all loader types.
 * <p>
 *
 * The content loader is used to wait on a queue of available content and
 * load the next available item in the queue.
 * <p>
 *
 * When loading, the content loader loads the complete file, it ignores any
 * reference part of the URI. This allows for better caching.
 *
 * The loader is used to
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
abstract class BaseLoadHandler {

    /** Flag indicating that the current load should be terminated */
    protected boolean terminateCurrent;

    /** The current resource connection if being used */
    protected ResourceConnection currentConnection;

    /**
     * Create a content loader that reads values from the given queue and
     * stores intermediate results in the given map. This does not start the
     * thread, that is the responsibility of the derived class
     *
     * @param tg The thread group to put this thread in
     * @param pending The list holding pending items to process
     * @param processing The map of items currently processing
     */
    BaseLoadHandler() {
        terminateCurrent = false;
    }

    //----------------------------------------------------------
    // Methods defined by LoadRequestHandler
    //----------------------------------------------------------

    /**
     * Notification to abort loading the current resource. If there is one
     * loading, it will terminate the procedure immediately and start fetching
     * the next available URI. This will only work if we are currently
     * processing a file. If we are not processing a file then this is
     * ignored.
     */
    public void abortCurrentFile() {
        terminateCurrent = true;

        // The drastic way of killing the connection. If they are in the
        // middle of reading a really large file, this is the only way that
        // we can get a quick response - grab the underlying stream and shut
        // it down.
        if(currentConnection != null)
            currentConnection.close();
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

        if(currentConnection != null)
            currentConnection.close();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Connect to the current resource connection. Encapsulates all of the
     * details and privilege handling to do this safely. If it can't connect
     * then it will barf. It assumes the class var resourceConnection is
     * valid.
     *
     * @param reporter The errorReporter to send all messages to
     * @return true if the connection was successfully made
     */
    protected boolean makeConnection(ErrorReporter reporter) {

        boolean ret_val = true;

        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run()  throws IOException {
                        currentConnection.connect();
                        return null;
                    }
                }
            );
        } catch(PrivilegedActionException pae) {
            ret_val = false;
            Exception e = pae.getException();

            if (e instanceof FileNotFoundException) {
                String msg = "File not found: " + currentConnection.getURI();
                reporter.warningReport(msg, null);
            } else {
                String msg = "IO Error reading external file " +
                         currentConnection.getURI();

                reporter.warningReport(msg, pae.getException());
            }
        }

        return ret_val;
    }
}
