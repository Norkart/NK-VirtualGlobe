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

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLEnvironmentTextureNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseTextureNode;

/**
 * Common implementation of a PixelCubeMapTexture node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class BasePixelCubeMapTexture extends BaseTextureNode
    implements VRMLEnvironmentTextureNodeType {

    /** front texture children Index */
    protected static final int FIELD_FRONT = LAST_NODE_INDEX + 1;

    /** back texture children Index */
    protected static final int FIELD_BACK = LAST_NODE_INDEX + 2;

    /** left texture children Index */
    protected static final int FIELD_LEFT = LAST_NODE_INDEX + 3;

    /** right texture children Index */
    protected static final int FIELD_RIGHT = LAST_NODE_INDEX + 4;

    /** top texture children Index */
    protected static final int FIELD_TOP = LAST_NODE_INDEX + 5;

    /** bottom texture children Index */
    protected static final int FIELD_BOTTOM = LAST_NODE_INDEX + 6;

    /** Last valid index for this texture */
    private static final int LAST_PIXELTEXTURE_INDEX = FIELD_BOTTOM;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_PIXELTEXTURE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /* Internally the arrays are kept as max of whatever was sent to us in
     * a setValue.  So a length variable is needed to know the "real" length
     * This will speed setValue calls.  It will cost on sendRoute
     */

    /** exposedField SFImage 0 0 0 */
    protected int[] vfFront;

    /** actual length of vfVector */
    protected int vfFrontLen;

    /** exposedField SFImage 0 0 0 */
    protected int[] vfBack;

    /** actual length of vfVector */
    protected int vfBackLen;

    /** exposedField SFImage 0 0 0 */
    protected int[] vfLeft;

    /** actual length of vfVector */
    protected int vfLeftLen;

    /** exposedField SFImage 0 0 0 */
    protected int[] vfRight;

    /** actual length of vfVector */
    protected int vfRightLen;

    /** exposedField SFImage 0 0 0 */
    protected int[] vfTop;

    /** actual length of vfVector */
    protected int vfTopLen;

    /** exposedField SFImage 0 0 0 */
    protected int[] vfBottom;

    /** actual length of vfVector */
    protected int vfBottomLen;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_FRONT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFImage",
                                     "front");
        fieldDecl[FIELD_BACK] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFImage",
                                     "back");
        fieldDecl[FIELD_LEFT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFImage",
                                     "left");
        fieldDecl[FIELD_RIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFImage",
                                     "right");
        fieldDecl[FIELD_TOP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFImage",
                                     "top");
        fieldDecl[FIELD_BOTTOM] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFImage",
                                     "bottom");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_FRONT);
        fieldMap.put("front", idx);
        fieldMap.put("set_front", idx);
        fieldMap.put("front_changed", idx);

        idx = new Integer(FIELD_BACK);
        fieldMap.put("back", idx);
        fieldMap.put("set_back", idx);
        fieldMap.put("back_changed", idx);

        idx = new Integer(FIELD_LEFT);
        fieldMap.put("left", idx);
        fieldMap.put("set_left", idx);
        fieldMap.put("left_changed", idx);

        idx = new Integer(FIELD_BOTTOM);
        fieldMap.put("bottom", idx);
        fieldMap.put("set_bottom", idx);
        fieldMap.put("bottom_changed", idx);

        idx = new Integer(FIELD_RIGHT);
        fieldMap.put("right", idx);
        fieldMap.put("set_right", idx);
        fieldMap.put("right_changed", idx);

        idx = new Integer(FIELD_TOP);
        fieldMap.put("top", idx);
        fieldMap.put("set_top", idx);
        fieldMap.put("top_changed", idx);
    }

    /**
     * Construct a default instance of this node.
     */
    public BasePixelCubeMapTexture() {
        super("PixelTexture");

        hasChanged = new boolean[NUM_FIELDS];
        vfFront = FieldConstants.EMPTY_SFIMAGE;
        vfBack = FieldConstants.EMPTY_SFIMAGE;
        vfLeft = FieldConstants.EMPTY_SFIMAGE;
        vfRight = FieldConstants.EMPTY_SFIMAGE;
        vfTop = FieldConstants.EMPTY_SFIMAGE;
        vfBottom = FieldConstants.EMPTY_SFIMAGE;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BasePixelCubeMapTexture(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("front");
            VRMLFieldData field = node.getFieldValue(index);

            int size = field.intArrayValue[1] * field.intArrayValue[2];
            vfFront = new int[size];
            System.arraycopy(field.intArrayValue, 0, vfFront, 0, size);
            vfFrontLen = size;

            index = node.getFieldIndex("back");
            field = node.getFieldValue(index);
            size = field.intArrayValue[1] * field.intArrayValue[2];
            vfBack = new int[size];
            System.arraycopy(field.intArrayValue, 0, vfBack, 0, size);
            vfBackLen = size;

            index = node.getFieldIndex("left");
            field = node.getFieldValue(index);
            size = field.intArrayValue[1] * field.intArrayValue[2];
            vfLeft = new int[size];
            System.arraycopy(field.intArrayValue, 0, vfLeft, 0, size);
            vfLeftLen = size;

            index = node.getFieldIndex("right");
            field = node.getFieldValue(index);
            size = field.intArrayValue[1] * field.intArrayValue[2];
            vfRight = new int[size];
            System.arraycopy(field.intArrayValue, 0, vfRight, 0, size);
            vfRightLen = size;

            index = node.getFieldIndex("top");
            field = node.getFieldValue(index);
            size = field.intArrayValue[1] * field.intArrayValue[2];
            vfTop = new int[size];
            System.arraycopy(field.intArrayValue, 0, vfTop, 0, size);
            vfTopLen = size;

            index = node.getFieldIndex("bottom");
            field = node.getFieldValue(index);
            size = field.intArrayValue[1] * field.intArrayValue[2];
            vfBottom = new int[size];
            System.arraycopy(field.intArrayValue, 0, vfBottom, 0, size);
            vfBottomLen = size;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the texture type of this texture.  Valid entries are defined
     * in the vrml.lang.TextureConstants.
     */
    public int getTextureType() {
        return TextureConstants.TYPE_MULTI;
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_PIXELTEXTURE_INDEX)
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
        return TypeConstants.TextureNodeType;
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
            case FIELD_FRONT:
                fieldData.clear();
                fieldData.intArrayValue = vfFront;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = vfFrontLen;
                break;

            case FIELD_BACK:
                fieldData.clear();
                fieldData.intArrayValue = vfBack;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = vfBackLen;
                break;

            case FIELD_LEFT:
                fieldData.clear();
                fieldData.intArrayValue = vfLeft;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = vfLeftLen;
                break;

            case FIELD_RIGHT:
                fieldData.clear();
                fieldData.intArrayValue = vfRight;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = vfRightLen;
                break;

            case FIELD_TOP:
                fieldData.clear();
                fieldData.intArrayValue = vfTop;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = vfTopLen;
                break;

            case FIELD_BOTTOM:
                fieldData.clear();
                fieldData.intArrayValue = vfBottom;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = vfBottomLen;
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
                case FIELD_FRONT:
                    destNode.setValue(destIndex, vfFront, 1);
                    break;

                case FIELD_BACK:
                    destNode.setValue(destIndex, vfBack, 1);
                    break;

                case FIELD_LEFT:
                    destNode.setValue(destIndex, vfLeft, 1);
                    break;

                case FIELD_RIGHT:
                    destNode.setValue(destIndex, vfRight, 1);
                    break;

                case FIELD_TOP:
                    destNode.setValue(destIndex, vfTop, 1);
                    break;

                case FIELD_BOTTOM:
                    destNode.setValue(destIndex, vfBottom, 1);
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
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_FRONT:
                int size = value[1] * value[2];

                if(size > vfFront.length)
                    vfFront = new int[size];

                System.arraycopy(value, 0, vfFront, 0, size);
                vfFrontLen = size;
                processImageData(FIELD_FRONT);

                if(!inSetup) {
                    hasChanged[FIELD_FRONT] = true;
                    fireFieldChanged(FIELD_FRONT);
                }
                break;

            case FIELD_BACK:
                size = value[1] * value[2];

                if(size > vfBack.length)
                    vfBack = new int[size];

                System.arraycopy(value, 0, vfBack, 0, size);
                vfBackLen = size;
                processImageData(FIELD_BACK);

                if(!inSetup) {
                    hasChanged[FIELD_BACK] = true;
                    fireFieldChanged(FIELD_BACK);
                }
                break;

            case FIELD_LEFT:
                size = value[1] * value[2];

                if(size > vfLeft.length)
                    vfLeft = new int[size];

                System.arraycopy(value, 0, vfLeft, 0, size);
                vfLeftLen = size;
                processImageData(FIELD_LEFT);

                if(!inSetup) {
                    hasChanged[FIELD_LEFT] = true;
                    fireFieldChanged(FIELD_LEFT);
                }
                break;

            case FIELD_RIGHT:
                size = value[1] * value[2];

                if(size > vfRight.length)
                    vfRight = new int[size];

                System.arraycopy(value, 0, vfRight, 0, size);
                vfRightLen = size;
                processImageData(FIELD_RIGHT);

                if(!inSetup) {
                    hasChanged[FIELD_RIGHT] = true;
                    fireFieldChanged(FIELD_RIGHT);
                }
                break;

            case FIELD_TOP:
                size = value[1] * value[2];

                if(size > vfTop.length)
                    vfTop = new int[size];

                System.arraycopy(value, 0, vfTop, 0, size);
                vfTopLen = size;
                processImageData(FIELD_TOP);

                if(!inSetup) {
                    hasChanged[FIELD_TOP] = true;
                    fireFieldChanged(FIELD_TOP);
                }
                break;

            case FIELD_BOTTOM:
                size = value[1] * value[2];

                if(size > vfBottom.length)
                    vfBottom = new int[size];

                System.arraycopy(value, 0, vfBottom, 0, size);
                vfBottomLen = size;
                processImageData(FIELD_BOTTOM);

                if(!inSetup) {
                    hasChanged[FIELD_BOTTOM] = true;
                    fireFieldChanged(FIELD_BOTTOM);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * The image data field has been updated, so time to process the image
     * right now. The field notifications have not been sent yet. If
     * overridden, This method should be called before the overridden code
     * is run.
     *
     * @param field The index of one of the six fields for the sides
     */
    protected abstract void processImageData(int field);
}
