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
import org.web3d.x3d.sai.MFColorRGBA;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.util.FieldValidator;

/**
 * Representation of an MFColorRGBA field.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the X3D IS specification Section 4.4.5 Standard units and
 * coordinate system
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class SAIMFColorRGBA extends BaseMField implements MFColorRGBA {

    /** Amount to increment the array by each time */
    private static final int ARRAY_INC = 32;

    /** Local array of data */
    private float[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAIMFColorRGBA(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by MFColorRGBA
    //----------------------------------------------------------

    /** Places a new value at the end of the existing value, increasing
     *  the field length accordingly.
     *
     * @param value The value to append
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *    value[3] = Alpha component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void append(float value[]) {
        checkAccess(true);

        if((localValue == null) || (localValue.length == numElements * 4)) {
            float[] tmp = new float[numElements * 4 + ARRAY_INC];
            System.arraycopy(localValue, 0, tmp, 0, numElements * 4);
            localValue = tmp;
        }

        localValue[numElements * 4] = value[0];
        localValue[numElements * 4 + 1] = value[1];
        localValue[numElements * 4 + 2] = value[2];
        localValue[numElements * 4 + 3] = value[3];

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
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *    value[3] = Alpha component [0-1] <BR>
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void insertValue(int index, float value[])
        throws ArrayIndexOutOfBoundsException {
        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(INSERT_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        if((localValue == null) || (localValue.length == numElements * 4)) {
            float[] tmp = new float[numElements * 4 + ARRAY_INC];
            System.arraycopy(localValue, 0, tmp, 0, numElements * 4);
            localValue = tmp;
        }

        System.arraycopy(localValue,
                         index * 4,
                         localValue,
                         index * 4 + 4,
                         (numElements - index) * 4);

        localValue[index * 4] = value[0];
        localValue[index * 4 + 1] = value[1];
        localValue[index * 4 + 2] = value[2];
        localValue[index * 4 + 3] = value[3];

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
                         index * 4,
                         localValue,
                         index * 4 - 4,
                         (numElements - index + 1) * 4);

        numElements--;
        dataChanged = true;
    }

    /**
     * Write the value of the event out to the given array.
     *
     * @param col The array to be filled in where<BR>
     *    col[i][0] = Red component [0-1] <BR>
     *    col[i][1] = Green component [0-1] <BR>
     *    col[i][2] = Blue component [0-1] <BR>
     *    col[i][3] = Alpha component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[][] col) {
        checkAccess(false);

        if(localValue != null) {
            int idx = 0;
            for(int i = 0; i < numElements; i++) {
                col[i][0] = localValue[idx++];
                col[i][1] = localValue[idx++];
                col[i][2] = localValue[idx++];
                col[i][3] = localValue[idx++];
            }
        }
    }

    /**
     * Get the values of the event out flattened into a single 1D array. The
     * array must be at least 3 times the size of the array.
     *
     * @param col The array to be filled in where the
     *    col[i + 0] = Red component [0-1] <BR>
     *    col[i + 1] = Green component [0-1] <BR>
     *    col[i + 2] = Blue component [0-1] <BR>
     *    col[i + 3] = Alpha component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] col) {
        checkAccess(false);

        if(localValue != null)
            System.arraycopy(localValue, 0, col, 0, numElements * 4);
    }

    /**
     * Get the value of a particular vector value in the event out array.
     *
     * @param index The position to get the vectory value from.
     * @param col The array to place the value in where.
     *    col[0] = Red component [0-1] <BR>
     *    col[1] = Green component [0-1] <BR>
     *    col[2] = Blue component [0-1] <BR>
     *    col[3] = Alpha component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small or
     *     the index was outside the current data array bounds.
     */
    public void get1Value(int index, float[] col) {
        checkAccess(false);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(GET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        col[0] = localValue[index * 4];
        col[1] = localValue[index * 4 + 1];
        col[2] = localValue[index * 4 + 2];
        col[3] = localValue[index * 4 + 3];
    }

    /**
     * Set the value of from the flat array of colours. Input is an array of
     * colour values in RGBA order [n, n+1, n+2, n+3]. All colour values are required
     * to be in the range 0-1. Colour values outside of this range will generate an
     * IllegalArgumentException. If the array does not contain at
     * least numColors * 4 values it will generate an ArrayIndexOutOfBoundsException.
     *
     * @param size The number of colour values in this array to copy
     * @param value The array of colour values where<BR>
     *    value[i] = Red component [0-1] <BR>
     *    value[i+1] = Green component [0-1] <BR>
     *    value[i+2] = Blue component [0-1] <BR>
     *    value[i+3] = Blue component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void setValue(int size, float[] value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length < size * 4))
            localValue = new float[size * 4];

        numElements = size;
        System.arraycopy(value, 0, localValue, 0, size * 4);
        dataChanged = true;
    }

    /**
     * Set the value of the array of colours. Input is an array of colour
     * values in RGBA order. All colour values are required to be in the
     * range 0-1. Colour values outside of this range will generate an
     * IllegalArgumentException. If value[i] that does not contain at
     * least three values will generate an ArrayIndexOutOfBoundsException.
     * If value[i] contains more than three items only the first three values
     * will be used and the rest ignored.
     *
     * @param size The number of colour values in this array to copy
     * @param value The array of colour values where<BR>
     *    value[i][0] = Red component [0-1] <BR>
     *    value[i][1] = Green component [0-1] <BR>
     *    value[i][2] = Blue component [0-1] <BR>
     *    value[i][3] = Alpha component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void setValue(int size, float[][] value) {
        checkAccess(false);

        if((localValue == null) || (localValue.length < size * 4))
            localValue = new float[size * 4];

        int idx = 0;
        for(int i = 0; i < size; i++) {
            localValue[idx++] = value[i][0];
            localValue[idx++] = value[i][1];
            localValue[idx++] = value[i][2];
            localValue[idx++] = value[i][3];
        }

        numElements = size;
        dataChanged = true;
    }

    /**
     * Set a particular colour value in the given eventIn array. To the VRML
     * world this will generate a full MFColor event with the nominated index
     * value changed. Colour values are required to be in the range [0-1].
     * <P>
     * The value array must contain at least three elements. If the array
     * contains more than 4 values only the first three values will be used and
     * the rest ignored.
     * <P>
     * If the index is out of the bounds of the current array of data values or
     * the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated. If the colour values are
     * out of range an IllegalArgumentException will be generated.
     *
     * @param index The position to set the colour value
     * @param value The array of colour values where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *    value[3] = Alpha component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least
     *    three values for the colour component
     */
    public void set1Value(int index, float[] value)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(SET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        FieldValidator.checkColorAlphaVector("SAIMFColor.set1Value", value);

        localValue[index * 4] = value[0];
        localValue[index * 4 + 1] = value[1];
        localValue[index * 4 + 2] = value[2];
        localValue[index * 4 + 3] = value[3];
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
        node.setValue(fieldIndex, localValue, numElements * 4);
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

        int req_size = data.numElements * 4;
        if((localValue == null) || (localValue.length < req_size))
            localValue = new float[req_size];

        numElements = data.numElements;
        if(numElements != 0)
            System.arraycopy(data.floatArrayValue, 0, localValue, 0, req_size);

        dataChanged = false;
    }
}
