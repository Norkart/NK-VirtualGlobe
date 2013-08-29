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

package org.web3d.vrml.scripting.jsai;

// External imports
import java.util.WeakHashMap;

// Local imports
import vrml.Browser;
import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.FrameStateManager;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * A factory implementation for creating and caching specific instances of
 * the script {@link vrml.Browser} interface.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
class VRML97ScriptBrowserFactory {

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** The basic browser core functionality that this script hooks to */
    private BrowserCore core;

    /** Route manager for dealing with add/remove ROUTE methods */
    private RouteManager routeManager;

    /** Mapping of execution spaces to browser instances */
    private WeakHashMap browsers;

    /** The LoadManager passed to the parser */
    private WorldLoaderManager loadManager;

    /** FrameState manager for creating nodes */
    private FrameStateManager stateManager;

    /**
     * Create a factory that represents the given universe details.
     *
     * @param browser The core representation of the browser
     * @param rm A route manager for users creating/removing routes
     * @param fsm State manager for coordinating inter-frame processing
     * @param wlm Loader for full files
     * @throws IllegalArgumentException Any one of the parameters is null
     */
    VRML97ScriptBrowserFactory(BrowserCore browser,
                               RouteManager rm,
                               FrameStateManager fsm,
                               WorldLoaderManager wlm) {

        core = browser;
        routeManager = rm;
        loadManager = wlm;
        stateManager = fsm;

        browsers = new WeakHashMap();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Create or fetch the browser instance of the browser interface that
     * corresponds to the given execution space.
     *
     * @param space The execution space we need a browser for
     * @return A corresponding {@link vrml.Browser} instance
     */
    public Browser getBrowser(VRMLExecutionSpace space) {
        Browser browser = (Browser)browsers.get(space);

        if(browser == null) {
            browser = new ScriptBrowser(space,
                                        core,
                                        routeManager,
                                        stateManager,
                                        loadManager);

            ((ScriptBrowser)browser).setErrorReporter(errorReporter);
            browsers.put(space, browser);
        }

        return browser;
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }
}

