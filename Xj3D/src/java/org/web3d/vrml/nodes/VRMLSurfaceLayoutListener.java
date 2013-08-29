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
import java.awt.Rectangle;

// Application specific imports
// None

/**
 * A listener for layout feedback information from a suface node type..
 * <p>
 *
 * The listener is used to make sure that the surface can notify its parent
 * class that it has resized.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLSurfaceLayoutListener {

    /**
     * Notification that its size has changed. Values shall not be negative.
     *
     * @param width The new width of the surface
     * @param height The new height of the surface
     */
    public void surfaceResized(int width, int height);
}
