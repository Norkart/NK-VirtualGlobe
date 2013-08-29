/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * Listener for the ViewpointManager changes in viewpoints.
 * <p>
 *
 * These callback methods provide information for changes within an existing
 * scene on the current active navigation layer. The viewpoint manager keeps
 * track of all the layers and viewpoints, so you don't need to do that. This
 * listener just responds with information on the current active layer - ie
 * what the user interface should be displaying right now.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface ViewpointStatusListener {

    /**
     * The list of active viewpoints have been changed to this new list. A new
     * list can be given due to either a new world being loaded, or the active
     * navigation layer has been changed. This method is not called if a single
     * viewpoint is added or removed from the scene.
     * <p>
     *
     * If the scene contains no viewpoints at all (except the default bindable),
     * then this will be called with a null parameter.
     * <p>
     *
     * On scene or navigation layer change, it is guaranteed that this method
     * will be called before the notification of the actual bound viewpoint.
     *
     * @param viewpoints An array of exactly the number of viewpoints in the list
     */
    public void availableViewpointsChanged(VRMLViewpointNodeType[] viewpoints);

    /**
     * Notification that the selected viewpoint has been changed to this
     * new instance. There are many different reasons this could happen -
     * new node bound, world changed or even active navigation layer has
     * changed and this is the default in that new layer.
     * <p>
     *
     * If the file contains no viewpoints or the default viewpoint is
     * bound (due to unbinding all real viewpoints on the stack) then this
     * will be called with a null parameter.
     * <p>
     *
     * It is guaranteed that this will always contain something from the
     * currently active viewpoint list. If all change, that callback will be
     * called before this one, to ensure consistency.
     *
     * @param vp The viewpoint instance that is now selected or null
     */
    public void selectedViewpointChanged(VRMLViewpointNodeType vp);

    /**
     * Notification that this viewpoint has been appended to the list of
     * available viewpoints.
     *
     * @param vp The viewpoint instance that was added
     */
    public void viewpointAdded(VRMLViewpointNodeType vp);

    /**
     * Notification that this viewpoint has been removed from the list of
     * available viewpoints.
     *
     * @param vp The viewpoint instance that was added
     */
    public void viewpointRemoved(VRMLViewpointNodeType vp);
}
