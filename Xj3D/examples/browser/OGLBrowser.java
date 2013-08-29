/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import javax.swing.*;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.audio.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.media.opengl.GLCapabilities;

import org.j3d.aviatrix3d.output.audio.OpenALAudioDevice;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.renderer.aviatrix3d.pipeline.DebugFrustumCullStage;

// Local imports
//import org.web3d.browser.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.loading.*;
import org.xj3d.core.eventmodel.*;
import org.xj3d.impl.core.eventmodel.*;

import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.Xj3DConstants;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;
import org.web3d.vrml.renderer.common.input.DefaultSensorManager;
import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLLayerManagerFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.PerFrameManager;
import org.web3d.vrml.renderer.ogl.input.DefaultPickingManager;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;

import org.xj3d.ui.awt.device.AWTDeviceFactory;

import org.xj3d.ui.awt.widgets.SwingNavigationToolbar;
import org.xj3d.ui.awt.widgets.SwingProgressListener;
import org.xj3d.ui.awt.widgets.SwingViewpointToolbar;

/**
 * A demonstration application that shows how to put together all of the
 * Xj3D toolkit into a browser application using the OpenGL renderer.
 * <p>
 *
 * The simple browser does not respond to changes in the list of viewpoints
 * in the virtual world. This is OK because scripts are not used or needed in
 * this simple environment. Once we implement scripts, we have to look at
 * something different.
 *
 * @author Justin Couch
 * @version $Revision: 1.60 $
 */
public class OGLBrowser extends DemoFrame
    implements WindowListener,
               SensorStatusListener,
               Runnable {

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Global clock */
    private VRMLClock clock;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    private ContentLoadManager loadManager;

    private ScriptLoader scriptLoader;

    /**
     * Create an instance of the demo class.
     */
    public OGLBrowser() {
        super("OpenGL/Aviatrix VRML & X3D Browser");
        addWindowListener(this);
        setSize(800, 600);
        setLocation(40, 40);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        viewpointDefMap = new HashMap();

        createWindow(setupAviatrix(), worldLoader);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));
    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Run method for the shutdown hook. This is to deal with someone using
     * ctrl-C to kill the application. Makes sure that all the resources
     * are cleaned up properly.
     */
    public void run()
    {
        shutdownApp();
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt) {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt) {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt) {
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt) {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt) {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt) {
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt) {
    }

    //----------------------------------------------------------
    // Methods required by the LinkSelectionListener interface.
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
                VRMLViewpointNodeType vp =
                    (VRMLViewpointNodeType)viewpointDefMap.get(def_name);

                if(vp != null) {
                    universe.changeViewpoint(vp);
                } else {
                    statusBar.setStatusText("Unknown Viewpoint " + def_name);
                    console.warningReport("Unknown Viewpoint " + def_name, null);
                }
            } else {
                // load the world.
                try {
                    URL url = new URL(url_list[i]);
                    InputSource is = new InputSource(url);
                    if(success = load(is))
                        break;

                } catch(MalformedURLException mue) {
                    statusBar.setStatusText("Invalid URL");
                    console.warningReport("Invalid URL: " + url_list[i], mue);
                }
            }
        }

        if(!success)
            console.errorReport("No valid URLs were found", null);
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Setup the avaiatrix pipeline here
     */
    private ViewpointManager setupAviatrix() {

        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

//        GraphicsCullStage culler = new NullCullStage();
//        GraphicsCullStage culler = new SimpleFrustumCullStage();
        GraphicsCullStage culler = new FrustumCullStage();
//        GraphicsCullStage culler = new DebugFrustumCullStage(false);
        culler.setOffscreenCheckEnabled(true);

//        GraphicsSortStage sorter = new NullSortStage();
        GraphicsSortStage sorter = new StateSortStage();
//        GraphicsSortStage sorter = new DepthSortedTransparencyStage();
//        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
//        SortStage sorter = new StateSortStage();

        surface = new DebugAWTSurface(caps);
//        surface = new SimpleAWTSurface(caps);
//        surface = new StereoAWTSurface(caps);
//        surface.setStereoRenderingPolicy(GraphicsOutputDevice.ALTERNATE_FRAME_STEREO);

        AudioOutputDevice adevice = new OpenALAudioDevice();

        AudioCullStage aculler = new NullAudioCullStage();
        AudioSortStage asorter = new NullAudioSortStage();

        DefaultAudioPipeline audioPipeline = new DefaultAudioPipeline();
        audioPipeline.setCuller(aculler);
        audioPipeline.setSorter(asorter);
        audioPipeline.setAudioOutputDevice(adevice);

        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();
        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

//        pipeline.setEyePointOffset(0.1f, 0, 0);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
        sceneManager.disableInternalShutdown();
        sceneManager.setMinimumFrameInterval(20);

// Currently causing lockups on exit sometimes.
//        sceneManager.addPipeline(audioPipeline);
//        sceneManager.setAudioOutputDevice(adevice);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        canvas = (Canvas)surface.getSurfaceObject();
        Container content_pane = getContentPane();

        content_pane.add(canvas, BorderLayout.CENTER);

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

        DefaultHumanoidManager hanim_manager =
            new DefaultHumanoidManager();
        DefaultRigidBodyPhysicsManager physics_manager =
            new DefaultRigidBodyPhysicsManager();
        DefaultParticleSystemManager particle_manager =
            new DefaultParticleSystemManager();
        NetworkManager network_manager = new DefaultNetworkManager();
        DISProtocolHandler dis_handler = new DISProtocolHandler();
        network_manager.addProtocolHandler(dis_handler);

        eventModel = new DefaultEventModelEvaluator();
        universe = new OGLStandardBrowserCore(eventModel,
                                              sceneManager,
                                              displayManager);
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

        NodeManager[] node_mgrs = {
            network_manager,
            hanim_manager,
            physics_manager,
            particle_manager
        };

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
                              node_mgrs);
        eventModel.setErrorReporter(console);

        GraphicsResizeListener[] listeners = ((OGLStandardBrowserCore)universe).getGraphicsResizeListeners();

        for(int i=0; i < listeners.length; i++) {
            surface.addGraphicsResizeListener(listeners[i]);
        }

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            canvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console );

        InputDeviceManager idm = new InputDeviceManager( deviceFactory );
        KeyDeviceSensorManager kdsm = new KeyDeviceSensorManager( deviceFactory );

        sensor_manager.setInputManager(idm);
        sensor_manager.setKeyDeviceSensorManager(kdsm);

        clock = universe.getVRMLClock();

        ScriptEngine jsai = new VRML97ScriptEngine(universe,
                                                   route_manager,
                                                   state_manager,
                                                   worldLoader);
        jsai.setErrorReporter(console);

        ScriptEngine ecma = new JavascriptScriptEngine(universe,
                                                       route_manager,
                                                       state_manager,
                                                       worldLoader);
        ecma.setErrorReporter(console);

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

        scriptLoader.registerScriptingEngine(jsai);
        scriptLoader.registerScriptingEngine(ecma);
        scriptLoader.registerScriptingEngine(java_sai);
        scriptLoader.registerScriptingEngine(ecma_sai);

        setupProperties(universe, worldLoader);

        return vp_manager;
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

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return true if the world loaded correctly
     */
    private boolean load(InputSource is) {
        inSetup = true;

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
                viewpointDefMap.put(key, vp);
        }

        statusBar.setStatusText("World Loaded Successfully");
        inSetup = false;

        return ret_val;
    }

    /**
     * Override addNotify so we know we have peer before calling setEnabled for Aviatrix3D.
     */
    public void addNotify() {
        super.addNotify();

        sceneManager.setEnabled(true);
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param argv The list of arguments for this application.
     */
    public static void main(String[] argv) {
        OGLBrowser browser = new OGLBrowser();
        browser.setVisible(true);
    }
}
