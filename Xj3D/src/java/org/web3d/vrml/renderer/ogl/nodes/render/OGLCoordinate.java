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

package org.web3d.vrml.renderer.ogl.nodes.render;

// Standard imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.render.BaseCoordinate;

/**
 * @author Rick Goldberg
 * @version $Revision: 1.2 $
 */
public class OGLCoordinate extends BaseCoordinate implements OGLVRMLNode {

    /**
     * Default constructor for a OGLCoordinate
     */
    public OGLCoordinate() {
    }

    /**
     * Copy constructor for a OGLColorRGBA
     *
     * @param node The node to copy the data from
     */
    public OGLCoordinate(VRMLNodeType node) {
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
