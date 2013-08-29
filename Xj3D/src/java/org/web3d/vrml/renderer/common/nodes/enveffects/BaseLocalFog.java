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

package org.web3d.vrml.renderer.common.nodes.enveffects;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLBindableNodeType;
import org.web3d.vrml.nodes.VRMLFogNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a LocalFog node.
 * <p>
 *
 * This node is an custom extension to Xj3D node.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseLocalFog extends AbstractNode
    implements VRMLFogNodeType {

    /** Message for when the fog type is invalid */
    private static final String UNKNOWN_TYPE_MSG =
        "The fog type provided is not recognised";

    /** Message for the visibility range being negative */
    private static final String NEGATIVE_RANGE_MSG =
        "The fog visibility range is negative";

    /** Index of the title field */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 1;

    /** Index for the color field */
    protected static final int FIELD_COLOR = LAST_NODE_INDEX + 2;

    /** Index for the fogType field */
    protected static final int FIELD_FOGTYPE = LAST_NODE_INDEX + 3;

    /** Index for the visibilityRange field */
    protected static final int FIELD_VISIBILITY_RANGE = LAST_NODE_INDEX + 4;

    /** The last index value used in this node */
    public static final int LAST_FOG_INDEX = FIELD_VISIBILITY_RANGE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_FOG_INDEX + 1;

    /** Constant describing linear fog type */
    protected static final String LINEAR_TYPE = "LINEAR";

    /** Constant describing exponential fog type */
    protected static final String EXPONENTIAL_TYPE = "EXPONENTIAL";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Mapping between type string and type ints */
    private static HashMap typeMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** field SFFloat visibilityRange */
    protected float vfVisibilityRange;

    /** field SFString fogType */
    protected String vfFogType;

    /** field SFColor color */
    protected float[] vfColor;

    /** Constant representing the type used */
    protected int fogType;

    /** exposedField SFBool enabled */
    protected boolean vfEnabled;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "color");
        fieldDecl[FIELD_VISIBILITY_RANGE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "visibilityRange");
        fieldDecl[FIELD_FOGTYPE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "fogType");
        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = new Integer(FIELD_VISIBILITY_RANGE);
        fieldMap.put("visibilityRange", idx);
        fieldMap.put("set_visibilityRange", idx);
        fieldMap.put("visibilityRange_changed", idx);

        fieldMap.put("fogType", new Integer(FIELD_FOGTYPE));

        typeMap = new HashMap();
        typeMap.put(LINEAR_TYPE, new Integer(FOG_TYPE_LINEAR));
        typeMap.put(EXPONENTIAL_TYPE, new Integer(FOG_TYPE_EXPONENTIAL));
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseLocalFog() {
        super("LocalFog");

        hasChanged = new boolean[LAST_FOG_INDEX + 1];
        vfVisibilityRange = 0;
        vfFogType = LINEAR_TYPE;
        vfColor = new float[] {1, 1, 1};
        vfEnabled = true;

        fogType = FOG_TYPE_LINEAR;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseLocalFog(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("color");
            VRMLFieldData field = node.getFieldValue(index);

            vfColor[0] = field.floatArrayValue[0];
            vfColor[1] = field.floatArrayValue[1];
            vfColor[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("visibilityRange");
            field = node.getFieldValue(index);

            vfVisibilityRange = field.floatValue;

            index = node.getFieldIndex("fogType");
            field = node.getFieldValue(index);
            vfFogType = field.stringValue;

            index = node.getFieldIndex("enabled");
            field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }

    }

    //----------------------------------------------------------
    // Methods defined by VRMLFogNodeType
    //----------------------------------------------------------

    /**
     * Get the color of the current fog.
     *
     * @param col An array to copy the current color to
     */
    public void getColor(float[] col) {
        col[0] = vfColor[0];
        col[1] = vfColor[1];
        col[2] = vfColor[2];
    }

    /**
     * Set the color of the current fog. If the color values are out of range
     * or the array is invalid, an exception will be generated.
     *
     * @param color The new colors to set
     */
    public void setColor(float[] color)
        throws InvalidFieldValueException {

        FieldValidator.checkColorVector("LocalFog.color",color);
        vfColor[0] = color[0];
        vfColor[1] = color[1];
        vfColor[2] = color[2];

        if(!inSetup) {
            hasChanged[FIELD_COLOR] = true;
            fireFieldChanged(FIELD_COLOR);
        }
    }

    /**
     * Get the current visibility limit on the fog to be viewed. A value of
     * zero would disable the fog. It will always be a positive number.
     *
     * @return A non-negative number indicating the distance
     */
    public float getVisibilityRange() {
        return vfVisibilityRange;
    }

    /**
     * Set the visibility limit on the fog to be viewed to a new value. The
     * value of zero will disable the fog. A negative number will generate an
     * exception.
     *
     * @param range A non-negative number indicating the distance
     * @throws InvalidFieldValueException The number was negative
     */
    public void setVisibilityRange(float range)
        throws InvalidFieldValueException {

        if(range < 0)
            throw new InvalidFieldValueException(NEGATIVE_RANGE_MSG);

        vfVisibilityRange = range;

        if(!inSetup) {
            hasChanged[FIELD_VISIBILITY_RANGE] = true;
            fireFieldChanged(FIELD_VISIBILITY_RANGE);
        }
    }

    /**
     * Get the currently set fog type. Will be one of the above
     * constant values.
     *
     * @return One of FOG_TYPE_LINEAR or FOG_TYPE_EXPONENTIAL
     */
    public int getFogType() {
        return fogType;
    }

    /**
     * Set the fog type to one of the new values. If the value is not known,
     * issue an exception and leave the value at the current.
     *
     * @param type Constant indicating the type. Should be one of
     *   FOG_TYPE_LINEAR or FOG_TYPE_EXPONENTIAL
     * @throws InvalidFieldValueException The value type is unknown
     */
    public void setFogType(int type) throws InvalidFieldValueException {

        if(type < FOG_TYPE_DISABLE && type > FOG_TYPE_EXPONENTIAL)
            throw new InvalidFieldValueException(UNKNOWN_TYPE_MSG);

        fogType = type;

        if(!inSetup) {
            hasChanged[FIELD_FOGTYPE] = true;
            fireFieldChanged(FIELD_FOGTYPE);
        }
    }


    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Notify this node that is has been DEFd.
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
        if(!inSetup)
            throw new IllegalStateException("Setting DEF out of node setup");

        isDEF = true;
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
        if(index < 0  || index > LAST_FOG_INDEX)
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
        return TypeConstants.FogNodeType;
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
            case FIELD_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_VISIBILITY_RANGE:
                fieldData.clear();
                fieldData.floatValue = vfVisibilityRange;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_FOGTYPE:
                fieldData.clear();
                fieldData.stringValue = vfFogType;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
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
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_COLOR:
                    destNode.setValue(destIndex, vfColor, 3);
                    break;

                case FIELD_VISIBILITY_RANGE:
                    destNode.setValue(destIndex, vfVisibilityRange);
                    break;

                case FIELD_FOGTYPE:
                    destNode.setValue(destIndex, vfFogType);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("LocalFog sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("LocalFog sendRoute: Invalid field Value: " +
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
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_VISIBILITY_RANGE:
                setVisibilityRange(value);
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
            case FIELD_COLOR:
                setColor(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The fog type string is invalid
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FOGTYPE:
                setFogType(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the local fog type to a new value. Converts the string form to the
     * internal representation.
     *
     * @param type The type string indicating what needs to be set
     * @throws InvalidFieldValueException The fog type string is invalid
     */
    protected void setFogType(String type) throws InvalidFieldValueException {
        if(type.equals(LINEAR_TYPE) || type.equals(EXPONENTIAL_TYPE)) {
            vfFogType = type;
            Integer i_type = (Integer)typeMap.get(type);
            setFogType(i_type.intValue());
        } else
            throw new InvalidFieldValueException(UNKNOWN_TYPE_MSG);
    }

    /**
     * Set a new state for the enabled field.
     *
     * @param state True if this sensor is to be enabled
     */
    protected void setEnabled(boolean state) {
        if(state != vfEnabled) {
            vfEnabled = state;

            if(!inSetup) {
                hasChanged[FIELD_ENABLED] = true;
                fireFieldChanged(FIELD_ENABLED);
            }
        }
    }
}
