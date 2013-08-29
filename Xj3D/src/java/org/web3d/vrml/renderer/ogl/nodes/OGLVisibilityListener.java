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
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.AxisAngle4f;

// Local imports
import org.web3d.vrml.renderer.common.nodes.VisibilityListener;

/**
 * The listener interface for receiving notice on entry or exit from an area.
 * <p>
 *
 * Each method receives both the user's current position and orientation in
 * V-world coordinates but also the transform of the object that was picked
 * and representing this interface. The idea of this is to save internal calls
 * to getLocalToVWorld() and the extra capability bits required for this. The
 * transform is available from the initial pick SceneGraphPath anyway, so this
 * comes for free and leads to better performance. In addition, it saves
 * needing to pass in a scene graph path for dealing with the
 * getLocalToVWorld() case when we are under a SharedGroup.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface OGLVisibilityListener extends VisibilityListener {

    /**
     * Invoked when the user enters or leaves an area.
     *
     * @param visible true when the user enters the area
     * @param position The position of the user on entry/exit
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener. null when visible is false.
     */
    public void visibilityStateChange(boolean visible,
                                      Point3f position,
                                      AxisAngle4f orientation,
                                      Matrix4f localPosition);

    /**
     * Notification that the object is still visible, but that the
     * viewer reference point has changed. Ignored for this implementation.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void viewPositionChanged(Point3f position,
                                    AxisAngle4f orientation,
                                    Matrix4f localPosition);
}
