/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports
import javax.vecmath.Matrix4f;

// Application specific imports
import org.web3d.vrml.nodes.VRMLTextureCoordinateTransformNodeType;

/**
 * Defines a transformation that is applied to a texture - either 2D or 3D.
 * <p>
 * The transform is suitable for use with either planar or volume texturing.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface OGLTextureCoordinateTransformNodeType
    extends VRMLTextureCoordinateTransformNodeType, OGLVRMLNode {

    /**
     * Request the Transformation used to represent a texture transformation.
     * The transform will contain all of the warp, scale and rotation
     *
     * @return The transform used to modify this texture
     */
    public Matrix4f[] getTransformMatrix();

    /**
     * Add a listener for textureTransform changes
     *
     * @param tl The listener to add
     */
     public void addTransformListener(OGLTextureTransformListener tl);

    /**
     * Remove a listener for textureTransform changes
     *
     * @param tl The listener to remove
     */
     public void removeTransformListener(OGLTextureTransformListener tl);
}
