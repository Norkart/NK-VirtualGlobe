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

package org.web3d.vrml.renderer.common.nodes;

// External imports
// none

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLInterpolatorNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Abstract implementation of an interpolator so that specific instances can
 * derive from it.
 * <p>
 *
 * Interpolator nodes are designed for linear keyframed animation.
 * Interpolators are driven by an input key and produce
 * corresponding piecewise-linear output functions.
 * <p>
 *
 * As interpolators all have the same number and named fields, we perform all
 * of the setup here in this class. The handling of the basic fields are
 * performed where you see the overridden methods, however routing and sets
 * are not looked after.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public abstract class BaseInterpolatorNode extends AbstractNode
    implements VRMLInterpolatorNodeType {

    // The fields that we might need.

    /** The field index for key */
    protected static final int FIELD_KEY = LAST_NODE_INDEX + 1;

    /** The field index for keyValue */
    protected static final int FIELD_KEY_VALUE = LAST_NODE_INDEX + 2;

    /** The field index for fraction */
    protected static final int FIELD_FRACTION = LAST_NODE_INDEX + 3;

    /** The field index for value */
    protected static final int FIELD_VALUE = LAST_NODE_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_INTERPOLATOR_INDEX = FIELD_VALUE;

    /** The value of the fraction field */
    protected float vfFraction;

    /** The value of the key field */
    protected float[] vfKey;

    /** The number of valid values in vfKey */
    protected int numKey;

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     *
     * @param name The name of the type of node
     */
    public BaseInterpolatorNode(String name) {
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
    protected void copy(VRMLInterpolatorNodeType node) {
        float[] field = node.getKey();
        numKey = node.getNumKey();
        vfKey = new float[numKey];

        System.arraycopy(field, 0, vfKey, 0, numKey);
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLInterpolatorNodeType
    //-------------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.InterpolatorNodeType;
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
     * Get current value of key field value. If no keys exist a blank float[]
     * will be returned.
     *
     * @return The current key values
     */
    public float[] getKey() {
        return vfKey;
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
     * Set a new value for the key field. Null will delete all key values.
     *
     * @param keys The new key values to use
     * @param numValid The number of valid values to copy from the array
     */
    public void setKey(float[] keys, int numValid) {
        numKey = numValid;

        if(vfKey.length < numValid)
            vfKey = new float[numValid];

        if(numKey != 0)
            System.arraycopy(keys, 0, vfKey, 0, numValid);

        if(!inSetup) {
            hasChanged[FIELD_KEY] = true;
            fireFieldChanged(FIELD_KEY);
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
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

            case FIELD_FRACTION:
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
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field as an array of floats.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldFormatException, InvalidFieldException,
               InvalidFieldValueException {

        switch(index) {
            case FIELD_KEY:
                setKey(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
