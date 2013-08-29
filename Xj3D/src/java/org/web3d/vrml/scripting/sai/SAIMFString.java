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
import org.web3d.x3d.sai.MFString;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a MFString field.
 * <P>
 * Strings are represented using standard java.lang.String representations.
 * The implementation of this class will provide any necessary conversions
 * to the UTF8 format required for VRML support.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class SAIMFString extends BaseMField implements MFString {

    /** Amount to increment the array by each time */
    private static final int ARRAY_INC = 8;

    /** Local array of data */
    private String[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAIMFString(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by MFString
    //----------------------------------------------------------

    /** Places a new value at the end of the existing value, increasing
     *  the field length accordingly.
     *
     * @param value The value to append
     */
    public void append(String value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length == numElements)) {
            String[] tmp = new String[numElements + ARRAY_INC];
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
    public void insertValue(int index, String value)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(INSERT_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        if((localValue == null) || (localValue.length == numElements)) {
            String[] tmp = new String[numElements + ARRAY_INC];
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
     * Write the value of the array of the strings to the given array. Individual
     * elements in the string array may be null depending on the implementation
     * of the browser and whether it maintains null references.
     *
     * @param strs The string array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(String[] values) {
        checkAccess(false);

        if(localValue != null)
            System.arraycopy(localValue, 0, values, 0, numElements);
    }

    /**
     * Get a particular string value in the given eventOut array.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the array reference
     * was null when set, an empty string will be returned to the caller.
     *
     * @param index The position to get the string value from
     * @return The string value
     *
     * @exception ArrayIndexOutOfBoundsException The index value was out of bounds of
     *     the current array.
     */
    public String get1Value(int index) {

        checkAccess(false);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(GET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        return localValue[index];
    }

    /**
     * Set the value of the array of strings.  If value[i] contains a null
     * referenc this will not cause an exception to be generated. However,
     * the resulting event that the eventIn receives will be implementation
     * specific as this is not dealt with in the VRML specification.
     *
     * @param size The number of items to be copied from this array
     * @param value The array of strings.
     */
    public void setValue(int size, String[] value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length < size))
            localValue = new String[size];

        numElements = size;
        System.arraycopy(value, 0, localValue, 0, size);
        dataChanged = true;
    }

    /**
     * Set a particular string value in the given eventIn array. To the VRML
     * world this will generate a full MFString event with the nominated index
     * value changed.
     * <P>
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated. If the value reference
     * is null then the result is implementation specific in terms of the array
     * reference that reaches the eventIn. In any case, an event will reach the
     * destination eventIn, but the values in that array are implementation
     * specific. No exception will be generated in this case.
     *
     * @param index The position to set the string value
     * @param value The string value
     *
     * @exception ArrayIndexOutOfBoundsException The index value was out of bounds of
     *     the current array.
     */
    public void set1Value(int index, String value)
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
            localValue = new String[req_size];

        numElements = data.numElements;
        if(numElements != 0)
            System.arraycopy(data.stringArrayValue, 0, localValue, 0, req_size);

        dataChanged = false;
    }
}
