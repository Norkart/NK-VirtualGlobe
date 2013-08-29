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

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports
//import javax.media.j3d.SceneGraphPath;
import org.j3d.aviatrix3d.Texture2D;

// Application specific imports
import org.web3d.vrml.nodes.VRMLBackgroundNodeType;

/**
 * An abstract representation of any background node for use in the OpenGL
 * renderer.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface OGLBackgroundNodeType
    extends OGLVRMLNode, VRMLBackgroundNodeType {

    /**
     * Get the list of textures defined for this background that have changed
     * since the last frame. The array contains the textures in the order
     * back, front, left, right, top, bottom. If the texture hasn't changed is no texture defined, then that array element is null.
     *
     * @param changes An array to copy in the flags of the individual textures
     *   that have changed
     * @param textures The list of textures that have changed for this background.
     * @return true if anything changed since the last time
     */
    public boolean getChangedTextures(Texture2D[] textures, boolean[] changes);

    /**
     * Get the list of textures defined for this background. The array contains
     * the textures in the order front, back, left, right, top, bottom. If
     * there is no texture defined, then that array element is null.
     *
     * @return The list of textures for this background.
     */
    public Texture2D[] getBackgroundTextures();
}
