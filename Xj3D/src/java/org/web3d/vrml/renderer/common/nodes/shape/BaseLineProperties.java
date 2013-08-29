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

/**
 * Common base implementation of a LineProperties node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public abstract class BaseLineProperties extends AbstractNode
    implements VRMLAppearanceChildNodeType {

    /** Field index for linewidthScaleFactor */
    protected static final int FIELD_LINEWIDTH_SCALE_FACTOR = LAST_NODE_INDEX + 1;

    /** Field index for linestyle */
    protected static final int FIELD_LINETYPE = LAST_NODE_INDEX + 2;

    /** Field index for applied */
    protected static final int FIELD_APPLIED = LAST_NODE_INDEX + 3;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = FIELD_APPLIED + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField SFInt32 linetype 0 */
    protected int vfLinetype;

    /** exposedField SFFloat linewidthScaleFactor 0 */
    protected float vfLinewidthScaleFactor;

    /** exposedField SFBool applied TRUE */
    protected boolean vfApplied;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_LINEWIDTH_SCALE_FACTOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "linewidthScaleFactor");
        fieldDecl[FIELD_LINETYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "linetype");

        fieldDecl[FIELD_APPLIED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "applied");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_LINEWIDTH_SCALE_FACTOR);
        fieldMap.put("linewidthScaleFactor", idx);
        fieldMap.put("set_linewidthScaleFactor", idx);
        fieldMap.put("linewidthScaleFactor_changed", idx);

        idx = new Integer(FIELD_LINETYPE);
        fieldMap.put("linetype", idx);
        fieldMap.put("set_linetype", idx);
        fieldMap.put("linetype_changed", idx);

        idx = new Integer(FIELD_APPLIED);
        fieldMap.put("applied", idx);
        fieldMap.put("set_applied", idx);
        fieldMap.put("applied_changed", idx);
    }

    /**
     * Construct a default instance of the material
     */
    protected BaseLineProperties() {
        super("LineProperties");

        hasChanged = new boolean[NUM_FIELDS];

        vfLinetype = 0;
        vfLinewidthScaleFactor = 0;
        vfApplied = true;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseLineProperties(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("linewidthScaleFactor");
            VRMLFieldData field = node.getFieldValue(index);
            vfLinewidthScaleFactor = field.floatValue;

            index = node.getFieldIndex("linetype");
            field = node.getFieldValue(index);
            vfLinetype = field.intValue;

            index = node.getFieldIndex("applied");
            field = node.getFieldValue(index);
            vfApplied = field.booleanValue;
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
            case FIELD_LINEWIDTH_SCALE_FACTOR:
                fieldData.clear();
                fieldData.floatValue = vfLinewidthScaleFactor;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_LINETYPE:
                fieldData.clear();
                fieldData.intValue = vfLinetype;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_APPLIED:
                fieldData.clear();
                fieldData.booleanValue = vfApplied;
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
                case FIELD_LINEWIDTH_SCALE_FACTOR:
                    destNode.setValue(destIndex, vfLinewidthScaleFactor);
                    break;

                case FIELD_LINETYPE:
                    destNode.setValue(destIndex, vfLinetype);
                    break;

                case FIELD_APPLIED:
                    destNode.setValue(destIndex, vfApplied);
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
            case FIELD_LINETYPE:
                setLineType(value);
                break;

            default:
                super.setValue(index, value);
        }
    }


    /**
     * Set the value of the field at the given index as a boolean.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_APPLIED:
                setApplied(value);
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
            case FIELD_LINEWIDTH_SCALE_FACTOR:
                setLineWidthScale(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the line width scale factor to the new value.
     *
     * @param value The scale value to check
     * @throws InvalidFieldValueException One of the colour components are out
     *     of range
     */
    protected void setLineWidthScale(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(
                "The linewidthScaleFactor is < 0: " + value);

        vfLinewidthScaleFactor = value;

        if(!inSetup) {
            hasChanged[FIELD_LINEWIDTH_SCALE_FACTOR] = true;
            fireFieldChanged(FIELD_LINEWIDTH_SCALE_FACTOR);
        }
    }

    /**
     * Set the line type.
     *
     * @param value The new line type
     * @throws InvalidFieldValueException Unknown line type
     *
     */
    protected void setLineType(int value)
        throws InvalidFieldValueException {

        if(value < 1)
            throw new InvalidFieldValueException(
                "The linestyle is < 1: " + value);

        vfLinetype = value;

        if(!inSetup) {
            hasChanged[FIELD_LINETYPE] = true;
            fireFieldChanged(FIELD_LINETYPE);
        }
    }

    /**
     * Set the applied field..
     *
     * @param value The new value
     */
    protected void setApplied(boolean value)
        throws InvalidFieldValueException {

        vfApplied = value;

        if(!inSetup) {
            hasChanged[FIELD_APPLIED] = true;
            fireFieldChanged(FIELD_APPLIED);
        }
    }

}
