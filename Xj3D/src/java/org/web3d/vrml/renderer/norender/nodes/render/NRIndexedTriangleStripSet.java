/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.render;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.render.BaseIndexedTriangleStripSet;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of a IndexedTriangleStripSet node.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRIndexedTriangleStripSet extends BaseIndexedTriangleStripSet
    implements NRVRMLNode {

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    public NRIndexedTriangleStripSet() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRIndexedTriangleStripSet(VRMLNodeType node) {
        super(node);
    }

    /**
     * Build the implementation.
     */
    protected void buildImpl() {
        // no op
    }
}
