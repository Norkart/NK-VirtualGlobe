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

package org.web3d.vrml.scripting.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.MFFloat;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a MFFloat field.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class SAIMFFloat extends BaseMField implements MFFloat {

    /** Amount to increment the array by each time */
    private static final int ARRAY_INC = 8;

    /** Local array of data */
    private float[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAIMFFloat(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by MFFloat
    //----------------------------------------------------------

    /**
     * Places a new value at the end of the existing value, increasing
     * the field length accordingly.
     *
     * @param value The value to append
     */
    public void append(float value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length == numElements)) {
            float[] tmp = new float[numElements + ARRAY_INC];
            System.arraycopy(localValue, 0, tmp, 0, numElements);
            localValue = tmp;
        }

        localValue[numElements] = value;

        numElements++;
        dataChanged = true;
    }

    /**
     * Inserts a value into an existing index of the field.  Current field values
     * from the index to the end of the field are shifted down and the field
     * length is increased by one to accomodate the new element.
     *
     * If the index is out of the bounds of the current field an
     * ArrayIndexOutofBoundsException will be generated.
     *
     * @param index The position at which to insert
     * @param value The new element to insert
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    public void insertValue(int index, float value)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(INSERT_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        if((localValue == null) || (localValue.length == numElements)) {
            float[] tmp = new float[numElements + ARRAY_INC];
            System.arraycopy(localValue, 0, tmp, 0, numElements);
            localValue = tmp;
        }

        System.arraycopy(localValue,
                         index,
                         localValue,
                         index + 1,
                         numElements - index);

        localValue[index] = value;
        numElements++;

        dataChanged = true;
    }

    /**
     * Removes one value from the field.  Values at indices above the
     * removed element will be shifted down by one and the size of the
     * field will be reduced by one.
     *
     * @param index The position of the value to remove.
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    public void removeValue(int index)
        throws ArrayIndexOutOfBoundsException {
        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(REMOVE_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        System.arraycopy(localValue,
                         index,
                         localValue,
                         index - 1,
                         numElements - index + 1);

        numElements--;
        dataChanged = true;
    }

    /**
     * Write the value of the array of the floats to the given array.
     *
     * @param values The array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] values) {
        checkAccess(false);

        if(localValue != null)
            System.arraycopy(localValue, 0, values, 0, numElements);
    }

    /**
     * Get the value of an individual item in the eventOut's value.
     *
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to be retrieved
     * @return The value to at that position
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    public float get1Value(int index)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(false);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(GET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        return localValue[index];
    }

    /**
     * Set the value of the eventIn to the new array of float values. This array
     * is copied internally so that the parameter array can be reused without
     * effecting the valid values of the eventIn.
     *
     * @param size The number of items to copy from the array
     * @param value The array of values to be used.
     */
    public void setValue(int size, float[] value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length < size))
            localValue = new float[size];

        numElements = size;
        System.arraycopy(value, 0, localValue, 0, size);
        dataChanged = true;
    }

    /**
     * Set the value of an individual item in the eventIn's value. This results in
     * a new event being generated that includes all of the array items with the
     * single element set.
     *
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the float value
     * @param value The value to be set
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least
     *    three values for the colour component
     */
    public void set1Value(int index, float value)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(SET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        localValue[index] = value;
        dataChanged = true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Notification to the field instance to update the value in the
     * underlying node now.
     */
    void updateNode() {
        node.setValue(fieldIndex, localValue, numElements);
        dataChanged = false;
    }

    /**
     * Notification to the field to update its field values from the
     * underlying node.
     */
    void updateField() {
        if(!isReadable())
            return;

        VRMLFieldData data = node.getFieldValue(fieldIndex);

        if((localValue == null) || (localValue.length < data.numElements))
            localValue = new float[data.numElements];

        numElements = data.numElements;
        if(numElements != 0)
            System.arraycopy(data.floatArrayValue, 0, localValue, 0, numElements);

        dataChanged = false;
    }
}
