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

package org.web3d.vrml.renderer.common.nodes.core;

// Standard imports
import java.util.ArrayList;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.renderer.common.nodes.BaseMetadataObjectNode;


/**
 * A node that represents a set of metadata nodes.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class BaseMetadataSet extends BaseMetadataObjectNode {

    /** Index of the url field */
    protected static final int FIELD_VALUE = LAST_METADATA_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_SET_INDEX = FIELD_VALUE;

    /** Message for when the proto is not a MetadataObject */
    protected static final String DATA_PROTO_MSG =
        "Proto does not describe a MetadataObject object";

    /** Message for when the node in setValue() is not a MetadataObject */
    protected static final String DATA_NODE_MSG =
        "Node does not describe a MetadataObject object";


    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** exposedField MFNode value  */
    protected VRMLNodeType[] vfValue;

    /** Temporary array to store the values in during node setup */
    private ArrayList valueList;

    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA, FIELD_VALUE };

        fieldDecl = new VRMLFieldDeclaration[LAST_SET_INDEX + 1];
        fieldMap = new HashMap(LAST_SET_INDEX * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "name");
        fieldDecl[FIELD_REFERENCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "reference");
        fieldDecl[FIELD_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "value");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);
        fieldMap.put("set_name", idx);
        fieldMap.put("name_changed", idx);

        idx = new Integer(FIELD_REFERENCE);
        fieldMap.put("reference", idx);
        fieldMap.put("set_reference", idx);
        fieldMap.put("reference_changed", idx);

        idx = new Integer(FIELD_VALUE);
        fieldMap.put("value", idx);
        fieldMap.put("set_value", idx);
        fieldMap.put("value_changed", idx);
    }

    /**
     * Create a new, default instance of this class.
     */
    public BaseMetadataSet() {
        super("MetadataSet");

        hasChanged = new boolean[LAST_SET_INDEX + 1];
        valueList = new ArrayList();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseMetadataSet(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLMetadataObjectNodeType)node);

        // Child nodes are copied elsewhere.
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
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(valueList.size() != 0) {
            vfValue = new VRMLNodeType[valueList.size()];

            for(int i = 0; i < valueList.size(); i++) {
                vfValue[i] = (VRMLNodeType)valueList.get(i);
                vfValue[i].setupFinished();
            }
        }

        valueList = null;
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
        if (index < 0  || index > LAST_SET_INDEX)
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
            case FIELD_VALUE:
                fieldData.clear();
                fieldData.nodeArrayValue = vfValue;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;

                if (vfValue == null)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = vfValue.length;
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
                case FIELD_VALUE:
                    destNode.setValue(destIndex, vfValue, vfValue.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a single node.
     * This would be used to set SFNode field type url.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, VRMLNodeType value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_VALUE:
                if(value instanceof VRMLMetadataObjectNodeType) {
                    if(inSetup)
                        valueList.add(value);
                    else {
                        vfValue = new VRMLNodeType[1];
                        vfValue[0] = value;
                    }
                } else if(value instanceof VRMLProtoInstance) {
                    VRMLProtoInstance proto = (VRMLProtoInstance)value;
                    VRMLNodeType impl = proto.getImplementationNode();

                    // Walk down the proto impl looking for the real node to check it
                    // is the right type.
                    while((impl != null) && (impl instanceof VRMLProtoInstance))
                        impl = ((VRMLProtoInstance)impl).getImplementationNode();

                    if((impl != null) && !(impl instanceof VRMLMetadataObjectNodeType))
                        throw new InvalidFieldValueException(DATA_PROTO_MSG);

                    if(inSetup)
                        valueList.add(value);
                    else {
                        vfValue = new VRMLNodeType[1];
                        vfValue[0] = value;
                    }
                } else
                    throw new InvalidFieldValueException(DATA_NODE_MSG);

                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field type url.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, VRMLNodeType[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_VALUE:
                // First check all new nodes for instance of the metadata type
                for(int i = 0; i < numValid; i++) {
                    if(value[i] instanceof VRMLMetadataObjectNodeType)
                        continue;

                    if(value[i] instanceof VRMLProtoInstance) {
                        VRMLProtoInstance proto = (VRMLProtoInstance)value[i];
                        VRMLNodeType impl = proto.getImplementationNode();

                        // Walk down the proto impl looking for the real node to check it
                        // is the right type.
                        while((impl != null) && (impl instanceof VRMLProtoInstance))
                            impl = ((VRMLProtoInstance)impl).getImplementationNode();

                        if((impl != null) && !(impl instanceof VRMLMetadataObjectNodeType))
                            throw new InvalidFieldValueException(DATA_PROTO_MSG);

                    } else
                        throw new InvalidFieldValueException(DATA_NODE_MSG);
                }

                if(numValid == 0)
                    vfValue = null;
                else {
                    if((vfValue == null) || (vfValue.length != numValid))
                        vfValue = new VRMLNodeType[numValid];

                    System.arraycopy(value, 0, vfValue, 0, numValid);
                }

                break;

            default :
                super.setValue(index, value, numValid);
        }
    }
}
