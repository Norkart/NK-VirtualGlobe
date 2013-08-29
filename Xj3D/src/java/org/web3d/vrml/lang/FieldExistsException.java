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
 * Exception indicating that a field declaration already exists.
 * <P>
 *
 * This is generated when either a script or proto defines a field twice with
 * two different definitions but the same name. May be generated during the
 * parsing process or by the field during runtime as the user is trying to
 * modify it.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class FieldExistsException extends FieldException {

    /** The name of the field with the error */
    private String fieldName;

    /**
     * Create a new exception with no message.
     */
    public FieldExistsException() {
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public FieldExistsException(String msg) {
        super(msg);
    }
    /**
     * Create a new exception with a message and the name of the field
     * that had the error.
     *
     * @param msg The message associated with this exception
     * @param field The name of the field with the error
     */
    public FieldExistsException(String msg, String field) {
        super(msg);
    }

    /**
     * Set the name of the field that caused this error. Used only by the code
     * that generated the exception if the format error and field names are
     * kept in two separate places.
     *
     * @param field The name of the field with the error
     */
    public void setFieldName(String field) {
        fieldName = field;
    }

    /**
     * Get the name of the field that generated this exception. It may or may
     * not be set, depending on the underlying implementation. If not set,
     * this will return null;
     *
     * @return The name of the field with the invalid error
     */
    public String getFieldName() {
        return fieldName;
    }
}
