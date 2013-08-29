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
import java.util.HashMap;
import java.util.WeakHashMap;

// Local imports
// none

/**
 * The file cache implementation uses the standard
 * {@link java.util.WeakHashMap} as the storage mechanism.
 * <p>
 *
 * Items are stored in the WeakHashMap so that when an item no longer
 * needs data, that item can be discarded.
 * <p>
 *
 * The internal implementation is thread safe, choosing to synchronize on the
 * internal mapping structures. This prevents one thread from loading something
 * into the cache and then having another thread interrupt it half way through
 * and get bogus data.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class WeakRefFileCache {

    /** The hashmap that contains our references */
    private WeakHashMap referenceMap;

    /** Auxillary map that contains the uri -> content type mapping */
    private HashMap contentMap;

    /**
     * Construct a default instance of this class.
     */
    WeakRefFileCache() {
        referenceMap = new WeakHashMap();
        contentMap = new HashMap();
    }

    /**
     * Check the cache for the file nominated by this URI string. Always
     * returns null.
     *
     * @param uri The uri to check for
     * @return The details of the item in cache or null
     */
    CacheDetails checkForFile(String uri) {
        CacheDetails ret_val = null;

        synchronized(contentMap) {
            if(contentMap.containsKey(uri)) {
                Object content = referenceMap.get(uri);

                // Do we currently have the item referenced? If it has been
                // discarded, then remove the mapping from the content type map
                // as well so that next time we don't end up here.
                if(content == null) {
                    contentMap.remove(uri);
                } else {
                    // Good, we do have it. Let's build the details
                    String type = (String)contentMap.get(uri);
                    ret_val = new DefaultCacheDetails(uri, type, content);
                }
            }
        }

        return ret_val;
    }

    /**
     * Store the item in the cache. Request is ignored
     *
     * @param uri The uri string for the content
     * @param contentType A String describing the MIME type of the content
     * @param content The actual Java representation of the URI's content
     */
    void cacheFile(String uri, String contentType, Object content) {
        synchronized(contentMap) {
            referenceMap.put(uri, content);
            contentMap.put(uri, contentType);
        }
    }
}
