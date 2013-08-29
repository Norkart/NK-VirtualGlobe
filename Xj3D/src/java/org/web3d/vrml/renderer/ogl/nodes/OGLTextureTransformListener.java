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

package org.web3d.vrml.renderer.ogl.nodes;

// External imports
import javax.vecmath.Matrix4f;

// Local imports
// None

/**
 * The listener interface for receiving notice that a textureTransform has changed.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface OGLTextureTransformListener {

    /**
     * Invoked when a textureTransform has changed.
     *
     * @param src The node instance that was the source of this change
     * @param tmatrix The new TransformMatrix array
     * @param updated Flag for each index illustrating whether it has
     *   been updated or not.
     */
    public void textureTransformChanged(OGLVRMLNode src,
                                        Matrix4f[] tmatrix,
                                        boolean[] updated);
}
