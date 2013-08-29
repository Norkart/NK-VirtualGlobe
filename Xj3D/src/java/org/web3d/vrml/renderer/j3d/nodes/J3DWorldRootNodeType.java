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
// none

// Application specific imports
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;

/**
 * An abstract representation of the root node of a world in Java3D.
 * <p>
 *
 * Extends the basic root node functionality with Java3D specific capabilities
 * - namely the ability to get the root node BranchGroup instance. This is the
 * object returned by the <code>getSceneGraphObject()</code> method.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface J3DWorldRootNodeType
    extends VRMLWorldRootNodeType, J3DVRMLNode {

    /**
     * Set the scene that is contained by this node, which happens to be an
     * execution space. The scene instance must be non-null.
     *
     * @param scene The scene to use
     */
    public void setContainedScene(BasicScene scene);
}
