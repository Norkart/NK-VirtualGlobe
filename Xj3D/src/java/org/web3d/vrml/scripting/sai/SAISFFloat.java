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
import org.web3d.x3d.sai.SFFloat;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Implementation of a SFFloat field.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SAISFFloat extends BaseField implements SFFloat {

    /** The local field value */
    private float localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFFloat(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    /**
     * Ge the double value in the given eventOut.
     *
     * @return The double value to of the eventOut
     */
    public float getValue() {
        checkAccess(false);

        return localValue;
    }

    /**
     * Set the double value in the given eventIn.
     *
     * @param value The new value to set.
     */
    public void setValue(float value) {
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
        localValue = data.floatValue;
        dataChanged = false;
    }
}
