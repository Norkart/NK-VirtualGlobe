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

package org.web3d.vrml.scripting.ecmascript.builtin;

// Standard imports
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.web3d.util.HashSet;

/**
 * ProfileInfo miscellaneous object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class X3DFieldDefinition extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** The name of the field */
    private final String name;

    /** The data type of the field */
    private final Integer dataType;

    /** The access type of the field */
    private final Integer accessType;

    static {
        propertyNames = new HashSet();
        propertyNames.add("name");
        propertyNames.add("accessType");
        propertyNames.add("dataType");
    }

    /**
     * Construct a profile descriptor for the given information.
     */
    public X3DFieldDefinition(String name, int access, int type) {
        super("X3DFieldDefinition");

        this.name = name;
        accessType = new Integer(access);
        dataType = new Integer(type);
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
                    ret_val = this.name;
                    break;

                case 'a':
                    ret_val = accessType;
                    break;

                case 'd':
                    ret_val = dataType;
                    break;
            }
        }

        return ret_val;
    }
}
