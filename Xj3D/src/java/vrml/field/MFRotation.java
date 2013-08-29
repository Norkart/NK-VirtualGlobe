/*****************************************************************************
 *                        Web4d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package vrml.field;

// Standard imports
// none

// Application specific imports
import vrml.MField;

import org.web3d.util.ArrayUtils;

/**
 * VRML JSAI type class containing multiple rotation fields.
 * <p>
 *
 * Internally, the class stores the values as a single, flat array so that is
 * the most efficient method to use to avoid reallocation. All methods make
 * internal copies of the values.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.6 $
 */
public class MFRotation extends MField {
    /**
     * How much should we increment the array by when there is an add or
     * insert request. This value is array items, not elements.
     */
    protected static final int ARRAY_INC = 20;

    /** The rot values */
    protected float[] data;

    /** working array to use to extract rotations */
    private float[] tmpField = new float[4];

    /**
     * Construct a new empty rotation.
     */
    public MFRotation() {
        data = new float[ARRAY_INC];
        numElements = 0;
    }

    /**
     * Construct a new rotation array based on all the given values.
     *
     * @param rots The source rotation values
     */
    public MFRotation(float rots[][]) {
        data = new float[rots.length * 4];

        ArrayUtils.flatten2(rots, rots.length, data);

        numElements = rots.length;
    }

    /**
     * Construct a new rotation array based on all the given values.
     *
     * @param rots The source rotation values
     */
    public MFRotation(float rots[]) {

        if(rots.length < 4)
            throw new IllegalArgumentException("Array length less than 4");

        // Adjust the length using int division to make sure we have a
        // multiple of 4 to work with.
        numElements = rots.length / 4;

        int length = numElements * 4;

        data = new float[length];

        System.arraycopy(rots, 0, data, 0, length);
    }

    /**
     * Construct a new rotation array based on a fraction of the items supplied.
     *
     * @param size The number of rotations is size / 4.
     * @param rots The source rotation values
     */
    public MFRotation(int size, float rots[]) {
        if(rots.length < 4)
            throw new IllegalArgumentException("Array length less than 4");

        numElements = size / 4;

        data = new float[rots.length];

        System.arraycopy(rots, 0, data, 0, rots.length);

    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param rots The target array to copy values into
     */
    public void getValue(float rots[][]) {
        ArrayUtils.raise4(data, numElements, rots);
    }

    /**
     * Copy the value of this field into the given flat array.
     *
     * @param rots The target array to copy values into
     */
    public void getValue(float rots[]) {
        System.arraycopy(data, 0, rots, 0, numElements * 4);
    }

    /**
     * Copy the value of the rotation at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param rots The array to copy the rotation value to
     */
    public void get1Value(int index, float rots[]) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;
        rots[0] = data[offset++];
        rots[1] = data[offset++];
        rots[2] = data[offset++];
        rots[3] = data[offset];
    }

    /**
     * Copy the rotation value at the given index into the supplied field.
     *
     * @param index The index in the array of values to read
     * @param rot The field to copy the rotation value to
     */
    public void get1Value(int index, SFRotation rot) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;
        rot.setValue(data[offset],
                     data[offset + 1],
                     data[offset + 2],
                     data[offset + 3]);
    }

    /**
     * Set the field to the new values.
     *
     * @param rots The new rot values to use
     */
    public void setValue(float rots[][]) {
        if(rots.length > numElements)
            data = new float[rots.length * 4];

        numElements = rots.length;

        ArrayUtils.flatten4(rots, numElements, data);
    }

    /**
     * Set the field to the new values.
     *
     * @param rots The new rot values to use
     */
    public void setValue(float rots[]) {

        int size = rots.length / 4;

        if(size > numElements)
            data = new float[size * 4];

        numElements = size;
        System.arraycopy(rots, 0, data, 0, size * 4);
    }

    /**
     * Set the value of this field given limited array of rots.
     * x1, y1, z1, x2, y2, z2, ....
     *
     * @param size The number of rots is size / 4.
     * @param rots Color triplicates flattened.
     */
    public void setValue(int size, float rots[]) {

        if(size > data.length)
            data = new float[size];

        numElements = size / 4;

        System.arraycopy(rots, 0, data, 0, size);
    }

    /**
     * Set the value of this field based on the values in the given field.
     *
     * @param rots The field to copy from
     */
    public void setValue(MFRotation rots) {
        int size = rots.getSize();

        if(size > numElements)
            data = new float[size * 4];

        numElements = size;

        rots.getValue(data);
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param rots The field to copy from
     */
    public void setValue(ConstMFRotation rots) {
        int size = rots.getSize();

        if(size > numElements)
            data = new float[size * 4];

        numElements = size;

        rots.getValue(data);
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param rot The field to copy the data from
     */
    public void set1Value(int index, ConstSFRotation rot) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;

        rot.getValue(tmpField);

        data[offset++] = tmpField[0];
        data[offset++] = tmpField[1];
        data[offset++] = tmpField[2];
        data[offset]   = tmpField[3];
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param rot The field to copy the data from
     */
    public void set1Value(int index, SFRotation rot) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;

        rot.getValue(tmpField);

        data[offset++] = tmpField[0];
        data[offset++] = tmpField[1];
        data[offset++] = tmpField[2];
        data[offset]   = tmpField[3];
    }

    /**
     * Set the element at the given index with the given rot components.
     *
     * @param index The index of the element to set
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     * @param a The angle component
     */
    public void set1Value(int index, float x, float y, float z, float a) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;

        data[offset++] = x;
        data[offset++] = y;
        data[offset++] = z;
        data[offset]   = a;
    }

    /**
     * Append the field as a new rot value to the end of this field.
     *
     * @param rot The field to append
     */
    public void addValue(ConstSFRotation rot) {
        int end = numElements * 4;

        realloc();

        numElements++;
        rot.getValue(tmpField);

        data[end++] = tmpField[0];
        data[end++] = tmpField[1];
        data[end++] = tmpField[2];
        data[end]   = tmpField[3];
    }

    /**
     * Append the field as a new rot value to the end of this field.
     *
     * @param rot The field to append
     */
    public void addValue(SFRotation rot) {
        int end = numElements * 4;

        realloc();

        numElements++;
        rot.getValue(tmpField);

        data[end++] = tmpField[0];
        data[end++] = tmpField[1];
        data[end++] = tmpField[2];
        data[end]   = tmpField[3];
    }

    /**
     * Append the components as a new rotation value to the end of this field.
     *
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     * @param a The angle component
     */
    public void addValue(float x, float y, float z, float a) {
        int end = numElements * 4;

        realloc();

        numElements++;

        data[end++] = x;
        data[end++] = y;
        data[end++] = z;
        data[end]   = a;
    }

    /**
     * Insert the rotation represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param rot The rot field to insert
     */
    public void insertValue(int index, ConstSFRotation rot) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;

        makeHole(offset);

        numElements++;
        rot.getValue(tmpField);

        data[offset++] = tmpField[0];
        data[offset++] = tmpField[1];
        data[offset++] = tmpField[2];
        data[offset]   = tmpField[3];
    }

    /**
     * Insert the rotation represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param rot The rot field to insert
     */
    public void insertValue(int index, SFRotation rot) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;

        makeHole(offset);

        numElements++;
        rot.getValue(tmpField);

        data[offset++] = tmpField[0];
        data[offset++] = tmpField[1];
        data[offset++] = tmpField[2];
        data[offset]   = tmpField[3];
    }

    /**
     * Insert the rotation represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     * @param a The angle component
     */
    public void insertValue(int index, float x, float y, float z, float a) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 4;

        makeHole(offset);

        numElements++;

        data[offset++] = x;
        data[offset++] = y;
        data[offset++] = z;
        data[offset]   = a;
    }

    /**
     * Clear the field of all elements
     */
    public void clear() {
        numElements = 0;
    }

    /**
     * Remove the element at the given position and shift any other items
     * down.
     *
     * @param index The position to delete the item at
     */
    public void delete(int index) {
        if((index < 0) || (index >= numElements))
            throw new ArrayIndexOutOfBoundsException("Invalid index");

        int offset = index * 4;
        int tail = numElements * 4 - offset;

        if(tail != 0)
            System.arraycopy(data, offset + 4, data, offset, tail);

        numElements--;
    }

    /**
     * Create a string representation of the field values.
     *
     * @return A string representing the values.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("[\n  ");

        int count = 0;

        for(int i = 0; i < numElements; i++) {
            buf.append(data[count++]);
            buf.append(' ');
            buf.append(data[count++]);
            buf.append(' ');
            buf.append(data[count++]);
            buf.append(' ');
            buf.append(data[count++]);
            buf.append(' ');

            if(i % 4 == 0)
                buf.append("\n  ");
        }

        buf.append("]");

        return buf.toString();
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    public Object clone() {
        return new MFRotation(numElements * 4, data);
    }

    /**
     * Convenience method to check and reallocate the internal array to a
     * larger size if needed.
     */
    private void realloc() {
        int end = numElements * 4;

        if(end >= data.length) {
            int size = end + ARRAY_INC;
            float[] tmp = new float[size];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }
    }

    /**
     * Convenience method to check and reallocate the internal array to a
     * larger size if needed while also creating a hole at the offset to
     * copy values into.
     *
     * @param offset The offset into the array to create the hole at
     */
    private void makeHole(int offset) {
        int end = numElements * 4;
        int tail = end - offset;

        if(end >= data.length) {
            int size = end + ARRAY_INC;
            float[] tmp = new float[size];

            System.arraycopy(data, 0, tmp, 0, offset - 1);
            System.arraycopy(data, offset, tmp, offset + 4, tail);
            data = tmp;
        } else {
            System.arraycopy(data, offset, data, offset + 4, tail);
        }
    }
}
