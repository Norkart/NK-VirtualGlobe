/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.nodes;

// External imports
import java.util.Set;

// Local imports
import org.web3d.vrml.lang.VRMLNodeTemplate;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.InvalidFieldFormatException;

/**
 * Prototype declaration within a file.
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 *
 * @see VRMLExternProtoDeclare
 * @see VRMLProtoInstance
 */
public interface VRMLProtoDeclare extends VRMLNodeTemplate {

    /**
     * Get the grouping node that represents the body of the proto. This is
     * implicitly created by the proto but does not represent the abstract type
     * of the proto that would allow it to be used in a scenegraph.
     *
     * @return A grouping node suitable for adding content to during parsing
     */
    public VRMLGroupingNodeType getBodyGroup();

    /**
     * Get the set of the current route declarations that are available for
     * this definitions.
     *
     * @return The set of {@link org.web3d.vrml.lang.ROUTE ROUTEs} registered
     */
    public Set getRouteDecls();

    /**
     * Get the value of a field. The return value will either be a String or
     * String[] representing the raw value of the field. The prototype
     * declaration does not bother attempting to parse and validate the
     * field values, so we only deal with raw items here.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException;


    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, long value)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, long[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException;

    /**
     * Used to set the field value where the field represents a SFNode or
     * MFNode. The behavior of this call is dependent on whether the field is
     * single or multiple.
     *
     * @param index The index of destination field to set
     * @param node The node to set or add to this field
     * @throws InvalidFieldValueException This field does not take a node
     * @throws InvalidFieldException The field index is not known
     */
    public void addFieldNodeValue(int index, VRMLNodeType node)
        throws InvalidFieldException, InvalidFieldValueException;

}
