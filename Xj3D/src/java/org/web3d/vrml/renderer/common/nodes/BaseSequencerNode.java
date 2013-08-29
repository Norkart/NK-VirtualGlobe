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

package org.web3d.vrml.renderer.common.nodes;

// External imports
// None

// Local imports
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLSequencerNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Abstract implementation of a sequencer so that specific instances can
 * derive from it.
 * <p>
 *
 * Sequencer nodes are designed for discrete animation.
 * Sequencers are driven by an input and produce
 * corresponding impulse output functions.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public abstract class BaseSequencerNode extends AbstractNode
    implements VRMLSequencerNodeType {

    // The fields that we might need.

    /** The field index for next */
    protected static final int FIELD_NEXT = LAST_NODE_INDEX + 1;

    /** The field index for previous */
    protected static final int FIELD_PREVIOUS = LAST_NODE_INDEX + 2;

    /** The field index for set_fraction */
    protected static final int FIELD_SET_FRACTION = LAST_NODE_INDEX + 3;

    /** The field index for key */
    protected static final int FIELD_KEY = LAST_NODE_INDEX + 4;

    /** The field index for keyValue */
    protected static final int FIELD_KEY_VALUE = LAST_NODE_INDEX + 5;

    /** The field index for the value_changed field */
    protected static final int FIELD_VALUE_CHANGED = LAST_NODE_INDEX + 6;

    /** The last field index used by this class */
    protected static final int LAST_SEQUENCER_INDEX = FIELD_VALUE_CHANGED;

    /** The value of the fraction field */
    protected float vfFraction;

    /** The value of the key field */
    protected float[] vfKey;

    /** The number of valid items in vfKey */
    protected int numKey;

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     *
     * @param name The name of the type of node
     */
    public BaseSequencerNode(String name) {
        super(name);

        vfFraction = 0;
        vfKey = FieldConstants.EMPTY_MFFLOAT;
    }

    /**
     * Set the fields of the interpolator node that has the fields set
     * based on the fields of the passed in node.
     *
     * @param node The interpolator node to copy info from
     */
    protected void copy(VRMLSequencerNodeType node) {
        float[] field = node.getKey();

        vfKey = new float[field.length];
        System.arraycopy(field, 0, vfKey, 0, field.length);
        numKey = field.length;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLSequencerNodeType
    //-------------------------------------------------------------

    /**
     * Cause the next value to be generated on the output. This is equivalent
     * to sending a value to the next inputOnly field.
     */
    public void setNext() {
        int key = findKey() + 1;

        if(key > numKey)
            key = 0;

        setFraction(vfKey[key]);
    }

    /**
     * Cause the previous value to be generated on the output. This is equivalent
     * to sending a value to the previous inputOnly field.
     */
    public void setPrevious() {
        int key = findKey() - 1;

        if(key < 0)
            key = numKey - 1;

        setFraction(vfKey[key]);
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.SequencerNodeType;
    }

    /**
     * Get the value of the fraction field.
     *
     * @return The current value for fraction
     */
    public float getFraction() {
        return vfFraction;
    }

    /**
     * Set a new value for the key field. Null will delete all key values.
     *
     * @param keys The new key values to use
     * @param numValid The number of valid values to copy from the array
     */
    public void setKey(float[] keys, int numValid) {

        if(vfKey.length < numValid)
            vfKey = new float[numValid];

        System.arraycopy(keys, 0, vfKey, 0, numValid);
        numKey = numValid;

        if(!inSetup) {
            hasChanged[FIELD_KEY] = true;
            fireFieldChanged(FIELD_KEY);
        }
    }

    /**
     * Get the number of valid keys defined for this interpolator.
     *
     * @return a value >= 0
     */
    public int getNumKey() {
        return numKey;
    }

    /**
     * Get current value of key field value. If no keys exist a blank float[]
     * will be returned.
     *
     * @return The current key values
     */
    public float[] getKey() {
        return vfKey;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType interface.
    //-------------------------------------------------------------

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
            case FIELD_KEY:
                fieldData.clear();
                fieldData.floatArrayValue = vfKey;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numKey;
                break;

            case FIELD_SET_FRACTION:
                fieldData.clear();
                fieldData.floatValue = vfFraction;
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
                case FIELD_KEY:
                    destNode.setValue(destIndex, vfKey, numKey);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseBooleanSequencer sendRoute: No field!" + srcIndex);
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseBooleanSequencer sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This would
     * be used to set SFBool field type for the field fraction
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_NEXT:
                setNext();
                break;

            case FIELD_PREVIOUS:
                setPrevious();
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFFloat and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_KEY:
                setKey(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //-------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------

    /**
     * Find the current active key value from the fraction. This will find
     * the key that is just less than or equal to the current key value, and
     * return its index into vfKey.
     *
     * @return The index of the key that the current fraction is using
     */
    private int findKey() {
        // simple brute-force method for now.
        int ret_val = 0;

        for(int i = 0; i < numKey; i++) {
            if(vfFraction > vfKey[i]) {
                ret_val = i - 1;
                break;
            }
        }

        // correct for when we run off the end of the array.
        if((ret_val == 0) && (vfFraction > vfKey[numKey - 1]))
            ret_val = numKey - 1;

        return ret_val;
    }
}
