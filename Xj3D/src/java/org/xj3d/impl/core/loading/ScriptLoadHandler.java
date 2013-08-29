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
import org.ietf.uri.*;

import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.scripting.InvalidScriptContentException;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.ScriptWrapper;

import org.xj3d.core.loading.CacheDetails;
import org.xj3d.core.loading.FileCache;
import org.xj3d.core.loading.LoadRequestHandler;

/**
 * A LoadRequestHandler implementation that process the script loading requests.
 * <p>
 *
 * The content loader is used to wait on a queue of available content and
 * load the next available item in the queue. When loading, the content loader
 * loads the complete file, it ignores any reference part of the URI as these
 * are meaningless for scripts.
 * <p>
 *
 * In the cache, if the referenced script is written in Java, the script is
 * held as a Class instance rather than the actual running script. This is
 * done because we want to create a new instance each time a script is
 * loaded, rather than re-using the same instance. This is crucial in protos.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ScriptLoadHandler extends BaseLoadHandler
    implements LoadRequestHandler {

    /** Error message when we couldn't load any valid scripting URLs */
    private static final String NO_VALID_URLS_MSG =
        "Cannot resolve any URLS for a script. URL: ";

    /** Error message when we get an unexpected error type */
    private static final String UNKNOWN_ERROR_MSG =
        "Loading the script generated an unexpected error.";

    /** The MIME type to represent Java files */
    private static final String JAVA_MIME = "application/x-java";

    /** The cache representation that this loader is using */
    private FileCache fileCache;

    /**
     * Create a content loader that reads values from the given queue and
     * stores intermediate results in the given map.
     *
     * @param cache The file cache implementation to use for this handler
     */
    ScriptLoadHandler(FileCache cache) {
        fileCache = cache;
    }

    //----------------------------------------------------------
    // Methods defined by LoadRequestHandler
    //----------------------------------------------------------

    /**
     * Process this load request now.
     *
     * @param reporter The errorReporter to send all messages to
     * @param url The list of URLs to load
     * @param loadList The list of LoadDetails objects to sent the fulfilled
     *    requests to
     */
    public void processLoadRequest(ErrorReporter reporter,
                                   String[] url,
                                   Vector loadList) {

        boolean content_found = false;
        Object content;
        ScriptLoadDetails details;
        int field_index = 0;
        boolean single;
        String mime_type;

        for(int i = 0; i < loadList.size(); i++) {
            details = (ScriptLoadDetails)loadList.get(i);
            VRMLScriptNodeType node = (VRMLScriptNodeType)details.node;
            node.setLoadState(VRMLExternalNodeType.LOADING);
        }

        if(terminateCurrent)
            return;

        for(int i = 0; !content_found && (i < url.length); i++) {

            // check the string for a # and remove the reference
            // These really should not be here as it doesn't make sense
            // for scripts. However, need to special case for inlined
            // ECMAScript code as there may well be embedded # chars in
            // stuff like a createVrmlFromString call. So, simplistic
            // check to see if the URL starts with either
            // javascript: or ecmascript: and ignore this check otherwise
            String file_url = url[i];
            int index;

            if((!file_url.startsWith("javascript:") &&
                 !file_url.startsWith("ecmascript:")) &&
               ((index = file_url.lastIndexOf('#')) != -1))
                file_url = file_url.substring(0, index);

            // Check the cache first to see if we have something here
            CacheDetails cached_version =
                fileCache.checkForFile(file_url);

            // Assume the same spec version for all instances and just find
            // the map based on the first item in the list.
            details = (ScriptLoadDetails)loadList.get(0);
            VRMLScriptNodeType s_node = (VRMLScriptNodeType)details.node;
            VRMLExecutionSpace space = s_node.getExecutionSpace();
            BasicScene internal_scene = space.getContainedScene();
            int spec_version = internal_scene.getSpecificationMajorVersion();
            Map engine_map = (Map)details.engineMap.get(spec_version);

            if(cached_version != null) {
                mime_type = cached_version.getContentType();

                ScriptEngine engine =
                    (ScriptEngine)engine_map.get(mime_type);

                if(engine == null)
                    continue;

                content = cached_version.getContent();
                content_found = true;

                for(int j = 0; j < loadList.size(); j++) {
                    details = (ScriptLoadDetails)loadList.get(j);
                    VRMLScriptNodeType node = (VRMLScriptNodeType)details.node;

                    if(!node.checkValidContentType(mime_type))
                        continue;

                    node.setLoadedURI(url[i]);

                    try {
                        ScriptWrapper wrapper =
                            engine.buildWrapper(node.getExecutionSpace(),
                                                mime_type,
                                                content);

                        node.setContent(mime_type, wrapper);

                    } catch(InvalidScriptContentException isce) {
                        // This should never happen unless someone changes the
                        // registered script engines on the fly. Something we
                        // Advise heavily against.
                        String msg = "Invalid content for script " + file_url;

                        reporter.errorReport(msg, isce);
                        continue;
                    }

                    node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                    details.statusListener.loadCompleted(node);
                }

            } else {
                content_found = loadExternal(reporter,
                                             file_url,
                                             loadList,
                                             engine_map);
            }

        }

        if(!content_found) {
            int size = loadList.size();

            for(int j = 0; j < loadList.size(); j++) {
                details = (ScriptLoadDetails)loadList.get(j);
                VRMLScriptNodeType node = (VRMLScriptNodeType)details.node;
                node.setLoadState(VRMLExternalNodeType.LOAD_FAILED);
                details.statusListener.loadFailed(node);
            }

            String truncUrl;

            if(url.length > 0) {
                if(url[0].length() > 80)
                    truncUrl = url[0].substring(0,80);
                else
                    truncUrl = url[0];
            } else {
                truncUrl = "Empty Url";
            }

            reporter.warningReport(NO_VALID_URLS_MSG + truncUrl,
                                   null);
        }

        // Cleanup so we don't hold any references longer than we need to
        currentConnection = null;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     *
     * @param reporter The errorReporter to send all messages to
     * @param fileUri The URI of the file to be loaded
     * @param sNode The script node that is being loaded
     * @param engineMap A mapping of mime types to scripting engines
     * @return true if the script loading succeeded, false for failure
     */
    private boolean loadExternal(ErrorReporter reporter,
                                 String fileUri,
                                 Vector loadList,
                                 Map engineMap) {

        boolean content_found = false;

        // Always assume a fully qualified URI
        URL[] source_urls = null;
        String mime_type;
        Object content;

        // Check the URL for ending in ./class. If this is the case, then
        // create a URLClassLoader and use that rather than going through the
        // URN system.
        if(fileUri.endsWith(".class")) {
            // no point going any further if we don't have an engine capable
            // of supporting Java based scripts
            ScriptEngine eng = (ScriptEngine)engineMap.get(JAVA_MIME);

            if(eng == null)
                return false;

            return loadClass(reporter, fileUri, loadList, eng);
        } else if(fileUri.startsWith("ecmascript:") ||
                  fileUri.startsWith("javascript:")) {

            mime_type = fileUri.startsWith("ecmascript:") ?
                "application/ecmascript" : "application/javascript";

            String real_code;
            if (fileUri.length() > 10)
                real_code = fileUri.substring(11);
            else
                real_code = "";

            ScriptEngine engine = (ScriptEngine)engineMap.get(mime_type);

            for(int j = 0; j < loadList.size(); j++) {
                ScriptLoadDetails details = (ScriptLoadDetails)loadList.get(j);
                VRMLScriptNodeType node = (VRMLScriptNodeType)details.node;

                if(!node.checkValidContentType(mime_type))
                    continue;

                content_found = true;
                node.setLoadedURI(fileUri);
                node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);

                ScriptWrapper wrapper =
                    engine.buildWrapper(node.getExecutionSpace(),
                                        mime_type,
                                        real_code);


                node.setContent(mime_type, wrapper);

                node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                details.statusListener.loadCompleted(node);
            }

            return content_found;
        } else {
            try {
                URI uri = URIUtils.createURI(fileUri);

                if(terminateCurrent)
                    return false;

                source_urls = uri.getURLList();
            } catch(IOException ioe) {
                // ignore and move on
                String msg = "URI does not resolve to anything useful.\n" +
                             "URI is " + fileUri;

                reporter.warningReport(msg, ioe);

                return false;
            }
        }

        if(terminateCurrent)
            return false;

        if(source_urls == null) {
            reporter.warningReport("Script contains no URLs. Ignoring",
                                        null);
            return false;
        }


        // loop through the list of candidate URLs and look for
        // something that matches. If it does, set it in the node
        // for use.
        for(int i = 0; i < source_urls.length; i++) {
            try {
                currentConnection = source_urls[i].getResource();
            } catch(IOException ioe) {
                String msg = "Can't find resource " + source_urls[i];
                reporter.warningReport(msg, ioe);

                continue;
            } catch(UnsupportedServiceException e) {
                String msg = "Can't run script: " + source_urls[i];
                reporter.warningReport(msg, e);

                continue;
            }

            if(!makeConnection(reporter)) {
                if(terminateCurrent)
                    break;
                else
                    continue;
            }

            try {

                if(terminateCurrent) {
                    currentConnection.close();
                    break;
                }

                mime_type = currentConnection.getContentType();

                if(terminateCurrent) {
                    currentConnection.close();
                    break;
                }

                if(mime_type == null) {
                    currentConnection.close();
                    continue;
                }

                ScriptEngine engine = (ScriptEngine)engineMap.get(mime_type);

                if(engine == null) {
                    currentConnection.close();
                    continue;
                }

                content = currentConnection.getContent();

                currentConnection.close();

                if(content == null)
                    continue;

                if(terminateCurrent)
                    break;

                boolean match_found = false;
                for(int j = 0; j < loadList.size(); j++) {
                    ScriptLoadDetails details = (ScriptLoadDetails)loadList.get(j);
                    VRMLScriptNodeType node = (VRMLScriptNodeType)details.node;

                    if(!node.checkValidContentType(mime_type))
                        continue;

                    match_found = true;
                    node.setLoadedURI(fileUri);
                    node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);

                    ScriptWrapper wrapper =
                        engine.buildWrapper(node.getExecutionSpace(),
                                            mime_type,
                                            content);


                    node.setContent(mime_type, wrapper);

                    node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                    details.statusListener.loadCompleted(node);
                }

                if(!match_found)
                    continue;

                if(terminateCurrent)
                    break;

                fileCache.cacheFile(fileUri, mime_type, content);

                if(terminateCurrent)
                    break;

                // Yippee! made it. Break out of the loop and exit
                // the load process.
                content_found = true;
                break;

            } catch(IOException ioe) {
                String msg = "IO Error reading external file " + fileUri;

                reporter.warningReport(msg, ioe);

                // ignore and move on
                if(terminateCurrent)
                    break;
                else
                    continue;
            } catch(IllegalArgumentException iae) {
                // from the setContent method
                reporter.warningReport("Can't set external content",
                                                iae);
                continue;
            } catch(InvalidScriptContentException isce) {
                String msg = "Invalid content for script url " + fileUri;

                reporter.warningReport(msg, isce);
                continue;
            } catch(Exception e) {
                // Probably want to include the URL here too.
                reporter.errorReport(UNKNOWN_ERROR_MSG, e);
            }

        } // for loop

        return content_found;
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     *
     * @param reporter The errorReporter to send all messages to
     * @param fileUri The URI of the file to be loaded
     * @param sNode The script node that is being loaded
     * @param engine The engine used to handle scripts
     * @return true if the script loading succeeded, false for failure
     */
    private boolean loadClass(ErrorReporter reporter,
                              String fileUri,
                              Vector loadList,
                              ScriptEngine engine) {

        boolean ret_val = true;

        String url_str;
        String class_name;

        int slash_index = fileUri.lastIndexOf('/');
        int dot_index = fileUri.lastIndexOf('.');

        if (fileUri.startsWith("jar:")) {

            int bang_index = fileUri.lastIndexOf("!");
            url_str = fileUri.substring(4,bang_index);
            class_name = fileUri.substring(bang_index + 2, dot_index);
        } else {
            // Strip the class name and just use the URL.

            url_str = fileUri.substring(0, slash_index + 1);
            class_name = fileUri.substring(slash_index + 1, dot_index);
        }

        String err_msg = null;
        Exception exception = null;

        try {
            java.net.URL[] url_list = { new java.net.URL(url_str) };

            URLClassLoader loader = new URLClassLoader(url_list);

            Class script_class = null;

            try {
                script_class = loader.loadClass(class_name);
            } catch(Exception e) {
                // WebStart fallback
                ClassLoader cl = this.getClass().getClassLoader();
                script_class = cl.loadClass(class_name);
            }

            ScriptWrapper wrapper;

            for(int j = 0; j < loadList.size(); j++) {
                ScriptLoadDetails details = (ScriptLoadDetails)loadList.get(j);
                VRMLScriptNodeType node = (VRMLScriptNodeType)details.node;

                try {
                    wrapper =
                        engine.buildWrapper(node.getExecutionSpace(),
                                            JAVA_MIME,
                                            script_class);
                } catch(Exception e) {
                    ClassLoader cl = this.getClass().getClassLoader();

                    script_class = cl.loadClass(class_name);

                    wrapper =
                        engine.buildWrapper(node.getExecutionSpace(),
                                            JAVA_MIME,
                                            script_class);
                }

                fileCache.cacheFile(fileUri, JAVA_MIME, script_class);
                node.setContent(JAVA_MIME, wrapper);

                node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                details.statusListener.loadCompleted(node);
            }
        } catch(MalformedURLException mue) {
            err_msg = "Invalid URL for the script " + url_str;
            exception = mue;
            ret_val = false;
        } catch(ClassNotFoundException cnfe) {
            err_msg = "Couldn't find the Java class " + class_name + " at: " + url_str;
            exception = cnfe;
            ret_val = false;
        } catch(InvalidScriptContentException isce) {
            err_msg = "Invalid content for script " + fileUri;
            exception = isce;
            ret_val = false;
        }

        if(err_msg != null)
            reporter.errorReport(err_msg, exception);

        return ret_val;
    }
}
