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
 * A listener for changes in a nodes URL content state.
 * <p>
 *
 * Used to act as a state hook for LoadSensor nodes where they wish to
 * view the current state of a node.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLContentStateListener {

    /**
     * Notification that the content state for this node has changed
     *
     * @param node The node that changed state
     * @param index The index of the field that has changed
     * @param state The new state that has changed
     */
    public void contentStateChanged(VRMLNodeType node, int index, int state);
}
