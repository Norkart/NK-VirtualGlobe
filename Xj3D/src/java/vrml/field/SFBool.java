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
 * VRML JSAI type class containing a boolean field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFBool extends Field {

    /** The value of this field */
    protected boolean data;

    /**
     * Construct a default field with the value set to true
     */
    public SFBool() {
        data = true;
    }

    /**
     * Construct a field using the given value.
     *
     * @param value The value to set the field to
     */
    public SFBool(boolean value) {
        data = value;
    }

    /**
     * Return the value of this field
     *
     * @return The current field value
     */
    public boolean getValue() {
        return data;
    }

    /**
     * Set the field to the new value
     *
     * @param b The new value to use
     */
    public void setValue(boolean b) {
        data = b;
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param b The field to use to set the value
     */
    public void setValue(SFBool b) {
        data = b.getValue();
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param b The field to use to set the value
     */
    public void setValue(ConstSFBool b) {
        data = b.getValue();
    }

    /**
     * Return a string form of this field suitable for parsing
     *
     * @return The string value at this field
     */
    public String toString() {
        return data ? "TRUE" : "FALSE";
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    public Object clone() {
        return new SFBool(data);
    }
}
