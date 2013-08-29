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
import javax.media.j3d.*;

// Application specific imports
import org.web3d.util.ObjectArray;
import org.web3d.vrml.renderer.j3d.input.J3DRendererEffectsHandler;
import org.web3d.vrml.renderer.j3d.input.J3DTerrainManager;

/**
 * Represents all of the render specific rendering effects in the world - overlays
 * terrains and particles.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class RendererEffectsGroup extends Group
    implements J3DRendererEffectsHandler {

    /** The terrain handler */
    private J3DTerrainManager terrainManager;

    /**
     * Create a new instance and controls whether the code will be used
     * in a static or dynamic environment.
     *
     * @param isStatic True if this is a static camera
     */
    RendererEffectsGroup(boolean isStatic) {
    }

    /**
     * Update the view matrix to be this new matrix.
     *
     * @param transform The new view matrix settings
     */
    public void setViewMatrix(Transform3D transform) {
        terrainManager.setViewMatrix(transform);
    }

    /**
     * Set the terrainManager for terrain handling.
     *
     * @param manager The terrain manager or null to clear.
     */
    public void setTerrainManager(J3DTerrainManager manager) {
        terrainManager = manager;
        addChild(terrainManager.getTerrainGroup());
    }

    /**
     * Notification that sectors have been added for terrain management.
     *
     * @param sectors The sectors added
     */
    public void sectorsAdded(ObjectArray sectors) {
        if (terrainManager != null)
            terrainManager.sectorsAdded(sectors);
    }

    /**
     * Notification that sectors have been removed from terrain management.
     *
     * @param sectors The sectors removed
     */
    public void sectorsRemoved(ObjectArray sectors) {
        if (terrainManager != null)
            terrainManager.sectorsRemoved(sectors);
    }

    /**
     * Get the nodes that need to reside under the view platform.
     *
     * @return The view dependent nodes
     */
     public Node[] getViewDependentNodes() {
        return null;
     }

    /**
     * Notification that sectors have been added for terrain management.
     *
     * @param sectors The sectors added
     */
    public void overlaysAdded(ObjectArray overlays) {
    }

    /**
     * Notification that sectors have been removed from terrain management.
     *
     * @param sectors The sectors removed
     */
    public void overlaysRemoved(ObjectArray sectors) {
    }

}
