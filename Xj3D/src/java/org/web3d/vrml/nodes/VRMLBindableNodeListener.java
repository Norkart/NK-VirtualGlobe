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

package org.web3d.vrml.nodes;

// Standard imports
// none

// Application specific imports
// none

/**
 * The listener interface for receiving notice that a node has changed its
 * global activation state.
 * <p>
 *
 * This listener may be used for any node type such as sensors. However, it
 * will probably be mainly seen in conjungtion with bindable nodes.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLBindableNodeListener {

    /**
     * Notification that the environment has requested that this node be now
     * bound as the active node.
     *
     * @param src The source node that is to be bound
     * @param yes true if the node is to becoming active
     */
    public void nodeIsBound(VRMLNodeType src, boolean yes);
}
