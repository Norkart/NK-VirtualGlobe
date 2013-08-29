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
 * Route miscellaneous object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class RouteArray extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** List of routes in the array */
    private Route[] routes;

    /** Length of the array as a property */
    private Integer length;

    static {
        propertyNames = new HashSet();
        propertyNames.add("length");
    }

    /**
     * Allow creating an empty list of objects. Local only access as it
     * is only used by the execution context handling.
     */
    RouteArray() {
        super("RouteArray");
        length = new Integer(0);
    }

    /**
     * Construct a profile descriptor for the given information.
     */
    public RouteArray(Route[] routes) {
        super("RouteArray");
        this.routes = routes;
        length = new Integer(routes.length);
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
            ret_val = length;
        }

        return ret_val;
    }

    /**
     * Get the value of the named function. If no function object is
     * registex for this name, the method will return null.
     *
     * @param name The index into the array
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(index >= 0 && index < length.intValue())
            ret_val = routes[index];

        return ret_val;
    }
}
