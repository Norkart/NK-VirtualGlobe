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

package org.web3d.vrml.scripting.ecmascript;

// External imports
import java.io.IOException;
import java.util.HashMap;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;
import org.web3d.vrml.scripting.ecmascript.builtin.MFNode;
import org.web3d.vrml.scripting.ecmascript.builtin.MFString;
import org.web3d.vrml.scripting.ecmascript.builtin.SFNode;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * Browser object within an Javascript VRML97 script.
 * <P>
 *
 * The current implementation ignores any parameter values provided by the
 * world when requesting a loadURL.
 * <p>
 *
 * The standard Java interface does not allow us to print output. The way that
 * all VRML vendors seem to have gotten around this is to add
 * <code>println</code> methods to the Browser object. We've done this as
 * well. The current implementation just calls System.out directly, but a
 * later revision will probably use a callback for a listener provided
 * instance to send the output to. In addition, this class has to be made
 * public so that Rhino can access the various print methods. Note that the
 * constructor is not private because you should never directly be creating
 * instances of this class.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class JavascriptBrowser {

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** The execution space used during route management */
    private VRMLExecutionSpace execSpace;

    /** The real browser instance that we delegate a lot of the requests to */
    private VRML97CommonBrowser realBrowser;

    /**
     * Create a browser instance that represents the given universe details.
     *
     * @param space The execution space we need a browser for
     * @param browser The core representation of the browser
     * @param rm A route manager for users creating/removing routes
     * @param wlm Loader for full files
     * @param fsm State manager for coordinating inter-frame processing
     * @throws IllegalArgumentException Any one of the parameters is null
     */
    JavascriptBrowser(VRMLExecutionSpace space,
                      BrowserCore browser,
                      RouteManager rm,
                      FrameStateManager fsm,
                      WorldLoaderManager wlm) {

        BasicScene scene = space.getContainedScene();
        VRMLNodeFactory node_fac = scene.getNodeFactory();

        realBrowser = new VRML97CommonBrowser(browser, rm, fsm, wlm, node_fac);

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        execSpace = space;

        realBrowser.setErrorReporter(errorReporter);
    }

    //----------------------------------------------------------
    // Methods required Annex C Table C.1.
    //----------------------------------------------------------

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
     * @see #replaceWorld(MFNode)
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
    public void replaceWorld(MFNode nodes){
        realBrowser.replaceWorld(nodes.getRawData());
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
     * @param vrmlSyntax The string containing VRML string syntax
     * @return A list of the top level nodes in VRML representation as defined
     *    in the parameter
     * @exception InvalidVRMLSyntaxException If the string does not contain legal
     *   VRML syntax or no node instantiations
     */
    public MFNode createVrmlFromString(String vrmlSyntax) {
        MFNode ret_val = null;

        try {
            VRMLNodeType[] real_nodes =
                realBrowser.createVrmlFromString(vrmlSyntax, execSpace);

            ret_val = new MFNode(real_nodes);
        } catch(Exception e) {
            errorReporter.errorReport("Error parsing string", e);
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
     */
    public void createVrmlFromURL(MFString url, SFNode node, String eventIn) {
        try {
            realBrowser.createVrmlFromURL(url.getRawData(),
                                          node.getImplNode(),
                                          eventIn);
        } catch(InvalidFieldException ife) {
            if(errorReporter != null)
                errorReporter.errorReport(null, ife);
            else
                ife.printStackTrace();
        }
    }

    /**
     * Add a route between two nodes, from an eventOut to an eventIn. If the
     * ROUTE already exists, this method silently exits. It does not attempt
     * to add a second parallel ROUTE.
     *
     * @param fromNode The source node for the route
     * @param fromEventOut The eventOut source of the route
     * @param toNode The destination node of the route
     * @param toEventIn The eventIn destination of the route
     * @throws InvalidEventInException the eventIn name is not a field of
     *   the destination node
     * @throws InvalidEventOutException the eventOut name is not a field of
     *   the source node
     */
    public void addRoute(SFNode fromNode,
                         String fromEventOut,
                         SFNode toNode,
                         String toEventIn) {

        VRMLNodeType src_node = fromNode.getImplNode();
        VRMLNodeType dest_node = toNode.getImplNode();

        try {
            realBrowser.addRoute(execSpace,
                                 src_node,
                                 fromEventOut,
                                 dest_node,
                                 toEventIn);
        } catch(InvalidFieldException ife) {
            if(errorReporter != null)
                errorReporter.errorReport(null, ife);
            else
                ife.printStackTrace();
        }
    }

    /**
     * Delete a route between two nodes. If the route does not exist, the
     * method silently exits.
     *
     * @param fromNode The source node for the route
     * @param fromEventOut The eventOut source of the route
     * @param toNode The destination node of the route
     * @param toEventIn The eventIn destination of the route
     * @throws InvalidEventInException the eventIn name is not a field of
     *   the destination node
     * @throws InvalidEventOutException the eventOut name is not a field of
     *   the source node
     */
    public void deleteRoute(SFNode fromNode,
                            String fromEventOut,
                            SFNode toNode,
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
            if(errorReporter != null)
                errorReporter.errorReport(null, ife);
            else
                ife.printStackTrace();
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
     * @param parameters The list of parameters to accompany the load call as
     *   defined in the Anchor node specification of VRML97
     */
    public void loadURL(MFString url, MFString parameters) {

        String[] raw_param = parameters.getRawData();

        // parse the parameter list and place the values into the map
        HashMap param_map = new HashMap();
        int num_params = (raw_param == null) ? 0 : raw_param.length;
        for(int i = 0; i < num_params; i++) {
            int eq_idx = raw_param[i].indexOf('=');
            String key = raw_param[i].substring(0, eq_idx);
            String value = raw_param[i].substring(eq_idx + 1);
            param_map.put(key, value);
        }

        realBrowser.loadURL(url.getRawData(), param_map);
    }

    //----------------------------------------------------------
    // Non-standard, standard methods
    //
    // These are methods that are not part of the Annex C spec
    // but pretty much every VRML browser has implemented them,
    // so we might as well too.
    //----------------------------------------------------------

    /**
     * Print a string to the output.
     *
     * @param str The string to print
     */
    public void println(String str) {
        if(errorReporter != null)
            errorReporter.messageReport(str);
        else
            System.out.println(str);
    }

    /**
     * Print an object instance to the output.
     *
     * @param obj The object to print
     */
    public void println(Object obj) {
        if(errorReporter != null)
            errorReporter.messageReport(obj.toString());
        else
            System.out.println(obj);
    }

    /**
     * Print a string to the output.
     *
     * @param str The string to print
     */
    public void print(String str) {
        if(errorReporter != null)
            errorReporter.messageReport(str);
        else
            System.out.print(str);
    }

    /**
     * Print an object instance to the output.
     *
     * @param obj The object to print
     */
    public void print(Object obj) {
        if(errorReporter != null)
            errorReporter.messageReport(obj.toString());
        else
            System.out.print(obj);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.  Also changes the ErrorReporter
     * used by the CommonBrowser instance.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        // Pass the ErrorReporter change onto the CommonBrowser instance
        // since the CommonBrowser is private.
        realBrowser.setErrorReporter(errorReporter);
    }

    /**
     * Convenience version of createVrmlFromString so that the caller can
     * be returned the raw nodes rather than the JavascriptScript versions.
     */
    public VRMLNodeType[] parseVrmlString(String vrmlSyntax)
        throws VRMLParseException, VRMLException {

        try {
            return realBrowser.createVrmlFromString(vrmlSyntax, execSpace);
        } catch(IOException ioe) {
            throw new VRMLException(ioe.getMessage());
        }
    }
}
