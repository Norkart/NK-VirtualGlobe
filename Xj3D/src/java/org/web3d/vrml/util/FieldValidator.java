/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.util;

// External imports

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
  * Validates the value of a VRML field against the specification.
  * <p>
  * Will throw a InvalidFieldValueException if the value is out of range
  *
  * @author Alan Hudson
  * @version $Revision: 1.12 $
  */
public class FieldValidator {

    /**
     * Check the validity of a color field
     * Valid range is [0,1] for each element
     *
     * @param idString A field ID name for nice error messages
     * @param newColor The proposed new value
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkColorVector(String idString, float[] newColor)
        throws InvalidFieldValueException {

        if (newColor[0] < 0 || newColor[0] > 1 ||
            newColor[1] < 0 || newColor[1] > 1 ||
            newColor[2] < 0 || newColor[2] > 1) {

            throw new InvalidFieldValueException(
                "Color components must be [0,1]. " + idString +
                " Value is: " + newColor[0] + "," + newColor[1] + "," + newColor[2]);
        }
    }

    /**
     * Check the validity of a color field
     * Valid range is [0,1] for each element
     *
     * @param idString A field ID name for nice error messages
     * @param newColor The proposed new value
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkColorAlphaVector(String idString, float[] newColor)
        throws InvalidFieldValueException {

        if (newColor[0] < 0 || newColor[0] > 1 ||
            newColor[1] < 0 || newColor[1] > 1 ||
            newColor[2] < 0 || newColor[2] > 1 ||
            newColor[3] < 0 || newColor[3] > 1) {

            throw new InvalidFieldValueException(
                "Color components must be [0,1]. " + idString +
                " Value is: " + newColor[0] + "," + newColor[1] +
                "," + newColor[2] + "," + newColor[3]);
        }
    }

    /**
     * Check the validity of a MFColor field array.
     * Valid range is [0,1] for each element
     *
     * @param idString A field ID name for nice error messages
     * @param newColor The proposed new value
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkColorArray(String idString, float[] newColor)
        throws InvalidFieldValueException {

        for(int i = 0; i < newColor.length; i++) {
            if(newColor[i] < 0 || newColor[i] > 1)
                throw new InvalidFieldValueException(
                    "Color components must be [0,1]. " + idString +
                    " Value at index " + i + " is: " + newColor[i]);
        }
    }

    /**
     * Check the validity of a float field
     * Valid range is [0,1]
     *
     * @param idString A field ID name for nice error messages
     * @param newFloat the float to validate
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkFloat(String idString, float newFloat)
        throws InvalidFieldValueException {

        if (newFloat < 0.0f || newFloat > 1.0f) {
            throw new InvalidFieldValueException(
                "Float value must be [0,1].  " + idString +  " Value is: "
                + newFloat);
        }
    }

    /**
     * Check the validity of a float pos infinity field
     * Valid range is [0,infinity)
     *
     * @param idString A field ID name for nice error messages
     * @param newFloat the float to validate
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkFloatPosInfinity(String idString, float newFloat)
        throws InvalidFieldValueException {

        if (newFloat < 0.0f) {
            throw new InvalidFieldValueException(
                "Float value must be [0,infinity). " + idString +
                " Value is: " + newFloat);
        }
    }

    /**
     * Check the validity of a double pos infinity field
     * Valid range is [0,infinity)
     *
     * @param idString A field ID name for nice error messages
     * @param newDouble the double to validate
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkDoublePosInfinity(String idString, double newDouble)
        throws InvalidFieldValueException {

        if (newDouble < 0.0) {
            throw new InvalidFieldValueException(
                "Double value must be [0,infinity). " + idString +
                " Value is: " + newDouble);
        }
    }

    /**
     * Check the validity of a integer pos infinity field
     * Valid range is [0,infinity)
     *
     * @param idString A field ID name for nice error messages
     * @param newInt the integer to validate
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkIntPosInfinity(String idString, int newInt)
        throws InvalidFieldValueException {

        if (newInt < 0) {
            throw new InvalidFieldValueException(
                "Integer value must be [0,infinity). " + idString +
                " Value is: " + newInt);
        }
    }

    /**
     * Check the validity of the bboxSize field. All values must be either
     * greater than or equal to zero. They may also have the value of
     * -1 for all fields to indicate that it should be auto-computed. If one
     * field is -1 then all fields must be -1.
     *
     * @param idString A field ID name for nice error messages
     * @param bboxSize The field to validate.
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkBBoxSize(String idString, float bboxSize[])
        throws InvalidFieldValueException {

        if (bboxSize[0] < 0 || bboxSize[1] < 0 || bboxSize[2] < 0) {
            if (bboxSize[0] != -1 || bboxSize[1] != -1 || bboxSize[2] != -1) {
                throw new InvalidFieldValueException(
                    "BBoxSize must be (0,inf) or -1, -1, -1. " + idString +
                    " Value is: " + bboxSize[0] + "," + bboxSize[1] + "," +
                    bboxSize[2]
                );
            }
        }
    }

    /**
     * Check the validity of the 2D bboxSize field. All values must be either
     * greater than or equal to zero. They may also have the value of
     * -1 for all fields to indicate that it should be auto-computed. If one
     * field is -1 then all fields must be -1.
     *
     * @param idString A field ID name for nice error messages
     * @param bboxSize The field to validate.
     * @throws InvalidFieldValueException Value out of spec
     */
    public static void checkBBoxSize2D(String idString, float bboxSize[])
        throws InvalidFieldValueException {

        if (bboxSize[0] < 0 || bboxSize[1] < 0) {
            if (bboxSize[0] != -1 || bboxSize[1] != -1) {
                throw new InvalidFieldValueException(
                    "BBoxSize must be (0,inf) or -1, -1. " + idString +
                    " Value is: " + bboxSize[0] + "," + bboxSize[1]
                );
            }
        }
    }

}
