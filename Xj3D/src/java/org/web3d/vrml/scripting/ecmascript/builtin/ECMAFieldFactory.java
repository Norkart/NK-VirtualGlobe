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
package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
// none

// Local imports
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Factory class for generating fields from a given node.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class ECMAFieldFactory implements FieldFactory {

    /**
     * Create a field given a name from the node.
     *
     * @param node The node to create the field from
     * @param fieldName The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     */
    public Object createField(VRMLNodeType node,
                             String name,
                             boolean checkEventIn) {

        int index = node.getFieldIndex(name);

        if(index == -1) {
            return null;
        }

        // Should check the VRML97 capabilities here
        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);
        VRMLFieldData data = null;

        try {
            data = node.getFieldValue(index);
        } catch(FieldException fe) {
            // should never get to this position
            fe.printStackTrace();
        }

        Object ret_val = null;

        switch(decl.getFieldType())
        {
            case FieldConstants.SFBOOL:
                ret_val = data.booleanValue ? Boolean.TRUE : Boolean.FALSE;
                break;
            case FieldConstants.SFCOLOR:
                ret_val = new SFColor(data.floatArrayValue);
                break;
            case FieldConstants.SFCOLORRGBA:
                ret_val = new SFColorRGBA(data.floatArrayValue);
                break;
            case FieldConstants.SFFLOAT:
                ret_val = new Double(data.floatValue);
                break;
            case FieldConstants.SFDOUBLE:
                ret_val = new Double(data.doubleValue);
                break;
            case FieldConstants.SFIMAGE:
                ret_val = new SFImage(data.intArrayValue, data.numElements);
                break;
            case FieldConstants.SFINT32:
                ret_val = new Integer(data.intValue);
                break;
            case FieldConstants.SFNODE:
                ret_val = new SFNode(node,
                                     index,
                                     (VRMLNodeType)data.nodeValue);
                break;
            case FieldConstants.SFROTATION:
                ret_val = new SFRotation(data.floatArrayValue);
                break;
            case FieldConstants.SFSTRING:
                ret_val = data.stringValue;
                break;
            case FieldConstants.SFTIME:
                ret_val = new Double(data.doubleValue);
                break;
            case FieldConstants.SFVEC2F:
                ret_val = new SFVec2f(data.floatArrayValue);
                break;
            case FieldConstants.SFVEC3F:
                ret_val = new SFVec3f(data.floatArrayValue);
                break;
            case FieldConstants.SFVEC2D:
                ret_val = new SFVec2d(data.doubleArrayValue);
                break;
            case FieldConstants.SFVEC3D:
                ret_val = new SFVec3d(data.doubleArrayValue);
                break;
            case FieldConstants.MFBOOL:
                ret_val = new MFBool(data.booleanArrayValue,
                                     data.numElements);
                break;
            case FieldConstants.MFCOLOR:
                ret_val = new MFColor(data.floatArrayValue,
                                      data.numElements * 3);
                break;
            case FieldConstants.MFCOLORRGBA:
                ret_val = new MFColorRGBA(data.floatArrayValue,
                                      data.numElements * 4);
                break;
            case FieldConstants.MFFLOAT:
                ret_val = new MFFloat(data.floatArrayValue,
                                      data.numElements);
                break;
            case FieldConstants.MFDOUBLE:
                ret_val = new MFDouble(data.doubleArrayValue,
                                      data.numElements);
                break;
            case FieldConstants.MFINT32:
                ret_val = new MFInt32(data.intArrayValue,
                                      data.numElements);
                break;
            case FieldConstants.MFIMAGE:
                ret_val = new MFImage(data.intArrayValue,
                                      data.numElements);
                break;
            case FieldConstants.MFNODE:
                ret_val = new MFNode(node,
                                     index,
                                     (VRMLNodeType[])data.nodeArrayValue,
                                     data.numElements);
                break;
            case FieldConstants.MFROTATION:
                ret_val = new MFRotation(data.floatArrayValue,
                                      data.numElements * 4);
                break;
            case FieldConstants.MFSTRING:
                ret_val = new MFString(data.stringArrayValue,
                                      data.numElements);
                break;
            case FieldConstants.MFTIME:
                ret_val = new MFTime(data.doubleArrayValue,
                                      data.numElements);
                break;
            case FieldConstants.MFVEC2F:
                ret_val = new MFVec2f(data.floatArrayValue,
                                      data.numElements * 2);
                break;
            case FieldConstants.MFVEC3F:
                ret_val = new MFVec3f(data.floatArrayValue,
                                      data.numElements * 3);
                break;
            case FieldConstants.MFVEC2D:
                ret_val = new MFVec2d(data.doubleArrayValue,
                                      data.numElements * 2);
                break;
            case FieldConstants.MFVEC3D:
                ret_val = new MFVec3d(data.doubleArrayValue,
                                      data.numElements * 3);
                break;
        }

        if(checkEventIn &&
           (ret_val instanceof FieldScriptableObject) &&
           (decl.getAccessType() == FieldConstants.EVENTIN)) {

           ((FieldScriptableObject)ret_val).setReadOnly();
        }

        return ret_val;
    }

    /**
     * Update a field given a name from the node.  Will return an updated
     * field or a new object as needed.
     *
     * @param field The field to update
     * @param node The node to create the field from
     * @param fieldName The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     */
    public Object updateField(Object field,
                             VRMLNodeType node,
                             String name,
                             boolean checkEventIn) {

        int index = node.getFieldIndex(name);

        if(index == -1) {
System.out.println("Bah humbug");
            return null;
        }

        // Should check the VRML97 capabilities here
        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);
        VRMLFieldData data = null;

        try {
            data = node.getFieldValue(index);
        } catch(FieldException fe) {
            // should never get to this position
            fe.printStackTrace();
        }

        // Default the return value to the value they handed us. Only overwrite if
        // needed by the update process.
        Object ret_val = field;

        switch(decl.getFieldType())
        {
            case FieldConstants.SFBOOL:
                ret_val = data.booleanValue ? Boolean.TRUE : Boolean.FALSE;
                break;
            case FieldConstants.SFCOLOR:
                ((SFColor)field).setRawData(data.floatArrayValue);
                break;
            case FieldConstants.SFCOLORRGBA:
                ((SFColorRGBA)field).setRawData(data.floatArrayValue);
                break;
            case FieldConstants.SFFLOAT:
                if(((Number)field).floatValue() != data.floatValue)
                    ret_val = new Double(data.floatValue);
                break;
            case FieldConstants.SFIMAGE:
                ret_val = new SFImage(data.intArrayValue, data.numElements);
                break;
            case FieldConstants.SFINT32:
                if(((Number)field).intValue() != data.intValue)
                    ret_val = new Integer(data.intValue);
                break;
            case FieldConstants.SFNODE:
                SFNode n = (SFNode) field;
                if (n.getImplNode() == data.nodeValue)
                    return field;
                else
                    ret_val = new SFNode(node,
                                         index,
                                         (VRMLNodeType)data.nodeValue);
                break;
            case FieldConstants.SFROTATION:
                ((SFRotation)field).setRawData(data.floatArrayValue);
                break;
            case FieldConstants.SFSTRING:
                ret_val = data.stringValue;
                break;
            case FieldConstants.SFTIME:
                if(((Number)field).doubleValue() != data.doubleValue)
                    ret_val = new Double(data.doubleValue);
                break;
            case FieldConstants.SFVEC2F:
                ((SFVec2f)field).setRawData(data.floatArrayValue);
                break;
            case FieldConstants.SFVEC3F:
                ((SFVec3f)field).setRawData(data.floatArrayValue);
                break;
            case FieldConstants.SFVEC4F:
                ((SFVec4f)field).setRawData(data.floatArrayValue);
                break;
            case FieldConstants.SFVEC2D:
                ((SFVec2d)field).setRawData(data.doubleArrayValue);
                break;
            case FieldConstants.SFVEC3D:
                ((SFVec3d)field).setRawData(data.doubleArrayValue);
                break;
            case FieldConstants.SFVEC4D:
                ((SFVec4d)field).setRawData(data.doubleArrayValue);
                break;
            case FieldConstants.SFMATRIX3F:
            case FieldConstants.SFMATRIX4F:
            case FieldConstants.SFMATRIX3D:
            case FieldConstants.SFMATRIX4D:
                break;
            case FieldConstants.MFBOOL:
                ((MFBool)field).updateRawData(data.booleanArrayValue,
                                              data.numElements);
                break;
            case FieldConstants.MFCOLOR:
                ((MFColor)field).updateRawData(data.floatArrayValue,
                                               data.numElements * 3);
                break;
            case FieldConstants.MFCOLORRGBA:
                ((MFColorRGBA)field).updateRawData(data.floatArrayValue,
                                                   data.numElements * 4);
                break;
            case FieldConstants.MFDOUBLE:
                ((MFDouble)field).updateRawData(data.doubleArrayValue,
                                                data.numElements);
                break;
            case FieldConstants.MFFLOAT:
                ((MFFloat)field).updateRawData(data.floatArrayValue,
                                               data.numElements);
                break;
            case FieldConstants.MFIMAGE:
                ((MFImage)field).updateRawData(data.intArrayValue,
                                               data.numElements);
                break;
            case FieldConstants.MFINT32:
                ((MFInt32)field).updateRawData(data.intArrayValue,
                                               data.numElements);
                break;
            case FieldConstants.MFNODE:
                ((MFNode)field).updateRawData((VRMLNodeType[])data.nodeArrayValue,
                                              data.numElements);
                break;
            case FieldConstants.MFROTATION:
                ((MFRotation)field).updateRawData(data.floatArrayValue,
                                                  data.numElements * 4);
                break;
            case FieldConstants.MFSTRING:
                ((MFString)field).updateRawData(data.stringArrayValue,
                                                data.numElements);
                break;
            case FieldConstants.MFTIME:
                ((MFTime)field).updateRawData(data.doubleArrayValue,
                                              data.numElements);
                break;
            case FieldConstants.MFVEC2F:
                ((MFVec2f)field).updateRawData(data.floatArrayValue,
                                               data.numElements * 2);
                break;
            case FieldConstants.MFVEC3F:
                ((MFVec3f)field).updateRawData(data.floatArrayValue,
                                               data.numElements * 3);
                break;
            case FieldConstants.MFVEC4F:
                ((MFVec4f)field).updateRawData(data.floatArrayValue,
                                               data.numElements * 4);
                break;
            case FieldConstants.MFVEC2D:
                ((MFVec2d)field).updateRawData(data.doubleArrayValue,
                                               data.numElements * 2);
                break;
            case FieldConstants.MFVEC3D:
                ((MFVec3d)field).updateRawData(data.doubleArrayValue,
                                               data.numElements * 3);
                break;
            case FieldConstants.MFVEC4D:
                ((MFVec4d)field).updateRawData(data.doubleArrayValue,
                                               data.numElements * 4);
                break;
            case FieldConstants.MFMATRIX3F:
            case FieldConstants.MFMATRIX4F:
            case FieldConstants.MFMATRIX3D:
            case FieldConstants.MFMATRIX4D:
                break;
        }

        if(checkEventIn &&
           (ret_val instanceof FieldScriptableObject) &&
           (decl.getAccessType() == FieldConstants.EVENTIN)) {

           ((FieldScriptableObject)ret_val).setReadOnly();
        }

        return ret_val;
    }
}
