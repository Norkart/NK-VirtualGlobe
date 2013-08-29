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
 * VRML type class containing a rotation field
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstSFRotation extends ConstField {

    /** The components of the field */
    protected float[] data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstSFRotation() {
        data = new float[4];
    }

    /**
     * Create a new rotation based on the given values.
     *
     * @param axisX The x component of the rotation axis
     * @param axisY The y component of the rotation axis
     * @param axisZ The z component of the rotation axis
     * @param angle The angle component of the rotation
     */
    public ConstSFRotation(float axisX, float axisY, float axisZ, float angle) {
        data = new float[] { axisX, axisY, axisZ, angle };
    }

    /**
     * Copy this angle into the user array
     *
     * @param rotations The target array to copy into
     */
    public void getValue(float[] rotations) {
        rotations[0] = data[0];
        rotations[1] = data[1];
        rotations[2] = data[2];
        rotations[3] = data[3];
    }

    /**
     * Create a string representation of this field.
     *
     * @return A string representation of the rotation
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(data[0]);
        buf.append(' ');
        buf.append(data[1]);
        buf.append(' ');
        buf.append(data[2]);
        buf.append(' ');
        buf.append(data[3]);

        return buf.toString();
    }

    /**
     * Create a cloned copy of this node.
     *
     * @return A complete copy of the node
     */
    public Object clone() {
        return new ConstSFRotation(data[0], data[1], data[2], data[3]);
    }
}
