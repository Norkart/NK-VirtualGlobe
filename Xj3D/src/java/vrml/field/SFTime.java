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
import vrml.Field;

/**
 * VRML JSAI type class containing a double field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFTime extends Field {

    /** The value of this field */
    protected double data;

    /**
     * Construct a default field with the value set to 0
     */
    public SFTime() {
    }

    /**
     * Construct a field using the given value.
     *
     * @param value The value to set the field to
     */
    public SFTime(double value) {
        data = value;
    }

    /**
     * Return the value of this field
     *
     * @return The current field value
     */
    public double getValue() {
        return data;
    }

    /**
     * Set the field to the new value
     *
     * @param time The new value to use
     */
    public void setValue(double time) {
        data = time;
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param time The field to use to set the value
     */
    public void setValue(SFTime time) {
        data = time.getValue();
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param time The field to use to set the value
     */
    public void setValue(ConstSFTime time) {
        data = time.getValue();
    }

    /**
     * Return a string form of this field suitable for parsing
     *
     * @return The string value at this field
     */
    public String toString() {
        return Double.toString(data);
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    public Object clone() {
        return new SFTime(data);
    }
}
