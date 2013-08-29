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
import javax.media.j3d.SceneGraphPath;
import javax.media.j3d.Texture2D;

// Application specific imports
import org.web3d.vrml.nodes.VRMLBackgroundNodeType;

/**
 * An abstract representation of any background node.
 * <p>
 *
 * Background nodes, as far as VRML is concerned has a few problems mapping
 * to the Java 3D equivalent. In Java3D, it is just a colour or texture blit
 * to the framebuffer. In VRML it defines a full 3D surface (actually 2 - a
 * sphere and an enclosed box). The background is supposed to be effected by
 * the enclosing transforms, but only the rotation part. Scale and translation
 * are supposed to be ignored.
 * <p>
 *
 * This is going to be a bit hard to deal with correctly. The Background node
 * wants to be not effected by scale and translation, but needs to know
 * rotation information.
 * <p>
 *
 * In the current model, we are going to introduce two separate peices of
 * scenegraph for Java3D. Firstly the background node will be a separate piece
 * of geometry to the scenegraph in which it lives. Thus, the node that is
 * returned to <code>getSceneGraphObject()</code> will not be the same as
 * the geometry that we ask for here to use as the background. The reason for
 * this is dealing with the rotation and scale issues. It is too complex to
 * have to deal with the transform changing all the time, so we're going to
 * just use the basic scenegraph information for the transformation information
 * and ignore the rest. The assumption is that the browser will take care of
 * the rest of the management of displaying the background node.
 * <p>
 *
 * One mandatory requirement of the implementing node is that the BranchGroup
 * used by the background node that exists in the main scenegraph must have
 * the <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set so that we can
 * query for the needed information.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface J3DBackgroundNodeType
    extends J3DVRMLNode, VRMLBackgroundNodeType {

    /**
     * A check to see if the parent scene graph path has changed from last
     * time we checked for this node. Assumes that the call is being made on
     * a node that we checked on last frame. If this has been just changed with
     * a new binding call then the caller should just immediately request the
     * current path anyway.
     *
     * @return true if the parent path has changed since last frame
     */
    public boolean hasScenePathChanged();

    /**
     * Fetch the scene graph path from the root of the scene to this node.
     * Typically used for the getLocalToVWorld transformation handling.
     * the node returns null then there is no path to the root of the scene
     * ie this node is somehow orphaned during the last frame.
     *
     * @return The fully qualified path from the root to here
     */
    public SceneGraphPath getSceneGraphPath();

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
