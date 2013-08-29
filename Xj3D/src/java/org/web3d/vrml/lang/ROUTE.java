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

package org.web3d.vrml.lang;

/**
 * Representation of a ROUTE.
 * <p>
 *
 * A route belongs to a specific execution space, whether that be the main
 * world or a proto or inline contained within that world. The information
 * about the execution space is not contained in this route as that is expected
 * to be set, along with this route, in the route manager.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface ROUTE {

    /**
     * Get the reference to the source node of this route.
     *
     * @return The source node reference
     */
    public VRMLNode getSourceNode();

    /**
     * Get the index of the source field of this route.
     *
     * @return The source node field index
     */
    public int getSourceIndex();

    /**
     * Get the reference to the destination node of this route.
     *
     * @return The destination node reference
     */
    public VRMLNode getDestinationNode();

    /**
     * Get the index of the destination field of this route.
     *
     * @return The destination node field index
     */
    public int getDestinationIndex();
}
