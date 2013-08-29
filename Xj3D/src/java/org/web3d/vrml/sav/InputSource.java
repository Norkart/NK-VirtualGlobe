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

package org.web3d.vrml.sav;

// External imports
import org.ietf.uri.*;
import org.ietf.uri.event.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.zip.GZIPInputStream;
import java.net.URLDecoder;

import org.ietf.uri.URIUtils;

// Local imports
import org.xj3d.io.*;

/**
 * Representation of an input stream of bytes to the reader.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public class InputSource {

    /** The encoding of the underlying stream. */
    private String encoding;

    /** The URL that represents the base of the passed file */
    private String baseURL;

    /** The fully qualified URL to the resource */
    private String realURL;

    /** The inputstream supplying bytes to us */
    private InputStream stream;

    /** The Reader representing the supply of characters to us */
    private Reader reader;

    /** The read progress listener */
    private ReadProgressListener readProgressListener;

    /** The progress listener */
    private ProgressListener progressListener;

    /** The size in bytes to issue update events */
    private int updateSize;

    /** The content type of the source */
    private String contentType;

    /**
     * Create an input source representation of the given URI string. This
     * may use the given string as a fully qualified URI that needs resolving.
     *
     * @param uri The URI to open.
     */
    public InputSource(String uri) {
        realURL = uri;

        if (realURL.startsWith("file:")) {
            realURL = realURL.replace('\\', '/');
            if (realURL.charAt(7) != '/') {
                // Change file:/ to file:///
                realURL = "file:///" + realURL.substring(6);
            }
        }

        String path = null;

        try {
            String[] stripped_file = URIUtils.stripFile(URLDecoder.decode(uri, "UTF-8"));

            path = stripped_file[0];
        } catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        // Would this really work? What if the uri is a URN?
        int index = path.lastIndexOf("/");
        baseURL = path.substring(0, index + 1);
    }

    /**
     * Create an input source representing the given file. It does not check
     * for the file existing or a directory on creation. This will be done at
     * the point stream is requested
     *
     * @param file The file to be used as the source
     */
    public InputSource(File file) {
        try {
            java.net.URL full = file.toURL();
            realURL = full.toString();

            if (realURL.startsWith("file:")) {
                realURL = realURL.replace('\\', '/');
                if (realURL.charAt(7) != '\\') {
                    // Change file:/ to file:///
                    realURL = "file:///" + realURL.substring(6);
                }
            }

            String path = null;

            try {
                String[] stripped_file = URIUtils.stripFile(URLDecoder.decode(realURL, "UTF-8"));

                path = stripped_file[0];
            } catch(UnsupportedEncodingException uee) {
                uee.printStackTrace();
            }

            int index = path.lastIndexOf("/");
            baseURL = path.substring(0, index + 1);
        } catch(MalformedURLException mue) {
        }
    }

    /**
     * Create an input source representing the given URL. It does not open the
     * URL until the stream is requested.
     *
     * @param url The URL to use
     */
    public InputSource(java.net.URL url) {
        realURL = url.toExternalForm();

        if (realURL.startsWith("file:")) {
            realURL = realURL.replace('\\', '/');
            if (realURL.charAt(7) != '\\') {
                // Change file:/ to file:///
                realURL = "file:///" + realURL.substring(6);
            }
        }

        String path = null;

        try {
            String[] stripped_file = URIUtils.stripFile(URLDecoder.decode(realURL, "UTF-8"));

            path = stripped_file[0];
        } catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        int index = path.lastIndexOf("/");
        baseURL = path.substring(0, index + 1);
    }

    /**
     * Create an input source representing the given URL. It does not open the
     * URL until the stream is requested.
     *
     * @param url The URL to use
     */
    public InputSource(URL url) {
        realURL = url.toExternalForm();

        if (realURL.startsWith("file:")) {
            realURL = realURL.replace('\\', '/');
            if (realURL.charAt(7) != '\\') {
                // Change file:/ to file:///
                realURL = "file:///" + realURL.substring(6);
            }
        }

        String path = null;

        try {
            String[] stripped_file = URIUtils.stripFile(URLDecoder.decode(realURL, "UTF-8"));

            path = stripped_file[0];
        } catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        int index = path.lastIndexOf("/");
        baseURL = path.substring(0, index + 1);
    }

    /**
     * Create an input source from the input stream and the defined base URL.
     * The base is required because it is not possible to determine this from
     * the stream.
     *
     * @param urlBase The name of the base URL for this stream
     * @param is The underlying stream to use
     */
    public InputSource(String urlBase, InputStream is) {
        this(urlBase, is, null);
    }

    /**
     * Create an input source from the input stream and the defined base URL
     * with the option of providing a known full URL. The base is required
     * because it is not possible to determine this from the stream. If the
     * real URL is not known, then set a value of null.
     *
     * @param urlBase The name of the base URL for this stream
     * @param is The underlying stream to use
     * @param fullUrl The fully qualified URL string
     */
    public InputSource(String urlBase, InputStream is, String fullUrl) {
        baseURL = urlBase;
        stream = is;
        realURL = fullUrl;
    }

    /**
     * Create an input source from the reader and the defined base URL. The
     * base is required because it is not possible to determine this from
     * the stream.
     *
     * @param urlBase The name of the base URL for this stream
     * @param rdr The underlying reader to use
     */
    public InputSource(String urlBase, Reader rdr) {
        this(urlBase, rdr, null);
    }

    /**
     * Create an input source from the reader and the defined base URL with
     * with the option of providing a known full URL. The base is required
     * because it is not possible to determine this from the stream. If the
     * real URL is not known, then set a value of null.
     *
     * @param urlBase The name of the base URL for this stream
     * @param rdr The underlying Reader to use
     * @param fullUrl The fully qualified URL string
     */
    public InputSource(String urlBase, Reader rdr, String fullUrl) {
        baseURL = urlBase;
        reader = rdr;
        realURL = fullUrl;
    }

    /**
     * Get the encoding, binary or string of the underlying stream. The
     * encoding is that of the character stream of the file rather than the
     * VRML encoding statement in the file header eg UTF8. This will not be
     * available until the stream has been opened.
     *
     * @return The encoding string
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get a stream of the characters from the source. The encoding is the
     * same as that given by the getEncoding() method. If the underlying
     * stream has not been opened, then this will force it to open.
     *
     * @return The reader representing the underlying stream
     * @throws IOException An error opening the stream
     */
    public Reader getCharacterStream() throws IOException {
        Reader ret_val = null;

        if(reader == null) {
            if(stream == null) {

                URL url;
                try {
                    url = new URL(realURL);

                    final ResourceConnection conn = url.getResource();

                    if (progressListener != null)
                        conn.addProgressListener(progressListener);

                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run()  throws IOException {
                                conn.connect();
                                return null;
                            }
                        }
                    );

                    stream = conn.getInputStream();
                    contentType = conn.getContentType();
                    encoding = conn.getContentEncoding();

                    // If it is a gzip stream, then wrap the ordinary stream with
                    // something that can decode it.
                    if (encoding != null && encoding.equals("x-gzip")) {
                        stream = new GZIPInputStream(stream);
                    }

                    if (readProgressListener != null) {
                        ReportableInputStreamReader ris =
                            new ReportableInputStreamReader(false,
                                                            updateSize,
                                                            readProgressListener,
                                                            stream);

                        ret_val = ris;

                    } else {
                        ret_val = new InputStreamReader(stream);
                    }


                } catch(MalformedURLException mue) {
                    throw new IOException("Unable to locate file");
                } catch(PrivilegedActionException pae) {
                    throw (IOException)pae.getException();
                }
            } else {
                if (readProgressListener != null) {
                    ret_val = new ReportableInputStreamReader(false,
                                                              updateSize,
                                                              readProgressListener,
                                                              stream);

                } else {
                    ret_val = new InputStreamReader(stream);
                }
            }

            reader = ret_val;
        } else {
            if (!(reader instanceof ReportableReader) && readProgressListener != null) {
                ret_val = new ReportableReader(false,
                                               updateSize,
                                               readProgressListener,
                                               reader);

            } else {
                ret_val = reader;
            }
        }

        return ret_val;
    }

    /**
     * Get a stream of raw bytes from the source. If the underlying stream
     * has not been opened, this will force it to open. The current
     * implementation barfs if the user supplied a raw stream.
     *
     * @return The stream representing the underlying bytes
     * @throws IOException An error opening the stream
     */
    public InputStream getByteStream() throws IOException {
        if((reader != null) && (stream == null))
            throw new IOException("Raw reader provided. Can't make stream");

        if(stream == null) {
            URL url;

            try {
                url = new URL(realURL);

                final ResourceConnection conn = url.getResource();

                if (progressListener != null)
                    conn.addProgressListener(progressListener);

                AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run()  throws IOException {
                            conn.connect();
                            return null;
                        }
                    }
                );

                stream = conn.getInputStream();
                contentType = conn.getContentType();
                encoding = conn.getContentEncoding();

                if (encoding != null && encoding.equals("x-gzip")) {
                    stream = new GZIPInputStream(stream);
                }

                if (readProgressListener != null) {
                    ReportableInputStream ris =
                        new ReportableInputStream(false,
                                                  updateSize,
                                                  readProgressListener,
                                                  stream);

                    return ris;
                }
            } catch(MalformedURLException mue) {
                throw new IOException("Unable to locate file");
            } catch(PrivilegedActionException pae) {
                throw (IOException)pae.getException();
            }
        } else {
            if (!(stream instanceof ReportableInputStream) && readProgressListener != null) {
                stream = new ReportableInputStream(false,
                                                   updateSize,
                                                   readProgressListener,
                                                   stream);
            }
        }

        return stream;
    }

    /**
     * Get the base URL of this stream. This is used by code that may need to
     * resolve other relative URIs in the stream.
     *
     * @return A string representing the base URL of this connection
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Get the fully qualified URL string to the source. If this is not known,
     * then null is returned and the user should use the base URL.
     *
     * @return A string representing the full URL of this connection or null
     */
    public String getURL() {
        return realURL;
    }

    /**
     * Close the underlying stream used by the source.
     *
     * @throws IOException An error closing the stream
     */
    public void close() throws IOException {
        if(stream != null)
            stream.close();

        if(reader != null)
            reader.close();
    }

    /**
     * Set the read progress listener.  If set it will be notified of read
     * progress.  This listener is used to expose the stream state to the URI library.
     *
     * @param listener The progress listener.
     * @param updateSize The number of bytes before issuing an update message.
     */
    public void setReadProgressListener(ReadProgressListener listener,
                                        int updateSize) {
        readProgressListener = listener;
        this.updateSize = updateSize;
    }

    /**
     * Set the progress listener.  This will notify the listener on changes
     * in download state and progress.
     *
     * @param listener The progress listener.
     */
    public void setProgressListener(ProgressListener listener) {
        progressListener = listener;
    }

    /**
     * Set the content type of this source.
     */
    public void setContentType(String type) {
        contentType = type;
    }

    /**
     * Get the content type of this source.
     */
    public String getContentType() {
        return contentType;
    }
}
