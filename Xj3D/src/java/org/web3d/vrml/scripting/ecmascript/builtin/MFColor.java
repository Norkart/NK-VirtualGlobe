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

// Local imports
import org.web3d.util.HashSet;

/**
 * MFColor field object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class MFColor extends FieldScriptableObject {

    private static final String OBJECT_NOT_COLOR_MSG =
        "The object you attempted to assign was not an SFColor instance";

    /** The properties of this class */
    private ArrayList valueList;

    /** Representation of the length as a class */
    private ReusableInteger sizeInt;

    /**
     * Raw data if that is where we started from. Not initialized if the data
     * is sourced through the jsConstructor. The valueList is considered the
     * truth in this implementation.
     */
    private float[] floatData;

    /** Temporary array for copying data into and out of the long array */
    private float[] workArray;

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
    public MFColor() {
        super("MFColor");

        valueList = new ArrayList();
        sizeInt = new ReusableInteger(0);
    }

    /**
     * Construct a field based on a flattened array of data (sourced from a
     * node).
     */
    public MFColor(float[] values, int numValid) {
        this();

        workArray = new float[3];

        int elements = numValid / 3;

        if(numValid != 0) {
            floatData = new float[numValid];
            System.arraycopy(values, 0, floatData, 0, numValid);

            // back fill the list with empty data.
            valueList.ensureCapacity(elements);
            for(int i = 0; i < elements; i++)
                valueList.add(null);
        }

        sizeInt.setValue(elements);
    }

    /**
     * Construct a field based on an array of SFColor objects.
     *
     * @param args the objects
     */
    public MFColor(Object[] args) {
        this();

        int cnt=0;

        for(int i=0; i < args.length; i++) {
            if (args[i] == jsUndefined)
                continue;

            if(!(args[i] instanceof SFColor))
                    throw new IllegalArgumentException("Non SFColor given");

            cnt++;
            valueList.add(args[i]);
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
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj,
                                           boolean inNewExpr) {

        MFColor result = new MFColor(args);

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

        if((index >= 0) && (index < valueList.size())) {
            ret_val = valueList.get(index);

            // could be null because the source data was an array. If so,
            // create the object and then put that into the arraylist.
            if(ret_val == null) {
                int idx = index * 3;
                workArray[0] = floatData[idx++];
                workArray[1] = floatData[idx++];
                workArray[2] = floatData[idx];

                SFColor n = new SFColor(workArray);
                n.setParentScope(this);
                valueList.set(index, n);

                ret_val = n;
            }
        } else if(index >= 0) {
            // Not in the array but the spec says we must expand to meet this
            // new size and return a valid object
            for(int i = valueList.size(); i <= index; i++) {
                SFColor n = new SFColor();
                n.setParentScope(this);
                valueList.add(n);
            }
            sizeInt.setValue(valueList.size());

            ret_val = (SFColor)valueList.get(index);
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
            ret_val = sizeInt;
        } else {
            ret_val = super.get(name, start);

            // it could be that this instance is dynamically created and so
            // the function name is not automatically registex by the
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
     * Sets a property based on the index. According to C.6.13.1 if the
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

        if(!(value instanceof SFColor)) {
            Context.reportError(OBJECT_NOT_COLOR_MSG);
            return;
        }

        Scriptable node = (SFColor)value;
        if(node.getParentScope() == null)
            node.setParentScope(this);

        if(index >= valueList.size()) {
            int toAdd = index - valueList.size();

            valueList.ensureCapacity(index+1);

            // Add default values
            for(int i=0; i < toAdd; i++) {
                valueList.add(new SFColor());
            }

            valueList.add(value);
            sizeInt.setValue(valueList.size());
        } else if(index >= 0) {
            valueList.set(index, value);
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
    // Table C.20
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
        int size = valueList.size();

        for(int i = 0; i < size; i++) {
            SFColor node = (SFColor)valueList.get(i);
            buf.append(node.toString());
            buf.append(' ');
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
        if(!(val instanceof MFColor))
            return false;

        MFColor o = (MFColor)val;

        int size = valueList.size();

        if(size != o.valueList.size())
            return false;

        return valueList.equals(o.valueList);
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
     * @param values The list of values to update here
     * @param numValid The number of valid values to use from the array
     */
    public void updateRawData(float[] values, int numValid) {
        if(dataChanged)
            return;

        int elements = numValid / 3;
        valueList.clear();

        if(numValid != 0) {
            floatData = new float[numValid];
            System.arraycopy(values, 0, floatData, 0, numValid);

            // back fill the list with empty data.
            valueList.ensureCapacity(elements);
            for(int i = 0; i < elements; i++)
                valueList.add(null);
        }

        sizeInt.setValue(elements);
    }

    /**
     * Get the data in this node in its raw form as an array of primitives.
     *
     * @return A flat array of values [r1, g1, b1, r2, g2, b2, ....]
     */
    public float[] getRawData() {
        int size = valueList.size();

        if((floatData == null) || (floatData.length != (size * 3)))
            floatData = new float[size * 3];

        if(workArray == null)
            workArray = new float[3];

        int count = 0;

        for(int i = 0; i < size; i++) {
            SFColor node = (SFColor)valueList.get(i);

            if (node != null) {
                node.getRawData(workArray);
                floatData[count++] = workArray[0];
                floatData[count++] = workArray[1];
                floatData[count++] = workArray[2];
            } else {
                count += 3;
            }
        }

        return floatData;
    }

    /**
     * Fetch the raw data held by this instance and copy it into the
     * provided array.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(float[] value) {
        int size = valueList.size();

        if(workArray == null)
            workArray = new float[3];

        int count = 0;

        for(int i = 0; i < size; i++) {
            SFColor node = (SFColor)valueList.get(i);
            if (node != null) {
                node.getRawData(workArray);
                floatData[count++] = workArray[0];
                floatData[count++] = workArray[1];
                floatData[count++] = workArray[2];
            } else {
                count += 3;
            }
        }
    }
}
