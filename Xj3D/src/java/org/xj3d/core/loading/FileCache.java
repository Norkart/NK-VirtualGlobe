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

package org.xj3d.core.loading;

// External imports
// None

// Local imports
// None

/**
 * Interface defining a file caching system without specifying an
 * implementation.
 * <p>
 *
 * The interface is designed to abstract various file caching implementations
 * so that we can use one content loading mechanism, but allow it to use
 * different caching schemes dependent on the need of the application.
 * <p>
 *
 * When using a cache, it is highly recommended that the URI passed in does
 * not include the reference part (any piece after the #) as this means that
 * you cannot cache the entire file or reference the file and extra another
 * part of it. For example, when refering to a proto definition, you would
 * want to store the entire file here so that any further protos in that same
 * file do not need to be reloaded. This relies on you, the caller to make
 * sure that the reference part is stripped from the URI string.
 * <p>
 *
 * The implementor of this interface is free to do whatever they want with
 * the requests - including completely ignoring it.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface FileCache {

    /**
     * Check the cache for the file nominated by this URI string. If the
     * file is part of the cache, a reference is returned to the details.
     * If the item is not in the cache, it will return null.
     *
     * @param uri The uri to check for
     * @return The details of the item in cache or null
     */
    public CacheDetails checkForFile(String uri);

    /**
     * Store the item in the cache. The item is stored according to whatever
     * rules the cache uses internally for kicking out older values etc
     *
     * @param uri The uri string for the content
     * @param contentType A String describing the MIME type of the content
     * @param content The actual Java representation of the URI's content
     */
    public void cacheFile(String uri, String contentType, Object content);
}
