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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import javax.media.j3d.Transform3D;

// Application specific imports
// none

/**
 * The listener interface for receiving notice that a textureTransform has changed.
 */
public interface J3DTextureTransformListener {
    /**
     * Invoked when a textureTransform has changed
     *
     * @param tmatrix The new TransformMatrix array
     */
    public void textureTransformChanged(Transform3D[] tmatrix);
}
