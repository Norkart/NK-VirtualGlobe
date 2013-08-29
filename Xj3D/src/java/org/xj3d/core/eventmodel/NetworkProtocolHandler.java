/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
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
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLNetworkInterfaceNodeType;

/**
 * A handler for a specific network protocol.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface NetworkProtocolHandler {
    /**
     * Get the protocol this handler supports.
     */
    public String getProtocol();

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Process network traffic now.
     */
    public void processNetworkTraffic();

    /**
     * Add a network node to the management system.
     *
     * @param node The instance to add to this manager
     */
    public void addNode(VRMLNetworkInterfaceNodeType node);

    /**
     * Remove a network node from the management system.
     *
     * @param node The instance to add to this manager
     */
    public void removeNode(VRMLNetworkInterfaceNodeType node);

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear();

    /**
     * Shutdown the protocol handler now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown();
}