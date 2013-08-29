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
// none

// Application specific imports
// none

/**
 * A simplified version of the
 * {@link org.web3d.vrml.nodes.SceneGraphTraversalDetailObserver} that only provides
 * the basic parent-child information.
 * <p>
 *
 * The observer will report the top of a use hierarchy. If the traverser, in
 * it's internal references, detects a reference re-use that is indicative of
 * a DEF/USE situation then the flag passed with each method call will be set
 * true. After reporting the USE, the traverser will not descend that part of
 * the scene graph any further.
 * <p>
 *
 * When reporting the parent node, if the root is the root node of the VRML
 * file or the body of a Proto, the parent reference will be null - regardless
 * of the type of node.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface SceneGraphTraversalSimpleObserver {

    /**
     * Notification of a child node.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    public void observedNode(VRMLNodeType parent,
                             VRMLNodeType child,
                             int field,
                             boolean used);
}
