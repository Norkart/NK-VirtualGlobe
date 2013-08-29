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
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import org.web3d.vrml.nodes.VRMLTextureNodeType;

// Application specific imports
// none

/**
 * The listener interface for receiving notice that a texture has changed.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface J3DTextureListener {
    /**
     * Invoked when a texture has changed
     *
     * @param tex The new texture impl
     * @param alpha Does this texture have an alpha channel
     * @param attrs The texture attributes
     */
    public void textureImplChanged(VRMLTextureNodeType node, Texture[] tex, boolean[] alpha,
        TextureAttributes[] attrs);
}
