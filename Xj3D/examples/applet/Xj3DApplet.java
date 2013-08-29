/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External Imports

import javax.swing.*;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.management.*;
import org.j3d.aviatrix3d.pipeline.*;
import org.j3d.aviatrix3d.pipeline.audio.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.media.opengl.GLCapabilities;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.output.audio.OpenALAudioDevice;

import org.j3d.aviatrix3d.management.SingleThreadRenderManager;

// Local imports
import org.xj3d.core.loading.*;
import org.xj3d.core.eventmodel.*;
import org.xj3d.impl.core.eventmodel.*;
import org.xj3d.impl.core.loading.*;

import org.xj3d.ui.awt.widgets.*;

import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.net.resolve.Web3DURNResolver;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.Xj3DConstants;

import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.DefaultSensorManager;
import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLLayerManagerFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.PerFrameManager;
import org.web3d.vrml.renderer.ogl.input.DefaultPickingManager;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.web3d.util.ErrorReporter;


import org.xj3d.ui.awt.device.AWTDeviceFactory;

import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;

/**
 * Run Xj3D in an applet form that can be embedded on a web page
 *
 */
public class Xj3DApplet extends Applet
    implements SensorStatusListener,
               BrowserCoreListener {

    /** Area to push error messages to */
    private ErrorReporter console;

    /** Created by the derived class */
    private OGLStandardBrowserCore universe;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The script loader */
    private ScriptLoader scriptLoader;

    /** The load manager */
    private AbstractLoadManager loadManager;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap<String, VRMLViewpointNodeType> viewpointDefMap;

    /** The URL of the model to load */
    private String modelURL;

    /**
     * Create a new viewer instance
     *
     * @param true viewOnly true if this is to view and not allow editing
     *    of the model
     */
    public Xj3DApplet() {
        setLayout(new BorderLayout());
        viewpointDefMap = new HashMap<String, VRMLViewpointNodeType>();

    }

    //----------------------------------------------------------
    // Methods defined by Applet
    //----------------------------------------------------------

    /**
     * Initialise the applet for the first time.
     */
    public void init() {

        modelURL = getParameter("modelURL");

        console = new SwingConsoleWindow();
        console.messageReport("Initializing Browser");

        setupUI();
    }

    /**
     * Start the applet again. Use this to restart the 3D rendering, and if the
     * first model load, then bring the model into view.
     */
    public void start() {
        sceneManager.setEnabled(true);

        try {
            URL url = new URL(modelURL);
            load(new InputSource(url));

        } catch(MalformedURLException mue) {
            showStatus("Invalid URL");
            console.warningReport("Invalid URL: " + modelURL, mue);
        }
    }

    /**
     * Applet is being hidden, so stop the rendering process now.
     */
    public void stop() {
        sceneManager.setEnabled(false);
    }

    //----------------------------------------------------------
    // Methods defined by LinkSelectionListener
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
     * @param url The url to load.
     */
    public void linkActivated(String[] url_list) {
        boolean success = false;

        for(int i = 0; i < url_list.length; i++) {
            if(url_list[i].charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url_list[i].substring(1);
                VRMLViewpointNodeType vp = viewpointDefMap.get(def_name);

                if(vp != null) {
                    universe.changeViewpoint(vp);
                } else {
                    showStatus("Unknown Viewpoint " + def_name);
                    console.warningReport("Unknown Viewpoint " + def_name, null);
                }
            } else {
                // load the world or see if this is something that we should
                // really hand off to the external browser that contains us.
                try {
                    URL url = new URL(url_list[i]);
                    InputSource is = new InputSource(url);
                    if(success = load(is))
                        break;

                } catch(MalformedURLException mue) {
                    showStatus("Invalid URL");
                    console.warningReport("Invalid URL: " + url_list[i], mue);
                }
            }
        }

        if(!success)
            console.errorReport("No valid URLs were found", null);
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //----------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
        sceneManager.setEnabled(true);
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
        sceneManager.setEnabled(false);

        if (console instanceof Window) {
            Window w = (Window)console;

            if(w.isVisible())
                w.toFront();
        }
    }

    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    public void browserShutdown() {
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupUI() {

        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();

        surface = new SimpleAWTSurface(caps);

        AudioOutputDevice adevice = new OpenALAudioDevice();

        AudioCullStage aculler = new NullAudioCullStage();
        AudioSortStage asorter = new NullAudioSortStage();

        DefaultAudioPipeline audio_pipe = new DefaultAudioPipeline();
        audio_pipe.setCuller(aculler);
        audio_pipe.setSorter(asorter);
        audio_pipe.setAudioOutputDevice(adevice);

        DefaultGraphicsPipeline graphics_pipe = new DefaultGraphicsPipeline();
        graphics_pipe.setCuller(culler);
        graphics_pipe.setSorter(sorter);
        graphics_pipe.setGraphicsOutputDevice(surface);

        SingleDisplayCollection display_mgr = new SingleDisplayCollection();
        display_mgr.addPipeline(graphics_pipe);
        display_mgr.addPipeline(audio_pipe);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.disableInternalShutdown();
        sceneManager.addDisplay(display_mgr);
        //sceneManager.setMinimumFrameInterval(20);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Canvas canvas = (Canvas)surface.getSurfaceObject();

        add(canvas, BorderLayout.CENTER);

        OGLSceneBuilderFactory builder_fac =
            new OGLSceneBuilderFactory(false,
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

//        ContentLoadManager lm = new SimpleLoadManager();
        loadManager = new MemCacheLoadManager();
        scriptLoader = new DefaultScriptLoader();

        ScriptManager script_manager = new DefaultScriptManager();
        script_manager.setScriptLoader(scriptLoader);

        FrameStateManager state_manager = new DefaultFrameStateManager();

        PickingManager picker_manager = new DefaultPickingManager();
        picker_manager.setErrorReporter(console);

        SensorManager sensor_manager = new DefaultSensorManager();
        sensor_manager.setPickingManager(picker_manager);

        RouteManager route_manager = new DefaultRouteManager();
//        route_manager.setRouterFactory(new SimpleRouterFactory());
        route_manager.setRouterFactory(new ListsRouterFactory());

        eventModel = new DefaultEventModelEvaluator();
        universe = new OGLStandardBrowserCore(eventModel, sceneManager, display_mgr);
        universe.addSensorStatusListener(this);
        universe.setErrorReporter(console);

        worldLoader = new DefaultWorldLoaderManager(universe,
                                                    state_manager,
                                                    route_manager);
        worldLoader.setErrorReporter(console);
        worldLoader.registerBuilderFactory(Xj3DConstants.OPENGL_RENDERER,
                                           builder_fac);
        worldLoader.registerParserFactory(Xj3DConstants.OPENGL_RENDERER,
                                          parser_fac);

        OGLLayerManagerFactory lmf = new OGLLayerManagerFactory();
        lmf.setErrorReporter(console);

        ViewpointManager vp_manager = new DefaultViewpointManager(universe);

        eventModel.initialize(script_manager,
                              route_manager,
                              sensor_manager,
                              state_manager,
                              loadManager,
                              vp_manager,
                              lmf,
                              (LayerRenderingManager)universe,
                              null);
        eventModel.setErrorReporter(console);

        GraphicsResizeListener[] listeners =
            ((OGLStandardBrowserCore)universe).getGraphicsResizeListeners();

        for(int i=0; i < listeners.length; i++) {
            surface.addGraphicsResizeListener(listeners[i]);
        }

        DeviceFactory device_factory = new AWTDeviceFactory(
            canvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console );

        InputDeviceManager idm = new InputDeviceManager(device_factory);
        KeyDeviceSensorManager kdsm =
            new KeyDeviceSensorManager(device_factory);

        sensor_manager.setInputManager(idm);
        sensor_manager.setKeyDeviceSensorManager(kdsm);

        ScriptEngine java_sai = new JavaSAIScriptEngine(universe,
                                                        vp_manager,
                                                        route_manager,
                                                        state_manager,
                                                        worldLoader);
        java_sai.setErrorReporter(console);

        ScriptEngine ecma_sai = new ECMAScriptEngine(universe,
                                                     vp_manager,
                                                     route_manager,
                                                     state_manager,
                                                     worldLoader);
        ecma_sai.setErrorReporter(console);

        scriptLoader.registerScriptingEngine(java_sai);
        scriptLoader.registerScriptingEngine(ecma_sai);

        setupProperties(universe, worldLoader);


        org.xj3d.ui.awt.widgets.CursorManager cm =
            new org.xj3d.ui.awt.widgets.CursorManager(canvas, null, console);
        universe.addSensorStatusListener(cm);
        universe.addNavigationStateListener(cm);

        universe.addCoreListener(this);

        SwingNavigationToolbar nav_tb =
            new SwingNavigationToolbar(universe,
                                       null,
                                       console);

        SwingViewpointToolbar viewpoint_toolbar =
            new SwingViewpointToolbar(universe,
                                      vp_manager,
                                      null,
                                      console);

        JPanel p2 = new JPanel(new BorderLayout());

        p2.add(nav_tb, BorderLayout.WEST);
        p2.add(viewpoint_toolbar, BorderLayout.CENTER);

        add(p2, BorderLayout.SOUTH);

        SwingConsoleButton console_button =
            new SwingConsoleButton((SwingConsoleWindow)console, null);
        p2.add(console_button, BorderLayout.EAST);

        FramerateThrottle throttle = new FramerateThrottle(universe, console);
        throttle.setScriptLoader(scriptLoader);
        throttle.setLoadManager(loadManager);
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return true if the world loaded correctly
     */
    private boolean load(InputSource is) {
        boolean ret_val = false;

        WorldLoader loader = worldLoader.fetchLoader();

        VRMLScene parsed_scene = null;

        try {
            parsed_scene = loader.loadNow(universe, is);
        } catch(Exception e) {
            console.errorReport("Failed to load ", e);
            worldLoader.releaseLoader(loader);
            return false;
        }

        worldLoader.releaseLoader(loader);

        universe.setScene(parsed_scene, null);

        ret_val = true;

        // Grab the list of viewpoints and place them into the toolbar.
        ArrayList vp_list =
            parsed_scene.getByPrimaryType(TypeConstants.ViewpointNodeType);

        if(vp_list.size() == 0)
            return ret_val;

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
                viewpointDefMap.put(key, (VRMLViewpointNodeType)vp);
        }

        showStatus("World Loaded Successfully");

        return ret_val;
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The core representation of the browser
     * @param loader Loader manager for doing async calls
     */
    protected void setupProperties(BrowserCore core, WorldLoaderManager loader) {
        // Disable font cache to fix getBounds nullPointer bug
        System.setProperty("sun.awt.font.advancecache","off");


        System.setProperty("uri.content.handler.pkgs",
                           "vlc.net.content");

        System.setProperty("uri.protocol.handler.pkgs",
                           "vlc.net.protocol");

        System.setProperty("java.content.handler.pkgs",
                           "vlc.content");

/*
JC: Re-enable when we work out how to do our own native library loading.
    as part of JNLPAppletLauncher.

        if (useImageLoader) {
            // Image loader
            System.setProperty("java.content.handler.pkgs",
                               "vlc.net.content");

            // Test if image loader loaded successfully
            try {
                Class cls = Class.forName("vlc.net.content.image.jpeg");
                Object fac = cls.newInstance();
            } catch(Exception e) {
                console.warningReport("Image loaders not available", e);
                System.out.println("Image message " + e);
            }
        }
*/
        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof Web3DResourceFactory)) {
            res_fac = new Web3DResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }

        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
        if(!(c_fac instanceof AWTContentHandlerFactory)) {
            c_fac = new AWTContentHandlerFactory(core, loader, c_fac);
            URI.setContentHandlerFactory(c_fac);
        }

        FileNameMap fn_map = URI.getFileNameMap();
        if(!(fn_map instanceof VRMLFileNameMap)) {
            fn_map = new VRMLFileNameMap(fn_map);
            URI.setFileNameMap(fn_map);
        }
    }

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    private void shutdownApp()
    {
        sceneManager.shutdown();
        loadManager.shutdown();
        scriptLoader.shutdown();
        eventModel.shutdown();
    }
}
