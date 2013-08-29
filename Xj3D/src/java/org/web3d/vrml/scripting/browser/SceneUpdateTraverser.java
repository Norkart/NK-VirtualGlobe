/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.browser;

// External imports
// none

// Local imports
import org.web3d.vrml.lang.AbstractScene;
import org.web3d.vrml.nodes.*;

/**
 * The traverser is responsible walking through an existing scene and checking
 * to make sure all the nodes are included in the scene details.
 * <P>
 * This class is used when an X3D replaceWorld() call is made with a scene.
 * That scene may not have all the details listed due to the user modification
 * (or they just created the entire scene from scratch).
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class SceneUpdateTraverser implements SceneGraphTraversalSimpleObserver {

    /** The VRMLScene to process (file elements into) */
    private AbstractScene scene;

    /**
     * Construct a SceneFillTraverser.  Does not initiate work.
     *
     * @param aScene The scene to add information to.
     */
    private SceneUpdateTraverser(AbstractScene aScene) {
        scene = aScene;
    }

    /**
     * Process an unfinished ReplaceWorldVRMLScene into a finished one.
     * This method fills in various fields in the ReplaceWorldVRMLScene, and
     * should never be called twice on the same scene.
     *
     * @param aScene The unfinished scene to process
     */
    static void updateScene(AbstractScene aScene) {
        SceneGraphTraverser traverser = new SceneGraphTraverser();
        SceneUpdateTraverser observer = new SceneUpdateTraverser(aScene);

        traverser.setObserver(observer);
        traverser.traverseGraph(aScene.getRootNode());
    }

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
                             boolean used) {
        if(!used)
            scene.addNode(child);
    }
}

