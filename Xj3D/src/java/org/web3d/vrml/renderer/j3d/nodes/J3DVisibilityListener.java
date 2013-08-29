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
import javax.media.j3d.Transform3D;

import javax.vecmath.Point3d;
import javax.vecmath.AxisAngle4d;

// Application specific imports
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
 * @version $Revision: 1.6 $
 */
public interface J3DVisibilityListener extends VisibilityListener {

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
                                      Point3d position,
                                      AxisAngle4d orientation,
                                      Transform3D localPosition);

    /**
     * Notification that the object is still visible, but that the
     * viewer reference point has changed. Ignored for this implementation.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void viewPositionChanged(Point3d position,
                                      AxisAngle4d orientation,
                                     Transform3D localPosition);
}
