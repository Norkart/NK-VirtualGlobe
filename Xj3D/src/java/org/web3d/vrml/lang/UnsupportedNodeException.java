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
 * Exception indicating that a node is not supported by the profiles and
 * components selected for the file/stream.
 * <p>
 *
 * A message is automatically generated for this node, so the caller only
 * needs to provide a string representing the name of the node. You'll need
 * to be careful about this because the constructor signature is just like all
 * the others that take a single string, which is normally just the complete
 * message. For example, use the exception like this:
 * <pre>
 *   throw new UnsupportedNodeException("Inline");
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class UnsupportedNodeException extends VRMLException {

    /**
     * Message when the user wants to create a node that is not part of the
     * set of profiles and components. This is an error according to the spec.
     */
    private static final String INVALID_NODE_MSG =
        "Request for a node that is not part of the specified profile and " +
        "components for this stream: ";

    /**
     * Create a new exception for the node of the given name.
     *
     * @param name The name of the node type that caused the error
     */
    public UnsupportedNodeException(String name) {
        super(INVALID_NODE_MSG + name);
    }
}
