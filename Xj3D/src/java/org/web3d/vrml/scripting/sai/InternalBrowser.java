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

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.x3d.sai.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.SceneMetaData;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.WriteableSceneMetaData;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.scripting.browser.X3DCommonBrowser;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * Basic browser interface that represents the interface to the VRML browser
 * from any application.
 * <p>
 * Individual VRML browser implementors are to extend this
 * interface and provide this functionality. The individual users will not see
 * anything but this interface.
 * <P>
 * A number of the methods in this applicationcan take strings representing URLs.
 * Relative URL strings contained in URL fields of nodes or these method
 * arguments are interpreted as follows:
 * <P>
 * Relative URLs are treated as per clause B.3.5 of the EAI Java Bindings
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.29 $
 */
class InternalBrowser implements Browser {

    /** Error message when the browser has been disposed of */
    private static final String INVALID_BROWSER_MSG =
        "Cannot access the Browser object. It has been disposed of";

    /** Error message when they give us neither profile or component */
    private static final String NULL_CREATE_SCENE_ARGS_MSG =
        "Both arguments are null. 19775-2, 6.3.11 requires that one or other " +
        "argument is to be non-null.";

    /** The default profile to use */
    private static final String DEFAULT_PROFILE = "Core";

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /** The field factory instance */
    private FieldFactory fieldFactory;

    /** Reference queue used for keeping track of field object instances */
    private ReferenceQueue fieldQueue;

    /** The node wrapper factory instance */
    private BaseNodeFactory baseNodeFactory;

    /** The execution space used during route management */
    private VRMLExecutionSpace execSpace;

    /** The real browser we are delegating the functionality to */
    private X3DCommonBrowser realBrowser;

    /** Internal scene representation */
    private BasicScene internalScene;

    /** The scene represented in an Rhino object */
    private BaseExecutionContext saiScene;

    /** Local route manager */
    private RouteManager routeManager;

    /** FrameState manager for creating nodes */
    private FrameStateManager stateManager;

    /** List of profile objects for the supported profiles */
    private ProfileInfo[] profileListing;

    /** List of component objects for the supported components */
    private ComponentInfo[] componentListing;

    /** Map that contains all of the ProfileInfo instances */
    private HashMap nameToProfileMap;

    /** Map that contains all of the ComponentInfo instances */
    private HashMap nameToComponentMap;

    /** Listener for dealing with the script wrapper for field access */
    private FieldAccessListener fieldAccessListener;

    /** The set of rendering properties that the browser supports */
    private Map renderingProperties;

    /** The set of browser properties that the browser supports */
    private Map browserProperties;

    /** The browser core */
    private BrowserCore browserCore;

    /**
     * Create a browser instance that represents the given universe details.
     *
     * @param space The execution space we need a browser for
     * @param browser The core representation of the browser
     * @param vpm The viewpoint manager for next/previous calls
     * @param rm A route manager for users creating/removing routes
     * @param wlm Loader for full files
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating access requests
     * @throws IllegalArgumentException Any one of the parameters is null
     */
    InternalBrowser(VRMLExecutionSpace space,
                    BrowserCore browser,
                    ViewpointManager vpm,
                    RouteManager rm,
                    FrameStateManager fsm,
                    WorldLoaderManager wlm,
                    ReferenceQueue refQueue,
                    FieldFactory fac,
                    FieldAccessListener fal) {

        execSpace = space;
        browserCore = browser;
        fieldQueue = refQueue;
        fieldFactory = fac;
        stateManager = fsm;
        fieldAccessListener = fal;
        internalScene = space.getContainedScene();
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        realBrowser = new X3DCommonBrowser(browser, vpm, rm, fsm, wlm);
        realBrowser.setErrorReporter(errorReporter);

        nameToProfileMap = new HashMap();
        nameToComponentMap = new HashMap();

        Map browser_props = new HashMap();

        boolean hasConcreteNodeImpls = false;
        try {
            // if ya ain't got Group, ya ain't got nodes
            Class c = Class.forName( "org.xj3d.sai.internal.node.grouping.SAIGroup" );
            hasConcreteNodeImpls = true;
        } catch ( ClassNotFoundException cnfe ) {
        }
        if ( hasConcreteNodeImpls ) {
            baseNodeFactory = new DefaultBaseNodeFactory( true, fieldQueue, fieldFactory, fieldAccessListener );
            browser_props.put( "ABSTRACT_NODES", new Boolean( true ) );
            browser_props.put( "CONCRETE_NODES", new Boolean( true ) );
        } else {
            baseNodeFactory = new DefaultBaseNodeFactory( false, fieldQueue, fieldFactory, fieldAccessListener );
            browser_props.put( "ABSTRACT_NODES", new Boolean( false ) );
            browser_props.put( "CONCRETE_NODES", new Boolean( false ) );
        }

        browser_props.put( "PROTOTYPE_CREATE", new Boolean( false ) );
        browser_props.put( "DOM_IMPORT", new Boolean( true ) );
        browser_props.put( "XML_ENCODING", new Boolean( true ) );
        browser_props.put( "CLASSIC_VRML_ENCODING", new Boolean( true ) );
        browser_props.put( "BINARY_ENCODING", new Boolean( true ) );
        browserProperties = Collections.unmodifiableMap(browser_props);

        Map render_props = new HashMap();
        // Fill in details of rendering properties here

        renderingProperties = Collections.unmodifiableMap(render_props);

        // Set up the profile and component listings. Note name class for
        // the internal and Spec-required objects.
        VRMLNodeFactory node_fac = internalScene.getNodeFactory();

        org.web3d.vrml.lang.ComponentInfo[] c_list =
            node_fac.getAvailableComponents();

        componentListing = new ComponentInfo[c_list.length];

        for(int i = 0; i < c_list.length; i++) {
            componentListing[i] = new SAIComponentInfo(c_list[i]);
            nameToComponentMap.put(c_list[i].getName(), componentListing[i]);
        }

        org.web3d.vrml.lang.ProfileInfo[] p_list =
            node_fac.getAvailableProfiles();

        profileListing = new ProfileInfo[p_list.length];
        ProfileInfo scene_profile = null;
        SceneMetaData md = internalScene.getMetaData();
        String profile_name = md.getProfileName();

        for(int i = 0; i < p_list.length; i++) {
            profileListing[i] = new SAIProfileInfo(p_list[i]);
            String p_name = p_list[i].getName();
            if(p_name.equals(profile_name))
                scene_profile = profileListing[i];

            nameToProfileMap.put(p_name, profileListing[i]);
        }

        if(internalScene instanceof VRMLScene) {
            saiScene = new WorldScene(space,
                                      rm,
                                      stateManager,
                                      scene_profile,
                                      fieldQueue,
                                      fieldFactory,
                                      fieldAccessListener,
                                      baseNodeFactory);
        } else {
            saiScene = new BaseExecutionContext(space,
                                                rm,
                                                stateManager,
                                                scene_profile,
                                                fieldQueue,
                                                fieldFactory,
                                                fieldAccessListener,
                                                baseNodeFactory);
        }
    }

    //----------------------------------------------------------
    // Methods defined by Browser
    //----------------------------------------------------------

    /**
     * Get the name of the browser. The name is an implementation specific
     * string representing the browser.
     *
     * @return The name of the browser or null if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public String getName()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return realBrowser.getName();
    }

    /**
     * Get the version of the browser. Returns an implementation specific
     * representation of the version number.
     *
     * @return The version of the browser or null if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public String getVersion()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return realBrowser.getVersion();
    }

    /**
     * Get a listing of the profiles that this browser implementation is
     * capable of supporting.
     *
     * @return The listing of all supported profiles
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public ProfileInfo[] getSupportedProfiles()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return profileListing;
    }

    /**
     * Get a specific profile.
     *
     * @param name The profile name
     * @return The specified profile
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws NotSupportedException The reqested profile is not supported
     */
    public ProfileInfo getProfile(String name)
        throws InvalidBrowserException, NotSupportedException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        ProfileInfo ret_val = (ProfileInfo)nameToProfileMap.get(name);

        if(ret_val == null)
            throw new NotSupportedException("Profile not supported by Xj3D: " + name);

        return ret_val;
    }

    /**
     * Get a listing of all the components that this browser implementation is
     * capable of supporting.
     *
     * @return The listing of all supported components
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public ComponentInfo[] getSupportedComponents()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return componentListing;
    }

    /**
     * Get a specific component at the requested support level.
     *
     * @param name The component name
     * @param level The minimum component level
     * @return The specified component
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws NotSupportedException The reqested component is not supported
     */
    public ComponentInfo getComponent(String name, int level)
        throws InvalidBrowserException, NotSupportedException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        ComponentInfo ret_val = (ComponentInfo)nameToComponentMap.get(name);

        if(ret_val == null)
            throw new NotSupportedException("Component not supported by Xj3D: " + name);

        if(level <= ret_val.getLevel())
            throw new NotSupportedException(
                "Component level higher than that supported: " + name + ":" + level);

        ret_val = new SAIComponentInfo(ret_val, level);

        return ret_val;
    }

    /**
     * Get the information about the current scene. If no scene has been set
     * then this will return null.
     *
     *
     * @return The current scene data
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public X3DExecutionContext getExecutionContext()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return saiScene;
    }

    /**
     * Create a new, empty scene that can be used to programmatically fill in
     * new scenes. This scene has the give profile and/or component information
     * set, and is not currently being rendered. Either argument may be null,
     * but not both arguments. If the profile is null, then the default profile
     * of "NONE" is used, and only the components are used. The scene version
     * is set to the highest supported version of the specification that this
     * browser currently implements.
     *
     * @param profile The profile to use for this scene or null for none
     * @param components The list of components to use or null
     * @return A new empty scene to work with
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws IllegalArgumentException Both arguments provided are null or
     *   zero length
     */
    public X3DScene createScene(ProfileInfo profile, ComponentInfo[] components)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if((profile == null) &&
           ((components == null) || (components.length == 0)))
           throw new IllegalArgumentException(NULL_CREATE_SCENE_ARGS_MSG);

        // Need to create a new root node for the new scene
        VRMLNodeFactory node_fac = internalScene.getNodeFactory();
        VRMLNodeFactory new_fac = null;

        try {
            new_fac = (VRMLNodeFactory)node_fac.clone();
        } catch(CloneNotSupportedException cnse) {
            throw new InvalidBrowserException("Error cloning node factory");
        }

// JC: Can we assume the version for the parent and this factory are the same?
        String profile_name = profile == null ? DEFAULT_PROFILE : profile.getName();

        new_fac.setProfile(profile_name);

        int num_comp = (components == null) ? 0 : components.length;
        for(int i = 0; i < num_comp; i++) {
            new_fac.addComponent(components[i].getName(),
                                 components[i].getLevel());
        }

        VRMLWorldRootNodeType root_node =
            (VRMLWorldRootNodeType)new_fac.createVRMLNode("WorldRoot",
                                                           false);
        root_node.setFrameStateManager(stateManager);
        root_node.setErrorReporter(errorReporter);
        root_node.setupFinished();

        WorldScene scene = new WorldScene(root_node,
                                          routeManager,
                                          stateManager,
                                          profile,
                                          components,
                                          fieldQueue,
                                          fieldFactory,
                                          fieldAccessListener,
                                          baseNodeFactory);

        int[] version = new_fac.getSpecVersion();

        WriteableSceneMetaData md =
            new WriteableSceneMetaData(version[0] + " " + version[1],
                                       false,
                                       SceneMetaData.SCRIPTED_ENCODING);

        SAIVRMLScene v_scene = new SAIVRMLScene(md, version[0], version[1]);
        v_scene.setNodeFactory(new_fac);
        v_scene.setWorldRootURL(realBrowser.getWorldURL());
        v_scene.setRootNode(root_node);

        root_node.setContainedScene(v_scene);

        return scene;
    }

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public float getCurrentSpeed()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return realBrowser.getCurrentSpeed();
    }

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public float getCurrentFrameRate()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return realBrowser.getCurrentFrameRate();
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
     * @param scene The new scene to render in the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public void replaceWorld(X3DScene scene)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLScene world = (VRMLScene)((WorldScene)scene).getInternalScene();
        realBrowser.replaceWorld(world);
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
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws InvalidURLException All of the URLs passed to this method are
     *    bogus and cannot be translated to usable values
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public void loadURL(String[] url, Map parameters)
        throws InvalidBrowserException, InvalidURLException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.loadURL(url, parameters);
    }

    /**
     * Get the description of the current world.
     *
     * @return A description string or null if none set
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public String getDescription()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return realBrowser.getDescription();
    }

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param desc The description string to set.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public void setDescription(String desc)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.setDescription(desc);
    }

    /**
     * Parse the given string and turn this into a list of VRML nodes. Method
     * is a blocking call that won't return until all of the top level nodes
     * defined in the string have been returned.
     * <P>
     * At the point that this method returns, external files such as textures,
     * sounds and inlines may not have been loaded.
     * <P>
     * The string may contain all legal VRML syntax - either UTF8 or XML
     * encoding. The VRML header line is not required to be present in the
     * string if UTF8 syntax.
     *
     * @param vrmlString The string containing VRML string syntax
     * @return The scene that was created from the string.
     * @throws InvalidX3DException If the string does not contain legal
     *   X3D/VRML syntax or no node instantiations
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public X3DScene createX3DFromString(String x3dString)
        throws InvalidBrowserException,
               InvalidX3DException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        X3DScene ret_val = null;

        try {
            VRMLScene scene =
                realBrowser.createX3DFromString(x3dString);

            VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();
            SceneMetaData md = scene.getMetaData();
            String name = md.getProfileName();

            ProfileInfo profile = (ProfileInfo)nameToProfileMap.get(name);
            ret_val = new WorldScene(space,
                                     routeManager,
                                     stateManager,
                                     profile,
                                     fieldQueue,
                                     fieldFactory,
                                     fieldAccessListener,
                                     baseNodeFactory);
        } catch(Exception e) {
            errorReporter.errorReport("Error parsing string", e);
        }

        return ret_val;
    }

    /**
     * Parse the contents of the stream and interpret it as VRML content. The
     * browser shall interpret the content using the initial characters of
     * the stream to determine which encoding and file format is used.
     *
     * @param input The input stream that contains the content to parse
     * @return The scene that was created from the string.
     * @throws InvalidX3DException If the string does not contain legal
     *   X3D/VRML syntax or no node instantiations
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws IOException An I/O Error occurred while reading the stream
     */
    public X3DScene createX3DFromStream(InputStream is)
        throws InvalidBrowserException,
               InvalidX3DException,
               IOException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        X3DScene ret_val = null;

        try {
            VRMLScene scene =
                realBrowser.createX3DFromStream(is);

            VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();
            SceneMetaData md = scene.getMetaData();
            String name = md.getProfileName();

            ProfileInfo profile = (ProfileInfo)nameToProfileMap.get(name);
            ret_val = new WorldScene(space,
                                     routeManager,
                                     stateManager,
                                     profile,
                                     fieldQueue,
                                     fieldFactory,
                                     fieldAccessListener,
                                     baseNodeFactory);
        } catch(Exception e) {
            errorReporter.errorReport("Error parsing string", e);
        }

        return ret_val;
    }

    /**
     * Create and load X3D from the given URL. The call will not return until
     * the basic top-level scene has been processed or an error has occurred.
     * Inlines, textures, sound and externprotos are not guaranteed to be
     * loaded at this time. If the caller needs to know what the final URL was
     * that loaded, use the getWorldURL() call from the returned scene.
     *
     * @param url The list of URLs in decreasing order of preference as defined
     *   in the VRML97/X3D specification.
     * @return The scene that was created from the URLs.
     * @throws InvalidX3DException If the string does not contain legal
     *   X3D/VRML syntax or no node instantiations
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws InvalidURLException All of the URLs passed to this method are
     *    bogus and cannot be translated to usable values
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public X3DScene createX3DFromURL(String[] url)
        throws InvalidBrowserException,
               InvalidURLException,
               InvalidX3DException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        X3DScene ret_val = null;

        try {
            VRMLScene scene =
                realBrowser.createX3DFromURL(url);

            VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();
            SceneMetaData md = scene.getMetaData();
            String name = md.getProfileName();

            ProfileInfo profile = (ProfileInfo)nameToProfileMap.get(name);
            ret_val = new WorldScene(space,
                                     routeManager,
                                     stateManager,
                                     profile,
                                     fieldQueue,
                                     fieldFactory,
                                     fieldAccessListener,
                                     baseNodeFactory);
        } catch(Exception e) {
            errorReporter.errorReport("Error parsing string", e);
        }

        return ret_val;
    }

    /**
     * A utility request to import a W3C DOM document or document fragment and
     * convert it to an X3D scene. The method only performs a conversion
     * process and does not display the resulting scene. The scene may then be
     * used as the argument for the replaceWorld service. When the conversion
     * is made, there is no lasting connection between the DOM and the
     * generated scene. Each request shall be a one-off conversion attempt
     * (the conversion may not be successful if the DOM does not match the X3D
     * scene graph structure).
     *
     * @param element The root element to convert
     * @return A scene representation corresponding to the document
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws InvalidDocumentException The document structure cannot be
     *    converted to an X3D scene for some reason
     */
    public X3DScene importDocument(Node element)
        throws InvalidBrowserException,
               InvalidDocumentException,
               NotSupportedException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLScene scene = realBrowser.importDocument(element);

        VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();
        SceneMetaData md = scene.getMetaData();
        String name = md.getProfileName();

        ProfileInfo profile = (ProfileInfo)nameToProfileMap.get(name);
        X3DScene  ret_val = new WorldScene(space,
                                           routeManager,
                                           stateManager,
                                           profile,
                                           fieldQueue,
                                           fieldFactory,
                                           fieldAccessListener,
                                           baseNodeFactory);

        return ret_val;
    }

    /**
     * Print the message to the browser console without wrapping a new line
     * onto it.
     *
     * @param msg The object to be printed
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void print(Object msg)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(msg != null)
            errorReporter.partialReport(msg.toString());
    }

    /**
     * Print the message to the browser console and append a new line
     * onto it.
     *
     * @param msg The object to be printed
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void println(Object msg)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(msg != null)
            errorReporter.messageReport(msg.toString());
    }

    /**
     * Bind the next viewpoint in the list. The definition of "next" is not
     * specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void nextViewpoint()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.nextViewpoint();
    }

    /**
     * Bind the next viewpoint in the list. The definition of "next" is not
     * specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void nextViewpoint(int layer)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.nextViewpoint(layer);
    }

    /**
     * Bind the previous viewpoint in the list. The definition of "previous" is
     * not specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void previousViewpoint()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.previousViewpoint();
    }

    /**
     * Bind the previous viewpoint in the list. The definition of "previous" is
     * not specified, and may be browser dependent. If only one viewpoint is
     * declared, this method does nothing.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void previousViewpoint(int layer)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.previousViewpoint(layer);
    }

    /**
     * Bind the first viewpoint in the list. This is the first viewpoint
     * declared in the user's file. ie The viewpoint that would be bound by
     * default on loading.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void firstViewpoint()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.firstViewpoint();
    }

    /**
     * Bind the first viewpoint in the list. This is the first viewpoint
     * declared in the user's file. ie The viewpoint that would be bound by
     * default on loading.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void firstViewpoint(int layer)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.firstViewpoint(layer);
    }

    /**
     * Bind the last viewpoint in the list.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void lastViewpoint()
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.lastViewpoint();
    }

    /**
     * Bind the last viewpoint in the list.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void lastViewpoint(int layer)
        throws InvalidBrowserException {

        if(realBrowser == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        realBrowser.lastViewpoint(layer);
    }

    /**
     * Get the collection of rendering properties that the browser provides.
     * Rendering properties are key/value pairs, as defined in table 9.2 of
     * ISO/IEC 19775-1. Keys are instances of Strings, while the value is
     * dependent on the property. If the property is not defined in the
     * returned map, treat it as not being supported by the browser.
     *
     * @return A read-only map of the list of properties defined by the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws InvalidOperationTimingException This was not called during the
     *    correct timing during a script (may be called at any time from
     *    external)
     */
    public Map getRenderingProperties()
        throws InvalidBrowserException, InvalidOperationTimingException {

        return renderingProperties;
    }

    /**
     * Get the collection of browser properties that the browser provides.
     * Rendering properties are key/value pairs, as defined in table 9.2 of
     * ISO/IEC 19775-1. Keys are instances of Strings, while the value is
     * dependent on the property. If the property is not defined in the
     * returned map, treat it as not being supported by the browser.
     *
     * @return A read-only map of the list of properties defined by the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws InvalidOperationTimingException This was not called during the
     *    correct timing during a script (may be called at any time from
     *    external)
     */
    public Map getBrowserProperties()
        throws InvalidBrowserException, InvalidOperationTimingException {

        return browserProperties;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        realBrowser.setErrorReporter(errorReporter);
    }

    /**
     * Pass-through method that allows the script wrapper to ensure that any
     * nodes that have had their values change due to be accessed from the
     * DEF map table will have the changes propogated through to the renderable
     * scene graph. Should only be called if directOutput is true.
     */
    void updateEventOuts() {
        saiScene.updateEventOuts();
    }

    /**
     * Fetch the instance of the field factory in use by this browser.
     *
     * @return The factory instance
     */
    FieldFactory getFieldFactory() {
        return fieldFactory;
    }

    /**
     * Get the shared field reference queue used by this browser instance.
     *
     * @return The Java reference queue in use
     */
    ReferenceQueue getSharedFieldQueue() {
        return fieldQueue;
    }

    /**
     * Return the instance of the node wrapper factory in use by this browser instance.
     *
     * @return The factory instance
     */
    BaseNodeFactory getBaseNodeFactory() {
        return( baseNodeFactory );
    }
}
