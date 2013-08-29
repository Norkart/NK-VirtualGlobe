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

package org.web3d.vrml.renderer.j3d.browser;

// Standard imports
import javax.media.j3d.Canvas3D;

// Application specific imports
// none

/**
 * A marker for a class that guarantees to provide the correct handling for
 * overlays.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface OverlayHandler {

    /**
     * Fetch the canvas that will be responsible for having the overlays
     * composited on them.
     *
     * @return The canvas instance to use
     */
    public Canvas3D getPrimaryCanvas();
}
