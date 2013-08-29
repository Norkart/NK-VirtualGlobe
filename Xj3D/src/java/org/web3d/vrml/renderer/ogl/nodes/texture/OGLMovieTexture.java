/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2007
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
import org.j3d.aviatrix3d.*;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.texture.BaseMovieTexture;

import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureNodeType;

/**
 * OGL implementation of a MovieTexture node.
 * <p>
 *
 * The most optimal way for this to work would be creating ByteTextureComponent2D
 * and using updateSubImage updates.  For now this will just use the current
 * mechanisms for texture updates(fireTextureImageChanged) and recreate the
 * texture.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class OGLMovieTexture extends BaseMovieTexture
   implements OGLTextureNodeType {

    /** The array of listeners for isActive changes */
    private ArrayList textureListeners;

    //private BufferedImage nextFrame;
    private int textureWidth;
    private int textureHeight;

    /**
     * Constructors
     */
    public OGLMovieTexture() {
        textureListeners = new ArrayList();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLMovieTexture(VRMLNodeType node) {
        this();
        copy(node);
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

    //----------------------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    //----------------------------------------------------------------------
    // Methods defined by VideoStreamHandler
    //----------------------------------------------------------------------


    public void videoStreamFrame(NIOBufferImage image) {
        //Debug.trace();
        //nextFrame = scaleVideoFrame(image);
        //stateManager.addEndOfThisFrameListener(this);

        fireTextureImageChanged(0, this, image, (String)null);
    }

    public void videoStreamFormat(int width, int height) {
    }

    public void videoStreamStart()
    {
        //Debug.trace();
        super.videoStreamStart();
    }

    public void videoStreamStop()
    {
        //Debug.trace();
        super.videoStreamStop();
    }

    //----------------------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. If the node needs to update itself for this
     * frame, it should do so now before the render pass takes place.
     */
    public void allEventsComplete() {
    }
}

