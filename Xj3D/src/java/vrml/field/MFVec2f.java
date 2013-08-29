/*****************************************************************************
 *                        Web2d.org Copyright (c) 2001
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
 * VRML JSAI type class containing multiple vector2f fields.
 * <p>
 *
 * Internally, the class stores the values as a single, flat array so that is
 * the most efficient method to use to avoid reallocation. All methods make
 * internal copies of the values.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.8 $
 */
public class MFVec2f extends MField {
    /**
     * How much should we increment the array by when there is an add or
     * insert request. This value is array items, not elements.
     */
    protected static final int ARRAY_INC = 10;

    /** The vec values */
    protected float[] data;

    /**
     * Construct a new empty vector.
     */
    public MFVec2f() {
        data = new float[ARRAY_INC];
        numElements = 0;
    }

    /**
     * Construct a new vector array based on all the given values.
     *
     * @param vec2s The source vector values
     */
    public MFVec2f(float[][] vec2s) {
        data = new float[vec2s.length * 2];

        ArrayUtils.flatten2(vec2s, vec2s.length, data);

        numElements = vec2s.length;
    }

    /**
     * Construct a new vector array based on all the given values.
     *
     * @param vec2s The source vector values
     */
    public MFVec2f(float[] vec2s) {

        if(vec2s.length < 2)
            throw new IllegalArgumentException("Array length less than 2");

        // Adjust the length using int division to make sure we have a
        // multiple of 2 to work with.
        numElements = vec2s.length / 2;

        int length = numElements * 2;

        data = new float[length];

        System.arraycopy(vec2s, 0, data, 0, length);
    }

    /**
     * Construct a new vector array based on a fraction of the items supplied.
     *
     * @param size The number of vectors is size / 2.
     * @param vec2s The source vector values
     */
    public MFVec2f(int size, float[] vec2s) {
        if(vec2s.length < 2)
            throw new IllegalArgumentException("Array length less than 2");

        numElements = size / 2;

        data = new float[vec2s.length];

        System.arraycopy(vec2s, 0, data, 0, vec2s.length);

    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param vec2s The target array to copy values into
     */
    public void getValue(float[][] vec2s) {
        ArrayUtils.raise2(data, numElements, vec2s);
    }

    /**
     * Copy the value of this field into the given flat array.
     *
     * @param vec2s The target array to copy values into
     */
    public void getValue(float[] vec2s) {
        System.arraycopy(data, 0, vec2s, 0, numElements * 2);
    }

    /**
     * Copy the value of the vector at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param vec2s The array to copy the vector value to
     */
    public void get1Value(int index, float[] vec2s) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;
        vec2s[0] = data[offset++];
        vec2s[1] = data[offset];
    }

    /**
     * Copy the vector value at the given index into the supplied field.
     *
     * @param index The index in the array of values to read
     * @param vec The field to copy the vector value to
     */
    public void get1Value(int index, SFVec2f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;
        vec.setValue(data[offset], data[offset + 1]);
    }

    /**
     * Set the field to the new values.
     *
     * @param vec2s The new vec values to use
     */
    public void setValue(float[][] vec2s) {
        if(vec2s.length > numElements)
            data = new float[vec2s.length * 2];

        numElements = vec2s.length;

        ArrayUtils.flatten2(vec2s, numElements, data);
    }

    /**
     * Set the field to the new values.
     *
     * @param vec2s The new vec values to use
     */
    public void setValue(float[] vec2s) {

        int size = vec2s.length / 2;

        if(size > numElements)
            data = new float[size * 2];

        numElements = size;
        System.arraycopy(vec2s, 0, data, 0, size * 2);
    }

    /**
     * Set the value of this field given limited array of vecs.
     * x1, y1, z1, x2, y2, z2, ....
     *
     * @param size The number of vecs is size / 2.
     * @param vec2s Color triplicates flattened.
     */
    public void setValue(int size, float[] vec2s) {

        if(size > data.length)
            data = new float[size];

        numElements = size / 2;

        System.arraycopy(vec2s, 0, data, 0, size);
    }

    /**
     * Set the value of this field based on the values in the given field.
     *
     * @param vecs The field to copy from
     */
    public void setValue(MFVec2f vecs) {
        int size = vecs.getSize();

        if(size > numElements)
            data = new float[size * 2];

        numElements = size;

        vecs.getValue(data);
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param vecs The field to copy from
     */
    public void setValue(ConstMFVec2f vecs) {
        int size = vecs.getSize();

        if(size > numElements)
            data = new float[size * 2];

        numElements = size;

        vecs.getValue(data);
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    public void set1Value(int index, ConstSFVec2f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;

        data[offset++] = vec.getX();
        data[offset] = vec.getY();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    public void set1Value(int index, SFVec2f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;

        data[offset++] = vec.getX();
        data[offset] = vec.getY();
    }

    /**
     * Set the element at the given index with the given vec components.
     *
     * @param index The index of the element to set
     * @param x The x component to use
     * @param y The y component to use
     */
    public void set1Value(int index, float x, float y) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;

        data[offset++] = x;
        data[offset] = y;
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    public void addValue(ConstSFVec2f vec) {
        int end = numElements * 2;

        realloc();

        numElements++;
        data[end++] = vec.getX();
        data[end] = vec.getY();
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    public void addValue(SFVec2f vec) {
        int end = numElements * 2;

        realloc();

        numElements++;
        data[end++] = vec.getX();
        data[end] = vec.getY();
    }

    /**
     * Append the components as a new vector value to the end of this field.
     *
     * @param x The x component to use
     * @param y The y component to use
     */
    public void addValue(float x, float y) {
        int end = numElements * 2;

        realloc();

        numElements++;
        data[end++] = x;
        data[end] = y;
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    public void insertValue(int index, ConstSFVec2f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;

        makeHole(offset);

        numElements++;
        data[offset++] = vec.getX();
        data[offset] = vec.getY();
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    public void insertValue(int index, SFVec2f vec) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;

        makeHole(offset);

        numElements++;
        data[offset++] = vec.getX();
        data[offset] = vec.getY();
    }

    /**
     * Insert the vector represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param x The x component to use
     * @param y The y component to use
     */
    public void insertValue(int index, float x, float y) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        int offset = index * 2;

        makeHole(offset);

        numElements++;
        data[offset++] = x;
        data[offset] = y;
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

        int offset = index * 2;
        int tail = numElements * 2 - offset;

        if(tail != 0)
            System.arraycopy(data, offset + 2, data, offset, tail);

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
        return new MFVec2f(numElements * 2, data);
    }

    /**
     * Convenience method to check and reallocate the internal array to a
     * larger size if needed.
     */
    private void realloc() {
        int end = numElements * 2;

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
        int end = numElements * 2;
        int tail = end - offset;

        if(end >= data.length) {
            int size = end + ARRAY_INC;
            float[] tmp = new float[size];

            System.arraycopy(data, 0, tmp, 0, offset - 1);
            System.arraycopy(data, offset, tmp, offset + 2, tail);
            data = tmp;
        } else {
            System.arraycopy(data, offset, data, offset + 2, tail);
        }
    }
}
