/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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

// External imports
// None

// Local imports
// None

/**
 * The listener interface for receiving notice that a transparency has changed.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface J3DTransparencyListener {
    /**
     * Invoked when a transparency value has changed
     *
     * @param transp The new transparency value
     */
    public void transparencyChanged(float transp);
}
