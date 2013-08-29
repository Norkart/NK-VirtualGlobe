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
import org.web3d.x3d.sai.MFVec2f;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a MFVec2f field.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class SAIMFVec2f extends BaseMField implements MFVec2f {

    /** Amount to increment the array by each time */
    private static final int ARRAY_INC = 16;

    /** Local array of data */
    private float[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAIMFVec2f(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by MFVec3f
    //----------------------------------------------------------

    /** Places a new value at the end of the existing value, increasing
     *  the field length accordingly.
     *
     * @param value The value to append
     *    value[0] = X<BR>
     *    value[1] = Y
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the vector definition.
     */
    public void append(float value[]) {
        checkAccess(true);

        if((localValue == null) || (localValue.length == numElements * 2)) {
            float[] tmp = new float[numElements * 2 + ARRAY_INC];
            System.arraycopy(localValue, 0, tmp, 0, numElements * 2);
            localValue = tmp;
        }

        localValue[numElements * 2] = value[0];
        localValue[numElements * 2 + 1] = value[1];

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
     *    value[0] = X<BR>
     *    value[1] = Y
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current field
     *    size.
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the vector definition.
     */
    public void insertValue(int index, float value[])
        throws ArrayIndexOutOfBoundsException {
        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(INSERT_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        if((localValue == null) || (localValue.length == numElements * 2)) {
            float[] tmp = new float[numElements * 2 + ARRAY_INC];
            System.arraycopy(localValue, 0, tmp, 0, numElements * 2);
            localValue = tmp;
        }

        System.arraycopy(localValue,
                         index * 2,
                         localValue,
                         index * 2 + 2,
                         (numElements - index) * 2);

        localValue[index * 2] = value[0];
        localValue[index * 2 + 1] = value[1];

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
                         index * 2,
                         localValue,
                         index * 2 - 2,
                         (numElements - index + 1) * 2);

        numElements--;
        dataChanged = true;
    }

    /**
     * Write the value of the event out to the given array.
     *
     * @param vec The array to be filled in where<BR>
     *    vec[i][0] = X<BR>
     *    vec[i][1] = Y
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[][] vec) {
        checkAccess(false);

        if(localValue != null) {
            int idx = 0;
            for(int i = 0; i < numElements; i++) {
                vec[i][0] = localValue[idx++];
                vec[i][1] = localValue[idx++];
            }
        }
    }

    /**
     * Get the values of the event out flattened into a single 1D array. The
     * array must be at least 3 times the size of the array.
     *
     * @param vec The array to be filled in where the
     *   vec[i + 0] = X<BR>
     *   vec[i + 1] = Y
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] vec) {
        checkAccess(false);

        if(localValue != null)
            System.arraycopy(localValue, 0, vec, 0, numElements * 2);
    }

    /**
     * Get the value of a particular vector value in the event out array.
     *
     * @param index The position to get the vectory value from.
     * @param vec The array to place the value in where.
     *    vec[0] = X<BR>
     *    vec[1] = Y
     * @exception ArrayIndexOutOfBoundsException The provided array was too small or
     *     the index was outside the current data array bounds.
     */
    public void get1Value(int index, float[] vec) {
        checkAccess(false);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(GET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        vec[0] = localValue[index * 2];
        vec[1] = localValue[index * 2 + 1];
    }


    /**
     * Set the value of the array of 2D vectors. Input is an array of floats
     * If value[i] does not contain at least two values it will generate an
     * ArrayIndexOutOfBoundsException. If value[i] contains more than two items
     * only the first two values will be used and the rest ignored.
     * <P>
     * If one or more of the values for value[i] are null then the resulting
     * event that is sent to the VRML scenegraph is implementation dependent but
     * no error indicator will be set here.
     *
     * @param size The number of items to be copied from the array
     * @param value The array of vec2f values where<BR>
     *    value[i][0] = X<BR>
     *    value[i][1] = Y
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least two
     *    values for the vector definition.
     */
    public void setValue(int size, float[][] value) {
        checkAccess(false);

        if((localValue == null) || (localValue.length < size * 2))
            localValue = new float[size * 2];

        int idx = 0;
        for(int i = 0; i < size; i++) {
            localValue[idx++] = value[i][0];
            localValue[idx++] = value[i][1];
        }

        numElements = size;
        dataChanged = true;
    }

    /**
     * Set the value of the array of 2D vectors. Input is an array of floats
     * If value[i] does not contain at least two values it will generate an
     * ArrayIndexOutOfBoundsException. If value[i] contains more than two items
     * only the first two values will be used and the rest ignored.
     * <P>
     * If one or more of the values for value[i] are null then the resulting
     * event that is sent to the VRML scenegraph is implementation dependent but
     * no error indicator will be set here.
     *
     * @param size The number of items to be copied from the array
     * @param value The array of vec2f values where<BR>
     *    value[i] = X<BR>
     *    value[i+1] = Y
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least two
     *    values for the vector definition.
     */
    public void setValue(int size, float[] value) {
        checkAccess(true);

        if((localValue == null) || (localValue.length < size * 2))
            localValue = new float[size * 2];

        numElements = size;
        System.arraycopy(value, 0, localValue, 0, size * 2);
        dataChanged = true;
    }

    /**
     * Set a particular vector value in the given eventIn array. To the VRML
     * world this will generate a full MFVec2f event with the nominated index
     * value changed.
     * <P>
     * The value array must contain at least two elements. If the array
     * contains more than 2 values only the first 2 values will be used and
     * the rest ignored.
     * <P>
     * If the index is out of the bounds of the current array of data values or
     * the array of values does not contain at least 2 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the vector value
     * @param value The array of vector values where<BR>
     *    value[0] = X<BR>
     *    value[1] = Y
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least 2
     *    values for the vector
     */
    public void set1Value(int index, float[] value)
        throws ArrayIndexOutOfBoundsException {

        checkAccess(true);

        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException(SET_OOB_ERR);

        if(index < 0)
            throw new ArrayIndexOutOfBoundsException(NEGATIVE_INDEX_ERR);

        localValue[index * 2] = value[0];
        localValue[index * 2 + 1] = value[1];
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
        node.setValue(fieldIndex, localValue, numElements * 2);
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

        int req_size = data.numElements * 2;
        if((localValue == null) || (localValue.length < req_size))
            localValue = new float[req_size];

        numElements = data.numElements;
        if(numElements != 0)
            System.arraycopy(data.floatArrayValue, 0, localValue, 0, req_size);

        dataChanged = false;
    }
}
