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

package org.web3d.vrml.renderer.common.nodes.eventutils;

// External imports
import java.util.HashMap;

// Local imports
import org.j3d.util.interpolator.IntegerInterpolator;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLSequencerNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseSequencerNode;

/**
 * Abstract implementation of an integer sequencer so that specific
 * renderer instances can derive from it.
 * <p>
 *
 * SPEC: Given > 1 set_fractions on the same interval should it generate 1
 * or multiple value_changed events?  Currently does multiple.
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public abstract class BaseIntegerSequencer extends BaseSequencerNode {

    /** The last field index used by this class */
    protected static final int LAST_INTEGER_SEQUENCER_INDEX = LAST_SEQUENCER_INDEX;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_INTEGER_SEQUENCER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the keyValue field */
    private int[] vfKeyValue;

    /** The number of valid items in vfKeyValue */
    protected int numKeyValue;

    /** The value of the value field */
    private int vfValueChanged;

    /** The interpolator we use to do the heavy work for us */
    private IntegerInterpolator interpolator;

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
        fieldDecl[FIELD_NEXT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "next");
        fieldDecl[FIELD_PREVIOUS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "previous");
        fieldDecl[FIELD_SET_FRACTION] = new
            VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                 "SFFloat",
                                 "set_fraction");
        fieldDecl[FIELD_KEY] = new
            VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFFloat",
                                 "key");
        fieldDecl[FIELD_KEY_VALUE] = new
            VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFInt32",
                                 "keyValue");
        fieldDecl[FIELD_VALUE_CHANGED] = new
            VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                 "SFInt32",
                                 "value_changed");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("next", new Integer(FIELD_NEXT));
        fieldMap.put("previous", new Integer(FIELD_PREVIOUS));
        fieldMap.put("set_fraction", new Integer(FIELD_SET_FRACTION));

        idx = new Integer(FIELD_KEY);
        fieldMap.put("key", idx);
        fieldMap.put("set_key", idx);
        fieldMap.put("key_changed", idx);

        idx = new Integer(FIELD_KEY_VALUE);
        fieldMap.put("keyValue", idx);
        fieldMap.put("set_keyValue", idx);
        fieldMap.put("keyValueChanged", idx);

        fieldMap.put("value_changed", new Integer(FIELD_VALUE_CHANGED));
    }

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    protected BaseIntegerSequencer() {
        super("IntegerSequencer");

        hasChanged = new boolean[NUM_FIELDS];

        vfKeyValue = new int[0];
        vfValueChanged = -1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseIntegerSequencer(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSequencerNodeType)node);

        try {
            int index = node.getFieldIndex("keyValue");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfKeyValue = new int[field.numElements];
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfKeyValue,
                                 0,
                                 field.numElements);

                numKeyValue = field.numElements;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLSequencerNodeType
    //-------------------------------------------------------------

    /**
     * Set a new value for the fraction field. This will always evaluate
     * even if the fraction is the same as the old fraction.
     *
     * @param newFraction The new value for fraction
     */
    public void setFraction(float newFraction) {
        vfFraction = newFraction;

        if(vfKey != null && vfKey.length > 0) {
            if (interpolator != null && vfKeyValue.length > 0) {
                int value = interpolator.intValue(vfFraction);

                setValueChanged(value);
            }
        }
    }

    /**
     * Set a new value for the key field. Null will delete all key values.
     *
     * @param keys The new key values
     * @param numValid The number of valid values to copy from the array
     */
    public void setKey(float[] keys, int numValid) {
        super.setKey(keys, numValid);

        if(!inSetup) {
            rebuildInterpolator();
            // now force a re-interpolation given the current fraction
            setFraction(vfFraction);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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

        rebuildInterpolator();
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNode
    //-------------------------------------------------------------

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
        if(index < 0  || index > LAST_INTEGER_SEQUENCER_INDEX)
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
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_KEY_VALUE:
                fieldData.clear();
                fieldData.intArrayValue = vfKeyValue;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numKeyValue;
                break;

            case FIELD_VALUE_CHANGED:
                fieldData.clear();
                fieldData.intValue = vfValueChanged;
                fieldData.dataType = VRMLFieldData.INT_DATA;
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
                case FIELD_KEY_VALUE:
                    destNode.setValue(destIndex, vfKeyValue, numKeyValue);
                    break;

                case FIELD_VALUE_CHANGED:
                    destNode.setValue(destIndex, vfValueChanged);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseIntegerSequencer sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseIntegerSequencer sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field type for the field fraction
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SET_FRACTION:
                setFraction(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of ints.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_KEY_VALUE:
                setKeyValue(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //-------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------

    /**
     * Internal method to rebuild the interpolator from the latest lot of key
     * and value information. This is an expensive method as you must clear
     * the entire interpolator and start from scratch. Obviously we don't
     * expect people to be changing their key values every other frame.
     * <p>
     * If the key and keyValue fields are not the same length then we take
     * the lesser of the two and only assign that many vertices to the
     * interpolator to handle.
     */
    private void rebuildInterpolator() {
        if((vfKey == null) || (vfKeyValue == null)) {
            interpolator = null;
            return;
        }

        int size = (vfKey.length < vfKeyValue.length) ?
                   vfKey.length :
                   vfKeyValue.length;

        interpolator = new IntegerInterpolator(size, IntegerInterpolator.STEP);

        for(int i = 0; i < size; i++)
            interpolator.addKeyFrame(vfKey[i], vfKeyValue[i]);
    }

    /**
     * Set the key value (MFVec3f) to the new value. If the value is null, it
     * will clear the currently set list and return it to an empty list.
     *
     * @param newKeyValue The new values to use
     */
    private void setKeyValue(int[] newKeyValue, int numValid) {
        if(newKeyValue == null)
            vfKeyValue = FieldConstants.EMPTY_MFINT32;
        else {
            vfKeyValue = newKeyValue;
        }

        numKeyValue = numValid;

        if(!inSetup) {
            rebuildInterpolator();
            // now force a re-interpolation given the current fraction
            setFraction(vfFraction);

            hasChanged[FIELD_KEY_VALUE] = true;
            fireFieldChanged(FIELD_KEY_VALUE);
        }
    }

    /**
     * Set the value_changed to the new value. Null values will be ignored.
     *
     * @param newValue The new value to use
     */
    private void setValueChanged(int value) {
        vfValueChanged = value;

        if(!inSetup) {
            hasChanged[FIELD_VALUE_CHANGED] = true;
            fireFieldChanged(FIELD_VALUE_CHANGED);
        }
    }
}
