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

package org.web3d.vrml.renderer.ogl.nodes.enveffects;

// External imports
import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.enveffects.BaseFog;

/**
 * OGL implementation of a fog node
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class OGLFog extends BaseFog implements OGLVRMLNode {

    /** A simple object to represent the background's place in the scene graph*/
    private Group implGroup;

    /**
     * Default constructor for a OGLFog
     */
    public OGLFog() {
        super();

        implGroup = new Group();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     * <P>
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type.
     */
    public OGLFog(VRMLNodeType node) {
        super(node);

        implGroup = new Group();
    }

    /**
     * Get the OGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The null representation.
     */
     public SceneGraphObject getSceneGraphObject() {
         return implGroup;
     }
}
