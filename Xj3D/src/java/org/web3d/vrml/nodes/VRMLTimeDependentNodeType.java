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
package org.web3d.vrml.nodes;

/**
 * Nodes which have behaviour controlled by a clock.
 * <p>
 *
 * Time information is provided by the clock that can be set by the
 * implementing runtime system. With the clock, the node can then register
 * clock ticks listeners for timing update information.
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public interface VRMLTimeDependentNodeType extends VRMLChildNodeType {
    /**
     * Set the clock that this time dependent node will be running with.
     * The clock provides all the information and listeners for keeping track
     * of time. Setting a value of null will ask the node to remove the clock
     * from it's use so that the node may be removed from the scene.
     *
     * @param clock The clock to use for this node
     */
    public void setVRMLClock(VRMLClock clock);
}
