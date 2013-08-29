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

package org.xj3d.core.eventmodel;

// External imports
// none

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLNetworkInterfaceNodeType;

/**
 * The manager of network interactions.
 * <p>
 *
 * The implementation encapsulates all network protocols.  A handler can
 * be registered for each protocol desired.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface NetworkManager extends NodeManager {

    /**
     * Register a new protocol handler instance. If a handler has been
     * registered for the given protocol type, this request is ignored
     * and a value of false is returned.
     *
     * @param nph The new protocol handler instance to use
     * @return false if a handler is already registered, true otherwise
     */
    public boolean addProtocolHandler(NetworkProtocolHandler nph);

    /**
     * Remove a previously registered protocol handler. If a this handler
     * instance has not been registered previously, the request is silently
     * ignored.
     *
     * @param nph The protocol handler instance to remove
     */
    public void removeProtocolHandler(NetworkProtocolHandler nph);

}
