/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package vrml;

/**
 * Java binding for the Browser object, which represents capabilities to create
 * and manage content.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class Browser {

    /**
     * Get the description string currently used by the world. Returns null if
     * not set or supported.
     *
     * @return The current description string or null
     */
    public String getDescription() {
        return null;
    }

    /**
     * Get the name of the browser. The name is an implementation specific
     * string representing the browser.
     *
     * @return The name of the browser or null if not supported
     */
    public String getName() {
        return null;
    }

    /**
     * Get the version of the browser. Returns an implementation specific
     * representation of the version number.
     *
     * @return The version of the browser or null if not supported
     */
    public String getVersion() {
        return null;
    }

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     */
    public float getCurrentSpeed() {
        return 0f;
    }

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     */
    public float getCurrentFrameRate() {
        return 0f;
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
        return null;
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
    public BaseNode[] createVrmlFromString(String vrmlSyntax)
        throws InvalidVRMLSyntaxException {

        return null;
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
     * @param event The name of the MFNode eventIn to send the nodes to.
     * @exception InvalidVRMLSyntaxException If the string does not contain legal
     *   VRML syntax or no node instantiations
     */
    public void createVrmlFromURL(String[] url, BaseNode node, String event)
        throws InvalidVRMLSyntaxException {
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
     */
    public void addRoute(BaseNode fromNode, String fromEventOut,
        BaseNode toNode, String toEventIn) {
    }

    /**
     * Delete a route between two nodes. If the route does not exist, the
     * method silently exits.
     *
     * @param fromNode The source node for the route
     * @param fromEventOut The eventOut source of the route
     * @param toNode The destination node of the route
     * @param toEventIn The eventIn destination of the route
     */
    public void deleteRoute(BaseNode fromNode, String fromEventOut,
        BaseNode toNode, String toEventIn) {
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
     * @param parameter The list of parameters to accompany the load call as
     *   defined in the Anchor node specification of VRML97
     * @exception InvalidVRMLSyntaxException If the string does not contain legal
     *   VRML syntax or no node instantiations
     */
    public void loadURL(String[] url, String[] parameter)
        throws InvalidVRMLSyntaxException {
    }

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param description The description string to set.
     */
    public void setDescription(String description) {
    }
}
