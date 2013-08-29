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

package org.web3d.vrml.renderer.common.nodes.texture;

// Standard imports
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLTextureTransformNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * norender implementation of a texture transform.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public abstract class BaseTextureTransform extends AbstractNode
  implements VRMLTextureTransformNodeType {

    /** Field index for center */
    protected static final int FIELD_CENTER = LAST_NODE_INDEX + 1;

    /** Field index for rotation */
    protected static final int FIELD_ROTATION = LAST_NODE_INDEX + 2;

    /** Field index for scale */
    protected static final int FIELD_SCALE = LAST_NODE_INDEX + 3;

    /** Field index for translation */
    protected static final int FIELD_TRANSLATION = LAST_NODE_INDEX + 4;

    /** ID of the last field index in this class */
    protected static final int LAST_TEXTURETRANSFORM_INDEX = FIELD_TRANSLATION;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_TEXTURETRANSFORM_INDEX + 1;

    /* VRML Field declarations */
    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;



    /** exposedField SFVec2f center 0 0 */
    protected float[] vfCenter;

    /** exposedField SFFloat rotation 0 */
    protected float vfRotation;

    /** exposedField SFVec2f scale 1 1 */
    protected float[] vfScale;

    /** exposedField SFVec2f translation 0 0 */
    protected float[] vfTranslation;

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
        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "center");
        fieldDecl[FIELD_ROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "rotation");
        fieldDecl[FIELD_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "scale");
        fieldDecl[FIELD_TRANSLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "translation");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_SCALE);
        fieldMap.put("scale", idx);
        fieldMap.put("set_scale", idx);
        fieldMap.put("scale_changed", idx);

        idx = new Integer(FIELD_CENTER);
        fieldMap.put("center", idx);
        fieldMap.put("set_center", idx);
        fieldMap.put("center_changed", idx);

        idx = new Integer(FIELD_ROTATION);
        fieldMap.put("rotation", idx);
        fieldMap.put("set_rotation", idx);
        fieldMap.put("rotation_changed", idx);

        idx = new Integer(FIELD_TRANSLATION);
        fieldMap.put("translation", idx);
        fieldMap.put("set_translation", idx);
        fieldMap.put("translation_changed", idx);
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseTextureTransform() {
        super("TextureTransform");

        vfCenter = new float[] { 0, 0 };
        vfRotation = 0;
        vfScale = new float[] { 1, 1 };
        vfTranslation = new float[] { 0, 0 };

        hasChanged = new boolean[LAST_TEXTURETRANSFORM_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseTextureTransform(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);
            vfCenter[0] = field.floatArrayValue[0];
            vfCenter[1] = field.floatArrayValue[1];

            index = node.getFieldIndex("scale");
            field = node.getFieldValue(index);
            vfScale[0] = field.floatArrayValue[0];
            vfScale[1] = field.floatArrayValue[1];

            index = node.getFieldIndex("translation");
            field = node.getFieldValue(index);
            vfTranslation[0] = field.floatArrayValue[0];
            vfTranslation[1] = field.floatArrayValue[1];

            index = node.getFieldIndex("rotation");
            field = node.getFieldValue(index);
            vfRotation = field.floatValue;
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
        if (index < 0  || index > LAST_TEXTURETRANSFORM_INDEX) {
            return null;
        }

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
        return TypeConstants.TextureTransformNodeType;
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

        fieldData.clear();

        switch(index) {
            case FIELD_CENTER :
                fieldData.floatArrayValue = vfCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ROTATION:
                fieldData.floatValue = vfRotation;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SCALE:
                fieldData.floatArrayValue = vfScale;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_TRANSLATION:
                fieldData.floatArrayValue = vfTranslation;
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
                case FIELD_CENTER:
                    destNode.setValue(destIndex, vfCenter, 2);
                    break;

                case FIELD_ROTATION:
                    destNode.setValue(destIndex, vfRotation);
                    break;

                case FIELD_SCALE:
                    destNode.setValue(destIndex, vfScale, 2);
                    break;

                case FIELD_TRANSLATION:
                    destNode.setValue(destIndex, vfTranslation, 2);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("TextureTransform sendRoute: No field!" +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("TextureTransform sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ROTATION:
                vfRotation = value;
                if(!inSetup) {
                    hasChanged[FIELD_ROTATION] = true;
                    fireFieldChanged(FIELD_ROTATION);
                }
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
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTER:
                vfCenter[0] = value[0];
                vfCenter[1] = value[1];

                if(!inSetup) {
                    hasChanged[FIELD_CENTER] = true;
                    fireFieldChanged(FIELD_CENTER);
                }
                break;

            case FIELD_SCALE:
                vfScale[0] = value[0];
                vfScale[1] = value[1];

                if(!inSetup) {
                    hasChanged[FIELD_SCALE] = true;
                    fireFieldChanged(FIELD_SCALE);
                }
                break;

            case FIELD_TRANSLATION:
                vfTranslation[0] = value[0];
                vfTranslation[1] = value[1];

                if(!inSetup) {
                    hasChanged[FIELD_TRANSLATION] = true;
                    fireFieldChanged(FIELD_TRANSLATION);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }

    }
}
