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

package org.web3d.vrml.renderer.common.nodes.nurbs;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

import org.j3d.geom.spline.BSplineCurveData;
import org.j3d.geom.spline.BSplineUtils;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ArrayUtils;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLInterpolatorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseInterpolatorNode;

/**
 * Abstract implementation of a position interpolator using NURBS path
 * definition so that specific renderer instances can derive from it.
 * <p>
 *
 * If the key and keyValue fields are not the same length then we take
 * the lesser of the two and only assign that many vertices to the
 * interpolator to handle.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public abstract class BaseNurbsPositionInterpolator
    extends BaseInterpolatorNode {

    /** The field index for weight */
    protected static final int FIELD_WEIGHT = LAST_INTERPOLATOR_INDEX + 1;

    /** The field index for knot */
    protected static final int FIELD_KNOT = LAST_INTERPOLATOR_INDEX + 2;

    /** The field index for order */
    protected static final int FIELD_ORDER = LAST_INTERPOLATOR_INDEX + 3;

    /** Last valid index used by this field */
    protected static final int LAST_NURBSPOSITIONINTERPOLATOR_INDEX =
        FIELD_ORDER;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_NURBSPOSITIONINTERPOLATOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the keyValue field. These are the control points */
    private float[] vfKeyValue;

    /** Number of valid values in the keyValue array */
    private int numKeyValue;

    /** The value of the weight field */
    private double[] vfWeight;

    /** The value of the knot field */
    private double[] vfKnot;

    /** The value of the order field */
    private int vfOrder;

    /** The value of the value_changed eventOut field */
    private float[] vfValue;

    /** The interpolator we use to do the heavy work for us */
    private BSplineCurveData curveData;

    /** Used for retrieving the position from the interpolator */
    private float[] position;

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
                                     "MFVec3f",
                                     "keyValue");
        fieldDecl[FIELD_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFVec3f",
                                     "value_changed");
        fieldDecl[FIELD_WEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFDouble",
                                     "weight");
        fieldDecl[FIELD_KNOT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFDouble",
                                     "knot");
        fieldDecl[FIELD_ORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "order");

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

        idx = new Integer(FIELD_WEIGHT);
        fieldMap.put("weight", idx);
        fieldMap.put("set_weight", idx);
        fieldMap.put("weight_changed", idx);

        fieldMap.put("knot", new Integer(FIELD_KNOT));
        fieldMap.put("order", new Integer(FIELD_ORDER));
    }

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    protected BaseNurbsPositionInterpolator() {
        super("NurbsPositionInterpolator");

        hasChanged = new boolean[NUM_FIELDS];

        vfKeyValue = FieldConstants.EMPTY_MFVEC3F;
        vfValue = new float[3];
        vfOrder = 3;

        curveData = new BSplineCurveData();
        position = new float[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseNurbsPositionInterpolator(VRMLNodeType node) {
        this();
        checkNodeType(node);

        copy((VRMLInterpolatorNodeType)node);

        int idx;

        try {
            int index = node.getFieldIndex("keyValue");
            VRMLFieldData field = node.getFieldValue(index);
            numKeyValue = field.numElements;
            if(field.numElements != 0) {
                vfKeyValue = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfKeyValue,
                                 0,
                                 field.numElements);
                numKeyValue = field.numElements;
            }

            index = node.getFieldIndex("order");
            field = node.getFieldValue(index);
            vfOrder = field.intValue;

            index = node.getFieldIndex("weight");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfWeight = new double[field.numElements];
                System.arraycopy(field.doubleArrayValue,
                                 0,
                                 vfWeight,
                                 0,
                                 field.numElements);
            }


            index = node.getFieldIndex("knot");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfKnot = new double[field.numElements];
                System.arraycopy(field.doubleArrayValue,
                                 0,
                                 vfKnot,
                                 0,
                                 field.numElements);
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

        if(vfKey != null && vfKey.length > 0) {
            BSplineUtils.interpolatePoint(curveData, newFraction, position);
            setValue(position);
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
            setFraction(vfFraction);
            hasChanged[FIELD_KEY] = true;
            fireFieldChanged(FIELD_KEY);
        }
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
        if(index < 0  || index > LAST_NURBSPOSITIONINTERPOLATOR_INDEX)
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
            case FIELD_KEY_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfKeyValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numKeyValue / 3;
                break;

            case FIELD_VALUE:
                fieldData.clear();
                fieldData.floatArrayValue = vfValue;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_WEIGHT:
                fieldData.clear();
                fieldData.doubleArrayValue = vfWeight;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfWeight == null ? 0 : vfWeight.length;
                break;

            case FIELD_KNOT:
                fieldData.clear();
                fieldData.doubleArrayValue = vfKnot;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfKnot == null ? 0 : vfKnot.length;
                break;

            case FIELD_ORDER:
                fieldData.clear();
                fieldData.intValue = vfOrder;
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

                case FIELD_VALUE:
                    destNode.setValue(destIndex, vfValue, 3);
                    break;

                case FIELD_WEIGHT:
                    destNode.setValue(destIndex, vfWeight, vfWeight.length);
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
     * Set the value of the field at the given index as an int.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldAccessException("Cannot set knot field");

        switch(index) {
            case FIELD_ORDER:
                vfOrder = value;
                curveData.degree = value - 1;

                if(!inSetup) {
                    hasChanged[FIELD_ORDER] = true;
                    fireFieldChanged(FIELD_ORDER);
                }
                break;

            default:
                super.setValue(index, value);
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
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_KEY_VALUE:
                setKeyValue(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_WEIGHT:
                setWeight(value, numValid);
                break;

            case FIELD_KNOT:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set knot");

                setKnot(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------

    /**
     * Internal convenience method to update the knot values.
     *
     * @param knots The list of knot values to use
     * @param numValid The number of valid values to copy from the array
     */
    private void setKnot(double[] knots, int numValid) {

        // Always reallocate the array. We're going to assume that this
        // very rarely changes so optimise for this case.
        int num_knots = 0;
        if(numValid != 0) {
            if(vfKnot == null || numValid > vfKnot.length) {
                vfKnot = new double[numValid];
                curveData.knots = new float[numValid];
            }
            num_knots = numValid;
        } else {
            vfKnot = null;
        }

        curveData.numKnots = num_knots;

        if(num_knots != 0) {
            System.arraycopy(knots, 0, vfKnot, 0, num_knots);

            for(int i = 0; i < num_knots; i++)
                curveData.knots[i] = (float)knots[i];
        }

        if(!inSetup) {
            hasChanged[FIELD_KNOT] = true;
            fireFieldChanged(FIELD_KNOT);
        }
    }

    /**
     * Internal convenience method to update the weight values.
     *
     * @param weights The list of weight values to use
     * @param numValid The number of valid values to copy from the array
     */
    private void setWeight(double[] weights, int numValid) {

        // Always reallocate the array. We're going to assume that this
        // very rarely changes so optimise for this case.
        int num_weights = 0;
        if(numValid != 0) {
            if(vfWeight == null || numValid > vfWeight.length) {
                vfWeight = new double[numValid];
                curveData.weights = new float[numValid];
            }

            num_weights = numValid;
        } else {
            vfWeight = null;
        }

        curveData.numWeights = num_weights;

        if(num_weights != 0) {
            System.arraycopy(weights, 0, vfWeight, 0, num_weights);

            for(int i = 0; i < num_weights; i++)
                curveData.weights[i] = (float)weights[i];
        }

        if(!inSetup) {
            hasChanged[FIELD_WEIGHT] = true;
            fireFieldChanged(FIELD_WEIGHT);
        }
    }

    /**
     * Set the key value (MFVec3f) to the new value. If the value is null, it
     * will clear the currently set list and return it to an empty list.
     *
     * @param keyValues The new values to use
     * @param numValid The number of valid values to copy from the array
     */
    private void setKeyValue(float[] keyValues, int numValid) {
        numKeyValue = numValid;

        if(numKeyValue > vfKeyValue.length)
            vfKeyValue = new float[numKeyValue];

        if(numKeyValue > 0)
            System.arraycopy(keyValues, 0, vfKeyValue, 0, numValid);

        curveData.controlPoints = keyValues;
        curveData.numControlPoints = numValid;

        if(!inSetup) {
            setFraction(vfFraction);

            hasChanged[FIELD_KEY_VALUE] = true;
            fireFieldChanged(FIELD_KEY_VALUE);
        }
    }

    /**
     * Set the value (SFVec3f) to the new value. Null values will be ignored.
     *
     * @param newValue The new value to use
     */
    private void setValue(float[] value) {
        if(value != null) {
            vfValue[0] = value[0];
            vfValue[1] = value[1];
            vfValue[2] = value[2];

            hasChanged[FIELD_VALUE] = true;
            fireFieldChanged(FIELD_VALUE);
        }
    }
}
