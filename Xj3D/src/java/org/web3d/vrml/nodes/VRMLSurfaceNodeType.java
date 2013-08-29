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
 * Representation of any node that allows the definition of a surface node
 * type.
 * <p>
 *
 * A surface allows the composition of one piece of content on another piece.
 * For example it allows an application such as a text editor to be embedded
 * into the 3D world as a texture on another object, or even as a standalone
 * item of geometry.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface VRMLSurfaceNodeType extends VRMLChildNodeType {

    /**
     * Notification that the area allocated to the surface has changed. The
     * new size in pixels is given.
     *
     * @param width The width of the surface in pixels
     * @param height The height of the surface in pixels
     */
    public void surfaceSizeChanged(int width, int height);

    /**
     * Get the current visibility state of this surface.
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
}
