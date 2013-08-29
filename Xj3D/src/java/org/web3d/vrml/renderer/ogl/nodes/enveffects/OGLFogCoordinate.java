/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.enveffects.BaseFogCoordinate;

/**
 * OGL implementation of an FogCoordinate node.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class OGLFogCoordinate extends BaseFogCoordinate implements OGLVRMLNode {

    /**
     * Default constructor for a OGLFogCoordinate
     */
    public OGLFogCoordinate() {
    }

    /**
     * Copy constructor for a OGLFogCoordinate
     *
     * @param node The node to copy the data from
     */
    public OGLFogCoordinate(VRMLNodeType node) {
        super(node);
    }

    /**
     * Get the OGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The null representation.
     */
     public SceneGraphObject getSceneGraphObject() {
         return null;
     }
}
