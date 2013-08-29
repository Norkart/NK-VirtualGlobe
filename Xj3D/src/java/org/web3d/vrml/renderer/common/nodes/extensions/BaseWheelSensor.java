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
import org.xj3d.device.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a WheelSensor
 * <p>
 *
 * This node is an custom extension to Xj3D node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class BaseWheelSensor extends BaseHIDSensor {

    /** Index of the equilibriumPoint field */
    private static final int FIELD_EQUILIBRIUM_POINT = LAST_HIDSENSOR_INDEX + 1;

    /** Index of the button1 field */
    private static final int FIELD_BUTTON_1 = LAST_HIDSENSOR_INDEX + 2;

    /** Index of the button2 field */
    private static final int FIELD_BUTTON_2 = LAST_HIDSENSOR_INDEX + 3;

    /** Index of the button3 field */
    private static final int FIELD_BUTTON_3 = LAST_HIDSENSOR_INDEX + 4;

    /** Index of the button4 field */
    private static final int FIELD_BUTTON_4 = LAST_HIDSENSOR_INDEX + 5;

    /** Index of the wheelX field */
    private static final int FIELD_WHEEL_X = LAST_HIDSENSOR_INDEX + 6;

    /** Index of the throttleSlider field */
    private static final int FIELD_THROTTLE_SLIDER = LAST_HIDSENSOR_INDEX + 7;

    /** Index of the featuresAvailable field */
    private static final int FIELD_FEATURES_AVAILABLE = LAST_HIDSENSOR_INDEX + 8;

    /** The last field index used by this class */
    private static final int LAST_WHEEL_SENSOR_INDEX = FIELD_FEATURES_AVAILABLE;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_WHEEL_SENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** The featuresAvailable field */
    private String[] vfFeaturesAvailable;

    /** The equilibriumPoint eventOut */
    private float vfEquilibriumPoint;

    /** The wheelX eventOut */
    private float vfWheelX;

    /** The button1 eventOut */
    private boolean vfButton1;

    /** The button2 eventOut */
    private boolean vfButton2;

    /** The button3 eventOut */
    private boolean vfButton3;

    /** The button4 eventOut */
    private boolean vfButton4;

    /** The throttleSlider eventOut */
    private float vfThrottleSlider;

    /** The real device backing this node, could be null */
    private WheelDevice realdevice;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");

        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "name");

        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");

        fieldDecl[FIELD_AXIS_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisValue");

        fieldDecl[FIELD_AXIS_MIN_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisMinValue");

        fieldDecl[FIELD_AXIS_MAX_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisMaxValue");

        fieldDecl[FIELD_AXIS_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFString",
                                     "axisName");

        fieldDecl[FIELD_AXIS_RESOLUTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisResolution");

        fieldDecl[FIELD_AXIS_WRAP] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFBool",
                                     "axisWrap");

        fieldDecl[FIELD_OUTPUT_MIN_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "outputMinValue");

        fieldDecl[FIELD_OUTPUT_MAX_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "outputMaxValue");

        fieldDecl[FIELD_OUTPUT_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFString",
                                     "outputName");

        fieldDecl[FIELD_OUTPUT_RESOLUTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "outputResolution");

        fieldDecl[FIELD_OUTPUT_WRAP] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFBool",
                                     "outputWrap");

        fieldDecl[FIELD_NUM_AXES] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "numAxes");

        fieldDecl[FIELD_NUM_OUTPUTS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "numOutputs");

        fieldDecl[FIELD_MANUFACTURER_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFString",
                                     "manufacturerName");


        // Convenvience fields

        fieldDecl[FIELD_EQUILIBRIUM_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "equilibriumPoint");

        fieldDecl[FIELD_FEATURES_AVAILABLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFString",
                                     "featuresAvailable");

        fieldDecl[FIELD_WHEEL_X] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "wheelX");

        fieldDecl[FIELD_BUTTON_1] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "button1");

        fieldDecl[FIELD_BUTTON_2] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "button2");

        fieldDecl[FIELD_BUTTON_3] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "button3");

        fieldDecl[FIELD_BUTTON_4] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "button4");

        fieldDecl[FIELD_THROTTLE_SLIDER] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "throttleSlider");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_IS_ACTIVE);
        fieldMap.put("isActive", idx);
        fieldMap.put("isActive_changed", idx);

        idx = new Integer(FIELD_OUTPUT_VALUE);
        fieldMap.put("outputValue", idx);
        fieldMap.put("outputValue_changed", idx);

        idx = new Integer(FIELD_AXIS_VALUE);
        fieldMap.put("axisValue", idx);
        fieldMap.put("axisValue_changed", idx);

        idx = new Integer(FIELD_AXIS_MIN_VALUE);
        fieldMap.put("axisMinValue", idx);
        fieldMap.put("axisMinValue_changed", idx);

        idx = new Integer(FIELD_AXIS_MAX_VALUE);
        fieldMap.put("axisMaxValue", idx);
        fieldMap.put("axisMaxValue_changed", idx);

        idx = new Integer(FIELD_AXIS_NAME);
        fieldMap.put("axisName", idx);
        fieldMap.put("axisName_changed", idx);

        idx = new Integer(FIELD_AXIS_RESOLUTION);
        fieldMap.put("axisResolution", idx);
        fieldMap.put("axisResolution_changed", idx);

        idx = new Integer(FIELD_AXIS_WRAP);
        fieldMap.put("axisWrap", idx);
        fieldMap.put("axisWrap_changed", idx);

        idx = new Integer(FIELD_OUTPUT_MIN_VALUE);
        fieldMap.put("outputMinValue", idx);
        fieldMap.put("outputMinValue_changed", idx);

        idx = new Integer(FIELD_OUTPUT_MAX_VALUE);
        fieldMap.put("outputMaxValue", idx);
        fieldMap.put("outputMaxValue_changed", idx);

        idx = new Integer(FIELD_OUTPUT_NAME);
        fieldMap.put("outputName", idx);
        fieldMap.put("outputName_changed", idx);

        idx = new Integer(FIELD_OUTPUT_RESOLUTION);
        fieldMap.put("outputResolution", idx);
        fieldMap.put("outputResolution_changed", idx);

        idx = new Integer(FIELD_OUTPUT_WRAP);
        fieldMap.put("outputWrap", idx);
        fieldMap.put("outputWrap_changed", idx);

        idx = new Integer(FIELD_NUM_AXES);
        fieldMap.put("numAxes", idx);
        fieldMap.put("numAxes_changed", idx);

        idx = new Integer(FIELD_NUM_OUTPUTS);
        fieldMap.put("numOutputs", idx);
        fieldMap.put("numOutputs_changed", idx);

        idx = new Integer(FIELD_FEATURES_AVAILABLE);
        fieldMap.put("featuresAvailable", idx);
        fieldMap.put("featuresAvailable_changed", idx);

        idx = new Integer(FIELD_MANUFACTURER_NAME);
        fieldMap.put("manufacturerName", idx);
        fieldMap.put("manufacturerName_changed", idx);


        // Convience Fields
        idx = new Integer(FIELD_WHEEL_X);
        fieldMap.put("wheelX", idx);
        fieldMap.put("wheelX_changed", idx);

        idx = new Integer(FIELD_EQUILIBRIUM_POINT);
        fieldMap.put("equilibriumPoint", idx);
        fieldMap.put("equilibriumPoint_changed", idx);

        idx = new Integer(FIELD_BUTTON_1);
        fieldMap.put("button1", idx);
        fieldMap.put("button1_changed", idx);

        idx = new Integer(FIELD_BUTTON_2);
        fieldMap.put("button2", idx);
        fieldMap.put("button2_changed", idx);

        idx = new Integer(FIELD_BUTTON_2);
        fieldMap.put("button2", idx);
        fieldMap.put("button2_changed", idx);

        idx = new Integer(FIELD_BUTTON_3);
        fieldMap.put("button3", idx);
        fieldMap.put("button3_changed", idx);

        idx = new Integer(FIELD_BUTTON_4);
        fieldMap.put("button4", idx);
        fieldMap.put("button4_changed", idx);

        idx = new Integer(FIELD_THROTTLE_SLIDER);
        fieldMap.put("throttleSlider", idx);
        fieldMap.put("throttleSlider_changed", idx);
    }



    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public BaseWheelSensor() {
        super("WheelSensor");

        hasChanged = new boolean[NUM_FIELDS];
        vfFeaturesAvailable = FieldConstants.EMPTY_MFSTRING;
        vfEquilibriumPoint = 0;
        vfWheelX  = 0;
        vfButton1 = false;
        vfButton1 = false;
        vfButton1 = false;
        vfButton1 = false;
        vfThrottleSlider = 0f;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseWheelSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy(node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLDeviceNodeType
    //----------------------------------------------------------

    /**
     * Set the realdevice backing this node.  This will not be called
     * if the device name does not map to a live device.
     *
     * @param device The real device.
     */
    public void setDevice(InputDevice device) {
        realdevice = (WheelDevice) device;
    }

    /**
     * Update this nodes field from the underlying device.
     *
     * @param device The device.
     */
    public void update(DeviceState state) {
        WheelState gpState = (WheelState) state;

        if (gpState.wheelX_changed) {
            vfWheelX = gpState.wheelX;

            hasChanged[FIELD_WHEEL_X] = true;
            fireFieldChanged(FIELD_WHEEL_X);
        }

        if (gpState.button1_changed) {
            vfButton1 = gpState.button1;

            hasChanged[FIELD_BUTTON_1] = true;
            fireFieldChanged(FIELD_BUTTON_1);
        }

        if (gpState.button2_changed) {
            vfButton2 = gpState.button2;

            hasChanged[FIELD_BUTTON_2] = true;
            fireFieldChanged(FIELD_BUTTON_2);
        }

        if (gpState.button3_changed) {
            vfButton3 = gpState.button3;

            hasChanged[FIELD_BUTTON_3] = true;
            fireFieldChanged(FIELD_BUTTON_3);
        }

        if (gpState.button4_changed) {
            vfButton1 = gpState.button4;

            hasChanged[FIELD_BUTTON_4] = true;
            fireFieldChanged(FIELD_BUTTON_4);
        }

        if (gpState.throttleSlider_changed) {
            vfThrottleSlider = gpState.throttleSlider;

            hasChanged[FIELD_THROTTLE_SLIDER] = true;
            fireFieldChanged(FIELD_THROTTLE_SLIDER);
        }
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
        if(index < 0  || index > LAST_WHEEL_SENSOR_INDEX)
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
            case FIELD_FEATURES_AVAILABLE:
                fieldData.clear();
                fieldData.stringArrayValue = vfFeaturesAvailable;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfFeaturesAvailable.length;
                break;

            case FIELD_WHEEL_X:
                fieldData.clear();
                fieldData.floatValue = vfWheelX;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_THROTTLE_SLIDER:
                fieldData.clear();
                fieldData.floatValue = vfThrottleSlider;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_BUTTON_1:
                fieldData.clear();
                fieldData.booleanValue = vfButton1;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_BUTTON_2:
                fieldData.clear();
                fieldData.booleanValue = vfButton2;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_BUTTON_3:
                fieldData.clear();
                fieldData.booleanValue = vfButton3;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_BUTTON_4:
                fieldData.clear();
                fieldData.booleanValue = vfButton4;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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
                case FIELD_WHEEL_X:
                    destNode.setValue(destIndex, vfWheelX);
                    break;

                case FIELD_THROTTLE_SLIDER:
                    destNode.setValue(destIndex, vfThrottleSlider);
                    break;

                case FIELD_BUTTON_1:
                    destNode.setValue(destIndex, vfButton1);
                    break;

                case FIELD_BUTTON_2:
                    destNode.setValue(destIndex, vfButton2);
                    break;

                case FIELD_BUTTON_3:
                    destNode.setValue(destIndex, vfButton3);
                    break;

                case FIELD_BUTTON_4:
                    destNode.setValue(destIndex, vfButton4);
                    break;

                case FIELD_FEATURES_AVAILABLE:
                    destNode.setValue(destIndex, vfFeaturesAvailable,
                        vfFeaturesAvailable.length);
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
     * Set the value of the field at the given index as a float. This is
     * be used to set SFFloat field types name.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_EQUILIBRIUM_POINT:
                setEquilibriumPoint(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the equilibriumPoint value.
     *
     * @param val The value.
     */
    protected void setEquilibriumPoint(float val) {
        if (realdevice != null) {
            realdevice.setEquilibriumPoint(val);
        }
    }
}
