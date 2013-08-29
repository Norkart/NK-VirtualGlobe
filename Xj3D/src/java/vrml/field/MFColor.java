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
 * VRML JSAI type class containing multiple color fields.
 * <p>
 *
 * Internally, the class stores the values as a single, flat array so that is
 * the most efficient method to use to avoid reallocation. All methods make
 * internal copies of the values.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.7 $
 */
public class MFColor extends MField {

    /**
     * How much should we increment the array by when there is an add or
     * insert request. This value is array items, not elements.
     */
    protected static final int ARRAY_INC = 15;

    /** The color values */
    protected float[] data;

    /**
     * Construct a new default color with the value set to an empty array.
     */
    public MFColor() {
        data = new float[0];
        numElements = 0;
    }

    /**
     * Construct a new color array based on all the given values.
     *
     * @param colors The source colour values to use
     */
    public MFColor(float[][] colors) {
        data = new float[colors.length * 3];

        ArrayUtils.flatten2(colors, colors.length, data);

        numElements = colors.length;
    }

    /**
     * Construct a new color array based on all the given values.
     *
     * @param colors The source colour values to use
     */
    public MFColor(float[] colors) {

        if(colors.length < 3)
            throw new IllegalArgumentException("Array length less than 3");

        // Adjust the length using int division to make sure we have a
        // multiple of 3 to work with.
        numElements = colors.length / 3;

        int length = numElements * 3;

        data = new float[length];

        System.arraycopy(colors, 0, data, 0, length);
    }

    /**
     * Cosntruct a new colour array based on a fraction of the items supplied.
     *
     * @param size The number of colors is size / 3.
     * @param colors The source color values to use
     */
    public MFColor(int size, float[] colors) {
        if(colors.length < 3)
            throw new IllegalArgumentException("Array length less than 3");

        numElements = size / 3;

        data = new float[colors.length];

        System.arraycopy(colors, 0, data, 0, colors.length);
    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param colors The target array to copy values into
     */
    public void getValue(float[][] colors) {
        ArrayUtils.raise3(data, numElements, colors);
    }

    /**
     * Returns the value of this field as a flat array.
     * Values are arranged as [0 - 2] = Color1, [3 - 5] = Color2...
     *
     * @param colors Color triplicates flattened
     */
    public void getValue(float[] colors) {
        System.arraycopy(data, 0, colors, 0, numElements * 3);
    }

    /**
     * Copy the value of the color at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param colors The array to copy the color value to
     */
    public void get1Value(int index, float[] colors) {
        int offset = index * 3;
        colors[0] = data[offset++];
        colors[1] = data[offset++];
        colors[2] = data[offset];
    }

    /**
     * Copy the color value at the given index into the supplied field.
     *
     * @param index The index in the array of values to read
     * @param color The field to copy the color value to
     */
    public void get1Value(int index, SFColor color) {
        int offset = index * 3;
        color.setValue(data[offset], data[offset + 1], data[offset + 2]);
    }

    /**
     * Set the field to the new values.
     *
     * @param colors The new color values to use
     */
    public void setValue(float[][] colors) {
        if(colors.length > numElements)
            data = new float[colors.length * 3];

        numElements = colors.length;

        ArrayUtils.flatten3(colors, numElements, data);
    }

    /**
     * Set the field to the new values.
     *
     * @param colors The new color values to use
     */
    public void setValue(float[] colors) {

        int size = colors.length / 3;

        if(size > numElements)
            data = new float[size * 3];

        numElements = size;
        System.arraycopy(colors, 0, data, 0, size * 3);
    }

    /**
     * Set the value of this field given limited array of colors.
     * Values are arranged as [0 - 2] = Color1, [3 - 5] = Color2...
     *
     * @param size The number of colors is size / 3.
     * @param colors Color triplicates flattened.
     */
    public void setValue(int size, float[] colors) {

        if(size > data.length)
            data = new float[size];

        numElements = size / 3;

        System.arraycopy(colors, 0, data, 0, size);
    }

    /**
     * Set the value of this field based on the values in the given field.
     *
     * @param colors The field to copy from
     */
    public void setValue(MFColor colors) {
        int size = colors.getSize();

        if(size > numElements)
            data = new float[size * 3];

        numElements = size;

        colors.getValue(data);
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param colors The field to copy from
     */
    public void setValue(ConstMFColor colors) {
        int size = colors.getSize();

        if(size > numElements)
            data = new float[size * 3];

        numElements = size;

        colors.getValue(data);
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param color The field to copy the data from
     */
    public void set1Value(int index, ConstSFColor color) {
        int offset = index * 3;

        data[offset++] = color.getRed();
        data[offset++] = color.getGreen();
        data[offset] = color.getBlue();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param color The field to copy the data from
     */
    public void set1Value(int index, SFColor color) {
        int offset = index * 3;

        data[offset++] = color.getRed();
        data[offset++] = color.getGreen();
        data[offset] = color.getBlue();
    }

    /**
     * Set the element at the given index with the given color components.
     *
     * @param index The index of the element to set
     * @param red The red component to use
     * @param green The green component to use
     * @param blue The blue component to use
     */
    public void set1Value(int index, float red, float green, float blue) {
        int offset = index * 3;

        data[offset++] = red;
        data[offset++] = green;
        data[offset] = blue;
    }

    /**
     * Append the field as a new color value to the end of this field.
     *
     * @param color The field to append
     */
    public void addValue(ConstSFColor color) {
        int end = numElements * 3;

        realloc();

        numElements++;
        data[end++] = color.getRed();
        data[end++] = color.getGreen();
        data[end] = color.getBlue();
    }

    /**
     * Append the field as a new color value to the end of this field.
     *
     * @param color The field to append
     */
    public void addValue(SFColor color) {
        int end = numElements * 3;

        realloc();

        numElements++;
        data[end++] = color.getRed();
        data[end++] = color.getGreen();
        data[end] = color.getBlue();
    }

    /**
     * Append the components as a new color value to the end of this field.
     *
     * @param red The red component to use
     * @param green The green component to use
     * @param blue The blue component to use
     */
    public void addValue(float red, float green, float blue) {
        int end = numElements * 3;

        realloc();

        numElements++;
        data[end++] = red;
        data[end++] = green;
        data[end] = blue;
    }

    /**
     * Insert the color represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param color The color field to insert
     */
    public void insertValue(int index, ConstSFColor color) {
        int offset = index * 3;

        makeHole(offset);

        numElements++;
        data[offset++] = color.getRed();
        data[offset++] = color.getGreen();
        data[offset] = color.getBlue();
    }

    /**
     * Insert the color represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param color The color field to insert
     */
    public void insertValue(int index, SFColor color) {
        int offset = index * 3;

        makeHole(offset);

        numElements++;
        data[offset++] = color.getRed();
        data[offset++] = color.getGreen();
        data[offset] = color.getBlue();
    }

    /**
     * Insert the color represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param red The red component to use
     * @param green The green component to use
     * @param blue The blue component to use
     */
    public void insertValue(int index, float red, float green, float blue) {
        int offset = index * 3;

        makeHole(offset);

        numElements++;
        data[offset++] = red;
        data[offset++] = green;
        data[offset] = blue;
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
        return new MFColor(numElements * 3, data);
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
