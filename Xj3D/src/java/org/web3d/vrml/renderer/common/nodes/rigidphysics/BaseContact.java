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
import org.odejava.PlaceableGeom;
import org.odejava.collision.BulkContact;
import org.odejava.ode.OdeConstants;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.util.IntHashMap;

/**
 * Implementation of the Contact node for rigid body physics.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseContact extends AbstractNode {

    // Field index constants

    /** The field index for appliedParameters */
    protected static final int FIELD_APPLIED_PARAMETERS = LAST_NODE_INDEX + 1;

    /** The field index for body1 */
    protected static final int FIELD_BODY1 = LAST_NODE_INDEX + 2;

    /** The field index for body2 */
    protected static final int FIELD_BODY2 = LAST_NODE_INDEX + 3;

    /** The field index for bounce */
    protected static final int FIELD_BOUNCE = LAST_NODE_INDEX + 4;

    /** The field index for contactNormal */
    protected static final int FIELD_CONTACT_NORMAL = LAST_NODE_INDEX + 5;

    /** The field index for depth */
    protected static final int FIELD_DEPTH = LAST_NODE_INDEX + 6;

    /** The field index for frictionCoefficients */
    protected static final int FIELD_FRICTION_COEFFICIENTS = LAST_NODE_INDEX + 7;

    /** The field index for frictionDirection */
    protected static final int FIELD_FRICTION_DIRECTION = LAST_NODE_INDEX + 8;

    /** The field index for geometry1 */
    protected static final int FIELD_GEOMETRY1 = LAST_NODE_INDEX + 9;

    /** The field index for geometry2 */
    protected static final int FIELD_GEOMETRY2 = LAST_NODE_INDEX + 10;

    /** The field index for minBounceSpeed */
    protected static final int FIELD_MIN_BOUNCE_SPEED = LAST_NODE_INDEX + 11;

    /** The field index for position */
    protected static final int FIELD_POSITION = LAST_NODE_INDEX + 12;

    /** The field index for slipCoefficients */
    protected static final int FIELD_SLIP_COEFFICIENTS = LAST_NODE_INDEX + 13;

    /** The field index for surfaceSpeed */
    protected static final int FIELD_SURFACE_SPEED = LAST_NODE_INDEX + 14;

    /** The field index for softnessConstantForceMix */
    protected static final int FIELD_SOFTNESS_CFM = LAST_NODE_INDEX + 15;

    /** The field index for softnessErrorCorrection */
    protected static final int FIELD_SOFTNESS_ERP = LAST_NODE_INDEX + 16;

    /** Last index used by this base node */
    protected static final int LAST_CONTACT_INDEX = FIELD_SOFTNESS_ERP;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_CONTACT_INDEX + 1;

    /** Message for when the proto is not a Body */
    protected static final String BODY_PROTO_MSG =
        "Proto does not describe a Body object";

    /** Message for when the node in setValue() is not a Body */
    protected static final String BODY_NODE_MSG =
        "Node does not describe a Body object";

    /** Message for when the proto is not a collidable */
    protected static final String GEOMETRY_PROTO_MSG =
        "Proto does not describe a X3DCollidableNode object";

    /** Message for when the node in setValue() is not a collidable */
    protected static final String GEOMETRY_NODE_MSG =
        "Node does not describe a X3DCollidableNode object";

    /** Message when a negative bounce value is given */
    protected static final String NEG_BOUNCE_MSG =
        "bounce is required to be non-negative: ";

    /** Message when a negative bounce speed is given */
    protected static final String NEG_BOUNCE_SPEED_MSG =
        "minBounceSpeed is required to be non-negative: ";

    /** Message when a negative error correction value is given */
    protected static final String NEG_ERP_MSG =
        "softnessErrorReduction is required to be non-negative: ";

    /** Message when a negative force mix value is given */
    protected static final String NEG_CFM_MSG =
        "softnessConstantForceMix is required to be non-negative: ";

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Mapping of parameter field names to the ODE constants */
    protected static final HashMap parameterIdMap;

    // The VRML field values

    /** The value of the body1 field */
    protected VRMLRigidBodyNodeType vfBody1;

    /** The proto version of the body1 */
    protected VRMLProtoInstance pBody1;

    /** The value of the body2 field */
    protected VRMLRigidBodyNodeType vfBody2;

    /** The proto version of the body2 */
    protected VRMLProtoInstance pBody2;

    /** The value of the geom1 field */
    protected VRMLNBodyCollidableNodeType vfGeometry1;

    /** The proto version of the geom1 */
    protected VRMLProtoInstance pGeometry1;

    /** The value of the geom2 field */
    protected VRMLNBodyCollidableNodeType vfGeometry2;

    /** The proto version of the geom2 */
    protected VRMLProtoInstance pGeometry2;

    /** The value of the appliedParameters field */
    protected String[] vfAppliedParameters;

    /** The number of valid parameters to apply */
    protected int numAppliedParameters;

    /** The value of the bounce field */
    protected float vfBounce;

    /** The value of the minBounceSpeed field */
    protected float vfMinBounceSpeed;

    /** The value of the depth field */
    protected float vfDepth;

    /** The value of the contactNormal field */
    protected float[] vfContactNormal;

    /** The value of the frictionCoefficients field */
    protected float[] vfFrictionCoefficients;

    /** The value of the frictionDirection field */
    protected float[] vfFrictionDirection;

    /** The value of the position field */
    protected float[] vfPosition;

    /** The value of the slipCoefficients field */
    protected float[] vfSlipCoefficients;

    /** The value of the surfaceSpeed field */
    protected float[] vfSurfaceSpeed;

    /** The value of the softnessErrorCorrection field */
    protected float vfSoftnessErrorCorrection;

    /** The value of the softnessConstantForceMix field */
    protected float vfSoftnessConstantForceMix;

    // Other vars

    /** The ODE object to send values to */
    protected BulkContact odeContact;

    /** The index of this contact's place oin the big array */
    protected int contactIndex;

    /**
     * The mode flag converted to a bit mask that is passed to the collision
     * system. This will always have dContactFDir1 and dContactApprox1 applied
     * regardless of the user setting.
     */
    private int surfaceModeFlags;

    /**
     * Static constructor to initialise all the field values.
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_BODY1,
            FIELD_BODY2,
            FIELD_GEOMETRY1,
            FIELD_GEOMETRY2
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_APPLIED_PARAMETERS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "appliedParameters");
        fieldDecl[FIELD_BODY1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body1");
        fieldDecl[FIELD_BODY2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body2");
        fieldDecl[FIELD_DEPTH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "depth");
        fieldDecl[FIELD_GEOMETRY1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "geometry1");
        fieldDecl[FIELD_GEOMETRY2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "geometry2");
        fieldDecl[FIELD_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "bounce");
        fieldDecl[FIELD_CONTACT_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "contactNormal");
        fieldDecl[FIELD_FRICTION_COEFFICIENTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "frictionCoefficients");
        fieldDecl[FIELD_FRICTION_DIRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "frictionDirection");
        fieldDecl[FIELD_MIN_BOUNCE_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "minBounceSpeed");
        fieldDecl[FIELD_POSITION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "position");
        fieldDecl[FIELD_SLIP_COEFFICIENTS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "slipCoefficients");
        fieldDecl[FIELD_SURFACE_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "surfaceSpeed");
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

        idx = new Integer(FIELD_BODY1);
        fieldMap.put("body1", idx);
        fieldMap.put("set_body1", idx);
        fieldMap.put("body1_changed", idx);

        idx = new Integer(FIELD_BODY2);
        fieldMap.put("body2", idx);
        fieldMap.put("set_body2", idx);
        fieldMap.put("body2_changed", idx);

        idx = new Integer(FIELD_GEOMETRY1);
        fieldMap.put("geometry1", idx);
        fieldMap.put("set_geometry1", idx);
        fieldMap.put("geometry1_changed", idx);

        idx = new Integer(FIELD_GEOMETRY2);
        fieldMap.put("geometry2", idx);
        fieldMap.put("set_geometry2", idx);
        fieldMap.put("geometry2_changed", idx);

        idx = new Integer(FIELD_BOUNCE);
        fieldMap.put("bounce", idx);
        fieldMap.put("set_bounce", idx);
        fieldMap.put("bounce_changed", idx);

        idx = new Integer(FIELD_MIN_BOUNCE_SPEED);
        fieldMap.put("minBounceSpeed", idx);
        fieldMap.put("set_minBounceSpeed", idx);
        fieldMap.put("minBounceSpeed_changed", idx);

        idx = new Integer(FIELD_CONTACT_NORMAL);
        fieldMap.put("contactNormal", idx);
        fieldMap.put("set_contactNormal", idx);
        fieldMap.put("contactNormal_changed", idx);

        idx = new Integer(FIELD_FRICTION_COEFFICIENTS);
        fieldMap.put("frictionCoefficients", idx);
        fieldMap.put("set_frictionCoefficients", idx);
        fieldMap.put("frictionCoefficients_changed", idx);

        idx = new Integer(FIELD_FRICTION_DIRECTION);
        fieldMap.put("frictionDirection", idx);
        fieldMap.put("set_frictionDirection", idx);
        fieldMap.put("frictionDirection_changed", idx);

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

        idx = new Integer(FIELD_DEPTH);
        fieldMap.put("depth", idx);
        fieldMap.put("set_depth", idx);
        fieldMap.put("depth_changed", idx);

        idx = new Integer(FIELD_POSITION);
        fieldMap.put("position", idx);
        fieldMap.put("set_position", idx);
        fieldMap.put("position_changed", idx);

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
    }

    /**
     * Construct a new default contact node object.
     */
    public BaseContact() {
        super("Contact");

        vfContactNormal = new float[] { 0, 1, 0 };
        vfFrictionCoefficients = new float[2];
        vfFrictionDirection = new float[] { 0, 1, 0 };
        vfPosition = new float[3];
        vfSlipCoefficients = new float[2];
        vfSurfaceSpeed = new float[2];
        vfDepth = 0;
        vfMinBounceSpeed = 0.1f;
        vfSoftnessErrorCorrection = 0.8f;
        vfSoftnessConstantForceMix = 0.0001f;

        vfAppliedParameters = new String[] {"BOUNCE"};
        numAppliedParameters = 1;

        surfaceModeFlags = OdeConstants.dContactApprox1 |
                           OdeConstants.dContactBounce;

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
    public BaseContact(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("bounce");
            VRMLFieldData field = node.getFieldValue(index);
            vfBounce = field.floatValue;

            index = node.getFieldIndex("minBounceSpeed");
            field = node.getFieldValue(index);
            vfMinBounceSpeed = field.floatValue;

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

        if(pBody1 != null)
            pBody1.setupFinished();
        else if(vfBody1 != null)
            vfBody1.setupFinished();

        if(pBody2 != null)
            pBody2.setupFinished();
        else if(vfBody2 != null)
            vfBody2.setupFinished();

        if(pGeometry1 != null)
            pGeometry1.setupFinished();
        else if(vfGeometry1 != null)
            vfGeometry1.setupFinished();

        if(pGeometry2 != null)
            pGeometry2.setupFinished();
        else if(vfGeometry2 != null)
            vfGeometry2.setupFinished();
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
        return TypeConstants.NONE;
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
        if(index < 0  || index > LAST_CONTACT_INDEX)
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
            case FIELD_BODY1:
                fieldData.clear();
                if(pBody1 != null)
                    fieldData.nodeValue = pBody1;
                else
                    fieldData.nodeValue = vfBody1;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_BODY2:
                fieldData.clear();
                if(pBody2 != null)
                    fieldData.nodeValue = pBody2;
                else
                    fieldData.nodeValue = vfBody2;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_BOUNCE:
                fieldData.clear();
                fieldData.floatValue = vfBounce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_CONTACT_NORMAL:
                fieldData.clear();
                fieldData.floatArrayValue = vfContactNormal;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_DEPTH:
                fieldData.clear();
                fieldData.floatValue = vfDepth;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_FRICTION_COEFFICIENTS:
                fieldData.clear();
                fieldData.floatArrayValue = vfFrictionCoefficients;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_FRICTION_DIRECTION:
                fieldData.clear();
                fieldData.floatArrayValue = vfFrictionDirection;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_GEOMETRY1:
                fieldData.clear();
                if(pGeometry1 != null)
                    fieldData.nodeValue = pGeometry1;
                else
                    fieldData.nodeValue = vfGeometry1;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEOMETRY2:
                fieldData.clear();
                if(pGeometry2 != null)
                    fieldData.nodeValue = pGeometry2;
                else
                    fieldData.nodeValue = vfGeometry2;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_MIN_BOUNCE_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfBounce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_POSITION:
                fieldData.clear();
                fieldData.floatArrayValue = vfPosition;
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
                case FIELD_BODY1:
                    if(pBody1 != null)
                        destNode.setValue(destIndex, pBody1);
                    else
                        destNode.setValue(destIndex, vfBody1);
                    break;

                case FIELD_BODY2:
                    if(pBody2 != null)
                        destNode.setValue(destIndex, pBody2);
                    else
                        destNode.setValue(destIndex, vfBody2);
                    break;

                case FIELD_GEOMETRY1:
                    if(pGeometry1 != null)
                        destNode.setValue(destIndex, pGeometry1);
                    else
                        destNode.setValue(destIndex, vfGeometry1);
                    break;

                case FIELD_GEOMETRY2:
                    if(pGeometry2 != null)
                        destNode.setValue(destIndex, pGeometry2);
                    else
                        destNode.setValue(destIndex, vfGeometry2);
                    break;

                case FIELD_BOUNCE:
                    destNode.setValue(destIndex, vfBounce);
                    break;

                case FIELD_CONTACT_NORMAL:
                    destNode.setValue(destIndex, vfContactNormal, 3);
                    break;

                case FIELD_DEPTH:
                    destNode.setValue(destIndex, vfDepth);
                    break;

                case FIELD_FRICTION_COEFFICIENTS:
                    destNode.setValue(destIndex, vfFrictionCoefficients, 2);
                    break;

                case FIELD_FRICTION_DIRECTION:
                    destNode.setValue(destIndex, vfFrictionDirection, 3);
                    break;

                case FIELD_MIN_BOUNCE_SPEED:
                    destNode.setValue(destIndex, vfMinBounceSpeed);
                    break;

                case FIELD_POSITION:
                    destNode.setValue(destIndex, vfPosition, 3);
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
            System.err.println("BaseJointNode.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseJointNode.sendRoute: Invalid field value: " +
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
            case FIELD_BOUNCE:
                setBounce(value);
                break;

            case FIELD_DEPTH:
                setDepth(value);
                break;

            case FIELD_MIN_BOUNCE_SPEED:
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
            case FIELD_CONTACT_NORMAL:
                setContactNormal(value);
                break;

            case FIELD_FRICTION_COEFFICIENTS:
                setFrictionCoefficients(value);
                break;

            case FIELD_FRICTION_DIRECTION:
                setFrictionDirection(value);
                break;

            case FIELD_POSITION:
                setPosition(value);
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
            case FIELD_BODY1:
                setBody1(child);
                break;

            case FIELD_BODY2:
                setBody2(child);
                break;

            case FIELD_GEOMETRY1:
                setGeometry1(child);
                break;

            case FIELD_GEOMETRY2:
                setGeometry2(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the contact object to use for this instance, and update this
     * object straight away.
     *
     */
    void setContact(BulkContact contact,
                    int index,
                    IntHashMap bodyIdMap,
                    IntHashMap geomIdMap) {

        odeContact = contact;
        contactIndex = index;

        // Should we also store the geometry ID map?

        // Now run through and update everything locally.
        vfBounce = odeContact.getBounce(contactIndex);
        vfDepth = odeContact.getDepth(contactIndex);
        vfMinBounceSpeed = odeContact.getBounceVel(contactIndex);
        vfSoftnessErrorCorrection = odeContact.getSoftErp(contactIndex);
        vfSoftnessConstantForceMix = odeContact.getSoftCfm(contactIndex);

        odeContact.getNormal(vfContactNormal, contactIndex);
        odeContact.getPosition(vfPosition, contactIndex);
        odeContact.getFdir1(vfFrictionDirection, contactIndex);

        vfFrictionCoefficients[0] = odeContact.getMu(contactIndex);
        vfFrictionCoefficients[1] = odeContact.getMu2(contactIndex);

        vfSlipCoefficients[0] = odeContact.getSlip1(contactIndex);
        vfSlipCoefficients[1] = odeContact.getSlip2(contactIndex);
        vfSurfaceSpeed[0] = odeContact.getMotion1(contactIndex);
        vfSurfaceSpeed[1] = odeContact.getMotion2(contactIndex);

        // Should we do it this way, or copy the default params back
        // from the surface and try to map it back to the global
        // values?
//        odeContact.setMode(surfaceModeFlags, contactIndex);

        // Need to find the appropriate body instance....
        int body_id = odeContact.getBodyID1(contactIndex);
        vfBody1 = (VRMLRigidBodyNodeType)bodyIdMap.get(body_id);
        pBody1 = null;

        body_id = odeContact.getBodyID2(contactIndex);
        vfBody2 = (VRMLRigidBodyNodeType)bodyIdMap.get(body_id);
        pBody2 = null;

        int geom_id = odeContact.getGeomID1(contactIndex);
        vfGeometry1 = (VRMLNBodyCollidableNodeType)geomIdMap.get(geom_id);
        pGeometry1 = null;

        geom_id = odeContact.getGeomID2(contactIndex);
        vfGeometry2 = (VRMLNBodyCollidableNodeType)geomIdMap.get(geom_id);
        pGeometry2 = null;
    }

    /**
     * Get the index that this contact used for updating the contact array.
     *
     * @return the current valid index
     */
    int getContactIndex() {
        return contactIndex;
    }

    /**
     * Fetch the most recent set of ODE contacts array that were registered for
     * this contact instances
     *
     * @return The set of bulk contacts generated
     */
    BulkContact getContacts() {
        return odeContact;
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
            odeContact.setSoftErp(value, contactIndex);
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
            odeContact.setSoftCfm(value, contactIndex);
            hasChanged[FIELD_SOFTNESS_CFM] = true;
            fireFieldChanged(FIELD_SOFTNESS_CFM);
        }
    }

    /**
     * Set the new bounce value to use.
     *
     * @param value The new value to use
     */
    private void setBounce(float value) {
        if(value < 0)
            throw new InvalidFieldValueException(NEG_BOUNCE_MSG + value);

        vfBounce = value;

        if(!inSetup) {
            odeContact.setBounce(value, contactIndex);
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

        vfMinBounceSpeed = value;

        if(!inSetup) {
            odeContact.setBounceVel(value, contactIndex);
            hasChanged[FIELD_MIN_BOUNCE_SPEED] = true;
            fireFieldChanged(FIELD_MIN_BOUNCE_SPEED);
        }
    }

    /**
     * Set the new bounce value to use.
     *
     * @param value The new value to use
     */
    private void setDepth(float value) {
        vfDepth = value;

        if(!inSetup) {
            odeContact.setDepth(value, contactIndex);
            hasChanged[FIELD_DEPTH] = true;
            fireFieldChanged(FIELD_DEPTH);
        }
    }

    /**
     * Set the new contact normal to use.
     *
     * @param value The new value to use
     */
    private void setContactNormal(float[] value) {
        vfContactNormal[0] = value[0];
        vfContactNormal[1] = value[1];
        vfContactNormal[2] = value[2];

        if(!inSetup) {
            odeContact.setNormal(value, contactIndex);
            hasChanged[FIELD_CONTACT_NORMAL] = true;
            fireFieldChanged(FIELD_CONTACT_NORMAL);
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
            odeContact.setMu(value[0], contactIndex);
            odeContact.setMu2(value[1], contactIndex);
            hasChanged[FIELD_FRICTION_COEFFICIENTS] = true;
            fireFieldChanged(FIELD_FRICTION_COEFFICIENTS);
        }
    }

    /**
     * Set the new contact normal to use.
     *
     * @param value The new value to use
     */
    private void setFrictionDirection(float[] value) {
        vfFrictionDirection[0] = value[0];
        vfFrictionDirection[1] = value[1];
        vfFrictionDirection[2] = value[2];


        if(!inSetup) {
            odeContact.setFdir1(value, contactIndex);
            hasChanged[FIELD_FRICTION_DIRECTION] = true;
            fireFieldChanged(FIELD_FRICTION_DIRECTION);
        }
    }

    /**
     * Set the new contact normal to use.
     *
     * @param value The new value to use
     */
    private void setPosition(float[] value) {
        vfPosition[0] = value[0];
        vfPosition[1] = value[1];
        vfPosition[2] = value[2];

        if(!inSetup) {
            odeContact.setPosition(value, contactIndex);
            hasChanged[FIELD_POSITION] = true;
            fireFieldChanged(FIELD_POSITION);
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
            odeContact.setSlip1(value[0], contactIndex);
            odeContact.setSlip2(value[1], contactIndex);
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
            odeContact.setMotion1(value[0], contactIndex);
            odeContact.setMotion2(value[1], contactIndex);
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
        if(vfAppliedParameters.length < numValid)
            vfAppliedParameters = new String[numValid];


        surfaceModeFlags = OdeConstants.dContactApprox1;

        for(int i = 0; i < numValid; i++) {
            Integer val = (Integer)parameterIdMap.get(value[i]);

            if(val != null)
                surfaceModeFlags |= val.intValue();

            vfAppliedParameters[i] = value[i];
        }

        numAppliedParameters = numValid;

        if(!inSetup) {
            odeContact.setMode(surfaceModeFlags, contactIndex);
            hasChanged[FIELD_SURFACE_SPEED] = true;
            fireFieldChanged(FIELD_SURFACE_SPEED);
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
    private void setBody1(VRMLNodeType body)
        throws InvalidFieldValueException {

        VRMLRigidBodyNodeType node = null;
        VRMLNodeType old_node;

        if(pBody1 != null)
            old_node = pBody1;
        else
            old_node = vfBody1;

        if(body instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)body).getImplementationNode();

            pBody1 = (VRMLProtoInstance)body;

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)body).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLRigidBodyNodeType))
                throw new InvalidFieldValueException(BODY_PROTO_MSG);

            node = (VRMLRigidBodyNodeType)impl;
        } else if(body instanceof VRMLRigidBodyNodeType) {
            pBody1 = null;
            node = (VRMLRigidBodyNodeType)body;
        } else if(body != null) {
            throw new InvalidFieldValueException(BODY_NODE_MSG);
        }

        vfBody1 = (VRMLRigidBodyNodeType)node;

        if(body != null)
            updateRefs(body, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(body != null)
                stateManager.registerAddedNode(body);

            if(vfBody1 != null) {
                Body o_body = vfBody1.getODEBody();
                int addr = o_body.getNativeAddr();
                odeContact.setBodyID1(addr, contactIndex);
            } else
                odeContact.setBodyID1(0, contactIndex);

            hasChanged[FIELD_BODY1] = true;
            fireFieldChanged(FIELD_BODY1);
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
    private void setBody2(VRMLNodeType body)
        throws InvalidFieldValueException {

        VRMLRigidBodyNodeType node = null;
        VRMLNodeType old_node;

        if(pBody2 != null)
            old_node = pBody2;
        else
            old_node = vfBody2;

        if(body instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)body).getImplementationNode();

            pBody2 = (VRMLProtoInstance)body;

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)body).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLRigidBodyNodeType))
                throw new InvalidFieldValueException(BODY_PROTO_MSG);

            node = (VRMLRigidBodyNodeType)impl;
        } else if(body instanceof VRMLRigidBodyNodeType) {
            pBody2 = null;
            node = (VRMLRigidBodyNodeType)body;
        } else if(body != null) {
            throw new InvalidFieldValueException(BODY_NODE_MSG);
        }

        vfBody2 = (VRMLRigidBodyNodeType)node;

        if(body != null)
            updateRefs(body, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(body != null)
                stateManager.registerAddedNode(body);

            if(vfBody2 != null) {
                Body o_body = vfBody2.getODEBody();
                int addr = o_body.getNativeAddr();
                odeContact.setBodyID2(addr, contactIndex);
            } else
                odeContact.setBodyID2(0, contactIndex);

            hasChanged[FIELD_BODY2] = true;
            fireFieldChanged(FIELD_BODY2);
        }
    }

    /**
     * Set node content as replacement for the geom1 field. This
     * checks only for basic node representation. If a concrete node needs a
     * specific set of nodes, it should override this method to check.
     *
     * @param geom The new geom representation.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setGeometry1(VRMLNodeType geom)
        throws InvalidFieldValueException {

        VRMLNBodyCollidableNodeType node = null;
        VRMLNodeType old_node;

        if(pGeometry1 != null)
            old_node = pGeometry1;
        else
            old_node = vfGeometry1;

        if(geom instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geom).getImplementationNode();

            pGeometry1 = (VRMLProtoInstance)geom;

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)geom).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLNBodyCollidableNodeType))
                throw new InvalidFieldValueException(GEOMETRY_PROTO_MSG);

            node = (VRMLNBodyCollidableNodeType)impl;
        } else if(geom instanceof VRMLNBodyCollidableNodeType) {
            pGeometry1 = null;
            node = (VRMLNBodyCollidableNodeType)geom;
        } else if(geom != null) {
            throw new InvalidFieldValueException(GEOMETRY_NODE_MSG);
        }

        vfGeometry1 = (VRMLNBodyCollidableNodeType)node;

        if(geom!= null)
            updateRefs(geom, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(geom != null)
                stateManager.registerAddedNode(geom);

            if(vfGeometry1 != null) {
                PlaceableGeom o_geom  = vfGeometry1.getODEGeometry();
                int addr = o_geom.getNativeAddr();
                odeContact.setGeomID1(addr, contactIndex);
            } else
                odeContact.setGeomID1(0, contactIndex);

            hasChanged[FIELD_GEOMETRY1] = true;
            fireFieldChanged(FIELD_GEOMETRY1);
        }
    }

    /**
     * Set node content as replacement for the geom1 field. This
     * checks only for basic node representation. If a concrete node needs a
     * specific set of nodes, it should override this method to check.
     *
     * @param geom The new geom representation.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setGeometry2(VRMLNodeType geom)
        throws InvalidFieldValueException {

        VRMLNBodyCollidableNodeType node = null;
        VRMLNodeType old_node;

        if(pGeometry2 != null)
            old_node = pGeometry2;
        else
            old_node = vfGeometry2;

        if(geom instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geom).getImplementationNode();

            pGeometry2 = (VRMLProtoInstance)geom;

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)geom).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLNBodyCollidableNodeType))
                throw new InvalidFieldValueException(GEOMETRY_PROTO_MSG);

            node = (VRMLNBodyCollidableNodeType)impl;
        } else if(geom instanceof VRMLNBodyCollidableNodeType) {
            pGeometry2 = null;
            node = (VRMLNBodyCollidableNodeType)geom;
        } else if(geom != null) {
            throw new InvalidFieldValueException(GEOMETRY_NODE_MSG);
        }

        vfGeometry2 = (VRMLNBodyCollidableNodeType)node;

        if(geom != null)
            updateRefs(geom, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(geom != null)
                stateManager.registerAddedNode(geom);

            if(vfGeometry2 != null) {
                PlaceableGeom o_geom  = vfGeometry2.getODEGeometry();
                int addr = o_geom.getNativeAddr();
                odeContact.setGeomID2(addr, contactIndex);
            } else
                odeContact.setGeomID2(0, contactIndex);

            hasChanged[FIELD_GEOMETRY2] = true;
            fireFieldChanged(FIELD_GEOMETRY2);
        }
    }
}
