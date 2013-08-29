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
import org.odejava.JointBall;
import org.odejava.JointGroup;
import org.odejava.World;
import org.odejava.ode.Ode;
import org.odejava.ode.OdeConstants;
import org.odejava.ode.SWIGTYPE_p_float;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the BallJoint node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public abstract class BaseBallJoint extends BaseJointNode {

    // Field index constants

    /** The field index for anchorPoint */
    protected static final int FIELD_ANCHOR_POINT = LAST_JOINT_INDEX + 1;

    /** The field index for body1AnchorPoint */
    protected static final int FIELD_BODY1_ANCHOR_POINT = LAST_JOINT_INDEX + 2;

    /** The field index for body2AnchorPoint */
    protected static final int FIELD_BODY2_ANCHOR_POINT = LAST_JOINT_INDEX + 3;

    /** Last index used by this base node */
    protected static final int LAST_BALL_INDEX = FIELD_BODY2_ANCHOR_POINT;

    /** Number of fields in this node */
    private static final int NUM_FIELDS = LAST_BALL_INDEX + 1;

    /** Message when the user attempt to write to the body1AnchorPoint field */
    private static final String AP1_WRITE_MSG =
        "body1AnchorPoint is outputOnly and cannot be set";

    /** Message when the user attempt to write to the body2AnchorPoint field */
    private static final String AP2_WRITE_MSG =
        "body2AnchorPoint is outputOnly and cannot be set";

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

    // Other vars

    /** The ODE representation of the joint */
    protected JointBall odeJoint;

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

        fieldMap.put("body1AnchorPoint", new Integer(FIELD_BODY1_ANCHOR_POINT));
        fieldMap.put("body2AnchorPoint", new Integer(FIELD_BODY2_ANCHOR_POINT));
    }

    /**
     * Construct a new default ball joint node object.
     */
    public BaseBallJoint() {
        super("BallJoint");

        vfAnchorPoint = new float[3];
        vfBody1AnchorPoint = new float[3];
        vfBody2AnchorPoint = new float[3];

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
    public BaseBallJoint(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("anchorPoint");
            VRMLFieldData field = node.getFieldValue(index);
            vfAnchorPoint[0] = field.floatArrayValue[0];
            vfAnchorPoint[1] = field.floatArrayValue[1];
            vfAnchorPoint[2] = field.floatArrayValue[2];

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
// TODO: Fix the creation of a new array every frame.

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
        if(wld != null)
            odeJoint = new JointBall(wld, grp);
        else
            odeJoint.delete();

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
        if (index < 0  || index > LAST_BALL_INDEX)
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

        if(odeJoint == null)
            return;

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

                case FIELD_BODY1_ANCHOR_POINT:
                    destNode.setValue(destIndex, vfBody1AnchorPoint, 3);
                    break;

                case FIELD_BODY2_ANCHOR_POINT:
                    destNode.setValue(destIndex, vfBody2AnchorPoint, 3);
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
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_ANCHOR_POINT:
                setAnchorPoint(value);
                break;

            case FIELD_BODY1_ANCHOR_POINT:
                throw new InvalidFieldAccessException(AP1_WRITE_MSG);

            case FIELD_BODY2_ANCHOR_POINT:
                throw new InvalidFieldAccessException(AP2_WRITE_MSG);

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
            if(odeJoint != null)
                odeJoint.setAnchor(pos[0], pos[1], pos[2]);
            hasChanged[FIELD_ANCHOR_POINT] = true;
            fireFieldChanged(FIELD_ANCHOR_POINT);
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
