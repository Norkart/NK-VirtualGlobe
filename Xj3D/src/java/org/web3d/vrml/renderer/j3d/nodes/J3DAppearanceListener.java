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
// Standard Imports
import javax.media.j3d.Appearance;

// Application Specific Imports

/**
 * The listener interface for receiving notice that an appearance item has
 * recreated itself.  Added to work around a Java3D bug in texture memory
 * handling for Java3D 1.3
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface J3DAppearanceListener {

    /**
     * Invoked when the underlying Java3D Appearance object is changed
     * @param app The new appearance object
     */
    public void appearanceChanged(Appearance app);
}
