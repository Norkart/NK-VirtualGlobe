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
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.pipeline.audio.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.j3d.aviatrix3d.output.audio.OpenALAudioDevice;
import org.j3d.aviatrix3d.output.graphics.ElumensAWTSurface;
import org.j3d.aviatrix3d.output.graphics.ElumensSWTSurface;

// TEST:
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

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
import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;

import org.web3d.vrml.sav.InputSource;

import org.xj3d.impl.core.eventmodel.DefaultViewpointManager;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;

import org.xj3d.sai.BrowserConfig;

/**
 * A single canvas that can display a VRML scene graph.
 * <p>
 *
 * The aim of this canvas is to work in one of two modes - a single canvas
 * that displays the full VRML content, or part of a series of canvases that
 * are used together to form a common view of a single VRML scene graph.
 * The first option represents your typical VRMLbrowser situation, where the
 * latter represents an immersive environment like a CAVE or stereo
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
 * possible and then call a separate thread to start the initialization
 * process in a separate thread.
 * <p>
 *
 * As part of the startup process, a lot of loading of extra items needs
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
 *     {@link org.xj3d.core.eventmodel.SensorManager} interface, which
 *     is used for managing sensors.
 * </li>
 * <li><code>org.xj3d.eventmodel.evaluator.class</code> The name of the
 *     class that implements the
 *     {@link org.xj3d.core.eventmodel.EventModelEvaluator} interface,
 *     which is used for running the event model.
 * </li>
 * </ul>
 *
 * <b>Note</b> This code already makes use of the link selection listener
 * with the universe. Callers should <i>not</i> register their own listener
 * with the universe, but should register it with this class.
 *
 * <b>Note</b>This class starts with its enabled state as false.
 * call setEnabled(true) when you want to start rendering.  If this is
 * parented to an AWT component then you should call this after
 * addNotify has been called on the component.
 *
 * @author Justin Couch
 * @version $Revision: 1.63 $
 */
public class OGLBrowserCanvas
    implements SensorStatusListener, BrowserCoreListener, Runnable {

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
     * {@link org.xj3d.core.eventmodel.SensorManager} interface.
     */
    public static final String SENSOR_MANAGER_PROP =
        "org.xj3d.sensor.manager.class";

    /**
     * Property defining the manager implementation for picking sensor
     * interactions. This should name a class that implements the
     * {@link org.xj3d.core.eventmodel.PickingManager} interface.
     */
    public static final String PICKING_MANAGER_PROP =
        "org.xj3d.sensor.manager.class";

    /**
     * Property defining the manager implementation for network
     * instances. This should name a class that implements the
     * {@link org.xj3d.core.eventmodel.NetworkManager} interface.
     */
    public static final String NETWORK_MANAGER_PROP =
        "org.xj3d.network.manager.class";

    /** Default class for the network manager */
    private static final String DEFAULT_NETWORK_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultNetworkManager";

    /**
     * Property defining the manager implementation for hanim
     * instances.
     */
    public static final String HANIM_MANAGER_PROP =
        "org.xj3d.hanim.manager.class";

    /** Default class for the hanim manager */
    private static final String DEFAULT_HANIM_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultHumanoidManager";

    /**
     * Property defining the manager implementation for physics
     * instances.
     */
    public static final String PHYSICS_MANAGER_PROP =
        "org.xj3d.physics.manager.class";

    /** Default class for the physics manager */
    private static final String DEFAULT_PHYSICS_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultRigidBodyPhysicsManager";

    /**
     * Property defining the manager implementation for particle
     * instances.
     */
    public static final String PARTICLE_MANAGER_PROP =
        "org.xj3d.particle.manager.class";

    /** Default class for the particle manager */
    private static final String DEFAULT_PARTICLE_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultParticleSystemManager";

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

    /** Default class for the picking manager */
    private static final String DEFAULT_PICKING_MANAGER =
        "org.web3d.vrml.renderer.ogl.input.DefaultPickingManager";

    /** Default class for the sensor manager */
    private static final String DEFAULT_SENSOR_MANAGER =
        "org.web3d.vrml.renderer.common.input.DefaultSensorManager";

    /** Default class for the sensor manager */
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
        "valid instance of SensorManager";

    private static final String NO_EVENT_MODEL_MSG =
        "The class that you specificed for the event model is not a " +
        "valid instance of EventModelEvaluator";

    private static final String NO_PICKING_MGR_MSG =
        "The class that you specificed for the sensor manager is not a " +
        "valid instance of OGLPickingManager";

    private static final String NO_NETWORK_MGR_MSG =
        "The class that you specificed for the sensor manager is not a " +
        "valid instance of NetworkManager";

    private static final String NO_HANIM_MGR_MSG =
        "The class that you specificed for the hanim manager is not a " +
        "valid instance of HAnimManager";

    private static final String NO_PHYSICS_MGR_MSG =
        "The class that you specificed for the physics manager is not a " +
        "valid instance of PhysicsManager";

    private static final String NO_PARTICLE_MGR_MSG =
        "The class that you specificed for the particle manager is not a " +
        "valid instance of ParticleManager";

    /** When sending a link changed event and there is an error */
    private static final String SEND_LINK_MSG =
        "There was an error sending the link changed notification";

    /** World load failed error message */
    private static final String FAILED_TO_LOAD_ERR_MSG = "Failed to load ";

    /** DISProtocol handler not found error message */
    private static final String DIS_PROTOCOL_HANDLER_NOT_FOUND_ERR_MSG =
        "DISProtocol handler not found";

    // Standard vars....

    /** The universe we are currently using */
    private OGLStandardBrowserCore universe;

    /** The script manager instance shared between all */
    private ScriptManager scriptManager;

    /** Frame state manager in use */
    private FrameStateManager stateManager;

    /** Load manager for loading external content */
    private ContentLoadManager loadManager;

    /** Script loader */
    private ScriptLoader s_loader;

    /** manager for the network handling */
    private NetworkManager networkManager;

    /** manager for the hanim handling */
    private NodeManager hanimManager;

    /** manager for the physics handling */
    private NodeManager physicsManager;

    /** manager for the particle handling */
    private NodeManager particleManager;

    /** Manager for route propogation */
    private RouteManager routeManager;

    /** manager for input and sensor handling */
    private SensorManager sensorManager;

    /** manager for picking sensors */
    private PickingManager pickingManager;

    /** Overarching event model manager */
    private EventModelEvaluator eventModel;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** Browser configuration parameters */
    private BrowserConfig configParams;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The device factory for pointing and keysensor devices
     *  associated with the drawing surface */
    private DeviceFactory deviceFactory;

    /** The scene manager for dealing with adding */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** The Aviatrix3D rendering pipeline */
    private DefaultGraphicsPipeline pipeline;

    /** The audio device */
    private AudioOutputDevice adevice;

    /** The audio rendering pipeline */
    private DefaultAudioPipeline audioPipeline;

    /** The Viewpoint manager */
    private ViewpointManager viewpointManager;

    /** Factory for generating new layer managers */
    //private LayerManagerFactory layerManagerFactory;

    /** Thread used to handle the system shutdown hook */
    private Thread shutdownThread;

    /**
     * Construct an empty canvase that contains a single view
     * that is provided by the user. This constructor would be used when
     * you want to create the initial eye of a stereo pair. If the view
     * is null then a default view is created.
     *
     * @param surface The drawing component to use
     * @param deviceFactory The factory for pointing and key sensor
     * devices associated with the surface.
     * @param configParams Browser configuration parameters
     */
    public OGLBrowserCanvas(GraphicsOutputDevice surface,
        DeviceFactory deviceFactory, BrowserConfig configParams ) {

        this.surface = surface;
        this.deviceFactory = deviceFactory;
        this.configParams = configParams;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        shutdownThread = new Thread(this, "Xj3D Canvas Shutdown");

        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    Runtime rt = Runtime.getRuntime();
                    rt.addShutdownHook(shutdownThread);
                    return(null);
                }
            }
            );
    }

    /**
     * Set a new surface to use.  NULL will clear the surface.
     *
     * @param surface - The new surface.
     * @param deviceFactory - The factory for pointing and key sensor
     * devices associated with the surface.
     */
    public void setSurface( GraphicsOutputDevice surface, DeviceFactory deviceFactory ) {
        this.surface = surface;
        this.deviceFactory = deviceFactory;

        pipeline.setGraphicsOutputDevice(surface);

        // setting the fov is redundant with the
        // caller at the Composite/JPanel level, why?
        if((surface instanceof ElumensAWTSurface)||
            (surface instanceof ElumensSWTSurface)) {
            universe.setHardwareFOV(180);
        } else {
            universe.setHardwareFOV(0);
        }

        if(sensorManager != null) {
            if( deviceFactory != null ) {
                InputDeviceManager idm = new InputDeviceManager( deviceFactory );
                KeyDeviceSensorManager kdsm = new KeyDeviceSensorManager( deviceFactory );

                sensorManager.setInputManager(idm);
                sensorManager.setKeyDeviceSensorManager(kdsm);
            }
        }

        GraphicsResizeListener[] listeners =
            ((OGLStandardBrowserCore)universe).getGraphicsResizeListeners( );

        for( int i=0; i < listeners.length; i++ ) {
            surface.addGraphicsResizeListener( listeners[i] );
        }
    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Run method for the shutdown hook. This is to deal with someone using
     * ctrl-C to kill the application. Makes sure that all the resources
     * are cleaned up properly.
     */
    public void run() {
        shutdownApp( true );
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
     * @param url_list The url to load.
     */
    public void linkActivated(String[] url_list) {

        for(int i = 0; i < url_list.length; i++) {
            if(url_list[i].charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url_list[i].substring(1);
                VRMLBindableNodeType vp =
                    (VRMLBindableNodeType)viewpointDefMap.get(def_name);

                if(vp != null) {
                    VRMLClock clk = universe.getVRMLClock();

                    vp.setBind(true, true, clk.getTime());
                } else {
                    errorReporter.warningReport(NOT_VP_MSG, null);
                }
            } else {
                System.out.println("Warning only loading the first URL");
                loadWorld(url_list[0]);
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
        //sceneManager.setEnabled(false);

        viewpointDefMap.clear();
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed( ) {
        synchronized( this ) {
            if( shutdownThread != null ) {
                AccessController.doPrivileged(
                    new PrivilegedAction( ) {
                        public Object run( ) {
                            Runtime rt = Runtime.getRuntime( );
                            rt.removeShutdownHook( shutdownThread );
                            return( null );
                        }
                    } );
                shutdownThread = null;
            }
        }
        // when explicitly calling shutdown on the sceneManager, ensure that
        // the sceneManager's shutdown thread is cleaned up.
        sceneManager.disableInternalShutdown( );
        shutdownApp( false );
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
        // do nothing for now. Need to fill in later. - JC
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
     * Make this canvas go through all its initialization process now. This
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
                    String[] ret_val = new String[13];

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
                    ret_val[10] = System.getProperty(HANIM_MANAGER_PROP,
                        DEFAULT_HANIM_MANAGER);
                    ret_val[11] = System.getProperty(PHYSICS_MANAGER_PROP,
                        DEFAULT_PHYSICS_MANAGER);
                    ret_val[12] = System.getProperty(PARTICLE_MANAGER_PROP,
                        DEFAULT_PARTICLE_MANAGER);

                    return ret_val;
                }
            }
            );

        RouterFactory r_fac = null;
        int num_node_managers = 0;

        s_loader = (ScriptLoader)loadClass(props[0], true, NO_SCRIPT_LOAD_MSG);
        scriptManager = (ScriptManager)loadClass( props[1], true, NO_SCRIPT_MGR_MSG);
        loadManager = (ContentLoadManager)loadClass(props[2], true, NO_LOADER_LOAD_MSG);
        r_fac = (RouterFactory)loadClass(props[3], true, NO_ROUTER_LOAD_MSG);
        routeManager = (RouteManager)loadClass(props[4], true, NO_ROUTER_MGR_MSG);
        stateManager = (FrameStateManager)loadClass(props[5], true, NO_STATE_MGR_MSG);
        sensorManager = (SensorManager)loadClass(props[6], true, NO_SENSOR_MGR_MSG);
        pickingManager = (PickingManager)loadClass(props[7], false, NO_PICKING_MGR_MSG);
        eventModel = (EventModelEvaluator)loadClass(props[8], true, NO_EVENT_MODEL_MSG);

        networkManager = (NetworkManager)loadClass(props[9],
            false,
            NO_NETWORK_MGR_MSG);
        if(networkManager != null)
            num_node_managers++;

        hanimManager = (NodeManager)loadClass(props[10],
            false,
            NO_HANIM_MGR_MSG);
        if(hanimManager != null)
            num_node_managers++;

        physicsManager = (NodeManager)loadClass(props[11],
            false,
            NO_PHYSICS_MGR_MSG);
        if(physicsManager != null)
            num_node_managers++;

        particleManager = (NodeManager)loadClass(props[12],
            false,
            NO_PARTICLE_MGR_MSG);
        if(particleManager != null)
            num_node_managers++;

        setupAviatrix();

        if(surface != null) {
            if(sensorManager != null) {
                if( deviceFactory != null ) {
                    InputDeviceManager idm = new InputDeviceManager( deviceFactory );
                    KeyDeviceSensorManager kdsm = new KeyDeviceSensorManager( deviceFactory );

                    sensorManager.setInputManager(idm);
                    sensorManager.setKeyDeviceSensorManager(kdsm);
                    sensorManager.setPickingManager(pickingManager);
                }
            }
        } else {
            if(sensorManager != null) {
                sensorManager.setPickingManager(pickingManager);
            }
        }

        if(networkManager != null) {
            try {
                DISProtocolHandler dis_handler = new DISProtocolHandler();
                networkManager.addProtocolHandler(dis_handler);
            } catch(NoClassDefFoundError ncdfe) {
                errorReporter.warningReport(DIS_PROTOCOL_HANDLER_NOT_FOUND_ERR_MSG, null);
            }
        }

        NodeManager[] node_managers = new NodeManager[num_node_managers];
        num_node_managers = 0;

        if(networkManager != null)
            node_managers[num_node_managers++] = networkManager;

        if(hanimManager != null)
            node_managers[num_node_managers++] = hanimManager;

        if(physicsManager != null)
            node_managers[num_node_managers++] = physicsManager;

        if(particleManager != null)
            node_managers[num_node_managers++] = particleManager;

        routeManager.setRouterFactory(r_fac);
        scriptManager.setScriptLoader(s_loader);

        if(scriptManager == null || routeManager == null ||
            sensorManager == null || stateManager == null ||
            loadManager == null) {

            throw new InvalidConfigurationException("Missing required Manager");
        }

        LayerManagerFactory layerManagerFactory = new OGLLayerManagerFactory();
        layerManagerFactory.setErrorReporter(errorReporter);

        // now create the universe
        universe = new OGLStandardBrowserCore(eventModel,
                                              sceneManager,
                                              displayManager);
        universe.addSensorStatusListener(this);
        universe.addCoreListener(this);
        universe.setErrorReporter(errorReporter);

        viewpointManager = new DefaultViewpointManager(universe);

        eventModel.initialize(scriptManager,
            routeManager,
            sensorManager,
            stateManager,
            loadManager,
            viewpointManager,
            layerManagerFactory,
            universe,
            node_managers);

        GraphicsResizeListener[] listeners = ((OGLStandardBrowserCore)universe).getGraphicsResizeListeners();

        for(int i=0; i < listeners.length; i++) {
            surface.addGraphicsResizeListener(listeners[i]);
        }

        // Now, set up the infrastructure needed to load new worlds and
        // stuff.

        viewpointDefMap = new HashMap();

        SceneBuilderFactory builder_fac = new OGLSceneBuilderFactory(
            configParams.vrml97Only,
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

        worldLoader = new DefaultWorldLoaderManager(universe,
            stateManager,
            routeManager);
        worldLoader.setErrorReporter(errorReporter);
        worldLoader.registerBuilderFactory(Xj3DConstants.OPENGL_RENDERER,
            builder_fac);
        worldLoader.registerParserFactory(Xj3DConstants.OPENGL_RENDERER,
            parser_fac);
    }

    /**
     * Control the rendering state. Allows the system to be shutdown temporarily
     * at the lowest level, effectively pausing the entire system.
     *
     * @param state true if this should be set to the running state
     */
    public void setEnabled(boolean state) {
        sceneManager.setEnabled(state);
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
        universe.setMinimumFrameInterval(millis, userSet);
    }

    /**
     * Connect or disconnect the render pipeline as requested by the argument.
     * This essentially enables or disables rendering, without stopping the
     * event model from running and is used free CPU resources while rendering
     * is not required.
     *
     * @param enable - true to connect the pipeline, false to disconnect.
     */
    public void enableRenderPipeline( boolean enable ) {
        displayManager.setEnabled( enable );
    }

    /**
     * A request to load the world given by the URL string. This string
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
            errorReporter.errorReport(FAILED_TO_LOAD_ERR_MSG, e);
            worldLoader.releaseLoader(loader);
            return;
        }

        worldLoader.releaseLoader(loader);
        String vpUrl = null;

        int refLoc = url.indexOf("#");
        if(refLoc > -1)
            vpUrl = url.substring(refLoc+1);

        universe.setScene(parsed_scene, vpUrl);

        // Finally set up the viewpoint def name list. Have to start from
        // the list of DEF names as the Viewpoint nodes don't store the DEF
        // name locally.
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
     * Fetch the layer rendering manager in use by this class
     *
     * @return The current manager instance
     */
    public LayerRenderingManager getLayerRenderingManager() {
        return universe;
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
    public OGLStandardBrowserCore getUniverse() {
        return universe;
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
            DefaultErrorReporter.getDefaultReporter() : reporter;

        eventModel.setErrorReporter(errorReporter);
        universe.setErrorReporter(errorReporter);
    }

    //----------------------------------------------------------
    // Internal convenience methods.
    //----------------------------------------------------------

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix() {

        // Assemble a simple single-threaded pipeline.
        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();

        GraphicsCullStage culler = null;
        String cullingMode = configParams.cullingMode;
        if( cullingMode.equals( "none" ) ) {
          culler = new NullCullStage();
        } else if( cullingMode.equals( "frustum" ) ) {
            culler = new FrustumCullStage();
        } else {
            culler = new FrustumCullStage();
        }
        culler.setOffscreenCheckEnabled(true);

        pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        adevice = new OpenALAudioDevice();

        AudioCullStage aculler = new NullAudioCullStage();
        AudioSortStage asorter = new NullAudioSortStage();

        audioPipeline = new DefaultAudioPipeline();
        audioPipeline.setCuller(aculler);
        audioPipeline.setSorter(asorter);
        audioPipeline.setAudioOutputDevice(adevice);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);
        displayManager.addPipeline(audioPipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
    }

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
                        if(required) {
                            Exception ewrapper = new Exception(err.getMessage(), err);
                            errorReporter.errorReport(msg, ewrapper);
                        } else
                            errorReporter.warningReport(msg, null);
                    } catch(Exception e) {
                        if(required)
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

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    private void shutdownApp( boolean isShutdownThread ) {

        loadManager.shutdown();

        if(s_loader != null)
            s_loader.shutdown();

        eventModel.shutdown();

        if( !isShutdownThread ) {
            sceneManager.shutdown( );
        }
    }
}
