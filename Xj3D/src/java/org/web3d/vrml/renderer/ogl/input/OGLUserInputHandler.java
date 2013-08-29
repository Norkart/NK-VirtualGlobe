/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.input;

// External imports
import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.TransformGroup;
import org.j3d.aviatrix3d.SceneGraphPath;

// Local imports
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.browser.NavigationStateListener;

import org.xj3d.core.eventmodel.UserInputHandler;

/**
 * A complete handler for all user input information within a scene.
 * <p>
 *
 * The handler takes care of all the handling needed for sensors, anchors,
 * navigation and keyboard. However, it does not define a way of sourcing
 * those events as it assumes that a user will either delegate or extend this
 * class with more specific information such as an AWT listener or Java3D
 * behavior.
 * <p>
 *
 * The current key handling does not allow keyboard navigation of the world.
 * It passes all key events directly through to the current key sensor if one
 * is registered.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public interface OGLUserInputHandler extends UserInputHandler {

    /**
     * Set the root group that we are doing the picking on. This allows
     * us to look for the picked items in the scene. A null value is used to
     * clear the world and disable picking behaviours.
     *
     * @param scene The scene to pick against
     */
    public void setPickableScene(Group scene);

    /**
     * Set the view and it's related transform group to use and the path to
     * get there from the root of the scene. The transform group must allow
     * for reading the local to Vworld coordinates so that we can accurately
     * implement terrain following. A null value for the path is permitted.
     *
     * @param vp The current viewpoint
     * @param tg The transform just about the viewpoint used to move it
     *    around in response to the UI device input
     * @param path The path from the root to the transform to use
     * @throws IllegalArgumentException The terminal node is not a viewpoint
     */
    public void setViewInfo(OGLViewpointNodeType vp,
                            TransformGroup tg,
                            SceneGraphPath path)
        throws IllegalArgumentException;

    /**
     * Set the listener for collision notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setCollisionListener(CollisionListener l);
}
