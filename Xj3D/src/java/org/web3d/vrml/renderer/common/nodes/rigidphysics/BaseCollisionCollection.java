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
import org.odejava.*;
import org.odejava.collision.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.odejava.ode.OdeConstants;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the Collidable node.
 * <p>
 *
 * The base class provides most of the basic functionality, including
 * interacting with the ODE portions. However, for returning the scene graph
 * object appropriate for the renderer will require the derived class to take
 * care of it.
 * <p>
 *
 * The X3D definition of CollisionCollection is:
 * <pre>
 * CollisionCollection : X3DChildNode {
 *   SFBool [in,out] autoSendContacts TRUE
 *   MFNode [in,out] collidables NULL  [X3DNBodyCollisionSpaceNode, X3DNBodyCollidableNode]
 *   SFBool [in,out] enabled     TRUE
 *   SFNode [in,out] metadata    NULL  [X3DMetadataObject]
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public abstract class BaseCollisionCollection extends AbstractNode
    implements VRMLNBodyGroupNodeType, VRMLChildNodeType {

    // Field index constants

    /** The field index for collidables */
    protected static final int FIELD_COLLIDABLES = LAST_NODE_INDEX + 1;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 2;

    /** The field index for bounce */
    protected static final int FIELD_BOUNCE = LAST_NODE_INDEX + 3;

    /** The field index for bounceSpeed */
    protected static final int FIELD_BOUNCE_SPEED = LAST_NODE_INDEX + 4;

    /** The field index for frictionCoefficients */
    protected static final int FIELD_FRICTION_COEFFICIENTS = LAST_NODE_INDEX + 5;

    /** The field index for slipCoefficients */
    protected static final int FIELD_SLIP_COEFFICIENTS = LAST_NODE_INDEX + 6;

    /** The field index for surfaceSpeed */
    protected static final int FIELD_SURFACE_SPEED = LAST_NODE_INDEX + 7;

    /** The field index for appliedParameters */
    protected static final int FIELD_APPLIED_PARAMETERS = LAST_NODE_INDEX + 8;

    /** The field index for softnessConstantForceMix */
    protected static final int FIELD_SOFTNESS_CFM = LAST_NODE_INDEX + 9;

    /** The field index for softnessErrorCorrection */
    protected static final int FIELD_SOFTNESS_ERP = LAST_NODE_INDEX + 10;

    /** Last index used by this base node */
    protected static final int LAST_COLLECTION_INDEX = FIELD_SOFTNESS_ERP;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_COLLECTION_INDEX + 1;

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLLIDABLE_PROTO_MSG =
        "Collidables field proto value does not describe a CollisionSpace " +
        " node or NBodyCollidableNode type.";

    /** Message for when the node in setValue() is not a primitive */
    protected static final String COLLIDABLE_NODE_MSG =
        "Collidables field node value does not describe a CollisionSpace " +
        " node or NBodyCollidableNode type.";

    /** Message when a negative bounce value is given */
    protected static final String NEG_BOUNCE_MSG =
        "bounce is required to be non-negative: ";

    /** Message when a negative bounce speed is given */
    protected static final String NEG_BOUNCE_SPEED_MSG =
        "minBounceSpeed is required to be non-negative: ";

    /** Message when a negative error correction value is given */
    protected static final String NEG_ERP_MSG =
        "softnessErrorCorrection is required to be non-negative: ";

    /** Message when a negative force mix value is given */
    protected static final String NEG_CFM_MSG =
        "softnessConstantForceMix is required to be non-negative: ";

    private static final int PARAM_BOUNCE = 1;
    private static final int PARAM_USER_FRICTION = 2;
    private static final int PARAM_FRICTION_COEFFICIENT_2 = 3;
    private static final int PARAM_ERROR_REDUCTION = 4;
    private static final int PARAM_CONSTANT_FORCE = 5;
    private static final int PARAM_SPEED_1 = 6;
    private static final int PARAM_SPEED_2 = 7;
    private static final int PARAM_SLIP_1 = 8;
    private static final int PARAM_SLIP_2 = 9;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Mapping of parameter field names to the ODE constants */
    protected static final HashMap parameterIdMap;

    /**
     * Separate param map to map the strings to our local internal
     * constants. The values in OdeConstants are not really constants at all -
     * they're automatically generated each time the application starts up.
     * GRRrrrr. Means we can't use them in switch statements. :(
     */
    protected static final HashMap internalParamIdMap;

    // The VRML field values

    /** The value of the collidables field */
    private ArrayList vfCollidables;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** The value of the bounce field */
    protected float vfBounce;

    /** The value of the bounceSpeed field */
    protected float vfBounceSpeed;

    /** The value of the frictionCoefficients field */
    protected float[] vfFrictionCoefficients;

    /** The value of the slipCoefficients field */
    protected float[] vfSlipCoefficients;

    /** The value of the surfaceSpeed field */
    protected float[] vfSurfaceSpeed;

    /** The value of the appliedParameters field */
    protected String[] vfAppliedParameters;

    /** The number of valid parameters to apply */
    protected int numAppliedParameters;

    /** The value of the softnessErrorCorrection field */
    protected float vfSoftnessErrorCorrection;

    /** The value of the softnessConstantForceMix field */
    protected float vfSoftnessConstantForceMix;

    // Other vars

    /** Internal scratch var for dealing with added/removed children */
    private VRMLNodeType[] nodeTmp;

    /** List of nodes that we've determined are child spaces */
    private ArrayList spaceChildren;

    /** List of nodes that we've determined are child geometry */
    private ArrayList geomChildren;

    /** The root space to which we add all the children */
    private Space rootSpace;

    /** Java collision handler if we have some interested sensors */
    private JavaCollision javaCollider;

    /** Bulk contact handler used by this collision group */
    private BulkContact contactCollection;

    /** The ODE world instance for controlling this simulation */
    private World odeWorld;

    /**
     * The mode flag converted to a bit mask that is passed to the collision
     * system. This will always have dContactFDir1 and dContactApprox1 applied
     * regardless of the user setting.
     */
    private int surfaceModeFlags;

    /** Matching int version of the vfAppliedParameters */
    private int[] appliedParamFlags;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_COLLIDABLES,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_COLLIDABLES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "collidables");
        fieldDecl[FIELD_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "bounce");
        fieldDecl[FIELD_BOUNCE_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "minBounceSpeed");
        fieldDecl[FIELD_FRICTION_COEFFICIENTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "frictionCoefficients");
        fieldDecl[FIELD_SLIP_COEFFICIENTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "slipCoefficients");
        fieldDecl[FIELD_SURFACE_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "surfaceSpeed");
        fieldDecl[FIELD_APPLIED_PARAMETERS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "appliedParameters");
        fieldDecl[FIELD_SOFTNESS_ERP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "softnessErrorCorrection");
        fieldDecl[FIELD_SOFTNESS_CFM] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "softnessConstantForceMix");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_COLLIDABLES);
        fieldMap.put("collidables", idx);
        fieldMap.put("set_collidables", idx);
        fieldMap.put("collidables_changed", idx);

        idx = new Integer(FIELD_BOUNCE);
        fieldMap.put("bounce", idx);
        fieldMap.put("set_bounce", idx);
        fieldMap.put("bounce_changed", idx);

        idx = new Integer(FIELD_BOUNCE_SPEED);
        fieldMap.put("minBounceSpeed", idx);
        fieldMap.put("set_minBounceSpeed", idx);
        fieldMap.put("minBounceSpeed_changed", idx);

        idx = new Integer(FIELD_FRICTION_COEFFICIENTS);
        fieldMap.put("frictionCoefficients", idx);
        fieldMap.put("set_frictionCoefficients", idx);
        fieldMap.put("frictionCoefficients_changed", idx);

        idx = new Integer(FIELD_SLIP_COEFFICIENTS);
        fieldMap.put("slipCoefficients", idx);
        fieldMap.put("set_slipCoefficients", idx);
        fieldMap.put("slipCoefficients_changed", idx);

        idx = new Integer(FIELD_SURFACE_SPEED);
        fieldMap.put("surfaceSpeed", idx);
        fieldMap.put("set_surfaceSpeed", idx);
        fieldMap.put("surfaceSpeed_changed", idx);

        idx = new Integer(FIELD_APPLIED_PARAMETERS);
        fieldMap.put("appliedParameters", idx);
        fieldMap.put("set_appliedParameters", idx);
        fieldMap.put("appliedParameters_changed", idx);

        idx = new Integer(FIELD_SOFTNESS_CFM);
        fieldMap.put("softnessConstantForceMix", idx);
        fieldMap.put("set_softnessConstantForceMix", idx);
        fieldMap.put("softnessConstantForceMix_changed", idx);

        idx = new Integer(FIELD_SOFTNESS_ERP);
        fieldMap.put("softnessErrorCorrection", idx);
        fieldMap.put("set_softnessErrorCorrection", idx);
        fieldMap.put("softnessErrorCorrection_changed", idx);


        parameterIdMap = new HashMap();
        parameterIdMap.put("BOUNCE", new Integer(OdeConstants.dContactBounce));
        parameterIdMap.put("USER_FRICTION",
                           new Integer(OdeConstants.dContactFDir1));
        parameterIdMap.put("FRICTION_COEFFICIENT-2",
                           new Integer(OdeConstants.dContactMu2));
        parameterIdMap.put("ERROR_REDUCTION",
                           new Integer(OdeConstants.dContactSoftERP));
        parameterIdMap.put("CONSTANT_FORCE",
                           new Integer(OdeConstants.dContactSoftCFM));
        parameterIdMap.put("SPEED-1",
                           new Integer(OdeConstants.dContactMotion1));
        parameterIdMap.put("SPEED-2",
                           new Integer(OdeConstants.dContactMotion2));
        parameterIdMap.put("SLIP-1",
                           new Integer(OdeConstants.dContactSlip1));
        parameterIdMap.put("SLIP-2",
                           new Integer(OdeConstants.dContactSlip2));

        internalParamIdMap = new HashMap();
        internalParamIdMap.put("BOUNCE", new Integer(PARAM_BOUNCE));
        internalParamIdMap.put("USER_FRICTION",
                           new Integer(PARAM_USER_FRICTION));
        internalParamIdMap.put("FRICTION_COEFFICIENT-2",
                           new Integer(PARAM_FRICTION_COEFFICIENT_2));
        internalParamIdMap.put("ERROR_REDUCTION",
                           new Integer(PARAM_ERROR_REDUCTION));
        internalParamIdMap.put("CONSTANT_FORCE",
                           new Integer(PARAM_CONSTANT_FORCE));
        internalParamIdMap.put("SPEED-1", new Integer(PARAM_SPEED_1));
        internalParamIdMap.put("SPEED-2", new Integer(PARAM_SPEED_2));
        internalParamIdMap.put("SLIP-1", new Integer(PARAM_SLIP_1));
        internalParamIdMap.put("SLIP-2", new Integer(PARAM_SLIP_2));
    }

    /**
     * Construct a new default CollisionCollection node object.
     */
    public BaseCollisionCollection() {
        super("CollisionCollection");

        vfCollidables = new ArrayList();
        vfEnabled = true;
        vfFrictionCoefficients = new float[2];
        vfSlipCoefficients = new float[2];
        vfSurfaceSpeed = new float[2];

        vfBounceSpeed = 0.1f;
        vfSoftnessErrorCorrection = 0.8f;
        vfSoftnessConstantForceMix = 0.0001f;

        vfAppliedParameters = new String[] {"BOUNCE"};
        appliedParamFlags = new int[] {PARAM_BOUNCE};
        numAppliedParameters = 1;

        surfaceModeFlags = OdeConstants.dContactApprox1 |
                           OdeConstants.dContactBounce;

        hasChanged = new boolean[NUM_FIELDS];

        spaceChildren = new ArrayList();
        geomChildren = new ArrayList();

        rootSpace = new HashSpace();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseCollisionCollection(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("enabled");
            VRMLFieldData field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("bounce");
            field = node.getFieldValue(index);
            vfBounce = field.floatValue;

            index = node.getFieldIndex("minBounceSpeed");
            field = node.getFieldValue(index);
            vfBounceSpeed = field.floatValue;

            index = node.getFieldIndex("softnessConstantForceMix");
            field = node.getFieldValue(index);
            vfSoftnessConstantForceMix = field.floatValue;

            index = node.getFieldIndex("softnessErrorCorrection");
            field = node.getFieldValue(index);
            vfSoftnessErrorCorrection = field.floatValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNBodyGroupNodeType
    //----------------------------------------------------------

    /**
     * Set the owning world for this collision space. This is a hack to get
     * around a bad assumption made by ODE - that there is only ever one world
     * current at time. This is the owner world of this collsion system.
     *
     * @param wld The world instance we're using.
     */
    public void setOwningWorld(World wld) {
        if(odeWorld != null)
            odeWorld.delete();
        else {
            javaCollider = new JavaCollision(wld);
            javaCollider.setSurfaceMode(surfaceModeFlags);
            javaCollider.setSurfaceBounce(0);
            javaCollider.setSurfaceBounceVel(vfBounceSpeed);
            javaCollider.setSurfaceSoftCfm(vfSoftnessConstantForceMix);
            javaCollider.setSurfaceSoftErp(vfSoftnessErrorCorrection);
            javaCollider.setSurfaceMu(vfFrictionCoefficients[0]);
            javaCollider.setSurfaceMu2(vfFrictionCoefficients[1]);
            javaCollider.setSurfaceMotion1(vfSurfaceSpeed[0]);
            javaCollider.setSurfaceMotion2(vfSurfaceSpeed[1]);
            javaCollider.setSurfaceSlip1(vfSlipCoefficients[0]);
            javaCollider.setSurfaceSlip2(vfSlipCoefficients[1]);

    //        javaCollider.setMaxStepContactsPerNearcallback(8192);

            contactCollection =
                new BulkContact(javaCollider.getContactIntBuffer(),
                                javaCollider.getContactFloatBuffer());
        }

        odeWorld = wld;
    }

    /**
     * This node is about to be deleted due to a change in loaded world. Clear
     * up the ODE resources in use.
     */
    public void delete() {
        rootSpace.delete();
        javaCollider.delete();
        odeWorld.delete();
    }

    /**
     * Tell the group to evaluate its contents now. This will generate contacts
     * as needed.
     */
    public void evaluateCollisions() {

// TODO:
// odejava has an implementation problem here and does not recurse into nested
// spaces. See Bug ID #12
// https://odejava.dev.java.net/issues/show_bug.cgi?id=12
//
// So, we have to manually do this calling the check on all contained spaces.

        javaCollider.collide(rootSpace);

        // Need to run through the contacts here and do some processing to
        // check for all the fixed objects and remove the appropriate body
        // reference from the contact object.
        int num_contacts = javaCollider.getContactCount();

        if(num_contacts != 0) {
            contactCollection.load(num_contacts);

            // go through and globally apply all the settings to every contact
            // instance. ODEJava.collision native code says that if any flag
            // is set, then use the values from the user-provided ones.
            for(int i = 0; i < num_contacts; i++) {
                // friction coefficient 1 must always be provided
                contactCollection.setIndex(i);
                contactCollection.setMu(vfFrictionCoefficients[0]);

                for(int j = 0; j < numAppliedParameters; j++) {
                    switch(appliedParamFlags[j]) {
                        case PARAM_BOUNCE:
                            contactCollection.setBounce(vfBounce);
                            contactCollection.setBounceVel(vfBounceSpeed);
                            break;

                        case PARAM_USER_FRICTION:
                            // do nothing for this
                            break;

                        case PARAM_FRICTION_COEFFICIENT_2:
                            contactCollection.setMu2(vfFrictionCoefficients[1]);
                            break;

                        case PARAM_ERROR_REDUCTION:
                            contactCollection.setSoftErp(vfSoftnessErrorCorrection);
                            break;

                        case PARAM_CONSTANT_FORCE:
                            contactCollection.setSoftCfm(vfSoftnessConstantForceMix);
                            break;

                        case PARAM_SPEED_1:
                            contactCollection.setMotion1(vfSurfaceSpeed[0]);
                            break;

                        case PARAM_SPEED_2:
                            contactCollection.setMotion2(vfSurfaceSpeed[1]);
                            break;

                        case PARAM_SLIP_1:
                            contactCollection.setSlip1(vfSlipCoefficients[0]);
                            break;

                        case PARAM_SLIP_2:
                            contactCollection.setSlip2(vfSlipCoefficients[1]);
                            break;
                    }
                }
            }
        }
    }

    /**
     * Fetch the number of contacts that were generated during the last
     * evaluation. Needed so that we can iterate through the BulkContact object
     * returned from {@link getContacts()}.
     *
     * @return A non-negative size indicator
     */
    public int numContacts() {
        return javaCollider.getContactCount();
    }

    /**
     * Fetch the most recent set of contacts that have been evaluated for this
     * space. The bulk object can be used to step through all the available
     * contacts that were generated.
     *
     * @return The set of bulk contacts generated
     */
    public BulkContact getContacts() {
        return contactCollection;
    }

    /**
     * Apply the contacts right now. All processing is complete, so it's
     * fine to continue the evaluation from before.
     */
    public void applyContacts() {
        contactCollection.commit();
        javaCollider.applyContacts();
    }

    /**
     * Is this group enabled for use right now?
     *
     * @return true if this is enabled
     */
    public boolean isEnabled() {
        return vfEnabled;
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

        // Set the parent space on nodes before calling their setupFinished()
        // methods, as they like it like that so that they don't go off
        // creating their own environment space.
        int size = spaceChildren.size();
        for(int i = 0; i < size; i++) {
            VRMLNBodySpaceNodeType n =
                (VRMLNBodySpaceNodeType)spaceChildren.get(i);
            n.setParentODESpace(rootSpace);
        }

        size = vfCollidables.size();
        for(int i = 0; i < size; i++) {
            VRMLNodeType n = (VRMLNodeType)vfCollidables.get(i);
            if(n != null)
                n.setupFinished();
        }

        size = geomChildren.size();
        for(int i = 0; i < size; i++) {
            VRMLNBodyCollidableNodeType n =
                (VRMLNBodyCollidableNodeType)geomChildren.get(i);

            PlaceableGeom geom = n.getODEGeometry();
            rootSpace.addGeom(geom);
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.nBodyCollisionCollectionNodeType;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A shape of this field's information
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
            case FIELD_COLLIDABLES:
                int num_kids = vfCollidables.size();

                if((nodeTmp == null) || (nodeTmp.length < num_kids))
                    nodeTmp = new VRMLNodeType[num_kids];
                vfCollidables.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_BOUNCE:
                fieldData.clear();
                fieldData.floatValue = vfBounce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_BOUNCE_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfBounce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_FRICTION_COEFFICIENTS:
                fieldData.clear();
                fieldData.floatArrayValue = vfFrictionCoefficients;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_SLIP_COEFFICIENTS:
                fieldData.clear();
                fieldData.floatArrayValue = vfSlipCoefficients;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_SURFACE_SPEED:
                fieldData.clear();
                fieldData.floatArrayValue = vfSurfaceSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_APPLIED_PARAMETERS:
                fieldData.clear();
                fieldData.stringArrayValue = vfAppliedParameters;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = numAppliedParameters;
                break;

            case FIELD_SOFTNESS_CFM:
                fieldData.clear();
                fieldData.floatValue = vfSoftnessConstantForceMix;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SOFTNESS_ERP:
                fieldData.clear();
                fieldData.floatValue = vfSoftnessErrorCorrection;
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
                case FIELD_COLLIDABLES:
                    int num_kids = vfCollidables.size();

                    if((nodeTmp == null) || (nodeTmp.length < num_kids))
                        nodeTmp = new VRMLNodeType[num_kids];
                    vfCollidables.toArray(nodeTmp);
                    destNode.setValue(destIndex, nodeTmp, num_kids);
                    break;

                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_BOUNCE:
                    destNode.setValue(destIndex, vfBounce);
                    break;

                case FIELD_BOUNCE_SPEED:
                    destNode.setValue(destIndex, vfBounceSpeed);
                    break;

                case FIELD_FRICTION_COEFFICIENTS:
                    destNode.setValue(destIndex, vfFrictionCoefficients, 2);
                    break;

                case FIELD_SLIP_COEFFICIENTS:
                    destNode.setValue(destIndex, vfSlipCoefficients, 2);
                    break;

                case FIELD_SURFACE_SPEED:
                    destNode.setValue(destIndex, vfSurfaceSpeed, 2);
                    break;

                case FIELD_APPLIED_PARAMETERS:
                    destNode.setValue(destIndex,
                                      vfAppliedParameters,
                                      numAppliedParameters);
                    break;

                case FIELD_SOFTNESS_CFM:
                    destNode.setValue(destIndex, vfSoftnessConstantForceMix);
                    break;

                case FIELD_SOFTNESS_ERP:
                    destNode.setValue(destIndex, vfSoftnessErrorCorrection);
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
            case FIELD_ENABLED:
                setEnabled(value);
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
            case FIELD_BOUNCE:
                setBounce(value);
                break;

            case FIELD_BOUNCE_SPEED:
                setBounceSpeed(value);
                break;

            case FIELD_SOFTNESS_CFM:
                setSoftnessConstantForceMix(value);
                break;

            case FIELD_SOFTNESS_ERP:
                setSoftnessErrorCorrection(value);
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
            case FIELD_FRICTION_COEFFICIENTS:
                setFrictionCoefficients(value);
                break;

            case FIELD_SLIP_COEFFICIENTS:
                setSlipCoefficients(value);
                break;

            case FIELD_SURFACE_SPEED:
                setSurfaceSpeed(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid items to use from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_APPLIED_PARAMETERS:
                setAppliedParameters(value, numValid);
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
            case FIELD_COLLIDABLES:
                if(!inSetup)
                    clearCollidables();

                addCollidable(node);

                if(!inSetup) {
                    hasChanged[FIELD_COLLIDABLES] = true;
                    fireFieldChanged(FIELD_COLLIDABLES);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_COLLIDABLES:
                if(!inSetup)
                    clearCollidables();

                for(int i = 0; i < numValid; i++ )
                    addCollidable(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_COLLIDABLES] = true;
                    fireFieldChanged(FIELD_COLLIDABLES);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the new state value of the enabled field.
     *
     * @param state True or false
     */
    private void setEnabled(boolean state) {
        vfEnabled = state;

        if(!inSetup) {
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Set the new state value of the softnessErrorCorrection field.
     *
     * @param value A non-negative value
     */
    private void setSoftnessErrorCorrection(float value) {
        if(value < 0)
            throw new InvalidFieldValueException(NEG_ERP_MSG + value);

        vfSoftnessErrorCorrection = value;

        if(!inSetup) {
            javaCollider.setSurfaceSoftErp(vfSoftnessErrorCorrection);
            hasChanged[FIELD_SOFTNESS_ERP] = true;
            fireFieldChanged(FIELD_SOFTNESS_ERP);
        }
    }

    /**
     * Set the new state value of the softnessConstantForceMix field.
     *
     * @param value A non-negative value
     */
    private void setSoftnessConstantForceMix(float value) {
        if(value < 0)
            throw new InvalidFieldValueException(NEG_CFM_MSG + value);

        vfSoftnessConstantForceMix = value;

        if(!inSetup) {
            javaCollider.setSurfaceSoftCfm(vfSoftnessConstantForceMix);
            hasChanged[FIELD_SOFTNESS_CFM] = true;
            fireFieldChanged(FIELD_SOFTNESS_CFM);
        }
    }

    /**
     * Set the new bounce value to use.
     *
     * @param value The new value to use
     */
    private void setBounce(float value) throws InvalidFieldValueException {

        if(value < 0)
            throw new InvalidFieldValueException(NEG_BOUNCE_MSG + value);

        vfBounce = value;

        if(!inSetup) {
            javaCollider.setSurfaceBounce(value);
            hasChanged[FIELD_BOUNCE] = true;
            fireFieldChanged(FIELD_BOUNCE);
        }
    }

    /**
     * Set the new bounce speed to use.
     *
     * @param value The new value to use
     */
    private void setBounceSpeed(float value) {
        if(value < 0)
            throw new InvalidFieldValueException(NEG_BOUNCE_SPEED_MSG + value);

        vfBounceSpeed = value;

        if(!inSetup) {
            javaCollider.setSurfaceBounceVel(value);
            hasChanged[FIELD_BOUNCE_SPEED] = true;
            fireFieldChanged(FIELD_BOUNCE_SPEED);
        }
    }

    /**
     * Set the new contact normal to use.
     *
     * @param value The new value to use
     */
    private void setFrictionCoefficients(float[] value) {
        vfFrictionCoefficients[0] = value[0];
        vfFrictionCoefficients[1] = value[1];

        if(!inSetup) {
            javaCollider.setSurfaceMu(value[0]);
            javaCollider.setSurfaceMu2(value[1]);
            hasChanged[FIELD_FRICTION_COEFFICIENTS] = true;
            fireFieldChanged(FIELD_FRICTION_COEFFICIENTS);
        }
    }

    /**
     * Set the new contact normal to use.
     *
     * @param value The new value to use
     */
    private void setSlipCoefficients(float[] value) {
        vfSlipCoefficients[0] = value[0];
        vfSlipCoefficients[1] = value[1];

        if(!inSetup) {
            javaCollider.setSurfaceSlip1(value[0]);
            javaCollider.setSurfaceSlip2(value[1]);
            hasChanged[FIELD_SLIP_COEFFICIENTS] = true;
            fireFieldChanged(FIELD_SLIP_COEFFICIENTS);
        }
    }

    /**
     * Set the new surface motion coefficients to use.
     *
     * @param value The new value to use
     */
    private void setSurfaceSpeed(float[] value) {
        vfSurfaceSpeed[0] = value[0];
        vfSurfaceSpeed[1] = value[1];

        if(!inSetup) {
            javaCollider.setSurfaceMotion1(value[0]);
            javaCollider.setSurfaceMotion2(value[1]);
            hasChanged[FIELD_SURFACE_SPEED] = true;
            fireFieldChanged(FIELD_SURFACE_SPEED);
        }
    }

    /**
     * Set the new applied parameter list to use.
     *
     * @param value The new values to use
     * @param numValid The number of valid values in the array
     */
    private void setAppliedParameters(String[] value, int numValid) {
        if(vfAppliedParameters.length < numValid) {
            vfAppliedParameters = new String[numValid];
            appliedParamFlags = new int[numValid];
        }


        surfaceModeFlags = OdeConstants.dContactApprox1;


        for(int i = 0; i < numValid; i++) {
            Integer val = (Integer)parameterIdMap.get(value[i]);

            if(val != null) {
                surfaceModeFlags |= val.intValue();

                val = (Integer)internalParamIdMap.get(value[i]);
                appliedParamFlags[i] = val.intValue();
            }

            vfAppliedParameters[i] = value[i];
        }

        numAppliedParameters = numValid;

        if(!inSetup) {
            javaCollider.setSurfaceMode(surfaceModeFlags);
            hasChanged[FIELD_SURFACE_SPEED] = true;
            fireFieldChanged(FIELD_SURFACE_SPEED);
        }
    }

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearCollidables() {
        int num_kids = vfCollidables.size();

        if((nodeTmp == null) || (nodeTmp.length < num_kids))
            nodeTmp = new VRMLNodeType[num_kids];

        vfCollidables.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++)
            updateRefs(nodeTmp[i], false);

        if(num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfCollidables.clear();

        spaceChildren.clear();
        geomChildren.clear();
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addCollidable(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLNBodyCollidableNodeType) {
            vfCollidables.add(node);
            geomChildren.add(node);
        } else if(node instanceof VRMLNBodySpaceNodeType) {
            vfCollidables.add(node);
            spaceChildren.add(node);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLNBodyCollidableNodeType) {
                vfCollidables.add(impl);
                geomChildren.add(impl);
            } else if(impl instanceof VRMLNBodySpaceNodeType) {
                vfCollidables.add(impl);
                spaceChildren.add(impl);
            } else
                throw new InvalidFieldValueException(COLLIDABLE_PROTO_MSG);
        } else if(node != null)
            throw new InvalidFieldValueException(COLLIDABLE_NODE_MSG);

        if(node != null)
            updateRefs(node, false);

        if(!inSetup)
            stateManager.registerAddedNode(node);
    }
}
