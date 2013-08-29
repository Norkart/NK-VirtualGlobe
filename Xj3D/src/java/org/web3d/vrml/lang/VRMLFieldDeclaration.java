/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.lang;

// External imports
import java.util.HashMap;

// Local imports
// none

/**
 * Representation of any field definition in the VRML system.
 * <p>
 * Each node contains a collection of fields. Each field has a fixed index for
 * all instances of this node.
 * <p>
 * This class represents the declaration of a field rather than the complete
 * field instance. For this reason it is immutable. You should not be allowed
 * to change items of a field declaration on the fly.
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 * @see org.web3d.vrml.lang.FieldConstants
 */
public class VRMLFieldDeclaration {

    /**
     * Definition of a field type that we don't know yet because it came from
     * an unresolved IMPORT statement.
     */
    public static final String UNKNOWN_IMPORT = "UnknownImportedField";

    /** Map of field type strings (key) to constant value Integer (value) */
    private static final HashMap fieldTypeMap;

    /** The name of the field. */
    private String name;

    /** The access type of the field */
    private int access;

    /** The type of the field (eg MFNode, SFInt32) */
    private int type;

    /** The string type of the field (eg MFNode, SFInt32) */
    private String typeString;

    /**
     * Static initialiser creates the set of name string to field type
     * mappings used by all instances.
     */
    static {
        fieldTypeMap = new HashMap();

        fieldTypeMap.put("SFInt32", new Integer(FieldConstants.SFINT32));
        fieldTypeMap.put("MFInt32", new Integer(FieldConstants.MFINT32));
        fieldTypeMap.put("SFFloat", new Integer(FieldConstants.SFFLOAT));
        fieldTypeMap.put("MFFloat", new Integer(FieldConstants.MFFLOAT));
        fieldTypeMap.put("SFDouble", new Integer(FieldConstants.SFDOUBLE));
        fieldTypeMap.put("MFDouble", new Integer(FieldConstants.MFDOUBLE));
        fieldTypeMap.put("SFLong", new Integer(FieldConstants.SFLONG));
        fieldTypeMap.put("MFLong", new Integer(FieldConstants.MFLONG));
        fieldTypeMap.put("SFBool", new Integer(FieldConstants.SFBOOL));
        fieldTypeMap.put("MFBool", new Integer(FieldConstants.MFBOOL));
        fieldTypeMap.put("SFVec2f", new Integer(FieldConstants.SFVEC2F));
        fieldTypeMap.put("MFVec2f", new Integer(FieldConstants.MFVEC2F));
        fieldTypeMap.put("SFVec2d", new Integer(FieldConstants.SFVEC2D));
        fieldTypeMap.put("MFVec2d", new Integer(FieldConstants.MFVEC2D));
        fieldTypeMap.put("SFVec3f", new Integer(FieldConstants.SFVEC3F));
        fieldTypeMap.put("MFVec3f", new Integer(FieldConstants.MFVEC3F));
        fieldTypeMap.put("SFVec3d", new Integer(FieldConstants.SFVEC3D));
        fieldTypeMap.put("MFVec3d", new Integer(FieldConstants.MFVEC3D));
        fieldTypeMap.put("SFVec4f", new Integer(FieldConstants.SFVEC4F));
        fieldTypeMap.put("MFVec4f", new Integer(FieldConstants.MFVEC4F));
        fieldTypeMap.put("SFVec4d", new Integer(FieldConstants.SFVEC4D));
        fieldTypeMap.put("MFVec4d", new Integer(FieldConstants.MFVEC4D));
        fieldTypeMap.put("SFImage", new Integer(FieldConstants.SFIMAGE));
        fieldTypeMap.put("MFImage", new Integer(FieldConstants.MFIMAGE));
        fieldTypeMap.put("SFTime", new Integer(FieldConstants.SFTIME));
        fieldTypeMap.put("MFTime", new Integer(FieldConstants.MFTIME));
        fieldTypeMap.put("SFNode", new Integer(FieldConstants.SFNODE));
        fieldTypeMap.put("MFNode", new Integer(FieldConstants.MFNODE));
        fieldTypeMap.put("SFString", new Integer(FieldConstants.SFSTRING));
        fieldTypeMap.put("MFString", new Integer(FieldConstants.MFSTRING));
        fieldTypeMap.put("SFRotation", new Integer(FieldConstants.SFROTATION));
        fieldTypeMap.put("MFRotation", new Integer(FieldConstants.MFROTATION));
        fieldTypeMap.put("SFColor", new Integer(FieldConstants.SFCOLOR));
        fieldTypeMap.put("MFColor", new Integer(FieldConstants.MFCOLOR));
        fieldTypeMap.put("SFColorRGBA", new Integer(FieldConstants.SFCOLORRGBA));
        fieldTypeMap.put("MFColorRGBA", new Integer(FieldConstants.MFCOLORRGBA));
        fieldTypeMap.put("SFMatrix3f", new Integer(FieldConstants.SFMATRIX3F));
        fieldTypeMap.put("MFMatrix3f", new Integer(FieldConstants.MFMATRIX3F));
        fieldTypeMap.put("SFMatrix3d", new Integer(FieldConstants.SFMATRIX3D));
        fieldTypeMap.put("MFMatrix3d", new Integer(FieldConstants.MFMATRIX3D));
        fieldTypeMap.put("SFMatrix4f", new Integer(FieldConstants.SFMATRIX4F));
        fieldTypeMap.put("MFMatrix4f", new Integer(FieldConstants.MFMATRIX4F));
        fieldTypeMap.put("SFMatrix4d", new Integer(FieldConstants.SFMATRIX4D));
        fieldTypeMap.put("MFMatrix4d", new Integer(FieldConstants.MFMATRIX4D));
        fieldTypeMap.put(UNKNOWN_IMPORT, new Integer(FieldConstants.UNKNOWN_IMPORT_TYPE));
    }

    /**
     * Create an instance of a field declaration. The string type is
     * automatically converted to the appropriate constant.
     *
     * @param accessType The type of access that this node has
     * @param type The type of the node (eg SFInt32)
     * @param name The name of this field
     * @see org.web3d.vrml.lang.FieldConstants
     * @throws InvalidFieldTypeException The type is not a recognised type in
     *    either X3D or VRML97.
     */
    public VRMLFieldDeclaration(int accessType, String type, String name)
        throws InvalidFieldTypeException {

        this.name = name;
        this.access = accessType;
        this.typeString = type;

        Integer tmp = (Integer)fieldTypeMap.get(type);

        if(tmp == null)
            throw new InvalidFieldTypeException("Unknown Type: " + type);

        this.type = tmp.intValue();
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Check for equivalence between this field declaration and another.
     * Makes sure the name, access type and data type are identical.
     *
     * @param o The object to compare against
     * @return true if these represent identical fields
     */
    public boolean equals(Object o) {
        if(!(o instanceof VRMLFieldDeclaration))
            return false;

        VRMLFieldDeclaration decl = (VRMLFieldDeclaration)o;

        return access == decl.access &&
               type == decl.type &&
               name.equals(decl.name);
    }

    /**
     * Create a pretty string version of this field. It will produce a string
     * of the form:
     * <pre>
     *   <i>access_type field_type field_name</i>
     * </pre>
     *
     * Assumes X3D field semantics. If you need VRML naming use
     * {@link #toString(boolean)}.
     *
     * @return A X3D string representation of this declaration
     */
    public String toString() {
        return toString(false);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the name of this field. This will be something like "children"
     * or "translation".
     *
     * @return The name of this field
     */
    public String getName() {
        return name;
    }

    /**
     * Get the access type of the field. Under VRML97 this will be one of
     * field, exposedField, eventIn or eventOut. For VRML 3.0, this will
     * always be exposedField.
     *
     * @return The access type of this node
     * @see org.web3d.vrml.lang.FieldConstants
     */
    public int getAccessType() {
        return access;
    }

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. A string is used to allow full extensibility.
     *
     * @return A constant describing the field type
     * @see org.web3d.vrml.lang.FieldConstants
     */
    public int getFieldType() {
        return type;
    }


    /**
     * Get the size of the raw element.  Ie an SFVec3f would be 3.  The lone
     * exception to this is the IMAGE types which will return 1.
     *
     * @return A constant describing the element size.
     */
    public int getFieldSize() {

        int size_mult;

        switch(type) {
            case FieldConstants.SFINT32:
            case FieldConstants.MFINT32:
            case FieldConstants.SFFLOAT:
            case FieldConstants.MFFLOAT:
            case FieldConstants.SFTIME:
            case FieldConstants.SFDOUBLE:
            case FieldConstants.MFTIME:
            case FieldConstants.MFDOUBLE:
            case FieldConstants.SFLONG:
            case FieldConstants.MFLONG:
            case FieldConstants.SFBOOL:
            case FieldConstants.MFBOOL:
            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
            case FieldConstants.MFSTRING:
            case FieldConstants.SFSTRING:
                size_mult = 1;
                break;

            case FieldConstants.SFIMAGE:
            case FieldConstants.MFIMAGE:
                size_mult = 1;
                break;

            case FieldConstants.SFVEC2F:
            case FieldConstants.MFVEC2F:
            case FieldConstants.MFVEC2D:
                size_mult = 2;
                break;

            case FieldConstants.SFVEC3F:
            case FieldConstants.SFCOLOR:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFCOLOR:
            case FieldConstants.SFVEC3D:
            case FieldConstants.MFVEC3D:
                size_mult = 3;
                break;

            case FieldConstants.SFROTATION:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.SFVEC4F:
            case FieldConstants.MFVEC4F:
            case FieldConstants.SFVEC4D:
            case FieldConstants.MFVEC4D:
                size_mult = 4;
                break;

            case FieldConstants.SFMATRIX3F:
            case FieldConstants.MFMATRIX3F:
            case FieldConstants.SFMATRIX3D:
            case FieldConstants.MFMATRIX3D:
                size_mult = 9;
                break;

            case FieldConstants.SFMATRIX4F:
            case FieldConstants.MFMATRIX4F:
            case FieldConstants.SFMATRIX4D:
            case FieldConstants.MFMATRIX4D:
                size_mult = 16;
                break;

            case FieldConstants.UNKNOWN_IMPORT_TYPE:
                size_mult = 0;
                break;

            default:
                System.out.println("Unhandled field type in VRMLFieldDeclaration.getFieldSize");
                size_mult = 1;
        }

        return size_mult;
    }

    /**
     * Get the field type. This string represents the field type such as
     * MFNode, SFInt32. A string is used to allow full extensibility.
     *
     * @return A string describing the field type
     * @see org.web3d.vrml.lang.FieldConstants
     */
    public String getFieldTypeString() {
        return typeString;
    }

    /**
     * Create a pretty string version of this field that modifies the field
     * type based on the version of the spec. It will produce a string
     * of the form
     * <pre>
     *   <i>access_type field_type field_name</i>
     * </pre>
     *
     * @param isVRML true if this should use the VRML terms, false for X3D
     * @return A string representation of this declaration
     */
    public String toString(boolean isVRML) {
        StringBuffer buf = new StringBuffer();

        buf.append(toAccessTypeString(access, isVRML));
        buf.append(typeString);
        buf.append(" ");
        buf.append(name);

        return buf.toString();
    }

    /**
     * Convert a field access type into a string representation in the
     * appropriate specification version
     *
     * @param type The access type constant
     * @param isVRML true if this should use the VRML terms, false for X3D
     * @return The string representation of it
     */
    public static String toAccessTypeString(int type, boolean isVRML) {
        String ret_val = null;

        if(isVRML) {
            switch(type) {
                case FieldConstants.EVENTIN:
                    ret_val = "eventIn ";
                    break;
                case FieldConstants.EVENTOUT:
                    ret_val = "eventOut ";
                    break;
                case FieldConstants.FIELD:
                    ret_val = "field ";
                    break;
                case FieldConstants.EXPOSEDFIELD:
                    ret_val = "exposedField ";
                    break;

                case FieldConstants.UNKNOWN_IMPORT_ACCESS:
                    ret_val = "invalidVRMLType ";
            }
        } else {
            switch(type) {
                case FieldConstants.EVENTIN:
                    ret_val = "inputOnly ";
                    break;
                case FieldConstants.EVENTOUT:
                    ret_val = "outputOnly ";
                    break;
                case FieldConstants.FIELD:
                    ret_val = "initializeOnly ";
                    break;
                case FieldConstants.EXPOSEDFIELD:
                    ret_val = "inputOutput ";
                    break;

                case FieldConstants.UNKNOWN_IMPORT_ACCESS:
                    ret_val = "unknownFromIMPORT ";
            }
        }

        return ret_val;
    }
}
