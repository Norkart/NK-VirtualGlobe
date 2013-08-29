/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.construct;

// External imports
import java.lang.reflect.Constructor;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.ArrayList;

import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.FileNameMap;
import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.InvalidConfigurationException;

import org.web3d.net.content.VRMLFileNameMap;

import org.web3d.net.protocol.VRML97ResourceFactory;
import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.net.protocol.X3DResourceFactory;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.FrameStateManager;

import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.parser.VRMLParserFactory;

import org.web3d.vrml.scripting.ScriptEngine;

import org.web3d.vrml.renderer.common.input.DefaultSensorManager;

import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.xj3d.core.eventmodel.InputDeviceManager;
import org.xj3d.core.eventmodel.KeyDeviceSensorManager;
import org.xj3d.core.eventmodel.LayerManagerFactory;
import org.xj3d.core.eventmodel.LayerRenderingManager;
import org.xj3d.core.eventmodel.NetworkManager;
import org.xj3d.core.eventmodel.NodeManager;
import org.xj3d.core.eventmodel.PickingManager;
import org.xj3d.core.eventmodel.RouterFactory;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.SensorManager;
import org.xj3d.core.eventmodel.TimeSensorManager;
import org.xj3d.core.eventmodel.ViewpointManager;

import org.xj3d.core.loading.ContentLoadManager;
import org.xj3d.core.loading.SceneBuilderFactory;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.impl.core.eventmodel.DefaultViewpointManager;

import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;

// Morpheus: "This is the construct. It’s our loading program.
// We can load anything from clothing, to equipment, weapons,
// training simulations... anything we need."

/**
 * The Construct is an abstract class that provides a flexible container
 * and builder for instantiating and configuring the objects that provide
 * the infrastructure of an X3D browser.
 * <br>
 * The Construct provides access to the management and rendering objects,
 * but does not define a user interface. It is left to application specific
 * sub-classes to provide a user interface.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public abstract class Construct implements ConstructBuilder {

    /** The logging identifer of this class */
    private static final String LOG_NAME = "Construct";

    ///////////////////////////////////////////////////////////////////
    // manager classes

    /** The DIS protocol handler class */
    protected String DIS_PROTOCOL_HANDLER =
        "org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler";

    /** The network manager class */
    protected String NETWORK_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultNetworkManager";

    /** The hanim manager class */
    protected String HANIM_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultHumanoidManager";

    /** The physics manager class */
    protected String PHYSICS_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultRigidBodyPhysicsManager";

    /** The particle manager class */
    protected String PARTICLE_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultParticleSystemManager";

    /** The script loader class */
    protected String SCRIPT_LOADER =
        "org.xj3d.impl.core.loading.DefaultScriptLoader";

    /** The script manager class */
    protected String SCRIPT_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultScriptManager";

    /** The external file loader class */
    protected String CONTENT_LOADER =
        "org.xj3d.impl.core.loading.MemCacheLoadManager";

    /** The router factory class */
    protected String ROUTER_FACTORY =
        "org.xj3d.impl.core.eventmodel.ListsRouterFactory";

    /** The route manager class */
    protected String ROUTER_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultRouteManager";

    /** The frame state manager class */
    protected String STATE_MANAGER =
        "org.xj3d.impl.core.eventmodel.DefaultFrameStateManager";

    /** The picking manager class */
    protected String PICKING_MANAGER =
        "org.web3d.vrml.renderer.ogl.input.DefaultPickingManager";

    /** The sensor manager class */
    protected String SENSOR_MANAGER =
        "org.web3d.vrml.renderer.common.input.DefaultSensorManager";

    /** The event model manager class */
    protected String EVENT_MODEL =
        "org.xj3d.impl.core.eventmodel.DefaultEventModelEvaluator";

    /** The time sensor manager class */
    protected String TIME_SENSOR_MANAGER =
        "org.xj3d.impl.core.eventmodel.RealTimeSensorManager";

    ///////////////////////////////////////////////////////////////////
    // scripting engine classes

    /** The VRML97 Java scripting engine class */
    protected String VRML_JAVA_SCRIPT_ENGINE =
        "org.web3d.vrml.scripting.jsai.VRML97ScriptEngine";

    /** The VRML97 Javascript scripting engine class */
    protected String VRML_JAVASCRIPT_SCRIPT_ENGINE =
        "org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine";

    /** The X3D Java scripting engine class */
    protected String X3D_JAVA_SCRIPT_ENGINE =
        "org.web3d.vrml.scripting.sai.JavaSAIScriptEngine";

    /** The X3D Javascript scripting engine class */
    protected String X3D_ECMASCRIPT_SCRIPT_ENGINE =
        "org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine";

    ///////////////////////////////////////////////////////////////////
    // Renderer specific classes, must be set by sub-class

    /** The layer manager factory class */
    protected String LAYER_MANAGER_FACTORY;

    ///////////////////////////////////////////////////////////////////
    // Toolkit specific classes, must be set by sub-class

    /** Toolkit specific content handler factory class */
    protected String CONTENT_HANDLER_FACTORY;

    ///////////////////////////////////////////////////////////////////
    // renderer definition, must be set by renderer specific sub-class

    /** Constant identifying the renderer */
    protected int renderer;

    /** String identifying the renderer */
    protected String renderer_id;

    ///////////////////////////////////////////////////////////////////
    // spec support preferences

    /** Flag indicating that the browser instance should support VRML97.
     *  Default value is true. */
    protected boolean supportVRML;

    /** Flag indicating that the browser instance should support X3D.
     *  Default value is true. */
    protected boolean supportX3D;

    ///////////////////////////////////////////////////////////////////
    // generic rendering preferences

    /** Flag indicating that the renderer component should be lightweight.
     *  Default value is false. */
    protected boolean lightweightRenderer;

    /** Flag indicating that MipMaps should be generated.
     *  Default value is true. */
    protected boolean useMipMaps;

    /** Flag indicating that double buffering should be used.
     *  Default value is true. */
    protected boolean doubleBuffered;

    /** Flag indicating that hardware acceleration should be used.
     *  Default value is true. */
    protected boolean hardwareAccelerated;

    /** Value indicating the anisotropic degree setting to be configured.
     *  Default value is 0, which disables anisotropic filtering. */
    protected int anisotropicDegree;

    /** Value indicating the antialias samples setting to be configured.
     *  Default value is 0, which disables antialiasing. */
    protected int antialiasSamples;

    ///////////////////////////////////////////////////////////////////
    // user interface device objects

    /** The device factory for pointing and keysensor devices
    *  associated with the rendering surface */
    protected DeviceFactory deviceFactory;

    ///////////////////////////////////////////////////////////////////
    // manager (xj3d) objects

    /** The browser core */
    protected BrowserCore core;

    /** The script manager */
    protected ScriptManager scriptManager;

    /** The script loader */
    protected ScriptLoader scriptLoader;

    /** The frame state manager */
    protected FrameStateManager stateManager;

    /** The manager for loading external content */
    protected ContentLoadManager loadManager;

    /** The manager for network handling */
    protected NetworkManager networkManager;

    /** The manager for H-Anim handling */
    protected NodeManager hanimManager;

    /** The manager for physics handling */
    protected NodeManager physicsManager;

    /** The manager for particle handling */
    protected NodeManager particleManager;

    /** The manager for route propogation */
    protected RouteManager routeManager;

    /** The manager for input and sensor handling */
    protected SensorManager sensorManager;

    /** The manager for time sensor handling */
    protected TimeSensorManager timeSensorManager;

    /** The manager for picking sensors */
    protected PickingManager pickingManager;

    /** THE event model manager */
    protected EventModelEvaluator eventModel;

    /** The manager of the world loaders */
    protected WorldLoaderManager worldLoader;

    /** The viewpoint manager */
    protected ViewpointManager viewpointManager;

    /** The scene builder factory */
    protected SceneBuilderFactory sceneBuilderFactory;

    ///////////////////////////////////////////////////////////////////

    /** The error reporting mechanism */
    protected ErrorReporter errorReporter;

    /** Utilites for loading classes */
    protected ClassLoadUtilities loader;

    /**
     * Restricted Constructor
     */
    protected Construct( ) {
        this( null );
    }

    /**
     * Restricted Constructor
     *
     * @param reporter The error reporter
     */
    protected Construct( ErrorReporter reporter ) {
        errorReporter =
            ( reporter == null ) ? new DefaultErrorReporter( ) : reporter;
        loader = new ClassLoadUtilities( errorReporter );

        supportVRML = true;
        supportX3D = true;
        lightweightRenderer = false;
        useMipMaps = true;
        doubleBuffered = true;
        hardwareAccelerated = true;
    }

    /**
     * Return the rendering surface
     *
     * @return The rendering surface
     */
    public abstract Object getGraphicsObject( );

    //----------------------------------------------------------
    // Methods defined by ConstructBuilder
    //----------------------------------------------------------

    /**
     * Create a 'default' configuration instance.
     */
    public void buildAll( ) {
        buildRenderingCapabilities( );
        buildRenderingDevices( );
        buildInterfaceDevices( );
        buildRenderer( );
        buildManagers( );
        buildScriptEngines( );
        buildNetworkCapabilities( );
    }

    /**
     * Create the rendering capabilities
     */
    public void buildRenderingCapabilities( ) {

        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Object>( ) {
                    public Object run( ) {

                        if ( useMipMaps ) {
                            System.setProperty(
                                "org.web3d.vrml.renderer.common.nodes.shape.useMipMaps",
                                "true" );
                        }
                        if ( anisotropicDegree > 1 ) {
                            System.setProperty(
                                "org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree",
                                new Integer( anisotropicDegree ).toString( ) );
                        }

                        return( null );
                    }
                } );
        } catch ( PrivilegedActionException pae ) {
            errorReporter.warningReport(
                LOG_NAME +": Exception setting System properties", pae );
        }
    }

    /**
     * Create the rendering devices
     */
    public void buildRenderingDevices( ) {

        buildGraphicsRenderingDevice( );
        buildAudioRenderingDevice( );
    }

    /**
     * Create the graphics rendering device
     */
    protected abstract void buildGraphicsRenderingDevice( );

    /**
     * Create the audio rendering device
     */
    protected abstract void buildAudioRenderingDevice( );

    /**
     * Create the x3d managers
     *
     * @exception InvalidConfigurationException If a required class
     * class cannot be loaded.
     */
    public void buildManagers( ) {

        ////////////////////////////////////////////////////////////////////////
        // load the required managers
        scriptManager =
            (ScriptManager)loader.loadClass( SCRIPT_MANAGER, true );

        loadManager =
            (ContentLoadManager)loader.loadClass( CONTENT_LOADER, true );

        routeManager =
            (RouteManager)loader.loadClass( ROUTER_MANAGER, true );

        stateManager =
            (FrameStateManager)loader.loadClass( STATE_MANAGER, true );

        timeSensorManager =
            (TimeSensorManager)loader.loadClass( TIME_SENSOR_MANAGER, true );

        eventModel =
            (EventModelEvaluator)loader.loadClass( EVENT_MODEL, true );

        ////////////////////////////////////////////////////////////////////////
        // load the optional managers

        scriptLoader =
            (ScriptLoader)loader.loadClass( SCRIPT_LOADER, false );

        RouterFactory routerFactory =
            (RouterFactory)loader.loadClass( ROUTER_FACTORY, false );

        pickingManager =
            (PickingManager)loader.loadClass( PICKING_MANAGER, false );

        int num_node_managers = 0;

        networkManager =
            (NetworkManager)loader.loadClass( NETWORK_MANAGER, false );
        if ( networkManager != null) {
            num_node_managers++;
        }

        hanimManager =
            (NodeManager)loader.loadClass( HANIM_MANAGER, false );
        if ( hanimManager != null ) {
            num_node_managers++;
        }

        physicsManager =
            (NodeManager)loader.loadClass( PHYSICS_MANAGER, false );
        if ( physicsManager != null ) {
            num_node_managers++;
        }

        particleManager =
            (NodeManager)loader.loadClass( PARTICLE_MANAGER, false );
        if ( particleManager != null ) {
            num_node_managers++;
        }

        NodeManager[] node_managers = new NodeManager[num_node_managers];
        num_node_managers = 0;

        if ( networkManager != null ) {
            node_managers[num_node_managers++] = networkManager;
        }

        if ( hanimManager != null ) {
            node_managers[num_node_managers++] = hanimManager;
        }

        if ( physicsManager != null ) {
            node_managers[num_node_managers++] = physicsManager;
        }

        if ( particleManager != null ) {
            node_managers[num_node_managers++] = particleManager;
        }

        DISProtocolHandler dis_handler =
            (DISProtocolHandler)loader.loadClass( DIS_PROTOCOL_HANDLER, false );

        ////////////////////////////////////////////////////////////////////////
        // configure, cross pollenate and instantiate 'other' managers

        // sensorManager is required
        sensorManager = new DefaultSensorManager( timeSensorManager );

        sensorManager.setErrorReporter( errorReporter );
        sensorManager.setPickingManager( pickingManager );

        InputDeviceManager idm =
            new InputDeviceManager( deviceFactory );

        KeyDeviceSensorManager kdsm =
            new KeyDeviceSensorManager( deviceFactory );

        sensorManager.setInputManager( idm );
        sensorManager.setKeyDeviceSensorManager( kdsm );

        routeManager.setRouterFactory( routerFactory );

        scriptManager.setScriptLoader( scriptLoader );

        if ( dis_handler != null ) {
            networkManager.addProtocolHandler( dis_handler );
        }

        buildBrowserCore( );
        core.setErrorReporter( errorReporter );

        viewpointManager =
            new DefaultViewpointManager( core );

        LayerManagerFactory layerManagerFactory =
            (LayerManagerFactory)loader.loadClass( LAYER_MANAGER_FACTORY, true );

        eventModel.initialize(
            scriptManager,
            routeManager,
            sensorManager,
            stateManager,
            loadManager,
            viewpointManager,
            layerManagerFactory,
            (LayerRenderingManager)core,
            node_managers );

        // eventModel sets the error reporter on the arguments to
        // initialize() - with the exception of the browser core
        // cannot set error reporter to the eventModel till after
        // initialize has been called - or NPE's occur.
        eventModel.setErrorReporter( errorReporter );

        ////////////////////////////////////////////////////////////////////////
        // Infrastructure needed to load worlds

        buildSceneBuilderFactory( );

        VRMLParserFactory parser_fac = null;

        try {
            parser_fac = VRMLParserFactory.newVRMLParserFactory( );
        } catch( FactoryConfigurationError fce ) {
            throw new RuntimeException(
                LOG_NAME +": Failed to load Parser Factory" );
        }

        worldLoader = new DefaultWorldLoaderManager(
            core,
            stateManager,
            routeManager );

        worldLoader.setErrorReporter( errorReporter );

        worldLoader.registerBuilderFactory(
            renderer,
            sceneBuilderFactory );

        worldLoader.registerParserFactory(
            renderer,
            parser_fac );
    }

    /**
     * Create the browser core
     */
    protected abstract void buildBrowserCore( );

    /**
     * Create the scene builder factory
     */
    protected abstract void buildSceneBuilderFactory( );

    /**
     * Create the x3d scripting engines
     *
     * @exception InvalidConfigurationException If a required class
     * class cannot be loaded.
     */
    public void buildScriptEngines( ) {

        if ( scriptLoader != null ) {

            ArrayList <String>engineList = new ArrayList<String>( );
            if ( supportVRML ) {
                engineList.add( VRML_JAVA_SCRIPT_ENGINE );
                engineList.add( VRML_JAVASCRIPT_SCRIPT_ENGINE );
            }
            if ( supportX3D ) {
                engineList.add( X3D_JAVA_SCRIPT_ENGINE );
                engineList.add( X3D_ECMASCRIPT_SCRIPT_ENGINE );
            }
            int num_engines = engineList.size( );
            if ( num_engines > 0 ) {
                String[] engine = new String[num_engines];
                engine = engineList.toArray( engine );
                ScriptEngine scriptEngine = null;
                for ( int i = 0; i < num_engines; i++ ) {

                    scriptEngine = loader.loadScriptEngine(
                        engine[i],
                        core,
                        viewpointManager,
                        routeManager,
                        stateManager,
                        worldLoader,
                        true );

                    scriptEngine.setErrorReporter( errorReporter );
                    scriptLoader.registerScriptingEngine( scriptEngine );
                }
            }
        } else {
            errorReporter.warningReport(
                LOG_NAME +": Cannot load ScriptEngines: ScriptLoader is not initialized", null );
        }
    }

    /**
     * Set up the networking properties and objects needed to run the browser.
     *
     * @exception InvalidConfigurationException If a required class
     * class cannot be loaded.
     */
    public void buildNetworkCapabilities( ) {
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Object>( ) {
                    public Object run( ) {
                        String prop = System.getProperty( "uri.content.handler.pkgs", "" );
                        if ( prop.indexOf( "vlc.net.content" ) == -1 ) {
                            System.setProperty( "uri.content.handler.pkgs",
                                "vlc.net.content" );
                        }

                        prop = System.getProperty( "uri.protocol.handler.pkgs", "" );
                        if ( prop.indexOf( "vlc.net.protocol" ) == -1 ) {
                            System.setProperty( "uri.protocol.handler.pkgs",
                                "vlc.net.protocol");
                        }

                        URIResourceStreamFactory res_fac =
                            URI.getURIResourceStreamFactory( );
                        if ( supportX3D && supportVRML ) {
                            if ( !( res_fac instanceof Web3DResourceFactory ) ) {
                                res_fac = new Web3DResourceFactory( res_fac );
                                URI.setURIResourceStreamFactory( res_fac );
                            }
                        } else if ( supportX3D ) {
                            if ( !( res_fac instanceof X3DResourceFactory ) ) {
                                res_fac = new X3DResourceFactory( res_fac );
                                URI.setURIResourceStreamFactory( res_fac );
                            }
                        } else if ( supportVRML ) {
                            if ( !( res_fac instanceof VRML97ResourceFactory ) ) {
                                res_fac = new VRML97ResourceFactory( res_fac );
                                URI.setURIResourceStreamFactory( res_fac );
                            }
                        }

                        Class factoryClass = null;
                        try {
                            factoryClass = Class.forName( CONTENT_HANDLER_FACTORY );
                        } catch ( ClassNotFoundException cnfe ) {
                            throw new InvalidConfigurationException(
                                LOG_NAME +": Missing required Factory:" +
                                CONTENT_HANDLER_FACTORY );
                        }
                        ContentHandlerFactory c_fac = URI.getContentHandlerFactory( );
                        if ( !factoryClass.isInstance( c_fac ) ) {

                            c_fac = (ContentHandlerFactory)loader.loadClass(
                                CONTENT_HANDLER_FACTORY,
                                new Object[] {
                                    core,
                                    worldLoader,
                                    c_fac },
                                new Class[] {
                                    BrowserCore.class,
                                    WorldLoaderManager.class,
                                    ContentHandlerFactory.class },
                                true );

                            URI.setContentHandlerFactory( c_fac );
                        }

                        FileNameMap fn_map = URI.getFileNameMap( );
                        if ( !( fn_map instanceof VRMLFileNameMap ) ) {
                            fn_map = new VRMLFileNameMap( fn_map );
                            URI.setFileNameMap( fn_map );
                        }

                        return( null );
                    }
                } );
        } catch ( PrivilegedActionException pae ) {
            errorReporter.warningReport(
                LOG_NAME +": Exception setting System properties", pae );
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    ///////////////////////////////////////////////////////////////////
    // convenience accessor methods for commonly needed class references

    /**
     * Return the error reporter.
     *
     * @return The error reporter.
     */
    public ErrorReporter getErrorReporter( ) {
        return( errorReporter );
    }

    /**
     * Return the script manager.
     *
     * @return The script manager.
     */
    public ScriptManager getScriptManager( ) {
        return( scriptManager );
    }

    /**
     * Return the script loader.
     *
     * @return The script loader.
     */
    public ScriptLoader getScriptLoader( ) {
        return( scriptLoader );
    }

    /**
     * Return the viewpoint manager.
     *
     * @return The viewpoint manager.
     */
    public ViewpointManager getViewpointManager() {
        return viewpointManager;
    }

    /**
     * Return the content load manager.
     *
     * @return The content load manager.
     */
    public ContentLoadManager getContentLoadManager( ) {
        return( loadManager );
    }

    /**
     * Return the route manager.
     *
     * @return The route manager.
     */
    public RouteManager getRouteManager( ) {
        return( routeManager );
    }

    /**
     * Return the sensor manager.
     *
     * @return The sensor manager.
     */
    public SensorManager getSensorManager( ) {
        return( sensorManager );
    }

    /**
     * Return the time sensor manager.
     *
     * @return The time sensor manager.
     */
    public TimeSensorManager getTimeSensorManager( ) {
        return( timeSensorManager );
    }

    /**
     * Return the world loader manager.
     *
     * @return The world loader manager.
     */
    public WorldLoaderManager getWorldLoaderManager( ) {
        return( worldLoader );
    }

    /**
     * Return the frame state manager.
     *
     * @return The frame state manager.
     */
    public FrameStateManager getFrameStateManager( ) {
        return( stateManager );
    }

    /**
     * Return the event model evaluator.
     *
     * @return The event model evaluator.
     */
    public EventModelEvaluator getEventModelEvaluator( ) {
        return( eventModel );
    }

    /**
     * Return the browser core.
     *
     * @return the browser core.
     */
    public BrowserCore getBrowserCore( ) {
        return( core );
    }
    ///////////////////////////////////////////////////////////////////
}
