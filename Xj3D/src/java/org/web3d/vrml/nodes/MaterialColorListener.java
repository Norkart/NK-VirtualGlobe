/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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
// none

// Local imports
// none

/**
 * A listener for changes in color values from the material node that are used
 * in a more global manner in the internal rendering structure.
 * <p>
 *
 * Some nodes, like lines and points need to pass some of the material colour
 * values back to the geometry for use there when there is no lighting. This
 * listener instance is typically implemented by the shape node, but passed
 * down to the material through the appearance.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface MaterialColorListener {

    /**
     * The emissiveColor value has changed.
     *
     * @param color The new color value to use
     */
    public void emissiveColorChanged(float[] color);
}
