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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

// Local imports
import vrml.eai.Browser;
import vrml.eai.VrmlComponent;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;
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

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.LayerManager;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.impl.core.loading.FramerateThrottle;

import org.xj3d.sai.BrowserConfig;

/**
 * Swing JPanel implementation that wraps the functionality of a VRML browser
 * into a convenient, easy to use form for the EAI.
 * <p>
 *
 * This panel is designed for use by the EAI and therefore will, by default,
 * restrict the content to VRML97 rather than X3D.
 *
 * @author Brad Vender
 * @version $Revision: 1.9 $
 */
public class VRMLBrowserJPanel extends BrowserJPanel implements VrmlComponent {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in VRMLBrowserJPanel";

    /** The Browser instance this is the display for */
    private EAIBrowser eaiBrowser;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param parameters The object containing the browser's configuration parameters
     */
    public VRMLBrowserJPanel(BrowserConfig parameters) {
        super(parameters);

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

        VRMLNodeFactory fac;
        /** This code was originally trying to use the browser ID, which doesn't work
         * when trying to substitute 'aviatrix3d' for 'ogl' in the profile loading.
         * Manually map between the two and then force the VRML97 profile since
         * the factory will only be used for replaceWorld scene building.
         */
        switch (universe.getRendererType()) {
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

        if (locToolbar != null) {
            FramerateThrottle frameThrottle = new FramerateThrottle(universe, console);
            frameThrottle.setScriptLoader(s_loader);
            frameThrottle.setLoadManager(mainCanvas.getContentLoadManager());
            locToolbar.setThrottle(frameThrottle);
        }
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
                         BrowserCore core = mainCanvas.getUniverse();
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
