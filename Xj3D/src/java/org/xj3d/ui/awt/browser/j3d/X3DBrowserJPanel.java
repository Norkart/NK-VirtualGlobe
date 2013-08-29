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
import org.web3d.net.protocol.X3DResourceFactory;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.browser.X3DCommonBrowser;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIBrowser;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DScene;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * Swing JPanel implementation that wraps the functionality of a X3D browser
 * into a convenient, easy to use form for the SAI.
 * <p>
 *
 * @author Justin Couch, Brad Vender, Alan Hudson
 * @version $Revision: 1.3 $
 */
public class X3DBrowserJPanel extends BrowserJPanel
    implements X3DComponent {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in X3DBrowserJPanel";

    /** The SAIBrowser instance */
    private SAIBrowser saiBrowser;

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
    public X3DBrowserJPanel(boolean showDash,
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

        this(false,
             showDash,
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
     * @param vrml97Only true if this is to be restricted to VRML97 only
     * @param showDash true to show the navigation bar
     * @param dashTop true to put the nav bar at the top
     * @param showUrl true to show the URL location bar
     * @param urlTop true to put the location bar at the top
     * @param urlReadOnly true to make the location bar read only
     * @param showConsole true if the console should be shown immediately
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param contentDirectory initial directory to load content from.  Must be a full path.
     * @param antialiased true to turn on antialiasing
     * @param antialiasingQuality low, medium, high, antialiasing must be turned on for this to matter.
     * @param primitiveQuality low, medium, high.
     * @param textureQuality low, medium, high.
     */
    public X3DBrowserJPanel(boolean vrml97Only,
                            boolean showDash,
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
                            String textureQuality) {

        this(vrml97Only,
             showDash,
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
     * @param vrml97Only true if this is to be restricted to VRML97 only
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
    public X3DBrowserJPanel(boolean vrml97Only,
                            boolean showDash,
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

        ScriptEngine java_sai = new JavaSAIScriptEngine(universe,
                                                        vp_manager,
                                                        route_manager,
                                                        state_manager,
                                                        wlm);
        java_sai.setErrorReporter(console);

        ScriptEngine ecma = new ECMAScriptEngine(universe,
                                                 vp_manager,
                                                 route_manager,
                                                 state_manager,
                                                 wlm);
        ecma.setErrorReporter(console);

        s_loader.registerScriptingEngine(java_sai);
        s_loader.registerScriptingEngine(ecma);

        X3DCommonBrowser browser_impl =
            new X3DCommonBrowser(universe,
                                 vp_manager,
                                 route_manager,
                                 state_manager,
                                 wlm);
        browser_impl.setErrorReporter(console);

        ExternalEventQueue eventQueue = new ExternalEventQueue(console);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        saiBrowser = new SAIBrowser(universe,
                                    browser_impl,
                                    route_manager,
                                    state_manager,
                                    eventQueue,
                                    console);
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
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The core representation of the browser
     * @param wlm Loader manager for doing async calls
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
                         if(!(res_fac instanceof X3DResourceFactory)) {
                             res_fac = new X3DResourceFactory(res_fac);
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
