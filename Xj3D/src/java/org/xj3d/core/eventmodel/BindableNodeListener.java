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

package org.xj3d.core.eventmodel;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLBindableNodeType;

/**
 * Listener for notification of changes in the current bound node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface BindableNodeListener {

    /**
     * Notification that the currently bound node is now this one.
     *
     * @param node The newly bound listener
     */
    public void newNodeBound(VRMLBindableNodeType node);

    /**
     * Notification that a new bindable has been added.
     *
     * @param node The new node
     * @param isDefault Is the node a default
     */
    public void bindableAdded(VRMLBindableNodeType node, boolean isDefault);

    /**
     * Notification that a bindable has been removed.
     *
     * @param node The node
     */
    public void bindableRemoved(VRMLBindableNodeType node);
}
