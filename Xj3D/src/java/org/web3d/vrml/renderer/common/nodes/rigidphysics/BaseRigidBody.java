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

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Quat4f;

import org.odejava.Body;
import org.odejava.PlaceableGeom;
import org.odejava.World;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the RigidBody node.
 * <p>
 *
 * In this implementation, the sets of nodes for the bodies and geometry are
 * not expected to change much, if at all. As such, arrays are used to
 * represent the field values, rather than the more customary ArrayList that
 * other classes use for MFNode fields
 *
 * The X3D definition of RigidBody is:
 * <pre>
 * RigidBody : X3DNode {
 *   SFVec3f    [in,out] angularVelocity     0 0 0
 *   SFBool     [in,out] autoDisable         FALSE
 *   SFVec3f    [in,out] centerOfMass        0 0 0
 *   SFFloat    [in,out] disableAngularSpeed 0      [0,&#8734;)
 *   SFFloat    [in,out] disableLinearSpeed  0      [0,&#8734;)
 *   SFFloat    [in,out] disableTime         0      [0,&#8734;)
 *   SFBool     [in,out] enabled             TRUE
 *   SFVec3f    [in,out] finiteRotationAxis  0 0 0
 *   SFBool     [in,out] fixed               FALSE
 *   MFVec3f    [in,out] forces              []
 *   MFNode     [in,out] geometry            []      [X3DNBodyCollidableNode]
 *   SFMatrix3f [in,out] inertia             1 0 0  0 1 0  0 0 1
 *   SFVec3f    [in,out] linearVelocity      0 0 0
 *   SFFloat    [in,out] mass                1       (0,&#8734;)
 *   SFNode     [in,out] massDensityModel    NULL    [Sphere, Box, Cone]
 *   SFNode     [in,out] metadata            NULL    [X3DMetadataObject]
 *   SFRotation [in,out] orientation         0 0 1 0 [0,1]
 *   SFVec3f    [in,out] position            0 0 0   (-&#8734;,&#8734;)
 *   MFVec3f    [in,out] torques             []
 *   SFBool     [in,out] useFiniteRotation   FALSE
 *   SFBool     [in,out] useGlobalGravity    TRUE
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseRigidBody extends AbstractNode
    implements VRMLRigidBodyNodeType {

    // Field index constants

    /** The field index for angularDampingFactor */
    protected static final int FIELD_ANGULAR_DAMPING_FACTOR = LAST_NODE_INDEX + 1;

    /** The field index for angularVelocity */
    protected static final int FIELD_ANGULAR_VELOCITY = LAST_NODE_INDEX + 2;

    /** The field index for autoDamp */
    protected static final int FIELD_AUTO_DAMP = LAST_NODE_INDEX + 3;

    /** The field index for autoDisable */
    protected static final int FIELD_AUTO_DISABLE = LAST_NODE_INDEX + 4;

    /** The field index for centerOfMass */
    protected static final int FIELD_CENTER_OF_MASS = LAST_NODE_INDEX + 5;

    /** The field index for disableTime */
    protected static final int FIELD_DISABLE_TIME = LAST_NODE_INDEX + 6;

    /** The field index for disableAngularSpeed    */
    protected static final int FIELD_DISABLE_ANGULAR_SPEED = LAST_NODE_INDEX + 7;

    /** The field index for disableLinearSpeed    */
    protected static final int FIELD_DISABLE_LINEAR_SPEED = LAST_NODE_INDEX + 8;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 9;

    /** The field index for finiteRotationAxis */
    protected static final int FIELD_FINITE_ROTATION_AXIS = LAST_NODE_INDEX + 10;

    /** The field index for forces */
    protected static final int FIELD_FORCES = LAST_NODE_INDEX + 11;

    /** The field index for geometry */
    protected static final int FIELD_GEOMETRY = LAST_NODE_INDEX + 12;

    /** The field index for inertia */
    protected static final int FIELD_INERTIA = LAST_NODE_INDEX + 13;

    /** The field index for linearDampingFactor */
    protected static final int FIELD_LINEAR_DAMPING_FACTOR = LAST_NODE_INDEX + 14;

    /** The field index for linearVelocity */
    protected static final int FIELD_LINEAR_VELOCITY = LAST_NODE_INDEX + 15;

    /** The field index for mass */
    protected static final int FIELD_MASS = LAST_NODE_INDEX + 16;

    /** The field index for massDensityModel */
    protected static final int FIELD_MASS_DENSITY_MODEL = LAST_NODE_INDEX + 17;

    /** The field index for orientation */
    protected static final int FIELD_ORIENTATION = LAST_NODE_INDEX + 18;

    /** The field index for position */
    protected static final int FIELD_POSITION = LAST_NODE_INDEX + 19;

    /** The field index for torques */
    protected static final int FIELD_TORQUES = LAST_NODE_INDEX + 20;

    /** The field index for useFiniteRotation */
    protected static final int FIELD_USE_FINITE_ROTATION = LAST_NODE_INDEX + 21;

    /** The field index for useGlobalGravity */
    protected static final int FIELD_USE_GLOBAL_GRAVITY = LAST_NODE_INDEX + 22;

    /** Last index used by this rigid body node */
    protected static final int LAST_BODY_INDEX = FIELD_USE_GLOBAL_GRAVITY;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_BODY_INDEX + 1;

    /** Message for when the proto for mass density is not a primitive */
    protected static final String MASS_TYPE_PROTO_MSG =
        "Proto does not describe a valid mass density object. Permitted " +
        "values are Sphere, Box and Cone";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String MASS_PROTO_MSG =
        "MassDensityModel proto does not describe a X3DGeometryNode object.";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String MASS_NODE_MSG =
        "MassDensityModel node does not describe a X3DGeometryNode object.";

    /** Message for when the proto for mass density is not a primitive */
    protected static final String MASS_TYPE_MSG =
        "MassDensityModel describes a geometry node, but it is not one of the" +
        " allowable types. Permitted values are Sphere, Box and Cone";

    protected static final String GEOM_NODE_MSG =
        "An invalid node type has been specified for the geometry. Valid " +
        "types extend X3DCollidableNode";

    protected static final String GEOM_PROTO_MSG =
        "An invalid node type has been specified for the geometry. Valid " +
        "types extend X3DCollidableNode";

    /** Message when the stopBounce value is out of range */
    protected static final String DIS_ANG_VEL_RANGE_MSG =
        "The disableAngularSpeed value is out of the required range [0,inf): ";

    /** Message when the stopBounce value is out of range */
    protected static final String DIS_LIN_VEL_RANGE_MSG =
        "The disableLinearSpeed value is out of the required range [0,inf): ";

    /** When the mass value is non-positive, provide this value */
    protected static final String NEG_MASS_MSG =
        "The mass value is negative or zero. Mass must be positive.";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** Valid primitive node types for the mass density model */
    private static HashSet validDensityTypes;

    // The VRML field values

    /** The value of the angularDampingFactor field */
    protected float vfAngularDampingFactor;

    /** The value of the angularVelocity field */
    protected float[] vfAngularVelocity;

    /** The value of the autoDamp field */
    protected boolean vfAutoDamp;

    /** The value of the autoDisable field */
    protected boolean vfAutoDisable;

    /** The value of the centerOfMass field */
    protected float[] vfCenterOfMass;

    /** The value of the disableTime field */
    protected float vfDisableTime;

    /** The value of the disableAngularSpeed field */
    protected float vfDisableAngularSpeed;

    /** The value of the disableLinearSpeed field */
    protected float vfDisableLinearSpeed;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** The value of the finiteRotationAxis field */
    protected float[] vfFiniteRotationAxis;

    /** The value of the forces field */
    protected float[] vfForces;

    /** The number of valid values in vfForces */
    protected int numForces;

    /** The value of the geometry field */
    protected VRMLNodeType[] vfGeometry;

    /** The number of valid values in vfGeometry */
    protected int numGeometry;

    /** The value of the inertia matrix  field */
    protected float[] vfInertia;

    /** The number of valid values in the inertia matrix */
    protected int numInertia;

    /** The value of the linearDampingFactor field */
    protected float vfLinearDampingFactor;

    /** The value of the linearVelocity field */
    protected float[] vfLinearVelocity;

    /** The value of the mass field */
    protected float vfMass;

    /** The value of the massDensityModel field */
    protected VRMLGeometryNodeType vfMassDensityModel;

    /** The proto version of vfMassDensityModel */
    protected VRMLProtoInstance pMassDensityModel;

    /** The value of the orientation field */
    protected float[] vfOrientation;

    /** The value of the position field */
    protected float[] vfPosition;

    /** The value of the torques field */
    protected float[] vfTorques;

    /** The number of valid values in vfTorques */
    protected int numTorques;

    /** The value of the useFiniteRotation field */
    protected boolean vfUseFiniteRotation;

    /** The value of the useGlobalGravity field */
    protected boolean vfUseGlobalGravity;

    // Other variables

    /** The ODE odeBody that this node wraps */
    private Body odeBody;

    /** The parent ODE world instance containing the odeBody */
    private World odeWorld;

    /** Helper to fetch the body position from ODE */
    private Vector3f positionTmp;

    /** Helper to fetch the body orientation from ODE */
    private AxisAngle4f angleTmp;

    /** Helper to fetch the body orientation from ODE */
    private Quat4f orientTmp;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_GEOMETRY,
            FIELD_MASS_DENSITY_MODEL
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ANGULAR_DAMPING_FACTOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "angularDampingFactor");
        fieldDecl[FIELD_ANGULAR_VELOCITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "angularVelocity");
        fieldDecl[FIELD_AUTO_DAMP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "autoDamp");
        fieldDecl[FIELD_AUTO_DISABLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "autoDisable");
        fieldDecl[FIELD_CENTER_OF_MASS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "centerOfMass");
        fieldDecl[FIELD_DISABLE_ANGULAR_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "disableAngularSpeed");
        fieldDecl[FIELD_DISABLE_LINEAR_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "disableLinearSpeed");
        fieldDecl[FIELD_DISABLE_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "disableTime");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_FINITE_ROTATION_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "finiteRotationAxis");
        fieldDecl[FIELD_FORCES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFVec3f",
                                     "forces");
        fieldDecl[FIELD_GEOMETRY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "geometry");
        fieldDecl[FIELD_INERTIA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFMatrix3f",
                                     "inertia");
        fieldDecl[FIELD_LINEAR_DAMPING_FACTOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "lineaarDampingFactor");
        fieldDecl[FIELD_LINEAR_VELOCITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "linearVelocity");
        fieldDecl[FIELD_MASS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "mass");
        fieldDecl[FIELD_MASS_DENSITY_MODEL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "massDensityModel");
        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFRotation",
                                     "orientation");
        fieldDecl[FIELD_POSITION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "position");
        fieldDecl[FIELD_TORQUES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFVec3f",
                                     "torques");
        fieldDecl[FIELD_USE_FINITE_ROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "useFiniteRotation");
        fieldDecl[FIELD_USE_GLOBAL_GRAVITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "useGlobalGravity");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ANGULAR_VELOCITY);
        fieldMap.put("angularVelocity", idx);
        fieldMap.put("set_angularVelocity", idx);
        fieldMap.put("angularVelocity_changed", idx);

        idx = new Integer(FIELD_ANGULAR_DAMPING_FACTOR);
        fieldMap.put("angularDampingFactor", idx);
        fieldMap.put("set_angularDampingFactor", idx);
        fieldMap.put("angularDampingFactor_changed", idx);

        idx = new Integer(FIELD_AUTO_DAMP);
        fieldMap.put("autoDamp", idx);
        fieldMap.put("set_autoDamp", idx);
        fieldMap.put("autoDamp_changed", idx);

        idx = new Integer(FIELD_AUTO_DISABLE);
        fieldMap.put("autoDisable", idx);
        fieldMap.put("set_autoDisable", idx);
        fieldMap.put("autoDisable_changed", idx);

        idx = new Integer(FIELD_CENTER_OF_MASS);
        fieldMap.put("centerOfMass", idx);
        fieldMap.put("set_centerOfMass", idx);
        fieldMap.put("centerOfMass_changed", idx);

        idx = new Integer(FIELD_DISABLE_ANGULAR_SPEED);
        fieldMap.put("disableAngularSpeed", idx);
        fieldMap.put("set_disableAngularSpeed", idx);
        fieldMap.put("disableAngularSpeed_changed", idx);

        idx = new Integer(FIELD_DISABLE_LINEAR_SPEED);
        fieldMap.put("disableLinearSpeed", idx);
        fieldMap.put("set_disableLinearSpeed", idx);
        fieldMap.put("disableLinearSpeed_changed", idx);

        idx = new Integer(FIELD_DISABLE_TIME);
        fieldMap.put("disableTime", idx);
        fieldMap.put("set_disableTime", idx);
        fieldMap.put("disableTime_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_FINITE_ROTATION_AXIS);
        fieldMap.put("finiteRotationAxis", idx);
        fieldMap.put("set_finiteRotationAxis", idx);
        fieldMap.put("finiteRotationAxis_changed", idx);

        idx = new Integer(FIELD_FORCES);
        fieldMap.put("forces", idx);
        fieldMap.put("set_forces", idx);
        fieldMap.put("forces_changed", idx);

        idx = new Integer(FIELD_GEOMETRY);
        fieldMap.put("geometry", idx);
        fieldMap.put("set_geometry", idx);
        fieldMap.put("geometry_changed", idx);

        idx = new Integer(FIELD_INERTIA);
        fieldMap.put("inertia", idx);
        fieldMap.put("set_inertia", idx);
        fieldMap.put("inertia_changed", idx);

        idx = new Integer(FIELD_LINEAR_VELOCITY);
        fieldMap.put("linearVelocity", idx);
        fieldMap.put("set_linearVelocity", idx);
        fieldMap.put("linearVelocity_changed", idx);

        idx = new Integer(FIELD_LINEAR_DAMPING_FACTOR);
        fieldMap.put("linearDampingFactor", idx);
        fieldMap.put("set_linearDampingFactor", idx);
        fieldMap.put("linearDampingFactor_changed", idx);

        idx = new Integer(FIELD_MASS);
        fieldMap.put("mass", idx);
        fieldMap.put("set_mass", idx);
        fieldMap.put("mass_changed", idx);

        idx = new Integer(FIELD_MASS_DENSITY_MODEL);
        fieldMap.put("massDensityModel", idx);
        fieldMap.put("set_massDensityModel", idx);
        fieldMap.put("massDensityModel_changed", idx);

        idx = new Integer(FIELD_ORIENTATION);
        fieldMap.put("orientation", idx);
        fieldMap.put("set_orientation", idx);
        fieldMap.put("orientation_changed", idx);

        idx = new Integer(FIELD_POSITION);
        fieldMap.put("position", idx);
        fieldMap.put("set_position", idx);
        fieldMap.put("position_changed", idx);

        idx = new Integer(FIELD_TORQUES);
        fieldMap.put("torques", idx);
        fieldMap.put("set_torques", idx);
        fieldMap.put("torques_changed", idx);

        idx = new Integer(FIELD_USE_FINITE_ROTATION);
        fieldMap.put("useFiniteRotation", idx);
        fieldMap.put("set_useFiniteRotation", idx);
        fieldMap.put("useFiniteRotation_changed", idx);

        idx = new Integer(FIELD_USE_GLOBAL_GRAVITY);
        fieldMap.put("useGlobalGravity", idx);
        fieldMap.put("set_useGlobalGravity", idx);
        fieldMap.put("useGlobalGravity_changed", idx);

        validDensityTypes = new HashSet();
        validDensityTypes.add("Cone");
        validDensityTypes.add("Box");
        validDensityTypes.add("Sphere");
    }

    /**
     * Construct a new default RigidBody node object.
     */
    public BaseRigidBody() {
        super("RigidBody");

        vfAngularVelocity = new float[3];
        vfAngularDampingFactor = 0.001f;
        vfAutoDamp = false;
        vfAutoDisable = false;
        vfCenterOfMass = new float[3];
        vfDisableAngularSpeed = 0;
        vfDisableLinearSpeed = 0;
        vfDisableTime = 0;
        vfEnabled = true;
        vfFiniteRotationAxis = new float[3];
        vfForces = FieldConstants.EMPTY_MFVEC3F;
        vfInertia = new float[9];
        vfInertia[0] = 1;
        vfInertia[4] = 1;
        vfInertia[8] = 1;
        numInertia = 6;
        vfLinearVelocity = new float[3];
        vfLinearDampingFactor = 0.001f;
        vfMass = 1;
        vfOrientation = new float[] { 0, 0, 1, 0 };
        vfPosition = new float[3];
        vfTorques = FieldConstants.EMPTY_MFVEC3F;
        vfUseFiniteRotation = false;
        vfUseGlobalGravity = true;

        hasChanged = new boolean[NUM_FIELDS];

        orientTmp = new Quat4f();
        angleTmp = new AxisAngle4f();
        positionTmp = new Vector3f();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseRigidBody(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("angularVelocity");
            VRMLFieldData field = node.getFieldValue(index);
            vfAngularVelocity[0] = field.floatArrayValue[0];
            vfAngularVelocity[1] = field.floatArrayValue[1];
            vfAngularVelocity[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("angularDampingFactor");
            field = node.getFieldValue(index);
            vfAngularDampingFactor = field.floatValue;

            index = node.getFieldIndex("autoDamp");
            field = node.getFieldValue(index);
            vfAutoDamp = field.booleanValue;

            index = node.getFieldIndex("autoDisable");
            field = node.getFieldValue(index);
            vfAutoDisable = field.booleanValue;

            index = node.getFieldIndex("centerOfMass");
            field = node.getFieldValue(index);
            vfCenterOfMass[0] = field.floatArrayValue[0];
            vfCenterOfMass[1] = field.floatArrayValue[1];
            vfCenterOfMass[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("disableAngularSpeed   ");
            field = node.getFieldValue(index);
            vfDisableAngularSpeed = field.floatValue;

            index = node.getFieldIndex("disableLinearSpeed   ");
            field = node.getFieldValue(index);
            vfDisableLinearSpeed = field.floatValue;

            index = node.getFieldIndex("disableTime");
            field = node.getFieldValue(index);
            vfDisableTime = field.floatValue;

            index = node.getFieldIndex("enabled");
            field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("finiteRotationAxis");
            field = node.getFieldValue(index);
            vfFiniteRotationAxis[0] = field.floatArrayValue[0];
            vfFiniteRotationAxis[1] = field.floatArrayValue[1];
            vfFiniteRotationAxis[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("forces");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfForces = new float[field.numElements * 3];
                numForces = field.numElements * 3;
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfForces,
                                 0,
                                 field.numElements * 3);
            }

            index = node.getFieldIndex("inertia");
            field = node.getFieldValue(index);
            System.arraycopy(field.floatArrayValue,
                             0,
                             vfInertia,
                             0,
                             9);

            index = node.getFieldIndex("linearDampingFactor");
            field = node.getFieldValue(index);
            vfLinearDampingFactor = field.floatValue;

            index = node.getFieldIndex("linearVelocity");
            field = node.getFieldValue(index);
            vfLinearVelocity[0] = field.floatArrayValue[0];
            vfLinearVelocity[1] = field.floatArrayValue[1];
            vfLinearVelocity[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("mass");
            field = node.getFieldValue(index);
            vfMass = field.floatValue;

            index = node.getFieldIndex("orientation");
            field = node.getFieldValue(index);
            vfOrientation[0] = field.floatArrayValue[0];
            vfOrientation[1] = field.floatArrayValue[1];
            vfOrientation[2] = field.floatArrayValue[2];
            vfOrientation[3] = field.floatArrayValue[3];

            index = node.getFieldIndex("position");
            field = node.getFieldValue(index);
            vfPosition[0] = field.floatArrayValue[0];
            vfPosition[1] = field.floatArrayValue[1];
            vfPosition[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("torques");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfTorques = new float[field.numElements * 3];
                numTorques = field.numElements * 3;
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfTorques,
                                 0,
                                 field.numElements * 3);
            }

            index = node.getFieldIndex("useFiniteRotation");
            field = node.getFieldValue(index);
            vfUseFiniteRotation = field.booleanValue;

            index = node.getFieldIndex("useGlobalGravity");
            field = node.getFieldValue(index);
            vfUseGlobalGravity = field.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLRigidBodyNodeType
    //----------------------------------------------------------

    /**
     * Set the parent world that this odeBody belongs to. A null value clears
     * the world and indicates the physics model is no longer in use.
     *
     * @param wld The new world instance to use or null
     */
    public void setODEWorld(World wld) {
        if(wld != null) {
            odeBody = new Body(wld);

            // Setup the basics of the body before creating the joints
            odeBody.setPosition(vfPosition[0],
                                vfPosition[1],
                                vfPosition[2]);
            odeBody.setAxisAndAngle(vfOrientation[0],
                                    vfOrientation[1],
                                    vfOrientation[2],
                                    vfOrientation[3]);
            odeBody.setLinearVel(vfLinearVelocity[0],
                                 vfLinearVelocity[1],
                                 vfLinearVelocity[2]);
            odeBody.setAngularVel(vfAngularVelocity[0],
                                  vfAngularVelocity[1],
                                  vfAngularVelocity[2]);
            odeBody.setFiniteRotationAxis(vfFiniteRotationAxis[0],
                                          vfFiniteRotationAxis[1],
                                          vfFiniteRotationAxis[2]);
            odeBody.setMassParameters(vfMass,
                                      vfCenterOfMass[0], vfCenterOfMass[1],
                                      vfCenterOfMass[2],
                                      vfInertia[0], vfInertia[4], vfInertia[8],
                                      vfInertia[1], vfInertia[2], vfInertia[5]);

            odeBody.setFiniteRotationMode(vfUseFiniteRotation ? 1 : 0);
            odeBody.setGravityMode(vfUseGlobalGravity ? 1 : 0);
            odeBody.setAngularVelocityDisableThreshold(vfDisableAngularSpeed);
            odeBody.setLinearVelocityDisableThreshold(vfDisableLinearSpeed);
            odeBody.setTimeDisableThreshold(vfDisableTime);
            odeBody.setAutoDisable(vfAutoDisable);
            odeBody.adjustMass(vfMass);
        }

        // there's no delete() call on body. We have to assume that the
        // containing world object is going to do that for us.
        odeWorld = wld;

    }

    /**
     * Get the ODE object that represents the odeBody to evaluate.
     *
     * @return The odeBody object representing this node
     */
    public Body getODEBody() {
        return odeBody;
    }

    /**
     * Update any pre-evaluation values for the body. For example, forces
     * applied to the body need to be reset every frame as ODE will zero out
     * force and torque values after every frame.
     */
    public void updateODEFromNode() {
        int force_count = numForces / 3;
        for(int i = 0; i < force_count; i++)
            odeBody.addForce(vfForces[i * 3],
                             vfForces[i * 3 + 1],
                             vfForces[i * 3 + 2]);

        force_count = numTorques / 3;
        for(int i = 0; i < force_count; i++)
            odeBody.addTorque(vfTorques[i * 3],
                              vfTorques[i * 3 + 1],
                              vfTorques[i * 3 + 2]);

        // Apply the automated damping
        if(vfAutoDamp) {
            if(vfLinearDampingFactor != 0) {
                odeBody.addForce(vfLinearVelocity[0] * -vfLinearDampingFactor,
                                 vfLinearVelocity[1] * -vfLinearDampingFactor,
                                 vfLinearVelocity[2] * -vfLinearDampingFactor);
            }

            if(vfAngularDampingFactor != 0) {
                odeBody.addTorque(vfAngularVelocity[0] * -vfAngularDampingFactor,
                                  vfAngularVelocity[1] * -vfAngularDampingFactor,
                                  vfAngularVelocity[2] * -vfAngularDampingFactor);
            }
        }
    }

    /**
     * Update the local fields after the physics model has been evaluated. For
     * example the position and orientation are most likely to have changed, so
     * these should be read back from ODE and updated in the local fields.
     */
    public void updateNodeFromODE() {

        if(!vfEnabled)
            return;

        odeBody.getPosition(positionTmp);
        vfPosition[0] = positionTmp.x;
        vfPosition[1] = positionTmp.y;
        vfPosition[2] = positionTmp.z;

        hasChanged[FIELD_POSITION] = true;
        fireFieldChanged(FIELD_POSITION);

        odeBody.getQuaternion(orientTmp);
        angleTmp.set(orientTmp);
        vfOrientation[0] = angleTmp.x;
        vfOrientation[1] = angleTmp.y;
        vfOrientation[2] = angleTmp.z;
        vfOrientation[3] = angleTmp.angle;

        hasChanged[FIELD_ORIENTATION] = true;
        fireFieldChanged(FIELD_ORIENTATION);

        odeBody.getLinearVel(positionTmp);
        vfLinearVelocity[0] = positionTmp.x;
        vfLinearVelocity[1] = positionTmp.y;
        vfLinearVelocity[2] = positionTmp.z;

        hasChanged[FIELD_LINEAR_VELOCITY] = true;
        fireFieldChanged(FIELD_LINEAR_VELOCITY);

        odeBody.getPosition(positionTmp);
        vfAngularVelocity[0] = positionTmp.x;
        vfAngularVelocity[1] = positionTmp.y;
        vfAngularVelocity[2] = positionTmp.z;

        hasChanged[FIELD_ANGULAR_VELOCITY] = true;
        fireFieldChanged(FIELD_ANGULAR_VELOCITY);
    }

    /**
     * Get the number of valid geometry that this odeBody has as sub objects.
     *
     * @return A number greater than or equal to zero
     */
    public int numGeometry() {
        return numGeometry;
    }

    /**
     * Get the geometry list, provides a live reference not a copy. The number of
     * valid values is available from numGeometry();
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getGeometry() {
        return vfGeometry;
    }

    /**
     * Set the collection of geometry nodes that this odeBody should use to render
     * the main scene transformation. If passed a zero for numValid this method
     * will remove all current values.
     * <p>
     *
     * Geometry is allowed to be one of the grouping nodes or a shape node
     * type. However, if you provide something like a LOD or Switch, don't
     * expect anything to actually work correctly.
     *
     * @param geometry Array of new geometry node instances to use
     * @param numValid The number of valid values to get from the array
     * @throw InvalidFieldValueException one of the provided nodes is not a
     *   X3DRigidBodyNode instance
     */
    public void setGeometry(VRMLNodeType[] geometry, int numValid) {
        // need to clear out the old geometry from the Body here.
        if(!inSetup) {
            for(int i = 0; i < numGeometry; i++) {
                if(vfGeometry[i] != null) {
                    // Fetch its geometry and place it into the body.
                    PlaceableGeom ode_geom = findODEGeometry(vfGeometry[i]);

                    if(ode_geom != null)
                        odeBody.removeGeom(ode_geom);
                }
            }
        }

        // Check the sets of geometry for valid object types and then register
        // it with the odeBody. Anything other should result in an exception
        for(int i = 0; i < numValid; i++) {
            if(geometry[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)geometry[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if((impl != null) &&
                   !(impl instanceof VRMLNBodyCollidableNodeType))
                    throw new InvalidFieldValueException(GEOM_PROTO_MSG);

                // Now do something here.
            } else if(geometry[i] instanceof VRMLNBodyCollidableNodeType) {
                // do something.
            } else if(geometry[i] != null)
                throw new InvalidFieldValueException(GEOM_NODE_MSG);
        }

        // All the checks pass, now copy the values into the local array.
        if(vfGeometry == null || vfGeometry.length < numValid)
            vfGeometry = new VRMLNodeType[numValid];

        System.arraycopy(geometry, 0, vfGeometry, 0, numValid);
        numGeometry = numValid;

        if(!inSetup) {
            for(int i = 0; i < numGeometry; i++) {
                if(vfGeometry[i] != null) {
                    // Fetch its geometry and place it into the body.
                    PlaceableGeom ode_geom = findODEGeometry(vfGeometry[i]);

                    if(ode_geom != null)
                        odeBody.addGeom(ode_geom);
                }
            }

            hasChanged[FIELD_GEOMETRY] = true;
            fireFieldChanged(FIELD_GEOMETRY);
        }
    }

    /**
     * Fetch the reference to the node that represents the mass density model.
     *
     * @return The reference to the node defining the mass model
     */
    public VRMLNodeType getMassDensityModel() {
        if(pMassDensityModel != null)
            return pMassDensityModel;
        else
            return vfMassDensityModel;
    }

    /**
     * Set the node that should be used to represent the mass density model.
     * Setting a value of null will clear the current model in use and return
     * the system to a spherical model.
     * <p>
     *
     * The valid odeBody nodes are Sphere, Box and Cone.
     *
     * @param geom The new instance to use or null
     * @throws InvalidFieldValueException This was not a valid node type
     */
    public void setMassDensityModel(VRMLNodeType geom)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;
        String name = null;

        if(pMassDensityModel != null)
            old_node = pMassDensityModel;
        else
            old_node = vfMassDensityModel;

        if(geom instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geom).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLGeometryNodeType))
                throw new InvalidFieldValueException(MASS_PROTO_MSG);

            name = impl.getVRMLNodeName();
            if(!validDensityTypes.contains(name))
                throw new InvalidFieldValueException(MASS_TYPE_MSG);

            pMassDensityModel = (VRMLProtoInstance)geom;
            vfMassDensityModel = (VRMLGeometryNodeType)impl;
        } else if(geom instanceof VRMLGeometryNodeType) {
            name = geom.getVRMLNodeName();
            if(!validDensityTypes.contains(name))
                throw new InvalidFieldValueException(MASS_TYPE_MSG);

            pMassDensityModel = null;
            vfMassDensityModel = (VRMLGeometryNodeType)geom;
        } else
            throw new InvalidFieldValueException(MASS_NODE_MSG);


        if(vfMassDensityModel != null)
            updateRefs(vfMassDensityModel, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(vfMassDensityModel != null)
                stateManager.registerAddedNode(vfMassDensityModel);

            if(vfMassDensityModel != null) {
                int index;
                VRMLFieldData field;

                if(name.equals("Box")) {
                    index = vfMassDensityModel.getFieldIndex("size");
                    field = vfMassDensityModel.getFieldValue(index);
                    float[] size = field.floatArrayValue;
                    float volume = size[0] * size[1] * size[2];

                    odeBody.setBoxMass(vfMass/volume, size[0], size[1], size[2]);

                } else if(name.equals("Sphere")) {
                    index = vfMassDensityModel.getFieldIndex("radius");
                    field = vfMassDensityModel.getFieldValue(index);

                    float radius = field.floatValue;
                    odeBody.setSphereMass(vfMass / (radius * radius), radius);
                } else
System.out.println("RigidBody unhandled mass density model type object: " + name);
            }

            hasChanged[FIELD_MASS_DENSITY_MODEL] = true;
            fireFieldChanged(FIELD_MASS_DENSITY_MODEL);
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

        if(pMassDensityModel != null)
            pMassDensityModel.setupFinished();
        else if(vfMassDensityModel != null)
            vfMassDensityModel.setupFinished();

        odeBody.setPosition(vfPosition[0],
                            vfPosition[1],
                            vfPosition[2]);
        odeBody.setAxisAndAngle(vfOrientation[0],
                                vfOrientation[1],
                                vfOrientation[2],
                                vfOrientation[3]);
        odeBody.setLinearVel(vfLinearVelocity[0],
                             vfLinearVelocity[1],
                             vfLinearVelocity[2]);
        odeBody.setAngularVel(vfAngularVelocity[0],
                              vfAngularVelocity[1],
                              vfAngularVelocity[2]);

        odeBody.setFiniteRotationMode(vfUseFiniteRotation ? 1 : 0);
        odeBody.setFiniteRotationAxis(vfFiniteRotationAxis[0],
                                      vfFiniteRotationAxis[1],
                                      vfFiniteRotationAxis[2]);

        odeBody.setGravityMode(vfUseGlobalGravity ? 1 : 0);
        odeBody.setAngularVelocityDisableThreshold(vfDisableAngularSpeed);
        odeBody.setLinearVelocityDisableThreshold(vfDisableLinearSpeed);
        odeBody.setTimeDisableThreshold(vfDisableTime);
        odeBody.setAutoDisable(vfAutoDisable);
        odeBody.setEnabled(vfEnabled);

        for(int i = 0; i < numGeometry; i++) {
            if(vfGeometry[i] != null) {
                vfGeometry[i].setupFinished();

                // Fetch its geometry and place it into the body.
                PlaceableGeom ode_geom = findODEGeometry(vfGeometry[i]);

                if(ode_geom != null)
                    odeBody.addGeom(ode_geom);
            }
        }

        if(vfMassDensityModel != null) {
            int index;
            VRMLFieldData field;
            String name = vfMassDensityModel.getVRMLNodeName();

            if(name.equals("Box")) {
                index = vfMassDensityModel.getFieldIndex("size");
                field = vfMassDensityModel.getFieldValue(index);
                float[] size = field.floatArrayValue;
                float volume = size[0] * size[1] * size[2];

                odeBody.setBoxMass(vfMass/volume, size[0], size[1], size[2]);
            } else if(name.equals("Sphere")) {
                index = vfMassDensityModel.getFieldIndex("radius");
                field = vfMassDensityModel.getFieldValue(index);

                float radius = field.floatValue;

                float volume = (float)(4 * Math.PI / 3 * radius * radius * radius);
                odeBody.setSphereMass(vfMass / volume, radius);
            }

        } else {
            odeBody.setMassParameters(vfMass,
                                      vfCenterOfMass[0], vfCenterOfMass[1],
                                      vfCenterOfMass[2],
                                      vfInertia[0], vfInertia[4], vfInertia[8],
                                      vfInertia[1], vfInertia[2], vfInertia[5]);
        }

        odeBody.adjustMass(vfMass);
        // There is no point updating the Force and torque values here as
        // ODE zeroes these out every frame. We'll look after this in the
        // pre-update call.
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
        if(index < 0  || index > LAST_BODY_INDEX)
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
        return TypeConstants.RigidBodyNodeType;
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
            case FIELD_ANGULAR_VELOCITY:
                fieldData.clear();
                fieldData.floatArrayValue = vfAngularVelocity;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ANGULAR_DAMPING_FACTOR:
                fieldData.clear();
                fieldData.floatValue = vfAngularDampingFactor;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_AUTO_DAMP:
                fieldData.clear();
                fieldData.booleanValue = vfAutoDamp;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_AUTO_DISABLE:
                fieldData.clear();
                fieldData.booleanValue = vfAutoDisable;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CENTER_OF_MASS:
                fieldData.clear();
                fieldData.floatArrayValue = vfCenterOfMass;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_DISABLE_ANGULAR_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfDisableAngularSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_DISABLE_LINEAR_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfDisableLinearSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_DISABLE_TIME:
                fieldData.clear();
                fieldData.floatValue = vfDisableTime;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_FINITE_ROTATION_AXIS:
                fieldData.clear();
                fieldData.floatArrayValue = vfFiniteRotationAxis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_FORCES:
                fieldData.clear();
                fieldData.floatArrayValue = vfForces;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numForces / 3;
                break;

            case FIELD_GEOMETRY:
                fieldData.clear();
                fieldData.nodeArrayValue = vfGeometry;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = numGeometry;
                break;

            case FIELD_INERTIA:
                fieldData.clear();
                fieldData.floatArrayValue = vfInertia;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_LINEAR_DAMPING_FACTOR:
                fieldData.clear();
                fieldData.floatValue = vfLinearDampingFactor;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_LINEAR_VELOCITY:
                fieldData.clear();
                fieldData.floatArrayValue = vfLinearVelocity;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_MASS:
                fieldData.clear();
                fieldData.floatValue = vfMass;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MASS_DENSITY_MODEL:
                fieldData.clear();

                if(pMassDensityModel != null)
                    fieldData.nodeValue = pMassDensityModel;
                else
                    fieldData.nodeValue = vfMassDensityModel;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_ORIENTATION:
                fieldData.clear();
                fieldData.floatArrayValue = vfOrientation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_POSITION:
                fieldData.clear();
                fieldData.floatArrayValue = vfPosition;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_TORQUES:
                fieldData.clear();
                fieldData.floatArrayValue = vfTorques;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numTorques / 3;
                break;

            case FIELD_USE_FINITE_ROTATION:
                fieldData.clear();
                fieldData.booleanValue = vfUseFiniteRotation;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_USE_GLOBAL_GRAVITY:
                fieldData.clear();
                fieldData.booleanValue = vfUseGlobalGravity;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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
                case FIELD_ANGULAR_VELOCITY:
                    destNode.setValue(destIndex, vfAngularVelocity, 3);
                    break;

                case FIELD_ANGULAR_DAMPING_FACTOR:
                    destNode.setValue(destIndex, vfAngularDampingFactor);
                    break;

                case FIELD_AUTO_DAMP:
                    destNode.setValue(destIndex, vfAutoDamp);
                    break;

                case FIELD_AUTO_DISABLE:
                    destNode.setValue(destIndex, vfAutoDisable);
                    break;

                case FIELD_CENTER_OF_MASS:
                    destNode.setValue(destIndex, vfCenterOfMass, 3);
                    break;

                case FIELD_DISABLE_ANGULAR_SPEED:
                    destNode.setValue(destIndex, vfDisableAngularSpeed);
                    break;

                case FIELD_DISABLE_LINEAR_SPEED:
                    destNode.setValue(destIndex, vfDisableLinearSpeed);
                    break;

                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_FINITE_ROTATION_AXIS:
                    destNode.setValue(destIndex, vfFiniteRotationAxis, 3);
                    break;

                case FIELD_FORCES:
                    destNode.setValue(destIndex, vfForces, numForces);
                    break;

                case FIELD_GEOMETRY:
                    destNode.setValue(destIndex, vfGeometry, numGeometry);
                    break;

                case FIELD_INERTIA:
                    destNode.setValue(destIndex, vfInertia, numInertia);
                    break;

                case FIELD_LINEAR_DAMPING_FACTOR:
                    destNode.setValue(destIndex, vfLinearDampingFactor);
                    break;

                case FIELD_LINEAR_VELOCITY:
                    destNode.setValue(destIndex, vfLinearVelocity, 3);
                    break;

                case FIELD_MASS:
                    destNode.setValue(destIndex, vfMass);
                    break;

                case FIELD_MASS_DENSITY_MODEL:
                    if(pMassDensityModel != null)
                        destNode.setValue(destIndex, pMassDensityModel);
                    else
                        destNode.setValue(destIndex, vfMassDensityModel);
                    break;

                case FIELD_ORIENTATION:
                    destNode.setValue(destIndex, vfOrientation, 4);
                    break;

                case FIELD_POSITION:
                    destNode.setValue(destIndex, vfPosition, 3);
                    break;

                case FIELD_TORQUES:
                    destNode.setValue(destIndex, vfTorques, numTorques);
                    break;

                case FIELD_USE_FINITE_ROTATION:
                    destNode.setValue(destIndex, vfUseFiniteRotation);
                    break;

                case FIELD_USE_GLOBAL_GRAVITY:
                    destNode.setValue(destIndex, vfUseGlobalGravity);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseJointNode.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseJointNode.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }


    /**
     * Set the value of the field at the given index as a single boolean.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_AUTO_DISABLE:
                setAutoDisable(value);
                break;

            case FIELD_AUTO_DAMP:
                setAutoDamp(value);
                break;

            case FIELD_ENABLED:
                setEnabled(value);
                break;

            case FIELD_USE_FINITE_ROTATION:
                setUseFiniteRotation(value);
                break;

            case FIELD_USE_GLOBAL_GRAVITY:
                setUseGlobalGravity(value);
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
            case FIELD_ANGULAR_DAMPING_FACTOR:
                setAngularDampingFactor(value);
                break;

            case FIELD_DISABLE_ANGULAR_SPEED:
                setDisableAngularVelocity(value);
                break;

            case FIELD_DISABLE_LINEAR_SPEED:
                setDisableLinearVelocity(value);
                break;

            case FIELD_DISABLE_TIME:
                setDisableTime(value);
                break;

            case FIELD_LINEAR_DAMPING_FACTOR:
                setLinearDampingFactor(value);
                break;

            case FIELD_MASS:
                setMass(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec3f etc field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid items to use from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_ANGULAR_VELOCITY:
                setAngularVelocity(value);
                break;

            case FIELD_CENTER_OF_MASS:
                setCenterOfMass(value);
                break;

            case FIELD_FINITE_ROTATION_AXIS:
                setFiniteRotationAxis(value);
                break;

            case FIELD_FORCES:
                setForces(value, numValid);
                break;

            case FIELD_INERTIA:
                setInertia(value, numValid);
                break;

            case FIELD_LINEAR_VELOCITY:
                setLinearVelocity(value);
                break;

            case FIELD_ORIENTATION:
                setOrientation(value);
                break;

            case FIELD_POSITION:
                setPosition(value);
                break;

            case FIELD_TORQUES:
                setTorques(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node.
     * This would be used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_MASS_DENSITY_MODEL:
                setMassDensityModel(child);
                break;

            case FIELD_GEOMETRY:
                addGeometry(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as a node.
     * This would be used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
     * @param numValid The number of valid items to use from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_GEOMETRY:
                setGeometry(children, numValid);
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the new angular velocity to use.
     *
     * @param value The new value to use
     */
    private void setAngularVelocity(float[] value) {
        vfAngularVelocity[0] = value[0];
        vfAngularVelocity[1] = value[1];
        vfAngularVelocity[2] = value[2];

        if(!inSetup) {
            odeBody.setAngularVel(value[0], value[1], value[2]);
            hasChanged[FIELD_ANGULAR_VELOCITY] = true;
            fireFieldChanged(FIELD_ANGULAR_VELOCITY);
        }
    }

    /**
     * Set the value of the new autoDamp field.
     *
     * @param state True for false
     */
    private void setAutoDamp(boolean state) {
        vfAutoDamp = state;

        if(!inSetup) {
            hasChanged[FIELD_AUTO_DAMP] = true;
            fireFieldChanged(FIELD_AUTO_DAMP);
        }
    }

    /**
     * Set the value of the new angularDampingFactor field.
     *
     * @param factor A factor for damping
     */
    private void setAngularDampingFactor(float factor) {
        vfAngularDampingFactor = factor;

        if(!inSetup) {
            hasChanged[FIELD_ANGULAR_DAMPING_FACTOR] = true;
            fireFieldChanged(FIELD_ANGULAR_DAMPING_FACTOR);
        }
    }

    /**
     * Set the value of the new angularDampingFactor field.
     *
     * @param factor A factor for damping
     */
    private void setLinearDampingFactor(float factor) {
        vfLinearDampingFactor = factor;

        if(!inSetup) {
            hasChanged[FIELD_LINEAR_DAMPING_FACTOR] = true;
            fireFieldChanged(FIELD_LINEAR_DAMPING_FACTOR);
        }
    }

    /**
     * Set the value of the new autoDisable field.
     *
     * @param state True for false
     */
    private void setAutoDisable(boolean state) {
        vfAutoDisable = state;

        if(!inSetup) {
            odeBody.setAutoDisable(state);
            hasChanged[FIELD_AUTO_DISABLE] = true;
            fireFieldChanged(FIELD_AUTO_DISABLE);
        }
    }

    /**
     * Set the value of the center of mass field.
     *
     * @param value The new value to use
     */
    private void setCenterOfMass(float[] value) {
        vfCenterOfMass[0] = value[0];
        vfCenterOfMass[1] = value[1];
        vfCenterOfMass[2] = value[2];

        if(!inSetup) {
            odeBody.setMassParameters(vfMass,
                                      value[0], value[1], value[2],
                                      vfInertia[0], vfInertia[4], vfInertia[8],
                                      vfInertia[1], vfInertia[2], vfInertia[5]);

            hasChanged[FIELD_CENTER_OF_MASS] = true;
            fireFieldChanged(FIELD_CENTER_OF_MASS);
        }
    }

    /**
     * Set the value of the new disableAngularSpeed    field.
     *
     * @param value The new value of the
     */
    private void setDisableAngularVelocity(float value) {
        vfDisableAngularSpeed = value;

        if(!inSetup) {
            odeBody.setAngularVelocityDisableThreshold(value);
            hasChanged[FIELD_DISABLE_ANGULAR_SPEED] = true;
            fireFieldChanged(FIELD_DISABLE_ANGULAR_SPEED);
        }
    }

    /**
     * Set the value of the new disableLinearSpeed    field.
     *
     * @param value The new value of the
     */
    private void setDisableLinearVelocity(float value) {
        vfDisableLinearSpeed = value;

        if(!inSetup) {
            odeBody.setLinearVelocityDisableThreshold(value);
            hasChanged[FIELD_DISABLE_LINEAR_SPEED] = true;
            fireFieldChanged(FIELD_DISABLE_LINEAR_SPEED);
        }
    }

    /**
     * Set the value of the new disableTime field.
     *
     * @param value The new value of the time, in milliseconds
     */
    private void setDisableTime(float value) {
        vfDisableTime = value;

        if(!inSetup) {
            odeBody.setTimeDisableThreshold(value);
            hasChanged[FIELD_DISABLE_TIME] = true;
            fireFieldChanged(FIELD_DISABLE_TIME);
        }
    }

    /**
     * Set the new state value of the autoDisable field.
     *
     * @param state True for false
     */
    private void setEnabled(boolean state) {
        vfEnabled = state;

        if(!inSetup) {
            // Zero out everything before enabling it
            if(state) {
                odeBody.setForce(0, 0, 0);
                odeBody.setTorque(0, 0, 0);
            }
            odeBody.setEnabled(state);
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Set the new axis for the finite rotation capabilities to use.
     *
     * @param value The new value to use
     */
    private void setFiniteRotationAxis(float[] value) {
        vfFiniteRotationAxis[0] = value[0];
        vfFiniteRotationAxis[1] = value[1];
        vfFiniteRotationAxis[2] = value[2];

        if(!inSetup) {
            odeBody.setFiniteRotationAxis(value[0], value[1], value[2]);
            hasChanged[FIELD_FINITE_ROTATION_AXIS] = true;
            fireFieldChanged(FIELD_FINITE_ROTATION_AXIS);
        }
    }

    /**
     * Set the collection of forces on the odeBody.
     *
     * @param value The new value to use
     */
    private void setForces(float[] value, int numValid) {

        if(vfForces.length < numValid)
            vfForces = new float[numValid];

        System.arraycopy(value, 0, vfForces, 0, numValid);
        numForces = numValid;

        if(!inSetup) {
            // No need to update forces here as these are zeroed every frame
            // anyway. Updated in the pre-eval method above.

            hasChanged[FIELD_FORCES] = true;
            fireFieldChanged(FIELD_FORCES);
        }
    }

    /**
     * Set the inertia matrix to use for the mass parameters.
     *
     * @param value The new value to use
     */
    private void setInertia(float[] value, int numValid) {

        if(vfInertia.length < numValid)
            vfInertia = new float[numValid];

        System.arraycopy(value, 0, vfInertia, 0, numValid);
        numInertia = numValid;

        if(!inSetup) {
            odeBody.setMassParameters(vfMass,
                                      vfCenterOfMass[0],
                                      vfCenterOfMass[1],
                                      vfCenterOfMass[2],
                                      vfInertia[0], vfInertia[4], vfInertia[8],
                                      vfInertia[1], vfInertia[2], vfInertia[5]);
            hasChanged[FIELD_INERTIA] = true;
            fireFieldChanged(FIELD_INERTIA);
        }
    }

    /**
     * Set the new linear velocity to use.
     *
     * @param value The new value to use
     */
    private void setLinearVelocity(float[] value) {
        vfLinearVelocity[0] = value[0];
        vfLinearVelocity[1] = value[1];
        vfLinearVelocity[2] = value[2];

        if(!inSetup) {
            odeBody.setLinearVel(value[0], value[1], value[2]);
            hasChanged[FIELD_LINEAR_VELOCITY] = true;
            fireFieldChanged(FIELD_LINEAR_VELOCITY);
        }
    }

    /**
     * Set the new object mass.
     *
     * @param value The new value to use
     */
    private void setMass(float value) throws InvalidFieldValueException {

        if(value <= 0)
            throw new InvalidFieldValueException(NEG_MASS_MSG);

        vfMass = value;

        if(!inSetup) {
            odeBody.adjustMass(vfMass);
            hasChanged[FIELD_MASS] = true;
            fireFieldChanged(FIELD_MASS);
        }
    }

    /**
     * Set the new linear velocity to use.
     *
     * @param value The new value to use
     */
    private void setOrientation(float[] value) {
        vfOrientation[0] = value[0];
        vfOrientation[1] = value[1];
        vfOrientation[2] = value[2];
        vfOrientation[3] = value[3];

        if(!inSetup) {
            odeBody.setAxisAndAngle(vfOrientation[0],
                                    vfOrientation[1],
                                    vfOrientation[2],
                                    vfOrientation[3]);

            hasChanged[FIELD_ORIENTATION] = true;
            fireFieldChanged(FIELD_ORIENTATION);
        }
    }

    /**
     * Set the new linear velocity to use.
     *
     * @param value The new value to use
     */
    private void setPosition(float[] value) {
        vfPosition[0] = value[0];
        vfPosition[1] = value[1];
        vfPosition[2] = value[2];

        if(!inSetup) {
            odeBody.setPosition(value[0], value[1], value[2]);
            hasChanged[FIELD_POSITION] = true;
            fireFieldChanged(FIELD_POSITION);
        }
    }


    /**
     * Set the collection of torques on the odeBody.
     *
     * @param value The new values to use
     */
    private void setTorques(float[] value, int numValid) {

        if(vfTorques.length < numValid)
            vfTorques = new float[numValid];

        System.arraycopy(value, 0, vfTorques, 0, numValid);
        numTorques = numValid;

        if(!inSetup) {
            // No need to update torques here as these are zeroed every frame
            // anyway. Updated in the pre-eval method above.

            hasChanged[FIELD_TORQUES] = true;
            fireFieldChanged(FIELD_TORQUES);
        }
    }

    /**
     * Set the new state value of the useFiniteRotation field.
     *
     * @param state True for false
     */
    private void setUseFiniteRotation(boolean state) {
        vfUseFiniteRotation = state;

        if(!inSetup) {
            odeBody.setFiniteRotationMode(state ? 1 : 0);
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Set the new state value of the useGlobalGravity field.
     *
     * @param state True for false
     */
    private void setUseGlobalGravity(boolean state) {
        vfUseGlobalGravity = state;

        if(!inSetup) {
            odeBody.setGravityMode(state ? 1 : 0);
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Add a geometry node to the list for this body during setup. If this
     * is not during setup, bitch loudly.
     *
     * @param node The potential node to check on
     * @throws InvalidFieldValueException The node was not geometry
     */
    protected void addGeometry(VRMLNodeType node)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException("Should not call out of setup");

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(node instanceof VRMLNBodyCollidableNodeType))
                throw new InvalidFieldValueException(GEOM_PROTO_MSG);

        } else if((node != null) && !(node instanceof VRMLNBodyCollidableNodeType))
            throw new InvalidFieldValueException(GEOM_NODE_MSG);

        if(vfGeometry == null || numGeometry == vfGeometry.length) {
            VRMLNodeType[] tmp = new VRMLNodeType[numGeometry + 8];
            if(vfGeometry != null)
                System.arraycopy(vfGeometry, 0, tmp, 0, numGeometry);

            vfGeometry = tmp;
        }

        vfGeometry[numGeometry++] = node;
    }

    /**
     * Wander down the geometry node instance and find the placeable geom.
     *
     * @param geom The geometry node instance
     * @return The contained PlaceableGeom instance or null
     */
    private PlaceableGeom findODEGeometry(VRMLNodeType geom) {
        PlaceableGeom ret_val = null;

        if(geom instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geom).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl != null)
                ret_val = ((VRMLNBodyCollidableNodeType)impl).getODEGeometry();
        } else if(geom != null) {
            ret_val = ((VRMLNBodyCollidableNodeType)geom).getODEGeometry();
        }

        return ret_val;
    }
}
