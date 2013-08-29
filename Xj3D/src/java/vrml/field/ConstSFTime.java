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
 * Constant VRML JSAI type class containing a single double value.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstSFTime extends ConstField {

    /** The data that the field contains */
    protected double data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstSFTime() {
    }

    /**
     * Construct a new constant field based on the given value.
     *
     * @param value The value to copy
     */
    public ConstSFTime(double value) {
        data = value;
    }

    /**
     * Fetch the value of the field.
     *
     * @return The value at that position
     */
    public double getValue() {
        return data;
    }

    /**
     * Create a string representation of the field value.
     *
     * @return A string representing the value.
     */
    public String toString() {
        return Double.toString(data);
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    public Object clone() {
        return new ConstSFTime(data);
    }
}
