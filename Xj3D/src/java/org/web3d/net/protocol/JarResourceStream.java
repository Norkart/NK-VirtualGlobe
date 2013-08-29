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

// Standard imports
import java.io.IOException;
import java.net.MalformedURLException;

import org.ietf.uri.URI;
import org.ietf.uri.URIUtils;
import org.ietf.uri.ResourceConnection;
import org.ietf.uri.URIResourceStream;
import org.ietf.uri.MalformedURNException;

// Application specific imports
// None

/**
 * A JAR protocol handler.
 * <P>
 *
 * The basic connection handler for dealing with URLs of the
 * type <CODE>jar:&lt;url&gt;!/&lt;entry&gt;</CODE> and URNs that resolve to
 * an item in a jar file.
 * <P>
 *
 * The path defines the name of JAR file and the entry in that file. If no
 * file is defined, then the whole JAR file is refered to.
 * <P>
 *
 * For details on URIs see the IETF working group:
 * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
 * <P>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class JarResourceStream extends URIResourceStream {
    /**
     * Explicit public constructor as required by java reflection.
     * Currently does nothing.
     */
    JarResourceStream() {
    }

    /**
     * Open a connection for the given URI. The host and port arguments for
     * this stream type are ignored. If a host is needed for a UNC name
     * then that is included in the path.
     *
     * @param host The host name to connect to
     * @param port The port on the host
     * @param path The path needed to access the resource using the given protocol
     * @exception IOException I/O errors reading from the stream
     * @exception IllegalArgumentException host, port or URI were invalid
     */
    protected ResourceConnection openConnection(String host,
                                                int port,
                                                String path)
        throws IOException, IllegalArgumentException {

        ResourceConnection res = null;

        // split the path into the two parts
        int index = path.indexOf('!');
        String uri_str = path.substring(0, index);
        String entry = null;

        if(path.length() > index + 1)
            entry = path.substring(index + 2);

        try {
            URI uri = URIUtils.createURI(uri_str);

            res = new JarResourceConnection(uri, entry);
        } catch(MalformedURLException mue) {
            throw new IllegalArgumentException("Cannot construct JAR file URL");
        } catch(MalformedURNException mne) {
            throw new IllegalArgumentException("Cannot construct JAR file URN");
        }

        return res;
    }
}
