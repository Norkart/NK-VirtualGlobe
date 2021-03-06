/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.swt.browser.ogl;

// External imports
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.part.ViewPart;

import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

// Local imports
import org.web3d.browser.BrowserCore;

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

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.ViewpointManager;

import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.impl.core.loading.FramerateThrottle;

import org.xj3d.sai.BrowserConfig;

/**
 * SWT implementation that wraps the functionality of a X3D browser
 * into a convenient, easy to use form for the SAI.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.11 $
 */
public class X3DBrowserComposite extends BrowserComposite
    implements X3DComponent {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in X3DBrowserComposite";

    /** The SAIBrowser instance */
    private SAIBrowser saiBrowser;

    /**
     * Create an instance of the panel configured per the arguments.
     *
     * @param parentComposite The SWT Composite widget that this will be added to
     * @param viewPart The Eclipse ViewPart that this browser is associated with.
     * May be null if the browser is running standalone in SWT.
     * @param parameters The object containing the browser's configuration parameters
     */
    public X3DBrowserComposite(
        Composite parentComposite,
        ViewPart viewPart,
        BrowserConfig parameters ) {

        super(
            parentComposite,
            viewPart,
            parameters );

        setupProperties( );

        RouteManager route_manager = mainCanvas.getRouteManager( );
        FrameStateManager state_manager = mainCanvas.getFrameStateManager( );
        WorldLoaderManager wlm = mainCanvas.getWorldLoaderManager( );
        ViewpointManager vp_manager = mainCanvas.getViewpointManager( );

        ScriptManager sm = mainCanvas.getScriptManager( );
        ScriptLoader scriptLoader = sm.getScriptLoader( );

        ScriptEngine java_sai = new JavaSAIScriptEngine(
            browserCore,
            vp_manager,
            route_manager,
            state_manager,
            wlm );
        java_sai.setErrorReporter( messageBuffer );

        ScriptEngine ecma = new ECMAScriptEngine(
            browserCore,
            vp_manager,
            route_manager,
            state_manager,
            wlm );
        ecma.setErrorReporter( messageBuffer );

        scriptLoader.registerScriptingEngine( java_sai );
        scriptLoader.registerScriptingEngine( ecma );

        X3DCommonBrowser browser_impl = new X3DCommonBrowser(
            browserCore,
            vp_manager,
            route_manager,
            state_manager,
            wlm);
        browser_impl.setErrorReporter( messageBuffer );

        ExternalEventQueue eventQueue = new ExternalEventQueue( messageBuffer );
        mainCanvas.getEventModelEvaluator( ).addExternalView( eventQueue );

        saiBrowser = new SAIBrowser(
            browserCore,
            browser_impl,
            route_manager,
            state_manager,
            eventQueue,
            cursorManager,
            messageBuffer);

        if (locToolbar != null) {
            FramerateThrottle frameThrottle = new FramerateThrottle( browserCore, messageBuffer );
            frameThrottle.setScriptLoader( scriptLoader );
            frameThrottle.setLoadManager( mainCanvas.getContentLoadManager( ) );
            locToolbar.setThrottle( frameThrottle );
        }
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
    public void shutdown( ) {
        try {
            // the external browser instance can be
            // explicitly disposed by the user, without
            // our knowledge. calling dispose a second
            // time will throw an exception - javadoc
            // says we're suppose to quitely ignore
            // repeated calls.

            saiBrowser.dispose( );

        } catch ( Exception e ) {
        }
        if ( browserCore != null ) {
            browserCore.dispose( );
        }
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
                new PrivilegedExceptionAction() {
                    public Object run() {
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
            messageBuffer.warningReport(PROPERTY_SETUP_ERR, null);
        }
    }
}
