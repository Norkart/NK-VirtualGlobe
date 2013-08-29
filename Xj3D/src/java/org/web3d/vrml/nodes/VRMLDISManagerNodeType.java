/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
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
import mil.navy.nps.dis.EntityStatePdu;
import org.web3d.xmsf.dis.*;

// Local imports
// None

/**
 * Common interface for all DIS Managers.
 * <p>
 *
 * Manages new and removed entities and any simulation PDU packets.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface VRMLDISManagerNodeType extends VRMLDISNodeType {
    /**
     * A new entity has arrived.
     *
     * @param espdu The new entity.
     */
    public void entityArrived(EntityStatePdu espdu);

    /**
     * A new entity has arrived.
     *
     * @param espdu The new entity.
     */
    public void entityArrived(EntityStatePduType espdu);

    /**
     * An entity has been removed from the simulation.
     *
     * @param node The entity being removed
     */
    public void entityRemoved(VRMLDISNodeType node);
}
