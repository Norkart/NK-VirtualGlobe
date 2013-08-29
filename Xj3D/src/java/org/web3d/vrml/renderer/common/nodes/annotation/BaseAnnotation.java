/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.annotation;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;


/**
 * Common base class for all the AnnotationNodeType implementations.
 *
 * <p>
 * The base X3D node definition is:
 * <pre>
 * X3DAnnotationNode : X3DChildNode {
 *   SFString [in,out] annotationGroupID ""
 *   SFString [in,out] displayPolicy     "NEVER"  ["POINTER_OVER", "POINTER_ACTIVATE", "ALWAYS", "WHEN_VISIBLE", "NEVER"]
 *   SFBool   [in,out] enabled           TRUE
 *   SFNode   [in,out] metadata          NULL     [X3DMetadataObject]
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class BaseAnnotation extends AbstractNode
    implements VRMLAnnotationNodeType {

    /** Index of the annotations field */
    protected static final int FIELD_ANNOTATION_GROUP_ID = LAST_NODE_INDEX + 1;

    /** Index of the leadLineStyle field */
    protected static final int FIELD_DISPLAY_POLICY = LAST_NODE_INDEX + 2;

    /** Index of the leadLineStyle field */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 3;

    /** The last field index used by this class */
    protected static final int LAST_ANNOTATION_INDEX = FIELD_MARKER;

    /** Display Policy is NEVER */
    protected static final String POLICY_NEVER = "NEVER";

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final HashMap<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** inputOutput SFVec3f referencePoint 0, 0, 0 */
    protected String vfAnnotationGroupID;

    /** inputOutput MFNode annotations */
    protected String vfDisplayPolicy;

    /** Temporary array to store the values in during node setup */
    protected boolean vfEnabled;

    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[LAST_ANNOTATION_INDEX + 1];
        fieldMap = new HashMap(LAST_ANNOTATION_INDEX * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ANNOTATION_GROUP_ID] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "annotationGroupID");
        fieldDecl[FIELD_DISPLAY_POLICY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "displayPolicy");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_REFERENCE_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "referencePoint");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ANNOTATION_GROUP_ID);
        fieldMap.put("annotationGroupID", idx);
        fieldMap.put("set_annotationGroupID", idx);
        fieldMap.put("annotationGroupID_changed", idx);

        idx = new Integer(FIELD_DISPLAY_POLICY);
        fieldMap.put("displayPolicy", idx);
        fieldMap.put("set_displayPolicy", idx);
        fieldMap.put("displayPolicy_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);
    }

    /**
     * Create a new, default instance of this class.
     */
    protetect BaseAnnotation(String name) {
        super(name);

        hasChanged = new boolean[LAST_ANNOTATION_INDEX + 1];

        vfEnabled = true;
        vfAnnotationGroupID = "";
        vfDisplayPolicy = POLICY_NEVER;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseAnnotationTarget(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("referencePoint");
            VRMLFieldData field = node.getFieldValue(index);

            vfReferencePoint[0] = field.floatArrayValue[0];
            vfReferencePoint[1] = field.floatArrayValue[1];
            vfReferencePoint[2] = field.floatArrayValue[2];
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLAnnotationNodeType
    //----------------------------------------------------------

    /**
     * Get the current annotation group ID. If there is none set, this will
     * return an empty string.
     *
     * @return A string representing the group ID
     */
    public String getAnnotationGroupID() {
    }

    /**
     * Set the annotation group ID field to the new value. A null value or
     * zero length string will be treated as clearing the current value.
     *
     * @param id The new ID string to use
     */
    public void setAnnotationGroupID(String id) {
    }

    /**
     * Set a new enabled state for the annotation.
     *
     * @param newEnabled The new enabled value
     */
    public void setEnabled(boolean state) {
    }

    /**
     * Get current value of the enabled field. The default value is
     * <code>true</code>.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
    }

    /**
     * Get the policy for when this annotation should be displayed. The default
     * value is "NEVER".
     *
     * @return A string representing the current display policy
     */
    public String getDisplayPolicy() {
    }

    /**
     * Set the display policy field to the new value. The field must be a valid
     * string from the collection described in the class overview documentation.
     * Null values are treated as an error.
     *
     * @param id The new display policy to use
     */
    public void setDisplayPolicy(String policy)
        throws InvalidFieldValueException {
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.AnnotationType;
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
            case FIELD_DISPLAY_POLICY:
                fieldData.stringValue = vfDisplayPolicy;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_ENABLED:
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_ANNOTATION_GROUP_ID:
                fieldData.stringValue = vfAnnotations;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
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
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_DISPLAY_POLICY:
                    destNode.setValue(destIndex, vfDisplayPolicy);
                    break;

                case FIELD_ANNOTATION_GROUP_ID:
                    destNode.setValue(destIndex, vfAnnotationGroupID);
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
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat field type.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_REFERENCE_POINT:
                vfReferencePoint[0] = value[0];
                vfReferencePoint[1] = value[1];
                vfReferencePoint[2] = value[2];
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
}
