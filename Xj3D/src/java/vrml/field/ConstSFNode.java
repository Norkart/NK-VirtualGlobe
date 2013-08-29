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
import vrml.BaseNode;
import vrml.ConstField;

/**
 * Constant VRML JSAI type class containing a single BaseNode value.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstSFNode extends ConstField {

    /** The data that the field contains */
    protected BaseNode data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstSFNode() {
    }

    /**
     * Construct a new constant field based on the given value.
     *
     * @param value The value to copy
     */
    public ConstSFNode(BaseNode value) {
        data = value;
    }

    /**
     * Fetch the value of the field.
     *
     * @return The value at that position
     */
    public BaseNode getValue() {
        return data;
    }

    /**
     * Return a string form of this field suitable for parsing. Currently
     * only provides the minimal required by scripts.
     *
     * @return The string value at this field
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        if(data == null) {
            buf.append("NULL");
        } else {
            buf.append(data.getType());
            buf.append("{}");
        }

        return buf.toString();
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    public Object clone() {
        return new ConstSFNode(data);
    }
}
