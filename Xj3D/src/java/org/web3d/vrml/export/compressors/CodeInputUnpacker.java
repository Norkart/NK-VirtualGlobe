package org.web3d.vrml.export.compressors;

/* "CodeInputUnpacker.java" */

import java.io.*;

/** Unpacks N bit codes from source bytes,
 * provided as either an array or stream.
 * N may be more or less than (or equal) to 8 bits,
 * and may be as much as 16 bits,
 * and of course must be what was actually packed.
 * <P>
 * Since code width N may be greater than 8,
 * unpacked codes are always returned as ints.
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
 * @version $Revision: 1.2 $
 */
public class CodeInputUnpacker {

    private byte [] bytes = null;

    private InputStream stream = null;

    private int ctr = 0;

    private int width = 12;

    private int accum = 0;
    private int acc_bits = 0;

    private
    int masks[] = { 0x0000,
            0x0001, 0x0003, 0x0007, 0x000F,
                    0x001F, 0x003F, 0x007F, 0x00FF,
                    0x01FF, 0x03FF, 0x07FF, 0x0FFF,
                    0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF };

/** Constructor from array of packed bytes,
 * for default code width.
 * @param bytes the data array.
 */

    public CodeInputUnpacker (byte [] bytes) { this.bytes = bytes; }

/** Constructor from input stream of packed bytes,
 * for default code width.
 * @param stream the data stream.
 */

    public CodeInputUnpacker (InputStream stream) { this.stream = stream; }

/** Constructor from array of packed bytes.
 * @param bytes the data array.
 * @param N the expected code width in bits,
 * which of course must be what was actually packed.
 */

    public CodeInputUnpacker (byte [] bytes, int N) {
        this.bytes = bytes;
        this.width = N;
    }

/** Constructor from input stream of packed bytes.
 * @param stream the data stream.
 * @param N the expected code width in bits,
 * which of course must be what was actually packed.
 */

    public CodeInputUnpacker (InputStream stream, int N) {
        this.stream = stream;
        this.width = N;
    }

    public void setWidth(int width) {
        this.width = width;
    }

/** Get next N bit code,
 * according to set or default code width,
 * which of course must be what was actually packed.
 * <P>
 * Since code width N may be greater than 8,
 * unpacked codes are always returned as ints.
 * @exception IOException possible only if based on a stream. */

    public int getN () throws IOException {

/* read bytes until we have enough bits to make a code,
 * always shifting accum left and adding bytes at right,
 * incr bits count by each byte's worth */

/* Note Java types differ from C types:
 * byte (8), short (16), int (32) are signed, there is no unsigned.
 * char (16) not (8) to support Unicode, so char is not the same as byte. */

        while (acc_bits < width) {

            if (acc_bits == 0)
        accum = 0;
            else
            accum = (accum << 8) & 0xFF00;

            int val16 = 0;

            if (null != bytes)
                val16 = bytes[ctr] & 0x00ff;
            else if (null != stream)
        val16 = stream.read () & 0x00ff;

        accum |= val16;

            acc_bits += 8;
        ctr++;
        }

/* take code from high end of the accumulator (shifted and masked),
 * decr bits count by width consumed */

        int code = (accum >> (acc_bits - width)) & masks [width];

        acc_bits -= width;

    return code;
    }

/** Get array of all N bit codes,
 * according to set or default code width,
 * which of course must be what was actually packed.
 * <P>
 * Since code width N may be greater than 8,
 * unpacked codes are always returned as ints.
 * @param maxcodes maximum number of codes expected.
 * Any codes beyond this length will be discarded!
 * @return array sized and filled with codes as unpacked.
 * @exception IOException possible only if based on a stream. */

    public int [] getNArray (int maxcodes) throws IOException {
    int [] ca = new int [maxcodes];
    int ii = 0;
    while ((ii < ca.length) && !atEnd ())
        ca [ii++] = getN ();

/* If less than expected return shorter filled array */

        if (ii < ca.length) {
        int [] cc = new int [ii];
        for (int iii = 0; iii < ii; iii++)
        cc [iii] = ca [iii];
        return cc;
    }

    return ca;
    }

/** Fill (or partly fill) a given array with N bit codes,
 * according to set or default code width,
 * which of course must be what was actually packed.
 * <P>
 * Since code width N may be greater than 8,
 * unpacked codes are always returned as ints.
 * @param codearray array to fill sized for maximum number of codes expected.
 * Any codes beyond this length will be discarded!
 * @return actual count of codes as unpacked.
 * @exception IOException possible only if based on a stream. */

    public int fillNArray (int [] codearray) throws IOException {
    int ii = 0;
    while ((ii < codearray.length) && !atEnd ())
        codearray [ii++] = getN ();

    return ii;
    }

/** Doing reset () on Unpacker means use buffer from the top,
 * the assumption is that it has different data in it now.
 * If the Unpacker was constructed on an input stream,
 * and the stream supports reset,
 * that meaning will be used. */

    public void reset () {
    ctr = 0;
        acc_bits = 0;

        if (null != stream)
        try { ((DataInputStream)stream).reset (); }
            catch (Exception ex) { }
    }

/** True when no more codes available
 * Note that for some code widths,
 * packing may result in the appearance of one or more final codes,
 * which actually are empty fill in the final byte.
 * A caller must be able to recognize these by a special value,
 * or otherwise know how many valid codes are expected.
 * @exception IOException possible only if based on a stream.  */

    public boolean atEnd () throws IOException {

/* Allowing for bits already on hand,
 * how many more bytes (if any) are needed for another code?
 * Can we get them? */

        if (acc_bits < width) {
        int bitct = acc_bits,
        need_bytes = 0;
            while (bitct < width) {
        need_bytes++;
        bitct += 8;
        }

            if (null != bytes)
                return (ctr + need_bytes > bytes.length);
            else if (null != stream)
            return (need_bytes > stream.available ());
    }
    return true;
    }

}
/* <IMG SRC="/cgi-bin/counter">*/
///////////////////////////////////////////////////////
