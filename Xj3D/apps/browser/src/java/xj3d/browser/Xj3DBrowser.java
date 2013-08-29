/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2001 - 2007
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

package xj3d.browser;

// External imports
import java.awt.*;
import java.awt.event.*;
import javax.swing.AbstractAction;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.lang.reflect.Constructor;

import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;
import org.ietf.uri.*;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.management.*;
import org.j3d.aviatrix3d.pipeline.*;
import org.j3d.aviatrix3d.pipeline.audio.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.output.graphics.*;
import org.j3d.aviatrix3d.output.audio.OpenALAudioDevice;

import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
//import org.j3d.device.output.elumens.SPI;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.opengl.GLCapabilities;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.loading.*;
import org.xj3d.core.eventmodel.*;
import org.xj3d.impl.core.eventmodel.*;
import org.xj3d.ui.awt.widgets.*;

import org.web3d.browser.BrowserCore;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.net.resolve.Web3DURNResolver;

import org.xj3d.impl.core.loading.FramerateThrottle;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.Xj3DConstants;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.export.PlainTextErrorReporter;
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
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;
import org.web3d.util.ErrorReporter;

import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;
import org.xj3d.impl.core.loading.AbstractLoadManager;

import org.xj3d.ui.awt.device.AWTDeviceFactory;

import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;

/**
 * A standalone X3D/VRML browser application.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.98 $
 */
public class Xj3DBrowser extends JFrame
    implements SurfaceManager, WindowListener,
               SensorStatusListener,
               KeyListener, BrowserCoreListener,
               Runnable {

    /** Name of the property file that defines everything we need */
    private static final String PROPERTY_FILE = "xj3d_browser.properties";

    /** Typical usage message with program options */
    private static final String USAGE_MSG =
      "Usage: Xj3DBrowser [options] [filename]\n" +
      "  -help                   Prints out this help message\n" +
//      "  -spherical              Render using spherical rendering. \n" +
      "  -fullscreen n           Runs the browser in fullscreen exclusive mode on screen n.  n is optional\n" +
      "  -stereo quad|alternate  Enables stereo projection output\n" +
      "  -antialias n            Use n number of multisamples to antialias scene\n" +
      "  -useMipMaps TRUE|FALSE  Forces mipmap usage on all textures\n" +
      "  -anisotropicDegree n    Forces anisotripic filtering of nTH degree\n" +
      "  -numChannels n          Selects how many channels to use for Elumens mode(1-4)\n" +
      "  -numCpus n              Select how many cpu's to use.  Defaults to 1\n" +
      "  -nice                   Do not use all the CPU for rendering\n" +
      "  -captureViewpoints      Generate a screenshot of each viewpoint of the file being loaded\n" +
      "  -zbuffer n              Select how many bits of zbuffer, 8,16, 24, or 32\n" +
      "  -screenSize w h         Specify the screen size to use\n";

    /** What minimum frame cycle should we use during startup */
    private static final int FRAME_CYCLE_MINIMUM_STARTUP = 250;

    /** What minimum frame cycle should we use during noloading operation */
    private int FRAME_CYCLE_MINIMUM_NOLOADING = 0;

    /** What minimum frame cycle should we use during post startup loading operation */
    private static final int FRAME_CYCLE_MINIMUM_LOADING = 60;

    /** The real component that is being rendered to */
    protected Canvas canvas;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /** The content pane for the frame */
    private Container mainPane;

    /** Area to push error messages to */
    protected ErrorReporter console;

    /** Created by the derived class */
    protected OGLStandardBrowserCore universe;

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

    /** The load manager */
    private AbstractLoadManager loadManager;

    /** The input device manager */
    private InputDeviceManager idm;

    /* The KeyDevice sensor manager */
    private KeyDeviceSensorManager kdsm;

    /** The sensor manager */
    private SensorManager sensorManager;

    /** The script loader */
    private ScriptLoader scriptLoader;

    /** The location tool bar */
    private SwingLocationToolbar locToolbar;

    /** The viewpoint tool bar */
    private SwingViewpointToolbar viewpointToolbar;

    /** Is the initial world loading done. */
    private boolean initialLoadingDone;

    /** Are we done parsing the world */
    private boolean parsingDone;

    /** The viewpoint manager. */
    private ViewpointManager viewpointManager;

    /** Are we in wireframe mode */
    private boolean wireframe;

    /** point or filled mode */
    private boolean pointrender;

    /** The glCapabilities choosen */
    private GLCapabilities caps;

    /** The graphics pipeline */
    private DefaultGraphicsPipeline pipeline;

    /** The audio pipeline */
    private DefaultAudioPipeline audioPipeline;

    /** The framerate throttle */
    private FramerateThrottle frameThrottle;

    /** The antialising action */
    private AntialiasingAction antialiasingAction;

    /** Scene info action */
    private SceneInfoAction sceneInfoAction;

    /** Profiling info action */
    private ProfilingInfoAction profilingInfoAction;

    /** Scene tree action */
    private SceneTreeAction sceneTreeAction;

    /** Screen capture action */
    private ScreenShotAction screenShotAction;

    /** Movie Start action */
    private MovieAction movieStartAction;

    /** Movie End action */
    private MovieAction movieEndAction;

    /** Capture viewpoint action */
    private CaptureViewpointsAction capAction;

    /** Are we waiting for the load to finish */
    private boolean waitingForLoad;

    /** Has addNotify happended */
    private boolean addNotifyHandled;

    /** Should we use full screen mode */
    private boolean useFullscreen;

    /** Spherical properties, null if not set from properties file */
    private static float[] eyePos;
    private static float[] lensPos;
    private static double[] screenOrientation;
    private static int[] chanSize;

    // Command line arguments
    private static int fullscreen = -1;
    private static boolean stereo = false;
    private static boolean sphericalMode = false;
    private static int desiredSamples = 1;
    private static int numChannels = 3;
    private static int numCpus = 1;
    private static int numZBits = -1;
    private static boolean redirect = true;
    private static boolean nice = true;
    private static boolean useMipMaps = true;
    private static int anisotropicDegree = 1;
    private static int stereoMode = 0;
    private static boolean captureViewpoints = false;
    private static boolean useImageLoader = true;
    private static int[] screenSize = null;   // null means not provided

    /**
     * Create an instance of the demo class.
     */
    public Xj3DBrowser() {

        super("Xj3D Browser - Aviatrix3D");

        wireframe = false;
        pointrender = false;

        addNotifyHandled = false;

        addWindowListener(this);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        viewpointDefMap = new HashMap();

        if (captureViewpoints) {
            console = new PlainTextErrorReporter();
        } else {
            console = new SwingConsoleWindow();
        }
        console.messageReport("Initializing Browser");

        viewpointManager = setupAviatrix();

/*
        setSize(512 + 8, 512 + 141);
        setLocation(0, 0);
*/

        int width = 800;
        int height = 600;

        if (screenSize != null) {
            // TODO: Bah, wish I knew how to get these window dressing params
            width = screenSize[0] + 8;
            height = screenSize[1] + 141;
        }

        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev;

        if (fullscreen >= 0) {
            useFullscreen = true;
            GraphicsDevice[] gs = env.getScreenDevices();
            if (gs.length < fullscreen + 1) {
                System.out.println("Invalid fullscreen device.  Using default");
                dev = env.getDefaultScreenDevice();

                Dimension dmn = Toolkit.getDefaultToolkit().getScreenSize();
                width = (int) dmn.getWidth();
                height = (int) dmn.getHeight();
            } else {
                dev = gs[fullscreen];
                DisplayMode dm = dev.getDisplayMode();
                width = dm.getWidth();
                height = dm.getHeight();
            }
        } else {
            dev = env.getDefaultScreenDevice();
        }

        if(useFullscreen && !dev.isFullScreenSupported()) {
            System.out.println("Fullscreen not supported");
            useFullscreen = false;
        }

        // If we are fullscreen mode, make the frame do that,
        // but don't put any of the normal decorations like buttons,
        // URL bars etc.
        if(useFullscreen) {
            setUndecorated(true);
            dev.setFullScreenWindow(this);
        }

        createUI(viewpointManager);

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);


        ImageIcon icon = IconLoader.loadIcon("images/branding/yumetech-16x16.gif", console);

        if (icon != null)
            setIconImage(icon.getImage());

        setSize(width, height);

        if (!useFullscreen && screenSize == null) {
            setLocation(40, 40);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    //---------------------------------------------------------------
    // Methods defined by SurfaceManager
    //---------------------------------------------------------------

    /**
     * Reset the surface with a new mode or parameters.
     */
    public void resetSurface() {
        if (sphericalMode) {
            System.out.println("Switching to Spherical Mode");

            sceneManager.setEnabled(false);
            surface.dispose();

            mainPane.remove(canvas);

            surface = new ElumensAWTSurface(caps);
            ((ElumensAWTSurface) surface).setNumberOfChannels(3);

            pipeline.setGraphicsOutputDevice(surface);

            //sceneManager.setGraphicsOutputDevice(surface);
            sceneManager.addDisplay(displayManager);
            canvas = (Canvas)surface.getSurfaceObject();

            mainPane.add(canvas, BorderLayout.CENTER);

            universe.setHardwareFOV(180);
            loadProperties();

            sceneManager.setEnabled(true);
        } else {
            System.out.println("Switching to Normal Mode");

            sceneManager.setEnabled(false);
            surface.dispose();

            mainPane.remove(canvas);

            if (stereo) {
                surface = new StereoAWTSurface(caps, stereoMode);
            } else {
                surface = new SimpleAWTSurface(caps);
            }
            pipeline.setGraphicsOutputDevice(surface);
            //sceneManager.setGraphicsOutputDevice(surface);

            canvas = (Canvas)surface.getSurfaceObject();

            mainPane.add(canvas, BorderLayout.CENTER);

            universe.setHardwareFOV(0);
            sceneManager.setEnabled(true);
        }

        screenShotAction.setSurface(surface);
        movieStartAction.setSurface(surface);
        movieEndAction.setSurface(surface);

        canvas.addKeyListener(this);

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            canvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console );

        idm.reinitialize(deviceFactory);
        kdsm.reinitialize(deviceFactory);

        sensorManager.setInputManager(idm);
        sensorManager.setKeyDeviceSensorManager(kdsm);

        GraphicsResizeListener[] listeners = ((OGLStandardBrowserCore)universe).getGraphicsResizeListeners();

        for(int i=0; i < listeners.length; i++) {
            surface.addGraphicsResizeListener(listeners[i]);
        }
    }

    /**
     * Get the current capability bits.
     */
    public GLCapabilities getCapabilities() {
        return caps;
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

    //----------------------------------------------------------
    // Methods required by the KeyListener interface.
    //----------------------------------------------------------
    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {

            case KeyEvent.VK_A:
                if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    antialiasingAction.actionPerformed(new ActionEvent(this, 0, "Cycle"));
                }
                break;

            case KeyEvent.VK_P:
                if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    Vector3f pos = new Vector3f();
                    AxisAngle4f ori = new AxisAngle4f();

                    getUserPosition(pos, ori);

                    console.messageReport("Viewpoint {");
                    console.messageReport("   position " + pos.x + " " + pos.y + " " + pos.z);
                    console.messageReport("   orientation " + ori.x + " " + ori.y + " " + ori.z + " " + ori.angle);
                    console.messageReport("}");
                }
                break;
            case KeyEvent.VK_V:
/*
                if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    browser.printScene();
                }
*/
                break;
            case KeyEvent.VK_S:
                if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    System.out.println("Straighten not implemented");
                } else if((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    // Enter/Exit Spherical mode
                    sphericalMode = !sphericalMode;

                    // TODO: Do we need to cleanup old sensor manager stuff?

                    if (sphericalMode) {
                        surface = new ElumensAWTSurface(caps);
                        ((ElumensAWTSurface) surface).setNumberOfChannels(3);
                        pipeline.setGraphicsOutputDevice(surface);
                        //sceneManager.setGraphicsOutputDevice(surface);

                        loadProperties();
                    }

                    resetSurface();

                }

                break;
            case KeyEvent.VK_C:
/*
                // Configure -- Should be more then eye sep later
                eyePanel = (JPanel) browser.getConfigurationComponent();
                if((eyePanel != null) &&
                   (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    JOptionPane.showMessageDialog(this,
                                                  eyePanel,
                                                  "Set Eye Separation",
                                                  JOptionPane.PLAIN_MESSAGE);
                }
*/
                break;
            case KeyEvent.VK_1:
/*
                if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    browser.setNumberOfChannels(1);
                }
*/
                break;
            case KeyEvent.VK_2:
/*
                if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    browser.setNumberOfChannels(2);
                }
*/
                break;
            case KeyEvent.VK_3:
/*
                if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    browser.setNumberOfChannels(3);
                }
*/
                break;
            case KeyEvent.VK_4:
/*
                if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    browser.setNumberOfChannels(4);
                }
*/
                break;
        }

    }

    public void keyTyped(KeyEvent e) {
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
        // TODO: pause sceneManager?
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt) {
        // TODO: Moved to addNotify to see if that fixes startup issue
        //sceneManager.setEnabled(true);
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
    // BrowserCoreListener methods
    //----------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    public void browserInitialized(VRMLScene scene) {
        waitingForLoad = false;

        if (addNotifyHandled)
            sceneManager.setEnabled(true);
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
        waitingForLoad = false;

        if (captureViewpoints) {
            System.exit(0);
        }

        sceneManager.setEnabled(false);

        if (console instanceof Window) {
            ((Window)console).toFront();
        } else {
            System.out.println("Can't put console to front");
        }
/*
        JOptionPane.showMessageDialog(this,
                                      eyePanel,
                                      "Set Eye Separation",
                                      JOptionPane.PLAIN_MESSAGE);
*/
    }

    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    public void browserShutdown() {
        waitingForLoad = false;
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    public void browserDisposed() {
        waitingForLoad = false;
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Setup the avaiatrix pipeline here
     */
    private ViewpointManager setupAviatrix() {

        // Assemble a simple single-threaded pipeline.
        caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        if (desiredSamples > 1)
            caps.setSampleBuffers(true);
        else
            caps.setSampleBuffers(false);

        caps.setNumSamples(desiredSamples);

        if (numZBits > 0)
            caps.setDepthBits(numZBits);

//System.out.println("Using Null Cull Stage");
//        GraphicsCullStage culler = new NullCullStage();
//        GraphicsCullStage culler = new SimpleFrustumCullStage();
        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

//System.out.println("Using NULL sort stage");
//        GraphicsSortStage sorter = new NullSortStage();
        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();

        if (sphericalMode) {
            surface = new ElumensAWTSurface(caps);
            ((ElumensAWTSurface) surface).setNumberOfChannels(3);
        } else {
            if (stereo) {
                surface = new StereoAWTSurface(caps, stereoMode);
            } else {
                surface = new SimpleAWTSurface(caps);
//System.out.println("Using debug surface");
//                surface = new DebugAWTSurface(caps);

            }
        }

        AudioOutputDevice adevice = new OpenALAudioDevice();

        AudioCullStage aculler = new NullAudioCullStage();
        AudioSortStage asorter = new NullAudioSortStage();

        audioPipeline = new DefaultAudioPipeline();
        audioPipeline.setCuller(aculler);
        audioPipeline.setSorter(asorter);
        audioPipeline.setAudioOutputDevice(adevice);

        pipeline = new DefaultGraphicsPipeline();
        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

//        pipeline.setEyePointOffset(0.1f, 0, 0);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.disableInternalShutdown();
        sceneManager.addDisplay(displayManager);
        //sceneManager.setMinimumFrameInterval(20);

// Currently causing lockups on exit sometimes.
//        sceneManager.addPipeline(audioPipeline);
//        sceneManager.setAudioOutputDevice(adevice);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        canvas = (Canvas)surface.getSurfaceObject();
        canvas.addKeyListener(this);

        mainPane = getContentPane();

        mainPane.add(canvas, BorderLayout.CENTER);

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

        sensorManager = new DefaultSensorManager();
        sensorManager.setPickingManager(picker_manager);

        RouteManager route_manager = new DefaultRouteManager();
//        route_manager.setRouterFactory(new SimpleRouterFactory());
        route_manager.setRouterFactory(new ListsRouterFactory());

        DefaultHumanoidManager hanim_manager =
            new DefaultHumanoidManager();

        NodeManager physics_manager = null;

        try {
            Object manager = Class.forName("org.xj3d.impl.core.eventmodel.DefaultRigidBodyPhysicsManager").newInstance();
            physics_manager = (NodeManager) manager;
        } catch (Exception e) {
            console.warningReport("PhysicsManager not found, Physics disabled", null);
        } catch (NoClassDefFoundError nc) {
            console.warningReport("PhysicsManager not found, Physics disabled", null);
        }

        DefaultParticleSystemManager particle_manager =
            new DefaultParticleSystemManager();
        NetworkManager network_manager = new DefaultNetworkManager();

        try {
            Object handler = Class.forName("org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler").newInstance();
            network_manager.addProtocolHandler((NetworkProtocolHandler)handler);

        } catch (Exception e) {
            console.warningReport("DISProtocolHandler not found, DIS handling disabled", null);
        } catch (NoClassDefFoundError nc) {
            console.warningReport("DISProtocolHandler not found, DIS handling disabled", null);
        }
/*
        try {
            Object handler = Class.forName("org.web3d.vrml.renderer.common.input.dis.DISXMLProtocolHandler").newInstance();
            network_manager.addProtocolHandler((NetworkProtocolHandler)handler);

        } catch (Exception e) {
            // Do not warn about this
            //console.warningReport("DISXMLProtocolHandler not found, DISXML handling disabled", null);
        } catch (NoClassDefFoundError nc) {
            // Do not warn about this
            //console.warningReport("DISXMLProtocolHandler not found, DISXML handling disabled", null);
        }
*/
        eventModel = new DefaultEventModelEvaluator();
        universe = new OGLStandardBrowserCore(eventModel, sceneManager, displayManager);
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

        NodeManager[] node_mgrs;

        if (physics_manager != null) {
            node_mgrs = new NodeManager[] {
                network_manager,
                hanim_manager,
                physics_manager,
                particle_manager
            };
        } else {
            node_mgrs = new NodeManager[] {
                network_manager,
                hanim_manager,
                particle_manager
            };
        }

        OGLLayerManagerFactory lmf = new OGLLayerManagerFactory();
        lmf.setErrorReporter(console);

        ViewpointManager vp_manager = new DefaultViewpointManager(universe);

        eventModel.initialize(script_manager,
                              route_manager,
                              sensorManager,
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

        idm = new InputDeviceManager( deviceFactory );
        kdsm = new KeyDeviceSensorManager( deviceFactory );

        sensorManager.setInputManager(idm);
        sensorManager.setKeyDeviceSensorManager(kdsm);

        clock = universe.getVRMLClock();

        createScriptEngine("org.web3d.vrml.scripting.jsai.VRML97ScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);

        createScriptEngine("org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);

        createScriptEngine("org.web3d.vrml.scripting.sai.JavaSAIScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);


        createScriptEngine("org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);

        setupProperties(universe, worldLoader);


        if (stereo)
            universe.setStereoEnabled(true);

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
        // TODO: Why not just use the LocationToolbar's method?
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
     * Create the window contents now. Assumes that the universe
     * variable is already set.
     */
    protected void createUI(ViewpointManager vpMgr) {

        ArrayList actionList = new ArrayList();

        org.xj3d.ui.awt.widgets.CursorManager cm =
            new org.xj3d.ui.awt.widgets.CursorManager(canvas, null, console);
        universe.addSensorStatusListener(cm);
        universe.addNavigationStateListener(cm);

        universe.addCoreListener(this);

        locToolbar =
            new SwingLocationToolbar(universe,
                                     worldLoader,
                                     false,
                                     true,
                                     true,
                                     null,
                                     null,
                                     console);

        SwingNavigationToolbar nav_tb =
            new SwingNavigationToolbar(universe,
                                       null,
                                       console);

        viewpointToolbar =
            new SwingViewpointToolbar(universe,
                                      vpMgr,
                                      null,
                                      console);

        statusBar = new SwingStatusBar(universe,
                                       true,
                                       true,
                                       null,
                                       console);

        locToolbar.setProgressListener(statusBar.getProgressListener());

        JPanel p2 = new JPanel(new BorderLayout());

        if (!useFullscreen) {
            p2.add(nav_tb, BorderLayout.WEST);
            p2.add(viewpointToolbar, BorderLayout.CENTER);
            p2.add(statusBar, BorderLayout.SOUTH);

            mainPane.add(locToolbar, BorderLayout.NORTH);
            mainPane.add(p2, BorderLayout.SOUTH);

            SwingConsoleButton console_button;

            if (!captureViewpoints) {
                console_button = new SwingConsoleButton((SwingConsoleWindow)console, null);
                p2.add(console_button, BorderLayout.EAST);
                ((SwingConsoleWindow)console).setVisible(true);
            }
/*
            System.out.println("Creating SceneTreeViewer");
            SceneTreeViewer sceneTree = new SceneTreeViewer(universe);

            mainPane.add(sceneTree, BorderLayout.WEST);
*/
        }

        frameThrottle = new FramerateThrottle(universe, console);
        frameThrottle.setScriptLoader(scriptLoader);
        frameThrottle.setLoadManager(loadManager);
        locToolbar.setThrottle(frameThrottle);

        if (nice) {
            frameThrottle.setMinimumNoLoading(20);
        }

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem = null;
        JRadioButtonMenuItem rbItem;

        // File Menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Open Item
        Action openAction = locToolbar.getOpenAction();
        actionList.add(openAction);
        fileMenu.add(new JMenuItem(openAction));

        ExitAction exitAction = new ExitAction();
        actionList.add(exitAction);

        // Exit Item
        fileMenu.add(new JMenuItem(exitAction));

        // View Menu
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        // Reload Item
        Action reloadAction = locToolbar.getReloadAction();
        actionList.add(reloadAction);
        viewMenu.add(new JMenuItem(reloadAction));

        // Render Style SubMenu
        JMenu renderStyle = new JMenu("Render Style");

        ButtonGroup rsGroup = new ButtonGroup();
        PointsStyleAction psa = new PointsStyleAction(universe, statusBar);
        actionList.add(psa);
        rbItem = new JRadioButtonMenuItem(psa);
        rsGroup.add(rbItem);
        renderStyle.add(rbItem);
        LinesStyleAction lsa = new LinesStyleAction(universe, statusBar);
        actionList.add(lsa);
        rbItem = new JRadioButtonMenuItem(lsa);
        rsGroup.add(rbItem);
        renderStyle.add(rbItem);
        ShadedStyleAction ssa = new ShadedStyleAction(universe, statusBar, psa, lsa);
        rbItem = new JRadioButtonMenuItem(ssa);
        actionList.add(ssa);
        rsGroup.add(rbItem);
        renderStyle.add(rbItem);
        rbItem.setSelected(true);

        viewMenu.add(renderStyle);

        // Screen Capture SubMenu
        JMenu captureMenu = new JMenu("Screen Capture");

        // Single Frame Item
        screenShotAction = new ScreenShotAction(console, universe);
        actionList.add(screenShotAction);
        screenShotAction.setSurface(surface);

        menuItem = new JMenuItem(screenShotAction);
        captureMenu.add(menuItem);
        viewMenu.add(captureMenu);

        // Movie Start Item
        movieStartAction = new MovieAction(true,console, universe);
        actionList.add(movieStartAction);
        movieStartAction.setSurface(surface);

        menuItem = new JMenuItem(movieStartAction);
        captureMenu.add(menuItem);
        viewMenu.add(captureMenu);

        // Movie End Item
        movieEndAction = new MovieAction(false,console, universe);
        actionList.add(movieEndAction);
        movieEndAction.setSurface(surface);

        menuItem = new JMenuItem(movieEndAction);
        captureMenu.add(menuItem);

        capAction = new CaptureViewpointsAction(console, universe, viewpointManager);
        actionList.add(capAction);
        capAction.setSurface(surface);
        menuItem = new JMenuItem(capAction);
        captureMenu.add(menuItem);

        viewMenu.add(captureMenu);


        // Scene Info Item
        sceneInfoAction = new SceneInfoAction(console, displayManager);
        actionList.add(sceneInfoAction);
        menuItem = new JMenuItem(sceneInfoAction);
        viewMenu.add(menuItem);

        profilingInfoAction = new ProfilingInfoAction(console, universe);
        actionList.add(profilingInfoAction);
        menuItem = new JMenuItem(profilingInfoAction);
        viewMenu.add(menuItem);

        // Scene Tree Item
        sceneTreeAction = new SceneTreeAction(console, universe, mainPane, BorderLayout.WEST);
        actionList.add(sceneTreeAction);
        menuItem = new JMenuItem(sceneTreeAction);
        viewMenu.add(menuItem);

        // Viewpoint Menu
        JMenu viewpointMenu = new JMenu("Viewpoint");
        menuBar.add(viewpointMenu);

        viewpointMenu.add(new JMenuItem(viewpointToolbar.getNextViewpointAction()));
        actionList.add(viewpointToolbar.getNextViewpointAction());
        viewpointMenu.add(new JMenuItem(viewpointToolbar.getPreviousViewpointAction()));
        actionList.add(viewpointToolbar.getPreviousViewpointAction());
        viewpointMenu.add(new JMenuItem(viewpointToolbar.getHomeViewpointAction()));
        actionList.add(viewpointToolbar.getHomeViewpointAction());

        JMenu navMenu = new JMenu("Navigation");
        menuBar.add(navMenu);
        navMenu.add(new JMenuItem(nav_tb.getFlyAction()));
        actionList.add(nav_tb.getFlyAction());
        navMenu.add(new JMenuItem(nav_tb.getWalkAction()));
        actionList.add(nav_tb.getWalkAction());
        navMenu.add(new JMenuItem(nav_tb.getExamineAction()));
        actionList.add(nav_tb.getExamineAction());
        navMenu.add(new JMenuItem(nav_tb.getTiltAction()));
        actionList.add(nav_tb.getTiltAction());
        navMenu.add(new JMenuItem(nav_tb.getPanAction()));
        actionList.add(nav_tb.getPanAction());
        navMenu.add(new JMenuItem(nav_tb.getTrackAction()));
        actionList.add(nav_tb.getTrackAction());
        navMenu.add(new JMenuItem(viewpointToolbar.getLookatAction()));
        actionList.add(viewpointToolbar.getLookatAction());
        navMenu.add(new JMenuItem(viewpointToolbar.getFitWorldAction()));
        actionList.add(viewpointToolbar.getFitWorldAction());

        JMenu optionsMenu = new JMenu("Options");
        NiceAction niceAction = new NiceAction(frameThrottle, statusBar, nice);
        actionList.add(niceAction);
        JCheckBoxMenuItem niceMenu = new JCheckBoxMenuItem(niceAction);
        niceMenu.setState(nice);
        optionsMenu.add(niceMenu);

        antialiasingAction = new AntialiasingAction(this, statusBar);
        actionList.add(antialiasingAction);

        JMenu antialiasingMenu = new JMenu("Anti-Aliasing");
        ButtonGroup antialiasingGroup = new ButtonGroup();

        int n = 2;

        rbItem = new JRadioButtonMenuItem("Disabled");
        if (desiredSamples <= 1)
            rbItem.setSelected(true);
        rbItem.setActionCommand("Disabled");
        rbItem.addActionListener(antialiasingAction);
        antialiasingMenu.add(rbItem);
        antialiasingGroup.add(rbItem);
        int maxSamples = antialiasingAction.getMaximumNumberOfSamples();

        while(n <= maxSamples) {
            rbItem = new JRadioButtonMenuItem(n + " Samples", n == desiredSamples);

            rbItem.addActionListener(antialiasingAction);
            rbItem.setActionCommand(Integer.toString(n));
            antialiasingMenu.add(rbItem);
            antialiasingGroup.add(rbItem);

            n = n * 2;
        }

        optionsMenu.add(antialiasingMenu);

        menuBar.add(optionsMenu);

        try {
            HelpAction helpAction = new HelpAction(false, null);
            actionList.add(helpAction);

            JMenu helpMenu = new JMenu("Help");
            menuBar.add(helpMenu);

            helpMenu.add(helpAction);
        } catch(NoClassDefFoundError nc) {
            console.warningReport("JavaHelp not found, help disabled", null);
        }

        if (!useFullscreen) {
            setJMenuBar(menuBar);
        } else {
            // Need to register all actions with canvas manually
            JComponent comp = (JComponent) getContentPane();
            KeyStroke ks;
            String actionName;

            Iterator itr = actionList.iterator();
            Action action;

            while(itr.hasNext()) {
                action = (Action) itr.next();

                ks = (KeyStroke) action.getValue(AbstractAction.ACCELERATOR_KEY);
                actionName = (String) action.getValue(AbstractAction.SHORT_DESCRIPTION);

                comp.getInputMap().put(ks, actionName);
                comp.getActionMap().put(actionName, action);
            }
        }
    }

    /**
     * Load content on the browser.  This is only called if a file is provided
     * on the command-line.  Otherwise the locationoolbar handles it.
     *
     * @param url The url to load
     */
    public void loadURL(String url) {

        String basename;
        int idx = url.indexOf("\\");
        if (idx > -1)
            basename = url.substring(idx);
        else
            basename = url;

        capAction.setBasename(basename);

        if (captureViewpoints) {
            waitingForLoad = true;
        }

        try {
            locToolbar.loadURL(url);
        } catch(IOException ioe) {
            console.errorReport("Error loading file: " + url, ioe);
        }

        if (captureViewpoints) {
            DeathTimer dt = new DeathTimer(20000);
            dt.start();

            while(waitingForLoad) {
                try {
                    Thread.sleep(100);
                } catch(Exception e) {}
            }

            while(!frameThrottle.isInitialLoadDone()) {
                try {
                    Thread.sleep(200);
                } catch(Exception e) {}
            }

            dt.exit();

            Component comp = (Component) surface.getSurfaceObject();
            Dimension size = comp.getSize();

            CaptureViewpointsAction action = new CaptureViewpointsAction(console, universe, viewpointManager);
            action.setBasename(basename);
            action.setSurface(surface);

            action.actionPerformed(new ActionEvent(this, 0, "Capture"));

            captureViewpoints = false;

            System.exit(0);
        }
    }

    /**
     * Redirect system messages to the console.
     */
    public void redirectSystemMessages() {
        if (console != null && console instanceof SwingConsoleWindow)
            ((SwingConsoleWindow)console).redirectSystemMessages();
    }

    /**
     * Get the current user position.
     *
     * @param pos The position
     * @param ori The orientation
     */
    private void getUserPosition(Vector3f pos, AxisAngle4f ori) {
        universe.getUserPosition(pos, ori);
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

        if (useMipMaps) {
            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
        }
        if (anisotropicDegree > 1) {
            System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", new Integer(anisotropicDegree).toString());
        }

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
     * Load the elumens.properties file.
     */
    public void loadProperties() {
        String user_dir = System.getProperty("user.dir");
        InputStream is;
        String file = user_dir + File.separator + PROPERTY_FILE;

        try {
            is = new FileInputStream(file);
        } catch(FileNotFoundException fnfe) {
            // Fallback to default
            is = (InputStream) ClassLoader.getSystemClassLoader().getResourceAsStream(PROPERTY_FILE);
        }

        if (is == null) {
            System.out.println("No elumens properties loaded");
        } else {

            Properties props = new Properties();
            try {
                props.load(is);
                is.close();
            } catch(IOException ioe) {
                System.out.println("Error reading elumens.properties");
            }

            StringTokenizer st;
            int fnum;
            String str = props.getProperty("EyePosition");

            if (str != null) {
                eyePos = new float[3];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        eyePos[fnum++] = Float.parseFloat(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }

            str = props.getProperty("LensPosition");

            if (str != null) {
                lensPos = new float[3];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        lensPos[fnum++] = Float.parseFloat(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }

            str = props.getProperty("ScreenOrientation");

            if (str != null) {
                screenOrientation = new double[3];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        screenOrientation[fnum++] = Double.parseDouble(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }

            str = props.getProperty("ChanSize");

            if (str != null) {
                chanSize = new int[2];

                try {
                    fnum=0;
                    st = new StringTokenizer(str);
                    while (st.hasMoreTokens()) {
                        chanSize[fnum++] = Integer.parseInt(st.nextToken());
                    }
                } catch(NumberFormatException pe) {
                    pe.printStackTrace();
                }
            }
        }
/*
        if (lensPos != null) {
            ((ElumensAWTSurface)surface).setChannelLensPosition(SPI.SPI_ALL_3_CHAN,lensPos[0],lensPos[1],lensPos[2]);
        }
        if (eyePos != null) {
            ((ElumensAWTSurface)surface).setChannelEyePosition(SPI.SPI_ALL_3_CHAN,eyePos[0],eyePos[1],eyePos[2]);
        }
        if (screenOrientation != null) {
            ((ElumensAWTSurface)surface).setScreenOrientation(screenOrientation[0],
                screenOrientation[1],screenOrientation[2]);
        }
        if (chanSize != null) {
            ((ElumensAWTSurface)surface).setChannelSize(SPI.SPI_ALL_3_CHAN,chanSize[0],chanSize[1]);
        }
*/
    }

    /**
     * Create a script engine.  Use Class.forName to avoid direct linking.  Will issue
     * a warning to the console if it fails.
     *
     * @param name The script engine to create
     * @param vpManager The Viewpoint manager to use
     * @param universe The universe to use
     * @param routeManager The route manager to use
     * @param stateManger The state manager to use
     * @param worldLoader The loader manager to use
     */
     public void createScriptEngine(String name, OGLStandardBrowserCore universe,
        ViewpointManager vpManager, RouteManager routeManager, FrameStateManager stateManager,
        WorldLoaderManager worldLoader) {

        Class scriptClass;
        Object[] paramTypes;
        Object[] constParams1 = new Object[] {universe, routeManager, stateManager, worldLoader};
        Object[] constParams2 = new Object[] {universe, vpManager, routeManager, stateManager, worldLoader};
        ScriptEngine script = null;
        boolean found = false;

        try {
            scriptClass = Class.forName(name);

            Constructor[] consts = scriptClass.getConstructors();

            for(int i = 0; i < consts.length; i++) {
                paramTypes = consts[i].getParameterTypes();
                if(paramTypes.length == constParams1.length) {
                    script = (ScriptEngine) consts[i].newInstance(constParams1);

                    script.setErrorReporter(console);
                    scriptLoader.registerScriptingEngine(script);

                    found = true;
                    break;
                } else if(paramTypes.length == constParams2.length) {
                    script = (ScriptEngine) consts[i].newInstance(constParams2);

                    script.setErrorReporter(console);
                    scriptLoader.registerScriptingEngine(script);

                    found = true;
                    break;
                }
            }

            if (!found) {
                console.warningReport("Cannot start " + name + " Scripting engine", null);
            }
        } catch(Exception e) {
            console.warningReport("Cannot start " + name + " Scripting engine", null);
        }
    }

    /**
     * Override addNotify so we know we have peer before calling setEnabled for Aviatrix3D.
     */
    public void addNotify() {
        super.addNotify();

        addNotifyHandled = true;
        sceneManager.setEnabled(true);
    }


    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param argv The list of arguments for this application.
     */
    public static void main(String[] args) {
        int lastUsed = -1;

        for(int i = 0; i < args.length; i++) {
            if(args[i].startsWith("-")) {
                if(args[i].equals("-fullscreen")) {
                    fullscreen = 0;
                    lastUsed = i;

                    try {
                        String val = args[i+1];
                        fullscreen = Integer.valueOf(val).intValue();
                        lastUsed = i + 1;
                    } catch(Exception e) {}
                } else if(args[i].equals("-screenSize")) {
                    lastUsed = i;

                    screenSize = new int[2];

                    try {
                        String val = args[i+1];
                        screenSize[0] = Integer.valueOf(val).intValue();
                        lastUsed = i + 1;

                        screenSize[1] = Integer.valueOf(val).intValue();
                        lastUsed = i + 1;
                    } catch(Exception e) {
                        System.out.println("Invalid screen size");

                        screenSize = null;
                    }
                } else if(args[i].equals("-stereo")) {
                    stereo = true;
                    String val = args[++i];
                    if (val.equalsIgnoreCase("quad")) {
                        stereoMode = GraphicsOutputDevice.QUAD_BUFFER_STEREO;
                    } else if (val.equalsIgnoreCase("alternate")) {
                        stereoMode = GraphicsOutputDevice.ALTERNATE_FRAME_STEREO;
                    } else {
                        System.out.println("Unknown stereo mode: " + val);
                    }

                    lastUsed = i;
                } else if(args[i].equals("-help")) {
                    System.out.println(USAGE_MSG);
                    return;
                } else if(args[i].equals("-spherical")) {
                    sphericalMode = true;
                    lastUsed = i;
                } else if (args[i].equals("-useMipMaps")) {
                    String val = args[++i];
                    useMipMaps = Boolean.valueOf(val).booleanValue();
                    lastUsed = i;
                } else if (args[i].equals("-useImageLoader")) {
                    String val = args[++i];
                    boolean new_val = Boolean.valueOf(val).booleanValue();

                    if (new_val != useImageLoader) {
                        System.out.println("Using native image loaders");
                    }

                    useImageLoader = new_val;

                    if (useImageLoader == true) {
                        // TODO: Need to single thread image loader right now as it crashes otherwise
                        //System.setProperty("org.xj3d.core.loading.threads", "1");
                    }


                    lastUsed = i;
                } else if (args[i].equals("-anisotropicDegree")) {
                    String val = args[++i];
                    anisotropicDegree = Integer.valueOf(val).intValue();
                    if (anisotropicDegree > 1)
                        useMipMaps = true;
                    lastUsed = i;
                } else if (args[i].equals("-antialias")) {
                    String val = args[++i];
                    desiredSamples = Integer.valueOf(val).intValue();
                    lastUsed = i;
                } else if (args[i].equals("-noredirect")) {
                    System.out.println("Redirect canceled");
                    redirect = false;
                    lastUsed = i;
                } else if (args[i].equals("-numChannels")) {
                    String val = args[++i];
                    numChannels = Integer.valueOf(val).intValue();
                    lastUsed = i;
                } else if (args[i].equals("-zbuffer")) {
                    String val = args[++i];
                    numZBits = Integer.valueOf(val).intValue();
                    lastUsed = i;
                } else if (args[i].equals("-numCpus")) {
                    String val = args[++i];
                    numCpus = Integer.valueOf(val).intValue();
                    lastUsed = i;
                } else if (args[i].equals("-nice")) {
                    lastUsed = i;
                    nice = true;
                } else if (args[i].equals("-captureViewpoints")) {
                    lastUsed = i;
                    captureViewpoints = true;
                } else if (args[i].startsWith("-")) {
                    System.out.println("Unknown flag: " + args[i]);
                    lastUsed = i;
                }
            }
        }

        Xj3DBrowser browser = new Xj3DBrowser();

        if (redirect)
            browser.redirectSystemMessages();

        // The last argument is the filename parameter
        String filename = null;

        if((args.length > 0) && (lastUsed + 1 < args.length)) {
            filename = args[args.length - 1];

            browser.loadURL(filename);
        }

        //browser.setVisible(true);

        // Display FPS, use framestate manager when updated
        while(true) {
            try {
                Thread.sleep(100);
            } catch(Exception e) {}

/*
            if (!firstFocused) {
                canvas = browser.getCanvas();
                canvas.requestFocus();
                firstFocused = canvas.isFocusOwner();
            }
*/

        }
    }
}
