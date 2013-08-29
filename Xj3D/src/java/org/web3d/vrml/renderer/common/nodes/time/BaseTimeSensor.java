/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.time;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseTimeControlledNode;

/**
 * Common implementation of a TimeSensor node.
 * <p>
 *
 * The implementation uses the standard VRML time clock to send and retrieve
 * time information. As an efficiency measure, if the time sensor is disabled
 * it will remove itself as a listener to the global clock. When it becomes
 * re-enabled that listener will be added back again.
 * <p>
 *
 * When setting values we always set the variable first and then set the flag
 * indicating that the field has changed. This is so that we don't end up with
 * a multi-threaded access thinking that a value has changed when it really
 * hasn't (yet) and then ignoring the value. As we desperately try to avoid
 * synchronized access internally, we try to set the real value first so that
 * if someone does check and find it's changed, we can then give them the real
 * value as soon as they ask for it.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.40 $
 */
public class BaseTimeSensor extends BaseTimeControlledNode
    implements VRMLTimeListener, VRMLSensorNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.TimeControlledNodeType };

    // Field index constants

    /** The field index for cycleInterval */
    protected static final int FIELD_CYCLE_INTERVAL = LAST_TIME_INDEX + 1;

    /** The field index for fraction */
    protected static final int FIELD_FRACTION = LAST_TIME_INDEX + 2;

    /** The field index for time */
    protected static final int FIELD_TIME = LAST_TIME_INDEX + 3;

    /** The field index for cycleTime */
    protected static final int FIELD_CYCLE_TIME = LAST_TIME_INDEX + 4;

    /** The field index for isActive */
    protected static final int FIELD_IS_ACTIVE = LAST_TIME_INDEX + 5;

    /** The field index for isPaused */
    protected static final int FIELD_IS_PAUSED = LAST_TIME_INDEX + 6;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_TIME_INDEX + 7;

    /** The last field index used by this class */
    protected static final int LAST_TIME_SENSOR_INDEX = FIELD_ENABLED;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TIME_SENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the cycle time field */
    protected double vfCycleTime;

    /** The value of the cycle interval field */
    protected double vfCycleInterval;

    /** The value of the fraction interval */
    protected float vfFraction;

    /** The value of the time field */
    protected double vfTime;

    /** The value of the isActive field */
    protected boolean vfIsActive;

    /** The value of the isPaused field */
    protected boolean vfIsPaused;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    // Internal working variables

    /** The current cycle number that we are processing */
    private long currentCycle;

    /** We should just loop forever */
    private boolean loopForever;

    /** Precalculated internal stop time */
    private long internalStopTime;

    /** Precalculated internal start time */
    private long internalStartTime;

    /** Precalculated internal pause time */
    private long internalPauseTime;

    /** Precalculated internal resume time */
    private long internalResumeTime;

    /** Precalculated internal cycle interval */
    private long internalCycleInterval;

    /** The last time paused */
    private long pausedTime;

    /** The amount of time spent paused since becoming active */
    private long totalPausedTime;

    /** The start time to apply next */
    private double nextStart;

    /** The start time to apply next */
    private double nextStop;

    /** Have we just started this node */
    private boolean justStarted;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_LOOP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "loop");
        fieldDecl[FIELD_START_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "startTime");
        fieldDecl[FIELD_STOP_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "stopTime");
        fieldDecl[FIELD_PAUSE_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "pauseTime");
        fieldDecl[FIELD_ELAPSED_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "elapsedTime");
        fieldDecl[FIELD_RESUME_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "resumeTime");
        fieldDecl[FIELD_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "time");
        fieldDecl[FIELD_CYCLE_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "cycleTime");
        fieldDecl[FIELD_CYCLE_INTERVAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "cycleInterval");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_IS_PAUSED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isPaused");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_FRACTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "fraction_changed");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_LOOP);
        fieldMap.put("loop", idx);
        fieldMap.put("set_loop", idx);
        fieldMap.put("loop_changed", idx);

        idx = new Integer(FIELD_START_TIME);
        fieldMap.put("startTime", idx);
        fieldMap.put("set_startTime", idx);
        fieldMap.put("startTime_changed", idx);

        idx = new Integer(FIELD_STOP_TIME);
        fieldMap.put("stopTime", idx);
        fieldMap.put("set_stopTime", idx);
        fieldMap.put("stopTime_changed", idx);

        idx = new Integer(FIELD_PAUSE_TIME);
        fieldMap.put("pauseTime", idx);
        fieldMap.put("set_pauseTime", idx);
        fieldMap.put("pauseTime_changed", idx);

        idx = new Integer(FIELD_RESUME_TIME);
        fieldMap.put("resumeTime", idx);
        fieldMap.put("set_resumeTime", idx);
        fieldMap.put("resumeTime_changed", idx);

        idx = new Integer(FIELD_CYCLE_INTERVAL);
        fieldMap.put("cycleInterval", idx);
        fieldMap.put("set_cycleInterval", idx);
        fieldMap.put("cycleInterval_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        fieldMap.put("time", new Integer(FIELD_TIME));
        fieldMap.put("cycleTime", new Integer(FIELD_CYCLE_TIME));
        fieldMap.put("isActive", new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("isPaused", new Integer(FIELD_IS_PAUSED));
        fieldMap.put("fraction_changed", new Integer(FIELD_FRACTION));
        fieldMap.put("elapsedTime", new Integer(FIELD_ELAPSED_TIME));
    }

    /**
     * Construct a new time sensor object
     */
    public BaseTimeSensor() {
        super("TimeSensor");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfTime = -1;
        vfCycleTime = 0;
        vfPauseTime = 0;
        vfResumeTime = 0;
        vfCycleInterval = 1;
        vfIsActive = false;
        vfIsPaused = false;
        vfEnabled = true;
        vfLoop = false;

        internalCycleInterval = 1000;

        // Set this to -1 so that when we first activate the sensor it will
        // trigger sending the cycleTime eventOut
        currentCycle = -1;
        justStarted = true;
        nextStart = -1;
        nextStop = -1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseTimeSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLTimeControlledNodeType)node);

        try {
            int index = node.getFieldIndex("enabled");
            VRMLFieldData field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("cycleInterval");
            field = node.getFieldValue(index);
            vfCycleInterval = field.doubleValue;

            index = node.getFieldIndex("pauseTime");
            field = node.getFieldValue(index);
            vfPauseTime = field.doubleValue;

            index = node.getFieldIndex("resumeTime");
            field = node.getFieldValue(index);
            vfResumeTime = field.doubleValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        // Handle start/stop sequencing

        // Force this if we know we have an infinite loop case.
        loopForever = vfLoop && (vfStopTime <= vfStartTime);
        if(loopForever) {
            nextStart = -1;
            nextStop = -1;
        }

        if (nextStop > 0) {
            if(nextStop < vfTime)
                nextStop = vfTime;

            vfStopTime = nextStop;
            hasChanged[FIELD_STOP_TIME] = true;
            fireFieldChanged(FIELD_STOP_TIME);
            internalStartTime = (long) (vfStartTime * 1000d);
            totalPausedTime = 0;

            recalcStopTime();

            nextStop = -1;
        }

        if (nextStart > 0) {
            vfStartTime = nextStart;
            hasChanged[FIELD_START_TIME] = true;
            fireFieldChanged(FIELD_START_TIME);

            internalStartTime = (long) (vfStartTime * 1000d);
            totalPausedTime = 0;

            recalcStopTime();

            if(!inSetup && vfEnabled)
                vrmlClock.addTimeListener(this);

            nextStart = -1;
            vfFraction = 0;
        }

        long t = vrmlClock.getWallTime();
/*
        // If we started in the past then start now.
        if(justStarted && internalStartTime < t) {
System.out.println("Forcing internalStartTime to t, why: " + t);
            internalStartTime = t;
        }
*/
        justStarted = false;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLTimeSensor .
    //-------------------------------------------------------------

    /**
     * Get the current value of the cycleTime field.
     *
     * @return the current time value
     */
    public double getCycleTime() {
        return vfCycleTime;
    }

    /**
     * Get the current value of the cycleInterval field.
     *
     * @return the current time value
     */
    public double getCycleInterval() {
        return vfCycleInterval;
    }

    /**
     * Set the cycleInterval field value to the new value. If it is out of
     * range the throw an exception.
     *
     * @param newCycleInterval The new time to set
     * @throws InvalidFieldValueException The field is out of range
     */
    public void setCycleInterval(double newCycleInterval)
        throws InvalidFieldValueException {

        if(vfIsActive)
            return;

        if(newCycleInterval <= 0)
            throw new InvalidFieldValueException("cycleInterval is <= 0");

        if(newCycleInterval != vfCycleInterval) {
            vfCycleInterval = newCycleInterval;

            if(!inSetup) {
                hasChanged[FIELD_CYCLE_INTERVAL] = true;
                fireFieldChanged(FIELD_CYCLE_INTERVAL);
            }

            internalCycleInterval = (long)(vfCycleInterval * 1000d);
            loopForever = vfLoop && (vfStopTime <= vfStartTime);
            recalcStopTime();
        }
    }

    /**
     * Get the current value of the fraction field.
     *
     * @return the current time value
     */
    public float getFraction() {
        return vfFraction;
    }

    /**
     * Get the current value of the time field.
     *
     * @return the current time value
     */
    public double getTime() {
        return vfTime;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLTimeDependentNodeType .
    //-------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     * The vrmlClock provides all the information and listeners for keeping track
     * of time. If we are enabled at the time that this method is called we
     * automatically register the listener. Then, all the events that need
     * to be generated will be handled at the next vrmlClock tick we get issued.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        if(vrmlClock != null)
            vrmlClock.removeTimeListener(this);

        this.vrmlClock = clk;

        // Clearing out the vrmlClock so don't need to do any more
        if(vrmlClock == null)
            return;

        vfTime = vrmlClock.getTime();
        if(!inSetup && vfEnabled && (loopForever || vfTime > vfStopTime))
            vrmlClock.addTimeListener(this);

        if((nextStart > -1 || nextStop > -1))
            stateManager.addEndOfThisFrameListener(this);
    }

    //-------------------------------------------------------------
    // Methods defined by BaseTimeDependentNode
    //-------------------------------------------------------------

    /**
     * Set the loop field value.
     *
     * @param newLoop Whether this field loops or not
     */
    public void setLoop(boolean newLoop) {

        if(newLoop != vfLoop) {
            vfLoop = newLoop;
            hasChanged[FIELD_LOOP] = true;
            fireFieldChanged(FIELD_LOOP);

            loopForever = vfLoop && (vfStopTime <= vfStartTime);
            if(vfEnabled) {
                // Need to finish current cycle
                internalStopTime = internalStartTime + internalCycleInterval;
            } else {
                recalcStopTime();
            }
        }
    }

    /**
     * Set a new value for the start time. If the sensor is active then it is
     * ignored.
     *
     * @param newStartTime The new start time
     */
    public void setStartTime(double newStartTime) {
        super.setStartTime(newStartTime);

        nextStart = newStartTime;
        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Set a new value for the pause time.
     *
     * @param newPauseTime The new start time
     */
    public void setPauseTime(double newPauseTime) {
        internalPauseTime = (long) (newPauseTime * 1000d);

        vfPauseTime = newPauseTime;
        hasChanged[FIELD_PAUSE_TIME] = true;
        fireFieldChanged(FIELD_PAUSE_TIME);
    }

    /**
     * Set a new value for the resume time.
     *
     * @param newResumeTime The new resume time
     */
    public void setResumeTime(double newResumeTime) {
        internalResumeTime = (long) (newResumeTime * 1000d);

        vfResumeTime = newResumeTime;
        hasChanged[FIELD_RESUME_TIME] = true;
        fireFieldChanged(FIELD_RESUME_TIME);
    }

    /**
     * Set a new value for the stop time. If the sensor is active and the stop
     * time is less than the current start time, it is ignored. If the stop
     * time is less that now, it is set to the value now.
     *
     * @param newStopTime The new stop time
     */
    public void setStopTime(double newStopTime) {
        super.setStopTime(newStopTime);

        if(vfIsActive && (newStopTime < vfStartTime))
            return;

        nextStop = newStopTime;
        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLSensorNodeType .
    //-------------------------------------------------------------

    /**
     * Set the sensor enabled or disabled.
     *
     * @param newEnabled The new enabled value
     */
    public void setEnabled(boolean newEnabled) {
        if(vfEnabled != newEnabled) {
            vfEnabled = newEnabled;
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);

            // If this is a forced setEnable to cancel it, set
            // isActive to false too
            if(!vfEnabled) {
                vfIsActive = false;
                hasChanged[FIELD_IS_ACTIVE] = true;
                fireFieldChanged(FIELD_IS_ACTIVE);
            }

            if(vrmlClock != null) {
                if(vfEnabled)
                    vrmlClock.addTimeListener(this);
                else
                    vrmlClock.removeTimeListener(this);
            }
        }
    }

    /**
     * Accessor method to get current value to the enabled field.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
        return vfEnabled;
    }

    /**
     * Accessor method to get current value of field <b>isActive</b>.
     *
     * @return The current value of isActive
     */
    public boolean getIsActive () {
        return vfIsActive;
    }

    //----------------------------------------------------------
    // Methods defined by J3DVRMLNodeTypeType .
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

        loopForever = vfLoop && (vfStopTime <= vfStartTime);
        // Force this if we know we have an infinite loop case.
        if(loopForever) {
            nextStart = -1;
            nextStop = -1;
        }

        internalCycleInterval = (long)(vfCycleInterval * 1000d);

        recalcStopTime();

        justStarted = true;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType .
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
        if(index < 0  || index > LAST_TIME_SENSOR_INDEX)
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
        return TypeConstants.SensorNodeType;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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

            case FIELD_IS_ACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_IS_PAUSED:
                fieldData.clear();
                fieldData.booleanValue = vfIsPaused;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_LOOP:
                fieldData.clear();
                fieldData.booleanValue = vfLoop;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_FRACTION:
                fieldData.clear();
                fieldData.floatValue = vfFraction;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_CYCLE_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfCycleTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_CYCLE_INTERVAL:
                fieldData.clear();
                fieldData.doubleValue = vfCycleInterval;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
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

            case FIELD_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
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
                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;
                case FIELD_LOOP:
                    destNode.setValue(destIndex, vfLoop);
                    break;
                case FIELD_FRACTION:
                    destNode.setValue(destIndex, vfFraction);
                    break;
                case FIELD_CYCLE_TIME:
                    destNode.setValue(destIndex, vfCycleTime);
                    break;
                case FIELD_CYCLE_INTERVAL:
                    destNode.setValue(destIndex, vfCycleInterval);
                    break;
                case FIELD_START_TIME:
                    destNode.setValue(destIndex, vfStartTime);
                    break;
                case FIELD_STOP_TIME:
                    destNode.setValue(destIndex, vfStopTime);
                    break;
                case FIELD_PAUSE_TIME:
                    destNode.setValue(destIndex, vfPauseTime);
                    break;
                case FIELD_RESUME_TIME:
                    destNode.setValue(destIndex, vfResumeTime);
                    break;
                case FIELD_ELAPSED_TIME:
                    destNode.setValue(destIndex, vfElapsedTime);
                    break;
                case FIELD_TIME:
                    destNode.setValue(destIndex, vfTime);
                    break;
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;
                case FIELD_IS_PAUSED:
                    destNode.setValue(destIndex, vfIsPaused);
                    break;
                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseTimeSensor.sendRoute: No field! " + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
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
            case FIELD_CYCLE_INTERVAL:
                setCycleInterval(value);
                break;

            case FIELD_PAUSE_TIME:
                setPauseTime(value);
                break;

            case FIELD_RESUME_TIME:
                setResumeTime(value);
                break;

            case FIELD_CYCLE_TIME:
                throw new InvalidFieldValueException("cycleTime is an outputOnly field");

            case FIELD_TIME:
                throw new InvalidFieldValueException("time is an outputOnly field");

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
            case FIELD_IS_ACTIVE:
                throw new InvalidFieldValueException("Cannot set eventout");

            case FIELD_ENABLED:
                setEnabled(value);
                break;

            default :
                super.setValue(index, value);
        }
    }


    //-------------------------------------------------------------
    // Methods defined by VRMLTimeListener
    //-------------------------------------------------------------

    /**
     * Notification that the time is now this value.
     *
     * @param time The current time
     */
    public void timeClick(long time) {
        if(!vfEnabled)
            return;

        double vrml_time = time * 0.001;
        double f;
        double cycle_count = 0;

        if (!vfIsPaused && time >= internalPauseTime &&
             internalPauseTime > internalResumeTime) {
            pausedTime = time;
            vfIsPaused = true;
            hasChanged[FIELD_IS_PAUSED] = true;
            fireFieldChanged(FIELD_IS_PAUSED);
        }

        if (vfIsPaused && time >= internalResumeTime &&
            internalResumeTime > internalPauseTime) {
            totalPausedTime += (time - pausedTime);
            vfIsPaused = false;
            hasChanged[FIELD_IS_PAUSED] = true;
            fireFieldChanged(FIELD_IS_PAUSED);
        }

        if (vfIsPaused)
            return;

        if((time >= internalStartTime && time <= internalStopTime) ||
           loopForever) {

            cycle_count = (time - internalStartTime - totalPausedTime) /
                          (float)internalCycleInterval;


        // On the first tick, send an event to say it is active now. If the
        // user has decided in their infinite wisdom to set isActive to false
        // while this sensor is running, this will set it back to true as well,
        // which is what the VRML spec says it should say.
            if(!vfIsActive) {
                if (loopForever && (vfStartTime >= vfStopTime)) {
                    internalStartTime = time;
                }

                vfIsActive = true;
                hasChanged[FIELD_IS_ACTIVE] = true;
                fireFieldChanged(FIELD_IS_ACTIVE);

                vfCycleTime = vrml_time;

                hasChanged[FIELD_CYCLE_TIME] = true;
                fireFieldChanged(FIELD_CYCLE_TIME);

                cycle_count =
                    (time - internalStartTime - totalPausedTime) /
                    (float) internalCycleInterval;

                currentCycle = (long) Math.floor(cycle_count);

                f = (cycle_count % 1.0);
                vfFraction = (f == 0) ? 1 : (float)f;

                hasChanged[FIELD_FRACTION] = true;

                vfIsPaused = false;
                pausedTime = 0;
            } else {
                f = (cycle_count % 1.0);
                vfFraction = (f == 0) ? 1 : (float)f;
            }

            if (Math.floor(cycle_count) != currentCycle) {
                currentCycle = (long) Math.floor(cycle_count);
                vfCycleTime = vrml_time;
                hasChanged[FIELD_CYCLE_TIME] = true;
                fireFieldChanged(FIELD_CYCLE_TIME);

                if (vfLoop) {
                    recalcStopTime();
                } else {
                    internalStopTime = 0;
                    vfIsActive = false;
                    hasChanged[FIELD_IS_ACTIVE] = true;
                    fireFieldChanged(FIELD_IS_ACTIVE);
                }

                // Make the final fraction be 1.0 for consitancy
                vfFraction = 1;
                hasChanged[FIELD_FRACTION] = true;
                fireFieldChanged(FIELD_FRACTION);
            } else {
                hasChanged[FIELD_FRACTION] = true;
                fireFieldChanged(FIELD_FRACTION);
            }

            if(vfTime != vrml_time) {
                vfTime = vrml_time;

                hasChanged[FIELD_TIME] = true;
                fireFieldChanged(FIELD_TIME);

                vfElapsedTime = (time - internalStartTime - totalPausedTime) * 0.001;
                hasChanged[FIELD_ELAPSED_TIME] = true;
                fireFieldChanged(FIELD_ELAPSED_TIME);
            }
        } else if(vfIsActive) {
            vfIsActive = false;
            hasChanged[FIELD_IS_ACTIVE] = true;
            fireFieldChanged(FIELD_IS_ACTIVE);

            vfTime = internalStopTime * 0.001;

            cycle_count = (internalStopTime - internalStartTime - totalPausedTime) /
                          (float)internalCycleInterval;

            f = (cycle_count % 1.0);
            // There is a 10 millisecond accuracy limit on the timers for
            // Windows atleast, so round up to 1.0 to make sure we get the last
            // fraction changed event correct.
            if (f>0.99) {
                f=1.0;
            }
            vfFraction = (f == 0) ? 1 : (float)f;

            hasChanged[FIELD_TIME] = true;
            fireFieldChanged(FIELD_TIME);
            hasChanged[FIELD_FRACTION] = true;
            fireFieldChanged(FIELD_FRACTION);

            vfElapsedTime = (time - internalStartTime - totalPausedTime) * 0.001;
            hasChanged[FIELD_ELAPSED_TIME] = true;
            fireFieldChanged(FIELD_ELAPSED_TIME);

            vrmlClock.removeTimeListener(this);
        }
    }

    /**
     * Internal convenience method to recalculate the internal stop time based
     * on the various field settings
     */
    private void recalcStopTime() {

        if(vfStopTime <= vfStartTime) {
            internalStopTime = internalStartTime + internalCycleInterval;
        } else {
            internalStopTime = (long) (vfStopTime * 1000d);
        }
    }
}
