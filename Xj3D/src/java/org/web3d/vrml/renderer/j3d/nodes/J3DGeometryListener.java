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

/**
 * The listener interface for receiving notice that a geometry item has changed.
 * <p>
 * The index values here relate to the index values for each item of geometry
 * that is returned in the {@link J3DGeometryNodeType#getGeometry()} call.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface J3DGeometryListener {

    /**
     * A piece of geometry has been added to the list. The index is the
     * position in the array that the geometry has been added if you were to
     * fetch the geometry now. This allows new items to be inserted into the
     * array. The array will never be null.
     *
     * @param items The geometry items that have been added
     */
    public void geometryAdded(int[] items);

    /**
     * Invoked when a single geometry item has changed. If the value is
     * null, that indicates that all geometry should be reloaded. Otherwise
     * the array lists the index values of all items of geometry that have
     * changed.
     *
     * @param items The geometry items that have changed or null for all
     */
    public void geometryChanged(int[] items);

    /**
     * Invoked when one or more pieces of geometry have changed. The items list
     * the index values of the geometry before they were removed from the
     * array. At the point that this method is called, the geometry will have
     * already been removed from the array. If the array is null, that means
     * all geometry is to be removed.
     *
     * @param items The geometry items that have removed or null for all
     */
    public void geometryRemoved(int[] items);
}
