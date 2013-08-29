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
package org.web3d.vrml.export.compressors;

// Standard library imports

import java.io.DataOutputStream;
import java.io.IOException;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * UnPack a stream of integers of variables bits from a packed form.
 * Loosely copied from Johnathon Blow's "Packing Integers" article.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class BitUnpacker {
    // TODO: Does this need to be a long?
    /** The number of bits remaing to be read */
    private int num_bits_remaining;

    /** The buffer to read frame */
    private byte buffer[];

    /** The next bit to read */
    private int next_bit_to_read;

    /**
     * Construct a bit unpacker.
     *
     * @param buffer The buffer to decode.
     */
    public BitUnpacker(byte[] buffer) {
        num_bits_remaining = buffer.length * 8;
        this.buffer = buffer;
        next_bit_to_read = 0;
    }

    /**
     * Unpack a number of bits.
     */
    public int unpack(int num_bits) {
        int result=0;

        if (num_bits > num_bits_remaining)
            throw new RuntimeException("Trying to read more bits then are left in BitUnpacker");

        num_bits_remaining -= num_bits;

        int sign_mask;

        if (num_bits < 32)
            sign_mask = -1 << num_bits;
        else
            sign_mask = -1 << 31;

        int byte_index = 0;
        int bit_index = 0;

        int src_mask = 0;
        int dest_mask = 0;
        boolean signed = false;
        boolean first = true;

        while(num_bits > 0) {
            byte_index = (next_bit_to_read / 8);
            bit_index = (next_bit_to_read % 8);

            src_mask = (1 << (7 - bit_index));
            dest_mask = (1 << (num_bits -1 ));

            if ((buffer[byte_index] & src_mask) != 0) {
                if (first)
                    signed = true;

                result |= dest_mask;
            }

            first = false;
//System.out.println("src: " + Integer.toBinaryString(src_mask) + " dest: " + Integer.toBinaryString(dest_mask) + " res: " + Integer.toBinaryString(result));

            num_bits--;
            next_bit_to_read++;
        }

        if (signed) {
//System.out.println("signed.  mask: " + Integer.toBinaryString(sign_mask));
            return result | sign_mask;
        }

        return result;
    }

    /**
     * Set this bit unpacker to a new value.  Resests the
     * postion to the beginning.
     */
     public void reset(byte[] buffer) {
        num_bits_remaining = buffer.length * 8;
        this.buffer = buffer;
        next_bit_to_read = 0;
     }

    /**
     * Get the number of bits remaining to be procesed.
     *
     * @return The number of bits left to process.
     */
    public long getNumBitsRemaining() {
        return num_bits_remaining;
    }

    /**
     * Small test of unpack/pack routines.
     */
    public static void main(String args[]) {
        BitPacker bp = new BitPacker(3);
        byte[] result;

        bp.pack(0,4);
        bp.pack(82,7);
        bp.pack(69,7);

        result = new byte[bp.size()];
        bp.getResult(result);

        BitUnpacker bup = new BitUnpacker(result);
        int i;

        System.out.println(bup.unpack(4));
        System.out.println(bup.unpack(7));
        System.out.println(bup.unpack(7));
    }
}
