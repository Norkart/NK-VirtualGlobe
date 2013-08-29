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
import javax.vecmath.*;

import org.j3d.aviatrix3d.*;

import java.util.ArrayList;

import org.j3d.aviatrix3d.picking.PickRequest;

// Local imports
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLSensorNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLAreaListener;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

/**
 * Handler for all nodes that require user location information to perform
 * correctly.
 * <p>
 *
 * The manager works for all nodes that require the user location information.
 * Typically these are sensors like ProximitySensor.
 * <p>
 *
 * The implementation uses a single pick to find all the nodes that are
 * intersect with the user's position and orientation. It assumes the user
 * is a point location in space and performs a pick based on this value.
 * Anything discovered by this means will then have the appropriate methods
 * on the listener called.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
class AreaManager {

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 25;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 25;

    /** The root of the SG */
    private Group worldRoot;

    /** Two sets used to determine when a viewSensor becomes inactive */
    private HashSet activeObjects;

    /** The newly visible sensors this frame. */
    private HashSet newObjects;

    /** A scratch list used to avoid garbage generation */
    private OGLAreaListener[] list;

    /** Last item in the list */
    private int lastListener;

    /** Pick request object for terrain */
    private PickRequest rayPicker;

    /** Temp variable for fetching the local transformation values */
    private Matrix4f localTx;

    /** Pick request object for terrain */
    private PickRequest picker;


    /**
     * Construct a new manager for area objects.
     */
    AreaManager() {

        picker = new PickRequest();
        picker.pickType = PickRequest.FIND_PROXIMITY;
        picker.pickGeometryType = PickRequest.PICK_POINT;
        picker.pickSortType = PickRequest.SORT_ALL;
        picker.generateVWorldMatrix = true;
        picker.foundPaths = new ArrayList();

        localTx = new Matrix4f();
        activeObjects = new HashSet();
        newObjects = new HashSet();

        list = new OGLAreaListener[LIST_START_SIZE];
        lastListener = 0;
    }

    /**
     * Add a new sensor node instance to this handler.
     *
     * @param node The node instance to add
     */
    void addSensor(VRMLSensorNodeType node) {
    }

    /**
     * Remove a sensor node instance from this handler.
     *
     * @param node The node instance to remove
     */
    void removeSensor(VRMLSensorNodeType node) {
    }

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    void setWorldRoot(Group root) {
        worldRoot = root;
    }

    /**
     * Clear the current list of items internally. This is because the world
     * has changed and that the currently cached data is no longer usable.
     */
    void clear() {
        activeObjects.clear();
        newObjects.clear();
    }

    /**
     * Process this frame using the given position and orientation of the
     * user position in Virtual world coordinates.
     *
     * @param pos The position of the user
     * @param orient The orientation of the user
     * @param limit The visibility limit
     */
    void processFrame(Point3f pos,
                      Vector3f orient,
                      Matrix4f vpMatrix,
                      float limit) {

        if(worldRoot == null)
            return;

        // Calculates the end point from the params
        picker.origin[0] = pos.x;
        picker.origin[1] = pos.y;
        picker.origin[2] = pos.z;

        worldRoot.pickSingle(picker);

        if(picker.pickCount != 0) {

            // Clear the visited entries for active VisSensors
            newObjects.clear();
            ArrayList pick_list = (ArrayList)picker.foundPaths;

            for(int i = 0; i < picker.pickCount; i++) {
                SceneGraphPath found = (SceneGraphPath)pick_list.get(i);

                Node n = found.getTerminalNode();
                OGLUserData user_data = (OGLUserData)n.getUserData();

                if((user_data != null) && (user_data.areaListener != null)) {
                    OGLAreaListener l = user_data.areaListener;
                    found.getTransform(localTx);

                    if(activeObjects.contains(l)) {
                        l.userPositionChanged(pos, orient, vpMatrix, localTx);

                        activeObjects.remove(l);
                    } else {
                        l.areaEntry(pos, orient, vpMatrix, localTx);
                    }

                    newObjects.add(l);
                }
            }
        }

        int size = activeObjects.size();

        if(size != 0) {
            // Remove unvisited entries
            resizeList(size);

            activeObjects.toArray(list);

            for(int i = 0; i < size; i++) {
                if(list[i] == null)
                    break;

                OGLAreaListener l = (OGLAreaListener)list[i];

                l.areaExit();
                activeObjects.remove(l);
            }
        }

        // Swap over lists
        HashSet tmp = activeObjects;

        activeObjects = newObjects;
        newObjects = tmp;
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

            OGLAreaListener [] tmp_nodes = new OGLAreaListener[new_size];

            System.arraycopy(list, 0, tmp_nodes, 0, old_size);

            list = tmp_nodes;
        }
    }
}
