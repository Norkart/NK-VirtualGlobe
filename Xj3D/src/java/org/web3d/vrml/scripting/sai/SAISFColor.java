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
import org.web3d.x3d.sai.SFColor;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.util.FieldValidator;

/**
 * Representation of a SFColor field.
 * <P>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system.
 *
 * @version 1.0 30 April 1998
 */
class SAISFColor extends BaseField implements SFColor {

    /** The local field value */
    private float[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFColor(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);

        localValue = new float[3];
    }

    /**
     * Write the value of the colour to the given array.
     *
     * @param col The array of colour values to be filled in where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(float[] col) {

        checkAccess(false);

        col[0] = localValue[0];
        col[1] = localValue[1];
        col[2] = localValue[2];
    }

    /**
     * Set the colour value in the given eventIn.  Colour values are required
     * to be in the range [0-1].
     * <P>
     * The value array must contain at least three elements. If the array
     * contains more than 3 values only the first three values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated. If the colour values are
     * out of range an IllegalArgumentException will be generated.
     *
     * @param value The array of colour values where<BR>
     *    value[0] = Red component [0-1] <BR>
     *    value[1] = Green component [0-1] <BR>
     *    value[2] = Blue component [0-1] <BR>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    public void setValue(float[] value) {

        checkAccess(true);

        FieldValidator.checkColorVector("SAI.SFColor", value);

        localValue[0] = value[0];
        localValue[1] = value[1];
        localValue[2] = value[2];
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
        node.setValue(fieldIndex, localValue, 3);
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
        dataChanged = false;
    }
}
