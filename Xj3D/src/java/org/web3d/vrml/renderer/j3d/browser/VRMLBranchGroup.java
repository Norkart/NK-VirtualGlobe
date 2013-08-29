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

package org.web3d.vrml.renderer.j3d.browser;

// External imports
import javax.media.j3d.*;

// Have to name these individually because the Locale class clashes
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.j3d.renderer.java3d.navigation.ViewpointTransition;

// Local imports
import org.web3d.browser.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.input.*;

import org.xj3d.core.eventmodel.*;

import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.UnsupportedProfileException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.renderer.j3d.J3DNodeFactory;
import org.web3d.vrml.renderer.j3d.nodes.J3DViewpointNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DBackgroundNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DFogNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

import org.xj3d.core.loading.ContentLoadManager;
import org.xj3d.core.loading.ScriptLoader;

/**
 * Representation of a complete VRML runtime system that is a encapsulated
 * within a branch group.
 * <p>
 *
 * Apart from the VirtualUniverse, this also holds all of the view information
 * model. In particular, this is shared between multiple
 * {@link org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas} instances to
 * enable multiple views of the same scenegraph or for stereo viewing.
 * <p>
 *
 * The group is responsible for handling the management of the viewpoints
 * within a scene. VRML defines a single viewpoint model for multiple canvases.
 * If we want to set this environment up for stereo rendering or CAVE type
 * environments, the renderer will need to use a single view for all of them.
 * If you are trying to perform multiple views of the scenegraph, such as an
 * editor environment, this universe is not suitable as it uses a single
 * View object and attaches all canvases to that view and hence the underlying
 * currently bound viewpoint.
 * <p>
 * The group is not responsible for loading more VRML content. To handle
 * anchors, it delegates to the supplied listener.
 *
 * @author Justin Couch
 * @version $Revision: 1.42 $
 */
public class VRMLBranchGroup extends BranchGroup
    implements BrowserCore, BindableNodeListener, EventModelInitListener {

    /**
     * What we define to be an infinite visibility limit. J3D likes to have a
     * value other than zero. Unfortunately, it doesn't have the equivalent
     * setting of infinite that the VRML spec wants. So we take a guess at the
     * value for something that we consider "infinite".
     */
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

    /** Load manager for scripts */
    private ScriptLoader scriptLoader;

    /** Load manager for loading external content */
    private ContentLoadManager loadManager;

    /** Manager for route propogation */
    private RouteManager routeManager;

    /** The View that we use for everything */
    private View commonView;

    /** The current viewpoint node we are playing with */
    private J3DViewpointNodeType currentViewpoint;

    /** The current navigation info we are running with */
    private VRMLNavigationInfoNodeType currentNavInfo;

    /** The body that our avatar uses */
    private PhysicalBody avatarBody;

    /** The environment our avatar exists in */
    private PhysicalEnvironment avatarEnvironment;

    /**
     * The branchgroup holding the common items for all scenes. In here you
     * will find the default viewpoint and the VRML clock used for timing
     * purposes. Once set, these never change.
     */
    private BranchGroup commonSceneGraph;

    /**
     * The branchgroup that contains all the runtime, dynamic nodes that
     * represent the "real" running content. The children of this BG will
     * be removed, updated and changed as needed.
     */
    private BranchGroup contentSceneGraph;

    /** The node stack for viewpoints */
    private BindableNodeManager viewpointStack;

    /** The node stack for navigation information */
    private BindableNodeManager navInfoStack;

    /** The node stack for navigation information */
    private BindableNodeManager backgroundStack;

    /** The node stack for navigation information */
    private BindableNodeManager fogStack;

    /** A list of the canvases registered with this universe */
    private ArrayList viewList;

    /** The user input manager for this and all classes */
    private J3DUserInputHandler userInput;

    /** Sensor manager of Java3D nodes */
    private J3DSensorManager sensorManager;

    /** handler of the smooth viewpoint transitions */
    private ViewpointTransition vpTransition;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** The listeners for core content */
    private ArrayList coreListeners;

    /** The common view platform */
    private GlobalEffectsGroup globalEffects;

    /** Flag describing whether the world is static or not */
    private boolean isStatic;

    /** Event model evaluator to use */
    private EventModelEvaluator eventModel;

    /** Ticker used to drive the event model each frame */
    private PerFrameBehavior frameTicker;

    /** The last render timing */
    private long lastRenderTime;

    /**
     * Construct a default, empty universe that contains no scenegraph. A flag
     * is provided so that you may give the code a hint as to how the branch
     * group will be used. The most common user of this code will be the Java3D
     * loaders, which may use this as a purely static geometry setup and does
     * not need any of the dynamic behaviours like bindable nodes, navigation
     * etc.
     *
     * @param staticOnly true if this is only used to load static geometry
     * @param eme The evaluator for the event model
     * @throws IllegalArgumentException The evaluator does not contain
     *    appropriate managers for Java3D handler
     */
    public VRMLBranchGroup(boolean staticOnly, EventModelEvaluator eme) {

        isStatic = staticOnly;
        eventModel = eme;
        eventModel.setInitListener(this);

        defMap = Collections.EMPTY_MAP;
        coreListeners = new ArrayList();
        globalEffects = new GlobalEffectsGroup(staticOnly);

        if(!isStatic) {
            viewList = new ArrayList();
            vpTransition = new ViewpointTransition();
            frameTicker = new PerFrameBehavior(eventModel, this);
        }

        SensorManager s_mgr = eventModel.getSensorManager();

        if(!(s_mgr instanceof J3DSensorManager))
            throw new IllegalArgumentException("Not a Java3D sensor manager");

        sensorManager = (J3DSensorManager)s_mgr;
        sensorManager.setGlobalEffectsHandler(globalEffects);
        userInput = (J3DUserInputHandler) sensorManager.getUserInputHandler();

        J3DNodeFactory fac = J3DNodeFactory.getJ3DNodeFactory();

        try {
            fac.setSpecVersion(3, 1);
            fac.setProfile("Interactive");
            fac.addComponent("EnvironmentalEffects", 2);
        } catch(UnsupportedProfileException upe) {
            // ignore
        }

        J3DViewpointNodeType def_vp =
            (J3DViewpointNodeType)fac.createVRMLNode("Viewpoint", false);

        def_vp.setDescription("Default viewpoint");
        def_vp.setupFinished();

        VRMLNavigationInfoNodeType def_ni =
            (VRMLNavigationInfoNodeType)fac.createVRMLNode("NavigationInfo", false);
        def_ni.setupFinished();

        J3DBackgroundNodeType def_bg =
            (J3DBackgroundNodeType)fac.createVRMLNode("Background", false);
        def_bg.setupFinished();

        J3DFogNodeType def_fog =
            (J3DFogNodeType)fac.createVRMLNode("Fog", false);
        def_fog.setFogType(VRMLFogNodeType.FOG_TYPE_DISABLE);
        def_fog.setupFinished();

        eventModel.setDefaultBindables(def_vp, def_ni, def_bg, def_fog);

        viewpointStack =
            eventModel.getBindableManager(TypeConstants.ViewpointNodeType);

        navInfoStack =
            eventModel.getBindableManager(TypeConstants.NavigationInfoNodeType);

        Node vp = (Node)def_vp.getSceneGraphObject();
        Node back = (Node)def_bg.getSceneGraphObject();
        Node fog = (Node)def_fog.getSceneGraphObject();

        contentSceneGraph = new BranchGroup();
        contentSceneGraph.setCapability(Group.ALLOW_CHILDREN_WRITE);
        contentSceneGraph.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        // Has to be added to allow VWorld calcs
        commonSceneGraph = new BranchGroup();
        commonSceneGraph.addChild(vp);
        commonSceneGraph.addChild(back);
        commonSceneGraph.addChild(fog);
        commonSceneGraph.addChild(globalEffects);

        if(!isStatic)
            commonSceneGraph.addChild(frameTicker);

        addChild(commonSceneGraph);
        addChild(contentSceneGraph);

        viewpointStack.setNodeChangeListener(this);
        navInfoStack.setNodeChangeListener(this);

        inSetup = false;
    }

    //----------------------------------------------------------
    // Methods required by the EventModelInitListener interface.
    //----------------------------------------------------------

    /**
     * Notification from the event model evaluator that the
     * initialization phase is now complete. Use this to send off
     * the external Browser init event.
     */
    public void worldInitComplete() {
        VRMLClock clk = eventModel.getVRMLClock();
        double time = clk.getTime();

        fireInitEvent();

        currentNavInfo.setBind(true, true, time);
        currentViewpoint.setBind(true, true, time);

        inSetup = false;
    }

    /**
     * Notification that its safe to clear the world.  The underlying
     * rendering layer should now be cleared and loaded with the
     * world.
     */
    public void changeWorld() {
        if(isStatic)
            return;

        if(currentScene != null) {
            fireShutdownEvent();
            clearCurrentWorld();
        }

        currentScene = nextScene;

        if (currentScene == null)
            return;

        defMap = currentScene.getDEFNodes();
        J3DVRMLNode vrml_root = (J3DVRMLNode)currentScene.getRootNode();
        BranchGroup root_node = (BranchGroup)vrml_root.getSceneGraphObject();

        currentSpace = (VRMLExecutionSpace)vrml_root;

        userInput.setPickableScene(root_node);
        sensorManager.setWorldRoot(root_node);

        contentSceneGraph.addChild(root_node);

        setupView();

        //loadManager.queueSceneLoad(currentSpace);
        //scriptLoader.loadScripts(currentSpace);

        VRMLClock clk = eventModel.getVRMLClock();
        clk.resetTimeZero();

        frameTicker.setEnable(true);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLBindableNodeListener interface.
    //----------------------------------------------------------

    /**
     * Notification that a binding stack has requested that this node be now
     * bound as the active node.
     *
     * @param src The source node that is to be bound
     */
    public void newNodeBound(VRMLBindableNodeType src) {

        // We assume this would only be called if it is a non-static world.

        int type = src.getPrimaryType();
        if(src instanceof J3DViewpointNodeType) {
            // Manage the transition between the viewpoints
            changeViewpoints((J3DViewpointNodeType)src);
        } else if(src instanceof VRMLNavigationInfoNodeType) {
            changeNavInfo((VRMLNavigationInfoNodeType)src);
        }
    }

    /**
     * Notification that a new bindable has been added.
     *
     * @param node The new node
     */
    public void bindableAdded(VRMLBindableNodeType node, boolean isDefault) {
        // ignored
    }

    /**
     * Notification that a bindable has been removed.
     *
     * @param node The node
     */
    public void bindableRemoved(VRMLBindableNodeType node) {
        // ignored
    }

    //----------------------------------------------------------
    // Methods required by the BrowserCore interface.
    //----------------------------------------------------------

    /**
     * Get the type of renderer that implements the browser core. The only
     * valid values returned are the constants in this interface.
     *
     * @return The renderer type
     */
    public int getRendererType() {
        return Xj3DConstants.JAVA3D_RENDERER;
    }

    /**
     * Get the ID string for this renderer.
     *
     * @return The String token for this renderer.
     */
    public String getIDString() {
        return Xj3DConstants.JAVA3D_ID;
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
        frameTicker.setEventModelStatusListener(l);
    }

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addNavigationStateListener(NavigationStateListener l) {
        userInput.addNavigationStateListener(l);
    }

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeNavigationStateListener(NavigationStateListener l) {
        userInput.removeNavigationStateListener(l);
    }

    /**
     * Add a listener for sensor state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addSensorStatusListener(SensorStatusListener l) {
        userInput.addSensorStatusListener(l);
    }

    /**
     * Remove a sensor state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeSensorStatusListener(SensorStatusListener l) {
        userInput.removeSensorStatusListener(l);
    }

    /**
     * Add a listener for viewpoint status changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    public void addViewpointStatusListener(ViewpointStatusListener l) {
        // ignored
    }

    /**
     * Remove a viewpoint state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    public void removeViewpointStatusListener(ViewpointStatusListener l) {
        // ignored
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
                        return System.getProperty("user.dir");
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
        inSetup = true;

        if(!isStatic) {
            nextScene = scene;
            eventModel.setScene(scene, viewpoint);

            if(frameTicker != null)
                frameTicker.setEnable(true);

            return;
        }

        if(currentScene != null) {
            fireShutdownEvent();
            clearCurrentWorld();
        }

        currentScene = scene;

        defMap = currentScene.getDEFNodes();
        J3DVRMLNode vrml_root = (J3DVRMLNode)currentScene.getRootNode();
        BranchGroup root_node = (BranchGroup)vrml_root.getSceneGraphObject();

        currentSpace = (VRMLExecutionSpace)vrml_root;

        eventModel.setScene(scene, viewpoint);
        contentSceneGraph.addChild(root_node);

        setupView();

        // Now we have everything set, let's start queuing content to load:
        currentSpace = (VRMLExecutionSpace)vrml_root;
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
     * Get the currently selected viewpoint. If there is no scene set then this
     * returns null.
     *
     * @return The current active viewpoint
     */
    public J3DViewpointNodeType getViewpoint() {
        return currentViewpoint;
    }

    /**
     * Grab the current top level behaviours used by this branchgroup. These are
     * the behaviours that are not part of the internal scene graph, but are used
     * by our code to ensure that a runtime system functions. Will always return
     * a non-null list.
     *
     * @return All the currently registered behaviours;
     */
    public Behavior[] getSystemBehaviors() {

        // Dynamically create this each time as we only ever expect it to be
        // called once, by the VRMLLoader setup, and then thrown away.


        Behavior[] ret_val;

        if(isStatic) {
            ret_val = new Behavior[0];
        } else {
            ret_val = new Behavior[1];
            ret_val[0] = frameTicker;
        }

        return ret_val;
    }

    /**
     * Set the primary view to be used by this universe. This view is used to
     * ask for information like framerate and behaviours for navigation. This
     * will also automatically add the view to the common internal structures
     * so there is no need to also call <code>addView()</code>. Call assumes
     * that this is a non-static scene graph.
     *
     * @param view The new view to use as the primary view
     */
    public void setPrimaryView(View view) {
        if(view == null) {
            if(commonView != null) {
                commonView.attachViewPlatform(null);
                viewList.remove(commonView);
                commonView = null;
            }

            return;
        }

        // Update the view if we have something active
        if(commonView != view)
            view.attachViewPlatform(globalEffects.getViewPlatform());

        commonView = view;

        if(!viewList.contains(view))
            viewList.add(view);

        setupView();
    }

    /**
     * Add a view to this universe. If the view already exists, it will
     * not be added a second time. Null references will be ignored.
     *
     * @param view The new view to add
     */
    public void addView(View view) {
        if((view == null) || viewList.contains(view))
            return;

        viewList.add(view);

        // Update the view if we have something active
        if(currentScene != null) {
            view.setPhysicalBody(avatarBody);
            view.setPhysicalEnvironment(avatarEnvironment);
            view.setFrontClipPolicy(View.VIRTUAL_EYE);
            view.setBackClipPolicy(View.VIRTUAL_EYE);

            view.attachViewPlatform(globalEffects.getViewPlatform());
        }
    }

    /**
     * Remove a browser view from viewing this universe. If the reference is
     * null or is not registered with this universe, the request is ignored.
     *
     * @param view The view to be removed
     */
    public void removeView(View view) {
        if((view == null) || !viewList.contains(view))
            return;

        viewList.remove(view);
    }

    /**
     * Set the navigation mode selected from the user interface.
     *
     * @param mode The new mode
     */
     public void setNavigationMode(String mode) {
        userInput.setNavigationMode(mode);
     }

    /**
     * Move the user's location to see the entire world.  Change the users
     * orientation to look at the center of the world.
     *
     * @param animated Should the transistion be animated.  Defaults to FALSE.
     */
    public void fitToWorld(boolean animated) {
        System.out.println("Fit to world not implemented");
    }

    /**
     * Sync UI updates with the Application thread.  This method alls the core
     * to push work off to the app thread.
     */
    public void syncUIUpdates() {
        // ignored
    }

    /**
     * Setup the view platform needed for this world. In the end, this will
     * intialise all of the physical body settings for using VRML in a room
     * or CAVE type environment.
     */
    private void setupView() {

        avatarBody = new PhysicalBody();
        avatarEnvironment = new PhysicalEnvironment();

        if(commonView != null) {
            commonView.setPhysicalBody(avatarBody);
            commonView.setPhysicalEnvironment(avatarEnvironment);

            // Everything is spec'd as physical distances in VRML, so translate that
            // here - particularly useful in a CAVE environment.
            commonView.setFrontClipPolicy(View.VIRTUAL_EYE);
            commonView.setBackClipPolicy(View.VIRTUAL_EYE);
        }
    }

    /**
     * Convenience method used to clear the current world. We have to remove
     * all of the list of viewpoints, remove the default viewpoint so we can
     * reuse it and then clear the stack information.
     */
    private void clearCurrentWorld() {

        // Stop the clock as the first thing done!
        frameTicker.setEnable(false);

        BranchGroup root_node;
        J3DVRMLNode vrml_root;

        currentSpace = null;

        // Remove the branchgroup from the locale to stop it being rendered.
        vrml_root = (J3DVRMLNode)currentScene.getRootNode();
        root_node = (BranchGroup)vrml_root.getSceneGraphObject();
        root_node.detach();

        eventModel.clear();

        // Now go through and kill all the inlines. If we don't and someone
        // decides to inline the same .wrl file that has been cached, we can
        // end up with J3D MultipleParentExceptions
        clearInlines(currentScene);

        userInput.setPickableScene(null);
        userInput.setViewInfo(null, null, null);
    }

    /**
     * Convenience method to do the transition between the current viewpoint
     * and the newly given one. Will jump or smooth transition depending on
     * the new viewpoint requirements.
     *
     * @param vp The new viewpoint to move to
     */
    private void changeViewpoints(J3DViewpointNodeType vp) {

        if(vp.getJump() || inSetup) {
            currentViewpoint = vp;

            // Always reset the viewpoint to the default position as we might
            // be re-binding the same viewpoint, which is supposed to place it
            // back in it's original spot.
            TransformGroup old_tg = currentViewpoint.getPlatformGroup();
            Transform3D tx = currentViewpoint.getViewTransform();
            old_tg.setTransform(tx);

            // A straight jump just puts the camera at the new place
            if(commonView != null) {
                // TODO: Need support for OrthoViewpoint
                commonView.setFieldOfView(currentViewpoint.getFieldOfView()[0]);
                SceneGraphPath path = currentViewpoint.getSceneGraphPath();

                if(checkForLinks(path))
                    userInput.setViewInfo(commonView, old_tg, path);
                else
                    userInput.setViewInfo(commonView, old_tg, null);
            }
        } else {
            // We have to do a smooth transition between the points
            TransformGroup old_tg = currentViewpoint.getPlatformGroup();
            TransformGroup new_tg = vp.getPlatformGroup();

            Transform3D old_tx = new Transform3D();
            Transform3D dest_tx = new Transform3D();
            Transform3D final_tx = vp.getViewTransform();

            SceneGraphPath old_path = currentViewpoint.getSceneGraphPath();
            SceneGraphPath new_path = vp.getSceneGraphPath();

            boolean old_shared = checkForLinks(old_path);
            boolean new_shared = checkForLinks(new_path);

            if(old_shared)
                old_tg.getLocalToVworld(old_path, old_tx);
            else
                old_tg.getLocalToVworld(old_tx);

            if(new_shared)
                new_tg.getLocalToVworld(new_path, dest_tx);
            else
                new_tg.getLocalToVworld(dest_tx);


            // To work out the transition, the transform group of the dest
            // viewpoint must be set to exactly the virtual world position of
            // the old viewpoint and then allowed to transition across to the
            // final destination. However, we must allow for the differences
            // in their parent transforms first and make sure we have a proper
            // scale value.

            Vector3d old_trans = new Vector3d();
            Vector3d dest_trans = new Vector3d();
            Vector3d new_trans = new Vector3d();
            Quat4d old_orient = new Quat4d();
            Quat4d dest_orient = new Quat4d();
            Quat4d new_orient = new Quat4d();

            dest_tx.get(dest_orient, dest_trans);
            old_tx.get(old_orient, old_trans);

            new_trans.sub(dest_trans, old_trans);
            new_orient.sub(dest_orient, old_orient);

            Transform3D new_tx = new Transform3D();
            new_tx.set(new_orient, new_trans, 1);
            new_tx.set(new_trans);

            new_tg.setTransform(new_tx);

            if(commonView != null)
                commonView.setFieldOfView(vp.getFieldOfView()[0]);

            currentViewpoint = vp;

            if(commonView != null) {
                vpTransition.transitionTo(commonView,
                                          new_tg,
                                          final_tx,
                                          VP_TRANSITION_TIME);

                userInput.setViewInfo(commonView, new_tg, new_path);

                if(new_shared)
                    userInput.setViewInfo(commonView, new_tg, new_path);
                else
                    userInput.setViewInfo(commonView, new_tg, null);
            }
        }

        globalEffects.useHeadlight(currentNavInfo.getHeadlight());
    }

    /**
     * Act of binding the new NavigationInfo for this scene. Take the current
     * view information and set it up according to the requirements.
     *
     * @param ni The new navigation info to use
     */
    private void changeNavInfo(VRMLNavigationInfoNodeType ni) {

        currentNavInfo = ni;

        // We set the back clip distance based on the current vis limit. This
        // probably isn't 100% correct as j3d doesn't work on real world
        // coordinates but more relative coordinates based on distances between
        // the screen plate and virtual world.
        // For a first pass, this should do.
        float vis_limit = currentNavInfo.getVisibilityLimit();

        if(vis_limit <= 0)
            vis_limit = INFINITE_VIS_LIMIT;

        if(commonView != null) {
            commonView.setBackClipDistance(vis_limit);

            // Set the front clip distance. VRML recommends half of collision
            // radius to be used. ie index[0]
            float[] avatar_size = currentNavInfo.getAvatarSize();
            float near = avatar_size[0] / 2;

            if (near < 0.001f)
                near = 0.001f;

            commonView.setFrontClipDistance(near);
        }

        globalEffects.useHeadlight(currentNavInfo.getHeadlight());
        userInput.setNavigationInfo(currentNavInfo);
    }

    /**
     * Walk the scene and remove all the inlines currently in use. Note that
     * this assumes all the inlines are held within the VRMLScene instance.
     * The method is recursive, walking down the tree looking to unbuild any
     * child nodes.
     */
    private void clearInlines(BasicScene scene) {

        // First, start with all the normal inlines:
        ArrayList node_list =
            scene.getByPrimaryType(TypeConstants.InlineNodeType);
        int size = node_list.size();
        VRMLScene sc;

        for(int i = 0; i < size; i++) {
            // First detach this inline from the parent scene graph
            J3DVRMLNode node = (J3DVRMLNode)node_list.get(i);
            SceneGraphObject obj = node.getSceneGraphObject();

            if(obj instanceof BranchGroup) {
                ((BranchGroup)obj).detach();
            } else {
                System.out.println("Inline root node is not a BG! " +
                                   obj.getClass());
            }

            // Next clear the root node of the scene because that is what
            // we really play with when adding them again next time. If it
            // doesn't contain a scene (hasn't loaded yet) then no need to
            // recurse further.
            sc = (VRMLScene)((VRMLInlineNodeType)node).getContainedScene();

            if(sc == null)
                continue;

            J3DVRMLNode root_node = (J3DVRMLNode)sc.getRootNode();
            BranchGroup bg = (BranchGroup)root_node.getSceneGraphObject();
            bg.detach();

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

    /**
     * Convenience method to check whether the given scene graph path contains
     * any link nodes.
     *
     * @param path The path to check
     * @return true if there are link nodes found
     */
    private boolean checkForLinks(SceneGraphPath path) {
        boolean ret_val = false;

        if (path == null)
           return false;

        int size = path.nodeCount();

        for(int i = 0; i < size && !ret_val; i++) {
            if(path.getNode(i) instanceof Link)
                ret_val = true;
        }

        return ret_val;
    }
}
