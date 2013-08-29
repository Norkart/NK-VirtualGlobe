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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedAction;

// Have to name these individually because the Locale class clashes
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import javax.vecmath.Point2d;

import org.j3d.renderer.java3d.navigation.ViewpointTransition;
import org.j3d.renderer.java3d.overlay.OverlayManager;
import org.j3d.renderer.java3d.overlay.UpdateControlBehavior;
import org.j3d.terrain.TerrainData;

// Local imports
import org.web3d.browser.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.input.*;
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.util.ObjectArray;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.common.browser.OverlayWrapper;
import org.web3d.vrml.renderer.j3d.input.J3DTerrainManager;

import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.xj3d.core.eventmodel.EventModelInitListener;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.LayerRenderingManager;

import org.xj3d.core.loading.ContentLoadManager;
import org.xj3d.core.loading.ScriptLoader;

import com.sun.j3d.audioengines.javasound.JavaSoundMixer;

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
 * @version $Revision: 1.8 $
 */
public class J3DBrowserCore extends VirtualUniverse
    implements BrowserCore,
               BindableNodeListener,
               EventModelInitListener,
               J3DParentPathRequestHandler {

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

    /** The locale the universe is operating in */
    private Locale locale;

    /** The orderedGroup to order overlays, background and content */
    private OrderedGroup ogroup;

    /** The BranchGroup needed to contain the ogroup to add to a locale */
    private BranchGroup bogroup;

    /**
     * The branchgroup holding the common items for all scenes. In here you
     * will find the default viewpoint and the VRML clock used for timing
     * purposes. Once set, these never change.
     */
    private BranchGroup commonSceneGraph;

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

    /** Node factory used at the high level */
    private VRMLNodeFactory nodeFactory;

    /** The common view platform */
    private GlobalEffectsGroup globalEffects;

    /** The renderer effects group */
    private RendererEffectsGroup rendererEffects;

    /** Event model evaluator to use */
    private EventModelEvaluator eventModel;

    /** Ticker used to drive the event model each frame */
    private PerFrameBehavior frameTicker;

    /** The overlay manager for all the overlays */
    private OverlayManager overlayManager;

    /** Wrapper for handling overlay setups */
    private OverlayWrapper overlayWrapper;

    /** Does this scene have overlays */
    private boolean hasOverlays;

    /** The field of view.  A value of zero says to use the viewpoints setting */
    private float fieldOfView;

    /** Temporary array for passing back the scene root information */
    private ObjectArray j3dScenePath;

    /** The terrain data source for geoVRML */
    private TerrainData terrain_data;

    /** The last render timing */
    private long lastRenderTime;

    /** The list of ViewpointStatus listeners */
    private ArrayList viewpointStatusListeners;

    /**
     * Construct a default, empty universe that contains no scenegraph. If the
     * evaluator is null, then it assumes a static model with no event cascade
     * processing to be performed, nor any user input handling. It will render
     * an entirely static scene based on time zero loading.
     *
     * @param eme The evaluator for the event model
     * @param clockEvents Should this universe clock the events or
     *      is it external controlled.
     * @throws IllegalArgumentException The evaluator does not contain
     *    appropriate managers for Java3D handler
     */
    public J3DBrowserCore(EventModelEvaluator eme,
                          boolean clockEvents,
                          OverlayHandler oh) {

        if(eme != null) {
            eventModel = eme;
            eventModel.setInitListener(this);
        }

        defMap = Collections.EMPTY_MAP;
        coreListeners = new ArrayList();
        viewList = new ArrayList();
        j3dScenePath = new ObjectArray();
        vpTransition = new ViewpointTransition();
        viewpointStatusListeners = new ArrayList(1);

        globalEffects = new GlobalEffectsGroup(false);
        rendererEffects = new RendererEffectsGroup(false);

        locale = new Locale(this);
        bogroup = new BranchGroup();
        bogroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        bogroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        ogroup = new OrderedGroup();
        ogroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        ogroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        ogroup.setCapability(OrderedGroup.ALLOW_CHILD_INDEX_ORDER_WRITE);

        bogroup.addChild(ogroup);
        locale.addBranchGraph(bogroup);

        commonSceneGraph = new BranchGroup();
        commonSceneGraph.setCapability(Group.ALLOW_CHILDREN_WRITE);
        commonSceneGraph.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        commonSceneGraph.addChild(globalEffects);
        commonSceneGraph.addChild(rendererEffects);

        if(clockEvents) {
            frameTicker = new PerFrameBehavior(eme, this);
            commonSceneGraph.addChild(frameTicker);
        }

        fieldOfView = 0f;
        hasOverlays = false;

        if(eventModel != null) {
            SensorManager s_mgr = eventModel.getSensorManager();
            if(!(s_mgr instanceof J3DSensorManager))
                throw new IllegalArgumentException("Not a Java3D sensor manager");

            sensorManager = (J3DSensorManager)s_mgr;
            sensorManager.setGlobalEffectsHandler(globalEffects);
            sensorManager.setRendererEffectsHandler(rendererEffects);
            userInput = (J3DUserInputHandler)sensorManager.getUserInputHandler();

            VRMLNodeFactory fac =
                DefaultNodeFactory.createFactory(
                    DefaultNodeFactory.JAVA3D_RENDERER);

            nodeFactory = fac;

            try {
                fac.setSpecVersion(3, 1);
                fac.setProfile("Interactive");
                fac.addComponent("EnvironmentalEffects", 2);
            } catch(UnsupportedProfileException upe) {
                // ignore
            }

            J3DViewpointNodeType def_vp =
                (J3DViewpointNodeType)fac.createVRMLNode("Viewpoint",
                                                         false);

            def_vp.setDescription("Default viewpoint");
            def_vp.setupFinished();

            VRMLNavigationInfoNodeType def_ni =
                (VRMLNavigationInfoNodeType)fac.createVRMLNode("NavigationInfo",
                                                               false);
            def_ni.setupFinished();

            J3DBackgroundNodeType def_bg =
                (J3DBackgroundNodeType)fac.createVRMLNode("Background",
                                                          false);
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

            backgroundStack =
                eventModel.getBindableManager(TypeConstants.BackgroundNodeType);

            fogStack = eventModel.getBindableManager(TypeConstants.FogNodeType);

            // Register the listeners for the viewpoint & background
            if(def_vp instanceof J3DPathAwareNodeType) {
                J3DPathAwareNodeType node = (J3DPathAwareNodeType)def_vp;
                node.addParentPathListener(this);
            }

            if(def_bg instanceof J3DPathAwareNodeType) {
                J3DPathAwareNodeType node = (J3DPathAwareNodeType)def_bg;
                node.addParentPathListener(this);
            }

            if(def_fog instanceof J3DPathAwareNodeType) {
                J3DPathAwareNodeType node = (J3DPathAwareNodeType)def_fog;
                node.addParentPathListener(this);
            }

            Node vp = (Node)def_vp.getSceneGraphObject();
            Node back = (Node)def_bg.getSceneGraphObject();
            Node fog = (Node)def_fog.getSceneGraphObject();

            // Has to be added to allow VWorld calcs
            commonSceneGraph.addChild(back);
            commonSceneGraph.addChild(vp);
            commonSceneGraph.addChild(fog);
        }

        if(oh != null) {
            UpdateControlBehavior updater = new UpdateControlBehavior();
            updater.setSchedulingBounds(new BoundingSphere(new Point3d(), 1e32));

            Canvas3D canvas = oh.getPrimaryCanvas();

            overlayManager = new OverlayManager(canvas, updater);
            J3DTerrainManager tm = new J3DTerrainManager(canvas);
            rendererEffects.setTerrainManager(tm);

            commonSceneGraph.addChild(updater);

            // Note that overlays are required to be added to the view
            // platform's transform, not the world one.
            globalEffects.addViewDependentChild(overlayManager);

            overlayWrapper = new OverlayWrapper(canvas);
            ogroup.addChild(commonSceneGraph);
        } else {
            // To keep transparemcy sorting working add non-overlay worlds to the branchgroup
            bogroup.addChild(commonSceneGraph);
        }

        viewpointStack.setNodeChangeListener(this);
        navInfoStack.setNodeChangeListener(this);

        // No node change listeners for Fog or Viewpoints

        inSetup = false;
    }

    //----------------------------------------------------------
    // Methods from the J3DParentPathRequestHandler interface.
    //----------------------------------------------------------

    /**
     * Check to see if the parent path to the root of the scene graph has
     * changed in structure and the scene graph path needs to be regenerated.
     * This is a query only and if this level has not changed then the parent
     * level above should be automatically requested until the root of the
     * live scene graph is reached.
     *
     * @return true if this or a parent of this path has changed
     */
    public boolean hasParentPathChanged() {
        return false;
    }

    /**
     * Fetch the scene graph path from the root of the world to this node.
     * If this node's SceneGraphObject is represented by a SharedGroup, then
     * the last item in the given path will be the Link node that is attached
     * to this object. If this node cannot find the root of the scene graph
     * or the child is not a registered child of this node, return null.
     * <p>
     * The path array will have the first element as the root locale, and the
     * the children will be all the link nodes, in order from the root to this
     * level.
     *
     * @param requestingChild A reference to the child that's making the request
     * @return The list of locales and nodes in the path down to this node or null
     */
    public ObjectArray getParentPath(J3DVRMLNode requestingChild) {
        // This is fine so long as the requests are coming through
        // single threaded
        j3dScenePath.clear();
        j3dScenePath.add(locale);

        return j3dScenePath;
    }

    //----------------------------------------------------------
    // Methods defined by EventModelInitListener interface.
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
        if (currentScene != null) {
            fireShutdownEvent();
            clearCurrentWorld();
        }

        currentScene = nextScene;
        hasOverlays = false;

        if (currentScene == null)
            return;

        defMap = currentScene.getDEFNodes();

        J3DVRMLNode vrml_root = (J3DVRMLNode)currentScene.getRootNode();
        BranchGroup root_node = (BranchGroup)vrml_root.getSceneGraphObject();

        currentSpace = (VRMLExecutionSpace)vrml_root;

        if(vrml_root instanceof J3DPathAwareNodeType)
            ((J3DPathAwareNodeType)vrml_root).addParentPathListener(this);

        userInput.setPickableScene(root_node);
        sensorManager.setWorldRoot(root_node);

        if(overlayManager != null) {
            ArrayList list = currentScene.getByPrimaryType(TypeConstants.OverlayNodeType);
            int size = list.size();

            for(int i = 0; i < size; i++) {
                J3DOverlayStructureNodeType st =
                    (J3DOverlayStructureNodeType)list.get(i);

                st.setOverlayManager(overlayManager);
            }

            overlayWrapper.setSurfaces(list);

            if (size > 0) {
                hasOverlays = true;
                ogroup.addChild(root_node);
                ogroup.setChildIndexOrder(new int[]{1,0});
            } else {
                bogroup.addChild(root_node);
            }
        } else {
            bogroup.addChild(root_node);
        }

        setupView();

        // Right, reset the time and let's go. Send the init event just before
        // the clock starts, as per EAI spec.
        VRMLClock clk = eventModel.getVRMLClock();
        clk.resetTimeZero();

        if(overlayManager != null)
            overlayManager.viewChanged();

        if(frameTicker != null)
            frameTicker.setEnable(true);
    }

    //----------------------------------------------------------
    // Methods defined by BindableNodeListener
    //----------------------------------------------------------

    /**
     * Notification that a binding stack has requested that this node be now
     * bound as the active node.
     *
     * @param src The source node that is to be bound
     */
    public void newNodeBound(VRMLBindableNodeType src) {
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
    }

    /**
     * Notification that a bindable has been removed.
     *
     * @param node The node
     */
    public void bindableRemoved(VRMLBindableNodeType node) {
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
        inSetup = true;
        nextScene = scene;

        if(eventModel != null)
            eventModel.setScene(scene, viewpoint);

        if(frameTicker != null)
            frameTicker.setEnable(true);
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
     * Get the node factory instance used at the top-level.
     *
     * @return The current instance in use
     */
    public VRMLNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    /**
     * Specify a field of view.  This will override the values specified
     * in the viewpoint field.  A value of zero will reset the system to
     * use the viewpoints setting.
     *
     * @param fov The field of view in radians
     */
     public void setFieldOfView(float fov) {
        fieldOfView = fov;
     }

    /**
     * Add an arbitrary piece of scene graph to the world. This is a
     * convenience method to allow any end user to customise the 3D
     * graphics rendering portion with their own information. This
     * will be added as a separate group to the root locale of the
     * universe. It may only be called before the world is made
     * live.
     *
     * @param bg Extra piece of scene graph to be added
     */
    public void addSceneGraphExtras(BranchGroup bg) {
        commonSceneGraph.addChild(bg);
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
     * Get the currently selected viewpoint. If there is no scene set then this
     * returns null.
     *
     * @return The current active viewpoint
     */
    public J3DViewpointNodeType getViewpoint() {
        return currentViewpoint;
    }

    /**
     * Set the primary view to be used by this universe. This view is used to
     * ask for information like framerate and behaviours for navigation. This
     * will also automatically add the view to the common internal structures
     * so there is no need to also call <code>addView()</code>
     *
     * @param view The new view to use as the primary view
     */
    public void setPrimaryView(View view) {
        if(view == null)
            return;

        setupView();

        // Update the view if we have something active
        if(commonView != view) {
            view.setPhysicalBody(avatarBody);
            view.setPhysicalEnvironment(avatarEnvironment);

            // Everything is spec'd as physical distances in VRML, so translate that
            // here - particularly useful in a CAVE environment.
            view.setFrontClipPolicy(View.VIRTUAL_EYE);
            view.setBackClipPolicy(View.VIRTUAL_EYE);

            view.attachViewPlatform(globalEffects.getViewPlatform());
        }

        commonView = view;

        if(!viewList.contains(view))
            viewList.add(view);
    }

    /**
     * Set a custom physical body description
     *
     * @param body The new body instance to use
     */
    public void setPhysicalBody(PhysicalBody body) {
        if(body == null)
            avatarBody = new PhysicalBody();
        else
            avatarBody = body;

        int size = viewList.size();
        for(int i = 0; i < size; i++) {
            View v = (View)viewList.get(i);
            v.setPhysicalBody(avatarBody);
        }
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

        // Now do all the other views
        int size = viewList.size();
        View v;

        for(int i = 0; i < size; i++) {
            v = (View)viewList.get(i);

            v.setPhysicalBody(avatarBody);
            v.setPhysicalEnvironment(avatarEnvironment);

            // Seems starting up a Mixer is a privleged action
            try {
                AccessController.doPrivileged(
                    new PrivilegedExceptionAction () {
                        public Object run() {
                            boolean tryAgain=false;
                            // HeadspaceMixer is present in Java3D 1.3.1, but not present in
                            // 1.3.2 and later.  Try to load the HeadspaceMixer but then fall back to
                            // the more generic mixer if it doesn't work.
                            try {
                                ClassLoader cl = this.getClass().getClassLoader();
                                Class c = cl.loadClass("com.sun.j3d.audioengines.headspace.HeadspaceMixer");
                                Constructor f=c.getConstructor(new Class[]{PhysicalEnvironment.class});
                                AudioDevice3DL2 mixer=(AudioDevice3DL2) f.newInstance(new Object[]{avatarEnvironment});
                                mixer.initialize();
                            } catch (ClassNotFoundException cnfe) {
                                cnfe.printStackTrace();
                                tryAgain=true;
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                                tryAgain=true;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                tryAgain=true;
                            } catch (SecurityException e) {
                                e.printStackTrace();
                                tryAgain=true;
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                                tryAgain=true;
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                                tryAgain=true;
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                                tryAgain=true;
                            }
                            if (tryAgain) {
                                System.out.println("HeadspaceMixer not loading, defaulting to JavaSoundMixer");
                                JavaSoundMixer mixer = new JavaSoundMixer(avatarEnvironment);
                                mixer.initialize();
                            }
                            return null;
                        }
                    }
                );
            } catch (PrivilegedActionException pae) {
                System.out.println("Error starting Audio, can't get privilege");
            }

            // Everything is spec'd as physical distances in VRML, so translate that
            // here - particularly useful in a CAVE environment.
            v.setFrontClipPolicy(View.VIRTUAL_EYE);
            v.setBackClipPolicy(View.VIRTUAL_EYE);
        }
    }

    /**
     * Convenience method used to clear the current world. We have to remove
     * all of the list of viewpoints, remove the default viewpoint so we can
     * reuse it and then clear the stack information.
     */
    private void clearCurrentWorld() {
        // Stop the clock as the first thing done!
        if(frameTicker != null)
            frameTicker.setEnable(false);

        // Stop user input handling
        userInput.setPickableScene(null);

        BranchGroup root_node;
        J3DVRMLNode vrml_root;

        currentSpace = null;

        // Remove the branchgroup from the locale to stop it being rendered.
        vrml_root = (J3DVRMLNode)currentScene.getRootNode();
        root_node = (BranchGroup)vrml_root.getSceneGraphObject();
        try {
            root_node.detach();
        } catch(CapabilityNotSetException cnse) {
            System.out.println("Can't detach root");
            cnse.printStackTrace();
        }

        if(vrml_root instanceof J3DPathAwareNodeType)
            ((J3DPathAwareNodeType)vrml_root).removeParentPathListener(this);

        eventModel.clear();
        sensorManager.setWorldRoot(null);

        // Now go through and kill all the inlines. If we don't and someone
        // decides to inline the same .wrl file that has been cached, we can
        // end up with J3D MultipleParentExceptions
        clearInlines(currentScene);

        overlayManager.clearOverlays();
        userInput.setPickableScene(null);
        userInput.setViewInfo(null, null, null);

        if (hasOverlays)
            ogroup.removeChild(root_node);
        else
            bogroup.removeChild(root_node);
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
            if(fieldOfView == 0f) {
                // TODO: Need support for OrthoCamera
                commonView.setFieldOfView(currentViewpoint.getFieldOfView()[0]);
            } else {
                commonView.setFieldOfView(fieldOfView);
            }

            SceneGraphPath path = currentViewpoint.getSceneGraphPath();

            if(checkForLinks(path))
                userInput.setViewInfo(commonView, old_tg, path);
            else
                userInput.setViewInfo(commonView, old_tg, null);
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

            if (fieldOfView == 0f) {
                // TODO: Need support for OrthoCamera
                commonView.setFieldOfView(vp.getFieldOfView()[0]);
            } else
                commonView.setFieldOfView(fieldOfView);

            vpTransition.transitionTo(commonView,
                                      new_tg,
                                      final_tx,
                                      VP_TRANSITION_TIME);
            currentViewpoint = vp;

            if(new_shared)
                userInput.setViewInfo(commonView, new_tg, new_path);
            else
                userInput.setViewInfo(commonView, new_tg, null);
        }
        if (currentNavInfo != null)
            globalEffects.useHeadlight(currentNavInfo.getHeadlight());

        if(overlayManager != null)
            overlayManager.viewChanged();
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

        commonView.setBackClipDistance(vis_limit);

        // Set the front clip distance. VRML recommends half of collision
        // radius to be used. ie index[0]
        float[] avatar_size = currentNavInfo.getAvatarSize();
        float near = avatar_size[0] / 2;

        if (near < 0.001f)
            near = 0.001f;

        commonView.setFrontClipDistance(near);

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
