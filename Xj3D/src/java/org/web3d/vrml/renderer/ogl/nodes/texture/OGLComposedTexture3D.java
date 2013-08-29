/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
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
import java.util.*;

import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.Texture;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLSingleExternalNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLUrlListener;
import org.web3d.vrml.nodes.VRMLContentStateListener;
import org.web3d.vrml.renderer.common.nodes.texture.BaseComposedTexture3D;

import org.web3d.vrml.util.URLChecker;

/**
 * OpenGL implementation of a ComposedTexture3D node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLComposedTexture3D extends BaseComposedTexture3D
    implements OGLTextureNodeType {

    private static final String NOT_OGL_TEXTURE_MSG =
        "The node instance provided is not an instance of OGLTexture2DNodeType";

    /**
     * Default constructor generates node with default field values.
     */
    public OGLComposedTexture3D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLComposedTexture3D(VRMLNodeType node) {
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
        // Ignored for this class.
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
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
