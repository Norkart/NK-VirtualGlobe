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
import org.web3d.util.ColorUtils;
import org.web3d.util.HashSet;

/**
 * SFColor field object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class SFColorRGBA extends FieldScriptableObject {

    /** The color values held by this class */
    private double red;
    private double green;
    private double blue;
    private double alpha;

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** Set of the valid function names for this object */
    private static HashSet functionNames;

    static {
        propertyNames = new HashSet();
        propertyNames.add("r");
        propertyNames.add("g");
        propertyNames.add("b");
        propertyNames.add("a");

        functionNames = new HashSet();
        functionNames.add("setHSV");
        functionNames.add("getHSV");
        functionNames.add("toString");
        functionNames.add("equals");
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public SFColorRGBA() {
        super("SFColorRGBA");
    }

    /**
     * Construct a field based on the given array of data (sourced from a
     * node).
     */
    public SFColorRGBA(float[] c) {
        this();

        if(c == null || c.length == 0)
            return;

        red = c[0];
        green = c[1];
        blue = c[2];
        alpha = c[3];
    }

    /**
     * Constructor for all three values that rhino calls.
     *
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     */
    public void jsConstructor(double r, double g, double b, double a) {
        red = Double.isNaN(r) ? 0 : r;
        green = Double.isNaN(g) ? 0 : g;
        blue = Double.isNaN(b) ? 0 : b;
        alpha = Double.isNaN(a) ? 0 : a;
    }

    /**
     * Check for the indexed property presence.
     */
    public boolean has(int index, Scriptable start) {
        return (index >= 0 && index < 3);
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
     * Get the variable at the given index. Since we don't support integer
     * index values for fields of the script, this always returns NOT_FOUND.
     */
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;

        switch(index) {
            case 0:
                ret_val = new Double(red);
                break;

            case 1:
                ret_val = new Double(green);
                break;

            case 2:
                ret_val = new Double(blue);
                break;

            case 3:
                ret_val = new Double(alpha);
                break;
        }

        return ret_val;
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
        Object ret_val = null;

        if(propertyNames.contains(name)) {
            char prop = name.charAt(0);
            double dbl_val = 0;

            switch(prop) {
                case 'r':
                    dbl_val = red;
                    break;

                case 'g':
                    dbl_val = green;
                    break;

                case 'b':
                    dbl_val = blue;
                    break;

                case 'a':
                    dbl_val = alpha;
                    break;
            }

            ret_val = new Double(dbl_val);
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
     * Sets a property based on the index.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(int index, Scriptable start, Object value) {

        Number num = (Number)value;

        switch(index) {
            case 0:
                red = num.doubleValue();
                break;

            case 1:
                green = num.doubleValue();
                break;

            case 2:
                blue = num.doubleValue();
                break;

            case 3:
                alpha = num.doubleValue();
                break;
        }

        dataChanged = true;
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFColor(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(String name, Scriptable start, Object value) {

        if(propertyNames.contains(name)) {
            if(readOnly && !scriptField) {
                Context.reportError(READONLY_MSG);
                return;
            }

            char prop = name.charAt(0);
            double dbl_val = 0;

            if(value instanceof Number) {
                Number num = (Number)value;
                dbl_val = num.doubleValue();
            } else if(value instanceof String) {
                try {
                    dbl_val = Double.parseDouble((String)value);
                } catch(NumberFormatException nfe) {
                    Context.reportRuntimeError(BAD_FORMAT_MSG + value);
                    return;
                }
            } else {
                Context.reportRuntimeError(INVALID_TYPE_MSG);
                return;
            }

            switch(prop) {
                case 'r':
                    red = dbl_val;
                    break;

                case 'g':
                    green = dbl_val;
                    break;

                case 'b':
                    blue = dbl_val;
                    break;

                case 'a':
                    alpha = dbl_val;
                    break;
            }

            dataChanged = true;
        } else if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.3
    //

    public void jsFunction_setHSV(double h, double s, double v) {
        float[] rgb = new float[3];
        ColorUtils.convertHSVtoRGB((float)h, (float)s, (float)v, rgb);

        red = rgb[0];
        green = rgb[1];
        blue = rgb[2];

        dataChanged = true;
    }

    public Object jsFunction_getHSV() {

        float[] hsv = new float[3];
        ColorUtils.convertRGBtoHSV((float)red, (float)green, (float)blue, hsv);

        return hsv;
/*
        try {
            Context cx = Context.getCurrentContext();
            result = cx.newObject(scope, "Array", lines);
        } catch (PropertyException e) {
            throw Context.reportRuntimeError(e.getMessage());
        } catch (NotAFunctionException e) {
            throw Context.reportRuntimeError(e.getMessage());
        }
*/
    }

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

    /**
     * Format the internal values of this matrix as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this matrix
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(red);
        buf.append(' ');
        buf.append(green);
        buf.append(' ');
        buf.append(blue);
        buf.append(' ');
        buf.append(alpha);

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
        if(!(val instanceof SFColorRGBA))
            return false;

        SFColorRGBA o = (SFColorRGBA)val;

        return (o.red == red) &&
               (o.green == green) &&
               (o.blue == blue) &&
               (o.alpha == alpha);
    }

    /**
     * Fetch the raw data held by this instance and copy it into the
     * provided array.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(float[] value) {
        value[0] = (float)red;
        value[1] = (float)green;
        value[2] = (float)blue;
        value[3] = (float)alpha;
    }

    /**
     * Convenience method to reset the values used by this instance. Typically
     * used by the X3DMatrix class, but others may use this too. It does not
     * set the data changed flag when called.
     *
     * @param values The new values to use
     */
    public void setRawData(float[] values) {
        red = values[0];
        green = values[1];
        blue = values[2];
        alpha = values[3];
    }

    /**
     * Convenience method to reset the values used by this instance. Typically
     * used by the VrmlMatrix class, but others may use this too. It does not
     * set the data changed flag when called.
     *
     * @param values The new values to use
     */
    public void setRawData(double[] values) {
        red = values[0];
        green = values[1];
        blue = values[2];
        alpha = values[3];
    }
}
