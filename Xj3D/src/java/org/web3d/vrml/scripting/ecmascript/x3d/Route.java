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
import org.web3d.vrml.scripting.ecmascript.builtin.SFNode;
import org.web3d.vrml.scripting.ecmascript.builtin.AbstractScriptableObject;

/**
 * Route object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class Route extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** The source node of the route */
    final SFNode srcNode;

    /** The source field name of the route */
    final String srcField;

    /** The destination node of the route */
    final SFNode destNode;

    /** The destination field name of the route */
    final String destField;

    static {
        propertyNames = new HashSet();
        propertyNames.add("sourceNode");
        propertyNames.add("sourceField");
        propertyNames.add("destinationNode");
        propertyNames.add("destinationField");
    }

    /**
     * Construct a profile descriptor for the given information.
     */
    public Route(SFNode srcNode,
                 String srcField,
                 SFNode destinationNode,
                 String destinationField) {
        super("Route");

        this.srcNode = srcNode;
        this.srcField = srcField;
        this.destNode = destinationNode;
        this.destField = destinationField;
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
                case 's':
                    if(name.charAt(name.length() - 1) == 'd')
                        ret_val = srcField;
                    else
                        ret_val = srcNode;
                    break;

                case 'd':
                    if(name.charAt(name.length() - 1) == 'd')
                        ret_val = destField;
                    else
                        ret_val = destNode;
                    break;
            }
        }

        return ret_val;
    }
}
