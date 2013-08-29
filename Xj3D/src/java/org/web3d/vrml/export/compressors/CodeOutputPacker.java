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

package org.web3d.vrml.export.compressors;

// External imports
import java.io.*;

// Local imports
// None

/**
 * Packs N bit codes into destination bytes,
 * either an array or stream.
 * N may be more or less than (or equal) to 8.
 * Packing order as shown for some widths:
 * <PRE>
 * Width = 12 x 2 codes = 8 x 3
 * |BA987654|
 * |3210/BA98|
 * |76543210|
 * Width = 10 x 4 codes = 8 x 5
 * |98765432|
 * |10/987654|
 * |3210/9876|
 * |543210/98|
 * |76543210|
 * Width = 8,
 * |76543210|
 * Width = 7 x 8 codes = 8 x 7
 * |6543210/6|
 * |543210/65|
 * |43210/654|
 * |3210/6543|
 * |210/65432|
 * |10/654321|
 * |0/6543210|
 * Width = 6 x 4 codes = 8 x 3
 * |543210/54|
 * |3210/5432|
 * |10/543210|
 * </PRE>
 * <IMG SRC="/cgi-bin/counter">
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class CodeOutputPacker {

    private byte [] bytes = null;

    private OutputStream stream = null;

    private int ctr = 0;

    /**
     * Constructor to fill a byte array, for default code width.
     *
     * @param bytes the data array
     */
    public CodeOutputPacker (byte [] bytes) {
        this.bytes = bytes;
    }

    /**
     * Constructor to make and fill a byte array, for default code width.
     *
     * @param nbytes the max expected size of data array
     */
    public CodeOutputPacker (int nbytes) {
        this.bytes = new byte [nbytes];
    }

    /**
     * Constructor to output bytes to a stream, for default code width.
     * @param stream the data stream
     */
    public CodeOutputPacker (OutputStream stream) {
        this.stream = stream;
    }

    /**
     * Constructor to fill a byte array,
     *
     * @param bytes the data array.
     * @param N the code width in bits, default 12 bits.
     */
    public CodeOutputPacker (byte [] bytes, int N) {
        this.bytes = bytes;
        this.width = N;
    }

    /**
     * Constructor to make and fill a byte array,
     *
     * @param nbytes the data array size.
     * @param N the code width in bits, default 12 bits.
     */
    public CodeOutputPacker (int nbytes, int N) {
        this.bytes = new byte [nbytes];
        this.width = N;
    }

    /**
     * Constructor to output bytes to a stream,
     *
     * @param stream the data stream.
     * @param N the code width in bits, default 12 bits
     */
    public CodeOutputPacker (OutputStream stream, int N) {
        this.stream = stream;
        this.width = N;
    }

    /**
     * Make sure any pending bits are output.
     * No further codes will be added to this output.
     * <P>
     * Note that for some code widths,
     * packing may result in the appearance of one or more final codes,
     * which actually are empty fill in the final byte.
     * A caller must be able to recognize these by a special value,
     * or otherwise know how many valid codes are expected.
     */
    public void flush () throws IOException {

        /* if / we still have pending bits,
         * that were not enough to fill a byte,
         * write a final byte (left shifted and masked) */

        if (acc_bits > 0) {

            if (null != bytes)
                bytes [ctr] = (byte)(accum << (acc_bits) & 0xff);
            else if (null != stream)
                stream.write ((byte)(accum << (acc_bits) & 0xff));

            acc_bits = 0;
        ctr++;
        }

    }

    /**
     * Put and pack an array of all N bit codes,
     * according to set or default code width.
     * <P>
     * Note that this call may (but need not) be the only source of data.
     * Either or both putN and putNArray may be called one or more times,
     * followed by a call to flush when no more data will be added.
     * @param codes the array of codes to be packed.
     * @exception IOException possible only if based on a stream.
     */
    public void putNArray (int [] codes) throws IOException {
        for (int ii = 0; ii < codes.length; ii++)
         putN (codes [ii]);
    }

    /** Doing reset () on Packer means fill buffer from the top,
     * the assumption here is that it has been written somewhere. */
    public void reset () {
        acc_bits = 0;
        ctr = 0;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Return how many bytes of packed data were written,
     * to the array or stream.
     */
    public int getCount () { return ctr; }

    /**
     * Copy to Byte Array sized for actual data contained,
     * valid only if an array was used,
     * otherwise returns null.
     * @return array sized for actual data contained. */
    public byte [] toByteArray () {
        if(null == bytes)
            return null;

        byte [] rv = new byte [ctr];
        for (int ii = 0; ii < ctr; ii++)
             rv [ii] = bytes [ii];
        return rv;
    }

    private int width = 12;

    private int accum = 0;
    private int acc_bits = 0;
    private
    int masks[] = { 0x0000,
            0x0001, 0x0003, 0x0007, 0x000F,
                    0x001F, 0x003F, 0x007F, 0x00FF,
                    0x01FF, 0x03FF, 0x07FF, 0x0FFF,
                    0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF };

    /**
     * Write or accumulate another N bit code
     */
    public void putN (int code) throws IOException {
        /* mask to only handle the claimed width */

        code &= masks [width];

        /* first or later code in a cycle */

        if (acc_bits == 0)
            accum = code;
        else
            accum = (accum << width) | code;

        /* advance bits count by width */

        acc_bits += width;

        /* if / while we have enough bits to write a complete byte,
         * write it from high end of the accumulator (shifted and masked),
         * decr how bits count by byte's worth */

        while (acc_bits >= 8) {
            if (null != bytes)
                bytes [ctr] = (byte)(accum >> (acc_bits -8) & 0xff);
            else if (null != stream)
                stream.write ((byte)(accum >> (acc_bits -8) & 0xff));

            acc_bits -= 8;
        ctr++;
        }
    }

}
