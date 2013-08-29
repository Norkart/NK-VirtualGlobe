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
import vrml.field.ConstSFFloat;
import vrml.field.SFFloat;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Xj3D Specific implementation of the SFFloat field when extracted from part
 * of a node.
 * <p>
 *
 * The node assumes that the index and node have been checked before use by
 * this class.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class JSAIConstSFFloat extends ConstSFFloat
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
    JSAIConstSFFloat(VRMLNodeType n, int index) {
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
     * @return The value of the field
     */
    public float getValue() {
        updateLocalData();

        return data;
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    public Object clone() {
        return new JSAIConstSFFloat(node, fieldIndex);
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

    private void updateLocalData() {
        if(!valueChanged)
            return;

        try {
            VRMLFieldData fd = node.getFieldValue(fieldIndex);
            data = (fd == null) ? 0 : fd.floatValue;
            valueChanged = false;
        } catch(FieldException ife) {
        }
    }
}
