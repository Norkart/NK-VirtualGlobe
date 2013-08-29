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
package org.web3d.vrml.scripting.ecmascript.builtin;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Factory abstract interface for generating field objects from a given node.
 * <p>
 *
 * The idea of this factory is to break a circular compile dependency
 * between SF/MFNode and all the field classes.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface FieldFactory {

    /**
     * Create a field given a name from the node.
     *
     * @param node The node to create the field from
     * @param field The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventIn
     * @throws InvalidExposedFieldException The field is not an exposedField
     */
    Object createField(VRMLNodeType node, String field, boolean checkEventIn);

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
                             boolean checkEventIn);
}
