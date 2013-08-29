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

package org.web3d.vrml.scripting.jsai;

// External imports
// none

// Local imports
import vrml.*;
import vrml.field.*;
import vrml.node.Node;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Factory class for generating fields from a given node.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class JSAIFieldFactory implements FieldFactory {

    /**
     * Create a field given a name from the node.
     *
     * @param node The node to create the field from
     * @param name The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventIn
     * @throws InvalidExposedFieldException The field is not an exposedField
     */
    public Field createField(VRMLNodeType node,
                             String name,
                             boolean checkEventIn) {

        int index = node.getFieldIndex(name);

        if(index == -1) {
            if(checkEventIn)
                throw new InvalidEventInException(name);
            else
                throw new InvalidExposedFieldException(name);
        }

        // Should check the VRML97 capabilities here
        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);

        Field ret_val = null;

        switch(decl.getFieldType())
        {
            case FieldConstants.SFBOOL:
                ret_val = new JSAISFBool(node, index);
                break;
            case FieldConstants.SFCOLOR:
                ret_val = new JSAISFColor(node, index);
                break;
            case FieldConstants.SFFLOAT:
                ret_val = new JSAISFFloat(node, index);
                break;
            case FieldConstants.SFIMAGE:
                ret_val = new JSAISFImage(node, index);
                break;
            case FieldConstants.SFINT32:
                ret_val = new JSAISFInt32(node, index);
                break;
            case FieldConstants.SFNODE:
                ret_val = new JSAISFNode(node, index);
                break;
            case FieldConstants.SFROTATION:
                ret_val = new JSAISFRotation(node, index);
                break;
            case FieldConstants.SFSTRING:
                ret_val = new JSAISFString(node, index);
                break;
            case FieldConstants.SFTIME:
                ret_val = new JSAISFTime(node, index);
                break;
            case FieldConstants.SFVEC2F:
                ret_val = new JSAISFVec2f(node, index);
                break;
            case FieldConstants.SFVEC3F:
                ret_val = new JSAISFVec3f(node, index);
                break;
            case FieldConstants.MFCOLOR:
                ret_val = new JSAIMFColor(node, index);
                break;
            case FieldConstants.MFFLOAT:
                ret_val = new JSAIMFFloat(node, index);
                break;
            case FieldConstants.MFINT32:
                ret_val = new JSAIMFInt32(node, index);
                break;
            case FieldConstants.MFNODE:
                ret_val = new JSAIMFNode(node, index);
                break;
            case FieldConstants.MFROTATION:
                ret_val = new JSAIMFRotation(node, index);
                break;
            case FieldConstants.MFSTRING:
                ret_val = new JSAIMFString(node, index);
                break;
            case FieldConstants.MFTIME:
                ret_val = new JSAIMFTime(node, index);
                break;
            case FieldConstants.MFVEC2F:
                ret_val = new JSAIMFVec2f(node, index);
                break;
            case FieldConstants.MFVEC3F:
                ret_val = new JSAIMFVec3f(node, index);
                break;
        }

        return ret_val;
    }

    /**
     * Create a constant field that represents an eventOut.
     *
     * @param node The node to create the field from
     * @param fieldName The name of the field to fetch
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventOut
     */
    public ConstField createConstField(VRMLNodeType node, String fieldName) {

        int index = node.getFieldIndex(fieldName);

        if(index == -1)
            throw new InvalidExposedFieldException(fieldName);

        // Should check the VRML97 capabilities here
        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);

        // Note that this table leaves out the VRML200x types as a VRML97
        // script should never be looking for these anyway.
        ConstField ret_val = null;

        switch(decl.getFieldType())
        {
            case FieldConstants.SFBOOL:
                ret_val = new JSAIConstSFBool(node, index);
                break;
            case FieldConstants.SFCOLOR:
                ret_val = new JSAIConstSFColor(node, index);
                break;
            case FieldConstants.SFFLOAT:
                ret_val = new JSAIConstSFFloat(node, index);
                break;
            case FieldConstants.SFIMAGE:
                ret_val = new JSAIConstSFImage(node, index);
                break;
            case FieldConstants.SFINT32:
                ret_val = new JSAIConstSFInt32(node, index);
                break;
            case FieldConstants.SFNODE:
                ret_val = new JSAIConstSFNode(node, index);
                break;
            case FieldConstants.SFROTATION:
                ret_val = new JSAIConstSFRotation(node, index);
                break;
            case FieldConstants.SFSTRING:
                ret_val = new JSAIConstSFString(node, index);
                break;
            case FieldConstants.SFTIME:
                ret_val = new JSAIConstSFTime(node, index);
                break;
            case FieldConstants.SFVEC2F:
                ret_val = new JSAIConstSFVec2f(node, index);
                break;
            case FieldConstants.SFVEC3F:
                ret_val = new JSAIConstSFVec3f(node, index);
                break;
            case FieldConstants.MFCOLOR:
                ret_val = new JSAIConstMFColor(node, index);
                break;
            case FieldConstants.MFFLOAT:
                ret_val = new JSAIConstMFFloat(node, index);
                break;
            case FieldConstants.MFINT32:
                ret_val = new JSAIConstMFInt32(node, index);
                break;
            case FieldConstants.MFNODE:
                ret_val = new JSAIConstMFNode(node, index);
                break;
            case FieldConstants.MFROTATION:
                ret_val = new JSAIConstMFRotation(node, index);
                break;
            case FieldConstants.MFSTRING:
                ret_val = new JSAIConstMFString(node, index);
                break;
            case FieldConstants.MFTIME:
                ret_val = new JSAIConstMFTime(node, index);
                break;
            case FieldConstants.MFVEC2F:
                ret_val = new JSAIConstMFVec2f(node, index);
                break;
            case FieldConstants.MFVEC3F:
                ret_val = new JSAIConstMFVec3f(node, index);
                break;
        }

        return ret_val;
    }
}
