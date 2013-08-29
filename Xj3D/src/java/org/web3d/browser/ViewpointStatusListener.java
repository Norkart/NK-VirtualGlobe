/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * A listener to track viewpoint list changes.
 * <p>
 *
 * A listener needs to be aware that there are multiple layers that can come
 * and go in the core of the application. This status listener keeps the user
 * informed of the layer changes and the layer it belongs to. The UI should
 * then adapt it's list to the active layer as needed.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface ViewpointStatusListener {

    /**
     * Notification of the addition of a valid layer ID to the current
     * list. This layer is currently empty of viewpoints. Calls to
     * {@link #viewpointAdded} will subsequently follow with all the viewpoints
     * listed in this layer. It can be assumed that the layer is not currently
     * the active layer. A separate notificaion is available for that.
     *
     * @param layerId The ID of the layer to be added
     */
    public void viewpointLayerAdded(int layerId);

    /**
     * Notification that a Layer ID is no longer valid. Any viewpoints that
     * have been made available for that layer should now be removed from the
     * layer. The {@link #viewpointRemoved} callback will not be made for this
     * case.
     *
     * @param layerId The ID of the layer to be added
     */
    public void viewpointLayerRemoved(int layerId);

    /**
     * The given layer is now made the active layer. If there is a viewpoint
     * list being maintained per-layer then the UI can perform some sort of
     * highlighting to indicate this. Viewpoints in other layers are still
     * allowed to be bound by the user interface. If there was a previously
     * active layer, ignore it.
     * <p>
     * The code will guarantee that if the active layer is removed, then this
     * method will be called first to set a different valid layer, before
     * removing that layer ID.
     * <p>
     *
     * If a value of -1 is provided, that means no layers are active and that
     * we currently have a completely clear browser with no world loaded. The
     * UI should act appropriately.
     *
     * @param layerId The ID of the layer to be made current or -1
     */
    public void viewpointLayerActive(int layerId);

    /**
     * Invoked when a viewpoint has been added
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is added to
     * @param isDefault Is the node a default
     */
    public void viewpointAdded(VRMLViewpointNodeType node,
                               int layerId,
                               boolean isDefault);

    /**
     * Invoked when a viewpoint has been removed
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is removed from
     */
    public void viewpointRemoved(VRMLViewpointNodeType node, int layerId);

    /**
     * Invoked when a viewpoint has been bound.
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is bound on
     */
    public void viewpointBound(VRMLViewpointNodeType node, int layerId);
}
