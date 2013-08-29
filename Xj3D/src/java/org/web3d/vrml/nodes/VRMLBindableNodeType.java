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
 * Indicates that a node can be bound within the scenegraph to make it the
 * active item.
 * <p>
 *
 * These nodes obey the binding stack properties of the VRML spec. The
 * interface does not allow for dynamic feedback of how the binding is
 * notified to the user interface because it is assumed that the renderer
 * implementor will have their own ideas on the best way to do this.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */

public interface VRMLBindableNodeType extends VRMLChildNodeType {

    /**
     * Add a listener for activable events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addBindableNodeListener(VRMLBindableNodeListener l);

    /**
     * Remove a listener for geometry changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeBindableNodeListener(VRMLBindableNodeListener l);

    /**
     * Notify the bindable node that it is on the stack, or not on the
     * stack, as the case may be and that it should send bind events as
     * appropriate
     *
     * @param onStack true if this node is now on the stack
     */
    public void setOnStack(boolean onStack);

    /**
     * Set the bind field of this node. This will cause the node to be moved
     * within the stack according to the properties.
     *
     * @param enable True if this node is to be bound
     * @param notify true if this should notify the listeners
     * @param time The time that this was sent
     */
    public void setBind(boolean enable, boolean notify, double time);

    /**
     * Get the current isBound state of the node.
     *
     * @return the current binding state
     */
    public boolean getIsBound();

    /**
     * Get the time of the last bound state change.
     *
     * @return the time value
     */
    public double getBindTime();
}
