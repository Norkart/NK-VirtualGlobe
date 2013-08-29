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

package org.web3d.vrml.scripting.external.sai;

// Application specific imports
import org.web3d.x3d.sai.ComponentInfo;

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
 * @version $Revision: 1.2 $
 */
class SAIComponentInfo implements ComponentInfo {

    /** The name of this component */
    private final String name;

    /** The level of the component */
    private final int level;

    /** The Title of the component */
    private final String title;

    /** The URL of the provider of this component */
    private final String providerUrl;

    /**
     * Construct a basic component description that just has a name and level.
     *
     * @param name The name of the component
     * @param level The level of the component
     * @throws IllegalArgumentException The name was null or level < 1
     */
    SAIComponentInfo(String name, int level) {
        this(name, level, null, null);
    }

    /**
     * Construct a component descriptor based on the internal representation
     * of same.
     *
     * @param info The description of the component to use
     */
    public SAIComponentInfo(org.web3d.vrml.lang.ComponentInfo info) {
        this.name = info.getName();
        this.level = info.getLevel();
        this.title = info.getTitle();
        this.providerUrl = info.getProviderURL();
    }

    /**
     * Construct a full component description. Typically used within a node
     * factory or some other place that knows how to find this information
     * out.
     *
     * @param name The name of the component
     * @param level The level of the component
     * @param title The title of this component
     * @param url The provider URL
     * @throws IllegalArgumentException The name was null or level < 1
     */
    SAIComponentInfo(String name, int level, String title, String url) {

        if(name == null)
            throw new IllegalArgumentException("Null name");

        if(level < 1)
            throw new IllegalArgumentException("Level <= 0");

        this.name = name;
        this.level = level;

        this.title = title;
        this.providerUrl = url;
    }

    /**
     * Copy constructor for creating a derived version of this component
     * info but with a different level set. Useful for creating instances
     * for individual files.
     *
     * @param info The source to extract information from
     * @param level The new level to use
     * @throws IllegalArgumentException level < 1
     */
    SAIComponentInfo(ComponentInfo info, int level) {

        if(level < 1)
            throw new IllegalArgumentException("Level <= 0");

        this.name = info.getName();
        this.level = level;

        this.title = info.getTitle();
        this.providerUrl = info.getProviderURL();
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
    public int getLevel() {
        return level;
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
     * Get the URL of the provider. This is used for user interface information
     * to point an end user at someone who has implemented this bit of
     * functionality. It is not used by the system to download the component
     * or its definition.
     *
     * @return The URL of the provider as a string
     */
    public String getProviderURL() {
        return providerUrl;
    }

    /**
     * Return a formatted string version of this component that conforms to
     * the X3D 1.0 specification for VRML file encoding. The string will start
     * with the <code>COMPONENT</code> keyword, as per spec.
     *
     * @return A correctly formatted string.
     */
    public String toX3DString() {
        return "COMPONENT " + name + ':' + level;
    }
}
