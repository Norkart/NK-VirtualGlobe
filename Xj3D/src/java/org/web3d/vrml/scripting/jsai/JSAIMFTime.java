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
import vrml.field.SFTime;
import vrml.field.MFTime;
import vrml.field.ConstMFTime;
import vrml.field.ConstSFTime;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * VRML type class containing multiple rotation fields
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
class JSAIMFTime extends MFTime
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
    JSAIMFTime(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Copy the values from this field into the user array
     *
     * @param times The target array to copy into
     */
    public void getValue(double[] times) {
        updateLocalData();
        super.getValue(times);
    }

    /**
     * Get the value of the field at the given index
     *
     * @param index The position to get the value from
     * @return The value at that index
     */
    public double get1Value(int index) {
        updateLocalData();
        return super.get1Value(index);
    }

    /**
     * Replace the value of this array with the given values
     *
     * @param times The new values to use
     */
    public void setValue(double[] times) {
        super.setValue(times);
        updateCoreData();
    }

    /**
     * Replace the value of this array with a subsection of the given values.
     *
     * @param size The number of elements to copy
     * @param times The new values to use
     */
    public void setValue(int size, double[] times) {
        super.setValue(times);
        updateCoreData();
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param times The field to copy data from
     */
    public void setValue(MFTime times) {
        super.setValue(times);
        updateCoreData();
    }

    /**
     * Set the value of this field based on the value of the given field.
     *
     * @param times The field to copy data from
     */
    public void setValue(ConstMFTime times) {
        super.setValue(times);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param time The new value to use
     */
    public void set1Value(int index, double time) {
        super.set1Value(index, time);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param time The new value to use
     */
    public void set1Value(int index, ConstSFTime time) {
        super.set1Value(index, time);
        updateCoreData();
    }

    /**
     * Replace one value in the array with this value.
     *
     * @param index The index to replace
     * @param time The new value to use
     */

    public void set1Value(int index, SFTime time) {
        super.set1Value(index, time);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param time The new value to add
     */
    public void addValue(double time) {
        super.addValue(time);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param time The new value to add
     */
    public void addValue(ConstSFTime time) {
        super.addValue(time);
        updateCoreData();
    }

    /**
     * Add this value to the end of the list.
     *
     * @param time The new value to add
     */
    public void addValue(SFTime time) {
        super.addValue(time);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param time The new value to insert
     */
    public void insertValue(int index, double time) {
        super.insertValue(index, time);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param time The new value to insert
     */
    public void insertValue(int index, ConstSFTime time) {
        super.insertValue(index, time);
        updateCoreData();
    }

    /**
     * Insert a value at the given index into the array
     *
     * @param index The index to replace
     * @param time The new value to insert
     */
    public void insertValue(int index, SFTime time) {
        super.insertValue(index, time);
        updateCoreData();
    }

    /**
     * Clear the field of all elements
     */
    public void clear() {
System.out.println("JSAIMFTime clear not implemented yet");
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
        return new JSAIMFTime(node, fieldIndex);
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
                data = new double[0];
            } else
                super.setValue(fd.numElements, fd.doubleArrayValue);

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
