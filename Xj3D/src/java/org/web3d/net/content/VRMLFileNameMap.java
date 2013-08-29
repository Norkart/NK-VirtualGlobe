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
import java.util.HashMap;

import org.ietf.uri.FileNameMap;

// Application specific imports
// none

/**
 * An implementation of a file name mapping for handling VRML file types.
 * <p>
 *
 * When asked for a .wrl file it will return <code>model/vrml</code>. For the
 * reverse mapping it will handle both the old and new types
 * <p>
 *
 * This filename mapping must be registered with the URI class by the user in
 * order to work. You can use the following code to do this:
 *
 * <pre>
 *  import org.ietf.uri.URI;
 *  import org.ietf.uri.FileNameMap;
 *
 *  ...
 *
 *       FileNameMap fn_map = URI.getFileNameMap();
 *       if(!(fn_map instanceof VRMLFileNameMap)) {
 *           fn_map = new VRMLFileNameMap(fn_map);
 *           URI.setFileNameMap(fn_map);
 *       }
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.7 $
 */
public class VRMLFileNameMap implements FileNameMap
{
    /** A mapping of file extensions (key) to mime types (value) */
    private static HashMap extensionMap;

    /** A mapping of mime types (value) to file extensions (key) */
    private static HashMap reverseMap;

    /** A followup map that may help to resolve if we don't */
    private FileNameMap nextMap;

    /**
     * Static constructor builds the lookup map information.
     */
    static {
        extensionMap = new HashMap();
        extensionMap.put("wrl", "model/vrml");
        extensionMap.put("xml", "application/xml");
        extensionMap.put("x3d", "model/x3d+xml");
        extensionMap.put("x3dz", "model/x3d+xml");
        extensionMap.put("x3dv", "model/x3d+vrml");
        extensionMap.put("x3dvz", "model/x3d+vrml");
        extensionMap.put("x3db", "model/x3d+binary");
        extensionMap.put("js", "application/ecmascript");
        extensionMap.put("gz", "application/x-gzip");

        reverseMap = new HashMap();
        reverseMap.put("model/vrml", "wrl");
        reverseMap.put("x-world/x-vrml", "wrl");
        reverseMap.put("application/xml", "xml");
        reverseMap.put("model/x3d+xml", "x3d");
        reverseMap.put("model/x3d+vrml", "x3dv");
        reverseMap.put("model/x3d+binary", "x3db");
        reverseMap.put("application/ecmascript", "js");
        reverseMap.put("application/x-gzip", "gz");
    }

    /**
     * Create a default filename map that does not delegate to any other
     * map if we can't resolve it.
     */
    public VRMLFileNameMap() {
        this(null);
    }

    /**
     * Create a filename map that will delegate to the given map if we cannot
     * resolve the name locally. If the parameter is null, no checking will
     * be done.
     *
     * @param map The map to delegate to
     */
    public VRMLFileNameMap(FileNameMap map) {
        nextMap = map;
    }

    /**
     * Fetch the content type for the given file name. If we don't
     * understand the file name, return null.
     *
     * @param filename The name of the file to check
     * @return The content type for that file
     */
    public String getContentTypeFor(String filename) {

        int index = filename.lastIndexOf('.');
        String ext = filename.substring(index + 1);

        ext = ext.toLowerCase();

        String ret_val = (String)extensionMap.get(ext);

        if((ret_val == null) && (nextMap != null)) {
            ret_val = nextMap.getContentTypeFor(filename);
        }

        return ret_val;
    }

    /**
     * Get the standardised extension used for the given MIME type. This
     * provides a reverse mapping feature over the standard
     * {@link java.net.FileNameMap} that only supplies the opposite method.
     *
     * @param mimetype The mime type to check for
     * @return The extension or <CODE>null</CODE> if it cannot be resolved
     */
    public String getFileExtension(String mimetype) {
        String ret_val = (String)reverseMap.get(mimetype);

        if((ret_val == null) && (nextMap != null)) {
            ret_val = nextMap.getFileExtension(mimetype);
        }

        return ret_val;
    }
}
