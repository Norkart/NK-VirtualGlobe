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
package org.web3d.vrml.lang;

// External imports
import java.util.List;

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * Denotes a node which allows it's fields to be defined in code - namely
 * scripts and protos.
 * <p>
 * This interface allows a node to add and remove nodes from it's definition.
 * The interface compliments an ordinary node type. The normal nodes will allow
 * you to set the values of a field, this allows you to change their definition
 * rather than set or query the values.
 * <p>
 * Due to the assumptions of the event model implementation of using index to
 * name a given field. Deleting a field just removes it from the index. An
 * index value cannot be reused by placing another field at that position.
 * Adding fields appends them to the list. If an API to this allows the
 * insertion of nodes then it must layer that capability over this node keeping
 * track of logical versus actual index lists.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public interface VRMLNodeTemplate {

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Create a new instance of a real node from this template. This will
     * ensure that all the internals are created as needed, based on the
     * current state of the node. Note that sometimes, creating an instance
     * of this template may result in an invalid node construction. Cases
     * where this could occur is when there's no node definition yet loaded
     * or that the loaded definition does not match this template.
     *
     * @param root The node that represents the root node of the
     *   VRMLExecutionSpace that we're in.
     * @param isStatic true if this is created within a StaticGroup
     * @return A new node instance from this template
     * @throws InvalidNodeTypeException The root node is not a node capable
     *    of representing a root of a scene graph
     * @see org.web3d.vrml.nodes.VRMLProtoInstance
     * @see org.web3d.vrml.nodes.VRMLWorldRootNodeType
     */
    public VRMLNode createNewInstance(VRMLNode root, boolean isStatic)
        throws InvalidNodeTypeException;

    /**
     * Get the name of this node as a string. The name is the name the template
     * would appear as in a VRML file that was requesting an instance of this
     * node.
     *
     * @return The name of the node
     */
    public String getVRMLNodeName();

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements and when trying to determine if the node
     * has been used in the right place. If it is unknown (eg not yet loaded
     * extern proto) then return -1.
     *
     * @return The primary type
     */
    public int getPrimaryType();

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName);

    /**
     * Get the number of fields defined for this node.
     *
     * @return The number of fields.
     */
    public int getNumFields();

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index);

    /**
     * Get the count of the number of fields currently registered.
     *
     * @return The number of fields available
     */
    public int getFieldCount();

    /**
     * Make a listing of all fields that are currently registered in this
     * node. The list contains instances of
     * {@link org.web3d.vrml.lang.VRMLFieldDeclaration}.
     *
     * @return A list of the current field declarations
     */
    public List getAllFields();

    /**
     * Append a field declaration to this node. This is added to the current
     * list on the end. If the field already exists with the given name but
     * different values exception will be generated. If the field has exactly
     * the same signature it will silently ignore the request.
     *
     * @param field The new field to add
     * @return The index that this field was added at
     * @throws FieldExistsException A conflicting field of the same name
     *   already exists for this node
     * @throws InvalidFieldException The field type is not valid for this
     *   specification version.
     */
    public int appendField(VRMLFieldDeclaration field)
        throws FieldExistsException, InvalidFieldException;

    /**
     * Delete the field at the given index. This will not shuffle fields down
     * from higher index values. That index just becomes invalid to set values
     * to. If no field exists at that index or it is out of range, an exception
     * will be generated.
     *
     * @param index The index of the field to delete
     * @throws InvalidFieldException The field does not exist at the index
     * @throws ArrayIndexOutOfBoundsException The index provided is out of
     *   range for the current field numbers
     */
    public void deleteField(int index)
        throws InvalidFieldException, ArrayIndexOutOfBoundsException;

    /**
     * Delete the named field. This will not shuffle fields down from higher
     * index values. That index just becomes invalid to set values to. If no
     * field exists at that index or it is out of range, an exception will be
     * generated.
     *
     * @param field The field to delete
     * @throws InvalidFieldException The named field does not exist
     * @throws ArrayIndexOutOfBoundsException The index provided is out of
     *   range for the current field numbers
     */
    public void deleteField(VRMLFieldDeclaration field)
        throws InvalidFieldException, ArrayIndexOutOfBoundsException;
}
