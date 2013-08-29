/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
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

// Standard imports
import javax.media.j3d.SceneGraphPath;

// Application specific imports
// none

/**
 * A listener interface used to notify of a collision between the user position
 * and geometry in the scene.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class CollisionListenerAdapter
    implements org.j3d.renderer.java3d.navigation.CollisionListener {

    /** The listener for xj3d */
    private CollisionListener xj3dListener;

    /**
     * Construct an adapter for the Xj3D listener
     *
     * @param l The listener to adapt
     */
    CollisionListenerAdapter(CollisionListener l) {
        xj3dListener = l;
    }

    /**
     * Notification that a collision has taken place with the given Java3D
     * scene object.
     *
     * @param node The node that was collided with
     */
    public void avatarCollision(SceneGraphPath path) {
        xj3dListener.avatarCollision(path);
    }
}
