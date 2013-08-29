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
import java.lang.reflect.Method;
import java.util.HashMap;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

// Application specific imports
// none

/**
 * Base representation of all miscellaneous objects in ecmascript scripting.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class AbstractScriptableObject implements Scriptable {

    /**
     * Error message when given a string that you parse for a number and it
     * is not a valid format. The message assumes that you will tack on the
     * passed in string as part of the message.
     */
    protected static final String BAD_FORMAT_MSG =
        "Attempting to set a property that we have determined is a number " +
        "but you passed to us as a String. The string you passed us was: ";

    /**
     * Error message when a property is being set and it is not a compatible
     * javascript type with what is required.
     */
    protected static final String INVALID_TYPE_MSG =
        "The type passed to the underlying object is invalid ";

    /** Error message for when the field is marked readOnly */
    protected static final String READONLY_MSG =
        "You are not allowed to directly set this property because the script " +
        "is marked as read only";

    /** The prefix of the name for any function call we dynamically look up */
    protected static final String JS_FUNCTION_PREFIX = "jsFunction_";

    private static final Object[] EMPTY_IDS = new Object[0];

    /** The parent scope of this object */
    private Scriptable parentScope;

    /** The prototype definition of this node */
    private Scriptable prototype;

    /** The name of this field type */
    private final String className;

    /** Flag to be set if the data has changed */
    protected boolean dataChanged;

    /** Flag to say this field is read only */
    protected boolean readOnly;

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    protected AbstractScriptableObject (String name) {
        className = name;
        readOnly = true;
    }

    //----------------------------------------------------------
    // Methods required by the Scriptable interface.
    //----------------------------------------------------------

    /**
     * The class name as defined to the Javascript function.
     *
     * @return the name of this class
     */
    public String getClassName() {
        return className;
    }

    /**
     * Return a default value for this. Return null until we have a better
     * idea.
     */
    public Object getDefaultValue(Class hint) {
        return null;
    }

    /**
     * Check for the indexed property presence. Always returns NOT_FOUND as
     * ECMAScript doesn't support indexed objects.
     */
    public boolean has(int index, Scriptable start) {
        return false;
    }

    /**
     * Check for the named property presence. Normally does nothing, but if
     * function objects are registered it will confirm or deny their presence.
     *
     * @return true if it is a defined function
     */
    public boolean has(String name, Scriptable start) {
        return false;
    }

    /**
     * Get the variable at the given index. Since we don't support integer
     * index values for fields of the script, this always returns NOT_FOUND.
     */
    public Object get(int index, Scriptable start) {
        return NOT_FOUND;
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    public Object get(String name, Scriptable start) {
        return NOT_FOUND;
    }

    /**
     * Sets a property based on the index. Since we don't support indexes,
     * this is ignored.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(int index, Scriptable start, Object value) {
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFVec3f(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(String name, Scriptable start, Object value) {
        // ignore anything else
    }

    /**
     * Delete a property. There are no dynamic fields, so this does nothing.
     *
     * @param index The index of the property to delete
     */
    public void delete(int index) {
        // Do nothing
    }

    /**
     * Delete a property. There are no dynamic fields, so this does nothing.
     *
     * @param name The name of the property to delete
     */
    public void delete(String name) {
        // Do nothing
    }

    // These are the standard methods below here. No need to override them.

    /**
     * Get properties.
     *
     * We return an empty array since we define all properties to be DONTENUM.
     */
    public Object[] getIds() {
        return EMPTY_IDS;
    }

    /**
     * instanceof operator.
     *
     * We mimick the normal JavaScript instanceof semantics, returning
     * true if <code>this</code> appears in <code>value</code>'s prototype
     * chain.
     */
    public boolean hasInstance(Scriptable value) {
        Scriptable proto = value.getPrototype();
        while (proto != null) {
            if (proto.equals(this))
                return true;
            proto = proto.getPrototype();
        }

        return false;
    }

    /**
     * Fetch the parent scope of this context.
     */
    public Scriptable getParentScope() {
        return parentScope;
    }

    /**
     * Set the parent scope of this object. Should be the global shared
     * instance.
     */
    public void setParentScope(Scriptable parent) {
        parentScope = parent;
    }

    /**
     * Get the prototype used by this context.
     */
    public Scriptable getPrototype() {
        return prototype;
    }

    /**
     * Ignored. Set the prototype for this context. The context does not have
     * a prototype that we want the user to play with.
     *
     * @param proto The prototype definition to use
     */
    public void setPrototype(Scriptable proto) {
        prototype = proto;
    }

    //----------------------------------------------------------
    // Public local methods
    //----------------------------------------------------------

    /**
     * Set this field to be read only. Once set, cannot be turned off.
     */
    public void setReadOnly() {
        readOnly = true;
    }
}
