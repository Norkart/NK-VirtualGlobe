/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2005
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
import javax.media.j3d.Transform3D;
import javax.media.j3d.Node;

// Local imports
import org.web3d.util.ObjectArray;

/**
 * A generic interface for the control of renderer specific effects like
 * overlays, terrains and particles.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface J3DRendererEffectsHandler {
    /**
     * Update the view matrix to be this new matrix.
     *
     * @param transform The new view matrix settings
     */
    public void setViewMatrix(Transform3D transform);

    /**
     * Set the terrainManager for terrain handling.
     *
     * @param manager The terrain manager or null to clear.
     */
    public void setTerrainManager(J3DTerrainManager manager);

    /**
     * Notification that sectors have been added for terrain management.
     *
     * @param sectors The sectors added
     */
    public void sectorsAdded(ObjectArray sectors);

    /**
     * Notification that sectors have been removed from terrain management.
     *
     * @param sectors The sectors removed
     */
    public void sectorsRemoved(ObjectArray sectors);

    /**
     * Get the nodes that need to reside under the view platform.
     *
     * @return The view dependent nodes
     */
     public Node[] getViewDependentNodes();

    /**
     * Notification that overlays have been added for terrain management.
     *
     * @param overlays The overlays added
     */
    public void overlaysAdded(ObjectArray overlays);

    /**
     * Notification that overlays have been removed from terrain management.
     *
     * @param overlays The overlays removed
     */
    public void overlaysRemoved(ObjectArray overlays);
}
