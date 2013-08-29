/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.geospatial;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoMetadata;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * NoRender implementation of an GeoMetadata
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRGeoMetadata extends BaseGeoMetadata
    implements NRVRMLNode {

    /**
     * Default constructor
     */
    public NRGeoMetadata() {
        super();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRGeoMetadata(VRMLNodeType node) {
        super(node);
    }
}
