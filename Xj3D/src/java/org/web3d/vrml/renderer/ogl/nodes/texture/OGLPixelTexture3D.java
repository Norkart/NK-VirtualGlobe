/*****************************************************************************
 *                      Web3d.org Copyright (c) 2001 - 2007
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.Texture;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.texture.BasePixelTexture3D;

import org.web3d.vrml.renderer.ogl.nodes.OGLTextureNodeType;

/**
 * OpenGL implementation of a PixelTexture3D node.
 * <p>
 *
 * Given a MFInt32 this will produce a Texture3D object
 *
 * TODO:
 *      Needs more testing
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class OGLPixelTexture3D extends BasePixelTexture3D
    implements OGLTextureNodeType {

    /** Class vars for performance */
    private int texHeight;

    /** The height of the texture */
    private int texWidth;

	/** The AV3D Texture representation */
	private Texture texture;
	
    /**
     * Construct a default instance of this node.
     */
    public OGLPixelTexture3D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLPixelTexture3D(VRMLNodeType node) {
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
        texture = tex;
    }


    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Aviatrix3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The Aviatrix3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return texture;
    }
}
