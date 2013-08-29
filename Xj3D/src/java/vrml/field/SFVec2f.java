/*****************************************************************************
 *                        Web2d.org Copyright (c) 2001
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
 * VRML JSAI type class containing a 2 component vector field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFVec2f extends Field {

    /** The components of the field */
    protected float[] data;

    /**
     * Create a new default vec with the fields set to 0 0 0
     */
    public SFVec2f() {
        this(0, 0);
    }

    /**
     * Create a new vec based on the given values.
     *
     * @param x The x component of the vec
     * @param y The y component of the vec
     */
    public SFVec2f(float x, float y) {
        data = new float[] { x, y };
    }

    /**
     * Copy this angle into the user array
     *
     * @param vec The target array to copy into
     */
    public void getValue(float[] vec) {
        vec[0] = data[0];
        vec[1] = data[1];
    }

    /**
     * Get the x component of the color
     *
     * @return The x value
     */
    public float getX() {
        return data[0];
    }

    /**
     * Get the y component of the color
     *
     * @return The y value
     */
    public float getY() {
        return data[1];
    }

    /**
     * Set the field to the new value based on the user data
     *
     * @param vec The new data to copy
     */
    public void setValue(float[] vec) {
        data[0] = vec[0];
        data[1] = vec[1];
    }

    /**
     * Set the field to the new components.
     *
     * @param x The x component of the vec
     * @param y The y component of the vec
     */
    public void setValue(float x, float y) {
        data[0] = x;
        data[1] = y;
    }

    /**
     * Set this field to the values from the passed in field
     *
     * @param vec The field to copy the values from
     */
    public void setValue(ConstSFVec2f vec) {
        vec.getValue(data);
    }

    /**
     * Set this field to the values from the passed in field
     *
     * @param vec The field to copy the values from
     */
    public void setValue(SFVec2f vec) {
        vec.getValue(data);
    }

    /**
     * Create a string representation of this field.
     *
     * @return A string representation of the vec
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(data[0]);
        buf.append(' ');
        buf.append(data[1]);

        return buf.toString();
    }

    /**
     * Create a cloned copy of this node.
     *
     * @return A complete copy of the node
     */
    public Object clone() {
        return new SFVec2f(data[0], data[1]);
    }
}
