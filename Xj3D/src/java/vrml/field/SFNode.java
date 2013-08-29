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
import vrml.Field;

/**
 * VRML JSAI type class containing a BaseNode field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFNode extends Field {

    /** The value of this field */
    protected BaseNode data;

    /**
     * Construct a default field with the value set to NULL
     */
    public SFNode() {
    }

    /**
     * Construct a field using the given value.
     *
     * @param value The value to set the field to
     */
    public SFNode(BaseNode value) {
        data = value;
    }

    /**
     * Return the value of this field
     *
     * @return The current field value
     */
    public BaseNode getValue() {
        return data;
    }

    /**
     * Set the field to the new value
     *
     * @param node The new value to use
     */
    public void setValue(BaseNode node) {
        data = node;
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param node The field to use to set the value
     */
    public void setValue(SFNode node) {
        data = node.getValue();
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param node The field to use to set the value
     */
    public void setValue(ConstSFNode node) {
        data = node.getValue();
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
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    public Object clone() {
        return new SFNode(data);
    }
}
