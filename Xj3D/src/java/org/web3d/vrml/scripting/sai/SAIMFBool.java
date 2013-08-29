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
import org.web3d.x3d.sai.MFBool;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Interface representing a MFBool field.
 * <P>
 *
 * @version 1.0 30 April 1998
 */
class SAIMFBool extends BaseMField implements MFBool {

    /** Amount to increment the array by each time */
    private static final int ARRAY_INC = 8;

    /** Local array of data */
    private boolean[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAIMFBool(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by MFBool
    //----------------------------------------------------------

    /** Places a new value at the end of the existing value, increasing
     *  the field length accordingly.
     *
     * @param value The value to append
     */
    public void append(boolean value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length == numElements)) {
            boolean[] tmp = new boolean[numElements + ARRAY_INC];
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
     * <p>
     * If the index is out of the bounds of the current field an
     * ArrayIndexOutofBoundsException will be generated.
     *
     * @param index The position at which to insert
     * @param value The new element to insert
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     */
    public void insertValue(int index, boolean value)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(INSERT_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        if((localValue == null) || (localValue.length == numElements)) {
            boolean[] tmp = new boolean[numElements + ARRAY_INC];
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
     * Write the value of the field out to the given array.
     *
     * @param vals The array to be filled in
     * @throws ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(boolean[] vals) {
        checkAccess(false);

        if(localValue != null)
            System.arraycopy(localValue, 0, vals, 0, numElements);
    }

    /**
     * Get a particular boolean value in this field.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to get the time value
     * @return The time value.
     * @throws ArrayIndexOutOfBoundsException The index was outside of the bounds of
     * the current array.
     */
    public boolean get1Value(int index) {
        checkAccess(false);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(GET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        return localValue[index];
    }

    /**
     * Set the value of the field to the new array of boolean values. This array
     * is copied internally so that the parameter array can be reused without
     * effecting the valid values of the eventIn.
     *
     * @param size The number of items to copy from the array
     * @param value The array of values to be used.
     */
    public void setValue(int size, boolean[] value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length < size))
            localValue = new boolean[size];

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
     * @param index The position to set the boolean value
     * @param value The value to be set
     *
     * @exception ArrayIndexOutOfBoundsException The index was invalid
     */
    public void set1Value(int index, boolean value)
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
            localValue = new boolean[data.numElements];

        numElements = data.numElements;
        if(numElements != 0)
            System.arraycopy(data.booleanArrayValue, 0, localValue, 0, numElements);

        dataChanged = false;
    }
}
