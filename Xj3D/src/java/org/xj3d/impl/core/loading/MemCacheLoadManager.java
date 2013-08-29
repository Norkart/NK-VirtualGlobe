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
 * A manager for loading files that are external to the currently loading file
 * eg Textures, inlines and protos.
 * <p>
 *
 * The loader is given a scene and told to start loading the contents. During
 * this time items progress from pending to loading to loaded. The load manager
 * is cancelable so that a particular scene can be interrupted part way through
 * loading. The manager is designed to load multiple scenes in parallel or to
 * have parallel instances of this manager loading data.
 * <p>
 *
 * This implementation has two memory constraints - the maximum allocated by
 * the user and total constraints by the JVM. If the user specifies more memory
 * for caching than the JVM will allow, we have to make do. For this, we use
 * the standard WeakHashMap so that files that are consuming memory and not
 * used can be discarded by the VM.
 *
 * <b>Properties</b>
 * <p>
 * The following properties are used by this class
 * <ul>
 * <li><code>org.web3d.vrml.nodes.loader.cache.mem.size</code> The amount of
 *     memory in Kilobytes (integer value) to allocate to in-memory file
 *     caching. If the value is zero or less, no caching is performed.
 * </li>
 * </ul>
 *
 * <B>Note</B> Sort order is not implemented yet.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MemCacheLoadManager extends AbstractLoadManager {

    /**
     * The cache that we use. Everyone joins in in order to get maximum
     * amount of caching.
     */
    private static FileCache cache = new WeakRefFileCache();

    /**
     * Create a new load manager initialised with the content loading threads
     * ready to work.
     */
    public MemCacheLoadManager() {
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
