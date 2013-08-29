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

package org.web3d.vrml.renderer.common.nodes.shape;

// Standard imports
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLPointPropertiesNodeType;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a PointProperties node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class BasePointProperties extends AbstractNode
    implements VRMLPointPropertiesNodeType {

    /** Field index for pointsizeScaleFactor */
    protected static final int FIELD_POINTSIZE_SCALE_FACTOR = LAST_NODE_INDEX + 1;

    /** Field index for pointsizeMinValue */
    protected static final int FIELD_POINTSIZE_MIN_VALUE = LAST_NODE_INDEX + 2;

    /** Field index for pointsizeMaxValue */
    protected static final int FIELD_POINTSIZE_MAX_VALUE = LAST_NODE_INDEX + 3;

    /** Field index for pointsizeAttenuation */
    protected static final int FIELD_POINTSIZE_ATTENUATION = LAST_NODE_INDEX + 4;

    /** Field index for colorMode */
    protected static final int FIELD_COLOR_MODE = LAST_NODE_INDEX + 5;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = FIELD_COLOR_MODE + 1;

    /** Error message when the user code barfs */
    private static final String COLOR_MODE_ERROR_MSG =
        "Error sending color mode changed notification to: ";

    /** Message for when the color mode is invalid */
    private static final String UNKNOWN_TYPE_MSG =
        "The color mode provided is not recognised";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFFloat pointsizeScaleFactor 0 */
    protected float vfPointsizeScaleFactor;

    /** exposedField SFFloat pointsizeMinValue 0 */
    protected float vfPointsizeMinValue;

    /** exposedField SFFloat pointsizeMaxValue 0 */
    protected float vfPointsizeMaxValue;

    /** exposedField SFVec3f pointsizeAttenuation 1 0 0 */
    protected float[] vfPointsizeAttenuation;

    /** exposedField SFString colorMode 0 */
    protected String vfColorMode;

    /** Constant representing the mode used */
    protected int colorMode;

    /** Constant describing texture color mode */
    protected static final String TEXTURE_COLORMODE = "TEXTURE_COLOR";

    /** Constant describing point color mode */
    protected static final String POINT_COLORMODE = "POINT_COLOR";

    /** Constant describing texture and point mode */
    protected static final String TEXTURE_AND_POINT_COLORMODE = "TEXTURE_AND_POINT_COLOR";

    /** Mapping between type string and type ints */
    private static HashMap typeMap;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_POINTSIZE_SCALE_FACTOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "pointsizeScaleFactor");
        fieldDecl[FIELD_POINTSIZE_MIN_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "pointsizeMinValue");
        fieldDecl[FIELD_POINTSIZE_MAX_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "pointsizeMaxValue");
        fieldDecl[FIELD_POINTSIZE_ATTENUATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "pointsizeAttenuation");
        fieldDecl[FIELD_COLOR_MODE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "colorMode");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_POINTSIZE_SCALE_FACTOR);
        fieldMap.put("pointsizeScaleFactor", idx);
        fieldMap.put("set_pointsizeScaleFactor", idx);
        fieldMap.put("pointsizeScaleFactor_changed", idx);

        idx = new Integer(FIELD_POINTSIZE_MIN_VALUE);
        fieldMap.put("pointsizeMinValue", idx);
        fieldMap.put("set_pointsizeMinValue", idx);
        fieldMap.put("pointsizeMinValue_changed", idx);

        idx = new Integer(FIELD_POINTSIZE_MAX_VALUE);
        fieldMap.put("pointsizeMaxValue", idx);
        fieldMap.put("set_pointsizeMaxValue", idx);
        fieldMap.put("pointsizeMaxValue_changed", idx);

        idx = new Integer(FIELD_POINTSIZE_ATTENUATION);
        fieldMap.put("pointsizeAttenuation", idx);
        fieldMap.put("set_pointsizeAttenuation", idx);
        fieldMap.put("pointsizeAttenuation_changed", idx);

        idx = new Integer(FIELD_COLOR_MODE);
        fieldMap.put("colorMode", idx);
        fieldMap.put("set_colorMode", idx);
        fieldMap.put("colorMode_changed", idx);

        typeMap = new HashMap();
        typeMap.put(TEXTURE_COLORMODE, new Integer(TEXTURE_COLOR_MODE));
        typeMap.put(POINT_COLORMODE, new Integer(POINT_COLOR_MODE));
        typeMap.put(TEXTURE_AND_POINT_COLORMODE, new Integer(TEXTURE_AND_POINT_COLOR_MODE));

    }

    /**
     * Construct a default instance of the material
     */
    protected BasePointProperties() {
        super("PointProperties");

        hasChanged = new boolean[NUM_FIELDS];

        vfPointsizeScaleFactor = 1;
        vfPointsizeMinValue = 1;
        vfPointsizeMaxValue = 1;
        vfPointsizeAttenuation = new float[] { 1, 0, 0 };
        vfColorMode = TEXTURE_COLORMODE;
        colorMode = TEXTURE_COLOR_MODE;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BasePointProperties(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("pointsizeScaleFactor");
            VRMLFieldData field = node.getFieldValue(index);
            vfPointsizeScaleFactor = field.floatValue;

            index = node.getFieldIndex("pointsizeMinValue");
            field = node.getFieldValue(index);
            vfPointsizeMinValue = field.floatValue;

            index = node.getFieldIndex("pointsizeMaxValue");
            field = node.getFieldValue(index);
            vfPointsizeMaxValue = field.floatValue;

            index = node.getFieldIndex("pointsizeAttenuation");
            field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfPointsizeAttenuation[0] = field.floatArrayValue[0];
                vfPointsizeAttenuation[1] = field.floatArrayValue[1];
                vfPointsizeAttenuation[2] = field.floatArrayValue[2];
            }

            index = node.getFieldIndex("colorMode");
            field = node.getFieldValue(index);
            vfColorMode = field.stringValue;
            colorMode = ((Integer)typeMap.get(vfColorMode)).intValue();
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
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
        if (index < 0  || index > NUM_FIELDS - 1)
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
        return TypeConstants.PointPropertiesNodeType;
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
        VRMLFieldData fieldData = (VRMLFieldData)fieldLocalData.get();

        switch(index) {
            case FIELD_POINTSIZE_SCALE_FACTOR:
                fieldData.clear();
                fieldData.floatValue = vfPointsizeScaleFactor;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_POINTSIZE_MIN_VALUE:
                fieldData.clear();
                fieldData.floatValue = vfPointsizeMinValue;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_POINTSIZE_MAX_VALUE:
                fieldData.clear();
                fieldData.floatValue = vfPointsizeMaxValue;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_POINTSIZE_ATTENUATION:
                fieldData.clear();
                fieldData.floatArrayValue = vfPointsizeAttenuation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_COLOR_MODE:
                fieldData.clear();
                fieldData.stringValue = vfColorMode;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
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
                case FIELD_POINTSIZE_SCALE_FACTOR:
                    destNode.setValue(destIndex, vfPointsizeScaleFactor);
                    break;

                case FIELD_POINTSIZE_MIN_VALUE:
                    destNode.setValue(destIndex, vfPointsizeMinValue);
                    break;

                case FIELD_POINTSIZE_MAX_VALUE:
                    destNode.setValue(destIndex, vfPointsizeMaxValue);
                    break;

                case FIELD_POINTSIZE_ATTENUATION:
                    destNode.setValue(destIndex, vfPointsizeAttenuation, 3);
                    break;

                case FIELD_COLOR_MODE:
                    destNode.setValue(destIndex, vfColorMode);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an int.
     * This would be used to set String field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_COLOR_MODE:
                setColorMode(value);
                break;

            default:
                super.setValue(index, value);
        }
    }


    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_POINTSIZE_SCALE_FACTOR:
                setPointSizeScale(value);
                break;

            case FIELD_POINTSIZE_MIN_VALUE:
                setPointSizeMin(value);
                break;

            case FIELD_POINTSIZE_MAX_VALUE:
                setPointSizeMax(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFColor and SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_POINTSIZE_ATTENUATION:
                setPointSizeAttenuation(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the point size scale factor to the new value.
     *
     * @param value The scale value to check
     * @throws InvalidFieldValueException One of the colour components are out
     *     of range
     */
    protected void setPointSizeScale(float value)
        throws InvalidFieldValueException {

        if(value <= 0)
            throw new InvalidFieldValueException(
                "The pointsizeScaleFactor is <= 0: " + value);

        vfPointsizeScaleFactor = value;

        if(!inSetup) {
            hasChanged[FIELD_POINTSIZE_SCALE_FACTOR] = true;
            fireFieldChanged(FIELD_POINTSIZE_SCALE_FACTOR);
        }
    }

   /**
     * Set the point size min value to the new value.
     *
     * @param value The min value to set
     * @throws InvalidFieldValueException The submitted value is out
     *     of range
     */
    protected void setPointSizeMin(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(
                "The pointsizeMinValue is < 0: " + value);

        vfPointsizeMinValue = (value == 0) ? 1 : value;

        if(!inSetup) {
            hasChanged[FIELD_POINTSIZE_MIN_VALUE] = true;
            fireFieldChanged(FIELD_POINTSIZE_MIN_VALUE);
        }
    }

   /**
     * Set the point size max value to the new value.
     *
     * @param value The max value to set
     * @throws InvalidFieldValueException The submitted value is out
     *     of range
     */
    protected void setPointSizeMax(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(
                "The pointsizeMaxValue is < 0: " + value);

        vfPointsizeMaxValue = (value == 0) ? 1 : value;

        if(!inSetup) {
            hasChanged[FIELD_POINTSIZE_MAX_VALUE] = true;
            fireFieldChanged(FIELD_POINTSIZE_MAX_VALUE);
        }
    }

    /**
     * Set the attenuation factor of the point size.
     *
     * @param factor The new attenuation factor to use
     * @throws InvalidFieldValueException Attenuation was not [0,1]
     */
    protected void setPointSizeAttenuation(float[] factor)
        throws InvalidFieldValueException {

        if((factor[0] < 0) || (factor[1] < 0) || (factor[2] < 0) ||
           (factor[0] > 1) || (factor[1] > 1) || (factor[2] > 1))
            throw new InvalidFieldValueException("attenuation value out of range [0,1]");

        if((factor[0] == 0) && (factor[1] == 0) && (factor[2] == 0))
            factor[0] = 1;

        vfPointsizeAttenuation[0] = factor[0];
        vfPointsizeAttenuation[1] = factor[1];
        vfPointsizeAttenuation[2] = factor[2];

        if(!inSetup) {
            hasChanged[FIELD_POINTSIZE_ATTENUATION] = true;
            fireFieldChanged(FIELD_POINTSIZE_ATTENUATION);
        }
    }

    /**
     * Get the currently set color mode. Will be one of the above
     * constant values.
     *
     * @return One of TEXTURE_COLORMODE, COLOR_COLORMODE, TEXTURE_AND_POINT_COLORMODE
     */
    public int getColorMode() {
        return colorMode;
    }

    /**
     * Set the color mode to one of the new values. If the value is not known,
     * issue an exception and leave the value at the current.
     *
     * @param mode Constant indicating the mode.
     * @throws InvalidFieldValueException The value type is unknown
     */
    public void setColorMode(int mode) throws InvalidFieldValueException {

        if(mode < DISABLE_COLOR_MODE && mode > TEXTURE_AND_POINT_COLOR_MODE)
            throw new InvalidFieldValueException(UNKNOWN_TYPE_MSG);

        colorMode = mode;

        if(!inSetup) {
            hasChanged[FIELD_COLOR_MODE] = true;
            fireFieldChanged(FIELD_COLOR_MODE);
        }
    }

   /**
     * Set the color mode to the new value.
     *
     * @param value The color mode to set
     * @throws InvalidFieldValueException The submitted value is out
     *     of range
     */
    public void setColorMode(String mode)
        throws InvalidFieldValueException {

        if (typeMap.get(mode) != null) {
            vfColorMode = mode;
            Integer i_mode = (Integer)typeMap.get(mode);
            setColorMode(i_mode.intValue());
        } else
            throw new InvalidFieldValueException(UNKNOWN_TYPE_MSG + ": " + mode);
    }
}
