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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
// None

// Application specific imports
import org.web3d.util.ObjectArray;

/**
 * Marker for a class capable of providing scene graph path information about
 * the node and its parent.
 * <p>
 *
 * This interface is used by classes that are monitoring the structure of the
 * scene graph for changes and can provide useful SceneGraphPath information
 * for the leaf nodes that need it. The idea is to provide an efficient
 * mechanism for doing a reverse walk from the leaf node to the root of the
 * scene graph without needing to always provide direct parent pointers. It
 * is expected that at least the parentPathChanged method will be called every
 * frame so that requires a very efficient test implementation.
 * <p>
 *
 * During a query run, the current node will ask the parent through this
 * interface, if the path has changed. Based on the VRML/X3D spec, we can make
 * a number of assumptions - there is only one "path" as such to the root of
 * the scene graph. This path can be arbitrary, all we need to know is that it
 * is constant each time through, unless a parent is removed. If the parent is
 * removed, (such as being deleted from a tree) then the path has changed. Due
 * to the DEF/USE nature of the shared scene graph, there may be another path
 * to the root of the tree, so the idea is that this will allow the code to
 * readjust to the new path. The spec says that if you do DEF/USE a node,
 * particularly a bindable node, then the results are ambiguous, that's what
 * this system allows us to deal with semi reasonably. Thus, when discovering
 * the root path we don't care what the actual path is, just that there is one
 * and that when it changes, that is detected.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface J3DParentPathRequestHandler {

    /**
     * Check to see if the parent path to the root of the scene graph has
     * changed in structure and the scene graph path needs to be regenerated.
     * This is a query only and if this level has not changed then the parent
     * level above should be automatically requested until the root of the
     * live scene graph is reached.
     *
     * @return true if this or a parent of this path has changed
     */
    public boolean hasParentPathChanged();

    /**
     * Fetch the scene graph path from the root of the world to this node.
     * If this node's SceneGraphObject is represented by a SharedGroup, then
     * the last item in the given path will be the Link node that is attached
     * to this object. If this node cannot find the root of the scene graph
     * or the child is not a registered child of this node, return null.
     * <p>
     * The path array will have the first element as the root locale, and the
     * the children will be all the link nodes, in order from the root to this
     * level.
     *
     * @param requestingChild A reference to the child that's making the request
     * @return The list of locales and nodes in the path down to this node or null
     */
    public ObjectArray getParentPath(J3DVRMLNode requestingChild);
}
