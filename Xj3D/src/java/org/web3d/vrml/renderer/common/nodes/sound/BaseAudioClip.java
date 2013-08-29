/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.sound;

// Standard imports
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.util.URLChecker;
import org.web3d.vrml.renderer.common.nodes.BaseTimeControlledNode;

/**
 * AudioClip node implementation.
 * <p>
 *
 *
 * @author Guy Carpenter
 * @version $Revision: 1.23 $
 */
// IMPLEMENTATION NOTES:
//
// Extracts from Annotated VRML:
//
// The default values for each of the time-dependent nodes are
// specified such that any node with default values is already inactive
// (and, therefore, will generate no events upon loading). A time-dependent
// node can be defined such that it will be active upon reading by specifying
// loop TRUE. This use of a non-terminating time-dependent node should be
// used with caution since it incurs continuous overhead on the simulation.
//
// When a time-dependent node is read from a file and the ROUTEs specified
// within the file have been established, the node should determine if it is
// active and, if so, generate an isActive TRUE event and begin generating
// any other necessary events. However, if a node would have become inactive
// at any time before the reading of the file, no events are generated upon
// the completion of the read.

public class BaseAudioClip extends BaseTimeControlledNode
    implements VRMLSingleExternalNodeType,
               VRMLTimeListener,
               VRMLTimeDependentNodeType,
               VRMLAudioClipNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.SingleExternalNodeType,
          TypeConstants.TimeDependentNodeType };

    /** Is this object receiving time callbacks */
    private boolean isTimeListening;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** The array of fields that need URL content */
    private static int[] urlFieldIndexList;

    /** Index of description field */
    private static final int FIELD_DESCRIPTION = LAST_TIME_INDEX + 1;

    /** Index of pitch field */
    private static final int FIELD_PITCH = LAST_TIME_INDEX + 2;

    /** Index of url field */
    private static final int FIELD_URL = LAST_TIME_INDEX + 3;

    /** Index of duration_changed field */
    private static final int FIELD_DURATION = LAST_TIME_INDEX + 4;

    /** Index of isActive output only field */
    private static final int FIELD_IS_ACTIVE = LAST_TIME_INDEX + 5;

    /** Index of isActive output only field */
    private static final int FIELD_IS_PAUSED = LAST_TIME_INDEX + 6;

    /** The last field index used by this class */
    private static final int LAST_AUDIOCLIP_INDEX = FIELD_IS_PAUSED;

    /** Number of fields implemented */
    private static final int NUM_FIELDS = LAST_AUDIOCLIP_INDEX + 1;

    /**  exposedField SFString description "" */
    protected String vfDescription;

    /**  eventOut SFBool isActive */
    protected boolean vfIsActive;

    /**  eventOut SFBool isPaused */
    protected boolean vfIsPaused;

    /**  exposedField SFFloat pitch, default 1.0 */
    protected float vfPitch;

    /**   duration of most recently loaded clip */
    protected double vfDuration;

    /**  exposedField   MFString url */
    protected String[] vfUrl;

    /** List of those who want to know about Url changes. */
    private ArrayList urlListeners;

    /** The array of listeners for isActive changes */
    private ArrayList soundStateListeners;

    /** List of those who want to know about state changes. */
    private ArrayList contentListeners;

    /** URL Load State */
    private int loadState = NOT_LOADED;

    /** URL Load State */
    private String worldURL;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "description");
        fieldDecl[FIELD_LOOP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "loop");
        fieldDecl[FIELD_PITCH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "pitch");
        fieldDecl[FIELD_START_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "startTime");

        fieldDecl[FIELD_PAUSE_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "pauseTime");

        fieldDecl[FIELD_RESUME_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "resumeTime");

        fieldDecl[FIELD_ELAPSED_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "elapsedTime");

        fieldDecl[FIELD_STOP_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "stopTime");
        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "url");

        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");

        fieldDecl[FIELD_DURATION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "duration_changed");

        fieldDecl[FIELD_IS_PAUSED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isPaused");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_DESCRIPTION);
        fieldMap.put("description", idx);
        fieldMap.put("set_description", idx);
        fieldMap.put("description_changed", idx);

        idx = new Integer(FIELD_LOOP);
        fieldMap.put("loop", idx);
        fieldMap.put("set_loop", idx);
        fieldMap.put("loop_changed", idx);

        idx = new Integer(FIELD_PITCH);
        fieldMap.put("pitch", idx);
        fieldMap.put("set_pitch", idx);
        fieldMap.put("pitch_changed", idx);

        idx = new Integer(FIELD_START_TIME);
        fieldMap.put("startTime", idx);
        fieldMap.put("set_startTime", idx);
        fieldMap.put("startTime_changed", idx);

        idx = new Integer(FIELD_PAUSE_TIME);
        fieldMap.put("pauseTime", idx);
        fieldMap.put("set_pauseTime", idx);
        fieldMap.put("pauseTime_changed", idx);

        idx = new Integer(FIELD_STOP_TIME);
        fieldMap.put("stopTime", idx);
        fieldMap.put("set_stopTime", idx);
        fieldMap.put("stopTime_changed", idx);

        idx = new Integer(FIELD_RESUME_TIME);
        fieldMap.put("resumeTime", idx);
        fieldMap.put("set_resumeTime", idx);
        fieldMap.put("resumeTime_changed", idx);

        idx = new Integer(FIELD_URL);
        fieldMap.put("url", idx);
        fieldMap.put("set_url", idx);
        fieldMap.put("url_changed", idx);

        fieldMap.put("isActive", new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("isPaused", new Integer(FIELD_IS_PAUSED));
        fieldMap.put("duration_changed", new Integer(FIELD_DURATION));
        fieldMap.put("elapsedTime", new Integer(FIELD_ELAPSED_TIME));

        urlFieldIndexList = new int[1];
        urlFieldIndexList[0] = FIELD_URL;
    }

    /**
     * Constructor creates a default audioclip node.
     */
    public BaseAudioClip() {
        super("AudioClip");

        urlListeners = new ArrayList();
        contentListeners = new ArrayList();
        soundStateListeners = new ArrayList();
        hasChanged = new boolean[LAST_AUDIOCLIP_INDEX + 1];

        vfUrl = new String[0];
        vfIsActive = false;
        vfIsPaused = false;
        vfLoop = false;
        vfStartTime = 0;
        vfStopTime = 0;
        vfDescription = null;
        vfPitch = 1.0f;
        vfDuration = -1.0;

        isTimeListening = false;
    }

    /**
     * Copies all of the field values from the passed nodes into
     * our own node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a compatible node
     */
    public void copy(VRMLNodeType node) {
        checkNodeType(node);
        try {
            int index;
            VRMLFieldData field;

            super.copy((VRMLTimeControlledNodeType)node);

            index = node.getFieldIndex("description");
            field = node.getFieldValue(index);
            setDescription(field.stringValue);

            index = node.getFieldIndex("loop");
            field = node.getFieldValue(index);
            setLoop(field.booleanValue);

            index = node.getFieldIndex("pitch");
            field = node.getFieldValue(index);
            setPitch(field.floatValue);

            index = node.getFieldIndex("url");
            field = node.getFieldValue(index);
            setUrl(field.stringArrayValue, field.numElements);

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------------------
    // The following methods are used internally and by subclasses
    // to change the values of the node attributes.  Subclasses can
    // override these if they need to perform actions when a property
    // changes.
    //----------------------------------------------------------------------

    /**
     * Set a new value for the start time. If the sensor is active then it is
     * ignored (as per the spec).
     *
     * @param newStartTime The new start time
     */
    public void setStartTime(double newStartTime) {
        if(vfIsActive) {
            // ignore starttime while active
        } else {
            // superclass will fire the fieldChanged events
            super.setStartTime(newStartTime);

            if (vrmlClock != null) {
                resetTimeListener(vrmlClock.getTime());
            }
        }
    }

    /**
     * Set a new value for the stop time. If the sensor is active and the stop
     * time is less than the current start time, it is ignored. If the stop
     * time is less that now, it is set to now.
     *
     * @param newStopTime The new stop time
     */
    public void setStopTime(double newStopTime) {
        if(vfIsActive && (newStopTime < vfStartTime)) {
            // ignore stoptime if active and new time is less than start time.
        } else {
            // superclass will fire the fieldChanged events
            super.setStopTime(newStopTime);
            if (vrmlClock == null)
                resetTimeListener(0);
            else
                resetTimeListener(vrmlClock.getTime());
        }
    }

    /**
     * Set a new description string for this node.
     *
     * @param newDescription The new description string.
     */
    public void setDescription(String newDescription) {
        vfDescription = newDescription;
        if (!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }

    /**
     * Set a new value for isActive.  This may be called by the containing
     * Sound node when non-looped audio has finished playing
     *
     * @param newIsActive New active state for this node.
     */
    public void setIsActive(boolean newIsActive) {
        if(vfIsActive != newIsActive) {
           vfIsActive = newIsActive;
            if(!inSetup) {
                hasChanged[FIELD_IS_ACTIVE] = true;
                fireFieldChanged(FIELD_IS_ACTIVE);
                // do this after the field changed event is sent
            }

            fireSoundStateChanged();
        }
    }

    /**
     * Sets a new value for this node's pitch. The value must be > 0.
     *
     * @param newPitch New value for pitch.
     * @throws InvalidFieldValueException Pitch is <= 0
     */
    public void setPitch(float newPitch) throws InvalidFieldValueException {
        if(newPitch < 0) {
            throw new InvalidFieldValueException
                ("Invalid pitch, must be >0: " + newPitch);
        }

        if (vfPitch != newPitch &&
            newPitch > 0) {
            vfPitch = newPitch;
            if (!inSetup) {
                hasChanged[FIELD_PITCH] = true;
                fireFieldChanged(FIELD_PITCH);
                // do this after the field changed event is sent
                fireSoundStateChanged();
            }
        }
    }

    public void setDuration(double newDuration) {
        if(newDuration != vfDuration) {
            vfDuration = newDuration;

            if(!inSetup) {
                hasChanged[FIELD_DURATION] = true;
                fireFieldChanged(FIELD_DURATION);
            }
        }
    }

    //----------------------------------------------------------------------
    // manage sound state listeners
    //----------------------------------------------------------------------

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param listener The listener instance to add
     */
    public void addSoundStateListener(VRMLSoundStateListener listener) {
        if (!soundStateListeners.contains(listener))
            soundStateListeners.add(listener);
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param listener The listener to be removed
     */
    public void removeSoundStateListener(VRMLSoundStateListener listener) {
        soundStateListeners.remove(listener);
    }

    //----------------------------------------------------------------------
    // Methods defined by manage content state listeners
    //----------------------------------------------------------------------

    /**
     * Add a listener to this node instance for the content state changes. If
     * the listener is already added or null the request is silently ignored.
     *
     * @param listener The listener instance to add
     */
    public void addContentStateListener(VRMLContentStateListener listener) {
        if(!contentListeners.contains(listener))
            contentListeners.add(listener);
    }

    /**
     * Remove a listener from this node instance for the content state changes.
     * If the listener is null or not registered, the request is silently ignored.
     *
     * @param listener The listener to be removed
     */
    public void removeContentStateListener(VRMLContentStateListener listener) {
        contentListeners.remove(listener);
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------------------

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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.AudioClipNodeType;
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
        if(index < 0  || index > LAST_AUDIOCLIP_INDEX)
            return null;

        if((vrmlMajorVersion < 3) && (index == FIELD_IS_PAUSED))
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
            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_PITCH:
                fieldData.clear();
                fieldData.floatValue = vfPitch;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_DURATION:
                fieldData.clear();
                fieldData.doubleValue = vfDuration;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_IS_ACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_IS_PAUSED:
                if(vrmlMajorVersion < 3)
                    throw new InvalidFieldException("Field isPaused not defined for VRML97");

                fieldData.clear();
                fieldData.booleanValue = vfIsPaused;
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

// TODO: Check for all fields handled
        try {
            switch(srcIndex) {
                case FIELD_DESCRIPTION:
                    destNode.setValue(destIndex, vfDescription);
                    break;

                case FIELD_PITCH:
                    destNode.setValue(destIndex, vfPitch);
                    break;

                case FIELD_URL:
                    destNode.setValue(destIndex, vfUrl, vfUrl.length);
                    break;

                case FIELD_DURATION:
                    destNode.setValue(destIndex, vfDuration);
                    break;

                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;

                case FIELD_IS_PAUSED:
                    destNode.setValue(destIndex, vfIsPaused);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseMidiSource sendRoute: No field!" +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseMidiSource sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DESCRIPTION:
                setDescription(value);
                break;

            case FIELD_URL:
                setUrl(new String[] { value }, 1);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL :
                setUrl(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_PITCH :
                setPitch(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        // Send urlChanged events only once per frame
        fireUrlChanged(FIELD_URL);
    }

     /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireUrlChanged(int index) {
        // Notify listeners of new value
        int num_listeners = urlListeners.size();
        VRMLUrlListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = (VRMLUrlListener)urlListeners.get(i);
            ul.urlChanged(this, index);
        }
    }

     /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireSoundStateChanged() {
        // Notify listeners of new value
        int num_listeners = soundStateListeners.size();
        VRMLSoundStateListener listener;

        for(int i = 0; i < num_listeners; i++) {
            listener = (VRMLSoundStateListener)soundStateListeners.get(i);
            listener.soundStateChanged(this, vfIsActive, vfLoop, vfPitch, vfStartTime);
        }
    }

    /**
     * Send a notification to the registered listeners that the content state
     * has been changed. If no listeners have been registered, then this does
     * nothing, so always call it regardless.
     */
    protected void fireContentStateChanged() {
        // Notify listeners of new value
        int num_listeners = contentListeners.size();
        VRMLContentStateListener csl;

        for(int i = 0; i < num_listeners; i++) {
            csl = (VRMLContentStateListener)contentListeners.get(i);
            csl.contentStateChanged(this, FIELD_URL, loadState);
        }
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType
    //----------------------------------------------------------------------

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined in VRMLSingleExternalNodeType.
     *
     * @return The current load state of the node
     */
    public int getLoadState() {
        return loadState;
    }

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined in VRMLSingleExternalNodeType.
     *
     * @param state The new state of the node
     */
    public void setLoadState(int state) {
        switch(state) {
            case VRMLSingleExternalNodeType.LOADING :
                break;
            case VRMLSingleExternalNodeType.LOAD_COMPLETE :
                // if loop is true at startup, we immediately activate
                if (vfLoop) {
                    setIsActive(true);
                }
                break;
            case VRMLSingleExternalNodeType.LOAD_FAILED :
                if (vfUrl != null && vfUrl.length > 0)
                    System.out.println("Loading failed: " + vfUrl[0]);
                break;
            case VRMLSingleExternalNodeType.NOT_LOADED:
                break;
            default :
                System.out.println("Unknown state: " + state);
        }

        loadState = state;

        fireContentStateChanged();
    }


    /**
     * Sets the URL to a new value.  We will load only one
     * of these URL's.  The list provides alternates.
     *
     * @param newURL Array of candidate URL strings
     * @param numValid The number of valid values to copy from the array
     */
    public void setUrl(String[] newURL, int numValid) {
        if(numValid > 0) {
            if(worldURL != null)
                vfUrl = URLChecker.checkURLs(worldURL, newURL, false);
            else
                vfUrl = newURL;
        } else {
            vfUrl = FieldConstants.EMPTY_MFSTRING;
        }

        if (!inSetup) {
            hasChanged[FIELD_URL] = true;
            fireFieldChanged(FIELD_URL);
        }
    }

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    public String[] getUrl() {
        return vfUrl;
    }

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype) {
        return true;  // TODO
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param URI The URI used to load this content
     */
    public void setLoadedURI(String URI) {
    }


    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(String mimetype,
                           Object content)
        throws IllegalArgumentException {
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLExternalNodeType
    //----------------------------------------------------------------------

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    public void setWorldUrl(String url) {
        if((url == null) || (url.length() == 0))
            return;

        // check for a trailing slash. If it doesn't have one, append it.
        if(url.charAt(url.length() - 1) != '/') {
            worldURL = url + '/';
        } else {
            worldURL = url;
        }

        if(vfUrl != null)
            URLChecker.checkURLsInPlace(worldURL, vfUrl, false);
    }

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    public String getWorldUrl() {
        return worldURL;
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param listener The listener instance to add
     */
    public void addUrlListener(VRMLUrlListener listener) {
        if (!urlListeners.contains(listener))
            urlListeners.add(listener);
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param listener The listener to be removed
     */
    public void removeUrlListener(VRMLUrlListener listener) {
        urlListeners.remove(listener);
    }

    //-------------------------------------------------------------------
    // Methods defined by VRMLTimeDependentNodeType
    //-------------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        super.setVRMLClock(clk);
        resetTimeListener(vrmlClock.getTime());
    }

    /**
     * Set the loop variable.  Override the base class.
     *
     * TODO: Need to commonize timer handling, this is not exactly right.
     */
    public void setLoop(boolean loop) {
        super.setLoop(loop);

        if (loop == true) {
            if (!vfIsActive) {
                setIsActive(true);
            }
        } else if (vrmlClock != null) {
            if (vfStopTime <= vrmlClock.getTime()) {
                setIsActive(false);
            }
        }
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLAudioClipNodeType
    //----------------------------------------------------------------------

    /**
     * Accessor method to get current value of field <b>description</b>
     *
     * @return The description
     */
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Accessor method to get current value of field <b>pitch</b>,
     * default value is <code>1</code>.
     *
     * @return The Pitch
     */
    public float getPitch () {
        return vfPitch;
    }

    /**
     * Accessor method to get current value of field duration
     *
     * @return The duration
     */
    public double getDuration() {
        return vfDuration;
    }

    /**
     * Accessor method to get current value of field isActive
     *
     * @return The value of isActive
     */
    public boolean getIsActive() {
        return vfIsActive;
    }

    //----------------------------------------------------------------------
    // Methods defined by VRMLTimeListener
    //----------------------------------------------------------------------

    /**
     * Notification that the time is now this value.
     *
     * @param time The current time
     */
    public void timeClick(long time) {
        resetTimeListener(time);
    }

    //----------------------------------------------------------------------
    // Local methods
    //----------------------------------------------------------------------

    /**
     * Sets the activity state depending on the time.
     * This method (1) checks to see if the node has gone
     * active or inactive, (2) determines if this node needs
     * to be serviced by the time clock, and (3) registers or
     * unregisters for time callbacks accordingly.
     *
     * @param time The current clock time
     */
    private void resetTimeListener(double time) {
        // time, start and stop times may have changed

        // 1: determine if active state changes
        if (vfIsActive) {
            // consider stopping if stopTime < time
            if ((vfStopTime > vfStartTime) &&
                (time >= vfStopTime)) {
                setIsActive(false);
            }
        } else {
            if (vfStartTime >= time && vfLoop) {
                setIsActive(true);
            }

            // consider starting if startTime <= time < stopTime
            if (vfStartTime != 0 && vfStartTime <= time &&
                (vfStopTime <= vfStartTime ||
                 vfStopTime > time)) {
                setIsActive(true);
            }
        }

        // 2: determine new listening state
        boolean newListening = false;  // should I listen?

        if (vfIsActive) {
            // listen if now active and stop time is set
            if (vfStopTime > vfStartTime) {
                newListening = true;
            }
        } else {
            // listen if inactive and start time is in the future
            if (vfStartTime >= time) {
                newListening = true;
            }
        }

        // 3: change listening state if necessary
        if (vrmlClock!=null) {
            if (newListening != isTimeListening) {
                if (isTimeListening) {
                    // stop listening to ticks
                    vrmlClock.removeTimeListener(this);
                } else {
                    // start listening to ticks
                    vrmlClock.addTimeListener(this);
                }
                isTimeListening = newListening;
            }
        }
    }
}
