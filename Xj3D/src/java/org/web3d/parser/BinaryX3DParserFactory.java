/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.parser;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.sav.VRMLReader;

import org.web3d.vrml.parser.VRMLParserFactory;

/**
 * Implementation of a factory that will only generate parsers of X3D binary
 * content.
 * <p>
 *
 * The parser factory will handle all 3 basic forms of request - VRML 2.0, X3D
 * and a specific request for a binary parser.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BinaryX3DParserFactory extends VRMLParserFactory {

    /** The list of features supported by this implementation */
    private static HashMap featureSet;

    /**
     * Static constructor to initialise the values in the featureSet.
     */
    static {
        featureSet = new HashMap();
        featureSet.put(X3D_FEATURE, "1.0");
    }


    /**
     * Construct a default instance of this factory.
     */
    public BinaryX3DParserFactory() {
    }

    /**
     * Request a new instance of a VRMLReader to parse documents.
     *
     * @return A new reader instance
     */
    public VRMLReader newVRMLReader() {

        return new BinaryReader();
    }

    /**
     * Check to see if this implementation has the nominated feature. If there
     * is no version information passed, it will look for features of all
     * versions. The special version string of '*' may be used to denote all
     * or all versions of the named feature.
     *
     * @param feature The name of the feature requested
     * @param version The version of the feature required or null for none
     * @return true if the feature and version is supported
     */
    public boolean hasFeature(String feature, String version) {
        boolean ret_val = false;

        String feature_version = (String)featureSet.get(feature);

        if(feature_version != null) {
            if(version != null)
                ret_val = feature_version.equalsIgnoreCase(version);
            else
                ret_val = feature_version.equalsIgnoreCase("anyVersion");
        }

        return ret_val;
    }
}
