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
import org.web3d.vrml.nodes.VRMLPickingSensorNodeType;
import org.web3d.vrml.util.NodeArray;

/**
 * An abstract representation of a class that would be responsible for
 * performing all the picking componet by the class.
 * <p>
 *
 * It is expected that the manager will be implemented by each renderer as
 * working out when sensors intersect and interact will require in-depth
 * knowledge of the rendering API.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface PickingManager {

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
     * Cleanup the given sensor and remove them from the list of processing
     * to be done each frame. The list will be created elsewhere (typically the
     * per-frame behaviour as a result of the event model processing) and
     * passed to this manager. The given list will contain instances of
     * VRMLSensorNodeType. There will be no protos as this is just the raw
     * sensor nodes internally.
     *
     * @param sensor The sensor to remove
     */
    public void removeSensor(VRMLPickingSensorNodeType sensor);

    /**
     * Add a new sensor instance to the system for processing.
     *
     * @param sensor The list of sensor to add
     */
    public void addSensor(VRMLPickingSensorNodeType sensor);

    /**
     * Process the list of picking sensors now.
     *
     * @param time The timestamp for "now"
     */
    public void processPickSensors(double time);

    /**
     * Load the contents of this scene into the sensor manager. The call does
     * not need to be recursive as it is expected the external caller will
     * work with this.
     *
     * @param scene The scene to source data from
     */
    public void loadScene(BasicScene scene);

    /**
     * Unload the contents of this scene into the sensor manager. The call does
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
