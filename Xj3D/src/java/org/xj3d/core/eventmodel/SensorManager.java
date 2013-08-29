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
 * An abstract representation of a class that would be responsible for
 * performing all the management and computations for environmental sensors.
 * <p>
 *
 * It is expected that the manager will be implemented by each renderer as
 * working out when sensors intersect and interact will require in-depth
 * knowledge of the rendering API.
 * <p>
 *
 * Because the sensor manager is a renderer-dependent implementation, it is
 * assumed that it will also function, or have the functionality for
 * representing the {@link org.web3d.vrml.nodes.VRMLClock} interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface SensorManager {

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
     * Set the user input manager to be used by this implementation. User input
     * is generally independent of the main render loop. A value of null may be
     * used to clear the currently set manager and make the handler not process
     * user input.
     *
     * @param manager The buffer instance to use or null
     */
    public void setInputManager(InputDeviceManager manager);

    /**
     * Set the manager responsible for handling key device sensor. A value of
     * null may be used to clear the currently set manager and make the handler
     * not process user input.
     *
     * @param manager Reference to the manager instance to use or null
     */
    public void setKeyDeviceSensorManager(KeyDeviceSensorManager manager);

    /**
     * Set the manager that is responsible for handling picking sensors.
     *
     * @param picker Reference to the manager instance to use or null
     */
    public void setPickingManager(PickingManager picker);

    /**
     * Add a per-layer manager to the sensor manager. Managers are added once,
     * and calling a manager for a layer ID will replace any manager at that
     * current ID.
     *
     * @param mgr The layer manager instance to add
     */
    public void addLayerSensorManager(LayerSensorManager mgr);

    /**
     * Remove this layer sensor manager from the system.
     *
     * @param mgr The layer manager instance to remove
     */
    public void removeLayerSensorManager(LayerSensorManager mgr);

    /**
     * Get the VRMLClock instance in use by this sensor manager.
     *
     * @return A reference to the clock
     */
    public VRMLClock getVRMLClock();

    /**
     * Set the rendering order for all the layers. This is so that input
     * handling, such as drag sensors are evaluated in the correct order, from
     * front to back.
     *
     * @param order The index of the list of rendered layers ids
     * @param numValid The number of valid items in the order list
     */
    public void setRenderOrder(int[] order, int numValid);

    /**
     * Process the user input to the scene now. User input is the mouse and
     * keyboard processing that would be used to send events to
     * Key/StringSensors and perform navigation and picking duties as well as
     * adjust items like billboards and LODs.
     *
     * @param layerId The ID of the layer that is active for navigation
     * @param time The clock time, in Java coordinates, not VRML
     */
    public void processUserInput(int layerId, long time);

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
}
