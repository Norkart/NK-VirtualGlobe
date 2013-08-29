/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// External imports
// none

// Local imports
// none

/**
 * Utility class for miscellaneous calculations, 
 * primarily used in pre-processing images.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public abstract class MathUtils {
	
    /**
     * Determine the nearest power of two value for a given argument.
     * This function uses the formal ln(x) / ln(2) = log2(x)
     *
     * @return The power-of-two-ized value
     */
    public static int nearestPowerTwo(int val, boolean imageScaleUp) {
        int log;

        if (imageScaleUp) {
            log = (int) Math.ceil(Math.log(val) / Math.log(2));
        } else {
            log = (int) Math.floor(Math.log(val) / Math.log(2));
        }

        return (int) Math.pow(2,log);
    }

    /**
     * Compute the n where 2^n = value.
     *
     * @param value The value to compute.
     */
    public static int computeLog(int value) {
        int i = 0;

        if (value == 0)
            return -1;

        for (;;) {
            if (value == 1)
                return i;
            value >>= 1;
            i++;
        }
    }
}
