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
import org.web3d.vrml.nodes.VRMLTexture3DNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseTexture3DNode;

/**
 * Common implementation of a 3D PixelTexture node.
 * <p>
 *
 * Given a MFImage this will produce a Texture3D object
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class BasePixelTexture3D extends BaseTexture3DNode {

    /** Field Index for the Image field */
    protected static final int FIELD_IMAGE = LAST_3DTEXTURE_INDEX + 1;

    /** Last valid index for this texture */
    private static final int LAST_PIXELTEXTURE_INDEX = FIELD_IMAGE;

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

    /** exposedField MFImage 0 0 0 */
    protected int[] vfImage;

    /** actual length of vfVector */
    protected int vfImageLen;


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
        fieldDecl[FIELD_REPEATS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatS");
        fieldDecl[FIELD_REPEATT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatT");
        fieldDecl[FIELD_REPEATR] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatR");
        fieldDecl[FIELD_IMAGE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFImage",
                                     "image");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_IMAGE);
        fieldMap.put("image", idx);
        fieldMap.put("set_image", idx);
        fieldMap.put("image_changed", idx);

        fieldMap.put("repeatS",new Integer(FIELD_REPEATS));
        fieldMap.put("repeatT",new Integer(FIELD_REPEATT));
        fieldMap.put("repeatR",new Integer(FIELD_REPEATR));
    }

    /**
     * Default constructor
     */
    protected BasePixelTexture3D() {
        super("PixelTexture3D");

        vfImage = FieldConstants.EMPTY_MFIMAGE;
        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BasePixelTexture3D(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLTexture3DNodeType)node);

        try {
            int index = node.getFieldIndex("image");
            VRMLFieldData field = node.getFieldValue(index);
            vfImage = new int[field.numElements];
            System.arraycopy(field.intArrayValue,
                             0,
                             vfImage,
                             0,
                             field.numElements);

            vfImageLen = field.numElements;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureNodeType interface.
    //----------------------------------------------------------

    /**
     * Get a string for cacheing this object.  Null means do not cache this
     * texture.
     *
     * @param stage The stage number,  0 for all single stage textures.
     * @return A string to use in lookups.  Typically the url loaded.
     */
    public String getCacheString(int stage) {
        return null;
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
            case FIELD_IMAGE:
                fieldData.clear();
                fieldData.intArrayValue = vfImage;
                fieldData.numElements = vfImageLen;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
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
                case FIELD_IMAGE :
                    destNode.setValue(destIndex, vfImage, vfImageLen);
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
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_IMAGE :
                if(numValid > vfImage.length)
                    vfImage = new int[numValid];

                System.arraycopy(value,0, vfImage, 0, numValid);
                vfImageLen = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_IMAGE] = true;
                    fireFieldChanged(FIELD_IMAGE);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
