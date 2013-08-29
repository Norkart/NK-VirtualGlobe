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
import org.odejava.JointAMotor;
import org.odejava.JointGroup;
import org.odejava.World;
import org.odejava.ode.OdeConstants;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the MotorJoint node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public abstract class BaseMotorJoint extends BaseJointNode {

    // Field index constants

    /** The field index for autoCalc */
    protected static final int FIELD_AUTO_CALC = LAST_JOINT_INDEX + 1;

    /** The field index for motor1Axis */
    protected static final int FIELD_MOTOR1_AXIS = LAST_JOINT_INDEX + 2;

    /** The field index for motor2Axis */
    protected static final int FIELD_MOTOR2_AXIS = LAST_JOINT_INDEX + 3;

    /** The field index for motor3Axis */
    protected static final int FIELD_MOTOR3_AXIS = LAST_JOINT_INDEX + 4;

    /** The field index for axis1Angle */
    protected static final int FIELD_AXIS1_ANGLE = LAST_JOINT_INDEX + 5;

    /** The field index for axis2Angle */
    protected static final int FIELD_AXIS2_ANGLE = LAST_JOINT_INDEX + 6;

    /** The field index for axis3Angle */
    protected static final int FIELD_AXIS3_ANGLE = LAST_JOINT_INDEX + 7;

    /** The field index for stop1Bounce */
    protected static final int FIELD_STOP1_BOUNCE = LAST_JOINT_INDEX + 8;

    /** The field index for stop2Bounce */
    protected static final int FIELD_STOP2_BOUNCE = LAST_JOINT_INDEX + 9;

    /** The field index for stop3Bounce */
    protected static final int FIELD_STOP3_BOUNCE = LAST_JOINT_INDEX + 10;

    /** The field index for axis1Torque */
    protected static final int FIELD_AXIS1_TORQUE = LAST_JOINT_INDEX + 11;

    /** The field index for axis2Torque */
    protected static final int FIELD_AXIS2_TORQUE = LAST_JOINT_INDEX + 12;

    /** The field index for axis3Torque */
    protected static final int FIELD_AXIS3_TORQUE = LAST_JOINT_INDEX + 13;

    /** The field index for motor1Angle */
    protected static final int FIELD_MOTOR1_ANGLE = LAST_JOINT_INDEX + 14;

    /** The field index for motor2Angle */
    protected static final int FIELD_MOTOR2_ANGLE = LAST_JOINT_INDEX + 15;

    /** The field index for motor3Angle */
    protected static final int FIELD_MOTOR3_ANGLE = LAST_JOINT_INDEX + 16;

    /** The field index for motor1AngleRate */
    protected static final int FIELD_MOTOR1_ANGLE_RATE = LAST_JOINT_INDEX + 17;

    /** The field index for motor2AngleRate */
    protected static final int FIELD_MOTOR2_ANGLE_RATE = LAST_JOINT_INDEX + 18;

    /** The field index for motor3AngleRate */
    protected static final int FIELD_MOTOR3_ANGLE_RATE = LAST_JOINT_INDEX + 19;

    /** The field index for stop1ErrorCorrection */
    protected static final int FIELD_STOP1_ERROR_CORRECTION = LAST_JOINT_INDEX + 20;

    /** The field index for stop2ErrorCorrection */
    protected static final int FIELD_STOP2_ERROR_CORRECTION = LAST_JOINT_INDEX + 21;

    /** The field index for stop3ErrorCorrection */
    protected static final int FIELD_STOP3_ERROR_CORRECTION = LAST_JOINT_INDEX + 22;

    /** The field index for enabledAxes */
    protected static final int FIELD_ENABLED_AXES = LAST_JOINT_INDEX + 23;

    /** Last index used by this base node */
    protected static final int LAST_MOTOR_INDEX = FIELD_ENABLED_AXES;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_MOTOR_INDEX + 1;

    /** Message when the axis1Angle value is out of range */
    protected static final String AXIS1_ANGLE_MSG =
        "The axis1Angle value is out of the required range [-pi,pi]: ";

    /** Message when the axis2Angle value is out of range */
    protected static final String AXIS2_ANGLE_MSG =
        "The axis2Angle value is out of the required range [-pi,pi]: ";

    /** Message when the axis3Angle value is out of range */
    protected static final String AXIS3_ANGLE_MSG =
        "The axis3Angle value is out of the required range [-pi,pi]: ";

    /** Message when the axis1Torque value is out of range */
    protected static final String AXIS1_TORQUE_MSG =
        "The axis1Torque value is out of the required range [-pi,pi]: ";

    /** Message when the axis2Torque value is out of range */
    protected static final String AXIS2_TORQUE_MSG =
        "The axis2Torque value is out of the required range [-pi,pi]: ";

    /** Message when the axis3Torque value is out of range */
    protected static final String AXIS3_TORQUE_MSG =
        "The axis3Torque value is out of the required range [-pi,pi]: ";

    /** Message when the stopBounce value is out of range */
    protected static final String BOUNCE1_RANGE_MSG =
        "The stop1Bounce value is out of the required range [0,1]: ";

    /** Message when the stop2Bounce value is out of range */
    protected static final String BOUNCE2_RANGE_MSG =
        "The stop2Bounce value is out of the required range [0,1]: ";

    /** Message when the stop3Bounce value is out of range */
    protected static final String BOUNCE3_RANGE_MSG =
        "The stop3Bounce value is out of the required range [0,1]: ";

    /** Message when the stop1ErrorCorrection value is out of range */
    protected static final String STOP_ERROR1_RANGE_MSG =
        "The stop1ErrorCorrection value is out of the required range [0,1]: ";

    /** Message when the stop2ErrorCorrection value is out of range */
    protected static final String STOP_ERROR2_RANGE_MSG =
        "The stop2ErrorCorrection value is out of the required range [0,1]: ";

    /** Message when the stop3ErrorCorrection value is out of range */
    protected static final String STOP_ERROR3_RANGE_MSG =
        "The stop3ErrorCorrection value is out of the required range [0,1]: ";

    /** Message when the user attempt to write to the motor1Angle field */
    private static final String MA1_WRITE_MSG =
        "motor1Angle is outputOnly and cannot be set";

    /** Message when the user attempt to write to the motor2Angle field */
    private static final String MA2_WRITE_MSG =
        "motor2Angle is outputOnly and cannot be set";

    /** Message when the user attempt to write to the motor3Angle field */
    private static final String MA3_WRITE_MSG =
        "motor3Angle is outputOnly and cannot be set";

    /** Message when the user attempt to write to the motor1AngleRate field */
    private static final String MAR1_WRITE_MSG =
        "motor1AngleRate is outputOnly and cannot be set";

    /** Message when the user attempt to write to the motor2AngleRate field */
    private static final String MAR2_WRITE_MSG =
        "motor2AngleRate is outputOnly and cannot be set";

    /** Message when the user attempt to write to the motor3AngleRate field */
    private static final String MAR3_WRITE_MSG =
        "motor3AngleRate is outputOnly and cannot be set";

    /** Message when enabledAxes is outside the range [0,3] */
    private static final String ENABLED_RANGE_MSG =
        "The value of enabledAxes must be between 0 and 3.";

    /** Index list if the output capable fields */
    private static final int[] outputFields;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the autoCalc field */
    protected boolean vfAutoCalc;

    /** The value of the enabledAxes field */
    protected int vfEnabledAxes;

    /** The value of the motor1Axis field */
    protected float[] vfMotor1Axis;

    /** The value of the motor2Axis field */
    protected float[] vfMotor2Axis;

    /** The value of the motor3Axis field */
    protected float[] vfMotor3Axis;

    /** The value of the axis1Angle field */
    protected float vfAxis1Angle;

    /** The value of the axis2Angle field */
    protected float vfAxis2Angle;

    /** The value of the axis3Angle field */
    protected float vfAxis3Angle;

    /** The value of the axis1Torque field */
    protected float vfAxis1Torque;

    /** The value of the axis2Torque field */
    protected float vfAxis2Torque;

    /** The value of the axis3Torque field */
    protected float vfAxis3Torque;

    /** The value of the motor1Angle field */
    protected float vfMotor1Angle;

    /** The value of the motor2Angle field */
    protected float vfMotor2Angle;

    /** The value of the motor3Angle field */
    protected float vfMotor3Angle;

    /** The value of the motor1AngleRate field */
    protected float vfMotor1AngleRate;

    /** The value of the motor2AngleRate field */
    protected float vfMotor2AngleRate;

    /** The value of the motor3AngleRate field */
    protected float vfMotor3AngleRate;

    /** The value of the stop1Bounce field */
    protected float vfStop1Bounce;

    /** The value of the stop2Bounce field */
    protected float vfStop2Bounce;

    /** The value of the stop3Bounce field */
    protected float vfStop3Bounce;

    /** The value of the stop1ErrorCorrection field */
    protected float vfStop1ErrorCorrection;

    /** The value of the stop2ErrorCorrection field */
    protected float vfStop2ErrorCorrection;

    /** The value of the stop3ErrorCorrection field */
    protected float vfStop3ErrorCorrection;

    // Other vars

    /** The ODE representation of the joint */
    protected JointAMotor odeJoint;

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
            FIELD_MOTOR1_ANGLE,
            FIELD_MOTOR2_ANGLE,
            FIELD_MOTOR3_ANGLE,
            FIELD_MOTOR1_ANGLE_RATE,
            FIELD_MOTOR2_ANGLE_RATE,
            FIELD_MOTOR3_ANGLE_RATE,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_AUTO_CALC] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "autoCalc");
        fieldDecl[FIELD_BODY1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body1");
        fieldDecl[FIELD_BODY2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body2");
        fieldDecl[FIELD_MOTOR1_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "motor1Axis");
        fieldDecl[FIELD_MOTOR2_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "motor2Axis");
        fieldDecl[FIELD_MOTOR3_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "motor3Axis");
        fieldDecl[FIELD_AXIS1_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "axis1Angle");
        fieldDecl[FIELD_AXIS2_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "axis2Angle");
        fieldDecl[FIELD_AXIS3_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "axis3Angle");
        fieldDecl[FIELD_AXIS1_TORQUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "axis1Torque");
        fieldDecl[FIELD_AXIS2_TORQUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "axis2Torque");
        fieldDecl[FIELD_AXIS3_TORQUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "axis3Torque");
        fieldDecl[FIELD_STOP1_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop1Bounce");
        fieldDecl[FIELD_STOP2_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop2Bounce");
        fieldDecl[FIELD_STOP3_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop3Bounce");
        fieldDecl[FIELD_STOP1_ERROR_CORRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop1ErrorCorrection");
        fieldDecl[FIELD_STOP2_ERROR_CORRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop2ErrorCorrection");
        fieldDecl[FIELD_STOP3_ERROR_CORRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stop3ErrorCorrection");
        fieldDecl[FIELD_MOTOR1_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "motor1Angle");
        fieldDecl[FIELD_MOTOR2_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "motor2Angle");
        fieldDecl[FIELD_MOTOR3_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "motor3Angle");
        fieldDecl[FIELD_MOTOR1_ANGLE_RATE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "motor1AngleRate");
        fieldDecl[FIELD_MOTOR2_ANGLE_RATE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "motor2AngleRate");
        fieldDecl[FIELD_MOTOR3_ANGLE_RATE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "motor3AngleRate");
        fieldDecl[FIELD_ENABLED_AXES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "enabledAxes");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_AUTO_CALC);
        fieldMap.put("autoCalc", idx);
        fieldMap.put("set_autoCalc", idx);
        fieldMap.put("autoCalc_changed", idx);

        idx = new Integer(FIELD_BODY1);
        fieldMap.put("body1", idx);
        fieldMap.put("set_body1", idx);
        fieldMap.put("body1_changed", idx);

        idx = new Integer(FIELD_BODY2);
        fieldMap.put("body2", idx);
        fieldMap.put("set_body2", idx);
        fieldMap.put("body2_changed", idx);

        idx = new Integer(FIELD_MOTOR1_AXIS);
        fieldMap.put("motor1Axis", idx);
        fieldMap.put("set_motor1Axis", idx);
        fieldMap.put("motor1Axis_changed", idx);

        idx = new Integer(FIELD_MOTOR2_AXIS);
        fieldMap.put("motor2Axis", idx);
        fieldMap.put("set_motor2Axis", idx);
        fieldMap.put("motor2Axis_changed", idx);

        idx = new Integer(FIELD_MOTOR3_AXIS);
        fieldMap.put("motor3Axis", idx);
        fieldMap.put("set_motor3Axis", idx);
        fieldMap.put("motor3Axis_changed", idx);

        idx = new Integer(FIELD_AXIS1_ANGLE);
        fieldMap.put("axis1Angle", idx);
        fieldMap.put("set_axis1Angle", idx);
        fieldMap.put("axis1Angle_changed", idx);

        idx = new Integer(FIELD_AXIS2_ANGLE);
        fieldMap.put("axis2Angle", idx);
        fieldMap.put("set_axis2Angle", idx);
        fieldMap.put("axis2Angle_changed", idx);

        idx = new Integer(FIELD_AXIS3_ANGLE);
        fieldMap.put("axis3Angle", idx);
        fieldMap.put("set_axis3Angle", idx);
        fieldMap.put("axis3Angle_changed", idx);

        idx = new Integer(FIELD_AXIS1_TORQUE);
        fieldMap.put("axis1Torque", idx);
        fieldMap.put("set_axis1Torque", idx);
        fieldMap.put("axis1Torque_changed", idx);

        idx = new Integer(FIELD_AXIS2_TORQUE);
        fieldMap.put("axis2Torque", idx);
        fieldMap.put("set_axis2Torque", idx);
        fieldMap.put("axis2Torque_changed", idx);

        idx = new Integer(FIELD_AXIS3_TORQUE);
        fieldMap.put("axis3Torque", idx);
        fieldMap.put("set_axis3Torque", idx);
        fieldMap.put("axis3Torque_changed", idx);

        idx = new Integer(FIELD_STOP1_BOUNCE);
        fieldMap.put("stop1Bounce", idx);
        fieldMap.put("set_stop1Bounce", idx);
        fieldMap.put("stop1Bounce_changed", idx);

        idx = new Integer(FIELD_STOP2_BOUNCE);
        fieldMap.put("stop2Bounce", idx);
        fieldMap.put("set_stop2Bounce", idx);
        fieldMap.put("stop2Bounce_changed", idx);

        idx = new Integer(FIELD_STOP3_BOUNCE);
        fieldMap.put("stop3Bounce", idx);
        fieldMap.put("set_stop3Bounce", idx);
        fieldMap.put("stop3Bounce_changed", idx);

        idx = new Integer(FIELD_STOP1_ERROR_CORRECTION);
        fieldMap.put("stop1ErrorCorrection", idx);
        fieldMap.put("set_stop1ErrorCorrection", idx);
        fieldMap.put("stop1ErrorCorrection_changed", idx);

        idx = new Integer(FIELD_STOP2_ERROR_CORRECTION);
        fieldMap.put("stop2ErrorCorrection", idx);
        fieldMap.put("set_stop2ErrorCorrection", idx);
        fieldMap.put("stop2ErrorCorrection_changed", idx);

        idx = new Integer(FIELD_STOP3_ERROR_CORRECTION);
        fieldMap.put("stop3ErrorCorrection", idx);
        fieldMap.put("set_stop3ErrorCorrection", idx);
        fieldMap.put("stop3ErrorCorrection_changed", idx);

        idx = new Integer(FIELD_ENABLED_AXES);
        fieldMap.put("enabledAxes", idx);
        fieldMap.put("set_enabledAxes", idx);
        fieldMap.put("enabledAxes_changed", idx);

        fieldMap.put("motor1Angle", new Integer(FIELD_MOTOR1_ANGLE));
        fieldMap.put("motor2Angle", new Integer(FIELD_MOTOR2_ANGLE));
        fieldMap.put("motor3Angle", new Integer(FIELD_MOTOR3_ANGLE));
        fieldMap.put("motor1AngleRate", new Integer(FIELD_MOTOR1_ANGLE_RATE));
        fieldMap.put("motor2AngleRate", new Integer(FIELD_MOTOR2_ANGLE_RATE));
        fieldMap.put("motor3AngleRate", new Integer(FIELD_MOTOR3_ANGLE_RATE));
    }

    /**
     * Construct a new default motor joint node object.
     */
    public BaseMotorJoint() {
        super("MotorJoint");

        vfAutoCalc = false;
        vfEnabledAxes = 1;
        vfMotor1Axis = new float[3];
        vfMotor2Axis = new float[3];
        vfMotor3Axis = new float[3];
        vfAxis1Angle = -(float)Math.PI;
        vfAxis2Angle = -(float)Math.PI;
        vfAxis3Angle = -(float)Math.PI;
        vfAxis1Torque = (float)Math.PI;
        vfAxis2Torque = (float)Math.PI;
        vfAxis3Torque = (float)Math.PI;
        vfStop1Bounce = 0;
        vfStop2Bounce = 0;
        vfStop3Bounce = 0;
        vfStop1ErrorCorrection = 0.8f;
        vfStop2ErrorCorrection = 0.8f;
        vfStop3ErrorCorrection = 0.8f;

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
    public BaseMotorJoint(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("autoCalc");
            VRMLFieldData field = node.getFieldValue(index);
            vfAutoCalc = field.booleanValue;

            index = node.getFieldIndex("motor1Axis");
            field = node.getFieldValue(index);
            vfMotor1Axis[0] = field.floatArrayValue[0];
            vfMotor1Axis[1] = field.floatArrayValue[1];
            vfMotor1Axis[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("motor2Axis");
            field = node.getFieldValue(index);
            vfMotor2Axis[0] = field.floatArrayValue[0];
            vfMotor2Axis[1] = field.floatArrayValue[1];
            vfMotor2Axis[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("motor3Axis");
            field = node.getFieldValue(index);
            vfMotor3Axis[0] = field.floatArrayValue[0];
            vfMotor3Axis[1] = field.floatArrayValue[1];
            vfMotor3Axis[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("axis1Angle");
            field = node.getFieldValue(index);
            vfAxis1Angle = field.floatValue;

            index = node.getFieldIndex("axis2Angle");
            field = node.getFieldValue(index);
            vfAxis2Angle = field.floatValue;

            index = node.getFieldIndex("axis3Angle");
            field = node.getFieldValue(index);
            vfAxis3Angle = field.floatValue;

            index = node.getFieldIndex("axis1Torque");
            field = node.getFieldValue(index);
            vfAxis1Torque = field.floatValue;

            index = node.getFieldIndex("axis3Torque");
            field = node.getFieldValue(index);
            vfAxis3Torque = field.floatValue;

            index = node.getFieldIndex("axis3Torque");
            field = node.getFieldValue(index);
            vfAxis3Torque = field.floatValue;

            index = node.getFieldIndex("stop1Bounce");
            field = node.getFieldValue(index);
            vfStop1Bounce = field.floatValue;

            index = node.getFieldIndex("stop2Bounce");
            field = node.getFieldValue(index);
            vfStop2Bounce = field.floatValue;

            index = node.getFieldIndex("stop3Bounce");
            field = node.getFieldValue(index);
            vfStop3Bounce = field.floatValue;

            index = node.getFieldIndex("stop1ErrorCorrection");
            field = node.getFieldValue(index);
            vfStop1ErrorCorrection = field.floatValue;

            index = node.getFieldIndex("stop2ErrorCorrection");
            field = node.getFieldValue(index);
            vfStop2ErrorCorrection = field.floatValue;

            index = node.getFieldIndex("stop3ErrorCorrection");
            field = node.getFieldValue(index);
            vfStop3ErrorCorrection = field.floatValue;

            index = node.getFieldIndex("enabledAxes");
            field = node.getFieldValue(index);
            vfEnabledAxes = field.intValue;

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
     * @param wld The new world instance to use or null
     * @param grp The group that this joint should belong to
     */
    public void setODEWorld(World wld, JointGroup grp) {
        if(wld != null) {
            odeJoint = new JointAMotor(wld, grp);
            Body body_1 = null;
            Body body_2 = null;

            if(vfBody1 != null)
                body_1 = vfBody1.getODEBody();

            if(vfBody2 != null)
                body_2 = vfBody2.getODEBody();

            odeJoint.attach(body_1, body_2);

            odeJoint.setMode(vfAutoCalc ?
                             OdeConstants.dAMotorEuler :
                             OdeConstants.dAMotorUser);


            // How many axes do we have? If
            odeJoint.setAxis(1,
                             0,
                             vfMotor1Axis[0],
                             vfMotor1Axis[1],
                             vfMotor1Axis[2]);

            odeJoint.setAxis(3,
                             0,
                             vfMotor3Axis[0],
                             vfMotor3Axis[1],
                             vfMotor3Axis[2]);

            // In euler mode, axis 1 is automatically computed, but needs to be
            // supplied in user mode.
            if(!vfAutoCalc) {
                odeJoint.setNumAxes(vfEnabledAxes);
                odeJoint.setAngle(1, vfAxis1Angle);
                odeJoint.setAngle(2, vfAxis2Angle);
                odeJoint.setAngle(3, vfAxis3Angle);

                odeJoint.setAxis(2,
                                 0,
                                 vfMotor2Axis[0],
                                 vfMotor2Axis[1],
                                 vfMotor2Axis[2]);
            }

/*
            odeJoint.setParam(OdeConstants.dParamLoStop, vfMinAngle1);
            odeJoint.setParam(OdeConstants.dParamHiStop, vfMaxAngle1);
            odeJoint.setParam(OdeConstants.dParamLoStop2, vfMinAngle2);
            odeJoint.setParam(OdeConstants.dParamHiStop2, vfMaxAngle2);
            odeJoint.setParam(OdeConstants.dParamLoStop3, vfMinAngle3);
            odeJoint.setParam(OdeConstants.dParamHiStop3, vfMaxAngle3);
*/
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

        if(odeJoint != null)
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

        if(odeJoint != null)
            odeJoint.attach(body_1, body_2);
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

        odeJoint.attach(body_1, body_2);

        odeJoint.setMode(vfAutoCalc ?
                         OdeConstants.dAMotorEuler :
                         OdeConstants.dAMotorUser);


        // How many axes do we have? If
        odeJoint.setAxis(1,
                         0,
                         vfMotor1Axis[0],
                         vfMotor1Axis[1],
                         vfMotor1Axis[2]);

        odeJoint.setAxis(3,
                         0,
                         vfMotor3Axis[0],
                         vfMotor3Axis[1],
                         vfMotor3Axis[2]);

        // In euler mode, axis 1 is automatically computed, but needs to be
        // supplied in user mode.
        if(!vfAutoCalc) {
            odeJoint.setNumAxes(vfEnabledAxes);
            odeJoint.setAngle(1, vfAxis1Angle);
            odeJoint.setAngle(2, vfAxis2Angle);
            odeJoint.setAngle(3, vfAxis3Angle);

            odeJoint.setAxis(2,
                             0,
                             vfMotor2Axis[0],
                             vfMotor2Axis[1],
                             vfMotor2Axis[2]);
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
        if (index < 0  || index > LAST_MOTOR_INDEX)
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
            case FIELD_AUTO_CALC:
                fieldData.clear();
                fieldData.booleanValue = vfAutoCalc;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_MOTOR1_AXIS:
                fieldData.clear();
                fieldData.floatArrayValue = vfMotor1Axis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_MOTOR2_AXIS:
                fieldData.clear();
                fieldData.floatArrayValue = vfMotor2Axis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_MOTOR3_AXIS:
                fieldData.clear();
                fieldData.floatArrayValue = vfMotor3Axis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_AXIS1_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfAxis1Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_AXIS2_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfAxis2Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_AXIS3_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfAxis3Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_AXIS1_TORQUE:
                fieldData.clear();
                fieldData.floatValue = vfAxis1Torque;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_AXIS2_TORQUE:
                fieldData.clear();
                fieldData.floatValue = vfAxis2Torque;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_AXIS3_TORQUE:
                fieldData.clear();
                fieldData.floatValue = vfAxis3Torque;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MOTOR1_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfMotor1Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MOTOR2_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfMotor2Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MOTOR3_ANGLE:
                fieldData.clear();
                fieldData.floatValue = vfMotor3Angle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MOTOR1_ANGLE_RATE:
                fieldData.clear();
                fieldData.floatValue = vfMotor1AngleRate;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MOTOR2_ANGLE_RATE:
                fieldData.clear();
                fieldData.floatValue = vfMotor2AngleRate;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MOTOR3_ANGLE_RATE:
                fieldData.clear();
                fieldData.floatValue = vfMotor3AngleRate;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
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

            case FIELD_STOP3_BOUNCE:
                fieldData.clear();
                fieldData.floatValue = vfStop3Bounce;
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

            case FIELD_STOP3_ERROR_CORRECTION:
                fieldData.clear();
                fieldData.floatValue = vfStop3ErrorCorrection;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_ENABLED_AXES:
                fieldData.clear();
                fieldData.intValue = vfEnabledAxes;
                fieldData.dataType = VRMLFieldData.INT_DATA;
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
                case FIELD_AUTO_CALC:
                    destNode.setValue(destIndex, vfAutoCalc);
                    break;

                case FIELD_MOTOR1_AXIS:
                    destNode.setValue(destIndex, vfMotor1Axis, 3);
                    break;

                case FIELD_MOTOR2_AXIS:
                    destNode.setValue(destIndex, vfMotor2Axis, 3);
                    break;

                case FIELD_MOTOR3_AXIS:
                    destNode.setValue(destIndex, vfMotor3Axis, 3);
                    break;

                case FIELD_AXIS1_ANGLE:
                    destNode.setValue(destIndex, vfAxis1Angle);
                    break;

                case FIELD_AXIS2_ANGLE:
                    destNode.setValue(destIndex, vfAxis2Angle);
                    break;

                case FIELD_AXIS3_ANGLE:
                    destNode.setValue(destIndex, vfAxis3Angle);
                    break;

                case FIELD_AXIS1_TORQUE:
                    destNode.setValue(destIndex, vfAxis1Torque);
                    break;

                case FIELD_AXIS2_TORQUE:
                    destNode.setValue(destIndex, vfAxis2Torque);
                    break;

                case FIELD_AXIS3_TORQUE:
                    destNode.setValue(destIndex, vfAxis3Torque);
                    break;

                case FIELD_MOTOR1_ANGLE:
                    destNode.setValue(destIndex, vfMotor1Angle);
                    break;

                case FIELD_MOTOR2_ANGLE:
                    destNode.setValue(destIndex, vfMotor2Angle);
                    break;

                case FIELD_MOTOR3_ANGLE:
                    destNode.setValue(destIndex, vfMotor3Angle);
                    break;

                case FIELD_MOTOR1_ANGLE_RATE:
                    destNode.setValue(destIndex, vfMotor1AngleRate);
                    break;

                case FIELD_MOTOR2_ANGLE_RATE:
                    destNode.setValue(destIndex, vfMotor2AngleRate);
                    break;

                case FIELD_MOTOR3_ANGLE_RATE:
                    destNode.setValue(destIndex, vfMotor3AngleRate);
                    break;

                case FIELD_STOP1_BOUNCE:
                    destNode.setValue(destIndex, vfStop1Bounce);
                    break;

                case FIELD_STOP2_BOUNCE:
                    destNode.setValue(destIndex, vfStop2Bounce);
                    break;

                case FIELD_STOP3_BOUNCE:
                    destNode.setValue(destIndex, vfStop3Bounce);
                    break;

                case FIELD_STOP1_ERROR_CORRECTION:
                    destNode.setValue(destIndex, vfStop1ErrorCorrection);
                    break;

                case FIELD_STOP2_ERROR_CORRECTION:
                    destNode.setValue(destIndex, vfStop2ErrorCorrection);
                    break;

                case FIELD_STOP3_ERROR_CORRECTION:
                    destNode.setValue(destIndex, vfStop3ErrorCorrection);
                    break;

                case FIELD_ENABLED_AXES:
                    destNode.setValue(destIndex, vfEnabledAxes);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("Single1AxisJoint.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("Single1AxisJoint.sendRoute: Invalid field value: " +
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
    public void setValue(int index, boolean value)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_AUTO_CALC:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "autoCalc");
                else
                    vfAutoCalc = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a single float.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int value)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_ENABLED_AXES:
                setEnabledAxes(value);
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
    public void setValue(int index, float value)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_AXIS1_ANGLE:
                set1AxisAngle(value);
                break;

            case FIELD_AXIS2_ANGLE:
                set2AxisAngle(value);
                break;

            case FIELD_AXIS3_ANGLE:
                set3AxisAngle(value);
                break;

            case FIELD_AXIS1_TORQUE:
                setMotor1Torque(value);
                break;

            case FIELD_AXIS2_TORQUE:
                setMotor2Torque(value);
                break;

            case FIELD_AXIS3_TORQUE:
                setMotor3Torque(value);
                break;

            case FIELD_STOP1_BOUNCE:
                setStop1Bounce(value);
                break;

            case FIELD_STOP2_BOUNCE:
                setStop2Bounce(value);
                break;

            case FIELD_STOP3_BOUNCE:
                setStop3Bounce(value);
                break;

            case FIELD_STOP1_ERROR_CORRECTION:
                setStop1ErrorCorrection(value);
                break;

            case FIELD_STOP2_ERROR_CORRECTION:
                setStop2ErrorCorrection(value);
                break;

            case FIELD_STOP3_ERROR_CORRECTION:
                setStop3ErrorCorrection(value);
                break;

            case FIELD_MOTOR1_ANGLE:
                throw new InvalidFieldAccessException(MA1_WRITE_MSG);

            case FIELD_MOTOR2_ANGLE:
                throw new InvalidFieldAccessException(MA2_WRITE_MSG);

            case FIELD_MOTOR3_ANGLE:
                throw new InvalidFieldAccessException(MA3_WRITE_MSG);

            case FIELD_MOTOR1_ANGLE_RATE:
                throw new InvalidFieldAccessException(MAR1_WRITE_MSG);

            case FIELD_MOTOR2_ANGLE_RATE:
                throw new InvalidFieldAccessException(MAR2_WRITE_MSG);

            case FIELD_MOTOR3_ANGLE_RATE:
                throw new InvalidFieldAccessException(MAR3_WRITE_MSG);

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
            case FIELD_MOTOR1_AXIS:
                setMotor1Axis(value);
                break;

            case FIELD_MOTOR2_AXIS:
                setMotor2Axis(value);
                break;

            case FIELD_MOTOR3_AXIS:
                setMotor3Axis(value);
                break;

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
    private void set1AxisAngle(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(AXIS1_ANGLE_MSG + angle);

        vfAxis1Angle = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamLoStop, angle);
            hasChanged[FIELD_AXIS1_ANGLE] = true;
            fireFieldChanged(FIELD_AXIS1_ANGLE);
        }
    }

    /**
     * Set the minimum angle stop position. This value should be between -PI
     * and +PI.
     *
     * @param angle The amount of angle to use
     * @throws InvalidFieldValueException The value was not between [-PI,PI]
     */
    private void set2AxisAngle(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(AXIS2_ANGLE_MSG + angle);

        vfAxis2Angle = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamLoStop2, angle);
            hasChanged[FIELD_AXIS2_ANGLE] = true;
            fireFieldChanged(FIELD_AXIS2_ANGLE);
        }
    }

    /**
     * Set the minimum angle stop position. This value should be between -PI
     * and +PI.
     *
     * @param angle The amount of angle to use
     * @throws InvalidFieldValueException The value was not between [-PI,PI]
     */
    private void set3AxisAngle(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(AXIS3_ANGLE_MSG + angle);

        vfAxis3Angle = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamLoStop3, angle);
            hasChanged[FIELD_AXIS3_ANGLE] = true;
            fireFieldChanged(FIELD_AXIS3_ANGLE);
        }
    }

    /**
     * Set the maximum angle stop position. This value should be between -PI
     * and +PI.
     *
     * @param angle The amount of angle to use
     * @throws InvalidFieldValueException The value was not between [-PI,PI]
     */
    private void setMotor1Torque(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(AXIS1_ANGLE_MSG + angle);

        vfAxis1Torque = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamHiStop, angle);
            hasChanged[FIELD_AXIS1_TORQUE] = true;
            fireFieldChanged(FIELD_AXIS1_TORQUE);
        }
    }

    /**
     * Set the maximum angle stop position. This value should be between -PI
     * and +PI.
     *
     * @param angle The amount of angle to use
     * @throws InvalidFieldValueException The value was not between [-PI,PI]
     */
    private void setMotor2Torque(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(AXIS2_ANGLE_MSG + angle);

        vfAxis2Torque = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamHiStop2, angle);
            hasChanged[FIELD_AXIS2_TORQUE] = true;
            fireFieldChanged(FIELD_AXIS2_TORQUE);
        }
    }

    /**
     * Set the maximum angle stop position. This value should be between -PI
     * and +PI.
     *
     * @param angle The amount of angle to use
     * @throws InvalidFieldValueException The value was not between [-PI,PI]
     */
    private void setMotor3Torque(float angle)
        throws InvalidFieldValueException {

        if(angle < -(float)Math.PI || angle > (float)Math.PI)
            throw new InvalidFieldValueException(AXIS3_ANGLE_MSG + angle);

        vfAxis3Torque = angle;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamHiStop3, angle);
            hasChanged[FIELD_AXIS3_TORQUE] = true;
            fireFieldChanged(FIELD_AXIS3_TORQUE);
        }
    }

    /**
     * Set the axis vector for this motor.
     *
     * @param axis An array of 3 values for the vector
     */
    private void setMotor1Axis(float[] axis) {
        vfMotor1Axis[0] = axis[0];
        vfMotor1Axis[1] = axis[1];
        vfMotor1Axis[2] = axis[2];

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setAxis(1, 0, axis[0], axis[1], axis[2]);
            hasChanged[FIELD_MOTOR1_AXIS] = true;
            fireFieldChanged(FIELD_MOTOR1_AXIS);
        }
    }

    /**
     * Set the axis vector for this motor.
     *
     * @param axis An array of 3 values for the vector
     */
    private void setMotor2Axis(float[] axis) {
        vfMotor2Axis[0] = axis[0];
        vfMotor2Axis[1] = axis[1];
        vfMotor2Axis[2] = axis[2];

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setAxis(2, 0, axis[0], axis[1], axis[2]);
            hasChanged[FIELD_MOTOR2_AXIS] = true;
            fireFieldChanged(FIELD_MOTOR2_AXIS);
        }
    }

    /**
     * Set the axis vector for this motor.
     *
     * @param axis An array of 3 values for the vector
     */
    private void setMotor3Axis(float[] axis) {
        vfMotor3Axis[0] = axis[0];
        vfMotor3Axis[1] = axis[1];
        vfMotor3Axis[3] = axis[3];

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setAxis(3, 0, axis[0], axis[1], axis[2]);
            hasChanged[FIELD_MOTOR3_AXIS] = true;
            fireFieldChanged(FIELD_MOTOR3_AXIS);
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
            if(odeJoint != null)
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
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamBounce2, bounce);
            hasChanged[FIELD_STOP2_BOUNCE] = true;
            fireFieldChanged(FIELD_STOP2_BOUNCE);
        }
    }

    /**
     * Set the amount of stop bounce. This value should be between 0 and 1.
     * 0 is no bounce at all, 1 is full bounce.
     *
     * @param bounce The amount of bounce to use
     * @throws InvalidFieldValueException The value was not between [0,1]
     */
    private void setStop3Bounce(float bounce)
        throws InvalidFieldValueException {

        if(bounce < 0 || bounce > 1)
            throw new InvalidFieldValueException(BOUNCE3_RANGE_MSG + bounce);

        vfStop3Bounce = bounce;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamBounce3, bounce);
            hasChanged[FIELD_STOP3_BOUNCE] = true;
            fireFieldChanged(FIELD_STOP3_BOUNCE);
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
            if(odeJoint != null)
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
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamStopERP2, error);
            hasChanged[FIELD_STOP2_ERROR_CORRECTION] = true;
            fireFieldChanged(FIELD_STOP2_ERROR_CORRECTION);
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
    private void setStop3ErrorCorrection(float error)
        throws InvalidFieldValueException {

        if(error < 0 || error > 1)
            throw new InvalidFieldValueException(STOP_ERROR3_RANGE_MSG + error);

        vfStop3ErrorCorrection = error;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setParam(OdeConstants.dParamStopERP3, error);
            hasChanged[FIELD_STOP3_ERROR_CORRECTION] = true;
            fireFieldChanged(FIELD_STOP3_ERROR_CORRECTION);
        }
    }

    /**
     * Set the number of axes that should be enabled
     *
     * @param num The number of axes to use
     * @throws InvalidFieldValueException The value was not between [0,3]
     */
    private void setEnabledAxes(int num)
        throws InvalidFieldValueException {

        if(num < 0 || num > 3)
            throw new InvalidFieldValueException(ENABLED_RANGE_MSG + num);

        vfEnabledAxes = num;

        if(!inSetup) {
            if(odeJoint != null)
                odeJoint.setNumAxes(num);
            hasChanged[FIELD_ENABLED_AXES] = true;
            fireFieldChanged(FIELD_ENABLED_AXES);
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
