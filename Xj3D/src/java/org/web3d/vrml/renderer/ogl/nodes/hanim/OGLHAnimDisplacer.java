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

package org.web3d.vrml.renderer.ogl.nodes.hanim;

// External imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.hanim.BaseHAnimDisplacer;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * OpenGL renderer implementation of a HAnimDisplacer node.
 * <p>
 *
 * This node is purely informational within the scenegraph. It does not have
 * a renderable representation.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class OGLHAnimDisplacer extends BaseHAnimDisplacer
    implements OGLVRMLNode {

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public OGLHAnimDisplacer() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLHAnimDisplacer(VRMLNodeType node) {
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
