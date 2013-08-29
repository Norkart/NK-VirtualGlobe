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

package org.xj3d.impl.core.loading;

// External imports
import java.io.IOException;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.IntHashMap;
import org.web3d.util.ObjectArray;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.sav.VRMLReader;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;

import org.xj3d.core.loading.SceneBuilder;
import org.xj3d.core.loading.SceneBuilderFactory;
import org.xj3d.core.loading.WorldLoader;

/**
 * Internal default implementation of the WorldLoader interface.
 * <p>
 *
 * The default implementation does a lot of caching of internal structures to
 * try to save on both memory consumption and startup time wherever possible.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class DefaultWorldLoader implements WorldLoader {

    /** Map for the renderer to scene builder mapping */
    private static IntHashMap builderFactoryMap;

    /**
     * Map of renderer type to a list of the available scene builder
     * instances. Used for caching.
     */
    private static IntHashMap builderInstanceMap;

    /** Map for the renderer to parser mapping */
    private static IntHashMap parserFactoryMap;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The frame state manager for nodes loaded by this class */
    private FrameStateManager stateManager;

    /**
     * Static initializer to get the caching set up correctly.
     */
    static {
        builderFactoryMap = new IntHashMap();
        builderInstanceMap = new IntHashMap();
        parserFactoryMap = new IntHashMap();
    }

    /**
     * Construct a new instance of the world loader that uses the given
     * frame state manager.
     *
     * @param fsm The state manager for this loader to use
     */
    DefaultWorldLoader(FrameStateManager fsm) {
        stateManager = fsm;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //-----------------------------------------------------------------------
    // Methods defined by WorldLoader
    //-----------------------------------------------------------------------

    /**
     * Request to load the world immediately. The method is blocking and will
     * not return until the base file has been loaded. It will not load any
     * chained content such as externprotos, scripts, textures etc.
     *
     * @param source The source to take the content from
     * @param core The browser core needed for obtaining information
     * @return The completely loaded scene
     * @throws IOException There was an I/OError reading the file
     * @throws VRMLParseException Some parsing error occurred during this
     *    scene processing
     */
    public VRMLScene loadNow(BrowserCore core, InputSource source)
        throws IOException, VRMLParseException {
        return loadNow(core, source, false);
    }

    /**
     * Request to load the world immediately. The method is blocking and will
     * not return until the base file has been loaded. It will not load any
     * chained content such as externprotos, scripts, textures etc.
     *
     * @param source The source to take the content from
     * @param core The browser core needed for obtaining information
     * @param needsHeaher false if it should look for the file header to
     *        check version information
     * @return The completely loaded scene
     * @throws IOException There was an I/OError reading the file
     * @throws VRMLParseException Some parsing error occurred during this
     *    scene processing
     */
    public VRMLScene loadNow(BrowserCore core,
                             InputSource source,
                             boolean ignoreHeader)
        throws IOException, VRMLParseException {

        int renderer = core.getRendererType();

        SceneBuilder bldr = getBuilder(renderer);

        if(bldr == null)
            throw new VRMLException ("Unable to find a builder to locate " +
                                     "SceneBuilder instance");

        VRMLParserFactory parser = getParserFactory(renderer);

        if(parser == null)
            throw new VRMLException("Unable to find a builder to locate " +
                                    "Parser instance");

        bldr.reset();
        bldr.setFrameStateManager(stateManager);
        bldr.setErrorReporter(errorReporter);

        VRMLReader vrml_reader = parser.newVRMLReader();

        vrml_reader.setHeaderIgnore(ignoreHeader);
        vrml_reader.setContentHandler(bldr);
        vrml_reader.setScriptHandler(bldr);
        vrml_reader.setProtoHandler(bldr);
        vrml_reader.setRouteHandler(bldr);
        vrml_reader.setErrorReporter(errorReporter);

        vrml_reader.parse(source);

        VRMLScene scene = bldr.getScene();
		bldr.releaseScene();

        VRMLNodeType rootSpace = (VRMLNodeType) scene.getRootNode();
        rootSpace.setFrameStateManager(stateManager);

        // release back to the cache
        releaseBuilder(renderer, bldr);

        return scene;
    }

    /**
     * Request to load the world immediately and constrain that loading to
     * using a specific specification version. The method is blocking and will
     * not return until the base file has been loaded. It will not load any
     * chained content such as externprotos, scripts, textures etc.
     * <p>
     *
     * A major version of 0 means to ignore the required version and just
     * load whatever can be found.
     *
     * @param source The source to take the content from
     * @param core The browser core needed for obtaining information
     * @param needsHeaher false if it should look for the file header to
     *        check version information
     * @param majorVersion Require the given major version
     * @param minorVersion Require the given minor version
     * @return The completely loaded scene
     * @throws IOException There was an I/OError reading the file
     * @throws VRMLParseException Some parsing error occurred during this
     *    scene processing
     */
    public VRMLScene loadNow(BrowserCore core,
                             InputSource source,
                             boolean ignoreHeader,
                             int majorVersion,
                             int minorVersion)
        throws IOException, VRMLParseException {

// TODO: Later stages come back as 3 and use the wrong parser for XML booleans.
//majorVersion = 0;

        if(majorVersion == 0)
            return loadNow(core, source, ignoreHeader);

        int renderer = core.getRendererType();

        SceneBuilder bldr = getBuilder(renderer);

        if(bldr == null)
            throw new VRMLException ("Unable to find a builder to locate " +
                                     "SceneBuilder instance");

        VRMLParserFactory parser = getParserFactory(renderer);

        if(parser == null)
            throw new VRMLException("Unable to find a builder to locate " +
                                    "Parser instance");

        bldr.reset();
        bldr.setFrameStateManager(stateManager);
        bldr.setErrorReporter(errorReporter);

        String version = majorVersion + "." + minorVersion;

        parser.setProperty(VRMLParserFactory.REQUIRE_VERSION_PROP,version);
        VRMLReader vrml_reader = parser.newVRMLReader();

        vrml_reader.setHeaderIgnore(ignoreHeader);
        vrml_reader.setContentHandler(bldr);
        vrml_reader.setScriptHandler(bldr);
        vrml_reader.setProtoHandler(bldr);
        vrml_reader.setRouteHandler(bldr);

        vrml_reader.parse(source);

        VRMLScene scene = bldr.getScene();
		bldr.releaseScene();

        VRMLNodeType rootSpace = (VRMLNodeType) scene.getRootNode();
        rootSpace.setFrameStateManager(stateManager);

        // release back to the cache
        releaseBuilder(renderer, bldr);

        return scene;
    }

    /** Shutdown the loader, release any resources */
    public void shutdown( ) {
        builderFactoryMap.clear( );
        builderInstanceMap.clear( );
        parserFactoryMap.clear( );
    }
    
    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register the scene builder factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the facctory for the given renderer type from the
     * map.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory to use
     */
    static void registerBuilderFactory(int renderer,
                                       SceneBuilderFactory factory) {
        if(factory == null)
            builderFactoryMap.remove(renderer);
        else
            builderFactoryMap.put(renderer, factory);
    }

    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory or null
     */
    static SceneBuilderFactory getBuilderFactory(int renderer) {
        return (SceneBuilderFactory)builderFactoryMap.get(renderer);
    }

    /**
     * Register the parser factory to be used for the given renderer
     * type. There can only be one for any given renderer type (where the type
     * value is defined by the constants in
     * {@link org.web3d.browser.BrowserCore}. If the factory instance is
     * null, it will clear the facctory for the given renderer type from the
     * map.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory to use
     */
    static void registerParserFactory(int renderer, VRMLParserFactory factory) {
        if(factory == null)
            parserFactoryMap.remove(renderer);
        else
            parserFactoryMap.put(renderer, factory);
    }

    /**
     * Get the factory for the given renderer type. If no factory exists
     * return null.
     *
     * @param renderer The ID of the renderer type
     * @param factory The instance of the factory or null
     */
    static VRMLParserFactory getParserFactory(int renderer) {
        return (VRMLParserFactory)parserFactoryMap.get(renderer);
    }

    /**
     * Fetch a scene builder instance for the given renderer type. It will
     * attempt to grab one from the cache, but will create another if all
     * the available instances are in use. If no factory is registered for
     * the type it will return null.
     *
     * @param renderer The ID of the renderer type
     * @return An instance of a scene builder for the renderer
     */
    private static synchronized SceneBuilder getBuilder(int renderer) {

        ObjectArray items = (ObjectArray)builderInstanceMap.get(renderer);
        SceneBuilder ret_val = null;

        if((items != null) && (items.size() != 0))
            ret_val = (SceneBuilder)items.remove(items.size() - 1);
        else {
            SceneBuilderFactory fac = getBuilderFactory(renderer);

            if(fac != null)
                ret_val = fac.createBuilder();
        }

        return ret_val;
    }


    /**
     * Release the scene builder instance back to the cache.
     *
     * @param renderer The ID of the renderer type
     * @param builder the builder instance to insert
     */
    private static void releaseBuilder(int renderer, SceneBuilder builder) {
        ObjectArray items = (ObjectArray)builderInstanceMap.get(renderer);

        if(items == null) {
            items = new ObjectArray();
            builderInstanceMap.put(renderer, items);
        }

        items.add(builder);
    }
}
