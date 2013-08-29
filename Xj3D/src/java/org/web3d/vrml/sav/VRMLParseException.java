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

package org.web3d.vrml.sav;

// Standard imports
// none

// Application specific imports
// none

/**
 * An exception that represents a parsing error in a VRML stream.
 * <p>
 * This is usually due to some form or syntax error. If the parser supports
 * locators then this will also include line and column information. If they
 * are not supported this will have values of -1 for unknown values.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class VRMLParseException extends SAVException {

    /** The line number the exception occurred on */
    private int line;

    /** The column number the exception occurred on */
    private int column;

    /**
     * Create an exception that says an error occurred on the given line and
     * column numbers.
     *
     * @param line The line number the error occurred on
     * @param col The column number the error occurred on
     */
    public VRMLParseException(int line, int col) {
        this.line = line;
        this.column = col;
    }

    /**
     * Create an exception that says an error occurred on the given line and
     * column numbers and the given message.
     *
     * @param line The line number the error occurred on
     * @param col The column number the error occurred on
     */
    public VRMLParseException(int line, int col, String msg) {
        super(msg);

        this.line = line;
        this.column = col;
    }

    /**
     * Get the line number this exception occurred on. If the original parser
     * did not support line numbers this returns -1;
     *
     * @return The line number the error was on.
     */
    public int getLineNumber() {
        return line;
    }

    /**
     * Get the column number this exception occurred on. If the original parser
     * did not support column numbers this returns -1;
     *
     * @return The column number the error was on.
     */
    public int getColumnNumber() {
        return column;
    }
}

