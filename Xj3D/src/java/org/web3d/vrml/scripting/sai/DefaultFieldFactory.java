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

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;

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
 * @version $Revision: 1.7 $
 */
public class DefaultFieldFactory implements FieldFactory {

    /**
     * Create a field given a name from the node.
     *
     * @param node The node to create the field from
     * @param name The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @param internal true if this represents an internal field definition
     * @param fieldQueue The access listener for propogating s2 requests
     * @param baseNodeFac The factory used to create node wrappers
     * @return An instance of the field class representing the field or null
     * @throws InvalidEventInException The field is not an eventIn
     * @throws InvalidExposedFieldException The field is not an exposedField
     */
    public BaseField createField(VRMLNodeType node,
                                 String name,
                                 boolean checkEventIn,
                                 boolean internal,
                                 ReferenceQueue fieldQueue,
                                 BaseNodeFactory baseNodeFac) {

        int index = node.getFieldIndex(name);

        if(index == -1)
            return null;

        // Should check the X3D capabilities here
        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);
        VRMLFieldData data = null;

        BaseField ret_val = null;

        switch(decl.getFieldType()) {
            case FieldConstants.SFBOOL:
                ret_val = new SAISFBool(node, index, internal);
                break;
            case FieldConstants.SFCOLOR:
                ret_val = new SAISFColor(node, index, internal);
                break;
            case FieldConstants.SFCOLORRGBA:
                ret_val = new SAISFColorRGBA(node, index, internal);
                break;
            case FieldConstants.SFFLOAT:
                ret_val = new SAISFFloat(node, index, internal);
                break;
            case FieldConstants.SFDOUBLE:
                ret_val = new SAISFDouble(node, index, internal);
                break;
            case FieldConstants.SFIMAGE:
                ret_val = new SAISFImage(node, index, internal);
                break;
            case FieldConstants.SFINT32:
                ret_val = new SAISFInt32(node, index, internal);
                break;
            case FieldConstants.SFNODE:
                ret_val = new SAISFNode(node, index, internal);
                ((SAISFNode)ret_val).setFieldFactory(this);
                ((SAISFNode)ret_val).setFieldReferenceQueue(fieldQueue);
                ((SAISFNode)ret_val).setNodeFactory(baseNodeFac);
                break;
            case FieldConstants.SFROTATION:
                ret_val = new SAISFRotation(node, index, internal);
                break;
            case FieldConstants.SFSTRING:
                ret_val = new SAISFString(node, index, internal);
                break;
            case FieldConstants.SFTIME:
                ret_val = new SAISFTime(node, index, internal);
                break;
            case FieldConstants.SFVEC2F:
                ret_val = new SAISFVec2f(node, index, internal);
                break;
            case FieldConstants.SFVEC3F:
                ret_val = new SAISFVec3f(node, index, internal);
                break;
            case FieldConstants.SFVEC2D:
                ret_val = new SAISFVec2d(node, index, internal);
                break;
            case FieldConstants.SFVEC3D:
                ret_val = new SAISFVec3d(node, index, internal);
                break;
            case FieldConstants.MFBOOL:
                ret_val = new SAIMFBool(node, index, internal);
                break;
            case FieldConstants.MFCOLOR:
                ret_val = new SAIMFColor(node, index, internal);
                break;
            case FieldConstants.MFCOLORRGBA:
                ret_val = new SAIMFColorRGBA(node, index, internal);
                break;
            case FieldConstants.MFFLOAT:
                ret_val = new SAIMFFloat(node, index, internal);
                break;
            case FieldConstants.MFDOUBLE:
                ret_val = new SAIMFDouble(node, index, internal);
                break;
            case FieldConstants.MFINT32:
                ret_val = new SAIMFInt32(node, index, internal);
                break;
            case FieldConstants.MFIMAGE:
                ret_val = new SAIMFImage(node, index, internal);
                break;
            case FieldConstants.MFNODE:
                ret_val = new SAIMFNode(node, index, internal);
                ((SAIMFNode)ret_val).setFieldFactory(this);
                ((SAIMFNode)ret_val).setFieldReferenceQueue(fieldQueue);
                ((SAIMFNode)ret_val).setNodeFactory(baseNodeFac);
                break;
            case FieldConstants.MFROTATION:
                ret_val = new SAIMFRotation(node, index, internal);
                break;
            case FieldConstants.MFSTRING:
                ret_val = new SAIMFString(node, index, internal);
                break;
            case FieldConstants.MFTIME:
                ret_val = new SAIMFTime(node, index, internal);
                break;
            case FieldConstants.MFVEC2F:
                ret_val = new SAIMFVec2f(node, index, internal);
                break;
            case FieldConstants.MFVEC3F:
                ret_val = new SAIMFVec3f(node, index, internal);
                break;
            case FieldConstants.MFVEC2D:
                ret_val = new SAIMFVec2d(node, index, internal);
                break;
            case FieldConstants.MFVEC3D:
                ret_val = new SAIMFVec3d(node, index, internal);
                break;
        }

        if(checkEventIn &&
           (internal && decl.getAccessType() == FieldConstants.EVENTIN) ||
           (!internal && decl.getAccessType() == FieldConstants.EVENTOUT))
           ret_val.setReadOnly();

        return ret_val;
    }
}
