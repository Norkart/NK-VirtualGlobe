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

package org.web3d.parser.x3d;

// External imports
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.sav.Locator;

/**
 * The field parser implementation class for X3D field values to turn
 * them into Java primitive types.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class X3DFieldReader implements VRMLFieldReader {

    /** The current flag dealing with upper v lower case parsing of booleans */
    private boolean useLowerBoolean = false;

    /** The real parser */
    private X3DFieldParser fieldParser;

    /** Synchronisation tool to prevent multiple users all calling at once */
    private Object mutex;

    /** A scratch StringBuffer for MFString quoting.  Marshalled by mutex */
    private StringBuffer sBuff;

    /** Lookup table to enable fast boolean parsing using uppercase. */
    private static HashMap upperBooleanMap;

    /** Lookup table to enable fast boolean parsing using lowercase. */
    private static HashMap lowerBooleanMap;

    /** Locator for finding location in the main file */
    private Locator locator;

    /**
     * Static initalizer to populate the boolean maps
     */
    static {
        upperBooleanMap = new HashMap();
        upperBooleanMap.put("TRUE", Boolean.TRUE);
        upperBooleanMap.put("FALSE", Boolean.FALSE);

        lowerBooleanMap = new HashMap();
        lowerBooleanMap.put("true", Boolean.TRUE);
        lowerBooleanMap.put("false", Boolean.FALSE);
    }

    /**
     * Create a new instance of the field parser ready to go
     */
    public X3DFieldReader() {
        StringReader input = new StringReader("");

        fieldParser = new X3DFieldParser(input);
        sBuff = new StringBuffer();
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
     * @param lower false if we want to have lower case
     */
    public void setCaseSensitive(boolean lower) {
        useLowerBoolean = !lower;
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
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing unknown field type value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
            throw new InvalidFieldFormatException(pe.getMessage(),
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFInt32 field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFInt32 field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in MFInt32 field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFInt32 field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
        StringReader input = new StringReader(value);

        long ret_val = 0;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFLong();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFLong field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        long[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFLong();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFLong field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
    public long[] MFLong(String[] value) throws InvalidFieldFormatException {

        int size = (value == null) ? 0 : value.length;
        long[] ret_val = new long[size];
        int i = 0;

        try {
            synchronized(mutex) {
                for(i = 0; i < size; i++) {
                    ret_val[i] = Long.parseLong(value[i]);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new InvalidFieldFormatException("Invalid floating point value " +
                                                  "in MFLong field: " + value[i], locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFLong field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFFloat();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFFloat field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFFloat field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in MFFloat field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFFloat field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
        StringReader input = new StringReader(value);

        double ret_val = 0;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFDouble();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFDouble field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFDouble();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFDouble field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }

    /**
     * Parse an MFDouble value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFDouble(String[] value) throws InvalidFieldFormatException {

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
                                                  "in MFDouble field: " + value[i], locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFDouble field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                if(useLowerBoolean)
                    ret_val = fieldParser.SFBoolLower();
                else
                    ret_val = fieldParser.SFBool();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFBool field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
        StringReader input = new StringReader(value);

        boolean[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                if(useLowerBoolean)
                    ret_val = fieldParser.MFBoolLower();
                else
                    ret_val = fieldParser.MFBool();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFBool field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
    public boolean[] MFBool(String[] value) throws InvalidFieldFormatException {
        boolean[] ret_val = new boolean[value.length];

        HashMap map = useLowerBoolean ? lowerBooleanMap : upperBooleanMap;

        for(int i = 0; i < value.length; i++) {
            Boolean bool = (Boolean)map.get(value[i]);

            if(bool == null)
                throw new InvalidFieldFormatException("Invalid boolean " +
                    "in MFBool field: '" + value[i] + "'", locator.getLineNumber(),
                                                  locator.getColumnNumber());

            ret_val[i] = bool.booleanValue();
        }

        return ret_val;
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
        String ret_val = value;

        try {
            synchronized(mutex) {
                if (value.charAt(0) != '\"') {
                    sBuff.setLength(0);
                    sBuff.append("\"");
                    sBuff.append(value);
                    sBuff.append("\"");
                    value = sBuff.toString();
                }

                StringReader input = new StringReader(value);

                fieldParser.ReInit(input);

                ret_val = fieldParser.SFString();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFString field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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

        String[] ret_val = null;

        try {
            synchronized(mutex) {
                if (value.length() == 0 || value.charAt(0) != '\"') {
                    sBuff.setLength(0);
                    sBuff.append("\"");
                    sBuff.append(value);
                    sBuff.append("\"");
                    value = sBuff.toString();
                }

                StringReader input = new StringReader(value);

                fieldParser.ReInit(input);

                ret_val = fieldParser.MFString();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFString field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFVec2f();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFVec2f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                      "in SFVec2f field: " + value[0], locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec2f field: " + value[1]);
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
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec2f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in MFVec2f field: " + value[i], locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec2f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFVec2d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFVec2d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }

    /**
     * Parse an SFVec2d value. If there is more than one double value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec2d(String[] value) throws InvalidFieldFormatException {

        double[] ret_val = new double[2];

        synchronized(mutex) {
            try {
                ret_val[0] = Double.parseDouble(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid doubleing point value " +
                                                      "in SFVec2d field: " + value[0],
                                                      locator.getLineNumber(),
                                                      locator.getColumnNumber());
            }

            try {
                ret_val[1] = Double.parseDouble(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid doubleing point value " +
                                                      "in SFVec2d field: " + value[1],
                                                      locator.getLineNumber(),
                                                      locator.getColumnNumber());
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFVec2d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec2d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
    public double[] MFVec2d(String[] value) throws InvalidFieldFormatException {

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
                                                  "in MFVec2d field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec2d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
    public float[] SFVec3f(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFVec3f();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFVec3f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                      "in SFVec3f field: " + value[0],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec3f field: " + value[1],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec3f field: " + value[2],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec3f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in MFVec3f field: " + value[i], locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec3f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFVec3d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFVec3d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }

    /**
     * Parse an SFVec3d value. If there is more than one double value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec3d(String[] value) throws InvalidFieldFormatException {

        double[] ret_val = new double[3];

        synchronized(mutex) {
            try {
                ret_val[0] = Double.parseDouble(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid doubleing point value " +
                                                      "in SFVec3d field: " + value[0],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Double.parseDouble(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid doubleing point value " +
                                                      "in SFVec3d field: " + value[1],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[2] = Double.parseDouble(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid doubleing point value " +
                                                      "in SFVec3d field: " + value[2],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFVec3d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec3d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }

    /**
     * Parse an MFVec3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] MFVec3d(String[] value) throws InvalidFieldFormatException {

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
                                                  "in MFVec3d field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec3d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFRotation();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFVec4f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        float[] ret_val = new float[4];

        synchronized(mutex) {
            try {
                ret_val[0] = Float.parseFloat(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4f field: " + value[0],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4f field: " + value[1],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber()                                                      );
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4f field: " + value[2],
                                                    locator.getLineNumber(),
                                                  locator.getColumnNumber()                                                      );
            }

            try {
                ret_val[3] = Float.parseFloat(value[3]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4f field: " + value[3],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFVec4f();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec4f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
                                                  "in MFVec4f field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec4f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFVec4d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFVec4d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        double[] ret_val = new double[4];

        synchronized(mutex) {
            try {
                ret_val[0] = Double.parseDouble(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4d field: " + value[0],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Double.parseDouble(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4d field: " + value[1],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[2] = Double.parseDouble(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4d field: " + value[2],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[3] = Double.parseDouble(value[3]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFVec4d field: " + value[3],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFVec4d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec4d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
                                                  "in MFVec4d field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFVec4d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFRotation();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFRotation field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                      "in SFRotation field: " + value[0],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFRotation field: " + value[1],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFRotation field: " + value[2],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[3] = Float.parseFloat(value[3]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFRotation field: " + value[3],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFRotation field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in MFRotation field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFRotation field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFTime();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFTime field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFTime field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in MFTime field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFTime field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }

    /**
     * Parse an SFColor value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three color components
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFColor(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFColor();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFColor field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }

    /**
     * Parse an SFColor value. If there is more than one float value in the string
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
                                                      "in SFColor field: " + value[0],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColor field: " + value[1],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColor field: " + value[2],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFColor field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in MFColor field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFColor field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFColorRGBA();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFColorRGBA field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
    public float[] SFColorRGBA(String[] value) throws InvalidFieldFormatException {

        float[] ret_val = new float[4];

        synchronized(mutex) {
            try {
                ret_val[0] = Float.parseFloat(value[0]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColorRGBA field: " + value[0],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[1] = Float.parseFloat(value[1]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColorRGBA field: " + value[1],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[2] = Float.parseFloat(value[2]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColorRGBA field: " + value[2],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }

            try {
                ret_val[3] = Float.parseFloat(value[3]);
            } catch(NumberFormatException pe) {
                throw new InvalidFieldFormatException("Invalid floating point value " +
                                                      "in SFColorRGBA field: " + value[3],
                                                      locator.getLineNumber(),
                                                  locator.getColumnNumber());
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFColorRGBA();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFColorRGBA field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
    public float[] MFColorRGBA(String[] value) throws InvalidFieldFormatException {

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
                                                  "in MFColorRGBA field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFColorRGBA field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFImage();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFImage field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
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
                                                  "in SFImage field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFImage field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
    public int[] MFImage(String value) throws InvalidFieldFormatException {
        StringReader input = new StringReader(value);

        int[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFImage();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFImage field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }

    /**
     * Parse an MFImage value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int[] MFImage(String[] value) throws InvalidFieldFormatException {

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
                                                  "in MFImage field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFImage field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFMatrix3f();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFMatrix3f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        float[] ret_val = new float[9];

        synchronized(mutex) {
            for(int i = 0; i < 9; i++) {
                try {
                    ret_val[i] = Float.parseFloat(value[i]);
                } catch(NumberFormatException pe) {
                    throw new InvalidFieldFormatException(
                        "Invalid floating point value " +
                        "in SFMatrix3f field: " + value[i],
                        locator.getLineNumber(),
                        locator.getColumnNumber());
                }
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFMatrix3f();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFMatrix3f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        int size = (value == null) ? 0 : value.length;

        if((size % 9) != 0)
            throw new InvalidFieldFormatException("MFMatrix3f must have a " +
                                                  "multiple of 9 values defined", locator.getLineNumber(),
                                                  locator.getColumnNumber());

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
                                                  "in MFMatrix3f field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFMatrix3d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFMatrix3d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        double[] ret_val = new double[9];

        synchronized(mutex) {
            for(int i = 0; i < 9; i++) {
                try {
                    ret_val[i] = Double.parseDouble(value[i]);
                } catch(NumberFormatException pe) {
                    throw new InvalidFieldFormatException(
                        "Invalid floating point value " +
                        "in SFMatrix3d field: " + value[i],
                        locator.getLineNumber(),
                                                  locator.getColumnNumber());
                }
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFMatrix3d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(),
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFMatrix3d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        int size = (value == null) ? 0 : value.length;

        if((size % 9) != 0)
            throw new InvalidFieldFormatException("MFMatrix3d must have a " +
                                                  "multiple of 9 values defined",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());

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
                                                  "in MFMatrix3d field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFMatrix4f();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFMatrix4f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        float[] ret_val = new float[16];

        synchronized(mutex) {
            for(int i = 0; i < 16; i++) {
                try {
                    ret_val[i] = Float.parseFloat(value[i]);
                } catch(NumberFormatException pe) {
                    throw new InvalidFieldFormatException(
                        "Invalid floating point value " +
                        "in SFMatrix4f field: " + value[i],
                        locator.getLineNumber(),
                                                  locator.getColumnNumber());
                }
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        float[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFMatrix4f();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFMatrix4f field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        int size = (value == null) ? 0 : value.length;

        if((size % 16) != 0)
            throw new InvalidFieldFormatException("MFMatrix4f must have a " +
                                                  "multiple of 16 values defined",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());

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
                                                  "in MFMatrix4f field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        if ((value == null ? 0 : value.length()) == 0)
            return ret_val;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.SFMatrix4d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing SFMatrix4d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        double[] ret_val = new double[16];

        synchronized(mutex) {
            for(int i = 0; i < 16; i++) {
                try {
                    ret_val[i] = Double.parseDouble(value[i]);
                } catch(NumberFormatException pe) {
                    throw new InvalidFieldFormatException(
                        "Invalid floating point value " +
                        "in SFMatrix4d field: " + value[i],
                        locator.getLineNumber(),
                                                  locator.getColumnNumber());
                }
            }
        }

        return ret_val;
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
        StringReader input = new StringReader(value);

        double[] ret_val = null;

        try {
            synchronized(mutex) {
                fieldParser.ReInit(input);

                ret_val = fieldParser.MFMatrix4d();
            }
        } catch(ParseException pe) {
            throw new InvalidFieldFormatException(pe.getMessage(), locator.getLineNumber(),
                                                  locator.getColumnNumber());
        } catch(TokenMgrError tme) {
            throw new InvalidFieldFormatException("Invalid field value encountered when parsing MFMatrix4d field value",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
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
        int size = (value == null) ? 0 : value.length;

        if((size % 16) != 0)
            throw new InvalidFieldFormatException("MFMatrix4d must have a " +
                                                  "multiple of 16 values defined",
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());

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
                                                  "in MFMatrix4d field: " + value[i],
                                                  locator.getLineNumber(),
                                                  locator.getColumnNumber());
        }

        return ret_val;
    }
}
