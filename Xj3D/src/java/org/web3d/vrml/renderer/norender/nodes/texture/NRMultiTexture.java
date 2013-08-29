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

// Standard imports
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRTextureNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.texture.BaseMultiTexture;

/**
 * Null-renderer implementation of a MultiTexture node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRMultiTexture extends BaseMultiTexture
    implements NRVRMLNode, NRTextureNodeType {

    /**
     * Empty constructor
     */
    public NRMultiTexture() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRMultiTexture(VRMLNodeType node) {
        super(node);
    }
}
