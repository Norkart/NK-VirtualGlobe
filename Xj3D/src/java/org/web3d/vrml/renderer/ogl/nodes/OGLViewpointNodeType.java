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
import javax.vecmath.Matrix4f;

import org.j3d.aviatrix3d.TransformGroup;
import org.j3d.aviatrix3d.Viewpoint;

// Application specific imports
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * Shell representation of a viewpoint node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public interface OGLViewpointNodeType
    extends VRMLViewpointNodeType, OGLBindableNodeType {

    /**
     * Get the default Transform representation of this viewpoint based on
     * its current position and orientation values. This is used to reset the
     * viewpoint to the original position after the user has moved around or
     * we transition between two viewpoints. It should remain independent of
     * the underlying TransformGroup.
     *
     * @return The default transform of this viewpoint
     */
    public Matrix4f getViewTransform();

    /**
     * Get the parent transform used to control the view platform. Used for
     * the navigation controls.
     *
     * @return The current view TransformGroup
     */
    public TransformGroup getPlatformGroup();

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    public void setWorldScale(float scale);

    /**
     * Set a new transform for this viewpoint.  Used to affect
     * navigation changes.  Users should not directly modify the
     * Aviatrix3d transform.  This should only be called from
     * the updateNodeBoundsChanged method of the viewpointGroup.
     *
     * @param transform The view transform
     */
    public void setNavigationTransform(Matrix4f transform);
}
