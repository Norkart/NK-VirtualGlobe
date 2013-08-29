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

package org.web3d.vrml.renderer.norender.nodes.interpolator;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.interpolator.BaseCoordinateInterpolator2D;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Implementation of a CoordinateInterpolator2D.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRCoordinateInterpolator2D extends BaseCoordinateInterpolator2D
    implements NRVRMLNode {

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    public NRCoordinateInterpolator2D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not an interpolator node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRCoordinateInterpolator2D(VRMLNodeType node) {
        super(node);
    }
}
