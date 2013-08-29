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

package org.xj3d.loaders.j3d;

// External imports
import java.io.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Behavior;

import com.sun.j3d.loaders.LoaderBase;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;

import org.ietf.uri.ContentHandlerFactory;
import org.ietf.uri.FileNameMap;
import org.ietf.uri.URI;
import org.ietf.uri.URN;

import org.j3d.renderer.java3d.loaders.ManagedLoader;

// Local imports
import org.web3d.vrml.parser.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.resolve.Web3DURNResolver;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;
import org.web3d.vrml.renderer.j3d.J3DSceneBuilderFactory;
import org.web3d.vrml.renderer.j3d.browser.VRMLBranchGroup;
import org.web3d.vrml.renderer.j3d.input.DefaultSensorManager;
import org.web3d.vrml.renderer.j3d.input.UserInputBehavior;
import org.web3d.vrml.renderer.j3d.input.J3DUserInputHandler;
import org.web3d.vrml.renderer.j3d.input.J3DSensorManager;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.sav.VRMLReader;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;

import org.xj3d.impl.core.eventmodel.DefaultEventModelEvaluator;
import org.xj3d.impl.core.eventmodel.DefaultFrameStateManager;
import org.xj3d.impl.core.eventmodel.DefaultViewpointManager;

import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;

/**
 * A Java3D file loader implementation for reading X3D utf8 files only and
 * building a Java3D scenegraph with them.
 * <p>
 *
 * The loader considers sensor handling and routing to be behaviours. Some
 * asynchronous loading of files for textures is performed. Sound file loading
 * is performed if audio clips are asked for. For example, if behaviours are
 * not requested then Inlines will not have their content loaded.
 * <p>
 *
 * If the loader asks for no behaviors, then we will still load nodes that
 * use behaviors, but will disable their use. For example, a LOD will still
 * need to have all of the geometry loaded, just not shown or activated
 * because the LOD's internal behavior is disabled. Scripts are considered
 * to be behaviours, and they will not be loaded at all if behaviour loading
 * is disabled.
 * <p>
 *
 * The implementation only makes use of two behaviours. One is a per-frame
 * behaviour for the event model evaluation. The other is a handler for
 * trapping user input events. If you disable behaviours, you loose both
 * of these. For content other than static geometry, such as animations,
 * turning off behaviours will result in no animations. However, every loaded
 * scene will be attempting to do work like navigation. This will become quite
 * CPU intensive because every model will be performing picking operations.
 * To cut down on this CPU usage, the navigation processing is turned off
 * by default. If you want the loaded code to also do the navigation of the
 * viewpoints, then you can call the setNavigationEnable() method.
 *
 * The default setup for runtime activities is
 * {@link org.xj3d.core.eventmodel.ListsRouterFactory} and
 * {@link org.xj3d.impl.core.loading.MemCacheLoadManager}
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public abstract class BaseLoader extends LoaderBase
    implements ManagedLoader {

    /** The parser factory that we are going to use. */
    protected VRMLParserFactory parserFactory;

    /** Global flag to indicate properties have been set up already */
    private static boolean setupComplete = false;

    /** Flag to say if the scene builder factory should only use VRML97 */
    protected boolean vrml97Only = false;

    /** The map of the override capability bit settings */
    protected Map overrideCapBitsMap;

    /** The map of the required capability bit settings */
    protected Map requiredCapBitsMap;

    /** The map of the override capability bit settings */
    protected Map overrideFreqBitsMap;

    /** The map of the required capability bit settings */
    protected Map requiredFreqBitsMap;

    /** Class that represents the external reporter */
    protected ErrorReporter errorReporter;

    /** The high-level VRML scene that was parsed to create the J3D scene */
    protected VRMLScene parsedScene;

    /** Flag to say if navigation handling should be disabled */
    private boolean navigationEnabled;

    /** Resolver for processing URNs like Hamin and GeoVRML */
    private Web3DURNResolver resolver;

    /**
     * Construct a default loader implementation with no flags set. When asked
     * to load a file it will not produce anything unless flags have been
     * set through the <code>setFlags()</code> method.
     */
    protected BaseLoader() {
        this(0);
    }

    /**
     * Construct a loader with the given flags set.
     *
     * @param flags The flag values to be used
     * @throws RuntimeException The factory for loading VRML content could
     *   not be found
     */
    public BaseLoader(int flags) {
        super(flags);

        resolver = new Web3DURNResolver();
        navigationEnabled = false;

        try {
            parserFactory = VRMLParserFactory.newVRMLParserFactory();
        } catch(FactoryConfigurationError fce) {
            throw new RuntimeException("Failed to load factory");
        }
    }

    //----------------------------------------------------------
    // Methods overriding BaseLoader
    //----------------------------------------------------------

    /**
     * Load the scene from the given reader. The
     * scene instance returned by this builder will not have had any external
     * references resolved. Externprotos, scripts, Inlines and all other nodes
     * that reference part of their data as a URL will need to be loaded
     * separately.
     *
     * @param reader The source of input characters
     * @return A description of the scene
     * @throws IncorrectFormatException The file is not one our loader
     *    understands (VRML 1.0 or X3D content)
     * @throws ParsingErrorException An error parsing the file
     */
    public Scene load(Reader reader)
        throws IncorrectFormatException, ParsingErrorException {

        URL url = getBaseUrl();
        String worldURL = null;

        if (url == null) {
            try {
                worldURL = (String) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() {
                            return System.getProperty("user.dir");
                        }
                    }
                );
            } catch (PrivilegedActionException pae) {
                System.out.println("Cannot get user dir in BaseLoader");
            }

        } else
            worldURL = url.toString();

        InputSource is = new InputSource(worldURL, reader);

        return load(is);
    }

    /**
     * Load a scene from the given filename. The scene instance returned by
     * this builder will not have had any external references resolved.
     * Externprotos, scripts, Inlines and all other nodes that reference part
     * of their data as a URL will need to be loaded separately.
     *
     * @param filename The name of the file to load
     * @return A description of the scene
     * @throws FileNotFoundException The reader can't find the file
     * @throws IncorrectFormatException The file is not one our loader
     *    understands (VRML 1.0 or X3D content)
     * @throws ParsingErrorException An error parsing the file
     */
    public Scene load(String filename)
        throws FileNotFoundException,
               IncorrectFormatException,
               ParsingErrorException {

        File file = new File(filename);

        if(!file.exists())
            throw new FileNotFoundException("File does not exist");

        if(file.isDirectory())
            throw new FileNotFoundException("File is a directory");

        InputSource is = new InputSource(file);

        return load(is);
    }

    /**
     * Load a scene from the named URL. The scene instance returned by
     * this builder will not have had any external references resolved.
     * Externprotos, scripts, Inlines and all other nodes that reference part
     * of their data as a URL will need to be loaded separately.
     *
     * @param url The URL instance to load data from
     * @return A description of the scene
     * @throws FileNotFoundException The reader can't find the file
     * @throws IncorrectFormatException The file is not one our loader
     *    understands (VRML 1.0 or X3D content)
     * @throws ParsingErrorException An error parsing the file
     */
    public Scene load(URL url)
        throws FileNotFoundException,
               IncorrectFormatException,
               ParsingErrorException {

        InputSource is = new InputSource(url);

        return load(is);
    }

    //----------------------------------------------------------
    // Methods required ManagedLoader
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        overrideCapBitsMap = capBits;
        overrideFreqBitsMap = freqBits;
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        requiredCapBitsMap = capBits;
        requiredFreqBitsMap = freqBits;
    }

    //----------------------------------------------------------
    // Public methods
    //----------------------------------------------------------

    /**
     * Get the currently set navigation state.
     *
     * @return true for the current state
     */
    public boolean getNavigationEnabled() {
        return navigationEnabled;
    }

    /**
     * Enable or disable navigation processing sub-section of the
     * user input processing. By default the navigation processing is enabled.
     *
     * @param state true to enable navigation
     */
    public void setNavigationEnabled(boolean state) {
        navigationEnabled = state;
    }

    /**
     * Fetch the high-level Scene from the last parsed file that Xj3D uses to
     * represent the VRML scene graph. This will give you access to all the
     * real node representations, particularly those that do not have a Java3D
     * SceneGraphObject equivalent representation and cannot be fetched
     * through the usual Loader/Scene interfaces. If no file has been parsed
     * yet, this will return null.
     *
     * @return A representation of the high-level scene or null
     */
    public VRMLScene getVRMLScene() {
        return parsedScene;
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Add a prefix and directory to the URN resolution system. Whenever the
     * prefix is found under the web3d area, this directory will be searched.
     *
     * @param prefix The subspace prefix to use
     * @param directory The directory that GeoVRML is installed in
     * @throws IllegalArgumentException The directory is not valid
     */
    public void registerURNLocation(String prefix, String directory)
        throws IllegalArgumentException {

        resolver.registerPrefixLocation(prefix, directory);
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return The scene description
     * @throws IncorrectFormatException The file is not one our loader
     *    understands (VRML 1.0 or X3D content)
     * @throws ParsingErrorException An error parsing the file
     */
    private Scene load(InputSource is)
        throws IncorrectFormatException,
               ParsingErrorException {

        // set the loading flags. Ignore the behaviours for the moment
        // as we don't have anything to translate that to.
        boolean use_bg = (loadFlags & LOAD_BACKGROUND_NODES) != 0;
        boolean use_fog = (loadFlags & LOAD_FOG_NODES) != 0;
        boolean use_light = (loadFlags & LOAD_LIGHT_NODES) != 0;
        boolean use_audio = (loadFlags & LOAD_SOUND_NODES) != 0;
        boolean use_view = (loadFlags & LOAD_VIEW_GROUPS) != 0;
        boolean use_behaviors = (loadFlags & LOAD_BEHAVIOR_NODES) != 0;

        // Set the load requirements. We duplicate the load behavior flag
        // for both the external and behavior node requirements.
        SceneBuilderFactory builder_fac =
            new J3DSceneBuilderFactory(vrml97Only,
                                       use_bg,
                                       use_fog,
                                       use_light,
                                       use_audio,
                                       use_view,
                                       use_behaviors);

        VRMLParserFactory parser_fac = null;

        try {
            parser_fac = VRMLParserFactory.newVRMLParserFactory();
        } catch(FactoryConfigurationError fce) {
            throw new RuntimeException("Failed to load factory");
        }

        FrameStateManager fsm = new DefaultFrameStateManager();

        J3DScene ret_val = new J3DScene();

        // If we are using behaviours, use a different codepath that sets up
        // everything that you need to know about to make it run.
        if(use_behaviors) {

            RouteManager rm = new DefaultRouteManager();
            rm.setRouterFactory(new ListsRouterFactory());

            ScriptManager s_mgr = new DefaultScriptManager();
            J3DSensorManager sens_mgr = new DefaultSensorManager();
            sens_mgr.setNavigationEnabled(navigationEnabled);

            EventModelEvaluator event_model = new DefaultEventModelEvaluator();
            ContentLoadManager load_mgr = new MemCacheLoadManager();

            DefaultHumanoidManager hanim_manager = new DefaultHumanoidManager();
            NetworkManager network_manager = new DefaultNetworkManager();
            DISProtocolHandler dis_handler = new DISProtocolHandler();
            network_manager.addProtocolHandler(dis_handler);

            NodeManager[] node_mgrs = { hanim_manager, network_manager };

            ViewpointManager vp_mgr = new DefaultViewpointManager(universe);

            event_model.initialize(s_mgr, rm, sens_mgr, fsm, load_mgr,
                                   vp_mgr, node_mgrs);

            event_model.setErrorReporter(errorReporter);

            VRMLBranchGroup root_group =
                new VRMLBranchGroup(!use_behaviors, event_model);

            WorldLoaderManager w_loader =
                new DefaultWorldLoaderManager(root_group, fsm, rm);
            w_loader.setErrorReporter(errorReporter);
            w_loader.registerBuilderFactory(Xj3DConstants.JAVA3D_RENDERER,
                                            builder_fac);
            w_loader.registerParserFactory(Xj3DConstants.JAVA3D_RENDERER,
                                           parser_fac);

            WorldLoader ldr = w_loader.fetchLoader();

            setupProperties(root_group, w_loader);

            try {
                parsedScene = ldr.loadNow(root_group, is);
            } catch(IOException ioe) {
                throw new ParsingErrorException(ioe.getMessage());
            }

            w_loader.releaseLoader(ldr);

            // Construct the root scene to return;
            UserInputBehavior i_buf = new UserInputBehavior();
            ret_val.addBehavior(i_buf);
            root_group.addChild(i_buf);

            J3DUserInputHandler ui_handler = (J3DUserInputHandler) sens_mgr.getUserInputHandler();
            ui_handler.setNavigationEnabled(navigationEnabled);

            ScriptLoader s_loader = new DefaultScriptLoader();
            s_mgr.setScriptLoader(s_loader);

            ScriptEngine jsai =
                new VRML97ScriptEngine(root_group, rm, fsm, w_loader);

            ScriptEngine ecma =
                new JavascriptScriptEngine(root_group, rm, fsm, w_loader);
            jsai.setErrorReporter(errorReporter);
            ecma.setErrorReporter(errorReporter);

            s_loader.registerScriptingEngine(jsai);
            s_loader.registerScriptingEngine(ecma);

            // Add the behaviour that is per frame behaviour
            Behavior[] beh = root_group.getSystemBehaviors();

            for(int i = 0; i < beh.length; i++)
                ret_val.addBehavior(beh[i]);

            root_group.setScene(parsedScene, null);
            ret_val.setRootNode(root_group);

        } else {
            RouteManager rm = new StaticRouteManager();
            BrowserCore core = new StaticBrowserCore();

            WorldLoaderManager w_loader =
                new DefaultWorldLoaderManager(core, fsm, rm);
            w_loader.setErrorReporter(errorReporter);
            w_loader.registerBuilderFactory(Xj3DConstants.JAVA3D_RENDERER,
                                            builder_fac);
            w_loader.registerParserFactory(Xj3DConstants.JAVA3D_RENDERER,
                                           parser_fac);

            setupProperties(core, w_loader);

            // Not using behaviours, so run with a static loader.
            SequentialContentLoader loader =
                new SequentialContentLoader(parser_fac,
                                            builder_fac,
                                            fsm,
                                            core);

            ScriptEngine jsai = new VRML97ScriptEngine(core,
                                                       rm,
                                                       fsm,
                                                       w_loader);
            jsai.setErrorReporter(errorReporter);

            ScriptEngine ecma =
                new JavascriptScriptEngine(core, rm, fsm, w_loader);
            ecma.setErrorReporter(errorReporter);

            loader.registerScriptingEngine(jsai);
            loader.registerScriptingEngine(ecma);

            try {
                parsedScene = loader.loadContent(is);
            } catch(IOException ioe) {
                throw new ParsingErrorException(ioe.getMessage());
            }

            J3DVRMLNode world_root = (J3DVRMLNode)parsedScene.getRootNode();
            BranchGroup root_node = (BranchGroup)world_root.getSceneGraphObject();

            ret_val.setRootNode(root_node);
        }

        ret_val.setValues(parsedScene);

        return ret_val;
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     * May be overridden by derived classes, but should also call this
     * for the standard setup.
     *
     * @param core The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    private void setupProperties(final BrowserCore core,
                                 final WorldLoaderManager wlm) {

        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    // Disable font cache to fix getBounds nullPointer bug
                    System.setProperty("sun.awt.font.advancecache","off");

                    if(System.getProperty("uri.content.handler.pkgs") == null)
                        System.setProperty("uri.content.handler.pkgs",
                                           "vlc.net.content");

                    if(System.getProperty("uri.protocol.handler.pkgs") == null)
                        System.setProperty("uri.protocol.handler.pkgs",
                                           "vlc.net.protocol");

                    ContentHandlerFactory c_fac =
                        URI.getContentHandlerFactory();

                    if(!(c_fac instanceof VRMLContentHandlerFactory)) {
                        c_fac = new VRMLContentHandlerFactory(core, wlm);
                        URI.setContentHandlerFactory(c_fac);
                    }


                    FileNameMap fn_map = URI.getFileNameMap();
                    if(!(fn_map instanceof VRMLFileNameMap)) {
                        fn_map = new VRMLFileNameMap(fn_map);
                        URI.setFileNameMap(fn_map);
                    }

                    URN.addResolver(resolver);

                    setupPropertiesProtected();
                    return null;
                }
            }
        );

        setupComplete = true;
    }

    /**
     * Set up the system properties needed to run the browser within the
     * context of a privileged block. Default implementation is empty. May be
     * overridded by derived class.
     */
    void setupPropertiesProtected() {
    }
}
