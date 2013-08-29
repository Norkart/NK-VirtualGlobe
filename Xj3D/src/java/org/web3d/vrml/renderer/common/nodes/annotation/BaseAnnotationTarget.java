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
 * A node that represents target for annotations.
 * <p>
 * The node definition is:
 * <pre>
 * AnnotationTarget : X3DChildNode {
 *   MFNode  [in,out] annotations    []       [X3DAnnotationNode]
 *   SFNode  [in,out] leadLineStyle  NULL     [X3DLinePropertiesNode]
 *   SFNode  [in,out] metadata       NULL     [X3DMetadataObject]
 *   SFVec3f [in,out] referencePoint 0, 0, 0  (-&#8734;,&#8734;)
 *   SFNode  [in,out] marker         NULL     [X3DShapeNode]
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BaseAnnotationTarget extends AbstractNode
    implements VRMLChildNodeType {

    /** Index of the annotations field */
    protected static final int FIELD_ANNOTATIONS = LAST_NODE_INDEX + 1;

    /** Index of the leadLineStyle field */
    protected static final int FIELD_LEAD_LINE_STYLE = LAST_NODE_INDEX + 2;

    /** Index of the leadLineStyle field */
    protected static final int FIELD_REFERENCE_POINT = LAST_NODE_INDEX + 3;

    /** Index of the leadLineStyle field */
    protected static final int FIELD_MARKER = LAST_NODE_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_ANNOTATION_INDEX = FIELD_MARKER;

    /** Message for when the proto is not a LineProperties */
    protected static final String LEAD_LINE_STYLE_PROTO_MSG =
        "Proto does not describe a LineProperties object";

    /** Message for when the node in setValue() is not a LineProperties */
    protected static final String LEAD_LINE_STYLE_NODE_MSG =
        "Node does not describe a LineProperties object";

    /** Message for when the proto is not a X3DShapeNode */
    protected static final String MARKER_PROTO_MSG =
        "Proto does not describe a X3DShapeNode object";

    /** Message for when the node in setValue() is not a X3DShapeNode */
    protected static final String MARKER_NODE_MSG =
        "Node does not describe a X3DShapeNode object";

    /** Message for when the proto is not a X3DAnnotationNode */
    protected static final String ANNOTATION_PROTO_MSG =
        "Proto does not describe a X3DAnnotationNode object";

    /** Message for when the node in setValue() is not a X3DAnnotationNode */
    protected static final String ANNOTATION_NODE_MSG =
        "Node does not describe a X3DAnnotationNode object";

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final HashMap<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // Note that this wants to be an appearance style, but we really need to
    // restrict it further to being a line properties node. There's no abstract
    // type for that yet in our system.

    /** inputOutput SFNode leadLineStyle NULL */
    protected VRMLAppearanceChildNodeType vfLeadLineStyle;

    /** Proto version of the leadLineStyle field */
    protected VRMLProtoInstance pLeadLineStyle;

    /** inputOutput SFNode field */
    protected VRMLShapeNodeType vfMarker;

    /** Proto version of the marker field */
    protected VRMLProtoInstance pMarker;

    /** inputOutput SFVec3f referencePoint 0, 0, 0 */
    protected float[] vfReferencePoint;

    /** inputOutput MFNode annotations */
    protected VRMLNodeType[] vfAnnotations;

    /** Temporary array to store the values in during node setup */
    protected ArrayList<VRMLNodeType> annotationsList;

    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_ANNOTATIONS,
            FIELD_LEAD_LINE_STYLE,
            FIELD_MARKER
        };

        fieldDecl = new VRMLFieldDeclaration[LAST_ANNOTATION_INDEX + 1];
        fieldMap = new HashMap(LAST_ANNOTATION_INDEX * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_LEAD_LINE_STYLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "leadLineStyle");
        fieldDecl[FIELD_MARKER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "marker");
        fieldDecl[FIELD_ANNOTATIONS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "annotations");
        fieldDecl[FIELD_REFERENCE_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "referencePoint");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ANNOTATIONS);
        fieldMap.put("annotations", idx);
        fieldMap.put("set_annotations", idx);
        fieldMap.put("annotations_changed", idx);

        idx = new Integer(FIELD_REFERENCE_POINT);
        fieldMap.put("referencePoint", idx);
        fieldMap.put("set_referencePoint", idx);
        fieldMap.put("referencePoint_changed", idx);

        idx = new Integer(FIELD_MARKER);
        fieldMap.put("marker", idx);
        fieldMap.put("set_marker", idx);
        fieldMap.put("marker_changed", idx);

        idx = new Integer(FIELD_LEAD_LINE_STYLE);
        fieldMap.put("leadLineStyle", idx);
        fieldMap.put("set_leadLineStyle", idx);
        fieldMap.put("leadLineStyle_changed", idx);
    }

    /**
     * Create a new, default instance of this class.
     */
    public BaseAnnotationTarget() {
        super("AnnotationTarget");

        hasChanged = new boolean[LAST_ANNOTATION_INDEX + 1];
        vfReferencePoint = new float[3];
        annotationsList = new ArrayList<VRMLNodeType>();
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
        if (index < 0  || index > LAST_ANNOTATION_INDEX)
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
        return TypeConstants.AnnotationTargetType;
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
            case FIELD_REFERENCE_POINT:
                fieldData.clear();
                fieldData.floatArrayValue = vfReferencePoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_LEAD_LINE_STYLE:
                fieldData.clear();
                if(pLeadLineStyle != null)
                    fieldData.nodeValue = pLeadLineStyle;
                else
                    fieldData.nodeValue = vfLeadLineStyle;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_MARKER:
                fieldData.clear();
                if(pMarker != null)
                    fieldData.nodeValue = pMarker;
                else
                    fieldData.nodeValue = vfMarker;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_ANNOTATIONS:
                fieldData.clear();
                fieldData.nodeArrayValue = vfAnnotations;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;

                if(vfAnnotations == null)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = vfAnnotations.length;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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

        if(annotationsList.size() != 0) {
            vfAnnotations = new VRMLNodeType[annotationsList.size()];

            for(int i = 0; i < annotationsList.size(); i++) {
                vfAnnotations[i] = (VRMLNodeType)annotationsList.get(i);
                vfAnnotations[i].setupFinished();
            }
        }

        annotationsList = null;
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
                case FIELD_REFERENCE_POINT:
                    destNode.setValue(destIndex, vfReferencePoint, 3);
                    break;

                case FIELD_MARKER:
                    if(pMarker != null)
                        destNode.setValue(destIndex, pMarker);
                    else
                        destNode.setValue(destIndex, vfMarker);
                    break;

                case FIELD_LEAD_LINE_STYLE:
                    if(pMarker != null)
                        destNode.setValue(destIndex, pMarker);
                    else
                        destNode.setValue(destIndex, vfMarker);
                    break;

                case FIELD_ANNOTATIONS:
                    destNode.setValue(destIndex,
                                      vfAnnotations,
                                      vfAnnotations.length);
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

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_MARKER:
                setMarker(node);
                break;

            case FIELD_LEAD_LINE_STYLE:
                setLeadLineStyle(node);
                break;

            case FIELD_ANNOTATIONS:
                if(child instanceof VRMLAnnotationNodeType) {
                    if(inSetup)
                        annotationsList.add(child);
                    else {
                        vfAnnotations = new VRMLNodeType[1];
                        vfAnnotations[0] = child;
                    }
                } else if(child instanceof VRMLProtoInstance) {
                    VRMLProtoInstance proto = (VRMLProtoInstance)child;
                    VRMLNodeType impl = proto.getImplementationNode();

                    // Walk down the proto impl looking for the real node to check it
                    // is the right type.
                    while((impl != null) && (impl instanceof VRMLProtoInstance))
                        impl = ((VRMLProtoInstance)impl).getImplementationNode();

                    if((impl != null) && !(impl instanceof VRMLAnnotationNodeType))
                        throw new InvalidFieldValueException(ANNOTATION_PROTO_MSG);

                    if(inSetup)
                        annotationsList.add(child);
                    else {
                        vfAnnotations= new VRMLNodeType[1];
                        vfAnnotations[0] = child;
                    }
                } else
                    throw new InvalidFieldValueException(ANNOTATION_NODE_MSG);

                break;

            default:
                super.setValue(index, child);
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
            case FIELD_ANNOTATIONS:
                // First check all new nodes for instance of the metadata type
                for(int i = 0; i < numValid; i++) {
                    if(value[i] instanceof VRMLAnnotationNodeType)
                        continue;

                    if(value[i] instanceof VRMLProtoInstance) {
                        VRMLProtoInstance proto = (VRMLProtoInstance)value[i];
                        VRMLNodeType impl = proto.getImplementationNode();

                        // Walk down the proto impl looking for the real node to check it
                        // is the right type.
                        while((impl != null) && (impl instanceof VRMLProtoInstance))
                            impl = ((VRMLProtoInstance)impl).getImplementationNode();

                        if((impl != null) && !(impl instanceof VRMLAnnotationNodeType))
                            throw new InvalidFieldValueException(ANNOTATION_PROTO_MSG);

                    } else
                        throw new InvalidFieldValueException(ANNOTATION_NODE_MSG);
                }

                if(numValid == 0)
                    vfAnnotations = null;
                else {
                    if((vfAnnotations == null) || (vfAnnotations.length != numValid))
                        vfAnnotations = new VRMLNodeType[numValid];

                    System.arraycopy(value, 0, vfAnnotations, 0, numValid);
                }

                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set node content as replacement for <code>marker</code>.
     *
     * @param marker The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setMarker(VRMLNodeType marker)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;

        if(pMarker != null)
            old_node = pMarker;
        else
            old_node = vfMarker;

        if(marker instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)marker).getImplementationNode();

                // Walk down the proto impl looking for the real node to check it
                // is the right type.
                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if((impl != null) && !(impl instanceof VRMLShapeNodeType))
                    throw new InvalidFieldValueException(MARKER_PROTO_MSG);

                pMarker = (VRMLProtoInstance)marker;
                vfMarker = (VRMLShapeNodeType)impl;

        } else if(marker != null && !(marker instanceof VRMLShapeNodeType)) {
            throw new InvalidFieldValueException(MARKER_NODE_MSG);
        } else {
            pMarker = null;
            vfMarker = (VRMLShapeNodeType)marker;
        }

        if(marker != null)
            updateRefs(marker, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(marker != null)
                stateManager.registerAddedNode(marker);

            hasChanged[FIELD_MARKER] = true;
            fireFieldChanged(FIELD_MARKER);
        }
    }

    /**
     * Set node content as replacement for <code>lineStyle</code>.
     *
     * @param lineStyle The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setLeadLineStyle(VRMLNodeType lineStyle)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;

        if(pLeadLineStyle != null)
            old_node = pLeadLineStyle;
        else
            old_node = vfLeadLineStyle;

        if(lineStyle instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)lineStyle).getImplementationNode();

                // Walk down the proto impl looking for the real node to check it
                // is the right type.
                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if((impl != null) && !(impl instanceof VRMLAppearanceChildNodeType))
                    throw new InvalidFieldValueException(LEAD_LINE_STYLE_PROTO_MSG);

                pLeadLineStyle = (VRMLProtoInstance)lineStyle;
                vfLeadLineStyle = (VRMLAppearanceChildNodeType)impl;

        } else if(lineStyle != null && !(lineStyle instanceof VRMLAppearanceChildNodeType)) {
            throw new InvalidFieldValueException(LEAD_LINE_STYLE_NODE_MSG);
        } else {
            pLeadLineStyle = null;
            vfLeadLineStyle = (VRMLAppearanceChildNodeType)lineStyle;
        }

        if(lineStyle != null)
            updateRefs(lineStyle, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(lineStyle != null)
                stateManager.registerAddedNode(lineStyle);

            hasChanged[FIELD_LEAD_LINE_STYLE] = true;
            fireFieldChanged(FIELD_LEAD_LINE_STYLE);
        }
    }
}
