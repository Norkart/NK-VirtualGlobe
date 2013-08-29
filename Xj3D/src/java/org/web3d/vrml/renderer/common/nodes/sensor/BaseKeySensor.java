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

package org.web3d.vrml.renderer.common.nodes.sensor;

// Standard imports - NONE
import java.util.ArrayList;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLKeyDeviceSensorNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSensorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;

import org.web3d.vrml.util.Xj3DKeyCode;
import org.web3d.vrml.util.Xj3DKeyEvent;

/**
 * Base common implementation of a KeySensor node.
 * <p>
 *
 * UI toolkit specific key mappings are handled through
 * the Xj3DKeyEvent object. This object determines the
 * key's properties - whether a character, action key or
 * modifier key.
 *
 * @see Xj3DKeyCode
 * @see Xj3DKeyEvent
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class BaseKeySensor extends BaseSensorNode
    implements VRMLKeyDeviceSensorNodeType {

    // Field index constants
    /** Index of the keyPress field */
    protected static final int FIELD_KEY_PRESS = LAST_SENSOR_INDEX + 1;

    /** Index of the keyRelease field */
    protected static final int FIELD_KEY_RELEASE = LAST_SENSOR_INDEX + 2;

    /** Index of the actionKeyPress field */
    protected static final int FIELD_ACTION_PRESS = LAST_SENSOR_INDEX + 3;

    /** Index of the actionKeyRelease field */
    protected static final int FIELD_ACTION_RELEASE = LAST_SENSOR_INDEX + 4;

    /** Index of the shiftKey field */
    protected static final int FIELD_SHIFTKEY = LAST_SENSOR_INDEX + 5;

    /** Index of the controlKey field */
    protected static final int FIELD_CONTROLKEY = LAST_SENSOR_INDEX + 6;

    /** Index of the altKey field */
    protected static final int FIELD_ALTKEY = LAST_SENSOR_INDEX + 8;

    /** The last index to use */
    protected static final int LAST_KEYSENSOR_INDEX = FIELD_ALTKEY;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_KEYSENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFInt32 keyPress eventOut */
    protected String vfKeyPress;

    /** SFInt32 keyRelease eventOut */
    protected String vfKeyRelease;

    /** SFInt32 actionKeyPress eventOut */
    protected int vfActionKeyPress;

    /** SFInt32 actionKeyPress eventOut */
    protected int vfActionKeyRelease;

    /** SFBool shiftKey_changed eventOut */
    protected boolean vfShiftKey;

    /** SFBool controlKey_changed eventOut */
    protected boolean vfControlKey;

    /** SFBool altKey_changed eventOut */
    protected boolean vfAltKey;

    /**
     * Static constructor intialises all of the field declarations
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
        fieldDecl[FIELD_KEY_PRESS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFString",
                                     "keyPress");
        fieldDecl[FIELD_KEY_RELEASE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFString",
                                     "keyRelease");
        fieldDecl[FIELD_ACTION_PRESS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "actionKeyPress");
        fieldDecl[FIELD_ACTION_RELEASE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "actionKeyRelease");
        fieldDecl[FIELD_SHIFTKEY] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "shiftKey");
        fieldDecl[FIELD_CONTROLKEY] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "controlKey");
        fieldDecl[FIELD_ALTKEY] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "altKey");

        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_KEY_PRESS);
        fieldMap.put("keyPress", idx);
        fieldMap.put("keyPress_changed", idx);

        idx = new Integer(FIELD_KEY_RELEASE);
        fieldMap.put("keyRelease", idx);
        fieldMap.put("keyRelease_changed", idx);

        idx = new Integer(FIELD_ACTION_PRESS);
        fieldMap.put("actionKeyPress", idx);
        fieldMap.put("actionKeyPress_changed",idx);

        idx = new Integer(FIELD_ACTION_RELEASE);
        fieldMap.put("actionKeyRelease", idx);
        fieldMap.put("actionKeyRelease_changed", idx);

        idx = new Integer(FIELD_SHIFTKEY);
        fieldMap.put("shiftKey", idx);
        fieldMap.put("shiftKey_changed", idx);

        idx = new Integer(FIELD_ALTKEY);
        fieldMap.put("altKey", idx);
        fieldMap.put("altKey_changed", idx);

        idx = new Integer(FIELD_CONTROLKEY);
        fieldMap.put("controlKey", idx);
        fieldMap.put("controlKey_changed", idx);

    }

    /**
     * Construct a default viewpoint instance
     */
    public BaseKeySensor() {
        super("KeySensor");

        hasChanged = new boolean[NUM_FIELDS];

        vfShiftKey = false;
        vfControlKey = false;
        vfAltKey = false;

        vfEnabled = true;
        vfIsActive = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseKeySensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSensorNodeType)node);
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLKeyDeviceSensorNodeType interface.
    //-------------------------------------------------------------

    /**
     * See if this key device sensor requires only the last key sent or all of
     * them. Always returns true for KeySensors.
     *
     * @return true if this only requires the last key event
     */
    public boolean requiresLastEventOnly() {
        return true;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLKeyListener interface.
    //-------------------------------------------------------------

    /**
     * Notification of a key press event.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyPressed( Xj3DKeyEvent evt ) {

        vfIsActive = true;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);

        if ( evt.isCharacter( ) ) {
            vfKeyPress = String.valueOf( evt.getKeyChar( ) );
            hasChanged[FIELD_KEY_PRESS] = true;
            fireFieldChanged(FIELD_KEY_PRESS);
        }
        else if ( evt.isModifier( ) ) {
            int keyCode = evt.getKeyCode();
            switch( keyCode ) {
            case Xj3DKeyCode.MODIFIER_KEY_SHIFT:
                vfShiftKey = true;
                hasChanged[FIELD_SHIFTKEY] = true;
                fireFieldChanged(FIELD_SHIFTKEY);
                break;

            case Xj3DKeyCode.MODIFIER_KEY_CONTROL:
                vfControlKey = true;
                hasChanged[FIELD_CONTROLKEY] = true;
                fireFieldChanged(FIELD_CONTROLKEY);
                break;

            case Xj3DKeyCode.MODIFIER_KEY_ALT:
                vfAltKey = true;
                hasChanged[FIELD_ALTKEY] = true;
                fireFieldChanged(FIELD_ALTKEY);
                break;
            }
        }
        else if ( evt.isAction( ) ) {
            vfActionKeyPress = evt.getKeyCode( );
            hasChanged[FIELD_ACTION_PRESS] = true;
            fireFieldChanged(FIELD_ACTION_PRESS);
        }
    }

    /**
     * Notification of a key release event.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyReleased( Xj3DKeyEvent evt ) {
        if ( evt.isCharacter( ) ) {
            vfKeyRelease = String.valueOf( evt.getKeyChar( ) );
            hasChanged[FIELD_KEY_RELEASE] = true;
            fireFieldChanged(FIELD_KEY_RELEASE);
        }
        else if ( evt.isModifier( ) ) {
            int keyCode = evt.getKeyCode();
            switch( keyCode ) {
            case Xj3DKeyCode.MODIFIER_KEY_SHIFT:
                vfShiftKey = false;
                hasChanged[FIELD_SHIFTKEY] = true;
                fireFieldChanged(FIELD_SHIFTKEY);
                break;

            case Xj3DKeyCode.MODIFIER_KEY_CONTROL:
                vfControlKey = false;
                hasChanged[FIELD_CONTROLKEY] = true;
                fireFieldChanged(FIELD_CONTROLKEY);
                break;

            case Xj3DKeyCode.MODIFIER_KEY_ALT:
                vfAltKey = false;
                hasChanged[FIELD_ALTKEY] = true;
                fireFieldChanged(FIELD_ALTKEY);
                break;
            }
        }
        else if ( evt.isAction( ) ) {
            vfActionKeyRelease = evt.getKeyCode( );
            hasChanged[FIELD_ACTION_RELEASE] = true;
            fireFieldChanged(FIELD_ACTION_RELEASE);
        }

        vfIsActive = false;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
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
        if(index < 0  || index > LAST_KEYSENSOR_INDEX)
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
        return TypeConstants.KeyDeviceSensorNodeType;
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
            case FIELD_KEY_PRESS:
                fieldData.clear();
                fieldData.stringValue = vfKeyPress;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_KEY_RELEASE:
                fieldData.clear();
                fieldData.stringValue = vfKeyRelease;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_ACTION_PRESS:
                fieldData.clear();
                fieldData.intValue = vfActionKeyPress;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_ACTION_RELEASE:
                fieldData.clear();
                fieldData.intValue = vfActionKeyRelease;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_SHIFTKEY:
                fieldData.clear();
                fieldData.booleanValue = vfShiftKey;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ALTKEY:
                fieldData.clear();
                fieldData.booleanValue = vfAltKey;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CONTROLKEY:
                fieldData.clear();
                fieldData.booleanValue = vfControlKey;
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
                case FIELD_KEY_PRESS:
                    destNode.setValue(destIndex, vfKeyPress);
                    break;

                case FIELD_KEY_RELEASE:
                    destNode.setValue(destIndex, vfKeyRelease);
                    break;

                case FIELD_ACTION_PRESS:
                    destNode.setValue(destIndex, vfActionKeyPress);
                    break;

                case FIELD_ACTION_RELEASE:
                    destNode.setValue(destIndex, vfActionKeyRelease);
                    break;

                case FIELD_SHIFTKEY:
                    destNode.setValue(destIndex, vfShiftKey);
                    break;

                case FIELD_CONTROLKEY:
                    destNode.setValue(destIndex, vfControlKey);
                    break;

                case FIELD_ALTKEY:
                    destNode.setValue(destIndex, vfAltKey);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }
}
