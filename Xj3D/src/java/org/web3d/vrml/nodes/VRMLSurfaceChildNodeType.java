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
package org.web3d.vrml.nodes;

// External imports
import java.awt.Rectangle;

// Local imports
// None

/**
 * A legal child for an OverlayNodeType parent.
 * <p>
 *
 * An overlay child node describes one item of an overlay. This item may be
 * 2D or 3D and can be organised with a position on the surface using a
 * series of relationships nodes that control how this node works with the
 * rest of the renderable surface.
 * <p>
 *
 * Each overlay child node has a visibility state that controls whether it
 * is rendered or not. When a child is not visible, it does not take part in
 * any layout organisation. If the child is a layout node, then the visibility
 * state controls all its children. If the layout is made not-visible then
 * none of its children are rendered.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface VRMLSurfaceChildNodeType extends VRMLNodeType {

    /**
     * Get the current visibility state of this node.
     *
     * @return true if the node is current visible, false otherwise
     */
    public boolean isVisible();

    /**
     * Set the visibility state of the surface. A non-visible surface will
     * still take events and update, just not be rendered.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setVisible(boolean state);

    /**
     * Notification from the parent node about this node's visiblity state.
     * Used to control the rendering so that if a parent is not visible it can
     * inform this node that it is also not visible without needing to stuff
     * with the local visibility state.
     *
     * @param state true to make this node visible, false to hide
     */
    public void setParentVisible(boolean state);

    /**
     * Request the node's current parent visibility state. Mainly used for
     * proto copying.
     *
     * @return state true to make this node visible, false to hide
     */
    public boolean getParentVisible();

    /**
     * Get the value of the 2D bounding box size of this overlay. The bounds
     * are given in pixel coordinates relative to the center of this overlay.
     * Although the return values are always floats, the values will alway be
     * integer based and will not contain fractional values.
     *
     * @return The current bounds
     */
    public float[] getBboxSize();

    /**
     * Get the current value of the 2D bounding box of this overlay. The bounds
     * are given in pixel coordinates relative to the top left corner.
     *
     * @return The current bounds [x, y, width, height]
     */
    public Rectangle getRealBounds();


    /**
     * Tell this overlay that it's position in window coordinates has been
     * changed to this new value. The position is always that of the top-left
     * corner of the bounding box in screen coordinate space. Values are in
     * pixels.
     *
     * @param x The x location of the window in pixels
     * @param y The y location of the window in pixels
     */
    public void setLocation(int x, int y);

    /**
     * Set the layout listener for this node. Setting a value of null clears
     * the current listener instance.
     *
     * @param l The new listener to use or null
     */
    public void setLayoutListener(VRMLSurfaceLayoutListener l);
}
