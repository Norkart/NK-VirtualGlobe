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
// none

// Local imports
// none

/**
 * Collection of internal constants for load manager implementors
 * <p>
 * The default sort order is defined to be:
 * </p>
 *
 * <ol>
 * <li><code>loadURL</code> (any loadURL() call from the SAI/EAI)</li>
 * <li><code>textures</code> All texture types and Background nodes, except
 *   for MovieTexture.</li>
 * <li><code>externprotos</code> Externproto loading</li>
 * <li><code>scripts</code> All scripts, regardless of whether they are locally
 *   defined or external</li>
 * <li><code>inlines</code> Any Inline node type</li>
 * <li><code>create</code> Any of the createX3DFromX or createVRMLFromURL()
 *   calls</li>
 * <li><code>audio</code> AudioClip nodes, but not MovieTexture</li>
 * <li><code>movies</code> MovieTexture node.</li>
 * <li><code>shaders</code> Any shader source.</li>
 * <li><code>others</code> Anything else not covered by one of the fixed types</li>
 * </ol>
 *
 * <p>
 * In keeping with the extensible nature of Xj3D, other types may be defined as
 * part of the sort order definition. The loadURL type is always the highest
 * priority and cannot be changed. Specifying this in the sort order list will be
 * ignored.
 * </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface LoadConstants {

    /**
     * Property name defining the sort order
     * (<code>org.xj3d.core.loading.sort.order</code>)
     */
    public static final String SORT_ORDER_PROP =
        "org.xj3d.core.loading.sort.order";

    /** Sort the loadURL, which always has the highest priority */
    public static final String SORT_LOAD_URL = "url";

    /** Script object sorting order */
    public static final String SORT_SCRIPT = "scripts";

    /** Inline nodes sorting order */
    public static final String SORT_INLINE = "inlines";

    /** Any form of texture object sorting order */
    public static final String SORT_TEXTURE = "textures";

    /** EXTERNPROTOS sorting order */
    public static final String SORT_PROTO = "externprotos";

    /** AudioClip nodes, but not MovieTexture sorting order */
    public static final String SORT_AUDIO = "audio";

    /** createX3DFromX calls sorting order */
    public static final String SORT_CREATE = "create";

    /** MovieTexture object sorting order */
    public static final String SORT_MOVIE = "movies";

    /** Shader source objects sorting order */
    public static final String SORT_SHADER = "shaders";

    /** Any other object sorting order */
    public static final String SORT_OTHER = "others";

    /**
     * Property name defining how much memory should be used
     * (<code>org.xj3d.core.loading.cache.mem.size</code>)
     */
    public static final String MEM_ALLOC_PROP =
        "org.xj3d.core.loading.cache.mem.size";

    /** The default sort order as a series of concatenated properties */
    public static final String DEFAULT_SORT_ORDER =
        SORT_TEXTURE + ',' +
        SORT_PROTO + ',' +
        SORT_SCRIPT + ',' +
        SORT_INLINE + ',' +
        SORT_CREATE + ',' +
        SORT_AUDIO + ',' +
        SORT_MOVIE + ',' +
        SORT_SHADER + ',' +
        SORT_OTHER;
}
