/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
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

// External imports
import org.j3d.geom.IntersectionUtils;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLDragSensorNodeType;

/**
 * An abstract representation of any form of sensor for
 * subclassing by specific implementations.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.11 $
 */
public abstract class BaseDragSensorNode extends BaseSensorNode
    implements VRMLDragSensorNodeType {

    /** The field index for enabled */
    protected static final int FIELD_AUTOOFFSET = LAST_SENSOR_INDEX + 1;

    /** The field index for trackPoint_changed */
    protected static final int FIELD_TRACKPOINT_CHANGED = LAST_SENSOR_INDEX + 2;

    /** The field index for description */
    protected static final int FIELD_DESCRIPTION = LAST_SENSOR_INDEX + 3;

    /** The field index for isOver */
    protected static final int FIELD_IS_OVER = LAST_SENSOR_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_DRAG_SENSOR_INDEX = FIELD_IS_OVER;

    // NOTE
    // The assumption here works fine when you are working with only a single
    // rendering cycle happening. HOWEVER, If you've used the EAI/SAI to create
    // multiple browser windows within the one JVM, I'm not sure whether you
    // will run into multithreading issues with this or not. I don't think you
    // will as only one window can receive input drag events at a time, but it
    // is an untested area and one that needs to be check out in the future.
    //
    // JC.

    /**
     * Common intersection utils class for the ray to geometry testing during
     * the drag process. Single common instance used because we know the event
     * model calling this will be single threaded.
     */
    protected static IntersectionUtils intersectionUtils;

    /** Array to return intersection point info with */
    protected static float[] wkPoint;

    /** The value of the autoOffset field */
    protected boolean vfAutoOffset;

    /** The value of the trackPoint_changed field */
    protected float[] vfTrackPointChanged;

    /** The value of the description field */
    protected String vfDescription;

    /** The value of the isOver field */
    protected boolean vfIsOver;

    /** The initial position of the input touch for generating the tx */
    protected float[] initialPosition;

    /**
     * Common static initialiser.
     */
    static {
        intersectionUtils = new IntersectionUtils();
        wkPoint = new float[3];
    }

    /**
     * Initialise the sensor node and it's fields that are held
     * locally.
     *
     * @param name The name of the type of node
     */
    protected BaseDragSensorNode(String name) {
        super(name);

        vfTrackPointChanged = new float[3];
        vfAutoOffset = true;
        vfIsOver = false;

        initialPosition = new float[3];
    }

    /**
     * Set the fields of the sensor node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The sensor node to copy info from
     */
    protected void copy(VRMLDragSensorNodeType node) {
        super.copy(node);

        try {
            int index = node.getFieldIndex("autoOffset");
            VRMLFieldData field = node.getFieldValue(index);
            vfAutoOffset = field.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //--------------------------------------------------------------------
    // Methods defined by VRMLPointingDeviceSensorNodeType
    //--------------------------------------------------------------------

    /**
     * Get the description to associate with the link. This is a line of text
     * suitable for mouseovers, status information etc. If there is no
     * description set then it returns null.
     *
     * @return The current description or null
     */
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Set the description string for this link. Setting a value of null will
     * clear the current description.
     *
     * @param desc The new description to set
     */
    public void setDescription(String desc) {
        vfDescription = desc;

        if(!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }

    /**
     * Notification that this sensor has just been clicked on to start a drag
     * action.
     *
     * @param hitPoint Where the input device intersected the object sensor
     * @param location Where the sensor origin is in local coordinates
     */
    public void notifySensorDragStart(float[] hitPoint, float[] location) {

        if(!vfEnabled)
            return;

        vfIsActive = true;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);

        initialPosition[0] = hitPoint[0];
        initialPosition[1] = hitPoint[1];
        initialPosition[2] = hitPoint[2];
    }

    /**
     * Notify the drag sensor that a sensor is currently dragging this device
     * and that it's position and orientation are as given.
     *
     * @param position Where the sensor origin is in local coordinates
     * @param direction Vector showing the direction the sensor is pointing
     */
    public void notifySensorDragChange(float[] position, float[] direction) {

        if(!vfEnabled)
            return;

        vfIsActive = false;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);

        processDrag(position, direction);
    }

    /**
     * Notification that this sensor has finished a drag action.
     *
     * @param position Where the sensor origin is in local coordinates
     * @param direction Vector showing the direction the sensor is pointing
     */
    public void notifySensorDragEnd(float[] position, float[] direction) {
        if(!vfEnabled)
            return;

        processDrag(position, direction);
    }

    /**
     * Flag to notify the user whether the node implementation only needs the
     * hit point information, or it needs everything else as well. This is an
     * optimisation method that allows the internals of the event model to
     * avoid doing unnecessary work. If the return value is true, then the
     * hitNormal and hitTexCoord parameter values will not be supplied (they'll
     * be null references).
     *
     * @return true if the node implementation only requires hitPoint information
     */
    public boolean requiresPointOnly() {
        return true;
    }

    /**
     * Set the flag describing whether the pointing device is over this sensor.
     * The result should be that isOver SFBool output only field is set
     * appropriately at the node level.
     *
     * @param newIsOver The new value for isOver
     */
    public void setIsOver(boolean newIsOver) {
        vfIsOver = newIsOver;
        hasChanged[FIELD_IS_OVER] = true;
        fireFieldChanged(FIELD_IS_OVER);
    }

    /**
     * Get the current value of the isOver field.
     *
     * @return The current value of isOver
     */
    public boolean getIsOver() {
        return vfIsOver;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLDragSensorNodeType
    //----------------------------------------------------------

    /**
     * Set a new value for the autoOffset field.
     *
     * @param state The new value for AutoOffset
     */
    public void setAutoOffset(boolean state) {
        vfAutoOffset = state;

        if(!inSetup) {
            hasChanged[FIELD_AUTOOFFSET] = true;
            fireFieldChanged(FIELD_AUTOOFFSET);
        }
    }

    /**
     * Accessor method to get current value of field autoOffset.
     * Default value is <code>true</code>
     *
     * @return The current value of AutoOffset
     */
    public boolean getAutoOffset() {
        return vfAutoOffset;
    }

    /**
     * Accessor method to get current value of the trackPoint field. The value
     * returned is a temporary array that may be reused. The called should not
     * maintain a reference to the value or expect that the value will be always
     * constant or valid.
     *
     * @return The current value of trackPoint_changed(SFVec3f)
     */
    public float[] getTrackPointChanged() {
        return vfTrackPointChanged;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.DragSensorNodeType;
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
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        fieldData.clear();

        switch(index) {
            case FIELD_AUTOOFFSET:
                fieldData.booleanValue = vfAutoOffset;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_IS_OVER:
                fieldData.booleanValue = vfIsOver;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_TRACKPOINT_CHANGED:
                fieldData.floatArrayValue = vfTrackPointChanged;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
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
                case FIELD_AUTOOFFSET:
                    destNode.setValue(destIndex, vfAutoOffset);
                    break;

                case FIELD_IS_OVER:
                    destNode.setValue(destIndex, vfIsOver);
                    break;

                case FIELD_TRACKPOINT_CHANGED:
                    destNode.setValue(destIndex, vfTrackPointChanged, 3);
                    break;

                case FIELD_DESCRIPTION:
                    destNode.setValue(destIndex, vfDescription);
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
            case FIELD_AUTOOFFSET:
                setAutoOffset(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFString field "title".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DESCRIPTION:
                setDescription(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local Convenience Methods
    //----------------------------------------------------------

    /**
     * Generate the tracking output based on the input hit position.
     *
     * @param location The position of the mouse locally
     * @param direction Vector showing the direction the sensor is pointing
     */
    protected abstract void processDrag(float[] location, float[] direction);
}
