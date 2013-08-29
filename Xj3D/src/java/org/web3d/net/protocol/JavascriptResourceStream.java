/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.net.protocol;

import java.io.IOException;
import java.util.HashSet;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.UnknownProtocolException;
import org.ietf.uri.URIResourceStream;
import org.ietf.uri.URL;

/**
 * A factory for producing resources specfic to VRML97/X3D.
 * <P>
 *
 * The current factory supports handlers for the javascript and ecmascript
 * protocol types. Because these protocols have the content type inlined
 * into string, the factory ignores the host and port parameters.
 * <P>
 *
 * For details on URIs see the IETF working group:
 * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
 * <P>
 *
 * This softare is released under the
 * <A HREF="http://www.gnu.org/copyleft/lgpl.html">GNU LGPL</A>
 * <P>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class JavascriptResourceStream extends URIResourceStream {

    /** Flag to indicate if this is an ecmascript or javascipt connection */
    private boolean useEcma;

    JavascriptResourceStream(boolean isEcma) {
        useEcma = isEcma;
    }

    public ResourceConnection openConnection(String host,
                                             int port,
                                             String path)
        throws UnknownProtocolException,
               IOException,
               IllegalArgumentException {

        ResourceConnection ret_val = null;

        // check if it is one of our local types
        String url_str = (useEcma ? "ecmascript:" : "javascript:") + path;
        URL url = new URL(url_str);

        ret_val = new JavascriptResourceConnection(useEcma, url, path);

        return ret_val;
    }
}
