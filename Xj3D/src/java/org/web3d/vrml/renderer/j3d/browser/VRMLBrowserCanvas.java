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

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Local imports
import org.web3d.browser.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;

import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;
import org.web3d.vrml.renderer.j3d.J3DSceneBuilderFactory;
import org.web3d.vrml.renderer.j3d.input.J3DPickingManager;
import org.web3d.vrml.renderer.j3d.input.J3DSensorManager;
import org.xj3d.core.eventmodel.InputDeviceManager;
import org.xj3d.core.eventmodel.KeyDeviceSensorManager;
import org.xj3d.core.eventmodel.DeviceFactory;
import org.web3d.vrml.sav.InputSource;

import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;

import org.xj3d.ui.awt.device.AWTDeviceFactory;

/**
 * A single canvas that can display a VRML scene graph.
 * <p>
 *
 * The aim of this canvas is to work in one of two modes - a single canvas
 * that displays the full VRML content, or part of a series of canvases that
 * are used together to form a common view of a single VRML scene graph.
 * The first option represents your typical VRMLbrowser situation, where the
 * latter represents an immercive environment like a CAVE or stereo
 * glasses. The setup of the constructor determines which of these two modes
 * you operate in.
 * <p>
 *
 * To operate in multicanvas mode, you first start with a single canvas.
 * This canvas is then used as the source of information for all the
 * other canvases. They feed from this central item of information and
 * work from there to build their extra scene information.
 * <p>
 *
 * Startup of the canvas is a two-phase process. In the first phase, you get
 * Just the canvas and a bit of view information set up. There is no live
 * VRML scene graph at this point, and the various getter methods will
 * return null. After that, there is a second stage that is achieved by
 * calling the <code>initialize()</code> method. This creates the VRML
 * structures needed by this class. This second step is quite time consuming
 * so it allows the caller code to get a UI item on the screen as quickly as
 * possible and then call a separate thread to start the initialisation
 * process in a separate thread.
 * <p>
 *
 * As part of the strartup process, a lot of loading of extra items needs
 * to be performed. Instead of requiring the user to create their own, this
 * class allows the information to be specified as a collection of system
 * properties. The only part that is not loaded as part of this startup
 * process are the scripting engines. The end user must create their own
 * scripting engine(s) and register those with the ScriptManager, which is
 * available from this class after initialize() has been called.
 * <p>
 *
 * The following system properties can be defined as part of this class:
 *
 * <ul>
 * <li><code>org.xj3d.script.loader.class</code> The name of the class
 *     that implements the {@link org.xj3d.core.loading.ScriptLoader}
 *     interface, which is used for loading scripts.
 * </li>
 * <li><code>org.xj3d.script.manager.class</code> The name of the class
 *     that implements the {@link org.xj3d.core.eventmodel.ScriptManager}
 *     interface, which is used for managing scripts.
 * </li>
 * <li><code>org.xj3d.file.loader.class</code> The name of the class
 *     that implements the
 *     {@link org.xj3d.core.loading.ContentLoadManager} interface,
 *     which is used for loading content other than scripts.
 * </li>
 * <li><code>org.xj3d.router.manager.class</code> The name of the class
 *     that implements the {@link org.xj3d.core.eventmodel.RouteManager}
 *     interface, which is used for managing routes.
 * </li>
 * <li><code>org.xj3d.router.factory.class</code> The name of the class
 *     that implements the {@link org.xj3d.core.eventmodel.RouterFactory}
 *     interface, which is used for creating routers.
 * </li>
 * <li><code>org.xj3d.frame.state.class</code> The name of the class
 *     that implements the {@link org.web3d.vrml.nodes.FrameStateManager}
 *     interface, which is used for managing per-frame state.
 * </li>
 * <li><code>org.xj3d.sensor.manager.class</code> The name of the class
 *     that implements the
 *     {@link org.web3d.vrml.renderer.j3d.input.J3DSensorManager} interface, which
 *     is used for managing sensors.
 * </li>
 * <li><code>org.xj3d.picking.manager.class</code> The name of the class
 *     that implements the
 *     {@link org.web3d.vrml.renderer.j3d.input.J3DPickingManager} interface, which
 *     is used for managing sensors.
 * </li>
 * <li><code>org.xj3d.eventmodel.evaluator.class</code> The name of the
 *     class that implements the
 *     {@link org.xj3d.core.eventmodel.EventModelEvaluator} interface,
 *     which is used for runing the event model.
 * </li>
 * </ul>
 *
 * <b>Note</b> This code already makes use off the link selection listener
 * with the universe. Callers should <i>not</i> register their own listener
 * with the universe, but should register it with this class.
 *
 * @author Justin Couch
 * @version $Revision: 1.45 $
 */
public class VRMLBrowserCanvas extends Canvas3D
    implements SensorStatusListener, BrowserCoreListener, OverlayHandler {

    /**
     * Property defining the script loader instance to use. This should
     * name a class that implements the
     * {@link org.xj3d.core.loading.ScriptLoader} interface.
     */
    public static final String SCRIPT_LOADER_PROP =
        "org.xj3d.script.loader.class";

    /**
     * Property defining the script loader instance to use. This should
     * name a class that implements the
     * {@link org.xj3d.core.eventmodel.ScriptManager} interface.
     */
    public static final String SCRIPT_MANAGER_PROP =
        "org.xj3d.script.manager.class";

    /**
     * Property defining the loader for external files to use. This should
     * name a class that implements the
     * {@link org.xj3d.core.loading.ContentLoadManager} interface.
     */
    public static final String FILE_LOADER_PROP =
        "org.xj3d.file.loader.class";

    /**
     * Property defining the factory implementation for providing router
     * instances. This should name a class that implements the
     * {@link org.xj3d.core.eventmodel.RouterFactory} interface.
     */
    public static final String ROUTER_FACTORY_PROP =
        "org.xj3d.router.factory.class";

    /**
     * Property defining the manager implementation for providing router
     * instances. This should name a class that implements the
     * {@link org.xj3d.core.eventmodel.RouteManager} interface.
     */
    public static final String ROUTER_MANAGER_PROP =
        "org.xj3d.router.manager.class";

    /**
     * Property defining the manager implementation for providing router
     * instances. This should name a class that implements the
     * {@link org.web3d.vrml.nodes.FrameStateManager} interface.
     */
    public static final String STATE_MANAGER_PROP =
        "org.xj3d.state.manager.class";

    /**
     * Property defining the manager implementation for sensor
     * instances. This should name a class that implements the
     * {@link org.web3d.vrml.renderer.j3d.input.J3DSensorManager} interface.
     */
    public static final String SENSOR_MANAGER_PROP =
        "org.xj3d.sensor.manager.class";

    /**
     * Property defining the manager implementation for sensor
     * instances. This should name a class that implements the
     * {@link org.xj3d.core.eventmodel.NetworkManager} interface.
     */
    public static final String NETWORK_MANAGER_PROP =
        "org.xj3d.network.manager.class";

    /**
     * Property defining the manager implementation for picking sensor
     * instances. This should name a class that implements the
     * {@link org.web3d.vrml.renderer.j3d.input.J3DPickingManager} interface.
     */
    public static final String PICKING_MANAGER_PROP =
        "org.xj3d.sensor.manager.class";

    /**
     * Property defining the manager implementation of the event model.
     * This should name a class that implements the
     * {@link org.xj3d.core.eventmodel.EventModelEvaluator} interface.
     */
    public static final String EVENT_MODEL_PROP =
        "org.xj3d.eventmodel.evaluator.class";

    // Defaults for the above properties if none are defined

    /** Default class for the script loader */
    private static final String DEFAULT_SCRIPT_LOADER =
        "org.xj3d.impl.core.loading.DefaultScriptLoader";

    /** Default class for the script manager */
    private static final String DEFAULT_SCRIPT_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultScriptManager";

    /** Default class for the external file loader */
    private static final String DEFAULT_EXTERNAL =
        "org.xj3d.impl.core.loading.MemCacheLoadManager";

    /** Default class for the router factory */
    private static final String DEFAULT_ROUTER_FACTORY =
        "org.xj3d.impl.core.eventmodel.ListsRouterFactory";

    /** Default class for the router factory */
    private static final String DEFAULT_ROUTER_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultRouteManager";

    /** Default class for the frame state manager */
    private static final String DEFAULT_STATE_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultFrameStateManager";

    /** Default class for the sensor manager */
    private static final String DEFAULT_SENSOR_MANAGER =
        "org.web3d.vrml.renderer.common.input.DefaultSensorManager";

    /** Default class for the network manager */
    private static final String DEFAULT_NETWORK_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultNetworkManager";

    /** Default class for the picking manager */
    private static final String DEFAULT_PICKING_MANAGER =
        "org.web3d.vrml.renderer.j3d.input.DefaultPickingManager";

    /** Default class for the event manager */
    private static final String DEFAULT_EVENT_MODEL =
        "org.xj3d.impl.core.eventmodel.DefaultEventModelEvaluator";

    // Error messages

    /** Message string when the selected VP is not a viewpoint */
    private static final String NOT_VP_MSG =
        "Referenced item is not a viewpoint";

    /** Attempt to load an invalid script loader class */
    private static final String NO_SCRIPT_LOAD_MSG =
        "The class that you specificed for the script loader is not a " +
        " valid instance of ScriptLoader";

    /** Attempt to load an invalid script loader class */
    private static final String NO_SCRIPT_MGR_MSG =
        "The class that you specificed for the script manager is not a " +
        "valid instance of ScriptManager";

    /** Attempt to load an invalid file loader class */
    private static final String NO_LOADER_LOAD_MSG =
        "The class that you specificed for the load manager is not a " +
        "valid instance of ContentLoadManager";

    /** Attempt to load an invalid router factory class */
    private static final String NO_ROUTER_LOAD_MSG =
        "The class that you specificed for the router factory is not a " +
        "valid instance of RouterFactory";

    /** Attempt to load an invalid router factory class */
    private static final String NO_ROUTER_MGR_MSG =
        "The class that you specificed for the router manager is not a " +
        "valid instance of RouteManager";

    private static final String NO_STATE_MGR_MSG =
        "The class that you specificed for the frame state manager is not a " +
        "valid instance of FrameStateManager";

    private static final String NO_SENSOR_MGR_MSG =
        "The class that you specificed for the sensor manager is not a " +
        "valid instance of J3DSensorManager";

    private static final String NO_NETWORK_MGR_MSG =
        "The class that you specificed for the sensor manager is not a " +
        "valid instance of NetworkManager";

    private static final String NO_PICKING_MGR_MSG =
        "The class that you specificed for the sensor manager is not a " +
        "valid instance of J3DPickingManager";

    private static final String NO_EVENT_MODEL_MSG =
        "The class that you specificed for the event model is not a " +
        "valid instance of EventModelEvaluator";

    /** When sending a link changed event and there is an error */
    private static final String SEND_LINK_MSG =
        "There was an error sending the link changed notification";

    /** When the URL failed to load and this is a callback from the core */
    private static final String LOAD_URL_FAIL_MSG =
        "The URLs failed to load: ";

    // Standard vars....

    /** The current viewpoint model */
    private VRMLViewpointNodeType currentViewpoint;

    /** The universe we are currently using */
    private J3DStandardBrowserCore universe;

    /** The View that we use for everything */
    private View localView;

    /** The script manager instance shared between all */
    private ScriptManager scriptManager;

    /** Frame state manager in use */
    private FrameStateManager stateManager;

    /** Load manager for loading external content */
    private ContentLoadManager loadManager;

    /** Manager for route propogation */
    private RouteManager routeManager;

    /** manager for input and sensor handling */
    private J3DSensorManager sensorManager;

    /** manager for the network handling */
    private NetworkManager networkManager;

    /** Overarching event model manager */
    private EventModelEvaluator eventModel;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** Manager of viewpoints for the canvas */
    private ViewpointManager viewpointManager;

    /** The set of child canvases */
    private HashSet childCanvases;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** Flag to say this is vrml97 only */
    private boolean vrml97Only;

    /** Flag to control how the link callbacks are handled */
    private boolean disableInternalLink;

    /** The device factory for pointing and keysensor devices
     *  associated with the drawing surface */
    private DeviceFactory deviceFactory;

    /**
     * Construct a default, empty canvas with the option of restricting the
     * content to being VRML97 only. It contains a single view that is set at
     * the default projection.Internal link handling is enabled by default.
     *
     * @param cfg The graphics configuration to use
     * @param vrml97Only true if this is to be restricted to VRML97 only
     */
    public VRMLBrowserCanvas(GraphicsConfiguration cfg, boolean vrml97Only) {

        this(cfg, vrml97Only, null, false);
    }

    /**
     * Construct an empty canvase that contains a single view
     * that is provided by the user..This constructor would be used when
     * you want to create the initial eye of a stereo pair. If the view
     * is null then then a default view is created. Internal link handling
     * is enabled by default.
     *
     * @param cfg The graphics configuration to use
     * @param vrml97Only true if this is to be restricted to VRML97 only
     * @param view The view information to use for this class
     */
    public VRMLBrowserCanvas(GraphicsConfiguration cfg, boolean vrml97Only, View view) {

        this(cfg, vrml97Only, view, false);
    }

    /**
     * Construct a default, empty canvas with the option of restricting the
     * content to being VRML97 only and disabling the internal linke selection
     * handling. It contains a single view that is set at the default
     * projection.
     *
     * @param cfg The graphics configuration to use
     * @param vrml97Only true if this is to be restricted to VRML97 only
     * @param disableInternalLink true to disable the handling of internal
     *    link (Anchor) processing and only call the external
     *    LinkSelectionListener.
     */
    public VRMLBrowserCanvas(GraphicsConfiguration cfg,
        boolean vrml97Only, boolean disableInternalLink) {

        this(cfg, vrml97Only, null, disableInternalLink);
    }

    /**
     * Construct an empty canvase that contains a single view
     * that is provided by the user..This constructor would be used when
     * you want to create the initial eye of a stereo pair. If the view
     * is null then then a default view is created.
     *
     * @param cfg The graphics configuration to use
     * @param vrml97Only true if this is to be restricted to VRML97 only
     * @param view The view information to use for this class
     * @param disableInternalLink true to disable the handling of internal
     *    link (Anchor) processing and only call the external
     *    LinkSelectionListener.
     */
    public VRMLBrowserCanvas(GraphicsConfiguration cfg, boolean vrml97Only,
        View view, boolean disableInternalLink) {

        super(cfg);

        this.deviceFactory = new AWTDeviceFactory(
            this,
            Xj3DConstants.JAVA3D_ID,
            null,
            null );

        this.vrml97Only = vrml97Only;
        this.disableInternalLink = disableInternalLink;

        if(view == null)
            localView = new View();
        else
            localView = view;

        localView.addCanvas3D(this);

        //localView.setMinimumFrameCycleTime(20);
        //localView.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);

        setBackground(Color.black);
        childCanvases = new HashSet();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by OverlayHandler
    //----------------------------------------------------------

    /**
     * Fetch the canvas that will be responsible for having the overlays
     * composited on them.
     *
     * @return The canvas instance to use
     */
    public Canvas3D getPrimaryCanvas() {
        return this;
    }

    //----------------------------------------------------------
    // Methods defined by SensorStatusListener
    //----------------------------------------------------------

    /**
     * Invoked when a sensor/anchor is in contact with a tracker capable of picking.
     *
     * @param type The sensor type
     * @param desc The sensor's description string
     */
    public void deviceOver(int type, String desc) {
    }

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    public void deviceNotOver(int type) {
    }

    /**
     * Invoked when a tracker activates the sensor.  Anchors will not receive
     * this event, they get a linkActivated call.
     *
     * @param type The sensor type
     */
    public void deviceActivated(int type) {
    }

    /**
     * Invoked when a tracker follows a link.
     *
     * @param urlList The url to load.
     */
    public void linkActivated(String[] urlList) {

        if(!disableInternalLink) {

            for(int i = 0; i < urlList.length; i++) {
                if(urlList[i].charAt(0) == '#') {
                    // move to the viewpoint.
                    String def_name = urlList[i].substring(1);
                    VRMLViewpointNodeType vp =
                        (VRMLViewpointNodeType)viewpointDefMap.get(def_name);

                    if(vp != null) {
                        VRMLClock clk = universe.getVRMLClock();

                        double time = clk.getTime();
                        if(currentViewpoint != null)
                            currentViewpoint.setBind(false, true, time);
                        vp.setBind(true, true, time);
                        currentViewpoint = vp;
                    } else {
                        errorReporter.warningReport(NOT_VP_MSG, null);
                    }
                } else {
                    System.out.println("Warning only loading the first URL");
                    loadWorld(urlList[0]);
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //----------------------------------------------------------

    /**
     * Notification that the browser is shutting down the current content.
     * Use it to clear out any current items that only last for this
     * world.
     */
    public void browserShutdown() {
        viewpointDefMap.clear();
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
        errorReporter.errorReport(LOAD_URL_FAIL_MSG + msg, null);
    }

    /**
     * Notification that a world has been loaded into the core of the browser.
     * Use this information to rebuild the viewpoint def map.
     *
     * @param scene The new scene that has been loaded
     */
    public void browserInitialized(VRMLScene scene) {
        // Finally set up the viewpoint def name list. Have to start from
        // the list of DEF names as the Viewpoint nodes don't store the DEF
        // name locally.
        VRMLViewpointNodeType active_vp = universe.getViewpoint();
        currentViewpoint = active_vp;

        Map def_map = scene.getDEFNodes();
        Iterator itr = def_map.keySet().iterator();

        while(itr.hasNext()) {
            String key = (String)itr.next();
            Object vp = def_map.get(key);

            if(vp instanceof VRMLViewpointNodeType)
                viewpointDefMap.put(key, vp);
        }
    }

    //----------------------------------------------------------
    // Local methods.
    //----------------------------------------------------------

    /**
     * Make this canvas go through all its initialisation process now. This
     * will make sure that all dependent canvases are ready to go with the
     * same information as well. The system properties for the various loaders
     * must be set before calling this method. Setting them after this pint
     * will result in the information being ignored.
     */
    public void initialize() {

        // first fetch all the sytem properties in one hit using the
        // privileges API.
        String[] props = (String[])AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    // privileged code goes here, for example:
                    String[] ret_val = new String[10];

                    ret_val[0] = System.getProperty(SCRIPT_LOADER_PROP,
                        DEFAULT_SCRIPT_LOADER);
                    ret_val[1] = System.getProperty(SCRIPT_MANAGER_PROP,
                        DEFAULT_SCRIPT_MANAGER);
                    ret_val[2] = System.getProperty(FILE_LOADER_PROP,
                        DEFAULT_EXTERNAL);
                    ret_val[3] = System.getProperty(ROUTER_FACTORY_PROP,
                        DEFAULT_ROUTER_FACTORY);
                    ret_val[4] = System.getProperty(ROUTER_MANAGER_PROP,
                        DEFAULT_ROUTER_MANAGER);
                    ret_val[5] = System.getProperty(STATE_MANAGER_PROP,
                        DEFAULT_STATE_MANAGER);
                    ret_val[6] = System.getProperty(SENSOR_MANAGER_PROP,
                        DEFAULT_SENSOR_MANAGER);
                    ret_val[7] = System.getProperty(PICKING_MANAGER_PROP,
                        DEFAULT_PICKING_MANAGER);
                    ret_val[8] = System.getProperty(EVENT_MODEL_PROP,
                        DEFAULT_EVENT_MODEL);
                    ret_val[9] = System.getProperty(NETWORK_MANAGER_PROP,
                        DEFAULT_NETWORK_MANAGER);
                    return ret_val;
                }
            }
            );

        ScriptLoader s_loader = null;
        RouterFactory r_fac = null;
        J3DPickingManager picker = null;
        int num_node_managers = 0;

        s_loader = (ScriptLoader)loadClass(props[0], true, NO_SCRIPT_LOAD_MSG);
        scriptManager = (ScriptManager)loadClass( props[1], true, NO_SCRIPT_MGR_MSG);
        loadManager = (ContentLoadManager)loadClass(props[2], true, NO_LOADER_LOAD_MSG);
        r_fac = (RouterFactory)loadClass(props[3], true, NO_ROUTER_LOAD_MSG);
        routeManager = (RouteManager)loadClass(props[4], true, NO_ROUTER_MGR_MSG);
        stateManager = (FrameStateManager)loadClass(props[5], true, NO_STATE_MGR_MSG);
        sensorManager = (J3DSensorManager)loadClass(props[6], true, NO_SENSOR_MGR_MSG);
        picker = (J3DPickingManager)loadClass(props[7], false, NO_PICKING_MGR_MSG);
        eventModel = (EventModelEvaluator)loadClass(props[8], true, NO_EVENT_MODEL_MSG);

        networkManager = (NetworkManager)loadClass(props[9],false,NO_NETWORK_MGR_MSG);
        if (networkManager != null)
            num_node_managers++;

        if (sensorManager != null) {
            if ( deviceFactory != null ) {
                InputDeviceManager idm = new InputDeviceManager( deviceFactory );
                KeyDeviceSensorManager kdsm = new KeyDeviceSensorManager( deviceFactory );

                sensorManager.setInputManager(idm);
                sensorManager.setKeyDeviceSensorManager(kdsm);
                sensorManager.setPickingManager(picker);
            }
        }

        if (networkManager != null) {
            try {
                DISProtocolHandler dis_handler = new DISProtocolHandler();
                networkManager.addProtocolHandler(dis_handler);
            } catch(NoClassDefFoundError ncdfe) {
                errorReporter.warningReport("DISProtocol handler not found", null);
            }
        }

        NodeManager[] node_managers = new NodeManager[num_node_managers];
        num_node_managers = 0;

        if(networkManager != null)
            node_managers[num_node_managers++] = networkManager;

        if (scriptManager == null || routeManager == null || sensorManager == null ||
            stateManager == null || loadManager == null) {

            throw new InvalidConfigurationException("Missing required Manager");
        }

        routeManager.setRouterFactory(r_fac);
        scriptManager.setScriptLoader(s_loader);

        viewpointManager = new DefaultViewpointManager(universe);

        eventModel.initialize(scriptManager,
            routeManager,
            sensorManager,
            stateManager,
            loadManager,
            viewpointManager,
            node_managers);

        // now create the universe
        universe = new J3DStandardBrowserCore(eventModel, true, this);

        universe.setPrimaryView(localView);
        universe.addSensorStatusListener(this);
        universe.addCoreListener(this);

        // Do we have any dependent canvases? If so, iterate through them and
        // add their views to the mix.
        Object[] canvas_list = childCanvases.toArray();
        for(int i = 0; i < canvas_list.length; i++) {
            Canvas3D canvas = (Canvas3D)canvas_list[i];
            universe.addView(canvas.getView());
        }

        // Now, set up the infrastructure needed to load new worlds and
        // stuff.

        viewpointDefMap = new HashMap();

        SceneBuilderFactory builder_fac =
            new J3DSceneBuilderFactory(vrml97Only,
            true,
            true,
            true,
            true,
            true,
            true);

        VRMLParserFactory parser_fac = null;

        try {
            parser_fac = VRMLParserFactory.newVRMLParserFactory();

        } catch(FactoryConfigurationError fce) {
            throw new RuntimeException("Failed to load factory");
        }

        if(vrml97Only) {
            parser_fac.setProperty(VRMLParserFactory.REQUIRE_VERSION_PROP,"2.0");
            parser_fac.setProperty(VRMLParserFactory.REQUIRE_FORMAT_PROP,"VRML");
        }

        worldLoader = new DefaultWorldLoaderManager(universe,
            stateManager,
            routeManager);
        worldLoader.setErrorReporter(errorReporter);
        worldLoader.registerBuilderFactory(Xj3DConstants.JAVA3D_RENDERER,
            builder_fac);
        worldLoader.registerParserFactory(Xj3DConstants.JAVA3D_RENDERER,
            parser_fac);

    }

    /**
     * Control the rendering state. Allows the system to be shutdown temporarily
     * at the lowest level, effectively pausing the entire system.
     *
     * @param state true if this should be set to the running state
     */
    public void setEnabled(boolean state) {
        if(state)
            startRenderer();
        else
            stopRenderer();
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
        localView.setMinimumFrameCycleTime(millis);
    }

    /**
     * A request to loade the world given by the URL string. This string
     * must point to a proper, valid URL string because this code will not
     * check or correct the given value and will cause a crash of the parsing
     * process otherwise.
     *
     * @param url The URL to attempt to load
     */
    public void loadWorld(String url) {
        WorldLoader loader = worldLoader.fetchLoader();

        VRMLScene parsed_scene = null;

        try {
            InputSource is = new InputSource(url);
            parsed_scene = loader.loadNow(universe, is);
        } catch(Exception e) {
            errorReporter.errorReport("Failed to load ", e);
            worldLoader.releaseLoader(loader);
            return;
        }

        worldLoader.releaseLoader(loader);

        String vpUrl = null;

        int refLoc = url.indexOf("#");
        if (refLoc > -1)
            vpUrl = url.substring(refLoc+1);

        universe.setScene(parsed_scene, vpUrl);

        // Finally set up the viewpoint def name list. Have to start from
        // the list of DEF names as the Viewpoint nodes don't store the DEF
        // name locally.
        VRMLViewpointNodeType active_vp = universe.getViewpoint();
        currentViewpoint = active_vp;

        viewpointDefMap.clear();
        Map def_map = parsed_scene.getDEFNodes();
        Iterator itr = def_map.keySet().iterator();

        while(itr.hasNext()) {
            String key = (String)itr.next();
            Object vp = def_map.get(key);

            if(vp instanceof VRMLViewpointNodeType)
                viewpointDefMap.put(key, vp);
        }
    }

    /**
     * Fetch the script loader in use by this class
     *
     * @return The current loader instance
     */
    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    /**
     * Fetch the viewpoint manager in use by this class.
     *
     * @return The current viewpoint manager.
     */
    public ViewpointManager getViewpointManager() {
        return viewpointManager;
    }

    /**
     * Fetch the load manager in use by this class
     *
     * @return The current load manager instance
     */
    public ContentLoadManager getContentLoadManager() {
        return loadManager;
    }

    /**
     * Get the current route manager in use with this class.
     *
     * @return The current route manager in use
     */
    public RouteManager getRouteManager() {
        return routeManager;
    }

    /**
     * Get the current sensor manager in use with this class.
     *
     * @return The current sensor manager in use
     */
    public SensorManager getSensorManager() {
        return sensorManager;
    }

    /**
     * Get the current world loader manager in use with this class.
     *
     * @return The current world loader manager in use
     */
    public WorldLoaderManager getWorldLoaderManager() {
        return worldLoader;
    }

    /**
     * Get the current frame state manager in use with this class.
     *
     * @return The current frame state manager in use
     */
    public FrameStateManager getFrameStateManager() {
        return stateManager;
    }

    /**
     * Get the current event model handler in use with this class.
     *
     * @return The current event model in use
     */
    public EventModelEvaluator getEventModelEvaluator() {
        return eventModel;
    }

    /**
     * Get the universe used by this instant.
     */
    public J3DStandardBrowserCore getUniverse() {
        return universe;
    }

    /**
     * Register the given canvas as being dependent on this canvas for init
     * information and anything else. Each instance can only be added once,
     * so calling this multiple times will with the same instance will
     * silently ignore any requests after the first.
     *
     * @param canvas The dependent instance to add
     */
    public void registerDependentCanvas(VRMLDependentCanvas canvas) {
        if(childCanvases.contains(canvas))
            return;

        childCanvases.add(canvas);

        if(universe != null)
            universe.addView(canvas.getView());
    }

    /**
     * Unregister the dependent canvas from this primary canvas. If the
     * canvas was not previously registered, this will silently ignore the
     * request.
     *
     * @param canvas The dependent instance to remove
     */
    public void unregisterDependentCanvas(VRMLDependentCanvas canvas) {
        if(!childCanvases.contains(canvas))
            return;

        childCanvases.remove(canvas);

        if(universe != null)
            universe.removeView(canvas.getView());
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = (reporter == null) ?
            new DefaultErrorReporter() :
            reporter;

        // Are we pre or post initialise? If we are pre then do nothing.
        // If it is post, send the reporter through to the other areas.
        // Since the world loader is always created at the end of initialise()
        // that will be the marker.
        if(worldLoader == null)
            return;

        scriptManager.setErrorReporter(errorReporter);
        stateManager.setErrorReporter(errorReporter);
        loadManager.setErrorReporter(errorReporter);
        routeManager.setErrorReporter(errorReporter);
        sensorManager.setErrorReporter(errorReporter);
        networkManager.setErrorReporter(errorReporter);
        eventModel.setErrorReporter(errorReporter);
        worldLoader.setErrorReporter(errorReporter);
    }

    //----------------------------------------------------------
    // Internal convenience methods.
    //----------------------------------------------------------

    /**
     * Load a factory class instance as needed. Separated out as a new method
     * because it will need to perform the privileged action of class loading,
     * which is not permitted under an applet.
     *
     * @param classname The fully qualified name of the class to load
     * @param required Is this class required for operation
     * @param msg The message to issue if its not found.
     */
    private Object loadClass(final String classname, final boolean required, final String msg) {
        Object ret_val = AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    try {
                        Class cls = Class.forName(classname);
                        return cls.newInstance();
                    } catch(Error err) {
                        if (required) {
                            Exception ewrapper = new Exception(err.getMessage(), err);
                            errorReporter.errorReport(msg, ewrapper);
                        } else
                            errorReporter.warningReport(msg, null);
                    } catch(Exception e) {
                        if (required)
                            errorReporter.errorReport(msg, e);
                        else
                            errorReporter.warningReport(msg, null);
                    }

                    return null;
                }
            }
            );

        return ret_val;
    }
}
