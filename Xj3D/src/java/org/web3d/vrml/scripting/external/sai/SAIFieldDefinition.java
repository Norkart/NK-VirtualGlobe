package org.web3d.vrml.scripting.external.sai;

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

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;

import org.xj3d.sai.X3DFieldTypeMapper;

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
 * @author Brad Vender (comments copied from X3DFieldDefinition)
 * @version $Revision: 1.5 $
 */
class SAIFieldDefinition implements X3DFieldDefinition {

    /** The field access type */
    int accessType;

    /** The field name */
    String fieldName;

    /** The field type (integer enumeration) */
    int fieldType;

    /** The string name of the field type */
    String fieldTypeString;

    /** Make a X3DFieldDefinition loading the values from the
      * VRMLFieldDeclaration data
      * @param node The underlying node
      * @param fieldID The field index to get data from
      */
    SAIFieldDefinition(VRMLNodeType node, int fieldID) {
        VRMLFieldDeclaration decl=node.getFieldDeclaration(fieldID);
        accessType=decl.getAccessType();
        fieldName=decl.getName();
        fieldType=X3DFieldTypeMapper.getInstance( ).getX3DFieldType( decl.getFieldType( ) );
        fieldTypeString=decl.getFieldTypeString();
    }

    /**
     * Make an X3DFieldDefinition from the field decl.
     *
     * @param decl The field decl.
     */
    SAIFieldDefinition(VRMLFieldDeclaration decl) {
        accessType=decl.getAccessType();
        fieldName=decl.getName();
        fieldType=X3DFieldTypeMapper.getInstance( ).getX3DFieldType( decl.getFieldType( ) );
        fieldTypeString=decl.getFieldTypeString();
    }

    /** @see java.lang.Object#equals.
     *  See class comments for behavior of this method.
     */
    public boolean equals(Object obj) {
        if (obj instanceof SAIFieldDefinition) {
            SAIFieldDefinition other=(SAIFieldDefinition)obj;
            return getName().equals(other.getName()) && getFieldType()==other.getFieldType() && getAccessType()==other.getAccessType();
        } else return super.equals(obj);
    }
    
    /**
     * Get the name of this field. This will be something like "children"
     * or "translation". If the field is an exposed field then the name
     * give will be the base name without any <i>set_</i> or <i>_changed</i>
     * added to the name, regardless of how the initial field was fetched.
     *
     * @return The name of this field
     */
    public String getName() {
        return fieldName;
    }

    /**
     * Get the access type of the field. This will be one of field,
     * exposedField, eventIn or eventOut constants described in the
     * X3DFieldTypes interface.
     *
     * @return The access type of this node
     * @see org.web3d.x3d.sai.X3DFieldTypes
     */
    public int getAccessType() {
        return accessType;
        /** At the moment, the access types in FieldConstants and
         *  X3DFieldTypes agree.  In the event of symbolic paranoia,
         *  replace the above one liner with the below code.
        switch (accessType) {
            case FieldConstants.EVENTIN:
                return X3DFieldTypes.INPUT_ONLY;
            case FieldConstants.FIELD:
                return X3DFieldTypes.INITIALIZE_ONLY;
            case FieldConstants.EVENTOUT:
                return X3DFieldTypes.INPUT_OUTPUT;
            default:
                throw new RuntimeException("Unknown field type "+accessType);
        }
        */
    }

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. The defintion of the int values returned is
     * described in the X3DFieldType interface.
     *
     * @return A constant describing the field type
     * @see org.web3d.x3d.sai.X3DFieldTypes
     */
    public int getFieldType() {
        return fieldType;
    }

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. A string is used to allow full extensibility.
     *
     * @return A string describing the field type
     */
    public String getFieldTypeString() {
        return fieldTypeString;
    }
    
    /** @see java.lang.Object#hashCode
     *  Overridden due to override on equals.
     */
    public int hashCode() {
        return getName().hashCode()+getAccessType();
    }
    
}
