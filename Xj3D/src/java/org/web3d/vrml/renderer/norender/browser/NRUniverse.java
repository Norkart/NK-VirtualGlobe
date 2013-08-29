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

package org.web3d.vrml.renderer.norender.browser;

// External imports
import java.util.*;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

// Local imports
import org.web3d.browser.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.core.loading.ContentLoadManager;
import org.xj3d.core.loading.ScriptLoader;

import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.xj3d.core.eventmodel.EventModelInitListener;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.LayerRenderingManager;


/**
 * Representation of a Null renderer universe object suitable for use in single
 * screen, stereo and walled environments.
 * <p>
 * Apart from the VirtualUniverse, this also holds all of the view information
 * model. In particular, this is shared between multiple
 * {@link org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas} instances to
 * enable multiple views of the same scenegraph or for stereo viewing.
 * <p>
 * The universe is responsible for handling the management of the viewpoints
 * within a scene. VRML defines a single viewpoint model for multiple canvases.
 * If we want to set this environment up for stereo rendering or CAVE type
 * environments, the renderer will need to use a single view for all of them.
 * If you are trying to perform multiple views of the scenegraph, such as an
 * editor environment, this universe is not suitable as it uses a single
 * View object and attaches all canvases to that view and hence the underlying
 * currently bound viewpoint.
 * <p>
 * The universe is not responsible for loading more VRML content. To handle
 * anchors, it delegates to the supplied listener.
 *
 * @author Justin Couch
 * @version $Revision: 1.27 $
 */
public class NRUniverse
    implements BrowserCore, EventModelInitListener {

    /** What we define to be an infinite visibility limit. */
    private static final float INFINITE_VIS_LIMIT = 10000;

    /** The time in milliseconds to move between two points */
    private static final int VP_TRANSITION_TIME = 2000;

    /** Description string of this world */
    private String worldDescription;

    /** The Scene we are currently working with */
    private VRMLScene currentScene;

    /** The next Scene to load */
    private VRMLScene nextScene;

    /** The space that represents the complete world we are running */
    private VRMLExecutionSpace currentSpace;

    /** List of the current DEF mappings */
    private Map defMap;

    /** Frame manager for the update cycles */
    private PerFrameManager frameManager;

    /** Event model evaluator to use */
    private EventModelEvaluator eventModel;

    /** The error errorReporter instance */
    private ErrorReporter errorReporter;

    /** The user input manager for this and all classes */
//    private UserInputBehavior userInput;

    /** handler of the smooth viewpoint transitions */
//    private ViewpointTransition vpTransition;

    /** The listeners for core content */
    private ArrayList coreListeners;

    /** The list of ViewpointStatus listeners */
    private ArrayList viewpointStatusListeners;

    /** The current mimimum frame cycle time. */
    private int frameCycleTime;

    /** The frame cycle time set by the end user. */
    private int userCycleTime;

    /** The frame cycle time set by the browser internals. */
    private int internalCycleTime;

    /**
     * Construct a default, empty universe that contains no scenegraph.
     *
     * @param eme The event model evaluation processor
     */
    public NRUniverse(EventModelEvaluator eme) {
        eventModel = eme;
        eventModel.setInitListener(this);

        defMap = Collections.EMPTY_MAP;
        coreListeners = new ArrayList();
        viewpointStatusListeners = new ArrayList(1);

        frameManager = new PerFrameManager(eventModel);
        frameManager.setEnable(false);
        frameManager.start();

        frameCycleTime = 0;
        userCycleTime = 0;
        internalCycleTime = 0;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by EventModelInitListener
    //----------------------------------------------------------

    /**
     * Notification from the event model evaluator that the
     * initialization phase is now complete. Use this to send off
     * the external Browser init event.
     */
    public void worldInitComplete() {
        fireInitEvent();
    }

    /**
     * Notification that its safe to clear the world.  The underlying
     * rendering layer should now be cleared and loaded with the
     * world.
     */
    public void changeWorld() {
        if (currentScene != null) {
            fireShutdownEvent();
            clearCurrentWorld();
        }

        currentScene = nextScene;

        if (currentScene == null)
            return;

        frameManager.setEnable(false);

        if(currentScene != null) {
            fireShutdownEvent();
            clearCurrentWorld();
        }

        defMap = currentScene.getDEFNodes();
        NRVRMLNode vrml_root = (NRVRMLNode)currentScene.getRootNode();

        currentSpace = (VRMLExecutionSpace)vrml_root;

        // Right, reset the time and let's go. Send the init event just before
        // the clock starts, as per EAI spec.
        VRMLClock clk = eventModel.getVRMLClock();
        clk.resetTimeZero();
        frameManager.setEnable(true);
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCore
    //----------------------------------------------------------

    /**
     * Get the type of renderer that implements the browser core. The only
     * valid values returned are the constants in this interface.
     *
     * @return The renderer type
     */
    public int getRendererType() {
        return Xj3DConstants.NULL_RENDERER;
    }

    /**
     * Get the ID string for this renderer.
     *
     * @return The String token for this renderer.
     */
    public String getIDString() {
        return Xj3DConstants.NULL_ID;
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

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Change the rendering style that the browser should currently be using
     * for all layers. Various options are available based on the constants
     * defined in this interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    public void setRenderingStyle(int style)
        throws IllegalArgumentException {
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    public int getRenderingStyle() {
        return Xj3DConstants.RENDER_SHADED;
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources taken up
     * by the 3D renderer. By default it will use all of them. The second
     * parameter is used to control whether this is a user-set hard minimum or
     * something set by the browser internals. User set values are always
     * treated as the minimum unless the browser internals set a value that is
     * a slower framerate than the user set. If the browser then sets a faster
     * framerate than the user set value, the user value is used instead.
     *
     * @param millis The minimum time in milleseconds.
     * @param userSet true if this is an end-user set minimum
     */
    public void setMinimumFrameInterval(int millis, boolean userSet) {
        if(userSet)
            userCycleTime = millis;
        else
            internalCycleTime = millis;

        frameCycleTime = (userCycleTime >= internalCycleTime) ?
                         userCycleTime : internalCycleTime;
    }

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The cycle interval time in milliseconds
     */
    public int getMinimumFrameInterval() {
        return frameCycleTime;
    }

    /**
     * Get the clock instance in use by the core. We need this for when
     * new nodes are added to the scene to make sure they are all appropriately
     * configured.
     *
     * @return The clock used by the browser core
     */
    public VRMLClock getVRMLClock() {
        return eventModel.getVRMLClock();
    }

    /**
     * Get the mapping of DEF names to the node instances that they represent.
     * Primarily used for the EAI functionality. The map instance changes each
     * time a new world is loaded so will need to be re-fetched. If no mappings
     * are available (eg scripting replaceWorld() type call) then the map will
     * be empty.
     *
     * @return The current mapping of DEF names to node instances
     */
    public Map getDEFMappings() {
        return defMap;
    }

    /**
     * Convenience method to ask for the execution space that the world is
     * currently operating in. Sometimes this is not known, particularly if
     * the end user has called a loadURL type function that is asynchronous.
     * This will change each time a new scene is loaded.
     *
     * @return The current world execution space.
     */
    public VRMLExecutionSpace getWorldExecutionSpace() {
        return currentSpace;
    }

    /**
     * Get the description string currently used by the world. Returns null if
     * not set or supported.
     *
     * @return The current description string or null
     */
    public String getDescription() {
        return worldDescription;
    }

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param desc The description string to set.
     */
    public void setDescription(String desc) {
        worldDescription = desc;
    }

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     */
    public float getCurrentSpeed() {
        return 0.0f;
    }

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     */
    public float getCurrentFrameRate() {
//        float frame_time = commonView.getLastFrameDuration() / 1000f;
//        return 1 / frame_time;
        return 0;
    }

    /**
     * Set the last frame render time used for FPS calculations.  Only the
     * per frame mamanger should call this.
     *
     * @param lastTime The time it took to render the last frame in milliseconds.
     */
    public void setLastRenderTime(long lastTime) {
        //lastRenderTime = lastTime;
    }

    /**
     * Set the eventModelStatus listener.
     *
     * @param l The listener.  Null will clear it.
     */
    public void setEventModelStatusListener(EventModelStatusListener l) {
//        frameManager.setEventModelStatusListener(l);
    }

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addNavigationStateListener(NavigationStateListener l) {
//        userInput.addNavigationStateListener(l);
    }

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeNavigationStateListener(NavigationStateListener l) {
//        userInput.removeNavigationStateListener(l);
    }

    /**
     * Add a listener for sensor state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addSensorStatusListener(SensorStatusListener l) {
//        userInput.addSensorStatusListener(l);
    }

    /**
     * Remove a sensor state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeSensorStatusListener(SensorStatusListener l) {
//        userInput.removeSensorStatusListener(l);
    }

    /**
     * Add a listener for viewpoint status changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addViewpointStatusListener(ViewpointStatusListener l) {
        if((l != null) && !viewpointStatusListeners.contains(l))
            viewpointStatusListeners.add(l);
    }

    /**
     * Remove a viewpoint state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeViewpointStatusListener(ViewpointStatusListener l) {
        viewpointStatusListeners.remove(l);
    }

    /**
     * Add an observer for a specific node type. A single instance may be
     * registered for more than one type. Each type registered will result in
     * a separate call per frame - one per type. If the observer is currently
     * added for this type ID, the request is ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to add
     */
    public void addNodeObserver(int nodeType, NodeObserver obs) {
        eventModel.addNodeObserver(nodeType, obs);
    }

    /**
     * Remove the given node observer instance for the specific node type. It
     * will not be removed for any other requested node types. If the instance
     * is not registered for the given node type ID, the request will be
     * silently ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to remove
     */
    public void removeNodeObserver(int nodeType, NodeObserver obs) {
        eventModel.removeNodeObserver(nodeType, obs);
    }

    /**
     * Notify the core that it can dispose all resources.  The core cannot be used for
     * rendering after that.
     */
    public void dispose() {
        fireDisposeEvent();
    }

    /**
     * Get the fully qualified URL of the currently loaded world. This returns
     * the entire URL including any possible arguments that might be associated
     * with a CGI call or similar mechanism. If the initial world is replaced
     * with <CODE>loadURL</CODE> then the string will reflect the new URL. If
     * <CODE>replaceWorld</CODE> is called then the URL still represents the
     * original world.
     *
     * @return A string of the URL or null if not supported.
     */
    public String getWorldURL() {
        String ret_val = null;

        if(currentScene != null)
            ret_val = currentScene.getWorldRootURL();

        return ret_val;
    }

    /**
     * Set the scene to use within this universe. If null, this will clear this
     * scene and de-register all listeners. The View will be detached from the
     * ViewPlatform and therefore the canvas will go blank.
     *
     * @param scene The new scene to load, or null
     * @param viewpoint The viewpoint.description to bind to or null for default
     */
    public void setScene(VRMLScene scene, String viewpoint) {
        nextScene = scene;
        eventModel.setScene(scene, viewpoint);

        frameManager.setEnable(true);
    }

    /**
     * Request that this viewpoint object is bound at the start of the next
     * frame. This method should only be called by external users such as
     * UI toolkits etc that need to synchronize the viewpoint change with
     * rendering loop, but are not able to synchronize themselves because they
     * exist on a different thread that cannot block.
     *
     * @param vp The new viewpoint instance to bind to
     */
    public void changeViewpoint(VRMLViewpointNodeType vp) {
        eventModel.changeViewpoint(vp);
    }

    /**
     * Add a listener for browser core events. These events are used to notify
     * all listeners of internal structure changes, such as the browser
     * starting and stopping. A listener can only be added once. Duplicate
     * requests are ignored.
     *
     * @param l The listener to add
     */
    public void addCoreListener(BrowserCoreListener l) {
        if((l != null) && !coreListeners.contains(l))
            coreListeners.add(l);
    }

    /**
     * Remove a browser core listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeCoreListener(BrowserCoreListener l) {
        coreListeners.remove(l);
    }

    /**
     * Request notification of profiling information.
     *
     * @param l The listener
     */
    public void addProfilingListener(ProfilingListener l) {
    }

    /**
     * Remove notification of profiling information.
     *
     * @param l The listener
     */
    public void removeProfilingListener(ProfilingListener l) {
    }

    /**
     * Set the navigation mode selected from the user interface.
     *
     * @param mode The new mode
     */
    public void setNavigationMode(String mode) {
        //userInput.setNavigationMode(mode);
    }

    /**
     * Get the user's location and orientation.  This will use the viewpoint
     * bound in the active layer.
     *
     * @param pos The current user position
     * @param ori The current user orientation
     */
    public void getUserPosition(Vector3f pos, AxisAngle4f ori) {
        //ignored
    }

    /**
     * Move the user's location to see the entire world.  Change the users
     * orientation to look at the center of the world.
     *
     * @param animated Should the transistion be animated.  Defaults to FALSE.
     */
    public void fitToWorld(boolean animated) {
        // ignored
    }

    /**
     * Sync UI updates with the Application thread.  This method alls the core
     * to push work off to the app thread.
     */
    public void syncUIUpdates() {
        // ignored
    }

    /**
     * Set the listener for navigation state change notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setNavigationStateListener(NavigationStateListener l) {
        // userInput.setNavigationStateListener(l);
    }

    /**
     * Send to the core listeners the error message that a URL failed to load
     * for some reason. This is for the EAI/ESAI spec conformance.
     *
     * @param msg The message to send
     */
    public void sendURLFailEvent(String msg) {
        fireFailedURL(msg);
    }

    //----------------------------------------------------------
    // Local methods.
    //----------------------------------------------------------

    /**
     * Get the scene that this universe is currently holding. If none is set
     * then null is returned.
     *
     * @return The currently set scene instance
     */
    public VRMLScene getScene() {
        return currentScene;
    }

    /**
     * Convenience method used to clear the current world. We have to remove
     * all of the list of viewpoints, remove the default viewpoint so we can
     * reuse it and then clear the stack information.
     */
    private void clearCurrentWorld() {

        frameManager.setEnable(false);

        currentSpace = null;

        eventModel.clear();

        // Now go through and kill all the inlines.
        clearInlines(currentScene);

//        userInput.setPickableScene(null);
//        userInput.setViewInfo(null, null);
    }

    /**
     * Walk the scene and remove all the inlines currently in use. Note that
     * this assumes all the inlines are held within the VRMLScene instance.
     * The method is recursive, walking down the tree looking to unbuild any
     * child nodes.
     */
    private void clearInlines(BasicScene scene) {

        // At the moment, the scripting objects  are not putting dynamically
        // created scenes into the VRMLScene object. That will need to be
        // looked into as they do play with the cache. Perhaps we also need to
        // add calls for the cache to go through and clean up all the items
        // held in it.

        // First, start with all the normal inlines:
        ArrayList node_list =
            scene.getByPrimaryType(TypeConstants.InlineNodeType);
        int size = node_list.size();
        VRMLScene sc;

        for(int i = 0; i < size; i++) {
            // First detach this inline from the parent scene graph
            NRVRMLNode node = (NRVRMLNode)node_list.get(i);

            // Next clear the root node of the scene because that is what
            // we really play with when adding them again next time. If it
            // doesn't contain a scene (hasn't loaded yet) then no need to
            // recurse further.
            sc = (VRMLScene)((VRMLInlineNodeType)node).getContainedScene();

            if(sc == null)
                continue;

            // now get this inline to clear any children inlines.
            clearInlines(sc);
        }

        // Now, fetch the proto instances and recurse through all of those
        // looking for inlines. Detach those as well.
        node_list = scene.getByPrimaryType(TypeConstants.ProtoInstance);
        size = node_list.size();

        for(int i = 0; i < size; i++) {
            VRMLProtoInstance proto = (VRMLProtoInstance)node_list.get(i);

            clearInlines(proto.getContainedScene());
        }
    }

    /**
     * Fire an initialised event to all the listeners. The listeners are given
     * the instance of currentScene.
     */
    private void fireInitEvent() {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l =
                    (BrowserCoreListener)coreListeners.get(i);

                l.browserInitialized(currentScene);
            } catch(Exception e) {
                System.out.println("Error sending init event " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire an event about a URL failing to load to all the listeners. The
     * listeners are given the instance of currentScene.
     */
    private void fireFailedURL(String msg) {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l =
                    (BrowserCoreListener)coreListeners.get(i);

                l.urlLoadFailed(msg);
            } catch(Exception e) {
                System.out.println("Error sending init event " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a shutdown event to all the listeners.
     */
    private void fireShutdownEvent() {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l =
                    (BrowserCoreListener)coreListeners.get(i);

                l.browserShutdown();
            } catch(Exception e) {
                System.out.println("Error sending init event " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a dispose event to all the listeners.
     */
    private void fireDisposeEvent() {
        int size = coreListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                BrowserCoreListener l =
                    (BrowserCoreListener)coreListeners.get(i);

                l.browserDisposed();
            } catch(Exception e) {
                System.out.println("Error sending init event " + e);
                e.printStackTrace();
            }
        }
    }
}
