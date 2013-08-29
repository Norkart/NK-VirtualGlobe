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

package org.web3d.vrml.scripting.jsai;

// Standard imports
// none

// Application specific imports
import vrml.field.ConstSFColor;
import vrml.field.SFColor;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Xj3D Specific implementation of the SFColor field when extracted from part
 * of a node.
 * <p>
 *
 * The node assumes that the index and node have been checked before use by
 * this class.
 * <p>
 *
 * An interesting implementation question is dealing with the methods that
 * fetch an individual color component. The current implementation does not
 * force a re-fetch of the value from the underlying node. Perhaps it should.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
class JSAISFColor extends SFColor
    implements VRMLNodeListener {

    /** The node that this field references */
    private VRMLNodeType node;

    /** The index of the field this representation belongs to */
    private int fieldIndex;

    /**
     * Create a new field that represents the underlying instance
     *
     * @param n The node to fetch information from
     * @param index The index of the field to use
     */
    JSAISFColor(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Get the value of the field. Overrides the basic implementation to
     * make sure that it fetches new data each time.
     *
     * @return The array to copy values to
     */
    public void getValue(float[] colors) {
        updateLocalData();

        colors[0] = data[0];
        colors[1] = data[1];
        colors[2] = data[2];
    }

    /**
     * Set the field to the new value
     *
     * @param val The new value to use
     */
    public void setValue(float[] val) {
        data[0] = val[0];
        data[1] = val[1];
        data[2] = val[2];

        try {
            node.setValue(fieldIndex, val, 3);
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of the field to the given set of component colors.
     *
     * @param red The red component of the color
     * @param green The green component of the color
     * @param blue The blue component of the color
     */
    public void setValue(float red, float green, float blue) {
        data[0] = red;
        data[1] = green;
        data[2] = blue;

        try {
            node.setValue(fieldIndex, data, 3);
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    public void setValue(ConstSFColor val) {
        val.getValue(data);

        try {
            node.setValue(fieldIndex, data, 3);
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    public void setValue(SFColor val) {
        val.getValue(data);

        try {
            node.setValue(fieldIndex, data, 3);
        } catch(FieldException ife) {
        }
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    public Object clone() {
        return new JSAISFColor(node, fieldIndex);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeListener interface.
    //----------------------------------------------------------

    /**
     * Notification that the field represented by the given index has changed.
     *
     * @param index The index of the field that has changed
     */
    public void fieldChanged(int index) {
        if(index != fieldIndex)
            return;

        valueChanged = true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Fetch the field from the core and update any values internally.
     */
    private void updateLocalData() {
        if(!valueChanged)
            return;

        try {
            VRMLFieldData fd = node.getFieldValue(fieldIndex);

            if(fd == null) {
                data[0] = 0;
                data[1] = 0;
                data[2] = 0;
            } else {
                data[0] = fd.floatArrayValue[0];
                data[1] = fd.floatArrayValue[1];
                data[2] = fd.floatArrayValue[2];
            }

            valueChanged = true;
        } catch(FieldException ife) {
        }
    }
}
