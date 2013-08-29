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
import org.web3d.vrml.renderer.common.nodes.texture.BaseTextureCoordinate3D;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * OpenGL implementation of a TextureCoordinate3D node.
 * <p>
 *
 * The texture coordinate node does not occupy a space in the Aviatrix3D
 * scenegraph as it is part of the GeometryArray class. This is used as
 * a VRML construct only. When VRML changes the values here, we pass them
 * back courtesy of the listeners to the children nodes.
 * <p>
 * Points are held internally as a flat array of values. The point list
 * returned will always be flat. We do this because Java3D takes point values
 * into the geometry classes as a single flat array. The array returned will
 * always contain exactly the number of points specified.
 * <p>
 * The effect of this is that point values may be routed out of this node as
 * a flat array of points rather than a 2D array. Receiving nodes should check
 * for this version as well. This implementation will handle being routed
 * either form.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class OGLTextureCoordinate3D extends BaseTextureCoordinate3D
    implements OGLVRMLNode {

    /**
     * Empty constructor
     */
    public OGLTextureCoordinate3D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLTextureCoordinate3D(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the OGLVRMLNode interface.
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
