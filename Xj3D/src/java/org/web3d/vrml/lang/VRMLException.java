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
 * Superclass of all exceptions provided by this package and for all of the
 * VRML implementation.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class VRMLException extends RuntimeException {

    /**
     * Create a default exception that does not contain a message.
     */
    public VRMLException() {
    }

    /**
     * Create an exception that contains the given message.
     *
     * @param msg The message to associate
     */
    public VRMLException(String msg) {
        super(msg);
    }
}
