/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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
import org.j3d.aviatrix3d.Texture;

// Local imports
// None

/**
 * An abstract representation of texture nodes.
 * <p>
 *
 * This interface provides a single method to allow setting of the Aviatrix3D
 * object back into the texture. In the architecture of Xj3D, we don't create
 * the texture object in the texture node implementations. Instead it is
 * deferred to the Appearance node implementation. However, for the loader
 * case, we still need access to the AV3D Texture object so that the loader
 * can set the image pixels into the texture. This class allows the texture
 * to be handed back its Texture object so that later on in the loader process
 * it can get the real object.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface OGLTextureNodeType extends OGLVRMLNode {

    /**
     * Set the Aviatrix3D texture representation back into the node
     * implementation.
     *
     * @param index The index of the texture (for multitexture)
     * @param tex The texture object to set
     */
    public void setTexture(int index, Texture tex);
}
