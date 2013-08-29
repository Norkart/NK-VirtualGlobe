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
import vrml.field.ConstMFRotation;
import vrml.field.SFRotation;

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
class JSAIConstMFRotation extends ConstMFRotation
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
    JSAIConstMFRotation(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;

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
        return new JSAIConstMFRotation(node, fieldIndex);
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
                setValue(fd.numElements * 4, fd.floatArrayValue);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of this field given limited array of rots.
     * x1, y1, z1, x2, y2, z2, ....
     *
     * @param size The number of rots is size / 4.
     * @param rots Color triplicates flattened.
     */
    private void setValue(int size, float rots[]) {

        if(size > data.length)
            data = new float[size];

        numElements = size / 4;

        System.arraycopy(rots, 0, data, 0, size);
    }
}
