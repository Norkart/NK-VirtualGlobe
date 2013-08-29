/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.extensions;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.nodes.VRMLDeviceSensorNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a GamepadSensor
 * <p>
 *
 * This node is an custom extension to Xj3D node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public abstract class BaseHIDSensor extends AbstractNode
    implements VRMLDeviceSensorNodeType {

    /** Index of the name field */
    protected static final int FIELD_NAME = LAST_NODE_INDEX + 1;

    /** Index of the outputValue field */
    protected static final int FIELD_OUTPUT_VALUE = LAST_NODE_INDEX + 2;

    /** Index of the isActive field */
    protected static final int FIELD_IS_ACTIVE = LAST_NODE_INDEX + 3;

    /** Index of the axisValue field */
    protected static final int FIELD_AXIS_VALUE = LAST_NODE_INDEX + 4;

    /** Index of the axisMinValue field */
    protected static final int FIELD_AXIS_MIN_VALUE = LAST_NODE_INDEX + 5;

    /** Index of the axisMaxValue field */
    protected static final int FIELD_AXIS_MAX_VALUE = LAST_NODE_INDEX + 6;

    /** Index of the axisName field */
    protected static final int FIELD_AXIS_NAME = LAST_NODE_INDEX + 7;

    /** Index of the axisResolution field */
    protected static final int FIELD_AXIS_RESOLUTION = LAST_NODE_INDEX + 8;

    /** Index of the axisWrap field */
    protected static final int FIELD_AXIS_WRAP = LAST_NODE_INDEX + 9;

    /** Index of the manufacturerName field */
    protected static final int FIELD_MANUFACTURER_NAME = LAST_NODE_INDEX + 10;

    /** Index of the numAxes field */
    protected static final int FIELD_NUM_AXES = LAST_NODE_INDEX + 11;

    /** Index of the numOutputs field */
    protected static final int FIELD_NUM_OUTPUTS = LAST_NODE_INDEX + 12;

    /** Index of the outputName field */
    protected static final int FIELD_OUTPUT_NAME = LAST_NODE_INDEX + 13;

    /** Index of the outputMinValue field */
    protected static final int FIELD_OUTPUT_MIN_VALUE = LAST_NODE_INDEX + 14;

    /** Index of the outputMaxValue field */
    protected static final int FIELD_OUTPUT_MAX_VALUE = LAST_NODE_INDEX + 15;

    /** Index of the outputResolution field */
    protected static final int FIELD_OUTPUT_RESOLUTION = LAST_NODE_INDEX + 16;

    /** Index of the axisWrap field */
    protected static final int FIELD_OUTPUT_WRAP = LAST_NODE_INDEX + 17;

    /** Index of the enabled field */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 18;

    /** The last field index used by this class */
    protected static final int LAST_HIDSENSOR_INDEX = FIELD_ENABLED;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_HIDSENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFBool enabled */
    protected boolean vfEnabled;

    /** field SFString name */
    protected String vfName;

    /** eventOut SFBool isActive */
    protected boolean vfIsActive;

    /** eventOut MFFloat axisValue */
    protected float[] vfAxisValue;

    /** eventOut MFFloat axisMinValue */
    protected float[] vfAxisMinValue;

    /** eventOut MFFloat axisMaxValue */
    protected float[] vfAxisMaxValue;

    /** eventOut MFString axisName */
    protected String[] vfAxisName;

    /** eventOut MFFloat axisResolution */
    protected float[] vfAxisResolution;

    /** eventOut MFBool axisWrap */
    protected boolean[] vfAxisWrap;

    /** eventOut SFString manufacturerName */
    protected String vfManufacturerName;

    /** eventOut SFInt numAxes */
    protected int vfNumAxes;

    /** eventOut SFInt numOutputs */
    protected int vfNumOutputs;

    /** eventIn MFFloat outputValue */
    protected float[] vfOutputValue;

    /** eventOut MFFloat outputMinValue */
    protected float[] vfOutputMinValue;

    /** eventOut MFFloat outputMaxValue */
    protected float[] vfOutputMaxValue;

    /** eventOut MFString outputName */
    protected String[] vfOutputName;

    /** eventOut MFFloat outputResolution */
    protected float[] vfOutputResolution;

    /** eventOut MFBoolean outputWrap */
    protected boolean[] vfOutputWrap;

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public BaseHIDSensor(String nodeName) {
        super(nodeName);

        hasChanged = new boolean[NUM_FIELDS];
        vfEnabled = true;
        vfIsActive = false;
        vfAxisValue = FieldConstants.EMPTY_MFFLOAT;
        vfAxisMinValue = FieldConstants.EMPTY_MFFLOAT;
        vfAxisMaxValue = FieldConstants.EMPTY_MFFLOAT;
        vfAxisName = FieldConstants.EMPTY_MFSTRING;
        vfAxisResolution = FieldConstants.EMPTY_MFFLOAT;
        vfAxisWrap = FieldConstants.EMPTY_MFBOOL;
        vfOutputMinValue = FieldConstants.EMPTY_MFFLOAT;
        vfOutputMaxValue = FieldConstants.EMPTY_MFFLOAT;
        vfOutputName = FieldConstants.EMPTY_MFSTRING;
        vfOutputResolution = FieldConstants.EMPTY_MFFLOAT;
        vfOutputWrap = FieldConstants.EMPTY_MFBOOL;
        vfNumAxes = 0;
        vfNumOutputs = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected void copy(VRMLNodeType node) {
        try {
            int index = node.getFieldIndex("enabled");
            VRMLFieldData field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("name");
            field = node.getFieldValue(index);
            vfName = field.stringValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLDeviceSensorNodeType.
    //----------------------------------------------------------
    /**
     * Get the name field.
     *
     * @param The name.
     */
    public String getName() {
        return vfName;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLSensorNodeType.
    //----------------------------------------------------------

    /**
     * Accessor method to get current value of field <b>enabled</b>,
     * default value is <code>true</code>.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
        return vfEnabled;
    }

    /**
     * Get the current value of the isActive field
     *
     * @return True if currently active, false otherwise
     */
    public boolean getIsActive() {
        return vfIsActive;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType.
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_HIDSENSOR_INDEX)
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
        return TypeConstants.DeviceSensorNodeType;
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
            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_IS_ACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_MANUFACTURER_NAME:
                fieldData.clear();
                fieldData.stringValue = vfManufacturerName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_NAME:
                fieldData.clear();
                fieldData.stringValue = vfName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_AXIS_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfAxisValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = vfAxisValue.length;
                break;

            case FIELD_AXIS_MIN_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfAxisMinValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = vfAxisMinValue.length;
                break;

            case FIELD_AXIS_MAX_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfAxisMaxValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = vfAxisMaxValue.length;
                break;

            case FIELD_AXIS_NAME:
                fieldData.clear();
                fieldData.stringArrayValue = vfAxisName;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfAxisName.length;
                break;

            case FIELD_AXIS_WRAP:
                fieldData.clear();
                fieldData.booleanArrayValue = vfAxisWrap;
                fieldData.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
                fieldData.numElements = vfAxisWrap.length;
                break;

            case FIELD_OUTPUT_MIN_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfOutputMinValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = vfOutputMinValue.length;
                break;

            case FIELD_OUTPUT_MAX_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfOutputMaxValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = vfOutputMaxValue.length;
                break;

            case FIELD_OUTPUT_NAME:
                fieldData.clear();
                fieldData.stringArrayValue = vfOutputName;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfOutputName.length;
                break;

            case FIELD_OUTPUT_WRAP:
                fieldData.clear();
                fieldData.booleanArrayValue = vfOutputWrap;
                fieldData.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
                fieldData.numElements = vfOutputWrap.length;
                break;

            case FIELD_NUM_AXES:
                fieldData.clear();
                fieldData.floatValue = vfNumAxes;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_NUM_OUTPUTS:
                fieldData.clear();
                fieldData.floatValue = vfNumOutputs;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
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
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_MANUFACTURER_NAME:
                    destNode.setValue(destIndex, vfManufacturerName);
                    break;

                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
                    break;

                case FIELD_NUM_AXES:
                    destNode.setValue(destIndex, vfNumAxes);
                    break;

                case FIELD_NUM_OUTPUTS:
                    destNode.setValue(destIndex, vfNumOutputs);
                    break;

                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;

                case FIELD_AXIS_VALUE:
                    destNode.setValue(destIndex, vfAxisValue, vfAxisValue.length);
                    break;

                case FIELD_AXIS_MIN_VALUE:
                    destNode.setValue(destIndex, vfAxisMinValue, vfAxisMinValue.length);
                    break;

                case FIELD_AXIS_MAX_VALUE:
                    destNode.setValue(destIndex, vfAxisMaxValue, vfAxisMaxValue.length);
                    break;

                case FIELD_AXIS_NAME:
                    destNode.setValue(destIndex, vfAxisName, vfAxisName.length);
                    break;

                case FIELD_AXIS_RESOLUTION:
                    destNode.setValue(destIndex, vfAxisResolution, vfAxisResolution.length);
                    break;

                case FIELD_AXIS_WRAP:
                    destNode.setValue(destIndex, vfAxisWrap, vfAxisWrap.length);
                    break;

                case FIELD_OUTPUT_VALUE:
                    destNode.setValue(destIndex, vfOutputValue, vfOutputValue.length);
                    break;

                case FIELD_OUTPUT_MIN_VALUE:
                    destNode.setValue(destIndex, vfOutputMinValue, vfOutputMinValue.length);
                    break;

                case FIELD_OUTPUT_MAX_VALUE:
                    destNode.setValue(destIndex, vfOutputMaxValue, vfOutputMaxValue.length);
                    break;

                case FIELD_OUTPUT_NAME:
                    destNode.setValue(destIndex, vfOutputName, vfOutputName.length);
                    break;

                case FIELD_OUTPUT_RESOLUTION:
                    destNode.setValue(destIndex, vfOutputResolution, vfOutputResolution.length);
                    break;

                case FIELD_OUTPUT_WRAP:
                    destNode.setValue(destIndex, vfOutputWrap, vfOutputWrap.length);
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
     * Set the value of the field at the given index as a boolean. This is
     * be used to set SFBool field types isActive, enabled and loop.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ENABLED:
                setEnabled(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a String. This is
     * be used to set SFString field types name.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_NAME:
                setName(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set a new state for the enabled field.
     *
     * @param state True if this sensor is to be enabled
     */
    public void setEnabled(boolean state) {
        if(state != vfEnabled) {
            vfEnabled = state;

            if(!inSetup) {
                hasChanged[FIELD_ENABLED] = true;
                fireFieldChanged(FIELD_ENABLED);
            }
        }
    }

    /**
     * Set a new state for the name field.
     *
     * @param name The new name
     */
    protected void setName(String name) {
        vfName = name;
        if(!inSetup) {
            hasChanged[FIELD_NAME] = true;
            fireFieldChanged(FIELD_NAME);
        }
    }

}
