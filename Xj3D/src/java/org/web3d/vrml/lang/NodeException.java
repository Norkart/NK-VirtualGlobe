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
 * Superclass of all exceptions relating to node errors.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class NodeException extends VRMLException {

    /** Basic message start */
    private static final String MSG_TEXT =
        "A problem occurred in the following node type: ";

    /**
     * Create a blank node exception that refers to the given node type.
     *
     * @param name The name of the node type that caused the error
     */
    public NodeException(String name) {
        super(MSG_TEXT + name);
    }

    /**
     * Create an exception for the given node that has a message associated
     * with it.
     *
     * @param name The name of the node type that caused the error
     * @param msg The message to associate with this error
     */
    public NodeException(String name, String msg) {
        super(MSG_TEXT + name + "\nMessage: " + msg);
    }
}
