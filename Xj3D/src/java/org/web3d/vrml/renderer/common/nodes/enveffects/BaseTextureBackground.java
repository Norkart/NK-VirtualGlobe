/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.enveffects;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.HashSet;

import org.web3d.vrml.nodes.VRMLContentStateListener;
import org.web3d.vrml.nodes.VRMLBackgroundNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLUrlListener;

import org.web3d.vrml.renderer.common.nodes.BaseBindableNode;

import org.web3d.vrml.util.URLChecker;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a TextureBackground node.
 * <p>
 *
 * TODO: This is a very light copy of background right now to get transparency in
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public abstract class BaseTextureBackground extends BaseBindableNode
    implements VRMLBackgroundNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.BindableNodeType,
    };

    /** Index of the groundAngle field */
    protected static final int FIELD_GROUND_ANGLE = LAST_BINDABLE_INDEX + 1;

    /** Index of the groundColor field */
    protected static final int FIELD_GROUND_COLOR = LAST_BINDABLE_INDEX + 2;

    /** Index of the skyAngle field */
    protected static final int FIELD_SKY_ANGLE = LAST_BINDABLE_INDEX + 3;

    /** Index of the skyColor field */
    protected static final int FIELD_SKY_COLOR = LAST_BINDABLE_INDEX + 4;

    /** Index of the backUrl field */
    protected static final int FIELD_BACK_TEXTURE = LAST_BINDABLE_INDEX + 5;

    /** Index of the frontUrl field */
    protected static final int FIELD_FRONT_TEXTURE = LAST_BINDABLE_INDEX + 6;

    /** Index of the leftUrl field */
    protected static final int FIELD_LEFT_TEXTURE = LAST_BINDABLE_INDEX + 7;

    /** Index of the rightUrl field */
    protected static final int FIELD_RIGHT_TEXTURE = LAST_BINDABLE_INDEX + 8;

    /** Index of the bottomUrl field */
    protected static final int FIELD_BOTTOM_TEXTURE = LAST_BINDABLE_INDEX + 9;

    /** Index of the topUrl field */
    protected static final int FIELD_TOP_TEXTURE = LAST_BINDABLE_INDEX + 10;

    /** Index of the topUrl field */
    protected static final int FIELD_TRANSPARENCY = LAST_BINDABLE_INDEX + 11;

    // Local working constants

    /** The last field index used by this class */
    protected static final int LAST_BACKGROUND_INDEX = FIELD_TRANSPARENCY;

    /** The number of fields implemented */
    protected static final int NUM_FIELDS = LAST_BACKGROUND_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;


    /** An empty list of URL fields for initialisation */
    protected static final String[] EMPTY_LIST = {};

    // Side constants for readability when generating the background box.
    // Also used to control the shown SwitchGroup as children
    protected static final int BACK   = 0;
    protected static final int FRONT  = 1;
    protected static final int LEFT   = 2;
    protected static final int RIGHT  = 3;
    protected static final int TOP    = 4;
    protected static final int BOTTOM = 5;
    protected static final int SKY_SPHERE = 6;
    protected static final int GROUND_SPHERE = 7;
    protected static final int NUM_BG_OBJECTS = 8;

    // Field declarations

    /** MFString backTexture list */
    protected VRMLNodeType vfBackTexture;

    /** MFString frontTexture list */
    protected VRMLNodeType vfFrontTexture;

    /** MFString leftTexture list */
    protected VRMLNodeType vfLeftTexture;

    /** MFString rightTexture list */
    protected VRMLNodeType vfRightTexture;

    /** MFString topTexture list */
    protected VRMLNodeType vfTopTexture;

    /** MFString bottomTexture list */
    protected VRMLNodeType vfBottomTexture;

    /** MFFloat groundAngle */
    protected float[] vfGroundAngle;

    /** MFColor groundColor */
    protected float[] vfGroundColor;

    /** MFFloat skyAngle */
    protected float[] vfSkyAngle;

    /** MFColor skyColor */
    protected float[] vfSkyColor;

    /** SFFloat transparency */
    protected float vfTransparency;

    /** Number of valid values in vfGroundAngle */
    protected int numGroundAngle;

    /** Number of valid values in vfGroundColor */
    protected int numGroundColor;

    /** Number of valid values in vfSkyAngle */
    protected int numSkyAngle;

    /** Number of valid values in vfSkyColor */
    protected int numSkyColor;

    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_FRONT_TEXTURE,
            FIELD_BACK_TEXTURE,
            FIELD_LEFT_TEXTURE,
            FIELD_RIGHT_TEXTURE,
            FIELD_TOP_TEXTURE,
            FIELD_BOTTOM_TEXTURE,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BIND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "set_bind");
        fieldDecl[FIELD_IS_BOUND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isBound");
        fieldDecl[FIELD_BIND_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "bindTime");
        fieldDecl[FIELD_GROUND_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "groundAngle");
        fieldDecl[FIELD_GROUND_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFColor",
                                     "groundColor");
        fieldDecl[FIELD_SKY_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "skyAngle");
        fieldDecl[FIELD_SKY_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFColor",
                                     "skyColor");
        fieldDecl[FIELD_BACK_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "backTexture");
        fieldDecl[FIELD_FRONT_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "frontTexture");
        fieldDecl[FIELD_LEFT_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "leftTexture");
        fieldDecl[FIELD_RIGHT_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "rightTexture");
        fieldDecl[FIELD_TOP_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "topTexture");
        fieldDecl[FIELD_BOTTOM_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "bottomTexture");
        fieldDecl[FIELD_TRANSPARENCY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "transparency");


        fieldMap.put("set_bind",new Integer(FIELD_BIND));
        fieldMap.put("isBound",new Integer(FIELD_IS_BOUND));

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_BIND_TIME);
        fieldMap.put("bindTime", idx);
        fieldMap.put("bindTime_changed", idx);

        idx = new Integer(FIELD_BACK_TEXTURE);
        fieldMap.put("backTexture", idx);
        fieldMap.put("set_backTexture", idx);
        fieldMap.put("backTexture_changed", idx);

        idx = new Integer(FIELD_FRONT_TEXTURE);
        fieldMap.put("frontTexture", idx);
        fieldMap.put("set_frontTexture", idx);
        fieldMap.put("frontTexture_changed", idx);

        idx = new Integer(FIELD_LEFT_TEXTURE);
        fieldMap.put("leftTexture", idx);
        fieldMap.put("set_leftTexture", idx);
        fieldMap.put("leftTexture_changed", idx);

        idx = new Integer(FIELD_RIGHT_TEXTURE);
        fieldMap.put("rightTexture", idx);
        fieldMap.put("set_rightTexture", idx);
        fieldMap.put("rightTexture_changed", idx);

        idx = new Integer(FIELD_TOP_TEXTURE);
        fieldMap.put("topTexture", idx);
        fieldMap.put("set_topTexture", idx);
        fieldMap.put("topTexture_changed", idx);

        idx = new Integer(FIELD_BOTTOM_TEXTURE);
        fieldMap.put("bottomTexture", idx);
        fieldMap.put("set_bottomTexture", idx);
        fieldMap.put("bottomTexture_changed", idx);

        idx = new Integer(FIELD_GROUND_ANGLE);
        fieldMap.put("groundAngle", idx);
        fieldMap.put("set_groundAngle", idx);
        fieldMap.put("groundAngle_changed", idx);

        idx = new Integer(FIELD_GROUND_COLOR);
        fieldMap.put("groundColor", idx);
        fieldMap.put("set_groundColor", idx);
        fieldMap.put("groundColor_changed", idx);

        idx = new Integer(FIELD_SKY_ANGLE);
        fieldMap.put("skyAngle", idx);
        fieldMap.put("set_skyAngle", idx);
        fieldMap.put("skyAngle_changed", idx);

        idx = new Integer(FIELD_SKY_COLOR);
        fieldMap.put("skyColor", idx);
        fieldMap.put("set_skyColor", idx);
        fieldMap.put("skyColor_changed", idx);

        idx = new Integer(FIELD_TRANSPARENCY);
        fieldMap.put("transparency", idx);
        fieldMap.put("set_transparency", idx);
        fieldMap.put("transparency_changed", idx);

    }

    /**
     * Create a new, default instance of this class.
     */
    protected BaseTextureBackground() {
        super("TextureBackground");

        hasChanged = new boolean[NUM_FIELDS];
        vfSkyColor = new float[] {0, 0, 0};

        vfGroundAngle = FieldConstants.EMPTY_MFFLOAT;
        vfGroundColor = FieldConstants.EMPTY_MFFLOAT;
        vfSkyAngle = FieldConstants.EMPTY_MFFLOAT;
        vfTransparency = 0;

        numGroundAngle = 0;
        numGroundColor = 0;
        numSkyAngle = 0;
        numSkyColor = 3;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     * <P>
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type.
     */
    protected BaseTextureBackground(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index;
            VRMLFieldData field;

            index = node.getFieldIndex("transparency");
            field = node.getFieldValue(index);

            vfTransparency = field.floatValue;

            index = node.getFieldIndex("groundAngle");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfGroundAngle = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,0,vfGroundAngle,0,
                    field.numElements);

                numGroundAngle = field.numElements;
            }

            index = node.getFieldIndex("groundColor");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfGroundColor = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,0,vfGroundColor,0,
                    field.numElements);

                numGroundColor = field.numElements * 3;
            }

            index = node.getFieldIndex("skyAngle");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSkyAngle = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,0,vfSkyAngle,0,
                    field.numElements);

                numSkyAngle = field.numElements;
            }

            index = node.getFieldIndex("skyColor");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSkyColor = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,0,vfSkyColor,0,
                    field.numElements);

                numSkyColor = field.numElements * 3;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLBackgroundNodeType
    //----------------------------------------------------------

    /**
     * Get the transparency of the background.
     */
    public float getTransparency() {
        return vfTransparency;
    }

    /**
     * Set the transparency of the background.
     *
     * @param val The transparency value
     */
    public void setTransparency(float val) {
        vfTransparency = val;

        if(!inSetup) {
            hasChanged[FIELD_TRANSPARENCY] = true;
            fireFieldChanged(FIELD_TRANSPARENCY);
        }
    }

    /**
     * Get the number of valid sky color values that are currently defined.
     *
     * @return The number of values
     */
    public int getNumSkyColors() {
        return numSkyColor / 3;
    }

    /**
     * Get the number of valid ground color values that are currently defined.
     *
     * @return The number of values
     */
    public int getNumGroundColors() {
        return numGroundColor / 3;
    }

    /**
     * Fetch the color and angles for the sky values. Assumes that the sky
     * color size is at least 1.
     *
     * @param color The array to return the color values in
     * @param angle The array to return the angle values in
     */
    public void getSkyValues(float[] color, float[] angle) {
        System.arraycopy(vfSkyColor, 0, color, 0, numSkyColor);

        if(numSkyAngle != 0)
            System.arraycopy(vfSkyAngle, 0, angle, 0, numSkyAngle);
    }

    /**
     * Fetch the color and angles for the ground values. Assumes that the ground
     * color size is at least 1.
     *
     * @param color The array to return the color values in
     * @param angle The array to return the angle values in
     */
    public void getGroundValues(float[] color, float[] angle) {
        System.arraycopy(vfGroundColor, 0, color, 0, numGroundColor);

        if(numGroundAngle != 0)
            System.arraycopy(vfGroundAngle, 0, angle, 0, numGroundAngle);
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
        if (index < 0  || index > LAST_BACKGROUND_INDEX)
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
        return TypeConstants.BackgroundNodeType;
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
            case FIELD_SKY_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfSkyColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numSkyColor / 3;
                break;

            case FIELD_SKY_ANGLE:
                fieldData.clear();
                fieldData.floatArrayValue = vfSkyAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numSkyAngle;
                break;

            case FIELD_GROUND_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfGroundColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numGroundColor / 3;
                break;

            case FIELD_GROUND_ANGLE:
                fieldData.clear();
                fieldData.floatArrayValue = vfGroundAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numGroundAngle;
                break;

            case FIELD_TRANSPARENCY:
                fieldData.clear();
                fieldData.floatValue = vfTransparency;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_FRONT_TEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfFrontTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BACK_TEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfBackTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_LEFT_TEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfLeftTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_RIGHT_TEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfRightTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_TOP_TEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfTopTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BOTTOM_TEXTURE:
                fieldData.clear();
                fieldData.nodeValue = vfBottomTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
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
                case FIELD_GROUND_ANGLE:
                    destNode.setValue(destIndex, vfGroundAngle, numGroundAngle);
                    break;

                case FIELD_GROUND_COLOR:
                    destNode.setValue(destIndex, vfGroundColor, numGroundColor);
                    break;

                case FIELD_SKY_ANGLE:
                    destNode.setValue(destIndex, vfSkyAngle, numSkyAngle);
                    break;

                case FIELD_SKY_COLOR:
                    destNode.setValue(destIndex, vfSkyColor, numSkyColor);
                    break;

                case FIELD_TRANSPARENCY:
                    destNode.setValue(destIndex, vfTransparency);
                    break;

                case FIELD_FRONT_TEXTURE:
                    destNode.setValue(destIndex, vfFrontTexture);
                    break;

                case FIELD_BACK_TEXTURE:
                    destNode.setValue(destIndex, vfBackTexture);
                    break;

                case FIELD_LEFT_TEXTURE:
                    destNode.setValue(destIndex, vfLeftTexture);
                    break;

                case FIELD_RIGHT_TEXTURE:
                    destNode.setValue(destIndex, vfRightTexture);
                    break;

                case FIELD_TOP_TEXTURE:
                    destNode.setValue(destIndex, vfTopTexture);
                    break;

                case FIELD_BOTTOM_TEXTURE:
                    destNode.setValue(destIndex, vfBottomTexture);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFFloat fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TRANSPARENCY:
               setTransparency(value);
               break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFVec3f field types bboxCenter and bboxSize.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GROUND_ANGLE:
                if(numValid > vfGroundAngle.length)
                    vfGroundAngle = new float[numValid];

                System.arraycopy(value, 0, vfGroundAngle, 0, numValid);
                numGroundAngle = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_GROUND_ANGLE] = true;
                    fireFieldChanged(FIELD_GROUND_ANGLE);
                }
                break;

            case FIELD_GROUND_COLOR:
                FieldValidator.checkColorArray("Background.GroundColor", value);

                if(numValid > vfGroundColor.length)
                    vfGroundColor = new float[numValid];

                System.arraycopy(value, 0, vfGroundColor, 0, numValid);
                numGroundColor = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_GROUND_COLOR] = true;
                    fireFieldChanged(FIELD_GROUND_COLOR);
                }
                break;

            case FIELD_SKY_ANGLE:
                if(value.length > vfSkyAngle.length)
                    vfSkyAngle = new float[numValid];

                System.arraycopy(value, 0, vfSkyAngle, 0, numValid);
                numSkyAngle = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_SKY_ANGLE] = true;
                    fireFieldChanged(FIELD_SKY_ANGLE);
                }
                break;

            case FIELD_SKY_COLOR:
                FieldValidator.checkColorArray("Background.SkyColor", value);
                if(numValid > vfSkyColor.length)
                    vfSkyColor = new float[numValid];

                System.arraycopy(value, 0, vfSkyColor, 0, numValid);
                numSkyColor = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_SKY_COLOR] = true;
                    fireFieldChanged(FIELD_SKY_COLOR);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field type url.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        boolean notif = false;

        switch(index) {
            case FIELD_FRONT_TEXTURE:
                vfFrontTexture = child;

                if(!inSetup) {
                    hasChanged[FIELD_FRONT_TEXTURE] = true;
                    fireFieldChanged(FIELD_FRONT_TEXTURE);
                }
                break;

            case FIELD_BACK_TEXTURE:
                vfBackTexture = child;

                if(!inSetup) {
                    hasChanged[FIELD_BACK_TEXTURE] = true;
                    fireFieldChanged(FIELD_BACK_TEXTURE);
                }
                break;

            case FIELD_LEFT_TEXTURE:
                vfLeftTexture = child;

                if(!inSetup) {
                    hasChanged[FIELD_LEFT_TEXTURE] = true;
                    fireFieldChanged(FIELD_LEFT_TEXTURE);
                }
                break;

            case FIELD_RIGHT_TEXTURE:
                vfRightTexture = child;

                if(!inSetup) {
                    hasChanged[FIELD_RIGHT_TEXTURE] = true;
                    fireFieldChanged(FIELD_RIGHT_TEXTURE);
                }
                break;

            case FIELD_TOP_TEXTURE:
                vfTopTexture = child;

                if(!inSetup) {
                    hasChanged[FIELD_TOP_TEXTURE] = true;
                    fireFieldChanged(FIELD_TOP_TEXTURE);
                }
                break;

            case FIELD_BOTTOM_TEXTURE:
                vfBottomTexture = child;

                if(!inSetup) {
                    hasChanged[FIELD_BOTTOM_TEXTURE] = true;
                    fireFieldChanged(FIELD_BOTTOM_TEXTURE);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------
}
