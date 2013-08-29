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

package org.web3d.vrml.renderer.norender.nodes.geom3d;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.geom3d.BaseIndexedFaceSet;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of an IndexedFaceSet.
 * <p>
 *
 * TODO:
 *    Needed Listeners: solid
 *    Needed Observers: color, normal, texCoord
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class NRIndexedFaceSet extends BaseIndexedFaceSet
    implements NRVRMLNode {

    /**
     * Empty constructor
     */
    public NRIndexedFaceSet() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRIndexedFaceSet(VRMLNodeType node) {
        super(node);
    }
}
