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
import vrml.ConstField;

/**
 * Constant VRML JSAI type class containing a single float value.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstSFFloat extends ConstField {

    /** The data that the field contains */
    protected float data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstSFFloat() {
    }

    /**
     * Construct a new constant field based on the given value.
     *
     * @param value The value to copy
     */
    public ConstSFFloat(float value) {
        data = value;
    }

    /**
     * Fetch the value of the field.
     *
     * @return The value at that position
     */
    public float getValue() {
        return data;
    }

    /**
     * Create a string representation of the field value.
     *
     * @return A string representing the value.
     */
    public String toString() {
        return Float.toString(data);
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    public Object clone() {
        return new ConstSFFloat(data);
    }
}
