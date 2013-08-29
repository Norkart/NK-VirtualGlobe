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

package org.web3d.vrml.scripting.sai;

// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.x3d.sai.ComponentInfo;
import org.web3d.x3d.sai.ProfileInfo;

/**
 * Description of a single profile.
 * <p>
 *
 * A profile is a short-hand way of describing an aggregation of components.
 * A valid profile will always have one or more components, but a title
 * string is optional.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SAIProfileInfo implements ProfileInfo {

    /** The name of this component */
    private final String name;

    /** The Title of the component */
    private final String title;

    /** The list of components */
    private ComponentInfo[] components;

    /**
     * Construct a basic component description that just has a name and level.
     *
     * @param name The name of the component
     * @param level The level of the component
     * @throws IllegalArgumentException The name was null or level < 1
     */
    SAIProfileInfo(String name, String title, ComponentInfo[] comps) {

        if((name == null) || (name.length() == 0))
            throw new IllegalArgumentException("Null name");

        if((comps == null) || (comps.length == 0))
            throw new IllegalArgumentException("No components provided");

        this.name = name;
        this.title = title;
        this.components = comps;
    }

    /**
     * Construct a component descriptor based on the internal representation
     * of same.
     *
     * @param info The description of the component to use
     */
    public SAIProfileInfo(org.web3d.vrml.lang.ProfileInfo info) {

        this.name = info.getName();
        this.title = info.getTitle();

        org.web3d.vrml.lang.ComponentInfo[] c_list = info.getComponents();
        components = new ComponentInfo[c_list.length];

        for(int i = 0; i < c_list.length; i++)
            components[i] = new SAIComponentInfo(c_list[i]);
    }

    /**
     * Get the name of this component.
     *
     * @return name The name of the component
     */
    public String getName() {
        return name;
    }

    /**
     * Get the title of this component. This is a long-form version that could
     * be used in a UI. If no title is set, will return null.
     *
     * @return The title string of this component
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the list of defined components for this profile. A profile will
     * always have one or more components.
     *
     * @return An array of the component definitions for this profile
     */
    public ComponentInfo[] getComponents() {
        return components;
    }

    /**
     * Return a formatted string version of this component that conforms to
     * the X3D 1.0 specification for VRML file encoding. The string will start
     * with the <code>PROFILE</code> keyword, as per spec.
     *
     * @return A correctly formatted string.
     */
    public String toX3DString() {
        return "PROFILE " + name;
    }
}
