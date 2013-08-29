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

import javax.vecmath.Vector3f;
import javax.vecmath.Quat4d;
import javax.vecmath.AxisAngle4d;

// Application specific imports
import org.web3d.util.HashSet;

/**
 * SFRotation field object.
 * <P>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.22 $
 */
public class SFRotation extends FieldScriptableObject {

    /** The properties of this class */
    private double x;
    private double y;
    private double z;
    private double angle;

    /** Set of the valid property names for this object */
    private static HashSet propertyNames;

    /** Set of the valid function names for this object */
    private static HashSet functionNames;

    /** The Javascript Undefined value */
    private static Object jsUndefined;

    static {
        propertyNames = new HashSet();
        propertyNames.add("x");
        propertyNames.add("y");
        propertyNames.add("z");
        propertyNames.add("angle");

        functionNames = new HashSet();
        functionNames.add("getAxis");
        functionNames.add("setAxis");
        functionNames.add("inverse");
        functionNames.add("multiply");
        functionNames.add("multVec");
        functionNames.add("slerp");
        functionNames.add("toString");
        functionNames.add("equals");

        jsUndefined = Context.getUndefinedValue();
    }


    /**
     * Default public constructor requix by Rhino for when created by
     * an Ecmascript call.
     */
    public SFRotation() {
        super("SFRotation");
    }

    /**
     * Construct a field based on the given array of data (sourced from a
     * node).
     */
    public SFRotation(float[] c) {
        this();

        if(c == null || c.length == 0)
            return;

        x = c[0];
        y = c[1];
        z = c[2];
        angle = c[3];
    }

    /**
     * Constructor for all three values that rhino calls.
     * We have to deal with 3 forms:
     *    numeric x, numeric y, numeric z, numeric a
     *    SFVec3f axis, numeric angle
     *    SFVec3f fromVector, SFVec3f toVector
     *
     */
    public void jsConstructor(Object nodes, Object nodes2, Object nodes3, Object nodes4) {
        if (nodes == jsUndefined) {
            x = 0;
            y = 1;
            z = 0;
            angle = 0;
        } else if (nodes != jsUndefined && nodes2 != jsUndefined &&
            nodes3 != jsUndefined & nodes4 != jsUndefined) {

            // x,y,z,a constructor
            x = ((Number)nodes).doubleValue();
            y = ((Number)nodes2).doubleValue();
            z = ((Number)nodes3).doubleValue();
            angle = ((Number)nodes4).doubleValue();
        } else {
            // one of the two param constructors
            double[] vals = new double[3];
            ((SFVec3f)nodes).getRawData(vals);

            if (nodes2 instanceof SFVec3f) {
                // fromVector, toVector construct

                double[] vec2 = new double[3];
                ((SFVec3f)nodes2).getRawData(vec2);

                Vector3f v1 = new Vector3f((float)vals[0],(float)vals[1],
                    (float)vals[2]);
                Vector3f v2 = new Vector3f((float)vec2[0], (float)vec2[1],
                    (float)vec2[2]);

                angle = v1.angle(v2);

                v1.cross(v1, v2);
                v1.normalize();

                x = Double.isNaN(v1.x) ? 0 : v1.x;
                y = Double.isNaN(v1.y) ? 1 : v1.y;
                z = Double.isNaN(v1.z) ? 0 : v1.z;
            } else {
                // axis, numeric angle
                x = vals[0];
                y = vals[1];
                z = vals[2];

                angle = ((Number)nodes2).doubleValue();
            }
        }
    }

    /**
     * Check for the indexed property presence. Always returns NOT_FOUND as
     * ECMAScript doesn't support indexed objects.
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
                ret_val = new Double(x);
                break;

            case 1:
                ret_val = new Double(y);
                break;

            case 2:
                ret_val = new Double(z);
                break;

            case 3:
                ret_val = new Double(angle);
                break;
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
            char prop = name.charAt(0);
            double dbl_val = 0;

            switch(prop) {
                case 'x':
                    dbl_val = x;
                    break;

                case 'y':
                    dbl_val = y;
                    break;

                case 'z':
                    dbl_val = z;
                    break;

                case 'a':
                    dbl_val = angle;
                    break;
            }

            ret_val = new Double(dbl_val);
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
                x = num.doubleValue();
                break;

            case 1:
                y = num.doubleValue();
                break;

            case 2:
                z = num.doubleValue();
                break;

            case 3:
                angle = num.doubleValue();
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
                case 'x':
                    x = dbl_val;
                    break;

                case 'y':
                    y = dbl_val;
                    break;

                case 'z':
                    z = dbl_val;
                    break;

                case 'a':
                    angle = dbl_val;
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
    // Table C.8
    //

    /**
     * Get the axis of this quaternion.
     *
     * @return The object representing the axis
     */
    public SFVec3f jsFunction_getAxis() {

        SFVec3f ret_val = new SFVec3f();
        ret_val.setRawData(x, y, z);

        return ret_val;
    }

    /**
     * Change the axis to the new value
     *
     * @param axis The new axis to use
     */
    public void jsFunction_setAxis(Scriptable sc) {
        if (!(sc instanceof SFVec3f)) {
            Context.reportRuntimeError(INVALID_TYPE_MSG);
        }
        SFVec3f axis = (SFVec3f) sc;

        float[] val = new float[3];

        axis.getRawData(val);

        x = val[0];
        y = val[1];
        z = val[2];
    }

    /**
     * Calculate and return the inverse of this matrix.
     *
     * @return A rotation representing the inverse of this one
     */
    public SFRotation jsFunction_inverse() {
        SFRotation ret_val = new SFRotation();

        double mag = 1 / Math.sqrt(x * x + y * y + z * z + angle * angle);

        ret_val.x = -x / mag;
        ret_val.y = -y / mag;
        ret_val.z = -z / mag;
        ret_val.angle =  angle / mag;

        return ret_val;
    }

    /**
     * Multiply this rotation by the given rotation.
     *
     * @param rot The rotation to multiply this by
     * @return The result of the multiplication
     */
    public SFRotation jsFunction_multiply(Scriptable sc) {
        if (!(sc instanceof SFRotation)) {
                Context.reportRuntimeError(INVALID_TYPE_MSG);
        }
        SFRotation rot = (SFRotation) sc;

        SFRotation ret_val = new SFRotation();

        ret_val.x = angle * rot.x + x * rot.angle + y * rot.z - z * rot.y;
        ret_val.y = angle * rot.y + y * rot.angle + z * rot.x - x * rot.z;
        ret_val.z = angle * rot.z + z * rot.angle + x * rot.y - y * rot.x;
        ret_val.angle = angle * rot.angle - x * rot.x - y * rot.y - z * rot.z;

        return ret_val;
    }

    /**
     * Turn this rotation into a rotation matrix and then mutliple the vector
     * by that matrix. Mr . V = V' to give the return vector.
     *
     * @param vec The vector to mutliply by this rotation
     * @return A vector containing the result
     */
    public SFVec3f jsFunction_multVec(Scriptable sc) {
        if (!(sc instanceof SFVec3f)) {
            Context.reportRuntimeError(INVALID_TYPE_MSG);
        }
        SFVec3f vec = (SFVec3f) sc;

        // Create the rotation matrix from this quaternion.
        // Only create a 3x3 temporary matrix as that is all we need to
        // work with.
        double[] m = new double[9];
        double[] v = new double[3];
        vec.getRawData(v);

        // Calculate the bits for the rotation Quat -> Matrix conversion:
        double xx = x * x;
        double xy = x * y;
        double xz = x * z;
        double xw = x * angle;

        double yy = y * y;
        double yz = y * z;
        double yw = y * angle;

        double zz = z * z;
        double zw = z * angle;

        m[0] =  1 - 2 * (yy + zz);
        m[1] =  2 * (xy + zw);
        m[2] =  2 * (xz - yw);
        m[3] =  2 * (xy - zw);
        m[4] =  1 - 2 * (xx + zz);
        m[5] =  2 * (yz + xw);
        m[6] =  2 * (xz + yw);
        m[7] =  2 * (yz - xw);
        m[8] = 1 - 2 * (xx + yy);

        double r_x = m[0] * v[0] + m[1] * v[1] + m[2] * v[2];
        double r_y = m[3] * v[0] + m[4] * v[1] + m[5] * v[2];
        double r_z = m[6] * v[0] + m[7] * v[1] + m[8] * v[2];

        SFVec3f ret_val = new SFVec3f();
        ret_val.setRawData(r_x, r_y, r_z);

        return ret_val;
    }

    public SFRotation jsFunction_slerp(Scriptable sc, double t) {
        if (!(sc instanceof SFRotation)) {
                Context.reportRuntimeError(INVALID_TYPE_MSG);
        }
        SFRotation rot = (SFRotation) sc;
        AxisAngle4d axis;

        axis = new AxisAngle4d(x,y,z,angle);

        Quat4d q1 = new Quat4d();
        q1.set(axis);

        double[] val = new double[4];
        rot.getRawData(val);

        axis = new AxisAngle4d(val[0],val[1],val[2],val[3]);

        Quat4d q2 = new Quat4d();
        q2.set(axis);

        q1.interpolate(q2, t);

        axis.set(q1);

        SFRotation res = new SFRotation(new float[] { (float)axis.x, (float)axis.y, (float)axis.z, (float)axis.angle});

        return res;
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

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Format the internal values of this matrix as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this matrix
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(x);
        buf.append(' ');
        buf.append(y);
        buf.append(' ');
        buf.append(z);
        buf.append(' ');
        buf.append(angle);

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
        if(!(val instanceof SFRotation))
            return false;

        SFRotation o = (SFRotation)val;

        return (o.x == x) && (o.y == y) && (o.z == z) && (o.angle == angle);
    }

    /**
     * Fetch the raw data held by this instance and copy it into the
     * provided array as float values.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(float[] value) {
        value[0] = (float)x;
        value[1] = (float)y;
        value[2] = (float)z;
        value[3] = (float)angle;
    }

    /**
     * Fetch the raw data held by this instance and copy it into the
     * provided array.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(double[] value) {
        value[0] = x;
        value[1] = y;
        value[2] = z;
        value[3] = angle;
    }

    /**
     * Convenience method to reset the values used by this instance. Typically
     * used by the VrmlMatrix class, but others may use this too. It does not
     * set the data changed flag when called.
     *
     * @param values The new values to use
     */
    public void setRawData(float[] values) {
        x = values[0];
        y = values[1];
        z = values[2];
        angle = values[3];
    }

    /**
     * Convenience method to reset the values used by this instance. Typically
     * used by the VrmlMatrix class, but others may use this too. It does not
     * set the data changed flag when called.
     *
     * @param values The new values to use
     */
    public void setRawData(double[] values) {
        x = values[0];
        y = values[1];
        z = values[2];
        angle = values[3];
    }
}
