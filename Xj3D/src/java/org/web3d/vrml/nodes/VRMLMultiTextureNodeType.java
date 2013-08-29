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

// External import
// None

// Local import
// None

/**
 * Specifies a multi texture for associated geometry.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface VRMLMultiTextureNodeType extends VRMLComposedTextureNodeType {

    /**
     * Get the texture params for each stage of this texture.
     *
     * @param start Where in the array to start filling in textures.
     * @param function The preallocated array to return function IDs in.  Error if too small.
     * @param modes The TextureConstant modes.
     */
    public void getTextureParams(int start, int[] modes, int[] function, int[] source);
}
