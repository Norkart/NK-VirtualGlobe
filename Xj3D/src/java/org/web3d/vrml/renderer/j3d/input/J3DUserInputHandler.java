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

package org.web3d.vrml.renderer.j3d.input;

// External imports
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.SceneGraphPath;

// Local imports
import org.web3d.browser.NavigationStateListener;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.renderer.common.input.LinkSelectionListener;
import org.web3d.vrml.renderer.common.input.TouchSensorSelectionListener;
import org.web3d.vrml.renderer.common.input.DragSensorSelectionListener;

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
 * @version $Revision: 1.12 $
 */
public interface J3DUserInputHandler extends UserInputHandler {

    /**
     * Set the world branchgroup that we are doing the picking on. This allows
     * us to look for the picked items in the scene. A null value is used to
     * clear the world and disable picking behaviours.
     *
     * @param scene The scene to pick against
     */
    public void setPickableScene(BranchGroup scene);

    /**
     * Set the view and it's related transform group to use. This view is what
     * we navigation around the scene with.
     *
     * @param view is the View object that we're modifying.
     * @param tg The transform group above the view object that should be used
     * @param path The path from the root to here, or null
     */
    public void setViewInfo(View view, TransformGroup tg, SceneGraphPath path);

    /**
     * Change the currently set scene graph path for the world root to this new
     * path without changing the rest of the view setup. Null will clear the
     * current path set.
     *
     * @param path The new path to use for the viewpoint
     */
    public void setViewPath(SceneGraphPath path);

    /**
     * Set the listener for collision notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setCollisionListener(CollisionListener l);
}
