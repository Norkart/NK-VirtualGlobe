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
import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.vrml.parser.VRMLParserFactory;

/**
 * A Java3D file loader implementation for reading all Web3D file formats
 * building a Java3D scenegraph with them.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class Web3DLoader extends BaseLoader {

    /**
     * Construct a default loader implementation with no flags set. When asked
     * to load a file it will not produce anything unless flags have been
     * set through the <code>setFlags()</code> method.
     */
    public Web3DLoader() {
        this(0);
    }

    /**
     * Construct a loader with the given flags set.
     *
     * @param flags The flag values to be used
     * @throws RuntimeException The factory for loading VRML content could
     *   not be found
     */
    public Web3DLoader(int flags) {
        super(flags);
        vrml97Only = false;
    }

    /**
     * Set up the system properties needed to run the browser within the
     * context of a privileged block.
     */
    void setupPropertiesProtected() {

        URIResourceStreamFactory res_fac =
            URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof Web3DResourceFactory)) {
            res_fac = new Web3DResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }
    }
}
