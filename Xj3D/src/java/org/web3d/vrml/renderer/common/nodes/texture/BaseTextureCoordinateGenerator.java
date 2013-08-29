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

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLTextureCoordinateNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;
/**
 * Common base implementation of a texture coordinate generator node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public abstract class BaseTextureCoordinateGenerator extends BaseGeometricPropertyNode
    implements VRMLTextureCoordinateNodeType {

    /** Field index for mode */
    protected static final int FIELD_MODE = LAST_NODE_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_TEXTURECOORDINATE_INDEX = FIELD_MODE;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_TEXTURECOORDINATE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFString mode "REPLACE" */
    protected String vfMode;

    //----------------------------------------------------------
    // Methods internal to NRTextureCoordinate
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
        fieldDecl[FIELD_MODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "mode");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("mode",new Integer(FIELD_MODE));
    }

    /**
     * Empty constructor
     */
    protected BaseTextureCoordinateGenerator() {
        super("TextureCoordinateGenerator");

        hasChanged = new boolean[LAST_TEXTURECOORDINATE_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseTextureCoordinateGenerator(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("mode");
            VRMLFieldData field = node.getFieldValue(index);
            vfMode = field.stringValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-----------------------------------------------------------------
    // Methods required by the VRMLTextureCoordinateNodeType interface.
    //-----------------------------------------------------------------

    /**
     * Get the number of components defined for this texture type. SHould
     * be one of 2, 3 or 4 for 2D, 3D or time-driven textures.
     *
     * @return one of 2, 3 or 4
     */
    public int getNumTextureComponents() {
        return 2;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 1;
    }

    /**
     * Get the size of the specified set
     *
     * @param setNum The set to size
     */
    public int getSize(int setNum) {
        return 0;
    }

    /**
     * Accessor method to set a new value for field attribute point.  Attempts
     * to set nodes > numSets will throw an exception.
     *
     * @param setNum The set which this point belongs.
     * @param newPoint New value for the point field
     * @param numValid number of valid items to use from the size array
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setPoint(int setNum, float[] newPoint, int numValid) {
        // Ignore
    }

    /**
     * Accessor method to get current value of field point.  Sets outside
     * the numSize will throw an exception.
     *
     * @return The current value of point
     * @throws ArrayIndexOutOfBoundsException
     */
    public void getPoint(int setNum, float[] point) {
        // ignore
    }

    /**
     * Determine if this index is shared via DEF/USE inside this set
     *
     * @param index The index to check
     * @return The index if not shared or the original index DEFed
     */
    public int isShared(int index) {
        return index;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return vfMode;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

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
        if (index < 0  || index > LAST_TEXTURECOORDINATE_INDEX)
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
        return TypeConstants.TextureCoordinateNodeType;
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
            case FIELD_MODE:
                fieldData.clear();
                fieldData.stringValue = vfMode;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
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
                case FIELD_MODE :
                    destNode.setValue(destIndex, vfMode);
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
     * Set the value of the field as a string.
     *
     * @param index The index of destination field to set
     * @param value The value string to be used
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     */
    public void setValue(int index, String value)
        throws InvalidFieldFormatException {

        switch(index) {
            case FIELD_MODE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "mode is initializeOnly");

                vfMode = value;
                break;

            default:
                super.setValue(index, value);
        }
    }
}
