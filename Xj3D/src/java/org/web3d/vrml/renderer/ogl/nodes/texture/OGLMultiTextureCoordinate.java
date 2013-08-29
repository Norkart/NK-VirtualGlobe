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

package org.web3d.vrml.renderer.ogl.nodes.texture;

// Standard imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.texture.BaseMultiTextureCoordinate;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * OpenGL implementation of a multi texture coordinate.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class OGLMultiTextureCoordinate extends BaseMultiTextureCoordinate
    implements OGLVRMLNode {

    /**
     * Construct a new default instance of this class.
     */
    public OGLMultiTextureCoordinate() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLMultiTextureCoordinate(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the OGLVRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
