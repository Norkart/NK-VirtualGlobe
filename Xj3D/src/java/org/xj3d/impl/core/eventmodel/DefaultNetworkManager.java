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

package org.xj3d.impl.core.eventmodel;

// External imports
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.TypeConstants;

import org.xj3d.core.eventmodel.NetworkManager;
import org.xj3d.core.eventmodel.NetworkProtocolHandler;

/**
 * The manager of network interactions.
 * <p>
 *
 * Does not perform networking itself, but manages the different
 * protocol handlers.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class DefaultNetworkManager implements NetworkManager {

    /** List of managed node types */
    private static final int[] MANAGED_NODE_TYPES = {
        TypeConstants.NetworkInterfaceNodeType
    };

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** A map of protocol strings to NetworkProtocolHandlers */
    private HashMap protocolMap;

    /** An array of protocol handlers derived from protocolMap */
    private NetworkProtocolHandler[] protocolHandlers;

    /** The number of valid handlers currently available */
    private int numHandlers;

    /**
     * Create a new instance of the execution space manager to run all the
     * routing.
     */
    public DefaultNetworkManager() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        protocolMap = new HashMap();
        protocolHandlers = new NetworkProtocolHandler[4];
    }

    //----------------------------------------------------------
    // Methods defined by NodeManager
    //----------------------------------------------------------

    /**
     * Initialise the node manager now with any per-manager setup that is
     * needed. If this returns false, then the node manager is assumed to have
     * failed some part of the setup and will be removed from the system
     *
     * @return true if initialisation was successful
     */
    public boolean initialize() {
        return true;
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown() {
        for(int i=0; i < numHandlers; i++)
            protocolHandlers[i].shutdown();
    }

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Ask whether this manager needs to be run before the event model
     * has been evaluated for this frame.
     *
     * @return true if this is to be run pre-event model, false otherwise
     */
    public boolean evaluatePreEventModel() {
        return true;
    }

    /**
     * Ask whether this manager should run after the event model has been
     * evaluated for this frame.
     *
     * @return true if this is post event model, false otherwise
     */
    public boolean evaluatePostEventModel() {
        return false;
    }

    /**
     * Get the list of component names that this manager would normally manage.
     * The component definition is asssumed to be the same across all versions
     * of the specifications that the browser supports. The level of the
     * component is assumed to be the lowest level supported (ie if the given
     * level fails, then levels above this cannot be supported, but those below
     * can still be).
     * <p>
     * Mostly this is used for when initialisation fails and we wish to disable
     * support for loading of nodes in that component.
     *
     * @return The collection of components that this manager supports
     */
    public ComponentInfo[] getSupportedComponents() {
        // JC: May want to pull this from the lowever-level networking
        // protocol handlers. Simple version for now.
        return new ComponentInfo[] {
            new ComponentInfo("DIS", 1)
        };
    }

    /**
     * Set the VRMLClock instance in use by this manager. Ignored for this
     * manager.
     *
     * @param clk A reference to the clock to use
     */
    public void setVRMLClock(VRMLClock clk) {
    }

    /**
     * Reset the local time zero for the manager. This is called when a new
     * root world has been loaded and any manager that needs to rely on delta
     * time from the start of the world loading can reset it's local reference
     * from the passed in {@link VRMLClock} instance.
     */
    public void resetTimeZero() {
    }

    /**
     * Get the list of node type IDs that this manager wants to handle. These
     * should be the constants from {@link org.web3d.vrml.lang.TypeConstants}.
     *
     * @return A list of managed node identifiers
     */
    public int[] getManagedNodeTypes() {
        return MANAGED_NODE_TYPES;
    }

    /**
     * Run the pre-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePreEventModel(long time) {
        // Used for non blocking IO, not used right now
        // as NIO doesn't support multicast

        for(int i=0; i < numHandlers; i++)
            protocolHandlers[i].processNetworkTraffic();
    }


    /**
     * Run the post-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePostEventModel(long time) {
        // do nothing
    }

    /**
     * Add a network node to the management system.
     *
     * @param node The instance to add to this manager
     */
    public void addManagedNode(VRMLNodeType node) {

        if(!(node instanceof VRMLNetworkInterfaceNodeType)) {
            errorReporter.warningReport("Non-network node added to the manager: " + node,
                                        null);
            return;
        }

        VRMLNetworkInterfaceNodeType nn = (VRMLNetworkInterfaceNodeType)node;

        NetworkProtocolHandler nph =
            (NetworkProtocolHandler)protocolMap.get(nn.getProtocol());

        if (nph == null) {
            errorReporter.warningReport("No network handler for protocol: " +
                                        nn.getProtocol(),
                                        null);
        } else {
            nph.addNode(nn);
        }
    }

    /**
     * Remove a network node from the management system.
     *
     * @param node The instance to add to this manager
     */
    public void removeManagedNode(VRMLNodeType node) {
        if(!(node instanceof VRMLNetworkInterfaceNodeType)) {
            errorReporter.warningReport("Non-network node added to the manager",
                                        null);
            return;
        }

        VRMLNetworkInterfaceNodeType nn = (VRMLNetworkInterfaceNodeType)node;

        NetworkProtocolHandler nph =
            (NetworkProtocolHandler)protocolMap.get(nn.getProtocol());

        if (nph == null) {
            errorReporter.warningReport("No network handler for protocol: " +
                                        nn.getProtocol(),
                                        null);
        } else {
            nph.removeNode(nn);
        }
    }

    /**
     * Clear all of the nodes that this manager is currently dealing
     * with. All listeners are removed and no watch is kept on the nodes.
     */
    public void clear() {
        for(int i = 0; i < numHandlers; i++) {
            protocolHandlers[i].clear();
        }
    }

    //----------------------------------------------------------
    // Methods defined by NetworkManager
    //----------------------------------------------------------

    /**
     * Register a new protocol handler instance. If a handler has been
     * registered for the given protocol type, this request is ignored
     * and a value of false is returned.
     *
     * @param nph The new protocol handler instance to use
     * @return false if a handler is already registered, true otherwise
     */
    public boolean addProtocolHandler(NetworkProtocolHandler nph) {
        if (protocolMap.containsKey(nph))
            return false;

        protocolMap.put(nph.getProtocol(), nph);

        if(protocolHandlers.length == numHandlers) {
            NetworkProtocolHandler[] tmp =
                new NetworkProtocolHandler[numHandlers + 4];
            System.arraycopy(protocolHandlers, 0, tmp, 0, numHandlers);
            protocolHandlers = tmp;
        }

        protocolHandlers[numHandlers++] = nph;
        return true;
    }

    /**
     * Remove a previously registered protocol handler. If a this handler
     * instance has not been registered previously, the request is silently
     * ignored.
     *
     * @param nph The protocol handler instance to remove
     */
    public void removeProtocolHandler(NetworkProtocolHandler nph) {
        if (!protocolMap.containsKey(nph))
            return;

        for(int i = 0; i < numHandlers; i++) {
            if(protocolHandlers[i] == nph) {
                System.arraycopy(protocolHandlers,
                                 i + 1,
                                 protocolHandlers,
                                 i,
                                 numHandlers - i - 1);
                numHandlers--;
            }
        }

        protocolMap.remove(nph);
    }
}
