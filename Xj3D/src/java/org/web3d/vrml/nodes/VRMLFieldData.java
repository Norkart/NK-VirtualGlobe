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
package org.web3d.vrml.nodes;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.VRMLNode;

/**
 * Data holder class to hold information that is returned from the
 * {@link VRMLNodeType} {@link VRMLNodeType#getFieldValue(int)} method.
 * <p>
 *
 * <b>WARNING</b>
 * <p>
 * Note that in the interests of speed this class will normally represent a
 * reference to the internal structure rather than a copy of it. The user of
 * this array should <b>never</b> directly manipulate these values because it
 * will reek havoc to the internal implementation. It is really designed for
 * the scripting and external interface glue code to use and no-one else.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class VRMLFieldData {

    /** The field returned was boolean data */
    public static final short BOOLEAN_DATA = 1;

    /** The field returned was int data */
    public static final short INT_DATA = 2;

    /** The field returned was long data */
    public static final short LONG_DATA = 3;

    /** The field returned was float data */
    public static final short FLOAT_DATA = 4;

    /** The field returned was double data */
    public static final short DOUBLE_DATA = 5;

    /** The field returned was a string */
    public static final short STRING_DATA = 6;

    /** The field returned was a node */
    public static final short NODE_DATA = 7;

    /** The field returned was boolean array data */
    public static final short BOOLEAN_ARRAY_DATA = 8;

    /** The field returned was int array data */
    public static final short INT_ARRAY_DATA = 9;

    /** The field returned was long array data */
    public static final short LONG_ARRAY_DATA = 10;

    /** The field returned was float array data */
    public static final short FLOAT_ARRAY_DATA = 11;

    /** The field returned was double array data */
    public static final short DOUBLE_ARRAY_DATA = 12;

    /** The field returned was a node array */
    public static final short NODE_ARRAY_DATA = 13;

    /** The field returned was a string array */
    public static final short STRING_ARRAY_DATA = 14;


    /** The type of data that has been returned in this class */
    public short dataType;

    /**
     * The number of valid values in an array field. If the array is a flattened
     * array of items such as MFColor, then this is the number of values in the
     * array not the length of items (ie size = numElements * 3 for MFColor).
     */
    public int numElements;

    /** The integer value of the field. */
    public int intValue;

    /** The long value of the field. */
    public long longValue;

    /** The float value of the field. */
    public float floatValue;

    /** The double value of the field. */
    public double doubleValue;

    /** The boolean value of the field. */
    public boolean booleanValue;

    /** The node value of the field */
    public VRMLNode nodeValue;

    /** The string value of the field */
    public String stringValue;

    /** An array of int values for MFInt, SFImage  etc */
    public int[] intArrayValue;

    /** An array of int values for MFLong */
    public long[] longArrayValue;

    /** An array of float values for MFFloat, SFVec3f, MFColor etc */
    public float[] floatArrayValue;

    /** An array of double values for MFTime, MFDouble etc */
    public double[] doubleArrayValue;

    /** An array of boolean values for MFBool etc */
    public boolean[] booleanArrayValue;

    /** An array of node values of the field */
    public VRMLNode[] nodeArrayValue;

    /** An array of string values of the field */
    public String[] stringArrayValue;

    /**
     * Default constructor creates a data object with no values set.
     */
    public VRMLFieldData() {
    }

    /**
     * Copy constructor that takes the data in the given node and assigns
     * the same values to this instance. For array data types, this is a
     * copy by reference action.
     *
     * @param data The instance to copy values from
     */
    public VRMLFieldData(VRMLFieldData data) {
        dataType = data.dataType;
        numElements = data.numElements;

        switch(dataType) {
            case BOOLEAN_DATA:
                booleanValue = data.booleanValue;
                break;

            case INT_DATA:
                intValue = data.intValue;
                break;

            case LONG_DATA:
                longValue = data.longValue;
                break;

            case FLOAT_DATA:
                floatValue = data.floatValue;
                break;

            case DOUBLE_DATA:
                doubleValue = data.doubleValue;
                break;

            case STRING_DATA:
                stringValue = data.stringValue;
                break;

            case NODE_DATA:
                nodeValue = data.nodeValue;
                break;

            case BOOLEAN_ARRAY_DATA:
                booleanArrayValue = data.booleanArrayValue;
                break;

            case INT_ARRAY_DATA:
                intArrayValue = data.intArrayValue;
                break;

            case LONG_ARRAY_DATA:
                longArrayValue = data.longArrayValue;
                break;

            case FLOAT_ARRAY_DATA:
                floatArrayValue = data.floatArrayValue;
                break;

            case DOUBLE_ARRAY_DATA:
                doubleArrayValue = data.doubleArrayValue;
                break;

            case NODE_ARRAY_DATA:
                nodeArrayValue = data.nodeArrayValue;
                break;

            case STRING_ARRAY_DATA:
                stringArrayValue = data.stringArrayValue;
                break;

        }
    }

    /**
     * Clear the contents of the current field data.
     */
    public void clear() {
        intArrayValue = null;
        floatArrayValue = null;
        doubleArrayValue = null;
        booleanArrayValue = null;
        nodeValue = null;
        nodeArrayValue = null;
        stringValue = null;
        stringArrayValue = null;
    }
}
