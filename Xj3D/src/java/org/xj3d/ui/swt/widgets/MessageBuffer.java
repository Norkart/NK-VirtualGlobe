/*****************************************************************************
 *                        Web3d.org Copyright(c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.widgets;

// External imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.UnknownHostException;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;

import org.web3d.browser.Xj3DConstants;


import org.web3d.vrml.sav.ErrorHandler;
import org.web3d.vrml.sav.Locator;
import org.web3d.vrml.sav.VRMLParseException;

/**
 * A buffer for holding messages and a runnable instance for
 * flushing the messages to the console Text widgets on the
 * display thread.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class MessageBuffer implements Runnable, ErrorHandler {

    /** The version message prefix text */
    private static final String VERSION_MESSAGE_PREFIX = "Xj3D Version: ";

    /** a buffer for accumulating messages */
    private ArrayList buffer;

    /** an index into the buffer of the last displayed message */
    private int index;

    /** The set of exceptions to ignore the stack trace for */
    private HashSet ignoredExceptionTypes;

    /** Locator used for printing out line/column information */
    private Locator docLocator;

    /** The Text widget displaying the messages */
    private Text text;

    /** The display object */
    private Display display;

    /**
     * Constructor
     */
    public MessageBuffer() {

        buffer = new ArrayList();

        messageReport(VERSION_MESSAGE_PREFIX + Xj3DConstants.VERSION + "\n");

        ignoredExceptionTypes = new HashSet();
        ignoredExceptionTypes.add(InvalidFieldException.class);
        ignoredExceptionTypes.add(InvalidFieldFormatException.class);
        ignoredExceptionTypes.add(InvalidFieldValueException.class);
        ignoredExceptionTypes.add(FileNotFoundException.class);
        ignoredExceptionTypes.add(IOException.class);
        ignoredExceptionTypes.add(UnknownHostException.class);
        ignoredExceptionTypes.add(IllegalArgumentException.class);
        ignoredExceptionTypes.add(InvalidFieldConnectionException.class);
        ignoredExceptionTypes.add(VRMLParseException.class);
        ignoredExceptionTypes.add(UnsupportedComponentException.class);
        ignoredExceptionTypes.add(UnsupportedNodeException.class);
        ignoredExceptionTypes.add(UnsupportedProfileException.class);
        ignoredExceptionTypes.add(ClassNotFoundException.class);
    }

    //----------------------------------------------------------
    // Methods defined by ErrorHandler
    //----------------------------------------------------------

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <CODE>startDocument()</CODE> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    public void setDocumentLocator(Locator loc) {
        docLocator = loc;
    }

    /**
     * Notification of a warning in the way the code has been handled. The
     * parser will continue through the file after this. Throw another
     * exception if we want the parser to halt as a result.
     *
     * @param vpe The exception that caused this warning
     * @throws VRMLException Create a further warning condition
     */
    public void warning(VRMLException vpe) throws VRMLException {
        warning(vpe.getMessage(), true);
    }

    /**
     * Notification of a recoverable error in the parsing. The parser will
     * continue to keep parsing after this error. Throw another exception if
     * we really want the parser to stop at this point.
     *
     * @param vpe The exception that caused this warning
     * @throws VRMLException Create a further warning condition
     */
    public void error(VRMLException vpe) throws VRMLException {
        error(vpe.getMessage(), true);
    }

    /**
     * Notification of a non-recoverable error. The parser will not continue
     * after calling this method. Throw another exception if we really want
     * to make note of this, the parser will stop anyway.
     *
     * @param vpe The exception that caused this warning
     * @throws VRMLException Create a further warning condition
     */
    public void fatalError(VRMLException vpe) throws VRMLException {
        StringBuffer buf = new StringBuffer("Fatal Error: ");
        buf.append(" Line: ");
        buf.append(docLocator.getLineNumber());
        buf.append(" Column: ");
        buf.append(docLocator.getColumnNumber());
        buf.append(" ");
        buf.append(vpe.getMessage());
        buf.append("\n");
        append(buf.toString());

        throw vpe;
    }

    //----------------------------------------------------------
    // Methods defined by ErrorReporter
    //----------------------------------------------------------

    /**
     * Notification of an partial message from the system. When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    public void partialReport(String msg) {
        append(msg);
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    public void messageReport(String msg) {
        StringBuffer buf = new StringBuffer("Message: ");
        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        append(buf.toString());
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
        StringBuffer buf = new StringBuffer("Warning: ");

        if(e instanceof FieldException) {
            FieldException fe =(FieldException)e;

            String name = fe.getFieldName();
            if(name != null) {
                buf.append("Field name: ");
                buf.append(name);
            }
        }

        if(e instanceof VRMLParseException) {
            buf.append(" Line: ");
            buf.append(((VRMLParseException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((VRMLParseException)e).getColumnNumber());
            buf.append('\n');
        } else if(e instanceof InvalidFieldFormatException) {
            buf.append(" Line: ");
            buf.append(((InvalidFieldFormatException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((InvalidFieldFormatException)e).getColumnNumber());
            buf.append('\n');
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String txt = e.getMessage();
            if(txt == null)
                txt = e.getClass().getName();

            buf.append(txt);
            buf.append('\n');

            if(!ignoredExceptionTypes.contains(e.getClass())) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                buf.append(sw.toString());
            }
        }

        append(buf.toString());
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
        StringBuffer buf = new StringBuffer("Error: ");

        if(e instanceof FieldException) {
            FieldException fe =(FieldException)e;

            String name = fe.getFieldName();
            if(name != null) {
                buf.append("Field name: ");
                buf.append(name);
            }
        }

        if(e instanceof VRMLParseException) {
            buf.append(" Line: ");
            buf.append(((VRMLParseException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((VRMLParseException)e).getColumnNumber());
            buf.append('\n');
        } else if(e instanceof InvalidFieldFormatException) {
            buf.append(" Line: ");
            buf.append(((InvalidFieldFormatException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((InvalidFieldFormatException)e).getColumnNumber());
            buf.append('\n');
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String txt = e.getMessage();
            if(txt == null)
                txt = e.getClass().getName();

            buf.append(txt);
            buf.append('\n');

            if(!ignoredExceptionTypes.contains(e.getClass())) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                buf.append(sw.toString());
            }
        }

        append(buf.toString());
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
        StringBuffer buf = new StringBuffer("Fatal Error: ");

        if(e instanceof FieldException) {
            FieldException fe =(FieldException)e;

            String name = fe.getFieldName();
            if(name != null) {
                buf.append("Field name: ");
                buf.append(name);
            }
        }

        if(e instanceof VRMLParseException) {
            buf.append(" Line: ");
            buf.append(((VRMLParseException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((VRMLParseException)e).getColumnNumber());
            buf.append('\n');
        } else if(e instanceof InvalidFieldFormatException) {
            buf.append(" Line: ");
            buf.append(((InvalidFieldFormatException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((InvalidFieldFormatException)e).getColumnNumber());
            buf.append('\n');
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String txt = e.getMessage();
            if(txt == null)
                txt = e.getClass().getName();

            buf.append(txt);
            buf.append('\n');

            if(!ignoredExceptionTypes.contains(e.getClass())) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                buf.append(sw.toString());
            }
        }

        append(buf.toString());
    }

    //----------------------------------------------------------
    // Methods defined by Runnable
    //----------------------------------------------------------

    /** Called on the display thread to flush the buffered
     *  messages to the text widget */
    public void run() {
        if(text != null) {
            synchronized(buffer) {
                while(buffer.size() > index) {
                    text.append((String)buffer.get(index++));
                }
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Append the argument message to the buffer
     * @param message - The message
     */
    public void append(String message) {
        if(message != null) {
            synchronized(buffer) {
                buffer.add(message);
                if(display != null) {
                    display.asyncExec(this);
                }
            }
        }
    }

    /**
     * Clear the buffer
     */
    public void clear() {
        synchronized(buffer) {
            buffer.clear();
            index = 0;
        }
    }

    /**
     * Set the text widget to use for displaying the messages.
     * @param text - The Text widget displaying the messages
     */
    public void setTextWidget(Text text) {
        this.text = text;
        if(text == null) {
            display = null;
        } else {
            display = text.getDisplay();
        }
        index = 0;
    }

    /**
     * Redirect system messages to the console.
     */
    public void redirectSystemMessages() {
        //PrintStream out =
        //    new PrintStream(new SwingTextAreaOutputStream("System.out: ",
        //                                                 errorField));
        //System.setOut(out);
        //
        //PrintStream err =
        //    new PrintStream(new SwingTextAreaOutputStream("System.err: ",
        //                                                  errorField));
        //System.setErr(out);
    }

    /**
     * Send a warning message to the screen with the option of using the
     * docLocator to present line number info.The string will have the prefix
     * "Warning:" prepended to it.
     *
     * @param msg The message to be written.
     * @param useLocator true if line number information should be used
     */
    private void warning(String msg, boolean useLocator) {

        StringBuffer buf = new StringBuffer("Warning: ");

        if(useLocator) {
            buf.append(" Line: ");
            buf.append(docLocator.getLineNumber());
            buf.append(" Column: ");
            buf.append(docLocator.getColumnNumber());
            buf.append(" ");
        }

        buf.append(msg);
        buf.append("\n");
        append(buf.toString());
    }

    /**
     * Send an error message to the screen with the option of using the
     * docLocator to present line number info.The string will have the prefix
     * "Error:" prepended to it.
     *
     * @param msg The message to be written
     * @param useLocator true if line number information should be used
     */
    private void error(String msg, boolean useLocator) {

        StringBuffer buf = new StringBuffer("Error: ");

        if(useLocator) {
            buf.append(" Line: ");
            buf.append(docLocator.getLineNumber());
            buf.append(" Column: ");
            buf.append(docLocator.getColumnNumber());
            buf.append(" ");
        }

        buf.append(msg);
        buf.append("\n");
        append(buf.toString());
    }
}

