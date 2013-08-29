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

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLAppearanceChildNodeType;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a FillProperties node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class BaseFillProperties extends AbstractNode
    implements VRMLAppearanceChildNodeType {

    /** Field index for hatchStyle */
    protected static final int FIELD_HATCHSTYLE = LAST_NODE_INDEX + 1;

    /** Field index for hatchColor */
    protected static final int FIELD_HATCHCOLOR = LAST_NODE_INDEX + 2;

    /** Field index for fillStyle */
    protected static final int FIELD_FILLSTYLE = LAST_NODE_INDEX + 3;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = FIELD_HATCHSTYLE + 1;

    /** The fill type "NONE" string */
    protected static final String FILL_TYPE_NONE = "NONE";

    /** The fill type "HATCHED" string */
    protected static final String FILL_TYPE_HATCH = "HATCHED";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFString fillStyle "NONE" */
    protected String vfFillStyle;

    /** exposedField SFInt32 hatchStyle 0 */
    protected int vfHatchStyle;

    /** exposedField SFColor hatchColor 0 0 0 */
    protected float[] vfHatchColor;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_HATCHSTYLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "hatchStyle");
        fieldDecl[FIELD_HATCHCOLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "hatchColor");
        fieldDecl[FIELD_FILLSTYLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "fillStyle");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_HATCHSTYLE);
        fieldMap.put("hatchStyle", idx);
        fieldMap.put("set_hatchStyle", idx);
        fieldMap.put("hatchStyle_changed", idx);

        idx = new Integer(FIELD_HATCHCOLOR);
        fieldMap.put("hatchColor", idx);
        fieldMap.put("set_hatchColor", idx);
        fieldMap.put("hatchColor_changed", idx);

        idx = new Integer(FIELD_FILLSTYLE);
        fieldMap.put("fillStyle", idx);
        fieldMap.put("set_fillStyle", idx);
        fieldMap.put("fillStyle_changed", idx);
    }

    /**
     * Construct a default instance of the material
     */
    protected BaseFillProperties() {
        super("FillProperties");

        hasChanged = new boolean[NUM_FIELDS];

        vfFillStyle = FILL_TYPE_NONE;
        vfHatchStyle = 0;
        vfHatchColor = new float[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseFillProperties(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("fillStyle");
            VRMLFieldData field = node.getFieldValue(index);
            vfFillStyle = field.stringValue;

            index = node.getFieldIndex("hatchStyle");
            field = node.getFieldValue(index);
            vfHatchStyle = field.intValue;

            index = node.getFieldIndex("hatchColor");
            field = node.getFieldValue(index);
            vfHatchColor[0] = field.floatArrayValue[0];
            vfHatchColor[1] = field.floatArrayValue[1];
            vfHatchColor[2] = field.floatArrayValue[2];
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
        if(index < 0  || index > NUM_FIELDS - 1)
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
        return TypeConstants.AppearanceChildNodeType;
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
            case FIELD_FILLSTYLE:
                fieldData.clear();
                fieldData.stringValue = vfFillStyle;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_HATCHSTYLE:
                fieldData.clear();
                fieldData.intValue = vfHatchStyle;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_HATCHCOLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfHatchColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
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
                case FIELD_FILLSTYLE:
                    destNode.setValue(destIndex, vfFillStyle);
                    break;

                case FIELD_HATCHSTYLE:
                    destNode.setValue(destIndex, vfHatchStyle);
                    break;

                case FIELD_HATCHCOLOR:
                    destNode.setValue(destIndex, vfHatchColor, 3);
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
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_HATCHSTYLE:
                setHatchStyle(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set MFFloat, SFColor field types.
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
            case FIELD_HATCHCOLOR:
                setHatchColor(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a String.
     * This would be used to set SFString field types.
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
            case FIELD_FILLSTYLE:
                setFillStyle(value);
                break;

            default :
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the fill style to the new value.
     *
     * @param style The new style string type
     */
    protected void setFillStyle(String style) {
        vfFillStyle = style;

        if(!inSetup) {
            hasChanged[FIELD_FILLSTYLE] = true;
            fireFieldChanged(FIELD_FILLSTYLE);
        }
    }

    /**
     * Set the hatch color to the new value.
     *
     * @param value The new color value to check
     * @throws InvalidFieldValueException One of the colour components are out
     *     of range
     */
    protected void setHatchColor(float[] value)
        throws InvalidFieldValueException {
        FieldValidator.checkColorVector("FillProperties.hatchColor",
                                        value);

        vfHatchColor[0] = value[0];
        vfHatchColor[1] = value[1];
        vfHatchColor[2] = value[2];

        if(!inSetup) {
            hasChanged[FIELD_HATCHCOLOR] = true;
            fireFieldChanged(FIELD_HATCHCOLOR);
        }
    }

    /**
     * Set the hatch style to the new value.
     *
     * @param value The style type value to check
     * @throws InvalidFieldValueException One of the colour components are out
     *     of range
     */
    protected void setHatchStyle(int value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException("The linestyle is < 0: " +
                                                 value);

        vfHatchStyle = value;

        if(!inSetup) {
            hasChanged[FIELD_HATCHSTYLE] = true;
            fireFieldChanged(FIELD_HATCHSTYLE);
        }
    }
}
