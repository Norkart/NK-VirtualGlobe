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
 * A listener for representation of the current frame state.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface FrameStateListener {

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. If the node needs to update itself for this
     * frame, it should do so now before the render pass takes place.
     */
    public void allEventsComplete();
}
