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
import vrml.ConstMField;

import org.web3d.util.ArrayUtils;

/**
 * Constant VRML JSAI type class containing multiple color fields.
 * <p>
 *
 * Internally the class keeps data as a flat array, so that is the most
 * efficient way of interacting with this class.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.7 $
 */
public class ConstMFColor extends ConstMField {

    /** The data of the underlying field */
    protected float[] data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstMFColor() {
        data = new float[0];
    }

    /**
     * Construct a new field based on all the given 2D array of values.
     *
     * @param colors The color values to use
     */
    public ConstMFColor(float colors[][]) {

        numElements = colors.length;
        data = new float[colors.length * 3];
        ArrayUtils.flatten3(colors, colors.length, data);

    }

    /**
     * Construct a new field based on all the given data values
     *
     * @param colors The color values to use
     */
    public ConstMFColor(float colors[]) {
        numElements = colors.length /3;

        // use int rounding to get an exact multiple of 3
        int size = numElements * 3;
        data = new float[size];
        System.arraycopy(colors, 0, data, 0, size);
    }

    /**
     * Construct a new field based on subsection the given data values
     *
     * @param size The number of items to use. numElements = size / 3
     * @param colors The color values to use
     */
    public ConstMFColor(int size, float colors[]) {
        numElements = size /3;

        // use int rounding to get an exact multiple of 3
        int real_size = numElements * 3;
        data = new float[real_size];
        System.arraycopy(colors, 0, data, 0, real_size);
    }

    /**
     * Get the color values used in this field and copy them into the user
     * provided array.
     *
     * @param colors The array to copy values to
     */
    public void getValue(float[][] colors) {
        if (numElements > 0)
            ArrayUtils.raise3(data, numElements, colors);
    }

    /**
     * Copy the color values used in this field into the user provided array.
     *
     * @param colors The array to copy values to
     */
    public void getValue(float[] colors) {
        System.arraycopy(data, 0, colors, 0, data.length);
    }

    /**
     * Get the color value at the given position and copy it into the user
     * provided array.
     *
     * @param index The position of the color to copy
     * @param color The array to copy data to
     */
    public void get1Value(int index, float[] color) {
        int offset = index * 3;
        color[0] = data[offset++];
        color[1] = data[offset++];
        color[2] = data[offset];
    }

    /**
     * Get the color value at the given position and copy it into the user
     * provided field.
     *
     * @param index The position of the color to copy
     * @param color The field to copy data to
     */
    public void get1Value(int index, SFColor color) {
        int offset = index * 3;
        color.setValue(data[offset], data[offset + 1], data[offset + 2]);
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
        return new ConstMFColor(data);
    }
}
