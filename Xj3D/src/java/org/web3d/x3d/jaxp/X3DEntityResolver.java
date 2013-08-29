/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.jaxp;

// External imports
import java.io.InputStream;
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.HashMap;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

// Local imports
// None

/**
 * An entity resolver for both DOM and SAX models of the X3D document.
 * <p>
 * The entity resolver only handles queries for the DTD. It will find
 * any URI that ends in *.dtd and return an {@link org.xml.sax.InputSource}.
 * <p>
 * As the X3D specification does not yet define what the system resource
 * ID is, we'll take a guess. The current resolution scheme only strips the
 * name of the DTD from the URI and attempts to find that in the classpath.
 * <p>
 * To determine the DTD name it will search from the end of the string until it
 * finds a '/' character. The resulting string is treated as a filename to
 * search for and has the directory DTD/ prepended to the name.
 * This filename is then found in the CLASSPATH used by the
 * application using the standard Java resolution rules. Note that we do not
 * need to implement any more intelligent behaviour than this because if the
 * System or PublicID returned are files or URLs, the standard parser
 * mechanisms will load them. The only more intelligent behaviour that we may
 * wish to add in the future will be to resolve a full URN if we are given it.
 * <p>
 * The current implementation ignores the publicId information.
 */
public class X3DEntityResolver implements EntityResolver {

    /** Table of the allowed types of public IDs */
    private static HashMap allowedPublicIDs;

    /**
     * Set up the system ID mapping
     */
    static {
        allowedPublicIDs = new HashMap();
        allowedPublicIDs.put(X3DConstants.GENERAL_PUBLIC_ID_3_0,
                             "xml/DTD/x3d-3.0.dtd");
        allowedPublicIDs.put(X3DConstants.INTERCHANGE_PUBLIC_ID_3_0,
                             "xml/DTD/x3d-3.0.dtd");
        allowedPublicIDs.put(X3DConstants.INTERACTIVE_PUBLIC_ID_3_0,
                             "xml/DTD/x3d-3.0.dtd");
        allowedPublicIDs.put(X3DConstants.IMMERSIVE_PUBLIC_ID_3_0,
                             "xml/DTD/x3d-3.0.dtd");
        allowedPublicIDs.put(X3DConstants.FULL_PUBLIC_ID_3_0,
                             "xml/DTD/x3d-3.0.dtd");

        allowedPublicIDs.put(X3DConstants.GENERAL_PUBLIC_ID_3_1,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.INTERCHANGE_PUBLIC_ID_3_1,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.INTERACTIVE_PUBLIC_ID_3_1,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.IMMERSIVE_PUBLIC_ID_3_1,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.FULL_PUBLIC_ID_3_1,
                             "xml/DTD/x3d-3.1.dtd");

        allowedPublicIDs.put(X3DConstants.GENERAL_PUBLIC_ID_3_2,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.INTERCHANGE_PUBLIC_ID_3_2,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.INTERACTIVE_PUBLIC_ID_3_2,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.IMMERSIVE_PUBLIC_ID_3_2,
                             "xml/DTD/x3d-3.1.dtd");
        allowedPublicIDs.put(X3DConstants.FULL_PUBLIC_ID_3_2,
                             "xml/DTD/x3d-3.1.dtd");

        allowedPublicIDs.put(X3DConstants.OLD_PUBLIC_ID, "xml/DTD/x3d-3.0.dtd");
    }

    /**
     * Resolve the combination of system and public identifiers. This
     * resolver ignores the publicId information.
     *
     * @param publicId The public identifier to use (if set)
     * @param systemId The system identifier to resolve
     * @return An input source to the entity or null if not handled
     * @throws IOException An error reading the stream
     */
    public InputSource resolveEntity(String publicId, String systemId)
        throws IOException {

        InputSource ret_val = null;
        InputStream is = null;

        if(publicId != null) {
            is = resolveDTDFromPublic(publicId);

        }

        // If the public ID didn't resolve to anything useful, try using the
        // system ID instead
        if(is == null) {

            is = resolveDTDFromSystem(systemId);
        }


        if(is != null) {
            ret_val = new InputSource(is);
            ret_val.setPublicId(publicId);
            ret_val.setSystemId(systemId);
        }

        return ret_val;
    }

    /**
     * Resolve the DTD uri and return an InputStream used by this.
     *
     * @param uri The DTD uri to resolve
     * @return An input stream to the entity or null if not handled
     * @throws IOException An error reading the stream
     */
    public InputStream resolveDTDFromSystem(String uri) throws IOException {
        InputStream ret_val = null;

        // grab the system ID and remove the last word from it prior to
        // a '/' (if one exists).
        int pos = uri.lastIndexOf('/');

        String filename = uri;

        if(pos != -1)
            filename = uri.substring(pos + 1);

        filename = "xml/DTD/" + filename;

        return loadFile(filename);
    }

    /**
     * Resolve the DTD uri and return an InputStream used by this.
     *
     * @param id The DTD uri to resolve
     * @return An input stream to the entity or null if not handled
     * @throws IOException An error reading the stream
     */
    public InputStream resolveDTDFromPublic(String id) throws IOException {
        String filename = (String)allowedPublicIDs.get(id);
        if(filename == null)
            return null;

        return loadFile(filename);
    }

    /**
     * Internal convenience method to make use of a single privileged block.
     */
    private InputStream loadFile(final String filename) {
        return (InputStream)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    InputStream ret_val = null;


                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    ret_val = cl.getResourceAsStream(filename);
                    // Fallback mechanism for WebStart
                    if (ret_val == null) {
                        cl = X3DEntityResolver.class.getClassLoader();
                        ret_val = cl.getResourceAsStream(filename);
                    }

                    return ret_val;
                }
            }
        );
    }
}

