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

package org.web3d.util;

// External imports
// None

// Local imports
// None

/**
 * An implementation of the ErrorReporter interface that just writes everything
 * to System.out.
 * <p>
 *
 * The default implementation to be used as convenience code for when the end
 * user has not supplied their own instance.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class DefaultErrorReporter implements ErrorReporter {

    /** Global singleton instance. */
    private static DefaultErrorReporter instance;

    /**
     * Creates a new, default instance of the reporter
     */
    public DefaultErrorReporter() {
    }

    /**
     * Fetch the common global instance of the reporter.
     *
     * @return The global instance
     */
    public static ErrorReporter getDefaultReporter() {
        if(instance == null)
            instance = new DefaultErrorReporter();

        return instance;
    }

    /**
     * Notification of an partial message from the system. When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    public void partialReport(String msg) {
        System.out.print(msg);
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    public void messageReport(String msg) {
        System.out.print("Message: ");
        System.out.println(msg);
    }

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void warningReport(String msg, Exception e) {
        System.out.print("Warning: ");
        System.out.println(msg);

        if(e != null)
            e.printStackTrace();
    }

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to add a route to a non-existent node or the
     * use of a node that the system cannot find the definition of.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void errorReport(String msg, Exception e) {
        System.out.print("Error: ");
        System.out.println(msg);

        if(e != null)
            e.printStackTrace();
    }

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you recieve this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void fatalErrorReport(String msg, Exception e) {
        System.out.print("Fatal Error: ");
        System.out.println(msg);

        if(e != null)
            e.printStackTrace();
    }
}
