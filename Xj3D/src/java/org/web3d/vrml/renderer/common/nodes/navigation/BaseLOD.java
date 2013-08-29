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

package org.web3d.vrml.renderer.common.nodes.navigation;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLViewDependentNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common base implementation of a LOD node.
 * <p>
 *
 * Internally the LOD keeps both the basic range and the values squared. This
 * makes computation much faster, eliminating the need to take expensive
 * square-roots each frame.
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public abstract class BaseLOD extends BaseGroupingNode
    implements VRMLViewDependentNodeType {

    protected static final int[] SECONDARY_TYPE = {
        TypeConstants.ViewDependentNodeType
    };

    /** Index of the center field */
    protected static final int FIELD_CENTER = LAST_GROUP_INDEX + 1;

    /** The index of the range field */
    protected static final int FIELD_RANGE = LAST_GROUP_INDEX + 2;

    /** The index of the level_changed field */
    protected static final int FIELD_LEVEL_CHANGED = LAST_GROUP_INDEX + 3;

    /** The index of the forceTransitions field */
    protected static final int FIELD_FORCE_TRANSITIONS = LAST_GROUP_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_LOD_INDEX = FIELD_FORCE_TRANSITIONS;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_LOD_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Indices of the fields that are MFNode or SFnode */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFVec3f center */
    protected float[] vfCenter;

    /** MFFloat range */
    protected float[] vfRange;

    /** The number of range items in use in the array */
    protected int rangeLen;

    /** The values of vfRange squared */
    protected float[] rangeSquared;

    /** The value of the forceTransitions field */
    protected boolean vfForceTransitions;

    /** The value of the outputOnly field level_changed */
    protected int vfLevelChanged;

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
        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "center");
        fieldDecl[FIELD_RANGE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFFloat",
                                     "range");

        fieldDecl[FIELD_FORCE_TRANSITIONS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "forceTransitions");

        fieldDecl[FIELD_LEVEL_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "level_changed");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        fieldMap.put("level", idx);
        fieldMap.put("set_level", idx);
        fieldMap.put("level_changed", idx);

        idx = new Integer(FIELD_ADDCHILDREN);
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = new Integer(FIELD_REMOVECHILDREN);
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        fieldMap.put("center",new Integer(FIELD_CENTER));
        fieldMap.put("range",new Integer(FIELD_RANGE));

        fieldMap.put("level_changed",new Integer(FIELD_LEVEL_CHANGED));
        fieldMap.put("forceTransitions",new Integer(FIELD_FORCE_TRANSITIONS));
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    protected BaseLOD() {
        super("LOD");

        hasChanged = new boolean[NUM_FIELDS];

        vfCenter = new float[] { 0, 0, 0 };
        vfRange = new float[0];
        rangeSquared = new float[0];
        rangeLen = 0;
        vfForceTransitions = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseLOD(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);

            vfCenter[0] = field.floatArrayValue[0];
            vfCenter[1] = field.floatArrayValue[1];
            vfCenter[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("range");
            field = node.getFieldValue(index);

            if(field.floatArrayValue.length > rangeLen) {
                vfRange = new float[field.floatArrayValue.length];
                rangeSquared = new float[field.floatArrayValue.length];
            }

            rangeLen = field.numElements;

            System.arraycopy(field.floatArrayValue, 0, vfRange, 0, rangeLen);

            for(int i = 0; i < rangeLen; i++)
                rangeSquared[i] = vfRange[i] * vfRange[i];

            index = node.getFieldIndex("forceTransitions");

            if (index != -1) {
                field = node.getFieldValue(index);

                vfForceTransitions = field.booleanValue;
            }

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

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
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldMap.get(fieldName);

        int ret_val = (index == null) ? -1 : index.intValue();

        // forceTransitions and level_changed are added in 3.1. Change
        // the field index to say that they don't exist for VRML or
        // X3D 3.0.
        if((ret_val == FIELD_FORCE_TRANSITIONS ||
            ret_val == FIELD_LEVEL_CHANGED) &&
           ((vrmlMajorVersion == 2) ||
            ((vrmlMajorVersion == 3) && (vrmlMinorVersion == 0))))
            ret_val = -1;

        return ret_val;
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
        if(index < 0  || index > LAST_LOD_INDEX)
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
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_CENTER:
                fieldData.clear();
                fieldData.floatArrayValue = vfCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_RANGE:
                fieldData.clear();
                fieldData.floatArrayValue = vfRange;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = rangeLen;
                break;

            case FIELD_LEVEL_CHANGED:
                fieldData.clear();
                fieldData.intValue = vfLevelChanged;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_FORCE_TRANSITIONS:
                fieldData.clear();
                fieldData.booleanValue = vfForceTransitions;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.numElements = 1;
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
                case FIELD_CENTER:
                    destNode.setValue(destIndex, vfCenter, 3);
                    break;

                case FIELD_RANGE:
                    destNode.setValue(destIndex, vfRange, rangeLen);
                    break;

                case FIELD_LEVEL_CHANGED:
                    destNode.setValue(destIndex, vfLevelChanged);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTER:
                setCenter(value);
                break;

            case FIELD_RANGE:
                setRange(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a boolean.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FORCE_TRANSITIONS:
                if(!inSetup)
                    throw new InvalidFieldValueException(
                        "forceTransitions is an initialize-only field");

                vfForceTransitions = value;
                break;
            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    protected void setCenter(float[] center)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                "Center is an initialize-only field");

        if(center == null)
            throw new InvalidFieldValueException("Center value null");

        vfCenter[0] = center[0];
        vfCenter[1] = center[1];
        vfCenter[2] = center[2];
    }

    /**
     * Set the range to the new series of values. A null value is the same as
     * removing the range altogether.
     *
     * @param range the new range values to use
     * @param numValid The number of valid values to copy from the array
     */
    protected void setRange(float[] range, int numValid)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(
                "Range is an initialize-only field");

        if(numValid == 0) {
            rangeLen = 0;
        } else {
            int i;

            // check for all the range values being >= 0
            for(i = numValid; --i >= 0; ) {
                if(range[i] < 0) {
                    throw new InvalidFieldValueException(
                        "Negative range value " +  range[i]);
                }
            }

            // reallocate if the range needed is greater than we have
            if(numValid > vfRange.length) {
                vfRange = new float[numValid];
                rangeSquared = new float[numValid];
            }

            rangeLen = numValid;

            System.arraycopy(range, 0, vfRange, 0, rangeLen);

            for(i = 0; i < rangeLen; i++)
                rangeSquared[i] = vfRange[i] * vfRange[i];
        }
    }

    /**
     * Convenience method to call when the implementation has detected that
     * the currently active level has changed.
     *
     * @param level The new level to use
     */
    protected void setLevelChanged(int level) {
        vfLevelChanged = level;

        if(!inSetup) {
            hasChanged[FIELD_LEVEL_CHANGED] = true;
            fireFieldChanged(FIELD_LEVEL_CHANGED);
        }
    }
}
