/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.texture;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a TextureProperties node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public abstract class BaseTextureProperties extends AbstractNode {

    /** Index of the boundaryColor field */
    protected static final int FIELD_BOUNDARY_COLOR = LAST_NODE_INDEX + 1;

    /** Index of the boundaryWidth field */
    protected static final int FIELD_BOUNDARY_WIDTH = LAST_NODE_INDEX + 2;

    /** Index of the boundaryModeS field */
    protected static final int FIELD_BOUNDARY_MODE_S = LAST_NODE_INDEX + 3;

    /** Index of the boundaryModeT field */
    protected static final int FIELD_BOUNDARY_MODE_T = LAST_NODE_INDEX + 4;

    /** Index of the magnificationFilter field */
    protected static final int FIELD_MAGNIFICATION_FILTER = LAST_NODE_INDEX + 5;

    /** Index of the minificationFilter field */
    protected static final int FIELD_MINIFICATION_FILTER = LAST_NODE_INDEX + 6;

    /** Index of the generateMipMaps field */
    protected static final int FIELD_GENERATE_MIPMAPS = LAST_NODE_INDEX + 7;

    /** Index of the anistropicMode field */
    protected static final int FIELD_ANISOTROPIC_MODE = LAST_NODE_INDEX + 8;

    /** Index of the anistropicMode field */
    protected static final int FIELD_ANISOTROPIC_FILTER_DEGREE = LAST_NODE_INDEX + 9;

    /** The last field index used by this class */
    protected static final int LAST_TEXTUREPROPS_INDEX =
            FIELD_ANISOTROPIC_FILTER_DEGREE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TEXTUREPROPS_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    // TODO: Needs to be an SFNode
    /** SFColorRGBA boundaryColor */
    protected float[] vfBoundaryColor;

    /** SFInt32 boundary width */
    protected int vfBoundaryWidth;

    /** SFString boundaryModeS */
    protected String vfBoundaryModeS;

    /** SFString boundaryModeT */
    protected String vfBoundaryModeT;

    /** SFString magnificationFilter */
    protected String vfMagnificationFilter;

    /** SFString magnificationFilter */
    protected String vfMinificationFilter;

    /** SFBool generateMipMaps */
    protected boolean vfGenerateMipMaps;

    /** SFString anistropicMode */
    protected String vfAnisotropicMode;

    /** SFFloat anistropicFilterDegree */
    protected float vfAnisotropicFilterDegree;

    /**
     * Static constructor to initialise all of the field values.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BOUNDARY_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFColorRGBA",
                                     "boundaryColor");

        fieldDecl[FIELD_BOUNDARY_WIDTH] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "boundaryWidth");

        fieldDecl[FIELD_BOUNDARY_MODE_S] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "boundaryModeS");

        fieldDecl[FIELD_BOUNDARY_MODE_T] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "boundaryModeT");

        fieldDecl[FIELD_MAGNIFICATION_FILTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "magnificationFilter");

        fieldDecl[FIELD_MINIFICATION_FILTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "minificationFilter");

        fieldDecl[FIELD_GENERATE_MIPMAPS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "generateMipMaps");

        fieldDecl[FIELD_ANISOTROPIC_MODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "anisotropicMode");

        fieldDecl[FIELD_ANISOTROPIC_FILTER_DEGREE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "anisotropicFilterDegree");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_BOUNDARY_COLOR);
        fieldMap.put("boundaryColor", idx);

        idx = new Integer(FIELD_BOUNDARY_WIDTH);
        fieldMap.put("boundaryWidth", idx);

        idx = new Integer(FIELD_BOUNDARY_MODE_S);
        fieldMap.put("boundaryModeS", idx);

        idx = new Integer(FIELD_BOUNDARY_MODE_T);
        fieldMap.put("boundaryModeT", idx);

        idx = new Integer(FIELD_MAGNIFICATION_FILTER);
        fieldMap.put("magnificationFilter", idx);

        idx = new Integer(FIELD_MINIFICATION_FILTER);
        fieldMap.put("minificationFilter", idx);

        idx = new Integer(FIELD_GENERATE_MIPMAPS);
        fieldMap.put("generateMipMaps", idx);

        idx = new Integer(FIELD_ANISOTROPIC_MODE);
        fieldMap.put("anisotropicMode", idx);

        idx = new Integer(FIELD_ANISOTROPIC_FILTER_DEGREE);
        fieldMap.put("anisotropicFilterDegree", idx);
    }

    /**
     * Construct a default node with all of the values set to the given types.
     */
    protected BaseTextureProperties() {
        super("TextureProperties");

        hasChanged = new boolean[NUM_FIELDS];

        vfBoundaryColor = new float[] { 0f, 0f, 0f, 0f };
        vfBoundaryWidth = 0;
        vfBoundaryModeS = "WRAP";
        vfBoundaryModeT = "WRAP";
        vfMagnificationFilter = "FASTEST";
        vfMinificationFilter = "FASTEST";
        vfGenerateMipMaps = false;
        vfAnisotropicMode = "NONE";
        vfAnisotropicFilterDegree = 1.0f;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseTextureProperties(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("boundaryColor");
            VRMLFieldData field = node.getFieldValue(index);

            vfBoundaryColor[0] = field.floatArrayValue[0];
            vfBoundaryColor[1] = field.floatArrayValue[1];
            vfBoundaryColor[2] = field.floatArrayValue[2];
            vfBoundaryColor[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("boundaryWidth");
            field = node.getFieldValue(index);
            vfBoundaryWidth = field.intValue;

            index = node.getFieldIndex("boundaryModeS");
            field = node.getFieldValue(index);
            vfBoundaryModeS = field.stringValue;

            index = node.getFieldIndex("boundaryModeT");
            field = node.getFieldValue(index);
            vfBoundaryModeT = field.stringValue;

            index = node.getFieldIndex("minificationFilter");
            field = node.getFieldValue(index);
            vfMinificationFilter = field.stringValue;

            index = node.getFieldIndex("maxificationFilter");
            field = node.getFieldValue(index);
            vfMinificationFilter = field.stringValue;

            index = node.getFieldIndex("generateMipMaps");
            field = node.getFieldValue(index);
            vfGenerateMipMaps = field.booleanValue;

            index = node.getFieldIndex("anisotropicMode");
            field = node.getFieldValue(index);
            vfAnisotropicMode = field.stringValue;

            index = node.getFieldIndex("anisotropicFilterDegree");
            field = node.getFieldValue(index);
            vfAnisotropicFilterDegree = field.floatValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        super.setupFinished();

        if(!inSetup)
            return;

        inSetup = false;
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_TEXTUREPROPS_INDEX)
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
            case FIELD_BOUNDARY_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfBoundaryColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BOUNDARY_WIDTH:
                fieldData.clear();
                fieldData.intValue = vfBoundaryWidth;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_MAGNIFICATION_FILTER:
                fieldData.clear();
                fieldData.stringValue = vfMagnificationFilter;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_MINIFICATION_FILTER:
                fieldData.clear();
                fieldData.stringValue = vfMinificationFilter;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_GENERATE_MIPMAPS:
                fieldData.clear();
                fieldData.booleanValue = vfGenerateMipMaps;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ANISOTROPIC_MODE:
                fieldData.clear();
                fieldData.stringValue = vfAnisotropicMode;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_ANISOTROPIC_FILTER_DEGREE:
                fieldData.clear();
                fieldData.floatValue = vfAnisotropicFilterDegree;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            default:
                return(super.getFieldValue(index));
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

        // Simple impl for now.  ignores time and looping. Note that for a
        // couple of the fields, if the array size is greater than the number
        // of components in it, we create a temporary array to send. This is
        // a negative hit, but it is very rare that someone will route out of
        // these fields, so we don't consider it to be a major impact compared
        // to the performance of having to reallocate the arrays every time
        // someone sets the values, which will happen much, much more often.

        try {
            switch(srcIndex) {
                case FIELD_BOUNDARY_COLOR:
                    destNode.setValue(destIndex, vfBoundaryColor, 4);
                    break;

                case FIELD_BOUNDARY_WIDTH:
                    destNode.setValue(destIndex, vfBoundaryWidth);
                    break;

                case FIELD_BOUNDARY_MODE_S:
                    destNode.setValue(destIndex, vfBoundaryModeS);
                    break;

                case FIELD_BOUNDARY_MODE_T:
                    destNode.setValue(destIndex, vfBoundaryModeT);
                    break;

                case FIELD_MAGNIFICATION_FILTER:
                    destNode.setValue(destIndex, vfMagnificationFilter);
                    break;

                case FIELD_MINIFICATION_FILTER:
                    destNode.setValue(destIndex, vfMinificationFilter);
                    break;

                case FIELD_GENERATE_MIPMAPS:
                    destNode.setValue(destIndex, vfGenerateMipMaps);
                    break;

                case FIELD_ANISOTROPIC_MODE:
                    destNode.setValue(destIndex, vfAnisotropicMode);
                    break;

                case FIELD_ANISOTROPIC_FILTER_DEGREE:
                    destNode.setValue(destIndex, vfAnisotropicFilterDegree);
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
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types headlight and bind.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GENERATE_MIPMAPS:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set generateMipMaps field.");

                vfGenerateMipMaps = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types speed and visibilityLimit.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ANISOTROPIC_FILTER_DEGREE:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set anistropicFilterDegree field.");

                vfAnisotropicFilterDegree = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BOUNDARY_WIDTH:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set boundaryWidth field.");

                vfBoundaryWidth = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat field type avatarSize.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BOUNDARY_COLOR:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set boundaryColor field.");

                vfBoundaryColor[0] = value[0];
                vfBoundaryColor[1] = value[1];
                vfBoundaryColor[2] = value[2];
                vfBoundaryColor[3] = value[3];
                break;

            default :
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string.
     * This would be used to set the SFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BOUNDARY_MODE_S:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set boundaryModeS field.");

                    vfBoundaryModeS = value;
                break;

            case FIELD_BOUNDARY_MODE_T:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set boundaryModeT field.");

                    vfBoundaryModeT = value;
                break;

            case FIELD_MAGNIFICATION_FILTER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set magnificationFilter field.");

                    vfMagnificationFilter = value;
                break;

            case FIELD_MINIFICATION_FILTER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set minificationFilter field.");

                    vfMinificationFilter = value;
                break;

            case FIELD_ANISOTROPIC_MODE:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set anistropicMode field.");

                    vfAnisotropicMode = value;
                break;

            default :
                super.setValue(index, value);
        }
    }
}
