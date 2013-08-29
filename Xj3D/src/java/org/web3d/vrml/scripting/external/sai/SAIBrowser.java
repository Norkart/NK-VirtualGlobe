/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.io.InputStream;
import java.io.IOException;

import java.lang.reflect.Constructor;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.w3c.dom.Node;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.x3d.sai.*;
import org.xj3d.sai.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;
import org.web3d.browser.BrowserCoreListener;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.lang.SceneMetaData;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.WriteableSceneMetaData;
import org.web3d.vrml.lang.InvalidNodeTypeException;

import org.web3d.vrml.scripting.browser.X3DCommonBrowser;

import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

import org.web3d.vrml.scripting.sai.SAIVRMLScene;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.NavigationManager;
import org.xj3d.core.eventmodel.CursorManager;

import org.xj3d.impl.core.eventmodel.DefaultNavigationManager;

/**
 * SAIBrowser implements the {@link Browser} interface, largely by
 * translating and interfacing between the wrapper classes and the
 * implementation class represented by {org.web3d.vrml.scripting.CommonBrowser}.
 * <P>
 * To function correctly, SAIBrowser needs to be constructed using
 * CommonBrowser and BrowserCore instances.  The SAIBrowser then registers
 * as a listener so that BrowserCoreListener BrowserInitialized and
 * browserShutdown messages.  The BrowserCore instance is necessary mainly
 * for the global namespace and VRMLExecutionSpace information.
 *
 * <P>
 *
 * @author Brad Vender, Rex Melton, Justin Couch
 * @version $Revision: 1.55 $
 */
public class SAIBrowser implements Xj3DBrowser, BrowserCoreListener {

    /** Error message when the browser has been disposed of */
    private static final String INVALID_BROWSER_MSG =
        "Cannot access the Browser object. It has been disposed of.";

    /** Error message when they give us neither profile or component */
    private static final String NULL_CREATE_SCENE_ARGS_MSG =
        "Both arguments are null. 19775-2, 6.3.11 requires that one or other " +
        "argument is to be non-null.";

    /** The default profile to use */
    private static final String DEFAULT_PROFILE = "Core";

    /**
     * A minimal X3D scene for use in querying supported capabilities
     * in the abscence of a scene.
     */
    private static final String MINIMAL_X3D_SCENE =
        "#X3D V3.0 utf8\n"+"PROFILE Core\n";

    /** String for getBrowserVersion */
    private static final String browserVersion = Xj3DConstants.VERSION;

    /** String for getBrowserName */
    private static final String browserName ="Xj3D SAI External Browser";

    /** The class name of a potential node factory */
    private String NODE_FACTORY_CLASS_NAME =
        "org.xj3d.sai.external.MappingSAINodeFactory";

    /** The list of browser listeners for browser events */
    private BrowserListener browserListener;

    /** List of external status event listeners.  */
    private StatusAdapter statusAdapter;

    /** Executor service used to send out browser events */
    private ExecutorService eventExecutor;

    /** The secondary implementation of the Browser*/
    private BrowserCore browserCore;

    /** The main implementation of the Browser */
    private X3DCommonBrowser browserImpl;

    /** The ErrorReporter to send errors and warnings to. */
    private ExternalErrorReporterAdapter errorReporter;

    /** The queue to post events to.*/
    private ExternalEventQueue eventQueue;

    /** The SAINodeFactory for use in mapping between VRMLNodeType and
    *  X3DNode instances. */
    private SAINodeFactory saiNodeFactory;

    /** The node factory for getting profile and component information,
    * and not actually used for constructing nodes here */
    private VRMLNodeFactory vrmlNodeFactory;

    /** The event adapter factory.
    * The event adapter system is reachable through this object. */
    private BufferedMappingSAIEventAdapterFactory adapterFactory;

    /** Route manager for handling user added/removed routes */
    private RouteManager routeManager;

    /** FrameState manager for creating nodes */
    private FrameStateManager stateManager;

    /** The CursorManager */
    private CursorManager cursorManager;

    /** The current execution space */
    private VRMLExecutionSpace currentSpace;

    /** The current context */
    private X3DExecutionContext currentContext;

    /** The set of rendering properties that the browser supports */
    private Map<String, Object> renderingProperties;

    /** The set of browser properties that the browser supports */
    private Map<String, Object> browserProperties;

    /**
     * External navigation manager for extended SAI use. Only created if
     * requested by the end user.
     */
    private NavigationUIManagerAdapter externalNavManager;

    /**
     * External cursor manager for extended SAI use. Only created if
     * requested by the end user.
     */
    private CursorUIManagerAdapter externalCursorManager;

    /**
     * External interface for those that want to mess with the CAD-specific
     * view.
     */
    private CADViewAdapter externalCADView;

    /**
     * Construct an SAIBrowser for the given VrmlDisplayPanel
     *
     *
     * @param browserImpl The delegated browser implementation
     * @param browserCore The BrowserCore to use as the implementation.
     * @param eventQueue The buffer to send events to.
     * @param reporter The ErrorReporter to use.  If null, will use
     *     DefaultErrorReporter's default.
     */
    public SAIBrowser(BrowserCore browserCore,
                      X3DCommonBrowser browserImpl,
                      RouteManager rm,
                      FrameStateManager fsm,
                      ExternalEventQueue eventQueue,
                      CursorManager cm,
                      ErrorReporter reporter) {

        if(browserCore == null)
            throw new IllegalArgumentException("Null BrowserCore");

        if(browserImpl == null)
            throw new IllegalArgumentException("Null CommonBrowser");

        this.browserCore = browserCore;
        this.browserImpl = browserImpl;
        this.eventQueue = eventQueue;
        routeManager = rm;
        stateManager = fsm;
        cursorManager = cm;

        errorReporter = new ExternalErrorReporterAdapter(reporter);
        BrowserListenerMulticaster.setErrorReporter(errorReporter);

        eventExecutor = Executors.newSingleThreadExecutor();

        browserCore.addCoreListener(this);

        Map browser_props = new HashMap<String, Object>();
        // Fill in details of browser properties here

        Map render_props = new HashMap<String, Object>();
        // Fill in details of rendering properties here

        renderingProperties = Collections.unmodifiableMap(render_props);

        // The null factory reference is corrected in the
        // NonMappingSAINodeFactory constructor.

        SimpleSAIFieldFactory fieldFactory=
            new SimpleSAIFieldFactory(eventQueue);

        adapterFactory=
            new BufferedMappingSAIEventAdapterFactory(2, browserCore.getVRMLClock());

        //////////////////////////////////////////////////////////////////////////////
        // rem: commented out pending relo-ing the scripting
        // packages to the org.xj3d.sai hierarchy
        //saiNodeFactory = getSAINodeFactory( fieldFactory, eventQueue );
        //if( saiNodeFactory == null ) {
        //////////////////////////////////////////////////////////////////////////////
        boolean use_concrete_nodes = false;
        try {
            // if ya ain't got Group, ya ain't got nodes
            Class c = Class.forName( "org.xj3d.sai.external.node.grouping.SAIGroup" );
            use_concrete_nodes = true;
        } catch (ClassNotFoundException cnfe) {
        }

        if(use_concrete_nodes) {
            saiNodeFactory = new MappingSAINodeFactory(fieldFactory, eventQueue);
            browser_props.put("ABSTRACT_NODES", Boolean.TRUE);
            browser_props.put("CONCRETE_NODES", Boolean.TRUE);
        } else {
            saiNodeFactory = new NonMappingSAINodeFactory(fieldFactory, eventQueue);
            browser_props.put("ABSTRACT_NODES", Boolean.FALSE);
            browser_props.put("CONCRETE_NODES", Boolean.FALSE);
        }

        adapterFactory.setFieldFactory(fieldFactory);
        fieldFactory.setNodeFactory(saiNodeFactory);
        fieldFactory.setSAIEventAdapterFactory(adapterFactory);

        browser_props.put("PROTOTYPE_CREATE", Boolean.FALSE);
        browser_props.put("DOM_IMPORT", Boolean.TRUE);
        browser_props.put("EXTERNAL_INTERACTIONS", Boolean.TRUE);
        browser_props.put("XML_ENCODING", Boolean.TRUE);
        browser_props.put("CLASSIC_VRML_ENCODING", Boolean.TRUE);
        browser_props.put("BINARY_ENCODING", Boolean.TRUE);
        browserProperties = Collections.unmodifiableMap(browser_props);
    }

    //-------------------------------------------------------------------
    // Methods defined by Xj3DBrowser
    //-------------------------------------------------------------------

    /**
     * Set the handler for error messages. This can be used to replace the
     * stock console. Passing a value of null removes the currently registered
     * reporter. Setting this will replace the current reporter with this
     * instance. If the current reporter is the default system console, then
     * the console will not receive any further messages.
     *
     * @param reporter The error reporter instance to use
     */
    public void setErrorReporter(Xj3DErrorReporter reporter) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        errorReporter.setErrorReporter(reporter);
        //statusAdapter.setErrorReporter(reporter);
    }

    /**
     * Add a listener for status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addStatusListener(Xj3DStatusListener l) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if (statusAdapter == null) {
            statusAdapter = new StatusAdapter(browserCore, errorReporter);
        }
        statusAdapter.addStatusListener(l);
    }

    /**
     * Remove a listener for status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeStatusListener(Xj3DStatusListener l) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if (statusAdapter == null) {
            statusAdapter = new StatusAdapter(browserCore, errorReporter);
        }
        statusAdapter.removeStatusListener(l);
    }

    /**
     * Fetch the interface that allows an external application to implement
     * their own navigation user interface. This is guaranteed to be unique
     * per browser instance.
     *
     * @return An interface allowing end-user code to manipulate the
     *    navigation.
     */
    public Xj3DNavigationUIManager getNavigationManager() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(externalNavManager == null) {
            NavigationManager mgr =
                new DefaultNavigationManager(browserCore);

            mgr.setErrorReporter(errorReporter);

            externalNavManager =
                new NavigationUIManagerAdapter(browserImpl.getViewpointManager(),
                                               mgr,
                                               browserCore);

            externalNavManager.setErrorReporter(errorReporter);
        }

        return externalNavManager;
    }

    /**
     * Fetch the interface that allows an external application to implement
     * their own cursor user interface.
     *
     * @return An interface allowing end-user code to manipulate the
     *    cursor.
     */
    public Xj3DCursorUIManager getCursorManager() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(externalCursorManager == null) {

            externalCursorManager =
                new CursorUIManagerAdapter(cursorManager, browserCore);

            externalCursorManager.setErrorReporter(errorReporter);
        }

        return externalCursorManager;
    }

    /**
     * Fetch the copontent-specific interface for managing a CAD scene. This
     * interface exposes CAD structures
     *
     * @return An interface allowing end-user code to manipulate the
     *    the CAD-specific structures in the scene.
     * @exception InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public Xj3DCADView getCADView()
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(externalCADView == null) {
            externalCADView = new CADViewAdapter(browserCore, saiNodeFactory);
            externalCADView.setErrorReporter(errorReporter);
        }

        return externalCADView;
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources
     * taken up by the 3D renderer.  By default it will use all of them.
     *
     * @param millis The minimum time in milleseconds.
     */
    public void setMinimumFrameInterval(int millis) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserCore.setMinimumFrameInterval(millis, true);
    }

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The cycle interval time in milliseconds
     */
    public int getMinimumFrameInterval() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserCore.getMinimumFrameInterval();
    }

    /**
     * Change the rendering style that the browser should currently be using.
     * Various options are available based on the constants defined in this
     * interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    public void setRenderingStyle(int style)
        throws IllegalArgumentException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserCore.setRenderingStyle(style);
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    public int getRenderingStyle() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserCore.getRenderingStyle();
    }

    //-------------------------------------------------------------------
    // Methods defined by ExternalBrowser
    //-------------------------------------------------------------------

    /**
     * addBrowserListener adds the specified listener to the set of listeners
     * for this browser.
     *
     * @param l The listener to add to the list of listeners for this browser
     */
    public void addBrowserListener(BrowserListener l)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserListener = BrowserListenerMulticaster.add(browserListener, l);
    }

    /** @see org.web3d.x3d.sai.ExternalBrowser#beginUpdate */
    public void beginUpdate() throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        eventQueue.beginUpdate();
    }

    /** @see org.web3d.x3d.sai.ExternalBrowser#endUpdate */
    public void endUpdate() throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        eventQueue.endUpdate();
    }

    /** @see org.web3d.x3d.sai.ExternalBrowser#pauseRender */
    public void pauseRender() {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        /** Should do something here. */
        throw new RuntimeException("Not yet implemented");
    }

    /** removeBrowserListener removes the specified listener from the set of
     *  listeners for this browser.
     * @param l The listener to remove from the list.
     * @see org.web3d.x3d.sai.ExternalBrowser#removeBrowserListener
     */
    public void removeBrowserListener(BrowserListener l)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserListener =
            BrowserListenerMulticaster.remove(browserListener, l);
    }

    /** @see org.web3d.x3d.sai.ExternalBrowser#startRender */
    public void startRender() {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        /** Should do something here. */
        throw new RuntimeException("Not yet implemented");
    }

    /** @see org.web3d.x3d.sai.ExternalBrowser#stopRender */
    public void stopRender() {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        /** Should do something here. */
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * Clean up and get rid of this browser.  When this method is called,
     * the event queue will be processed, the browser will shut down,
     * and any subsequent calls to browser methods will result in
     * InvalidBrowserException's being generated.
     */
    public void dispose() throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        endUpdate();
        browserImpl = null;
        broadcastEvent(new BrowserEvent(this,BrowserEvent.SHUTDOWN));

        adapterFactory.shutdown();
        statusAdapter.shutdown();
        statusAdapter = null;
//        externalCADView.shutdown();
//        externalNavManager.shutdown();
    }

    //-------------------------------------------------------------------
    // Methods defined by Browser
    //-------------------------------------------------------------------

    /** @see org.web3d.x3d.sai.Browser#createScene */
    public X3DScene createScene(ProfileInfo profile,
                                ComponentInfo[] components)
                                throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if((profile == null) &&
            ((components == null) || (components.length == 0)))
            throw new IllegalArgumentException(NULL_CREATE_SCENE_ARGS_MSG);

        VRMLNodeFactory new_fac;

        try {
            new_fac = (VRMLNodeFactory)vrmlNodeFactory.clone();
        } catch(CloneNotSupportedException cnse) {
            throw new InvalidBrowserException("Error cloning node factory");
        }

        String profile_name = profile == null ? DEFAULT_PROFILE : profile.getName();

        // JC: Do we need to set the spec version here from the parent factory or can we
        // assume that everything is still the same?
        //         new_fac.setSpecVersion();
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

        int[] version = new_fac.getSpecVersion();

        WriteableSceneMetaData md =
            new WriteableSceneMetaData(version[0] + " " + version[1],
                                       false,
                                       SceneMetaData.SCRIPTED_ENCODING);

        SAIVRMLScene v_scene = new SAIVRMLScene(md, version[0], version[1]);

        v_scene.setNodeFactory(new_fac);
        v_scene.setWorldRootURL(browserImpl.getWorldURL());
        v_scene.setRootNode(root_node);

        // TODO: need to generateProtoCreator and call setTemplateCreator

        root_node.setContainedScene(v_scene);

        SAIScene x3dScene = new SAIScene(v_scene,
                                         routeManager,
                                         stateManager,
                                         saiNodeFactory,
                                         eventQueue,
                                         (VRMLExecutionSpace)root_node,
                                         errorReporter);

        return x3dScene;
    }

    /** @see org.web3d.x3d.sai.Browser#createX3DFromString */
    public X3DScene createX3DFromString(String string)
        throws InvalidBrowserException, InvalidX3DException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLScene scene;

        try {
            scene = browserImpl.createX3DFromString(string);
        } catch(IOException e) {
            throw new InvalidX3DException(e.getMessage());
        } catch(InvalidNodeTypeException inte) {
            throw new InvalidX3DException(inte.getMessage());
        }

        VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();

        return new SAIScene(scene,
            routeManager,
            stateManager,
            saiNodeFactory,
            eventQueue,
            space,
            errorReporter);
    }

    /** @see org.web3d.x3d.sai.Browser#createX3DFromStream */
    public X3DScene createX3DFromStream(InputStream is)
        throws InvalidBrowserException, InvalidX3DException, IOException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLScene scene=browserImpl.createX3DFromStream(is);
        VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();

        X3DScene result=new SAIScene(scene,
                                     routeManager,
                                     stateManager,
                                     saiNodeFactory,
                                     eventQueue,
                                     space,
                                     errorReporter);

        return result;
    }

    /** @see org.web3d.x3d.sai.Browser#createX3DFromURL */
    public X3DScene createX3DFromURL(String[] url)
        throws InvalidBrowserException, InvalidURLException, InvalidX3DException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLScene scene=browserImpl.createX3DFromURL(url);
        VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();

        X3DScene result=new SAIScene(scene,
            routeManager,
            stateManager,
            saiNodeFactory,
            eventQueue,
            space,
            errorReporter);
        return result;
    }

    /** @see org.web3d.x3d.sai.Browser#getComponent */
    public ComponentInfo getComponent(String name, int level)
        throws InvalidBrowserException, NotSupportedException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        ComponentInfo components[]=getSupportedComponents();
        for(int counter=0; counter<components.length; counter++) {
            if(components[counter].getName().equals(name)) {
                if(components[counter].getLevel()>=level) {
                    return new SAIComponentInfo(name,
                                                level,
                                                components[counter].getTitle(),
                                                components[counter].getProviderURL());
                } else
                    throw new NotSupportedException();
            }
        }

        throw new NotSupportedException();
    }

    /** @see org.web3d.x3d.sai.Browser#getCurrentSpeed */
    public float getCurrentSpeed() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserImpl.getCurrentSpeed();
    }

    /** @see org.web3d.x3d.sai.Browser#getCurrentFrameRate */
    public float getCurrentFrameRate() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserImpl.getCurrentFrameRate();
    }

    /** @see org.web3d.x3d.sai.Browser#getExecutionContext */
    public X3DExecutionContext getExecutionContext()
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLExecutionSpace space = browserCore.getWorldExecutionSpace();

        if(space != currentSpace) {
            X3DExecutionContext ctx = new
                SAIScene((VRMLScene)space.getContainedScene(),
                routeManager,
                stateManager,
                saiNodeFactory,
                eventQueue,
                browserCore.getWorldExecutionSpace(),
                errorReporter);

            currentContext = ctx;
        }

        return currentContext;
    }

    /** Returns the name of the Browser.
     *  @return The name of the Browser
     *  @see org.web3d.x3d.sai.Browser#getName
     */
    public String getName() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserName;
    }

    /** @see org.web3d.x3d.sai.Browser#getProfile */
    public ProfileInfo getProfile(String profileName) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        ProfileInfo[] profiles=getSupportedProfiles();
        for(int counter=0; counter<profiles.length; counter++)
            if(profiles[counter].getName().equals(profileName))
                return profiles[counter];
        throw new NotSupportedException();
    }

    /** @see org.web3d.x3d.sai.Browser#getSupportedComponents */
    public ComponentInfo[] getSupportedComponents() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        org.web3d.vrml.lang.ComponentInfo[] components=
            getVRMLNodeFactory().getAvailableComponents();
        ComponentInfo results[]=new ComponentInfo[components.length];
        if(results==null)
            return new ComponentInfo[0];
        for(int counter=0;counter<results.length;counter++)
            results[counter]=new SAIComponentInfo(
            components[counter]
            );
        return results;
    }

    /** @see org.web3d.x3d.sai.Browser#getSupportedProfiles */
    public ProfileInfo[] getSupportedProfiles() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        org.web3d.vrml.lang.ProfileInfo profiles[]=
            getVRMLNodeFactory().getAvailableProfiles();
        if(profiles==null)
            throw new RuntimeException("Null array from getAvailableProfiles");
        else {
            ProfileInfo[] result=new ProfileInfo[profiles.length];
            for(int counter=0; counter<profiles.length; counter++) {
                result[counter]=new SAIProfileInfo(profiles[counter]);
            }
            return result;
        }
    }

    /**
     * Returns the version string for this Browser.
     * @return The version string for this Browser
     * @see org.web3d.x3d.sai.Browser#getVersion
     */
    public String getVersion() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserVersion;
    }

    /** @see org.web3d.x3d.sai.Browser#importDocument */
    public X3DScene importDocument(Node aDocument) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);
        try {
            VRMLScene scene = browserImpl.importDocument(aDocument);

            VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();

            return new SAIScene(scene,
                routeManager,
                stateManager,
                saiNodeFactory,
                eventQueue,
                space,
                errorReporter);
        } catch (Exception e) {
            throw new InvalidDocumentException("Unable to process document.  Reason:  "+e.getMessage());
        }
    }

    /** @see org.web3d.x3d.sai.Browser#loadURL */
    public void loadURL(String[] urls, Map params)
        throws InvalidBrowserException, InvalidURLException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);


        browserImpl.loadURL(urls,params);
    }

    /** * @see org.web3d.x3d.sai.Browser#replaceWorld */
    public void replaceWorld(X3DScene scene)
        throws IllegalArgumentException, InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(scene instanceof SAIScene)
            browserImpl.replaceWorld(((SAIScene)scene).getRealScene());
        else if(scene == null)
            browserImpl.replaceWorld(null);
        else
            throw new IllegalArgumentException("Incorrect scene type.");
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

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserImpl.getDescription();
    }

    /** @see org.web3d.x3d.sai.Browser#setDescription */
    public void setDescription(String newDescription)
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.setDescription(newDescription);
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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserProperties;
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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.nextViewpoint();
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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.nextViewpoint(layer);
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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.previousViewpoint();
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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.previousViewpoint(layer);
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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.firstViewpoint();
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
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.firstViewpoint(layer);
    }

    /**
     * Bind the last viewpoint in the list.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void lastViewpoint()
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.lastViewpoint();
    }

    /**
     * Bind the last viewpoint in the list.
     *
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void lastViewpoint(int layer)
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.lastViewpoint(layer);
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

        if(browserImpl == null)
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

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(msg != null)
            errorReporter.messageReport(msg.toString());
    }

    //---------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------

    /** @see org.web3d.browser.BrowserCoreListener#browserInitialized */
    public void browserInitialized(VRMLScene newScene) {
        broadcastEvent(new BrowserEvent(this, BrowserEvent.INITIALIZED));
    }

    /**
     * The browser tried to load a URL and failed. It is typically because
     * none of the URLs resolved to anything valid or there were network
     * failures.
     *
     * @param msg An error message to go with the failure
     */
    public void urlLoadFailed(String msg) {
        broadcastEvent(new BrowserEvent(this, BrowserEvent.URL_ERROR));
    }

    /** @see org.web3d.browser.BrowserCoreListener#browserShutdown */
    public void browserShutdown() {
        broadcastEvent(new BrowserEvent(this, BrowserEvent.SHUTDOWN));
    }

    /** @see org.web3d.browser.BrowserCoreListener#browserDisposed */
    public void browserDisposed() {
        if( browserImpl != null ) {
            // if the browser core is being disposed and this client
            // has not already been disposed - take care of it now.
            dispose( );
        }
    }

    //---------------------------------------------------------
    // Internal implementation methods
    //---------------------------------------------------------

    /**
     * Initialize the world to a known state.
     * This is necessary to ensure that all of the underlying behaviors
     * are active (which the buffering system relies on).  As such, this
     * method calls down to the browser core and common browser instances
     * directly rather than using the SAI methods.
     */
    public void initializeWorld() {
    }

    /**
     * Internal convenience routine for sending events to all listeners.
     * Not very efficient, but faithful to the wording of the spec.
     */
    private void broadcastEvent(BrowserEvent e) {
        if(browserListener != null)
            eventExecutor.submit(new BrowserEventTask(browserListener, e));
    }

    /** Get a valid VRMLNodeFactory instance */
    private VRMLNodeFactory getVRMLNodeFactory() {
        if(vrmlNodeFactory == null) {
            X3DExecutionContext context=getExecutionContext();
            if(context == null) {
                // resort to making a new scene and getting its node factory
                // Would be really nice to have a method on X3DCommonBrowser
                // that produced an appropriate VRMLNodeFactory.
                SAIScene basicScene =
                    (SAIScene)(createX3DFromString(MINIMAL_X3D_SCENE));
                vrmlNodeFactory=basicScene.getVRMLNodeFactory();
            } else {
                vrmlNodeFactory=((SAIScene)(context)).getVRMLNodeFactory();
            }
        }
        return vrmlNodeFactory;
    }

    /**
     * Search for and return a higher conformance level node factory
     *
     * @return An SAINodeFactory, or null if it could not be found
     */
    private SAINodeFactory getSAINodeFactory(SAIFieldFactory fieldFactory,
                                             ExternalEventQueue queue) {
        SAINodeFactory factory = null;
        try {
            // get the class instance
            Class factoryClass = Class.forName(NODE_FACTORY_CLASS_NAME);

            // get the class's constructor with the appropriate arguments
            Constructor constructor =
                factoryClass.getConstructor(new Class[]{
                                            SAIFieldFactory.class,
                                            ExternalEventQueue.class } );

            // instantiate the object
            Object object =
                constructor.newInstance(new Object[]{ fieldFactory, queue});

            // cast it to ensure it's the type we want
            factory = (SAINodeFactory)object;

        } catch (Exception e) {
        }

        return factory;
    }
}
