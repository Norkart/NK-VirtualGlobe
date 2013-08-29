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
// None

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * A grouping node that collects together and provides layout information for
 * items on a surface.
 * <p>
 *
 * When the instance is given nodes to manage, it should manage it until told
 * not to. Any time that the window size changes, the relationship should
 * recalculate the on-screen position and notify the overlay items of their
 * new pixel coordinates.
 * <p>
 *
 * It is legal for a layout node to also contain other layout nodes in a
 * nested fashion. An implementation should be aware of this and make sure
 * it handles nested layouts correctly.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLSurfaceLayoutNodeType extends VRMLSurfaceChildNodeType {

    /**
     * Set the new window size, requesting that the layout implementation
     * rebuild and re-evalutate all of the items it contains. If this layout
     * is a child of another layout, the size set will be the allocated size
     * for the child window. The location is in standard 2D screen coordinates
     * of the top-left corner.
     *
     * @param x The x location of the window in pixels
     * @param y The y location of the window in pixels
     * @param width The width of the window in pixels
     * @param height The height of the window in pixels
     */
    public void windowChanged(int x, int y, int width, int height);

    /**
     * Set the drawable content of this node to the surface. If value is set
     * to null, then it clears all the renderable list and nothing is show.
     * The nodes provided must be {@link VRMLSurfaceChildNodeType} or
     * {@link VRMLProtoInstance}.
     *
     * @param kids The list of new nodes to layout
     * @throws InvalidFieldValueException The nodes are not one of the required
     *   types.
     */
    public void setChildren(VRMLNodeType[] kids)
        throws InvalidFieldValueException;

    /**
     * Get the list of current children used by this node. If none are
     * defined, this returns a null value.
     * be {@link VRMLSurfaceChildNodeType} or {@link VRMLProtoInstance}.
     *
     * @return The list of current managed nodes or null
     */
    public VRMLNodeType[] getChildren();
}
