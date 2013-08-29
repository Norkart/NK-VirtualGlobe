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
 * Exception indicating that a profile is not supported by the implementation.
 * <P>
 *
 * This exception may be generated either by the parser implementation if it
 * has knowledge of profiles or by the implementation of the various handlers.
 * If the handlers generate it then the parser should at least understand the
 * exception. They are not required do anything such as then filter nodes or
 * anything. They may treat it as either a warning or full error that stops
 * the parser.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class UnsupportedProfileException extends VRMLException {

    /**
     * Create a new exception with no message.
     */
    public UnsupportedProfileException() {
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public UnsupportedProfileException(String msg) {
        super(msg);
    }
}
