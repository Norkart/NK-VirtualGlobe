/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

/**
 * A listener for changes in local color state.
 * <p>
 * Local color states whether a geometry node has color per vertex/face information
 * that overrides the diffuse component of the Material node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface LocalColorsListener {
    /**
     * The localColors state has change.
     *
     * @param enabled True if the geometry has local colors.
     * @param hasAlpha true with the local color also contains alpha valuess
     */
    public void localColorsChanged(boolean enabled, boolean hasAlpha);
}
