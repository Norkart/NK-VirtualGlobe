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

package org.web3d.vrml.renderer.common.nodes.rigidphysics;

// External imports
import java.util.HashMap;

import org.odejava.Body;
import org.odejava.JointGroup;
import org.odejava.JointUniversal;
import org.odejava.World;
import org.odejava.ode.OdeConstants;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the UniversalJoint node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class BaseUniversalJoint extends BaseJointNode {

    // Field index constants

    /** The field index for anchorPoint */
    protected static final int FIELD_ANCHOR_POINT = LAST_JOINT_INDEX + 1;

    /** The field index for axis1 */
    protected static final int FIELD_AXIS1 = LAST_JOINT_INDEX + 2;

    /** The field index for axis2 */
    protected static final int FIELD_AXIS2 = LAST_JOINT_INDEX + 3;

    /** The field index for body1AnchorPoint */
    protected static final int FIELD_BODY1_ANCHOR_POINT = LAST_JOINT_INDEX + 4;

    /** The field index for body2AnchorPoint */
    protected static final int FIELD_BODY2_ANCHOR_POINT = LAST_JOINT_INDEX + 5;

    /** The field index for body1Axis */
    protected static final int FIELD_BODY1_AXIS = LAST_JOINT_INDEX + 6;

    /** The field index for body2Axis */
    protected static final int FIELD_BODY2_AXIS = LAST_JOINT_INDEX + 7;

    /** The field index for stop1Bounce */
    protected static final int FIELD_STOP1_BOUNCE = LAST_JOINT_INDEX + 8;

    /** The field index for stop2Bounce */
    protected static final int FIELD_STOP2_BOUNCE = LAST_JOINT_INDEX + 9;

    /** The field index for stop1ErrorCorrection */
    protected static final int FIELD_STOP1_ERROR_CORRECTION = LAST_JOINT_INDEX + 10;

    /** The field index for stop2ErrorCorrection */
    protected static final int FIELD_STOP2_ERROR_CORRECTION = LAST_JOINT_INDEX + 11;

    /** Last index used by this base node */
    protected static final int LAST_INDEX = FIELD_STOP2_ERROR_CORRECTION;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_INDEX + 1;

    /** Message when the stopBounce value is out of range */
    protected static final String BOUNCE1_RANGE_MSG =
        "The stop1Bounce value is out of the required range [0,1]: ";

    /** Message when the stop2Bounce value is out of range */
    protected static final String BOUNCE2_RANGE_MSG =
        "The stop2Bounce value is out of the required range [0,1]: ";

    /** Message when the stop1ErrorCorrection value is out of range */
    protected static final String STOP_ERROR1_RANGE_MSG =
        "The stop1ErrorCorrection value is out of the required range [0,1]: ";

    /** Message when the stop2ErrorCorrection value is out of range */
    protected static final String STOP_ERROR2_RANGE_MSG =
        "The stop2ErrorCorrection value is out of the required range [0,1]: ";

    /** Message when the user attempt to write to the body1AnchorPoint field */
    private static final String AP1_WRITE_MSG =
        "body1AnchorPoint is outputOnly and cannot be set";

    /** Message when the user attempt to write to the body2AnchorPoint field */
    private static final String AP2_WRITE_MSG =
        "body2AnchorPoint is outputOnly and cannot be set";

    /** Message when the user attempt to write to the body1Axis field */
    private static final String AX1_WRITE_MSG =
        "body1Axis is outputOnly and cannot be set";

    /** Message when the user attempt to write to the body2Axis field */
    private static final String AX2_WRITE_MSG =
        "body2Axis is outputOnly and cannot be set";

    /** Index list if the output capable fields */
    private static final int[] outputFields;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    // The VRML field values

    /** The value of the anchorPoint field */
    protected float[] vfAnchorPoint;

    /** The value of the axis1 field */
    protected float[] vfAxis1;

    /** The value of the axis2 field */
    protected float[] vfAxis2;

    /** The value of the body1AnchorPoint field */
    protected float[] vfBody1AnchorPoint;

    /** The value of the body1AnchorPoint field */
    protected float[] vfBody2AnchorPoint;

    /** The value of the body1Axis field */
    protected float[] vfBody1Axis;

    /** The value of the body1Axis field */
    protected float[] vfBody2Axis;

    /** The value of the stop1Bounce field */
    protected float vfStop1Bounce;

    /** The value of the stop2Bounce field */
    protected float vfStop2Bounce;

    /** The value of the stop1ErrorCorrection field */
    protected float vfStop1ErrorCorrection;

    /** The value of the stop2ErrorCorrection field */
    protected float vfStop2ErrorCorrection;

    // Other vars

    /** The ODE representation of the joint */
    protected JointUniversal odeJoint;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_BODY1,
            FIELD_BODY2
        };

        outputFields = new int[] {
            FIELD_BODY1_ANCHOR_POINT,
            FIELD_BODY2_ANCHOR_POINT,
            FIELD_BODY1_AXIS,
            FIELD_BODY2_AXIS,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ANCHOR_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "anchorPoint");
        fieldDecl[FIELD_AXIS1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "axis1");
        fieldDecl[FIELD_AXIS2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "axis2");
        fieldDecl[FIELD_BODY1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body1");
        fieldDecl[FIELD_BODY2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body2");
        fieldDecl[FIELD_BODY1_ANCHOR_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "body1AnchorPoint");
        fieldDecl[FIELD_BODY2_ANCHOR_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "body2AnchorPoint");
        fieldDecl[FIELD_BODY1_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "body1Axis");
        fieldDecl[FIELD_BODY2_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "body2Axis");
        fieldDecl[FIELD_STOP1_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop1Bounce");
        fieldDecl[FIELD_STOP2_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop2Bounce");
        fieldDecl[FIELD_STOP1_ERROR_CORRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop1ErrorCorrection");
        fieldDecl[FIELD_STOP2_ERROR_CORRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop2ErrorCorrection");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ANCHOR_POINT);
        fieldMap.put("anchorPoint", idx);
        fieldMap.put("set_anchorPoint", idx);
        fieldMap.put("anchorPoint_changed", idx);

        idx = new Integer(FIELD_BODY1);
        fieldMap.put("body1", idx);
        fieldMap.put("set_body1", idx);
        fieldMap.put("body1_changed", idx);

        idx = new Integer(FIELD_BODY2);
        fieldMap.put("body2", idx);
        fieldMap.put("set_body2", idx);
        fieldMap.put("body2_changed", idx);

        idx = new Integer(FIELD_AXIS1);
        fieldMap.put("axis1", idx);
        fieldMap.put("set_axis1", idx);
        fieldMap.put("axis1_changed", idx);

        idx = new Integer(FIELD_AXIS2);
        fieldMap.put("axis2", idx);
        fieldMap.put("set_axis2", idx);
        fieldMap.put("axis2_changed", idx);

        idx = new Integer(FIELD_STOP1_BOUNCE);
        fieldMap.put("stop1Bounce", idx);
        fieldMap.put("set_stop1Bounce", idx);
        fieldMap.put("stop1Bounce_changed", idx);

        idx = new Integer(FIELD_STOP2_BOUNCE);
        fieldMap.put("stop2Bounce", idx);
        fieldMap.put("set_stop2Bounce", idx);
        fieldMap.put("stop2Bounce_changed", idx);

        idx = new Integer(FIELD_STOP1_ERROR_CORRECTION);
        fieldMap.put("stop1ErrorCorrection", idx);
        fieldMap.put("set_stop1ErrorCorrection", idx);
        fieldMap.put("stop1ErrorCorrection_changed", idx);

        idx = new Integer(FIELD_STOP2_ERROR_CORRECTION);
        fieldMap.put("stop2ErrorCorrection", idx);
        fieldMap.put("set_stop2ErrorCorrection", idx);
        fieldMap.put("stop2ErrorCorrection_changed", idx);

        fieldMap.put("body1AnchorPoint", new Integer(FIELD_BODY1_ANCHOR_POINT));
        fieldMap.put("body2AnchorPoint", new Integer(FIELD_BODY2_ANCHOR_POINT));
        fieldMap.put("body1Axis", new Integer(FIELD_BODY1_AXIS));
        fieldMap.put("body2Axis", new Integer(FIELD_BODY2_AXIS));
    }

    /**
     * Construct a new default universal joint node object.
     */
    public BaseUniversalJoint() {
        super("UniversalJoint");

        vfAnchorPoint = new float[3];
        vfAxis1 = new float[3];
        vfAxis2 = new float[3];
        vfStop1Bounce = 0;
        vfStop2Bounce = 0;
        vfStop1ErrorCorrection = 0.8f;
        vfStop2ErrorCorrection = 0.8f;

        vfBody1AnchorPoint = new float[3];
        vfBody2AnchorPoint = new float[3];
        vfBody1Axis = new float[3];
        vfBody2Axis = new float[3];

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseUniversalJoint(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("anchorPoint");
            VRMLFieldData field = node.getFieldValue(index);
            vfAnchorPoint[0] = field.floatArrayValue[0];
            vfAnchorPoint[1] = field.floatArrayValue[1];
            vfAnchorPoint[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("axis1");
            field = node.getFieldValue(index);
            vfAxis1[0] = field.floatArrayValue[0];
            vfAxis1[1] = field.floatArrayValue[1];
            vfAxis1[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("axis2");
            field = node.getFieldValue(index);
            vfAxis2[0] = field.floatArrayValue[0];
            vfAxis2[1] = field.floatArrayValue[1];
            vfAxis2[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("stop1Bounce");
            field = node.getFieldValue(index);
            vfStop1Bounce = field.floatValue;

            index = node.getFieldIndex("stop2Bounce");
            field = node.getFieldValue(index);
            vfStop2Bounce = field.floatValue;

            index = node.getFieldIndex("stop1ErrorCorrection");
            field = node.getFieldValue(index);
            vfStop1ErrorCorrection = field.floatValue;

            index = node.getFieldIndex("stop2ErrorCorrection");
            field = node.getFieldValue(index);
            vfStop2ErrorCorrection = field.floatValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLRigidJointNodeType
    //----------------------------------------------------------

    /**
     * This node is about to be deleted due to a change in loaded world. Clear
     * up the ODE resources in use.
     */
    public void delete() {
        odeJoint.delete();
    }

    /**
     * Instruction to the node to fetch the appropriate field values' output
     * from the physics model and update the outputOnly field with the values.
     */
    public void updateRequestedOutputs() {
        for(int i = 0; i < numOutputIndices; i++) {
            switch(outputIndices[i]) {
            }
        }
    }

    /**
     * Set the parent world that this body belongs to. A null value clears
     * the world and indicates the physics model or body is no longer in use
     * by this world (eg deletes it).
     *
     * @param grp The group that this joint should belong to
     */
    public void setODEWorld(World wld, JointGroup grp) {
        if(wld != null) {
            odeJoint = new JointUniversal(wld, grp);
            Body body_1 = null;
            Body body_2 = null;

            if(vfBody1 != null)
                body_1 = vfBody1.getODEBody();

            if(vfBody2 != null)
                body_2 = vfBody2.getODEBody();

            odeJoint.attach(body_1, body_2);
            odeJoint.setAnchor(vfAnchorPoint[0],
                               vfAnchorPoint[1],
                               vfAnchorPoint[2]);
            odeJoint.setAxis1(vfAxis1[0], vfAxis1[1], vfAxis1[2]);
            odeJoint.setAxis2(vfAxis2[0], vfAxis2[1], vfAxis2[2]);
            odeJoint.setParam(OdeConstants.dParamBounce, vfStop1Bounce);
            odeJoint.setParam(OdeConstants.dParamBounce2, vfStop2Bounce);
            odeJoint.setParam(OdeConstants.dParamStopERP, vfStop1ErrorCorrection);
            odeJoint.setParam(OdeConstants.dParamStopERP2, vfStop2ErrorCorrection);
        } else
            odeJoint.delete();

    }

    /**
     * Set node content as replacement for the body1 field. This
     * checks only for basic node representation. If a concrete node needs a
     * specific set of nodes, it should override this method to check.
     *
     * @param body The new body representation.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setBody1(VRMLNodeType body)
        throws InvalidFieldValueException {

        super.setBody1(body);

        if(inSetup)
            return;

        Body body_1 = null;
        Body body_2 = null;

        if(vfBody1 != null)
            body_1 = vfBody1.getODEBody();

        if(vfBody2 != null)
            body_2 = vfBody2.getODEBody();

        odeJoint.attach(body_1, body_2);
    }

    /**
     * Set node content as replacement for the body1 field. This
     * checks only for basic node representation. If a concrete node needs a
     * specific set of nodes, it should override this method to check.
     *
     * @param body The new body representation.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setBody2(VRMLNodeType body)
        throws InvalidFieldValueException {

        super.setBody2(body);

        if(inSetup)
            return;

        Body body_1 = null;
        Body body_2 = null;

        if(vfBody1 != null)
            body_1 = vfBody1.getODEBody();

        if(vfBody2 != null)
            body_2 = vfBody2.getODEBody();

        odeJoint.attach(body_1, body_2);
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
        if (index < 0  || index > LAST_INDEX)
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
        return TypeConstants.RigidJointNodeType;
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

        Body body_1 = null;
        Body body_2 = null;

        if(vfBody1 != null)
            body_1 = vfBody1.getODEBody();

        if(vfBody2 != null)
            body_2 = vfBody2.getODEBody();

        odeJoint.attach(body_1, body_2);
        odeJoint.setAnchor(vfAnchorPoint[0],
                           vfAnchorPoint[1],
                           vfAnchorPoint[2]);
        odeJoint.setAxis1(vfAxis1[0], vfAxis1[1], vfAxis1[2]);
        odeJoint.setAxis2(vfAxis2[0], vfAxis2[1], vfAxis2[2]);
        odeJoint.setParam(OdeConstants.dParamBounce, vfStop1Bounce);
        odeJoint.setParam(OdeConstants.dParamBounce2, vfStop2Bounce);
        odeJoint.setParam(OdeConstants.dParamStopERP, vfStop1ErrorCorrection);
        odeJoint.setParam(OdeConstants.dParamStopERP2, vfStop2ErrorCorrection);
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
            case FIELD_ANCHOR_POINT:
                fieldData.clear();
                fieldData.floatArrayValue = vfAnchorPoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_AXIS1:
                fieldData.clear();
                fieldData.floatArrayValue = vfAxis1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_AXIS2:
                fieldData.clear();
                fieldData.floatArrayValue = vfAxis2;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BODY1_ANCHOR_POINT:
                fieldData.clear();
                fieldData.floatArrayValue = vfBody1AnchorPoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BODY2_ANCHOR_POINT:
                fieldData.clear();
                fieldData.floatArrayValue = vfBody2AnchorPoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BODY1_AXIS:
                fieldData.clear();
                fieldData.floatArrayValue = vfBody1Axis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BODY2_AXIS:
                fieldData.clear();
                fieldData.floatArrayValue = vfBody2Axis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_STOP1_BOUNCE:
                fieldData.clear();
                fieldData.floatValue = vfStop1Bounce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP2_BOUNCE:
                fieldData.clear();
                fieldData.floatValue = vfStop2Bounce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP1_ERROR_CORRECTION:
                fieldData.clear();
                fieldData.floatValue = vfStop1ErrorCorrection;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP2_ERROR_CORRECTION:
                fieldData.clear();
                fieldData.floatValue = vfStop2ErrorCorrection;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
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
                case FIELD_ANCHOR_POINT:
                    destNode.setValue(destIndex, vfAnchorPoint, 3);
                    break;

                case FIELD_AXIS1:
                    destNode.setValue(destIndex, vfAxis1, 3);
                    break;

                case FIELD_AXIS2:
                    destNode.setValue(destIndex, vfAxis2, 3);
                    break;

                case FIELD_BODY1_ANCHOR_POINT:
                    destNode.setValue(destIndex, vfBody1AnchorPoint, 3);
                    break;

                case FIELD_BODY2_ANCHOR_POINT:
                    destNode.setValue(destIndex, vfBody2AnchorPoint, 3);
                    break;

                case FIELD_BODY1_AXIS:
                    destNode.setValue(destIndex, vfBody1Axis, 3);
                    break;

                case FIELD_BODY2_AXIS:
                    destNode.setValue(destIndex, vfBody2Axis, 3);
                    break;

                case FIELD_STOP1_BOUNCE:
                    destNode.setValue(destIndex, vfStop1Bounce);
                    break;

                case FIELD_STOP2_BOUNCE:
                    destNode.setValue(destIndex, vfStop2Bounce);
                    break;

                case FIELD_STOP1_ERROR_CORRECTION:
                    destNode.setValue(destIndex, vfStop1ErrorCorrection);
                    break;

                case FIELD_STOP2_ERROR_CORRECTION:
                    destNode.setValue(destIndex, vfStop2ErrorCorrection);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("SingleAxis1Joint.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("SingleAxis1Joint.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a single float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float value)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_STOP1_BOUNCE:
                setStop1Bounce(value);
                break;

            case FIELD_STOP2_BOUNCE:
                setStop2Bounce(value);
                break;

            case FIELD_STOP1_ERROR_CORRECTION:
                setStop1ErrorCorrection(value);
                break;

            case FIELD_STOP2_ERROR_CORRECTION:
                setStop2ErrorCorrection(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a single float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_ANCHOR_POINT:
                setAnchorPoint(value);
                break;

            case FIELD_AXIS1:
                setAxis1(value);
                break;

            case FIELD_AXIS2:
                setAxis2(value);
                break;

            case FIELD_BODY1_ANCHOR_POINT:
                throw new InvalidFieldAccessException(AP1_WRITE_MSG);

            case FIELD_BODY2_ANCHOR_POINT:
                throw new InvalidFieldAccessException(AP2_WRITE_MSG);

            case FIELD_BODY1_AXIS:
                throw new InvalidFieldAccessException(AX1_WRITE_MSG);

            case FIELD_BODY2_AXIS:
                throw new InvalidFieldAccessException(AX2_WRITE_MSG);

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the anchor point location for this hinge.
     *
     * @param pos An array of 3 values for the position
     */
    private void setAnchorPoint(float[] pos) {
        vfAnchorPoint[0] = pos[0];
        vfAnchorPoint[1] = pos[1];
        vfAnchorPoint[2] = pos[2];

        if(!inSetup) {
            odeJoint.setAnchor(pos[0], pos[1], pos[2]);
            hasChanged[FIELD_ANCHOR_POINT] = true;
            fireFieldChanged(FIELD_ANCHOR_POINT);
        }
    }

    /**
     * Set the axis vector for this hinge.
     *
     * @param axis An array of 3 values for the vector
     */
    private void setAxis1(float[] axis) {
        vfAxis1[0] = axis[0];
        vfAxis1[1] = axis[1];
        vfAxis1[2] = axis[2];

        if(!inSetup) {
            odeJoint.setAxis1(axis[0], axis[1], axis[2]);
            hasChanged[FIELD_AXIS1] = true;
            fireFieldChanged(FIELD_AXIS1);
        }
    }

    /**
     * Set the axis vector for this hinge.
     *
     * @param axis An array of 3 values for the vector
     */
    private void setAxis2(float[] axis) {
        vfAxis2[0] = axis[0];
        vfAxis2[1] = axis[1];
        vfAxis2[2] = axis[2];

        if(!inSetup) {
            odeJoint.setAxis2(axis[0], axis[1], axis[2]);
            hasChanged[FIELD_AXIS2] = true;
            fireFieldChanged(FIELD_AXIS2);
        }
    }
    /**
     * Set the amount of stop bounce. This value should be between 0 and 1.
     * 0 is no bounce at all, 1 is full bounce.
     *
     * @param bounce The amount of bounce to use
     * @throws InvalidFieldValueException The value was not between [0,1]
     */
    private void setStop1Bounce(float bounce)
        throws InvalidFieldValueException {

        if(bounce < 0 || bounce > 1)
            throw new InvalidFieldValueException(BOUNCE1_RANGE_MSG + bounce);

        vfStop1Bounce = bounce;

        if(!inSetup) {
            odeJoint.setParam(OdeConstants.dParamBounce, bounce);
            hasChanged[FIELD_STOP1_BOUNCE] = true;
            fireFieldChanged(FIELD_STOP1_BOUNCE);
        }
    }

    /**
     * Set the amount of stop bounce. This value should be between 0 and 1.
     * 0 is no bounce at all, 1 is full bounce.
     *
     * @param bounce The amount of bounce to use
     * @throws InvalidFieldValueException The value was not between [0,1]
     */
    private void setStop2Bounce(float bounce)
        throws InvalidFieldValueException {

        if(bounce < 0 || bounce > 1)
            throw new InvalidFieldValueException(BOUNCE2_RANGE_MSG + bounce);

        vfStop2Bounce = bounce;

        if(!inSetup) {
            odeJoint.setParam(OdeConstants.dParamBounce2, bounce);
            hasChanged[FIELD_STOP2_BOUNCE] = true;
            fireFieldChanged(FIELD_STOP2_BOUNCE);
        }
    }

    /**
     * Set the amount of error correction that should be performed on a value
     * that has hit the stop. This value should be between 0 and 1.
     * 0 is no correction at all, 1 is full correction in a single step.
     *
     * @param bounce The amount of error to correct
     * @throws InvalidFieldValueException The value was not between [0,1]
     */
    private void setStop1ErrorCorrection(float error)
        throws InvalidFieldValueException {

        if(error < 0 || error > 1)
            throw new InvalidFieldValueException(STOP_ERROR1_RANGE_MSG + error);

        vfStop1ErrorCorrection = error;

        if(!inSetup) {
            odeJoint.setParam(OdeConstants.dParamStopERP, error);
            hasChanged[FIELD_STOP1_ERROR_CORRECTION] = true;
            fireFieldChanged(FIELD_STOP1_ERROR_CORRECTION);
        }
    }

    /**
     * Set the amount of error correction that should be performed on a value
     * that has hit the stop. This value should be between 0 and 1.
     * 0 is no correction at all, 1 is full correction in a single step.
     *
     * @param bounce The amount of error to correct
     * @throws InvalidFieldValueException The value was not between [0,1]
     */
    private void setStop2ErrorCorrection(float error)
        throws InvalidFieldValueException {

        if(error < 0 || error > 1)
            throw new InvalidFieldValueException(STOP_ERROR2_RANGE_MSG + error);

        vfStop2ErrorCorrection = error;

        if(!inSetup) {
            odeJoint.setParam(OdeConstants.dParamStopERP2, error);
            hasChanged[FIELD_STOP2_ERROR_CORRECTION] = true;
            fireFieldChanged(FIELD_STOP2_ERROR_CORRECTION);
        }
    }

    /**
     * Return to the caller a list of the indices of all output fields of this
     * concrete node. Array must be the correct length.
     *
     * @return the output listing of indicies
     */
    int[] getAllOutputFieldIndices() {
        return outputFields;
    }
}
