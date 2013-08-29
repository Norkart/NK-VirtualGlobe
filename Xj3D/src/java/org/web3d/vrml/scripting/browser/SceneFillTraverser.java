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

package org.web3d.vrml.scripting.browser;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.*;

/**
 * The SceneFillTraverser is responsible for taking a raw collection of
 * nodes and filling in the remaining scene details.
 * <P>
 * The method for accomplishing this is to be given a ReplaceWorldVRMLScene,
 * construct a SceneTraverser, and call the appropriate addX methods.
 *
 * @author Bradley Vender
 * @version $Revision: 1.2 $
 */
class SceneFillTraverser implements SceneGraphTraversalSimpleObserver {

    // The VRMLScene to process (file elements into)
    private ReplaceWorldVRMLScene scene;

    /**
     * Construct a SceneFillTraverser.  Does not initiate work.
     *
     * @param aScene The scene to add information to.
     */
    private SceneFillTraverser(ReplaceWorldVRMLScene aScene) {
        scene = aScene;
    }

    /**
     * Process an unfinished ReplaceWorldVRMLScene into a finished one.
     * This method fills in various fields in the ReplaceWorldVRMLScene, and
     * should never be called twice on the same scene.
     *
     * @param aScene The unfinished scene to process
     */
    static void processNodes(ReplaceWorldVRMLScene aScene) {
        SceneGraphTraverser traverser = new SceneGraphTraverser();
        SceneFillTraverser observer = new SceneFillTraverser(aScene);

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

