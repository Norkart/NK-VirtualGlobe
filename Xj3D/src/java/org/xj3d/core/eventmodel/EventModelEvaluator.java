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
import org.web3d.vrml.nodes.*;

import org.web3d.browser.NodeObserver;
import org.web3d.util.ErrorReporter;

import org.xj3d.core.loading.ContentLoadManager;

/**
 * An abstract representation of a class that would be responsible for
 * performing all the event model computations and organisation on a per-frame
 * basis.
 * <p>
 *
 * This class allows extensibility of the event model implementation by both
 * providing an abstract view of the event model, but also allowing individual
 * node types to have their own managers that are dealt with by the event model.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface EventModelEvaluator {

    /**
     * Initialise the evaluator with the given managers.
     *
     * @param scripts The manager for loading scripts
     * @param router The manager for handling routes
     * @param sensors The manager for all sensors
     * @param fsm State manager for the frame
     * @param elm Manager for loading external content
     * @param vm Manager of viewpoint interactions
     * @param lmf Factory for producing new layer managers
     * @param lrm Manager for handling layer rendering
     * @param extManagers List of external managers to handle
     */
    public void initialize(ScriptManager scripts,
                           RouteManager router,
                           SensorManager sensors,
                           FrameStateManager fsm,
                           ContentLoadManager elm,
                           ViewpointManager vm,
                           LayerManagerFactory lmf,
                           LayerRenderingManager lrm,
                           NodeManager[] extManagers);

    /**
     * Add an observer for a specific node type. A single instance may be
     * registered for more than one type. Each type registered will result in
     * a separate call per frame - one per type. If the observer is currently
     * added for this type ID, the request is ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to add
     */
    public void addNodeObserver(int nodeType, NodeObserver obs);

    /**
     * Remove the given node observer instance for the specific node type. It
     * will not be removed for any other requested node types. If the instance
     * is not registered for the given node type ID, the request will be
     * silently ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to remove
     */
    public void removeNodeObserver(int nodeType, NodeObserver obs);

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
     * Add an external view to the evaluator. A view can only be added once.
     * If the view is added more than once, the second and subsequent calls
     * are ignored.
     *
     * @param view The new view to add
     */
    public void addExternalView(ExternalView view);

    /**
     * Remove the external view from this evaluator. If the view is not
     * currently registered, the request is silently ignored.
     *
     * @param view The new view to remove
     */
    public void removeExternalView(ExternalView view);

    /**
     * Get the layer manager for the the layer ID.
     *
     * @param id a value >= 0 that is the ID of the layer to get
     * @return The layer manager for it
     */
    public LayerManager getLayerManager(int id);

    /**
     * Get the VRMLClock instance in use by this evaluator.
     *
     * @return A reference to the clock
     */
    public VRMLClock getVRMLClock();

    /**
     * Run the event model for this frame now. This is a blocking call and
     * does not return until the event model is complete for this frame. The
     * time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    public void evaluate(long time);

    /**
     * Used to set the scene to the new content. Will automatically shutdown
     * the old scene. Assumes a valid scene instance is passed. Also assumes
     * that no clock ticks will be recieved during the setup phase.
     *
     * @param scene The new scene instance to use.
     * @param useView The initial viewpoint DEF name to bind to,
     *    Null means normal speced viewpoint.
     */
    public void setScene(VRMLScene scene, String useView);

    /**
     * Request that this viewpoint object is bound at the start of the next
     * frame. This method should only be called by external users such as
     * UI toolkits etc that need to synchronize the viewpoint change with
     * rendering loop, but are not able to synchronize themselves because they
     * exist on a different thread that cannot block.
     *
     * @param vp The new viewpoint instance to bind to
     */
    public void changeViewpoint(VRMLViewpointNodeType vp);

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear();

    /**
     * Get the script manager in use by the evaluator.
     *
     * @return The script manager implementation
     */
    public ScriptManager getScriptManager();

    /**
     * Get the route manager in use by the evaluator.
     *
     * @return The route manager implementation
     */
    public RouteManager getRouteManager();

    /**
     * Get the content loader in use by the evaluator.
     *
     * @return The content loader implementation
     */
    public ContentLoadManager getContentLoader();

    /**
     * Get the frame state manager in use by the evaluator.
     *
     * @return The frame state  manager implementation
     */
    public FrameStateManager getFrameStateManager();

    /**
     * Get the list of external node managers currently in use. If there are no
     * registered managers, returns null.
     *
     * @return An array of managers or null
     */
    public NodeManager[] getNodeManagers();

    /**
     * Set the listener for intialisation information. Setting null will
     * clear the current instance.
     *
     * @param l The listener instance to set
     */
    public void setInitListener(EventModelInitListener l);
}
