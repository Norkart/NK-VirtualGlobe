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
// None

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the abstract X3DRigidJointNode type.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class BaseJointNode extends AbstractNode
    implements VRMLRigidJointNodeType {

    // Field index constants

    /** The field index for body1 */
    protected static final int FIELD_BODY1 = LAST_NODE_INDEX + 1;

    /** The field index for body2 */
    protected static final int FIELD_BODY2 = LAST_NODE_INDEX + 2;

    /** The field index for mustOutput */
    protected static final int FIELD_MUST_OUTPUT = LAST_NODE_INDEX + 3;

    /** Last index used by this base node */
    protected static final int LAST_JOINT_INDEX = FIELD_MUST_OUTPUT;

    /** Message for when the proto is not a Body */
    protected static final String BODY_PROTO_MSG =
        "Proto does not describe a Body object";

    /** Message for when the node in setValue() is not a Body */
    protected static final String BODY_NODE_MSG =
        "Node does not describe a Body object";

    // The VRML field values

    /** The value of the body1 field */
    protected VRMLRigidBodyNodeType vfBody1;

    /** The proto version of the body1 */
    protected VRMLProtoInstance pBody1;

    /** The value of the body2 field */
    protected VRMLRigidBodyNodeType vfBody2;

    /** The proto version of the body2 */
    protected VRMLProtoInstance pBody2;

    /** The value of the mustOutput list */
    protected String[] vfMustOutput;

    /** The number of valid values in vfMustOutput */
    protected int numMustOutput;

    /** Converted version of the output index list */
    protected int[] outputIndices;

    /** The number of valid values in the output list */
    protected int numOutputIndices;

    /**
     * Construct a new generalised joint node object.
     *
     * @param name The VRML name of this node
     */
    public BaseJointNode(String name) {
        super(name);

        vfMustOutput = new String[] { "NONE" };
        numMustOutput = 1;
        numOutputIndices = 0;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLRigidJointNodeType
    //----------------------------------------------------------

    /**
     * Get the number of valid fields that the user has requested updates for.
     *
     * @return a value greater than or equal to zero
     */
    public int numOutputs() {
        return numOutputIndices;
    }

    /**
     * Get the array of output field indices for this joint. These are
     * previously mapped internally from the output listing to the field
     * index values corresponding to the user-supplied field names, as well
     * as processing for the special NONE and ALL types.
     *
     * @return an array of field indices that are to be used
     */
    public int[] getOutputFields() {
        return outputIndices;
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

        VRMLRigidBodyNodeType node;
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
        } else {
            throw new InvalidFieldValueException(BODY_NODE_MSG);
        }

        vfBody1 = (VRMLRigidBodyNodeType)node;

        if(body!= null)
            updateRefs(body, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(body != null)
                stateManager.registerAddedNode(body);

            hasChanged[FIELD_BODY1] = true;
            fireFieldChanged(FIELD_BODY1);
        }
    }

    /**
     * Fetch the node that is being used to pick the geometry
     *
     * @return The valid geometry node or null if not set
     */
    public VRMLNodeType getBody1() {
        return vfBody1;
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

        VRMLRigidBodyNodeType node;
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
        } else {
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

            hasChanged[FIELD_BODY1] = true;
            fireFieldChanged(FIELD_BODY1);
        }
    }

    /**
     * Fetch the node that is being used to pick the geometry
     *
     * @return The valid geometry node or null if not set
     */
    public VRMLNodeType getBody2() {
        return vfBody2;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

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

        if(pBody1 != null)
            pBody1.setupFinished();
        else if(vfBody1 != null)
            vfBody1.setupFinished();

        if(pBody2 != null)
            pBody2.setupFinished();
        else if(vfBody2 != null)
            vfBody2.setupFinished();
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

            case FIELD_MUST_OUTPUT:
                fieldData.clear();
                fieldData.stringArrayValue = vfMustOutput;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = numMustOutput;
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

                case FIELD_MUST_OUTPUT:
                    destNode.setValue(destIndex, vfMustOutput, numMustOutput);
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
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {
            case FIELD_MUST_OUTPUT:
                setMustOutput(value, numValid);
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

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the value of the mustOutput field and process the values to set the
     * outputFieldIndices array.
     *
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    private void setMustOutput(String[] value, int numValid) {

        if(vfMustOutput.length < numValid)
            vfMustOutput = new String[numValid];

        System.arraycopy(value, 0, vfMustOutput, 0, numValid);
        numMustOutput = numValid;

        // look for a couple of the edge cases - "ALL" and "NONE".
        boolean need_all = false;

        if(numValid == 1) {
            if(value[0].equals("NONE"))
                numOutputIndices = 0;
            else if(value[0].equals("ALL"))
                need_all = true;
        } else {
            for(int i = 0; !need_all && i < numValid; i++) {
                if(value[i].equals("ALL"))
                    need_all = true;
            }
        }

        if(need_all) {
            int[] outputs = getAllOutputFieldIndices();

            if(outputIndices == null || outputIndices.length < outputs.length)
                outputIndices = new int[outputs.length];

            System.arraycopy(outputs, 0, outputIndices, 0, outputs.length);
            numOutputIndices = outputs.length;

        } else if(numValid != 0) {
            if(outputIndices == null || outputIndices.length < numValid)
                outputIndices = new int[numValid];

            numOutputIndices = 0;

            // iterate through the current fields and see what happens
            for(int i = 0; i < numValid; i++) {
                int index = getFieldIndex(value[i]);
                if(index != -1)
                    outputIndices[numOutputIndices++] = index;
            }
        } else
            numOutputIndices = 0;

        if(!inSetup) {
            hasChanged[FIELD_MUST_OUTPUT] = true;
            fireFieldChanged(FIELD_MUST_OUTPUT);
        }
    }

    /**
     * Return to the caller a list of the indices of all output fields of this
     * concrete node. Array must be the correct length.
     *
     * @return the output listing of indicies
     */
    abstract int[] getAllOutputFieldIndices();
}
