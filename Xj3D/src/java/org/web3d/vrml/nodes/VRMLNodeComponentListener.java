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
package org.web3d.vrml.nodes;

// Standard imports
// none

// Application specific imports
// none

/**
 * An listener for changes in a component node that makes up a larger
 * strucuter..
 * <p>
 *
 * The listener does simple notifications of the component node that has
 * changed and the field index. This listener is intended to be used by
 * internal structural changes within the scene graph, not to the outside
 * world such as scripting and EAI.s
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLNodeComponentListener {

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param index The index of the field that has changed
     */
    public void fieldChanged(VRMLNodeType node, int index);
}
