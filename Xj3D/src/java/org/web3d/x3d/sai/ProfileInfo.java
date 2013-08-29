/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// Standard imports
import java.util.Map;

// Application specific imports
// none

/**
 * Description of a single profile.
 * <p>
 *
 * A profile is a short-hand way of describing an aggregation of components.
 * A valid profile will always have one or more components, but a title
 * string is optional.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface ProfileInfo {

    /**
     * Get the name of this component.
     *
     * @return name The name of the component
     */
    public String getName();

    /**
     * Get the title of this component. This is a long-form version that could
     * be used in a UI. If no title is set, will return null.
     *
     * @return The title string of this component
     */
    public String getTitle();

    /**
     * Get the list of defined components for this profile. A profile will
     * always have one or more components.
     *
     * @return An array of the component definitions for this profile
     */
    public ComponentInfo[] getComponents();

    /**
     * Return a formatted string version of this component that conforms to
     * the X3D 1.0 specification for VRML file encoding. The string will start
     * with the <code>PROFILE</code> keyword, as per spec.
     *
     * @return A correctly formatted string.
     */
    public String toX3DString();
}
