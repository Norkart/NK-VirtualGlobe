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
import org.web3d.x3d.sai.SFBool;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Implementation of a SFBool field.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SAISFBool extends BaseField implements SFBool {

    /** The local field value */
    private boolean localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFBool(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    /**
     * Get the value in the given eventOut.
     *
     * @return The boolean value of the eventOut
     */
    public boolean getValue() {

        checkAccess(false);

        return localValue;
    }

    /**
     * Set the value in the given eventIn.
     *
     * @param value The boolean value to set the eventIn to.
     */
    public void setValue(boolean value) {

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
        localValue = data.booleanValue;
        dataChanged = false;
    }
}
