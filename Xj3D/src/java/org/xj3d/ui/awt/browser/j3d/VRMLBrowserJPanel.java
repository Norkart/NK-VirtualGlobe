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
 ****************************************************************************/

package org.xj3d.ui.awt.browser.j3d;

// External imports
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

// Local imports
import vrml.eai.Browser;
import vrml.eai.VrmlComponent;

import org.web3d.net.protocol.VRML97ResourceFactory;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.eai.EAIBrowser;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.ScriptLoader;

/**
 * Swing JPanel implementation that wraps the functionality of a VRML browser
 * into a convenient, easy to use form for the EAI.
 * <p>
 *
 * This panel is designed for use by the EAI and therefore will, by default,
 * restrict the content to VRML97 rather than X3D. An alternate constructor
 * allows the use of non VRML97 content.
 *
 * @author Brad Vender
 * @version $Revision: 1.3 $
 */
public class VRMLBrowserJPanel extends BrowserJPanel
    implements VrmlComponent {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in VRMLBrowserJPanel";

    /** The Browser instance this is the display for */
    private EAIBrowser eaiBrowser;

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
     * @param showOpenButton true to put an open button with the URL location bar
     * @param showReloadButton true to put a reload button with the URL location bar
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param contentDirectory initial directory to load content from.  Must be a full path.
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     */
    public VRMLBrowserJPanel(boolean showDash,
                             boolean dashTop,
                             boolean showUrl,
                             boolean urlTop,
                             boolean urlReadOnly,
                             boolean showConsole,
                             boolean showOpenButton,
                             boolean showReloadButton,
                             boolean showStatusBar,
                             boolean showFPS,
                             boolean antialiased,
                             String contentDirectory,
                             String antialiasingQuality,
                             String primitiveQuality,
                             String textureQuality) {

        this(showDash,
             dashTop,
             showUrl,
             urlTop,
             urlReadOnly,
             showConsole,
             showOpenButton,
             showReloadButton,
             showStatusBar,
             showFPS,
             contentDirectory,
             antialiased,
             antialiasingQuality,
             primitiveQuality,
             textureQuality,
             null);
    }

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
     * @param skinProperties The properties object to configure appearance with
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param contentDirectory initial directory to load content from.  Must be a full path.
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     * @param skinProperties Customisation of the browser buttons etc
     */
    public VRMLBrowserJPanel(boolean showDash,
                             boolean dashTop,
                             boolean showUrl,
                             boolean urlTop,
                             boolean urlReadOnly,
                             boolean showConsole,
                             boolean showOpenButton,
                             boolean showReloadButton,
                             boolean showStatusBar,
                             boolean showFPS,
                             String contentDirectory,
                             boolean antialiased,
                             String antialiasingQuality,
                             String primitiveQuality,
                             String textureQuality,
                             Properties skinProperties) {
        super(showDash,
              dashTop,
              showUrl,
              urlTop,
              urlReadOnly,
              showConsole,
              showOpenButton,
              showReloadButton,
              showStatusBar,
              showFPS,
              contentDirectory,
              antialiased,
              antialiasingQuality,
              primitiveQuality,
              textureQuality,
              skinProperties);

        setupProperties();

        RouteManager route_manager = mainCanvas.getRouteManager();
        FrameStateManager state_manager = mainCanvas.getFrameStateManager();
        WorldLoaderManager wlm = mainCanvas.getWorldLoaderManager();
        ViewpointManager vp_manager = mainCanvas.getViewpointManager();

        ScriptManager sm = mainCanvas.getScriptManager();
        ScriptLoader s_loader = sm.getScriptLoader();

        ScriptEngine jsai = new VRML97ScriptEngine(universe,
                                                   route_manager,
                                                   state_manager,
                                                   wlm);
        jsai.setErrorReporter(console);

        ScriptEngine ecma = new JavascriptScriptEngine(universe,
                                                       route_manager,
                                                       state_manager,
                                                       wlm);
        ecma.setErrorReporter(console);

        s_loader.registerScriptingEngine(jsai);
        s_loader.registerScriptingEngine(ecma);

        VRMLNodeFactory fac =
            DefaultNodeFactory.newInstance(universe.getIDString());

        VRML97CommonBrowser browser_impl =
            new VRML97CommonBrowser(universe,
                                    route_manager,
                                    state_manager,
                                    wlm,
                                    fac);
        browser_impl.setErrorReporter(console);

        ExternalEventQueue eventQueue = new ExternalEventQueue(console);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        eaiBrowser = new EAIBrowser(universe,
                                    browser_impl,
                                    eventQueue,
                                    console);

        eaiBrowser.initializeWorld();
    }

    //-----------------------------------------------------------------------
    // Methods defined by VrmlComponent
    //-----------------------------------------------------------------------

    /**
     * Return the {@link vrml.eai.Browser} object which corresponds to this
     * VrmlComponent, as required by the specification.
     *
     * @return The vrml.eai.Browser object associated with this VrmlComponent
     */
    public Browser getBrowser() {
        return eaiBrowser;
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     */
    private void setupProperties() {
         try {
             AccessController.doPrivileged(
                 new PrivilegedExceptionAction () {
                     public Object run() {
                         BrowerCore core = mainCanvas.getUniverse();
                         WorldLoaderManager wlm =
                            mainCanvas.getWorldLoaderManager();

                         URIResourceStreamFactory res_fac =
                            URI.getURIResourceStreamFactory();
                         if(!(res_fac instanceof VRML97ResourceFactory)) {
                             res_fac = new VRML97ResourceFactory(res_fac);
                             URI.setURIResourceStreamFactory(res_fac);
                         }

                         return null;
                     }
                 }
             );
         } catch (PrivilegedActionException pae) {
             console.warningReport(PROPERTY_SETUP_ERR, null);
         }
    }
}
