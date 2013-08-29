/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of an GeoMetadata node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class BaseGeoMetadata extends AbstractNode
    implements VRMLChildNodeType {

    /** Index of the data field */
    protected static final int FIELD_DATA = LAST_NODE_INDEX + 1;

    /** Index of the summary field */
    protected static final int FIELD_SUMMARY = LAST_NODE_INDEX + 2;

    /** Index of the url field */
    protected static final int FIELD_URL = LAST_NODE_INDEX + 3;

    /** The last index of the fields used by the metadata */
    protected static final int LAST_GEOMETADATA_INDEX = FIELD_URL;

    /** Number of fields constant */
    private static final int NUM_FIELDS=LAST_GEOMETADATA_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration fieldDecl[];

    /** Hashmap between a field name and its index */
    protected static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** field MFNode data */
    protected ArrayList vfData;

    /** field MFString summary */
    protected String[] vfSummary;

    /** field MFString url */
    protected String[] vfUrl;

    /**
     * Initialise all the field declaration values
     */
    static {
        nodeFields = new int[] { FIELD_DATA, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_DATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "data");

        fieldDecl[FIELD_SUMMARY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "summary");

        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "url");


        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_DATA);
        fieldMap.put("data",idx);
        fieldMap.put("set_data",idx);
        fieldMap.put("data_changed",idx);

        idx = new Integer(FIELD_SUMMARY);
        fieldMap.put("summary",idx);
        fieldMap.put("set_summary",idx);
        fieldMap.put("summary_changed",idx);

        idx = new Integer(FIELD_URL);
        fieldMap.put("url",idx);
        fieldMap.put("set_url",idx);
        fieldMap.put("url_changed",idx);
    }

    /**
     * Construct a default GeoMetaData instance
     */
    public BaseGeoMetadata() {
        super("GeoMetadata");

        hasChanged = new boolean[NUM_FIELDS];
        vfData = new ArrayList();
        vfUrl = FieldConstants.EMPTY_MFSTRING;
        vfSummary = FieldConstants.EMPTY_MFSTRING;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoMetadata(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("summary");
            VRMLFieldData field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfSummary = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfSummary, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("url");
            field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfUrl, 0,
                    field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        int len = vfData.size();
        VRMLNodeType node;

        for(int i=0; i < len; i++) {
            node = (VRMLNodeType) vfData.get(i);
            node.setupFinished();
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
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
        return (index < 0 || index > LAST_GEOMETADATA_INDEX) ?
            null : fieldDecl[index];
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
        return TypeConstants.ChildNodeType;
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
            case FIELD_SUMMARY:
                fieldData.clear();
                fieldData.stringArrayValue = vfSummary;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfSummary.length;
                break;

            case FIELD_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfUrl.length;
                break;

            case FIELD_DATA:
                VRMLNodeType kids[] = new VRMLNodeType[vfData.size()];
                vfData.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValue = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
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
                case FIELD_DATA:
                    VRMLNodeType kids[] = new VRMLNodeType[vfData.size()];
                    vfData.toArray(kids);
                    destNode.setValue(destIndex, kids, kids.length);
                    break;

                case FIELD_SUMMARY:
                    destNode.setValue(destIndex, vfSummary, vfSummary.length);
                    break;

                case FIELD_URL:
                    destNode.setValue(destIndex, vfUrl, vfUrl.length);
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
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SUMMARY:
                vfSummary = value;

                if (!inSetup) {
                    hasChanged[FIELD_SUMMARY] = true;
                    fireFieldChanged(FIELD_SUMMARY);
                }
                break;

            case FIELD_URL:
                vfUrl = value;

                if (!inSetup) {
                    hasChanged[FIELD_URL] = true;
                    fireFieldChanged(FIELD_URL);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DATA:
                if (!inSetup)
                    vfData.clear();

                vfData.add(child);

                if(!inSetup) {
                    hasChanged[FIELD_DATA] = true;
                    fireFieldChanged(FIELD_DATA);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DATA:
                if(!inSetup)
                    vfData.clear();

                for(int i=0; i < children.length; i++)
                    vfData.add(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_DATA] = true;
                    fireFieldChanged(FIELD_DATA);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }
}
