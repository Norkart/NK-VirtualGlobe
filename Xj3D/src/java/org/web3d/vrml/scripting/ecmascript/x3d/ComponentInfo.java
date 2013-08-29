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

package org.web3d.vrml.scripting.ecmascript.x3d;

// Standard imports
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.web3d.util.HashSet;
import org.web3d.vrml.scripting.ecmascript.builtin.AbstractScriptableObject;

/**
 * ProfileInfo miscellaneous object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class ComponentInfo extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** The name of the profile */
    private final String name;

    /** The level of the profile */
    private final Integer level;

    /** The title string of the profile */
    private final String title;

    /** The provider URL of the profile */
    private final String url;

    static {
        propertyNames = new HashSet();
        propertyNames.add("name");
        propertyNames.add("level");
        propertyNames.add("title");
        propertyNames.add("providerUrl");
    }

    /**
     * Construct a component descriptor for the given information.
     *
     * @param name The name of this component
     * @param level The current level or max available level
     * @param title An arbitrary title string
     * @param url An optional provider url
     */
    public ComponentInfo(String name, int level, String title, String url) {
        super("ComponentInfo");

        this.name = name;
        this.level = new Integer(level);
        this.title = title;
        this.url = url;
    }

    /**
     * Construct a component descriptor based on the internal representation
     * of same.
     *
     * @param info The description of the component to use
     */
    public ComponentInfo(org.web3d.vrml.lang.ComponentInfo info) {
        super("ComponentInfo");

        this.name = info.getName();
        this.level = new Integer(info.getLevel());
        this.title = info.getTitle();
        this.url = info.getProviderURL();
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    public boolean has(String name, Scriptable start) {
        return propertyNames.contains(name);
    }

    /**
     * Get the value of the named function. If no function object is
     * registex for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    public Object get(String name, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(propertyNames.contains(name)) {
            char prop = name.charAt(0);

            switch(prop) {
                case 'n':
                    ret_val = name;
                    break;

                case 'l':
                    ret_val = level;
                    break;

                case 't':
                    ret_val = title;
                    break;

                case 'p':
                    ret_val = url;
            }
        }

        return ret_val;
    }
}
