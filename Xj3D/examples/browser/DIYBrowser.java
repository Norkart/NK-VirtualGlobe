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
import java.awt.*;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.ietf.uri.*;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.input.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;
import org.xj3d.impl.core.eventmodel.*;

import org.web3d.browser.Xj3DConstants;
import org.web3d.browser.SensorStatusListener;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;
import org.web3d.vrml.renderer.j3d.J3DSceneBuilderFactory;
import org.web3d.vrml.renderer.j3d.browser.OverlayHandler;
import org.web3d.vrml.renderer.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.renderer.j3d.input.J3DPickingManager;
import org.web3d.vrml.renderer.j3d.input.DefaultPickingManager;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DViewpointNodeType;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;

import org.xj3d.ui.awt.widgets.SwingNavigationToolbar;
import org.xj3d.ui.awt.widgets.SwingViewpointToolbar;

/**
 * A demonstration application that shows how to put together all of the
 * Xj3D toolkit into a browser application.
 * <p>
 * This is by no means a fully complete browser, it is just a demonstration
 * of what can be done. We expect that you will take this code and rip it
 * apart and put it into your own application.
 *
 * @author Justin Couch
 * @version $Revision: 1.69 $
 */
public class DIYBrowser extends DemoFrame
    implements SensorStatusListener,
               OverlayHandler,
               Runnable {

    /** The universe to place our scene into */
    private J3DStandardBrowserCore universe;

    /** The toolbar holding viewpoint information */
    private SwingViewpointToolbar vpToolbar;

    /** The toolbar holding navigation information */
    private SwingNavigationToolbar navToolbar;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** The graphics config template that is best to use */
    private GraphicsConfiguration gfxConfig;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** Place for error messages to go */
    private ConsoleWindow console;

    /** Global clock */
    private VRMLClock clock;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The global canvas for rendering */
    private Canvas3D canvas;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    /**
     * Create an instance of the demo class.
     */
    public DIYBrowser() {
        super("DIY VRML Browser");

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        viewpointDefMap = new HashMap();

        J3DSceneBuilderFactory builder_fac =
            new J3DSceneBuilderFactory(false,
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

        Container content_pane = getContentPane();

        JPanel p1 = new JPanel(new BorderLayout());
        content_pane.add(p1, BorderLayout.CENTER);

        console = new ConsoleWindow();

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        gfxConfig = dev.getBestConfiguration(template);
        // We also need a canvas to display stuff with and a universe to set
        // the content in.
        canvas = new Canvas3D(gfxConfig);
        View view = new View();

        view.setMinimumFrameCycleTime(30);
        view.addCanvas3D(canvas);

//        ContentLoadManager lm = new SimpleLoadManager();
        ContentLoadManager load_manager = new MemCacheLoadManager();
        ScriptLoader script_loader = new DefaultScriptLoader();
        ScriptManager script_manager = new DefaultScriptManager();
        script_manager.setScriptLoader(script_loader);

        FrameStateManager state_manager = new DefaultFrameStateManager();

        J3DPickingManager picker_manager = new DefaultPickingManager();
        picker_manager.setErrorReporter(console);

        J3DSensorManager sensor_manager = new DefaultSensorManager();
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
        universe = new J3DStandardBrowserCore(eventModel, true, this);
        universe.setPrimaryView(view);
        universe.addSensorStatusListener(this);

        worldLoader = new DefaultWorldLoaderManager(universe,
                                                    state_manager,
                                                    route_manager);
        worldLoader.setErrorReporter(console);
        worldLoader.registerBuilderFactory(Xj3DConstants.JAVA3D_RENDERER,
                                           builder_fac);
        worldLoader.registerParserFactory(Xj3DConstants.JAVA3D_RENDERER,
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
                                                        canvas,
                                                        canvas);
        sensor_manager.setInputManager(idm);

/*
        // If using the canvas AWTEventListener handler then comment out
        // these lines.
        BranchGroup extra_behaviors = new BranchGroup();
        extra_behaviors.addChild(i_buf);
        universe.addSceneGraphExtras(extra_behaviors);
*/
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

        p1.add(canvas, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout());
        p1.add(p2, BorderLayout.SOUTH);

        navToolbar = new SwingNavigationToolbar(universe, console);
        p2.add(navToolbar, BorderLayout.WEST);

        vpToolbar = new SwingViewpointToolbar(universe, console);

        p2.add(vpToolbar, BorderLayout.CENTER);

        setupProperties(universe, worldLoader);

        console.setVisible(true);

        DownloadProgressListener dl_list =
            new DownloadProgressListener(statusLabel, console);

        ResourceConnection.addGlobalProgressListener(dl_list);

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));
    }

    //----------------------------------------------------------
    // Methods required by the OverlayHandler interface.
    //----------------------------------------------------------

    /**
     * Fetch the canvas that will be responsible for having the overlays
     * composited on them.
     *
     * @return The canvas instance to use
     */
    public Canvas3D getPrimaryCanvas() {
        return canvas;
    }

    //----------------------------------------------------------
    // Methods required by the SensorStatusListener interface.
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
                    success = true;
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
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    private void shutdownApp()
    {
        canvas.stopRenderer();
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

        if(vp_list.size() == 0) {
            return ret_val;

        VRMLViewpointNodeType active_vp = universe.getViewpoint();
        currentViewpoint = active_vp;
        ViewpointData active_data = null;
        J3DVRMLNode node;
        ViewpointData[] data = new ViewpointData[vp_list.size()];
        int count = 0;
        String desc;
        TransformGroup tg;
        int size = vp_list.size();

        for(int i = 0; i < size; i++) {
            node = (J3DVRMLNode)vp_list.get(i);

            if(node.getPrimaryType() == TypeConstants.ProtoInstance)
                node = (J3DVRMLNode)((VRMLProtoInstance)node).getImplementationNode();

            desc = ((VRMLViewpointNodeType)node).getDescription();

            if((desc == null) || (desc.length() == 0)) {
                desc = "Viewpoint " + count;
            }

            tg = ((J3DViewpointNodeType)node).getPlatformGroup();

            data[count] = new J3DViewpointData(desc, count, tg);
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
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param argv The list of arguments for this application.
     */
    public static void main(String[] argv) {
        DIYBrowser browser = new DIYBrowser();
        browser.show();

        String filename = null;

        if (argv.length >= 1) {
            filename = argv[0];
        }

        if (filename != null) {
            File fil = new File(filename);
            try {
                if (fil.exists()) {
                    browser.gotoLocation(fil);
                } else {
                    try {
                        URL url = new URL(filename);
                        browser.gotoLocation(url);
                    } catch(MalformedURLException mfe) {
                        System.out.println("Malformed URL: " + filename);
                    }
                }

            } catch(Exception e) {
                System.out.println("Error in parsing file.");
                e.printStackTrace();
            }
        }
    }
}
