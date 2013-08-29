/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
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
import java.util.List;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * An abstract representation of a class that would be responsible for
 * performing Viewpoint management.
 * <p>
 *
 * This interface represents a further abstracted view of viewpoint management
 * handling beyond the {@link org.web3d.browser.BrowserCore}. This gives you
 * all the handling that is normally seen at a user interface level. You should
 * use one or the other, but not both as implementations of this class will
 * also interact with BrowserCore.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public interface ViewpointManager {

    /**
     * Update the viewpoint. Called at the beginning of the event model.
     *
     * @param time The time of the current event model.
     */
    public void updateViewpoint(long time);

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown();

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
     * Set the current viewpoint to an arbitrary instance.
     *
     * @param viewpoint The new current viewpoint.
     */
    public void setViewpoint(VRMLViewpointNodeType viewpoint);

    /**
     * Go to the first declared viewpoint at the next available oppourtunity.
     * This corresponds to the SAI Browser.firstViewpoint() call.
     */
    public void firstViewpoint();

    /**
     * Go to the first declared viewpoint int the specified layer at the
     * next available oppourtunity. This corresponds to the SAI
     * Browser.firstViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void firstViewpoint(int layer);

    /**
     * Go to the last declared viewpoint at the next available oppourtunity.
     * This corresponds to the SAI Browser.lastViewpoint() call.
     */
    public void lastViewpoint();

    /**
     * Go to the last declared viewpoint of the specified layer at the next
     * available oppourtunity. This corresponds to the SAI
     * Browser.lastViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void lastViewpoint(int layer);

    /**
     * Reset the viewpoint back to its original values.
     */
    public void resetViewpoint();

    /**
     * Go to the next viewpoint at the next available oppourtunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * next index it can find. This corresponds to the SAI
     * Browser.nextViewpoint() call.
     */
    public void nextViewpoint();

    /**
     * Go to the next viewpoint at the next available oppourtunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * next index it can find for the specific layer ID. This corresponds to the
     * SAI Browser.previousViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void nextViewpoint(int layer);

    /**
     * Go to the previous viewpoint at the next available oppourtunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * previous index it can find. This corresponds to the SAI
     * Browser.previousViewpoint() call.
     */
    public void previousViewpoint();

    /**
     * Go to the previous viewpoint at the next available oppourtunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * previous index it can find for the specific layer ID. This corresponds to the
     * SAI Browser.previousViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    public void previousViewpoint(int layer);

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear();

    /**
     * Gets the viewpoints for the currently active layer.
     *
     * @return A list of the viewpoint nodes
     */
    public List<VRMLViewpointNodeType> getActiveViewpoints();

    /**
     * Add a listener for viewpoint status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addViewpointListener(ViewpointStatusListener l);

    /**
     * Remove a listener for viewpoint status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeViewpointListener(ViewpointStatusListener l);
}

