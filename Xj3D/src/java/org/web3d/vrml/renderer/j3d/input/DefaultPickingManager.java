/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
import javax.media.j3d.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.j3d.geom.GeometryData;
import org.j3d.renderer.java3d.util.J3DIntersectionUtils;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.FloatArray;
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.util.NodeArray;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;
import org.web3d.vrml.renderer.j3d.nodes.J3DPickableTargetNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DPickingSensorNodeType;

import org.xj3d.core.eventmodel.PickingManager;

/**
 * Manager for processing the functionality of the Picking Utilities component.
 * <p>
 *
 * Picking and, ultimately, n-body object collision detection is handled by
 * this manager.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class DefaultPickingManager implements J3DPickingManager {

    /** Increment size constant */
    private static final int ARRAY_INC = 50;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** All the sensors being processed currently */
    private NodeArray sensorList;

    /** Set of currently active sensors */
    private HashSet activeSensors;

    /** Mapping of the active sensor node instance to it's current path */
    private HashMap activePaths;

    /** Mapping of the active sensor path to a link valid state */
    private HashMap pathLinkStatus;

    /** The world root we pick against to find sensors */
    private BranchGroup worldRoot;

    /** Pick shape for doing conical picking */
    private PickConeSegment conePicker;

    /** Pick shape for doing box picking */
    private PickBounds boxPicker;

    /** Bounds used for the box picker */
    private BoundingBox boxBounds;

    /** Pick shape for doing spherical picking */
    private PickBounds spherePicker;

    /** Bounds used for the sphere picker */
    private BoundingSphere sphereBounds;

    /** Pick shape for doing cylindrical picking */
    private PickCylinderSegment cylinderPicker;

    /** Pick shape for doing line segment picking */
    private PickSegment linePicker;

    /** Pick shape for doing point picking */
    private PickPoint pointPicker;

    /** Last node index in the array */
    private int lastNodeIndex;

    /** Last coordinate/normal/texture index in the array */
    private int lastCoordIndex;

    /** Array used to pass the found nodes to the sensor */
    private VRMLNodeType[] nodeList;

    /** Array used to pass the found coordinates to the sensor */
    private float[] coordList;

    /** Array used to pass the found normals to the sensor */
    private float[] normalList;

    /** Array used to pass the found texture coordinates to the sensor */
    private float[] textureList;

    /** Work variable to copy the point location into an item */
    private Point3d tmpPoint1;
    private Point3d tmpPoint2;
    private Point3d wkPoint;
    private Vector3d wkDirection;

    /** Transform for the pick geometry to v-world coords */
    private Transform3D pickTransform;

    /** The class used to perform exact intersection */
    private J3DIntersectionUtils iutils;

    /** The current hit point in the sensor's coordinate system */
    private float[] hitPoint;

    /** The current normal in the geometry coordinate system */
    private float[] hitNormal;

    /**
     * The current texture coordinate in the geometry coordinate system.
     * The array will have 3 indexes so that it will cope with 3D textures
     * if and when VRML gets them. For the moment, the code only works with
     * 2D textures and the third index will always be zero.
     */
    private float[] hitTexCoord;

    /**
     * Vector for doing difference calculations on the point we have and the
     * next while doing the pick geometry processing.
     */
    private Vector3d diffVec;

    /**
     * Construct a new instance of this class.
     */
    public DefaultPickingManager() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        sensorList = new NodeArray();
        activeSensors = new HashSet();
        activePaths = new HashMap();
        pathLinkStatus = new HashMap();

        // Setup each of the picking objects
        boxBounds = new BoundingBox();
        sphereBounds = new BoundingSphere();

        conePicker = new PickConeSegment();
        boxPicker = new PickBounds(boxBounds);
        spherePicker = new PickBounds(sphereBounds);
        cylinderPicker = new PickCylinderSegment();
        linePicker = new PickSegment();
        pointPicker = new PickPoint();

        // some random startup size
        nodeList = new VRMLNodeType[100];
        coordList = new float[300];
        normalList = new float[300];
        textureList = new float[300];

        hitPoint = new float[3];
        hitNormal = new float[3];
        hitTexCoord = new float[3];

        tmpPoint1 = new Point3d();
        tmpPoint2 = new Point3d();
        wkPoint = new Point3d();
        wkDirection = new Vector3d();
        diffVec = new Vector3d();

        pickTransform = new Transform3D();
        iutils = new J3DIntersectionUtils();
    }

    //-------------------------------------------------------------
    // Methods defined by PickingManager
    //-------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
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
     * Process the list of picking sensors now.
     *
     * @param time The timestamp for "now"
     */
    public void processPickSensors(double time) {
        int size = sensorList.size();

        for(int i = 0; i < size; i++) {
            J3DPickingSensorNodeType sensor =
                (J3DPickingSensorNodeType)sensorList.get(i);

            if(!sensor.getEnabled())
                continue;

            Map targets = sensor.getTargetGroups();

            if(targets.size() == 0)
                continue;

            checkIntersections(sensor, targets);
        }
    }

    /**
     * Add a new sensor instance to the system for processing.
     *
     * @param sensor The sensor instance to add
     */
    public void addSensor(VRMLPickingSensorNodeType sensor) {
        sensorList.add(sensor);
    }

    /**
     * Cleanup the given sensors and remove them from the list of processing
     * to be done each frame. The list will be created elsewhere (typically the
     * per-frame behaviour as a result of the event model processing) and
     * passed to this manager. The given list will contain instances of
     * VRMLSensorNodeType. There will be no protos as this is just the raw
     * sensor nodes internally.
     *
     * @param sensor The sensor instance to remove
     */
    public void removeSensor(VRMLPickingSensorNodeType sensor) {
        sensorList.remove(sensor);
    }

    /**
     * Load the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void loadScene(BasicScene scene) {
        ArrayList nodes =
            scene.getByPrimaryType(TypeConstants.PickingSensorNodeType);

        int i;
        int size = nodes.size();

        for(i = 0; i < size; i++)
            sensorList.add((VRMLNodeType)nodes.get(i));
    }

    /**
     * Unload the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void unloadScene(BasicScene scene) {
        ArrayList nodes =
            scene.getByPrimaryType(TypeConstants.PickingSensorNodeType);

        int i;
        int size = nodes.size();

        for(i = 0; i < size; i++)
            sensorList.remove((VRMLNodeType)nodes.get(i));
    }

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear() {
        sensorList.clear();
        activeSensors.clear();
    }

    //-------------------------------------------------------------
    // Internal methods
    //-------------------------------------------------------------

    /**
     * Process a single node for intersection checks now
     */
    private void checkIntersections(VRMLPickingSensorNodeType node,
                                    Map targets) {

        VRMLNodeType pick_geom = node.getPickingGeometry();
        VRMLGeometryNodeType real_geom = findRealGeometry(pick_geom);
        int sort_type = node.getSortOrder();
        int pick_type = node.getIntersectionType();
        J3DPickingSensorNodeType sensor = (J3DPickingSensorNodeType)node;
        SceneGraphPath pick_path = null;

        // Is it an active sensor? If so, see if the root path has changed
        // and update if it has.
        if(activeSensors.contains(node)) {
            if(sensor.hasScenePathChanged()) {
                pick_path = sensor.getSceneGraphPath();
                Boolean has_links =
                    checkForLinks(pick_path) ? Boolean.TRUE : Boolean.FALSE;

                activePaths.put(node, pick_path);
                pathLinkStatus.put(pick_path, has_links);
            } else
                pick_path = (SceneGraphPath)activePaths.get(node);
        } else {
            pick_path = sensor.getSceneGraphPath();
            Boolean has_links =
                checkForLinks(pick_path) ? Boolean.TRUE : Boolean.FALSE;

            activePaths.put(node, pick_path);
            pathLinkStatus.put(pick_path, has_links);
        }

        switch(node.getPickingType()) {
            case VRMLPickingSensorNodeType.POINT_PICK:
                processPointPicking(pick_path,
                                    pick_type,
                                    sort_type,
                                    real_geom,
                                    targets);
                break;

            case VRMLPickingSensorNodeType.LINE_PICK:
                processLinePicking(pick_path,
                                   pick_type,
                                   sort_type,
                                   real_geom,
                                   targets);
                break;

            case VRMLPickingSensorNodeType.SPHERE_PICK:
                processSpherePicking(pick_path,
                                     pick_type,
                                     sort_type,
                                     real_geom,
                                     targets);
                break;

            case VRMLPickingSensorNodeType.BOX_PICK:
                processBoxPicking(pick_path,
                                  pick_type,
                                  sort_type,
                                  real_geom,
                                  targets);
                break;

            case VRMLPickingSensorNodeType.CONE_PICK:
                processConePicking(pick_path,
                                   pick_type,
                                   sort_type,
                                   real_geom,
                                   targets);
                break;

            case VRMLPickingSensorNodeType.CYLINDER_PICK:
                processCylinderPicking(pick_path,
                                       pick_type,
                                       sort_type,
                                       real_geom,
                                       targets);
                break;

            case VRMLPickingSensorNodeType.VOLUME_PICK:
                processVolumePicking(pick_path,
                                     pick_type,
                                     sort_type,
                                     real_geom,
                                     targets);
                break;

            default:
                errorReporter.warningReport("Unknown picking type defined", null);
        }

        // Send the details off to the sensor node.
        try {
            if(lastNodeIndex != 0) {
                if(activeSensors.contains(node)) {
                    node.notifyPickChange(lastNodeIndex,
                                          nodeList,
                                          coordList,
                                          normalList,
                                          textureList);
                } else {
                    activeSensors.add(node);
                    node.notifyPickStart(lastNodeIndex,
                                         nodeList,
                                         coordList,
                                         normalList,
                                         textureList);
                }
            } else if(activeSensors.contains(node)) {
                // mark the sensor as no longer active.
                node.notifyPickEnd();
                activeSensors.remove(node);
                activePaths.remove(node);
                pathLinkStatus.remove(pick_path);
            }
        } catch(Exception e) {
            errorReporter.errorReport("Sending output to PickingSensor", e);
        }
    }

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processPointPicking(SceneGraphPath pickPath,
                                     int intersectType,
                                     int sortOrder,
                                     VRMLGeometryNodeType geom,
                                     Map targets) {

        // First go find the coordinates of the points
        VRMLComponentGeometryNodeType comp_geom =
            (VRMLComponentGeometryNodeType)geom;

        VRMLNodeType[] components = comp_geom.getComponents();

        if(components == null || components.length == 0)
            return;

        VRMLCoordinateNodeType coord = null;

        for(int i = 0; i < components.length && coord == null; i++)
            coord = findRealCoordinates(components[i]);

        if(coord == null)
            return;

        // run through every point on one piece of geometry before moving
        // to the next. Right now, ignores the pick type. Only does bounds
        // picking.

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        float[] coord_data = coord.getPointRef();
        int num_points = coord.getNumPoints() / 3;

        int idx = 0;
        SceneGraphPath[] potentials = null;

        // The local to VWorld transform for the location that the pick
        // geometry is in. Need this to transform the picking point to world
        // space coordinates as we are picking from the world root.
        if(pickPath == null) {
            pickTransform.setIdentity();
        } else {
            Node terminal_node = pickPath.getObject();

            Boolean bool = (Boolean)pathLinkStatus.get(pickPath);

            if(bool.booleanValue())
                terminal_node.getLocalToVworld(pickPath, pickTransform);
            else
                terminal_node.getLocalToVworld(pickTransform);
        }

        for(int i = 0; i < num_points; i++) {
            tmpPoint1.x = coord_data[idx++];
            tmpPoint1.y = coord_data[idx++];
            tmpPoint1.z = coord_data[idx++];

            pickTransform.transform(tmpPoint1);
            pointPicker.set(tmpPoint1);

            potentials = worldRoot.pickAll(pointPicker);

            if(potentials != null && potentials.length != 0) {

                // Second set of case statemets to know whether to terminate
                // completely, or start doing some sorting and classifying of the
                // found results.
                //
                // For each potential pick, walk down the scene graph path and see
                // if each item in the branch exists in the set of target nodes.
                // When a match is found then do the sortOrder specific action.
                boolean terminate = false;

                for(int j = 0; j < potentials.length; j++) {
                    SceneGraphPath path = potentials[j];
                    int len = path.nodeCount();
                    terminate = false;

                    for(int k = 0; !terminate && k < len; k++) {
                        Node node = path.getNode(k);
                        if(targets.containsKey(node)) {
                            VRMLNodeType n = (VRMLNodeType)targets.get(node);
                            switch(sortOrder) {
                                case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                                case VRMLPickingSensorNodeType.SORT_ALL:
                                    checkArraySize(1);
                                    nodeList[lastNodeIndex++] = n;
                                    terminate = false;
                                    break;

                                case VRMLPickingSensorNodeType.SORT_CLOSEST:
                                case VRMLPickingSensorNodeType.SORT_ANY:
                                    checkArraySize(1);
                                    nodeList[lastNodeIndex++] = n;
                                    terminate = true;
                                    break;
                            }
                        }
                    }
                }

                // Exit out of the outer loop too.
                if(terminate)
                    break;
            }
        }
    }

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processLinePicking(SceneGraphPath pickPath,
                                    int intersectType,
                                    int sortOrder,
                                    VRMLGeometryNodeType geom,
                                    Map targets) {

        // First go find the coordinates of the points
        VRMLComponentGeometryNodeType comp_geom =
            (VRMLComponentGeometryNodeType)geom;

        VRMLNodeType[] components = comp_geom.getComponents();

        if(components == null || components.length == 0)
            return;

        VRMLCoordinateNodeType coord_node = null;

        for(int i = 0; i < components.length && coord_node == null; i++)
            coord_node = findRealCoordinates(components[i]);

        if(coord_node == null)
            return;

        // Are we using indexed geometry? There's no good way of testing this
        // so just look for the coordIndex field. If it exists (ie we get a
        // valid ID, not -1), then work in indexed mode, otherwise work in
        // single segment mode.
        VRMLFieldData field_data;
        int field_idx = geom.getFieldIndex("coordIndex");
        int[] index_list = null;
        int   num_index = 0;

        if(field_idx != -1) {
            field_data = geom.getFieldValue(field_idx);
            index_list = field_data.intArrayValue;
            num_index = field_data.numElements;
        }


        // run through every point on one piece of geometry before moving
        // to the next. Right now, ignores the pick type. Only does bounds
        // picking.

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        float[] coords = coord_node.getPointRef();
        int num_points = coord_node.getNumPoints() / 3;

        int idx = 0;
        SceneGraphPath[] potentials = null;

        // The local to VWorld transform for the location that the pick
        // geometry is in. Need this to transform the picking point to world
        // space coordinates as we are picking from the world root.
        if(pickPath == null) {
            pickTransform.setIdentity();
        } else {
            Node terminal_node = pickPath.getObject();

            Boolean bool = (Boolean)pathLinkStatus.get(pickPath);

            if(bool.booleanValue())
                terminal_node.getLocalToVworld(pickPath, pickTransform);
            else
                terminal_node.getLocalToVworld(pickTransform);
        }

        if(num_index == 0) {
            boolean search_complete = false;
            double closest_point;

            for(int i = 0; i < num_points && !search_complete; i++) {
                tmpPoint1.x = coords[idx++];
                tmpPoint1.y = coords[idx++];
                tmpPoint1.z = coords[idx++];

                tmpPoint2.x = coords[idx++];
                tmpPoint2.y = coords[idx++];
                tmpPoint2.z = coords[idx++];

                pickTransform.transform(tmpPoint1);
                pickTransform.transform(tmpPoint2);

                linePicker.set(tmpPoint1, tmpPoint2);

                // Geometry or bounds based intersection testing?
                if(intersectType != VRMLPickingSensorNodeType.INTERSECT_BOUNDS) {
                    potentials = worldRoot.pickAll(linePicker);

                    // Second set of case statemets to know whether to terminate
                    // completely, or start doing some sorting and classifying of the
                    // found results.
                    //
                    // For each potential pick, walk down the scene graph path and see
                    // if each item in the branch exists in the set of target nodes.
                    // When a match is found then do the sortOrder specific action.
                    int size = potentials == null ? 0 : potentials.length;
                    boolean terminate = false;

                    for(int j = 0; j < potentials.length; j++) {
                        SceneGraphPath path = potentials[j];
                        int len = path.nodeCount();
                        terminate = false;

                        for(int k = 0; !terminate && k < len; k++) {
                            Node node = path.getNode(k);
                            if(targets.containsKey(node)) {
                                VRMLNodeType n = (VRMLNodeType)targets.get(node);

                                // Set up the pick direction vector then look
                                // for more details.
                                wkDirection.x = tmpPoint2.x - tmpPoint1.x;
                                wkDirection.y = tmpPoint2.y - tmpPoint1.y;
                                wkDirection.z = tmpPoint2.z - tmpPoint1.z;

                                // Now work out if it is a direct hit on the
                                // geometry....
                                double dist = detailLinePick(path);

                                if(dist < 0) {
                                    // do we want to continue working down the
                                    // tree? Not really sure. Left out for now.
                                    terminate = true;
                                    continue;
                                }

                                switch(sortOrder) {
                                    case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                                    case VRMLPickingSensorNodeType.SORT_ALL:
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;
                                        coordList[lastCoordIndex] = hitPoint[0];
                                        coordList[lastCoordIndex + 1] = hitPoint[1];
                                        coordList[lastCoordIndex + 2] = hitPoint[2];

                                        normalList[lastCoordIndex] = hitNormal[0];
                                        normalList[lastCoordIndex + 1] = hitNormal[1];
                                        normalList[lastCoordIndex + 2] = hitNormal[2];

                                        textureList[lastCoordIndex] = hitTexCoord[0];
                                        textureList[lastCoordIndex + 1] = hitTexCoord[1];
                                        textureList[lastCoordIndex + 2] = hitTexCoord[2];

                                        lastCoordIndex += 3;
                                        terminate = false;
                                        break;

                                    case VRMLPickingSensorNodeType.SORT_CLOSEST:
                                    case VRMLPickingSensorNodeType.SORT_ANY:
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;

                                        coordList[lastCoordIndex] = hitPoint[0];
                                        coordList[lastCoordIndex + 1] = hitPoint[1];
                                        coordList[lastCoordIndex + 2] = hitPoint[2];

                                        normalList[lastCoordIndex] = hitNormal[0];
                                        normalList[lastCoordIndex + 1] = hitNormal[1];
                                        normalList[lastCoordIndex + 2] = hitNormal[2];

                                        textureList[lastCoordIndex] = hitTexCoord[0];
                                        textureList[lastCoordIndex + 1] = hitTexCoord[1];
                                        textureList[lastCoordIndex + 2] = hitTexCoord[2];

                                        lastCoordIndex += 3;
                                        terminate = true;
                                        break;
                                }
                            }
                        }
                    }

                    // Exit out of the outer loop too.
                    if(terminate)
                        search_complete = true;
                } else {
                    // bounds intersection testing.
                    int size;
                    boolean terminate;

                    switch(sortOrder) {
                        case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                        case VRMLPickingSensorNodeType.SORT_CLOSEST:
                            potentials = worldRoot.pickAllSorted(linePicker);

                            size = potentials == null ? 0 : potentials.length;
                            terminate = false;

                            for(int j = 0; !terminate && j < size; j++) {
                                SceneGraphPath path = potentials[j];
                                int len = path.nodeCount();
                                terminate = false;

                                for(int k = 0; !terminate && k < len; k++) {
                                    Node node = path.getNode(k);
                                    if(targets.containsKey(node)) {
                                        VRMLNodeType n = (VRMLNodeType)targets.get(node);
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;

                                        // Exit out of the local loop, but
                                        // keep going on the potentials list.
                                        if(sortOrder == VRMLPickingSensorNodeType.SORT_CLOSEST)
                                            terminate = true;

                                        break;
                                    }
                                }
                            }
                            break;

                        case VRMLPickingSensorNodeType.SORT_ALL:
                        case VRMLPickingSensorNodeType.SORT_ANY:
                            potentials = worldRoot.pickAll(linePicker);

                            size = potentials == null ? 0 : potentials.length;
                            terminate = false;

                            for(int j = 0; !terminate && j < size; j++) {
                                SceneGraphPath path = potentials[j];
                                int len = path.nodeCount();
                                terminate = false;

                                for(int k = 0; !terminate && k < len; k++) {
                                    Node node = path.getNode(k);
                                    if(targets.containsKey(node)) {
                                        VRMLNodeType n = (VRMLNodeType)targets.get(node);
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;
                                        search_complete = true;

                                        // Exit out of the local loop, but
                                        // keep going on the potentials list.
                                        if(sortOrder == VRMLPickingSensorNodeType.SORT_ANY)
                                            terminate = true;

                                        break;
                                    }
                                }
                            }

                            break;
                    }   // switch(sortOrder)
                } // End of bounds-based intersection test block
            }
        } else {
            // This is for indexed lines
            for(int i = 0; i < num_index - 1; i++) {
                int x1 = index_list[i] * 3;
                int x2 = index_list[i + 1] * 3;

                // Abort if there's a -1 and jump 2 places ahead
                if(x2 < 0) {
                    i++;
                    continue;
                }

                tmpPoint1.x = coords[x1];
                tmpPoint1.y = coords[x1 + 1];
                tmpPoint1.z = coords[x1 + 2];

                tmpPoint2.x = coords[x2];
                tmpPoint2.y = coords[x2 + 1];
                tmpPoint2.z = coords[x2 + 2];

                pickTransform.transform(tmpPoint1);
                pickTransform.transform(tmpPoint2);

                linePicker.set(tmpPoint1, tmpPoint2);

                // Geometry or bounds based intersection testing?
                if(intersectType != VRMLPickingSensorNodeType.INTERSECT_BOUNDS) {
                    potentials = worldRoot.pickAll(linePicker);

                    int size = potentials == null ? 0 : potentials.length;
                    boolean terminate = false;

                    for(int j = 0; j < size; j++) {
                        SceneGraphPath path = potentials[j];
                        int len = path.nodeCount();
                        terminate = false;

                        for(int k = 0; !terminate && k < len; k++) {
                            Node node = path.getNode(k);
                            if(targets.containsKey(node)) {
                                VRMLNodeType n = (VRMLNodeType)targets.get(node);

                                // Set up the pick direction vector then look
                                // for more details.
                                wkDirection.x = tmpPoint2.x - tmpPoint1.x;
                                wkDirection.y = tmpPoint2.y - tmpPoint1.y;
                                wkDirection.z = tmpPoint2.z - tmpPoint1.z;

                                // Now work out if it is a direct hit on the
                                // geometry....
                                double dist = detailLinePick(path);

                                if(dist < 0) {
                                    // do we want to continue working down the
                                    // tree? Not really sure. Left out for now.
                                    // terminate = true;
                                    continue;
                                }

                                switch(sortOrder) {
                                    case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                                    case VRMLPickingSensorNodeType.SORT_ALL:
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;
                                        coordList[lastCoordIndex] = hitPoint[0];
                                        coordList[lastCoordIndex + 1] = hitPoint[1];
                                        coordList[lastCoordIndex + 2] = hitPoint[2];

                                        normalList[lastCoordIndex] = hitNormal[0];
                                        normalList[lastCoordIndex + 1] = hitNormal[1];
                                        normalList[lastCoordIndex + 2] = hitNormal[2];

                                        textureList[lastCoordIndex] = hitTexCoord[0];
                                        textureList[lastCoordIndex + 1] = hitTexCoord[1];
                                        textureList[lastCoordIndex + 2] = hitTexCoord[2];

                                        lastCoordIndex += 3;
                                        terminate = false;
                                        break;

                                    case VRMLPickingSensorNodeType.SORT_CLOSEST:
                                    case VRMLPickingSensorNodeType.SORT_ANY:
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;
                                        coordList[lastCoordIndex] = hitPoint[0];
                                        coordList[lastCoordIndex + 1] = hitPoint[1];
                                        coordList[lastCoordIndex + 2] = hitPoint[2];

                                        normalList[lastCoordIndex] = hitNormal[0];
                                        normalList[lastCoordIndex + 1] = hitNormal[1];
                                        normalList[lastCoordIndex + 2] = hitNormal[2];

                                        textureList[lastCoordIndex] = hitTexCoord[0];
                                        textureList[lastCoordIndex + 1] = hitTexCoord[1];
                                        textureList[lastCoordIndex + 2] = hitTexCoord[2];

                                        lastCoordIndex += 3;
                                        terminate = true;
                                        break;
                                }
                            }
                        }
                    }

                    // Exit out of the outer loop too.
                    if(terminate)
                        break;
                } else {
                    // bounds based interesection testing for indexed lines.
                    potentials = worldRoot.pickAll(linePicker);

                    int size = potentials == null ? 0 : potentials.length;
                    boolean terminate = false;

                    for(int j = 0; j < size; j++) {
                        SceneGraphPath path = potentials[j];
                        int len = path.nodeCount();
                        terminate = false;

                        for(int k = 0; !terminate && k < len; k++) {
                            Node node = path.getNode(k);
                            if(targets.containsKey(node)) {
                                VRMLNodeType n = (VRMLNodeType)targets.get(node);

                                switch(sortOrder) {
                                    case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                                    case VRMLPickingSensorNodeType.SORT_ALL:
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;
                                        terminate = false;
                                        break;

                                    case VRMLPickingSensorNodeType.SORT_CLOSEST:
                                    case VRMLPickingSensorNodeType.SORT_ANY:
                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;
                                        terminate = true;
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processSpherePicking(SceneGraphPath pickPath,
                                      int intersectType,
                                      int sortOrder,
                                      VRMLGeometryNodeType geom,
                                      Map targets) {
        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("radius");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);

        sphereBounds.setRadius(field_data.floatValue);
        tmpPoint1.x = 0;
        tmpPoint1.y = 0;
        tmpPoint1.z = 0;

        // if there's a null path then the picker is at the root of the
        // scene graph. There's no point transforming any points
        if(pickPath != null) {
            Node terminal_node = pickPath.getObject();

            Boolean bool = (Boolean)pathLinkStatus.get(pickPath);

            if(bool.booleanValue())
                terminal_node.getLocalToVworld(pickPath, pickTransform);
            else
                terminal_node.getLocalToVworld(pickTransform);

            pickTransform.transform(tmpPoint1);
        }

        sphereBounds.setCenter(tmpPoint1);

        //spherePicker.set(sphereBounds);

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        SceneGraphPath[] potentials = worldRoot.pickAll(spherePicker);

        if(potentials == null)
            return;

        // Second set of case statemets to know whether to terminate
        // completely, or start doing some sorting and classifying of the
        // found results.
        //
        // For each potential pick, walk down the scene graph path and see
        // if each item in the branch exists in the set of target nodes.
        // When a match is found then do the sortOrder specific action.

        for(int j = 0; j < potentials.length; j++) {
            SceneGraphPath path = potentials[j];
            int len = path.nodeCount();
            boolean terminate = false;

            for(int k = 0; !terminate && k < len; k++) {
                Node node = path.getNode(k);
                if(targets.containsKey(node)) {
                    VRMLNodeType n = (VRMLNodeType)targets.get(node);
                    switch(sortOrder) {
                        case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                        case VRMLPickingSensorNodeType.SORT_ALL:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = false;
                            break;

                        case VRMLPickingSensorNodeType.SORT_CLOSEST:
                        case VRMLPickingSensorNodeType.SORT_ANY:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = true;
                            break;
                    }
                }
            }
        }
    }


    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processBoxPicking(SceneGraphPath pickPath,
                                   int intersectType,
                                   int sortOrder,
                                   VRMLGeometryNodeType geom,
                                   Map targets) {

        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("size");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);
        float x = field_data.floatArrayValue[0] * 0.5f;
        float y = field_data.floatArrayValue[1] * 0.5f;
        float z = field_data.floatArrayValue[2] * 0.5f;

        tmpPoint1.x = -x;
        tmpPoint1.y = -y;
        tmpPoint1.z = -z;

        tmpPoint2.x = x;
        tmpPoint2.y = y;
        tmpPoint2.z = z;

        // The local to VWorld transform for the location that the pick
        // geometry is in. Need this to transform the picking point to world
        // space coordinates as we are picking from the world root.

        // if there's a null path then the picker is at the root of the
        // scene graph. There's no point transforming any points
        if(pickPath != null) {
            Node terminal_node = pickPath.getObject();

            Boolean bool = (Boolean)pathLinkStatus.get(pickPath);

            if(bool.booleanValue())
                terminal_node.getLocalToVworld(pickPath, pickTransform);
            else
                terminal_node.getLocalToVworld(pickTransform);

            pickTransform.transform(tmpPoint1);
            pickTransform.transform(tmpPoint2);
        }

        boxBounds.setLower(tmpPoint1);
        boxBounds.setUpper(tmpPoint2);

        // May need to reset the box here.

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        SceneGraphPath[] potentials = worldRoot.pickAll(boxPicker);

        if(potentials == null)
            return;

        // Second set of case statemets to know whether to terminate
        // completely, or start doing some sorting and classifying of the
        // found results.
        //
        // For each potential pick, walk down the scene graph path and see
        // if each item in the branch exists in the set of target nodes.
        // When a match is found then do the sortOrder specific action.

        for(int j = 0; j < potentials.length; j++) {
            SceneGraphPath path = potentials[j];
            int len = path.nodeCount();
            boolean terminate = false;

            for(int k = 0; !terminate && k < len; k++) {
                Node node = path.getNode(k);
                if(targets.containsKey(node)) {
                    VRMLNodeType n = (VRMLNodeType)targets.get(node);
                    switch(sortOrder) {
                        case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                        case VRMLPickingSensorNodeType.SORT_ALL:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = false;
                            break;

                        case VRMLPickingSensorNodeType.SORT_CLOSEST:
                        case VRMLPickingSensorNodeType.SORT_ANY:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = true;
                            break;
                    }
                }
            }
        }
    }

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processConePicking(SceneGraphPath pickPath,
                                    int intersectType,
                                    int sortOrder,
                                    VRMLGeometryNodeType geom,
                                    Map targets) {
        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("height");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);

        tmpPoint1.y = field_data.floatValue;

        // Calculate the spread angle from the radius and height. This is
        // expensive because of the tan being used every frame. May want to
        // investigate a more efficient method

        field_idx = geom.getFieldIndex("bottomRadius");
        field_data = geom.getFieldValue(field_idx);
        double spread = Math.tan(field_data.floatValue / tmpPoint1.y);

        // readjust again....
        tmpPoint1.x = 0;
        tmpPoint1.y *= 0.5f;
        tmpPoint1.z = 0;

        tmpPoint2.x = 0;
        tmpPoint2.y = -tmpPoint1.y;
        tmpPoint2.x = 0;

        // The local to VWorld transform for the location that the pick
        // geometry is in. Need this to transform the picking point to world
        // space coordinates as we are picking from the world root.
        // if there's a null path then the picker is at the root of the
        // scene graph. There's no point transforming any points
        if(pickPath != null) {
            Node terminal_node = pickPath.getObject();

            Boolean bool = (Boolean)pathLinkStatus.get(pickPath);

            if(bool.booleanValue())
                terminal_node.getLocalToVworld(pickPath, pickTransform);
            else
                terminal_node.getLocalToVworld(pickTransform);

            pickTransform.transform(tmpPoint1);
            pickTransform.transform(tmpPoint2);
        }

        conePicker.set(tmpPoint1, tmpPoint2, spread);

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        SceneGraphPath[] potentials = worldRoot.pickAll(conePicker);

        if(potentials == null)
            return;

        // Second set of case statemets to know whether to terminate
        // completely, or start doing some sorting and classifying of the
        // found results.
        //
        // For each potential pick, walk down the scene graph path and see
        // if each item in the branch exists in the set of target nodes.
        // When a match is found then do the sortOrder specific action.

        for(int j = 0; j < potentials.length; j++) {
            SceneGraphPath path = potentials[j];
            int len = path.nodeCount();
            boolean terminate = false;

            for(int k = 0; !terminate && k < len; k++) {
                Node node = path.getNode(k);
                if(targets.containsKey(node)) {
                    VRMLNodeType n = (VRMLNodeType)targets.get(node);
                    switch(sortOrder) {
                        case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                        case VRMLPickingSensorNodeType.SORT_ALL:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = false;
                            break;

                        case VRMLPickingSensorNodeType.SORT_CLOSEST:
                        case VRMLPickingSensorNodeType.SORT_ANY:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = true;
                            break;
                    }
                }
            }
        }
    }

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processCylinderPicking(SceneGraphPath pickPath,
                                        int intersectType,
                                        int sortOrder,
                                        VRMLGeometryNodeType geom,
                                        Map targets) {
        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("height");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);

        tmpPoint1.x = 0;
        tmpPoint1.y = field_data.floatValue * 0.5f;
        tmpPoint1.z = 0;

        tmpPoint2.x = 0;
        tmpPoint2.y = -tmpPoint1.y;
        tmpPoint2.x = 0;

        field_idx = geom.getFieldIndex("radius");
        field_data = geom.getFieldValue(field_idx);

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        // The local to VWorld transform for the location that the pick
        // geometry is in. Need this to transform the picking point to world
        // space coordinates as we are picking from the world root.
        // if there's a null path then the picker is at the root of the
        // scene graph. There's no point transforming any points
        if(pickPath != null) {
            Node terminal_node = pickPath.getObject();

            Boolean bool = (Boolean)pathLinkStatus.get(pickPath);

            if(bool.booleanValue())
                terminal_node.getLocalToVworld(pickPath, pickTransform);
            else
                terminal_node.getLocalToVworld(pickTransform);

            pickTransform.transform(tmpPoint1);
            pickTransform.transform(tmpPoint2);
        }

        cylinderPicker.set(tmpPoint1, tmpPoint2, field_data.floatValue);

        SceneGraphPath[] potentials = worldRoot.pickAll(cylinderPicker);

        if(potentials == null)
            return;

        // Second set of case statemets to know whether to terminate
        // completely, or start doing some sorting and classifying of the
        // found results.
        //
        // For each potential pick, walk down the scene graph path and see
        // if each item in the branch exists in the set of target nodes.
        // When a match is found then do the sortOrder specific action.

        for(int j = 0; j < potentials.length; j++) {
            SceneGraphPath path = potentials[j];
            int len = path.nodeCount();
            boolean terminate = false;

            for(int k = 0; !terminate && k < len; k++) {
                Node node = path.getNode(k);
                if(targets.containsKey(node)) {
                    VRMLNodeType n = (VRMLNodeType)targets.get(node);
                    switch(sortOrder) {
                        case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                        case VRMLPickingSensorNodeType.SORT_ALL:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = false;
                            break;

                        case VRMLPickingSensorNodeType.SORT_CLOSEST:
                        case VRMLPickingSensorNodeType.SORT_ANY:
                            checkArraySize(1);
                            nodeList[lastNodeIndex++] = n;
                            terminate = true;
                            break;
                    }
                }
            }
        }
    }

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processVolumePicking(SceneGraphPath pickPath,
                                      int intersectType,
                                      int sortOrder,
                                      VRMLGeometryNodeType geom,
                                      Map targets) {
    }

    /**
     * Walk down the set of node definitions, potentially inside a proto
     * that defines a geometry node type.
     *
     * @param n The node to test for geometry
     * @return The real geometry or null if not found.
     */
    private VRMLGeometryNodeType findRealGeometry(VRMLNodeType n) {
        // fast check as this will nearly always be the case
        if(n instanceof VRMLGeometryNodeType)
            return (VRMLGeometryNodeType)n;

        VRMLGeometryNodeType ret_val = null;
        VRMLNodeType current = n;

        while(ret_val == null) {
            if(current instanceof VRMLGeometryNodeType)
                ret_val = (VRMLGeometryNodeType)current;
            else if(current instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)current;
                current = proto.getImplementationNode();
            } else {
System.out.println("Non-geometry proto found. Maybe extern proto?");
                break;
            }
        }

        return ret_val;
    }

    /**
     * Walk down the set of node definitions, potentially inside a proto
     * that defines a coordinate node type.
     *
     * @param n The node to test for coordinate
     * @return The real geometry or null if not found.
     */
    private VRMLCoordinateNodeType findRealCoordinates(VRMLNodeType n) {
        // fast check as this will nearly always be the case
        if(n instanceof VRMLCoordinateNodeType)
            return (VRMLCoordinateNodeType)n;

        VRMLCoordinateNodeType ret_val = null;
        VRMLNodeType current = n;

        while(ret_val == null) {
            if(current instanceof VRMLCoordinateNodeType)
                ret_val = (VRMLCoordinateNodeType)current;
            else if(current instanceof VRMLProtoInstance) {
                VRMLProtoInstance proto = (VRMLProtoInstance)current;
                current = proto.getImplementationNode();
            } else {
System.out.println("Non-coordinate proto found. Maybe extern proto?");
                break;
            }
        }

        return ret_val;
    }

    /**
     * Convenience methods to check that the initial array size is enough
     *
     * @param inc The number of items to increments the array by
     */
    private void checkArraySize(int inc) {

        if(lastNodeIndex + inc < nodeList.length)
            return;

        // resize all
        int size = nodeList.length + ARRAY_INC;
        if(size < lastNodeIndex + inc)
            size += inc;

        VRMLNodeType[] tmp1 = new VRMLNodeType[size];
        float[] tmp2 = new float[size * 3];
        float[] tmp3 = new float[size * 3];
        float[] tmp4 = new float[size * 3];

        System.arraycopy(nodeList, 0, tmp1, 0, lastNodeIndex);
        System.arraycopy(coordList, 0, tmp2, 0, lastCoordIndex);
        System.arraycopy(normalList, 0, tmp3, 0, lastCoordIndex);
        System.arraycopy(textureList, 0, tmp4, 0, lastCoordIndex);

        nodeList = tmp1;
        coordList = tmp2;
        normalList = tmp3;
        textureList = tmp4;
    }

    /**
     * Convenience method to check whether the given scene graph path contains
     * any link nodes.
     *
     * @param path The path to check
     * @return true if there are link nodes found
     */
    private boolean checkForLinks(SceneGraphPath path) {
        boolean ret_val = false;

        if (path == null)
           return false;

        int size = path.nodeCount();

        for(int i = 0; i < size && !ret_val; i++) {
            if(path.getNode(i) instanceof Link)
                ret_val = true;
        }

        return ret_val;
    }

    /**
     * Process the line picking for a detailed answer about where the line
     * intersects the geometry. If there is no intersection, the distance will
     * be a negative number.
     */
    private double detailLinePick(SceneGraphPath path) {

        // Use exact geometry intersection to find the "one true"
        Node node = path.getObject();
        Transform3D local_tx;
        Enumeration geom_list;
        GeometryArray geom;
        double shortest_length = Double.POSITIVE_INFINITY;
        boolean length_set = false;
        int shortest = -1;

        if(node instanceof Shape3D) {
            local_tx = path.getTransform();

            try {
                geom_list = ((Shape3D)node).getAllGeometries();
            } catch (CapabilityNotSetException cnse) {
                return -1;
            }

            while(geom_list.hasMoreElements()) {
                geom = (GeometryArray)geom_list.nextElement();

                J3DUserData u_data = (J3DUserData)geom.getUserData();

                boolean found = false;
                if((u_data != null) && (u_data.geometryData != null) &&
                   (u_data.geometryData instanceof GeometryData))
                {
                    GeometryData gd = (GeometryData)u_data.geometryData;

                    found = iutils.rayUnknownGeometry(tmpPoint1,
                                                      wkDirection,
                                                      0,
                                                      gd,
                                                      local_tx,
                                                      wkPoint,
                                                      true);
                } else {
                    found = iutils.rayUnknownGeometry(tmpPoint1,
                                                      wkDirection,
                                                      0,
                                                      geom,
                                                      local_tx,
                                                      wkPoint,
                                                      true);
                }

                if(found) {
                    diffVec.sub(tmpPoint1, wkPoint);

                    if(diffVec.lengthSquared() < shortest_length) {
                        shortest_length = diffVec.lengthSquared();
                        hitPoint[0] = (float)wkPoint.x;
                        hitPoint[1] = (float)wkPoint.y;
                        hitPoint[2] = (float)wkPoint.z;

                        // Need to also calculate normals and texCoords here
                        length_set = true;
                    }
                }
            }
        }

        return length_set ? shortest_length : -1;
    }
}
