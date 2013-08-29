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
// None

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.xj3d.sai.Xj3DErrorReporter;

/**
 * An implementation of the ErrorReporter interface that interfaces between
 * our internal error handlers and the external one that we have exposed
 * through the {@link org.xj3d.sai.Xj3DErrorReporter} interface.
 * <p>
 *
 * This implementation is mostly a pass-through, but it will also, eventually,
 * do conversion of the exceptions from our internal representation to
 * something that makes sense at the SAI level.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ExternalErrorReporterAdapter implements ErrorReporter {

    /** The external adapter */
    private Xj3DErrorReporter externalReporter;

    /** The local reporter for internal usage when no external reporter */
    private ErrorReporter internalReporter;

    /**
     * Creates a instance of the adapter that will interface with the
     * given internal error reporter instance.
     */
    ExternalErrorReporterAdapter(ErrorReporter reporter) {
        setErrorReporter(reporter);
    }

    //-------------------------------------------------------------------
    // Methods defined by Xj3DBrowser
    //-------------------------------------------------------------------

    /**
     * Notification of an partial message from the system. When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    public void partialReport(String msg) {
        try {
            if(externalReporter != null)
                externalReporter.partialReport(msg);
            else
                internalReporter.partialReport(msg);
        } catch(Exception e) {
            System.out.println("Error sending partial report to output");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    public void messageReport(String msg) {
        try {
            if(externalReporter != null)
                externalReporter.messageReport(msg);
            else
                internalReporter.messageReport(msg);
        } catch(Exception e) {
            System.out.println("Error sending message report to output");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
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
        try {
            if(externalReporter != null)
                externalReporter.warningReport(msg, e);
            else
                internalReporter.warningReport(msg, e);
        } catch(Exception e2) {
            System.out.println("Error sending warning report to output");
            System.out.println(e2.getMessage());
            e2.printStackTrace();
        }
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
        try {
            if(externalReporter != null)
                externalReporter.errorReport(msg, e);
            else
                internalReporter.errorReport(msg, e);
        } catch(Exception e2) {
            System.out.println("Error sending error report to output");
            System.out.println(e2.getMessage());
            e2.printStackTrace();
        }
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
        try {
            if(externalReporter != null)
                externalReporter.fatalErrorReport(msg, e);
            else
                internalReporter.fatalErrorReport(msg, e);
        } catch(Exception e2) {
            System.out.println("Error sending fatal report to output");
            System.out.println(e2.getMessage());
            e2.printStackTrace();
        }
    }

    //-------------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------------

    /**
     * Replace the internal error reporter instance with this one. Setting
     * a null value returns to the default error reporter.
     *
     * @param reporter The new instance to use
     */
    void setErrorReporter(ErrorReporter reporter) {
        if(reporter == null)
            internalReporter = DefaultErrorReporter.getDefaultReporter();
        else
            internalReporter = reporter;
    }

    /**
     * Replace the external error reporter instance with this one. Setting
     * a null value means the adapter will return to using the internal
     * reporter that is set.
     *
     * @param reporter The new instance to use
     */
    void setErrorReporter(Xj3DErrorReporter reporter) {
        externalReporter = reporter;
    }

}
