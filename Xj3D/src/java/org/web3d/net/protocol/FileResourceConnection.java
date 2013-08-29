/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005
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

// External imports
import java.io.*;

import java.net.MalformedURLException;
import java.util.HashSet;

// Local imports
// None

/**
 * Extended implementation of the file resource connection that looks for
 * X3D-specific file extensions to set the content type correctly.
 *
 *
 */
class FileResourceConnection extends vlc.net.protocol.file.FileResourceConnection
{
    /** Set of types that are internally labelled with the gzip input stream type */
    private static final HashSet X3D_GZIP_TYPES;

    /**
     * Set of types that are internally labelled as something we need to perform
     * extra processing steps on. Anything not in this list is returned simply.
     */
    private static final HashSet NONSTANDARD_TYPES;

    /** The content encoding, if known */
    private String encoding;

    /** The MIME Type of the described file. Null if not yet known */
    private String contentType;

    /**
     * Static constructor to populate the global set.
     */
    static {
        NONSTANDARD_TYPES = new HashSet();
        NONSTANDARD_TYPES.add("application/x-gzip");
        NONSTANDARD_TYPES.add("model/x3d+vrml");
        NONSTANDARD_TYPES.add("model/x3d+xml");

        X3D_GZIP_TYPES = new HashSet();
        X3D_GZIP_TYPES.add("x3dvz");
        X3D_GZIP_TYPES.add("x3dz");
    }

    /**
     * Create an instance of this connection.
     *
     * @param uri The URI to establish the connection to
     * @exception MalformedURLException We stuffed up something in the filename
     */
    FileResourceConnection(String uri)
        throws MalformedURLException {

        super(uri);
    }

    /**
     * Get the content type of the resource that this stream points to.
     * Returns a standard MIME type string. If the content type is not known then
     * <CODE>unknown/unknown</CODE> is returned (the default implementation).
     *
     * @return The content type of this resource
     */
    public String getContentType() {

        if(contentType != null)
            return contentType;

        String content_type = super.getContentType();

        if(!NONSTANDARD_TYPES.contains(content_type)) {
            contentType = content_type;
            return contentType;
        }

        int dot_idx = path.lastIndexOf(".");

        // If this is a gzip extension, then look back one further "." and
        // see if we have anything matching that.

        if(content_type.equals("application/x-gzip")) {
            String sub_path = path.substring(0, dot_idx);
            String new_type = findContentType(sub_path);

            if(new_type != null) {
                encoding = "x-gzip";
                contentType = new_type;
            }
        } else {
            String ext = path.substring(dot_idx + 1);

            if(X3D_GZIP_TYPES.contains(ext)) {
                encoding = "x-gzip";
            }

            contentType = content_type;
        }

        return contentType;
    }

    /**
     * Get the encoding type of the content. Allows for dealing with
     * multi-lingual content and also multiple buried types. If it cannot be
     * determined then null  is returned (which is the default implementation).
     *
     * @return The encoding string or null
     */
    public String getContentEncoding() {
        if((encoding == null) && (contentType == null))
            getContentType();

        return encoding;
    }
}
