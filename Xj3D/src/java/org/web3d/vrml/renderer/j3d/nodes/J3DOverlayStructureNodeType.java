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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import org.j3d.renderer.java3d.overlay.OverlayManager;

// Application specific imports
// none

/**
 * Auxillary interface to allow passing in the overlay structural information.
 * <p>
 *
 * Any node involved in the overlay support may implement this interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface J3DOverlayStructureNodeType extends J3DVRMLNode {

    /**
     * Set the overlay manager to use.
     *
     * @param mgr The overlay manager instance to use
     */
    public void setOverlayManager(OverlayManager mgr);
}
