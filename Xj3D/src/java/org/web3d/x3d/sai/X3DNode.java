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
 * The base representation of any VRML node in the system whether built in or
 * a proto.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface X3DNode {

    /**
     * Set the Metadata object that belongs to this node. If the object
     * instance is null, then it clears the currently set node instance.
     *
     * @param node The new node instance to use
     */
    public void setMetadata(X3DMetadataObject node);

    /**
     * Get the metadata object associated with this node. If none is set, it
     * will return null.
     *
     * @return The metadata object instance or null
     */
    public X3DMetadataObject getMetadata();

    /**
     * Get the type of this node. The string returned should be the name of
     * the VRML node or the name of the proto instance this node represents.
     *
     * @return The type of this node.
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public String getNodeName()
      throws InvalidNodeException;

    /**
     * Get the list of fields that this node contains. This will return one
     * definition for each field regardless of whether it is eventIn/Out,
     * exposedField or field access type.
     *
     * @return The definitions for all fields of this node
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public X3DFieldDefinition[] getFieldDefinitions()
        throws InvalidNodeException;

    /**
     * Notify this node that its setup stage is now complete. This will cause
     * all its fields to become non-writable, leaving only eventIns and
     * exposedFields writable. A user is not required to call this method as
     * it will be implicitly called immediately this node is added to any
     * other node. Any call after the first is ignored.
     *
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public void realize()
        throws InvalidNodeException;

    /**
     * Check to see if this node has completed its setup either by being
     * directly informed of it or through implicit measures (see the
     * specification for details).
     *
     * @return true if this node has completed the setup stage, false otherwise
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public boolean isRealized()
        throws InvalidNodeException;

    /**
     * Get the basic categorisation type(s) of this node. The types values are
     * provided in the array of values. There is no specific order of the
     * returned types. It is expected that most node types, which only descend
     * from a single parent type would return an array of length 1.
     * The returned value(s) should be the most derived type applicable for
     * that node. For example, a Material node should return MaterialNodeType
     * value, not AppearanceChildNodeType value.
     *
     * @return The primary type(s) of this node
     * @throws InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public int[] getNodeType()
        throws InvalidNodeException;

    /**
     * Get a field for this node.
     * <P>
     * If the basic field required is an exposedField you can use either the
     * standard name (such as <i>translation</i>) or you can use the <i>set_</i>
     * / <i>_changed</i> modifier (such as <i>set_translation</i>). If the field
     * asked for is of field access type then an object is returned that cannot
     * be read or written to. However, this allows the option for building
     * editor type applications that may permit reading and writing of field
     * access types when not running the VRML event model.
     *
     * @param name The name of the field that is required
     * @return A reference to the field requested.
     * @throws InvalidFieldException The named field does not exist for
     *   this node.
     * @throws InvalidNodeException The node has had it's resources disposed
     *   of
     */
    public X3DField getField(String name)
        throws InvalidFieldException, InvalidNodeException;

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
