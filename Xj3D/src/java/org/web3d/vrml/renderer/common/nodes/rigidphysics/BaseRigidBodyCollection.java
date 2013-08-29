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
import java.util.ArrayList;
import java.util.HashMap;

import org.odejava.Body;
import org.odejava.JointGroup;
import org.odejava.World;
import org.odejava.collision.BulkContact;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the RigidBodyCollection node.
 * <p>
 *
 * In this implementation, the sets of nodes for the bodies and geometry are
 * not expected to change much, if at all. As such, arrays are used to
 * represent the field values, rather than the more customary ArrayList that
 * other classes use for MFNode fields
 *
 * The X3D definition of RigidBody is:
 * <pre>
 * RigidBodyCollection : X3DChildNode {
 *   MFNode  [in]     set_contacts            [Contact]
 *   SFBool  [in,out] autoDisable             FALSE
 *   MFNode  [in,out] bodies                  []       [RigidBody]
 *   SFFloat [in,out] contactSurfaceThickness 0        [0,&#8734;)
 *   SFFloat [in,out] disableAngularSpeed     0        [0,&#8734;)
 *   SFFloat [in,out] disableLinearSpeed      0        [0,&#8734;)
 *   SFFloat [in,out] disableTime             0        [0,&#8734;)
 *   SFBool  [in,out] enabled                 TRUE
 *   SFFloat [in,out] errorCorrectionFactor   0        [0,1]
 *   SFVec3f [in,out] gravity                 0 -9.8 0
 *   SFInt32 [in,out] iterations              10       [0,&#8734;)
 *   MFNode  [in,out] joints                  []       [X3DRigidJointNode]
 *   SFFloat [in,out] maxCorrectionSpeed      -1       [0,&#8734;) or -1
 *   SFNode  [in,out] metadata                NULL     [X3DMetadataObject]
 *   SFBool  [in,out] preferAccuracy          FALSE
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class BaseRigidBodyCollection extends AbstractNode
    implements VRMLRigidBodyGroupNodeType, VRMLChildNodeType {

    // Field index constants

    /** The field index for set_contacts */
    protected static final int FIELD_SET_CONTACTS = LAST_NODE_INDEX + 1;

    /** The field index for autoDisable */
    protected static final int FIELD_AUTO_DISABLE = LAST_NODE_INDEX + 2;

    /** The field index for bodies */
    protected static final int FIELD_BODIES = LAST_NODE_INDEX + 3;

    /** The field index for constantForceMix */
    protected static final int FIELD_CONSTANT_FORCE_MIX = LAST_NODE_INDEX + 4;

    /** The field index for contactSurfaceThickness */
    protected static final int FIELD_CONTACT_SURFACE_THICKNESS = LAST_NODE_INDEX + 5;

    /** The field index for disableAngularSpeed    */
    protected static final int FIELD_DISABLE_ANGULAR_SPEED = LAST_NODE_INDEX + 6;

    /** The field index for disableLinearSpeed    */
    protected static final int FIELD_DISABLE_LINEAR_SPEED = LAST_NODE_INDEX + 7;

    /** The field index for disableTime */
    protected static final int FIELD_DISABLE_TIME = LAST_NODE_INDEX + 8;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 9;

    /** The field index for errorCorrection */
    protected static final int FIELD_ERROR_CORRECTION = LAST_NODE_INDEX + 10;

    /** The field index for gravity */
    protected static final int FIELD_GRAVITY = LAST_NODE_INDEX + 11;

    /** The field index for iterations */
    protected static final int FIELD_ITERATIONS = LAST_NODE_INDEX + 12;

    /** The field index for joints */
    protected static final int FIELD_JOINTS = LAST_NODE_INDEX + 13;

    /** The field index for maxCorrectionVelocity */
    protected static final int FIELD_MAX_CORRECTION_SPEED = LAST_NODE_INDEX + 14;

    /** The field index for preferAccuracy */
    protected static final int FIELD_PREFER_ACCURACY = LAST_NODE_INDEX + 15;

    /** The field index for collider */
    protected static final int FIELD_COLLIDER = LAST_NODE_INDEX + 16;

    /** Last index used by this collection node */
    protected static final int LAST_COLLECTION_INDEX = FIELD_COLLIDER;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_COLLECTION_INDEX + 1;

    /** Message for when the proto is not a Body */
    protected static final String BODY_PROTO_MSG =
        "Proto does not describe a Body object.";

    /** Message for when the node in setValue() is not a Contact */
    protected static final String BODY_NODE_MSG =
        "Node does not describe a Body object.";

    /** Message for when the proto is not a Body */
    protected static final String CONTACT_PROTO_MSG =
        "Proto does not describe a Contact node.";

    /** Message for when the node in setValue() is not a Contact */
    protected static final String CONTACT_NODE_MSG =
        "Node does not describe a Contact node.";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLL_PROTO_MSG =
        "collider proto does not describe a CollisionCollection object.";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLL_NODE_MSG =
        "Collider node does not describe a CollisionCollection object.";

    protected static final String INVALID_BODY_NODE_MSG =
        "An invalid node type has been specified for the bodies field. The " +
        "only valid node is RigidBody.";

    protected static final String INVALID_JOINT_NODE_MSG =
        "An invalid node type has been specified for the joints field. Valid " +
        "nodes must be of the X3DRigidJointNode base type.";

    /** Message when the stopBounce value is out of range */
    protected static final String DIS_ANG_VEL_RANGE_MSG =
        "The disableAngularSpeed value is out of the required range [0,inf): ";

    /** Message when the stopBounce value is out of range */
    protected static final String DIS_LIN_VEL_RANGE_MSG =
        "The disableLinearSpeed value is out of the required range [0,inf): ";

    /** Message when the contactSurfaceThickness is negative */
    protected static final String NEG_SURFACE_MSG =
        "The value of contactSurfaceThickness field is negative.";

    /** Message when the errorCorrection is negative */
    protected static final String NEG_ERR_CORR_MSG =
        "The value of errorCorrection field is negative.";

    /** Message when the maxCorrectionSpeed is negative */
    protected static final String NEG_ERR_SPEED_MSG =
        "The value of maxCorrectionSpeed is negative.";

    /** Message when the maxCorrectionSpeed is negative */
    protected static final String NEG_ITERATIONS_MSG =
        "The value of iterations is negative.";

    /** Message when the constantForceMix is negative */
    protected static final String NEG_CFM_MSG =
        "The value of constantForceMix is negative: ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // The VRML field values

    /** Values set for set_contacts input event */
    protected ArrayList vfSetContacts;

    /** The value of the autoDisable field */
    protected boolean vfAutoDisable;

    /** The value of the contactSurfaceThickness field */
    protected float vfContactSurfaceThickness;

    /** The value of the constantForceMix field */
    protected float vfConstantForceMix;

    /** The value of the bodies field */
    protected VRMLNodeType[] vfBodies;

    /** The number of valid bodies to use */
    protected int numBodies;

    /** The value of the disableTime field */
    protected float vfDisableTime;

    /** The value of the disableAngularSpeed field */
    protected float vfDisableAngularSpeed;

    /** The value of the disableLinearSpeed field */
    protected float vfDisableLinearSpeed;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** The value of the errorCorrection field */
    protected float vfErrorCorrection;

    /** The value of the gravity field */
    protected float[] vfGravity;

    /** The value of the iterations field */
    protected int vfIterations;

    /** The value of the joints field */
    protected VRMLNodeType[] vfJoints;

    /** The number of valid values in vfJoints */
    protected int numJoints;

    /** The value of the maxCorrectionSpeed field */
    protected float vfMaxCorrectionSpeed;

    /** The value of the preferAccuracy field */
    protected boolean vfPreferAccuracy;

    /** The value of the collider field */
    protected VRMLNBodyGroupNodeType vfCollider;

    /** The proto version of vfCollider */
    protected VRMLProtoInstance pCollider;

    // Other variables

    /** The ODE world instance for controlling this simulation */
    private World odeWorld;

    /** The joint group used to hold all the joints of this world */
    private JointGroup odeJointGroup;

    /** All the resolved body nodes from protos currently registered. */
    private ArrayList bodyNodes;

    /**
     * Tracking array to know which contacts to delete. If a value is false
     * in this array then the end user has removed that index from the
     * incoming array that was sent in the outgoing one. Thus, after processing
     * all the incoming contacts and marking as true the ones we have
     * received, we run through the array and delete the ones we didn't get.
     */
    private boolean[] receivedContacts;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_BODIES,
            FIELD_JOINTS,
            FIELD_COLLIDER
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_SET_CONTACTS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFNode",
                                     "set_contacts");
        fieldDecl[FIELD_AUTO_DISABLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "autoDisable");
        fieldDecl[FIELD_BODIES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "bodies");
        fieldDecl[FIELD_CONSTANT_FORCE_MIX] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "constantForceMix");
        fieldDecl[FIELD_CONTACT_SURFACE_THICKNESS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "contactSurfaceThickness");
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
        fieldDecl[FIELD_ERROR_CORRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "errorCorrectionFactor");
        fieldDecl[FIELD_GRAVITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "gravity");
        fieldDecl[FIELD_JOINTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "joints");
        fieldDecl[FIELD_ITERATIONS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "iterations");
        fieldDecl[FIELD_MAX_CORRECTION_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxCorrectionSpeed");
        fieldDecl[FIELD_COLLIDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "collider");

        fieldDecl[FIELD_PREFER_ACCURACY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "preferAccuracy");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("set_contacts", new Integer(FIELD_SET_CONTACTS));
        fieldMap.put("collider", new Integer(FIELD_COLLIDER));

        idx = new Integer(FIELD_AUTO_DISABLE);
        fieldMap.put("autoDisable", idx);
        fieldMap.put("set_autoDisable", idx);
        fieldMap.put("autoDisable_changed", idx);

        idx = new Integer(FIELD_BODIES);
        fieldMap.put("bodies", idx);
        fieldMap.put("set_bodies", idx);
        fieldMap.put("bodies_changed", idx);

        idx = new Integer(FIELD_CONSTANT_FORCE_MIX);
        fieldMap.put("constantForceMix", idx);
        fieldMap.put("set_constantForceMix", idx);
        fieldMap.put("constantForceMix_changed", idx);

        idx = new Integer(FIELD_CONTACT_SURFACE_THICKNESS);
        fieldMap.put("contactSurfaceThickness", idx);
        fieldMap.put("set_contactSurfaceThickness", idx);
        fieldMap.put("contactSurfaceThickness_changed", idx);

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

        idx = new Integer(FIELD_ERROR_CORRECTION);
        fieldMap.put("errorCorrection", idx);
        fieldMap.put("set_errorCorrection", idx);
        fieldMap.put("errorCorrection_changed", idx);

        idx = new Integer(FIELD_GRAVITY);
        fieldMap.put("gravity", idx);
        fieldMap.put("set_gravity", idx);
        fieldMap.put("gravity_changed", idx);

        idx = new Integer(FIELD_JOINTS);
        fieldMap.put("joints", idx);
        fieldMap.put("set_joints", idx);
        fieldMap.put("joints_changed", idx);

        idx = new Integer(FIELD_ITERATIONS);
        fieldMap.put("iterations", idx);
        fieldMap.put("set_iterations", idx);
        fieldMap.put("iterations_changed", idx);

        idx = new Integer(FIELD_MAX_CORRECTION_SPEED);
        fieldMap.put("maxCorrectionSpeed", idx);
        fieldMap.put("set_maxCorrectionSpeed", idx);
        fieldMap.put("maxCorrectionSpeed_changed", idx);

        idx = new Integer(FIELD_PREFER_ACCURACY);
        fieldMap.put("preferAccuracy", idx);
        fieldMap.put("set_preferAccuracy", idx);
        fieldMap.put("preferAccuracy_changed", idx);
    }

    /**
     * Construct a new default RigidBodyCollection node object.
     */
    public BaseRigidBodyCollection() {
        super("RigidBodyCollection");

        vfAutoDisable = false;
        vfContactSurfaceThickness = 0;
        vfDisableAngularSpeed = 0;
        vfDisableLinearSpeed = 0;
        vfDisableTime = 0;
        vfEnabled = true;
        vfErrorCorrection = 0.8f;
        vfConstantForceMix = 0.0001f;
        vfGravity = new float[] { 0, -0.98f, 0 };
        vfIterations = 10;
        vfMaxCorrectionSpeed = 0;
        vfPreferAccuracy = false;

        vfSetContacts = new ArrayList();

        odeJointGroup = new JointGroup();
        bodyNodes = new ArrayList();

        receivedContacts = new boolean[BulkContact.ARRAY_SIZE];

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
    public BaseRigidBodyCollection(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("autoDisable");
            VRMLFieldData field = node.getFieldValue(index);
            vfAutoDisable = field.booleanValue;

            index = node.getFieldIndex("contactSurfaceThickness");
            field = node.getFieldValue(index);
            vfContactSurfaceThickness = field.floatValue;

            index = node.getFieldIndex("disableAngularSpeed");
            field = node.getFieldValue(index);
            vfDisableAngularSpeed = field.floatValue;

            index = node.getFieldIndex("disableLinearSpeed");
            field = node.getFieldValue(index);
            vfDisableLinearSpeed = field.floatValue;

            index = node.getFieldIndex("disableTime");
            field = node.getFieldValue(index);
            vfDisableTime = field.floatValue;

            index = node.getFieldIndex("enabled");
            field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("errorCorrection");
            field = node.getFieldValue(index);
            vfErrorCorrection = field.floatValue;

            index = node.getFieldIndex("gravity");
            field = node.getFieldValue(index);
            vfGravity[0] = field.floatArrayValue[0];
            vfGravity[1] = field.floatArrayValue[1];
            vfGravity[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("iterations");
            field = node.getFieldValue(index);
            vfIterations = field.intValue;

            index = node.getFieldIndex("maxCorrectionSpeed");
            field = node.getFieldValue(index);
            vfMaxCorrectionSpeed = field.floatValue;

            index = node.getFieldIndex("preferAccuracy");
            field = node.getFieldValue(index);
            vfPreferAccuracy = field.booleanValue;

            index = node.getFieldIndex("constantForceMix");
            field = node.getFieldValue(index);
            vfConstantForceMix = field.floatValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLRigidBodyGroupNodeType
    //----------------------------------------------------------

    /**
     * Go through the list of input contacts, process them and send them off
     * to ODE.
     */
    public void processInputContacts() {

        int num_contacts = vfSetContacts.size();
        if((vfSetContacts.size() == 0) || (vfCollider == null))
            return;

        int num_sent = vfCollider.numContacts();
        if(receivedContacts.length < num_sent)
            receivedContacts = new boolean[num_sent];

        for(int i = 0; i < num_sent; i++)
            receivedContacts[i] = false;

        BulkContact contacts = vfCollider.getContacts();

        for(int i = 0; i < num_contacts; i++) {
            BaseContact contact = (BaseContact)vfSetContacts.get(i);

            // Safety first - if someone gave us a contact that was not
            // generated from our local collider, we want to ignore it. We can
            // tell the origin, just by looking at the BulkContact array
            // instance as each collider runs their own instance.
            //
            // One thing that this will miss is end-users creating new contacts
            // on the fly. We don't deal with this right now, but it may be
            // worthwhile investigating a way of putting these aside (the
            // contact array will be null) and then doing another pass through
            // and assigning these to the blank spots that were otherwise
            // deleted. We can't create a new, separate BulkContact instance
            // here because the collision API of odejava has the
            // applyContacts() on the JavaCollision class. There does not
            // appear to be any way to create new sets of contacts withouth
            // going around the back.
            if(contact.getContacts() != contacts)
                continue;

            receivedContacts[i] = true;
        }

        for(int i = 0; i < num_sent; i++) {
            if(!receivedContacts[i])
                contacts.ignoreContact(i);
        }

        vfCollider.applyContacts();
    }

    /**
     * Adjust the model's timestep to the new value (in seconds). This is
     * called periodically to adjust the timestep made based on the current
     * frame rate to adjust the model.
     *
     * @param deltaT The time change in seconds
     */
    public void setTimestep(float deltaT) {
        odeWorld.setStepSize(deltaT);
    }

    /**
     * Instruct the group node to evaluate itself right now based on the given
     * time delta from the last time this was evaluated.
     */
    public void evaluateModel() {

        // Update the bodies to send their force/torque values to ODE
        int size = bodyNodes.size();
        for(int i = 0; i < size; i++) {
            VRMLRigidBodyNodeType r_body =
                (VRMLRigidBodyNodeType)bodyNodes.get(i);

            r_body.updateODEFromNode();
        }

        if(vfPreferAccuracy) {
            odeWorld.step();
        } else {
            odeWorld.quickStep();
        }
    }

    /**
     * This node is about to be deleted due to a change in loaded world. Clear
     * up the ODE resources in use.
     */
    public void delete() {
        odeWorld.delete();
    }

    /**
     * Update everything from ODE, back into the node fields. This is done at
     * the start of the next frame so that all the events, listeners etc fire
     * at the right time in the event model.
     */
    public void updatePostSimulation() {
        int size = bodyNodes.size();
        for(int i = 0; i < size; i++) {
            VRMLRigidBodyNodeType r_body =
                (VRMLRigidBodyNodeType)bodyNodes.get(i);

            r_body.updateNodeFromODE();
        }
    }

    /**
     * Is this group enabled for use right now?
     *
     * @return true if this is enabled
     */
    public boolean isEnabled() {
        return vfEnabled;
    }

    /**
     * Set the global gravity direction for this collection.
     *
     * @param gravity An array of 3 values for the gravity vector
     */
    public void setGravity(float[] gravity) {
        vfGravity[0] = gravity[0];
        vfGravity[1] = gravity[1];
        vfGravity[2] = gravity[2];

        if(!inSetup) {
            odeWorld.setGravity(gravity[0], gravity[1], gravity[2]);
            hasChanged[FIELD_GRAVITY] = true;
            fireFieldChanged(FIELD_GRAVITY);
        }
    }

    /**
     * Get the current gravity vector applying to this collection.
     *
     * @param gravity An array to copy the values into
     */
    public void getGravity(float[] gravity) {
        gravity[0] = vfGravity[0];
        gravity[1] = vfGravity[1];
        gravity[2] = vfGravity[2];
    }

    /**
     * Get the number of valid joints that this collection has.
     *
     * @return A number greater than or equal to zero
     */
    public int numJoints() {
        return numJoints;
    }

    /**
     * Get the joint list, provides a live reference not a copy. The number of
     * valid values is available from numJoints();
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getJoints() {
        return vfJoints;
    }

    /**
     * Set the collection of Joint nodes that this collection should manage.
     * If passed a zero for numValid this method will remove all current
     * values.
     *
     * @param joints Array of new joint node instances to use
     * @param numValid The number of valid values to get from the array
     * @throw InvalidFieldValueException one of the provided nodes is not a
     *   X3DRigidJointNode instance
     */
    public void setJoints(VRMLNodeType[] joints, int numValid) {
        // need to clear out the old joints from the local world here.
        for(int i = 0; i < numJoints; i++) {
            if(joints[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)vfJoints[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if(impl != null) {
                    VRMLRigidJointNodeType r_joint =
                        (VRMLRigidJointNodeType)impl;
                    r_joint.setODEWorld(null, null);
                }
            } else if(vfJoints[i] instanceof VRMLRigidJointNodeType) {
                VRMLRigidJointNodeType r_joint =
                    (VRMLRigidJointNodeType)vfJoints[i];
                r_joint.setODEWorld(null, null);
            }

            odeJointGroup.empty();
            vfJoints[i] = null;
        }

        // Check the set of joints for valid types and then register
        // it with the local body. Anything other should result in an exception
        for(int i = 0; i < numValid; i++) {
            if(joints[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)joints[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if((impl != null) && !(impl instanceof VRMLRigidJointNodeType))
                    throw new InvalidFieldValueException(INVALID_JOINT_NODE_MSG);

                // Now do something here.
                if(impl != null) {
                    VRMLRigidJointNodeType r_joint = (VRMLRigidJointNodeType)impl;
                    r_joint.setODEWorld(odeWorld, odeJointGroup);
                }
            } else if(joints[i] instanceof VRMLRigidJointNodeType) {
                VRMLRigidJointNodeType r_joint = (VRMLRigidJointNodeType)joints[i];
                r_joint.setODEWorld(odeWorld, odeJointGroup);
            } else if(joints[i] != null)
                throw new InvalidFieldValueException(INVALID_JOINT_NODE_MSG);
        }

        // All the checks pass, now copy the values into the local array.
        if(vfJoints == null || vfJoints.length < numValid)
            vfJoints = new VRMLNodeType[numValid];

        System.arraycopy(joints, 0, vfJoints, 0, numValid);
        numJoints = numValid;

        if(!inSetup) {
            hasChanged[FIELD_JOINTS] = true;
            fireFieldChanged(FIELD_JOINTS);
        }
    }

    /**
     * Get the number of valid bodies that this body has as sub objects.
     *
     * @return A number greater than or equal to zero
     */
    public int numBodies() {
        return numBodies;
    }

    /**
     * Get the body list, provides a live reference not a copy. The number of
     * valid values is available from numBodies();
     *
     * @return An array of VRMLNodeTypes
     */
    public VRMLNodeType[] getBodies() {
        return vfBodies;
    }

    /**
     * Set the collection of RigidBody nodes that this body should manage.
     * If passed a zero for numValid this method will remove all current
     * values.
     *
     * @param bodies Array of new joint node instances to use
     * @param numValid The number of valid values to get from the array
     * @throw InvalidFieldValueException one of the provided nodes is not a
     *   X3DRigidBodyNode instance
     */
    public void setBodies(VRMLNodeType[] bodies, int numValid) {
        // need to clear out the old bodies from the local world here.
        bodyNodes.clear();

        for(int i = 0; i < numBodies; i++) {
            if(bodies[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)vfBodies[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if(impl != null) {
                    VRMLRigidBodyNodeType r_body = (VRMLRigidBodyNodeType)impl;
                    Body body = r_body.getODEBody();
                    odeWorld.deleteBody(body);
                }
            } else if(vfBodies[i] instanceof VRMLRigidBodyNodeType) {
                VRMLRigidBodyNodeType r_body = (VRMLRigidBodyNodeType)vfBodies[i];
                Body body = r_body.getODEBody();
                odeWorld.deleteBody(body);
            }

            vfBodies[i] = null;
        }

        // Check the sets of bodies for valid types and then register
        // it with the local body. Anything other should result in an exception
        for(int i = 0; i < numValid; i++) {
            if(bodies[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)bodies[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if((impl != null) && !(impl instanceof VRMLRigidBodyNodeType))
                    throw new InvalidFieldValueException(INVALID_BODY_NODE_MSG);

                // Now do something here.
                if(impl != null) {
                    VRMLRigidBodyNodeType r_body = (VRMLRigidBodyNodeType)impl;
                    r_body.setODEWorld(odeWorld);
                    bodyNodes.add(r_body);
                }
            } else if(bodies[i] instanceof VRMLRigidBodyNodeType) {
                VRMLRigidBodyNodeType r_body = (VRMLRigidBodyNodeType)bodies[i];
                r_body.setODEWorld(odeWorld);
                bodyNodes.add(r_body);
            } else if(bodies[i] != null)
                throw new InvalidFieldValueException(INVALID_BODY_NODE_MSG);
        }

        // All the checks pass, now copy the values into the local array.
        if(vfBodies == null || vfBodies.length < numValid)
            vfBodies = new VRMLNodeType[numValid];

        System.arraycopy(bodies, 0, vfBodies, 0, numValid);
        numBodies = numValid;

        if(!inSetup) {
            hasChanged[FIELD_BODIES] = true;
            fireFieldChanged(FIELD_BODIES);
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

        setWorld(new World());

        for(int i = 0; i < numJoints; i++) {
            if(vfJoints[i] != null)
                vfJoints[i].setupFinished();
        }

        for(int i = 0; i < numBodies; i++) {
            if(vfBodies[i] != null)
                vfBodies[i].setupFinished();
        }

        if(pCollider != null)
            pCollider.setupFinished();
        else if(vfCollider != null)
            vfCollider.setupFinished();

        if(vfCollider != null)
            vfCollider.setOwningWorld(odeWorld);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Change the reference count up or down by one.
     *
     * @param add true to increment the reference count, false to decrement
     * @return The new reference count
     */
    public void updateRefCount(int layer, boolean add) {
        boolean new_world = (layerIds == null) && add && (odeWorld == null);

        super.updateRefCount(layer, add);

        if(layerIds == null) {
            odeWorld.delete();
            setWorld(null);
        } else if(new_world) {
            setWorld(new World());
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
        if(index < 0  || index > LAST_COLLECTION_INDEX)
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
        return TypeConstants.RigidBodyCollectionNodeType;
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
            case FIELD_AUTO_DISABLE:
                fieldData.clear();
                fieldData.booleanValue = vfAutoDisable;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_BODIES:
                fieldData.clear();
                fieldData.nodeArrayValue = vfBodies;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = numBodies;
                break;

            case FIELD_CONSTANT_FORCE_MIX:
                fieldData.clear();
                fieldData.floatValue = vfConstantForceMix;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_CONTACT_SURFACE_THICKNESS:
                fieldData.clear();
                fieldData.floatValue = vfContactSurfaceThickness;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
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

            case FIELD_ERROR_CORRECTION:
                fieldData.clear();
                fieldData.floatValue = vfErrorCorrection;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_GRAVITY:
                fieldData.clear();
                fieldData.floatArrayValue = vfGravity;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ITERATIONS:
                fieldData.clear();
                fieldData.intValue = vfIterations;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_JOINTS:
                fieldData.clear();
                fieldData.nodeArrayValue = vfJoints;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = numJoints;
                break;

            case FIELD_MAX_CORRECTION_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfMaxCorrectionSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_PREFER_ACCURACY:
                fieldData.clear();
                fieldData.booleanValue = vfPreferAccuracy;
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
                case FIELD_AUTO_DISABLE:
                    destNode.setValue(destIndex, vfAutoDisable);
                    break;

                case FIELD_BODIES:
                    destNode.setValue(destIndex, vfBodies, numBodies);
                    break;

                case FIELD_CONSTANT_FORCE_MIX:
                    destNode.setValue(destIndex, vfConstantForceMix);
                    break;

                case FIELD_CONTACT_SURFACE_THICKNESS:
                    destNode.setValue(destIndex, vfContactSurfaceThickness);
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

                case FIELD_ERROR_CORRECTION:
                    destNode.setValue(destIndex, vfErrorCorrection);
                    break;

                case FIELD_GRAVITY:
                    destNode.setValue(destIndex, vfGravity, 3);
                    break;

                case FIELD_ITERATIONS:
                    destNode.setValue(destIndex, vfIterations);
                    break;

                case FIELD_JOINTS:
                    destNode.setValue(destIndex, vfJoints, numJoints);
                    break;

                case FIELD_MAX_CORRECTION_SPEED:
                    destNode.setValue(destIndex, vfMaxCorrectionSpeed);
                    break;

                case FIELD_PREFER_ACCURACY:
                    destNode.setValue(destIndex, vfPreferAccuracy);
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

            case FIELD_ENABLED:
                setEnabled(value);
                break;

            case FIELD_PREFER_ACCURACY:
                setPreferAccuracy(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a single boolean.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int value)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_ITERATIONS:
                setIterations(value);
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
            case FIELD_CONSTANT_FORCE_MIX:
                setConstantForceMix(value);
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

            case FIELD_CONTACT_SURFACE_THICKNESS:
                setContactSurfaceThickness(value);
                break;

            case FIELD_ERROR_CORRECTION:
                setErrorCorrection(value);
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
            case FIELD_GRAVITY:
                setGravity(value);
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
            case FIELD_BODIES:
                addBody(child);
                break;

            case FIELD_JOINTS:
                addJoint(child);
                break;

            case FIELD_COLLIDER:
                setCollider(child);
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
            case FIELD_BODIES:
                setBodies(children, numValid);
                break;

            case FIELD_JOINTS:
                setJoints(children, numValid);
                break;

            case FIELD_SET_CONTACTS:
                setContacts(children, numValid);
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the list of contacts for this frame.
     */
    private void setContacts(VRMLNodeType[] children, int numValid) {
        vfSetContacts.clear();
        vfSetContacts.ensureCapacity(numValid);

        for(int i = 0; i < numValid; i++) {
            if(children[i] instanceof BaseContact)
                vfSetContacts.add(children[i]);
            else if(children[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)children[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if((impl != null) && !(impl instanceof BaseContact))
                    throw new InvalidFieldValueException(CONTACT_PROTO_MSG);

                vfSetContacts.add(impl);
            } else if(children[i] != null) {
                throw new InvalidFieldValueException(CONTACT_NODE_MSG);
            }

            // null values are ignored.
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
            odeWorld.setAutoDisableBodies(state);
            hasChanged[FIELD_AUTO_DISABLE] = true;
            fireFieldChanged(FIELD_AUTO_DISABLE);
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
            odeWorld.setAngularVelocityDisableThreshold(value);
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
            odeWorld.setLinearVelocityDisableThreshold(value);
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
            odeWorld.setTimeDisableThreshold(value);
            hasChanged[FIELD_DISABLE_TIME] = true;
            fireFieldChanged(FIELD_DISABLE_TIME);
        }
    }

    /**
     * Set the value of the new ConstantForceMix field.
     *
     * @param value The new mix value
     */
    private void setConstantForceMix(float value) {
        if(value < 0)
            throw new InvalidFieldValueException(NEG_CFM_MSG + value);

        vfConstantForceMix = value;

        if(!inSetup) {
            odeWorld.setConstantForceMix(value);
            hasChanged[FIELD_CONSTANT_FORCE_MIX] = true;
            fireFieldChanged(FIELD_CONSTANT_FORCE_MIX);
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
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Set the new thickness for the contact surface.
     *
     * @param value The new value to use
     */
    private void setContactSurfaceThickness(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_SURFACE_MSG);

        vfContactSurfaceThickness = value;

        if(!inSetup) {
            odeWorld.setContactSurfaceThickness(value);
            hasChanged[FIELD_CONTACT_SURFACE_THICKNESS] = true;
            fireFieldChanged(FIELD_CONTACT_SURFACE_THICKNESS);
        }
    }

    /**
     * Set the new error correction parameter.
     *
     * @param value The new value to use
     */
    private void setErrorCorrection(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_ERR_CORR_MSG);

        vfErrorCorrection = value;

        if(!inSetup) {
            odeWorld.setErrorReductionParameter(value);
            hasChanged[FIELD_ERROR_CORRECTION] = true;
            fireFieldChanged(FIELD_ERROR_CORRECTION);
        }
    }

    /**
     * Set the new iterations value.
     *
     * @param value The new value to use
     */
    private void setIterations(int value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_ITERATIONS_MSG);

        vfIterations = value;

        if(!inSetup) {
            odeWorld.setStepInteractions(vfIterations);
            hasChanged[FIELD_ITERATIONS] = true;
            fireFieldChanged(FIELD_ITERATIONS);
        }
    }

    /**
     * Set the new maximum speed that the error correction can use.
     *
     * @param value The new value to use
     */
    private void setMaxCorrectionSpeed(float value)
        throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_ERR_SPEED_MSG);

        vfMaxCorrectionSpeed = value;

        if(!inSetup) {
            if(vfMaxCorrectionSpeed == -1)
                odeWorld.setMaxCorrectionVelocity(Float.POSITIVE_INFINITY);
            else
                odeWorld.setMaxCorrectionVelocity(vfMaxCorrectionSpeed);
            hasChanged[FIELD_MAX_CORRECTION_SPEED] = true;
            fireFieldChanged(FIELD_MAX_CORRECTION_SPEED);
        }
    }

    /**
     * Set the value of the new autoDisable field.
     *
     * @param state True for false
     */
    private void setPreferAccuracy(boolean state) {
        vfPreferAccuracy = state;

        if(!inSetup) {
            hasChanged[FIELD_PREFER_ACCURACY] = true;
            fireFieldChanged(FIELD_PREFER_ACCURACY);
        }
    }

    /**
     * Add a body during the setup interval.
     *
     * @param child The new body instance to check out
     */
    private void addBody(VRMLNodeType child)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new IllegalStateException("single addBody state error!");

        if(child instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)child).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLRigidBodyNodeType))
                throw new InvalidFieldValueException(INVALID_BODY_NODE_MSG);

            bodyNodes.add(impl);

        } else if((child != null) && !(child instanceof VRMLRigidBodyNodeType))
            throw new InvalidFieldValueException(INVALID_BODY_NODE_MSG);
        else
            bodyNodes.add(child);

        if(vfBodies == null || vfBodies.length == numBodies) {
            VRMLNodeType[] tmp = new VRMLNodeType[numBodies + 8];

            if(vfBodies != null)
                System.arraycopy(vfBodies, 0, tmp, 0, numBodies);
            vfBodies = tmp;
        }

        vfBodies[numBodies++] = child;
    }

    /**
     * Add a body during the setup interval.
     *
     * @param child The new body instance to check out
     */
    private void addJoint(VRMLNodeType child)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new IllegalStateException("single addBody state error!");

        if(child instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)child).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLRigidJointNodeType))
                throw new InvalidFieldValueException(INVALID_BODY_NODE_MSG);

        } else if((child != null) && !(child instanceof VRMLRigidJointNodeType))
            throw new InvalidFieldValueException(INVALID_BODY_NODE_MSG);

        if(vfJoints == null || vfJoints.length == numJoints) {
            VRMLNodeType[] tmp = new VRMLNodeType[numJoints + 8];

            if(vfJoints != null)
                System.arraycopy(vfJoints, 0, tmp, 0, numJoints);
            vfJoints = tmp;
        }

        vfJoints[numJoints++] = child;
    }

    /**
     * Set the node that should be used to represent the collider collection.
     * Setting a value of null will clear the current model in use and return
     * the system to a spherical model.
     *
     * @param coll The new instance to use or null
     * @throws InvalidFieldValueException This was not a valid node type
     */
    public void setCollider(VRMLNodeType coll)
        throws InvalidFieldValueException {

        VRMLNodeType old_node;
        String name = null;

        if(pCollider != null)
            old_node = pCollider;
        else
            old_node = vfCollider;

        if(coll instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)coll).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLNBodyGroupNodeType))
                throw new InvalidFieldValueException(COLL_PROTO_MSG);

            pCollider = (VRMLProtoInstance)coll;
            vfCollider = (VRMLNBodyGroupNodeType)impl;
        } else if(coll instanceof VRMLNBodyGroupNodeType) {
            pCollider = null;
            vfCollider = (VRMLNBodyGroupNodeType)coll;
        } else
            throw new InvalidFieldValueException(COLL_NODE_MSG);
    }

    /**
     * Set the parent world that this body belongs to. A null value clears
     * the world and indicates the physics model is no longer in use.
     *
     * @param wld The new world instance to use or null
     */
    private void setWorld(World wld) {
        odeWorld = wld;

        if(odeWorld != null) {
            odeWorld.setStepInteractions(vfIterations);
            odeWorld.setGravity(vfGravity[0], vfGravity[1], vfGravity[2]);
            odeWorld.setErrorReductionParameter(vfErrorCorrection);

            if(vfMaxCorrectionSpeed == -1)
                odeWorld.setMaxCorrectionVelocity(Float.POSITIVE_INFINITY);
            else
                odeWorld.setMaxCorrectionVelocity(vfMaxCorrectionSpeed);

            odeWorld.setAngularVelocityDisableThreshold(vfDisableAngularSpeed);
            odeWorld.setLinearVelocityDisableThreshold(vfDisableLinearSpeed);
            odeWorld.setTimeDisableThreshold(vfDisableTime);
            odeWorld.setAutoDisableBodies(vfAutoDisable);
            odeWorld.setContactSurfaceThickness(vfContactSurfaceThickness);
            odeWorld.setConstantForceMix(vfConstantForceMix);
        }

        for(int i = 0; i < numBodies; i++) {
            if(vfBodies[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)vfBodies[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if(impl != null)
                    ((VRMLRigidBodyNodeType)impl).setODEWorld(wld);

            } else if(vfBodies[i] != null) {
                ((VRMLRigidBodyNodeType)vfBodies[i]).setODEWorld(wld);
            }
        }

        for(int i = 0; i < numJoints; i++) {
            if(vfJoints[i] instanceof VRMLProtoInstance) {
                VRMLNodeType impl =
                    ((VRMLProtoInstance)vfJoints[i]).getImplementationNode();

                while((impl != null) && (impl instanceof VRMLProtoInstance))
                    impl = ((VRMLProtoInstance)impl).getImplementationNode();

                if(impl != null)
                    ((VRMLRigidJointNodeType)impl).setODEWorld(wld,
                                                               odeJointGroup);

            } else if(vfJoints[i] != null) {
                ((VRMLRigidJointNodeType)vfJoints[i]).setODEWorld(wld,
                                                                  odeJointGroup);
            }
        }
    }
}
