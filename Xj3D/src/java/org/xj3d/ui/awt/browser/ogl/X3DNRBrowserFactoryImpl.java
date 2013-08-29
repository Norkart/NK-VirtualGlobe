/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import java.io.*;

import java.applet.Applet;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Properties;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Properties;

import javax.media.opengl.GLCapabilities;

import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.FileNameMap;
import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;

// Local imports
import org.web3d.browser.*;
import org.web3d.x3d.sai.*;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.X3DResourceFactory;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLLinkNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.browser.X3DCommonBrowser;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIBrowser;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoader;
import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.sai.BrowserConfig;

/**
 * Factory implementation for X3D SAI which will produce components using
 * the Null renderer.
 *
 * <b>Component Creation</b>
 * <p>
 *
 * This implementation allows you to create a new component that is ready to
 * place content in. Parameters can be supplied in the Map as defined by
 * the SAI. The first column is the parameter name string, the second is
 * the type of data, and the third is an explanation.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class X3DNRBrowserFactoryImpl
    implements BrowserFactoryImpl,
               X3DComponent
{

    /** Error message for passing a null String in loadURL */
    private static final String NULL_PARAMETER_ERROR = "Null parameter strings not allowed.";

    /** Error message for malformed parameter String in loadURL */
    private static final String MALFORMED_PARAMETER_STRING_ERROR =
        "Malformed parameter string."+
        "  Expecting strings of the form A=B";

    /** The canvas used to display the world */
    private OGLBrowserCanvas mainCanvas;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** Mapping of def'd Viewpoints to their real implementation */
    private HashMap viewpointDefMap;

    /** The SAIBrowser instance */
    private SAIBrowser saiBrowser;

    /** Global clock */
    private VRMLClock clock;

    /** The internal universe */
    private OGLStandardBrowserCore universe;

    /**
     * Create a new instance of this factory.
     */
    public X3DNRBrowserFactoryImpl() {
    }

    /**
     * @see org.web3d.x3d.sai.BrowserFactoryImpl#createComponent(java.util.Map)
     */
    public X3DComponent createComponent(Map params)
        throws NotSupportedException {

        return new X3DNRBrowserFactoryImpl(true);
    }

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @ignore This is needed to allow private construction
     */
    private X3DNRBrowserFactoryImpl (boolean ignore) {
        //GLCapabilities caps = new GLCapabilities();
        //caps.setDoubleBuffered(true);
        //caps.setHardwareAccelerated(true);
        //SimpleAWTSurface surface = new SimpleAWTSurface(caps);

        mainCanvas = new OGLBrowserCanvas(null, null, new BrowserConfig( ));

        mainCanvas.initialize();
        mainCanvas.setMinimumFrameInterval(20, false);

        RouteManager route_manager = mainCanvas.getRouteManager();
        ViewpointManager vp_manager = mainCanvas.getViewpointManager();
        universe = mainCanvas.getUniverse();

        FrameStateManager state_manager = mainCanvas.getFrameStateManager();
        worldLoader = mainCanvas.getWorldLoaderManager();

        ScriptManager sm = mainCanvas.getScriptManager();
        ScriptLoader s_loader = sm.getScriptLoader();

        ScriptEngine java_sai = new JavaSAIScriptEngine(universe,
                                                        vp_manager,
                                                        route_manager,
                                                        state_manager,
                                                        worldLoader);
        ScriptEngine ecma = new ECMAScriptEngine(universe,
                                                 vp_manager,
                                                 route_manager,
                                                 state_manager,
                                                 worldLoader);
        s_loader.registerScriptingEngine(java_sai);
        s_loader.registerScriptingEngine(ecma);

        setupProperties(universe, worldLoader);

        X3DCommonBrowser browser_impl =
            new X3DCommonBrowser(universe,
                                 vp_manager,
                                 route_manager,
                                 state_manager,
                                 worldLoader);

        ExternalEventQueue eventQueue=new ExternalEventQueue(null);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        saiBrowser = new SAIBrowser(universe,
                                    browser_impl,
                                    route_manager,
                                    state_manager,
                                    eventQueue,
                                    null,
                                    null);

        clock = universe.getVRMLClock();

        mainCanvas.setEnabled(true);
    }

    /**
     * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.applet.Applet)
     */
    public ExternalBrowser getBrowser(Applet applet)
        throws NotSupportedException, NoSuchBrowserException, ConnectionException {
        throw new NotSupportedException();
    }

    /**
     * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.applet.Applet,
     *      java.lang.String, int)
     */
    public ExternalBrowser getBrowser(
        Applet applet,
        String frameName,
        int index)
        throws NotSupportedException, NoSuchBrowserException, ConnectionException {
        throw new NotSupportedException();
    }

    /**
     * @see org.web3d.x3d.sai.BrowserFactoryImpl#getBrowser(java.net.InetAddress,
     *      int)
     */
    public ExternalBrowser getBrowser(InetAddress address, int port)
        throws
            NotSupportedException,
            NoSuchBrowserException,
            UnknownHostException,
            ConnectionException {
        throw new NotSupportedException();
    }

    //-----------------------------------------------------------------------
    // Methods defined by X3DComponent
    //-----------------------------------------------------------------------

    /**
     * @see org.web3d.x3d.sai.X3DComponent#getBrowser()
     */
    public ExternalBrowser getBrowser() {
        return saiBrowser;
    }

    /**
     * @see org.web3d.x3d.sai.X3DComponent#getImplementation()
     */
    public Object getImplementation() {
        return this;
    }

    /**
     * @see org.web3d.x3d.sai.X3DComponent#shutdown()
     */
    public void shutdown() {
        saiBrowser.dispose();
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
     * @throws IOException On a failed load or badly formatted URL
     */
    public void loadURL(String url) throws IOException {
        URL real_url = new URL(url);

        WorldLoader wl = worldLoader.fetchLoader();
        InputSource source = new InputSource(url);

        VRMLScene scene = wl.loadNow(universe, source);
        universe.setScene(scene, real_url.getRef());
    }

    /**
     * Change the panels content to the provided URL.
     *
     * @param src the source to load the content from
     * @throws IOException On a failed load or badly formatted URL
     */
    public void loadURL(InputSource src) throws IOException {

        WorldLoader wl = worldLoader.fetchLoader();

        VRMLScene scene = wl.loadNow(universe, src);

        URL url = new URL(src.getBaseURL());
        universe.setScene(scene, url.getRef());
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
                         if(!(res_fac instanceof X3DResourceFactory)) {
                             res_fac = new X3DResourceFactory(res_fac);
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
