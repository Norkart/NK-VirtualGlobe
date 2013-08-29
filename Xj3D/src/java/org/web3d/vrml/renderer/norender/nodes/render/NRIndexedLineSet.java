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

package org.web3d.vrml.renderer.norender.nodes.render;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.render.BaseIndexedLineSet;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of a VRML IndexedLineSet.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class NRIndexedLineSet extends BaseIndexedLineSet
    implements NRVRMLNode {

    /**
     * Empty constructor
     */
    public NRIndexedLineSet() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRIndexedLineSet(VRMLNodeType node) {
        super(node);
    }
}
