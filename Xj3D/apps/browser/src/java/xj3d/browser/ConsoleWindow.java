/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003-2005
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External imports
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.*;

import java.net.UnknownHostException;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.sav.ErrorHandler;
import org.web3d.vrml.sav.Locator;
import org.web3d.vrml.sav.VRMLParseException;

import org.web3d.browser.Xj3DConstants;
import org.web3d.browser.TextAreaOutputStream;

/**
 * A window that can act as console for error messages from the application.
 * <p>
 *
 * The window will print error messages for all the error levels and only
 * throw an exception for the fatalError.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public class ConsoleWindow extends JFrame
    implements ActionListener, ErrorHandler, ErrorReporter {

    /** The set of exceptions to ignore the stack trace for */
    private HashSet ignoredExceptionTypes;

    /** A frame to put error information in */
    private JFrame ErrorFrame;

    /** The textfield to put the error information in */
    private JTextArea errorField;

    /** Clear button to remove stuff from window */
    private JButton clearButton;

    /** Locator used for printing out line/column information */
    private Locator docLocator;

    /**
     * Create an instance of the console window.
     */
    public ConsoleWindow() {
        super("Xj3D Console");

        Container content_pane = getContentPane();

        errorField = new JTextArea(20, 60);

        JScrollPane scroller = new JScrollPane(errorField);

        content_pane.add(scroller, BorderLayout.CENTER);

        JPanel p1 = new JPanel(new FlowLayout());
        content_pane.add(p1, BorderLayout.SOUTH);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);

        p1.add(clearButton);

        setSize(600, 400);
        setLocation(80, 80);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        ignoredExceptionTypes = new HashSet();
        ignoredExceptionTypes.add(VRMLException.class);
        ignoredExceptionTypes.add(InvalidFieldException.class);
        ignoredExceptionTypes.add(InvalidFieldFormatException.class);
        ignoredExceptionTypes.add(InvalidFieldValueException.class);
        ignoredExceptionTypes.add(FileNotFoundException.class);
        ignoredExceptionTypes.add(IOException.class);
        ignoredExceptionTypes.add(UnknownHostException.class);
        ignoredExceptionTypes.add(InvalidFieldConnectionException.class);
        ignoredExceptionTypes.add(VRMLParseException.class);
        ignoredExceptionTypes.add(UnsupportedComponentException.class);
        ignoredExceptionTypes.add(UnsupportedNodeException.class);
        ignoredExceptionTypes.add(UnsupportedProfileException.class);

        messageReport("Xj3D Version: " + Xj3DConstants.VERSION + "\n");
    }

    /**
     * Redirect system messages to the console.
     */
    public void redirectSystemMessages() {
        PrintStream out = new PrintStream(new TextAreaOutputStream("System.out: ", errorField));
        System.setErr(out);
        System.setOut(out);
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
        errorField.append(buf.toString());

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
        errorField.append(msg);
        autoScroll();
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    public void messageReport(String msg) {
        StringBuffer buf = new StringBuffer(msg);
        buf.append('\n');

        errorField.append(buf.toString());
        autoScroll();
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

        errorField.append(buf.toString());
        autoScroll();
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

        errorField.append(buf.toString());

        autoScroll();
        toFront();
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

        errorField.append(buf.toString());

        autoScroll();

        toFront();
    }

    //----------------------------------------------------------
    // Methods defined by ActionListener
    //----------------------------------------------------------

    /**
     * Process the action generated from the user interface.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();

        if(src == clearButton)
            errorField.setText("");

    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
        errorField.append(buf.toString());

        autoScroll();
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
        errorField.append(buf.toString());

        autoScroll();
    }

    /**
     * Autoscroll the console window so the latest message is visible.
     */
    private void autoScroll() {
        try {
            int length = errorField.getLineCount();

            Rectangle rect = errorField.modelToView(length - 1);

            if (rect != null) {
                rect.y = length * rect.height;
                errorField.scrollRectToVisible(rect);
            }
        } catch(javax.swing.text.BadLocationException ble) {
            System.out.println("Bad location in console window");
        }

    }
}
