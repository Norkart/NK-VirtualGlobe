/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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

// External imports
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.Texture;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseMultiTexture;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureNodeType;

/**
 * OpenGL implementation of a MultiTexture node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class OGLMultiTexture extends BaseMultiTexture
    implements OGLTextureNodeType {

    /**
     * Empty constructor.
     */
    public OGLMultiTexture() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLMultiTexture(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by OGLTextureNodeType
    //----------------------------------------------------------

    /**
     * Set the Aviatrix3D texture representation back into the node
     * implementation.
     *
     * @param index The index of the texture (for multitexture)
     * @param tex The texture object to set
     */
    public void setTexture(int index, Texture tex) {
        VRMLNodeType node = (VRMLNodeType)vfTexture.get(index);

        // JC: Note that this does not correctly handle a texture object
        // wrapped in a proto.
        if(node instanceof OGLTextureNodeType) {
            OGLTextureNodeType o_tex = (OGLTextureNodeType)node;
            o_tex.setTexture(0, tex);
        }
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
