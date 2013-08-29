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
// None

// Local imports
import org.xj3d.core.loading.CacheDetails;
import org.xj3d.core.loading.FileCache;

/**
 * The default file cache implementation that performs no caching at all.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DefaultFileCache implements FileCache {

    //----------------------------------------------------------
    // Methods defined by FileCache
    //----------------------------------------------------------

    /**
     * Check the cache for the file nominated by this URI string. Always
     * returns null.
     *
     * @param uri The uri to check for
     * @return The details of the item in cache or null
     */
    public CacheDetails checkForFile(String uri) {
        return null;
    }

    /**
     * Store the item in the cache. Request is ignored
     *
     * @param uri The uri string for the content
     * @param contentType A String describing the MIME type of the content
     * @param content The actual Java representation of the URI's content
     */
    public void cacheFile(String uri, String contentType, Object content) {
        // do nothing
    }
}
