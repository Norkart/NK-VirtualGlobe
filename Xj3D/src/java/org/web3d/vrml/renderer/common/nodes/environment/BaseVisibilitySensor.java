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

package org.web3d.vrml.renderer.common.nodes.environment;

// Standard imports
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;

/**
 * Base implementation of a VisibilitySensor node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public abstract class BaseVisibilitySensor extends BaseSensorNode
    implements VRMLEnvironmentalSensorNodeType,
               VRMLTimeDependentNodeType {

    /** Secondary types of this node */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.TimeDependentNodeType };

    // Field index constants

    /** The field index for center */
    protected static final int FIELD_CENTER = LAST_SENSOR_INDEX + 1;

    /** The field index for size */
    protected static final int FIELD_SIZE = LAST_SENSOR_INDEX + 2;

    /** The field index for enterTime */
    protected static final int FIELD_ENTER_TIME = LAST_SENSOR_INDEX + 5;

    /** The field index for exitTime */
    protected static final int FIELD_EXIT_TIME = LAST_SENSOR_INDEX + 6;

    /** The last field index used by this class */
    protected static final int LAST_VISIBILITYSENSOR_INDEX = FIELD_EXIT_TIME;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_VISIBILITYSENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the center field */
    protected float[] vfCenter;

    /** The value of the size field */
    protected float[] vfSize;

    /** The value of the enterTime field */
    protected double vfEnterTime;

    /** The value of the exitTime field */
    protected double vfExitTime;

    /** The sim clock this node uses */
    protected VRMLClock vrmlClock;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "center");

        fieldDecl[FIELD_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "size");

        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");

        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");

        fieldDecl[FIELD_ENTER_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "enterTime");

        fieldDecl[FIELD_EXIT_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "exitTime");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CENTER);
        fieldMap.put("center", idx);
        fieldMap.put("set_center", idx);
        fieldMap.put("center_changed", idx);

        idx = new Integer(FIELD_SIZE);
        fieldMap.put("size", idx);
        fieldMap.put("set_size", idx);
        fieldMap.put("size_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        fieldMap.put("isActive", new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("enterTime", new Integer(FIELD_ENTER_TIME));
        fieldMap.put("exitTime", new Integer(FIELD_EXIT_TIME));
    }

    /**
     * Construct a new visibility sensor object
     */
    protected BaseVisibilitySensor() {
        super("VisibilitySensor");
        hasChanged = new boolean[NUM_FIELDS];

        vfCenter = new float[3];
        vfSize = new float[3];
        vfEnabled = true;
        vfIsActive = false;
        vfEnterTime = -1;
        vfExitTime = -1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type
     */
    protected BaseVisibilitySensor(VRMLNodeType node) {
        this();

        checkNodeType(node);
        copy((VRMLSensorNodeType)node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);
            vfCenter[0] = field.floatArrayValue[0];
            vfCenter[1] = field.floatArrayValue[1];
            vfCenter[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("size");
            field = node.getFieldValue(index);
            vfSize[0] = field.floatArrayValue[0];
            vfSize[1] = field.floatArrayValue[1];
            vfSize[2] = field.floatArrayValue[2];

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------------
    // Methods required by the VRMLTimeDependentNodeType interface.
    //-------------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        vrmlClock = clk;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        if(index < 0  || index > LAST_VISIBILITYSENSOR_INDEX)
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.EnvironmentalSensorNodeType;
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
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        if(index <= LAST_SENSOR_INDEX)
            return super.getFieldValue(index);

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_CENTER:
                fieldData.clear();
                fieldData.floatArrayValue = vfCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ENTER_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfEnterTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_EXIT_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfExitTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
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

                case FIELD_SIZE:
                    destNode.setValue(destIndex, vfSize, 3);
                    break;

                case FIELD_ENTER_TIME:
                    destNode.setValue(destIndex, vfEnterTime);
                    break;

                case FIELD_EXIT_TIME:
                    destNode.setValue(destIndex, vfExitTime);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
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
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SIZE:
                setSize(value);
                break;

            case FIELD_CENTER:
                setCenter(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------

    /**
     * Update the size of the sensor. May be overridden by derived classes
     * but should make sure to call this as well
     *
     * @param val The new size to use
     */
    protected void setSize(float[] val) {
        vfSize[0] = val[0];
        vfSize[1] = val[1];
        vfSize[2] = val[2];

        if(!inSetup) {
            hasChanged[FIELD_SIZE] = true;
            fireFieldChanged(FIELD_SIZE);
        }
    }

    /**
     * Update the center of the sensor. May be overridden by derived classes
     * but should make sure to call this as well
     *
     * @param val The new center to use
     */
    protected void setCenter(float[] val) {
        vfCenter[0] = val[0];
        vfCenter[1] = val[1];
        vfCenter[2] = val[2];

        if(!inSetup) {
            hasChanged[FIELD_CENTER] = true;
            fireFieldChanged(FIELD_CENTER);
        }
    }
}
