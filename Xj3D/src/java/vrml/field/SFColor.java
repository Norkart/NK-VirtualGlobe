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
 * VRML JSAI type class containing a color field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFColor extends Field {

    /** The components of the field */
    protected float[] data;

    /**
     * Create a new default color with the fields set to 0 0 0
     */
    public SFColor() {
        this(0, 0, 0);
    }

    /**
     * Create a new color based on the given values.
     *
     * @param red The red component of the color
     * @param green The green component of the color
     * @param blue The blue component of the color
     */
    public SFColor(float red, float green, float blue) {
        data = new float[] { red, green, blue };
    }

    /**
     * Copy this angle into the user array
     *
     * @param colors The target array to copy into
     */
    public void getValue(float[] colors) {
        colors[0] = data[0];
        colors[1] = data[1];
        colors[2] = data[2];
    }

    /**
     * Get the red component of the color
     *
     * @return The red value [0 - 1]
     */
    public float getRed() {
        return data[0];
    }

    /**
     * Get the green component of the color
     *
     * @return The green value [0 - 1]
     */
    public float getGreen() {
        return data[1];
    }

    /**
     * Get the blue component of the color
     *
     * @return The blue value [0 - 1]
     */
    public float getBlue() {
        return data[2];
    }

    /**
     * Set the field to the new value based on the user data
     *
     * @param colors The new data to copy
     */
    public void setValue(float[] colors) {
        data[0] = colors[0];
        data[1] = colors[1];
        data[2] = colors[2];
    }

    /**
     * Set the field to the new components.
     *
     * @param red The red component of the color
     * @param green The green component of the color
     * @param blue The blue component of the color
     */
    public void setValue(float red, float green, float blue) {
        data[0] = red;
        data[1] = green;
        data[2] = blue;
    }

    /**
     * Set this field to the values from the passed in field
     *
     * @param color The field to copy the values from
     */
    public void setValue(ConstSFColor color) {
        color.getValue(data);
    }

    /**
     * Set this field to the values from the passed in field
     *
     * @param color The field to copy the values from
     */
    public void setValue(SFColor color) {
        color.getValue(data);
    }

    /**
     * Create a string representation of this field.
     *
     * @return A string representation of the color
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(data[0]);
        buf.append(' ');
        buf.append(data[1]);
        buf.append(' ');
        buf.append(data[2]);

        return buf.toString();
    }

    /**
     * Create a cloned copy of this node.
     *
     * @return A complete copy of the node
     */
    public Object clone() {
        return new SFColor(data[0], data[1], data[2]);
    }
}
