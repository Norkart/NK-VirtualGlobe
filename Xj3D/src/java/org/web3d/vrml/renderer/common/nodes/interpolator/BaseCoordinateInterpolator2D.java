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

package org.web3d.vrml.renderer.common.nodes.interpolator;

// Standard imports
import java.util.HashMap;

import org.j3d.util.interpolator.Coordinate2DInterpolator;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLInterpolatorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseInterpolatorNode;

/**
 * Abstract implementation of a coordinate interpolator so that specific
 * renderer instances can derive from it.
 * <p>
 *
 * Interpolator nodes are designed for linear keyframed animation.
 * Interpolators are driven by an input key ranging [0..1] and produce
 * corresponding piecewise-linear output functions.
 * <p>
 *
 * If the key and keyValue fields are not the same length then we take
 * the lesser of the two and only assign that many vertices to the
 * interpolator to handle.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public abstract class BaseCoordinateInterpolator2D extends BaseInterpolatorNode {

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_INTERPOLATOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    // Internally the arrays are kept as max of whatever was sent to us in
    // a setValue.  So a length variable is needed to know the "real" length
    // This will speed setValue calls.  It will cost on sendRoute. We don't
    // expect to have to route out values that often, except for the value
    // output, so we make sure it is the right length always.

    /** The value of the keyValue field */
    private float[] vfKeyValue;

    /** The used length of the keyValue array */
    private int numKeyValue;

    /** The value of the value field */
    private float[] vfValue;

    /** The interpolator we use to do the heavy work for us */
    private Coordinate2DInterpolator interpolator;

    /** A temporary array that we use to setup the interpolator */
    private float[] coordKeySetup;

    /** The number of coordinates per key value in the vfKeyValue array */
    private int coordValuesPerKey;

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
        fieldDecl[FIELD_FRACTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_fraction");

        fieldDecl[FIELD_KEY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "key");

        fieldDecl[FIELD_KEY_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFVec2f",
                                     "keyValue");

        fieldDecl[FIELD_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFVec2f",
                                     "value_changed");

        fieldMap.put("set_fraction", new Integer(FIELD_FRACTION));

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_KEY);
        fieldMap.put("key", idx);
        fieldMap.put("set_key", idx);
        fieldMap.put("key_changed", idx);

        idx = new Integer(FIELD_KEY_VALUE);
        fieldMap.put("keyValue", idx);
        fieldMap.put("set_keyValue", idx);
        fieldMap.put("keyValue_changed", idx);

        fieldMap.put("value_changed", new Integer(FIELD_VALUE));
    }

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    protected BaseCoordinateInterpolator2D() {
        super("CoordinateInterpolator2D");

        hasChanged = new boolean[NUM_FIELDS];

        // we don't assign vfValue or vfKeyValue until setupFinished
        // so that we know how many keys we have.
        numKeyValue = 0;
        coordValuesPerKey = 0;
        vfKeyValue = FieldConstants.EMPTY_MFVEC2F;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not an interpolator node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseCoordinateInterpolator2D(VRMLNodeType node) {
        this();

        checkNodeType(node);
        copy((VRMLInterpolatorNodeType)node);

        try {
            int index = node.getFieldIndex("keyValue");
            VRMLFieldData field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfKeyValue = new float[field.numElements * 2];
                System.arraycopy(field.floatArrayValue, 0, vfKeyValue, 0,
                    field.numElements * 2);

                numKeyValue = field.numElements * 2;
                coordValuesPerKey = numKeyValue / vfKey.length;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLInterpolatorNodeType interface.
    //-------------------------------------------------------------

    /**
     * Set a new value for the fraction field. This will always evaluate
     * even if the fraction is the same as the old fraction.
     *
     * @param newFraction The new value for fraction
     */
    public void setFraction(float newFraction) {
        vfFraction = newFraction;

        // Only interpolate if the key field is non-null
        if(vfKey != null && vfKey.length > 0) {
            if (interpolator != null && vfKeyValue.length > 0) {
                float[] value = interpolator.floatValue(vfFraction);
                setValue(value, value.length);
            }
        }

        if (!inSetup) {
            hasChanged[FIELD_FRACTION] = true;
            fireFieldChanged(FIELD_FRACTION);
        }
    }

    /**
     * Set a new value for the key field. Null will delete all key values.
     *
     * @param keys The new key values to use
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
    // Methods required by the BaseVRMLNodeTypeType interface.
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

        // Avoid issuing an event
        inSetup = true;
        setFraction(vfFraction);
        inSetup = false;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        if(index < 0  || index > LAST_INTERPOLATOR_INDEX)
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

    //-------------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_KEY_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfKeyValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = coordValuesPerKey == 0 ? 0 : numKeyValue / coordValuesPerKey;
                break;

            case FIELD_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = coordValuesPerKey / 3;
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

                case FIELD_VALUE:
                    destNode.setValue(destIndex, vfValue, coordValuesPerKey);
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
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field type for the field fraction
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        if(index != FIELD_FRACTION) {
            super.setValue(index, value);
            return;
        }

        setFraction(value);
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        if(index != FIELD_KEY_VALUE) {
            super.setValue(index, value, numValid);
            return;
        }

        setKeyValue(value, numValid);
    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------


    /**
     * Set the key value (MFVec3f) to the new value. If the value is null, it
     * will clear the currently set list and return it to an empty list.
     *
     * @param newKeyValue The new values to use
     */
    private void setKeyValue(float[] newKeyValue, int numValid) {

        if(numValid == 0) {
            numKeyValue = 0;
        } else {
            if(numValid > vfKeyValue.length)
                vfKeyValue = new float[numValid];

            numKeyValue = numValid;
            coordValuesPerKey = numKeyValue / numKey;
            System.arraycopy(newKeyValue, 0, vfKeyValue, 0, numKeyValue);
        }

        if(!inSetup) {
            rebuildInterpolator();

            // now force a re-interpolation given the current fraction
            setFraction(vfFraction);
            hasChanged[FIELD_KEY_VALUE] = true;
            fireFieldChanged(FIELD_KEY_VALUE);
        }
    }

    /**
     * Set the value (MFVec3f) to the new value. Null values will be ignored.
     * The array length will always be set to the length needed for the
     * list of coordinates generated from the interpolator. If the incoming
     * value is greater than needed, it will be trimmed, if it is less than
     * is needed, the existing remaining values are untouched.
     *
     * @param newValue The new value to use
     */
    private void setValue(float[] value, int numValid) {

        if(numValid == 0)
            return;

        if((vfValue == null) || (numValid > vfValue.length))
            vfValue = new float[numValid];

        System.arraycopy(value, 0, vfValue, 0, coordValuesPerKey);

        if (!inSetup) {
            hasChanged[FIELD_VALUE] = true;
            fireFieldChanged(FIELD_VALUE);
        }
    }

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
        if((numKey == 0) || (numKeyValue == 0)) {
            interpolator = null;
            return;
        }

        int size = (numKey < numKeyValue / 2) ? numKey : numKeyValue / 2;

        interpolator = new Coordinate2DInterpolator(size);

        int count = 0;

        if((coordKeySetup == null) ||
           (coordKeySetup.length != coordValuesPerKey)) {
            coordKeySetup = new float[coordValuesPerKey];
        }

        for(int i = 0; i < size; i++) {
            System.arraycopy(vfKeyValue,
                             count,
                             coordKeySetup,
                             0,
                             coordValuesPerKey);

            interpolator.addKeyFrame(vfKey[i], coordKeySetup);

            count += coordValuesPerKey;
        }
    }
}
