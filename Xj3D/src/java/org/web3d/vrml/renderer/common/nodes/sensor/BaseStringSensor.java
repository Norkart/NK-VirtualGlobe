/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLKeyDeviceSensorNodeType;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSensorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseSensorNode;

import org.web3d.vrml.util.Xj3DKeyEvent;

/**
 * Base common implementation of a StringSensor node.
 * <p>
 *
 * UI toolkit specific key mappings are handled through
 * the Xj3DKeyEvent object. This object determines the
 * key's properties - whether a character or control key.
 *
 * @see Xj3DKeyEvent
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class BaseStringSensor extends BaseSensorNode
    implements VRMLKeyDeviceSensorNodeType {

    // Field index constants

    /** Index of the deletionAllowed field */
    protected static final int FIELD_DELETION_ALLOWED = LAST_SENSOR_INDEX + 1;

    /** Index of the enteredText field */
    protected static final int FIELD_ENTERED_TEXT = LAST_SENSOR_INDEX + 2;

    /** Index of the finalText field */
    protected static final int FIELD_FINAL_TEXT = LAST_SENSOR_INDEX + 3;

    /** The last index to use */
    protected static final int LAST_STRINGSENSOR_INDEX = FIELD_FINAL_TEXT;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_STRINGSENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** SFString enteredText eventOut */
    protected String vfEnteredText;

    /** SFString finalText eventOut */
    protected String vfFinalText;

    /** SFBool deletionAllowed field */
    protected boolean vfDeletionAllowed;

    /**
     * The internal buffer holding key strokes as they're entered. If this is
     * a new string then the reference will be null. It is cleared just after
     * the finalText string is issued.
     */
    private StringBuffer workingString;

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
        fieldDecl[FIELD_ENTERED_TEXT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
            "SFString",
            "enteredText");
        fieldDecl[FIELD_FINAL_TEXT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
            "SFString",
            "finalText");
        fieldDecl[FIELD_DELETION_ALLOWED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFBool",
            "deletionAllowed");


        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = new Integer(FIELD_FINAL_TEXT);
        fieldMap.put("finalText", idx);
        fieldMap.put("finalText_changed", idx);

        idx = new Integer(FIELD_ENTERED_TEXT);
        fieldMap.put("enteredText", idx);
        fieldMap.put("enteredText_changed", idx);

        idx = new Integer(FIELD_DELETION_ALLOWED);
        fieldMap.put("deletionAllowed", idx);
        fieldMap.put("deletionAllowed_changed",idx);
        fieldMap.put("set_deletionAllowed", idx);

        fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
    }

    /**
     * Construct a default StringSensor instance
     */
    public BaseStringSensor() {
        super("StringSensor");

        hasChanged = new boolean[NUM_FIELDS];

        vfDeletionAllowed = true;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseStringSensor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSensorNodeType)node);

        try {
            int index = node.getFieldIndex("deletionAllowed");
            VRMLFieldData field = node.getFieldValue(index);
            vfDeletionAllowed = field.booleanValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLKeyDeviceSensorNodeType
    //-------------------------------------------------------------

    /**
     * See if this key device sensor requires only the last key sent or all of
     * them. Always returns true for StringSensors.
     *
     * @return true if this only requires the last key event
     */
    public boolean requiresLastEventOnly() {
        return false;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLKeyListener
    //-------------------------------------------------------------

    /**
     * Notification of a key press event. Ignored. All manipulation
     * of the working string is processed in the keyReleased( ) method.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyPressed( Xj3DKeyEvent evt ) {
    }

    /**
     * Notification of a key release event. All manipulation of the
     * working string is processed in this method.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyReleased( Xj3DKeyEvent evt ) {
        if ( evt.isCharacter( ) ) {
            if ( evt.isEnterKey( ) ) {
                if ( workingString != null ) {
                    vfFinalText = workingString.toString( );
                    vfEnteredText = null;

                    // clear text and set to non-active again
                    workingString = null;

                    vfIsActive = false;
                    hasChanged[FIELD_IS_ACTIVE] = true;
                    fireFieldChanged(FIELD_IS_ACTIVE);

                    hasChanged[FIELD_FINAL_TEXT] = true;
                    fireFieldChanged(FIELD_FINAL_TEXT);
                }
            }
            else if ( evt.isBackspaceKey( ) ) {
                if( vfDeletionAllowed && ( workingString != null ) ) {
                    int len = workingString.length( );
                    if ( len > 0 ) {
                        workingString.deleteCharAt( len - 1 );
                        hasChanged[FIELD_ENTERED_TEXT] = true;
                        fireFieldChanged(FIELD_ENTERED_TEXT);
                    }
                }
            }
            else {
                // All other keys are added to the string buffer
                if( workingString == null ) {
                    workingString = new StringBuffer( );

                    vfIsActive = true;
                    hasChanged[FIELD_IS_ACTIVE] = true;
                    fireFieldChanged(FIELD_IS_ACTIVE);
                }

                workingString.append( evt.getKeyChar( ) );

                hasChanged[FIELD_ENTERED_TEXT] = true;
                fireFieldChanged(FIELD_ENTERED_TEXT);
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
        if(index < 0  || index > LAST_STRINGSENSOR_INDEX)
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
        case FIELD_DELETION_ALLOWED:
            fieldData.clear();
            fieldData.booleanValue = vfDeletionAllowed;
            fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
            break;

        case FIELD_ENTERED_TEXT:
            fieldData.clear();

            if(workingString != null) {
                vfEnteredText = workingString.toString();
                fieldData.stringValue = vfEnteredText;
            }
            fieldData.dataType = VRMLFieldData.STRING_DATA;
            break;

        case FIELD_FINAL_TEXT:
            fieldData.clear();
            fieldData.stringValue = vfFinalText;
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
            case FIELD_DELETION_ALLOWED:
                destNode.setValue(destIndex, vfDeletionAllowed);
                break;

            case FIELD_ENTERED_TEXT:
                if(workingString != null)
                    vfEnteredText = workingString.toString();

                destNode.setValue(destIndex, vfEnteredText);
                break;

            case FIELD_FINAL_TEXT:
                destNode.setValue(destIndex, vfFinalText);
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
        case FIELD_DELETION_ALLOWED:
            vfDeletionAllowed = value;

            if(!inSetup) {
                hasChanged[FIELD_DELETION_ALLOWED] = true;
                fireFieldChanged(FIELD_DELETION_ALLOWED);
            }
            break;

        default:
            super.setValue(index, value);
        }
    }
}
