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

package org.xj3d.loaders.ogl;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

// Local imports
import org.web3d.browser.BrowserCore;
import org.xj3d.core.loading.WorldLoaderManager;
import org.web3d.net.protocol.VRML97ResourceFactory;
import org.web3d.vrml.parser.VRMLParserFactory;

/**
 * A Java3D file loader implementation for reading VRML97 utf8 files only
 * and building a Java3D scenegraph with them.
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
 * @version $Revision: 1.4 $
 */
public class VRML97Loader extends BaseLoader {

    /**
     * Construct a default loader implementation with no flags set. When asked
     * to load a file it will not produce anything unless flags have been
     * set through the <code>setFlags()</code> method.
     */
    public VRML97Loader() {

        vrml97Only = true;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseLoader
    //---------------------------------------------------------------

    /**
     * Set up the system properties needed to run the browser within the
     * context of a privileged block.
     */
    protected void setupPropertiesProtected(BrowserCore core,
					    WorldLoaderManager wlm) {

        URIResourceStreamFactory res_fac =
            URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof VRML97ResourceFactory)) {
            res_fac = new VRML97ResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }
    }

    /**
     * Set up any derived-class specific properties for the parser factory
     * instance. This will be called just before a new parser factory will
     * be instantiated.
     *
     * @param factory The factory instance to place any configuration info
     */
    void setupParserFactory(VRMLParserFactory factory) {
        factory.setProperty(VRMLParserFactory.REQUIRE_FORMAT_PROP, "VRML");
        factory.setProperty(VRMLParserFactory.REQUIRE_VERSION_PROP, "2.0");
    }
}
