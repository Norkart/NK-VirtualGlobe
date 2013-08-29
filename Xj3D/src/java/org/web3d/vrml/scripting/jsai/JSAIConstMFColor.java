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
import vrml.field.ConstMFColor;
import vrml.field.SFColor;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Xj3D Specific implementation of the MFColor field when extracted from part
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
 * @version $Revision: 1.8 $
 */
class JSAIConstMFColor extends ConstMFColor
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
    JSAIConstMFColor(VRMLNodeType n, int index) {
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
    public void getValue(float[][] colors) {
        updateLocalData();
        super.getValue(colors);
    }

    /**
     * Returns the value of this field as a flat array.
     *
     * @param colors Color triplicates flattened
     */
    public void getValue(float[] colors) {
        updateLocalData();
        super.getValue(colors);
    }

    /**
     * Copy one value of the values from this field into the user provided
     * array.
     *
     * @param index The position in the field to get
     * @param colors The array to copy values to
     */
    public void get1Value(int index, float[] colors) {
        updateLocalData();

        super.get1Value(index, colors);
    }

    /**
     * Copy one value of the array into the user provided field.
     *
     * @param index The position in the field to get
     * @param color The field to copy values to
     */
    public void get1Value(int index, SFColor color) {
        updateLocalData();
        super.get1Value(index, color);
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
        return new JSAIConstMFColor(node, fieldIndex);
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
                setValue(fd.numElements * 3, fd.floatArrayValue);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of this field given limited array of colors.
     * 0-2 = Color1, 3-5 = Color2...
     *
     * @param size The number of colors is size / 3.
     * @param colors Color triplicates flattened.
     */
    private void setValue(int size, float[] colors) {

        if(size > data.length)
            data = new float[size];

        numElements = size / 3;

        System.arraycopy(colors, 0, data, 0, size);
    }
}
