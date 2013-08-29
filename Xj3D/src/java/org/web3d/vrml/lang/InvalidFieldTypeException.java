/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
 * Error when the type of the field does not match, such as for routes,
 * scripting or internal manipulation.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class InvalidFieldTypeException extends FieldException {

    /**
     * Create a blank Field exception that refers to the given Field type.
     *
     * @param name The name of the Field type that caused the error
     */
    public InvalidFieldTypeException(String name) {
        super(name);
    }
}
