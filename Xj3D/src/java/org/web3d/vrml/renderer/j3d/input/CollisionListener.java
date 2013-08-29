/*****************************************************************************
 *                        J3D.org Copyright (c) 2000 - 2005
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
import javax.media.j3d.SceneGraphPath;

// Local imports
// none

/**
 * A listener interface used to notify of a collision between the user position
 * and geometry in the scene.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface CollisionListener {

    /**
     * Notification that a collision has taken place with the given Java3D
     * scene object.
     *
     * @param path The path from the root of the scene graph to here
     */
    public void avatarCollision(SceneGraphPath path);
}
