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
 * Superclass of all exceptions describing errors in fields.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class FieldException extends VRMLException {

    /** The name of the field that this exception has been generated for */
    protected String fieldName;

    /**
     * Create a default exception that does not contain a message.
     */
    public FieldException() {
    }

    /**
     * Create an exception that contains the given message.
     *
     * @param msg The message to associate
     */
    public FieldException(String msg) {
        super(msg);
    }

    /**
     * Set the name of the field that this exception was generated for. A
     * null value will clear the current field name.
     *
     * @param name The name of the field
     */
    public void setFieldName(String name) {
        fieldName = name;
    }

    /**
     * Get the name of the field that this exception was generated for. May be
     * null if not set.
     *
     * @return The current name or null if not set
     */
    public String getFieldName() {
        return fieldName;
    }
}
