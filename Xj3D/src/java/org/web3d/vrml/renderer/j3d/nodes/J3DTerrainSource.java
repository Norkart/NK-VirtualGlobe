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

package org.web3d.vrml.renderer.j3d.nodes;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLTerrainSource;
import org.web3d.vrml.renderer.j3d.nodes.J3DAppearanceNodeType;

/**
 * Representation of a node that supplies terrain data.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface J3DTerrainSource extends VRMLTerrainSource {
    /**
     * Set the appearance node for this terrain.  Updated whenever
     * the appearance changes in the X3D Scenegraph.
     *
     * @param app The appearance
     */
    public void setAppearance(J3DAppearanceNodeType app);

    /**
     * Get the appearance node for this terrain.  Updated whenever
     * the appearance changes in the X3D Scenegraph.
     *
     * @return The appearance
     */
    public J3DAppearanceNodeType getAppearance();

}
