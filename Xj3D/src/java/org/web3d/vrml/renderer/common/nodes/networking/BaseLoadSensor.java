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

package org.web3d.vrml.renderer.common.nodes.networking;

// Standard imports
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.util.FieldValidator;

import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;

/**
 * Base implementation of a LoadSensor node.
 * <p>
 * TODO: Likely need switch over to use a Set to track completion
 * so that set_url during tracking doesn't cause extra counts.
 * TODO: Changes of watchList might happen after an asset has loaded when
 * using a set_url, set_watchList on the same frame
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class BaseLoadSensor extends BaseSensorNode
    implements VRMLEnvironmentalSensorNodeType,
               VRMLTimeDependentNodeType,
               VRMLContentStateListener {

    /** Secondary types of this node */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.TimeDependentNodeType };

    // Field index constants

    /** The field index for watchList */
    protected static final int FIELD_WATCHLIST = LAST_SENSOR_INDEX + 1;

    /** The field index for timeout */
    protected static final int FIELD_TIMEOUT = LAST_SENSOR_INDEX + 2;

    /** The field index for loadTime */
    protected static final int FIELD_LOAD_TIME = LAST_SENSOR_INDEX + 3;

    /** The field index for isLoaded */
    protected static final int FIELD_IS_LOADED = LAST_SENSOR_INDEX + 4;

    /** The field index for progress */
    protected static final int FIELD_PROGRESS = LAST_SENSOR_INDEX + 5;

    /** The last field index used by this class */
    protected static final int LAST_LOADSENSOR_INDEX = FIELD_PROGRESS;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_LOADSENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Indices of the fields that are MFNode or SFnode */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the watchList field */
    protected ArrayList vfWatchList;

    /** The value of the timeout field */
    protected double vfTimeout;

    /** The value of the loadTime field */
    protected double vfLoadTime;

    /** The value of the isLoaded field */
    protected boolean vfIsLoaded;

    /** The value of the progress field */
    protected float vfProgress;

    /** The sim clock this node uses */
    private VRMLClock vrmlClock;

    /** The number of children loaded */
    private int loadCnt;

    /** Have we issued a failed event */
    private boolean failed;

    /** Has the isActive field changed */
    private boolean isActiveChanged;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_WATCHLIST, FIELD_METADATA };

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
        fieldDecl[FIELD_WATCHLIST] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "watchList");
        fieldDecl[FIELD_TIMEOUT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "timeOut");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_LOAD_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "loadTime");
        fieldDecl[FIELD_IS_LOADED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isLoaded");
        fieldDecl[FIELD_PROGRESS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "progress");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_WATCHLIST);
        fieldMap.put("watchList", idx);
        fieldMap.put("set_watchList", idx);
        fieldMap.put("watchList_changed", idx);

        idx = new Integer(FIELD_TIMEOUT);
        fieldMap.put("timeOut", idx);
        fieldMap.put("set_timeOut", idx);
        fieldMap.put("timeOut_changed", idx);

        idx = new Integer(FIELD_IS_ACTIVE);
        fieldMap.put("isActive", idx);
        fieldMap.put("isActive_changed", idx);

        idx = new Integer(FIELD_LOAD_TIME);
        fieldMap.put("loadTime", idx);
        fieldMap.put("loadTime_changed", idx);

        idx = new Integer(FIELD_IS_LOADED);
        fieldMap.put("isLoaded", idx);
        fieldMap.put("isLoaded_changed", idx);

        idx = new Integer(FIELD_PROGRESS);
        fieldMap.put("progress", idx);
        fieldMap.put("progress_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseLoadSensor() {
        super("LoadSensor");
        hasChanged = new boolean[NUM_FIELDS];

        vfEnabled = true;
        vfIsActive = false;
        isActiveChanged = false;
        vfWatchList = new ArrayList();
        vfTimeout = 0;
        vfProgress = 0;
        vfIsLoaded = false;
        loadCnt = 0;
        failed = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.  A shallow copy of the watchList will be performed.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type
     */
    public BaseLoadSensor(VRMLNodeType node) {
        this();

        copy((VRMLSensorNodeType)node);

        try {
            int index = node.getFieldIndex("timeout");
            VRMLFieldData field = node.getFieldValue(index);
            vfTimeout = field.longValue;

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
        if (failed) {
            hasChanged[FIELD_IS_LOADED] = true;
            fireFieldChanged(FIELD_IS_LOADED);

            hasChanged[FIELD_IS_ACTIVE] = true;
            fireFieldChanged(FIELD_IS_ACTIVE);
            return;
        }

        hasChanged[FIELD_PROGRESS] = true;
        fireFieldChanged(FIELD_PROGRESS);

        if (vfProgress == 1.0) {
            hasChanged[FIELD_LOAD_TIME] = true;
            fireFieldChanged(FIELD_LOAD_TIME);

            hasChanged[FIELD_IS_LOADED] = true;
            fireFieldChanged(FIELD_IS_LOADED);
        }

        if (isActiveChanged) {
            hasChanged[FIELD_IS_ACTIVE] = true;
            fireFieldChanged(FIELD_IS_ACTIVE);
        }
    }

    //-------------------------------------------------------------------
    // Methods required by the VRMLTimeDependentNodeType interface.
    //-------------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        vrmlClock = clk;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        if(index < 0  || index > LAST_LOADSENSOR_INDEX)
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
        return TypeConstants.EnvironmentalSensorNodeType;
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

            case FIELD_WATCHLIST:
                VRMLNodeType kids[] = new VRMLNodeType[vfWatchList.size()];
                vfWatchList.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValue = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
                break;

            case FIELD_IS_ACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_IS_LOADED:
                fieldData.clear();
                fieldData.booleanValue = vfIsLoaded;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_LOAD_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfLoadTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_TIMEOUT:
                fieldData.clear();
                fieldData.doubleValue = vfTimeout;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_PROGRESS:
                fieldData.clear();
                fieldData.floatValue = vfProgress;
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
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_WATCHLIST:
                    VRMLNodeType kids[] = new VRMLNodeType[vfWatchList.size()];
                    vfWatchList.toArray(kids);
                    destNode.setValue(destIndex, kids, kids.length);
                    break;

                case FIELD_TIMEOUT:
                    destNode.setValue(destIndex, vfTimeout);
                    break;

                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;

                case FIELD_LOAD_TIME:
                    destNode.setValue(destIndex, vfLoadTime);
                    break;

                case FIELD_IS_LOADED:
                    destNode.setValue(destIndex, vfIsLoaded);
                    break;

                case FIELD_PROGRESS:
                    destNode.setValue(destIndex, vfProgress);
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
                vfEnabled = value;
                if(!inSetup) {
                    hasChanged[FIELD_ENABLED] = true;
                    fireFieldChanged(FIELD_ENABLED);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a double.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TIMEOUT:
                vfTimeout = value;
                if(!inSetup) {
                    hasChanged[FIELD_TIMEOUT] = true;
                    fireFieldChanged(FIELD_TIMEOUT);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_WATCHLIST:
                boolean issueEvent=true;

                if (loadCnt > vfWatchList.size()) {
                   issueEvent = false;
                }

                clearChildren();
                addChildNode(child);

                if (child != null) {
                    vfIsLoaded = false;
                }

                failed = false;
                if (!inSetup && issueEvent) {
                    stateManager.addEndOfThisFrameListener(this);
                }
                break;

            default:
                super.setValue(index, child);
        }

        hasChanged[index] = true;
        fireFieldChanged(index);
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
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_WATCHLIST:
                boolean issueEvent=true;

                if (loadCnt > vfWatchList.size())
                   issueEvent = false;

                clearChildren();

                for(int i = 0; i < numValid; i++)
                    addChildNode(children[i]);

                if (numValid != 0)
                    vfIsLoaded = false;

                failed = false;
                if (!inSetup && issueEvent)
                    stateManager.addEndOfThisFrameListener(this);
                break;

            default:
                super.setValue(index, children, numValid);
        }

        hasChanged[index] = true;
        fireFieldChanged(index);
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.  It is then responsbile for calling
     * its childrens setupFinished.
     */
    public void setupFinished() {

        if(!inSetup)
            return;

        super.setupFinished();

        // Call setupFinished on children
        int num_kids = vfWatchList.size();
        VRMLNodeType kid;

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfWatchList.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLContentStateListener
    //-------------------------------------------------------------

    /**
     * Notification that the content state for this node has changed
     *
     * @param node The node that changed state
     * @param index The index of the field that has changed
     * @param state The new state that has changed
     */
    public void contentStateChanged(VRMLNodeType node, int index, int state) {
        if(vfEnabled == false)
            return;

        int watchSize = vfWatchList.size();

        switch(state) {
            case VRMLExternalNodeType.LOAD_COMPLETE:
                loadCnt++;
                if(loadCnt >= watchSize) {
                    vfProgress = 1;
                    vfIsLoaded = true;
                    vfLoadTime = vrmlClock.getTime();
                    vfIsActive = false;
                    isActiveChanged = true;
                } else {
                    vfProgress = (float) loadCnt / watchSize;
                }

                stateManager.addEndOfThisFrameListener(this);

                break;

            case VRMLExternalNodeType.LOAD_FAILED:
                if (!failed) {
                    vfIsLoaded = false;
                    vfIsActive = false;
                    isActiveChanged = true;
                    failed = true;

                    stateManager.addEndOfThisFrameListener(this);
                }

                break;

            default:
                // ignore the other states
        }

    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------

    /**
     * Clear the current children list.
     */
    private void clearChildren() {
        if(inSetup)
            return;

        int watch_size = vfWatchList.size();

        // Clean up the old nodes and their listeners first so that
        // we no longer monitor them.
        for(int i = 0; i < watch_size; i++) {
            VRMLNodeType node = (VRMLNodeType)vfWatchList.get(i);

            if(!(node instanceof VRMLExternalNodeType))
                continue;

            VRMLExternalNodeType en = (VRMLExternalNodeType)node;
            en.removeContentStateListener(this);
        }

        vfWatchList.clear();
        loadCnt = 0;
        vfProgress = 0;
    }

    /**
     * Add a single child node to the list.
     *
     * @param child The new child node to add
     */
    private void addChildNode(VRMLNodeType child) {
        vfWatchList.add(child);

        if(child != null) {
            // Anything that is not an external node type just count
            // as being loaded anyway.
            if(!(child instanceof VRMLSingleExternalNodeType))
                loadCnt++;
            else {
                VRMLSingleExternalNodeType en =
                    (VRMLSingleExternalNodeType)child;
                en.addContentStateListener(this);
            }
        }
    }
}
