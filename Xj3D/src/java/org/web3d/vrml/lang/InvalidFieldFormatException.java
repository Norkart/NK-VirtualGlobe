/*****************************************************************************
 *                    Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.lang;

// External imports
// none

// Local imports
// none

/**
 * Exception for when the format provided by the field is invalid for what
 * the field requires.
 * <P>
 *
 * The exception also has the option of including line and column number
 * information for when it is generated from a file that was read. If the
 * field was parsed from a non-file source, such as the SAI, then no
 * number information will be present and the two getter methods will
 * return a value of -1.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class InvalidFieldFormatException extends FieldException {

    /** The name of the field with the error */
    private String fieldName;

    /** The line number the exception occurred on */
    private int line;

    /** The column number the exception occurred on */
    private int column;

    /**
     * Create a new exception with no message.
     */
    public InvalidFieldFormatException() {
        line = -1;
        column = -1;
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public InvalidFieldFormatException(String msg) {
        super(msg);
        line = -1;
        column = -1;
    }

    /**
     * Create a new exception with a message and the name of the field
     * that had the error.
     *
     * @param msg The message associated with this exception
     * @param field The name of the field with the error
     */
    public InvalidFieldFormatException(String msg, String field) {
        super(msg);
        line = -1;
        column = -1;
    }

    /**
     * Create a new exception with a message and the name of the field
     * that had the error.
     *
     * @param msg The message associated with this exception
     * @param lineNumber The line number the error occurred on
     * @param columnNumber The column number the error occurred on
     */
    public InvalidFieldFormatException(String msg,
                                       int lineNumber,
                                       int columnNumber) {
        super(msg);

        line = lineNumber;
        column = columnNumber;
    }

    /**
     * Create a new exception with a message and the name of the field
     * that had the error.
     *
     * @param msg The message associated with this exception
     * @param field The name of the field with the error
     */
    public InvalidFieldFormatException(String msg,
                                       int lineNumber,
                                       int columnNumber,
                                       String field) {
        super(msg);

        line = lineNumber;
        column = columnNumber;
    }

    /**
     * Get the line number this exception occurred on. If the original parser
     * did not support line numbers this returns -1;
     *
     * @param lineNumber The line number the error was on.
     */
    public void setLineNumber(int lineNumber) {
        line = lineNumber;
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
     * @param columnNumber The column number the error was on.
     */
    public void setColumnNumber(int columnNumber) {
        column = columnNumber;
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
