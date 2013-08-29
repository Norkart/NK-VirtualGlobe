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

package org.web3d.vrml.scripting.ecmascript.x3d;

// External imports
import java.lang.reflect.Method;
import java.io.IOException;
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.scripting.browser.X3DCommonBrowser;
import org.web3d.vrml.scripting.ecmascript.builtin.*;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * Browser object within an ECMAScript X3D script.
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
 * @version $Revision: 1.19 $
 */
public class Browser extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** Set of the valid function names for this object */
    private static HashSet functionNames;

    /** The function objects to maintain */
    private HashMap functionObjects;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** The execution space used during route management */
    private VRMLExecutionSpace execSpace;

    /** Internal scene representation */
    private BasicScene internalScene;

    /** The real browser instance that we delegate a lot of the requests to */
    private X3DCommonBrowser realBrowser;

    /** The scene represented in an Rhino object */
    private X3DExecutionContext ecmaScene;

    /** Local route manager */
    private RouteManager routeManager;

    /** FrameState manager for creating nodes */
    private FrameStateManager stateManager;

    /** List of profile objects for the supported profiles */
    private ProfileInfoArray profileListing;

    /** List of component objects for the supported components */
    private ComponentInfoArray componentListing;

    /** Map that contains all of the ProfileInfo instances */
    private HashMap nameToProfileMap;

    /**
     * Static initialisation block to set up the basic ECMAScript definitions.
     */
    static {
        propertyNames = new HashSet();
        propertyNames.add("name");
        propertyNames.add("version");
        propertyNames.add("currentSpeed");
        propertyNames.add("currentFrameRate");
        propertyNames.add("description");
        propertyNames.add("supportedComponents");
        propertyNames.add("supportedProfiles");
        propertyNames.add("currentScene");

        functionNames = new HashSet();
        functionNames.add("replaceWorld");
        functionNames.add("createX3DFromString");
        functionNames.add("createX3DFromURL");
        functionNames.add("loadURL");
        functionNames.add("importDocument");
        functionNames.add("getRenderingProperty");
        functionNames.add("addBrowserListener");
        functionNames.add("removeBrowserListener");
        functionNames.add("print");
        functionNames.add("println");
        functionNames.add("nextViewpoint");
        functionNames.add("previousViewpoint");
        functionNames.add("firstViewpoint");
        functionNames.add("lastViewpoint");
    }

    /**
     * Create a browser instance that represents the given universe details.
     *
     * @param space The execution space we need a browser for
     * @param browser The core representation of the browser
     * @param vpm The viewpoint manager for next/previous calls
     * @param rm A route manager for users creating/removing routes
     * @param wlm Loader for full files
     * @param fsm State manager for coordinating inter-frame processing
     * @throws IllegalArgumentException Any one of the parameters is null
     */
    public Browser(VRMLExecutionSpace space,
                   BrowserCore browser,
                   ViewpointManager vpm,
                   RouteManager rm,
                   FrameStateManager fsm,
                   WorldLoaderManager wlm) {

        super("Browser");

        functionObjects = new HashMap();
        nameToProfileMap = new HashMap();
        stateManager = fsm;
        routeManager = rm;

        realBrowser = new X3DCommonBrowser(browser, vpm, rm, fsm, wlm);

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        execSpace = space;

        realBrowser.setErrorReporter(errorReporter);

        internalScene = space.getContainedScene();

        // Set up the profile and component listings. Note name class for
        // the internal and Spec-required objects.
        VRMLNodeFactory node_fac = internalScene.getNodeFactory();

        org.web3d.vrml.lang.ComponentInfo[] c_list =
            node_fac.getAvailableComponents();

        ComponentInfo[] ecma_comps = new ComponentInfo[c_list.length];

        for(int i = 0; i < c_list.length; i++)
            ecma_comps[i] = new ComponentInfo(c_list[i]);

        componentListing = new ComponentInfoArray(ecma_comps);

        org.web3d.vrml.lang.ProfileInfo[] p_list =
            node_fac.getAvailableProfiles();

        ProfileInfo[] ecma_profiles = new ProfileInfo[p_list.length];
        ProfileInfo scene_profile = null;
        SceneMetaData md = internalScene.getMetaData();
        String profile_name = md.getProfileName();

        nameToProfileMap = new HashMap();

        for(int i = 0; i < p_list.length; i++) {
            ecma_profiles[i] = new ProfileInfo(p_list[i]);
            String p_name = p_list[i].getName();
            if(p_name.equals(profile_name))
                scene_profile = ecma_profiles[i];

            nameToProfileMap.put(p_name, ecma_profiles[i]);
        }

        profileListing = new ProfileInfoArray(ecma_profiles);

        if(internalScene instanceof VRMLScene) {
            ecmaScene = new Scene(space, rm, fsm, scene_profile);
        } else {
            ecmaScene = new X3DExecutionContext(space, rm, fsm, scene_profile);
        }

        ecmaScene.setParentScope(this);
    }

    //----------------------------------------------------------
    // Methods defined by Scriptable
    //----------------------------------------------------------

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    public boolean has(String name, Scriptable start) {
        return (propertyNames.contains(name) || functionNames.contains(name));
    }

    /**
     * Get the value of the named function. If no function object is
     * registex for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    public Object get(String name, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(propertyNames.contains(name)) {
            char prop = name.charAt(0);

            switch(prop) {
                case 'n':
                    ret_val = realBrowser.getName();
                    break;

                case 'v':
                    ret_val = realBrowser.getVersion();
                    break;

                case 'c':
                    if(name.equals("currentSpeed"))
                        ret_val = new Float(realBrowser.getCurrentSpeed());
                    else if(name.equals("currentFrameRate"))
                        ret_val = new Float(realBrowser.getCurrentFrameRate());
                    else
                        ret_val = ecmaScene;
                    break;

                case 'd':
                    ret_val = realBrowser.getDescription();
                    break;

                case 's':
                    if(name.equals("supportedProfiles"))
                        ret_val = profileListing;
                    else
                        ret_val = componentListing;

                    break;
            }

        } else if(functionNames.contains(name)) {
            ret_val = locateFunction(name);
        }

        return ret_val;
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFColor(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            functionObjects.put(name, value);
        } else if(name.equals("description")) {
            realBrowser.setDescription((String)value);
        }
    }

    //----------------------------------------------------------
    // Local methods from the Rhino lookup capabilities
    //----------------------------------------------------------

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
    public void jsFunction_replaceWorld(Scene scene){
        VRMLScene int_scene = (VRMLScene)scene.getInternalScene();

        realBrowser.replaceWorld(int_scene);
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
    public Scene jsFunction_createX3DFromString(String vrmlSyntax) {
        Scene ret_val = null;

        try {
            VRMLScene scene =
                realBrowser.createX3DFromString(vrmlSyntax);

            VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();
            SceneMetaData md = scene.getMetaData();
            String name = md.getProfileName();

            ProfileInfo profile = (ProfileInfo)nameToProfileMap.get(name);
            ret_val = new Scene(space, routeManager, stateManager, profile);
            ret_val.setParentScope(this);
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
    public Scene jsFunction_createX3DFromURL(MFString url) {
        Scene ret_val = null;

        try {
            String[] url_str = url.getRawData();
            VRMLScene scene =
                realBrowser.createX3DFromURL(url_str);

            VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();
            SceneMetaData md = scene.getMetaData();
            String name = md.getProfileName();

            ProfileInfo profile = (ProfileInfo)nameToProfileMap.get(name);
            ret_val = new Scene(space, routeManager, stateManager, profile);
            ret_val.setParentScope(this);
        } catch(Exception e) {
            errorReporter.errorReport("Error parsing string", e);
        }

        return ret_val;
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
     */
    public void jsFunction_loadURL(MFString url, MFString parameter) {

        String[] raw_param = parameter.getRawData();

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

    public String jsFunction_getRenderingProperty(String name) {
        errorReporter.messageReport("Browser.getRenderingProperty() not implemented yet");
        return null;
    }

    public void jsFunction_addBrowserListener(String functionName) {
        errorReporter.messageReport("Browser.addBrowserListener() not implemented yet");
    }

    public void jsFunction_removeBrowserListener(String functionName) {
        errorReporter.messageReport("Browser.removeBrowserListener() not implemented yet");
    }

    /**
     * Print a string to the output.
     *
     * @param str The string to print
     */
    public void jsFunction_println(String str) {
        errorReporter.messageReport(str);
    }

    /**
     * Print an object instance to the output.
     *
     * @param obj The object to print
     */
    public void jsFunction_println(Object obj) {
        errorReporter.messageReport(obj.toString());
    }

    /**
     * Print a string to the output.
     *
     * @param str The string to print
     */
    public void jsFunction_print(String str) {
        errorReporter.partialReport(str);
    }

    /**
     * Print an object instance to the output.
     *
     * @param obj The object to print
     */
    public void jsFunction_print(Object obj) {
        errorReporter.partialReport(obj.toString());
    }

    /**
     * Bind the next viewpoint in the list. The definition of "next" is not
     * specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     */
    public void jsFunction_nextViewpoint() {
        realBrowser.nextViewpoint();
    }


    /**
     * Bind the previous viewpoint in the list. The definition of "previous" is
     * not specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     */
    public void jsFunction_previousViewpoint() {
        realBrowser.previousViewpoint();
    }

    /**
     * Bind the first viewpoint in the list. This is the first viewpoint
     * declared in the user's file. ie The viewpoint that would be bound by
     * default on loading.
     */
    public void jsFunction_firstViewpoint() {
        realBrowser.firstViewpoint();
    }

    /**
     * Bind the last viewpoint in the list.
     */
    public void jsFunction_lastViewpoint() {
        realBrowser.lastViewpoint();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Check to see if any of the scene variables have been changed, such as
     * the root nodes, proto definitions etc.
     *
     * @return true if any of the scene structures have changed
     */
    public boolean hasSceneChanged() {
        return ecmaScene.hasSceneChanged();
    }


    /**
     * Get the list of fields that have changed. The return value may be
     * either a single {@link NodeFieldData} instance or an
     * {@link java.util.ArrayList} of field data instances if more than one
     * has changed. When called, this is recursive so that all fields and
     * nodes referenced by this node field will be included. If no fields have
     * changed, this will return null. However, that should never happen as the
     * user should always check {@link FieldScriptableObject#hasChanged()} which
     * would return false before calling this method.
     *
     * @return A single {@link NodeFieldData}, {@link java.util.ArrayList}
     *   or null
     */
    public Object getChangedData() {
        return ecmaScene.getChangedData();
    }

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
        ecmaScene.setErrorReporter(errorReporter);
    }

    /**
     * Register a function object with this field type
     *
     * @param name The name to associate it with
     * @param value The object to keep this as
     */
    protected void registerFunction(String name, Object value) {
        functionObjects.put(name, value);
    }

    /**
     * Convenience method to locate a function name for this object and
     * create an appriate Function instance to represent it. It assumes that
     * the name you give it is the normal name and will add a "jsFunction_"
     * prefix to locate that from the method details. There is also the
     * implicit assumption that you have made a check for this name being a
     * valid function for this object before you call this method. If a
     * function object is found for this method, it will automatically be
     * registered and you can also have a copy of it returned to use.
     *
     * @param name The real method name to look for
     * @return The function object corresponding to the munged method name
     */
    protected FunctionObject locateFunction(String name) {
        String real_name = JS_FUNCTION_PREFIX + name;

        Method[] methods = FunctionObject.findMethods(getClass(), real_name);

        FunctionObject function = new FunctionObject(name, methods[0], this);

        functionObjects.put(name, function);

        return function;
    }
}
