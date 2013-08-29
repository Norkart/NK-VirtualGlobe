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

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import java.util.*;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.aviatrix3d.*;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.j3d.aviatrix3d.management.RenderManager;
import org.j3d.aviatrix3d.management.DisplayCollection;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

// Local imports
import org.web3d.browser.*;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.input.*;

import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.xj3d.core.eventmodel.EventModelInitListener;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.LayerRenderingManager;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

/**
 * Representation of a core of a browser implemented using the OpenGL
 * rendering APIs.
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
 * The universe is not responsible for loading VRML/X3D content. To handle
 * anchors, it delegates to the supplied listener.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.34 $
 */
public class OGLStandardBrowserCore
    implements BrowserCore,
        EventModelInitListener,
        LayerRenderingManager {

    /** Error occurred sending disposed events message */
    private static final String DISPOSE_EVENT_ERR_MSG =
        "Error sending dispose event ";

    /** Error occurred sending shutdown events message */
    private static final String SHUTDOWN_EVENT_ERR_MSG =
        "Error sending shutdown event ";

    /** Error occurred sending failed URL events message */
    private static final String FAILED_URL_ERR_MSG =
        "URL failed to load ";

    /** Error occurred sending init events message */
    private static final String INIT_EVENT_ERR_MSG =
        "Error sending init event ";

    /** Framerate for paused mode */
    private static final int PAUSED_FPS = 4;

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
    private ApplicationFrameManager frameManager;

    /** The scene manager used for this node */
    private RenderManager sceneManager;

    /** Manager for the layers etc */
    private DisplayCollection displayManager;

    /** Event model evaluator to use */
    private EventModelEvaluator eventModel;

    /** The listeners for core content */
    private ArrayList coreListeners;

    /** The list of Profiling listeners */
    private ArrayList<ProfilingListener> profilingListeners;

    /** A group to hold the last vp group for later update */
    private TransformGroup vpTransformGroup;

    /** The last render timing */
    private long lastRenderTime;

    /**
     * Does the device require a field of view. 0 = no, otherwise use this
     * instead of content
     */
    private float hardwareFOV;

    /** The list of ViewpointStatus listeners */
    private ArrayList viewpointStatusListeners;

    /** The list of SensorStatus listeners */
    private ArrayList sensorStatusListeners;

    /** The list of NavigationStatus listeners */
    private ArrayList navigationStateListeners;

    /**
     * List of currently active AV3D layer objects. This is the reordered
     * version of allLayers, after input from the LayerSet object.
     */
    private SimpleLayer[] activeLayers;

    /** The current render order */
    private int[] renderOrder;

    /** List of currently all AV3D layer objects */
    private SimpleLayer[] allLayers;

    /** Collection of layer managers that are active */
    private OGLLayerManager[] allManagers;

    /** The number of active layers current */
    private int numActiveLayers;

    /** Viewport manager for all the layers */
    private ViewportResizeManager viewportManager;

    /** Viewport manager for all the layers */
    private ViewpointResizeManager viewpointManager;

    /** The error errorReporter instance */
    private ErrorReporter errorReporter;

    /** Global rendering style currently set */
    private int globalRenderingStyle;

    /** Is stereo enabled */
    private boolean stereoEnabled;

    /** The next navigation mode */
    private String nextNavigationMode;

    /** Should we fit to world on the next App call */
    private boolean fitToWorldChanged;

    /** Should we animate the next fit to world */
    private boolean animateFitToWorld;

    /** Is there is a new hardwareFOV */
    private boolean hardwareFOVChanged;

    /** The next hardware FOV */
    private float nextHardwareFOV;

    /** Did the stereoEnabled flag change */
    private boolean stereoEnabledChanged;

    /** The next stereo enabled */
    private boolean nextStereoEnabled;

    /** Should we take a screen capture */
    private boolean screenCaptureOnceChanged;

    /** Should we take a screen capture continually */
    private boolean screenCaptureStartChanged;

    /** Should we stop taking a screen capture continually */
    private boolean screenCaptureEndChanged;

    /** The screen capture listener */
    private ScreenCaptureListener screenCaptureListener;

    /** The screen surface width */
    private int screenCaptureWidth;

    /** The screen surface height */
    private int screenCaptureHeight;

    /** The frame cycle interval set, -1 if unset */
    private int frameCycleTime;

    /** The frame cycle time set by the end user. */
    private int userCycleTime;

    /** The frame cycle time set by the browser internals. */
    private int internalCycleTime;

    /**
     * Construct a default, empty universe that contains no scenegraph.
     *
     * @param eme The class used to evaluate the event model
     * @param sgm The manager of the scene graph
     */
    public OGLStandardBrowserCore(EventModelEvaluator eme,
                                  RenderManager sgm,
                                  DisplayCollection dc) {
        eventModel = eme;
        eventModel.setInitListener(this);

        sceneManager = sgm;
        displayManager = dc;

        SimpleViewport viewport = new SimpleViewport();

        allManagers = new OGLLayerManager[1];

        activeLayers = new SimpleLayer[1];
        activeLayers[0] = new SimpleLayer();
        activeLayers[0].setViewport(viewport);

        allLayers = new SimpleLayer[1];
        allLayers[0] = activeLayers[0];

        numActiveLayers = 0;

        viewportManager = new ViewportResizeManager();
        viewportManager.addFullscreenViewport(viewport);

        viewpointManager = new ViewpointResizeManager();

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        defMap = Collections.EMPTY_MAP;
        coreListeners = new ArrayList();

        sensorStatusListeners = new ArrayList(1);
        viewpointStatusListeners = new ArrayList(1);
        navigationStateListeners = new ArrayList(1);
        profilingListeners = new ArrayList(1);

        hardwareFOV = 0f;
        globalRenderingStyle = Xj3DConstants.RENDER_SHADED;

        frameManager = new ApplicationFrameManager(eventModel,
                                                   this,
                                                   viewportManager,
                                                   viewpointManager);

        sceneManager.setApplicationObserver(frameManager);

        frameCycleTime = 0;
        userCycleTime = 0;
        internalCycleTime = 0;

        // Throttle till content is loaded
        sceneManager.setMinimumFrameInterval(2000);
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
        if (frameCycleTime < 0)
            sceneManager.setMinimumFrameInterval(0);
        else
            sceneManager.setMinimumFrameInterval(frameCycleTime);

        fireInitEvent();
    }

    /**
     * Notification that its safe to clear the world.  The underlying
     * rendering layer should now be cleared and loaded with the
     * world.
     */
    public void changeWorld() {
        if(currentScene != null) {
            // TODO: Can't disable as changeWorld in VRMLUniverse expects events, just throttle
            sceneManager.setMinimumFrameInterval(1000 / PAUSED_FPS);

            fireShutdownEvent();
        }

        currentScene = nextScene;

        if (currentScene != null)
            currentSpace = (VRMLExecutionSpace)currentScene.getRootNode();
        nextScene = null;
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
        return Xj3DConstants.OPENGL_RENDERER;
    }

    /**
     * Get the ID string for this renderer.
     *
     * @return The String token for this renderer.
     */
    public String getIDString() {
        return Xj3DConstants.OPENGL_ID;
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

        int old = frameCycleTime;

        frameCycleTime = (userCycleTime >= internalCycleTime) ?
                         userCycleTime : internalCycleTime;

        if(frameCycleTime != old)
            sceneManager.setMinimumFrameInterval(frameCycleTime);
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
        return sceneManager.getMinimumFrameInterval();
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
        float frame_time = lastRenderTime / 1000f;
        return 1 / frame_time;
    }

    /**
     * Set the last frame render time used for FPS calculations.  Only the
     * per frame mamanger should call this.
     *
     * @param lastTime The time it took to render the last frame in milliseconds.
     */
    public void setLastRenderTime(long lastTime) {
        lastRenderTime = lastTime;
    }

    /**
     * Set the eventModelStatus listener.
     *
     * @param l The listener.  Null will clear it.
     */
    public void setEventModelStatusListener(EventModelStatusListener l) {
        frameManager.setEventModelStatusListener(l);
    }

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addNavigationStateListener(NavigationStateListener l) {
        if(l == null)
            return;

        if(!navigationStateListeners.contains(l))
            navigationStateListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addNavigationStateListener(l);
    }

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeNavigationStateListener(NavigationStateListener l) {
        if(l == null)
            return;

        navigationStateListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeNavigationStateListener(l);
    }

    /**
     * Add a listener for sensor state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addSensorStatusListener(SensorStatusListener l) {
        if(l == null)
            return;

        if(!sensorStatusListeners.contains(l))
            sensorStatusListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addSensorStatusListener(l);
    }

    /**
     * Remove a sensor state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeSensorStatusListener(SensorStatusListener l) {
        if(l == null)
            return;

        sensorStatusListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeSensorStatusListener(l);
    }

    /**
     * Add a listener for viewpoint status changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addViewpointStatusListener(ViewpointStatusListener l) {
        if(l == null)
            return;

        if(!viewpointStatusListeners.contains(l))
            viewpointStatusListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addViewpointStatusListener(l);
    }

    /**
     * Remove a viewpoint state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeViewpointStatusListener(ViewpointStatusListener l) {
        if(l == null)
            return;

        viewpointStatusListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeViewpointStatusListener(l);
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
        else {
            ret_val = (String)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        String ret_val = System.getProperty("user.dir");

                        // Do not use URL.toExternalForm as its file:/ is not right for us
                        ret_val = ret_val.replace('\\', '/');
                        ret_val = "file:///" + ret_val;

                        return ret_val;
                    }
                }
            );
        }

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

        // This call has moved to outside core to align with addNotify timing
//        sceneManager.setEnabled(true);
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
        if(l == null)
            return;

        if(!profilingListeners.contains(l))
            profilingListeners.add(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].addProfilingListener(l);
    }

    /**
     * Remove notification of profiling information.
     *
     * @param l The listener
     */
    public void removeProfilingListener(ProfilingListener l) {
        if(l == null)
            return;

        if(!profilingListeners.contains(l))
            profilingListeners.remove(l);

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].removeProfilingListener(l);
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

    /**
     * Set the desired navigation mode. The mode string is one of the
     * spec-defined strings for the NavigationInfo node in the VRML/X3D
     * specification.
     *
     * @param mode The requested mode.
     */
    public void setNavigationMode(String mode) {
        nextNavigationMode = mode;
    }

    /**
     * Get the user's location and orientation.  This will use the viewpoint
     * bound in the active layer.
     *
     * @param pos The current user position
     * @param ori The current user orientation
     */
    public void getUserPosition(Vector3f pos, AxisAngle4f ori) {
        LayerManager mgr = getActiveNavLayer();
        mgr.getUserPosition(pos, ori);
    }

    /**
     * Move the user's location to see the entire world.  Change the users
     * orientation to look at the center of the world.
     *
     * @param animated Should the transistion be animated.  Defaults to FALSE.
     */
    public void fitToWorld(boolean animated) {
        fitToWorldChanged = true;
        animateFitToWorld = animated;
    }

    /**
     * Sync UI updates with the Application thread.  This method alls the core
     * to push work off to the app thread.
     */
    public void syncUIUpdates() {
        if (nextNavigationMode != null) {
            LayerManager mgr = getActiveNavLayer();

            mgr.setNavigationMode(nextNavigationMode);

            nextNavigationMode = null;
        }

        if (fitToWorldChanged) {
            LayerManager mgr = getActiveNavLayer();

            mgr.fitToWorld(animateFitToWorld);

            fitToWorldChanged = false;
        }

        if (hardwareFOVChanged) {
            hardwareFOV = nextHardwareFOV;

            for(int i = 0; i < numActiveLayers; i++)
                allManagers[i].setHardwareFOV(hardwareFOV);
            hardwareFOVChanged = false;

        }

        if (stereoEnabledChanged) {
            stereoEnabled = nextStereoEnabled;

            for(int i = 0; i < numActiveLayers; i++)
                allManagers[i].setStereoEnabled(stereoEnabled);

            stereoEnabledChanged = false;
        }

        if (screenCaptureOnceChanged) {
            ((OGLLayerManager)allManagers[renderOrder[renderOrder.length - 1]]).captureScreenOnce(screenCaptureListener, screenCaptureWidth, screenCaptureHeight);
            screenCaptureOnceChanged = false;
            screenCaptureListener = null;
        }
        if (screenCaptureStartChanged) {
            ((OGLLayerManager)allManagers[renderOrder[renderOrder.length - 1]]).captureScreenStart(screenCaptureListener, screenCaptureWidth, screenCaptureHeight);
            screenCaptureStartChanged = false;
            screenCaptureListener = null;
        }
        if (screenCaptureEndChanged) {
            ((OGLLayerManager)allManagers[renderOrder[renderOrder.length - 1]]).captureScreenEnd();
            screenCaptureEndChanged = false;
        }
    }

    //----------------------------------------------------------
    // Methods defined by LayerRenderingManager
    //----------------------------------------------------------

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

        for(int i = 0; i < numActiveLayers; i++)
            allManagers[i].setRenderingStyle(style);

        globalRenderingStyle = style;
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    public int getRenderingStyle() {
        return globalRenderingStyle;
    }

    /**
     * Change the rendering style that the browser should currently be using
     * for for a specific layer. Various options are available based on the
     * constants defined in this interface.
     *
     * @param style One of the RENDER_* constants
     * @param layerId The ID of the layer that should have the style changed
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    public void setRenderingStyle(int style, int layerId)
        throws IllegalArgumentException {

        allManagers[layerId].setRenderingStyle(style);
    }

    /**
     * Get the currently set rendering style for a specific layer. The default
     * style is RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    public int getRenderingStyle(int layerId) {
        return allManagers[layerId].getRenderingStyle();
    }

    /**
     * Set the list of current layers that should be rendered.
     *
     * @param layers The list of layer managers to be rendered
     * @param numLayers The number of active items in the list
     */
    public void setActiveLayers(LayerManager[] layers, int numLayers) {
        // No need to clear out the old listeners from the existing layers
        // becase we assume that the LayerManager.clear() call did that
        // for us.
        if(allLayers.length < numLayers) {
            allManagers = new OGLLayerManager[numLayers];
            activeLayers = new SimpleLayer[numLayers];
            allLayers = new SimpleLayer[numLayers];
        }

        viewportManager.clear();

        for(int i = 0; i < numLayers; i++) {
            OGLLayerManager l = (OGLLayerManager)layers[i];
            l.setViewpointResizeManager(viewpointManager);
            l.setHardwareFOV(hardwareFOV);
            l.setStereoEnabled(stereoEnabled);
            allManagers[i] = l;
            allLayers[i] = l.getLayer();

            l.setRenderingStyle(globalRenderingStyle);

            Viewport vp = allLayers[i].getViewport();
            VRMLViewportNodeType node = l.getViewport();

            switch(l.getViewportType()) {
                case LayerManager.VIEWPORT_UNDEFINED:
                    // Do nothing for this case as we have nothing useful.
                    break;

                case LayerManager.VIEWPORT_FULLWINDOW:
                    viewportManager.addFullscreenViewport(vp);
                    break;

                case LayerManager.VIEWPORT_PROPORTIONAL:
                    viewportManager.addProportionalViewport(node, vp);
                    break;

                case LayerManager.VIEWPORT_FIXED:
                    viewportManager.addFixedViewport(node, vp);
                    break;

                case LayerManager.VIEWPORT_CUSTOM:
                    viewportManager.addCustomViewport(node, vp);
                    break;
            }

            // Add all the listeners back into this layer
            for(int j = 0; j < sensorStatusListeners.size(); j++) {
                SensorStatusListener sl =
                    (SensorStatusListener)sensorStatusListeners.get(j);

                l.addSensorStatusListener(sl);
            }

            for(int j = 0; j < navigationStateListeners.size(); j++) {
                NavigationStateListener sl =
                    (NavigationStateListener)navigationStateListeners.get(j);

                l.addNavigationStateListener(sl);
            }

            for(int j = 0; j < profilingListeners.size(); j++) {
                ProfilingListener sl =
                    (ProfilingListener)profilingListeners.get(j);

                l.addProfilingListener(sl);
            }

            for(int j = 0; j < viewpointStatusListeners.size(); j++) {
                ViewpointStatusListener sl =
                    (ViewpointStatusListener)viewpointStatusListeners.get(j);

                l.addViewpointStatusListener(sl);
            }
        }

        numActiveLayers = numLayers;
    }

    /**
     * Set the rendering order for all the layers on this manager
     *
     * @param order The index of the list of rendered layers ids
     * @param numValid The number of valid items in the order list
     */
    public void setRenderOrder(int[] order, int numValid) {
        int idx = 0;

        renderOrder = order;

        for(int i = 0; i < numValid; i++) {
            // Check for valid order values. Anything invalid we just skip and
            // ignore. We'll assume the LayerSet node has already issued an
            // error message about this, so we just silently continue on.
            if(order[i] < activeLayers.length)
                activeLayers[idx++] = allLayers[order[i]];
        }

        displayManager.setLayers(activeLayers, idx);
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown() {
    }

    /**
     * Force clearing all currently managed layers from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear() {
        displayManager.setLayers(null, 0);

        // Clear local references too. Don't call clear() on the layer here
        // as that will be called before this method is called. just clear the
        // local references.
        for(int i = 0; i < allManagers.length; i++)
            allManagers[i] = null;

        for(int i = 0; i < activeLayers.length; i++) {
            activeLayers[i] = null;
            allLayers[i] = null;
        }
        viewpointManager.clear();
        viewportManager.clear();
    }

    //----------------------------------------------------------
    // Local methods.
    //----------------------------------------------------------

    /**
     * Return the display manager
     *
     * @return The DisplayCollection object
     */
    public DisplayCollection getDisplayManager( ) {
        return( displayManager );
    }

    /**
     * Capture the screen on the next render.
     *
     * @param listener Listener for capture results
     * @param width The screen width
     * @param height The screen height
     */
    public void captureScreenOnce(ScreenCaptureListener listener, int width, int height) {
        screenCaptureOnceChanged = true;
        screenCaptureListener = listener;
        screenCaptureWidth = width;
        screenCaptureHeight = height;
    }

    /**
     * Capture the screen on each render.
     *
     * @param listener Listener for capture results
     * @param width The screen width
     * @param height The screen height
     */
    public void captureScreenStart(ScreenCaptureListener listener, int width, int height) {
        screenCaptureStartChanged = true;
        screenCaptureListener = listener;
        screenCaptureWidth = width;
        screenCaptureHeight = height;
    }

    /**
     * Stop capturing the screen on each render.
     */
    public void captureScreenEnd() {
        screenCaptureEndChanged = true;
    }

    /**
     * Get the graphics resize listener instance used by this universe, when
     * managing layers. The listener can be registered with the drawing surfac
     * and used to control the viewport layers internally generated. This
     * instance does not change over time.
     *
     * @return The listener instance used internally
     */
    public GraphicsResizeListener[] getGraphicsResizeListeners() {
        return new GraphicsResizeListener[] {viewportManager, viewpointManager};
    }

    /**
     * Override the file field of view values with a value that suits
     * the given output device. A value of 0 = no, otherwise use this
     * instead of content
     *
     * @param fov The fov in degrees.
     */
    public void setHardwareFOV(float fov) {
        hardwareFOVChanged = true;
        nextHardwareFOV = fov;
    }

    /**
     * Set whether stereo is enabled for all layers.
     */
    public void setStereoEnabled(boolean enabled) {
        nextStereoEnabled = enabled;
        stereoEnabledChanged = true;
    }

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
     * Get the currently selected viewpoint in the active navigation layer.
     * If there is no scene set then this returns null.
     *
     * @return The current active viewpoint
     */
    public VRMLViewpointNodeType getViewpoint() {
        VRMLViewpointNodeType ret_val = null;

        LayerManager active = getActiveNavLayer();

        return ret_val;
    }

    /**
     * Go through all the layers and find the active navigation layer.
     */
    private LayerManager getActiveNavLayer() {
        LayerManager ret_val = null;

        for(int i = 0; i < allManagers.length; i++) {
            if(allManagers[i].isActiveNavigationLayer()) {
                ret_val = allManagers[i];
                break;
            }
        }

        return ret_val;
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
                errorReporter.errorReport(INIT_EVENT_ERR_MSG, e);
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
                errorReporter.errorReport(FAILED_URL_ERR_MSG, e);
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
                errorReporter.errorReport(SHUTDOWN_EVENT_ERR_MSG, e);
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
                errorReporter.errorReport(DISPOSE_EVENT_ERR_MSG, e);
            }
        }
    }
}
