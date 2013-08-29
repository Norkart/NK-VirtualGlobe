/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
// none

// Local imports
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLTimeControlledNodeType;

/**
 * An abstract representation of any form of time dependent node for
 * subclassing by specific implementations.
 * <p>
 * The implementation performs the basic handling of the time fields but does
 * not create any data structures for them to run with.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public abstract class BaseTimeControlledNode extends BaseTimeDependentNode
    implements VRMLTimeControlledNodeType {

    /** The field index for Loop */
    protected static final int FIELD_LOOP = LAST_NODE_INDEX + 1;

    /** The field index for startTime */
    protected static final int FIELD_START_TIME = LAST_NODE_INDEX + 2;

    /** The field index for stopTime */
    protected static final int FIELD_STOP_TIME = LAST_NODE_INDEX + 3;

    /** The field index for pauseTime */
    protected static final int FIELD_PAUSE_TIME = LAST_NODE_INDEX + 4;

    /** The field index for resumeTime */
    protected static final int FIELD_RESUME_TIME = LAST_NODE_INDEX + 5;

    /** The field index for elapsedTime */
    protected static final int FIELD_ELAPSED_TIME = LAST_NODE_INDEX + 6;

    /** The last field index used by this class */
    protected static final int LAST_TIME_INDEX = FIELD_ELAPSED_TIME;

    /** The value of the loop field */
    protected boolean vfLoop;

    /** The value of the startTime field */
    protected double vfStartTime;

    /** The value of the stopTime field */
    protected double vfStopTime;

    /** The value of the pauseTime field */
    protected double vfPauseTime;

    /** The value of the resumeTime field */
    protected double vfResumeTime;

    /** The value of the pauseTime field */
    protected double vfElapsedTime;

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     *
     * @param name The name of the type of node
     */
    protected BaseTimeControlledNode(String name) {
        super(name);

        vfLoop = false;
        vfStartTime = 0;
        vfStopTime = 0;
    }

    /**
     * Set the fields of the time dependant node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The grouping node to copy info from
     */
    protected void copy(VRMLTimeControlledNodeType node) {
        try {
            int index = node.getFieldIndex("loop");
            VRMLFieldData field = node.getFieldValue(index);
            vfLoop = field.booleanValue;

            index = node.getFieldIndex("startTime");
            field = node.getFieldValue(index);
            vfStartTime = field.doubleValue;

            index = node.getFieldIndex("stopTime");
            field = node.getFieldValue(index);
            vfStopTime = field.doubleValue;

            index = node.getFieldIndex("pauseTime");
            field = node.getFieldValue(index);
            vfPauseTime = field.doubleValue;

            index = node.getFieldIndex("resumeTime");
            field = node.getFieldValue(index);
            vfPauseTime = field.doubleValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Accessor method to set a new value for field attribute <b>loop</b>
     *
     * @param newLoop Whether this field loops or not
     */
    public void setLoop(boolean newLoop) {
        if(newLoop != vfLoop) {
            vfLoop = newLoop;

            if(!inSetup) {
                hasChanged[FIELD_LOOP] = true;
                fireFieldChanged(FIELD_LOOP);
            }
        }
    }

    /**
     * Accessor method to get current value of field <b>loop</b>,
     * default value is <code>false</code>
     *
     * @return The value of the loop field
     */
    public boolean getLoop() {
        return vfLoop;
    }

    /**
     * Accessor method to set a new value for field attribute <b>startTime</b>
     *
     * @param newStartTime The new start time
     */
    public void setStartTime(double newStartTime) {
        if(newStartTime != vfStartTime) {
            vfStartTime = newStartTime;

            if(!inSetup) {
                hasChanged[FIELD_START_TIME] = true;
                fireFieldChanged(FIELD_START_TIME);
            }
        }
    }

    /**
     * Accessor method to set a new value for field attribute <b>pauseTime</b>
     *
     * @param newPauseTime The new start time
     */
    public void setPauseTime(double newPauseTime) {
        vfPauseTime = newPauseTime;

        if(!inSetup) {
            hasChanged[FIELD_PAUSE_TIME] = true;
            fireFieldChanged(FIELD_PAUSE_TIME);
        }
    }

    /**
     * Accessor method to set a new value for field attribute <b>resumeTime</b>
     *
     * @param newResumeTime The new start time
     */
    public void setResumeTime(double newResumeTime) {
        vfResumeTime = newResumeTime;

        if(!inSetup) {
            hasChanged[FIELD_RESUME_TIME] = true;
            fireFieldChanged(FIELD_RESUME_TIME);
        }
    }

    /**
     * Accessor method to get current value of field <b>startTime</b>,
     * default value is <code>0</code>.
     *
     * @return The current startTime
     */
    public double getStartTime() {
        return vfStartTime;
    }

    /**
     * Accessor method to set a new value for field attribute <b>stopTime</b>
     *
     * @param newStopTime The new stop time
     */
    public void setStopTime(double newStopTime) {
        if(newStopTime != vfStopTime) {
            vfStopTime = newStopTime;

            if(!inSetup) {
                hasChanged[FIELD_STOP_TIME] = true;
                fireFieldChanged(FIELD_STOP_TIME);
            }
        }
    }

    /**
     * Accessor method to get current value of field <b>stopTime</b>,
     * default value is <code>0</code>
     *
     * @return The current stop Time
     */
    public double getStopTime() {
        return vfStopTime;
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
            case FIELD_LOOP:
                fieldData.clear();
                fieldData.booleanValue = vfLoop;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_START_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfStartTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_STOP_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfStopTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_PAUSE_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfPauseTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_RESUME_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfResumeTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_ELAPSED_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfElapsedTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;
            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a double for the
     * SFTime fields. The fields effected by this cycleTime, cycleInterval,
     * startTime, stopTime, fraction and time fields. This method does not
     * currently check for negative values. Should it?
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_START_TIME:
                setStartTime(value);
                break;

            case FIELD_STOP_TIME:
                setStopTime(value);
                break;

            case FIELD_PAUSE_TIME:
                setPauseTime(value);
                break;

            case FIELD_RESUME_TIME:
                setResumeTime(value);
                break;

            default:
                super.setValue(index, value);
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
            case FIELD_LOOP:
                setLoop(value);
                break;

            default:
                super.setValue(index, value);
        }
    }
}
