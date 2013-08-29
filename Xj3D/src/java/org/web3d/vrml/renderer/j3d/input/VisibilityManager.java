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

package org.web3d.vrml.renderer.j3d.input;

// Standard imports
import javax.media.j3d.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;

// Application specific imports
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLSensorNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DVisibilityListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;

/**
 * Handler for all nodes that require visibility information to render
 * correctly.
 * <p>
 *
 * The manager works for all nodes that require visibility information. This
 * can be view-dependent nodes like LOD and Billboard or sensor nodes like
 * VisibilitySensor. All nodes that implement the J3DVisibilityListener
 * interface are directly managed by this class.
 * <p>
 *
 * The implementation uses a single pick to find all the nodes that are
 * currently visible from the user's position and orientation. To do this
 * it uses a cone that is either bounded or unbounded depending on the current
 * visibility limit given by the currently bound NavigationInfo node type.
 * If the vis limit is 0 then an unbounded pick is used. If the scene is really
 * large then anything in that cone of view will be detected and asked to
 * update. This could have quite a large performance impact, so user content
 * should always make sure to set the field value if they have really large
 * content to work with.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class VisibilityManager {

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 25;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 25;

    /** The root of the SG */
    private BranchGroup worldRoot;

    /** Two sets used to determine when a viewSensor becomes inactive */
    private HashSet activeVisSensors;

    /** The newly visible sensors this frame. */
    private HashSet newVisSensors;

    /** A scratch list used to avoid garbage generation */
    private J3DVisibilityListener[] list;

    /** Last item in the list */
    private int lastListener;

    /** Pick object for a defined visibilityLimit */
    private PickConeSegment limitedPicker;

    /** Pick object for no defined visibilityLimit */
    private PickConeRay endlessPicker;

    /** Util class representing the end of the picker */
    private Point3d endPoint;

    /**
     * Construct a new manager for visiblity sensors.
     */
    VisibilityManager() {

        limitedPicker = new PickConeSegment();
        endlessPicker = new PickConeRay();
        endPoint = new Point3d();

        activeVisSensors = new HashSet();
        newVisSensors = new HashSet();

        list = new J3DVisibilityListener[LIST_START_SIZE];
        lastListener = 0;
    }

    /**
     * Add a new sensor node instance to this handler.
     *
     * @param node The node instance to add
     */
    public void addSensor(VRMLSensorNodeType node) {
    }

    /**
     * Remove a sensor node instance from this handler.
     *
     * @param node The node instance to remove
     */
    public void removeSensor(VRMLSensorNodeType node) {
    }

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    public void setWorldRoot(BranchGroup root) {
        worldRoot = root;
    }

    /**
     * Clear the current list of items internally. This is because the world
     * has changed and that the currently cached data is no longer usable.
     */
    public void clear() {
        activeVisSensors.clear();
        newVisSensors.clear();
    }

    /**
     * Process this frame using the given position and orientation of the
     * user position in Virtual world coordinates.
     *
     * @param pos The position of the user
     * @param orient The orientation of the user
     * @param limit The visibility limit
     * @param fov The field of view angle in radians
     */
    public void processFrame(Point3d pos,
                             Vector3d orient,
                             AxisAngle4d axisOrient,
                             float limit,
                             float fov) {

        // TODO: This will need to change for handling OrthoViewpoints

        if(worldRoot == null)
            return;

        // Calculates the end point from the params
        SceneGraphPath[] found = null;
        if(limit != 0) {
            endPoint.x = orient.x * limit + pos.x;
            endPoint.y = orient.y * limit + pos.y;
            endPoint.z = orient.z * limit + pos.z;

            limitedPicker.set(pos, endPoint, fov);
            found = worldRoot.pickAll(limitedPicker);
        } else {
            endlessPicker.set(pos, orient, fov);
            found = worldRoot.pickAll(endlessPicker);
        }

        if((found != null) && (found.length != 0)) {
            // Clear the visited entries for active VisSensors
            newVisSensors.clear();

            for(int i = 0; i < found.length; i++) {
                Node n = found[i].getObject();
                J3DUserData user_data = (J3DUserData)n.getUserData();

                if((user_data != null) &&
                   (user_data.visibilityListener != null)) {

                    J3DVisibilityListener l = user_data.visibilityListener;

                    Transform3D local_tx = found[i].getTransform();

                    if(activeVisSensors.contains(l)) {
                        l.viewPositionChanged(pos, axisOrient, local_tx);
                        activeVisSensors.remove(l);
                    } else
                        l.visibilityStateChange(true, pos, axisOrient, local_tx);

                    newVisSensors.add(l);
                }
            }
        }

        // Remove unvisited entries
        int size = activeVisSensors.size();
        if(size != 0) {
            resizeList(size);
            activeVisSensors.toArray(list);

            for(int i = 0; i < size; i++) {
                if(list[i] == null)
                    break;

                J3DVisibilityListener l = (J3DVisibilityListener)list[i];

                l.visibilityStateChange(false, pos, axisOrient, null);
                activeVisSensors.remove(l);
            }
        }

        // Swap over lists
        HashSet tmp = activeVisSensors;

        activeVisSensors = newVisSensors;
        newVisSensors = tmp;
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeList(int maxSize) {

        if(list.length < maxSize) {
            int old_size = list.length;
            int new_size = old_size + LIST_INCREMENT;

            J3DVisibilityListener[] tmp = new J3DVisibilityListener[new_size];

            System.arraycopy(list, 0, tmp, 0, old_size);

            list = tmp;
        }
    }
}
