/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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

// External imports
import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.NativeArray;

// Local imports
import org.web3d.util.HashSet;

/**
 * MFString field object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.20 $
 */
public class MFString extends FieldScriptableObject {

    private static final String OBJECT_NOT_STRING_MSG =
        "The object you attempted to assign was not a String instance";

    /** The properties of this class */
    private ArrayList stringList;

    /** Representation of the length as a class */
    private ReusableInteger sizeInt;

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** Set of the valid function names for this object */
    private static HashSet functionNames;

    /** The Javascript Undefined value */
    private static Object jsUndefined;

    static {
        propertyNames = new HashSet();
        propertyNames.add("length");

        functionNames = new HashSet();
        functionNames.add("toString");
        functionNames.add("equals");

        jsUndefined = Context.getUndefinedValue();
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public MFString() {
        super("MFString");

        sizeInt = new ReusableInteger(0);
        stringList = new ArrayList();
    }

    /**
     * Construct a field based on the given array of data (sourced from a
     * node).
     */
    public MFString(String[] strs, int numValid) {
        this();

        if(numValid > 0) {
            stringList.ensureCapacity(numValid);

            for(int i = 0; i < numValid; i++)
                stringList.add(strs[i]);

            sizeInt.setValue(numValid);
        } else {
            sizeInt.setValue(0);
        }
    }

    /**
     * Construct a field based on an array of SFString objects.
     *
     * @param args the objects
     */
    public MFString(Object[] args) {
        this();

        int cnt=0;
        Object val;

        for(int i=0; i < args.length; i++) {
            if(args[i] == jsUndefined)
                continue;

            if(!(args[i] instanceof String))
                val = args[i].toString();
            else
                val = args[i];

            if(!(val instanceof String))
                throw new IllegalArgumentException("Non String given");

            cnt++;
            stringList.add(val);
        }

        sizeInt.setValue(cnt);
    }

    //----------------------------------------------------------
    // Methods used by ScriptableObject reflection
    //----------------------------------------------------------

    /**
     * Constructor for a new Rhino object
     *
     * @param nodes The list of nodes to use
     */
    public static Scriptable jsConstructor(Context cx,
                                           Object[] args,
                                           Function ctorObj,
                                           boolean inNewExpr) {

        MFString result = new MFString(args);

        return result;
    }

    //----------------------------------------------------------
    // Methods defined by Scriptable
    //----------------------------------------------------------

    /**
     * Check for the indexed property presence.
     */
    public boolean has(int index, Scriptable start) {
        return (index >= 0);
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    public boolean has(String name, Scriptable start) {
        boolean ret_val = false;

        if(propertyNames.contains(name))
            ret_val = true;
        else
            ret_val = super.has(name, start);

        return ret_val;
    }

    /**
     * Get the value at the given index.
     */
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if((index >= 0) && (index < sizeInt.intValue())) {
            ret_val = stringList.get(index);
        } else if(index >= 0) {
            // Not in the array but the spec says we must expand to meet this
            // new size and return a valid object
            for(int i = stringList.size(); i <= index; i++) {
                String n = new String();
                stringList.add(n);
            }
            sizeInt.setValue(stringList.size());

            ret_val = (String)stringList.get(index);
        }
        return ret_val;
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
        Object ret_val = null;

        if(propertyNames.contains(name)) {
            return sizeInt;
        } else {
            ret_val = super.get(name, start);

            // it could be that this instance is dynamically created and so
            // the function name is not automatically registered by the
            // runtime. Let's check to see if it is a standard method for
            // this object and then create and return a corresponding Function
            // instance.
            if((ret_val == null) && functionNames.contains(name))
                ret_val = locateFunction(name);
        }

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    /**
     * Sets a property based on the index. According to C.6.15.1 if the
     * index is greater than the current number of nodes, expand the size
     * by one and add the new value to the end.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(int index, Scriptable start, Object value) {
        if(readOnly && !scriptField) {
            Context.reportError(READONLY_MSG);
            return;
        }

        if(!(value instanceof String)) {
            if (value instanceof Number) {
                value = value.toString();
            } else {
                Context.reportError(OBJECT_NOT_STRING_MSG);
                return;
            }
        }

        if(index >= sizeInt.intValue()) {
            int toAdd = index - stringList.size();

            stringList.ensureCapacity(index+1);

            // Add default values
            for(int i=0; i < toAdd; i++) {
                stringList.add(new String());
            }

            stringList.add(value);
            sizeInt.setValue(stringList.size());
        } else if(index >= 0) {
            stringList.set(index, value);
        }

        dataChanged = true;
    }

    /**
     * Sets the named property with a new value. We don't allow the users to
     * dynamically change the length property of this node. That would cause
     * all sorts of problems. Therefore it is read-only as far as this
     * implementation is concerned.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.24
    //

    /**
     * Creates a string version of this node. Just calls the standard
     * toString() method of the object.
     *
     * @return A VRML string representation of the field
     */
    public String jsFunction_toString() {
        return toString();
    }

    /**
     * Comparison of this object to another of the same type. Just calls
     * the standard equals() method of the object.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    public boolean jsFunction_equals(Object val) {
        return equals(val);
    }

    //----------------------------------------------------------
    // Methods defined by Object.
    //----------------------------------------------------------

    /**
     * Format the internal values of this field as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this field
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        int size = stringList.size();

        for(int i = 0; i < size; i++) {
            String str = (String)stringList.get(i);
            buf.append('\"');
            buf.append(str);
            buf.append("\"\n");
        }

        return buf.toString();
    }

    /**
     * Compares two objects for equality base on the components being
     * the same.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    public boolean equals(Object val) {
        if(!(val instanceof MFString))
            return false;

        MFString o = (MFString)val;

        int size = stringList.size();

        if(size != o.stringList.size())
            return false;

        return stringList.equals(o.stringList);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Update the node's raw data from the underlying model. If this wrapper
     * has a local changed copy of the data that has not yet been committed to
     * the underlying model, this request is ignored and the current data
     * stays.
     *
     * @param value The list of values to update here
     * @param numValid The number of valid values to use from the array
     */
    public void updateRawData(String[] value, int numValid) {
        if(dataChanged)
            return;

        stringList.clear();

        if(numValid != 0) {
            // back fill the list with empty data.
            stringList.ensureCapacity(numValid);
            for(int i = 0; i < numValid; i++)
                stringList.add(value[i]);
        }

        sizeInt.setValue(numValid);
    }

    /**
     * Get the array of underyling String values.
     *
     * @retrurn An array of the values
     */
    public String[] getRawData() {
        int size = sizeInt.intValue();
        String[] ret_val = new String[size];

        if(size != 0)
            stringList.toArray(ret_val);

        return ret_val;
    }
}
