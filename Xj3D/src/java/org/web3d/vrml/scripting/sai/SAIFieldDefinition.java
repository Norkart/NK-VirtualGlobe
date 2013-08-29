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

package org.web3d.vrml.scripting.sai;

// External imports
// None

// Local imports
import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;

import org.xj3d.sai.X3DFieldTypeMapper;

/**
 * Implementation of a node's field definition.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SAIFieldDefinition implements X3DFieldDefinition {

    /** The name of the field */
    private final String name;

    /** The access type the field uses */
    private final int accessType;

    /** The data type of the field */
    private final int dataType;

    /** The data type of the field */
    private final String typeName;

    /**
     * Construct a new field definition based on the internal Xj3D data type
     * from the FieldConstants class. Designed to be directly compatible with
     * VRMLFieldDeclaration.
     *
     * @param name The name of the field
     * @param access The access type identifier
     * @param type The data type identifier
     */
    SAIFieldDefinition(String name, int access, int type) {
        this.name = name;

        switch(access) {
            case FieldConstants.EVENTIN:
                accessType = X3DFieldTypes.INPUT_ONLY;
                break;

            case FieldConstants.EVENTOUT:
                accessType = X3DFieldTypes.OUTPUT_ONLY;
                break;

            case FieldConstants.EXPOSEDFIELD:
                accessType = X3DFieldTypes.INPUT_OUTPUT;
                break;

            case FieldConstants.FIELD:
                accessType = X3DFieldTypes.INITIALIZE_ONLY;
                break;

            default:
                accessType = -1;
        }

        X3DFieldTypeMapper typeMapper = X3DFieldTypeMapper.getInstance( );
        
        dataType=typeMapper.getX3DFieldType( type );
        typeName=typeMapper.getTypeName( type );
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
        return name;
    }

    /**
     * Get the access type of the field. This will be one of field,
     * exposedField, eventIn or eventOut constants described in the
     * X3DFieldTypes interface.
     *
     * @return The access type of this node
     * @see X3DFieldTypes
     */
    public int getAccessType() {
        return accessType;
    }

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. The defintion of the int values returned is
     * described in the X3DFieldType interface.
     *
     * @return A constant describing the field type
     * @see X3DFieldTypes
     */
    public int getFieldType() {
        return dataType;
    }

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. A string is used to allow full extensibility.
     *
     * @return A string describing the field type
     */
    public String getFieldTypeString() {
        return( typeName );
    }

    /**
     * Check this object for comparison to another
     *
     * @param obj The object to compare it to
     * @return true if these represent exactly the same field type
     */
    public boolean equals(Object obj) {
        if(!(obj instanceof SAIFieldDefinition))
            return false;

        SAIFieldDefinition o = (SAIFieldDefinition)obj;

        return name.equals(o.name) &&
               o.dataType == dataType &&
               o.accessType == accessType;
    }
}
