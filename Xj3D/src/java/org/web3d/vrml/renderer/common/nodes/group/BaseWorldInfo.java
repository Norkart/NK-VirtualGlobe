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

package org.web3d.vrml.renderer.common.nodes.group;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLChildNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a WorldInfo node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class BaseWorldInfo extends AbstractNode implements VRMLChildNodeType {

    /** Index of the title field */
    private static final int FIELD_TITLE = LAST_NODE_INDEX + 1;

    /** Index of the info field */
    private static final int FIELD_INFO = LAST_NODE_INDEX + 2;

    /** The last field index used by this class */
    private static final int LAST_WORLDINFO_INDEX = FIELD_INFO;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_WORLDINFO_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** exposedField MFString info */
    private String[] vfInfo;

    /** exposedField SFString title */
    private String vfTitle;

    /** The number of active items in the info field */
    private int infoLen;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_INFO] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "info");
        fieldDecl[FIELD_TITLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "title");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("info",new Integer(FIELD_INFO));
        fieldMap.put("title",new Integer(FIELD_TITLE));
    }

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public BaseWorldInfo() {
        super("WorldInfo");

        hasChanged = new boolean[NUM_FIELDS];
        vfTitle = "";
        vfInfo = null;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseWorldInfo(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("info");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfInfo = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfInfo,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("title");
            field = node.getFieldValue(index);
            vfTitle = field.stringValue;
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
        if(index < 0  || index > LAST_WORLDINFO_INDEX)
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
        return TypeConstants.InfoNodeType;
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
            case FIELD_INFO:
                fieldData.clear();
                fieldData.stringArrayValue = vfInfo;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = (vfInfo == null) ? 0 : vfInfo.length;
                break;

            case FIELD_TITLE:
                fieldData.clear();
                fieldData.stringValue = vfTitle;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field from the raw string. This requires the
     * implementation to parse the string in the given format for the field
     * type. If the field type does not match the requirements for that index
     * then an exception will be thrown. If the destination field is a string,
     * then the leading and trailing quote characters will be stripped before
     * calling this method.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     */
    public void setValue(int index, String value)
        throws InvalidFieldFormatException, InvalidFieldException {

        switch(index) {
            case FIELD_TITLE :
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "Cannot write to initializeOnly field: title");

                vfTitle = value;
                break;

            case FIELD_INFO:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "Cannot write to initializeOnly field: info");

                vfInfo = new String[] { value };
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field from the raw collection of strings. Like the
     * single value version above, this needs to process things into the
     * correct form. If this field represents an MFString field it will be
     * guaranteed to be one SFString per index - with leading and trailing
     * quotes already stripped.
     *
     * @param index The index of destination field to set
     * @param value The raw value strings to be parsed
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldFormatException, InvalidFieldException,
               InvalidFieldValueException {

        if(index != FIELD_INFO) {
            super.setValue(index, value, numValid);
            return;
        }

        if(!inSetup)
            throw new InvalidFieldAccessException("Cannot write to initializeOnly field");

        vfInfo = value;
    }
}
