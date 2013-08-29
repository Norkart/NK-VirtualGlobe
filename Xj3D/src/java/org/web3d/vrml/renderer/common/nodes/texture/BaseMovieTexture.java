/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.texture;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.util.URLChecker;

import org.web3d.vrml.renderer.common.nodes.BaseTimeControlledNode;

import org.web3d.vrml.renderer.common.input.movie.MovieDecoder;
import org.web3d.vrml.renderer.common.input.movie.VideoStreamHandler;

/**
 * MovieTexture node implementation.
 * <p>
 *
 *
 * @author Guy Carpenter
 * @version $Revision: 1.21 $
 */

// IMPLEMENTATION NOTES:
//
// MovieTexture {
//   exposedField SFBool   loop             FALSE
//   exposedField SFFloat  speed            1.0      # (-,)
//   exposedField SFTime   startTime        0        # (-,)
//   exposedField SFTime   stopTime         0        # (-,)
//   exposedField MFString url              []
//   field        SFBool   repeatS          TRUE
//   field        SFBool   repeatT          TRUE
//   eventOut     SFTime   duration_changed
//   eventOut     SFBool   isActive
// }
//

//  From Annotated VRML: If a MovieTexture node is inactive when the
//  movie is first loaded, frame 0 of the movie texture is displayed
//  if speed is non-negative or the last frame of the movie texture is
//  shown if speed is negative.  A MovieTexture node shall display
//  frame 0 if speed = 0. For positive values of speed, an active
//  MovieTexture node displays the frame at movie time t as follows
//
// When a MovieTexture node becomes inactive, the frame corresponding
// to the time at which the MovieTexture became inactive will remain
// as the texture.

public class BaseMovieTexture extends BaseTimeControlledNode
    implements VRMLSingleExternalNodeType,
               VRMLTimeListener,
               VRMLTimeDependentNodeType,
               VRMLTexture2DNodeType,
               FrameStateListener,
               VideoStreamHandler {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.SingleExternalNodeType,
        TypeConstants.TimeDependentNodeType,
        TypeConstants.AudioClipNodeType
    };

    /** Index of speed field */
    private static final int FIELD_SPEED = LAST_TIME_INDEX + 1;

    /** Index of url field */
    private static final int FIELD_URL = LAST_TIME_INDEX + 2;

    /** Index of repeatS field */
    private static final int FIELD_REPEAT_S = LAST_TIME_INDEX + 3;

    /** Index of repeatT field */
    private static final int FIELD_REPEAT_T = LAST_TIME_INDEX + 4;

    /** Index of duration_changed field */
    private static final int FIELD_DURATION = LAST_TIME_INDEX + 5;

    /** Index of active field */
    private static final int FIELD_IS_ACTIVE = LAST_TIME_INDEX + 6;

    /**
     * Field Index for the textureProperties field that is available from
     * 3.2 or later specs.
     */
    protected static final int FIELD_TEXTURE_PROPERTIES = LAST_TIME_INDEX + 7;

    /** The last field index used by this class */
    private static final int LAST_AUDIOCLIP_INDEX = FIELD_TEXTURE_PROPERTIES;

    /** Number of fields implemented */
    private static final int NUM_FIELDS = LAST_AUDIOCLIP_INDEX + 1;

    /** Message for when the proto is not a TextureProperties */
    protected static final String TEXTURE_PROPS_PROTO_MSG =
        "Proto does not describe a TextureProperties object";

    /** Message for when the node in setValue() is not a TextureProperty */
    protected static final String TEXTURE_PROPS_NODE_MSG =
        "Node does not describe a TextureProperties object";


    /**
     * Message when accessing the texture properties field in a file with
     * the spec version earlier than 3.2.
     */
    protected static final String TEXPROPS_VERSION_MSG =
        "The textureProperties field is not available before X3D V3.2";

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** The array of fields that need URL content */
    private static int[] urlFieldIndexList;

    /** The sim clock this node uses */
    private VRMLClock vrmlClock;

    /** Is this object receiving time callbacks */
    private boolean isTimeListening;

    /**  exposedField SFFloat  speed, default 1.0 */
    protected float vfSpeed;

    /** exposedField MFString url */
    protected String[] vfURL;

    /** exposedField SFBool repeatS TRUE */
    protected boolean vfRepeatS;

    /** exposedField SFBool repeatT, TRUE */
    protected boolean vfRepeatT;

    /** duration of most recently loaded clip */
    protected double vfDuration;

    /** eventOut SFBool   isActive */
    protected boolean vfIsActive;

    /** PROTO version of the textureProperties node */
    protected VRMLProtoInstance pTextureProperties;

    /** inputOutput SFNode textureProperties main field */
    protected VRMLTextureProperties2DNodeType vfTextureProperties;

    /** List of those who want to know about Url changes. */
    private ArrayList urlListeners;

    /** The array of listeners for texture changes */
    protected ArrayList textureListeners;

    /** List of those who want to know about state changes. */
    private ArrayList contentListeners;

    /** URL Load State */
    private int loadState = NOT_LOADED;

    /** URL Load State */
    private String worldURL;

    /** decoder to parse the input stream into frames */
    private MovieDecoder movieDecoder;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_TEXTURE_PROPERTIES
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_LOOP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "loop");
        fieldDecl[FIELD_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "speed");
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

        fieldDecl[FIELD_RESUME_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFTime",
                                     "resumeTime");

        fieldDecl[FIELD_ELAPSED_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "elapsedTime");
        fieldDecl[FIELD_REPEAT_S] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatS");
        fieldDecl[FIELD_REPEAT_T] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatT");
        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "url");
        fieldDecl[FIELD_TEXTURE_PROPERTIES] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "textureProperties");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_LOOP);
        fieldMap.put("loop", idx);
        fieldMap.put("set_loop", idx);
        fieldMap.put("loop_changed", idx);

        idx = new Integer(FIELD_SPEED);
        fieldMap.put("speed", idx);
        fieldMap.put("set_speed", idx);
        fieldMap.put("speed_changed", idx);

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

        idx = new Integer(FIELD_REPEAT_S);
        fieldMap.put("repeatS", idx);

        idx = new Integer(FIELD_REPEAT_T);
        fieldMap.put("repeatT", idx);

        idx = new Integer(FIELD_TEXTURE_PROPERTIES);
        fieldMap.put("textureProperties", idx);

        idx = new Integer(FIELD_URL);
        fieldMap.put("url", idx);
        fieldMap.put("set_url", idx);
        fieldMap.put("url_changed", idx);

        fieldMap.put("isActive", new Integer(FIELD_IS_ACTIVE));
        fieldMap.put("duration_changed", new Integer(FIELD_DURATION));
        fieldMap.put("elapsedTime", new Integer(FIELD_ELAPSED_TIME));

        urlFieldIndexList = new int[1];
        urlFieldIndexList[0] = FIELD_URL;
    }

    /**
     * Constructor creates a default movietexture node.
     */
    public BaseMovieTexture() {
        super("MovieTexture");

        urlListeners = new ArrayList();
        contentListeners = new ArrayList();
        textureListeners = new ArrayList();
        hasChanged = new boolean[LAST_AUDIOCLIP_INDEX + 1];

        vfURL = FieldConstants.EMPTY_MFSTRING;
        vfIsActive = false;
        vfLoop = false;
        vfSpeed = 1.0f;
        vfStartTime = 0;
        vfStopTime = 0;
        vfRepeatS = true;
        vfRepeatT = true;
        vfDuration = -1.0;

        isTimeListening = false;

        try {
            movieDecoder = new MovieDecoder(this);
        } catch (NoClassDefFoundError ex) {
            System.err.println("Error creating movie decoder : "+ex.toString());
        }
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

            index = node.getFieldIndex("loop");
            field = node.getFieldValue(index);
            setLoop(field.booleanValue);

            index = node.getFieldIndex("repeatS");
            field = node.getFieldValue(index);
            vfRepeatS = field.booleanValue;

            index = node.getFieldIndex("repeatT");
            field = node.getFieldValue(index);
            vfRepeatT = field.booleanValue;

            index = node.getFieldIndex("speed");
            field = node.getFieldValue(index);
            setSpeed(field.floatValue);

            index = node.getFieldIndex("url");
            field = node.getFieldValue(index);
            setUrl(field.stringArrayValue, field.numElements);

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the texture type of this texture.  Valid entries are defined
     * in the vrml.lang.TextureConstants.
     */
    public int getTextureType() {
        return TextureConstants.TYPE_SINGLE_2D;
    }

    /**
     * Get a string for cacheing this object.  Null means do not cache this
     * texture.
     *
     * @param stage The stage number,  0 for all single stage textures.
     * @return A string to use in lookups.  Typically the url loaded.
     */
    public String getCacheString(int stage) {
/*
        VRMLTextureNodeType tex = (VRMLTextureNodeType) vfTexture.get(stage);

        if (tex == null)
            return null;

        return tex.getCacheString(0);
*/
    return null;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a listener for texture changes. If the listener is already
     * registered then this request is ignored.
     *
     * @param l The listener instance to be added
     */
    public void addTextureListener(VRMLTextureListener l) {
        if (!textureListeners.contains(l)) {
            textureListeners.add(l);
        }
    }

    /**
     * Removes a listener for texture changes. If the listener is not already
     * registered, the request is ignored.
     *
     * @param l The listener to be removed
     */
    public void removeTextureListener(VRMLTextureListener l) {
        textureListeners.remove(l);
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLTexture2DNodeType interface.
    //-------------------------------------------------------------
    /**
     * Get the image representation of this texture.
     *
     * @return The image.
     */
    public NIOBufferImage getImage() {
        return null;
    }

    /**
     * Get the value of field repeatS. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatS
     */
    public boolean getRepeatS() {
        return vfRepeatS;
    }

    /**
     * Get the value of field repeatT. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatT
     */
    public boolean getRepeatT() {
        return vfRepeatT;
    }

    /**
     * Get node content for the textureProperties field. This field is only
     * available for X3D 3.2 or later.
     *
     * @return The current field value
     * @throws InvalidFieldException This field was request in a field with
     *    spec version < 3.2
     */
    public VRMLNodeType getTextureProperties()
        throws InvalidFieldException {

        if(vrmlMajorVersion <= 3 && vrmlMinorVersion < 2) {
            InvalidFieldException ife =
                new InvalidFieldException(TEXPROPS_VERSION_MSG);
            ife.setFieldName("TextureProperties");
            throw ife;
        }

        if (pTextureProperties != null) {
            return pTextureProperties;
        } else {
            return vfTextureProperties;
        }
    }

    /**
     * Set node content as replacement for the textureProperties field. This
     * field is only available for X3D 3.2 or later.
     *
     * @param props The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     * @throws InvalidFieldException This field was request in a field with
     *    spec version < 3.2
     */
    public void setTextureProperties(VRMLNodeType props)
        throws InvalidFieldValueException, InvalidFieldException {

        if(vrmlMajorVersion <= 3 && vrmlMinorVersion < 2) {
            InvalidFieldException ife =
                new InvalidFieldException(TEXPROPS_VERSION_MSG);
            ife.setFieldName("TextureProperties");
            throw ife;
        }

        VRMLNodeType old_node;

        if(pTextureProperties != null)
            old_node = pTextureProperties;
        else
            old_node = vfTextureProperties;

        if(props instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)props).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLTextureProperties2DNodeType))
                throw new InvalidFieldValueException(TEXTURE_PROPS_PROTO_MSG);

            pTextureProperties = (VRMLProtoInstance)props;
            vfTextureProperties = (VRMLTextureProperties2DNodeType)impl;

        } else if(props != null && !(props instanceof VRMLTextureProperties2DNodeType)) {
            throw new InvalidFieldValueException(TEXTURE_PROPS_NODE_MSG);
        } else {
            pTextureProperties = null;
            vfTextureProperties = (VRMLTextureProperties2DNodeType)props;
        }

        if(props != null)
            updateRefs(props, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(props != null)
                stateManager.registerAddedNode(props);

            hasChanged[FIELD_TEXTURE_PROPERTIES] = true;
            fireFieldChanged(FIELD_TEXTURE_PROPERTIES);
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
        //Debug.trace();
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
        //Debug.trace();
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
     * Set a new value for isActive.  This may be called by the containing
     * Sound node when non-looped audio has finished playing
     *
     * @param newIsActive New active state for this node.
     */
    public void setIsActive(boolean newIsActive) {

        if (vfIsActive != newIsActive) {
            vfIsActive = newIsActive;
            if (!inSetup) {
                if (vfIsActive) {
                    movieRewind();
                    movieStart();
                } else {
                    movieStop();
                }
                hasChanged[FIELD_IS_ACTIVE] = true;
                fireFieldChanged(FIELD_IS_ACTIVE);
            }
        }
    }

    /**
     * Sets a new value for this node's speed.
     *
     * @param newSpeed New value for speed.
     */
    public void setSpeed(float newSpeed) {
        if (vfSpeed != newSpeed &&
            newSpeed > 0) {
            vfSpeed = newSpeed;
            movieSetSpeed(vfSpeed);
            if (!inSetup) {
                hasChanged[FIELD_SPEED] = true;
                fireFieldChanged(FIELD_SPEED);
            }
        }
    }

    /**
     * Sets a new value for this node's duration.
     * This call will be made from the VideoStreamHandler
     * interface callbacks once the duration has been determined.
     * The duration may be undefined, in which case it will be -1.
     *
     * @param newDuration - the new duration in seconds.
     */
    public void setDuration(double newDuration) {
        if (newDuration != vfDuration) {
            vfDuration = newDuration;

            if(!inSetup) {
                hasChanged[FIELD_DURATION] = true;
                fireFieldChanged(FIELD_DURATION);
            }
        }
    }

    //----------------------------------------------------------------------
    // manage content state listeners
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
     * Remove a listener from this node instance for the content state
     * changes.  If the listener is null or not registered, the
     * request is silently ignored.
     *
     * @param listener The listener to be removed
     */
    public void removeContentStateListener(VRMLContentStateListener listener) {
        contentListeners.remove(listener);
    }

    //----------------------------------------------------------------------
    // VRMLNodeType methods
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
        return TypeConstants.TextureNodeType;
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

            case FIELD_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_REPEAT_S:
                fieldData.clear();
                fieldData.booleanValue = vfRepeatS;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_REPEAT_T:
                fieldData.clear();
                fieldData.booleanValue = vfRepeatT;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfURL;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfURL.length;
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

            case FIELD_TEXTURE_PROPERTIES:
                if(vrmlMajorVersion <= 3 && vrmlMinorVersion < 2) {
                    InvalidFieldException ife =
                        new InvalidFieldException(TEXPROPS_VERSION_MSG);
                    ife.setFieldName("TextureProperties");
                    throw ife;
                }

                fieldData.clear();
                if(pTextureProperties != null)
                    fieldData.nodeValue = pTextureProperties;
                else
                    fieldData.nodeValue = vfTextureProperties;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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

        if(pTextureProperties != null)
            pTextureProperties.setupFinished();
        else if(vfTextureProperties != null)
            vfTextureProperties.setupFinished();
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
                case FIELD_TEXTURE_PROPERTIES:
                    if(pTextureProperties != null)
                        destNode.setValue(destIndex, pTextureProperties);
                    else
                        destNode.setValue(destIndex, vfTextureProperties);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException {

        switch (index) {
            case FIELD_REPEAT_S:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot write to repeatS");
                else
                    vfRepeatS = value;
                break;

            case FIELD_REPEAT_T:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot write to repeatT");
                else
                    vfRepeatT = value;
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
            case FIELD_SPEED :
                if(value < 0)
                    throw new InvalidFieldValueException
                        ("Invalid speed, must be >0: " + value);

                setSpeed(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     * @throws InvalidFieldException This field was request in a field with
     *    spec version < 3.2
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_TEXTURE_PROPERTIES:
                setTextureProperties(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------------------
    // Local methods
    //----------------------------------------------------------------------

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
    // VRMLSingleExternalNodeType interface
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
                if(vfLoop) {
                    setIsActive(true);
                }
                break;

            case VRMLSingleExternalNodeType.LOAD_FAILED :
                if (vfURL != null && vfURL.length > 0)
                    System.out.println("Movie Loading failed: " + vfURL[0]);

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
                vfURL = URLChecker.checkURLs(worldURL, newURL, false);
            else
                vfURL = newURL;
        } else {
            vfURL = FieldConstants.EMPTY_MFSTRING;
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
        return vfURL;
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
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {

        // REVISIT - we ignore the content object and use
        // URL[0] directly.  This is very wrong, but we haven't
        // been able to get agreement on how to update the content
        // loader yet, so this will have to do in the meantime.
        String url = vfURL[0];
        if (movieDecoder!=null) {
            movieDecoder.init(url);
        } else {
            throw new IllegalArgumentException("JMF not available");
        }
    }

    //----------------------------------------------------------------------
    // VRMLExternalNodeType interfaces
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

        if(vfURL != null) {
            URLChecker.checkURLsInPlace(worldURL, vfURL, false);
        }
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
        if(!urlListeners.contains(listener))
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
    // VRMLTimeDependentNodeType interface
    //-------------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        vrmlClock = clk;
        resetTimeListener(vrmlClock.getTime());
    }

    //----------------------------------------------------------------------
    // VRMLTimeListener interface
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
    // VRMLAudioClipNodeType interface
    //----------------------------------------------------------------------

    /**
     * Accessor method to get current value of field <b>speed</b>,
     * default value is <code>1</code>.
     *
     * @return The Speed
     */
    public float getSpeed () {
        return vfSpeed;
    }

    /**
     * Accessor method to get current value of field <b>duration</b>
     *
     * @return The duration
     */
    public double getDuration() {
        return vfDuration;
    }

    /**
     * Accessor method to get current value of field <b>isActive</b>
     *
     * @return The value of isActive
     */
    public boolean getIsActive() {
        return vfIsActive;
    }

    //----------------------------------------------------------------------
    // VideoStreamHandler interface
    //----------------------------------------------------------------------
    /**
     * Called when a frame is available for display.
     * NOTE - subclasses should override this method.
     *
     * @param image - the next image as an RGB format NIOBufferImage
     */
    public void videoStreamFrame(NIOBufferImage image) {
    }

    /**
     * Called when the format is known.
     * NOTE - subclasses should override this method.
     *
     * @param width - horizontal image size in pixels
     * @param height - vertical image size in pixels
     */
    public void videoStreamFormat(int width, int height) {
    }

    /**
     * Called after the last frame is sent.
     */
    public void videoStreamStop() {
        // if we are looping, we need to restart
        if (vfIsActive) {
            if (vfLoop) {
                movieRewind();
                movieStart();
            } else {
                setIsActive(false);
            }
        }
    }

    /**
     * Called once before the first frame is sent.
     */
    public void videoStreamStart() {
    }

    /**
     * Called when the duration of the stream is known.
     *
     * @params seconds - number of seconds the stream runs for, or -1 if unknown.
     */
    public void videoStreamDuration(double duration) {
        setDuration(duration);
    }

    //----------------------------------------------------------------------
    // private methods for pushing the movie around
    //----------------------------------------------------------------------

    /**
     * Starts the movie from it's current location
     */
    private void movieStart() {
        if (movieDecoder!=null) {
            movieDecoder.start();
        }
    }

    /**
     * Rewinds a stopped movie and leaves it stopped.
     */
    private void movieRewind() {
        if (movieDecoder!=null) {
            movieDecoder.rewind();
        }
    }

    /**
     * Stop a running movie
     */
    private void movieStop() {
        if (movieDecoder!=null) {
            movieDecoder.stop();
        }
    }

    /**
     * Set the relative speed of a stopped movie
     *
     * @param speed - new relative rate factor.
     */
    private void movieSetSpeed(float speed) {
        if (movieDecoder!=null) {
            movieDecoder.setRate(speed);
        }
    }


    /**
     * Fire a textureImageChanged event to the listeners.
     *
     * @param idx The stage
     * @param node The node which changed
     * @param image The new image
     * @param url The url used to load or null.
     */
    protected void fireTextureImageChanged(int idx, VRMLTextureNodeType node, NIOBufferImage image, String url) {
        int size = textureListeners.size();
        VRMLTextureListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (VRMLTextureListener)textureListeners.get(i);
                l.textureImageChanged(idx, node, image, url);
            } catch(Exception e) {
                System.out.println("Error sending textureImpl changed message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------------------
    // Internal methods
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
            // consider starting if startTime <= time < stopTime
            if (vfStartTime <= time &&
                (vfStopTime < vfStartTime ||
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
