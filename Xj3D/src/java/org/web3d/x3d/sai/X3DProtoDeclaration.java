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
 * The representation of a PROTO declaration.
 * <p>
 *
 * This is the representation of the declaration, not of a runtime node. For
 * this reason you cannot access the internals, nor can you work with the
 * individual field values. You can, however, perform basic introspection
 * tasks such as looking at the available field definitions and seeing the
 * basic node type.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface X3DProtoDeclaration  {

    /**
     * Get the type of this node. The string returned should be the name of
     * the VRML node or the name of the proto instance this node represents.
     *
     * @return The type of this node.
     * @exception InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public String getProtoName()
        throws InvalidNodeException;

    /**
     * Fetch the type of this proto. The proto's type is defined by the first
     * child node in the body, in accordance with the X3D specification.
     * <p>
     * The types values are provided in the array of values. There is no
     * specific order of the returned types. It is expected that most node
     * types, which only descend from a single parent type would return an
     * array of length 1.
     *
     * @return The primary type(s) of this node
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public int[] getNodeType()
        throws InvalidNodeException;

    /**
     * Get the list of fields that this node contains. This will return one
     * definition for each field regardless of whether it is eventIn/Out,
     * exposedField or field access type.
     *
     * @return The definitions for all fields of this node
     * @exception InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public X3DFieldDefinition[] getFieldDefinitions()
        throws InvalidNodeException;

    /**
     * Create an instance of this proto that may be used at runtime.
     *
     * @return An instance of this proto to work with
     * @exception InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public X3DProtoInstance createInstance()
        throws InvalidNodeException;

    /**
     * Dispose of this node's resources. This is used to indicate to the
     * browser that the java side of the application does not require the
     * resources represented by this node. The browser is now free to do
     * what it likes with the node.
     * <P>
     * This in no way implies that the browser is to remove this node from
     * the scene graph, only that the java code is no longer interested
     * in this particular node through this reference.
     * <P>
     * Once this method has been called, any further calls to methods of
     * this instance of the class is shall generate an InvalidNodeException.
     *
     * @throws InvalidNodeException The node is no longer valid and can't be
     *    disposed of again.
     */
    public void dispose()
        throws InvalidNodeException;
}
