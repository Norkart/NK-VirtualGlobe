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

package org.web3d.vrml.renderer.ogl.nodes.scripting;

// Standard imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.scripting.BaseScript;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * Scene graph representation of a script node.
 * <p>
 *
 * The script is different to all the other nodes. While it represents
 * a script, it doesn't have the normal content of a Java3D node. It is
 * also a bit different to the ordinary Abstract node implementation in
 * that a script can have fields added and removed on demand.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class OGLScript extends BaseScript
    implements OGLVRMLNode {

    /**
     * Construct a default instance of the script
     */
    public OGLScript() {
        super();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLScript(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
