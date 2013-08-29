/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.navigation;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;
import org.web3d.vrml.nodes.NavigationInfoChangeListener;
import org.web3d.vrml.renderer.common.nodes.BaseBindableNode;

/**
 * Common base implementation of a NavigationInfo node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.25 $
 */
public abstract class BaseNavigationInfo extends BaseBindableNode
    implements VRMLNavigationInfoNodeType {

    /** Index of the avatarSize field */
    protected static final int FIELD_AVATAR_SIZE = LAST_BINDABLE_INDEX + 1;

    /** Index of the headlight field */
    protected static final int FIELD_HEADLIGHT = LAST_BINDABLE_INDEX + 2;

    /** Index of the speed field */
    protected static final int FIELD_SPEED = LAST_BINDABLE_INDEX + 3;

    /** Index of the type field */
    protected static final int FIELD_TYPE = LAST_BINDABLE_INDEX + 4;

    /** Index of the visibilityLimit field */
    protected static final int FIELD_VISIBILITY_LIMIT = LAST_BINDABLE_INDEX + 5;

    /** Index of the transitionType field */
    protected static final int FIELD_TRANSITION_TYPE = LAST_BINDABLE_INDEX + 6;

    /** Index of the transitionTime field */
    protected static final int FIELD_TRANSITION_TIME = LAST_BINDABLE_INDEX + 7;

    /** The last field index used by this class */
    protected static final int LAST_NAVINFO_INDEX = FIELD_TRANSITION_TIME;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_NAVINFO_INDEX + 1;

    /** Error message when a negative speed is provided */
    private static final String NEG_SPEED_MSG =
        "New NavInfo speed value provided is negative. Must be [0,oo): ";

    /** Error message when a negative visibilityLimit is provided */
    private static final String NEG_VIS_LIMIT_MSG =
        "New NavInfo visibilityLimit value provided is negative. Must be [0,oo): ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** SFFloat list of avatar information */
    protected float[] vfAvatarSize;

    /** SFFloat list of transition time information */
    protected float[] vfTransitionTime;

    /** The number of active items in the avatar size field */
    protected int numAvatarSize;

    /** SFBool indicating if the headlight should be on */
    protected boolean vfHeadlight;

    /** SFFloat current speed we are to travel through the world at */
    protected float vfSpeed;

    /** MFString of the different navigation modes to use */
    protected String[] vfType;

    /** MFString of the way to transition between viewpoints */
    protected String[] vfTransitionType;

    /** The number of active items in the type field */
    protected int numType;

    /** The number of active items in the transitionType field */
    protected int numTransitionType;

    /** SFFloat Culling visibility limit */
    protected float vfVisibilityLimit;

    /** The listener for navigation info changes */
    protected ArrayList<NavigationInfoChangeListener> changeListener;

    /**
     * Static constructor to initialise all of the field values.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<String, Integer>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_AVATAR_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "avatarSize");
        fieldDecl[FIELD_HEADLIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "headlight");
        fieldDecl[FIELD_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "speed");
        fieldDecl[FIELD_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "type");
        fieldDecl[FIELD_VISIBILITY_LIMIT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "visibilityLimit");
        fieldDecl[FIELD_TRANSITION_TYPE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "transitionType");
        fieldDecl[FIELD_TRANSITION_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "transitionTime");
        fieldDecl[FIELD_BIND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "set_bind");

        fieldDecl[FIELD_BIND_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "bindTime");

        fieldDecl[FIELD_IS_BOUND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isBound");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_AVATAR_SIZE);
        fieldMap.put("avatarSize", idx);
        fieldMap.put("set_avatarSize", idx);
        fieldMap.put("avatarSize_changed", idx);

        fieldMap.put("set_bind",new Integer(FIELD_BIND));
        fieldMap.put("isBound",new Integer(FIELD_IS_BOUND));
        fieldMap.put("bindTime",new Integer(FIELD_BIND_TIME));

        idx = new Integer(FIELD_HEADLIGHT);
        fieldMap.put("headlight", idx);
        fieldMap.put("set_headlight", idx);
        fieldMap.put("headlight_changed", idx);

        idx = new Integer(FIELD_TRANSITION_TYPE);
        fieldMap.put("transitionType", idx);
        fieldMap.put("set_transitionType", idx);
        fieldMap.put("transitionType_changed", idx);

        idx = new Integer(FIELD_TRANSITION_TIME);
        fieldMap.put("transitionTime", idx);
        fieldMap.put("set_transitionTime", idx);
        fieldMap.put("transitionTime_changed", idx);

        idx = new Integer(FIELD_SPEED);
        fieldMap.put("speed", idx);
        fieldMap.put("set_speed", idx);
        fieldMap.put("speed_changed", idx);

        idx = new Integer(FIELD_TYPE);
        fieldMap.put("type", idx);
        fieldMap.put("set_type", idx);
        fieldMap.put("type_changed", idx);

        idx = new Integer(FIELD_VISIBILITY_LIMIT);
        fieldMap.put("visibilityLimit", idx);
        fieldMap.put("set_visibilityLimit", idx);
        fieldMap.put("visibilityLimit_changed", idx);
    }

    /**
     * Construct a default node with all of the values set to the given types.
     */
    protected BaseNavigationInfo() {
        super("NavigationInfo");

        hasChanged = new boolean[NUM_FIELDS];

        numAvatarSize = 3;
        numType = 2;

        vfAvatarSize = new float[] { 0.25f, 1.6f, 0.75f };
        vfTransitionTime = new float[] { 1 };
        vfHeadlight = true;
        vfSpeed = 1;

        // vfType is setin the setVersion method.

        vfTransitionType = new String[] { TRANSITION_TYPE_LINEAR };
        vfVisibilityLimit = 0;

        changeListener = new ArrayList<NavigationInfoChangeListener>();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseNavigationInfo(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("avatarSize");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfAvatarSize = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfAvatarSize,
                                 0,
                                 field.numElements);
                numAvatarSize = field.numElements;
            }

            index = node.getFieldIndex("headlight");
            field = node.getFieldValue(index);
            vfHeadlight = field.booleanValue;

            index = node.getFieldIndex("speed");
            field = node.getFieldValue(index);
            vfSpeed = field.floatValue;

            index = node.getFieldIndex("type");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfType = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfType,
                                 0,
                                 field.numElements);
                numType = field.numElements;
            }

            index = node.getFieldIndex("transitionType");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfTransitionType = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfTransitionType,
                                 0,
                                 field.numElements);
                numTransitionType = field.numElements;
            }

            index = node.getFieldIndex("visibilityLimit");
            field = node.getFieldValue(index);
            vfVisibilityLimit = field.floatValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNavigationInfoNodeType
    //-------------------------------------------------------------

    /**
     * Get the list of navigation types that are to be used. It may contain
     * some or all of the constants above. The array will always be non-null.
     *
     * @return The list of navigation types set
     */
    public String[] getType() {
        return vfType;
    }

    /**
     * Get the number of valid navigation types in the result from
     * getType().
     *
     * @return The number of elements in getType().
     */
    public int getNumTypes() {
        return numType;
    }

    /**
     * Add a listener for navigation info changes. Duplicate adds are
     * ignored.
     *
     * @param listener The new navigation info change listener
     */
    public void addNavigationChangedListener(NavigationInfoChangeListener l) {
        if(!changeListener.contains(l))
            changeListener.add(l);
    }

    /**
     * Remove the listener for navigation info changes. If not already added,
     * this request is ignored.
     *
     * @param listener The new navigation info change listener
     */
    public void removeNavigationChangedListener(NavigationInfoChangeListener l) {
        changeListener.remove(l);
    }

    /**
     * Sets the current position in world coordinates.
     *
     * @param wcpos Location of the user in world coordinates(x,y,z)
     */
    public void setWorldLocation(Vector3f wcpos) {
        // ignore
    }

    /**
     * Set the navigation type to the new value(s). The array must be non-null.
     * If the underlying implementation does not support any of the types
     * requested, it shall default to the type NONE. If the array is empty,
     * it defaults to NONE.
     *
     * @param types The list of types to now use in order of preference
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldValueException The list was null or empty
     */
    public void setType(String[] types, int numValid)
        throws InvalidFieldValueException {

        if(numValid == 0) {
            vfType[0] = NAV_TYPE_NONE;
            numType = 1;
        } else {
            if(vfType.length < numValid)
                vfType = new String[numValid];

            numType = numValid;
            System.arraycopy(types, 0, vfType, 0, numType);
        }

        if(!inSetup) {
            hasChanged[FIELD_TYPE] = true;
            fireFieldChanged(FIELD_TYPE);
            for(int i = 0; i < changeListener.size(); i++) {
                NavigationInfoChangeListener l = changeListener.get(i);
                l.notifyNavigationModesChanged(vfType, numType);
            }
        }
    }

    /**
     * Set the transition type to the new value(s). The array must be non-null.
     * If the underlying implementation does not support any of the types
     * requested, it shall default to the type LINEAR. If the array is empty,
     * it defaults to LINEAR.
     *
     * @param types The list of types to now use in order of preference
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldValueException The list was null or empty
     */
    public void setTransitionType(String[] types, int numValid)
        throws InvalidFieldValueException {

        if(numValid == 0) {
            vfTransitionType[0] = TRANSITION_TYPE_LINEAR;
            numTransitionType = 1;
        } else {
            if(vfTransitionType.length < numValid)
                vfTransitionType = new String[numValid];

            numTransitionType = numValid;
            System.arraycopy(types, 0, vfTransitionType, 0, numTransitionType);
        }

        if(!inSetup) {
            hasChanged[FIELD_TRANSITION_TYPE] = true;
            fireFieldChanged(FIELD_TRANSITION_TYPE);

            // TODO: Need something for this?
            //for(int i = 0; i < changeListener.size(); i++) {
            //    NavigationInfoChangeListener l = changeListener.get(i);
            //    l.notifyTransitionTypeChanged(vfSpeed);
            //}
        }
    }

    /**
     * Get the dimensions of the avatar in use.
     *
     * @return A list of floats describing the dimension of the avatar.
     */
    public float[] getAvatarSize() {
        return vfAvatarSize;
    }

    /**
     * Set the dimensions of the avatar in use. The array must have at least
     * three values in it as required by the specification.
     *
     * @param size The new size values to use
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldValueException The array did not contain 3 values
     */
    public void setAvatarSize(float[] size, int numValid)
        throws InvalidFieldValueException {

        if(vfAvatarSize.length < numValid)
            vfAvatarSize = new float[numValid];

        numAvatarSize = numValid;

        System.arraycopy(size, 0, vfAvatarSize, 0, numValid);

        if(!inSetup) {
            hasChanged[FIELD_AVATAR_SIZE] = true;
            fireFieldChanged(FIELD_AVATAR_SIZE);
            for(int i = 0; i < changeListener.size(); i++) {
                NavigationInfoChangeListener l = changeListener.get(i);
                l.notifyAvatarSizeChanged(vfAvatarSize,numAvatarSize);
            }
        }
    }

    /**
     * Get the speed that we are currently moving at.
     *
     * @return The current movement speed.
     */
    public float getSpeed() {
        return vfSpeed;
    }

    /**
     * Set the speed to move at. The speed value must be non-negative.
     *
     * @param newSpeed The new speed value to use
     * @throws InvalidFieldValueException The speed was negative
     */
    public void setSpeed(float newSpeed) throws InvalidFieldValueException {

        if(newSpeed < 0)
            throw new InvalidFieldValueException(NEG_SPEED_MSG + newSpeed);

        if(newSpeed != vfSpeed) {
            vfSpeed = newSpeed;

            if(!inSetup) {
                hasChanged[FIELD_SPEED] = true;
                fireFieldChanged(FIELD_SPEED);
                for(int i = 0; i < changeListener.size(); i++) {
                    NavigationInfoChangeListener l = changeListener.get(i);
                    l.notifyNavigationSpeedChanged(vfSpeed);
                }
            }
        }
    }

    /**
     * Get the visibility limit that we are currently operating at.
     *
     * @return The current movement visibility limit.
     */
    public float getVisibilityLimit() {
        return vfVisibilityLimit;
    }

    /**
     * Set the visibility limie to move at. The visibility limit value must be
     * non-negative.
     *
     * @param limit The new visibility limit value to use
     * @throws InvalidFieldValueException The visibility limit was negative
     */
    public void setVisibilityLimit(float limit)
        throws InvalidFieldValueException {

        if(limit < 0)
            throw new InvalidFieldValueException(NEG_VIS_LIMIT_MSG + limit);

        if(limit != vfVisibilityLimit) {
            vfVisibilityLimit = limit;

            if(!inSetup) {
                hasChanged[FIELD_VISIBILITY_LIMIT] = true;
                fireFieldChanged(FIELD_VISIBILITY_LIMIT);

                for(int i = 0; i < changeListener.size(); i++) {
                    NavigationInfoChangeListener l = changeListener.get(i);
                    l.notifyVisibilityLimitChanged(limit);
                }
            }
        }
    }

    /**
     * Get the status of the headlight that we are operating with. A true
     * value represents the headlight being on.
     *
     * @return true if the headlight is to be used
     */
    public boolean getHeadlight() {
        return vfHeadlight;
    }

    /**
     * Set the statte of the headlight to the new value.
     *
     * @param enable True if we are to use the headlight
     */
    public void setHeadlight(boolean enable) {
        if(enable != vfHeadlight) {
            vfHeadlight = enable;

            if(!inSetup) {
                hasChanged[FIELD_HEADLIGHT] = true;
                fireFieldChanged(FIELD_HEADLIGHT);

                for(int i = 0; i < changeListener.size(); i++) {
                    NavigationInfoChangeListener l = changeListener.get(i);
                    l.notifyHeadlightChanged(enable);
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {

        super.setVersion(major, minor, isStatic);

        // vfType is not null if it was pre-set in the copy constructor. Thus
        // we only want to set it to provide a default value when we are not
        // copying the node from another instance.
        if(vfType == null) {
            if(vrmlMajorVersion < 3)
                vfType = new String[] { NAV_TYPE_WALK, NAV_TYPE_ANY };
            else
                vfType = new String[] { NAV_TYPE_EXAMINE, NAV_TYPE_ANY };
        }
    }


    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

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
        if (index < 0  || index > LAST_NAVINFO_INDEX)
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
        return TypeConstants.NavigationInfoNodeType;
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
            case FIELD_AVATAR_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfAvatarSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numAvatarSize;
                break;
            case FIELD_TRANSITION_TIME:
                fieldData.clear();
                fieldData.floatArrayValue = vfTransitionTime;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = vfTransitionTime.length;
                break;
            case FIELD_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_HEADLIGHT:
                fieldData.clear();
                fieldData.booleanValue = vfHeadlight;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_TYPE:
                fieldData.clear();
                fieldData.stringArrayValue = vfType;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = numType;
                break;

            case FIELD_TRANSITION_TYPE:
                fieldData.clear();
                fieldData.stringArrayValue = vfTransitionType;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = numTransitionType;
                break;

            case FIELD_VISIBILITY_LIMIT:
                fieldData.clear();
                fieldData.floatValue = vfVisibilityLimit;
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

        // Simple impl for now.  ignores time and looping. Note that for a
        // couple of the fields, if the array size is greater than the number
        // of components in it, we create a temporary array to send. This is
        // a negative hit, but it is very rare that someone will route out of
        // these fields, so we don't consider it to be a major impact compared
        // to the performance of having to reallocate the arrays every time
        // someone sets the values, which will happen much, much more often.

        try {
            switch(srcIndex) {
                case FIELD_AVATAR_SIZE:
                    destNode.setValue(destIndex, vfAvatarSize, numAvatarSize);
                    break;
                case FIELD_TRANSITION_TIME:
                    destNode.setValue(destIndex, vfTransitionTime, vfTransitionTime.length);
                    break;

                case FIELD_HEADLIGHT:
                    destNode.setValue(destIndex, vfHeadlight);
                    break;

                case FIELD_SPEED:
                    destNode.setValue(destIndex, vfSpeed);
                    break;

                case FIELD_TYPE:
                    destNode.setValue(destIndex, vfType, numType);
                    break;

                case FIELD_TRANSITION_TYPE:
                    destNode.setValue(destIndex, vfTransitionType, numTransitionType);
                    break;

                case FIELD_VISIBILITY_LIMIT:
                    destNode.setValue(destIndex, vfVisibilityLimit);
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
     * be used to set SFBool field types headlight and bind.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_HEADLIGHT:
                setHeadlight(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types speed and visibilityLimit.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SPEED:
                setSpeed(value);
                break;

            case FIELD_VISIBILITY_LIMIT:
                setVisibilityLimit(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat field type avatarSize.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_AVATAR_SIZE:
                setAvatarSize(value, numValid);
                break;
            case FIELD_TRANSITION_TIME:
                // ignore for now
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TYPE:
                setType(value, numValid);
                break;
            case FIELD_TRANSITION_TYPE:
                setTransitionType(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
