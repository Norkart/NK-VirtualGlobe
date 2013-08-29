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
// none

// Local imports
// none

/**
 * A simple implementation of the data holder class  for file caching.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class DefaultCacheDetails implements CacheDetails {

    /** The uri of the content */
    private String uri;

    /** The mime type of the content */
    private String contentType;

    /** The real content */
    private Object content;

    /**
     * Construct an instance of this class for the given details
     *
     * @param uri The uri of the content
     * @param type The content mime type
     * @param content The actual content
     */
    DefaultCacheDetails(String uri, String type, Object content) {
        this.uri = uri;
        this.contentType = type;
        this.content = content;
    }

    /** The URI that represents this object */
    public String getURI() {
        return uri;
    }

    /** Get the content type of this cached object */
    public String getContentType() {
        return contentType;
    }

    /** Get the actual content of this object */
    public Object getContent() {
        return content;
    }
}
