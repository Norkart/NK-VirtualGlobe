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
import org.web3d.x3d.sai.MFInt32;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a MFInt32 field.
 *
 * @version 1.0 30 April 1998
 */
class SAIMFInt32 extends BaseMField implements MFInt32 {

    /** Amount to increment the array by each time */
    private static final int ARRAY_INC = 8;

    /** Local array of data */
    private int[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAIMFInt32(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by MFInt32
    //----------------------------------------------------------

    /** Places a new value at the end of the existing value, increasing
     *  the field length accordingly.
     *
     * @param value The value to append
     */
    public void append(int value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length == numElements)) {
            int[] tmp = new int[numElements + ARRAY_INC];
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
    public void insertValue(int index, int value)
        throws ArrayIndexOutOfBoundsException {
        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(INSERT_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        if((localValue == null) || (localValue.length == numElements)) {
            int[] tmp = new int[numElements + ARRAY_INC];
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
     * @throws ArrayIndexOutOfBoundsException The index was outside the current field
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
     * Write the value of the array of the ints to the given array.
     *
     * @param values The array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(int[] values) {
        checkAccess(false);

        if(localValue != null)
            System.arraycopy(localValue, 0, values, 0, numElements);
    }

    /**
     * Get a particular value from the eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to be retrieved
     * @return The value at that position
     * @throws ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    public int get1Value(int index)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(false);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(GET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        return localValue[index];
    }

    /**
     * Set the value of the array of integers. If the value array is length zero
     * this is equivalent of clearing the field.
     *
     * @param size The number of items to copy from this array
     * @param value The array of values to be set
     */
    public void setValue(int size, int[] value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length < size))
            localValue = new int[size];

        numElements = size;
        System.arraycopy(value, 0, localValue, 0, size);
        dataChanged = true;
    }

    /**
     * Set a particular value in the given eventIn array. To the VRML
     * world this will generate a full MFInt32 event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The value to be set
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void set1Value(int index, int value)
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

        int req_size = data.numElements;
        if((localValue == null) || (localValue.length < req_size))
            localValue = new int[req_size];

        numElements = data.numElements;

        if(numElements != 0)
            System.arraycopy(data.intArrayValue, 0, localValue, 0, req_size);

        dataChanged = false;
    }
}
