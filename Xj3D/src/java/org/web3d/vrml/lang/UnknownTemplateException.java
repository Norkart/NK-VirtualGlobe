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
 * Exception indicating that a node template could not be matched to anything
 * requested internally.
 * <P>
 * This is a fairly rare exception and only occurs when we can't map an
 * EXTERNPROTO to an underlying definition. Mostly this will occur at runtime
 * but may in fact be used when the user attempts to create or access nodes
 * that use this information.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class UnknownTemplateException extends VRMLException {

    /**
     * Create a new exception with no message.
     *
     * @param name The name of the node type that caused the error
     */
    public UnknownTemplateException(String name) {
    }

    /**
     * Create a new exception with a message.
     *
     * @param name The name of the node type that caused the error
     * @param msg The message associated with this exception
     */
    public UnknownTemplateException(String name, String msg) {
        super(msg);
    }
}
