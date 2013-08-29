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
import vrml.BaseNode;
import vrml.Browser;
import vrml.field.ConstMFNode;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * JSAI VRML type class containing multiple node fields
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
class JSAIConstMFNode extends ConstMFNode
    implements VRMLNodeListener, NodeField {

    /** The node that this field references */
    private VRMLNodeType node;

    /** The index of the field this representation belongs to */
    private int fieldIndex;

    /** The factory to create field references from */
    private FieldFactory fieldFactory;

    /** The browser instance we pass around the nodes */
    private Browser browser;

    /**
     * Create a new field that represents the underlying instance
     *
     * @param n The node to fetch information from
     * @param index The index of the field to use
     */
    JSAIConstMFNode(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;

        node.addNodeListener(this);
    }

    /**
     * Initialise this instance with the extra information needed.
     *
     * @param b The browser to use with this node
     * @param fac The Factory used to create fields
     */
    public void initialize(Browser b, FieldFactory fac) {
        browser = b;
        fieldFactory = fac;

        valueChanged = true;
        updateLocalData();
    }

    /**
     * Copy the values from this field into the user array
     *
     * @param values The target array to copy into
     */
    public void getValue(BaseNode[] node) {
        updateLocalData();
        super.getValue(node);
    }

    /**
     * Get the value of the field at the given index
     *
     * @param index The position to get the value from
     * @return The value at that index
     */
    public BaseNode get1Value(int index) {
        updateLocalData();
        return super.get1Value(index);
    }

    /**
     * Create a string representation of the field values.
     *
     * @return A string representing the values.
     */
    public String toString() {
        updateLocalData();
        return super.toString();
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    public Object clone() {
        return new JSAIConstMFNode(node, fieldIndex);
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

            if (fd == null)
                return;

            if (fd.numElements == 0) {
                numElements = 0;
                data = new BaseNode[0];
            } else {
                VRMLNodeType[] src_nodes = (VRMLNodeType[])fd.nodeArrayValue;

                if(numElements != fd.numElements) {
                    numElements = fd.numElements;
                    data = new BaseNode[numElements];
                }

                for(int i = 0; i < numElements; i++) {
                    data[i] = new JSAINode(src_nodes[i],
                                       browser,
                                       fieldFactory);
                }

            }

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }
}
