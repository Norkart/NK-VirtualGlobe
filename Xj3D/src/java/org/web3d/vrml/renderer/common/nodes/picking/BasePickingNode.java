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

package org.web3d.vrml.renderer.common.nodes.picking;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;

/**
 * Implementation of the abstract X3DPickingNode type.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public abstract class BasePickingNode extends BaseSensorNode
    implements VRMLPickingSensorNodeType {

    // Field index constants

    /** The field index for pickingGeometry */
    protected static final int FIELD_PICKING_GEOMETRY = LAST_SENSOR_INDEX + 1;

    /** The field index for hitPoint_Changed */
    protected static final int FIELD_PICK_TARGET = LAST_SENSOR_INDEX + 2;

    /** The field index for hitTexCoord_Changed */
    protected static final int FIELD_INTERSECTION_TYPE = LAST_SENSOR_INDEX + 3;

    /** The field index for pickedGeometry */
    protected static final int FIELD_PICKED_GEOMETRY = LAST_SENSOR_INDEX + 4;

    /** The field index for isOver  */
    protected static final int FIELD_SORT_ORDER = LAST_SENSOR_INDEX + 5;

    /** Index of the objectType field */
    protected static final int FIELD_OBJECT_TYPE = LAST_SENSOR_INDEX + 6;

    /** Last index used by this base node */
    protected static final int LAST_PICK_INDEX = FIELD_OBJECT_TYPE;

    /** Message for when the proto is not a Geometry */
    protected static final String GEOMETRY_PROTO_MSG =
        "Proto does not describe a Geometry object";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String GEOMETRY_NODE_MSG =
        "Node does not describe a Geometry object";

    /** Standard string for the default "ALL" object type */
    private static final String ALL_OBJECT_TYPE = "ALL";

    /** Map containing the string to constant mapping for sorting */
    protected static HashMap sortTypeMap;

    /** Map containing the string to constant mapping for intersection type */
    protected static HashMap intersectTypeMap;

    /** Error message for when an invalid geometry type is recieved */
    private final String PICK_GEOM_TYPE_MSG;

    // The VRML field values

    /** MFString objectType */
    protected String[] vfObjectType;

    /** The value of the pickingGeometry field */
    protected VRMLGeometryNodeType vfPickingGeometry;

    /** The proto version of the picking geometry */
    protected VRMLProtoInstance pPickingGeometry;

    /** The value of the pickTarget field */
    protected VRMLNodeType[] vfPickTarget;

    /** The value of the pickTarget field */
    protected VRMLNodeType[] vfPickedGeometry;

    /** The value of the intersectionType field */
    protected String vfIntersectionType;

    /** The value of the sortOrder field */
    protected String vfSortOrder;

    /** Constant representing the intersection type */
    protected int intersectionType;

    /** Constant representing the sort type */
    protected int sortType;

    /** Temporary array for holding targets during initial node build */
    private ArrayList targetList;

    /** Number of valid items in the vfPickTarget array */
    protected int numPickTarget;

    /** Number of valid items in the pickedGeometry array */
    protected int numPickedGeometry;

    /**
     * The set of valid geometry node name types that the picking instance is
     * allowed to use for the pick geometry. THis is sort of a bit of a kludge
     * to deal with the multiple renderer thing. However, when we do get a proto
     * instance, we first walk down the stack looking for the implementation node
     * to make sure it satisfies one of these names too.
     */
    protected HashSet validGeometryNodeNames;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        sortTypeMap = new HashMap();
        sortTypeMap.put("CLOSEST", new Integer(SORT_CLOSEST));
        sortTypeMap.put("ALL", new Integer(SORT_ALL));
        sortTypeMap.put("ALL_SORTED", new Integer(SORT_ALL_SORTED));
        sortTypeMap.put("ANY", new Integer(SORT_ANY));

        intersectTypeMap = new HashMap();
        intersectTypeMap.put("BOUNDS", new Integer(INTERSECT_BOUNDS));
        intersectTypeMap.put("GEOMETRY", new Integer(INTERSECT_GEOMETRY));
    }

    /**
     * Construct a new generalised picking node object.
     *
     * @param name The VRML name of this node
     * @param errorMsg Error message to use for the wrong geometry pick type
     */
    public BasePickingNode(String name, String errorMsg) {
        super(name);

        PICK_GEOM_TYPE_MSG = errorMsg;

        init();
    }

    /**
     * Set the fields of the sensor node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The sensor node to copy info from
     */
    protected void copy(VRMLPickingSensorNodeType node) {

        super.copy(node);

        try {
            int index = node.getFieldIndex("sortOrder");
            VRMLFieldData field = node.getFieldValue(index);
            vfSortOrder = field.stringValue;

            Integer i = (Integer)sortTypeMap.get(vfSortOrder);
            sortType = i.intValue();

            index = node.getFieldIndex("sortOrder");
            field = node.getFieldValue(index);
            vfIntersectionType = field.stringValue;

            i = (Integer)sortTypeMap.get(vfIntersectionType);
            intersectionType = i.intValue();

            index = node.getFieldIndex("objectType");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfObjectType = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfObjectType,
                                 0,
                                 field.numElements);
            }

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLPickingSensorNodeType
    //----------------------------------------------------------

    /**
     * Set the list of picking targets that this object corresponds to.
     * These can be an array of strings.
     *
     * @param types The list of object type strings to use
     * @param numValid The number of valid values to read from the array
     */
    public void setObjectType(String[] types, int numValid) {
        if(vfObjectType.length != numValid)
            vfObjectType = new String[numValid];

        System.arraycopy(types, 0, vfObjectType, 0, numValid);

        if(!inSetup) {
            hasChanged[FIELD_OBJECT_TYPE] = true;
            fireFieldChanged(FIELD_OBJECT_TYPE);
        }
    }

    /**
     * Get the current number of valid object type strings.
     *
     * @param a number >= 0
     */
    public int numObjectType() {
        return vfObjectType.length;
    }

    /**
     * Fetch the number of object type values in use currently.
     *
     * @param val An array to copy the values to
     */
    public void getObjectType(String[] val) {
        System.arraycopy(vfObjectType, 0, val, 0, vfObjectType.length);
    }

    /**
     * Get the intersection type requested for this node
     *
     * @return one of the SORT_* constants
     */
    public int getSortOrder() {
        return sortType;
    }

    /**
     * Get the intersection type requested for this node
     *
     * @return one of the INTERSECT_* constants
     */
    public int getIntersectionType() {
        return intersectionType;
    }

    /**
     * Set node content as replacement for the pickingGeometry field. This
     * checks only for basic geometry handling. If a concrete node needs a
     * specific set of nodes, it should override this method to check.
     *
     * @param app The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setPickingGeometry(VRMLNodeType child)
        throws InvalidFieldValueException {

        VRMLGeometryNodeType node;
        VRMLNodeType old_node;

        if(pPickingGeometry != null)
            old_node = pPickingGeometry;
        else
            old_node = vfPickingGeometry;

        if(child instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)child).getImplementationNode();

            pPickingGeometry = (VRMLProtoInstance)child;

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLGeometryNodeType))
                throw new InvalidFieldValueException(GEOMETRY_PROTO_MSG);

            String name = impl.getVRMLNodeName();
            if(!validGeometryNodeNames.contains(name))
                throw new InvalidFieldValueException(PICK_GEOM_TYPE_MSG);

            node = (VRMLGeometryNodeType)impl;
        } else if(child instanceof VRMLGeometryNodeType) {
            String name = child.getVRMLNodeName();
            if(!validGeometryNodeNames.contains(name))
                throw new InvalidFieldValueException(PICK_GEOM_TYPE_MSG);

            pPickingGeometry = null;
            node = (VRMLGeometryNodeType)child;
        } else {
            throw new InvalidFieldValueException(GEOMETRY_NODE_MSG);
        }

        vfPickingGeometry = (VRMLGeometryNodeType)node;

        if(child != null)
            updateRefs(child, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(child != null)
                stateManager.registerAddedNode(child);

            hasChanged[FIELD_PICKING_GEOMETRY] = true;
            fireFieldChanged(FIELD_PICKING_GEOMETRY);
        }
    }

    /**
     * Fetch the node that is being used to pick the geometry
     *
     * @return The valid geometry node or null if not set
     */
    public VRMLNodeType getPickingGeometry() {
        return vfPickingGeometry;
    }

    /**
     * Get the list of nodes that are used for the target geometry. This can
     * be a internal listing of children. Any node valid entries in the can be
     * set to null.
     */
    public VRMLNodeType[] getPickingTargets() {
        return vfPickTarget;
    }

    /**
     * Notification that this sensor has just been clicked on to start the
     * pick action. Derived classes should call this one to handle the basic
     * node output and state. Points, normals, texCoords not handled.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    public void notifyPickStart(int numPicks,
                                VRMLNodeType[] nodes,
                                float[] points,
                                float[] normals,
                                float[] texCoords) {

        processPickedGeometry(numPicks, nodes);

        vfIsActive = true;

        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
    }

    /**
     * Notify the drag sensor that a sensor is currently dragging this device
     * and that it's position and orientation are as given.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    public void notifyPickChange(int numPicks,
                                 VRMLNodeType[] nodes,
                                 float[] points,
                                 float[] normals,
                                 float[] texCoords) {
        processPickedGeometry(numPicks, nodes);
    }

    /**
     * Notification that this sensor has finished a picking action.
     */
    public void notifyPickEnd() {
        vfIsActive = false;
        numPickedGeometry = 0;

        hasChanged[FIELD_PICKED_GEOMETRY] = true;
        fireFieldChanged(FIELD_PICKED_GEOMETRY);

        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.PickingSensorNodeType;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pPickingGeometry != null)
            pPickingGeometry.setupFinished();
        if(vfPickingGeometry != null)
            vfPickingGeometry.setupFinished();

        numPickTarget = targetList.size();
        vfPickTarget = new VRMLNodeType[numPickTarget];
        for(int i = 0; i < numPickTarget; i++) {
            VRMLNodeType p = (VRMLNodeType)targetList.get(i);
            p.setupFinished();
            vfPickTarget[i] = p;
        }

        targetList = null;

        updateChildren(vfPickTarget, numPickTarget);
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_PICKING_GEOMETRY:
                fieldData.clear();

                if(pPickingGeometry != null)
                    fieldData.nodeValue = pPickingGeometry;
                else
                    fieldData.nodeValue = vfPickingGeometry;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_PICK_TARGET:
                fieldData.clear();
                fieldData.nodeArrayValue = vfPickTarget;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = numPickTarget;
                break;

            case FIELD_PICKED_GEOMETRY:
                fieldData.clear();
                fieldData.nodeArrayValue = vfPickedGeometry;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = numPickedGeometry;
                break;

            case FIELD_INTERSECTION_TYPE:
                fieldData.clear();
                fieldData.stringValue = vfIntersectionType;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_SORT_ORDER:
                fieldData.clear();
                fieldData.stringValue = vfSortOrder;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_OBJECT_TYPE:
                fieldData.clear();
                fieldData.numElements = vfObjectType.length;
                fieldData.stringArrayValue = vfObjectType;
                fieldData.dataType = fieldData.STRING_ARRAY_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_PICKING_GEOMETRY:
                    destNode.setValue(destIndex, vfPickingGeometry);
                    break;

                case FIELD_PICK_TARGET:
                    destNode.setValue(destIndex,
                                      vfPickTarget,
                                      vfPickTarget.length);
                    break;

                case FIELD_PICKED_GEOMETRY:
                    destNode.setValue(destIndex,
                                      vfPickedGeometry,
                                      numPickedGeometry);
                    break;

                case FIELD_OBJECT_TYPE:
                    destNode.setValue(destIndex, vfObjectType, vfObjectType.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BasePickingNode.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BasePickingNode.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }


    /**
     * Set the value of the field at the given index as a single string.
     * This would be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_INTERSECTION_TYPE:
                if(!inSetup)
                    throw new InvalidFieldAccessException("intersectionType can only " +
                                                          "be set at runtime");
                Integer i = (Integer)intersectTypeMap.get(value);

                if(i == null)
                    throw new InvalidFieldValueException("Unknown intersection type " + value);

                vfIntersectionType = value;
                intersectionType = i.intValue();
                break;

            case FIELD_SORT_ORDER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("sortOrder can only " +
                                                          "be set at runtime");

                i = (Integer)sortTypeMap.get(value);

                if(i == null)
                    throw new InvalidFieldValueException("Unknown sort type " + value);

                vfSortOrder = value;
                sortType = i.intValue();
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFString field type.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_OBJECT_TYPE:
                setObjectType(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_PICKING_GEOMETRY:
                setPickingGeometry(child);
                break;

            case FIELD_PICK_TARGET:
                checkPickTargetType(child);
                if(inSetup)
                    targetList.add(child);
                else {
                    // clear out the rest.
                    for(int i = 0; i < numPickTarget; i++) {
                        stateManager.registerRemovedNode(vfPickTarget[i]);
                        vfPickTarget[i] = null;
                    }

                    vfPickTarget[0] = child;
                    numPickTarget = 1;

                    hasChanged[FIELD_PICK_TARGET] = true;
                    fireFieldChanged(FIELD_PICK_TARGET);
                }
                break;

            case FIELD_PICKED_GEOMETRY:
                throw new InvalidFieldAccessException("pickedGeometry is outputOnly");

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_PICK_TARGET:

                for(int i = 0; i < numPickTarget; i++) {
                    stateManager.registerRemovedNode(vfPickTarget[i]);
                    vfPickTarget[i] = null;
                }

                if(children == null) {
                    numPickTarget = 0;
                } else {
                    numPickTarget = children.length;

                    if (inSetup) {
                        for(int i = 0; i < numPickTarget; i++) {
                            checkPickTargetType(children[i]);
                            targetList.add(children[i]);
                        }
                    } else {
                        if (vfPickTarget.length < numPickTarget)
                            vfPickTarget = new VRMLNodeType[numPickTarget];

                        for(int i = 0; i < numPickTarget; i++) {
                            checkPickTargetType(children[i]);

                            vfPickTarget[i] = children[i];
                            stateManager.registerAddedNode(vfPickTarget[i]);
                        }
                    }
                }

                updateChildren(children, numValid);

                hasChanged[FIELD_PICK_TARGET] = true;
                fireFieldChanged(FIELD_PICK_TARGET);
                break;

            case FIELD_PICKED_GEOMETRY:
                throw new InvalidFieldAccessException("pickedGeometry is outputOnly");

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseSensorNode
    //----------------------------------------------------------

    /**
     * Accessor method to set a new value for the enabled field.
     *
     * @param state Whether this sensor is enabled
     */
    public void setEnabled(boolean state) {
        super.setEnabled(state);

        // if we're disabling the sensor and it is currently active, shut
        // it down.
        if(vfIsActive && !state) {
            vfIsActive = false;

            hasChanged[FIELD_IS_ACTIVE] = true;
            fireFieldChanged(FIELD_IS_ACTIVE);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Common initialisation routine for startup.
     */
    private void init() {
        targetList = new ArrayList();

        intersectionType = INTERSECT_BOUNDS;
        vfIntersectionType = "BOUNDS";

        sortType = SORT_CLOSEST;
        vfSortOrder = "CLOSEST";

        numPickedGeometry = 0;
        numPickTarget = 0;

        vfObjectType = new String[] { ALL_OBJECT_TYPE };

        validGeometryNodeNames = new HashSet();
    }

    /**
     * Check to make sure the picking target node being added is of the correct
     * type. If not, issue an error.
     *
     * @param target The node to check that it follows the requirements
     * @throws InvalidFieldValueException The node is not a grouping or shape node
     */
    protected void checkPickTargetType(VRMLNodeType target)
        throws InvalidFieldValueException {


        if((target instanceof VRMLGroupingNodeType) ||
           (target instanceof VRMLShapeNodeType))
            return;

        if(!(target instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(
                "pickTarget node not a group or shape node type: " +
                 target.getVRMLNodeName());

        VRMLProtoInstance proto = (VRMLProtoInstance)target;
        VRMLNodeType node = proto.getImplementationNode();

        while((node != null) && (node instanceof VRMLProtoInstance))
            node = ((VRMLProtoInstance)node).getImplementationNode();

        if((node != null) &&
           (!(node instanceof VRMLGroupingNodeType) &&
            !(node instanceof VRMLShapeNodeType)))
            throw new InvalidFieldValueException(
                "pickTarget proto instance not a group or shape node type: " +
                 target.getVRMLNodeName());
    }

    /**
     * Update the child list with the new nodes. This is called after all the
     * basic filtering has been complete and may be overridden by derived
     * classes if needed. The default implementation is empty.
     *
     * @param targets The list of current children
     * @param numValid The number of valid children to check
     */
    protected void updateChildren(VRMLNodeType[] targets, int numValid) {
    }

    /**
     * Process the picked geometry output.
     *
     * @param numPicks The number of items picked in the array
     * @param geom The geometry that was picked
     */
    private void processPickedGeometry(int numPicks, VRMLNodeType[] geom) {

        if((vfPickedGeometry == null) || (vfPickedGeometry.length < numPicks))
            vfPickedGeometry = new VRMLNodeType[numPicks];

        System.arraycopy(geom, 0, vfPickedGeometry, 0, numPicks);
        numPickedGeometry = numPicks;

        hasChanged[FIELD_PICKED_GEOMETRY] = true;
        fireFieldChanged(FIELD_PICKED_GEOMETRY);
    }
}
