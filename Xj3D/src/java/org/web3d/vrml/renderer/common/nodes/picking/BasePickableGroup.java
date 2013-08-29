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
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLPickableNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common implementation of a PickGroup extension node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class BasePickableGroup extends BaseGroupingNode
    implements VRMLPickableNodeType {

    /** Secondary target node types */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.PickTargetNodeType
    };

    /** Index of the objectType field */
    protected static final int FIELD_PICKABLE = LAST_GROUP_INDEX + 1;

    /** Index of the objectType field */
    protected static final int FIELD_OBJECT_TYPE = LAST_GROUP_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_PICK_INDEX = FIELD_OBJECT_TYPE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_PICK_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** MFString objectType */
    protected String[] vfObjectType;

    /** SFBool pickable */
    protected boolean vfPickable;

    /**
     * Static constructor initialises all of the fields of the class
     */
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "children");
        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "addChildren");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "removeChildren");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_OBJECT_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "objectType");
        fieldDecl[FIELD_PICKABLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "pickable");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_ADDCHILDREN);
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = new Integer(FIELD_REMOVECHILDREN);
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        idx = new Integer(FIELD_OBJECT_TYPE);
        fieldMap.put("objectType", idx);
        fieldMap.put("set_objectType", idx);
        fieldMap.put("objectType_changed", idx);

        idx = new Integer(FIELD_PICKABLE);
        fieldMap.put("pickable", idx);
        fieldMap.put("set_pickable", idx);
        fieldMap.put("pickable_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BasePickableGroup() {
        super("PickableGroup");

        hasChanged = new boolean[LAST_PICK_INDEX + 1];

        vfObjectType = FieldConstants.EMPTY_MFSTRING;
        vfPickable = true;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BasePickableGroup(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("objectType");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfObjectType = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfObjectType,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("pickable");
            field = node.getFieldValue(index);
            vfPickable = field.booleanValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLPickableNodeType
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
     * Set the pickable state of this object. True to allow it and it's
     * children to participate in picking, false to remove it.
     *
     * @param state true to enable picking, false otherwise
     */
    public void setPickable(boolean state) {
        vfPickable = state;

        if(!inSetup) {
            hasChanged[FIELD_PICKABLE] = true;
            fireFieldChanged(FIELD_PICKABLE);
        }
    }

    /**
     * Get the current pickable state of the object.
     *
     * @return true if picking is allowed, false otherwise
     */
    public boolean getPickable() {
        return vfPickable;
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
        Integer index = (Integer) fieldMap.get(fieldName);

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
        if(index < 0  || index > LAST_PICK_INDEX)
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
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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
            case FIELD_OBJECT_TYPE:
                fieldData.clear();
                fieldData.numElements = vfObjectType.length;
                fieldData.stringArrayValue = vfObjectType;
                fieldData.dataType = fieldData.STRING_ARRAY_DATA;
                break;

            case FIELD_PICKABLE:
                fieldData.booleanValue = vfPickable;
                fieldData.dataType = fieldData.BOOLEAN_DATA;
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
                case FIELD_PICKABLE:
                    destNode.setValue(destIndex, vfPickable);
                    break;

                case FIELD_OBJECT_TYPE:
                    destNode.setValue(destIndex, vfObjectType, vfObjectType.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTransform.sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
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
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_PICKABLE:
                setPickable(value);
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
}
