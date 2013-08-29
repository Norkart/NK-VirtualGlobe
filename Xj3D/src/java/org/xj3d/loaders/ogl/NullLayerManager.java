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

package org.xj3d.loaders.ogl;

// External imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.aviatrix3d.rendering.BoundingVolume;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.ViewpointStatusListener;
import org.web3d.util.ErrorReporter;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.ogl.input.DefaultLayerSensorManager;
import org.web3d.vrml.renderer.ogl.input.OGLUserInputHandler;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLTransformNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

import org.xj3d.core.eventmodel.BindableNodeListener;
import org.xj3d.core.eventmodel.BindableNodeManager;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.SensorManager;

import org.web3d.vrml.renderer.ogl.browser.GlobalEffectsGroup;
import org.web3d.vrml.renderer.ogl.browser.OGLRenderingEffects;
import org.web3d.vrml.renderer.ogl.browser.ScreenCaptureListener;

/**
 * Null implementation of a layer manager.
 * <p>
 *
 * The layer manager is responsible for keeping track of all the top level
 * renderable structure within a scene.
 * <p>
 *
 * This layer manager will assume that someone else will inject the
 * scenegraph into Aviatrix3D.  This will use an X3D event model with the
 * following restrictions:
 *

 * No user navigation will be supported.
 * Viewpoint binding will be ignored.
 * NavigationInfo binding will be ignored.
 * Fog binding will be ignored.
 * Background binding will be ignored
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
class NullLayerManager
    implements LayerManager,
               NodeUpdateListener,
               BindableNodeListener,
               LayerListener {

    /** Message when the node factory doesn't have any configurations set */
    private static final String NODE_PROFILE_ERR =
        "LayerManager is unable to initialise the default bindables " +
        "due to missing node factory configurations.";

    /**
     * Message for a viewpoint with a weird projection constant. Since this is
     * Hard-coded into the viewpoint implemenation node, we should never see
     * this error, unless there is a user-implemented type of viewpoint. In
     * which case, you are now reading the right error message and know that
     * you need to go fix up the appropriate switch statements down the bottom
     * of this class.
     */
    private static final String UNKNOWN_PROJ_TYPE_MSG =
        "A viewpoint has been provided with an unknown projection type. The " +
        "code can only deal with perspective and orthographic projections. " +
        "We'll default to perspective now.";

    /** Error message when the viewpointAdded() call generates an error */
    private static final String VP_ADD_ERR =
        "Error sending viewpoint addition notification.";

    /** Error message when the viewpointRemoved() call generates an error */
    private static final String VP_REMOVE_ERR =
        "Error sending viewpoint removed notification.";

    /** Error message when the viewpointBound() call generates an error */
    private static final String VP_BOUND_ERR =
        "Error sending viewpoint binding notification.";

    /** Error message when the viewpointLayerActive() call generates an error */
    private static final String VP_ACTIVATE_ERR =
        "Error sending viewpoint layer activation notification.";

    /** Error message when the viewpointLayerAdded() call generates an error */
    private static final String VP_LAYER_ADD_ERR =
        "Error sending viewpoint layer addtion notification.";

    /** Error message when the viewpointLayerRemoved() call generates an error */
    private static final String VP_LAYER_REMOVE_ERR =
        "Error sending viewpoint layer removal notification.";

    /** The time in milliseconds to move between two points */
    private static final int VP_TRANSITION_TIME = 2000;

    /** The name of the navigation component */
    private static final String NAV_COMPONENT = "Navigation";

    /** The name of the environmental effects component */
    private static final String ENV_COMPONENT = "EnvironmentalEffects";


    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The View that we use for everything */
    private ViewEnvironment viewEnvironment;

    /** The current viewpoint node we are playing with */
    private OGLViewpointNodeType currentViewpoint;

    /** The current navigation info we are running with */
    private VRMLNavigationInfoNodeType currentNavInfo;

    /** Default viewpoint that exists in every scene */
    private OGLViewpointNodeType defaultViewpoint;

    /** Default navigationInfo that exists in every scene */
    private VRMLNavigationInfoNodeType defaultNavInfo;

    /** Default background that exists in every scene */
    private VRMLBackgroundNodeType defaultBackground;

    /** Default fog that exists in every scene */
    private VRMLFogNodeType defaultFog;

    /** The node stack for viewpoints */
    private BindableNodeManager viewpointStack;

    /** The node stack for navigation information */
    private BindableNodeManager navInfoStack;

    /** The node stack for navigation information */
    private BindableNodeManager backgroundStack;

    /** The node stack for navigation information */
    private BindableNodeManager fogStack;

    /** Map of node primary type to the bindable manager for that type */
    private IntHashMap bindablesMap;

    /** Clock for setting bindTime information */
    private VRMLClock clock;

    /** The global effects for this layer */
    private GlobalEffectsGroup globalEffects;

    /** Manager of the global sensor nodes */
    private SensorManager sensorManager;

    /** The per-layer sensor manager */
    private DefaultLayerSensorManager layerSensorManager;

    /** Is this layer currently active for navigation purposes? */
    private boolean navigationEnabled;

    /** Temporary holding the projection type for the update callback */
    private int projectionType;

    /** Input processing for this layer */
    private OGLUserInputHandler userInput;

    /** Listeners for the viewpoint status updates from this layer */
    private ArrayList viewpointStatusListeners;

    /** Rendering effects if you want something other than polygons */
    private OGLRenderingEffects renderEffects;

    // Aviatrix3D variables

    /** The layer above the scene */
    private SimpleLayer implLayer;

    /** Render manager for handling the backgrounds and fogs */
    private SimpleScene implScene;

    /** The top level node the universe is using */
    private Group worldGroup;

    /** The Transform holding the main scene, child 0=vp, 1=loaded scene */
    private TransformGroup sgTransformGroup;

    /** A group to hold the last vp group for later update */
    private TransformGroup vpTransformGroup;

    /** The current world root */
    private Group worldRoot;

    /** The geometry from the layer or world root that is pending addition */
    private Group pendingWorldGeom;

    /** A matrix to hold the last vp matrix for later update */
    private Matrix4f vpMatrix;

    /** Matrix containing the world scale that is calculated for this layer */
    private Matrix4f worldScaleMatrix;

    /** A matrix used when calculating the scene graph path */
    private Matrix4f pathMatrix;

    /** The ID of this layer */
    private int layerId;

    /** The type of viewport that this layer has */
    private int viewportType;

    /** The viewport node from the layer. Null if the root layer */
    private VRMLViewportNodeType viewport;

    /** Temp var used to fetch the scene graph path */
    private ArrayList pathList;

    /** An array used to fetch the nodes from pathNodes */
    private Node[] pathNodes;

    /**
     * Create a new instance of the layer manager that looks after the
     * given effects group.
     *
     * @param globals The handler for fog/background et al for this layer
     */
    NullLayerManager() {

        layerId = -1;

        pathList = new ArrayList();
        pathNodes = new Node[20];  // some arbitrary value;

        viewportType = VIEWPORT_FULLWINDOW;
        projectionType = ViewEnvironment.PERSPECTIVE_PROJECTION;

        implScene = new SimpleScene();
        viewEnvironment = implScene.getViewEnvironment();

        SimpleViewport vp = new SimpleViewport();
        vp.setScene(implScene);

        implLayer = new SimpleLayer();
        implLayer.setViewport(vp);

        globalEffects = new GlobalEffectsGroup(implScene);
        renderEffects = new OGLRenderingEffects(implScene);

        implScene.setRenderEffectsProcessor(renderEffects);

        layerSensorManager = new DefaultLayerSensorManager();
        layerSensorManager.setViewEnvironment(viewEnvironment);
        layerSensorManager.setGlobalEffectsHandler(globalEffects);

        userInput = (OGLUserInputHandler)layerSensorManager.getUserInputHandler();

        Group common_sg = new Group();
        common_sg.addChild(globalEffects);

        worldGroup = new Group();

        // Group for holding the main scene. Prepopulate two empty children.
        // The first child belongs to all the default bindable nodes and is
        // filled in at initialisation of this class. The second index is set
        // to be the geometry of the loaded world.
        sgTransformGroup = new TransformGroup();
        sgTransformGroup.addChild(null);
        sgTransformGroup.addChild(null);

        worldGroup.addChild(common_sg);
        worldGroup.addChild(sgTransformGroup);

        implScene.setRenderedGeometry(worldGroup);
        globalEffects.initialize();

        bindablesMap = new IntHashMap();
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        worldScaleMatrix = new Matrix4f();
        worldScaleMatrix.setIdentity();

        pathMatrix = new Matrix4f();

        viewpointStatusListeners = new ArrayList();
    }

    //----------------------------------------------------------
    // Methods defined by LayerManager
    //----------------------------------------------------------

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

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        int[] keys = bindablesMap.keySet();

        for(int i = 0; i < keys.length; i++) {
            BindableNodeManager mgr =
                (BindableNodeManager)bindablesMap.get(keys[i]);

            mgr.setErrorReporter(errorReporter);
        }
    }

    /**
     * Complete the initialisation of the layer manager now. This should be
     * called after setting the clock and the current error reporter instance.
     *
     * @param sensors The sensor manager to start with from the global list
     */
    public void initialise(SensorManager smgr) {
        sensorManager = smgr;

        clock = sensorManager.getVRMLClock();

        BindableNodeManager vp_mgr = new BindableNodeManager();
        vp_mgr.setVRMLClock(clock);
        vp_mgr.setErrorReporter(errorReporter);
        vp_mgr.setNodeChangeListener(this);
        bindablesMap.put(TypeConstants.ViewpointNodeType, vp_mgr);
        viewpointStack = vp_mgr;

        BindableNodeManager ni_mgr = new BindableNodeManager();
        ni_mgr.setVRMLClock(clock);
        ni_mgr.setErrorReporter(errorReporter);
        bindablesMap.put(TypeConstants.NavigationInfoNodeType, ni_mgr);
        navInfoStack = ni_mgr;

        BindableNodeManager bg_mgr = new BindableNodeManager();
        bg_mgr.setVRMLClock(clock);
        bg_mgr.setErrorReporter(errorReporter);
        bindablesMap.put(TypeConstants.BackgroundNodeType, bg_mgr);
        backgroundStack = bg_mgr;

        BindableNodeManager fog_mgr = new BindableNodeManager();
        fog_mgr.setVRMLClock(clock);
        fog_mgr.setErrorReporter(errorReporter);
        bindablesMap.put(TypeConstants.FogNodeType, fog_mgr);
        fogStack = fog_mgr;

        layerSensorManager.setNavigationStacks(vp_mgr,
                                               ni_mgr,
                                               bg_mgr,
                                               fog_mgr);
        layerSensorManager.setVRMLClock(clock);

        sensorManager.addLayerSensorManager(layerSensorManager);

        VRMLNodeFactory fac =
            DefaultNodeFactory.createFactory(
                DefaultNodeFactory.OPENGL_RENDERER
            );

        fac.setSpecVersion(3, 2);

        defaultViewpoint =
            (OGLViewpointNodeType)fac.createVRMLNode(NAV_COMPONENT,
                                                     "Viewpoint",
                                                     false);

        defaultViewpoint.setDescription("Default viewpoint");
        defaultViewpoint.setupFinished();

        defaultNavInfo =
            (VRMLNavigationInfoNodeType)fac.createVRMLNode(NAV_COMPONENT,
                                                           "NavigationInfo",
                                                            false);
        defaultNavInfo.setupFinished();

        defaultBackground =
            (VRMLBackgroundNodeType)fac.createVRMLNode(ENV_COMPONENT,
                                                       "Background",
                                                       false);
        defaultBackground.setupFinished();

        defaultFog =
            (VRMLFogNodeType)fac.createVRMLNode(ENV_COMPONENT, "Fog", false);
        defaultFog.setFogType(VRMLFogNodeType.FOG_TYPE_DISABLE);
        defaultFog.setupFinished();

        // Fill in the geometry of the default viewpoints.
        Node vp_node = (Node)defaultViewpoint.getSceneGraphObject();
        Node bg_node = (Node)((OGLVRMLNode)defaultBackground).getSceneGraphObject();
        Node fog_node = (Node)((OGLVRMLNode)defaultFog).getSceneGraphObject();

        Group grp = new Group();
        grp.addChild(vp_node);
        grp.addChild(bg_node);
        grp.addChild(fog_node);

        sgTransformGroup.setChild(grp, 1);
    }

    /**
     * Set or reset the layer ID to the new ID value.
     *
     * @param id A non-negative ID for the layer
     */
    public void setLayerId(int id) {
        layerId = id;
        layerSensorManager.setLayerId(id);
        globalEffects.setLayerId(id);
    }

    /**
     * Set the specification version that should be handled by this manager.
     * This is needed so that the correct version of the default bindables are
     * instantiated before the rest of the world loads. For example, the default
     * nav type for VRML is different to X3D, so this makes sure all the right
     * spec stuff is catered for.
     *
     * @param major The spec major version number
     * @param minor The spec minor version number
     */
    public void setSpecVersion(int major, int minor) {
    }

    /**
     * Change the rendering style that the browser should currently be using
     * for all layers. Various options are available based on the constants
     * defined in this
     *
     * @param style One of the RENDER_* constants from LayerRenderingManager
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    public void setRenderingStyle(int style)
        throws IllegalArgumentException {

        renderEffects.setRenderingStyle(style);
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants from LayerRenderingManager
     */
    public int getRenderingStyle() {
        return renderEffects.getRenderingStyle();
    }

    /**
     * Capture the screen on the next render.
     *
     * @param listener Listener for capture results
     * @param width The screen width
     * @param height The screen height
     */
    public void captureScreenOnce(ScreenCaptureListener listener, int width, int height) {
        // ignore
    }

    /**
     * Capture the screen on each render.
     *
     * @param listener Listener for capture results
     * @param width The screen width
     * @param height The screen height
     */
    public void captureScreenStart(ScreenCaptureListener listener, int width, int height) {
        // ignore
    }

    /**
     * Stop cpaturing the screen on each render.
     */
    public void captureScreenEnd() {
        // ignore
    }

    /**
     * Perform the initial bind for a new scene. This is typically called some
     * time just after the clear() method with a new scene. This will
     * automatically reset the current navigation state for this layer to be
     * inactive, even if it was previously active.
     */
    public void initialBind() {

        // create default nodes to suit.
        double time = clock.getTime();

        // Put the defaults back into the stacks after it has been
        // cleared
        viewpointStack.addNode(defaultViewpoint, true);
        navInfoStack.addNode(defaultNavInfo, true);
        backgroundStack.addNode(defaultBackground, true);
        fogStack.addNode((VRMLBindableNodeType)defaultFog, true);

// LAYERS:
// This should also go looking for the default for this viewpoint based on
// the #ref part of a URL and see if it is part of this layer.
        VRMLViewpointNodeType vp =
            (VRMLViewpointNodeType)viewpointStack.getFirstNode();

        VRMLNavigationInfoNodeType nav =
            (VRMLNavigationInfoNodeType)navInfoStack.getFirstNode();

        VRMLBackgroundNodeType bg =
            (VRMLBackgroundNodeType)backgroundStack.getFirstNode();

        VRMLFogNodeType fog = (VRMLFogNodeType)fogStack.getFirstNode();

        vp.setBind(true, true, time);
        nav.setBind(true, true, time);
        bg.setBind(true, true, time);
        ((VRMLBindableNodeType)fog).setBind(true, true, time);

        currentNavInfo = nav;

        SceneGraphPath path = generatePath(vpTransformGroup);

        if(sgTransformGroup.isLive())
            sgTransformGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(sgTransformGroup);
    }

    /**
     * Get the bindable node manager for the given node type. If the node type
     * does not have a bindable manager for it, one will be created.
     *
     * @param type The type constant of the node type for the manager
     * @return The bindable manager for it
     * @see org.web3d.vrml.lang.TypeConstants
     */
    public BindableNodeManager getBindableManager(int type) {
        BindableNodeManager ret_val =
            (BindableNodeManager)bindablesMap.get(type);

        if(ret_val == null) {
            ret_val = new BindableNodeManager();
            ret_val.setErrorReporter(errorReporter);
            ret_val.setVRMLClock(sensorManager.getVRMLClock());
            bindablesMap.put(type, ret_val);
        }

        return ret_val;
    }

    /**
     * Enable or disable this layer to be currently navigable layer. The
     * navigable layer takes the input from the input devices and interacts
     * with the currently bound viewpoint etc.
     *
     * @param state True to enable this layer as navigable
     */
    public void setActiveNavigationLayer(boolean state) {
        navigationEnabled = state;
        layerSensorManager.setNavigationEnabled(state);
        userInput.sendCurrentNavState();

        if(state) {
            int size = viewpointStatusListeners.size();

            for(int i = 0; i < size; i++) {
                try {
                    ViewpointStatusListener l =
                        (ViewpointStatusListener)viewpointStatusListeners.get(i);

                    l.viewpointLayerActive(layerId);
                } catch(Exception e) {
                    errorReporter.warningReport(VP_ACTIVATE_ERR, e);
                }
            }
        }
    }

    /**
     * Check to see if this is the active navigation layer.
     *
     * @return true if this is the currently active layer for navigation
     */
    public boolean isActiveNavigationLayer() {
        return navigationEnabled;
    }

    /**
     * Set the desired navigation mode. The mode string is one of the
     * spec-defined strings for the NavigationInfo node in the VRML/X3D
     * specification.
     *
     * @param mode The requested mode.
     * @return Whether the mode is valid.
     */
    public boolean setNavigationMode(String mode) {
        return userInput.setNavigationMode(mode);
    }

    /**
     * Get the user's location and orientation.  This will use the viewpoint
     * bound in the active layer.
     *
     * @param pos The current user position
     * @param ori The current user orientation
     */
    public void getUserPosition(Vector3f pos, AxisAngle4f ori) {
        userInput.getPosition(pos);
        userInput.getOrientation(ori);
    }

    /**
     * Move the user's location to see the entire world in this layer. Change
     * the users orientation to look at the center of the world.
     *
     * @param animated Should the transistion be animated.  Defaults to FALSE.
     */
    public void fitToWorld(boolean animated) {

        vpMatrix.setIdentity();

        float[] center = new float[3];

        BoundingVolume bounds = worldRoot.getBounds();
        bounds.getCenter(center);

        float[] boundsMin = new float[3];
        float[] boundsMax = new float[3];

        bounds.getExtents(boundsMin, boundsMax);
        float zrange = Math.abs(boundsMax[2] - boundsMin[2]);

        float mult = 1;

        if (zrange > 1000)
            mult = 1.25f;
        else
            mult = 2;

        float zloc = center[2] + zrange * mult;

        vpMatrix.setTranslation(new Vector3f(center[0], center[1], zloc));

        if (vpTransformGroup.isLive())
            vpTransformGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(vpTransformGroup);
    }

    /**
     * Set the contents that this layer manages to be the ungrouped nodes
     * of the scene. The code should take all the children nodes from this node
     * that are not part of a layer and render them as this layer.
     *
     * @param root The root of the world to handle
     */
    public void setManagedNodes(VRMLWorldRootNodeType root) {
        // The base layer always occupies the full window space.
        viewportType = VIEWPORT_FULLWINDOW;
        viewport = null;

        int size = viewpointStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointLayerAdded(layerId);
            } catch(Exception e) {
                errorReporter.warningReport(VP_LAYER_ADD_ERR, e);
            }
        }

        OGLVRMLNode ogl_root = (OGLVRMLNode)root;

        pendingWorldGeom = (Group)ogl_root.getSceneGraphObject();
        layerSensorManager.setWorldRoot(pendingWorldGeom);
    }

    /**
     * Set the contents that this layer manages the specific layer instance
     * provided.
     *
     * @param layer The root of the layer to handle
     */
    public void setManagedLayer(VRMLLayerNodeType layer) {
        viewportType = layer.getViewportType();

        VRMLNodeType vp = layer.getViewport();
        VRMLNodeType node = null;

        if(vp instanceof VRMLProtoInstance) {
            node = ((VRMLProtoInstance)vp).getImplementationNode();

            while((node != null) && (node instanceof VRMLProtoInstance))
                node = ((VRMLProtoInstance)node).getImplementationNode();

            if((node != null) && !(node instanceof VRMLViewportNodeType)) {
                // if it is invalid, replace the window with a fullscreen one.
                // Ideally we would never hit this as the field should never
                // have been allowed to be set in the first place.
                viewportType = VIEWPORT_FULLWINDOW;
                viewport = null;
                node = null;
            }
        } else if(vp != null &&
                  (!(vp instanceof VRMLViewportNodeType))) {

            // if it is invalid, replace the window with a fullscreen one.
            // Ideally we would never hit this as the field should never have
            // been allowed to be set in the first place.
            viewportType = VIEWPORT_FULLWINDOW;
            viewport = null;
            node = null;
        } else {
            node = vp;
        }

        viewport = (VRMLViewportNodeType)node;

        int size = viewpointStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointLayerAdded(layerId);
            } catch(Exception e) {
                errorReporter.warningReport(VP_LAYER_ADD_ERR, e);
            }
        }

        OGLVRMLNode ogl_root = (OGLVRMLNode)layer;

        pendingWorldGeom = (Group)ogl_root.getSceneGraphObject();
        layerSensorManager.setWorldRoot(pendingWorldGeom);
        layerSensorManager.setIsPickable(layer.isPickable());

        layer.addLayerListener(this);
    }

    /**
     * Override the file field of view values with a value that suits
     * the given output device. A value of 0 = no, otherwise use this
     * instead of content
     *
     * @param fov The fov in degrees.
     */
    public void setHardwareFOV(float fov) {
        // ignore
    }

    /**
     * Set whether stereo is enabled for all layers.
     */
    public void setStereoEnabled(boolean enabled) {
        viewEnvironment.setStereoEnabled(enabled);
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown() {
        sensorManager.removeLayerSensorManager(layerSensorManager);
    }

    /**
     * Update the viewing matrix.  Call this when you want the SensorManager to update
     * the viewing matrix.  Typically after all user input and events have resolved.
     */
    public void updateViewMatrix() {
        layerSensorManager.updateViewMatrix();
    }

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear() {
        viewport = null;
        int size = viewpointStatusListeners.size();


        // if we were the currently active nav layer, then reset the
        // active layer to layer 0, as that's the only one we can
        // guarantee always exists.
        //
        // Note, could be dodgy if we are layer 0. Need to check on this.
        if(navigationEnabled) {
            for(int i = 0; i < size; i++) {
                try {
                    ViewpointStatusListener l =
                        (ViewpointStatusListener)viewpointStatusListeners.get(i);

                    if(layerId == 0)
                        l.viewpointLayerActive(-1);
                    else
                        l.viewpointLayerActive(0);

                } catch(Exception e) {
                    errorReporter.warningReport(VP_ACTIVATE_ERR, e);
                }
            }
        }

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointLayerRemoved(layerId);
            } catch(Exception e) {
                errorReporter.warningReport(VP_LAYER_REMOVE_ERR, e);
            }
        }

        layerSensorManager.clear();
        layerSensorManager.setNavigationEnabled(false);

        vpTransformGroup = null;
        pendingWorldGeom = null;
        worldRoot = null;
        currentViewpoint = null;
        currentNavInfo = null;

        userInput.clear();
    }

    /**
     * Check to see if this is an unmanaged size layer. A layer that has no
     * specific viewport set, or a percentage size.
     *
     * @return One of the VIEWPORT_* constants
     */
    public int getViewportType() {
        return viewportType;
    }

    /**
     * Get the Viewport node that this layer uses. If the layer does not have
     * a viewport set, then it returns null. The value is the real
     * X3DViewportNode instance stripped from any surrounding proto shells etc.
     *
     * @return The current viewport node instance used by the layer
     */
    public VRMLViewportNodeType getViewport() {
        return viewport;
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
        if((l != null) && !viewpointStatusListeners.contains(l)) {
            viewpointStatusListeners.add(l);

            // Inform the listener of our status
            l.viewpointLayerAdded(layerId);

            if(navigationEnabled)
                l.viewpointLayerActive(layerId);

            // Go through the list of our currently available viewpoints
        }
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
    }

    //----------------------------------------------------------
    // Methods defined by VRMLBindableNodeListener
    //----------------------------------------------------------

    /**
     * Notification that a binding stack has requested that this node be now
     * bound as the active node.
     *
     * @param node The source node that is to be bound
     */
    public void newNodeBound(VRMLBindableNodeType node) {
        OGLViewpointNodeType vp = (OGLViewpointNodeType)node;

        changeViewpoints(vp);

        int size = viewpointStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointBound(vp, layerId);
            } catch(Exception e) {
                errorReporter.warningReport(VP_BOUND_ERR, e);
            }
        }
    }

    /**
     * Notification that a new bindable has been added.
     *
     * @param node The new node
     * @param isDefault True if this is a default node instance
     */
    public void bindableAdded(VRMLBindableNodeType node, boolean isDefault) {
        int size = viewpointStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointAdded((VRMLViewpointNodeType)node,
                                 layerId,
                                 isDefault);
            } catch(Exception e) {
                errorReporter.warningReport(VP_ADD_ERR, e);
            }
        }
    }

    /**
     * Notification that a bindable has been removed.
     *
     * @param node The node
     */
    public void bindableRemoved(VRMLBindableNodeType node) {
        int size = viewpointStatusListeners.size();

        for(int i = 0; i < size; i++) {
            try {
                ViewpointStatusListener l =
                    (ViewpointStatusListener)viewpointStatusListeners.get(i);

                l.viewpointRemoved((VRMLViewpointNodeType)node, layerId);
            } catch(Exception e) {
                errorReporter.warningReport(VP_REMOVE_ERR, e);
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by OGLTransformNodeType
    //----------------------------------------------------------

    /**
     * Get the transform matrix for this node.  A reference is ok as
     * the users of this method will not modify the matrix.
     *
     * @return The matrix.
     */
    public Matrix4f getTransform() {
        return worldScaleMatrix;
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {

        if(src == sgTransformGroup) {
            sgTransformGroup.setTransform(worldScaleMatrix);

            if(pendingWorldGeom != null) {
                // Do not parent this world
//                sgTransformGroup.setChild(pendingWorldGeom, 1);
                worldRoot = pendingWorldGeom;
                pendingWorldGeom = null;
            }

            userInput.setPickableScene(sgTransformGroup);
            layerSensorManager.setWorldRoot(sgTransformGroup);

// TODO: I expect this had some interaction with animated viewpoints but its causing timing issues
/*
            SceneGraphPath path = generatePath(vpTransformGroup);

            userInput.setViewInfo(currentViewpoint,
                                  vpTransformGroup,
                                  path);
*/
        } else if(vpTransformGroup == src) {
            vpTransformGroup.setTransform(vpMatrix);

// TODO: I expect this had some interaction with animated viewpoints but its causing timing issues
/*
            SceneGraphPath path = generatePath(vpTransformGroup);

            userInput.setViewInfo(currentViewpoint,
                                  vpTransformGroup,
                                  path);
*/
        } else {
            ((Group)src).removeAllChildren();
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
    }

    //----------------------------------------------------------
    // LayerListener methods
    //----------------------------------------------------------

    /**
     * The pickable status of the layer has changed.
     *
     * @param pickable Is this layer pickable
     */
    public void pickableStateChanged(boolean pickable) {
        layerSensorManager.setIsPickable(pickable);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the viewport layer that this layer manager works with.
     *
     * @return The AV3D layer representation used
     */
    SimpleLayer getLayer() {
        return implLayer;
    }

    /**
     * Convenience method to do the transition between the current viewpoint
     * and the newly given one. Will jump or smooth transition depending on
     * the new viewpoint requirements.
     *
     * @param inSetup true if this is the setup (initialBind) process
     */
    private void changeViewpoints(OGLViewpointNodeType vp) {
        if(vp.getJump()) {

            currentViewpoint = vp;

            // Always reset the viewpoint to the default position as we might
            // be re-binding the same viewpoint, which is supposed to place it
            // back in it's original spot.
            vpTransformGroup = currentViewpoint.getPlatformGroup();
            vpMatrix = currentViewpoint.getViewTransform();

            SceneGraphPath path = generatePath(vpTransformGroup);

            userInput.setViewInfo(currentViewpoint,
                                  vpTransformGroup,
                                  path);

            if(vpTransformGroup.isLive())
                vpTransformGroup.boundsChanged(this);
            else
                vpTransformGroup.setTransform(vpMatrix);

            switch(currentViewpoint.getProjectionType()) {
                case VRMLViewpointNodeType.PROJECTION_PERSPECTIVE:
                    projectionType = ViewEnvironment.PERSPECTIVE_PROJECTION;

                    break;

                case VRMLViewpointNodeType.PROJECTION_ORTHO:
                    projectionType = ViewEnvironment.ORTHOGRAPHIC_PROJECTION;
                    break;

                default:
                    errorReporter.warningReport(UNKNOWN_PROJ_TYPE_MSG, null);
                    projectionType = ViewEnvironment.PERSPECTIVE_PROJECTION;
            }

            viewEnvironment.setProjectionType(projectionType);

        } else {

System.out.println("non-jump transistions not handled yet");
            // Copied code from jump section

            currentViewpoint = vp;

            // Always reset the viewpoint to the default position as we might
            // be re-binding the same viewpoint, which is supposed to place it
            // back in it's original spot.
            vpTransformGroup = currentViewpoint.getPlatformGroup();
            vpMatrix = currentViewpoint.getViewTransform();

            SceneGraphPath path = generatePath(vpTransformGroup);

            userInput.setViewInfo(currentViewpoint,
                                  vpTransformGroup,
                                  path);

            if(vpTransformGroup.isLive())
                vpTransformGroup.boundsChanged(this);
            else
                vpTransformGroup.setTransform(vpMatrix);

            switch(currentViewpoint.getProjectionType()) {
                case VRMLViewpointNodeType.PROJECTION_PERSPECTIVE:
                    projectionType = ViewEnvironment.PERSPECTIVE_PROJECTION;
                    break;

                case VRMLViewpointNodeType.PROJECTION_ORTHO:
                    projectionType = ViewEnvironment.ORTHOGRAPHIC_PROJECTION;
                    break;

                default:
                    errorReporter.warningReport(UNKNOWN_PROJ_TYPE_MSG, null);
                    projectionType = ViewEnvironment.PERSPECTIVE_PROJECTION;
            }

            viewEnvironment.setProjectionType(projectionType);

            // End copied code
/*
            // We have to do a smooth transition between the points
            TransformGroup old_tg = currentViewpoint.getPlatformGroup();
            TransformGroup new_tg = vp.getPlatformGroup();

            oldTx.setIdentity();
            destTx.setIdentity();
            Matrix4f final_tx = vp.getViewTransform();

            getLocalToVworld(old_tg, oldTx);
            getLocalToVworld(new_tg, destTx);

            // To work out the transition, the transform group of the dest
            // viewpoint must be set to exactly the virtual world position of
            // the old viewpoint and then allowed to transition across to the
            // final destination. However, we must allow for the differences
            // in their parent transforms first and make sure we have a proper
            // scale value.

            Vector3f old_trans = new Vector3f();
            Vector3f dest_trans = new Vector3f();
            Vector3f new_trans = new Vector3f();
            Quat4f old_orient = new Quat4f();
            Quat4f dest_orient = new Quat4f();
            Quat4f new_orient = new Quat4f();

            dest_orient.set(destTx);
            old_orient.set(oldTx);


            destTx.get(dest_orient);
            destTx.get(dest_trans);
            oldTx.get(old_orient);
            oldTx.get(old_trans);

            new_trans.sub(dest_trans, old_trans);
            new_orient.sub(dest_orient, old_orient);

            Matrix4f new_tx = new Matrix4f();
            new_tx.set(new_orient, new_trans, 1);
            new_tx.set(new_trans);

            vpTransformGroup = new_tg;

            vpMatrix = new_tx;
            new_tg.boundsChanged(this);

            currentViewpoint = vp;

            if(hardwareFOV != 0f)
                viewEnvironment.setFieldOfView(hardwareFOV);
            else
                viewEnvironment.setFieldOfView(currentViewpoint.getFieldOfView()  *
                                               DEG_TO_RAD);
*/
        }

        if(currentNavInfo != null)
            globalEffects.useHeadlight(currentNavInfo.getHeadlight());
    }

    /**
     * From the given node, create a scene graph path to the root. Used
     * to make the path for the view handling. If a shared group is
     * encountered, just take the first parent all the time
     *
     * @param leaf The terminal node of the path
     * @return A path from here to the root
     */
    private SceneGraphPath generatePath(Node leaf) {

        Node parent = leaf.getParent();
        pathList.clear();

        if((leaf == null) || (parent == null))
            return null;

        while(parent != null) {
            if(parent instanceof SharedGroup) {
                SharedGroup sg = (SharedGroup)parent;

                if(sg.numParents() == 0)
                    break;

                sg.getParents(pathNodes);
                parent = pathNodes[0];
                pathList.add(parent);
            } else {
                pathList.add(parent);
                parent = parent.getParent();
            }
        }

        // Invert the array while copying it into the path.
        pathMatrix.setIdentity();
        int size =  pathList.size();

        if(pathNodes.length < size)
            pathNodes = new Node[size];

        for(int i = 0; i < size; i++)
            pathNodes[i] = (Node)pathList.get(size - i - 1);

        return new SceneGraphPath(pathNodes, size, pathMatrix, pathMatrix);
    }

    /**
     * Convenience method to walk to the root of the scene and calculate the
     * root to virtual world coordinate location of the given node. If a
     * sharedGroup is found, then take the first parent listed always.
     *
     * @param terminal The end node to calculate from
     * @param mat The matrix to put the final result into
     */
    private void getLocalToVworld(Node terminal, Matrix4f mat) {

        Node parent = terminal.getParent();
        pathList.clear();

        if(parent instanceof TransformGroup)
            pathList.add(parent);

        while(parent != null) {
            if(parent instanceof SharedGroup) {
                SharedGroup sg = (SharedGroup)parent;

                int num_parents = sg.numParents();

                if(num_parents == 0)
                    break;
                else if(num_parents > pathNodes.length)
                    pathNodes = new Node[num_parents];

                sg.getParents(pathNodes);
                parent = pathNodes[0];
            } else {
                if(parent instanceof TransformGroup)
                    pathList.add(parent);

                parent = parent.getParent();
            }
        }

        int num_nodes = pathList.size();
        mat.setIdentity();
        pathMatrix.setIdentity();

        // use vwTransform for fetching the tx. It is only a temp var anyway
        for(int i = num_nodes - 1; i >= 0; i--) {
            TransformGroup tg = (TransformGroup)pathList.get(i);
            tg.getTransform(pathMatrix);

            mat.mul(pathMatrix);
        }
    }
}
