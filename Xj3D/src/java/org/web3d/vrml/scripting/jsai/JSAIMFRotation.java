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
import vrml.field.SFRotation;
import vrml.field.MFRotation;
import vrml.field.ConstMFRotation;
import vrml.field.ConstSFRotation;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * VRML type class containing multiple rotation fields
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class JSAIMFRotation extends MFRotation
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
    JSAIMFRotation(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param rots The target array to copy values into
     */
    public void getValue(float[] rotations) {
        updateLocalData();
        super.getValue(rotations);
    }

    /**
     * Copy the value of the rotation at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param rots The array to copy the rotation value to
     */
    public void get1Value(int index, float rots[]) {
        updateLocalData();
        super.get1Value(index, rots);
    }

    /**
     * Copy the value of the rotation at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param rotations The array to copy the rotation value to
     */
    public void get1Value(int index, SFRotation rotation) {
        updateLocalData();
        super.get1Value(index, rotation);
    }

    /**
     * Set the field to the new values.
     *
     * @param rotations The new rot values to use
     */
    public void setValue(float[][] rotations) {
        super.setValue(rotations);
        updateCoreData();
    }

    /**
     * Set the field to the new values.
     *
     * @param rotations The new rot values to use
     */
    public void setValue(float[] rotations) {
        super.setValue(rotations);
        updateCoreData();
    }

    /**
     * Set the field to the new values.
     *
     * @param size The number of rots is size / 4.
     * @param rotations The new rot values to use
     */
    public void setValue(int size, float[] rotations) {
        super.setValue(size, rotations);
        updateCoreData();
    }

    /**
     * Set the field to the new values.
     *
     * @param rots The new rot values to use
     */
    public void setValue(MFRotation rots) {
        super.setValue(rots);
        updateCoreData();
    }

    /**
     * Set the field to the new values.
     *
     * @param rots The new rot values to use
     */
    public void setValue(ConstMFRotation rots) {
        super.setValue(rots);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param rot The field to copy the data from
     */
    public void set1Value(int index, ConstSFRotation rot) {
        super.set1Value(index, rot);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the given rot components.
     *
     * @param index The index of the element to set
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     * @param a The angle component
     */
    public void set1Value(int index, float x, float y, float z, float a) {
        super.set1Value(index, x, y, z, a);
        updateCoreData();
    }

    /**
     * Append the field as a new rot value to the end of this field.
     *
     * @param rot The field to append
     */
    public void addValue(ConstSFRotation rot) {
        super.addValue(rot);
        updateCoreData();
    }

    /**
     * Append the field as a new rot value to the end of this field.
     *
     * @param rot The field to append
     */
    public void addValue(SFRotation rot) {
        super.addValue(rot);
        updateCoreData();
    }

    /**
     * Append the components as a new rotation value to the end of this field.
     *
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     * @param a The angle component
     */
    public void addValue(float x, float y, float z, float a) {
        super.addValue(x, y, z, a);
        updateCoreData();
    }

    /**
     * Insert the rotation represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param rot The rot field to insert
     */
    public void insertValue(int index, ConstSFRotation rot) {
        super.insertValue(index, rot);
        updateCoreData();
    }

    /**
     * Insert the rotation represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param rot The rot field to insert
     */
    public void insertValue(int index, SFRotation rot) {
        super.insertValue(index, rot);
        updateCoreData();
    }

    /**
     * Insert the rotation represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     * @param a The angle component
     */
    public void insertValue(int index, float x, float y, float z, float a) {
        super.insertValue(index, x, y, z, a);
        updateCoreData();
    }

    /**
     * Clear the field of all elements
     */
    public void clear() {
System.out.println("JSAIMFRotation clear not implemented yet");
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
        return new JSAIMFRotation(node, fieldIndex);
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
                super.setValue(fd.numElements * 4, fd.floatArrayValue);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Update the core with the local field data after a change.
     */
    private void updateCoreData() {
        try {
            node.setValue(fieldIndex, data, numElements * 4);
        } catch(FieldException fe) {
        }
    }
}
