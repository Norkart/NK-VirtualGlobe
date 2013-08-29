/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a CADFace node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class BaseCADFace extends AbstractNode
    implements VRMLProductStructureChildNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {};

    /** Field Index */
    protected static final int FIELD_NAME = LAST_NODE_INDEX + 1;
    protected static final int FIELD_SHAPE = LAST_NODE_INDEX + 2;
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 3;
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_CADFACE_INDEX = FIELD_BBOX_SIZE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_CADFACE_INDEX + 1;

    /** Message for when the proto is not a Shape */
    protected static final String SHAPE_PROTO_MSG =
        "Proto does not describe a Shape object";

    /** Message for when the node in setValue() is not a Shape */
    protected static final String SHAPE_NODE_MSG =
        "Node does not describe a Shape object";


    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** The name field */
    protected String vfName;

    /** The shape field */
    protected VRMLNodeType vfShape;

    /** Proto version of the Shape */
    protected VRMLProtoInstance pShape;

    /** SFVec3f bboxCenter NULL */
    protected float[] vfBboxCenter;

    /** SFVec3f bboxSize NULL */
    protected float[] vfBboxSize;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_SHAPE, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_SHAPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFNode",
                                 "shape");
        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "name");

        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                  "SFVec3f",
                                 "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFVec3f",
                                 "bboxSize");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_SHAPE);
        fieldMap.put("shape", idx);
        fieldMap.put("set_shape", idx);
        fieldMap.put("shape_changed", idx);

        idx = new Integer(FIELD_NAME);
        fieldMap.put("name", idx);
        fieldMap.put("set_name", idx);
        fieldMap.put("name_changed", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseCADFace() {
        super("CADFace");

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};

        hasChanged = new boolean[LAST_CADFACE_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseCADFace(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {

            int index = node.getFieldIndex("name");
            VRMLFieldData field = node.getFieldValue(index);

            field = node.getFieldValue(index);
            vfName = field.stringValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLProductStructureChildNodeType
    //----------------------------------------------------------

    /**
     * Set the name of this node.
     *
     * @param name The new name.
     */
    public void setName(String name) {
        vfName = name;

        if(!inSetup) {
            hasChanged[FIELD_NAME] = true;
            fireFieldChanged(FIELD_NAME);
        }
    }

    /**
     * Get the name of this node.
     *
     * @return The name.
     */
    public String getName() {
        return vfName;
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

        if(pShape != null)
            pShape.setupFinished();

        if(vfShape != null)
            vfShape.setupFinished();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ProductStructureChildNodeType;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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
        if(index < 0  || index > LAST_CADFACE_INDEX)
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
            case FIELD_SHAPE:
                fieldData.clear();
                if(pShape != null)
                    fieldData.nodeValue = pShape;
                else
                    fieldData.nodeValue = vfShape;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_BBOX_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_CENTER:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
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
                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
                    break;
                case FIELD_SHAPE :
                    if(pShape != null)
                        destNode.setValue(destIndex, pShape);
                    else
                        destNode.setValue(destIndex, vfShape);
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
            case FIELD_SHAPE :
                setShape(node);
                break;

            default:
                super.setValue(index, child);
        }

        if(!inSetup) {
            hasChanged[index] = true;
            fireFieldChanged(index);
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
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_BBOX_CENTER:
                setBboxCenter(value);
                break;

            case FIELD_BBOX_SIZE:
                setBboxSize(value);
                break;
            default:
               super.setValue(index, value, numValid);
        }
    }


    /**
     * Set node content as replacement for <code>shape</code>.
     *
     * @param shape The new shape.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setShape(VRMLNodeType shape)
        throws InvalidFieldValueException {

        VRMLShapeNodeType node;

        VRMLNodeType old_node;

        if(pShape != null)
            old_node = pShape;
        else
            old_node = vfShape;

        if (shape instanceof VRMLProtoInstance) {
            node = (VRMLShapeNodeType)
                ((VRMLProtoInstance)shape).getImplementationNode();
            pShape = (VRMLProtoInstance) shape;
            if ((node != null) && !(node instanceof VRMLShapeNodeType)) {
                throw new InvalidFieldValueException(SHAPE_PROTO_MSG);
            }
        } else if (shape != null &&
            (!(shape instanceof VRMLShapeNodeType))) {
            throw new InvalidFieldValueException(SHAPE_NODE_MSG);
        } else {
            pShape = null;
            node = (VRMLShapeNodeType) shape;
        }

        vfShape = (VRMLShapeNodeType)node;

        if (!inSetup) {
            if(old_node != null) {
                updateRefs(old_node, false);
                stateManager.registerRemovedNode(old_node);
            }

            if(shape != null) {
                updateRefs(shape, true);
                stateManager.registerAddedNode(shape);
            }

            hasChanged[FIELD_SHAPE] = true;
            fireFieldChanged(FIELD_SHAPE);
        }
    }

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box center to set
     */
    protected void setBboxCenter(float[] val) {
        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + " bboxCenter");

        vfBboxCenter[0] = val[0];
        vfBboxCenter[1] = val[1];
        vfBboxCenter[2] = val[2];
    }

    /**
     * Set the bounding box for this node. Only used by the field parser
     * at setup.
     *
     * @param val The box size to set
     * @throws InvalidFieldValueException The bounds is not valid
     */
    protected void setBboxSize(float[] val) throws InvalidFieldValueException {
        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + " bboxSize");

        vfBboxSize[0] = val[0];
        vfBboxSize[1] = val[1];
        vfBboxSize[2] = val[2];
    }

}
