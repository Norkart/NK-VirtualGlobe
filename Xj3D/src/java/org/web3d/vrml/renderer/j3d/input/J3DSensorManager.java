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
import org.xj3d.core.eventmodel.SensorManager;

/**
 * Java3D extensions to the SensorManager interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public interface J3DSensorManager extends SensorManager {

    /**
     * Get the currently set navigation state.
     *
     * @return true for the current state
     */
    public boolean getNavigationEnabled();

    /**
     * Enable or disable navigation processing sub-section of the
     * user input processing. By default the navigation processing is enabled.
     *
     * @param state true to enable navigation
     */
    public void setNavigationEnabled(boolean state);

    /**
     * Set the global effects handler for this sensor manager.
     *
     * @param handler The new handler instance to use
     */
    public void setGlobalEffectsHandler(J3DGlobalEffectsHandler handler);

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    public void setWorldRoot(BranchGroup root);

    /**
     * Set the renderer effects handler for this sensor manager.
     *
     * @param handler The new handler instance to use
     */
    public void setRendererEffectsHandler(J3DRendererEffectsHandler handler);
}
