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

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

/**
 * Marker interface to indicate nodes of the X3DPickableObject type.
 * <p>
 *
 * The X3DPickableObject is an Xj3D extension node, and it's defined in the
 * specification at http://www.xj3d.org/extensions/picking.html
 *
 * @author Justin COuch
 * @version $Revision: 1.2 $
 */
public interface VRMLPickableNodeType extends VRMLNodeType {

    /**
     * Set the list of picking targets that this object corresponds to.
     * These can be an array of strings.
     *
     * @param types The list of object type strings to use
     * @param numValid The number of valid values to read from the array
     */
    public void setObjectType(String[] types, int numValid);

    /**
     * Get the current number of valid object type strings.
     *
     * @return a number >= 0
     */
    public int numObjectType();

    /**
     * Fetch the number of object type values in use currently.
     *
     * @param val An array to copy the values to
     */
    public void getObjectType(String[] val);

    /**
     * Set the pickable state of this object. True to allow it and it's
     * children to participate in picking, false to remove it.
     *
     * @param state true to enable picking, false otherwise
     */
    public void setPickable(boolean state);

    /**
     * Get the current pickable state of the object.
     *
     * @return true if picking is allowed, false otherwise
     */
    public boolean getPickable();

}
