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

package org.web3d.vrml.renderer.common.input;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLLinkNodeType;

/**
 * A listener to notify that a {@link org.web3d.vrml.nodes.VRMLLinkNodeType}
 * has been activated by the user interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface LinkSelectionListener {

    /**
     * Invoked when a link node has been activated. This is the node that has
     * been selected.
     *
     * @param node The selected node
     */
    public void linkSelected(VRMLLinkNodeType node);

    /**
     * Invoked when a link node is contact with a tracker capable of picking.
     */
    public void linkSelectable(VRMLLinkNodeType node);

    /**
     * Invoked when a link node is contact with a tracker capable of picking.
     */
    public void linkNonSelectable(VRMLLinkNodeType node);
}
