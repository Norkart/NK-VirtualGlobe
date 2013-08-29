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
 * VRML JSAI type class containing a String field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFString extends Field {

    /** The value of this field */
    protected String data;

    /**
     * Construct a default field with the value set to 0
     */
    public SFString() {
    }

    /**
     * Construct a field using the given value.
     *
     * @param value The value to set the field to
     */
    public SFString(String value) {
        data = value;
    }

    /**
     * Return the value of this field
     *
     * @return The current field value
     */
    public String getValue() {
        return data;
    }

    /**
     * Set the field to the new value
     *
     * @param s The new value to use
     */
    public void setValue(String s) {
        data = s;
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param s The field to use to set the value
     */
    public void setValue(SFString s) {
        data = s.getValue();
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param s The field to use to set the value
     */
    public void setValue(ConstSFString s) {
        data = s.getValue();
    }

    /**
     * Return a string form of this field suitable for parsing
     *
     * @return The string value at this field
     */
    public String toString() {
        return "\"" + data + "\"";
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    public Object clone() {
        return new SFString(data);
    }
}
