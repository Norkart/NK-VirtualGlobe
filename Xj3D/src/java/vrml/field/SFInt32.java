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
 * VRML JSAI type class containing a int field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFInt32 extends Field {

    /** The value of this field */
    protected int data;

    /**
     * Construct a default field with the value set to 0
     */
    public SFInt32() {
    }

    /**
     * Construct a field using the given value.
     *
     * @param value The value to set the field to
     */
    public SFInt32(int value) {
        data = value;
    }

    /**
     * Return the value of this field
     *
     * @return The current field value
     */
    public int getValue() {
        return data;
    }

    /**
     * Set the field to the new value
     *
     * @param val The new value to use
     */
    public void setValue(int val) {
        data = val;
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    public void setValue(SFInt32 val) {
        data = val.getValue();
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    public void setValue(ConstSFInt32 val) {
        data = val.getValue();
    }

    /**
     * Return a string form of this field suitable for parsing
     *
     * @return The string value at this field
     */
    public String toString() {
        return Integer.toString(data);
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    public Object clone() {
        return new SFInt32(data);
    }
}
