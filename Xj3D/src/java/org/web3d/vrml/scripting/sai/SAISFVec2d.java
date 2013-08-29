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
import org.web3d.x3d.sai.SFVec2d;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a SFVec2d field.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class SAISFVec2d extends BaseField implements SFVec2d {

    /** The local field value */
    private double[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFVec2d(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);

        localValue = new double[2];
    }

    /**
     * Write the vector value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where<BR>
     *    vec[0] = X<BR>
     *    vec[1] = Y
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    public void getValue(double[] vec) {
        checkAccess(false);

        vec[0] = localValue[0];
        vec[1] = localValue[1];
    }

    /**
     * Set the vector value in the given eventIn.
     * <P>
     * The value array must contain at least two elements. If the array
     * contains more than 2 values only the first 2 values will be used and
     * the rest ignored.
     * <P>
     * If the array of values does not contain at least 2 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param value The array of vector components where<BR>
     *    value[0] = X<BR>
     *    value[1] = Y<BR>
     *
     * @exception ArrayIndexOutOfBoundsException The value did not contain at least two
     *    values for the vector
     */
    public void setValue(double[] value) {
        checkAccess(true);

        localValue[0] = value[0];
        localValue[1] = value[1];
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
        node.setValue(fieldIndex, localValue, 2);
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
        localValue[0] = data.doubleArrayValue[0];
        localValue[1] = data.doubleArrayValue[1];
        dataChanged = false;
    }
}
