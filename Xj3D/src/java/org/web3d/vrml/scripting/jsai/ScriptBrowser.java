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
import java.io.IOException;
import java.util.HashMap;

// Local imports
import vrml.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.parser.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.sav.VRMLReader;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * Browser object within a JSAI VRML97 script.
 * <P>
 *
 * The current implementation ignores any parameter values provided by the
 * world.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
class ScriptBrowser extends Browser {

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** Field factory to pass to the node instance */
    private JSAIFieldFactory fieldFactory;

    /** The execution space used during route management */
    private VRMLExecutionSpace execSpace;

    /** The real browser we are delegating the functionality to */
    private VRML97CommonBrowser realBrowser;

    /**
     * Create a browser instance that represents the given universe details.
     *
     * @param space The execution space we need a browser for
     * @param browser The core representation of the browser
     * @param rm A route manager for users creating/removing routes
     * @param fsm State manager for coordinating inter-frame processing
     * @param wlm Loader for full files
     * @throws IllegalArgumentException Any one of the parameters is null
     */
    ScriptBrowser(VRMLExecutionSpace space,
                  BrowserCore browser,
                  RouteManager rm,
                  FrameStateManager fsm,
                  WorldLoaderManager wlm) {

        BasicScene scene = space.getContainedScene();
        VRMLNodeFactory node_fac = scene.getNodeFactory();

        realBrowser = new VRML97CommonBrowser(browser, rm, fsm, wlm, node_fac);

        execSpace = space;

        fieldFactory = new JSAIFieldFactory();
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        realBrowser.setErrorReporter(errorReporter);
    }

    /**
     * Get the description string currently used by the world. Returns null if
     * not set or supported.
     *
     * @return The current description string or null
     */
    public String getDescription() {
        return realBrowser.getDescription();
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
        realBrowser.setDescription(desc);
    }

    /**
     * Get the name of the browser. The name is an implementation specific
     * string representing the browser.
     *
     * @return The name of the browser or null if not supported
     */
    public String getName() {
        return realBrowser.getName();
    }

    /**
     * Get the version of the browser. Returns an implementation specific
     * representation of the version number.
     *
     * @return The version of the browser or null if not supported
     */
    public String getVersion() {
        return realBrowser.getVersion();
    }

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     */
    public float getCurrentSpeed() {
        return realBrowser.getCurrentSpeed();
    }

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     */
    public float getCurrentFrameRate() {
        return realBrowser.getCurrentFrameRate();
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
     * @see #loadURL(String[], String[])
     * @see #replaceWorld(BaseNode[])
     */
    public String getWorldURL() {
        return realBrowser.getWorldURL();
    }

    /**
     * Replace the current world with the given nodes. Replaces the entire
     * contents of the VRML world with the new nodes. Any node references that
     * belonged to the previous world are still valid but no longer form part of
     * the scene graph (unless it is these nodes passed to this method). The
     * URL of the world still represents the just unloaded world.
     * <P>
     * Calling this method causes a SHUTDOWN event followed by an INITIALIZED
     * event to be generated.
     *
     * @param nodes The list of nodes to use as the new root of the world
     */
    public void replaceWorld(BaseNode[] nodes){
        String world_url = nodes[0].getBrowser().getWorldURL();

        int len = nodes.length;
        VRMLNodeType[] n = new VRMLNodeType[len];

        for(int i=0; i < len; i++) {
            n[i] = nodes[i].getImplNode();
        }

        realBrowser.replaceWorld(n);
    }

    /**
     * Parse the given string and turn this into a list of VRML nodes. Method
     * is a blocking call that won't return until all of the top level nodes
     * defined in the string have been returned.
     * <P>
     * At the point that this method returns, external files such as textures,
     * sounds and inlines may not have been loaded.
     * <P>
     * The string may contain all legal VRML syntax. The VRML header line is not
     * required to be present in the string.
     *
     * @param vrmlString The string containing VRML string syntax
     * @return A list of the top level nodes in VRML representation as defined
     *    in the parameter
     * @exception InvalidVRMLSyntaxException If the string does not contain legal
     *   VRML syntax or no node instantiations
     */
    public BaseNode[] createVrmlFromString(String vrmlSyntax)
        throws InvalidVRMLSyntaxException {

        BaseNode[] ret_val = null;

        try {
            VRMLNodeType[] real_nodes =
                realBrowser.createVrmlFromString(vrmlSyntax, execSpace);

            if((real_nodes == null) || (real_nodes.length == 0))
                throw new InvalidVRMLSyntaxException("No node declarations");

            ret_val = new BaseNode[real_nodes.length];

            for(int i = 0; i < real_nodes.length; i++)
                ret_val[i] = new JSAINode(real_nodes[i], this, fieldFactory);
        } catch(VRMLParseException vpe) {
            throw new InvalidVRMLSyntaxException(vpe.getMessage());
        } catch(VRMLException ve) {
            // maybe should throw a different exception.
            throw new InvalidVRMLSyntaxException(ve.getMessage());
        } catch(IOException ioe) {
            // maybe should throw a different exception.
            throw new InvalidVRMLSyntaxException(ioe.getMessage());
        }

        return ret_val;
    }

    /**
     * Create and load VRML from the given URL and place the returned values
     * as nodes into the given VRML node in the scene. The difference between
     * this and loadURL is that this method does not replace the entire scene
     * with the contents from the URL. Instead, it places the return values
     * as events in the nominated node and MFNode eventIn.
     *
     * @param url The list of URLs in decreasing order of preference as defined
     *   in the VRML97 specification.
     * @param node The destination node for the VRML code to be sent to.
     * @param eventIn The name of the MFNode eventIn to send the nodes to.
     * @exception InvalidVRMLSyntaxException If the string does not contain legal
     *   VRML syntax or no node instantiations
     */
    public void createVrmlFromURL(String[] url, BaseNode node, String eventIn)
        throws InvalidVRMLSyntaxException {

        VRMLNodeType src_node = node.getImplNode();

        try {
            realBrowser.createVrmlFromURL(url, src_node, eventIn);
        } catch(org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new InvalidEventInException("Unknown field: " + eventIn);
        }
    }

    /**
     * Add a route between two nodes, from an eventOut to an eventIn. If the
     * ROUTE already exists, this method silently exits. It does not attempt
     * to add a second parallel ROUTE.
     *
     * @param fromNode The source node for the route
     * @param eventOut The eventOut source of the route
     * @param to Node The destination node of the route
     * @param eventIn The eventIn destination of the route
     * @throws InvalidEventInException the eventIn name is not a field of
     *   the destination node
     * @throws InvalidEventOutException the eventOut name is not a field of
     *   the source node
     */
    public void addRoute(BaseNode fromNode,
                         String fromEventOut,
                         BaseNode toNode,
                         String toEventIn) {

        VRMLNodeType src_node = fromNode.getImplNode();
        VRMLNodeType dest_node = toNode.getImplNode();

        try {
            realBrowser.addRoute(execSpace,
                                 src_node,
                                 fromEventOut,
                                 dest_node,
                                 toEventIn);
        } catch(org.web3d.vrml.lang.InvalidFieldException ife) {
            // Strictly speaking, not correct. Must fix.
            throw new InvalidEventInException(ife.getMessage());
        }
    }

    /**
     * Delete a route between two nodes. If the route does not exist, the
     * method silently exits.
     *
     * @param fromNode The source node for the route
     * @param eventOut The eventOut source of the route
     * @param to Node The destination node of the route
     * @param eventIn The eventIn destination of the route
     * @throws InvalidEventInException the eventIn name is not a field of
     *   the destination node
     * @throws InvalidEventOutException the eventOut name is not a field of
     *   the source node
     */
    public void deleteRoute(BaseNode fromNode,
                            String fromEventOut,
                            BaseNode toNode,
                            String toEventIn) {

        VRMLNodeType src_node = fromNode.getImplNode();
        VRMLNodeType dest_node = toNode.getImplNode();

        try {
            realBrowser.deleteRoute(execSpace,
                                    src_node,
                                    fromEventOut,
                                    dest_node,
                                    toEventIn);
        } catch(org.web3d.vrml.lang.InvalidFieldException ife) {
            // Strictly speaking, not correct. Must fix.
            throw new InvalidEventInException(ife.getMessage());
        }
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
     * @param paramaters The list of parameters to accompany the load call as
     *   defined in the Anchor node specification of VRML97
     * @exception InvalidVRMLSyntaxException If the string does not contain legal
     *   VRML syntax or no node instantiations
     */
    public void loadURL(String[] url, String[] parameter)
        throws InvalidVRMLSyntaxException {

        // parse the parameter list and place the values into the map
        HashMap param_map = new HashMap();
        int num_params = (parameter == null) ? 0 : parameter.length;
        for(int i = 0; i < num_params; i++) {
            int eq_idx = parameter[i].indexOf('=');
            String key = parameter[i].substring(0, eq_idx);
            String value = parameter[i].substring(eq_idx + 1);
            param_map.put(key, value);
        }

        realBrowser.loadURL(url, param_map);
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.  Also changes the ErrorReporter
     * used by the CommonBrowser layer.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        // Pass the change in ErrorReporter on to the CommonBrowser since
        // the CommonBrowser is private to this instance.
        realBrowser.setErrorReporter(reporter);
    }
}
