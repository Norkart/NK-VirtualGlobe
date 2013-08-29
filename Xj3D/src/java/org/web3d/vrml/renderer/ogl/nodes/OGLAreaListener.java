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

// External imports
import javax.vecmath.*;

// Local imports
import org.web3d.vrml.renderer.common.nodes.AreaListener;

/**
 * The listener interface for receiving notice of the viewpoint on entry or
 * exit from an area.
 * <p>
 * Each method receives both the user's current position and orientation in
 * V-world coordinates but also the transform of the object that was picked
 * and representing this interface. The idea of this is to save internal calls
 * to getLocalToVWorld() and the extra capability bits required for this. The
 * transform is available from the initial pick SceneGraphPath anyway, so this
 * comes for free and leads to better performance. In addition, it saves
 * needing to pass in a scene graph path for dealing with the
 * getLocalToVWorld() case when we are under a SharedGroup.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.3 $
 */
public interface OGLAreaListener extends AreaListener {

    /**
     * Invoked when the user enters an area.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void areaEntry(Point3f position,
                          Vector3f orientation,
                          Matrix4f vpMatrix,
                          Matrix4f localPosition);

    /**
     * Notification that the user is still in the area, but that the
     * viewer reference point has changed.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void userPositionChanged(Point3f position,
                                    Vector3f orientation,
                                    Matrix4f vpMatrix,
                                    Matrix4f localPosition);

    /**
     * Invoked when the tracked object exits on area.
     */
    public void areaExit();
}
