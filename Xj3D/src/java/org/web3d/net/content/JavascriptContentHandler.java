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

package org.web3d.net.content;

// Standard imports
import java.io.InputStream;
import java.io.*;
import java.net.URLConnection;

import org.ietf.uri.ContentHandler;
import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URIUtils;


// Application specific imports
// none

/**
 * Content handler implementation for loading VRML UTF8 content from a URI
 * resource connection.
 * <P>
 *
 * The returned object type for this loader is a
 * {@link org.web3d.vrml.nodes.VRMLScene}. It does not load
 * any further content apart from the contents of this file. If this file
 * contains further content then the receiver of this object must organise to
 * have all the routes processed, external nodes loaded etc.
 * <p>
 * This implementation also loads everything in the incoming file. There is no
 * filtering of the content.
 * <p>
 *
 * For details on URIs see the IETF working group:
 * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
 * <P>
 *
 * @author  Justin Couch
 * @version $Revision: 1.5 $
 */
class JavascriptContentHandler extends ContentHandler {

    /**
     * Construct a new instance of the content handler.
     *
     * @param builderFactory The factory used to create scene builders
     * @param clk The clock to use for content loading of this world
     */
    JavascriptContentHandler() {
    }

    /**
     * Given a fresh stream from a ResourceConnection, read and create an object
     * instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    public Object getContent(ResourceConnection resc)
        throws IOException {

        InputStream is = new BufferedInputStream(resc.getInputStream());

        StringBuffer buff = new StringBuffer(1024);
        byte[] b = new byte[1024];
        int len;

        while(true) {
            len = is.read(b);

            if (len < 0)
                break;

            buff.append(new String(b,0,len));
        }

        return buff.toString();
    }
}
