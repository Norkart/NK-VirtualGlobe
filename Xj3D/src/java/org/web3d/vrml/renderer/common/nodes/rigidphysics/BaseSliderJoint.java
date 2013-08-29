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
import org.odejava.JointSlider;
import org.odejava.World;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the SliderJoint node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class BaseSliderJoint extends BaseJointNode {

    // Field index constants

    /** The field index for axis */
    protected static final int FIELD_AXIS = LAST_JOINT_INDEX + 1;

    /** The field index for minSeparation */
    protected static final int FIELD_MIN_SEPARATION = LAST_JOINT_INDEX + 2;

    /** The field index for maxSeparation */
    protected static final int FIELD_MAX_SEPARATION = LAST_JOINT_INDEX + 3;

    /** The field index for separation */
    protected static final int FIELD_SEPARATION = LAST_JOINT_INDEX + 4;

    /** The field index for separationRate */
    protected static final int FIELD_SEPARATION_RATE = LAST_JOINT_INDEX + 5;

    /** The field index for stopBounce */
    protected static final int FIELD_STOP_BOUNCE = LAST_JOINT_INDEX + 6;

    /** The field index for stopErrorCorrection */
    protected static final int FIELD_STOP_ERROR_CORRECTION = LAST_JOINT_INDEX + 7;

    /** Last index used by this base node */
    protected static final int LAST_SLIDER_INDEX = FIELD_STOP_ERROR_CORRECTION;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_SLIDER_INDEX + 1;

    /** Message when the minSeparation value is out of range */
    protected static final String MIN_SEPARATION_MSG =
        "The minSeparation value is not allowed to be negative.";

    /** Message when the maxSeparation value is out of range */
    protected static final String MAX_SEPARATION_MSG =
        "The maxSeparation value is not allowed to be negative.";

    /** Message when the user attempt to write to the separation field */
    private static final String SEP_WRITE_MSG =
        "separationAngle is outputOnly and cannot be set";

    /** Message when the user attempt to write to the separationRate field */
    private static final String SEPR_WRITE_MSG =
        "separationRate is outputOnly and cannot be set";

    /** Message when the stopBounce value is out of range */
    protected static final String BOUNCE_RANGE_MSG =
        "The stopBounce value is out of the required range [0,1]: ";

    /** Message when the stopBounce value is out of range */
    protected static final String STOP_ERROR_RANGE_MSG =
        "The stopErrorCorrection value is out of the required range [0,1]: ";

    /** Index list if the output capable fields */
    private static final int[] outputFields;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    // The VRML field values

    /** The value of the axis field */
    protected float[] vfAxis;

    /** The value of the minSeparation field */
    protected float vfMinSeparation;

    /** The value of the maxSeparation field */
    protected float vfMaxSeparation;

    /** The value of the separation field */
    protected float vfSeparation;

    /** The value of the separationRate field */
    protected float vfSeparationRate;

    /** The value of the stopBounce field */
    protected float vfStopBounce;

    /** The value of the stopErrorCorrection field */
    protected float vfStopErrorCorrection;

    // Other vars

    /** The ODE representation of the joint */
    protected JointSlider odeJoint;

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
            FIELD_SEPARATION,
            FIELD_SEPARATION_RATE,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "axis");
        fieldDecl[FIELD_BODY1] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body1");
        fieldDecl[FIELD_BODY2] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "body2");
        fieldDecl[FIELD_MIN_SEPARATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "minSeparation");
        fieldDecl[FIELD_MAX_SEPARATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxSeparation");
        fieldDecl[FIELD_SEPARATION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "separation");
        fieldDecl[FIELD_SEPARATION_RATE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "separationRate");
        fieldDecl[FIELD_STOP_BOUNCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stopBounce");
        fieldDecl[FIELD_STOP_ERROR_CORRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "stopErrorCorrection");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_AXIS);
        fieldMap.put("axis", idx);
        fieldMap.put("set_axis", idx);
        fieldMap.put("axis_changed", idx);

        idx = new Integer(FIELD_BODY1);
        fieldMap.put("body1", idx);
        fieldMap.put("set_body1", idx);
        fieldMap.put("body1_changed", idx);

        idx = new Integer(FIELD_BODY2);
        fieldMap.put("body2", idx);
        fieldMap.put("set_body2", idx);
        fieldMap.put("body2_changed", idx);

        idx = new Integer(FIELD_MIN_SEPARATION);
        fieldMap.put("minSeparation", idx);
        fieldMap.put("set_minSeparation", idx);
        fieldMap.put("minSeparation_changed", idx);

        idx = new Integer(FIELD_MAX_SEPARATION);
        fieldMap.put("maxSeparation", idx);
        fieldMap.put("set_maxSeparation", idx);
        fieldMap.put("maxSeparation_changed", idx);

        idx = new Integer(FIELD_STOP_BOUNCE);
        fieldMap.put("stopBounce", idx);
        fieldMap.put("set_stopBounce", idx);
        fieldMap.put("stopBounce_changed", idx);

        idx = new Integer(FIELD_STOP_ERROR_CORRECTION);
        fieldMap.put("stopErrorCorrection", idx);
        fieldMap.put("set_stopErrorCorrection", idx);
        fieldMap.put("stopErrorCorrection_changed", idx);

        fieldMap.put("separation", new Integer(FIELD_SEPARATION));
        fieldMap.put("separationRate", new Integer(FIELD_SEPARATION_RATE));
    }

    /**
     * Construct a new default slider joint node object.
     */
    public BaseSliderJoint() {
        super("SliderJoint");

        vfAxis = new float[3];
        vfMinSeparation = -(float)Math.PI;
        vfMaxSeparation = (float)Math.PI;
        vfStopBounce = 0;
        vfStopErrorCorrection = 0.8f;

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
    public BaseSliderJoint(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("axis");
            VRMLFieldData field = node.getFieldValue(index);
            vfAxis[0] = field.floatArrayValue[0];
            vfAxis[1] = field.floatArrayValue[1];
            vfAxis[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("minSeparation");
            field = node.getFieldValue(index);
            vfMinSeparation = field.floatValue;

            index = node.getFieldIndex("maxSeparation");
            field = node.getFieldValue(index);
            vfMaxSeparation = field.floatValue;

            index = node.getFieldIndex("stopBounce");
            field = node.getFieldValue(index);
            vfStopBounce = field.floatValue;

            index = node.getFieldIndex("stopErrorCorrection");
            field = node.getFieldValue(index);
            vfStopErrorCorrection = field.floatValue;
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
     * @param wld The new world instance to use or null
     * @param grp The group that this joint should belong to
     */
    public void setODEWorld(World wld, JointGroup grp) {
        if(wld != null) {
            odeJoint = new JointSlider(wld, grp);

            Body body_1 = null;
            Body body_2 = null;

            if(vfBody1 != null)
                body_1 = vfBody1.getODEBody();

            if(vfBody2 != null)
                body_2 = vfBody2.getODEBody();

            odeJoint.attach(body_1, body_2);
            odeJoint.setAxis(vfAxis[0], vfAxis[1], vfAxis[2]);

            odeJoint.setMinimumPosition(vfMinSeparation);
            odeJoint.setMaximumPosition(vfMaxSeparation);
            odeJoint.setStopBounce(vfStopBounce);
            odeJoint.setStopERP(vfStopErrorCorrection);
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
        if (index < 0  || index > LAST_SLIDER_INDEX)
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
        odeJoint.setAxis(vfAxis[0], vfAxis[1], vfAxis[2]);

        odeJoint.setMinimumPosition(vfMinSeparation);
        odeJoint.setMaximumPosition(vfMaxSeparation);
        odeJoint.setStopBounce(vfStopBounce);
        odeJoint.setStopERP(vfStopErrorCorrection);
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
            case FIELD_AXIS:
                fieldData.clear();
                fieldData.floatArrayValue = vfAxis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_MIN_SEPARATION:
                fieldData.clear();
                fieldData.floatValue = vfMinSeparation;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MAX_SEPARATION:
                fieldData.clear();
                fieldData.floatValue = vfMaxSeparation;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SEPARATION:
                fieldData.clear();
                fieldData.floatValue = vfSeparation;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SEPARATION_RATE:
                fieldData.clear();
                fieldData.floatValue = vfSeparationRate;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP_BOUNCE:
                fieldData.clear();
                fieldData.floatValue = vfStopBounce;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_STOP_ERROR_CORRECTION:
                fieldData.clear();
                fieldData.floatValue = vfStopErrorCorrection;
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
                case FIELD_AXIS:
                    destNode.setValue(destIndex, vfAxis, 3);
                    break;

                case FIELD_MIN_SEPARATION:
                    destNode.setValue(destIndex, vfMinSeparation);
                    break;

                case FIELD_MAX_SEPARATION:
                    destNode.setValue(destIndex, vfMaxSeparation);
                    break;

                case FIELD_SEPARATION:
                    destNode.setValue(destIndex, vfSeparation);
                    break;

                case FIELD_SEPARATION_RATE:
                    destNode.setValue(destIndex, vfSeparationRate);
                    break;

                case FIELD_STOP_BOUNCE:
                    destNode.setValue(destIndex, vfStopBounce);
                    break;

                case FIELD_STOP_ERROR_CORRECTION:
                    destNode.setValue(destIndex, vfStopErrorCorrection);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("SingleAxisJoint.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("SingleAxisJoint.sendRoute: Invalid field value: " +
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
            case FIELD_MIN_SEPARATION:
                setMinSeparation(value);
                break;

            case FIELD_MAX_SEPARATION:
                setMaxSeparation(value);
                break;

            case FIELD_STOP_BOUNCE:
                setStopBounce(value);
                break;

            case FIELD_STOP_ERROR_CORRECTION:
                setStopErrorCorrection(value);
                break;

            case FIELD_SEPARATION:
                throw new InvalidFieldAccessException(SEP_WRITE_MSG);

            case FIELD_SEPARATION_RATE:
                throw new InvalidFieldAccessException(SEPR_WRITE_MSG);

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
            case FIELD_AXIS:
                setAxis(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the minimum dist stop position. This value should be non-negative.
     *
     * @param dist The amount of distance to use
     * @throws InvalidFieldValueException The value was negative
     */
    private void setMinSeparation(float dist)
        throws InvalidFieldValueException {

        if(dist < 0)
            throw new InvalidFieldValueException(MIN_SEPARATION_MSG);

        vfMinSeparation = dist;

        if(!inSetup) {
            odeJoint.setMinimumPosition(dist);
            hasChanged[FIELD_MIN_SEPARATION] = true;
            fireFieldChanged(FIELD_MIN_SEPARATION);
        }
    }

    /**
     * Set the maximum dist stop position. This value should be non-negative.
     *
     * @param dist The amount of distance to use
     * @throws InvalidFieldValueException The value was negative
     */
    private void setMaxSeparation(float dist)
        throws InvalidFieldValueException {

        if(dist < 0)
            throw new InvalidFieldValueException(MAX_SEPARATION_MSG);

        vfMaxSeparation = dist;

        if(!inSetup) {
            odeJoint.setMaximumPosition(dist);
            hasChanged[FIELD_MAX_SEPARATION] = true;
            fireFieldChanged(FIELD_MAX_SEPARATION);
        }
    }

    /**
     * Set the anchor point location for this hinge.
     *
     * @param pos An array of 3 values for the position
     */
    private void setAxis(float[] pos) {
        vfAxis[0] = pos[0];
        vfAxis[1] = pos[1];
        vfAxis[2] = pos[2];

        if(!inSetup) {
            odeJoint.setAxis(pos[0], pos[1], pos[2]);
            hasChanged[FIELD_AXIS] = true;
            fireFieldChanged(FIELD_AXIS);
        }
    }

    /**
     * Set the amount of stop bounce. This value should be between 0 and 1.
     * 0 is no bounce at all, 1 is full bounce.
     *
     * @param bounce The amount of bounce to use
     * @throws InvalidFieldValueException The value was not between [0,1]
     */
    private void setStopBounce(float bounce)
        throws InvalidFieldValueException {

        if(bounce < 0 || bounce > 1)
            throw new InvalidFieldValueException(BOUNCE_RANGE_MSG + bounce);

        vfStopBounce = bounce;

        if(!inSetup) {
            odeJoint.setStopBounce(bounce);
            hasChanged[FIELD_STOP_BOUNCE] = true;
            fireFieldChanged(FIELD_STOP_BOUNCE);
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
    private void setStopErrorCorrection(float error)
        throws InvalidFieldValueException {

        if(error < 0 || error > 1)
            throw new InvalidFieldValueException(STOP_ERROR_RANGE_MSG + error);

        vfStopErrorCorrection = error;

        if(!inSetup) {
            odeJoint.setStopERP(error);
            hasChanged[FIELD_STOP_ERROR_CORRECTION] = true;
            fireFieldChanged(FIELD_STOP_ERROR_CORRECTION);
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
