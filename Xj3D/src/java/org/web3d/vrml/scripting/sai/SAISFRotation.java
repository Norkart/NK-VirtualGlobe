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

package org.web3d.vrml.scripting.sai;

// Standard imports
// None

// Application specific imports
import org.web3d.x3d.sai.SFRotation;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Implementation of a SFRotation field.
 * <P>
 * Rotation values are specified according to the VRML IS Specification
 * Section 5.8 SFRotation and MFRotation.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class SAISFRotation extends BaseField implements SFRotation {

    /** The local field value */
    private float[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFRotation(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);

        localValue = new float[4];
    }

    /**
     * Write the rotation value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] vec) {

        checkAccess(false);

        vec[0] = localValue[0];
        vec[1] = localValue[1];
        vec[2] = localValue[2];
        vec[3] = localValue[3];
    }

    /**
     * Set the rotation value in the given eventIn.
     * <P>
     * The value array must contain at least four elements. If the array
     * contains more than 4 values only the first 4 values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 4 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param value The array of rotation values where<BR>
     *    value[0] = X component [0-1] <BR>
     *    value[1] = Y component [0-1] <BR>
     *    value[2] = Z component [0-1] <BR>
     *    value[3] = Angle of rotation [-PI - PI] (nominally).
     *
     * @exception ArrayIndexOutOfBoundsException The value did not contain at least 4
     *    values for the rotation.
     */
    public void setValue(float[] value) {

        checkAccess(true);

        localValue[0] = value[0];
        localValue[1] = value[1];
        localValue[2] = value[2];
        localValue[3] = value[3];
        dataChanged = true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Notification to the field instance to update the value in the
     * underlying node now.
     */
    void updateNode() {
        node.setValue(fieldIndex, localValue, 4);
        dataChanged = false;
    }

    /**
     * Notification to the field to update its field values from the
     * underlying node.
     */
    void updateField() {
        if(!isReadable())
            return;

        VRMLFieldData data = node.getFieldValue(fieldIndex);
        localValue[0] = data.floatArrayValue[0];
        localValue[1] = data.floatArrayValue[1];
        localValue[2] = data.floatArrayValue[2];
        localValue[3] = data.floatArrayValue[3];
        dataChanged = false;
    }
}
