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

package org.web3d.vrml.renderer.mobile.browser;

// External imports
import java.util.*;

//import javax.vecmath.Quat4d;
//import javax.vecmath.Vector3d;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.NodeObserver;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.common.input.NavigationStateListener;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.nodes.MobileViewpointNodeType;

import org.web3d.vrml.renderer.mobile.sg.Group;
import org.web3d.vrml.renderer.mobile.sg.Node;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;
import org.web3d.vrml.renderer.mobile.sg.Viewpoint;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.core.loading.ContentLoadManager;
import org.xj3d.core.loading.ScriptLoader;

/**
 * Representation of a Java 3D universe object suitable for use in single
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
 * @version $Revision: 1.20 $
 */
public class MobileUniverse
    implements BrowserCore, BindableNodeListener, EventModelInitListener {

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

    /** The View that we use for everything */
//    private View commonView;

    /** The current viewpoint node we are playing with */
    private MobileViewpointNodeType currentViewpoint;

    /** The current navigation info we are running with */
    private VRMLNavigationInfoNodeType currentNavInfo;

    /** Default navigationInfo that exists in every scene */
    private VRMLNavigationInfoNodeType defaultNavInfo;

    /** The top level node the universe is using */
    private Group worldGroup;

    /**
     * The branchgroup holding the common items for all scenes. In here you
     * will find the default viewpoint and the VRML clock used for timing
     * purposes. Once set, these never change.
     */
    private Group commonSceneGraph;

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

    /** Frame manager for the update cycles */
    private PerFrameManager frameManager;

    /** The scene manager used for this node */
    private SGManager sceneManager;

    /** Event model evaluator to use */
    private EventModelEvaluator eventModel;

    /** The user input manager for this and all classes */
//    private UserInputBehavior userInput;

    /** handler of the smooth viewpoint transitions */
//    private ViewpointTransition vpTransition;

    /** The error errorReporter instance */
    private ErrorReporter errorReporter;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** The listeners for core content */
    private ArrayList coreListeners;

    /** The common view platform */
//    private ViewGroup viewGroup;

    /**
     * Construct a default, empty universe that contains no scenegraph.
     *
     * @param rm The route manager to use for this universe
     * @param lm The load manager to use for this universe
     * @param sl The script loader to use for scripts
     * @param sgm The manager of the scene graph
     */
    public MobileUniverse(EventModelEvaluator eme, SGManager sgm) {
        eventModel = eme;

        sceneManager = sgm;
        viewpointStack = new BindableNodeManager();
        navInfoStack = new BindableNodeManager();

        defMap = Collections.EMPTY_MAP;
        coreListeners = new ArrayList();
        viewList = new ArrayList();
//        vpTransition = new ViewpointTransition();
//        userInput = new UserInputBehavior();

//        viewGroup = new ViewGroup(false);

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        frameManager = new PerFrameManager(sceneManager, eventModel);
        frameManager.setEnable(false);

        VRMLNodeFactory fac =
            DefaultNodeFactory.createFactory(
                DefaultNodeFactory.MOBILE_RENDERER
            );

        try {
            fac.setSpecVersion(3, 1);
            fac.setProfile("Interactive");
            fac.addComponent("EnvironmentalEffects", 2);
        } catch(UnsupportedProfileException upe) {
            // ignore
        }

        VRMLViewpointNodeType def_vp =
            (VRMLViewpointNodeType)fac.createVRMLNode("Viewpoint", false);

        def_vp.setDescription("Default viewpoint");
        def_vp.setupFinished();

        VRMLNavigationInfoNodeType def_ni =
            (VRMLNavigationInfoNodeType)fac.createVRMLNode("NavigationInfo", false);
        def_ni.setupFinished();

        VRMLBackgroundNodeType def_bg =
            (VRMLBackgroundNodeType)fac.createVRMLNode("Background", false);
        def_bg.setupFinished();

        VRMLFogNodeType def_fog =
            (VRMLFogNodeType)fac.createVRMLNode("Fog", false);
        def_fog.setFogType(VRMLFogNodeType.FOG_TYPE_DISABLE);
        def_fog.setupFinished();

        eventModel.setDefaultBindables(def_vp, def_ni, def_bg, def_fog);

        viewpointStack =
            eventModel.getBindableManager(TypeConstants.ViewpointNodeType);

        navInfoStack =
            eventModel.getBindableManager(TypeConstants.NavigationInfoNodeType);

        backgroundStack =
            eventModel.getBindableManager(TypeConstants.BackgroundNodeType);

        fogStack = eventModel.getBindableManager(TypeConstants.FogNodeType);

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
        fireInitEvent();

        inSetup = false;
    }

    /**
     * Notification that its safe to clear the world.  The underlying
     * rendering layer should now be cleared and loaded with the
     * world.
     */
    public void changeWorld() {
        if(currentScene != null) {
            fireShutdownEvent();
            clearCurrentWorld();
        }

        currentScene = nextScene;

        defMap = currentScene.getDEFNodes();
        MobileVRMLNode vrml_root = (MobileVRMLNode)currentScene.getRootNode();
        Group root_node = (Group)vrml_root.getSceneGraphObject();

        currentSpace = (VRMLExecutionSpace)vrml_root;

//        userInput.setPickableScene(root_node);

        worldGroup.addChild(root_node);

        sceneManager.setScene(worldGroup);
        sceneManager.setPerFrameManager(frameManager);
/*      TODO: Likely need to fix this
        if (currentViewpoint == defaultViewpoint)
            worldGroup.addChild((Node)currentViewpoint.getSceneGraphObject());
*/
        sceneManager.setActiveViewpoint(currentViewpoint.getView());

        // Right, reset the time and let's go. Send the init event just before
        // the clock starts, as per EAI spec.
        VRMLClock clk = eventModel.getVRMLClock();
        clk.resetTimeZero();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLBindableNodeListener interface.
    //----------------------------------------------------------

    /**
     * Notification that a binding stack has requested that this node be now
     * bound as the active node.
     *
     * @param src The source node that is to be bound
     * @param yes true if the node is becoming active
     */
    public void newNodeBound(VRMLBindableNodeType src) {
        int type = src.getPrimaryType();

        if(src instanceof MobileViewpointNodeType) {
            // Manage the transition between the viewpoints
            changeViewpoints((MobileViewpointNodeType)src);
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
/*
        if (!(node instanceof VRMLViewpointNodeType))
            return;

        int size = viewpointStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointAdded((VRMLViewpointNodeType)node, isDefault);
            } catch(Exception e) {
                System.out.println("Error sending viewpoint list changed " + e);
                e.printStackTrace();
            }
        }
*/
    }

    /**
     * Notification that a bindable has been removed.
     *
     * @param node The node
     */
    public void bindableRemoved(VRMLBindableNodeType node) {
/*
        if (!(node instanceof VRMLViewpointNodeType))
            return;

        int size = viewpointStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointRemoved((VRMLViewpointNodeType)node);
            } catch(Exception e) {
                System.out.println("Error sending viewpoint list changed " + e);
                e.printStackTrace();
            }
        }
*/
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
        return MOBILE_RENDERER;
    }

    /**
     * Get the ID string for this renderer.
     *
     * @return The String token for this renderer.
     */
    public String getIDString() {
        return MOBILE_ID;
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
     * @param long The time it took to render the last frame in milliseconds.
     */
    public void setLastRenderTime(long lastTime) {
//        lastRenderTime = lastTime;
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
        inSetup = true;
        nextScene = scene;
        eventModel.setScene(scene, viewpoint);

        frameManager.setEnable(true);
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
     * Set the current link selection listener to receive the load requests. A
     * null value will remove the listener.
     *
     * @param l The listener to use
     */
    public void setLinkSelectionListener(LinkSelectionListener l) {
        // userInput.setLinkSelectionListener(l);
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
     * Set the listener for navigation state change notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setNavigationStateListener(NavigationStateListener l) {
        // userInput.setNavigationStateListener(l);
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
    public VRMLViewpointNodeType getViewpoint() {
        return currentViewpoint;
    }

    /**
     * Convenience method used to clear the current world. We have to remove
     * all of the list of viewpoints, remove the default viewpoint so we can
     * reuse it and then clear the stack information.
     */
    private void clearCurrentWorld() {

        // Stop the clock as the first thing done!
  //      vrmlClock.setEnable(false);

        Group root_node;
        MobileVRMLNode vrml_root;

        currentSpace = null;

        // Remove the branchgroup from the locale to stop it being rendered.
        vrml_root = (MobileVRMLNode)currentScene.getRootNode();
        root_node = (Group)vrml_root.getSceneGraphObject();

        eventModel.clear();

        // Now go through and kill all the inlines.
        clearInlines(currentScene);

//        userInput.setPickableScene(null);
//        userInput.setViewInfo(null, null);
    }

    /**
     * Convenience method to do the transition between the current viewpoint
     * and the newly given one. Will jump or smooth transition depending on
     * the new viewpoint requirements.
     *
     * @param vp The new viewpoint to move to
     */
    private void changeViewpoints(MobileViewpointNodeType vp) {

/*
        if(vp.getJump() || inSetup) {
            currentViewpoint = vp;


            // Always reset the viewpoint to the default position as we might
            // be re-binding the same viewpoint, which is supposed to place it
            // back in it's original spot.
            TransformGroup old_tg = currentViewpoint.getPlatformGroup();
            Transform3D tx = currentViewpoint.getViewTransform();
            old_tg.setTransform(tx);

            // A straight jump just puts the camera at the new place
            commonView.setFieldOfView(currentViewpoint.getFieldOfView());

            currentViewpoint.setViewGroup(viewGroup);
            userInput.setViewInfo(commonView, old_tg);
        } else {
            // We have to do a smooth transition between the points
            TransformGroup old_tg = currentViewpoint.getPlatformGroup();
            TransformGroup new_tg = vp.getPlatformGroup();

            Transform3D old_tx = new Transform3D();
            Transform3D dest_tx = new Transform3D();
            Transform3D final_tx = vp.getViewTransform();

            old_tg.getLocalToVworld(old_tx);
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

            commonView.setFieldOfView(vp.getFieldOfView());
            vp.setViewGroup(viewGroup);

            vpTransition.transitionTo(commonView,
                                      new_tg,
                                      final_tx,
                                      VP_TRANSITION_TIME);
            currentViewpoint = vp;

            userInput.setViewInfo(commonView, new_tg);
        }

        viewGroup.useHeadlight(currentNavInfo.getHeadlight());
*/
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

//        commonView.setBackClipDistance(vis_limit);

        // Set the front clip distance. VRML recommends half of collision
        // radius to be used. ie index[0]
        float[] avatar_size = currentNavInfo.getAvatarSize();

//        commonView.setFrontClipDistance(avatar_size[0] / 2);

//        viewGroup.useHeadlight(currentNavInfo.getHeadlight());
//        userInput.setNavigationInfo(currentNavInfo);
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
            MobileVRMLNode node = (MobileVRMLNode)node_list.get(i);
            SceneGraphObject obj = node.getSceneGraphObject();

            // Next clear the root node of the scene because that is what
            // we really play with when adding them again next time. If it
            // doesn't contain a scene (hasn't loaded yet) then no need to
            // recurse further.
            sc = (VRMLScene)((VRMLInlineNodeType)node).getContainedScene();

            if(sc == null)
                continue;

            MobileVRMLNode root_node = (MobileVRMLNode)sc.getRootNode();
            Group bg = (Group)root_node.getSceneGraphObject();
//            bg.detach();

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
}
