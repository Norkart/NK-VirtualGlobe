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

package org.web3d.net.content;

// External imports
import java.io.InputStream;
import java.io.IOException;

import org.ietf.uri.ContentHandler;
import org.ietf.uri.ResourceConnection;
import org.xj3d.io.StreamContentContainer;

// Local imports
// None

/**
 * Content handler implementation for loading VRML Movie content from a URI
 * resource connection.
 * <P>
 *
 * The returned object type for this loader is an InputStream
 * <P>
 *
 * @author  Guy Carpenter
 * @version $Revision: 1.2 $
 */
class MovieContentHandler extends ContentHandler {

    /**
     * Construct a new instance of the content handler.
     */
    MovieContentHandler() {
    }

    /**
     * Given a fresh stream from a ResourceConnection,
     * read and create an object instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    public Object getContent(ResourceConnection resc)
        throws IOException {
        // REVISIT - need to work out something that Justin is
        // happy with.  We cannot pass the URL here, and we
        // also cannot pass the stream, since in the case of
        // multiple uses of the same object the stream will be
        // cached and reused, which of course fails.  So for
        // now we just pass back something which will be ignored,
        // and the node uses the URL to do it's own thing.
        return new Object();
    }
}
