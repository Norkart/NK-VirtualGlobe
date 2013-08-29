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
 * OverlayNodeType defines a surface that is always screen-aligned and is
 * drawn over the top of the basic 3D scene.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface VRMLOverlayNodeType extends VRMLSurfaceNodeType {

    /**
     * Set the layout to the new value. Setting a value of null will
     * clear the current layout and leave nothing visible on-screen. The node
     * provided must be either {@link VRMLSurfaceLayoutNodeType} or
     * {@link VRMLProtoInstance}.
     *
     * @param kids The new layout to use
     * @throws InvalidFieldValueException The nodes are not one of the required
     *   types.
     */
    public void setLayout(VRMLNodeType kids)
        throws InvalidFieldValueException;

    /**
     * Get the current layout of this overlay node. If none is set,
     * null is returned. The node returned will be either
     * {@link VRMLSurfaceLayoutNodeType} or {@link VRMLProtoInstance}.
     *
     * @return The current list of children or null
     */
    public VRMLNodeType getLayout();
}
