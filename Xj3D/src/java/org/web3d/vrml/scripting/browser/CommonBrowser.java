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

package org.web3d.vrml.scripting.browser;

// External imports
import java.util.Map;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.util.URLChecker;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * The common parts of a browser implementation that all the scripting
 * interfaces require.
 * <P>
 *
 * This class is a partial implementation that is then extended for
 * version-specific functionality. There are major functional differences
 * between VRML97 and X3D and require that some methods be implemented
 * differently. For example createVrmlFromURL is asynchronous in VRML97
 * by synchronous in X3D and therefore not implemented in this base
 * class. Look to the derived classes for a complete implementation.
 * <p>
 *
 * The current implementation ignores any parameter values provided by the
 * world.
 *
 * @author Justin Couch
 * @version $Revision: 1.20 $
 */
public abstract class CommonBrowser {

    /** The name of this browser */
    private final String BROWSER_NAME;

    /** The current version number */
    private static final String BROWSER_VERSION = Xj3DConstants.VERSION;

    /** The basic browser core functionality that this script hooks to */
    protected BrowserCore core;

    /** Route manager for dealing with add/remove ROUTE methods. */
    protected RouteManager routeManager;

    /** Loader manager for doing aysnc loads */
    protected WorldLoaderManager loaderManager;

    /** The frame state manager for nodes loaded by this class */
    protected FrameStateManager stateManager;

    /** The ErrorReporter for messages */
    protected ErrorReporter errorReporter;

    /**
     * Create a browser instance that represents the given universe details.
     * If the scene builder or factory is null, then it will find one from
     * the global pool defined for the renderer used by the browser core.
     *
     * @param browser The core representation of the browser
     * @param rm A route manager for users creating/removing routes
     * @param wlm Loader manager for doing async calls
     * @param fsm State manager for coordinating inter-frame processing
     * @throws IllegalArgumentException A paramter is null
     */
    protected CommonBrowser(BrowserCore browser,
                            RouteManager rm,
                            FrameStateManager fsm,
                            WorldLoaderManager wlm) {

        if(browser == null)
            throw new IllegalArgumentException("BrowserCore is null");

        if(rm == null)
            throw new IllegalArgumentException("RouteManager is null");

        if(wlm == null)
            throw new IllegalArgumentException("WorldLoader is null");

        if(fsm == null)
            throw new IllegalArgumentException("FrameStateManager is null");

        core = browser;
        routeManager = rm;
        stateManager = fsm;
        loaderManager = wlm;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        switch(browser.getRendererType()) {
            case Xj3DConstants.JAVA3D_RENDERER:
                BROWSER_NAME = "Xj3D (Java3D renderer)";
                break;

            case Xj3DConstants.NULL_RENDERER:
                BROWSER_NAME = "Xj3D (Null renderer)";
                break;

            case Xj3DConstants.OPENGL_RENDERER:
                BROWSER_NAME = "Xj3D (OpenGL renderer)";
                break;

            case Xj3DConstants.MOBILE_RENDERER:
                BROWSER_NAME = "Xj3D (OpenGL mobile renderer)";
                break;

            default:
                BROWSER_NAME = "Xj3D (Unknown renderer)";
        }
    }

    /**
     * Get the description string currently used by the world. Returns null if
     * not set or supported.
     *
     * @return The current description string or null
     */
    public String getDescription() {
        return core.getDescription();
    }

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param desc The description string to set.
     */
    public void setDescription(String desc) {
        core.setDescription(desc);
    }

    /**
     * Get the name of the browser. The name is an implementation specific
     * string representing the browser.
     *
     * @return The name of the browser or null if not supported
     */
    public String getName() {
        return BROWSER_NAME;
    }

    /**
     * Get the version of the browser. Returns an implementation specific
     * representation of the version number.
     *
     * @return The version of the browser or null if not supported
     */
    public String getVersion() {
        return BROWSER_VERSION;
    }

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     */
    public float getCurrentSpeed() {
        return core.getCurrentSpeed();
    }

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     */
    public float getCurrentFrameRate() {
        return core.getCurrentFrameRate();
    }

    /**
     * Get the fully qualified URL of the currently loaded world. This returns
     * the entire URL including any possible arguments that might be associated
     * with a CGI call or similar mechanism. If the initial world is replaced
     * with <CODE>loadURL</CODE> then the string will reflect the new URL. If
     * <CODE>replaceWorld</CODE> is called then the URL still represents the
     * original world.
     *
     * @return A string of the URL or null if not supported.
     * @see #loadURL(String[], Map)
     */
    public String getWorldURL() {
        return core.getWorldURL();
    }

    /**
     * Add a route between two nodes, from an eventOut to an eventIn. If the
     * ROUTE already exists, this method silently exits. It does not attempt
     * to add a second parallel ROUTE.
     *
     * @param execSpace The space that this route takes place in
     * @param fromNode The source node for the route
     * @param fromEventOut The eventOut source of the route
     * @param toNode The destination node of the route
     * @param toEventIn The eventIn destination of the route
     * @throws InvalidFieldException the eventIn or eventOut name is not a
     *   field of the src/destination node
     */
    public void addRoute(VRMLExecutionSpace execSpace,
                         VRMLNodeType fromNode,
                         String fromEventOut,
                         VRMLNodeType toNode,
                         String toEventIn)
        throws InvalidFieldException {

        int src_index = fromNode.getFieldIndex(fromEventOut);
        int dest_index = toNode.getFieldIndex(toEventIn);

        routeManager.addRoute(execSpace,
                              fromNode,
                              src_index,
                              toNode,
                              dest_index);
    }

    /**
     * Delete a route between two nodes. If the route does not exist, the
     * method silently exits.
     *
     * @param execSpace The space that this route takes place in
     * @param fromNode The source node for the route
     * @param fromEventOut The eventOut source of the route
     * @param toNode The destination node of the route
     * @param toEventIn The eventIn destination of the route
     * @throws InvalidFieldException the eventIn or eventOut name is not a
     *   field of the src/destination node
     */
    public void deleteRoute(VRMLExecutionSpace execSpace,
                            VRMLNodeType fromNode,
                            String fromEventOut,
                            VRMLNodeType toNode,
                            String toEventIn)
        throws InvalidFieldException {

        int src_index = fromNode.getFieldIndex(fromEventOut);
        int dest_index = toNode.getFieldIndex(toEventIn);

        routeManager.removeRoute(execSpace,
                                 fromNode,
                                 src_index,
                                 toNode,
                                 dest_index);
    }

    /**
     * Load the URL as the new root of the scene. Replaces all the current
     * scene graph with the new world. A non-blocking call that will change the
     * contents at some time in the future.
     * <P>
     * Generates an immediate SHUTDOWN event and then when the new contents are
     * ready to be loaded, sends an INITIALIZED event.
     *
     * @param url The list of URLs in decreasing order of preference as defined
     *   in the VRML97 specification.
     * @param parameters The list of parameters to accompany the load call as
     *   defined in the Anchor node specification of VRML97
     * @throws VRMLException General error during processing
     */
    public void loadURL(String[] url, Map parameters) {

        String worldURL = core.getWorldURL();

        url = completeUrl(url);

        loaderManager.queueLoadURL(url, parameters);
    }

    /**
     * Register an error reporter with the CommonBrowser instance
     * so that any errors generated can be reported in a nice manner.
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Insure that a url is a fully qualified URL.
     * Uses the core.worldURL or the local user directory if nothing is loaded.
     *
     * @param base The base url
     * @return A fully qualified URL
     */
    protected String[] completeUrl(String[] base) {
        String worldURL = core.getWorldURL();
        String[] urls = null;
        String url_base = null;

        if (worldURL == null) {
            try {
                url_base=(String)AccessController.doPrivileged(
                        new PrivilegedExceptionAction () {
                            public Object run() {
                                return System.getProperty("user.dir");
                            }
                        });
            } catch (PrivilegedActionException pae) {
                System.out.println("Error getting user.dir");
            }

            String complete_url = "file:///" + url_base + "/";

            urls = URLChecker.checkURLs(complete_url, base, false);
        } else {
            urls = URLChecker.checkURLs(worldURL, base, false);
        }

        return urls;
    }
}
