/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2007
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
// None

/**
 * Specifies a texture with multiple children textures.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface VRMLComposedTextureNodeType extends VRMLTextureNodeType {
    /**
     * Get the number of textures in this ComposedTexture node.
     *
     * @return The number of active textures.
     */
    public int getNumberTextures();

    /**
     * Get the textures which make up this composed textures..
     *
     * @param start Where in the array to start filling in textures.
     * @param texs The preallocated array to return texs in.  Error if too small.
     */
    public void getTextures(int start, VRMLTextureNodeType[] texs);
}
