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
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

// Local imports
import org.web3d.net.protocol.X3DResourceFactory;
import org.web3d.vrml.parser.VRMLParserFactory;

/**
 * A Java3D file loader implementation for reading X3D utf8 files only and
 * building a Java3D scenegraph with them.
 * <p>
 *
 * The loader considers sensor handling and routing to be behaviours. Some
 * asynchronous loading of files for textures is performed. Sound file loading
 * is performed if audio clips are asked for. For example, if behaviours are
 * not requested then Inlines will not have their content loaded.
 * <p>
 *
 * If the loader asks for no behaviors, then we will still load nodes that
 * use behaviors, but will disable their use. For example, a LOD will still
 * need to have all of the geometry loaded, just not shown or activated
 * because the LOD's internal behavior is disabled. Scripts are considered
 * to be behaviours, and they will not be loaded at all if behaviour loading
 * is disabled. However, we will take out other items. For example, we make
 * no attempt to load textures, scripts or anything else that may require
 * asynchronous loading of the content outside the single call to load().
 * <p>
 *
 * The default setup for runtime activities is
 * {@link org.xj3d.impl.core.eventmodel.ListsRouterFactory} and
 * {@link org.xj3d.impl.core.loading.MemCacheLoadManager}
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class X3DLoader extends BaseLoader {

    /**
     * Construct a default loader implementation with no flags set. When asked
     * to load a file it will not produce anything unless flags have been
     * set through the <code>setFlags()</code> method.
     */
    public X3DLoader() {
        this(0);
    }

    /**
     * Construct a loader with the given flags set.
     *
     * @param flags The flag values to be used
     * @throws RuntimeException The factory for loading VRML content could
     *   not be found
     */
    public X3DLoader(int flags) {
        super(flags);

        parserFactory.setProperty(VRMLParserFactory.REQUIRE_FORMAT_PROP,
                                  "X3D");
        parserFactory.setProperty(VRMLParserFactory.REQUIRE_VERSION_PROP,
                                  "1.0");
        vrml97Only = false;
    }

    /**
     * Set up the system properties needed to run the browser within the
     * context of a privileged block.
     */
    void setupPropertiesProtected() {

        URIResourceStreamFactory res_fac =
            URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof X3DResourceFactory)) {
            res_fac = new X3DResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }
    }
}
