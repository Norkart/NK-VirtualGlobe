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
import vrml.field.ConstSFNode;
import vrml.field.MFNode;
import vrml.field.SFNode;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * JSAI VRML type class containing multiple node fields
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
class JSAIMFNode extends MFNode
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
    JSAIMFNode(VRMLNodeType n, int index) {
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
     * Replace the value of this array with the given values
     *
     * @param values The new values to use
     */
    public void setValue(BaseNode[] values) {
        super.setValue(values);
        updateCoreData();
    }

    /**
     * Replace the value of this array with a subsection of the given values.
     *
     * @param size The number of elements to copy
     * @param values The new values to use
     */
    public void setValue(int size, BaseNode[] values) {
        super.setValue(size, values);
        updateCoreData();
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param value The field to copy data from
     */
    public void setValue(MFNode value) {
        super.setValue(value);
        updateCoreData();
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param value The field to copy data from
     */
    public void setValue(ConstMFNode value) {
        super.setValue(value);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, BaseNode f) {
        super.set1Value(index, f);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, ConstSFNode f) {
        super.set1Value(index, f);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, SFNode f) {
        super.set1Value(index, f);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(BaseNode f) {
        super.addValue(f);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(ConstSFNode f) {
        super.addValue(f);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(SFNode f) {
        super.addValue(f);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, BaseNode f) {
        super.insertValue(index, f);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, ConstSFNode f) {
        super.insertValue(index, f);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, SFNode f) {
        super.insertValue(index, f);
        updateCoreData();
    }

    /**
     * Clear the field of all elements.
     */
    public void clear() {
        numElements = 0;
        data = new BaseNode[0];
        updateCoreData();
    }

    /**
     * Remove the element at the given position and shift any other items
     * down.
     *
     * @param index The position to delete the item at
     */
    public void delete(int index) {
        updateLocalData();
        super.delete(index);
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
        return new JSAIMFNode(node, fieldIndex);
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

            if(fd == null)
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

    /**
     * Update the core with the local field data after a change.
     */
    private void updateCoreData() {
        try {
            VRMLNodeType ret[] = new VRMLNodeType[numElements];

            for(int i = 0; i < numElements; i++)
                ret[i] = data[i].getImplNode();

            node.setValue(fieldIndex, ret, numElements);
        } catch(FieldException fe) {
        }
    }
}
