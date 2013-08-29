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
import org.web3d.x3d.sai.SFTime;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Implementation of a SFTime field.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SAISFTime extends BaseField implements SFTime {

    /** The local field value */
    private double localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFTime(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    /**
     * Get the time value in the given eventOut. Time can be any value either
     * positive or negative but always absolute in value. As per the VRML
     * time specification, all time values are absolute.
     *
     * @return The current time of this eventOut.
     */
    public double getValue() {
        checkAccess(false);

        return localValue;
    }

    /**
     * Set the time value in the given eventIn. Time can be any value either
     * positive or negative but always absolute in value. As per the VRML
     * time specification, all time values are to be absolute.
     *
     * @param value The time value to be set.
     */
    public void setValue(double value) {
        checkAccess(true);

        localValue = value;
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
        node.setValue(fieldIndex, localValue);
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
        localValue = data.doubleValue;
        dataChanged = false;
    }
}
