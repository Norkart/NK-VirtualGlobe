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

package org.web3d.x3d.jaxp;

// Standard imports
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// Application specific imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * Adapter implementation of the SAX ErrorHandler interface that communicates
 * with our X3D ErrorReporter interface.
 * <p>
 * Provides marginally better error handling that prints to the nominated
 * output stream. If no stream is provided, it prints to stdout.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class X3DErrorHandler implements ErrorHandler {

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Create a new error handler that prints to the standard System.out.
     */
    public X3DErrorHandler() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //---------------------------------------------------------------
    // Methods defined by ErrorHandler
    //---------------------------------------------------------------

    /**
     * Process a warning exception. Just prints the message out
     *
     * @param spe The exception to be processed
     * @throws SAXException Never thrown
     */
    public void warning(SAXParseException spe) throws SAXException
    {
        String message = getParseExceptionInfo(spe);
        errorReporter.warningReport(message, spe);
    }

    /**
     * Process a non-fatal error exception. Prints the message out and
     * re-throws the exception.
     *
     * @param spe The exception to be processed
     * @throws SAXException A wrapped version of the original exception
     */
    public void error(SAXParseException spe) throws SAXException
    {
        String message = getParseExceptionInfo(spe);
        errorReporter.errorReport(message, spe);
        throw new SAXException(message);
    }

    /**
     * Process a non-fatal error exception. Prints the message out and
     * re-throws the exception.
     *
     * @param spe The exception to be processed
     * @throws SAXException A wrapped version of the original exception
     */
    public void fatalError(SAXParseException spe) throws SAXException
    {
        String message = getParseExceptionInfo(spe);
        errorReporter.fatalErrorReport(message, spe);
        throw new SAXException(message);
    }

    //----------------------------------------------------------
    // Local convenience Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Returns a string describing parse exception details
     *
     * @param spe The exception to extract information from
     * @return A string with formatted information
     */
    private String getParseExceptionInfo(SAXParseException spe) {
        String systemId = spe.getSystemId();
        if (systemId == null) {
            systemId = "null";
        }

        String info = "URI=" + systemId +
            " Line=" + spe.getLineNumber() +
            ": " + spe.getMessage();
        return info;
    }
}
