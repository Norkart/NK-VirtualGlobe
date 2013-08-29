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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.MalformedURLException;

import org.ietf.uri.URL;
import org.ietf.uri.ResourceConnection;

/**
 * Representation of a Javascript/ECMAScript resource.
 * <P>
 *
 * Implements the connection as a standard byte input stream. The javascript
 * protocol defined by the VRML97 specification allows you to inline the script
 * information. Therefore, for this system we just strip the protocol prefix
 * from the incoming URL and return the rest of the information as the input
 * stream.
 * <P>
 *
 * We have a number of limitations on the data supplied. From the base string
 * there is no way to determine the encoding type. This, in turn leads us to
 * generate an output stream of a byte array. When the content handler grabs
 * that stream, we don't know what the original encoding was and so
 * re-interpret the string contents using the default from the platform. This
 * will trash the original string's characters. Not good. Not sure of a good
 * way around this yet.
 * <P>
 *
 * This connection only supports input streams. The last modified time is not
 * known and always returns the default value.
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
 * @version $Revision: 1.2 $
 */
class JavascriptResourceConnection extends ResourceConnection {

    /** The default content type if none is supplied */
    private static final String DEFAULT_CONTENT_TYPE = "application/x-javascript";

    /** Alternate protocol type if the user defines ECMAScript as the protocol */
    private static final String ECMA_CONTENT_TYPE = "application/ecmascript";

    /** The parsed data after having header data removed */
    private String script;

    /** The content type of the data */
    private String contentType = DEFAULT_CONTENT_TYPE;

    /** The length of the content if we know what it is */
    private int contentLength = -1;

    /** Stream containing the current data */
    private ByteArrayInputStream stream;

    /**
     * Create an instance of this connection.
     *
     * @param uri The URI to establish the connection to
     * @exception MalformedURLException We stuffed up something in the filename
     */
    JavascriptResourceConnection(boolean isEcma, URL url, String path)
        throws MalformedURLException {

        super(url);


        script = path;

        // Not necessarily correct, but good first approximation
        contentLength = path.length();

        if(isEcma)
            contentType = ECMA_CONTENT_TYPE;
    }

    /**
     * Get the input stream for this.
     *
     * @return The stream
     */
    public InputStream getInputStream() throws IOException {
        // convertn the stream to
        if(stream == null) {
            stream = new ByteArrayInputStream(script.substring(11).getBytes());
            contentLength = stream.available();
        }

        return stream;
    }

    /**
     * Get the content type of the resource that this stream points to.
     * Returns a standard MIME type string. If the content type is not known then
     * <CODE>text/plain</CODE> is returned (the default for data protocol).
     *
     * @return The content type of this resource
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Connect to the named resource if not already connected. This is ignored
     * by this implementation.
     *
     * @exception An error occurred during the connection process
     */
    public void connect() throws IOException {
    }

    /**
     * Get the length of the content that is to follow on the stream. If the
     * length is unknown then -1 is returned. The content length could be the
     * the length of the raw stream or the object. Don't know yet????
     *
     * @return The length of the content in bytes or -1
     */
    public int getContentLength() {
        try {
          getInputStream();
        } catch(IOException ioe) {
          contentLength = -1;
        }

        // we could do some intelligent guessing here by looking to see if the
        // getContent returns an array and then return that??
        return contentLength;
    }

}
