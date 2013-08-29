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
 * Exception when the file attempts to make a connection such as a ROUTE or
 * IS between two incompatible or undefined fields and/or nodes.
 * <P>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class InvalidFieldConnectionException extends FieldException {

    /**
     * Create a new exception with no message.
     */
    public InvalidFieldConnectionException() {
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public InvalidFieldConnectionException(String msg) {
        super(msg);
    }
}
