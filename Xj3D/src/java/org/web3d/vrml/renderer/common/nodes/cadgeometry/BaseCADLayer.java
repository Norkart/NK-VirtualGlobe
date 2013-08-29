/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.cadgeometry;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;

/**
 * Common base implementation of a CADLayer node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public class BaseCADLayer extends BaseGroupingNode {

    /** Index for the field: name */
    protected static final int FIELD_NAME = LAST_GROUP_INDEX + 1;

    /** Index for the field: visible */
    protected static final int FIELD_VISIBLE = LAST_GROUP_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_CADLAYER_INDEX = FIELD_VISIBLE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = FIELD_VISIBLE + 1;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** The name field */
    protected String vfName;

    /** The visible field */
    protected boolean[] vfVisible;

    /** Number of valid values in vfVisible */
    protected int numVisible;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFNode",
                                 "children");
        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "addChildren");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "MFNode",
                                 "removeChildren");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                  "SFVec3f",
                                 "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFVec3f",
                                 "bboxSize");

        fieldDecl[FIELD_VISIBLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFBool",
                                     "visible");

        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "name");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_ADDCHILDREN);
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = new Integer(FIELD_REMOVECHILDREN);
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);
        fieldMap.put("set_name", idx);
        fieldMap.put("name_changed", idx);

        idx = new Integer(FIELD_VISIBLE);
        fieldMap.put("visible", idx);
        fieldMap.put("set_visible", idx);
        fieldMap.put("visible_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseCADLayer() {
        super("CADLayer");

        vfVisible = FieldConstants.EMPTY_MFBOOL;
        hasChanged = new boolean[LAST_CADLAYER_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseCADLayer(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {

            int index = node.getFieldIndex("visible");
            VRMLFieldData field = node.getFieldValue(index);

            vfVisible = new boolean[field.numElements];
            System.arraycopy(field.booleanArrayValue,
                             0,
                             vfVisible,
                             0,
                             field.numElements);
            numVisible = field.numElements;

            index = node.getFieldIndex("name");
            field = node.getFieldValue(index);
            vfName = field.stringValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods overriding VRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.CADLayerNodeType;
    }

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
        if(index < 0  || index > LAST_CADLAYER_INDEX)
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
            case FIELD_NAME:
                fieldData.clear();
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.stringValue = vfName;
                break;

            case FIELD_VISIBLE:
                fieldData.clear();
                fieldData.numElements = numVisible;
                fieldData.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
                fieldData.booleanArrayValue = vfVisible;
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
                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
                    break;

                case FIELD_VISIBLE:
                    destNode.setValue(destIndex, vfVisible, numVisible);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid fieldValue: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of booleans.
     * This would be used to set MFBoolean
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_VISIBLE:
                setVisible(value, numValid);
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
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_NAME:
                vfName = value;
                if(!inSetup) {
                    hasChanged[FIELD_NAME] = true;
                    fireFieldChanged(FIELD_NAME);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }


    /**
     * Set the visible field.  Override for renderer specific impl, but
     * call super.
     *
     * @param value The new value.
     */
    protected void setVisible(boolean[] value, int numValid) {
        if(numValid > vfVisible.length)
            vfVisible = new boolean[value.length];


        System.arraycopy(value, 0, vfVisible, 0, numValid);
        numVisible = numValid;

        if(!inSetup) {
            hasChanged[FIELD_VISIBLE] = true;
            fireFieldChanged(FIELD_VISIBLE);
        }
    }
}
