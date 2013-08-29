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

package org.web3d.vrml.renderer.norender.nodes.texture;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRTextureNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.texture.BasePixelCubeMapTexture;

/**
 * Null-renderer implementation of a PixelCubeMapTexture node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRPixelCubeMapTexture extends BasePixelCubeMapTexture
    implements NRVRMLNode, NRTextureNodeType {

    /**
     * Construct a default instance of this node.
     */
    public NRPixelCubeMapTexture() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRPixelCubeMapTexture(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods defined by BasePixelCubeMapTexture
    //----------------------------------------------------------

    /**
     * The image data field has been updated, so time to process the image
     * right now. The field notifications have not been sent yet. If
     * overridden, This method should be called before the overridden code
     * is run.
     *
     * @param field The index of one of the six fields for the sides
     */
    protected void processImageData(int field) {
        // do nothing.
    }
}
