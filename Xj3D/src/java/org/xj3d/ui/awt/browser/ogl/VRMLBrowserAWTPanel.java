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

package org.xj3d.ui.awt.browser.ogl;

// External imports
import java.awt.*;
import org.ietf.uri.*;

import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.media.opengl.GLCapabilities;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;

// Local imports
import vrml.eai.Browser;
import vrml.eai.VrmlComponent;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.VRML97ResourceFactory;

import org.web3d.browser.BrowserComponent;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.ErrorReporter;

import org.web3d.vrml.lang.VRMLNodeFactory;

import org.web3d.vrml.nodes.VRMLLinkNodeType;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;

import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.OGLLayerManagerFactory;

import org.web3d.vrml.sav.InputSource;

import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.eai.EAIBrowser;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.ViewpointManager;

import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoader;
import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.ui.awt.device.AWTDeviceFactory;

import org.xj3d.ui.awt.widgets.*;

import org.xj3d.sai.BrowserConfig;

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
 * @version $Revision: 1.9 $
 */
public class VRMLBrowserAWTPanel extends Panel
    implements VrmlComponent,
               BrowserComponent,
               ComponentListener,
               WindowListener {

    /** Framerate for paused mode */
    private static final int PAUSED_FPS = 1;

    /** The Browser instance this is the display for */
    private EAIBrowser eaiBrowser;

    /** The real component that is being rendered to */
    private Canvas glCanvas;

    /** The canvas used to display the world */
    private OGLBrowserCanvas mainCanvas;

    /** The universe to place our scene into */
    private OGLStandardBrowserCore universe;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The Label to show the description text on */
    private Label descriptionLabel;

    /** The Label to show the current URL text */
    private Label urlLabel;

    /** Area to push error messages to */
    private AWTConsoleWindow console;

    /** The frame cycle interval set, -1 if unset */
    private int frameCycleTime;

    /**
     * Create a VrmlComponent that belongs to an AWT panel.
     * and in that process construct the
     *  corresponding Browser, and the infrastructure required.
     *
     * @param parameters The object containing the browser's configuration parameters
     */
    public VRMLBrowserAWTPanel( BrowserConfig parameters ) {
        super(new BorderLayout());

        setSize(800, 600);

        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);
        SimpleAWTSurface surface = new SimpleAWTSurface(caps);

        glCanvas = (Canvas)surface.getSurfaceObject();

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            glCanvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console );

        mainCanvas = new OGLBrowserCanvas(surface, deviceFactory, parameters);
        mainCanvas.initialize();
        mainCanvas.setErrorReporter(console);

        glCanvas.addComponentListener(this);
        //glCanvas.addKeyListener(this);

        descriptionLabel = new Label();
        urlLabel = new Label();

        add(glCanvas, BorderLayout.CENTER);

        // Atleast humor the idea of parameters.
        if (parameters.showDash) {
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

        VRMLNodeFactory fac;
        /** This code was originally trying to use the browser ID, which doesn't work
         * when trying to substitute 'aviatrix3d' for 'ogl' in the profile loading.
         * Manually map between the two and then force the VRML97 profile since
         * the factory will only be used for replaceWorld scene building.
         */
        switch (universe.getRendererType()) {
        case Xj3DConstants.JAVA3D_RENDERER:
            fac=DefaultNodeFactory.newInstance(DefaultNodeFactory.JAVA3D_RENDERER);
            break;
        case Xj3DConstants.OPENGL_RENDERER:
            fac=DefaultNodeFactory.newInstance(DefaultNodeFactory.OPENGL_RENDERER);
            break;
        default:
            fac=DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);
            break;
        }
        fac.setSpecVersion(2,0);
        fac.setProfile("VRML97");

        VRML97CommonBrowser browser_impl =
            new VRML97CommonBrowser(universe,
                                    route_manager,
                                    state_manager,
                                    worldLoader,
                                    fac);

        browser_impl.setErrorReporter(console);

        ExternalEventQueue eventQueue = new ExternalEventQueue(console);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        eaiBrowser = new EAIBrowser(universe,
                                    browser_impl,
                                    eventQueue,
                                    console);

        mainCanvas.setErrorReporter(console);

        if(parameters.showConsole)
            console.setVisible(true);
    }

    //-----------------------------------------------------------------------
    // Methods defined by VRMLComponent
    //-----------------------------------------------------------------------

    /**
     * Return the vrml.eai.Browser object which corresponds to this VrmlComponent,
     * as required by the specification.
     * @return The vrml.eai.Browser object associated with this VrmlComponent
     */
    public Browser getBrowser() {
        return eaiBrowser;
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
        return glCanvas;
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
        frameCycleTime = millis;
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

    //----------------------------------------------------------
    // Methods defined by ComponentListener
    //----------------------------------------------------------

    /**
     * Invoked when the component has been made invisible.
     *
     * @param evt The event
     */
    public void componentHidden(ComponentEvent evt) {
        mainCanvas.setMinimumFrameInterval(1000 / PAUSED_FPS, false);
    }

    /**
     * Invoked when the component's position changes.
     *
     * @param evt The event
     */
    public void componentMoved(ComponentEvent evt) {
    }

    /**
     * Invoked when the component's size changes.
     *
     * @param evt The event
     */
    public void componentResized(ComponentEvent evt) {
        // Reget the parent each time as it might have changed.  Que changes
        // by resize, correct?
        Container cnt = this.getParent();
        Container tmpCnt;

        while(true) {
            tmpCnt = cnt.getParent();
            if (tmpCnt == null)
                break;

            cnt = tmpCnt;
        }

        ((Window)cnt).addWindowListener(this);
    }

    /**
     * Invoked when the component has been made visible.
     *
     * @param evt The event
     */
    public void componentShown(ComponentEvent evt) {
        if (frameCycleTime < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameCycleTime, false);
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
     * Ignored
     */
    public void windowClosing(WindowEvent evt) {
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt) {
    }

    /**
     * Invoked when a window is changed from a minimized to a normal state.
     *
     * @param evt The window event.
     */
    public void windowDeiconified(WindowEvent evt) {
        if (frameCycleTime < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameCycleTime, false);
    }

    /**
     * Invoked when a window is changed from a normal state to minimzed.
     *
     * @param evt The window event.
     */
    public void windowIconified(WindowEvent evt) {
        mainCanvas.setMinimumFrameInterval(1000 / PAUSED_FPS, false);
    }

    /**
     * Ignored
     */
    public void windowOpened(WindowEvent evt) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
    public OGLStandardBrowserCore getUniverse() {
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

    /**
     * Override addNotify so we know we have peer before calling setEnabled for Aviatrix3D.
     */
    public void addNotify() {
        super.addNotify();

        mainCanvas.setEnabled(true);
    }
}
