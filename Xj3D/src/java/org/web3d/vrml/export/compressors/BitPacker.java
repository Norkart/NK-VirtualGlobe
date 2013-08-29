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
 * Pack a stream of integers of variables bits into a packed form.
 * Loosely copied from Johnathon Blow's "Packing Integers" article.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class BitPacker {
    private int next_bit_to_write;
    private byte buffer[];

    /**
     * Construct a bit packer.
     *
     * @param maxLength The maximum length in bytes of the stream to pack.
     */
    public BitPacker(int maxLength) {
        next_bit_to_write = 0;
        buffer = new byte[maxLength];
    }

    /**
     * Get the size of the result.  This will be the number of bytes
     * used, not the maximum length.
     */
    public int size() {
        return (next_bit_to_write + 7) / 8;
    }

    /**
     * Get the result of the packing.  Use size to preallocate the result array.
     */
    public void getResult(byte[] result) {
        System.arraycopy(buffer, 0, result, 0, (next_bit_to_write + 7) / 8);
    }

    /**
     * Pack this value using num_bits.
     */
    public void pack(int value, int num_bits) {
        while (num_bits > 0) {
            int byte_index = (next_bit_to_write / 8);
            int bit_index =  (next_bit_to_write % 8);

            int src_mask = (1 << (num_bits - 1));
            byte dest_mask = (byte) (1 << (7 - bit_index));

            if ((value & src_mask) != 0)
                buffer[byte_index] |= dest_mask;

            next_bit_to_write++;
            num_bits--;
        }
    }

    /**
     * Write this stream out to a stream.  Only writes
     * the bytes used, not the maximum size.
     */
    public void writeStream(DataOutputStream dos) throws IOException {
        int size = size();

        for(int i=0; i < size; i++) {
            dos.writeByte(buffer[i]);
        }
    }
}
