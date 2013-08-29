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

package org.web3d.vrml.renderer.common.nodes;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSensorNodeType;

/**
 * An abstract representation of any form of sensor for
 * subclassing by specific implementations.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public abstract class BaseSensorNode extends AbstractNode
    implements VRMLSensorNodeType {

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 1;

    /** The field index for isActive */
    protected static final int FIELD_IS_ACTIVE = LAST_NODE_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_SENSOR_INDEX = FIELD_IS_ACTIVE;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** The value of the isActive field */
    protected boolean vfIsActive;

    /**
     * Initialise the sensor node and it's fields that are held
     * locally.
     *
     * @param name The name of the type of node
     */
    protected BaseSensorNode(String name) {
        super(name);

        vfEnabled = true;
        vfIsActive = false;
    }

    /**
     * Set the fields of the sensor node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The sensor node to copy info from
     */
    protected void copy(VRMLSensorNodeType node) {
        try {
            int index = node.getFieldIndex("enabled");
            VRMLFieldData field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLSensorNodeType
    //----------------------------------------------------------

    /**
     * Set a new state for the enabled field.
     *
     * @param state True if this sensor is to be enabled
     */
    public void setEnabled(boolean state) {
        if(state != vfEnabled) {
            vfEnabled = state;

            if(!inSetup) {
                hasChanged[FIELD_ENABLED] = true;
                fireFieldChanged(FIELD_ENABLED);
            }
        }
    }

    /**
     * Get the current value of the enabled field. Default value is
     * <code>true</code>.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
        return vfEnabled;
    }

    /**
     * Get current value of the output-only field isActive.
     *
     * @return The current value of isActive
     */
    public boolean getIsActive () {
        return vfIsActive;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        fieldData.clear();

        switch(index) {
            case FIELD_ENABLED:
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;
            case FIELD_IS_ACTIVE:
                fieldData.booleanValue = vfIsActive;
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

                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " +
                ife.getFieldName());
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
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
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
