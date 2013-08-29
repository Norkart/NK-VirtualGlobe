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
import org.web3d.vrml.nodes.VRMLNodeType;

// Application specific imports
// none

/**
 * A listener for changes to the isActive, loop and pitch fields in a component node.
 * <p>
 *
 * The listener does simple notifications of the component node that has
 * changed. This listener is intended to be used by
 * internal structural changes within the scene graph, not to the outside
 * world such as scripting and EAI.s
 *
 * @author Guy Carpenter
 * @version $Revision: 1.2 $
 */
public interface VRMLSoundStateListener {

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param isActive The current value of the isActive field
     * @param loop The current value of the loop field
     * @param pitch The current value of the pitch field 
     * @param startTime The current value of the startTime field
     */
    public void soundStateChanged(VRMLNodeType node, boolean isActive, boolean loop, float pitch, double startTime);
}
