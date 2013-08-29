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

package org.web3d.vrml.scripting;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.VRMLException;

/**
 * Exception indicating that the content provided to a script engine is not
 * appropriate for it's implementation.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class InvalidScriptContentException extends VRMLException {

    /**
     * Create a new exception with no message.
     */
    public InvalidScriptContentException() {
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public InvalidScriptContentException(String msg) {
        super(msg);
    }
}
