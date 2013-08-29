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

package org.web3d.vrml.renderer.common.nodes.sound;

// Standard imports
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common Sound node implementation for handling all the fields.
 * <p>
 *
 * The sound node is defined by X3D to be the following:
 * <pre>
 *  Sound : X3DSoundNode {
 *    SFVec3f [in,out] direction  0 0 1 (-inf,inf)
 *    SFFloat [in,out] intensity  1     [0,1]
 *    SFVec3f [in,out] location   0 0 0 (-inf,inf)
 *    SFFloat [in,out] maxBack    10    [0,inf)
 *    SFFloat [in,out] maxFront   10    [0,inf)
 *    SFNode  [in,out] metadata   NULL  [X3DMetadataObject]
 *    SFFloat [in,out] minBack    1     [0,inf)
 *    SFFloat [in,out] minFront   1     [0,inf)
 *    SFFloat [in,out] priority   0     [0,1]
 *    SFNode  [in,out] source     NULL  [X3DSoundSourceNode]
 *    SFBool  []       spatialize TRUE
 *  }
 * </pre>
 *
 * @author Guy Carpenter
 * @version $Revision: 1.15 $
 */
public class BaseSound extends AbstractNode
    implements VRMLContentStateListener,
               VRMLSoundStateListener,
               VRMLTimeDependentNodeType,
               VRMLSoundNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.TimeDependentNodeType };

    /** Index of direction field */
    private static final int FIELD_DIRECTION = LAST_NODE_INDEX + 1;

    /** Index of intensity field */
    private static final int FIELD_INTENSITY = LAST_NODE_INDEX + 2;

    /** Index of location field */
    private static final int FIELD_LOCATION = LAST_NODE_INDEX + 3;

    /** Index of maxback field */
    private static final int FIELD_MAXBACK = LAST_NODE_INDEX + 4;

    /** Index of maxfront field */
    private static final int FIELD_MAXFRONT = LAST_NODE_INDEX + 5;

    /** Index of minback field */
    private static final int FIELD_MINBACK = LAST_NODE_INDEX + 6;

    /** Index of minfront field */
    private static final int FIELD_MINFRONT = LAST_NODE_INDEX + 7;

    /** Index of priority field */
    private static final int FIELD_PRIORITY = LAST_NODE_INDEX + 8;

    /** Index of source field */
    private static final int FIELD_SOURCE = LAST_NODE_INDEX + 9;

    /** Index of spatialize field */
    private static final int FIELD_SPATIALIZE = LAST_NODE_INDEX + 10;

    /** The last field index used by this class */
    private static final int LAST_SOUND_INDEX = FIELD_SPATIALIZE;

    /** Number of fields implemented */
    private static final int NUM_FIELDS = LAST_SOUND_INDEX + 1;

    /** Message for when the proto is not a AudioClip node type */
    protected static final String SOURCE_PROTO_MSG =
        "Proto does not describe a AudioClipNodeType object";

    /** Message for when the node is not a AudioClip node type */
    protected static final String SOURCE_NODE_MSG =
        "Node does not describe a AudioClipNodeType object";

    /** The sim clock this node uses */
    protected VRMLClock vrmlClock;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** exposedField SFVec3f  direction, default 0 0 1 */
    protected float[] vfDirection;

    /** exposedField SFFloat intensity, default 1 */
    protected float vfIntensity;

    /** exposedField SFVec3f location, default 0 0 0 */
    protected float[] vfLocation;

    /** exposedField SFFloat maxBack, default 10 */
    protected float vfMaxBack;

    /** exposedField SFFloat maxFront, default 10 */
    protected float vfMaxFront;

    /** exposedField SFFloat minBack, default 1 */
    protected float vfMinBack;

    /** exposedField SFFloat minFront, default 1 */
    protected float vfMinFront;

    /** exposedField SFFloat priority, default 0 */
    protected float vfPriority;

    /** field SFBool spatialize, default TRUE */
    protected boolean vfSpatialize;

    /** exposedField SFNode source, default  NULL */
    protected VRMLAudioClipNodeType vfSource;

    /** proto version of the source node */
    protected VRMLProtoInstance pSource;


    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_SOURCE, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_DIRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "direction");
        fieldDecl[FIELD_INTENSITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "intensity");
        fieldDecl[FIELD_LOCATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "location");
        fieldDecl[FIELD_MAXBACK] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxBack");
        fieldDecl[FIELD_MINBACK] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "minBack");
        fieldDecl[FIELD_MAXFRONT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxFront");
        fieldDecl[FIELD_MINFRONT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "minFront");
        fieldDecl[FIELD_PRIORITY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "priority");
        fieldDecl[FIELD_SOURCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "source");
        fieldDecl[FIELD_SPATIALIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "spatialize");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_DIRECTION);
        fieldMap.put("direction", idx);
        fieldMap.put("set_direction", idx);
        fieldMap.put("direction_changed", idx);

        idx = new Integer(FIELD_LOCATION);
        fieldMap.put("location", idx);
        fieldMap.put("set_location", idx);
        fieldMap.put("location_changed", idx);

        idx = new Integer(FIELD_INTENSITY);
        fieldMap.put("intensity", idx);
        fieldMap.put("set_intensity", idx);
        fieldMap.put("intensity_changed", idx);

        idx = new Integer(FIELD_MAXBACK);
        fieldMap.put("maxBack", idx);
        fieldMap.put("set_maxBack", idx);
        fieldMap.put("maxBack_changed", idx);

        idx = new Integer(FIELD_MINBACK);
        fieldMap.put("minBack", idx);
        fieldMap.put("set_minBack", idx);
        fieldMap.put("minBack_changed", idx);

        idx = new Integer(FIELD_MAXFRONT);
        fieldMap.put("maxFront", idx);
        fieldMap.put("set_maxFront", idx);
        fieldMap.put("maxFront_changed", idx);

        idx = new Integer(FIELD_MINFRONT);
        fieldMap.put("minFront", idx);
        fieldMap.put("set_minFront", idx);
        fieldMap.put("minFront_changed", idx);

        idx = new Integer(FIELD_PRIORITY);
        fieldMap.put("priority", idx);
        fieldMap.put("set_priority", idx);
        fieldMap.put("priority_changed", idx);

        idx = new Integer(FIELD_SOURCE);
        fieldMap.put("source", idx);
        fieldMap.put("set_source", idx);
        fieldMap.put("source_changed", idx);

        fieldMap.put("spatialize",new Integer(FIELD_SPATIALIZE));
    }

    /**
     * Constructs a new default BaseSound.
     */
    public BaseSound() {
        super("Sound");

        vfDirection = new float[] {0,0,1};
        vfIntensity = 1;
        vfLocation = new float[] {0,0,0};
        vfMaxBack = 10;
        vfMaxFront = 10;
        vfMinBack = 1;
        vfMinFront = 1;
        vfPriority = 0;
        vfSpatialize = true;

        hasChanged = new boolean[LAST_SOUND_INDEX + 1];
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

            index = node.getFieldIndex("direction");
            field = node.getFieldValue(index);
            setDirection(field.floatArrayValue);

            index = node.getFieldIndex("intensity");
            field = node.getFieldValue(index);
            setIntensity(field.floatValue);

            index = node.getFieldIndex("location");
            field = node.getFieldValue(index);
            setLocation(field.floatArrayValue);

            index = node.getFieldIndex("maxBack");
            field = node.getFieldValue(index);
            setMaxBack(field.floatValue);

            index = node.getFieldIndex("maxFront");
            field = node.getFieldValue(index);
            setMaxFront(field.floatValue);

            index = node.getFieldIndex("minBack");
            field = node.getFieldValue(index);
            setMinBack(field.floatValue);

            index = node.getFieldIndex("minFront");
            field = node.getFieldValue(index);
            setMinFront(field.floatValue);

            index = node.getFieldIndex("priority");
            field = node.getFieldValue(index);
            setPriority(field.floatValue);

            index = node.getFieldIndex("spatialize");
            field = node.getFieldValue(index);
            vfSpatialize = field.booleanValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------------------
    // The following methods are used internally and by subclasses
    // to change the values of the sound properties.  Subclasses can
    // override these if they need to perform actions when a property
    // changes.
    //----------------------------------------------------------------------

    /**
     * Changes the direction vector for this sound.
     * Stores the direction vector in vfDirection and
     * fires a fieldChanged event.
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @param direction New direction vector
     * @return true if the field value was actually changed.
     */
    public boolean setDirection(float[] direction) {

        // revisit - do we need to filter out no-change calls?
        vfDirection = direction;

        if (!inSetup) {
            hasChanged[FIELD_DIRECTION] = true;
            fireFieldChanged(FIELD_DIRECTION);
        }

        return true;
    }

    /**
     * Changes the intensity (global gain) for this sound.
     * Stores the new intensity in vfIntensity and
     * fires a fieldChanged event.
     * <p>
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @param intensity New intensity value
     */
    public boolean setIntensity(float intensity) {
        boolean changed = false;
        if (intensity != vfIntensity &&
            intensity >= 0 &&
            intensity <= 1) {
            vfIntensity = intensity;
            if (!inSetup) {
                hasChanged[FIELD_INTENSITY] = true;
                fireFieldChanged(FIELD_INTENSITY);
            }
            changed = true;
        }
        return changed;
    }

    /**
     * Changes the location of this sound.
     * Stores the new location in vfLocation and
     * fires a fieldChanged event.
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @param location New location vector
     */
    public boolean setLocation(float[] location) {

        vfLocation[0] = location[0];
        vfLocation[1] = location[1];
        vfLocation[2] = location[2];

        if(!inSetup) {
            hasChanged[FIELD_LOCATION] = true;
            fireFieldChanged(FIELD_LOCATION);
        }

        return true;
    }

    /**
     * Changes the maxBack distance for this sound.
     * Stores the new value in vfMaxBack and
     * fires a fieldChanged event.  The maxback value
     * specifies the point at which the distance/gain
     * ramp falls to zero behind the sound source.
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @param maxBack New maxBack value.
     */
    public boolean setMaxBack(float maxBack) {
        boolean changed = false;
        if (maxBack != vfMaxBack &&
            maxBack >= 0) {
            vfMaxBack = maxBack;
            if (!inSetup) {
                hasChanged[FIELD_MAXBACK] = true;
                fireFieldChanged(FIELD_MAXBACK);
            }
            changed = true;
        }
        return changed;
    }

    /**
     * Changes the maxFront distance for this sound.
     * Stores the new value in vfMaxFront and
     * fires a fieldChanged event.  The maxfront value
     * specifies the point at which the distance/gain
     * ramp falls to zero in front of the sound source.
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @param maxFront New maxFront value.
     */
    public boolean setMaxFront(float maxFront) {
        boolean changed = false;
        if (maxFront != vfMaxFront &&
            maxFront >= 0) {
            vfMaxFront = maxFront;
            if (!inSetup) {
                hasChanged[FIELD_MAXFRONT] = true;
                fireFieldChanged(FIELD_MAXFRONT);
            }
            changed = true;
        }
        return changed;
    }

    /**
     * Changes the minBack distance for this sound.
     * Stores the new value in vfMinBack and
     * fires a fieldChanged event.  The minback value
     * specifies the point at which the distance/gain
     * ramp starts to fall off from 0db behind the sound source.
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @param minBack New minBack value.
     */
    public boolean setMinBack(float minBack) {
        boolean changed = false;
        if (minBack != vfMinBack &&
            minBack >= 0) {
            vfMinBack = minBack;
            if (!inSetup) {
                hasChanged[FIELD_MINBACK] = true;
                fireFieldChanged(FIELD_MINBACK);
            }
            changed = true;
        }
        return changed;
    }

    /**
     * Changes the minFront distance for this sound.
     * Stores the new value in vfMinFront and
     * fires a fieldChanged event.  The minfront value
     * specifies the point at which the distance/gain
     * ramp starts to fall off from 0db in front of
     * the sound source.
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @param minFront New minFront value.
     */
    public boolean setMinFront(float minFront) {
        boolean changed = false;
        if (minFront != vfMinFront &&
            minFront >= 0) {
            vfMinFront = minFront;
            if (!inSetup) {
                hasChanged[FIELD_MINFRONT] = true;
                fireFieldChanged(FIELD_MINFRONT);
            }
            changed = true;
        }
        return changed;
    }

    /**
     * Changes the sound priority.  Sets vfPriority
     * and fires a FieldChanged event.
     *
     * A false return value indicates the value was unchanged
     * either because it was already set to this value, or because
     * the new value was out of range.
     *
     * @params priority New priority value.
     * @return true if the new priority value is accepted.
     */
    public boolean setPriority(float priority) {
        boolean changed = false;
        if (priority != vfPriority &&
            priority >= 0.0f &&
            priority <= 1.0f) {
            vfPriority = priority;
            if (!inSetup) {
                hasChanged[FIELD_PRIORITY] = true;
                fireFieldChanged(FIELD_PRIORITY);
            }
            changed = true;
        }
        return changed;
    }

    //----------------------------------------------------------------------
    // VRMLNodeType interface
    //----------------------------------------------------------------------

    /**
     * Called when the construction phase of this node has finished.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pSource != null)
            pSource.setupFinished();
        else if(vfSource != null)
            vfSource.setupFinished();
    }

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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.SoundNodeType;
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
        if(index < 0  || index > LAST_SOUND_INDEX)
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
            case FIELD_DIRECTION:
                fieldData.clear();
                fieldData.floatArrayValue = vfDirection;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 3;
                break;

            case FIELD_INTENSITY:
                fieldData.clear();
                fieldData.floatValue = vfIntensity;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_LOCATION:
                fieldData.clear();
                fieldData.floatArrayValue = vfLocation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 3;
                break;

            case FIELD_MAXBACK:
                fieldData.clear();
                fieldData.floatValue = vfMaxBack;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MAXFRONT:
                fieldData.clear();
                fieldData.floatValue = vfMaxFront;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MINBACK:
                fieldData.clear();
                fieldData.floatValue = vfMinBack;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MINFRONT:
                fieldData.clear();
                fieldData.floatValue = vfMinFront;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_PRIORITY:
                fieldData.clear();
                fieldData.floatValue = vfPriority;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SOURCE:
                fieldData.clear();
                fieldData.nodeValue = vfSource;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_SPATIALIZE:
                fieldData.clear();
                fieldData.booleanValue = vfSpatialize;
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
                case FIELD_DIRECTION:
                    destNode.setValue(destIndex, vfDirection, 3);
                    break;

                case FIELD_INTENSITY:
                    destNode.setValue(destIndex, vfIntensity);
                    break;

                case FIELD_LOCATION:
                    destNode.setValue(destIndex, vfLocation, 3);
                    break;

                case FIELD_MAXBACK:
                    destNode.setValue(destIndex, vfMaxBack);
                    break;

                case FIELD_MAXFRONT:
                    destNode.setValue(destIndex, vfMaxFront);
                    break;

                case FIELD_MINBACK:
                    destNode.setValue(destIndex, vfMinBack);
                    break;

                case FIELD_MINFRONT:
                    destNode.setValue(destIndex, vfMinFront);
                    break;

                case FIELD_PRIORITY:
                    destNode.setValue(destIndex, vfPriority);
                    break;

                case FIELD_SOURCE:
                    if(pSource != null)
                        destNode.setValue(destIndex, pSource);
                    else
                        destNode.setValue(destIndex, vfSource);
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
     * Set the value of the field at the given index as a boolean. This would
     * be used to set SFBoolean field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_SPATIALIZE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "spatialize is initializeOnly");

                vfSpatialize = value;
                break;

            default:
                super.setValue(index, value);

        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_INTENSITY:
                setIntensity(value);
                break;

            case FIELD_MAXBACK:
                setMaxBack(value);
                break;

            case FIELD_MAXFRONT:
                setMaxFront(value);
                break;

            case FIELD_MINBACK:
                setMinBack(value);
                break;

            case FIELD_MINFRONT:
                setMinFront(value);
                break;

            case FIELD_PRIORITY:
                setPriority(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided does not fit this
     *    type of node
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_DIRECTION:
                setDirection(value);
                break;

            case FIELD_LOCATION:
                setLocation(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index to a node.
     * This would be used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     */

    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_SOURCE:
                setSource(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------------------
    // VRMLContentStateListener interface
    //----------------------------------------------------------------------

    /**
     * Called when the contentState of the child audio source changes.
     * The base implementation does nothing.  Subclasses can trap this
     * call to determine when the audio source is available for use (when
     * state==LOAD_COMPLETE).
     *
     * @param node The child sound source node.
     * @param index The index of the field that has changed state.
     * @param state The new state value.
     */
    public void contentStateChanged(VRMLNodeType node, int index, int state) {
    }

    //----------------------------------------------------------------------
    // VRMLSoundStateListener interface
    //----------------------------------------------------------------------

    /**
     * Called when the soundState of the child audio source changes.
     * The base implementation does nothing.  Subclasses can trap this
     * call to act when any of the sound state parameters (isActive,
     * loop, and pitch) change.
     *
     * @param node The child sound source node.
     * @param index The index of the field that has changed state.
     * @param state The new state value.
     */
    public void soundStateChanged(VRMLNodeType node,
                                  boolean newIsActive,
                                  boolean newLoop,
                                  float newPitch,
                                  double startTime) {
    }

    //----------------------------------------------------------------------
    // VRMLTimeDependentNodeType interface
    //----------------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        vrmlClock = clk;
    }

    //----------------------------------------------------------------------
    // VRMLNode interface
    //----------------------------------------------------------------------

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    //----------------------------------------------------------------------
    // VRMLSoundNodeNodeType interface
    // This interface is used when instantiating objects from PROTOs.
    // It defines a topology for a sound node - a single source.
    //----------------------------------------------------------------------

    /**
     * Get node content for <code>source</code>
     *
     * @return The current source
     */
    public VRMLAudioClipNodeType getSource() {
        return vfSource;
    }

    /**
     * Set node content as replacement for the audio sources
     *
     * @param newSource The new source.  null will act like delete
     */
    public void setSource(VRMLNodeType source) {

        // if we already had a source, disconnect from it
        deleteSource();

        VRMLNodeType old_node;

        if(pSource != null)
            old_node = pSource;
        else
            old_node = vfSource;

        if(source instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)source).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLAudioClipNodeType))
                throw new InvalidFieldValueException(SOURCE_PROTO_MSG);

            pSource = (VRMLProtoInstance)source;
            vfSource = (VRMLAudioClipNodeType)impl;

        } else if(source != null && !(source instanceof VRMLAudioClipNodeType)) {
            throw new InvalidFieldValueException(SOURCE_NODE_MSG);
        } else {
            pSource = null;
            vfSource = (VRMLAudioClipNodeType)source;
        }

        if(source != null) {
            updateRefs(source, true);
            // REVISIT - These methods must be moved to a new
            // interface which can be implemented by the MovieTexture node.
            ((BaseAudioClip)vfSource).addContentStateListener(this);
            ((BaseAudioClip)vfSource).addSoundStateListener(this);
        }

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(source != null)
                stateManager.registerAddedNode(source);

            hasChanged[FIELD_SOURCE] = true;
            fireFieldChanged(FIELD_SOURCE);
        }
    }

    /**
     * Delete all contained <code>source</code> content
     */
    public void deleteSource() {
        if (vfSource != null) {
        // REVISIT - These methods must be moved to a new
        // interface which can be implemented by the MovieTexture node.
            ((BaseAudioClip)vfSource).removeSoundStateListener(this);
            ((BaseAudioClip)vfSource).removeContentStateListener(this);
        }
        vfSource = null;
    }
}



