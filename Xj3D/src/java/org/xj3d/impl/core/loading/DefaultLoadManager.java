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
import org.xj3d.core.loading.FileCache;

/**
 * A simplistic manager for loading files that does no caching.
 * <p>
 *
 * The loader is given a scene and told to start loading the contents. During
 * this time items progress from pending to loading to loaded. The load manager
 * is cancelable so that a particular scene can be interrupted part way through
 * loading. The manager is designed to load multiple scenes in parallel or to
 * have parallel instances of this manager loading data.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DefaultLoadManager extends AbstractLoadManager {

    /**
     * The cache that we use. Everyone joins in in order to get maximum
     * amount of caching.
     */
    private static FileCache cache = new DefaultFileCache();

    /**
     * Create a new load manager initialised with the content loading threads
     * ready to work.
     */
    public DefaultLoadManager() {
    }

    //--------------------------------------------------------------
    // Methods defined by AbstractLoadManager
    //--------------------------------------------------------------

    /**
     * Request to fetch the cache used by the derived type.
     *
     * @return The file cache instance
     */
    protected FileCache getCache() {
        return cache;
    }
}
