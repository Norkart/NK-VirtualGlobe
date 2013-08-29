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

/**
 * Constant VRML JSAI type class containing multiple time fields.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstMFInt32 extends ConstMField {

    /** The data that the field contains */
    protected int[] data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstMFInt32() {
        data = new int[0];
    }

    /**
     * Construct a new constant field based on all the given values.
     *
     * @param values The values to copy
     */
    public ConstMFInt32(int[] values) {
        this(values.length, values);
    }

    /**
     * Construct new constant field based on the subset of the given values.
     *
     * @param size The number of items to copy
     * @param values The source values to copy
     */
    public ConstMFInt32(int size, int[] values) {
        numElements = size;
        data = new int[numElements];
        System.arraycopy(values, 0, data, 0, numElements);
    }

    /**
     * Copy the values of this array into the user provided array.
     *
     * @param values The array to copy data into
     */
    public void getValue(int[] values) {
        System.arraycopy(data, 0, values, 0, numElements);
    }

    /**
     * Fetch the value at the given array index position
     *
     * @param index The position to ask the value of
     * @return The value at that position
     */
    public int get1Value(int index) {
        return data[index];
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
        return new ConstMFInt32(numElements, data);
    }
}
