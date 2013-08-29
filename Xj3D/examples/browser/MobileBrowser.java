/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
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
import java.net.URL;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import gl4java.GLCapabilities;
import gl4java.GLContext;
import gl4java.awt.GLAnimCanvas;
import gl4java.drawable.GLDrawableFactory;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;
import org.xj3d.impl.core.eventmodel.*;

import org.web3d.browser.BrowserCore;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.LinkSelectionListener;
import org.web3d.vrml.renderer.common.input.NavigationStateListener;
import org.web3d.vrml.renderer.mobile.MobileSceneBuilderFactory;
import org.web3d.vrml.renderer.mobile.browser.MobileUniverse;
import org.web3d.vrml.renderer.mobile.browser.PerFrameManager;
import org.web3d.vrml.renderer.mobile.input.DefaultSensorManager;
import org.web3d.vrml.renderer.mobile.input.MobileSensorManager;
import org.web3d.vrml.renderer.mobile.nodes.MobileViewpointNodeType;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.sg.SGManager;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;

/**
 * A demonstration application that shows how to put together all of the
 * Xj3D toolkit into a browser application using the Mobile renderer.
 * <p>
 *
 * The simple browser does not respond to changes in the list of viewpoints
 * in the virtual world. This is OK because scripts are not used or needed in
 * this simple environment. Once we implement scripts, we have to look at
 * something different.
 *
 * @author Alan Hudson
 * @version $Revision: 1.20 $
 */
public class MobileBrowser extends DemoFrame
    implements LinkSelectionListener, Runnable {

    /** The universe to place our scene into */
    private MobileUniverse universe;

    /** The scene manager used for this node */
    private SGManager sceneManager;

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

    /** The GL drawable for this window */
    private GLAnimCanvas canvas;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    /**
     * Create an instance of the demo class.
     */
    public MobileBrowser() {
        super("Mobile DIY VRML Browser");

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        viewpointDefMap = new HashMap();

        MobileSceneBuilderFactory builder_fac =
            new MobileSceneBuilderFactory(false,
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

        // We also need a canvas to display stuff with and a universe to set
        // the content in.
        if(!GLContext.doLoadNativeLibraries(null, null, null)) {
            System.out.println("Unable to load native library");
            throw new RuntimeException();
        }

        GLCapabilities caps = new GLCapabilities();
        GLDrawableFactory fac = GLDrawableFactory.getFactory();
        canvas = fac.createGLAnimCanvas(caps, 100, 100);

        sceneManager = new SGManager(canvas);

        InputDeviceManager idm = new InputDeviceManager(canvas);

//        ContentLoadManager lm = new SimpleLoadManager();
        ContentLoadManager load_manager = new MemCacheLoadManager();
        ScriptLoader script_loader = new DefaultScriptLoader();
        ScriptManager script_manager = new DefaultScriptManager();
        script_manager.setScriptLoader(script_loader);

        FrameStateManager state_manager = new DefaultFrameStateManager();

        MobileSensorManager sensor_manager = new DefaultSensorManager();
        sensor_manager.setInputManager(idm);


        RouteManager route_manager = new DefaultRouteManager();
//        route_manager.setRouterFactory(new SimpleRouterFactory());
        route_manager.setRouterFactory(new ListsRouterFactory());

        eventModel = new DefaultEventModelEvaluator();
        universe = new MobileUniverse(eventModel, sceneManager);
        universe.setLinkSelectionListener(this);

        worldLoader = new DefaultWorldLoaderManager(universe,
                                                    state_manager,
                                                    route_manager);
        worldLoader.setErrorReporter(console);
        worldLoader.registerBuilderFactory(BrowserCore.MOBILE_RENDERER,
                                           builder_fac);
        worldLoader.registerParserFactory(BrowserCore.MOBILE_RENDERER,
                                          parser_fac);

        DefaultHumanoidManager hanim_manager = new DefaultHumanoidManager();
        NetworkManager network_manager = new DefaultNetworkManager();
        DISProtocolHandler dis_handler = new DISProtocolHandler();
        network_manager.addProtocolHandler(dis_handler);

        NodeManager[] node_mgrs = { network_manager, hanim_manager };

        ViewpointManager vp_manager = new DefaultViewpointManager(universe);

        eventModel.initialize(script_manager,
                               route_manager,
                               sensor_manager,
                               state_manager,
                               load_manager,
                               vp_manager,
                               node_mgrs);
        eventModel.setErrorReporter(console);

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

//        universe.setNavigationStateListener(navToolbar);
        universe.setLinkSelectionListener(this);

        setupProperties(universe, worldLoader);

        console.setVisible(true);

        DownloadProgressListener dl_list =
            new DownloadProgressListener(statusLabel, console);

        ResourceConnection.addGlobalProgressListener(dl_list);

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));
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

    /**
     * When the browser is shown, start the canvas
     */
    public void setVisible(boolean shown) {
        super.setVisible(shown);

        if(shown) {
            canvas.repaint();
            canvas.start();
        } else {
            canvas.stop();
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
        try {
            MobileBrowser browser = new MobileBrowser();
            browser.setVisible(true);
        } catch(RuntimeException re) {
            re.printStackTrace();
        }
    }
}
