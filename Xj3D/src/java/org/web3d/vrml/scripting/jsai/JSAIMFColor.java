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
import vrml.field.SFColor;
import vrml.field.MFColor;
import vrml.field.ConstMFColor;
import vrml.field.ConstSFColor;

import org.web3d.util.ArrayUtils;
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
class JSAIMFColor extends MFColor
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
    JSAIMFColor(VRMLNodeType n, int index) {
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
        ArrayUtils.raise3(data, numElements * 3, colors);
    }

    /**
     * Returns the value of this field as a flat array.
     *
     * @param colors Color triplicates flattened
     */
    public void getValue(float[] colors) {
        updateLocalData();
        System.arraycopy(data, 0, colors, 0, numElements * 3);
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

        int offset = index * 3;

        colors[0] = data[0];
        colors[1] = data[1];
        colors[2] = data[2];
    }

    /**
     * Copy one value of the array into the user provided field.
     *
     * @param index The position in the field to get
     * @param color The field to copy values to
     */
    public void get1Value(int index, SFColor color) {
        updateLocalData();
        int offset = index * 3;
        color.setValue(data[offset], data[offset + 1], data[offset + 2]);
    }

    /**
     * Set the field to the new values.
     *
     * @param colors The new color values to use
     */
    public void setValue(float[][] colors) {
        super.setValue(colors);
        updateCoreData();
    }

    /**
     * Set the field to the new values.
     *
     * @param colors The new color values to use
     */
    public void setValue(float[] colors) {
        super.setValue(colors);
        updateCoreData();
    }

    /**
     * Set the value of this field given limited array of colors.
     * 0-2 = Color1, 3-5 = Color2...
     *
     * @param size The number of colors is size / 3.
     * @param colors Color triplicates flattened.
     */
    public void setValue(int size, float[] colors) {
        super.setValue(size, colors);
        updateCoreData();
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param colors The field to copy from
     */
    public void setValue(MFColor colors) {
        super.setValue(colors);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param color The field to copy the data from
     */
    public void set1Value(int index, ConstSFColor color) {
        super.set1Value(index, color);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param color The field to copy the data from
     */
    public void set1Value(int index, SFColor color) {
        super.set1Value(index, color);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the given color components.
     *
     * @param index The index of the element to set
     * @param red The red component to use
     * @param green The green component to use
     * @param blue The blue component to use
     */
    public void set1Value(int index, float red, float green, float blue) {
        super.set1Value(index, red, green, blue);
        updateCoreData();
    }

    /**
     * Append the field as a new color value to the end of this field.
     *
     * @param color The field to append
     */
    public void addValue(ConstSFColor color) {
        super.addValue(color);
        updateCoreData();
    }

    /**
     * Append the field as a new color value to the end of this field.
     *
     * @param color The field to append
     */
    public void addValue(SFColor color) {
        super.addValue(color);
        updateCoreData();
    }

    /**
     * Append the components as a new color value to the end of this field.
     *
     * @param red The red component to use
     * @param green The green component to use
     * @param blue The blue component to use
     */
    public void addValue(float red, float green, float blue) {
        super.addValue(red, green, blue);
        updateCoreData();
    }

    /**
     * Insert the color represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param color The color field to insert
     */
    public void insertValue(int index, ConstSFColor color) {
        super.insertValue(index, color);
        updateCoreData();
    }

    /**
     * Insert the color represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param color The color field to insert
     */
    public void insertValue(int index, SFColor color) {
        super.insertValue(index, color);
        updateCoreData();
    }

    /**
     * Insert the color represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param red The red component to use
     * @param green The green component to use
     * @param blue The blue component to use
     */
    public void insertValue(int index, float red, float green, float blue) {
        super.insertValue(index, red, green, blue);
        updateCoreData();
    }

    /**
     * Clear the field of all elements
     */
    public void clear() {
System.out.println("JSAIMFColor clear not implemented yet");
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
        return new JSAIMFColor(node, fieldIndex);
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
                super.setValue(fd.numElements * 3, fd.floatArrayValue);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Update the core with the local field data after a change.
     */
    private void updateCoreData() {
        try {
            node.setValue(fieldIndex, data, numElements * 3);
        } catch(FieldException fe) {
        }
    }
}
