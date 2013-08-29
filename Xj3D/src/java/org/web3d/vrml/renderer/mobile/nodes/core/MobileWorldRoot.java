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

package org.web3d.vrml.renderer.mobile.nodes.core;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.core.BaseWorldRoot;
import org.web3d.vrml.renderer.mobile.nodes.MobileWorldRootNodeType;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.Group;
import org.web3d.vrml.renderer.mobile.sg.Node;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * Implementation of the world root class.
 * <p>
 *
 * Extends the basic root node functionality with Java3D specific capabilities
 * - namely the ability to get the root node BranchGroup instance. This is the
 * object returned by the <code>getSceneGraphObject()</code> method.
 * <p>
 * The world root has 3 fields available to use:
 * <ul>
 * <li>children</li>
 * <li>bboxSize</li>
 * <li>bboxCenter</li>
 * </ul>
 *
 * <b>Note:</b> This may want to extend BranchGroup instead.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.1 $
 */
public class MobileWorldRoot extends BaseWorldRoot
    implements MobileWorldRootNodeType {

    /** The renderable scenegraph node */
    private Group implGroup;

    /**
     * Construct an instance of this node.
     */
    public MobileWorldRoot() {
        implGroup = new Group();
    }

    //----------------------------------------------------------
    // Overrding base methods
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGroup;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        int size = vfChildren.size();

        if(size != 0) {
            for(int i = 0; i < size; i++) {
                MobileVRMLNode n = (MobileVRMLNode)vfChildren.get(i);

                n.setupFinished();

                Node ogl_node = (Node)n.getSceneGraphObject();

                if(ogl_node != null)
                    implGroup.addChild(ogl_node);
            }
        }
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate.
     *
     * @param node The node to view
     */
    protected void addChildNode(VRMLNodeType node) {

        if(node instanceof MobileVRMLNode) {
            super.addChildNode(node);
        } else
            throw new InvalidFieldValueException("Node is not a MobileVRMLNode");
    }
}
