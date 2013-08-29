/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript;

// Standard imports
import org.mozilla.javascript.EvaluatorException;

// Application specific imports
import org.web3d.util.ErrorReporter;

/**
 * An adapter between the Rhino reporting system and the system reporting
 * mechanisms provided by this codebase.
 * <p>
 *
 * If no system reporter is registered, the adapter will write the message
 * to <code>System.out</code>. Currently the messages are giving line numbers
 * according to the script they came from. It would be a nice thing to have
 * this generate line numbers according to the source VRML file so that it
 * makes locating the script error much easier for the end user.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class ReportAdapter implements org.mozilla.javascript.ErrorReporter {

    /** Who we munge the messages for and pass details on to */
    private ErrorReporter upstreamReporter;

    /**
     * Empty, default constructor.
     */
    ReportAdapter() {
    }

    //----------------------------------------------------------
    // Methods defined by the Rhino ErrorReporter interface
    //----------------------------------------------------------

    /**
     * Report a warning.
     *
     * @param message a String describing the warning
     * @param sourceName a String describing the JavaScript source
     *     where the warning occured; typically a filename or URL
     * @param line the line number associated with the warning
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     */
    public void warning(String message,
                        String sourceName,
                        int line,
                        String lineSource,
                        int lineOffset) {
        String msg =
            formatMessage(message, sourceName, line, lineSource, lineOffset);

        if(upstreamReporter != null)
            upstreamReporter.warningReport(msg, null);
        else
            System.out.println("ECMA.Warning: " + msg);
    }

    /**
     * Report an error. If execution has not yet begun, the JavaScript
     * engine is free to find additional errors rather than terminating
     * the translation. It will not execute a script that had errors, however.
     *
     * @param message a String describing the error
     * @param sourceName a String describing the JavaScript source
     *     where the error occured; typically a filename or URL
     * @param line the line number associated with the error
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     */
    public void error(String message,
                      String sourceName,
                      int line,
                      String lineSource,
                      int lineOffset) {
        String msg =
            formatMessage(message, sourceName, line, lineSource, lineOffset);

        if(upstreamReporter != null)
            upstreamReporter.errorReport(msg, null);
        else
            System.out.println("ECMA.Error: " + msg);
    }

    /**
     * Creates an EvaluatorException that may be thrown.
     * runtimeErrors, unlike errors, will always terminate the
     * current script.
     *
     * @param message a String describing the error
     * @param sourceName a String describing the JavaScript source
     *     where the error occured; typically a filename or URL
     * @param line the line number associated with the error
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     * @return an EvaluatorException that will be thrown.
     */
    public EvaluatorException runtimeError(String message,
                                           String sourceName,
                                           int line,
                                           String lineSource,
                                           int lineOffset) {
        String msg =
            formatMessage(message, sourceName, line, lineSource, lineOffset);

        if(upstreamReporter != null)
            upstreamReporter.errorReport(msg, null);
        else
            System.out.println("ECMA.RuntimeError: " + msg);

        return new EvaluatorException(msg);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the system reporter used to pass messages to.
     *
     * @param rep The new reporter to use
     */
    void setReporter(ErrorReporter rep) {
        upstreamReporter = rep;
    }

    /**
     * Get the current system reporter
     *
     * @return The reporter in use
     */
    ErrorReporter getErrorReporter() {
        return upstreamReporter;
    }

    /**
     * Convenience method to format the message into a string.
     *
     * @param message a String describing the error
     * @param sourceName a String describing the JavaScript source
     *     where the error occured; typically a filename or URL
     * @param line the line number associated with the error
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     * @return The formatted message string.
     */
    private String formatMessage(String message,
                                 String sourceName,
                                 int line,
                                 String lineSource,
                                 int lineOffset) {
        StringBuffer buf = new StringBuffer();
        buf.append("ECMAScript evaluation error on line ");
        buf.append(line);
        buf.append(" column ");
        buf.append(lineOffset);

        if(lineSource != null) {
            buf.append("\n\"");
            buf.append(lineSource);
            buf.append('\"');
        }

        buf.append("\n");
        buf.append(message);
        buf.append("\nThe script came from: ");
        buf.append(sourceName);

        return buf.toString();
    }
}
