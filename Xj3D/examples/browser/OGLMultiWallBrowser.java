/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import javax.swing.*;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.java.games.jogl.GLCapabilities;

import org.j3d.aviatrix3d.audio.OpenALAudioDevice;
import org.j3d.aviatrix3d.surface.SimpleAWTSurface;
import org.j3d.aviatrix3d.surface.StereoAWTSurface;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;
import org.xj3d.impl.core.eventmodel.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.SensorStatusListener;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;
import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.PerFrameManager;
import org.web3d.vrml.renderer.ogl.input.DefaultPickingManager;
import org.web3d.vrml.renderer.ogl.input.DefaultSensorManager;
import org.web3d.vrml.renderer.ogl.input.OGLSensorManager;
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

/**
 * A demonstration application that shows multiwall support in the OGL browser.
 * <p>
 *
 * The simple browser does not respond to changes in the list of viewpoints
 * in the virtual world. This is OK because scripts are not used or needed in
 * this simple environment. Once we implement scripts, we have to look at
 * something different.
 *
 * This program requires each canvas to have a 1:1 aspect ratio.  Hence the
 * width and height of each wall must be the same.
 *
 * @author Justin Couch
 * @version $Revision: 1.24 $
 */
public class OGLMultiWallBrowser extends DemoFrame
    implements WindowListener, SensorStatusListener,
               Runnable, ComponentListener {

    /** Manager for the scene graph handling */
//    private SingleThreadRenderManager sceneManager;
    private MultiThreadRenderManager sceneManager;

    /** Our primary drawing surface */
    private DrawableSurface surface1;

    /** Our secondary drawing surfaces */
    private DrawableSurface surface2;
    private DrawableSurface surface3;
    private DrawableSurface surface4;
    private DrawableSurface surface5;
    private DrawableSurface surface6;

    /** The universe to place our scene into */
    private OGLStandardBrowserCore universe;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Place for error messages to go */
    private ConsoleWindow console;

    /** Global clock */
    private VRMLClock clock;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    /**
     * Create an instance of the demo class.
     */
    public OGLMultiWallBrowser() {
        super("OpenGL/Aviatrix3D Multi-Wall Demo");
        addWindowListener(this);
        setSize(1132, 450);
        setLocation(40, 40);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        viewpointDefMap = new HashMap();

        Container content_pane = getContentPane();

        JPanel p1 = new JPanel(new BorderLayout());
        content_pane.add(p1, BorderLayout.CENTER);
        console = new ConsoleWindow();

        setupAviatrix(p1);

        JPanel p2 = new JPanel(new BorderLayout());
        p1.add(p2, BorderLayout.SOUTH);

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
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        shutdownApp();
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
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
                    statusLabel.setText("Unknown Viewpoint " + def_name);
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
                    statusLabel.setText("Invalid URL");
                    console.warningReport("Invalid URL: " + url_list[i], mue);
                }
            }
        }

        if(!success)
            console.errorReport("No valid URLs were found", null);
    }

    //----------------------------------------------------------
    // Implmentation of base class abstract methods
    //----------------------------------------------------------

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param url The URL to open
     */
    public void gotoLocation(URL url) {
        InputSource is = new InputSource(url);

        load(is);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param file The file to load
     */
    public void gotoLocation(File file) {
        InputSource is = new InputSource(file);

        load(is);
    }

    protected void setWarning(String msg) {
        statusLabel.setText(msg);
        console.warningReport(msg, null);
    }

    protected void setError(String msg) {
        statusLabel.setText(msg);
        console.errorReport(msg, null);
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix(JPanel panel)
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

//        CullStage culler1 = new NullCullStage();
//        CullStage culler2 = new NullCullStage();
//        CullStage culler3 = new NullCullStage();

        CullStage culler1 = new SimpleFrustumCullStage();
        CullStage culler2 = new SimpleFrustumCullStage();
        CullStage culler3 = new SimpleFrustumCullStage();
        culler1.setOffscreenCheckEnabled(true);
        culler2.setOffscreenCheckEnabled(true);
        culler3.setOffscreenCheckEnabled(true);

//        SortStage sorter = new NullSortStage();
//        SortStage sorter = new SimpleTransparencySortStage();
//        SortStage sorter = new DepthSortedTransparencyStage();
        SortStage sorter1 = new StateAndTransparencyDepthSortStage();
        SortStage sorter2 = new StateAndTransparencyDepthSortStage();
        SortStage sorter3 = new StateAndTransparencyDepthSortStage();

        surface1 = new SimpleAWTSurface(caps);
        surface2 = new SimpleAWTSurface(caps);
        surface3 = new SimpleAWTSurface(caps);
//        surface = new StereoAWTSurface(caps);

        surface1.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);
        surface2.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);
        surface3.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);

        AudioDevice adevice = new OpenALAudioDevice();

        AudioCullStage aculler = new NullAudioCullStage();
        AudioSortStage asorter = new NullAudioSortStage();

        DefaultAudioPipeline audioPipeline = new DefaultAudioPipeline();
        audioPipeline.setCuller(aculler);
        audioPipeline.setSorter(asorter);
        audioPipeline.setAudioDevice(adevice);

        DefaultRenderPipeline pipeline1 = new DefaultRenderPipeline();
        DefaultRenderPipeline pipeline2 = new DefaultRenderPipeline();
        DefaultRenderPipeline pipeline3 = new DefaultRenderPipeline();

        pipeline1.setCuller(culler1);
        pipeline2.setCuller(culler2);
        pipeline3.setCuller(culler3);

        pipeline1.setSorter(sorter1);
        pipeline2.setSorter(sorter2);
        pipeline3.setSorter(sorter3);

        pipeline1.setDrawableSurface(surface1);
        pipeline2.setDrawableSurface(surface2);
        pipeline3.setDrawableSurface(surface3);

        // Two panels, side by side, assuming 45deg field of view
//        pipeline1.setEyePointOffset(0.0765f, 0, 0);
//        pipeline2.setEyePointOffset(-0.0765f, 0, 0);

        pipeline1.setScreenOrientation(0, 1, 0, -(float)(Math.PI / 2));
        pipeline3.setScreenOrientation(0, 1, 0, (float)(Math.PI / 2));

        // Render manager
        sceneManager = new MultiThreadRenderManager();
        sceneManager.addPipeline(pipeline1);
        sceneManager.addPipeline(pipeline2);
        sceneManager.addPipeline(pipeline3);
//        sceneManager.setMinimumFrameInterval(20);
        sceneManager.addAudioPipeline(audioPipeline);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp1 = (Component)surface1.getSurfaceObject();
        Component comp2 = (Component)surface2.getSurfaceObject();
        Component comp3 = (Component)surface3.getSurfaceObject();
        comp1.addComponentListener(this);
        comp2.addComponentListener(this);
        comp3.addComponentListener(this);

        JPanel p1 = new JPanel(new GridLayout(1, 3));

        p1.add(comp1);
        p1.add(comp2);
        p1.add(comp3);

        panel.add(p1, BorderLayout.CENTER);


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
        ContentLoadManager load_manager = new MemCacheLoadManager();
        ScriptLoader script_loader = new DefaultScriptLoader();
        ScriptManager script_manager = new DefaultScriptManager();
        script_manager.setScriptLoader(script_loader);

        FrameStateManager state_manager = new DefaultFrameStateManager();

        PickingManager picker_manager = new DefaultPickingManager();
        picker_manager.setErrorReporter(console);

        OGLSensorManager sensor_manager = new DefaultSensorManager();
        sensor_manager.setPickingManager(picker_manager);

        RouteManager route_manager = new DefaultRouteManager();
//        route_manager.setRouterFactory(new SimpleRouterFactory());
        route_manager.setRouterFactory(new ListsRouterFactory());

        DefaultHumanoidManager hanim_manager = new DefaultHumanoidManager();
        DefaultRigidBodyPhysicsManager physics_manager =
            new DefaultRigidBodyPhysicsManager();
        DefaultParticleSystemManager particle_manager =
            new DefaultParticleSystemManager();
        NetworkManager network_manager = new DefaultNetworkManager();
        DISProtocolHandler dis_handler = new DISProtocolHandler();
        network_manager.addProtocolHandler(dis_handler);

        eventModel = new DefaultEventModelEvaluator();
        universe = new OGLStandardBrowserCore(eventModel, sceneManager);
        universe.addSensorStatusListener(this);
        universe.setErrorReporter(console);
        universe.setHardwareFOV(90);

        worldLoader = new DefaultWorldLoaderManager(universe,
                                                    route_manager,
                                                    state_manager);
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


        ViewpointManager vp_manager = new DefaultViewpointManager(universe);

        eventModel.initialize(script_manager,
                               route_manager,
                               sensor_manager,
                               state_manager,
                               load_manager,
                               vp_manager,
                               node_mgrs);
        eventModel.setErrorReporter(console);

        InputDeviceManager idm = new InputDeviceManager(universe.getIDString(),
                                                        comp2,
                                                        surface2);
        sensor_manager.setInputManager(idm);
        comp1.addKeyListener(idm);
        comp2.addKeyListener(idm);
        comp3.addKeyListener(idm);

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

        script_loader.registerScriptingEngine(jsai);
        script_loader.registerScriptingEngine(ecma);
        script_loader.registerScriptingEngine(java_sai);
        script_loader.registerScriptingEngine(ecma_sai);

        //universe.addNavigationStateListener(navToolbar);
        universe.addSensorStatusListener(this);

        setupProperties(universe, worldLoader);

        console.setVisible(true);

        DownloadProgressListener dl_list =
            new DownloadProgressListener(statusLabel, console);

        ResourceConnection.addGlobalProgressListener(dl_list);
    }

    //----------------------------------------------------------
    // Methods required by the ComponentListener interface.
    //----------------------------------------------------------

    public void componentHidden(ComponentEvent evt) {
    }

    public void componentMoved(ComponentEvent evt) {
    }

    public void componentResized(ComponentEvent evt) {
        Component canvas = (Component) evt.getSource();

        Dimension size = canvas.getSize();

        int width;
        int height;

        width = (int) size.getWidth();
        height = (int) size.getHeight();

        if (width > 0 && height > 0)
            universe.setViewport(new Rectangle(0,0,width,height));
    }

    public void componentShown(ComponentEvent evt) {
    }

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    private void shutdownApp()
    {
        sceneManager.setEnabled(false);
        eventModel.shutdown();
        surface1.dispose();
        surface2.dispose();
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

        statusLabel.setText("World Loaded Successfully");
        ret_val = true;
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
        OGLMultiWallBrowser browser = new OGLMultiWallBrowser();
        browser.setVisible(true);
    }
}
