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

package org.web3d.vrml.renderer.common.nodes.particle;

// External imports
import org.j3d.geom.particle.ParticleFunction;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of a GravityPhysics node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public abstract class BasePhysicsModel extends AbstractNode
    implements VRMLParticlePhysicsModelNodeType {

    // Field index constants

    /** The field index for cycleInterval */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_PHYSICS_INDEX = FIELD_ENABLED;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_PHYSICS_INDEX + 1;

    // The VRML field values

    /** The value of the cycle time field */
    protected boolean vfEnabled;

    /** The j3d.org particle function implementation this node is wrapping */
    protected ParticleFunction particleFunction;
    /**
     * Construct a new time sensor object
     */
    protected BasePhysicsModel(String nodeName) {
        super(nodeName);

        // Set the default values for the fields
        vfEnabled = true;
    }

    /**
     * Copy the node to this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected void copy(VRMLParticlePhysicsModelNodeType node) {

        try {
            int index = node.getFieldIndex("enabled");
            VRMLFieldData field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLParticlePhysicsModelNodeType
    //----------------------------------------------------------

    /**
     * Return the particle function implementation that this model controls.
     * May not be available until after setupFinished() is called.
     *
     * @return The current function to control the system
     */
    public ParticleFunction getParticleFunction() {
        return particleFunction;
    }

    /**
     * Set the enabled state of the Physics model.
     *
     * @param state true to enable the use of this model
     */
    public void setEnabled(boolean state) {
        vfEnabled = state;

        if(!inSetup) {
            particleFunction.setEnabled(state);
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Get the current enabled state of this model.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
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

        particleFunction.setEnabled(vfEnabled);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ParticlePhysicsModelNodeType;
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
            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
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
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This is
     * be used to set SFBool field types isActive, enabled and loop.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ENABLED:
                setEnabled(value);
                break;

            default:
                super.setValue(index, value);
        }
    }
}
