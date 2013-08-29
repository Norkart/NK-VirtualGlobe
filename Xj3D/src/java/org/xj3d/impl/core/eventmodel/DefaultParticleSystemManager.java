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

package org.xj3d.impl.core.eventmodel;

// External imports
import org.j3d.geom.particle.ParticleSystem;
import org.j3d.geom.particle.ParticleSystemManager;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLParticleSystemNodeType;
import org.web3d.vrml.util.NodeArray;

import org.xj3d.core.eventmodel.NodeManager;

/**
 * Manager for ParticleSystem nodes and their contained sets of children.
 * <p>
 *
 * Keeps track of all particle systems and makes sure that they update every
 * frame as well as tracking state for those that should not update any longer.
 * <p>
 *
 * This manager likes to run at the end of the frame.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DefaultParticleSystemManager implements NodeManager {

    /** List of managed node types */
    private static final int[] MANAGED_NODE_TYPES = {
        TypeConstants.ParticleSystemNodeType
    };

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Manager for all the systems here */
    private NodeArray systems;

    /** Manager for systems added since last frame */
    private NodeArray newSystems;

    /**
     * Create a new, empty instance of the humanoid manager.
     */
    public DefaultParticleSystemManager() {
        systems = new NodeArray();
        newSystems = new NodeArray();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //-------------------------------------------------------------
    // Methods defined by NodeManager
    //-------------------------------------------------------------

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
    }

    /**
     * Register an error reporter with the engine so that any errors generated
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
        return new ComponentInfo[] {
            new ComponentInfo("xj3d_ParticleSystem", 1)
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
     * Ask whether this manager needs to be run before the event model
     * has been evaluated for this frame.
     *
     * @return true if this is to be run pre-event model, false otherwise
     */
    public boolean evaluatePreEventModel() {
        return false;
    }

    /**
     * Ask whether this manager should run after the event model has been
     * evaluated for this frame.
     *
     * @return true if this is post event model, false otherwise
     */
    public boolean evaluatePostEventModel() {
        return true;
    }

    /**
     * Add a node of the require type to be managed.
     *
     * @param node The node instance to add for management
     */
    public void addManagedNode(VRMLNodeType node) {
        if(!(node instanceof VRMLParticleSystemNodeType)) {
            errorReporter.warningReport("Non-Particle System node added to the manager",
                                        null);
            return;
        }

        systems.add(node);
        newSystems.add(node);
    }

    /**
     * Remove a node of the require type to be managed.
     *
     * @param node The node instance to add for management
     */
    public void removeManagedNode(VRMLNodeType node) {
        if(!(node instanceof VRMLParticleSystemNodeType)) {
            errorReporter.warningReport("Non- Particle System removed from the manager",
                                        null);
            return;
        }

        systems.remove(node);
    }

    /**
     * Run the pre-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePreEventModel(long time) {
        // do nothing
    }


    /**
     * Run the post-event modelling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void executePostEventModel(long time) {

        int size = newSystems.size();

        for(int i = 0; i < size; i++) {
            VRMLParticleSystemNodeType system =
                (VRMLParticleSystemNodeType)newSystems.get(i);

            ParticleSystem p_system = system.getSystem();

            p_system.initialize(time);
        }

        newSystems.clear();

        size = systems.size();

        for(int i = 0; i < size; i++) {
            VRMLParticleSystemNodeType system =
                (VRMLParticleSystemNodeType)systems.get(i);

            if(!system.getEnabled() || !system.isActive())
                continue;

            ParticleSystem p_system = system.getSystem();

            if(!p_system.update(time))
                system.setActive(false);
        }
    }

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear() {
        systems.clear();
        newSystems.clear();
    }
}
