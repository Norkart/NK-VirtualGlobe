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

/**
 * VRML JSAI type class containing multiple float fields
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.6 $
 */
public class MFFloat extends MField {

    /**
     * How much should we increment the array by when there is an add or
     * insert request. This value is array items, not elements.
     */
    protected static final int ARRAY_INC = 5;

    /** The rot values */
    protected float[] data;

    /**
     * Create a new empty array with no content.
     */
    public MFFloat() {
        data = new float[ARRAY_INC];
        numElements = 0;
    }

    /**
     * Create a new float array based on all the values.
     *
     * @param values The values to copy
     */
    public MFFloat(float[] values) {
        numElements = values.length;

        data = new float[numElements];

        System.arraycopy(values, 0, data, 0, numElements);
    }

    /**
     * Create a new float array based on a selection of values
     *
     * @param size The number of elements to copy
     * @param values The values to copy
     */
    public MFFloat(int size, float[] values) {
        numElements = size;
        data = new float[size];
        System.arraycopy(values, 0, data, 0, size);
    }

    /**
     * Copy the values from this field into the user array
     *
     * @param values The target array to copy into
     */
    public void getValue(float[] values) {
        System.arraycopy(data, 0, values, 0, numElements);
    }

    /**
     * Get the value of the field at the given index
     *
     * @param index The position to get the value from
     * @return The value at that index
     */
    public float get1Value(int index) {
        if(index >= numElements)
            throw new IllegalArgumentException("Index > numElements");

        return data[index];
    }

    /**
     * Replace the value of this array with the given values
     *
     * @param values The new values to use
     */
    public void setValue(float[] values) {
        if(values.length > data.length)
            data = new float[values.length];

        System.arraycopy(values, 0, data, 0, values.length);
        numElements = values.length;
    }

    /**
     * Replace the value of this array with a subsection of the given values.
     *
     * @param size The number of elements to copy
     * @param values The new values to use
     */
    public void setValue(int size, float[] values) {
        if(size > data.length)
            data = new float[size];
        System.arraycopy(values, 0, data, 0, size);
        numElements = size;
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param value The field to copy data from
     */
    public void setValue(MFFloat value) {
        int size = value.getSize();

        if(size > data.length)
            data = new float[size];

        value.getValue(data);
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param value The field to copy data from
     */
    public void setValue(ConstMFFloat value) {
        int size = value.getSize();

        if(size > data.length)
            data = new float[size];

        value.getValue(data);
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, float f) {
        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException("Index > numElements");

        data[index] = f;
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, ConstSFFloat f) {
        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException("Index > numElements");

        data[index] = f.getValue();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, SFFloat f) {
        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException("Index > numElements");

        data[index] = f.getValue();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(float f) {
        realloc();

        data[numElements++] = f;
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(SFFloat f) {
        realloc();

        data[numElements++] = f.getValue();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(ConstSFFloat f) {
        realloc();

        data[numElements++] = f.getValue();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, float f) {
        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException("Index > numElements");

        makeHole(index);

        data[index] = f;
        numElements++;
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, ConstSFFloat f) {
        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException("Index > numElements");

        makeHole(index);

        data[index] = f.getValue();
        numElements++;
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, SFFloat f) {
        if(index >= numElements)
            throw new ArrayIndexOutOfBoundsException("Index > numElements");

        makeHole(index);

        data[index] = f.getValue();
        numElements++;
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

            if(i % 6 == 0)
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
        return new MFFloat(numElements, data);
    }

    /**
     * Convenience method to check and reallocate the internal array to a
     * larger size if needed.
     */
    private void realloc() {

        if(numElements >= data.length) {
            int size = numElements + ARRAY_INC;
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
        int tail = numElements - offset;

        if(numElements >= data.length) {
            int size = numElements + ARRAY_INC;
            float[] tmp = new float[size];

            System.arraycopy(data, 0, tmp, 0, offset - 1);
            System.arraycopy(data, offset, tmp, offset + 1, tail);
            data = tmp;
        } else {
            System.arraycopy(data, offset, data, offset + 1, tail);
        }
    }
}
