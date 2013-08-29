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
 * VRML type class containing a rotation field
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class SFRotation extends Field {

    /** The components of the field */
    protected float[] data;

    /**
     * Create a new default rotation with the fields set to 0 1 0 0
     */
    public SFRotation() {
        this(0, 1, 0, 0);
    }

    /**
     * Create a new rotation based on the given values.
     *
     * @param axisX The x component of the rotation axis
     * @param axisY The y component of the rotation axis
     * @param axisZ The z component of the rotation axis
     * @param angle The angle component of the rotation
     */
    public SFRotation(float axisX, float axisY, float axisZ, float angle) {
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
     * Set the field to the new value based on the user data
     *
     * @param rotations The new data to copy
     */
    public void setValue(float[] rotations) {
        data[0] = rotations[0];
        data[1] = rotations[1];
        data[2] = rotations[2];
        data[3] = rotations[3];
    }

    /**
     * Set the field to the new components.
     *
     * @param axisX The x component of the rotation axis
     * @param axisY The y component of the rotation axis
     * @param axisZ The z component of the rotation axis
     * @param angle The angle component of the rotation
     */
    public void setValue(float axisX, float axisY, float axisZ, float angle) {
        data[0] = axisX;
        data[1] = axisY;
        data[2] = axisZ;
        data[3] = angle;
    }

    /**
     * Set this field to the values from the passed in field
     *
     * @param rotation The field to copy the values from
     */
    public void setValue(ConstSFRotation rotation) {
        rotation.getValue(data);
    }

    /**
     * Set this field to the values from the passed in field
     *
     * @param rotation The field to copy the values from
     */
    public void setValue(SFRotation rotation) {
        rotation.getValue(data);
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
        return new SFRotation(data[0], data[1], data[2], data[3]);
    }
}
