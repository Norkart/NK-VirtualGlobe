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
import org.odejava.JointHinge2;
import org.odejava.JointGroup;
import org.odejava.World;
import org.odejava.ode.Ode;
import org.odejava.ode.SWIGTYPE_p_float;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the DoubleAxisHingeJoint node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class BaseDoubleAxisHingeJoint extends BaseJointNode {

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

    /** The field index for desiredAngularVelocity1 */
    protected static final int FIELD_ANGULAR_VELOCITY1 = LAST_JOINT_INDEX + 6;

    /** The field index for desiredAngularVelocity2 */
    protected static final int FIELD_ANGULAR_VELOCITY2 = LAST_JOINT_INDEX + 7;

    /** The field index for body1Axis */
    protected static final int FIELD_BODY1_AXIS = LAST_JOINT_INDEX + 8;

    /** The field index for body2Axis */
    protected static final int FIELD_BODY2_AXIS = LAST_JOINT_INDEX + 9;

    /** The field index for minAngle1 */
    protected static final int FIELD_MIN_ANGLE1 = LAST_JOINT_INDEX + 10;

    /** The field index for maxAngle1 */
    protected static final int FIELD_MAX_ANGLE1 = LAST_JOINT_INDEX + 11;

    /** The field index for maxTorque1 */
    protected static final int FIELD_MAX_TORQUE1 = LAST_JOINT_INDEX + 12;

    /** The field index for maxTorque2*/
    protected static final int FIELD_MAX_TORQUE2 = LAST_JOINT_INDEX + 13;

    /** The field index for hinge1Angle */
    protected static final int FIELD_HINGE1_ANGLE = LAST_JOINT_INDEX + 14;

    /** The field index for hinge2Angle */
    protected static final int FIELD_HINGE2_ANGLE = LAST_JOINT_INDEX + 15;

    /** The field index for hinge1AngleRate */
    protected static final int FIELD_HINGE1_ANGLE_RATE = LAST_JOINT_INDEX + 16;

    /** The field index for hinge2AngleRate */
    protected static final int FIELD_HINGE2_ANGLE_RATE = LAST_JOINT_INDEX + 17;

    /** The field index for stopBounce1 */
    protected static final int FIELD_STOP_BOUNCE1 = LAST_JOINT_INDEX + 18;

    /** The field index for stopErrorCorrection1 */
    protected static final int FIELD_STOP_ERROR_CORRECTION1 = LAST_JOINT_INDEX + 19;

    /** The field index for stopConstantForceMix1 */
    protected static final int FIELD_STOP_CFM1 = LAST_JOINT_INDEX + 20;

    /** The field index for suspensionForce */
    protected static final int FIELD_SUSPENSION_FORCE = LAST_JOINT_INDEX + 21;

    /** The field index for suspensionErrorCorrection */
    protected static final int FIELD_SUSPENSION_ERP = LAST_JOINT_INDEX + 22;

    /** Last index used by this base node */
    protected static final int LAST_INDEX = FIELD_SUSPENSION_ERP;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_INDEX + 1;

    /** Message when the minAngle1 value is out of range */
    protected static final String MIN_ANGLE1_MSG =
        "The minAngle1 value is out of the required range [-pi,pi]: ";

    /** Message when the maxAngle1 value is out of range */
    protected static final String MAX_ANGLE1_MSG =
        "The maxAngle1 value is out of the required range [-pi,pi]: ";

    /** Message when the stopBounce value is out of range */
    protected static final String BOUNCE1_RANGE_MSG =
        "The stopBounce1 value is out of the required range [0,1]: ";

    /** Message when the stopErrorCorrection1 value is out of range */
    protected static final String STOP_ERROR1_RANGE_MSG =
        "The stopErrorCorrection1 value is out of the required range [0,1]: ";

    /** Message when the suspension force value is negative */
    protected static final String STOP_CFM1_NEG_MSG =
        "The stopConstantForceMix1 value is negative: ";

    /** Message when the suspension force value is negative */
    protected static final String SUSP_NEG_MSG =
        "The suspensionForce value is negative: ";

    /** The suspenion error correction is out of range */
    protected static final String SUSP_ERROR_RANGE_MSG =
        "The suspensionErrorCorrection value is out of the required range [0,1]: ";

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

    /** Message when the user attempt to write to the hinge1Angle field */
    private static final String HA1_WRITE_MSG =
        "hinge1Angle is outputOnly and cannot be set";

    /** Message when the user attempt to write to the hinge2Angle field */
    private static final String HA2_WRITE_MSG =
        "hinge2Angle is outputOnly and cannot be set";

    /** Message when the user attempt to write to the hinge1AngleRate field */
    private static final String HAR1_WRITE_MSG =
        "hinge1AngleRate is outputOnly and cannot be set";

    /** Message when the user attempt to write to the hinge2AngleRate field */
    private static final String HAR2_WRITE_MSG =
        "hinge2AngleRate is outputOnly and cannot be set";

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

    /** The value of the body1AnchorPoint field */
    protected float[] vfBody1AnchorPoint;

    /** The value of the body1AnchorPoint field */
    protected float[] vfBody2AnchorPoint;

    /** The value of the axis1 field */
    protected float[] vfAxis1;

    /** The value of the axis2 field */
    protected float[] vfAxis2;

    /** The value of the body1Axis field */
    protected float[] vfBody1Axis;

    /** The value of the body1Axis field */
    protected float[] vfBody2Axis;

    /** The value of the desiredAngularVelocity1 field */
    protected float vfDesiredAngularVelocity1;

    /** The value of the desiredAngularVelocity2 field */
    protected float vfDesiredAngularVelocity2;

    /** The value of the maxTorque1 field */
    protected float vfMaxTorque1;

    /** The value of the maxTorque2 field */
    protected float vfMaxTorque2;

    /** The value of the minAngle1 field */
    protected float vfMinAngle1;

    /** The value of the maxAngle1 field */
    protected float vfMaxAngle1;

    /** The value of the hinge1Angle field */
    protected float vfHinge1Angle;

    /** The value of the hinge2Angle field */
    protected float vfHinge2Angle;

    /** The value of the hinge1AngleRate field */
    protected float vfHinge1AngleRate;

    /** The value of the hinge2AngleRate field */
    protected float vfHinge2AngleRate;

    /** The value of the stopBounce1 field */
    protected float vfStopBounce1;

    /** The value of the stopErrorCorrection1 field */
    protected float vfStopErrorCorrection1;

    /** The value of the stopConstantForceMix1 field */
    protected float vfStopConstantForceMix1;

    /** The value of the suspensionForce field */
    protected float vfSuspensionForce;

    /** The value of the suspensionErrorCorrection field */
    protected float vfSuspensionErrorCorrection;

    // Other vars

    /** The ODE representation of the joint */
    protected JointHinge2 odeJoint;

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
            FIELD_HINGE1_ANGLE,
            FIELD_HINGE2_ANGLE,
            FIELD_HINGE1_ANGLE_RATE,
            FIELD_HINGE2_ANGLE_RATE,
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
        fieldDecl[FIELD_MUST_OUTPUT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "mustOutput");
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
        fieldDecl[FIELD_MIN_ANGLE1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "minAngle1");
        fieldDecl[FIELD_MAX_ANGLE1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxAngle1");
        fieldDecl[FIELD_STOP_BOUNCE1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stopBounce1");
        fieldDecl[FIELD_MAX_TORQUE1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxTorque1");
        fieldDecl[FIELD_MAX_TORQUE2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxTorque2");
        fieldDecl[FIELD_ANGULAR_VELOCITY1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "desiredAngularVelocity1");
        fieldDecl[FIELD_ANGULAR_VELOCITY2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "desiredAngularVelocity2");
        fieldDecl[FIELD_HINGE1_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "hinge1Angle");
        fieldDecl[FIELD_HINGE2_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "hinge2Angle");
        fieldDecl[FIELD_HINGE1_ANGLE_RATE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "hinge1AngleRate");
        fieldDecl[FIELD_HINGE2_ANGLE_RATE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "hinge2AngleRate");
        fieldDecl[FIELD_STOP_ERROR_CORRECTION1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stopErrorCorrection1");
        fieldDecl[FIELD_STOP_CFM1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stopConstantForceMix1");
        fieldDecl[FIELD_SUSPENSION_FORCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "suspensionForce");
        fieldDecl[FIELD_SUSPENSION_ERP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "suspensionErrorCorrection");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ANCHOR_POINT);
        fieldMap.put("anchorPoint", idx);
        fieldMap.put("set_anchorPoint", idx);
        fieldMap.put("anchorPoint_changed", idx);

        idx = new Integer(FIELD_AXIS1);
        fieldMap.put("axis1", idx);
        fieldMap.put("set_axis1", idx);
        fieldMap.put("axis1_changed", idx);

        idx = new Integer(FIELD_AXIS2);
        fieldMap.put("axis2", idx);
        fieldMap.put("set_axis2", idx);
        fieldMap.put("axis2_changed", idx);

        idx = new Integer(FIELD_MUST_OUTPUT);
        fieldMap.put("mustOutput", idx);
        fieldMap.put("set_mustOutput", idx);
        fieldMap.put("mustOutput_changed", idx);

        idx = new Integer(FIELD_BODY1);
        fieldMap.put("body1", idx);
        fieldMap.put("set_body1", idx);
        fieldMap.put("body1_changed", idx);

        idx = new Integer(FIELD_BODY2);
        fieldMap.put("body2", idx);
        fieldMap.put("set_body2", idx);
        fieldMap.put("body2_changed", idx);

        idx = new Integer(FIELD_ANGULAR_VELOCITY1);
        fieldMap.put("desiredAngularVelocity1", idx);
        fieldMap.put("set_desiredAngularVelocity1", idx);
        fieldMap.put("desiredAngularVelocity1_changed", idx);

        idx = new Integer(FIELD_ANGULAR_VELOCITY2);
        fieldMap.put("desiredAngularVelocity2", idx);
        fieldMap.put("set_desiredAngularVelocity2", idx);
        fieldMap.put("desiredAngularVelocity2_changed", idx);

        idx = new Integer(FIELD_MAX_TORQUE1);
        fieldMap.put("maxTorque1", idx);
        fieldMap.put("set_maxTorque1", idx);
        fieldMap.put("maxTorque1_changed", idx);

        idx = new Integer(FIELD_MAX_TORQUE2);
        fieldMap.put("maxTorque2", idx);
        fieldMap.put("set_maxTorque2", idx);
        fieldMap.put("maxTorque2_changed", idx);

        idx = new Integer(FIELD_MIN_ANGLE1);
        fieldMap.put("minAngle1", idx);
        fieldMap.put("set_minAngle1", idx);
        fieldMap.put("minAngle1_changed", idx);

        idx = new Integer(FIELD_MAX_ANGLE1);
        fieldMap.put("maxAngle1", idx);
        fieldMap.put("set_maxAngle1", idx);
        fieldMap.put("maxAngle1_changed", idx);

        idx = new Integer(FIELD_STOP_BOUNCE1);
        fieldMap.put("stopBounce1", idx);
        fieldMap.put("set_stopBounce1", idx);
        fieldMap.put("stopBounce1_changed", idx);

        idx = new Integer(FIELD_STOP_ERROR_CORRECTION1);
        fieldMap.put("stopErrorCorrection1", idx);
        fieldMap.put("set_stopErrorCorrection1", idx);
        fieldMap.put("stopErrorCorrection1_changed", idx);

        idx = new Integer(FIELD_STOP_CFM1);
        fieldMap.put("stopConstantForceMix1", idx);
        fieldMap.put("set_stopConstantForceMix1", idx);
        fieldMap.put("stopConstantForceMix1_changed", idx);

        idx = new Integer(FIELD_SUSPENSION_FORCE);
        fieldMap.put("suspensionForce", idx);
        fieldMap.put("set_suspensionForce", idx);
        fieldMap.put("suspensionForce_changed", idx);

        idx = new Integer(FIELD_SUSPENSION_ERP);
        fieldMap.put("suspensionErrorCorrection", idx);
        fieldMap.put("set_suspensionErrorCorrection", idx);
        fieldMap.put("suspensionErrorCorrection_changed", idx);

        fieldMap.put("body1AnchorPoint", new Integer(FIELD_BODY1_ANCHOR_POINT));
        fieldMap.put("body2AnchorPoint", new Integer(FIELD_BODY2_ANCHOR_POINT));
        fieldMap.put("body1Axis", new Integer(FIELD_BODY1_AXIS));
        fieldMap.put("body2Axis", new Integer(FIELD_BODY2_AXIS));
        fieldMap.put("hinge1Angle", new Integer(FIELD_HINGE1_ANGLE));
        fieldMap.put("hinge2Angle", new Integer(FIELD_HINGE2_ANGLE));
        fieldMap.put("hinge1AngleRate", new Integer(FIELD_HINGE1_ANGLE_RATE));
        fieldMap.put("hinge2AngleRate", new Integer(FIELD_HINGE2_ANGLE_RATE));
    }

    /**
     * Construct a new double axis hinge joint node object.
     */
    public BaseDoubleAxisHingeJoint() {
        super("DoubleAxisHingeJoint");

        vfAnchorPoint = new float[3];
        vfAxis1 = new float[3];
        vfAxis2 = new float[3];
        vfMinAngle1 = -(float)Math.PI;
        vfMaxAngle1 = (float)Math.PI;
        vfStopBounce1 = 0;
        vfStopErrorCorrection1 = 0.8f;
        vfStopConstantForceMix1 = 0.001f;
        vfSuspensionErrorCorrection = 0.8f;

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
    public BaseDoubleAxisHingeJoint(VRMLNodeType node) {
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

            index = node.getFieldIndex("desiredAngularVelocity1");
            field = node.getFieldValue(index);
            vfDesiredAngularVelocity1 = field.floatValue;

            index = node.getFieldIndex("desiredAngularVelocity2");
            field = node.getFieldValue(index);
            vfDesiredAngularVelocity2 = field.floatValue;

            index = node.getFieldIndex("maxTorque1");
            field = node.getFieldValue(index);
            vfMaxTorque1 = field.floatValue;

            index = node.getFieldIndex("maxTorque2");
            field = node.getFieldValue(index);
            vfMaxTorque2 = field.floatValue;

            index = node.getFieldIndex("minAngle1");
            field = node.getFieldValue(index);
            vfMinAngle1 = field.floatValue;

            index = node.getFieldIndex("maxAngle1");
            field = node.getFieldValue(index);
            vfMaxAngle1 = field.floatValue;

            index = node.getFieldIndex("stopBounce1");
            field = node.getFieldValue(index);
            vfStopBounce1 = field.floatValue;

            index = node.getFieldIndex("stopErrorCorrection1");
            field = node.getFieldValue(index);
            vfStopErrorCorrection1 = field.floatValue;

            index = node.getFieldIndex("stopConstantForceMix1");
            field = node.getFieldValue(index);
            vfStopConstantForceMix1 = field.floatValue;

            index = node.getFieldIndex("suspensionForce");
            field = node.getFieldValue(index);
            vfSuspensionForce = field.floatValue;

            index = node.getFieldIndex("suspensionErrorCorrection");
            field = node.getFieldValue(index);
            vfSuspensionErrorCorrection = field.floatValue;

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
        if(odeJoint == null)
            return;

        // The implementation goes straight to the ODE C interface to avoid the
        // garbage generation that the getAnchor() method on JointBall creates.
        for(int i = 0; i < numOutputIndices; i++) {
            switch(outputIndices[i]) {
                case FIELD_BODY1_ANCHOR_POINT:
                    odeJoint.getAnchor(vfBody1AnchorPoint);

                    hasChanged[FIELD_BODY1_ANCHOR_POINT] = true;
                    fireFieldChanged(FIELD_BODY1_ANCHOR_POINT);
                    break;

                case FIELD_BODY2_ANCHOR_POINT:
                    odeJoint.getAnchor2(vfBody2AnchorPoint);

                    hasChanged[FIELD_BODY2_ANCHOR_POINT] = true;
                    fireFieldChanged(FIELD_BODY2_ANCHOR_POINT);
                    break;

                case FIELD_BODY1_AXIS:
                    odeJoint.getAxis1(vfBody1Axis);

                    hasChanged[FIELD_BODY1_AXIS] = true;
                    fireFieldChanged(FIELD_BODY1_AXIS);
                    break;

                case FIELD_BODY2_AXIS:
                    odeJoint.getAxis2(vfBody2Axis);

                    hasChanged[FIELD_BODY2_AXIS] = true;
                    fireFieldChanged(FIELD_BODY2_AXIS);
                    break;

                case FIELD_HINGE1_ANGLE:
                    vfHinge1Angle = odeJoint.getAngle1();

                    hasChanged[FIELD_HINGE1_ANGLE] = true;
                    fireFieldChanged(FIELD_HINGE1_ANGLE);
                    break;

                case FIELD_HINGE2_ANGLE:
// ODE API is missing for hinge angle 2 currently.
//                    vfHinge2Angle = odeJoint.getAngle2();
                    hasChanged[FIELD_HINGE2_ANGLE] = true;
                    fireFieldChanged(FIELD_HINGE2_ANGLE);
                    break;

                case FIELD_HINGE1_ANGLE_RATE:
                    vfHinge1AngleRate = odeJoint.getAngle1Rate();
                    hasChanged[FIELD_HINGE1_ANGLE_RATE] = true;
                    fireFieldChanged(FIELD_HINGE1_ANGLE_RATE);
                    break;

                case FIELD_HINGE2_ANGLE_RATE:
                    vfHinge2AngleRate = odeJoint.getAngle2Rate();
                    hasChanged[FIELD_HINGE2_ANGLE_RATE] = true;
                    fireFieldChanged(FIELD_HINGE2_ANGLE_RATE);
                    break;
            }
        }
    }

    /**
     * Set the parent world that this body belongs to. A null value clears
     * the world and indicates the physics model or body is no longer in use
     * by this world (eg deletes it).
     *
     * @param wld The new world instance to use or null
     * @param grp The group that this joint should belong to
     */
    public void setODEWorld(World wld, JointGroup grp) {
        if(wld != null) {
            odeJoint = new JointHinge2(wld, grp);

            Body body_1 = null;
            Body body_2 = null;

            if(vfBody1 != null)
                body_1 = vfBody1.getODEBody();

            if(vfBody2 != null)
                body_2 = vfBody2.getODEBody();

            if((body_1 == null && body_2 != null) ||
               (body_1 != null && body_2 == null)) {
                errorReporter.warningReport(
                   "Double Axis Hinge requires two non-null bodies", null);
                return;
            }

            odeJoint.attach(body_1, body_2);
            odeJoint.setAnchor(vfAnchorPoint[0],
                               vfAnchorPoint[1],
                               vfAnchorPoint[2]);
            odeJoint.setAxis1(vfAxis1[0], vfAxis1[1], vfAxis1[2]);
            odeJoint.setAxis2(vfAxis2[0], vfAxis2[1], vfAxis2[2]);

            odeJoint.setMinAngleStop(vfMinAngle1);
            odeJoint.setMaxAngleStop(vfMaxAngle1);
            odeJoint.setMaxTorque1(vfMaxTorque1);
            odeJoint.setMaxTorque2(vfMaxTorque2);
            odeJoint.setDesiredAngularVelocity1(vfDesiredAngularVelocity1);
            odeJoint.setDesiredAngularVelocity2(vfDesiredAngularVelocity2);
            odeJoint.setStopBounce(vfStopBounce1);
            odeJoint.setStopERP(vfStopErrorCorrection1);
            odeJoint.setStopCFM(vfStopConstantForceMix1);
            odeJoint.setSuspensionERP(vfSuspensionErrorCorrection);
            odeJoint.setSuspensionCFM(vfSuspensionForce);
//            odeJoint.setParam(org.odejava.ode.OdeConstants.dParamFudgeFactor, 0.1f);
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

        if(odeJoint != null) {
          if((body_1 == null && body_2 != null) ||
               (body_1 != null && body_2 == null)) {
                errorReporter.warningReport(
                   "Double Axis Hinge requires two non-null bodies", null);
                return;
            }

            odeJoint.attach(body_1, body_2);
        }
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

        if(odeJoint != null) {
            if((body_1 == null && body_2 != null) ||
               (body_1 != null && body_2 == null)) {
                errorReporter.warningReport(
                   "Double Axis Hinge requires two non-null bodies", null);
                return;
            }

            odeJoint.attach(body_1, body_2);
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

        if(odeJoint == null)
            return;

        Body body_1 = null;
        Body body_2 = null;

        if(vfBody1 != null)
            body_1 = vfBody1.getODEBody();

        if(vfBody2 != null)
            body_2 = vfBody2.getODEBody();

        if((body_1 == null && body_2 != null) ||
           (body_1 != null && body_2 == null)) {
            errorReporter.warningReport(
               "Double Axis Hinge requires two non-null bodies", null);
            return;
        }

        odeJoint.attach(body_1, body_2);
        odeJoint.setAnchor(vfAnchorPoint[0],
                           vfAnchorPoint[1],
                           vfAnchorPoint[2]);
        odeJoint.setMinAngleStop(vfMinAngle1);
        odeJoint.setMaxAngleStop(vfMaxAngle1);
        odeJoint.setMaxTorque1(vfMaxTorque1);
        odeJoint.setMaxTorque2(vfMaxTorque2);
        odeJoint.setDesiredAngularVelocity1(vfDesiredAngularVelocity1);
        odeJoint.setDesiredAngularVelocity2(vfDesiredAngularVelocity2);
        odeJoint.setStopBounce(vfStopBounce1);
        odeJoint.setStopERP(vfStopErrorCorrection1);
        odeJoint.setStopCFM(vfStopConstantForceMix1);
        odeJoint.setSuspensionERP(vfSuspensionErrorCorrection);
        odeJoint.setSuspensionCFM(vfSuspensionForce);
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

            case FIELD_ANGULAR_VELOCITY1:
                fieldData.clear();
                fieldData.floatValue = vfDesiredAngularVelocity1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_ANGULAR_VELOCITY2:
                fieldData.clear();
                fieldData.floatValue = vfDesiredAngularVelocity2;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
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

            case FIELD_MIN_ANGLE1:
                fieldData.clear();
                fieldData.floatValue = vfMinAngle1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MAX_ANGLE1:
                fieldData.clear();
                fieldData.floatValue = vfMaxAngle1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_HINGE1_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfHinge1Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_HINGE2_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfHinge2Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MAX_TORQUE1:
                fieldData.clear();
                fieldData.floatValue = vfMaxTorque1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MAX_TORQUE2:
                fieldData.clear();
                fieldData.floatValue = vfMaxTorque2;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_HINGE1_ANGLE_RATE:
                fieldData.clear();
                fieldData.floatValue = vfHinge1AngleRate;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_HINGE2_ANGLE_RATE:
                fieldData.clear();
                fieldData.floatValue = vfHinge2AngleRate;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP_BOUNCE1:
                fieldData.clear();
                fieldData.floatValue = vfStopBounce1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP_ERROR_CORRECTION1:
                fieldData.clear();
                fieldData.floatValue = vfStopErrorCorrection1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP_CFM1:
                fieldData.clear();
                fieldData.floatValue = vfStopConstantForceMix1;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SUSPENSION_FORCE:
                fieldData.clear();
                fieldData.floatValue = vfSuspensionForce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SUSPENSION_ERP:
                fieldData.clear();
                fieldData.floatValue = vfSuspensionErrorCorrection;
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

                case FIELD_MIN_ANGLE1:
                    destNode.setValue(destIndex, vfMinAngle1);
                    break;

                case FIELD_MAX_ANGLE1:
                    destNode.setValue(destIndex, vfMaxAngle1);
                    break;

                case FIELD_MAX_TORQUE1:
                    destNode.setValue(destIndex, vfMaxTorque1);
                    break;

                case FIELD_MAX_TORQUE2:
                    destNode.setValue(destIndex, vfMaxTorque2);
                    break;

                case FIELD_ANGULAR_VELOCITY1:
                    destNode.setValue(destIndex, vfDesiredAngularVelocity1);
                    break;

                case FIELD_ANGULAR_VELOCITY2:
                    destNode.setValue(destIndex, vfDesiredAngularVelocity2);
                    break;

                case FIELD_HINGE1_ANGLE:
                    destNode.setValue(destIndex, vfHinge1Angle);
                    break;

                case FIELD_HINGE2_ANGLE:
                    destNode.setValue(destIndex, vfHinge2Angle);
                    break;

                case FIELD_HINGE1_ANGLE_RATE:
                    destNode.setValue(destIndex, vfHinge1AngleRate);
                    break;

                case FIELD_HINGE2_ANGLE_RATE:
                    destNode.setValue(destIndex, vfHinge2AngleRate);
                    break;

                case FIELD_STOP_BOUNCE1:
                    destNode.setValue(destIndex, vfStopBounce1);
                    break;

                case FIELD_STOP_ERROR_CORRECTION1:
                    destNode.setValue(destIndex, vfStopErrorCorrection1);
                    break;

                case FIELD_STOP_CFM1:
                    destNode.setValue(destIndex, vfStopConstantForceMix1);
                    break;

                case FIELD_SUSPENSION_FORCE:
                    destNode.setValue(destIndex, vfSuspensionForce);
                    break;

                case FIELD_SUSPENSION_ERP:
                    destNode.setValue(destIndex, vfSuspensionErrorCorrection);
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
            case FIELD_MIN_ANGLE1:
                setMinAngle1(value);
                break;

            case FIELD_MAX_ANGLE1:
                setMaxAngle1(value);
                break;

            case FIELD_MAX_TORQUE1:
                setMaxTorque1(value);
                break;

            case FIELD_MAX_TORQUE2:
                setMaxTorque2(value);
                break;

            case FIELD_ANGULAR_VELOCITY1:
                setDesiredAngularVelocity1(value);
                break;

            case FIELD_ANGULAR_VELOCITY2:
                setDesiredAngularVelocity2(value);
                break;

            case FIELD_STOP_BOUNCE1:
                setStopBounce1(value);
                break;

            case FIELD_STOP_ERROR_CORRECTION1:
                setStopErrorCorrection1(value);
                break;

            case FIELD_STOP_CFM1:
                setStopConstantForceMix1(value);
                break;

            case FIELD_HINGE1_ANGLE:
                throw new InvalidFieldAccessException(HA1_WRITE_MSG);

            case FIELD_HINGE2_ANGLE:
                throw new InvalidFieldAccessException(HA2_WRITE_MSG);

            case FIELD_HINGE1_ANGLE_RATE:
                throw new InvalidFieldAccessException(HAR1_WRITE_MSG);

            case FIELD_HINGE2_ANGLE_RATE:
                throw new InvalidFieldAccessException(HAR2_WRITE_MSG);

            case FIELD_SUSPENSION_FORCE:
                setSuspensionForce(value);
                break;

            case FIELD_SUSPENSION_ERP:
                setSuspensionErrorCorrection(value);
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
     * Set the minimum angle stop position. This value should be between -PI
     * and +PI.
     *
     * @param angle The amount of angle to use
     * @throws InvalidFieldValueException The value was not between [-PI,PI]
     */
    private void setMinAngle1(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(MIN_ANGLE1_MSG + angle);

        vfMinAngle1 = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setMinAngleStop(angle);
            hasChanged[FIELD_MIN_ANGLE1] = true;
            fireFieldChanged(FIELD_MIN_ANGLE1);
        }
    }

    /**
     * Set the maximum angle stop position. This value should be between -PI
     * and +PI.
     *
     * @param angle The amount of angle to use
     * @throws InvalidFieldValueException The value was not between [-PI,PI]
     */
    private void setMaxAngle1(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(MAX_ANGLE1_MSG + angle);

        vfMaxAngle1 = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setMaxAngleStop(angle);
            hasChanged[FIELD_MAX_ANGLE1] = true;
            fireFieldChanged(FIELD_MAX_ANGLE1);
        }
    }

    /**
     * Set the maximum torque for axis 1.
     *
     * @param value The amount of torque to use
     */
    private void setMaxTorque1(float value) {

        vfMaxTorque1 = value;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setMaxTorque1(value);
            hasChanged[FIELD_MAX_TORQUE1] = true;
            fireFieldChanged(FIELD_MAX_TORQUE1);
        }
    }

    /**
     * Set the maximum torque for axis 2.
     *
     * @param value The amount of torque to use
     */
    private void setMaxTorque2(float value) {

        vfMaxTorque2 = value;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setMaxTorque2(value);
            hasChanged[FIELD_MAX_TORQUE2] = true;
            fireFieldChanged(FIELD_MAX_TORQUE2);
        }
    }

    /**
     * Set the maximum torque for axis 1.
     *
     * @param value The amount of torque to use
     */
    private void setDesiredAngularVelocity1(float value) {

        vfDesiredAngularVelocity1 = value;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setDesiredAngularVelocity1(value);
            hasChanged[FIELD_ANGULAR_VELOCITY1] = true;
            fireFieldChanged(FIELD_ANGULAR_VELOCITY1);
        }
    }

    /**
     * Set the maximum torque for axis 2.
     *
     * @param value The amount of torque to use
     */
    private void setDesiredAngularVelocity2(float value) {

        vfDesiredAngularVelocity2 = value;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setDesiredAngularVelocity2(value);
            hasChanged[FIELD_ANGULAR_VELOCITY2] = true;
            fireFieldChanged(FIELD_ANGULAR_VELOCITY2);
        }
    }

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
            if(odeJoint != null)
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
            if(odeJoint != null)
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
            if(odeJoint != null)
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
    private void setStopBounce1(float bounce)
        throws InvalidFieldValueException {

        if(bounce < 0 || bounce > 1)
            throw new InvalidFieldValueException(BOUNCE1_RANGE_MSG + bounce);

        vfStopBounce1 = bounce;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setStopBounce(bounce);
            hasChanged[FIELD_STOP_BOUNCE1] = true;
            fireFieldChanged(FIELD_STOP_BOUNCE1);
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
    private void setStopErrorCorrection1(float error)
        throws InvalidFieldValueException {

        if(error < 0 || error > 1)
            throw new InvalidFieldValueException(STOP_ERROR1_RANGE_MSG + error);

        vfStopErrorCorrection1 = error;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setStopERP(error);
            hasChanged[FIELD_STOP_ERROR_CORRECTION1] = true;
            fireFieldChanged(FIELD_STOP_ERROR_CORRECTION1);
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
    private void setStopConstantForceMix1(float error)
        throws InvalidFieldValueException {

        if(error < 0)
            throw new InvalidFieldValueException(STOP_CFM1_NEG_MSG + error);

        vfStopConstantForceMix1 = error;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setStopCFM(error);
            hasChanged[FIELD_STOP_CFM1] = true;
            fireFieldChanged(FIELD_STOP_CFM1);
        }
    }

    /**
     * Set the amount of error correction that should be performed on a value
     * that has hit the stop. This value should be between 0 and 1.
     * 0 is no correction at all, 1 is full correction in a single step.
     *
     * @param force The amount of force to use
     * @throws InvalidFieldValueException The value was negative
     */
    private void setSuspensionForce(float force)
        throws InvalidFieldValueException {

        if(force < 0)
            throw new InvalidFieldValueException(SUSP_NEG_MSG + force);

        vfSuspensionForce = force;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setSuspensionCFM(force);
            hasChanged[FIELD_SUSPENSION_FORCE] = true;
            fireFieldChanged(FIELD_SUSPENSION_FORCE);
        }
    }

    /**
     * Set the amount of error correction that should be performed on a value
     * that has hit the stop. This value should be between 0 and 1.
     * 0 is no correction at all, 1 is full correction in a single step.
     *
     * @param error The amount of error to correct
     * @throws InvalidFieldValueException The value was not between [0,1]
     */
    private void setSuspensionErrorCorrection(float error)
        throws InvalidFieldValueException {

        if(error < 0 || error > 1)
            throw new InvalidFieldValueException(SUSP_ERROR_RANGE_MSG + error);

        vfSuspensionErrorCorrection = error;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setSuspensionERP(error);
            hasChanged[FIELD_SUSPENSION_ERP] = true;
            fireFieldChanged(FIELD_SUSPENSION_ERP);
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
