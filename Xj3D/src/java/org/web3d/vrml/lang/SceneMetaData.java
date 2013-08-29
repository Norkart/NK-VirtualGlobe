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

package org.web3d.vrml.lang;

// Standard imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Application specific imports
// none

/**
 * MetaData related to the scene.
 * <p>
 *
 * MetaData provides setup information that is useful when trying to blend
 * multiple scenes together. For example, it will tell you about what version
 * and encoding the source file was in.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class SceneMetaData {

    /** The scene was built from userland code using scripting */
    public static final int SCRIPTED_ENCODING = 0;

    /** VRML 1.0 ASCII encoding */
    public static final int ASCII_ENCODING = 1;

    /** VRML97 classic encoding (UTF8) */
    public static final int VRML_ENCODING = 2;

    /** XML encoding */
    public static final int XML_ENCODING = 3;

    /** Binary encoding */
    public static final int BINARY_ENCODING = 4;

    /** MPEG-4 BIFS encoding */
    public static final int BIFS_ENCODING = 5;

    /**
     * The index to use for the start of a custom encoding representation
     * if an end user wants to build their own custom parser.
     */
    public static final int LAST_STD_ENCODING = 100;



    /** The file version that was loaded */
    private final String version;

    /** Are we X3D or VRML spec? */
    private final boolean isVrml;

    /** What file encoding was used for this spec */
    private final int encoding;

    /** The profile name used, if any. */
    protected String profile;

    /** List of components that were used */
    protected ArrayList components;

    /** Map of Meta tag information in name/value pairs */
    protected HashMap metaData;

    /**
     * Create a new metadata instance for a scene that describes the
     * given subset of information of the specification.
     *
     * @param ver The version string to be supported
     * @param vrml true if this is a VRMLX spec, false for X3D
     * @param enc The encoding type used for the source file
     */
    protected SceneMetaData(String ver, boolean vrml, int enc) {
        isVrml = vrml;
        encoding = enc;

        // check the version string and strip any crap from it.
        String ver_tmp = ver.trim();
        if(!Character.isDigit(ver_tmp.charAt(0))) {
            // run along to find where it isn't a string
            int i;
            char[] ch = ver_tmp.toCharArray();
            int start = 0;
            int end = ch.length - 1;

            for(i = 0; i < ch.length; i++) {
                if(Character.isDigit(ch[i])) {
                    start = i;
                    break;
                }
            }

            for(i = end; i >= 0; i--) {
                if(Character.isDigit(ch[i])) {
                    end = i;
                    break;
                }
            }

            ver_tmp = new String(ch, start, (end - start + 1));
        }

        version = ver_tmp;

        components = new ArrayList();
        metaData = new HashMap();
    }

    /**
     * Create a new, non-writeable instance that copies the information from
     * the given source
     *
     * @param src The metadata to source the information from
     */
    public SceneMetaData(SceneMetaData src) {
        isVrml = src.isVrml;
        encoding = src.encoding;
        version = src.version;

        components = new ArrayList(src.components);
        metaData = new HashMap(src.metaData);
    }

    /**
     * Get the specification version name that was used to describe this
     * scene. The version is a string that is relative to the specification
     * used and is in the format "X.Y" where X and Y are integer values
     * describing major and minor versions, respectively.
     *
     * @return The version used for this scene
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the flag indicating what specification this is refering to.
     *
     * @return true if this is a VRML spec, false for X3D
     */
    public boolean isVrmlSpec() {
        return isVrml;
    }

    /**
     * Get the encoding of the original file type.
     *
     * @return The encoding description
     */
    public int getEncoding() {
        return encoding;
    }

    /**
     * Get the name of the profile used by this scene. If the profile is
     * not set, will return null.
     *
     * @return The name of the profile, or null
     */
    public String getProfileName() {
        return profile;
    }

    /**
     * Get the list of all the components declared in the scene. If there were
     * no components registered, this will return null.
     *
     * @return The components declared or null
     */
    public ComponentInfo[] getComponents() {
        ComponentInfo[] ret_val = null;

        if(components.size() != 0) {
            ret_val = new ComponentInfo[components.size()];
            components.toArray(ret_val);
        }

        return ret_val;
    }

    /**
     * Get the meta data mapping from this scene. The map returned cannot
     * be changed and represents the current internal state.
     *
     * @return The current meta tag mappings
     */
    public Map getMetaData() {
        return Collections.unmodifiableMap(metaData);
    }
}
