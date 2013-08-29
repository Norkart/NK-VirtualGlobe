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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import gl4java.drawable.GLDrawable;
import gl4java.drawable.GLDrawableFactory;

// Local imports
import org.web3d.vrml.nodes.*;
import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.NavigationStateListener;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;

import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.LinkSelectionListener;
import org.web3d.vrml.renderer.mobile.MobileSceneBuilderFactory;
import org.web3d.vrml.renderer.mobile.input.MobileSensorManager;
import org.web3d.vrml.renderer.mobile.sg.SGManager;
import org.web3d.vrml.sav.InputSource;

import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;

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
 *     {@link org.web3d.vrml.renderer.mobile.input.MobileSensorManager} interface, which
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
 * @version $Revision: 1.11 $
 */
public class MobileBrowserCanvas
    implements LinkSelectionListener, BrowserCoreListener {

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
     * {@link org.web3d.vrml.renderer.mobile.input.MobileSensorManager} interface.
     */
    public static final String SENSOR_MANAGER_PROP =
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
        "org.xj3d.core.eventmodel.DefaultScriptManager";

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
        "org.web3d.vrml.renderer.mobile.input.DefaultSensorManager";

    /** Default class for the event model evaluation*/
    private static final String DEFAULT_EVENT_MODEL =
        "org.xj3d.core.eventmodel.DefaultEventModelEvaluator";

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
        "valid instance of MobileSensorManager";

    private static final String NO_EVENT_MODEL_MSG =
        "The class that you specificed for the event model is not a " +
        "valid instance of EventModelEvaluator";

    /** When sending a link changed event and there is an error */
    private static final String SEND_LINK_MSG =
        "There was an error sending the link changed notification";

    // Standard vars....

    /** The current viewpoint model */
    private VRMLViewpointNodeType currentViewpoint;

    /** The universe we are currently using */
    private MobileUniverse universe;

    /** The script manager instance shared between all */
    private ScriptManager scriptManager;

    /** Frame state manager in use */
    private FrameStateManager stateManager;

    /** Load manager for loading external content */
    private ContentLoadManager loadManager;

    /** Manager for route propogation */
    private RouteManager routeManager;

    /** manager for input and sensor handling */
    private MobileSensorManager sensorManager;

    /** Overarching event model manager */
    private EventModelEvaluator eventModel;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** Listener for link changed events */
    private LinkSelectionListener linkSelectionListener;

    /** Flag to say this is vrml97 only */
    private boolean vrml97Only;

    /** The GL drawable canvas */
    private GLDrawable canvas;

    /** The scene manager for dealing with adding */
    private SGManager sceneManager;

    /**
     * Construct an empty canvase that contains a single view
     * that is provided by the user..This constructor would be used when
     * you want to create the initial eye of a stereo pair. If the view
     * is null then then a default view is created.
     *
     * @param vrml97Only true if this is to be restricted to VRML97 only
     * @param cfg The graphics configuration to use
     * @param view The view information to use for this class
     */
    public MobileBrowserCanvas(GLDrawable canvas, boolean vrml97Only) {
        this.vrml97Only = vrml97Only;
        this.canvas = canvas;

        sceneManager = new SGManager(canvas);

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods required by the LinkSelectionListener interface.
    //----------------------------------------------------------

    /**
     * Invoked when a link node has been activated. This is the node that has
     * been selected.
     *
     * @param node The selected node
     */
    public void linkSelected(VRMLLinkNodeType node) {

        String[] url_list = node.getUrl();

        for(int i = 0; i < url_list.length; i++) {
            if(url_list[i].charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url_list[i].substring(1);
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
                loadWorld(url_list[0]);
            }
        }

        if(linkSelectionListener != null) {
            try {
                linkSelectionListener.linkSelected(node);
            } catch(Exception e) {
                errorReporter.errorReport(SEND_LINK_MSG, e);
            }
        }
    }

    /**
     * Invoked when a link node is contact with a tracker capable of picking.
     */
    public void linkSelectable(VRMLLinkNodeType node) {
    }

    /**
     * Invoked when a link node is contact with a tracker capable of picking.
     */
    public void linkNonSelectable(VRMLLinkNodeType node) {
    }

    //----------------------------------------------------------
    // Methods required by the BrowserCoreListener interface.
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
                    String[] ret_val = new String[8];

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
                    ret_val[7] = System.getProperty(EVENT_MODEL_PROP,
                                                    DEFAULT_EVENT_MODEL);
                    return ret_val;
                }
            }
        );

        ScriptLoader s_loader = null;
        RouterFactory r_fac = null;

        // Now try to load the classes. May need privilege here too....
        try {
            s_loader = (ScriptLoader)loadClass(props[0]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_SCRIPT_LOAD_MSG, cce);
        }

        // Now try to load the classes. May need privilege here too....
        try {
            scriptManager = (ScriptManager)loadClass(props[1]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_SCRIPT_MGR_MSG, cce);
        }

        try {
            loadManager = (ContentLoadManager)loadClass(props[2]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_LOADER_LOAD_MSG, cce);
        }

        try {
            r_fac = (RouterFactory)loadClass(props[3]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_ROUTER_LOAD_MSG, cce);
        }

        try {
            routeManager = (RouteManager)loadClass(props[4]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_ROUTER_MGR_MSG, cce);
        }

        try {
            stateManager = (FrameStateManager)loadClass(props[5]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_STATE_MGR_MSG, cce);
        }

        try {
            sensorManager = (MobileSensorManager)loadClass(props[6]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_SENSOR_MGR_MSG, cce);
        }

        try {
            eventModel = (EventModelEvaluator)loadClass(props[7]);
        } catch(ClassCastException cce) {
            errorReporter.warningReport(NO_EVENT_MODEL_MSG, cce);
        }

        AWTListenerEventBuffer i_buf = new AWTListenerEventBuffer();
        canvas.addMouseListener(i_buf);
        canvas.addMouseMotionListener(i_buf);
        canvas.addKeyListener(i_buf);

// TODO: Change over
//        sensorManager.setInputBuffer(i_buf);

        if((r_fac != null) && (routeManager != null))
            routeManager.setRouterFactory(r_fac);

        if((s_loader != null) && (scriptManager != null))
            scriptManager.setScriptLoader(s_loader);

        eventModel.initialize(scriptManager,
                              routeManager,
                              sensorManager,
                              stateManager,
                              loadManager);

        // now create the universe
        universe = new MobileUniverse(eventModel, sceneManager);
        universe.setLinkSelectionListener(this);
        universe.addCoreListener(this);

        // Now, set up the infrastructure needed to load new worlds and
        // stuff.

        viewpointDefMap = new HashMap();

        SceneBuilderFactory builder_fac =
            new MobileSceneBuilderFactory(vrml97Only,
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
    public MobileUniverse getUniverse() {
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
                           new DefaultErrorReporter() :
                           reporter;
    }

    /**
     * Set the current link selection listener to receive the load requests. A
     * null value will remove the listener.
     *
     * @param l The listener to use
     */
    public void setLinkSelectionListener(LinkSelectionListener l) {
        linkSelectionListener = l;
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
     */
    private Object loadClass(final String classname) {
        Object ret_val = AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    try {
                        Class cls = Class.forName(classname);
                        return cls.newInstance();
                    } catch(Exception e) {
                        System.out.println("Error loading class " + e);
                        e.printStackTrace();
                    }

                    return null;
                }
            }
        );

        return ret_val;
    }
}
