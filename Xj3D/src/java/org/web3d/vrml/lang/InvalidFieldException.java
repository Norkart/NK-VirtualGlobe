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

package org.web3d.vrml.lang;

// Standard imports
// none

// Application specific imports
// none

/**
 * Exception indicating that a field is not known by this node.
 * <P>
 *
 * This is generated when the node does not have the named or indexed field.
 * May be generated during the parsing process or by the field during
 * runtime as the user is trying to modify it.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class InvalidFieldException extends FieldException {

    /**
     * Create a new exception with no message.
     */
    public InvalidFieldException() {
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public InvalidFieldException(String msg) {
        super(msg);
    }

    /**
     * Create a new exception with a message and the name of the field
     * that had the error.
     *
     * @param msg The message associated with this exception
     * @param field The name of the field with the error
     */
    public InvalidFieldException(String msg, String field) {
        super("Invalid field: " + field + ": " + msg);
    }

    /**
     * Create a new exception for dealing with an invalid field index value.
     * Useful for many situations such as routing and set values
     *
     * @param index The index of the field being set
     * @param nodeName The node that we are getting the field set in
     */
    public InvalidFieldException(String nodeName, int index) {
        super("Invalid field " + index + " in node " + nodeName);
    }
}
