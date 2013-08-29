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
import org.ietf.uri.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.browser.BrowserCore;
import org.web3d.util.ErrorReporter;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.VRMLParseException;
import org.web3d.vrml.sav.VRMLReader;
import org.web3d.vrml.scripting.InvalidScriptContentException;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.ScriptWrapper;

import org.xj3d.core.loading.SceneBuilder;
import org.xj3d.core.loading.SceneBuilderFactory;

/**
 * A behind the scenes utility class that is responsible for loading
 * all external content for a world and then recursing through the scenes.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SequentialContentLoader {

    /** The MIME type to represent Java files */
    private static final String JAVA_MIME = "application/x-java";

    /**
     * The cache that we use. Everyone joins in in order to get maximum
     * amount of caching.
     */
    private static WeakRefFileCache cache = new WeakRefFileCache();

    /** Browser core instance needed for loading */
    private BrowserCore browserCore;

    /** Frame state manager needed for the nodes */
    private FrameStateManager stateManager;

    /** Parser factory to use for nodes */
    private VRMLParserFactory parserFactory;

    /** Scene builder factory for creating worlds */
    private SceneBuilder builder;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Reader used to handle the content */
    private VRMLReader reader;

    /** Mapping of type name to script engine name */
    private HashMap engineMap;

    /**
     * Construct a new instance of the loader using the given factories.
     *
     * @param pFac The parser factory to parse files with
     * @param sFac The scene builder factory to assemble a scene with
     * @param fsm The state manager for frame behaviours
     * @param browser The core browser repsrentation
     */
    SequentialContentLoader(VRMLParserFactory pFac,
                            SceneBuilderFactory sFac,
                            FrameStateManager fsm,
                            BrowserCore browser) {

        parserFactory = pFac;
        stateManager = fsm;
        browserCore = browser;

        builder = sFac.createBuilder();
        builder.setFrameStateManager(stateManager);

        engineMap = new HashMap();
    }

    /**
     * Time to load the file. This is a blocking call that does not return
     * until all of the content has been loaded.
     *
     * @param input The input source to read the data from
     * @return A scene describing the loaded file
     * @throws IOException Some I/O error reading the stream
     */
    VRMLScene loadContent(InputSource input) throws IOException {

        reader = parserFactory.newVRMLReader();
        reader.setContentHandler(builder);
        reader.setScriptHandler(builder);
        reader.setProtoHandler(builder);
        reader.setRouteHandler(builder);
        reader.parse(input);

        VRMLScene scene = builder.getScene();
        loadScene(scene);

        return scene;
    }

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

        builder.setErrorReporter(errorReporter);
    }

    /**
     * Register a new scripting engine with this loader. There can be only one
     * scripting engine per content type so if the new engine supports a
     * content type that is already registered, the new engine will replace the
     * old one.
     *
     * @param engine The new engine instance to register
     */
    public void registerScriptingEngine(ScriptEngine engine) {
        String[] types = engine.getSupportedContentTypes();

        for(int i = 0; i < types.length; i++)
            engineMap.put(types[i], engine);
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Convenience method to load a single scene. Used to load the root world
     * and then for chain loading any inlined content as well.
     *
     * @param scene The scene instance to load
     */
    private void loadScene(VRMLScene scene) {
        VRMLExecutionSpace space =
            (VRMLExecutionSpace)scene.getRootNode();

        // With the scene, find everything that needs to be loaded. The order
        // is scripts, other content types, externprotos, inlines
        ArrayList nodes =
            scene.getByPrimaryType(TypeConstants.ScriptNodeType);

        int size = nodes.size();

        if(size != 0) {
            for(int i = 0; i < size; i++) {
                VRMLScriptNodeType script = (VRMLScriptNodeType)nodes.get(i);
                loadScript(script);
            }
        }

        nodes = scene.getByPrimaryType(TypeConstants.InlineNodeType);
        size = nodes.size();

        if(size != 0) {
            for(int i = 0; i < size; i++) {
                VRMLInlineNodeType inline = (VRMLInlineNodeType)nodes.get(i);
                loadInlineNode(inline);
            }
        }

        // Now do all the other external nodes. Always filter the singles for
        // inlines and scripts as we don't need to load them twice.
        nodes = scene.getBySecondaryType(TypeConstants.SingleExternalNodeType);

        size = nodes.size();

        if(size != 0) {
            for(int i = 0; i < size; i++) {
                VRMLSingleExternalNodeType node =
                    (VRMLSingleExternalNodeType)nodes.get(i);

                if((node instanceof VRMLInlineNodeType) ||
                   (node instanceof VRMLScriptNodeType))
                    continue;

                loadExternalNode(node);
            }
        }

        nodes = scene.getBySecondaryType(TypeConstants.MultiExternalNodeType);
        size = nodes.size();

        if(size != 0) {
            for(int i = 0; i < size; i++) {
                VRMLMultiExternalNodeType node =
                    (VRMLMultiExternalNodeType)nodes.get(i);

                loadExternalNode(node);
            }
        }
    }

    /**
     * Load a script node now.
     *
     * @param script The node to load the details for
     */
    private void loadExternalNode(VRMLSingleExternalNodeType node) {

        String[] urls;
        boolean content_found = false;
        Object content = null;
        String mime_type;

        if (node.getLoadState() == VRMLExternalNodeType.LOAD_COMPLETE)
            return;

        node.setLoadState(VRMLExternalNodeType.LOADING);
        urls = node.getUrl();

        int num_urls = (urls == null) ? 0 : urls.length;

        for(int i = 0; !content_found && (i < num_urls); i++) {

            // check the string for a # and remove the reference
            String file_url = urls[i];
            int index;

            if((index = file_url.lastIndexOf('#')) != -1)
                file_url = file_url.substring(0, index);

            // Check the cache first to see if we have something here
            CacheDetails cached_version = cache.checkForFile(file_url);

            if(cached_version != null) {
                mime_type = cached_version.getContentType();
                content = cached_version.getContent();

                if(!node.checkValidContentType(mime_type))
                    continue;

                node.setLoadedURI(urls[i]);
                node.setContent(mime_type, content);

                content_found = true;
            } else {
                content_found = loadExternal(urls[i], file_url, node);
            }
        }

        if(content_found) {
            node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
        } else
            node.setLoadState(VRMLExternalNodeType.LOAD_FAILED);
    }

    /**
     * Load a complete Multi-external node now.
     *
     * @param script The node to load the details for
     */
    private void loadExternalNode(VRMLMultiExternalNodeType node) {
        int[] index_list = node.getUrlFieldIndexes();

        for(int i = 0; i < index_list.length; i++)
            loadExternalNode(node, index_list[i]);
    }


    /**
     * Load a single field from a multi node now.
     *
     * @param script The node to load the details for
     */
    private void loadExternalNode(VRMLMultiExternalNodeType node, int field) {
        String[] urls;
        boolean content_found = false;
        Object content;
        String mime_type;

        if(node.getLoadState(field) == VRMLExternalNodeType.LOAD_COMPLETE)
            return;

        node.setLoadState(field, VRMLExternalNodeType.LOADING);
        urls = node.getUrl(field);

        int num_urls = (urls == null) ? 0 : urls.length;

        for(int i = 0; !content_found && (i < num_urls); i++) {

            // check the string for a # and remove the reference
            String file_url = urls[i];
            int index;

            if((index = file_url.lastIndexOf('#')) != -1)
                file_url = file_url.substring(0, index);

            // Check the cache first to see if we have something here
            CacheDetails cached_version = cache.checkForFile(file_url);

            if(cached_version != null) {
                mime_type = cached_version.getContentType();
                content = cached_version.getContent();

                if(!node.checkValidContentType(field, mime_type))
                    continue;

                node.setLoadedURI(field, urls[i]);
                node.setContent(field, mime_type, content);

                content_found = true;
            } else {
                content_found = loadExternal(urls[i], file_url, field, node);
            }
        }

        if(content_found)
            node.setLoadState(field, VRMLExternalNodeType.LOAD_COMPLETE);
        else
            node.setLoadState(field, VRMLExternalNodeType.LOAD_FAILED);
    }

    /**
     * Load an inline node now. The implementation currently does not cache the
     * inlined file and copy the scene when it discovers a new version of it
     * around.
     *
     * @param inline The node to load the details for
     */
    private void loadInlineNode(VRMLInlineNodeType inline) {
        boolean content_found = false;

        String[] url_list = inline.getUrl();
        URL[] source_urls = null;
        String mime_type;

        for(int i = 0; i < url_list.length; i++) {

            try {
                URI uri = URIUtils.createURI(url_list[i]);
                source_urls = uri.getURLList();
            } catch(IOException ioe) {
                // ignore and move on
                continue;
            }

            if(source_urls == null)
                continue;

            // loop through the list of candidate URLs and look for
            // something that matches. If it does, set it in the node
            // for use.
            for(int j = 0; (j < source_urls.length); j++) {

                try {
                    InputSource input = new InputSource(source_urls[j]);
                    builder.reset();
                    reader.parse(input);

                    VRMLScene scene = builder.getScene();

                    inline.setContent("model/vrml", scene);
                    loadScene(scene);
                } catch(IOException ioe) {
                    // ignore and move on
                    continue;
                }
            }
        }
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     */
    private boolean loadExternal(String origUri,
                                 String fileUri,
                                 VRMLSingleExternalNodeType sNode) {

        boolean content_found = false;

        // Always assume a fully qualified URI
        URL[] source_urls = null;
        String mime_type;
        Object content = null;
        ResourceConnection connection;

        try {
            URI uri = URIUtils.createURI(fileUri);
            source_urls = uri.getURLList();
        } catch(IOException ioe) {
            // ignore and move on
            return false;
        }

        if(source_urls == null)
            return false;

        // loop through the list of candidate URLs and look for
        // something that matches. If it does, set it in the node
        // for use.
        for(int j = 0; (j < source_urls.length); j++) {
            try {
                connection = source_urls[j].getResource();
            } catch(IOException ioe) {
                continue;
            }

            try {
                if(!makeConnection(connection))
                    continue;

                mime_type = connection.getContentType();
                if(mime_type == null)
                    continue;

                content = connection.getContent();

                if (content == null)
                    continue;

                if(!sNode.checkValidContentType(mime_type)) {
                    connection.close();
                    continue;
                }

                sNode.setLoadedURI(origUri);
                sNode.setContent(mime_type, content);
                cache.cacheFile(fileUri, mime_type, content);

                // Yippee! made it. Break out of the loop and exit
                // the load process.
                content_found = true;
                break;

            } catch(IOException ioe) {
                // ignore and move on
                    continue;
            } catch(IllegalArgumentException iae) {
                // from the setContent method
                errorReporter.errorReport("Error setting external content:", iae);
                continue;
            } catch(InvalidFieldException ife) {
                continue;
            }
        } // for loop


        if(content instanceof VRMLScene)
            loadScene((VRMLScene)content);

        return content_found;
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     */
    private boolean loadExternal(String origUri,
                                 String fileUri,
                                 int field,
                                 VRMLMultiExternalNodeType mNode) {

        boolean content_found = false;

        // Always assume a fully qualified URI
        URL[] source_urls = null;
        String mime_type;
        Object content;
        ResourceConnection connection;

        try {
            URI uri = URIUtils.createURI(fileUri);

            source_urls = uri.getURLList();
        } catch(IOException ioe) {
            // ignore and move on
            return false;
        }

        if(source_urls == null)
            return false;

        // loop through the list of candidate URLs and look for
        // something that matches. If it does, set it in the node
        // for use.
        for(int j = 0; (j < source_urls.length); j++) {
            try {
                connection = source_urls[j].getResource();
            } catch(IOException ioe) {
                continue;
            }

            try {
                if(!makeConnection(connection))
                    continue;

                mime_type = connection.getContentType();
                if(mime_type == null)
                    continue;

                content = connection.getContent();

                if(content == null)
                    continue;

                if(!mNode.checkValidContentType(field, mime_type)) {
                    connection.close();
                    continue;
                }

                mNode.setLoadedURI(field, origUri);
                mNode.setContent(field, mime_type, content);

                // now cache it!
                cache.cacheFile(fileUri, mime_type, content);

                // Yippee! made it. Break out of the loop and exit
                // the load process.
                content_found = true;
                break;

            } catch(IOException ioe) {
                continue;
            } catch(IllegalArgumentException iae) {
                // from the setContent method
                errorReporter.errorReport("Error setting external content:", iae);
                continue;
            } catch(InvalidFieldException ife) {
                continue;
            }
        } // for loop

        return content_found;
    }

    /**
     * Load a script node now.
     *
     * @param script The node to load the details for
     */
    private void loadScript(VRMLScriptNodeType script) {

        script.setLoadState(VRMLExternalNodeType.LOADING);

        String[] urls = script.getUrl();
        URL[] source_urls = null;
        boolean content_found = false;
        Object content;
        String mime_type;
        ResourceConnection connection;

        for(int i = 0; !content_found && (i < urls.length); i++) {

            // check the string for a # and remove the reference
            // These really should not be here as it doesn't make sense
            // for scripts.
            String file_url = urls[i];
            int index;

            if((index = file_url.lastIndexOf('#')) != -1) {
                file_url = file_url.substring(0, index);
            }

            // Check the cache first to see if we have something here
            CacheDetails cached_version =
                cache.checkForFile(file_url);

            if(cached_version != null) {
                mime_type = cached_version.getContentType();

                ScriptEngine engine =
                    (ScriptEngine)engineMap.get(mime_type);

                if(engine == null)
                    continue;

                content = cached_version.getContent();
                content_found = true;

                try {
                    ScriptWrapper wrapper =
                        engine.buildWrapper(script.getExecutionSpace(),
                                            mime_type,
                                            content);

                    script.setContent(mime_type, wrapper);
                    continue;

                } catch(InvalidScriptContentException isce) {
                    // This should never happen unless someone changes the
                    // registered script engines on the fly. Something we
                    // Advise heavily against.
                    String msg = "Invalid content for script " + file_url;

                    errorReporter.errorReport(msg, isce);
                    continue;
                }
            }

            // Check the URL for ending in ./class. If this is the case, then
            // create a URLClassLoader and use that rather than going through the
            // URN system.
            if(file_url.endsWith(".class")) {
                // no point going any further if we don't have an engine capable
                // of supporting Java based scripts
                ScriptEngine eng = (ScriptEngine)engineMap.get(JAVA_MIME);

                content_found = loadClass(file_url, script, eng);
            }

            try {
                URI uri = URIUtils.createURI(file_url);
                source_urls = uri.getURLList();
            } catch(IOException ioe) {
                // ignore and move on
                String msg = "URI does not resolve to anything useful.\n" +
                             "URI is " + file_url;

                errorReporter.warningReport(msg, ioe);
            }

            if(source_urls == null) {
                errorReporter.warningReport("Script contains no URLs. Ignoring",
                                            null);
                continue;
            }

            // loop through the list of candidate URLs and look for
            // something that matches. If it does, set it in the node
            // for use.
            for(int j = 0; (j < source_urls.length); j++) {
                try {
                    connection = source_urls[j].getResource();
                } catch(IOException ioe) {
                    String msg = "Can't find resource " + source_urls[j];
                    errorReporter.warningReport(msg, ioe);

                    continue;
                }

                if(!makeConnection(connection))
                    continue;

                try {
                    mime_type = connection.getContentType();

                    if(mime_type == null)
                        continue;

                    ScriptEngine engine =
                        (ScriptEngine)engineMap.get(mime_type);

                    if(engine == null)
                        continue;

                    content = connection.getContent();

                    ScriptWrapper wrapper =
                        engine.buildWrapper(script.getExecutionSpace(),
                                            mime_type,
                                            content);

                    cache.cacheFile(file_url, mime_type, content);
                    script.setContent(mime_type, wrapper);

                    // Yippee! made it. Break out of the loop and exit
                    // the load process.
                    content_found = true;
                    break;

                } catch(IOException ioe) {
                    String msg = "IO Error reading external file " + file_url;

                    errorReporter.warningReport(msg, ioe);
                    continue;
                } catch(IllegalArgumentException iae) {
                    // from the setContent method
                    errorReporter.warningReport("Can't set external content",
                                                    iae);
                } catch(InvalidScriptContentException isce) {
                    String msg = "Invalid content for script url " + file_url;

                    errorReporter.warningReport(msg, isce);
                }
            } // for loop
        }

        if (content_found) {
            script.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
            script.initialize(0);
        } else {
            script.setLoadState(VRMLExternalNodeType.LOAD_FAILED);
            errorReporter.errorReport("Could not load any URLs for script",
                                      null);
        }
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     *
     * @param file_url The URI of the file to be loaded
     * @param cache The cache that is used for this load
     * @param script The script node that is being loaded
     * @param engine The engine used to handle scripts
     * @return true if the script loading succeeded, false for failure
     */
    private boolean loadClass(String file_url,
                              VRMLScriptNodeType script,
                              ScriptEngine engine) {

        boolean ret_val = true;

        // Strip the class name and just use the URL.
        int slash_index = file_url.lastIndexOf('/');
        int dot_index = file_url.lastIndexOf('.');

        String url_str = file_url.substring(0, slash_index + 1);
        String class_name = file_url.substring(slash_index + 1, dot_index);
        String err_msg = null;
        Exception exception = null;

        try {
            java.net.URL[] url_list = { new java.net.URL(url_str) };

            URLClassLoader loader = new URLClassLoader(url_list);
            Class script_class = loader.loadClass(class_name);

            ScriptWrapper wrapper =
                engine.buildWrapper(script.getExecutionSpace(),
                                    JAVA_MIME,
                                    script_class);

            cache.cacheFile(file_url, JAVA_MIME, script_class);
            script.setContent(JAVA_MIME, wrapper);

        } catch(MalformedURLException mue) {
            err_msg = "Invalid URL for the script " + url_str;
            exception = mue;
            ret_val = false;
        } catch(ClassNotFoundException cnfe) {
            err_msg = "Couldn't find the Java class " + class_name + " at: " + url_str;
            exception = cnfe;
            ret_val = false;
        } catch(InvalidScriptContentException isce) {
            err_msg = "Invalid content for script " + file_url;
            exception = isce;
            ret_val = false;
        }

        if(err_msg != null) {
            errorReporter.errorReport(err_msg, exception);
        }

        return ret_val;
    }

    /**
     * Connect to the current resource connection. Encapsulates all of the
     * details and privilege handling to do this safely. If it can't connect
     * then it will barf. It assumes the class var resourceConnection is
     * valid.
     *
     * @param connection The connection instance to make
     * @return true if the connection was successfully made
     */
    private boolean makeConnection(final ResourceConnection connection) {

        boolean ret_val = true;

        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        connection.connect();
                        return null;
                    }
                }
            );
        } catch(PrivilegedActionException pae) {
            String msg = "IO Error reading external file " +
                         connection.getURI();

            ret_val = false;
            errorReporter.warningReport(msg, pae.getException());
        }

        return ret_val;
    }
}
