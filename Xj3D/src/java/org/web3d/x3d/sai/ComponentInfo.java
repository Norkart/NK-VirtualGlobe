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
 * Description of a single component.
 * <p>
 *
 * A component description contains many useful pieces of information about
 * the requirements. At the basic level, it is just a name and a level. In
 * addition to that, we can include supplemental information, such as who the
 * provider of that component is, URL information and more.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface ComponentInfo {

    /**
     * Get the name of this component.
     *
     * @return name The name of the component
     */
    public String getName();

    /**
     * Get the level of the component. A level is always greater than zero.
     * The level information may represent one of two things, depending on
     * how the component info was created. When created as part of a file that
     * is requesting a specific level of support, the level will indicate the
     * requested level, not the maximum available on the system. When this is
     * returned from a query of the system to see what components are available
     * then the level is maximum supported by the implementation.
     *
     * @return The level indicator
     */
    public int getLevel();

    /**
     * Get the title of this component. This is a long-form version that could
     * be used in a UI. If no title is set, will return null.
     *
     * @return The title string of this component
     */
    public String getTitle();

    /**
     * Get the URL of the provider. This is used for user interface information
     * to point an end user at someone who has implemented this bit of
     * functionality. It is not used by the system to download the component
     * or its definition.
     *
     * @return The URL of the provider as a string
     */
    public String getProviderURL();

    /**
     * Return a formatted string version of this component that conforms to
     * the X3D 1.0 specification for VRML file encoding. The string will start
     * with the <code>COMPONENT</code> keyword, as per spec.
     *
     * @return A correctly formatted string.
     */
    public String toX3DString();
}
