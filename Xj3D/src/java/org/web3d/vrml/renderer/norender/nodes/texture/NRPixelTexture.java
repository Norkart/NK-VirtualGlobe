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
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRTextureNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.texture.BasePixelTexture;

/**
 * no-render implementation of a PixelTexture node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class NRPixelTexture extends BasePixelTexture
    implements NRVRMLNode, NRTextureNodeType {

    /**
     * Default constructor
     */
    public NRPixelTexture() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRPixelTexture(VRMLNodeType node) {
        super(node);
    }


    /**
     * Process the image data now. Does nothing for this implementation as
     * we don't have to render the texture.
     */
    protected void processImageData() {
    }
}
