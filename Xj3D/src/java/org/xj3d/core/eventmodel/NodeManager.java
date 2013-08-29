/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Abstract representation of a class that needs to manage a specific class of
 * nodes in the system.
 * <p>
 *
 * VRML and X3D have a collection of nodes that require some sort of per-frame
 * management that is part of the event loop. This may be as simple as
 * synchronising with external devices or the network, or as complex as a
 * high-detail terrain renderer. This interface allows the user to provide an
 * abstracted way of registering new capabilities with the
 * {@link org.xj3d.core.eventmodel.EventModelEvaluator} without needing
 * edit and recompile the entire Xj3D codebase.
 * <p>
 *
 * An implementation of this interface may handle pre event model updates (eg
 * sensor-style nodes), post event model updates (eg particle systems) or a
 * both at the same time. Methods are used to indicate which of these
 * classifications the manager wants to perform. This is asked once at the
 * start of the browser's lifetime. It is not possible to change these settings
 * after startup, even if there are no nodes of the desired type in the system.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface NodeManager {

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

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
    public ComponentInfo[] getSupportedComponents();

    /**
     * Set the VRMLClock instance in use by this manager.
     *
     * @param clk A reference to the clock to use
     */
    public void setVRMLClock(VRMLClock clk);

    /**
     * Get the list of node type IDs that this manager wants to handle. These
     * should be the constants from {@link org.web3d.vrml.lang.TypeConstants}.
     *
     * @return A list of managed node identifiers
     */
    public int[] getManagedNodeTypes();

    /**
     * Ask whether this manager needs to be run before the event model has been
     * evaluated for this frame.
     *
     * @return true if this is to be run pre-event model, false otherwise
     */
    public boolean evaluatePreEventModel();

    /**
     * Ask whether this manager needs to be run after the event model has been
     * evaluated for this frame.
     *
     * @return true if this is to be run post event model, false otherwise
     */
    public boolean evaluatePostEventModel();

    /**
     * Add a node of the require type to be managed.
     *
     * @param node The node instance to add for management
     */
    public void addManagedNode(VRMLNodeType node);

    /**
     * Remove a node of the require type to be managed.
     *
     * @param node The node instance to add for management
     */
    public void removeManagedNode(VRMLNodeType node);

    /**
     * Run the pre-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePreEventModel(long time);

    /**
     * Run the post-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePostEventModel(long time);

    /**
     * Reset the local time zero for the manager. This is called when a new
     * root world has been loaded and any manager that needs to rely on delta
     * time from the start of the world loading can reset it's local reference
     * from the passed in {@link VRMLClock} instance.
     */
    public void resetTimeZero();

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear();

    /**
     * Initialise the node manager now with any per-manager setup that is
     * needed. If this returns false, then the node manager is assumed to have
     * failed some part of the setup and will be removed from the system
     *
     * @return true if initialisation was successful
     */
    public boolean initialize();

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown();
}
