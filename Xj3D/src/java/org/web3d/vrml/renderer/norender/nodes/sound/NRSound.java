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

package org.web3d.vrml.renderer.norender.nodes.sound;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.sound.BaseSound;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Implementation of a sound node for no renderer.
 * This node is used for the creation of PROTOs
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRSound extends BaseSound
    implements NRVRMLNode {

    /**
     * Default constructor.  Does nothing.
     */
    public NRSound() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a BaseSound node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRSound(VRMLNodeType node) {
        this();
        copy(node);
    }
}
