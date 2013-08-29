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
import mil.navy.nps.dis.ProtocolDataUnit;

import org.web3d.xmsf.dis.ProtocolDataUnitType;

// Local imports
// None

/**
 * Common interface for all DIS nodes.
 * <p>
 *
 * Provides access to networking and indentification params.
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public interface VRMLDISNodeType extends VRMLNetworkInterfaceNodeType {

    /**
     * Get the siteID specified for this node.
     *
     * @return the siteID.
     */
    public int getSiteID();

    /**
     * Get the applicationID specified for this node.
     *
     * @return the applicationID.
     */
    public int getAppID();

    /**
     * Get the entityID specified for this node.
     *
     * @return the entityID.
     */
    public int getEntityID();

    /**
     * Get the network address to listen to.
     *
     * @return The address.
     */
    public String getAddress();

    /**
     * Get the network port to listen to.
     *
     * @return The port.
     */
    public int getPort();

    /**
     * Get the chat room username.
     *
     * @return The username.  null if none provided
     */
    public String getUsername();

    /**
     * Get the chat room password.
     *
     * @return The password.  null if none provided
     */
    public String getPassword();

    /**
     * Get the chat room auth server's.
     *
     * @return The auth servers.  null if none provided
     */
    public String[] getAuthServer();

    /**
     * Get the chat room mucServer.
     *
     * @return The mucServer.  null if none provided
     */
    public String getMucServer();

    /**
     * Get the chat room mucRoom.
     *
     * @return The mucRoom.  null if none provided
     */
    public String getMucRoom();

    /**
     * Set the isActive state for a DIS node.
     *
     * @param active Whether the node is active(traffic within 5 seconds).
     */
    public void setIsActive(boolean active);

    /**
     * Tell the DIS node that a packet arrived.
     * Used to update tiemstamp information and update local fields.
     *
     */
    public void packetArrived(ProtocolDataUnit pdu);

    // Prototype method for new DISXML codebase
    /**
     * Tell the DIS node that a packet arrived.
     * Used to update tiemstamp information and update local fields.
     *
     */
    public void packetArrived(ProtocolDataUnitType pdu);

    /**
     * Does the this node have new information to write.  This only
     * accounts for local values, not required DIS heartbeart rules.
     *
     * @return TRUE if values have changed.
     */
    public boolean valuesToWrite();

    /**
     * Get the nodes current state.  Assume that a single local scratch var can
     * be reused each time.
     *
     * @return The DIS state.
     */
    public ProtocolDataUnit getState();


    // Prototype method for new DISXML codebase
    /**
     * Get the nodes current state.  Assume that a single local scratch var can
     * be reused each time.
     *
     * @return The DIS state.
     */
    public ProtocolDataUnitType getStateDX();
}
