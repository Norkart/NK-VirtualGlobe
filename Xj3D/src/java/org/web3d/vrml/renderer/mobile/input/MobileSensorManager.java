/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.mobile.input;

// External imports
// None

// Local imports
import org.xj3d.core.eventmodel.SensorManager;
import org.web3d.vrml.renderer.mobile.sg.Group;

/**
 * Mobile extensions to the SensorManager interface.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface MobileSensorManager extends SensorManager {

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    public void setWorldRoot(Group root);
}
