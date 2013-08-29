/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Representation of a node's field definition.
 * <p>
 *
 * The field definition holds the static field information such as the field
 * access type, the data type and the name of the field.
 * <p>
 * The implementation of the toString() method of this class shall return the
 * full IDL declaration of the field as per the specification, not the UTF8 or
 * XML format. Implementation of <code>.equals()</code> shall return true if
 * the two field definitions share the same access type, data type and name. It
 * shall not include the underlying field's values at that point in time.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface X3DFieldDefinition {

    /**
     * Get the name of this field. This will be something like "children"
     * or "translation". If the field is an exposed field then the name
     * give will be the base name without any <i>set_</i> or <i>_changed</i>
     * added to the name, regardless of how the initial field was fetched.
     *
     * @return The name of this field
     */
    public String getName();

    /**
     * Get the access type of the field. This will be one of field,
     * exposedField, eventIn or eventOut constants described in the
     * X3DFieldTypes interface.
     *
     * @return The access type of this node
     * @see X3DFieldTypes
     */
    public int getAccessType();

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. The defintion of the int values returned is
     * described in the X3DFieldType interface.
     *
     * @return A constant describing the field type
     * @see X3DFieldTypes
     */
    public int getFieldType();

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. A string is used to allow full extensibility.
     *
     * @return A string describing the field type
     */
    public String getFieldTypeString();
}
