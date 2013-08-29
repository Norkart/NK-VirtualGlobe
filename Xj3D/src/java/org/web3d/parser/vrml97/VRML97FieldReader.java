/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.parser.vrml97;

// External imports
import java.io.IOException;
import java.io.StringReader;

// Local imports
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.sav.Locator;

/**
 * The default field parser implementation class for raw field values to turn
 * them into Java primitive types.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class VRML97FieldReader implements VRMLFieldReader {

    /** Error message to say doubles are not supported in VRML97 */
    private static final String DOUBLE_SPT_MSG =
        "VRML97 does not support doubles";

    /** Error message to say doubles are not supported in VRML97 */
    private static final String LONG_SPT_MSG =
        "VRML97 does not support longs";

    /** Error message to say doubles are not supported in VRML97 */
    private static final String RGBA_SPT_MSG =
        "VRML97 does not support RGBA colors";

    /** Error message to say doubles are not supported in VRML97 */
    private static final String MFIMAGE_SPT_MSG =
        "VRML97 does not support MFImage";

    /** Error message to say doubles are not supported in VRML97 */
    private static final String MFBOOL_SPT_MSG =
        "VRML97 does not support MFBool";

    /** The current flag dealing with upper v lower case parsing of booleans */
    private boolean useLowerBoolean = false;

    /** The real parser */
    private VRML97FieldParser fieldParser;

    /** Synchronisation tool to prevent multiple users all calling at once */
    private Object mutex;

    /** Locator for finding location in the main file */
    private Locator locator;

    /**
     * Create a new instance of the field parser ready to go
     */
    public VRML97FieldReader() {
        StringReader input = new StringReader("");

        fieldParser = new VRML97FieldParser(input);
        mutex = new Object();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLFieldReader
    //----------------------------------------------------------

    /**
     * Set the external locator that can be used to generate line numbers
     * externally to the field that is being parsed. This allows the
     * parsers to generate information that is relevant to the entire file
     * not just the field being processed right now.
     *
     * @param l The locator instance to be used
     */
    public void setDocumentLocator(Locator l) {
        locator = l;
    }

    /**
     * Set the flag to decide whether VRML field parsing should be case
     * sensitive or not. Since the XML version uses lower case for booleans
     * and the UTF8 format uses upper case, the flag is the simple way of
     * toggling between the two states. The default is for upper case parsing
     * only.
     *
     * @param lower True if we want to have lower case
     */
    public void setCaseSensitive(boolean lower) {
        useLowerBoolean = lower;
    }

    /**
     * This does not use the other methods to do the field parsing. Instead, it
     * goes on the raw values that are presented and returns objects to represent
     * that. We look for either single values or multiple values. For any of these
     * we return a representative object. The return value could be one of these:
     * <ul>
     * <li>Boolean
     * <li>Integer
     * <li>Float
     * <li>String
     * <li>boolean[]
     * <li>int[]
     * <li>float[]
     * <li>String[]
     * </ul>
     *
     * @param useInt A hint to use if this is a number field we are parsing to
     *    decide whether to parse number strings as an int or float for type
     *    checking. If expecting any other sort of field, has no effect.
     * @param value The raw value as a string to be parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public Object parseUnknownField(String value, boolean useInt)
        throws InvalidFieldFormatException {

        StringReader input = new StringReader(value);

        Object ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.parseUnknownField(useInt);
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an SFInt value. If there is more than one int value in the string it
     * will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The int value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int SFInt32(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        int ret_val = 0;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFInt32();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFInt32 value.
     * <pre>
     * MFInt32 ::=
     *   "[" NUMBER_LITERAL* "]" |
     *   NUMBER_LITERAL*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of int values as primitives
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int[] MFInt32(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        int[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFInt32();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFInt32 value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int[] MFInt32(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        int[] ret_val = new int[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Integer.parseInt(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MFInt32 field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse an SFLong value. If there is more than one int value in the string it
     * will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The long value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public long SFLong(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(LONG_SPT_MSG);
    }

    /**
     * Parse an MFLong value.
     * <pre>
     * MFLong ::=
     *   "[" NUMBER_LITERAL* "]" |
     *   NUMBER_LITERAL*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of long values as primitives
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public long[] MFLong(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(LONG_SPT_MSG);
    }

    /**
     * Parse an MFLong value.
     * <pre>
     * MFLong ::=
     *   "[" NUMBER_LITERAL* "]" |
     *   NUMBER_LITERAL*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of long values as primitives
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public long[] MFLong(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(LONG_SPT_MSG);
    }

    /**
     * Parse an SFFloat value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The float value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float SFFloat(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float ret_val = 0;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFFloat();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFFloat value.
     * <pre>
     * MFFloat ::=
     *   "[" NUMBER_LITERAL* "]" |
     *   NUMBER_LITERAL*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of float values as primitives
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFFloat(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFFloat();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFFloat value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFFloat(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        float[] ret_val = new float[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Float.parseFloat(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MFFloat field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse an SFDouble value. If there is more than one double value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The double value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double SFDouble(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFDouble value.
     * <pre>
     * MFDouble ::=
     *   "[" NUMBER_LITERAL* "]" |
     *   NUMBER_LITERAL*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of double values as primitives
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFDouble(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFDouble value.
     * <pre>
     * MFDouble ::=
     *   "[" NUMBER_LITERAL* "]" |
     *   NUMBER_LITERAL*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of double values as primitives
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFDouble(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFBool value. If there is more than one boolean value in the string
     * it will be ignored. Whether or not it handles the case sensitivity is
     * determined by the flag set in the {@link #setCaseSensitive(boolean)}
     * method.
     *
     * @param value The raw value as a string to be parsed
     * @return The primitive representation in the string
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public boolean SFBool(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        boolean ret_val = false;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFBool();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFBool value. Whether or not it handles the case sensitivity is
     * determined by the flag set in the {@link #setCaseSensitive(boolean)}
     * method.
     *
     * <pre>
     * MFBool ::=
     *   "[" ("TRUE" | "FALSE")* "]" |
     *   ("TRUE" | "FALSE")*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return An array of primitive representations of the values
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public boolean[] MFBool(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFBOOL_SPT_MSG);
    }

    /**
     * Parse an MFBool value. Whether or not it handles the case sensitivity is
     * determined by the flag set in the {@link #setCaseSensitive(boolean)}
     * method.
     *
     * <pre>
     * MFBool ::=
     *   "[" ("TRUE" | "FALSE")* "]" |
     *   ("TRUE" | "FALSE")*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return An array of primitive representations of the values
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public boolean[] MFBool(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFBOOL_SPT_MSG);
    }

    /**
     * Parse an SFString value. We really shouldn't need this, but it is here for
     * completeness. If the string starts and ends with quotes it will strips the
     * quotes.
     *
     * @param value The raw value as a string to be parsed
     * @return The same string
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public String SFString(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        String ret_val = value;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFString();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFString value.
     * <pre>
     * MFString ::=
     *   "[" ( STRING_LITERAL)* "]" |
     *   (STRING_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of strings represented in the original string
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public String[] MFString(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        String[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFString();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }


    /**
     * Parse an MFString value from an array of strings.
     * <pre>
     * MFString ::=
     *   "[" ( STRING_LITERAL)* "]" |
     *   (STRING_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of strings represented in the original string
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public String[] MFString(String[] value) throws InvalidFieldFormatException {
        // easiest way is to combine them into a single string and then just
        // parse the whole lot.
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i < value.length; i++)
            buf.append(value[i]);

        return MFString(buf.toString());
    }

    /**
     * Parse an SFVec2f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The two components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec2f(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFVec2f();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an SFVec2f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec2f(String[] value) throws InvalidFieldFormatException {

        float[] ret_val = new float[2];

        synchronized(mutex) {
            try {
                ret_val[0] = Float.parseFloat(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in MfVec3f field: " + value[0]);
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in MfVec3f field: " + value[1]);
            }
        }

        return ret_val;
    }

    /**
     * Parse an MFVec2f value.
     * <pre>
     * MFVec2f ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 2 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec2f(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFVec2f();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFVec2f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec2f(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        float[] ret_val = new float[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Float.parseFloat(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MFVec2f field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse an SFVec2d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The two components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec2d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFVec2d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The two components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec2d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec2d value.
     * <pre>
     * MFVec2d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 2 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFVec2d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec2d value.
     * <pre>
     * MFVec2d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 2 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFVec2d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFVec3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec3f(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFVec3f();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an SFVec3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec3f(String[] value) throws InvalidFieldFormatException {

        float[] ret_val = new float[3];

        synchronized(mutex) {
            try {
                ret_val[0] = Float.parseFloat(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in MfVec3f field: " + value[0]);
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in MfVec3f field: " + value[1]);
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in MfVec3f field: " + value[2]);
            }
        }

        return ret_val;
    }

    /**
     * Parse an MFVec3f value.
     * <pre>
     * MFVec3f ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 3 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec3f(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFVec3f();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }
        return ret_val;
    }

    /**
     * Parse an MFVec3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec3f(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        float[] ret_val = new float[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Float.parseFloat(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MfVec3f field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse an SFVec3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec3d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFVec3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec3d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec3d value.
     * <pre>
     * MFVec3d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 3 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFVec3d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec3d value.
     * <pre>
     * MFVec3d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 3 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFVec3d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFVec4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec4f(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFVec4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec4f(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec4f value.
     * <pre>
     * MFVec4f ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 3 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec4f(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec4f(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFVec4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec4d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFVec4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec4d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec4d value.
     * <pre>
     * MFVec4d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 3 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFVec4d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an MFVec4d value.
     * <pre>
     * MFVec4d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 3 component vectors parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFVec4d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(DOUBLE_SPT_MSG);
    }

    /**
     * Parse an SFRotation value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four values of the parsed quaternion
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFRotation(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFRotation();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an SFRotation value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFRotation(String[] value) throws InvalidFieldFormatException {

        float[] ret_val = new float[4];

        synchronized(mutex) {
            try {
                ret_val[0] = Float.parseFloat(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFRotation field: " + value[0]);
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFRotation field: " + value[1]);
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFRotation field: " + value[2]);
            }

            try {
                ret_val[3] = Float.parseFloat(value[3]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFRotation field: " + value[3]);
            }
        }

        return ret_val;
    }

    /**
     * Parse an MFRotation value.
     * <pre>
     * MFRotation ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of rotation quaternions parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFRotation(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFRotation();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFRotation value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFRotation(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        float[] ret_val = new float[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Float.parseFloat(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MFRotation field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse an SFTime value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The time value parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double SFTime(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        double ret_val = 0;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFTime();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFTime value. In VRML97 MFTime are not legal field types. However,
     * we provide it here for completeness and that it might be used by VRML 3.0.
     * <pre>
     * MFTime ::=
     *   "[" NUMBER_LITERAL* "]" |
     *   NUMBER_LITERAL*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of time values parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFTime(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFTime();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFTime value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFTime(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        double[] ret_val = new double[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Double.parseDouble(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MFTime field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse an SFColor value. The color differs from the float value by being
     * clamped between 0 and 1. Any more than a single colour value is ignored.
     * <pre>
     * SFColor ::=
     *   NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The three color components
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFColor(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFColor();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an SFColorRGBA value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFColor(String[] value) throws InvalidFieldFormatException {

        float[] ret_val = new float[3];

        synchronized(mutex) {
            try {
                ret_val[0] = Float.parseFloat(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColor field: " + value[0]);
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColor field: " + value[1]);
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColor field: " + value[2]);
            }
        }

        return ret_val;
    }

    /**
     * Parse an MFColor value. The color differs from the float value by being
     * clamped between 0 and 1.
     * <pre>
     * MFColor ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @param The array of colors, no range checking
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColor(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFColor();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFColor value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColor(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        float[] ret_val = new float[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Float.parseFloat(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MFColor field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse a SFColorRGBA value. The color differs from the float value by being
     * clamped between 0 and 1. Any more than a single colour value is ignored.
     * <pre>
     * SFColorRGBA ::=
     *   NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The three color components
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFColorRGBA(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(RGBA_SPT_MSG);
    }

    /**
     * Parse a SFColorRGBA value. The color differs from the float value by being
     * clamped between 0 and 1. Any more than a single colour value is ignored.
     * <pre>
     * SFColorRGBA ::=
     *   NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The three color components
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFColorRGBA(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(RGBA_SPT_MSG);
    }

    /**
     * Parse an MFColorRGBA value. The color differs from the float value by being
     * clamped between 0 and 1.
     * <pre>
     * MFColorRGBA ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @param The array of colors, no range checking
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColorRGBA(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(RGBA_SPT_MSG);
    }

    /**
     * Parse an MFColorRGBA value. The color differs from the float value by being
     * clamped between 0 and 1.
     * <pre>
     * MFColorRGBA ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @param The array of colors, no range checking
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColorRGBA(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(RGBA_SPT_MSG);
    }

    /**
     * Parse an SFImage value.
     * <pre>
     * SFImage ::=
     *   NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL (NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The integers that are parsed as an image
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int[] SFImage(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        int[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFImage();
            }
        } catch(ParseException pe) {
            String newMsg = pe.getMessage() + " Offending value: " + value;
            throw new InvalidFieldFormatException(newMsg);
        }

        return ret_val;
    }

    /**
     * Parse an MFVec2f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int[] SFImage(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        int[] ret_val = new int[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Integer.parseInt(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in SFImage field: " + value[i]);
        }

        return ret_val;
    }

    /**
     * Parse an MFImage value.
     * <pre>
     * SFImage ::=
     *   NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL (NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The integers that are parsed as an image
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int[] MFImage(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFImage value.
     * <pre>
     * SFImage ::=
     *   NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL (NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The integers that are parsed as an image
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int[] MFImage(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix3f(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix3f(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix3f value.
     * <pre>
     * MFMatrix3f ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 9 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFMatrix3f(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The array of 9 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFMatrix3f(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix3d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix3d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix3d value.
     * <pre>
     * MFMatrix3d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 9 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFMatrix3d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix3d value.
     * <pre>
     * MFMatrix3d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 9 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFMatrix3d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix4f(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix4f(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix4f value.
     * <pre>
     * MFMatrix4f ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 16 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFMatrix4f(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The array of 16 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFMatrix4f(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix4d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an SFMatrix4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix4d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix4d value.
     * <pre>
     * MFMatrix4d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 16 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFMatrix4d(String value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }

    /**
     * Parse an MFMatrix4d value.
     * <pre>
     * MFMatrix4d ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of 16 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFMatrix4d(String[] value) throws InvalidFieldFormatException {
        throw new InvalidFieldFormatException(MFIMAGE_SPT_MSG);
    }
}
