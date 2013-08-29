/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.parser;

// External imports
import java.io.IOException;

// Local imports
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.sav.Locator;

/**
 * A parser interface for raw field values to turn them into Java primitive
 * types for use within the runtime application.
 * <p>
 * The parser assumes that we have a raw field value that does not contain any
 * surrounding values. For example, it expects MFField values to be stripped of
 * surrounding brackets before being passed to these methods.
 * <p>
 * There are two ways of using this class: parsing a know field type, and
 * parsing an unknown field type and getting the parser to return the
 * appropriate (closest guess) value to you.
 * <p>
 * The assumption of this parser is that each time you call the method it will
 * have a non-zero length, non-empty string. There should be at least one
 * non-whitespace character in the string. If not, a parse exception will be
 * raised.
 *
 * By default, the boolean value parsing is upper case.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public interface VRMLFieldReader {

    /**
     * Set the external locator that can be used to generate line numbers
     * externally to the field that is being parsed. This allows the
     * parsers to generate information that is relevant to the entire file
     * not just the field being processed right now.
     *
     * @param l The locator instance to be used
     */
    public void setDocumentLocator(Locator l);

    /**
     * Set the flag to decide whether VRML field parsing should be case
     * sensitive or not. Since the XML version uses lower case for booleans
     * and the UTF8 format uses upper case, the flag is the simple way of
     * toggling between the two states. The default is for upper case parsing
     * only.
     *
     * @param lower True if we want to have lower case
     */
    public void setCaseSensitive(boolean lower);

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
        throws InvalidFieldFormatException;

    /**
     * Parse an SFInt value. If there is more than one int value in the string it
     * will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The int value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public int SFInt32(String value) throws InvalidFieldFormatException;

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
    public int[] MFInt32(String value) throws InvalidFieldFormatException;

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
    public int[] MFInt32(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFLong value. If there is more than one int value in the string it
     * will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The long value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public long SFLong(String value) throws InvalidFieldFormatException;

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
    public long[] MFLong(String value) throws InvalidFieldFormatException;

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
    public long[] MFLong(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFFloat value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The float value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float SFFloat(String value) throws InvalidFieldFormatException;

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
    public float[] MFFloat(String value) throws InvalidFieldFormatException;

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
    public float[] MFFloat(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFDouble value. If there is more than one double value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The double value as a primitive
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double SFDouble(String value) throws InvalidFieldFormatException;

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
    public double[] MFDouble(String value) throws InvalidFieldFormatException;

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
    public double[] MFDouble(String[] value) throws InvalidFieldFormatException;

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
    public boolean SFBool(String value) throws InvalidFieldFormatException;

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
    public boolean[] MFBool(String value) throws InvalidFieldFormatException;

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
    public boolean[] MFBool(String[] value) throws InvalidFieldFormatException;

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
    public String SFString(String value) throws InvalidFieldFormatException;

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
    public String[] MFString(String value) throws InvalidFieldFormatException;

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
    public String[] MFString(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec2f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The two components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec2f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec2f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The two components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec2f(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec2d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The two components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec2d(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec2d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The two components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec2d(String[] value) throws InvalidFieldFormatException;

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
    public double[] MFVec2d(String value) throws InvalidFieldFormatException;

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
    public double[] MFVec2d(String[] value) throws InvalidFieldFormatException;

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
    public float[] MFVec2f(String value) throws InvalidFieldFormatException;

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
    public float[] MFVec2f(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec3f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec3f(String[] value) throws InvalidFieldFormatException;

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
    public float[] MFVec3f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an MFVec3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec3f(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec3d(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The three components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec3d(String[] value) throws InvalidFieldFormatException;

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
    public double[] MFVec3d(String value) throws InvalidFieldFormatException;

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
    public double[] MFVec3d(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec4f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFVec4f(String[] value) throws InvalidFieldFormatException;

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
    public float[] MFVec4f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an MFVec4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFVec4f(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec4d(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFVec4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four components of the vector
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFVec4d(String[] value) throws InvalidFieldFormatException;

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
    public double[] MFVec4d(String value) throws InvalidFieldFormatException;

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
    public double[] MFVec4d(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFRotation value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four values of the parsed quaternion
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFRotation(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFRotation value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The four values of the parsed quaternion
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFRotation(String[] value) throws InvalidFieldFormatException;

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
    public float[] MFRotation(String value) throws InvalidFieldFormatException;

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
    public float[] MFRotation(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFTime value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The time value parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double SFTime(String value) throws InvalidFieldFormatException;

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
    public double[] MFTime(String value) throws InvalidFieldFormatException;

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
    public double[] MFTime(String[] value) throws InvalidFieldFormatException;

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
    public float[] SFColor(String value) throws InvalidFieldFormatException;

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
    public float[] SFColor(String[] value) throws InvalidFieldFormatException;

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
     * @return The array of colors, no range checking
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColor(String value) throws InvalidFieldFormatException;

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
     * @return The array of colors, no range checking
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColor(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFColorRGBA value. If there is more than one float value in the string
     * it will be ignored.
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
    public float[] SFColorRGBA(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFColorRGBA value. If there is more than one float value in the string
     * it will be ignored.
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
    public float[] SFColorRGBA(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an MFColorRGBA value. The color differs from the float value by being
     * clamped between 0 and 1.
     * <pre>
     * MFColor ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of colors, no range checking
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColorRGBA(String value) throws InvalidFieldFormatException;

    /**
     * Parse an MFColorRGBA value. The color differs from the float value by being
     * clamped between 0 and 1.
     * <pre>
     * MFColor ::=
     *   "[" (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)* "]" |
     *   (NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL)*
     * </pre>
     *
     * @param value The raw value as a string to be parsed
     * @return The array of colors, no range checking
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFColorRGBA(String[] value) throws InvalidFieldFormatException;

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
    public int[] SFImage(String value) throws InvalidFieldFormatException;

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
    public int[] SFImage(String[] value) throws InvalidFieldFormatException;

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
    public int[] MFImage(String value) throws InvalidFieldFormatException;

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
    public int[] MFImage(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix3f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix3f(String[] value) throws InvalidFieldFormatException;

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
    public float[] MFMatrix3f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an MFMatrix3f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The array of 9 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFMatrix3f(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix3d(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix3d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 9 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix3d(String[] value) throws InvalidFieldFormatException;

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
    public double[] MFMatrix3d(String value) throws InvalidFieldFormatException;

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
    public double[] MFMatrix3d(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix4f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] SFMatrix4f(String[] value) throws InvalidFieldFormatException;

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
    public float[] MFMatrix4f(String value) throws InvalidFieldFormatException;

    /**
     * Parse an MFMatrix4f value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as an array of strings to be parsed
     * @return The array of 16 component matrices parsed
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public float[] MFMatrix4f(String[] value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix4d(String value) throws InvalidFieldFormatException;

    /**
     * Parse an SFMatrix4d value. If there is more than one float value in the string
     * it will be ignored.
     *
     * @param value The raw value as a string to be parsed
     * @return The 16 components of the matrix
     * @throws InvalidFieldFormatException The field does not match the
     *    required profile
     */
    public double[] SFMatrix4d(String[] value) throws InvalidFieldFormatException;

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
    public double[] MFMatrix4d(String value) throws InvalidFieldFormatException;

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
    public double[] MFMatrix4d(String[] value) throws InvalidFieldFormatException;
}
