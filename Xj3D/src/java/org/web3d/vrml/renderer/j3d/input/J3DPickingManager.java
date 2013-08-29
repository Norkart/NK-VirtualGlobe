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

package org.web3d.vrml.renderer.j3d.input;

// External imports
import javax.media.j3d.BranchGroup;

// Local imports
import org.xj3d.core.eventmodel.PickingManager;

/**
 * Abstract manager representation for processing the functionality of the
 * Picking Utilities component, specific to the Java3D renderer.
 * <p>
 *
 * Picking and, ultimately, n-body object collision detection is handled by
 * this manager.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface J3DPickingManager extends PickingManager {

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    public void setWorldRoot(BranchGroup root);
}
