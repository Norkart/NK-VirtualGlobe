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
import org.web3d.vrml.renderer.common.nodes.render.BaseLineSet;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of an LineSet.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRLineSet extends BaseLineSet
    implements NRVRMLNode {

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public NRLineSet() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRLineSet(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLNodeComponentListener interface
    //-------------------------------------------------------------

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param index The index of the field that has changed
     */
    public void fieldChanged(VRMLNodeType node, int index) {
    }
}
