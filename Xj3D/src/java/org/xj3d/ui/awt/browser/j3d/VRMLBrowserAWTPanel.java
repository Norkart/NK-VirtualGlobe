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
 *****************************************************************************/

package org.xj3d.ui.awt.browser.j3d;

// External imports
import java.awt.*;

import org.ietf.uri.*;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Canvas3D;

// Local imports
import org.web3d.browser.*;

import vrml.eai.Browser;
import vrml.eai.VrmlComponent;
import org.xj3d.device.keyboard.KeyboardDevice;
import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.VRML97ResourceFactory;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLLinkNodeType;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.renderer.j3d.browser.J3DStandardBrowserCore;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.eai.EAIBrowser;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;

import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.SensorManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoader;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * A brower that uses the AWT panel and labels to draw render the
 * UI with.
 * <P>
 *
 * VRMLBrowserAWTPanel is the AWT based alternative to BrowserJPanel.
 * At the moment, it offers minimal functionality.
 * The "dashboard" is a text label, there aren't any navigation functions.
 * That, and urlReadOnly, urlTop, and dashTop are ignored.
 *
 * @author Brad Vender, Justin Couch
 * @version $Revision: 1.6 $
 */
public class VRMLBrowserAWTPanel extends Panel
    implements VrmlComponent,
               BrowserComponent {

    /** The Browser instance this is the display for */
    private EAIBrowser eaiBrowser;

    /** The canvas used to display the world */
    private VRMLBrowserCanvas mainCanvas;

    /** The universe to place our scene into */
    private J3DStandardBrowserCore universe;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The Label to show the description text on */
    private Label descriptionLabel;

    /** The Label to show the current URL text */
    private Label urlLabel;

    /** Area to push error messages to */
    private AWTConsoleWindow console;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param showDash true to show the navigation bar
     * @param dashTop true to put the nav bar at the top
     * @param showUrl true to show the URL location bar
     * @param urlTop true to put the location bar at the top
     * @param urlReadOnly true to make the location bar read only
     * @param showConsole true if the console should be shown immediately
     */
    public VRMLBrowserAWTPanel(boolean showDash,
                           boolean dashTop,
                           boolean showUrl,
                           boolean urlTop,
                           boolean urlReadOnly,
                           boolean showConsole) {

        this(true, showDash, dashTop, showUrl, urlTop, urlReadOnly, showConsole);
    }

    /**
     * Create a VrmlComponent that belongs to an AWT panel.
     * and in that process construct the
     *  corresponding Browser, and the infrastructure required.
     *
     * @param vrml97Only true if this is to be restricted to VRML97 only
     * @param showDash true to show the navigation bar
     * @param dashTop true to put the nav bar at the top
     * @param showUrl true to show the URL location bar
     * @param urlTop true to put the location bar at the top
     * @param urlReadOnly true to make the location bar read only
     * @param showConsole true if the console should be shown immediately
     */
    public VRMLBrowserAWTPanel(boolean vrml97Only,
                           boolean showDash,
                           boolean dashTop,
                           boolean showUrl,
                           boolean urlTop,
                           boolean urlReadOnly,
                           boolean showConsole) {
        super(new BorderLayout());

        setSize(800, 600);

        //vrml97Only = false;

        /** Make the panel */
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();

        //template.setDoubleBuffer(template.REQUIRED);
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();
        GraphicsConfiguration gfxConfig = dev.getBestConfiguration(template);

        GraphicsConfiguration gfx_cfg = dev.getBestConfiguration(template);
        mainCanvas = new VRMLBrowserCanvas(gfx_cfg, vrml97Only);

        mainCanvas.initialize();

        descriptionLabel = new Label();
        urlLabel = new Label();
        add(mainCanvas,BorderLayout.CENTER);

        // Atleast humor the idea of parameters.
        if (showDash) {
            add(descriptionLabel,BorderLayout.SOUTH);
            add(urlLabel,BorderLayout.NORTH);
        }

        RouteManager route_manager = mainCanvas.getRouteManager();
        universe = mainCanvas.getUniverse();

        FrameStateManager state_manager = mainCanvas.getFrameStateManager();
        worldLoader = mainCanvas.getWorldLoaderManager();

        ScriptManager sm = mainCanvas.getScriptManager();
        ScriptLoader s_loader = sm.getScriptLoader();

        console = new AWTConsoleWindow();

        // Register all the other bits. Set up scripting engines next....
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

        s_loader.registerScriptingEngine(jsai);
        s_loader.registerScriptingEngine(ecma);

        setupProperties(universe, worldLoader);

        // Determine the renderer in use to get a node factory.



        VRML97CommonBrowser browser_impl =
            new VRML97CommonBrowser(universe,
                                    route_manager,
                                    state_manager,
                                    worldLoader,
                                    universe.getNodeFactory());
        browser_impl.setErrorReporter(console);

        ExternalEventQueue eventQueue = new ExternalEventQueue(console);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        eaiBrowser = new EAIBrowser(universe,
                                    browser_impl,
                                    eventQueue,
                                    console);

        mainCanvas.setErrorReporter(console);

        if(showConsole)
            console.setVisible(true);
        eaiBrowser.initializeWorld();
    }

    //----------------------------------------------------------
    // Methods defined by BrowserComponent
    //----------------------------------------------------------

    /**
     * Get the spec version that is supported.
     *
     * @return a number representing the spec major version
     */
    public int supportedSpecificationVersion() {
        return 3;
    }

    /**
     * Get the AWT component holding this browser.
     *
     * @return The component
     */
    public Object getCanvas() {
        return mainCanvas;
    }

    /**
     * Get the renderer type.
     *
     * @return The BrowserCore type
     */
    public int getRendererType() {
        return universe.getRendererType();
    }

    /**
     * Get the core browser implementation.
     *
     * @return the BrowserCore
     */
    public BrowserCore getBrowserCore() {
        return universe;
    }

    /**
     * Fetch the error handler so that application code can post messages
     * too.
     *
     * @return The current error handler instance
     */
    public ErrorReporter getErrorReporter() {
        return console;
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
        mainCanvas.setMinimumFrameInterval(millis, userSet);
    }

    /**
     * Called to instruct the component instance to start rendering now.
     */
    public void start() {
        mainCanvas.setEnabled(true);
    }

    /**
     * Called to instruct the component instance to stop and suspend its state.
     * The renderer should stop at this point.
     */
    public void stop() {
        mainCanvas.setEnabled(false);
    }

    /**
     * Called to instruct the component instance to destroy itself and any
     * used resources. It will not be used again.
     */
    public void destroy() {
        mainCanvas.setEnabled(false);
        mainCanvas.browserShutdown();
    }

    //-----------------------------------------------------------------------
    // Methods defined by VrmlComponent
    //-----------------------------------------------------------------------

    /**
     * Return the vrml.eai.Browser object which corresponds to this VrmlComponent,
     * as required by the specification.
     * @return The vrml.eai.Browser object associated with this VrmlComponent
     */
    public Browser getBrowser() {
        return eaiBrowser;
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Get the scene object being rendered by this panel.
     *
     * @return The current scene.
     */
    public VRMLScene getScene() {
        return universe.getScene();
    }

    /**
     * Get the universe underlying this panel.
     *
     * @return The universe.
     */

    public J3DStandardBrowserCore getUniverse() {
        return universe;
    }

    /**
     * Change the panels content to the provided URL.
     *
     * @param url The URL to load.
     * @throws IOException On a failed load
     */
    public void loadURL(String url) throws IOException {
        WorldLoader wl = worldLoader.fetchLoader();
        InputSource source = new InputSource(url);

        VRMLScene scene = wl.loadNow(universe, source, false, 2, 0);
        universe.setScene(scene, null);
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    private void setupProperties(final BrowserCore core,
                                 final WorldLoaderManager wlm) {
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction () {
                    public Object run() {
                        String prop = System.getProperty("uri.content.handler.pkgs","");
                        if (prop.indexOf("vlc.net.content") == -1) {
                            System.setProperty("uri.content.handler.pkgs",
                                "vlc.net.content");
                        }

                        prop = System.getProperty("uri.protocol.handler.pkgs","");
                        if (prop.indexOf("vlc.net.protocol") == -1) {
                            System.setProperty("uri.protocol.handler.pkgs",
                                "vlc.net.protocol");
                        }
/*
System.out.println("Setting up to use contenloaders");
                        prop = System.getProperty("uri.protocol.handler.pkgs","");
                        if (prop.indexOf("vlc.content") == -1) {
                                System.setProperty("java.content.handler.pkgs",
                                    "vlc.content");
                        }
*/

                        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                        if(!(res_fac instanceof VRML97ResourceFactory)) {
                            res_fac = new VRML97ResourceFactory(res_fac);
                            URI.setURIResourceStreamFactory(res_fac);
                        }

                        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();

                        if(!(c_fac instanceof VRMLContentHandlerFactory)) {
                            c_fac = new VRMLContentHandlerFactory(core, wlm);
                            URI.setContentHandlerFactory(c_fac);
                        }

                        FileNameMap fn_map = URI.getFileNameMap();
                        if(!(fn_map instanceof VRMLFileNameMap)) {
                            fn_map = new VRMLFileNameMap(fn_map);
                            URI.setFileNameMap(fn_map);
                        }
                        return null;
                    }
                }
            );
        } catch (PrivilegedActionException pae) {
            System.out.println("Error setting Properties in BrowserJPanel");
        }
    }
}
