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
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

// Application specific imports
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * Shell representation of a viewpoint node.
 * <p>
 *
 * The scenegraph object returned from a viewpoint node shall be a
 * {@link javax.media.j3d.TransformGroup} that contains the initial offset
 * of the viewpoint (it's orientation and position as defined by the
 * node outside). The implementation should also have a
 * {@link javax.media.j3d.ViewPlatform} that is part of the scenegraph.
 *
 */
public interface J3DViewpointNodeType
    extends J3DVRMLNode, VRMLViewpointNodeType {

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
     * Get the default Transform representation of this viewpoint based on
     * its current position and orientation values. This is used to reset the
     * viewpoint to the original position after the user has moved around or
     * we transition between two viewpoints. It should remain independent of
     * the underlying TransformGroup.
     *
     * @return The default transform of this viewpoint
     */
    public Transform3D getViewTransform();

    /**
     * Get the parent transform used to control the view platform. Used for
     * the navigation controls.
     *
     * @return The current view TransformGroup
     */
    public TransformGroup getPlatformGroup();
}
