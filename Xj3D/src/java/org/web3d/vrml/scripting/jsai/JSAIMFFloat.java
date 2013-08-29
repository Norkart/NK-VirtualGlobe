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
import vrml.field.ConstMFFloat;
import vrml.field.ConstSFFloat;
import vrml.field.MFFloat;
import vrml.field.SFFloat;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * VRML type class containing multiple float fields
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
class JSAIMFFloat extends MFFloat
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
    JSAIMFFloat(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Copy the values from this field into the user array
     *
     * @param values The target array to copy into
     */
    public void getValue(float[] values) {
        updateLocalData();
        super.getValue(values);
    }

    /**
     * Get the value of the field at the given index
     *
     * @param index The position to get the value from
     * @return The value at that index
     */
    public float get1Value(int index) {
        updateLocalData();
        return super.get1Value(index);
    }

    /**
     * Replace the value of this array with the given values
     *
     * @param values The new values to use
     */
    public void setValue(float[] values) {
        super.setValue(values);
        updateCoreData();
    }

    /**
     * Replace the value of this array with a subsection of the given values.
     *
     * @param size The number of elements to copy
     * @param values The new values to use
     */
    public void setValue(int size, float[] values) {
        super.setValue(size, values);
        updateCoreData();
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param value The field to copy data from
     */
    public void setValue(MFFloat value) {
        super.setValue(value);
        updateCoreData();
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param value The field to copy data from
     */
    public void setValue(ConstMFFloat value) {
        super.setValue(value);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, float f) {
        super.set1Value(index, f);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, ConstSFFloat f) {
        super.set1Value(index, f);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param f The new value to use
     */
    public void set1Value(int index, SFFloat f) {
        super.set1Value(index, f);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(float f) {
        super.addValue(f);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(ConstSFFloat f) {
        super.addValue(f);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param f The new value to add
     */
    public void addValue(SFFloat f) {
        super.addValue(f);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, float f) {
        super.insertValue(index, f);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, ConstSFFloat f) {
        super.insertValue(index, f);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param f The new value to insert
     */
    public void insertValue(int index, SFFloat f) {
        super.insertValue(index, f);
        updateCoreData();
    }

    /**
     * Clear the field of all elements
     */
    public void clear() {
System.out.println("JSAIMFFloat clear not implemented yet");
    }

    /**
     * Remove the element at the given position and shift any other items
     * down.
     *
     * @param index The position to delete the item at
     */
    public void delete(int index) {
        super.delete(index);
        updateCoreData();
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    public Object clone() {
        return new JSAIMFFloat(node, fieldIndex);
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
                data = new float[0];
            } else
                super.setValue(fd.numElements, fd.floatArrayValue);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Update the core with the local field data after a change.
     */
    private void updateCoreData() {
        try {
            node.setValue(fieldIndex, data, numElements);
        } catch(FieldException fe) {
        }
    }
}
