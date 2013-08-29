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
package org.web3d.vrml.nodes;

// Standard imports
import java.util.List;

// Application specific imports
import org.web3d.vrml.lang.VRMLExecutionSpace;

/**
 * A instance of a Prototype.
 * <p>
 * Can be placed anywhere a regular VRML node can. This representation allows
 * access at the root node that is the first of the proto.
 * <p>
 * This proto may represent a prototype that comes from an external source. It
 * is possible that when the implementation asks for a node it may still be
 * loading it's contents. In this case, the value returned is null. It is an
 * open question as to how we deal with notification that the node content has
 * been loaded.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.9 $
 */
public interface VRMLProtoInstance extends VRMLNodeType, VRMLExecutionSpace {

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
     * Get the first node declared in the proto as that defines just how we
     * we can add this into the scenegraph. If this is an empty prototype
     * implementation, or represents an Extern proto that has not been loaded
     * yet then this will return null.
     *
     * @return The node instance that represents the first node
     */
    public VRMLNodeType getImplementationNode();

    /**
     * Get the list of all the body nodes in this proto instance. Nodes are
     * defined in declaration order. Index 0 is always the same value as
     * that returned by {@link #getImplementationNode()}. This should be called
     * sparingly. It is really only of use to something that needs to traverse
     * the entire scene graph or for scripting to provide access to the root of
     * the scene.
     *
     * @return The list of nodes from the body
     */
    public VRMLNodeType[] getBodyNodes();
}
