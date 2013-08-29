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

package org.web3d.vrml.renderer.common.nodes.render;

// Standard imports
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLColorNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;

/**
 * Common base implementation of a color node.
 * <p>
 *
 * Points are held internally as a flat array of values. The point list
 * returned will always be flat. We do this because renderers like point values
 * as a single flat array. The array returned will always contain exactly the
 * number of points specified.
 * <p>
 * The effect of this is that point values may be routed out of this node as
 * a flat array of points rather than a 2D array. Receiving nodes should check
 * for this version as well. This implementation will handle being routed
 * either form.
 *
 * @author Alan Hudson
 * @version $Revision: 1.14 $
 */
public abstract class BaseColor extends BaseGeometricPropertyNode
    implements VRMLColorNodeType {

    /** Field Index */
    protected static final int FIELD_COLOR = LAST_NODE_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_COLOR_INDEX = FIELD_COLOR;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_COLOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    // Internally the arrays are kept as max of whatever was sent to us in
    // a setValue.  So a length variable is needed to know the "real" length
    // This will speed setValue calls.  It will cost on sendRoute

    /** exposedField MFColor */
    protected float[] vfColor;

    /** actual length of vfColor */
    protected int numColor;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFColor",
                                     "color");
        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_COLOR);
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);
    }

    /**
     * Empty constructor
     */
    protected BaseColor() {
        super("Color");

        vfColor = FieldConstants.EMPTY_MFCOLOR;
        hasChanged = new boolean[LAST_COLOR_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseColor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("color");
            VRMLFieldData field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfColor = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfColor,
                                 0,
                                 field.numElements * 3);

                numColor = field.numElements * 3;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLNormalNodeType interface.
    //-------------------------------------------------------------

    /**
     * Get the number of components defined for this texture type. SHould
     * be one of 1, 2, 3 or 4. Stock Color node returns 3 component colour
     * values.
     *
     * @return one of 1, 2, 3 or 4
     */
    public int getNumColorComponents() {
        return 3;
    }

    /**
     * Accessor method to set a new value for field attribute <b>color</b>
     *
     * @param newColor An array of 3 floats(r,g,b) specifying the new color
     * @throws ArrayIndexOutofBoundsException
     */
    public void setColor(float[] newColor, int numValid) {
        if(numValid > vfColor.length)
            vfColor = new float[numValid];

        System.arraycopy(newColor,0, vfColor, 0, numValid);
        numColor = numValid;

        // We have to send the new value here because it will be the
        // correct length.
        if(!inSetup) {
            fireComponentChanged(FIELD_COLOR);

            hasChanged[FIELD_COLOR] = true;
            fireFieldChanged(FIELD_COLOR);
        }
    }

    /**
     * Get the number of items in the color array now. The number returned is
     * the total number of values in the flat array. This will allow the caller
     * to construct the correct size array for the getColor() call.
     *
     * @return The number of values in the array
     */
    public int getNumColors() {
        return numColor;
    }

    /**
     * Get current value of field color. Color is an array of Color or ColorRGBA
     * floats. Don't call if there are no colors in the array.
     *
     * @param colors The array to copy the color values into
     */
    public void getColor(float[] colors) {
        if (vfColor != null)
            System.arraycopy(vfColor, 0, colors, 0, numColor);
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
        if (index < 0  || index > LAST_COLOR_INDEX)
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
        return TypeConstants.ColorNodeType;
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
            case FIELD_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numColor / 3;
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
                case FIELD_COLOR :
                    destNode.setValue(destIndex, vfColor, numColor);
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
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_COLOR :
                setColor(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
