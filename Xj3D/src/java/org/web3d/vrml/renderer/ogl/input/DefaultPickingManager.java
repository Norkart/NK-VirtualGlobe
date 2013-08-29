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

package org.web3d.vrml.renderer.ogl.input;

// External imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.j3d.geom.GeometryData;
import org.j3d.aviatrix3d.picking.PickableObject;
import org.j3d.aviatrix3d.picking.PickRequest;
import org.j3d.renderer.aviatrix3d.util.AVIntersectionUtils;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.FloatArray;
import org.web3d.util.HashSet;
import org.web3d.util.IntArray;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.util.NodeArray;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickableTargetNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickingFlagConvertor;
import org.web3d.vrml.renderer.ogl.nodes.OGLPickingSensorNodeType;

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
public class DefaultPickingManager
    implements PickingManager, OGLPickingFlagConvertor {

    /** Increment size constant */
    private static final int ARRAY_INC = 50;

    /** Bitmask used when the object type string is ALL */
    private static final int ALL_BITMASK = 0xFFFFFFFF;

    /** Bitmask used when the object type is NONE */
    private static final int NONE_BITMASK = 0x0;

    /** Bitmask used when there's too many object types */
    private static final int EXTENDED_BITMASK = 0x7FFFFFFF;

    /** Collection of all of the currently available bitmasks */
    private IntArray availableBitmasks;

    /** A mapping of the object type name string to an Integer bitmask */
    private HashMap assignedBitmasks;

    /** A count for the number of users of a particular mask */
    private HashMap bitmaskCount;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** All the sensors being processed currently */
    private NodeArray sensorList;

    /** Set of currently active sensors */
    private HashSet activeSensors;

    /** Mapping of the active sensor node instance to it's current path */
    private HashMap activePaths;

    /** Pick request for single picking - eg point or line*/
    private PickRequest singlePicker;

    /** Pick shape for batch picking when we know we have a lot of requests */
    private PickRequest[] batchPicker;

    /** Complete vworld matrix of the picker from this point */
    private Matrix4f pickMatrix;

    /** Stack for calculating pickMatrix */
    private Matrix4f[] matrixStack;

    /** Tmp matrix for fetching vworld matrix info from a path */
    private Matrix4f tmpMatrix;

    /** Temporary array for fooling the picking system */
    private ArrayList selectionArray;

    /** Last node index in the nodeList array */
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

    /** The class used to perform exact intersection */
    private AVIntersectionUtils iutils;

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
        selectionArray = new ArrayList();

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
        tmpMatrix = new Matrix4f();

        iutils = new AVIntersectionUtils();

        singlePicker = new PickRequest();
        batchPicker = new PickRequest[4];

        for(int i = 0; i < 4; i++)
            batchPicker[i] = new PickRequest();

        availableBitmasks = new IntArray(32);
        assignedBitmasks = new HashMap();
        bitmaskCount = new HashMap();

        int mask = 0x01;

        for(int i = 0; i < 30; i++) {
            availableBitmasks.add(mask);
            mask <<= 1;
        }

        pickMatrix = new Matrix4f();
        matrixStack = new Matrix4f[16];
        for(int i = 0; i < 16; i++)
            matrixStack[i] = new Matrix4f();
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
     * Process the list of picking sensors now.
     *
     * @param time The timestamp for "now"
     */
    public void processPickSensors(double time) {
        int size = sensorList.size();

        for(int i = 0; i < size; i++) {
            OGLPickingSensorNodeType sensor =
                (OGLPickingSensorNodeType)sensorList.get(i);

            if(!sensor.getEnabled())
                continue;

            Map targets = sensor.getTargetMapping();

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
        ((OGLPickingSensorNodeType)sensor).setTypeConvertor(this);
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
        ((OGLPickingSensorNodeType)sensor).setTypeConvertor(null);
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

        for(i = 0; i < size; i++) {
            OGLPickingSensorNodeType node =
                (OGLPickingSensorNodeType)nodes.get(i);

            sensorList.add(node);
            node.setTypeConvertor(this);
        }

        nodes = scene.getBySecondaryType(TypeConstants.PickTargetNodeType);

        size = nodes.size();

        for(i = 0; i < size; i++) {
            OGLPickableTargetNodeType node =
                (OGLPickableTargetNodeType)nodes.get(i);

            node.setTypeConvertor(this);
        }
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
    // Methods defined by OGLPickingFlagConvertor
    //-------------------------------------------------------------

    /**
     * Add a new flag to the system and get told what bitmask to use.
     *
     * @param type The type string to use
     * @return An int bit mask to be applied
     */
    public int addObjectType(String type) {
        int ret_val = 0;

        // do we have this already? If so, update the ref count.
        Integer mask = (Integer)assignedBitmasks.get(type);
        if(mask != null) {
            Integer count = (Integer)bitmaskCount.get(type);
            bitmaskCount.put(type, new Integer(count.intValue() + 1));

            ret_val = mask.intValue();
        } else if(availableBitmasks.size() == 0) {
            assignedBitmasks.put(type, new Integer(EXTENDED_BITMASK));
            bitmaskCount.put(type, new Integer(1));

            ret_val = EXTENDED_BITMASK;
        } else {
            ret_val = availableBitmasks.remove(0);
            assignedBitmasks.put(type, new Integer(ret_val));
            bitmaskCount.put(type, new Integer(1));
        }

        return ret_val;
    }

    /**
     * Notify the system that the flag is no longer being used by this node.
     *
     * @param type The type string to use
     */
    public void removeObjectType(String type) {
        Integer count = (Integer)bitmaskCount.get(type);
        if(count.intValue() == 1) {
            bitmaskCount.remove(type);
            Integer mask = (Integer)assignedBitmasks.remove(type);
            availableBitmasks.add(mask.intValue());
        } else {
            bitmaskCount.put(type, new Integer(count.intValue() - 1));
        }
    }

    //-------------------------------------------------------------
    // Internal methods
    //-------------------------------------------------------------

    /**
     * Process a single node for intersection checks now
     */
    private void checkIntersections(OGLPickingSensorNodeType sensor,
                                    Map targetMap) {

        VRMLNodeType pick_geom = sensor.getPickingGeometry();
        VRMLGeometryNodeType real_geom = findRealGeometry(pick_geom);
        int sort_type = sensor.getSortOrder();
        int pick_type = sensor.getIntersectionType();
        int pick_mask = sensor.getPickMask();
        PickableObject[] targets = sensor.getTargetObjects();
        calculatePickMatrix(sensor);

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        switch(sensor.getPickingType()) {
            case VRMLPickingSensorNodeType.POINT_PICK:
                processPointPicking(pick_type,
                                    sort_type,
                                    pick_mask,
                                    real_geom,
                                    targets,
                                    targetMap);
                break;

            case VRMLPickingSensorNodeType.LINE_PICK:
                processLinePicking(pick_type,
                                   sort_type,
                                   pick_mask,
                                   real_geom,
                                   targets,
                                   targetMap);
                break;

            case VRMLPickingSensorNodeType.SPHERE_PICK:
                processSpherePicking(pick_type,
                                     sort_type,
                                     pick_mask,
                                     real_geom,
                                     targets,
                                     targetMap);
                break;

            case VRMLPickingSensorNodeType.BOX_PICK:
                processBoxPicking(pick_type,
                                  sort_type,
                                  pick_mask,
                                  real_geom,
                                  targets,
                                  targetMap);
                break;

            case VRMLPickingSensorNodeType.CONE_PICK:
                processConePicking(pick_type,
                                   sort_type,
                                   pick_mask,
                                   real_geom,
                                   targets,
                                   targetMap);
                break;

            case VRMLPickingSensorNodeType.CYLINDER_PICK:
                processCylinderPicking(pick_type,
                                       sort_type,
                                       pick_mask,
                                       real_geom,
                                       targets,
                                       targetMap);
                break;

            case VRMLPickingSensorNodeType.VOLUME_PICK:
                processVolumePicking(pick_type,
                                     sort_type,
                                     pick_mask,
                                     real_geom,
                                     targets,
                                     targetMap);
                break;

            default:
                errorReporter.warningReport("Unknown picking type defined", null);
        }

        // Send the details off to the sensor node.
        try {
            if(lastNodeIndex != 0) {
                if(activeSensors.contains(sensor)) {
                    sensor.notifyPickChange(lastNodeIndex,
                                            nodeList,
                                            coordList,
                                            normalList,
                                            textureList);
                } else {
                    activeSensors.add(sensor);
                    sensor.notifyPickStart(lastNodeIndex,
                                           nodeList,
                                           coordList,
                                           normalList,
                                           textureList);
                }
            } else if(activeSensors.contains(sensor)) {
                // mark the sensor as no longer active.
                sensor.notifyPickEnd();
                activeSensors.remove(sensor);
                activePaths.remove(sensor);
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
    private void processPointPicking(int intersectType,
                                     int sortOrder,
                                     int pickMask,
                                     VRMLGeometryNodeType geom,
                                     PickableObject[] targets,
                                     Map targetMap) {

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

        float[] coords = coord.getPointRef();
        int num_points = coord.getNumPoints() / 3;

        // should we do single or batch picking here?
        if(num_points == 1) {
            setupPicker(singlePicker, intersectType, sortOrder);
            singlePicker.pickGeometryType = PickRequest.PICK_POINT;
            singlePicker.pickType = pickMask;
            singlePicker.origin[0] = coords[0];
            singlePicker.origin[1] = coords[1];
            singlePicker.origin[2] = coords[2];

            transformPosition(singlePicker.origin);

            boolean terminate = false;

            for(int i = 0; !terminate && (i < targets.length); i++) {
                targets[i].pickSingle(singlePicker);

                if(singlePicker.pickCount == 0)
                    continue;

                VRMLNodeType n = (VRMLNodeType)targetMap.get(targets[i]);

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
        } else {
            checkBatchPicker(num_points);

            for(int i = 0; i < num_points; i++) {
                setupPicker(batchPicker[i], intersectType, sortOrder);
                batchPicker[i].pickGeometryType = PickRequest.PICK_POINT;
                batchPicker[i].pickType = pickMask;
                batchPicker[i].origin[0] = coords[i * 3];
                batchPicker[i].origin[1] = coords[i * 3 + 1];
                batchPicker[i].origin[2] = coords[i * 3 + 2];

                transformPosition(batchPicker[i].origin);
            }

            boolean terminate = false;

            for(int i = 0; !terminate && (i < targets.length); i++) {
                targets[i].pickBatch(batchPicker, num_points);

                for(int j = 0; !terminate && j < num_points; j++) {
                    if(batchPicker[j].pickCount == 0)
                        continue;

                    VRMLNodeType n = (VRMLNodeType)targetMap.get(targets[i]);

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
    private void processLinePicking(int intersectType,
                                    int sortOrder,
                                    int pickMask,
                                    VRMLGeometryNodeType geom,
                                    PickableObject[] targets,
                                    Map targetMap) {

        if(geom.getVRMLNodeName().equals("LineSet"))
            processLineSetPicking(intersectType,
                                  sortOrder,
                                  pickMask,
                                  geom,
                                  targets,
                                  targetMap);
        else
            processIndexedLineSetPicking(intersectType,
                                         sortOrder,
                                         pickMask,
                                         geom,
                                         targets,
                                         targetMap);
    }

    /**
     * Process a IndexedLineSet picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processLineSetPicking(int intersectType,
                                       int sortOrder,
                                       int pickMask,
                                       VRMLGeometryNodeType geom,
                                       PickableObject[] targets,
                                       Map targetMap) {

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

        int field_idx = geom.getFieldIndex("vertexCount");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);
        int[] index_list = field_data.intArrayValue;
        int num_index = field_data.numElements;

        if(num_index < 1)
            return;

        float[] coords = coord_node.getPointRef();

        setupPicker(singlePicker, intersectType, sortOrder);
        singlePicker.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
        singlePicker.pickType = pickMask;

        boolean search_complete = false;
        double closest_point;
        int idx = 0;

        for(int i = 0; !search_complete && i < num_index; i++) {
            for(int j = 1; !search_complete && j < index_list[i]; j++) {
                singlePicker.origin[0] = coords[idx++];
                singlePicker.origin[1] = coords[idx++];
                singlePicker.origin[2] = coords[idx++];

                singlePicker.destination[0] = coords[idx++];
                singlePicker.destination[1] = coords[idx++];
                singlePicker.destination[2] = coords[idx++];

                transformPosition(singlePicker.origin);
                transformPosition(singlePicker.destination);

                for(int k = 0; !search_complete && k < targets.length; k++) {
                    targets[k].pickSingle(singlePicker);

                    if(singlePicker.pickCount == 0)
                        continue;

                    ArrayList potentials;

                    if(singlePicker.foundPaths instanceof ArrayList)
                        potentials = (ArrayList)singlePicker.foundPaths;
                    else {
                        potentials = selectionArray;
                        selectionArray.clear();
                        selectionArray.add(singlePicker.foundPaths);
                    }

                    int size = potentials.size();

                    // Geometry or bounds based intersection testing?
                    if(intersectType != VRMLPickingSensorNodeType.INTERSECT_BOUNDS) {
                        // Second set of case statemets to know whether to terminate
                        // completely, or start doing some sorting and classifying of the
                        // found results.
                        //
                        // For each potential pick, walk down the scene graph path and see
                        // if each item in the branch exists in the set of target nodes.
                        // When a match is found then do the sortOrder specific action.
                        boolean terminate = false;

                        for(int l = 0; l < singlePicker.pickCount; l++) {
                            SceneGraphPath path = (SceneGraphPath)potentials.get(l);
                            int len = path.getNodeCount();
                            terminate = false;

                            for(int m = 0; !terminate && m < len; m++) {
                                Node node = path.getNode(m);
                                VRMLNodeType n = (VRMLNodeType)targetMap.get(node);

                                if(n == null)
                                    continue;

                                // Set up the pick direction vector then look
                                // for more details.
                                wkDirection.x = singlePicker.destination[0] -
                                                singlePicker.origin[0];
                                wkDirection.y = singlePicker.destination[1] -
                                                singlePicker.origin[1];
                                wkDirection.z = singlePicker.destination[2] -
                                                singlePicker.origin[2];
                                tmpPoint1.x = singlePicker.origin[0];
                                tmpPoint1.y = singlePicker.origin[1];
                                tmpPoint1.z = singlePicker.origin[2];

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

                        // Exit out of the outer loop too.
                        if(terminate)
                            search_complete = true;
                    } else {
                        // bounds intersection testing.
                        boolean terminate = false;

                        switch(sortOrder) {
                            case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                            case VRMLPickingSensorNodeType.SORT_CLOSEST:
                                for(int l = 0; !terminate && l < size; l++) {
                                    SceneGraphPath path = (SceneGraphPath)potentials.get(l);
                                    int len = path.getNodeCount();
                                    terminate = false;

                                    for(int m = 0; !terminate && m < len; m++) {
                                        Node node = path.getNode(m);
                                        VRMLNodeType n = (VRMLNodeType)targetMap.get(node);

                                        if(n == null)
                                            continue;

                                        checkArraySize(1);
                                        nodeList[lastNodeIndex++] = n;

                                        // Exit out of the local loop, but
                                        // keep going on the potentials list.
                                        if(sortOrder == VRMLPickingSensorNodeType.SORT_CLOSEST)
                                            terminate = true;

                                        break;
                                    }
                                }
                                break;

                            case VRMLPickingSensorNodeType.SORT_ALL:
                            case VRMLPickingSensorNodeType.SORT_ANY:
                                for(int l = 0; !terminate && l < size; l++) {
                                    SceneGraphPath path = (SceneGraphPath)potentials.get(l);
                                    int len = path.getNodeCount();
                                    terminate = false;

                                    for(int m = 0; !terminate && m < len; m++) {
                                        Node node = path.getNode(m);
                                        VRMLNodeType n = (VRMLNodeType)targetMap.get(node);
                                        if(n == null)
                                            continue;

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

                                break;
                        }   // switch(sortOrder)
                    } // End of bounds-based intersection test block
                }
            }
        }
    }

    /**
     * Process a IndexedLineSet picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processIndexedLineSetPicking(int intersectType,
                                              int sortOrder,
                                              int pickMask,
                                              VRMLGeometryNodeType geom,
                                              PickableObject[] targets,
                                              Map targetMap) {

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

        int field_idx = geom.getFieldIndex("coordIndex");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);
        int[] index_list = field_data.intArrayValue;
        int num_index = field_data.numElements;

        if(num_index == 0)
            return;

        float[] coords = coord_node.getPointRef();

        setupPicker(singlePicker, intersectType, sortOrder);
        singlePicker.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
        singlePicker.pickType = pickMask;

        // This is for indexed lines
        for(int i = 0; i < num_index - 1; i++) {
            int x1 = index_list[i] * 3;
            int x2 = index_list[i + 1] * 3;

            // Abort if there's a -1 and jump 2 places ahead
            if(x2 < 0) {
                i++;
                continue;
            }

            singlePicker.origin[0] = coords[x1];
            singlePicker.origin[1] = coords[x1 + 1];
            singlePicker.origin[2] = coords[x1 + 2];

            singlePicker.destination[0] = coords[x2];
            singlePicker.destination[1] = coords[x2 + 1];
            singlePicker.destination[2] = coords[x2 + 2];

            transformPosition(singlePicker.origin);
            transformPosition(singlePicker.destination);

            for(int l = 0; l < targets.length; l++) {
                targets[l].pickSingle(singlePicker);

                if(singlePicker.pickCount == 0)
                    continue;

                ArrayList potentials;

                if(singlePicker.foundPaths instanceof ArrayList)
                    potentials = (ArrayList)singlePicker.foundPaths;
                else {
                    potentials = selectionArray;
                    selectionArray.clear();
                    selectionArray.add(singlePicker.foundPaths);
                }
                int size = potentials.size();

                // Geometry or bounds based intersection testing?
                if(intersectType != VRMLPickingSensorNodeType.INTERSECT_BOUNDS) {
                    boolean terminate = false;

                    for(int j = 0; j < size; j++) {
                        SceneGraphPath path = (SceneGraphPath)potentials.get(j);
                        int len = path.getNodeCount();
                        terminate = false;

                        for(int k = 0; !terminate && k < len; k++) {
                            Node node = path.getNode(k);
                            VRMLNodeType n = (VRMLNodeType)targetMap.get(node);

                            if(n == null)
                                continue;

                            // Set up the pick direction vector then look
                            // for more details.
                            wkDirection.x = singlePicker.destination[0] -
                                            singlePicker.origin[0];
                            wkDirection.y = singlePicker.destination[1] -
                                            singlePicker.origin[1];
                            wkDirection.z = singlePicker.destination[2] -
                                            singlePicker.origin[2];
                            tmpPoint1.x = singlePicker.origin[0];
                            tmpPoint1.y = singlePicker.origin[1];
                            tmpPoint1.z = singlePicker.origin[2];

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

                    // Exit out of the outer loop too.
                    if(terminate)
                        break;
                } else {
                    // bounds based interesection testing for indexed lines.
                    boolean terminate = false;

                    for(int j = 0; j < size; j++) {
                        SceneGraphPath path = (SceneGraphPath)potentials.get(j);
                        int len = path.getNodeCount();
                        terminate = false;

                        for(int k = 0; !terminate && k < len; k++) {
                            Node node = path.getNode(k);
                            VRMLNodeType n = (VRMLNodeType)targetMap.get(node);

                            if(n == null)
                                continue;

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

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processSpherePicking(int intersectType,
                                      int sortOrder,
                                      int pickMask,
                                      VRMLGeometryNodeType geom,
                                      PickableObject[] targets,
                                      Map targetMap) {

        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("radius");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        float scale = pickMatrix.getScale();

        setupPicker(singlePicker, intersectType, sortOrder);
        singlePicker.pickGeometryType = PickRequest.PICK_SPHERE;
        singlePicker.pickType = pickMask;
        singlePicker.additionalData = field_data.floatValue * scale;
        singlePicker.origin[0] = 0;
        singlePicker.origin[1] = 0;
        singlePicker.origin[2] = 0;

        transformPosition(singlePicker.origin);

        boolean terminate = false;

        for(int i = 0; !terminate && (i < targets.length); i++) {
            targets[i].pickSingle(singlePicker);

            if(singlePicker.pickCount == 0)
                continue;

            VRMLNodeType n = (VRMLNodeType)targetMap.get(targets[i]);

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


    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processBoxPicking(int intersectType,
                                   int sortOrder,
                                   int pickMask,
                                   VRMLGeometryNodeType geom,
                                   PickableObject[] targets,
                                   Map targetMap) {

        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("size");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);
        float x = field_data.floatArrayValue[0] * 0.5f;
        float y = field_data.floatArrayValue[1] * 0.5f;
        float z = field_data.floatArrayValue[2] * 0.5f;

        setupPicker(singlePicker, intersectType, sortOrder);
        singlePicker.pickGeometryType = PickRequest.PICK_BOX;
        singlePicker.pickType = pickMask;
        singlePicker.origin[0] = -x;
        singlePicker.origin[1] = -y;
        singlePicker.origin[2] = -z;
        singlePicker.destination[0] = x;
        singlePicker.destination[1] = y;
        singlePicker.destination[2] = z;

        transformPosition(singlePicker.origin);
        transformPosition(singlePicker.destination);

        boolean terminate = false;

        for(int i = 0; !terminate && (i < targets.length); i++) {
            targets[i].pickSingle(singlePicker);

            if(singlePicker.pickCount == 0)
                continue;

            VRMLNodeType n = (VRMLNodeType)targetMap.get(targets[i]);

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

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processConePicking(int intersectType,
                                    int sortOrder,
                                    int pickMask,
                                    VRMLGeometryNodeType geom,
                                    PickableObject[] targets,
                                    Map targetMap) {

        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("height");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);

        float height = field_data.floatValue;

        // Calculate the spread angle from the radius and height. This is
        // expensive because of the tan being used every frame. May want to
        // investigate a more efficient method

        field_idx = geom.getFieldIndex("bottomRadius");
        field_data = geom.getFieldValue(field_idx);
        double spread = Math.tan(field_data.floatValue / height);
        float scale = pickMatrix.getScale();

        setupPicker(singlePicker, intersectType, sortOrder);
        singlePicker.pickGeometryType = PickRequest.PICK_CONE_SEGMENT;
        singlePicker.pickType = pickMask;
        singlePicker.additionalData = (float)spread * scale;
        singlePicker.origin[0] = 0;
        singlePicker.origin[1] = height * 0.5f;
        singlePicker.origin[2] = 0;
        singlePicker.destination[0] = 0;
        singlePicker.destination[1] = -height * 0.5f;
        singlePicker.destination[2] = 0;

        transformPosition(singlePicker.origin);
        transformPosition(singlePicker.destination);

        lastNodeIndex = 0;
        lastCoordIndex = 0;

        boolean terminate = false;

        for(int i = 0; !terminate && (i < targets.length); i++) {
            targets[i].pickSingle(singlePicker);

            if(singlePicker.pickCount == 0)
                continue;

            VRMLNodeType n = (VRMLNodeType)targetMap.get(targets[i]);

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

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processCylinderPicking(int intersectType,
                                        int sortOrder,
                                        int pickMask,
                                        VRMLGeometryNodeType geom,
                                        PickableObject[] targets,
                                        Map targetMap) {

        // First go find the size of the box. Do that by reading the field
        // value from it. No shortcut for this, have to do the long way.
        int field_idx = geom.getFieldIndex("height");
        VRMLFieldData field_data = geom.getFieldValue(field_idx);

        float height = field_data.floatValue * 0.5f;

        field_idx = geom.getFieldIndex("radius");
        field_data = geom.getFieldValue(field_idx);

        float scale = pickMatrix.getScale();

        setupPicker(singlePicker, intersectType, sortOrder);
        singlePicker.pickGeometryType = PickRequest.PICK_CYLINDER_SEGMENT;
        singlePicker.pickType = pickMask;
        singlePicker.additionalData = field_data.floatValue * scale;
        singlePicker.origin[0] = 0;
        singlePicker.origin[1] = height;
        singlePicker.origin[2] = 0;
        singlePicker.destination[0] = 0;
        singlePicker.destination[1] = -height;
        singlePicker.destination[2] = 0;

        transformPosition(singlePicker.origin);
        transformPosition(singlePicker.destination);

        boolean terminate = false;

        for(int i = 0; !terminate && (i < targets.length); i++) {
            targets[i].pickSingle(singlePicker);

            if(singlePicker.pickCount == 0)
                continue;

            VRMLNodeType n = (VRMLNodeType)targetMap.get(targets[i]);

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

    /**
     * Process a point picking option.
     *
     * @param geom The node to do the picking with
     * @param targets The items to pick against
     */
    private void processVolumePicking(int intersectType,
                                      int sortOrder,
                                      int pickMask,
                                      VRMLGeometryNodeType geom,
                                      PickableObject[] targets,
                                      Map targetMap) {
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
     * Check the batch picker array is long enough to support the required
     * size. If not, resize as needed.
     *
     * @param size The minimum number of elements to have
     */
    private void checkBatchPicker(int size) {

        if(batchPicker.length >= size)
            return;

        PickRequest[] tmp = new PickRequest[size];

        System.arraycopy(batchPicker, 0, tmp, 0, batchPicker.length);
        for(int i = batchPicker.length; i < size; i++)
            tmp[i] = new PickRequest();

        batchPicker = tmp;
    }

    /**
     * Convenience method to take the basic picking setup from Xj3D and convert
     * the values through to Aviatrix3D equivalents.
     *
     * @param picker The pick request instance to update
     */
    private void setupPicker(PickRequest picker,
                             int intersectType,
                             int sortOrder) {

        switch(intersectType) {
            case VRMLPickingSensorNodeType.INTERSECT_BOUNDS:
                picker.useGeometry = false;
                break;

            case VRMLPickingSensorNodeType.INTERSECT_GEOMETRY:
                picker.useGeometry = true;
                break;
        }

        switch(sortOrder) {
            case VRMLPickingSensorNodeType.SORT_CLOSEST:
                picker.pickSortType = PickRequest.SORT_CLOSEST;
                break;

            case VRMLPickingSensorNodeType.SORT_ALL:
                picker.pickSortType = PickRequest.SORT_ALL;
                break;

            case VRMLPickingSensorNodeType.SORT_ANY:
                picker.pickSortType = PickRequest.SORT_ANY;
                break;

            case VRMLPickingSensorNodeType.SORT_ALL_SORTED:
                picker.pickSortType = PickRequest.SORT_ORDERED;
                break;
        }
    }

    /**
     * Walk from this node up to the root and calculate the transformation
     * matrix. The result is left in the global pickerMatrix variable.
     */
    private void calculatePickMatrix(OGLPickingSensorNodeType sensor) {
        Node parent = sensor.getParentGroup();

        pickMatrix.setIdentity();

        int last_matrix = 0;

        while(parent != null) {
            if(parent instanceof TransformGroup) {
                if(matrixStack.length == last_matrix) {
                    Matrix4f[] tmp = new Matrix4f[last_matrix + 16];
                    System.arraycopy(matrixStack, 0, tmp, 0, last_matrix);

                    for(int i = last_matrix; i < tmp.length; i++)
                        matrixStack[i] = new Matrix4f();
                    matrixStack = tmp;
                }

                ((TransformGroup)parent).getTransform(matrixStack[last_matrix]);
                last_matrix++;
            }

            parent = parent.getParent();
        }

        for(int i = last_matrix; --i >= 0; )
            pickMatrix.mul(matrixStack[i]);
    }

    /**
     * Convenience method to transform a position array by the pick
     * matrix.
     *
     * @param pos The position value to modify
     */
    private void transformPosition(float[] pos) {
        float x = pickMatrix.m00 * pos[0] + pickMatrix.m01 * pos[1] +
                  pickMatrix.m02 * pos[2] + pickMatrix.m03;
        float y = pickMatrix.m10 * pos[0] + pickMatrix.m11 * pos[1] +
                  pickMatrix.m12 * pos[2] + pickMatrix.m13;
        float z = pickMatrix.m20 * pos[0] + pickMatrix.m21 * pos[1] +
                  pickMatrix.m22 * pos[2] + pickMatrix.m23;

        pos[0] = x;
        pos[1] = y;
        pos[2] = z;
    }

    /**
     * Convenience method to transform a vector array by the pick
     * matrix.
     *
     * @param pos The vector value to modify
     */
    private void transformVector(float[] vec) {
        float x = pickMatrix.m00 * vec[0] + pickMatrix.m01 * vec[1] +
                  pickMatrix.m02 * vec[2];
        float y = pickMatrix.m10 * vec[0] + pickMatrix.m11 * vec[1] +
                  pickMatrix.m12 * vec[2];
        float z = pickMatrix.m20 * vec[0] + pickMatrix.m21 * vec[1] +
                  pickMatrix.m22 * vec[2];

        vec[0] = x;
        vec[1] = y;
        vec[2] = z;
    }

    /**
     * Process the line picking for a detailed answer about where the line
     * intersects the geometry. If there is no intersection, the distance will
     * be a negative number.
     */
    private double detailLinePick(SceneGraphPath path) {

        // Use exact geometry intersection to find the "one true"
        Node node = path.getTerminalNode();

        double shortest_length = Double.POSITIVE_INFINITY;
        boolean length_set = false;
        int shortest = -1;

        if(node instanceof Shape3D) {
            path.getTransform(tmpMatrix);
            Geometry g = ((Shape3D)node).getGeometry();

            if(g instanceof VertexGeometry)
            {
                VertexGeometry geom = (VertexGeometry)g;

                OGLUserData u_data = (OGLUserData)geom.getUserData();

                boolean found = false;
                if((u_data != null) && (u_data.geometryData != null) &&
                   (u_data.geometryData instanceof GeometryData))
                {
                    GeometryData gd = (GeometryData)u_data.geometryData;

                    found = iutils.rayUnknownGeometry(tmpPoint1,
                                                      wkDirection,
                                                      0,
                                                      gd,
                                                      tmpMatrix,
                                                      wkPoint,
                                                      true);
                } else {
                    found = iutils.rayUnknownGeometry(tmpPoint1,
                                                      wkDirection,
                                                      0,
                                                      geom,
                                                      tmpMatrix,
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
/*
            else
            {
Process Text and Volume geometry here
            }
*/
        }

        return length_set ? shortest_length : -1;
    }
}
