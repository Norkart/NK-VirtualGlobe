/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.export.compressors;

/**
 * Compresses float by using quanitization.
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public class FloatPacker {

    private static final int MANTISSA_BITS_32 = 23;
    private static final long EXPONENT_BITS_32 = 8;
    private static final long MANTISSA_MASK_32 = 0x007fffff;
    private static final long EXPONENT_MASK_32 = 0x7f800000;
    private static final long SIGN_MASK_32     = 0x80000000;
    private static final int EXPONENT_BIAS_32 = 127;
    private static final int SIGN_SHIFT_32    = 31;

    private int exponent_bits, mantissa_bits;
    private int sign_mask, mantissa_mask, exponent_mask;
    private int exponent_bias;
    private int sign_shift;

    private int exponent_min, exponent_max;

    public FloatPacker(int num_exponent_bits, int num_mantissa_bits) {
        init(num_exponent_bits, num_mantissa_bits);
    }

    private void init(int num_exponent_bits, int num_mantissa_bits) {
        exponent_bits = num_exponent_bits;
        mantissa_bits = num_mantissa_bits;
        exponent_bias = (1 << (exponent_bits - 1)) - 1;
        sign_shift = exponent_bits + mantissa_bits;

        sign_mask = 1 << sign_shift;
        exponent_mask = ((1 << exponent_bits) - 1) << mantissa_bits;
        mantissa_mask = (1 << mantissa_bits) - 1;

        exponent_max = (1 << (exponent_bits - 1)) - 1;
        exponent_min = -exponent_max - 1;

        if (exponent_bits > EXPONENT_BITS_32) {
            System.out.println("Too many exponent bits, max: " + EXPONENT_BITS_32);
            new Exception().printStackTrace();
            exponent_bits = (int) EXPONENT_BITS_32;
        }
        if (mantissa_bits > MANTISSA_BITS_32) {
            System.out.println("Too many mantissa bits, max: " + MANTISSA_BITS_32);
            mantissa_bits = (int) MANTISSA_BITS_32;
        }
    }

    public void reinit(int num_exponent_bits, int num_mantissa_bits) {
        init(num_exponent_bits, num_mantissa_bits);
    }

    /**
     * Encode a float.  The return value is the bit reprenstation using
     * the requested mantissa and exponent bits.  Mask out the result
     * and save the bits.
     *
     * @param f The float to encode
     * @param rounding Whether to round the result.  Gives better accuracy.
     */
    public long encode(float f, boolean rounding) {
        if (f == 0.0f)
            return 0;     // IEEE 0 is a special case.

        long src = Float.floatToIntBits(f);

        int mantissa_shift = (MANTISSA_BITS_32 - mantissa_bits);

        // Mask out the mantissa, exponent, and sign fields.

        long mantissa = (src & MANTISSA_MASK_32);

        int exponent = (int) (src & EXPONENT_MASK_32) >> MANTISSA_BITS_32;
        long sign     = (src >> SIGN_SHIFT_32);

        // Subtract the IEEE-754 number's exponent bias, then add our own.

        exponent -= EXPONENT_BIAS_32;

        // Round the mantissa, and bump up the exponent if necessary.
        if (rounding && mantissa_shift != 0) {
            int rounding_constant = 1 << (mantissa_shift - 1);
            int test_bit = 1 << MANTISSA_BITS_32;

            mantissa += rounding_constant;

            if ((mantissa & test_bit) > 0) { // Is this correct change?
                mantissa = 0;
                exponent++;  // XXX exponent overflow
            }
        }

        // Shift the mantissa to the right, killing the extra precision.

        mantissa >>= mantissa_shift;

        // Deal with the exponent.

//System.out.println("exponent: " + exponent + " " + " binary: " + Integer.toBinaryString(exponent));
        //printf("  exponent %d, min %d, max %d\n", exponent, exponent_min, exponent_max);
//        System.out.println("exponent: " + exponent + " min: " + exponent_min + " max: " + exponent_max);
        if (exponent < exponent_min) {
            if (exponent < exponent_min - 1)
                return 0;
            exponent = exponent_min;
            System.out.println("***Clamping to min exponent: " + exponent + " max: " + exponent_min + " number: " + f);
        }

        if (exponent > exponent_max) {
            System.out.println("***Clamping to max exponent: " + exponent + " max: " + exponent_max + " number: " + f);
            exponent = exponent_max;
        }

        exponent = (exponent - exponent_min);

//System.out.println("exponent shifted: " + exponent);
        // Put the pieces back together.

//System.out.println("sign shift: " + sign_shift + " sign: " + sign + " exp: " + Integer.toBinaryString(exponent) + " mant: " + Long.toBinaryString(mantissa));
        long result = (sign << sign_shift) | (exponent << mantissa_bits)
                   | (mantissa);

        return result;
    }

    /**
     * Decodes a bit representation to a float value.
     *
     * @param src The source bits
     * @return The float value
     */
    public float decode(long src, boolean signed) {
        if (src == 0)
            return 0.0f;

        // Mask out the mantissa, exponent, and sign fields.

        long mantissa = (src & mantissa_mask);
        int exponent = (int) (src & exponent_mask) >> mantissa_bits;
        long sign     = (src >> sign_shift);

//System.out.println("sign shift: " + sign_shift + " sign: " + sign + " exp: " + Integer.toBinaryString(exponent) + " mant: " + Long.toBinaryString(mantissa));

        // Subtract our exponent bias, then add IEEE-754's.

        exponent += exponent_min;

//System.out.println("exp: " + exponent);
        exponent += EXPONENT_BIAS_32;

        // Adjust the mantissa.

        mantissa <<= (MANTISSA_BITS_32 - mantissa_bits);

        // Assemble the pieces.

        long result;

        if (signed) {
            result = (sign << SIGN_SHIFT_32) | (exponent << MANTISSA_BITS_32)
                   | (mantissa);
        } else {
            result = (exponent << MANTISSA_BITS_32)
                   | (mantissa);
        }

        return Float.intBitsToFloat((int)result);
    }
}
