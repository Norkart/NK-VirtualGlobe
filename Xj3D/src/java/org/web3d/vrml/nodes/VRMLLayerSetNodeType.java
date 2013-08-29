/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
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

// External imports
// None

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * An abstract representation of a collection of rendering layers.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface VRMLLayerSetNodeType extends VRMLWorldRootChildNodeType {

    /**
     * Get the layers this set manages. Provides a live reference not a copy.
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getLayers();

    /**
     * Set the layers field with a collection of new layers.
     *
     * @param layers Array of new layer instance nodes to use
     * @throws InvalidFieldValueException One or more of the passed nodes does
     *   not represent a layer node
     */
    public void setLayers(VRMLNodeType[] layers);

    /**
     * Get the ID of the layer that should be active for navigation in
     * during this frame.
     *
     * @return An int greater than or equal to zero
     */
    public int getActiveNavigationLayer();

    /**
     * Check to see if the layers themselves have changed this frame. If they
     * have, we need to rebuild the user interface next time around.
     *
     * @return true if the layer list has changed since last frame
     */
    public boolean hasLayerListChanged();

    /**
     * Check to see if the render order has changed this frame. If it has,
     * return true, otherwise return false. Can only be asked once per frame,
     * and is reset to false after calling.
     *
     * @return true if the render order has changed since last frame
     */
    public boolean hasRenderOrderChanged();

    /**
     * Query how many layers should be actively rendered this frame. This will
     * come from the order list's length
     *
     * @return A positive number
     */
    public int getNumRenderedLayers();

    /**
     * Get the rendering order of the layers, copied into the given array.
     * Assumes that the array must be at least {@link #getNumRenderedLayers()}
     * in length;
     *
     * @param order The array to copy values into
     */
    public void getRenderOrder(int[] order);
}
