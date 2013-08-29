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
// None

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.util.NodeArray;

/**
 * An abstract representation of a per-layer sensor manager supplement to
 * the main sensor manager.
 * <p>
 *
 * The main sensor manager can handle most of the common code, but certain
 * sections need to be handled in a per-layer manner. For example visibility
 * sensors have to be picked based on the layer that they're in and the
 * viewpoint they current have in that layer. This interface is registered
 * with the sensor manager for processing this per-layer information.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface LayerSensorManager {

    /**
     * Get the ID of the layer this sensor manager works with. The ID is the rendering
     * order, starting from 0. Once set, this shouldn't change.
     */
    public int getLayerId();

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
     * Set the VRML clock instance that we are using so that we can set the
     * bind time information for nodes that require it. A value of null can
     * be used to clear the current clock instance.
     *
     * @param clk The new clock to set
     */
    public void setVRMLClock(VRMLClock clk);

    /**
     * Set the user input manager to be used by this implementation. User input
     * is generally independent of the main render loop. A value of null may be
     * used to clear the currently set manager and make the handler not process
     * user input.
     *
     * @param manager The buffer instance to use or null
     */
    public void setInputManager(InputDeviceManager manager);

    /**
     * Set the manager that is responsible for handling picking sensors.
     *
     * @param picker Reference to the manager instance to use or null
     */
    public void setPickingManager(PickingManager picker);

    /**
     * Set the bindable stacks used for viewpoint and navigation nodes.
     * Used by the sensor for computing navigation and sensor information.
     *
     * @param vp The stack for viewpoints
     * @param nav The stack for navigationInfo nodes
     * @param back The stack for background nodes
     * @param fog The stack for fog nodes
     */
    public void setNavigationStacks(BindableNodeManager vp,
                                    BindableNodeManager nav,
                                    BindableNodeManager back,
                                    BindableNodeManager fog);


    /**
     * Process the user input to the scene now. User input is the mouse and
     * keyboard processing that would be used to send events to
     * Key/StringSensors and perform navigation and picking duties as well as
     * adjust items like billboards and LODs.
     *
     * @param time The clock time, in Java coordinates, not VRML
     * @return true if the user input was processed and performed an action
     *   for this layer
     */
    public boolean processUserInput(long time);

    /**
     * Initialise new sensors that are just about to be added to the scene.
     * These sensors should also be added to the processing list for dealing
     * with user input. Note that the adding process should only send the
     * initial events, but should not do any processing for environmental
     * effects like collisions or proximity sensing. It is assumed these will
     * be first processed in the next render pass.
     *
     * @param sensors The list of sensors to process
     */
    public void addSensors(NodeArray sensors);

    /**
     * Cleanup the given sensors and remove them from the list of processing
     * to be done each frame. The list will be created elsewhere (typically the
     * per-frame behaviour as a result of the event model processing) and
     * passed to this manager. The given list will contain instances of
     * VRMLSensorNodeType. There will be no protos as this is just the raw
     * sensor nodes internally.
     *
     * @param sensors The list of sensors to process
     */
    public void removeSensors(NodeArray sensors);

    /**
     * Add view-dependent nodes that need to be updated each frame based on
     * the user's position for rendering. These are not sensors nodes, but
     * others like Billboard, LOD etc.
     *
     * @param nodes List of nodes that need to be processed
     */
    public void addViewDependentNodes(NodeArray nodes);

    /**
     * Remove these view-dependent nodes from the scene.
     *
     * @param nodes List of nodes to be removed
     */
    public void removeViewDependentNodes(NodeArray nodes);

    /**
     * Load the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void loadScene(BasicScene scene);

    /**
     * UnLoad the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void unloadScene(BasicScene scene);

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear();

    /**
     * Get the currently set navigation state.
     *
     * @return true for the current state
     */
    public boolean getNavigationEnabled();

    /**
     * Enable or disable navigation processing sub-section of the
     * user input processing. By default the navigation processing is enabled.
     *
     * @param state true to enable navigation
     */
    public void setNavigationEnabled(boolean state);

    /**
     * Update the viewing matrix.  Call this when you want the SensorManager to update
     * the viewing matrix.  Typically after all user input and events have resolved.
     */
    public void updateViewMatrix();

    /**
     * Get the handler for user input.
     *
     * @return The user input handler */
    public UserInputHandler getUserInputHandler();

    /**
     * See if this layer is currently pickable.
     *
     * @return true if the contents of this layer can be picked
     */
    public boolean getIsPickable();

    /**
     * Set whether this layer is currently pickable.
     *
     * @param val Whether the layer is pickable
     */
    public void setIsPickable(boolean val);
}
