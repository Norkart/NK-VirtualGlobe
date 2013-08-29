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
import vrml.field.SFVec2f;
import vrml.field.MFVec2f;
import vrml.field.ConstMFVec2f;
import vrml.field.ConstSFVec2f;

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
class JSAIMFVec2f extends MFVec2f
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
    JSAIMFVec2f(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param vec2s The target array to copy values into
     */
    public void getValue(float[][] vec2s) {
        updateLocalData();
        super.getValue(vec2s);
    }

    /**
     * Copy the value of this field into the given flat array.
     *
     * @param vec2s The target array to copy values into
     */
    public void getValue(float[] vec2s) {
        updateLocalData();
        super.getValue(vec2s);
    }

    /**
     * Copy the value of the vector at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param vecs The array to copy the vector value to
     */
    public void get1Value(int index, float[] vec2s) {
        updateLocalData();
        super.get1Value(index, vec2s);
    }

    /**
     * Copy the vector value at the given index into the supplied field.
     *
     * @param index The index in the array of values to read
     * @param vec The field to copy the vector value to
     */
    public void get1Value(int index, SFVec2f vec) {
        updateLocalData();
        super.get1Value(index, vec);
    }

    /**
     * Set the field to the new values.
     *
     * @param vecs The new vec values to use
     */
    public void setValue(float[][] vec2s) {
        super.setValue(vec2s);
        updateCoreData();
    }

    /**
     * Set the field to the new values.
     *
     * @param vecs The new vec values to use
     */
    public void setValue(float[] vec2s) {
        super.setValue(vec2s);
        updateCoreData();
    }

    /**
     * Set the value of this field given limited array of vecs.
     * x1, y1, z1, x2, y2, z2, ....
     *
     * @param size The number of vecs is size / 2.
     * @param vecs Color triplicates flattened.
     */
    public void setValue(int size, float[] vec2s) {
        super.setValue(size, vec2s);
        updateCoreData();
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param vecs The field to copy from
     */
    public void setValue(MFVec2f vecs) {
        super.setValue(vecs);
        updateCoreData();
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param vecs The field to copy from
     */
    public void setValue(ConstMFVec2f vecs) {
        super.setValue(vecs);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the given vec components.
     *
     * @param index The index of the element to set
     * @param x The x component to use
     * @param y The y component to use
     */
    public void set1Value(int index, float x, float y) {
        super.set1Value(index, x, y);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    public void set1Value(int index, ConstSFVec2f vec) {
        super.set1Value(index, vec);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    public void set1Value(int index, SFVec2f vec) {
        super.set1Value(index, vec);
        updateCoreData();
    }

    /**
     * Append the components as a new vector value to the end of this field.
     *
     * @param x The x component to use
     * @param y The y component to use
     */
    public void addValue(float x, float y) {
        super.addValue(x, y);
        updateCoreData();
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    public void addValue(ConstSFVec2f vec) {
        super.addValue(vec);
        updateCoreData();
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    public void addValue(SFVec2f vec) {
        super.addValue(vec);
        updateCoreData();
    }

    /**
     * Insert the vector represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param x The x component to use
     * @param y The y component to use
     */
    public void insertValue(int index, float x, float y) {
        super.insertValue(index, x, y);
        updateCoreData();
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    public void insertValue(int index, ConstSFVec2f vec) {
        super.insertValue(index, vec);
        updateCoreData();
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    public void insertValue(int index, SFVec2f vec) {
        super.insertValue(index, vec);
        updateCoreData();
    }

    /**
     * Clear the field of all elements
     */
    public void clear() {
System.out.println("JSAIMFVec2f clear not implemented yet");
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
        return new JSAIMFVec2f(node, fieldIndex);
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
                super.setValue(fd.numElements * 2, fd.floatArrayValue);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Update the core with the local field data after a change.
     */
    private void updateCoreData() {
        try {
            node.setValue(fieldIndex, data, numElements * 2);
        } catch(FieldException fe) {
        }
    }
}
