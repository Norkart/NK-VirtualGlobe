/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Interface for classes that wish to know about when nodes are added or
 * removed during the course of an event cycle, and work as a dynamic interface
 * with the event model.
 * <p>
 *
 * A single observer instance may be registered for more than one node type ID.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface NodeObserver {

    /**
     * Notification that nodes of the require type have been added. The node
     * representations will either be the real node, or if the parent of a
     * proto, the proto instance they came from.
     *
     * @param nodeType The node type id that this notification refers to
     * @param nodes The node instances that have just been added.
     */
    public void nodesAdded(int nodeType, VRMLNodeType[] nodes, int numNodes);

    /**
     * Notification that nodes of the require type have been removed. The node
     * representations will either be the real node, or if the parent of a
     * proto, the proto instance they came from.
     *
     * @param nodeType The node type id that this notification refers to
     * @param nodes The node instances that have just been removed.
     */
    public void nodesRemoved(int nodeType, VRMLNodeType[] nodes, int numNodes);

    /**
     * Force clearing all currently managed nodes from this observer now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear();

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown();
}
