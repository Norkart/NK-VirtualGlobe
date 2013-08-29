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

package org.web3d.vrml.renderer.norender.nodes.particle;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.particle.BaseExplosionEmitter;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of a ExplosionEmitter node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class NRExplosionEmitter extends BaseExplosionEmitter
    implements NRVRMLNode {

    /**
     * Construct a new time sensor object
     */
    public NRExplosionEmitter() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRExplosionEmitter(VRMLNodeType node) {
        super(node);
    }
}
