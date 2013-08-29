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

/**
 * Implementation of the common PointPicker node for all renderers.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public abstract class BasePointPicker extends BasePickingNode {

    /** The field index for pickedPoint */
    protected static final int FIELD_PICKED_POINT = LAST_PICK_INDEX + 1;

    /** Last index used by this base node */
    protected static final int LAST_POINT_INDEX = FIELD_PICKED_POINT;

    /** The total number of fields in this node */
    protected static final int NUM_FIELDS = LAST_POINT_INDEX + 1;

    /** Message for when the proto is not a Geometry */
    protected static final String POINT_PROTO_MSG =
        "Proto does not describe a PointSet object";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String POINT_NODE_MSG =
        "Node does not describe a PointSet object";

    /** Error message for the geometry pick type */
    private static final String PICK_GEOM_MSG = "The pickGeometry type is " +
        "invalid for PointPicker. It must be a PointSet";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** Value for outputOnly field pickedPoint */
    private float[] vfPickedPoint;

    /** Number of points that are valid in the pickedPoint arrat */
    private int numPickedPoint;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] {
            FIELD_PICK_TARGET,
            FIELD_PICKING_GEOMETRY,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_PICKING_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "pickingGeometry");
        fieldDecl[FIELD_PICK_TARGET] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "pickTarget");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_PICKED_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "pickedGeometry");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_INTERSECTION_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "intersectionType");
        fieldDecl[FIELD_SORT_ORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "sortOrder");
        fieldDecl[FIELD_PICKED_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFVec3f",
                                     "pickedPoint");
        fieldDecl[FIELD_OBJECT_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "objectType");


        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_PICKING_GEOMETRY);
        fieldMap.put("pickingGeometry", idx);
        fieldMap.put("set_pickingGeometry", idx);
        fieldMap.put("pickingGeometry_changed", idx);

        idx = new Integer(FIELD_PICK_TARGET);
        fieldMap.put("pickTarget", idx);
        fieldMap.put("set_pickTarget", idx);
        fieldMap.put("pickTarget_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_OBJECT_TYPE);
        fieldMap.put("objectType", idx);
        fieldMap.put("set_objectType", idx);
        fieldMap.put("objectType_changed", idx);

        fieldMap.put("sortOrder",new Integer(FIELD_SORT_ORDER));
        fieldMap.put("intersectionType",new Integer(FIELD_INTERSECTION_TYPE));

        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("pickedGeometry",new Integer(FIELD_PICKED_GEOMETRY));
        fieldMap.put("pickedPoint",new Integer(FIELD_PICKED_POINT));
    }

    /**
     * Construct a new time sensor object
     */
    public BasePointPicker() {
        super("PointPicker", PICK_GEOM_MSG);

        hasChanged = new boolean[NUM_FIELDS];

        validGeometryNodeNames.add("PointSet");
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BasePointPicker(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLPickingSensorNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLPickingSensorNodeType
    //----------------------------------------------------------

    /**
     * Get the picking type that this class represents. A shortcut way of
     * quickly determining the picking strategy to be used by the internal
     * implementation to avoid unnessary calculations.
     *
     * @return One of the *_PICK constants
     */
    public int getPickingType() {
        return POINT_PICK;
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

        String name = child.getVRMLNodeName();

        // Test based on the node name. Can be dodgy if someone decides to use
        // a proto that redefines a built in node type.
        if(!name.equals("PointSet")) {
            if(!(child instanceof VRMLProtoInstance))
                throw new InvalidFieldValueException(POINT_NODE_MSG);

            VRMLNodeType impl = ((VRMLProtoInstance)child).getImplementationNode();
            name = impl.getVRMLNodeName();

            // Not really good enough, what if the first node is also a proto?
            // Will need to walk down the tree until it is not a proto.
            if(!name.equals("PointSet")) {
                if(!(child instanceof VRMLProtoInstance))
                    throw new InvalidFieldValueException(POINT_NODE_MSG);
            }
        }

        super.setPickingGeometry(child);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_POINT_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
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
            case FIELD_PICKED_POINT:
                fieldData.clear();
                fieldData.floatArrayValue = vfPickedPoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numPickedPoint / 3;
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
                case FIELD_PICKED_POINT:
                    destNode.setValue(destIndex, vfPickedPoint, numPickedPoint);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTouchSensor.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseTouchSensor.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }


    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------
}
