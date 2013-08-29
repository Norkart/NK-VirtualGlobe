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
 * ECMAScript Matrix3 builtin object.
 * <P>
 *
 * The implementation of all the functionality in this class is according to
 * the Matrix and Quaternion FAQ, which is currently located at:
 * <a href="http://www.j3d.org/matrix_faq/">http://www.j3d.org/matrix_faq/</a>
 * <p>
 * The spec is sort of a bit wishy-washy about how the flat references of
 * array indices. Internally the matrix is stored with the translations values
 * down the right "column" where the X3D spec has them across the bottom. Not
 * entirely convinced that this is correct. Just makes it easy to check this
 * code against the FAQ code, which uses them stored in the same way.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class Matrix3 extends FieldScriptableObject {

    // Implementation Note:
    // Originally I was going to use the java3d Matrix4d to back this class.
    // However that implied that the user would need to download and install
    // Java3D even if they didn't want it. Matrix4d only comes as part of J3D
    // as the javax.vecmath package is not separately downloadable :(
    // JC.

    /** Set of the valid function names for this object */
    private static HashSet functionNames;

    /** Standard identity matrix */
    private static final double[] IDENTITY = {
        1, 0, 0,
        0, 1, 0,
        0, 0, 1,
    };

    /** The default values according to the VRML spec */
    private static final double[] DEFAULT_TRANSLATION = {0, 0, 0};
    private static final double[] DEFAULT_ROTATION = {0, 0, 1, 0};
    private static final double[] DEFAULT_SCALE = {1, 1, 1};
    private static final double[] DEFAULT_SCALE_ORIENT = {0, 0, 1, 0};
    private static final double[] DEFAULT_CENTER = {0, 0, 0};

    /** The values of the matrix */
    private double[] matrix;

    /** Temporary work variables */
    private double[] workMatrix;

    private double[] workTranslation;
    private double[] workRotation;
    private double[] workScale;
    private double[] workOrientation;
    private double[] workCenter;

    static {
        functionNames = new HashSet();
        functionNames.add("setTransform");
        functionNames.add("getTransform");
        functionNames.add("inverse");
        functionNames.add("transpose");
        functionNames.add("multLeft");
        functionNames.add("multRight");
        functionNames.add("multVecMatrix");
        functionNames.add("multMatrixVec");
        functionNames.add("toString");
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public Matrix3() {
        super("Matrix3");
        matrix = new double[9];
        workMatrix = new double[9];
        workTranslation = new double[2];
        workRotation = new double[3];
        workScale = new double[2];
        workOrientation = new double[3];
        workCenter = new double[2];
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public Matrix3(double[] data) {
        this();

        if(data == null)
            return;

        // As we don't know the real size use System.arraycopy rather than
        // a loop. Max size is always 16 entries in the array for a 4x4
        // matrix.
        int size = data.length > 9 ? 9 : data.length;

        System.arraycopy(data, 0, matrix, 0, size);
    }

    // NOTE:
    // No need for a jsConstructor in this class because the constructor above
    // does the job. If you did define one with the same arguments, then rhino
    // would issue an error message about a bad constructor!
    public void jsConstructor(double f11, double f12, double f13,
                              double f21, double f22, double f23,
                              double f31, double f32, double f33) {

        matrix[0] =  Double.isNaN(f11) ? 1 : f11;
        matrix[1] =  Double.isNaN(f12) ? 0 : f12;
        matrix[2] =  Double.isNaN(f13) ? 0 : f13;
        matrix[3] =  Double.isNaN(f21) ? 0 : f21;
        matrix[4] =  Double.isNaN(f22) ? 1 : f22;
        matrix[5] =  Double.isNaN(f23) ? 0 : f23;
        matrix[6] =  Double.isNaN(f31) ? 0 : f31;
        matrix[7] =  Double.isNaN(f32) ? 0 : f32;
        matrix[8] =  Double.isNaN(f33) ? 1 : f33;
    }

    //----------------------------------------------------------
    // Methods overridden from the Scriptable interface.
    //----------------------------------------------------------

    /**
     * Check to see if a numeric property is valid. We only accept the first
     * 16 as that is the dimension of the array.
     *
     * @param index the index of the property
     * @param start the object where lookup began
     * @return true if 0 < index < 16
     */
    public boolean has(int index, Scriptable start) {
        return ((index >= 0) && (index < 9));
    }

    //
    // has(String, Scriptable) is implemented by the base class as the only
    // thing you can get using a string is the function names. There is no
    // named properties for Matrix3
    //

    /**
     * Look up the element in the associated matrix and return it if it
     * exists. If it doesn't exist return
     * @param index the index of the integral property
     * @param start the object where the lookup began
     */
    public Object get(int index, Scriptable start) {
        if ((index < 0) && (index > 8))
            return NOT_FOUND;

        return new Double(matrix[index]);
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
        Object ret_val = super.get(name, start);

        // it could be that this instance is dynamically created and so
        // the function name is not automatically registex by the
        // runtime. Let's check to see if it is a standard method for
        // this object and then create and return a corresponding Function
        // instance.
        if((ret_val == null) && functionNames.contains(name))
            ret_val = locateFunction(name);

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    /**
     * Set an indexed property. Only accept the first 16 values.
     *
     * @param index The index of the property to look up
     * @param start Theobject where the lookup began
     * @param value The value of the object to use
     */
    public void put(int index, Scriptable start, Object value) {
        if ((index < 0) && (index > 8))
            return;

        Number num = (Number)value;
        matrix[index] = num.doubleValue();
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
        if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.31
    //

    /**
     * Set the transform to the new values of translation, rotation, scale
     * etc. Takes a variable list of arguments. Anything more than 5 and we
     * ignore the extras. There may by up to 5 items in the array. Zero items
     * means to set the array to an identity matrix. After that, the
     * arguments are:
     * <pre>
     * args[0] SFVec3f translation
     * args[1] SFRotation rotation
     * args[2] SFVec3f scale
     * args[3] SFRotation scaleOrientation
     * args[4] SFVec3f center
     * </pre>
     */
    public void jsFunction_setTransform(Scriptable trans,
                                        Scriptable rot,
                                        Scriptable scale,
                                        Scriptable scaleOrient,
                                        Scriptable center) {

        if(trans != null && !(trans instanceof SFVec2f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(rot != null && !(rot instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(scale != null && !(scale instanceof SFVec2f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(scaleOrient != null && !(scaleOrient instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(center != null && !(center instanceof SFVec2f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);


        // If the first argument is null, that means we treat it like a clear
        // of the matrix, thus reseting to Identity matrix
        if(trans == null) {
            System.arraycopy(IDENTITY, 0, matrix, 0, 9);
            return;
        }

System.out.println("Need to implement Matrix3.set()");

/*
        manualSetup();
        ((SFVec2f)trans).getRawData(workTranslation);

        if(rot != null)
            ((SFVec3f)rot).getRawData(workRotation);

        if(scale != null)
            ((SFVec2f)scale).getRawData(workScale);

        if(scaleOrient != null)
            ((SFVec3f)scaleOrient).getRawData(workOrientation);

        if(center != null)
            ((SFVec2f)center).getRawData(workCenter);

        // Calculate the bits for the rotation Quat -> Matrix conversion:
        double xx = workRotation[0] * workRotation[0];
        double xy = workRotation[0] * workRotation[1];
        double xz = workRotation[0] * workRotation[2];

        double yy = workRotation[1] * workRotation[1];
        double yz = workRotation[1] * workRotation[2];

        double zz = workRotation[2] * workRotation[2];

        // Now do the matrix calcs
        matrix[0] =  workScale[0] + (1 - 2 * (yy + zz));
        matrix[1] =  2 * (xy + zw);
        matrix[2] =  2 * (xz - yw);

        matrix[3] =  2 * (xy - zw);
        matrix[4] =  workScale[1] + (1 - 2 * (xx + zz));
        matrix[5] =  2 * (yz + xw);

        matrix[6] =  2 * (xz + yw);
        matrix[7] =  2 * (yz - xw);
        matrix[8] =  workScale[2] + (1 - 2 * (xx + yy));
*/
    }

    /**
     * Get the value of the transform.
     *
     * @param tx The transform component
     * @param rot The orientation component
     * @param sc The scale component
     */
    public void jsFunction_getTransform(Scriptable tx,
                                        Scriptable rot,
                                        Scriptable sc) {

        if(tx != null && !(tx instanceof SFVec2f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(rot != null && !(rot instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(sc != null && !(sc instanceof SFVec2f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);


        SFVec2f translation = (SFVec2f)tx;
        SFVec3f rotation = (SFVec3f)rot;
        SFVec2f scale = (SFVec2f)sc;

        // Varargs handling. If the first is null, then the rest will be
        // and there's no point going any further.
        if(translation == null)
            return;

         workTranslation[0] = matrix[6];
         workTranslation[1] = matrix[7];
         workTranslation[2] = matrix[8];

         translation.setRawData(workTranslation);

         if(rotation == null)
            return;

System.out.println("Need to implement Matrix3.get()");
/*
        // calculate the trace of the matrix
        double trace = 1 + matrix[0] + matrix[5] + matrix[10];

        if(trace > 0.00000001) {
            double s = Math.sqrt(trace) * 2;
            double inv_s = 1 / s;   // save doing lots of divs

            workRotation[0] = (matrix[6] - matrix[9]) * inv_s;
            workRotation[1] = (matrix[8] - matrix[2]) * inv_s;
            workRotation[2] = (matrix[1] - matrix[4]) * inv_s;
            workRotation[3] = 0.25 * s;
        } else {
            // effectively trace is zero, so go looking for the major diag
            if((matrix[0] > matrix[5]) && (matrix[0] > matrix[10])) {
                // Column 0
                double s =
                    Math.sqrt((1 + matrix[0] - matrix[5] - matrix[10]) * 2);
                double inv_s = 1 / s;

                workRotation[0] = 0.25 * s;
                workRotation[1] = (matrix[1] + matrix[4]) * inv_s;
                workRotation[2] = (matrix[8] + matrix[2]) * inv_s;
                workRotation[3] = (matrix[6] - matrix[9]) * inv_s;

            } else if(matrix[5] > matrix[10]) {
                // Column 1
                double s =
                    Math.sqrt((1 + matrix[5] - matrix[0] - matrix[10]) * 2);
                double inv_s = 1 / s;

                workRotation[1] = (matrix[1] + matrix[4]) * inv_s;
                workRotation[0] = 0.25 * s;
                workRotation[3] = (matrix[6] + matrix[9]) * inv_s;
                workRotation[2] = (matrix[8] - matrix[2]) * inv_s;
            } else {
                // Column 2
                double s =
                    Math.sqrt((1 + matrix[10] - matrix[0] - matrix[5]) * 2);
                double inv_s = 1 / s;

                workRotation[2] = (matrix[8] + matrix[2]) * inv_s;
                workRotation[3] = (matrix[6] + matrix[9]) * inv_s;
                workRotation[0] = 0.25 * s;
                workRotation[1] = (matrix[1] - matrix[4]) * inv_s;
            }
        }

        rotation.setRawData(workRotation);

        if(scale == null)
            return;

        workScale[0] = matrix[0];
        workScale[1] = matrix[5];
        workScale[2] = matrix[10];

        scale.setRawData(workScale);
*/
    }

    /**
     * Create the inverse of the matrix and return that in a new matrix.
     */
    public Matrix3 jsFunction_inverse() {

System.out.println("Need to implement Matrix3.inverse()");

/*
        double[] tmp_matrix = new double[9];
        double mdet = determinant4x4(matrix, tmp_matrix);
        int i;
        int j;
        int sign;
        StringBuffer buf = new StringBuffer();
        for(i = 0; i < 16; ) {
            buf.append("| ");
            buf.append(matrix[i++]);
            buf.append(' ');
            buf.append(matrix[i++]);
            buf.append(' ');
            buf.append(matrix[i++]);
            buf.append(' ');
            buf.append(matrix[i++]);
            buf.append(" |\n");
        }
        System.out.println("preinverse: \n" + buf.toString());

        if(Math.abs(mdet) < 0.0005)
            System.arraycopy(IDENTITY, 0, workMatrix, 0, 16);
        else {
            for(i = 0; i < 4; i++) {
                for(j = 0; j < 4; j++) {
                    sign = 1 - ((i + j) % 2) * 2;

                    submatrix(matrix, i, j, tmp_matrix);

                    workMatrix[i + j * 4] =
                        (determinant3x3(tmp_matrix) * sign) / mdet;
                }
            }
        }

        buf = new StringBuffer();

        for(i = 0; i < 16; ) {
            buf.append("| ");
            buf.append(workMatrix[i++]);
            buf.append(' ');
            buf.append(workMatrix[i++]);
            buf.append(' ');
            buf.append(workMatrix[i++]);
            buf.append(' ');
            buf.append(workMatrix[i++]);
            buf.append(" |\n");
        }
*/
        return new Matrix3(workMatrix);
    }

    /**
     * Create the transpose of this matrix and return it in a new matrix.
     *
     * @return A matrix containing the transpose of this one
     */
    public Matrix3 jsFunction_transpose() {
        workMatrix[0] =  matrix[0];
        workMatrix[1] =  matrix[3];
        workMatrix[2] =  matrix[6];

        workMatrix[3] =  matrix[1];
        workMatrix[4] =  matrix[4];
        workMatrix[5] =  matrix[7];

        workMatrix[6] =  matrix[2];
        workMatrix[7] =  matrix[5];
        workMatrix[8] =  matrix[8];

        return new Matrix3(workMatrix);
    }

    /**
     * Multiply the passed matrix this matrix and return the value in a
     * new matrix instance.
     *
     * @param matrix The left hand matrix to use
     * @return A new matrix with the new values
     */
    public Matrix3 jsFunction_multLeft(Scriptable m) {

        if(!(m instanceof Matrix3))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        Matrix3 mat = (Matrix3)m;
        double[] left = mat.matrix;

/*
        workMatrix[0] =  left[0] * matrix[0] +
                         left[1] * matrix[4] +
                         left[2] * matrix[8] +
                         left[3] * matrix[12];
        workMatrix[1] =  left[0] * matrix[1] +
                         left[1] * matrix[5] +
                         left[2] * matrix[9] +
                         left[3] * matrix[13];
        workMatrix[2] =  left[0] * matrix[2] +
                         left[1] * matrix[6] +
                         left[2] * matrix[10] +
                         left[3] * matrix[14];

        workMatrix[4] =  left[4] * matrix[0] +
                         left[5] * matrix[4] +
                         left[6] * matrix[8] +
                         left[7] * matrix[12];
        workMatrix[5] =  left[4] * matrix[1] +
                         left[5] * matrix[5] +
                         left[6] * matrix[9] +
                         left[7] * matrix[13];
        workMatrix[6] =  left[4] * matrix[2] +
                         left[5] * matrix[6] +
                         left[6] * matrix[10] +
                         left[7] * matrix[14];

        workMatrix[8] =  left[8]  * matrix[0] +
                         left[9]  * matrix[4] +
                         left[10] * matrix[8] +
                         left[11] * matrix[12];
        workMatrix[9] =  left[8]  * matrix[1] +
                         left[9]  * matrix[5] +
                         left[10] * matrix[9] +
                         left[11] * matrix[13];
        workMatrix[10] = left[8]  * matrix[2] +
                         left[9]  * matrix[6] +
                         left[10] * matrix[10] +
                         left[11] * matrix[14];

        workMatrix[12] = left[12] * matrix[0] +
                         left[13] * matrix[4] +
                         left[14] * matrix[8] +
                         left[15] * matrix[12];
        workMatrix[13] = left[12] * matrix[1] +
                         left[13] * matrix[5] +
                         left[14] * matrix[9] +
                         left[15] * matrix[13];
        workMatrix[14] = left[12] * matrix[2] +
                         left[13] * matrix[6] +
                         left[14] * matrix[10] +
                         left[15] * matrix[14];
*/
        return new Matrix3(workMatrix);
    }

    /**
     * Multiply this matrix by the passed matrix and return the value in a
     * new matrix instance.
     *
     * @param m The left hand matrix to use
     * @return A new matrix with the new values
     */
    public Matrix3 jsFunction_multRight(Scriptable m) {

        if(!(m instanceof Matrix3))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        Matrix3 mat = (Matrix3)m;
        double[] right = mat.matrix;

/*
        workMatrix[0] =  matrix[0] * right[0] +
                         matrix[1] * right[4] +
                         matrix[2] * right[8] +
                         matrix[3] * right[12];
        workMatrix[1] =  matrix[0] * right[1] +
                         matrix[1] * right[5] +
                         matrix[2] * right[9] +
                         matrix[3] * right[13];
        workMatrix[2] =  matrix[0] * right[2] +
                         matrix[1] * right[6] +
                         matrix[2] * right[10] +
                         matrix[3] * right[14];
        workMatrix[3] =  matrix[0] * right[3] +
                         matrix[1] * right[7] +
                         matrix[2] * right[11] +
                         matrix[3] * right[15];

        workMatrix[4] =  matrix[4] * right[0] +
                         matrix[5] * right[4] +
                         matrix[6] * right[8] +
                         matrix[7] * right[12];
        workMatrix[5] =  matrix[4] * right[1] +
                         matrix[5] * right[5] +
                         matrix[6] * right[9] +
                         matrix[7] * right[13];
        workMatrix[6] =  matrix[4] * right[2] +
                         matrix[5] * right[6] +
                         matrix[6] * right[10] +
                         matrix[7] * right[14];
        workMatrix[7] =  matrix[4] * right[3] +
                         matrix[5] * right[7] +
                         matrix[6] * right[11] +
                         matrix[7] * right[15];

        workMatrix[8] =  matrix[8]  * right[0] +
                         matrix[9]  * right[4] +
                         matrix[10] * right[8] +
                         matrix[11] * right[12];
        workMatrix[9] =  matrix[8]  * right[1] +
                         matrix[9]  * right[5] +
                         matrix[10] * right[9] +
                         matrix[11] * right[13];
        workMatrix[10] = matrix[8]  * right[2] +
                         matrix[9]  * right[6] +
                         matrix[10] * right[10] +
                         matrix[11] * right[14];
        workMatrix[11] = matrix[8]  * right[3] +
                         matrix[9]  * right[7] +
                         matrix[10] * right[11] +
                         matrix[11] * right[15];

        workMatrix[12] = matrix[12] * right[0] +
                         matrix[13] * right[4] +
                         matrix[14] * right[8] +
                         matrix[15] * right[12];
        workMatrix[13] = matrix[12] * right[1] +
                         matrix[13] * right[5] +
                         matrix[14] * right[9] +
                         matrix[15] * right[13];
        workMatrix[14] = matrix[12] * right[2] +
                         matrix[13] * right[6] +
                         matrix[14] * right[10] +
                         matrix[15] * right[14];
        workMatrix[15] = matrix[12] * right[3] +
                         matrix[13] * right[7] +
                         matrix[14] * right[11] +
                         matrix[15] * right[15];
*/
        return new Matrix3(workMatrix);
    }

    /**
     * Return a vector that is this multiplied by the given vector as a
     * row.
     *
     * @param vector The row vector to use for multiplication
     * @return A new vectory containing the results
     */
    public SFVec3f jsFunction_multVecMatrix(Scriptable vector) {

        if(vector != null && !(vector instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        SFVec3f vec = (SFVec3f)vector;

        // Use the translation temp to do the multiplication but rename them
        // to make the code easier to understand.
        double[] row_vec = workTranslation;
        double[] result = workScale;

        vec.getRawData(row_vec);

        result[0] = matrix[0] * row_vec[0] + matrix[1] + matrix[2] + matrix[3];
        result[1] = matrix[0] * row_vec[1] + matrix[1] + matrix[2] + matrix[3];
        result[2] = matrix[0] * row_vec[2] + matrix[1] + matrix[2] + matrix[3];

        return new SFVec3f(result);
    }

    /**
     * Return a vector that is this multiplied by the given vector as a
     * column.
     *
     * @param vector The column vector to use for multiplication
     * @return A new vectory containing the results
     */
    public SFVec3f jsFunction_multMatrixVec(Scriptable vector) {

        if(vector != null && !(vector instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        SFVec3f vec = (SFVec3f)vector;

        // Use the translation temp to do the multiplication but rename them
        // to make the code easier to understand.
        double[] col_vec = workTranslation;
        double[] result = workScale;

        vec.getRawData(col_vec);

/*
        result[0] = matrix[0] * col_vec[0] +
                    matrix[1] * col_vec[1] +
                    matrix[2] * col_vec[2] +
                    matrix[3];
        result[1] = matrix[4] * col_vec[0] +
                    matrix[5] * col_vec[1] +
                    matrix[6] * col_vec[2] +
                    matrix[7];
        result[2] = matrix[8] * col_vec[0] +
                    matrix[9] * col_vec[1] +
                    matrix[10] * col_vec[2] +
                    matrix[11];
*/
        return new SFVec3f(result);
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

        for(int i = 0; i < 9; ) {
            buf.append("| ");
            buf.append(matrix[i++]);
            buf.append(' ');
            buf.append(matrix[i++]);
            buf.append(' ');
            buf.append(matrix[i++]);
            buf.append(" |\n");
        }

        return buf.toString();
    }

    /**
     * A manual setup of the array data. Much faster than doing
     * System.arraycopy() on each array as we don't have much to copy over
     * and it saves us making the jump to native code. Also, nice big juicy
     * Hotspot optimisable code.
     */
    private void manualSetup() {
System.out.println("Matrix3.manualSetup() not implemented");
/*
        workTranslation[0] = DEFAULT_TRANSLATION[0];
        workTranslation[1] = DEFAULT_TRANSLATION[1];
        workTranslation[2] = DEFAULT_TRANSLATION[2];

        workRotation[0] = DEFAULT_ROTATION[0];
        workRotation[1] = DEFAULT_ROTATION[1];
        workRotation[2] = DEFAULT_ROTATION[2];
        workRotation[3] = DEFAULT_ROTATION[3];

        workScale[0] = DEFAULT_SCALE[0];
        workScale[1] = DEFAULT_SCALE[1];
        workScale[2] = DEFAULT_SCALE[2];

        workOrientation[0] = DEFAULT_SCALE_ORIENT[0];
        workOrientation[1] = DEFAULT_SCALE_ORIENT[1];
        workOrientation[2] = DEFAULT_SCALE_ORIENT[2];
        workOrientation[3] = DEFAULT_SCALE_ORIENT[3];

        workCenter[0] = DEFAULT_CENTER[0];
        workCenter[1] = DEFAULT_CENTER[1];
        workCenter[2] = DEFAULT_CENTER[2];
*/
    }

    /**
     * Calculate the determine of this 3x3 matrix.
     */
    private double determinant(double[] mat) {

/*
        double ret_val = mat[0] * (mat[4] * mat[8] - mat[7] * mat[5]) -
                         mat[1] * (mat[3] * mat[8] - mat[6] * mat[5]) +
                         mat[2] * (mat[3] * mat[7] - mat[6] * mat[4]);

        return ret_val;
*/
System.out.println("Matrix3.determinant() not implemented");
return 0;
    }
}
