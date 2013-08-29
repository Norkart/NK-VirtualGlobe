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
import java.awt.Container;
import java.awt.Component;
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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import javax.media.opengl.GLCapabilities;

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
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;
import org.web3d.x3d.jaxp.X3DSAVAdapter;

import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;

import org.xj3d.ui.awt.widgets.SwingNavigationToolbar;
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
 * @version $Revision: 1.36 $
 */
public class XSLTViewer extends DemoFrame
    implements WindowListener,
               SensorStatusListener,
               Runnable {

    /** Name of the property to set the lexical handler in the XMLReader */
    private static final String LEXICAL_HANDLER_PROP =
    "http://xml.org/sax/properties/lexical-handler";

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The universe to place our scene into */
    private OGLStandardBrowserCore universe;

    /** The toolbar holding viewpoint information */
    private SwingViewpointToolbar vpToolbar;

    /** The toolbar holding navigation information */
    private SwingNavigationToolbar navToolbar;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Place for error messages to go */
    private ConsoleWindow console;

    /** Global clock */
    private VRMLClock clock;

    /** Factory for creating new scenes */
    private OGLSceneBuilderFactory builderFactory;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    /** Manager for external loading of content */
    private ContentLoadManager loadManager;

    /** The script content handler */
    private ScriptLoader scriptLoader;

    /** The stylesheet file name extracted from the command line */
    private static String xsltFile;

    /** The factory to generate SAX parser instances */
    private SAXParserFactory parserFactory;

    /** Common entity resolver instance */
    private X3DEntityResolver resolver;

    /** SAX Error handler for the system */
    private X3DErrorHandler x3dErrors;

    /** Debugging setup */
    private static boolean debug = false;

    /**
     * Create an instance of the demo class.
     */
    public XSLTViewer() {
        super("OpenGL/Aviatrix VRML & X3D Browser");
        addWindowListener(this);
        setSize(800, 600);
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

        navToolbar = new NavigationToolbar();
        p2.add(navToolbar, BorderLayout.WEST);

        vpToolbar = new ViewpointToolbar();

        p2.add(vpToolbar, BorderLayout.CENTER);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));

        console.setVisible(true);
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

//        CullStage culler = new NullCullStage();
        CullStage culler = new SimpleFrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

//        SortStage sorter = new NullSortStage();
//        SortStage sorter = new SimpleTransparencySortStage();
//        SortStage sorter = new DepthSortedTransparencyStage();
        SortStage sorter = new StateAndTransparencyDepthSortStage();
//        SortStage sorter = new StateSortStage();

        surface = new DebugAWTSurface(caps);
//        surface = new SimpleAWTSurface(caps);
//        surface = new StereoAWTSurface(caps);
//        surface.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);

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

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.disableInternalShutdown();
        sceneManager.addPipeline(pipeline);
        sceneManager.setGraphicsOutputDevice(surface);
        sceneManager.setMinimumFrameInterval(50);

// Currently causing lockups on exit sometimes.
//        sceneManager.addPipeline(audioPipeline);
//        sceneManager.setAudioOutputDevice(adevice);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        panel.add(comp, BorderLayout.CENTER);


        builderFactory = new OGLSceneBuilderFactory(false,
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

        worldLoader = new DefaultWorldLoaderManager(universe,
                                                    state_manager,
                                                    route_manager);
        worldLoader.setErrorReporter(console);
        worldLoader.registerBuilderFactory(Xj3DConstants.OPENGL_RENDERER,
                                           builderFactory);
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
                               loadManager,
                               vp_manager,
                               node_mgrs);
        eventModel.setErrorReporter(console);

        InputDeviceManager idm =
            new InputDeviceManager(universe.getIDString(), comp, surface);

        sensor_manager.setInputManager(idm);
        comp.addKeyListener(idm);

        clock = universe.getVRMLClock();

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

        panel.add(comp, BorderLayout.CENTER);

        //universe.addNavigationStateListener(navToolbar);
        universe.addSensorStatusListener(this);

        setupProperties(universe, worldLoader);

        console.setVisible(true);

        DownloadProgressListener dl_list =
            new DownloadProgressListener(statusLabel, console);

        ResourceConnection.addGlobalProgressListener(dl_list);
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

        VRMLScene parsed_scene = null;

        try {
            SceneBuilder sb = builderFactory.createBuilder();

            X3DSAVAdapter adap = new X3DSAVAdapter();

            // Need a better way todo this
            String path = System.getProperty("user.dir");
            path = "file://" + path.substring(3);

            adap.setLoadState(path, "XSLT", true);

            try {
                parserFactory = SAXParserFactory.newInstance();
            } catch(javax.xml.parsers.FactoryConfigurationError fce) {
                throw new FactoryConfigurationError("No SAX parser defined");
            }

            x3dErrors = new X3DErrorHandler();
            resolver = new X3DEntityResolver();

            parserFactory.setValidating(false);
            parserFactory.setNamespaceAware(true);

            org.xml.sax.XMLReader reader = null;

            try {
                SAXParser parser = parserFactory.newSAXParser();
                reader = parser.getXMLReader();
                reader.setContentHandler(adap);
                reader.setProperty(LEXICAL_HANDLER_PROP, adap);
                reader.setErrorHandler(x3dErrors);
                reader.setEntityResolver(resolver);
            } catch(Exception e) {
                throw new IOException("Unable to configure factory as required");
            }

            // Convert our InputSource, to their InputSource....
            org.xml.sax.InputSource xis = new org.xml.sax.InputSource();
            xis.setCharacterStream(is.getCharacterStream());
            xis.setEncoding(is.getEncoding());

            Source xmlSource = new SAXSource(reader,xis);

            File transform = new File(xsltFile);
            Source xsltSource = new StreamSource(transform);
            Result outres=null;
            if (debug)
                outres = new StreamResult(System.out);

            SAXResult result = new SAXResult(adap);

            adap.setContentHandler(sb);
            adap.setProtoHandler(sb);
            adap.setRouteHandler(sb);
            adap.setScriptHandler(sb);

            // create an instance of TransformerFactory
            TransformerFactory transFact = TransformerFactory.newInstance( );

            Transformer trans = transFact.newTransformer(xsltSource);

            if (!debug)
                System.out.println("Transforming input to X3D");

            if (debug) {
                trans.transform(xmlSource, outres);
                System.exit(0);
            }
            else
                trans.transform(xmlSource, result);

            if (!debug)
                System.out.println("Transformation complete");
            parsed_scene = sb.getScene();

        } catch(Exception e) {
            System.out.println("Exception in transforming file: " + e.getMessage());
            e.printStackTrace();
        }

        if (parsed_scene == null)
            return false;

        universe.setScene(parsed_scene, null);

        ret_val = true;

        // Grab the list of viewpoints and place them into the toolbar.
        ArrayList vp_list =
            parsed_scene.getByPrimaryType(TypeConstants.ViewpointNodeType);

        if(vp_list.size() == 0)
            return ret_val;

        VRMLViewpointNodeType active_vp = universe.getViewpoint();
        currentViewpoint = active_vp;
        ViewpointData active_data = null;
        OGLVRMLNode node;
        ViewpointData[] data = new ViewpointData[vp_list.size()];
        int count = 0;
        String desc;
        TransformGroup tg;
        int size = vp_list.size();

        for(int i = 0; i < size; i++) {
            node = (OGLVRMLNode)vp_list.get(i);

            if(node.getPrimaryType() == TypeConstants.ProtoInstance)
                node = (OGLVRMLNode)((VRMLProtoInstance)node).getImplementationNode();

            desc = ((VRMLViewpointNodeType)node).getDescription();

            if((desc == null) || (desc.length() == 0)) {
                desc = "Viewpoint " + count;
            }

            tg = ((OGLViewpointNodeType)node).getPlatformGroup();

            data[count] = new AVViewpointData(desc, count, tg);
            data[count].userData = node;

            if(node == active_vp)
                active_data = data[count];

            count++;
        }

        vpToolbar.setViewpoints(data);
        if(active_data != null)
            vpToolbar.selectViewpoint(active_data);

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

        statusLabel.setText("World Loaded Successfully");
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
     * @param args The list of arguments for this application.
     */
    public static void main(String[] args) {

        String filename = null;
        boolean dispUsage=true;

        if (args.length > 0) {
            for(int i=0; i < args.length; i++) {
                if (args[i].equals("-debug")) {
                    debug = true;
                }
                else {
                    if (xsltFile == null) {
                        xsltFile = args[i];
                        dispUsage = false;
                    } else {
                        filename = args[i];
                    }
                }
            }
        }

        if (dispUsage) {
            System.out.println("Usage XSLTViewer [OPTION] TRANSFORMER [SOURCE]");
            System.out.println("\n  -debug          Display the transformed document instead of viewing in 3D");
        } else {
            XSLTViewer viewer = new XSLTViewer();
            viewer.show();

            if (filename.startsWith("http://")) {
                try {
                    URL url = new URL(filename);
                    viewer.gotoLocation(url);
                } catch(MalformedURLException mfe) {
                   System.out.println("Malformed URL: " + filename);
                }
            } else {
                File fil = new File(filename);

                viewer.gotoLocation(fil);
            }
        }
    }
}
