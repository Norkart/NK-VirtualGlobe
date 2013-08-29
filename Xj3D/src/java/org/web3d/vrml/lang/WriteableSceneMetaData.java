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
 * @version $Revision: 1.1 $
 */
public class WriteableSceneMetaData extends SceneMetaData {

    /**
     * Create a new metadata instance for a scene that describes the
     * given subset of information of the specification.
     *
     * @param ver The version string to be supported
     * @param vrml true if this is a VRMLX spec, false for X3D
     * @param enc The encoding type used for the source file
     */
    public WriteableSceneMetaData(String ver, boolean vrml, int enc) {
        super(ver, vrml, enc);
    }

    /**
     * Set the name of the profile that is used by this scene. No validity
     * checking is performed on the name.
     *
     * @param prof The profile name
     */
    public void setProfileName(String prof) {
        profile = prof;
    }

    /**
     * Add a component description to this meta data. This describes
     * additional component capabilities over the main profile.
     *
     * @param comp The component information to add
     */
    public void addComponent(ComponentInfo comp) {
        if(comp != null)
            components.add(comp);
    }

    /**
     * Add a meta tag data item to this scene. Both name and value must be
     * non-null.
     *
     * @param name The name of the tag to add
     * @param value The value of the tag
     * @throws NullPointerException The name or value were null
     */
    public void addMetaData(String name, String value) {
        if(name == null)
            throw new NullPointerException("Name was null");

        if(value == null)
            throw new NullPointerException("Value was null");

        metaData.put(name, value);
    }

    /**
     * Remove the named tag from the map. If tag name does not exist, the
     * request is silently ignored.
     *
     * @param name The name of the tag to remove
     */
    public void removeMetaData(String name) {
        metaData.remove(name);
    }
}
