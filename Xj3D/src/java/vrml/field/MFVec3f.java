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
package vrml.field;

// Standard imports
// none

// Application specific imports
import vrml.MField;

import org.web3d.util.ArrayUtils;

/**
 * VRML JSAI type class containing multiple vector3f fields.
 * <p>
 *
 * Internally, the class stores the values as a single, flat array so that is
 * the most efficient method to use to avoid reallocation. All methods make
 * internal copies of the values.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.7 $
 */
public class MFVec3f extends MField {
    /**
     * How much should we increment the array by when there is an add or
     * insert request. This value is array items, not elements.
     */
    protected static final int ARRAY_INC = 15;

    /** The vec values */
    protected float[] data;

    /**
     * Construct a new empty vector.
     */
    public MFVec3f() {
        data = new float[ARRAY_INC];
        numElements = 0;
    }

    /**
     * Construct a new vector array based on all the given values.
     *
     * @param vec3s The source vector values
     */
    public MFVec3f(float vec3s[][]) {
        data = new float[vec3s.length * 3];

        ArrayUtils.flatten2(vec3s, vec3s.length, data);

        numElements = vec3s.length;
    }

    /**
     * Construct a new vector array based on all the given values.
     *
     * @param vec3s The source vector values
     */
    public MFVec3f(float vec3s[]) {

        if(vec3s.length < 3)
            throw new IllegalArgumentException("Array length less than 3");

        // Adjust the length using int division to make sure we have a
        // multiple of 3 to work with.
        numElements = vec3s.length / 3;

        int length = numElements * 3;

        data = new float[length];

        System.arraycopy(vec3s, 0, data, 0, length);
    }

    /**
     * Construct a new vector array based on a fraction of the items supplied.
     *
     * @param size The number of vectors is size / 3.
     * @param vec3s The source vector values
     */
    public MFVec3f(int size, float vec3s[]) {
        if(vec3s.length < 3)
            throw new IllegalArgumentException("Array length less than 3");

        numElements = size / 3;

        data = new float[vec3s.length];

        System.arraycopy(vec3s, 0, data, 0, vec3s.length);

    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param vec3s The target array to copy values into
     */
    public void getValue(float vec3s[][]) {
        ArrayUtils.raise3(data, numElements, vec3s);
    }

    /**
     * Copy the value of this field into the given flat array.
     *
     * @param vec3s The target array to copy values into
     */
    public void getValue(float vec3s[]) {
        System.arraycopy(data, 0, vec3s, 0, numElements * 3);
    }

    /**
     * Copy the value of the vector at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param vec3s The array to copy the vector value to
     */
    public void get1Value(int index, float vec3s[]) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;
        vec3s[0] = data[offset++];
        vec3s[1] = data[offset++];
        vec3s[2] = data[offset];
    }

    /**
     * Copy the vector value at the given index into the supplied field.
     *
     * @param index The index in the array of values to read
     * @param vec The field to copy the vector value to
     */
    public void get1Value(int index, SFVec3f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;
        vec.setValue(data[offset], data[offset + 1], data[offset + 2]);
    }

    /**
     * Set the field to the new values.
     *
     * @param vec3s The new vec values to use
     */
    public void setValue(float vec3s[][]) {
        if(vec3s.length > numElements)
            data = new float[vec3s.length * 3];

        numElements = vec3s.length;

        ArrayUtils.flatten3(vec3s, numElements, data);
    }

    /**
     * Set the field to the new values.
     *
     * @param vec3s The new vec values to use
     */
    public void setValue(float vec3s[]) {

        int size = vec3s.length / 3;

        if(size > numElements)
            data = new float[size * 3];

        numElements = size;
        System.arraycopy(vec3s, 0, data, 0, size * 3);
    }

    /**
     * Set the value of this field given limited array of vecs.
     * x1, y1, z1, x2, y2, z2, ....
     *
     * @param size The number of vecs is size / 3.
     * @param vec3s Color triplicates flattened.
     */
    public void setValue(int size, float vec3s[]) {

        if(size > data.length)
            data = new float[size];

        numElements = size / 3;

        System.arraycopy(vec3s, 0, data, 0, size);
    }

    /**
     * Set the value of this field based on the values in the given field.
     *
     * @param vecs The field to copy from
     */
    public void setValue(MFVec3f vecs) {
        int size = vecs.getSize();

        if(size > numElements)
            data = new float[size * 3];

        numElements = size;

        vecs.getValue(data);
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param vecs The field to copy from
     */
    public void setValue(ConstMFVec3f vecs) {
        int size = vecs.getSize();

        if(size > numElements)
            data = new float[size * 3];

        numElements = size;

        vecs.getValue(data);
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    public void set1Value(int index, ConstSFVec3f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;

        data[offset++] = vec.getX();
        data[offset++] = vec.getY();
        data[offset] = vec.getZ();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    public void set1Value(int index, SFVec3f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;

        data[offset++] = vec.getX();
        data[offset++] = vec.getY();
        data[offset] = vec.getZ();
    }

    /**
     * Set the element at the given index with the given vec components.
     *
     * @param index The index of the element to set
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     */
    public void set1Value(int index, float x, float y, float z) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;

        data[offset++] = x;
        data[offset++] = y;
        data[offset] = z;
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    public void addValue(ConstSFVec3f vec) {
        int end = numElements * 3;

        realloc();

        numElements++;
        data[end++] = vec.getX();
        data[end++] = vec.getY();
        data[end] = vec.getZ();
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    public void addValue(SFVec3f vec) {
        int end = numElements * 3;

        realloc();

        numElements++;
        data[end++] = vec.getX();
        data[end++] = vec.getY();
        data[end] = vec.getZ();
    }

    /**
     * Append the components as a new vector value to the end of this field.
     *
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     */
    public void addValue(float x, float y, float z) {
        int end = numElements * 3;

        realloc();

        numElements++;
        data[end++] = x;
        data[end++] = y;
        data[end] = z;
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    public void insertValue(int index, ConstSFVec3f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;

        makeHole(offset);

        numElements++;
        data[offset++] = vec.getX();
        data[offset++] = vec.getY();
        data[offset] = vec.getZ();
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    public void insertValue(int index, SFVec3f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;

        makeHole(offset);

        numElements++;
        data[offset++] = vec.getX();
        data[offset++] = vec.getY();
        data[offset] = vec.getZ();
    }

    /**
     * Insert the vector represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     */
    public void insertValue(int index, float x, float y, float z) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 3;

        makeHole(offset);

        numElements++;
        data[offset++] = x;
        data[offset++] = y;
        data[offset] = z;
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

        int offset = index * 3;
        int tail = numElements * 3 - offset;

        if(tail != 0)
            System.arraycopy(data, offset + 3, data, offset, tail);

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
        return new MFVec3f(numElements * 3, data);
    }

    /**
     * Convenience method to check and reallocate the internal array to a
     * larger size if needed.
     */
    private void realloc() {
        int end = numElements * 3;

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
        int end = numElements * 3;
        int tail = end - offset;

        if(end >= data.length) {
            int size = end + ARRAY_INC;
            float[] tmp = new float[size];

            System.arraycopy(data, 0, tmp, 0, offset - 1);
            System.arraycopy(data, offset, tmp, offset + 3, tail);
            data = tmp;
        } else {
            System.arraycopy(data, offset, data, offset + 3, tail);
        }
    }
}
