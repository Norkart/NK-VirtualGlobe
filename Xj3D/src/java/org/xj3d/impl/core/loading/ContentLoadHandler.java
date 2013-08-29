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
import java.io.*;

import org.ietf.uri.*;

import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Vector;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.loading.CacheDetails;
import org.xj3d.core.loading.FileCache;
import org.xj3d.core.loading.LoadRequestHandler;

/**
 * A loader thread for a single piece of content at any given time.
 * <p>
 *
 * The content loader is used to wait on a queue of available content and
 * load the next available item in the queue.
 * <p>
 *
 * When loading, the content loader loads the complete file, it ignores any
 * reference part of the URI. This allows for better caching.
 *
 * The loader is used to
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.6 $
 */
class ContentLoadHandler extends BaseLoadHandler
    implements LoadRequestHandler {

    /** Message for an unrecognized message */
    private static final String UNKNOWN_ERROR_MSG =
        "Unknown error in content loading process";

    /** Message for errors in setContent() */
    private static final String CONTENT_ERROR_MSG =
        "Error setting external content:";

    /** Message for errors attempting to write to an invalid field index */
    private static final String INVALID_FIELD_MSG =
        "Internal error caused by attempting to send content to an invalid " +
        "field index: ";

    /** Message for no valid URLS in the load process */
    private static final String NO_URLS_MSG =
        "Cannot resolve any URLS for URL: ";

    /** The cache representation that this loader is using */
    private FileCache fileCache;

    /** A map for determining whether content is an inline */
    private HashSet inlineSet;

    /**
     * Create a content loader that reads values from the given queue and
     * stores intermediate results in the given map.
     *
     * @param cache The file cache implementation to use for this handler
     */
    ContentLoadHandler(FileCache cache) {
        fileCache = cache;

        inlineSet = new HashSet(6);
        inlineSet.add("model/vrml");
        inlineSet.add("x-world/x-vrml");
        inlineSet.add("application/xml");
        inlineSet.add("model/x3d+xml");
        inlineSet.add("model/x3d+vrml");
        inlineSet.add("model/x3d+binary");
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

        String[] urls;
        boolean content_found = false;
        Object content;
        ContentLoadDetails details;
        boolean single;
        String mime_type;

        VRMLSingleExternalNodeType single_node = null;
        VRMLMultiExternalNodeType multi_node = null;

        // run through all the details and make sure that we have at
        // least one that needs to be loaded.
        boolean load_needed = false;

        for(int i = 0; i < loadList.size(); i++) {
            details = (ContentLoadDetails)loadList.get(i);
            int state = 0;
            if(details.fieldIndex == -1) {
                single_node = (VRMLSingleExternalNodeType)details.node;
                state = single_node.getLoadState();
            } else {
                multi_node = (VRMLMultiExternalNodeType)details.node;
                state = multi_node.getLoadState(details.fieldIndex);
            }

            load_needed = (state != VRMLExternalNodeType.LOAD_COMPLETE);
        }

        if(!load_needed) {
            return;
        }

        content_found = false;

        // Ignore this as we have not yet registered that we are
        // actually processing anything.
        terminateCurrent = false;

        for(int i = 0; i < loadList.size() && !load_needed; i++) {
            details = (ContentLoadDetails)loadList.get(i);
            if(details.fieldIndex == -1) {
                single_node = (VRMLSingleExternalNodeType)details.node;
                single_node.setLoadState(VRMLExternalNodeType.LOADING);
            } else {
                multi_node = (VRMLMultiExternalNodeType)details.node;
                multi_node.setLoadState(details.fieldIndex,
                                        VRMLExternalNodeType.LOADING);
            }
        }

        int num_urls = (url == null) ? 0 : url.length;

        for(int i = 0; !content_found && (i < num_urls); i++) {

            // check the string for a # and remove the reference
            String file_url = url[i];
            int index;
            if((index = file_url.lastIndexOf('#')) != -1) {
                file_url = file_url.substring(0, index);
            }

            // Check the cache first to see if we have something here
            CacheDetails cached_version =
                fileCache.checkForFile(file_url);

            if(cached_version != null) {
                mime_type = cached_version.getContentType();
                content = cached_version.getContent();

                content_found = true;

                for(int j = 0; j < loadList.size(); j++) {
                    try {
                        details = (ContentLoadDetails)loadList.get(j);
                        if(details.fieldIndex == -1) {
                            single_node = (VRMLSingleExternalNodeType)details.node;

                            if(!single_node.checkValidContentType(mime_type))
                                continue;

                            single_node.setLoadedURI(url[i]);
                            single_node.setContent(mime_type, content);
                            single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                        } else {
                            multi_node = (VRMLMultiExternalNodeType)details.node;

                            if(!multi_node.checkValidContentType(details.fieldIndex,
                                                                 mime_type))
                                continue;

                            multi_node.setLoadedURI(details.fieldIndex, url[i]);
                            multi_node.setContent(details.fieldIndex,
                                                  mime_type,
                                                  content);
                            multi_node.setLoadState(details.fieldIndex,
                                                    VRMLExternalNodeType.LOAD_COMPLETE);
                        }
                    } catch(IllegalArgumentException iae) {
                        // from the setContent method
                        reporter.errorReport(CONTENT_ERROR_MSG, iae);
                    } catch(InvalidFieldException ife) {
                        reporter.errorReport(INVALID_FIELD_MSG, ife);
                    } catch(Exception e) {
                        // Any other exception
                        reporter.errorReport(UNKNOWN_ERROR_MSG, e);
                    }
                }
            } else {
                content_found = loadExternal(reporter,
                                             url[i],
                                             file_url,
                                             loadList);
            }
        }

        if(!content_found) {
            for(int j = 0; j < loadList.size(); j++) {
                details = (ContentLoadDetails)loadList.get(j);
                if(details.fieldIndex == -1) {
                    single_node = (VRMLSingleExternalNodeType)details.node;
                    single_node.setLoadState(VRMLExternalNodeType.LOAD_FAILED);
                } else {
                    multi_node = (VRMLMultiExternalNodeType)details.node;
                    multi_node.setLoadState(details.fieldIndex,
                                            VRMLExternalNodeType.LOAD_FAILED);
                }
            }

            if(url.length > 0)
                reporter.warningReport(NO_URLS_MSG + url[0], null);
        }

        // Cleanup so we don't hold any references longer than we need to
        currentConnection = null;
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     *
     * @param reporter The errorReporter to send all messages to
     */
    private boolean loadExternal(ErrorReporter reporter,
                                 String origUri,
                                 String fileUri,
                                 Vector loadList) {

//System.out.println("Loading: " + fileUri);
        VRMLSingleExternalNodeType single_node = null;
        VRMLMultiExternalNodeType multi_node =  null;
        boolean content_found = false;

        // Always assume a fully qualified URI
        URL[] source_urls = null;
        String mime_type;
        Object content;

        try {
            URI uri = URIUtils.createURI(fileUri);

            if(terminateCurrent)
                return false;

            source_urls = uri.getURLList();
        } catch(IOException ioe) {
            // ignore and move on
            return false;
        } catch(ArrayIndexOutOfBoundsException aiob) {
            reporter.messageReport("Failed to parse url: " + fileUri);
            return false;
        }

        if(terminateCurrent || (source_urls == null)) {
            return false;
        }

        // loop through the list of candidate URLs and look for
        // something that matches. If it does, set it in the node
        // for use.
        for(int i = 0; (i < source_urls.length); i++) {
            try {
                currentConnection = source_urls[i].getResource();
            } catch(IOException ioe) {
                continue;
            }

            if(terminateCurrent)
                break;

            try {
                if(!makeConnection(reporter)) {
                    if(terminateCurrent)
                        break;
                    else
                        continue;
                }

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

                if (inlineSet.contains(mime_type)) {
                    content_found = loadInline(currentConnection, mime_type, source_urls[i], reporter, origUri, fileUri, loadList);

                    if (content_found)
                        break;
                    else
                        continue;
                }

                content = currentConnection.getContent();
                currentConnection.close();

                if(content == null)
                    continue;

                boolean match_found = false;

                for(int j = 0; j < loadList.size(); j++) {
                    ContentLoadDetails details = (ContentLoadDetails)loadList.get(j);
                    if(details.fieldIndex == -1) {
                        single_node = (VRMLSingleExternalNodeType)details.node;

                        if(!single_node.checkValidContentType(mime_type))
                            continue;

                        match_found = true;
                        single_node.setLoadedURI(origUri);

                        single_node.setContent(mime_type, content);
                        single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                    } else {
                        multi_node = (VRMLMultiExternalNodeType)details.node;

                        if(!multi_node.checkValidContentType(details.fieldIndex, mime_type))
                            continue;

                        match_found = true;
                        multi_node.setLoadedURI(details.fieldIndex, origUri);
                        multi_node.setContent(details.fieldIndex, mime_type, content);
                        multi_node.setLoadState(details.fieldIndex,
                                                VRMLExternalNodeType.LOAD_COMPLETE);
                    }
                }

                if(!match_found)
                    continue;

                // Disable caching of a loaded scene. Possibly not the
                // best thing - particularly for proto libraries, but it
                // is the best thing for now until we can work out the
                // issues with scene caching.

                if(!(content instanceof BasicScene)) {

                    // Images are already cached as Texture Objects
                    if (mime_type.indexOf("image") != 0)
                        fileCache.cacheFile(fileUri, mime_type, content);
                }
                // Yippee! made it. Break out of the loop and exit
                // the load process.
                content_found = true;
                break;

            } catch(IOException ioe) {
                // ignore and move on
                if(terminateCurrent)
                    break;
                else
                    continue;
            } catch(IllegalArgumentException iae) {
                // from the setContent method
                reporter.errorReport(CONTENT_ERROR_MSG, iae);
                continue;
            } catch(InvalidFieldException ife) {
                reporter.errorReport(INVALID_FIELD_MSG, ife);
                continue;
            } catch(Exception e) {
                // Any other exception
                reporter.errorReport(UNKNOWN_ERROR_MSG, e);
                continue;
            }

        } // for loop

        return content_found;
    }

    /**
     * Load the file from an external URL because we couldn't find it in
     * the cache.
     *
     * @param reporter The errorReporter to send all messages to
     */
    private boolean loadInline(ResourceConnection currentConnection,
                               String mimeType,
                               URL url,
                               ErrorReporter reporter,
                               String origUri,
                               String fileUri,
                               Vector loadList) {

        VRMLSingleExternalNodeType single_node = null;
        VRMLMultiExternalNodeType multi_node =  null;
        Object content;
        boolean match_found = false;

        // Inlines that have been notified
        HashSet alreadyNotified = new HashSet();


        try {
            content = currentConnection.getContent();
            currentConnection.close();

            if(content == null)
                return false;

            ContentLoadDetails details = (ContentLoadDetails)loadList.get(0);
            if(details.fieldIndex == -1) {
                single_node = (VRMLSingleExternalNodeType)details.node;

                if(single_node.checkValidContentType(mimeType)) {

                    match_found = true;
                    single_node.setLoadedURI(origUri);

                    alreadyNotified.add(single_node);
                    single_node.setContent(mimeType, content);
                    single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                }
            } else {
                multi_node = (VRMLMultiExternalNodeType)details.node;

                if(multi_node.checkValidContentType(details.fieldIndex, mimeType)) {

                    match_found = true;
                    multi_node.setLoadedURI(details.fieldIndex, origUri);
                    alreadyNotified.add(multi_node);
                    multi_node.setContent(details.fieldIndex, mimeType, content);
                    multi_node.setLoadState(details.fieldIndex,
                                            VRMLExternalNodeType.LOAD_COMPLETE);
                }
            }

        } catch(IOException ioe) {
            // ignore and move on
        } catch(IllegalArgumentException iae) {
            // from the setContent method
            reporter.errorReport(CONTENT_ERROR_MSG, iae);
        } catch(InvalidFieldException ife) {
            reporter.errorReport(INVALID_FIELD_MSG, ife);
        } catch(Exception e) {
            // Any other exception
            reporter.errorReport(UNKNOWN_ERROR_MSG, e);
        }


        // Create new geometry for each inline.  Reusing doesn't work right now
        String mime_type;

        for(int j = 1; j < loadList.size(); j++) {
            ContentLoadDetails details = (ContentLoadDetails)loadList.get(j);

            if (alreadyNotified.contains(details.node)) {
                continue;
            }

            try {
                currentConnection = url.getResource();
            } catch(IOException ioe) {
                return match_found;
            }

            if(terminateCurrent)
                break;

            try {
                if(!makeConnection(reporter)) {
                    if(terminateCurrent)
                        break;
                    else
                        return match_found;
                }

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
                    return match_found;
                }

                content = currentConnection.getContent();
                currentConnection.close();

                if(content == null)
                    return match_found;


                if(details.fieldIndex == -1) {
                    single_node = (VRMLSingleExternalNodeType)details.node;

                    if(single_node.checkValidContentType(mimeType)) {

                        match_found = true;
                        single_node.setLoadedURI(origUri);

                        alreadyNotified.add(single_node);
                        single_node.setContent(mimeType, content);
                        single_node.setLoadState(VRMLExternalNodeType.LOAD_COMPLETE);
                    }
                } else {
                    multi_node = (VRMLMultiExternalNodeType)details.node;

                    if(multi_node.checkValidContentType(details.fieldIndex, mimeType)) {

                        match_found = true;
                        multi_node.setLoadedURI(details.fieldIndex, origUri);
                        alreadyNotified.add(multi_node);
                        multi_node.setContent(details.fieldIndex, mimeType, content);
                        multi_node.setLoadState(details.fieldIndex,
                                                VRMLExternalNodeType.LOAD_COMPLETE);
                    }
                }
            } catch(IOException ioe) {
                // ignore and move on
            } catch(IllegalArgumentException iae) {
                // from the setContent method
                reporter.errorReport(CONTENT_ERROR_MSG, iae);
            } catch(InvalidFieldException ife) {
                reporter.errorReport(INVALID_FIELD_MSG, ife);
            } catch(Exception e) {
                // Any other exception
                reporter.errorReport(UNKNOWN_ERROR_MSG, e);
            }
        }

        return match_found;
    }

}
