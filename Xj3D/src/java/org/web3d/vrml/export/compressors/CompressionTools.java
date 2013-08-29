/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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
import java.util.zip.*;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.xj3d.io.BlockDataInputStream;

/**
 * A set of general tools for compression routines.
 *
 * @author Alan Hudson
 * @version $Revision: 1.19 $
 */
public class CompressionTools {
    private static int[] bcnts = new int[40];

    private static boolean debug = false;

    private static int huffmanTablesSize;

    private static int lastTableDataLength = -1;
    private static byte[] outputBuff = new byte[1024];

    /**
     * Convert an array of floats into an array of integers.  A multiplication
     * factor 10^n is returned.
     *
     * @param fval The float data
     * @param vals The integer result.  Preallocate this.
     * @return the multipler used.
     */
    public static int convertFloatArrayToIntArray(float fval[], int[] vals) {
        double rint;
        float max=Float.MIN_VALUE;
        float min=Float.MAX_VALUE;
        float tmp;
        int cnt;
        int mult=0;

        for(int i=0; i < fval.length; i++) {
            rint = Math.rint(fval[i]);
            tmp = Math.abs((float)(fval[i] - rint));
            //System.out.println("val: " + fval[i] + " tmp: " + tmp);

            cnt=0;
            if (tmp > 0.000000001) {
                while(tmp < 1 && cnt < 8) {
                    cnt++;
                    tmp = tmp * 10.0f;
                }
            }

            if (cnt > mult)
                mult = cnt;

            if (fval[i] < min)
                min = fval[i];
            if (fval[i] > max) {
                max = fval[i];
            }
        }


        // TODO: Need to limit to avoid overflowing
        mult = (int) Math.pow(10, mult);

        for(int i=0; i < fval.length; i++) {
            vals[i] = (int) (fval[i] * (float)mult);
        }

        return mult;
    }

    /**
     * Calculate a tolerance value as a percentage of the minimum bounds.
     *
     * @param coordinates The array of coordinates.
     * @param percent The percent of the maximum coordinate range to use as an error.
     * @return The tolerance value to use.
     */
    public static float calcTolerance(float[] coordinates, float percent) {

        if (coordinates.length < 3)
            return 0.0000001f;

        float min_x = coordinates[0];
        float min_y = coordinates[1];
        float min_z = coordinates[2];

        float max_x = coordinates[0];
        float max_y = coordinates[1];
        float max_z = coordinates[2];
        int cnt;

        min_z = coordinates[2];
        max_z = coordinates[2];
        cnt = 3;
        int numCoords = coordinates.length / 3;
        for(int i = 1; i < numCoords; i++) {
            if (coordinates[cnt] < min_x)
                min_x = coordinates[cnt];
            if (coordinates[cnt] > max_x)
                max_x = coordinates[cnt];
            cnt++;

            if (coordinates[cnt] < min_y)
                min_y = coordinates[cnt];
            if (coordinates[cnt] > max_y)
                max_y = coordinates[cnt];

            cnt++;

            if (coordinates[cnt] < min_z)
                min_z = coordinates[cnt];
            if (coordinates[cnt] > max_z)
                max_z = coordinates[cnt];

            cnt++;
        }

        float xlen = Math.abs(max_x - min_x);
        float ylen = Math.abs(max_y - min_y);
        float zlen = Math.abs(max_z - min_z);
        float ret_val;

        if (xlen > ylen && xlen > zlen) {
            ret_val = xlen * percent;
        } else if (ylen > xlen && ylen > zlen) {
            ret_val = ylen * percent;
        } else {
            ret_val = zlen * percent;
        }

        if (ret_val == 0)
            ret_val = 0.00001f;

        return ret_val;
    }

    public static void quantizeFloatArray(DataOutputStream dos, float fval[], float tolerance) throws IOException {
        int exponent;
        int mantissa;
        int signNeeded = 0;

        if (fval.length == 0)
            return;

        // TODO: Can we avoid this gc
        float[] minmax = new float[3];

        findMinMax(fval,minmax);
//System.out.println("min: " + minmax[0] + " max: " + minmax[1] + " signed: " + minmax[2] + " tol: " + tolerance);
        exponent = exponentNeeded(minmax);
        mantissa = mantissaNeeded(fval, exponent, tolerance);

        /*
        // Cap to 15 to store as 4 bits
        if (mantissa > 15)
            mantissa = 15;
        */

        if (minmax[2] > 0)
            signNeeded = 1;

        int numBits = exponent + mantissa + 1;
//        int numBits = exponent + mantissa + signNeeded;
//System.out.println("Num Bits: " + numBits + " exp: " + exponent + " man: " + mantissa + " len: " + fval.length + " size: " + (fval.length * numBits / 8f));
        FloatPacker encoder = new FloatPacker(exponent, mantissa);
        int bits;
        int len = fval.length;


        BitPacker packer = new BitPacker((int)Math.ceil(len * numBits / 8.0f));
        dos.writeByte(exponent);
        dos.writeByte(mantissa);

/*
        // TODO: Increases size with gzip
        BitPacker packer = new BitPacker((int)Math.ceil(len * numBits / 8.0) + 1);
        packer.pack(exponent, 3);
        packer.pack(mantissa, 5);
        packer.pack(signNeeded, 1);

*/


        for(int i=0; i < len; i++) {
            bits = (int) encoder.encode(fval[i], true);
            packer.pack(bits, numBits);
        }

        byte[] result = new byte[packer.size()];
        packer.getResult(result);
        int size = packer.size();

        dos.writeInt(len);
        dos.write(result, 0, size);
/*
        // Error checking logic
        if (mantissa < 15) {
            System.out.println("Checking errors:");
            BitUnpacker unpacker = new BitUnpacker(result);
            FloatPacker decoder = new FloatPacker(exponent, mantissa);

            long val;

            float err;
            float ans;
            int errNum=0;

            for(int i=0; i < len; i++) {
                val = unpacker.unpack(numBits);
                ans = decoder.decode(val, true);

                err = Math.abs(ans - fval[i]);
                if (err > tolerance) {
                    errNum++;
                    System.out.println("Err:  idx: " + i + " orig: " + fval[i] + " enc: " + ans + " err: " + err);
                }
            }

            if (errNum > 0) {
System.out.println("Retesting:");
                debug = true;
                mantissa = mantissaNeeded(fval, exponent, tolerance);
                debug = false;
            }
        }
*/
    }

    public static float[] dequantizeFloatArray(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);


        byte exponent = (byte) dis.read();
        byte mantissa = (byte) dis.read();

        int numFloats = dis.readInt();
        boolean signed = true;

/*
        byte[] headerBytes = new byte[1];
        dis.read(headerBytes);

        BitUnpacker header = new BitUnpacker(headerBytes);
        int exponent = header.unpack(3);
        int mantissa = header.unpack(4);
        int signNeeded = header.unpack(1);


        if (signNeeded > 0)
            signed = true;
        else
            signed = false;

*/
        int numBits = exponent + mantissa + 1;
//        int numBits = exponent + mantissa + signNeeded;
//System.out.println("Num Bits: " + numBits + " exp: " + exponent + " man: " + mantissa + " signed: " + signNeeded);

        int numBytes = (int)Math.ceil(numFloats * numBits / 8.0f);

        byte[] buff = new byte[numBytes];

        dis.read(buff);

        BitUnpacker unpacker = new BitUnpacker(buff);

        FloatPacker decoder = new FloatPacker(exponent, mantissa);

//System.out.println("Len: " + numBytes + " Num Floats: " + numFloats + " numBits: " + numBits);
        float[] result = new float[numFloats];
        long val;

        boolean even = false;
        for(int i=0; i < numFloats; i++) {
            val = unpacker.unpack(numBits);
            result[i] = decoder.decode(val, signed);
/*
if (even) {
   System.out.println(result[i]);
   even = false;
} else {
   even = true;
   System.out.print(result[i] + " ");
}
*/
        }
        return result;
    }

    /**
     * Quantize a short vector.  The length will not be stored.
     * The (exponent + mantissa + signed) * len must be a multiple of 8.
     *
     * @param dos The output stream
     * @param exponent How many bits to store for the exponent
     * @param mantissa How many bits to store for the mantissa
     * @param signed Is the number sometimes negative
     * @param fval The vector
     */
    public static void quantizeVector(DataOutputStream dos, int exponent, int mantissa, boolean signed, float fval[]) throws IOException {
        int numBits = exponent + mantissa;
        if (signed) numBits++;

        FloatPacker encoder = new FloatPacker(exponent, mantissa);
        int bits;
        int len = fval.length;

        BitPacker packer = new BitPacker((int)Math.ceil(len * numBits / 8.0));

        for(int i=0; i < len; i++) {
            bits = (int) encoder.encode(fval[i], true);
            packer.pack(bits, numBits);
        }

        byte[] result = new byte[packer.size()];
        packer.getResult(result);

        // Assume this will be written out at the beginning of the file
/*
        dos.writeByte(exponent);
        dos.writeByte(mantissa);
*/
        int size = packer.size();

        dos.write(result, 0, size);
    }

    /**
     * Dequantize a color vector.  The (exponent + mantissa + sign) * len must be a multiple of 8.
     *
     * @param is The input stream
     * @param exponent How many bits to store for the exponent
     * @param mantissa How many bits to store for the mantissa
     * @param signed Is the number sometimes negative
     * @param len The length of the vector.
     * @return The decoded vector
     */
    public static float[] dequantizeVector(InputStream is,
                                           int exponent,
                                           int mantissa,
                                           boolean signed,
                                           int len)
        throws IOException {

        DataInputStream dis = new DataInputStream(is);

        int numBits = exponent + mantissa;
        if (signed) numBits++;
        FloatPacker decoder = new FloatPacker(exponent, mantissa);

        //        int numBytes = (int)Math.ceil((len * 8 / numBits));
        int numBytes = (int)Math.ceil((len * numBits / 8));

        byte[] buff = new byte[numBytes];
        int numFloats = (int) Math.floor(numBytes * 8 / (float) numBits);

        float[] result = new float[numFloats];

        dis.read(buff);

        BitUnpacker unpacker = new BitUnpacker(buff);
        long val;

        for(int i=0; i < numFloats; i++) {
            val = unpacker.unpack(numBits);
            result[i] = decoder.decode(val, signed);
        }

        return result;
    }

    /**
     * Compress an array of floats.
     *
     * @param dos The stream to write the results.
     * @param delta Whether to use delta encoding.
     * @param span The repeating frequency of data, a MFVec3f would be a span of 3.
     * @param fval The float array to compress
     * @throws IOException on an IO error
     */
    public static void compressFloatArray(DataOutputStream dos, boolean delta, int span, float fval[]) throws IOException {
        int[] vals = new int[fval.length];

        double rint;
        float max=Float.MIN_VALUE;
        float min=Float.MAX_VALUE;
        float tmp;
        int cnt;
        int mult=0;

        for(int i=0; i < fval.length; i++) {
            rint = Math.rint(fval[i]);
            tmp = Math.abs((float)(fval[i] - rint));

            cnt=0;
            if (tmp > 0.000000001) {
                while(tmp < 1 && cnt < 8) {
                    cnt++;
                    tmp = tmp * 10.0f;
                }
            }

            if (cnt > mult)
                mult = cnt;

            if (fval[i] < min)
                min = fval[i];
            if (fval[i] > max) {
                max = fval[i];
            }
        }

        if (mult > 8)
            mult = 8;

        dos.writeByte(mult);
        mult = (int) Math.pow(10, mult);

        for(int i=0; i < fval.length; i++) {
            vals[i] = (int) (fval[i] * (float)mult);

        }
/*
        // Calculate Error
        float dcvals[] = new float[fval.length];
        float max_error=0;
        float max_percent=0;
        float error;
        float percent;

        for(int i=0; i < fval.length; i++) {
            dcvals[i] = vals[i] / ((float)mult);
            error = Math.abs(dcvals[i] - fval[i]);
            if (error > max_error)
                max_error = error;

            //System.out.println("dcval: " + dcvals[i] + " orig: " + fval[i] + " error: " + error);

            percent = error / fval[i];
            if (percent > max_percent) {
                max_percent = percent * 100;
                //System.out.println("NewMax: " + fval[i] + " enc: " + dcvals[i] + " error: " + error + " percent: " + percent);
            }
        }

        //System.out.println("max_error: " + max_error + " max_percent: " + max_percent + "%" + " min: " + min + " max: " + max);
*/

        rangeCompressIntArray(dos,delta,span,vals);
    }

    /**
     * Decompress a stream into an array of floats.  This stream must of been encoded
     * via the compressFloatArray call.
     *
     * @param dis The stream to read.
     * @param delta Whether to use delta encoding.
     * @param span The repeating frequency of data, a MFVec3f would be a span of 3.
     * @return The float array decompressed.
     * @throws IOException on an IO error
     */

    public static float[] decompressFloatArray(InputStream dis, boolean delta, int span) throws IOException {
        double rint;
        int mult = dis.read();
        mult = (int) Math.pow(10, mult);

        int[] vals = rangeDecompressIntArray(dis,delta,span);

        float[] fvals = new float[vals.length];

        int len = vals.length;
        for(int i=0; i < len; i++) {
            fvals[i] = (vals[i]  / (float) mult);
        }

        return fvals;
    }

    public static int calcRange(boolean delta, int span, int[] ival) {
        int[] dval = new int[ival.length];
        int max=Integer.MIN_VALUE;
        int min=Integer.MAX_VALUE;
        int[] lastVal = new int[span];
        int scnt=0;
        if (delta) {
            for(int i=0; i < span; i++) {
                lastVal[i] = ival[i];
            }
            int hold;
            for(int i=0; i < ival.length; i++) {
                //System.out.println("orig: " + ival[i] + " last: " + lastVal[scnt] + " delta: " + (ival[i] - lastVal[scnt]));
                hold = ival[i];
                dval[i] = ival[i] - lastVal[scnt];
                lastVal[scnt++] = hold;
                if (scnt == span)
                    scnt=0;
            }

            for(int i=0; i < dval.length; i++) {
                if (dval[i] > max) {
                    max = dval[i];
                }

                if (dval[i] < min)
                    min = dval[i];
            }
        } else {
            for(int i=0; i < ival.length; i++) {
                if (ival[i] > max) {
                    max = ival[i];
                }

                if (ival[i] < min)
                    min = ival[i];
            }
        }

        int bits = computeBits(max - min);
        //System.out.println("Min: " + min + " Max: " + max + " bits: " + bits);

        return bits;
    }

    public static void quantizeFloatArrayHuffman(DataOutputStream dos, float fval[], float tolerance) throws IOException {
        int exponent;
        int mantissa;
        int signNeeded = 0;

        if (fval.length == 0)
            return;

        // TODO: Can we avoid this gc
        float[] minmax = new float[3];

        findMinMax(fval,minmax);
//System.out.println("min: " + minmax[0] + " max: " + minmax[1] + " signed: " + minmax[2] + " tol: " + tolerance);
        exponent = exponentNeeded(minmax);
        mantissa = mantissaNeeded(fval, exponent, tolerance);

        /*
        // Cap to 15 to store as 4 bits
        if (mantissa > 15)
            mantissa = 15;
        */

        if (minmax[2] > 0)
            signNeeded = 1;

        int numBits = exponent + mantissa + 1;
//        int numBits = exponent + mantissa + signNeeded;
//System.out.println("Num Bits: " + numBits + " exp: " + exponent + " man: " + mantissa + " len: " + fval.length + " size: " + (fval.length * numBits / 8f));
        FloatPacker encoder = new FloatPacker(exponent, mantissa);
        int bits;
        int len = fval.length;


        dos.writeByte(exponent);
        dos.writeByte(mantissa);

        HuffmanTable table = new HuffmanTable();
        byte[] result;
        BitPacker packer;
        IntegerHuffmanNode node;
        int span = 0;
        int shift = 0;

        int[] data = new int[len];

        // Calculate shift
        int min = (int)encoder.encode(fval[0], true);
        int max = min;
//System.out.println("float vals");
        for(int i=0; i < len; i++) {
            data[i] = (int)encoder.encode(fval[i], true);
//if (i<10) System.out.print(fval[i] + "=" + data[i] + " ");

            if (data[i] < min)
                min = data[i];
            else if (data[i] > max)
                max = data[i];
        }
//System.out.println();

        shift = -min;

        max = max + shift;
        min = 0;

//System.out.print("Shifted vals:");
        // Apply shift and initialize huffman table
        for(int i=0; i < len; i++) {
            data[i] += shift;
//if(i < 10) System.out.print(data[i] + " ");
            node = new IntegerHuffmanNode(data[i]);
            table.addEntry(node);
        }
//System.out.println();
        table.computeTags();

//        int dataLength = computeBits(max - min);
        int dataLength = numBits;

        writeIntHuffman(dos, table, span, shift, dataLength,data, false);
    }

    public static float[] dequantizeFloatArrayHuffman(InputStream is) throws IOException {

        DataInputStream dis = new DataInputStream(is);


        byte exponent = (byte) dis.read();
        byte mantissa = (byte) dis.read();
        int numBits = exponent + mantissa + 1;

        int[] vals = decompressIntArrayDeltaHuffman(dis);

//System.out.println("bytes:  len: " + vals.length + " numBits: " + numBits + " maxBytes: " + Math.ceil((vals.length * numBits / 8f)));

        BitPacker packer = new BitPacker((int)Math.ceil(vals.length * numBits / 8f));

//System.out.println("post huffman");
        for(int i=0; i < vals.length; i++) {
//if (i < 10) System.out.print(vals[i] + " ");
            packer.pack(vals[i], numBits);
        }
//System.out.println();
        byte[] buff = new byte[packer.size()];
        packer.getResult(buff);

        int numFloats = vals.length;
        boolean signed = true;

//        int numBits = exponent + mantissa + signNeeded;
//System.out.println("Num Bits: " + numBits + " exp: " + exponent + " man: " + mantissa + " signed: " + signNeeded);

        BitUnpacker unpacker = new BitUnpacker(buff);

        FloatPacker decoder = new FloatPacker(exponent, mantissa);

//System.out.println("Len: " + numBytes + " Num Floats: " + numFloats + " numBits: " + numBits);
        float[] result = new float[numFloats];
        long val;

        boolean even = false;
//System.out.println("Result: numBits: " + numBits + " numFloats: " + numFloats);
        for(int i=0; i < numFloats; i++) {
            val = unpacker.unpack(numBits);
            result[i] = decoder.decode(val, signed);
//   if(i < 10) System.out.print(val + "=" + result[i] + " ");
        }
//System.out.println();
        return result;
    }

    public static void quantizeFloatArrayLZW(DataOutputStream dos, float fval[], float tolerance) throws IOException {
        int exponent;
        int mantissa;
        int signNeeded = 0;

        // TODO: Can we avoid this gc
        float[] minmax = new float[3];

        findMinMax(fval,minmax);
//System.out.println("min: " + minmax[0] + " max: " + minmax[1] + " signed: " + minmax[2] + " tol: " + tolerance);
        exponent = exponentNeeded(minmax);
        mantissa = mantissaNeeded(fval, exponent, tolerance);

        /*
        // Cap to 15 to store as 4 bits
        if (mantissa > 15)
            mantissa = 15;
        */

        if (minmax[2] > 0)
            signNeeded = 1;

        int numBits = exponent + mantissa + 1;
//        int numBits = exponent + mantissa + signNeeded;
//System.out.println("Num Bits: " + numBits + " exp: " + exponent + " man: " + mantissa + " len: " + fval.length + " size: " + (fval.length * numBits / 8f));
        FloatPacker encoder = new FloatPacker(exponent, mantissa);
        int bits;
        int len = fval.length;


        dos.writeByte(exponent);
        dos.writeByte(mantissa);

        BitPacker packer = new BitPacker((int)Math.ceil(len * numBits / 8f));
        int data;
        for(int i=0; i < len; i++) {
            data = (int)encoder.encode(fval[i], true);
            packer.pack(data, numBits);
        }

        byte[] vals = new byte[packer.size()];
        packer.getResult(vals);
/*
System.out.println("Pre LZW Data:  size: " + vals.length);
for(int i=0; i < vals.length; i++) {
if(i < 20)    System.out.print(vals[i] + " ");
}
System.out.println();
*/
        ByteArrayInputStream bais
         = new ByteArrayInputStream(vals);

        // TODO: Guess on a possible expansion of data
        int maxSize;

        if (vals.length < 50)
            maxSize = 250;
        else
            maxSize = (int) (vals.length * 2.5f);

        CodeOutputPacker baos = new CodeOutputPacker (maxSize,16);

        int nComp, nExp;

        nComp = LZW.Compress (bais, baos);
        byte[] cbits = baos.toByteArray();
/*
System.out.println("Post LZW Data: size: " + cbits.length);
for(int i=0; i < cbits.length; i++) {
if(i < 10)    System.out.print(cbits[i] + " ");
}
System.out.println();
*/
        dos.writeInt(cbits.length);
        dos.writeInt(len);
        dos.write(cbits);
        System.out.println("compressed size: " + cbits.length + " orig: " + nComp);

    }

    public static void quantizeFloatArrayDeflater(DataOutputStream dos, float fval[], float tolerance) throws IOException {
        int exponent;
        int mantissa;
        int signNeeded = 0;
/*
        // TODO: Can we avoid this gc
        float[] minmax = new float[3];

        findMinMax(fval,minmax);
//System.out.println("min: " + minmax[0] + " max: " + minmax[1] + " signed: " + minmax[2] + " tol: " + tolerance);
        exponent = exponentNeeded(minmax);
        mantissa = mantissaNeeded(fval, exponent, tolerance);

*/
        int[] output = new int[3];
        findFloatParams(fval, tolerance, output);
        exponent = output[0];
        mantissa = output[1];
        signNeeded = output[2];

        int numBits = exponent + mantissa + 1;

        FloatPacker encoder = new FloatPacker(exponent, mantissa);
        int bits;
        int len = fval.length;

        dos.writeByte(exponent);
        dos.writeByte(mantissa);

        BitPacker packer = new BitPacker((int)Math.ceil(len * numBits / 8f));
        int data;
        float test;
        boolean validate = true;

        for(int i=0; i < len; i++) {
            data = (int) encoder.encode(fval[i], false);

            if (validate == true) {
                test = encoder.decode(data, true);

                if (Math.abs(fval[i] - test) > tolerance)
                   System.out.println("*** error on: " + fval[i] + " = " + test + " exp: " + exponent + " mantissa: " + mantissa);
            }

            packer.pack(data, numBits);
        }

        byte[] vals = new byte[packer.size()];
        packer.getResult(vals);

        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION, false);
        compresser.setInput(vals);
        compresser.finish();

        dos.writeInt(vals.length);
        dos.writeInt(len);

        int compressedDataLength;
        boolean more = true;

        while(more) {
            compressedDataLength = compresser.deflate(outputBuff);
            if (compressedDataLength != outputBuff.length)
                more = false;
            dos.write(outputBuff, 0, compressedDataLength);
        }
    }

    public static float[] dequantizeFloatArrayInflater(byte[] data, int start, int length) throws IOException {

        byte exponent = data[start++];
        byte mantissa = data[start++];
        int len = readInt(data, start);
        start += 4;
        int numFloats = readInt(data,start);
        start += 4;
        int numBits = exponent + mantissa + 1;

        byte[] temp_result = null;
        try {
            Inflater decompresser = new Inflater(false);
            decompresser.setInput(data, start, length - 10);
            temp_result = new byte[len];
            int resultLength = decompresser.inflate(temp_result);
            decompresser.end();

        } catch(Exception e) {
            System.out.println("Invalid format in dequantizeFloatArrayInflater");
            e.printStackTrace();
        }

/*
        BitPacker packer = new BitPacker((int)Math.ceil(data.length * numBits / 8f));
        int ival;

System.out.println("post lzw bits: numBits: " + numBits);
        for(int i=0; i < data.length / 4; i++) {
if (i < 10) System.out.print(data[i*4] + " " + data[i*4+1] + " " + data[i*4+2] + " " + data[i*4+3] + " ");
            ival = (data[i*4] << 24) | (data[i*4+1] << 16) | (data[i*4+2] << 8) | (data[i*4+3]);
            packer.pack(ival, numBits);
        }
System.out.println();
        byte[] pbuff = new byte[packer.size()];
        packer.getResult(pbuff);
        BitUnpacker unpacker = new BitUnpacker(pbuff);
*/
        BitUnpacker unpacker = new BitUnpacker(temp_result);

        FloatPacker decoder = new FloatPacker(exponent, mantissa);

//System.out.println("Len: " + len + " Num Floats: " + numFloats + " numBits: " + numBits);
        float[] result = new float[numFloats];
        long val;

//System.out.println("num_floats: " + numFloats + " numBits: " + numBits + " packer: " + len + " cd: " + (length-10));
        for(int i=0; i < numFloats; i++) {
            val = unpacker.unpack(numBits);
//System.out.println(val);
            result[i] = decoder.decode(val, true);
//System.out.println(val + " = " + result[i]);
        }
        return result;
    }

    public static float[] dequantizeFloatArrayLZW(InputStream is) throws IOException {

        DataInputStream dis = new DataInputStream(is);

        byte[] buff = new byte[4];

        byte exponent = (byte) dis.read();
        byte mantissa = (byte) dis.read();
        int len = readInt(dis, buff);
        int numFloats = readInt(dis, buff);

        int numBits = exponent + mantissa + 1;

        byte[] cbits = new byte[len];
        dis.read(cbits);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int nExp = LZW.Expand (new CodeInputUnpacker(cbits,16), baos);

        byte[] data = baos.toByteArray();

        System.out.println("post lzw size: " + data.length);
/*
        BitPacker packer = new BitPacker((int)Math.ceil(data.length * numBits / 8f));
        int ival;

System.out.println("post lzw bits: numBits: " + numBits);
        for(int i=0; i < data.length / 4; i++) {
if (i < 10) System.out.print(data[i*4] + " " + data[i*4+1] + " " + data[i*4+2] + " " + data[i*4+3] + " ");
            ival = (data[i*4] << 24) | (data[i*4+1] << 16) | (data[i*4+2] << 8) | (data[i*4+3]);
            packer.pack(ival, numBits);
        }
System.out.println();
        byte[] pbuff = new byte[packer.size()];
        packer.getResult(pbuff);
        BitUnpacker unpacker = new BitUnpacker(pbuff);
*/
        BitUnpacker unpacker = new BitUnpacker(data);

        FloatPacker decoder = new FloatPacker(exponent, mantissa);

//System.out.println("Len: " + len + " Num Floats: " + numFloats + " numBits: " + numBits);
        float[] result = new float[numFloats];
        long val;

        boolean even = false;
//System.out.println("Result: numBits: " + numBits + " numFloats: " + numFloats);
        for(int i=0; i < numFloats; i++) {
            val = unpacker.unpack(numBits);
            result[i] = decoder.decode(val, true);
//if(i < 10)
//    System.out.print(result[i] + " ");
        }
//System.out.println();
        return result;
    }

    /**
     * Compress an integer array.  Uses delta encoding if applicable.  Huffman encodes the result.
     *
     * @param dos The stream to write to
     * @param data The integer array to encode.
     */
    public static void compressIntArrayDeltaHuffman(DataOutputStream dos, int[] data) throws IOException {
        int len = data.length;
        BitPacker packer;
        byte[] result;
        boolean removeMarkers = false;

        // Less the 20 its not worth dealing with Huffman
        if (len < 2) {
//System.out.println("Fallback huffman method");
            packer = new BitPacker(8);
            packer.pack(1, 0);      // We are not using huffman
            packer.pack(0, 7);   // More then we need likely

            result = new byte[1];
            packer.getResult(result);

            dos.write(result, 0, 1);

            dos.writeInt(len);

            for(int i=0; i < len; i++) {
                dos.writeInt(data[i]);
            }

            return;
        }

        HuffmanTable table = new HuffmanTable();
        IntegerHuffmanNode node;
        int max;
        int min;

        // Sniff for delta span.  Only check
        int span=-1;
        int cnt=0;
        int checkLen;
        int shift=0;

        if (len < 20)
            checkLen = len;
        else
            checkLen = 20;

        for(int i=0; i < checkLen; i++) {
            cnt++;
            if (data[i] == -1) {
                if (span == -1) {
                    span = cnt;
                    cnt = 0;
                }
                else if (span != cnt) {
                    span = -1;
                    break;
                } else {
                    cnt = 0;
                }
            }
        }

        if (span == -1) {
            span = 0;
            min = data[0];
            max = data[0];

            for(int i=0; i < len; i++) {
                node = new IntegerHuffmanNode(data[i]);
                if (data[i] > max)
                    max = data[i];
                else if (data[i] < min)
                    min = data[i];

                table.addEntry(node);
            }


            shift = -min;
            min = 0;
            max = max + shift;

            for(int i=0; i < len; i++) {
                data[i] += shift;
                node = new IntegerHuffmanNode(data[i]);
                table.addEntry(node);
            }

            table.computeTags();

//            System.out.println("Regular Size: " + table.streamBits());
        } else {
            int scnt=0;
            int hold;
            int[] delts = new int[len];

            min = data[0];
            max = data[0];

            scnt = 0;
            int[] lastVal = new int[span];

            int[] newDelts = delts;
            int idx = 0;

            if (removeMarkers) {
                newDelts = new int[delts.length * (span-1) / span];
            }

//            System.out.println("delta values:  span:" + span);
            for(int i=0; i < len; i++) {
                hold = data[i];
                delts[i] = data[i] - lastVal[scnt];
//if (i < 26) System.out.print(delts[i] + " ");
                if (delts[i] > max)
                    max = delts[i];
                else if (delts[i] < min)
                    min = delts[i];

                lastVal[scnt++] = hold;
                if (scnt == span)
                    scnt=0;

                if (removeMarkers && ((i+1) % span != 0)) {
                    newDelts[idx++] = delts[i];
                }

            }

            if (removeMarkers) {
//System.out.println("new delts size: " + newDelts.length + " orig: " + delts.length);
                delts = newDelts;
                len = delts.length;
            }

            // Shift up to postive numbers

            shift = -min;
            min = 0;

            max = max + shift;
//System.out.println("Shifted vals:");
            for(int i=0; i < len; i++) {
                delts[i] += shift;
//System.out.print(delts[i] + " ");
            }
//System.out.println();

            HuffmanTable deltaTable = new HuffmanTable();

            for(int i=0; i < len; i++) {
                node = new IntegerHuffmanNode(delts[i]);
                deltaTable.addEntry(node);
            }
            deltaTable.computeTags();

//            System.out.println("Span: " + span + " Delta Size: " + (deltaTable.streamBits() / 8) + " max: " + max);
            table = deltaTable;
            data = delts;
        }

        int dataLength = computeBits(max - min);

        lastTableDataLength = dataLength;
        writeIntHuffman(dos, table, span, shift, dataLength,data, false);
    }

    /**
     * Decompress an integer array.  Uses delta encoding if applicable.  Huffman encodes the result.
     *
     * @param dis The stream to write to
     * @return The integer array with the decompressed info
     */
    public static int[] decompressIntArrayDeltaHuffman(InputStream dis) throws IOException {
        byte[] buff = new byte[4];
        byte[] header = new byte[1];
        int data[];

        dis.read(header);
        BitUnpacker unpacker = new BitUnpacker(header);
        int useHuffman = unpacker.unpack(1);
        byte span = (byte) unpacker.unpack(7);

        // Less the 20 its not worth dealing with Huffman
        if (useHuffman == 0) {
            int len = readInt(dis, buff);
//System.out.println("fallback huffman: len: " + len);
            data = new int[len];

            for(int i=0; i < len; i++) {
                data[i] = readInt(dis, buff);
//System.out.print(data[i] + " ");
            }

            return data;
        }

        int shift = readInt(dis, buff);
        int numInts = readInt(dis,buff);

        HuffmanTable table = new HuffmanTable();
        table.readDict(dis);
        int len = readInt(dis, buff);

//System.out.println("DecompressIntHuff Span: " + span + " shift: " + shift + " len: " + len + " numInts: " + numInts);
        byte[] result = new byte[len];
        dis.read(result);

        data = new int[numInts];
        IntegerHuffmanNode node;

        table.decode(result, data);

        if (span == 0) {

//System.out.println("decoded huffman");
            // Apply shift and return
            for(int i=0; i < numInts; i++) {
//if(i < 10) System.out.print(data[i] + "~" + (data[i] - shift) + " ");
                data[i] -= shift;
            }
            return data;
        }

        int[] lastVal = new int[span];

        for(int i=0; i < span; i++) {
            lastVal[i] = data[i] - shift;
            data[i] = lastVal[i];
        }

        int scnt=0;
//System.out.println("decoded huffman:");
        for(int i=span; i < numInts; i++) {
            data[i] = data[i] + lastVal[scnt] - shift;
//if (i < 10) System.out.print(data[i] + " ");
            lastVal[scnt++] = data[i];
            if (scnt == span)
                scnt=0;
        }

        return data;
    }

    /**
     * Compress a byte array.  Uses delta encoding if applicable.  Huffman encodes the result.
     *
     * @param dos The stream to write to
     * @param data The integer array to encode.
     */
    public static void compressByteArrayDeltaHuffman(DataOutputStream dos, byte[] data) throws IOException {
        int len = data.length;
        BitPacker packer;
        byte[] result;

        int streamPos = dos.size();

        // Less the 20 its not worth dealing with Huffman
        if (len < 2) {
            packer = new BitPacker(8);
            packer.pack(1, 0);      // We are not using huffman
            packer.pack(0, 7);   // More then we need likely

            result = new byte[1];
            packer.getResult(result);
            dos.write(result, 0, 1);

            dos.writeInt(len);
            for(int i=0; i < len; i++) {
                dos.write(data[i]);
            }

            return;
        }

        HuffmanTable table = new HuffmanTable();
        ByteHuffmanNode node;
        int max;
        int min;

        // Sniff for delta span.  Only check
        int span=-1;
        int cnt=0;
        int checkLen;
        int shift=0;

        if (len < 20)
            checkLen = len;
        else
            checkLen = 20;

        for(int i=0; i < checkLen; i++) {
            cnt++;
            if (data[i] == -1) {
                if (span == -1) {
                    span = cnt;
                    cnt = 0;
                }
                else if (span != cnt) {
                    span = -1;
                    break;
                } else {
                    cnt = 0;
                }
            }
        }

        if (span == -1) {
            span = 0;
            min = data[0];
            max = data[0];

            for(int i=0; i < len; i++) {
                node = new ByteHuffmanNode(data[i]);
                if (data[i] > max)
                    max = data[i];
                else if (data[i] < min)
                    min = data[i];

                table.addEntry(node);
            }

            shift = -min;
            max = max + shift;

            for(int i=0; i < len; i++) {
                if (data[i] + shift > 255) {
                    System.out.println("Overflow in byte shift");
                }

                data[i] += shift;
                node = new ByteHuffmanNode(data[i]);
                table.addEntry(node);
            }

            table.computeTags();

            System.out.println("Regular Size: " + table.streamBits());
        } else {
            int scnt=0;
            int hold;
            byte[] delts = new byte[len];

            min = data[0];
            max = data[0];

            scnt = 0;
//System.out.println("delta values:");
            int[] lastVal = new int[span];
            for(int i=0; i < len; i++) {
                hold = data[i];
                delts[i] = (byte)(data[i] - lastVal[scnt]);
//System.out.print(data[i] + "=" + delts[i] + " ");
                if (delts[i] > max)
                    max = delts[i];
                else if (delts[i] < min)
                    min = delts[i];

                lastVal[scnt++] = hold;
                if (scnt == span)
                    scnt=0;
            }

            // Shift up to postive numbers

            shift = -min;
            max = max + shift;
//System.out.println("Shifted vals:");
            for(int i=0; i < len; i++) {
                if (delts[i] + shift > 255) {
                    System.out.println("Overflow in byte shift");
                }
                delts[i] += shift;
//System.out.print(delts[i] + " ");
            }
//System.out.println();
            HuffmanTable deltaTable = new HuffmanTable();

            for(int i=0; i < len; i++) {
                node = new ByteHuffmanNode((byte)delts[i]);
                deltaTable.addEntry(node);
            }
            deltaTable.computeTags();

//            System.out.println("Span: " + span + " Delta Size: " + deltaTable.streamBits() / 8);
            table = deltaTable;
            data = delts;
        }

        packer = new BitPacker(8);
        packer.pack(1, 1);      // We are using huffman
        packer.pack(span, 7);   // More then we need likely

        result = new byte[1];
        packer.getResult(result);
        dos.write(result, 0, 1);

        dos.write((byte)shift);
        dos.writeInt(len);
        int bits = computeBits(max - min);
        table.setDataLength(bits);
        table.writeDict(dos);
        packer = new BitPacker(table.streamBits());
        ByteHuffmanNode inode = new ByteHuffmanNode();

        for(int i=0; i < len; i++) {
            inode.setValue(data[i]);

            node = (ByteHuffmanNode) table.getEntry(inode);
//System.out.println("tag: " + node.tag + " len: " + node.tagLength + " val: " + node.getValue());
            packer.pack(node.tag, node.tagLength);
        }

        result = new byte[packer.size()];
        packer.getResult(result);
        int size = packer.size();
        dos.writeInt(size);
        dos.write(result, 0, size);
//System.out.println("encoded size: " + size + " streamSize: " + (dos.size() - streamPos));
    }

    /**
     * Compress a byte array.  Uses delta encoding if applicable.  Huffman encodes the result.
     *
     * @param dos The stream to write to
     * @param data The integer array to encode.
     */
    public static void compressShortArrayDeltaHuffman(DataOutputStream dos, short[] data) throws IOException {
        int len = data.length;
        BitPacker packer;
        byte[] result;

        int streamPos = dos.size();

        // Less the 20 its not worth dealing with Huffman
        if (len < 2) {
            packer = new BitPacker(8);
            packer.pack(1, 0);      // We are not using huffman
            packer.pack(0, 7);   // More then we need likely

            result = new byte[1];
            packer.getResult(result);
            dos.write(result, 0, 1);

            dos.writeInt(len);
            for(int i=0; i < len; i++) {
                dos.write(data[i]);
            }

            return;
        }

        HuffmanTable table = new HuffmanTable();
        ShortHuffmanNode node;
        int max;
        int min;

        // Sniff for delta span.  Only check
        int span=-1;
        int cnt=0;
        int checkLen;
        int shift=0;

        if (len < 20)
            checkLen = len;
        else
            checkLen = 20;

        for(int i=0; i < checkLen; i++) {
            cnt++;
            if (data[i] == -1) {
                if (span == -1) {
                    span = cnt;
                    cnt = 0;
                }
                else if (span != cnt) {
                    span = -1;
                    break;
                } else {
                    cnt = 0;
                }
            }
        }

        if (span == -1) {
            span = 0;
            min = data[0];
            max = data[0];

            for(int i=0; i < len; i++) {
                node = new ShortHuffmanNode(data[i]);
                if (data[i] > max)
                    max = data[i];
                else if (data[i] < min)
                    min = data[i];

                table.addEntry(node);
            }

            shift = -min;
            max = max + shift;

            for(int i=0; i < len; i++) {
                if (data[i] + shift > 65535) {
                    System.out.println("Overflow in short shift");
                }

                data[i] += shift;
                node = new ShortHuffmanNode(data[i]);
                table.addEntry(node);
            }

            table.computeTags();

//            System.out.println("Regular Size: " + table.streamBits());
        } else {
            int scnt=0;
            int hold;
            short[] delts = new short[len];

            min = data[0];
            max = data[0];

            scnt = 0;
//System.out.println("delta values:");
            int[] lastVal = new int[span];
            for(int i=0; i < len; i++) {
                hold = data[i];
                delts[i] = (byte)(data[i] - lastVal[scnt]);
//System.out.print(data[i] + "=" + delts[i] + " ");
                if (delts[i] > max)
                    max = delts[i];
                else if (delts[i] < min)
                    min = delts[i];

                lastVal[scnt++] = hold;
                if (scnt == span)
                    scnt=0;
            }

            // Shift up to postive numbers

            shift = -min;
            max = max + shift;
//System.out.println("Shifted vals:");
            for(int i=0; i < len; i++) {
                if (delts[i] + shift > 65536) {
                    System.out.println("Overflow in byte shift");
                }
                delts[i] += shift;
//System.out.print(delts[i] + " ");
            }
//System.out.println();
            HuffmanTable deltaTable = new HuffmanTable();

            for(int i=0; i < len; i++) {
                node = new ShortHuffmanNode((byte)delts[i]);
                deltaTable.addEntry(node);
            }
            deltaTable.computeTags();

//            System.out.println("Span: " + span + " Delta Size: " + deltaTable.streamBits() / 8);
            table = deltaTable;
            data = delts;
        }

        packer = new BitPacker(8);
        packer.pack(1, 1);      // We are using huffman
        packer.pack(span, 7);   // More then we need likely

        result = new byte[1];
        packer.getResult(result);
        dos.write(result, 0, 1);

        dos.writeShort((short)shift);
        dos.writeInt(len);
        int bits = computeBits(max - min);
        table.setDataLength(bits);
        table.writeDict(dos);
        packer = new BitPacker(table.streamBits());
        ShortHuffmanNode inode = new ShortHuffmanNode();

        for(int i=0; i < len; i++) {
            inode.setValue(data[i]);

            node = (ShortHuffmanNode) table.getEntry(inode);
//System.out.println("tag: " + node.tag + " len: " + node.tagLength + " val: " + node.getValue());
            packer.pack(node.tag, node.tagLength);
        }

        result = new byte[packer.size()];
        packer.getResult(result);
        int size = packer.size();
        dos.writeInt(size);
        dos.write(result, 0, size);
//System.out.println("encoded size: " + size + " streamSize: " + (dos.size() - streamPos));
    }

    private static void writeIntHuffman(DataOutputStream dos, HuffmanTable table, int span, int shift, int bits, int[] data, boolean deltaTable) throws IOException {
        int len = data.length;
        BitPacker packer;
        byte[] result;
        IntegerHuffmanNode node;

        packer = new BitPacker(8);
        packer.pack(1, 1);      // We are using huffman
        packer.pack(span, 7);   // More then we need likely

        result = new byte[1];
        packer.getResult(result);
        dos.write(result, 0, 1);
//System.out.println("writeHuff: span: " + span + " shift: " + shift + " bits: " + bits);
        dos.writeInt(shift);
        dos.writeInt(len);
        table.setDataLength(bits);
        if (!deltaTable) {
            int startPos = dos.size();
            table.writeDict(dos);
            huffmanTablesSize += (dos.size() - startPos);
        }
        packer = new BitPacker(table.streamBits());
        IntegerHuffmanNode inode = new IntegerHuffmanNode();

        for(int i=0; i < len; i++) {
            inode.setValue(data[i]);

            node = (IntegerHuffmanNode) table.getEntry(inode);
//if (i < 10) System.out.println("tag: " + node.tag + " len: " + node.tagLength + " val: " + node.getValue() + " hc: " + node.hashCode());
            packer.pack(node.tag, node.tagLength);
        }

        result = new byte[packer.size()];
        packer.getResult(result);
        int size = packer.size();
//System.out.println("encoded size: " + size);
        dos.writeInt(size);
        dos.write(result, 0, size);
    }

    /**
     * Compress an array of integers by determning the number of bits needed to
     * express the range of values.  Optional delta coding is provided.
     *
     *  Format:
     *     Number of ints encoded(27, nothing else if length=0)
     *     Number if bits per entry(5)
     *     Shift(32)
     *     Initial value(32 * span,if delta encoding)
     */
    public static void rangeCompressIntArray(DataOutputStream dos, boolean delta, int span,int[] ival) throws IOException {
        int max=Integer.MIN_VALUE;
        int min=Integer.MAX_VALUE;
        int shift;
        int cnt=0;
        byte b;
        boolean onelast=false;
        int len = ival.length;
        boolean removeMarkers = false;

        if (ival.length < 5)
            removeMarkers = false;

        BitPacker fieldBits;

        if (!delta)
            fieldBits = new BitPacker(8);
        else
            fieldBits = new BitPacker(8 + span*4);

        fieldBits.pack(ival.length, 27);    // Use 27 bits to encode length

        // TODO; Need to convert to use the calcRange method, but need bits and shift from it
        int[] lastVal = new int[span];
        int scnt=0;
        if (delta) {
            int hold;
            for(int i=0; i < ival.length; i++) {
//                System.out.println("orig: " + ival[i] + " last: " + lastVal[scnt] + " delta: " + (ival[i] - lastVal[scnt]));
                hold = ival[i];
                ival[i] = ival[i] - lastVal[scnt];
                lastVal[scnt++] = hold;
                if (scnt == span)
                    scnt=0;
            }
        }

        int[] newIval = ival;

        if (removeMarkers) {
            newIval = new int[ival.length * (span-1) / span];
        }

        int idx=0;
        for(int i=0; i < ival.length; i++) {
            if (ival[i] > max) {
                max = ival[i];
            }

            if (ival[i] < min)
                min = ival[i];

            if (removeMarkers && ((i+1) % span != 0)) {
                newIval[idx++] = ival[i];
            }
        }

        if (removeMarkers) {
//System.out.println("idx: " + idx + " len: " + ival.length);
            ival = newIval;
        }

//System.out.println("max: " + max + " min: " + min);
        int bits = computeBits(max - min);

        // Move the range to start at 0
        if (min < 0)
            shift = - min;
        else
            shift = min;

        fieldBits.pack(bits, 5);

//int tsize = dos.size();

        if (ival.length == 0) {
            fieldBits.writeStream(dos);
            return;
        }

        fieldBits.pack(shift, 32);

        if (delta) {
            // Write out initial values to lower overall range
            for(int i=0; i < span; i++) {
                lastVal[i] = ival[i];
                fieldBits.pack(lastVal[i],32);
            }
        }

        BitPacker bp = new BitPacker((int)Math.ceil(ival.length * bits / 8.0));
        int val;
        for(int i=0; i < ival.length; i++) {
            val = ival[i] + shift;
            bp.pack(val, bits);
        }

        fieldBits.writeStream(dos);

        bp.writeStream(dos);

//System.out.println("len: " + (ival.length*4) + " int size: " + (dos.size() - tsize) + " bits: " + bits);

        bcnts[bits]++;
    }

    /**
     * Decompress an array of integers compressed by determning the number of bits needed to
     * express the range of values.  Optional delta coding is provided.
     *
     *  Format:
     *     Number of ints encoded(27)
     *     Number if bits per entry(5, nothing else if length=0)
     *     Shift(32)
     *     Initial value(32 * span,if delta encoding)
     *
     */
    public static int[] rangeDecompressIntArray(InputStream dis, boolean delta, int span) throws IOException {
        BitUnpacker bup;
        byte[] buff;

        buff = new byte[4];

        dis.read(buff);
        // Get len and bits per entry
        bup = new BitUnpacker(buff);
        int len = bup.unpack(27);    // Use 27 bits to encode length
        int bpe = bup.unpack(5);

        if (len == 0)
            return new int[0];

        // Get shift
        int shift = readInt(dis,buff);
        int[] startVals = new int[span];

        if (delta) {
            for(int i=0; i < span; i++) {
                startVals[i] = readInt(dis,buff);
            }
        }

        int[] ival = new int[len];
        int buffLen = (int) Math.ceil(len * bpe / 8.0);
        if (buff.length < buffLen)
            buff = new byte[buffLen];

        dis.read(buff,0,buffLen);

        bup.reset(buff);

        int val;

        if (!delta) {
            for(int i=0; i < len; i++) {
                val = bup.unpack(bpe);
                ival[i] = val - shift;
            }
        } else {
            int[] lastVal = new int[span];
            int scnt=0;
            for(int i=0; i < ival.length; i++) {
                val = bup.unpack(bpe);
                ival[i] = val - shift;
//System.out.println("lastVal: " + lastVal[scnt] + " this: " + ival[i]);
                ival[i] = ival[i] + lastVal[scnt];
                lastVal[scnt++] = ival[i];
                if (scnt == span)
                    scnt=0;
            }
        }

        return ival;
    }

    public static int computeBits(int value) {
        int i = 0;
        if (value == 0) return 1;
        for (;;) {
            if (value == 0)
                return i;
            value >>= 1;
            i++;
            if (i >= 32)
                return 32;
        }
    }

    public static void printStats() {
        System.out.println("Huffman Tables Size: " + huffmanTablesSize);
/*
        System.out.println("Bit spread");
        for(int i=0; i < 32; i++) {
            if (bcnts[i] > 0) {
                System.out.println(i + " --> " + bcnts[i]);
            }
        }
*/
    }

    /**
     * Reads a stream and converts the next 4 entries into an integer.
     * Provide a preallocated 4 byte buffer for buff.
     */
    private static int readInt(InputStream dis, byte[] buff) throws IOException {
        dis.read(buff);
        int ch1 = buff[0];
        int ch2 = (buff[1] & 255);
        int ch3 = (buff[2] & 255);
        int ch4 = (buff[3] & 255);

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * Reads a stream and converts the next 4 entries into an integer.
     */
    private static int readInt(byte[] data, int start) throws IOException {
        int ch1 = data[start++];
        int ch2 = (data[start++] & 255);
        int ch3 = (data[start++] & 255);
        int ch4 = (data[start++] & 255);

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * Find the min/max of a float dataset.
     *
     * @param result The min/max.  Preallocate the array to 2 or greater.
     */
    private static void findMinMax(float[] data, float[] result) {
        int len = data.length;
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        float signNeeded = 0;

        for(int i=0; i < len; i++) {
            if (data[i] < 0)
                signNeeded = 1;
            if (data[i] < min)
                min = data[i];
            if (data[i] > max)
                max = data[i];
        }

        result[0] = min;
        result[1] = max;
        result[2] = signNeeded;
    }

    /**
     * Determine how many exponent bits needed for a given minimum/max.
     *
     * @param minmax The minimum and maximum float values
     * @return The minimum exponent needed to express without error.
     */
    private static int exponentNeededOld(float[] minmax) {
        FloatPacker encoder;
        FloatPacker decoder;
        int mantissa = 22;
        int exponent = 3;   // 3/4 give varing results for mantissa needed, why?
        boolean done = false;
        int bits;
        float val;
        float tolerance = 1.0f;

        while(!done) {
            exponent++;
//System.out.println("Trying exponent: " + exponent);
            encoder = new FloatPacker(exponent, mantissa);
            bits = (int) encoder.encode(minmax[0], true);
            val = encoder.decode(bits, true);
//System.out.println("orig min: " + minmax[0] + " newval: " + val);
            if (Math.abs(minmax[0] - val) >= tolerance)
                continue;

            bits = (int) encoder.encode(minmax[1], true);
            val = encoder.decode(bits, true);
//System.out.println("orig max: " + minmax[1] + " newval: " + val);
            if (Math.abs(minmax[1] - val) <= tolerance)
                done = true;

            if (exponent >= 8) {
                System.out.println("Big exponent needed for: " + minmax[0] + " -> " + minmax[1]);
                done = true;
            }
        }

        return exponent;
    }

    /**
     * Determine how many exponent bits needed for a given minimum/max.
     *
     * @param minmax The minimum and maximum float values
     * @return The minimum exponent needed to express without error.
     */
    private static int exponentNeeded(float[] minmax) {
        // convert to int, then to binary string.  Count digits
        int ival = Math.round(minmax[0]);
        String s = Integer.toBinaryString(ival);
        int max = s.length();

        ival = Math.round(minmax[1]);
        s = Integer.toBinaryString(ival);

        int len = s.length();

        if (len > max)
            max = len;

        return computeBits(max);
    }

    /**
     * Determine how many mantissa bits needed for a given array.
     *
     * @param data The minimum and maximum float values
     * @return The minimum exponent needed to express without error.
     */
    private static int mantissaNeeded(float[] data, int exponent, float tolerance) {
        FloatPacker encoder;
        int mantissa = 1;
        //int exponent = 8;
        boolean done = false;
        int bits;
        float val;
        int maxmantissa = mantissa;
        int len = data.length;

        for(int i=0; i < len; i++) {
            done = false;
            mantissa = maxmantissa - 1;

            while(!done) {
                mantissa++;

                if (mantissa == 23)
                    return 23;


if (debug)    System.out.println("Trying mantissa: " + mantissa + " exp: " + exponent);
                encoder = new FloatPacker(exponent, mantissa);
                bits = (int) encoder.encode(data[i], true);
                val = encoder.decode(bits, true);

if (debug)    System.out.println("orig min: " + data[i] + " newval: " + val);
                if (Math.abs(data[i] - val) > tolerance) {
                    continue;
                }
if (debug) System.out.println("Pass with err: " + Math.abs(data[i] - val));
                break;
            }

            if (mantissa > maxmantissa)
                maxmantissa = mantissa;
        }
if(debug) System.out.println("final mantissa: " + mantissa);
        return maxmantissa;
    }

    /**
     * Determine how many mantissa bits needed for a given array.
     *
     * @param data The minimum and maximum float values
     * @return The minimum exponent needed to express without error.
     */
    private static int displayFloatArrayParams(float[] data, int exponent, float tolerance) {
        FloatPacker encoder;
        int mantissa = 1;
        //int exponent = 8;
        boolean done = false;
        int bits;
        float val;
        int maxmantissa = mantissa;
        int len = data.length;

System.out.println("Displaying float calcs   exponent: " + exponent + " tolerance: " + tolerance + " len: " + data.length);
        for(int i=0; i < len; i++) {
            done = false;
            mantissa = maxmantissa - 1;

            while(!done) {
                mantissa++;

                if (mantissa == 23)
                    return 23;


System.out.println("Trying mantissa: " + mantissa + " exp: " + exponent);
                encoder = new FloatPacker(exponent, mantissa);
                bits = (int) encoder.encode(data[i], true);
                val = encoder.decode(bits, true);

System.out.println("orig val: " + data[i] + " newval: " + val);
                if (Math.abs(data[i] - val) > tolerance) {
                    continue;
                }
System.out.println("Pass with err: " + Math.abs(data[i] - val));
                break;
            }

            if (mantissa > maxmantissa)
                maxmantissa = mantissa;
        }
System.out.println("final mantissa: " + mantissa);
        return maxmantissa;
    }


    /**
     * Find the float parameters of exponent, mantissa and sign that will
     * express the float value within the tolerence specified.
     *
     * @param origval The original value
     * @param tolerance The error tolerance
     * @param output The exponent, mantisa and sign bits needed
     */
    public static void findFloatParams(float origval, float tolerance, int[] output) {
        // determine exponent, mantissa and sign requirements
        int exponent = 6;
        int mantissa = 23;
        int signed = 0;
        boolean ldebug = false;
        boolean search = true;
        boolean searchup = false;

        if (origval < 0) {
            signed = 1;
        }

        int src = Float.floatToIntBits(origval);

        long MANTISSA_MASK_32 = 0x007fffff;
        long EXPONENT_MASK_32 = 0x7f800000;

if(ldebug) System.out.println("initial value: " + origval);
        exponent = (int) (((src & EXPONENT_MASK_32) >> 23));

        if (exponent == 0)
            exponent = 0;
        else {
            exponent -= - 127;
    if (ldebug) System.out.println("exponent bits: " + (Integer.toBinaryString((int) (src & EXPONENT_MASK_32))) + " val: " + exponent);

            // TODO: Can we do this in less compares
            if (exponent > 63)
                exponent = 8;
            else if (exponent > 31)
                exponent = 7;
            else if (exponent > 15)
                exponent = 6;
            else if (exponent > 7)
                exponent = 5;
            else if (exponent > 3)
                exponent = 4;
            else if (exponent > 1)
                exponent = 3;
            else if (exponent > 0)
                exponent = 2;
            else if (exponent < -64)
                exponent = 8;
            else if (exponent < -32)
                exponent = 7;
            else if (exponent < -16)
                exponent = 6;
            else if (exponent < -8)
                exponent = 5;
            else if (exponent < -4)
                exponent = 4;
            else if (exponent < -2)
                exponent = 3;
            else if (exponent < -1)
                exponent = 2;
            else
                exponent = 2;   // TODO: I think this should be 1, but it doesn't work for 0.5
        }
if (ldebug) System.out.println("exponent bits: " + exponent);
        mantissa = (int) ((src & MANTISSA_MASK_32));

        if (searchup) {
            mantissa = 0;

            FloatPacker encoder = new FloatPacker(exponent, mantissa);
            int bits;
            float val;
            boolean passed = false;

            while(!passed) {
                bits = (int) encoder.encode(origval, false);  // rounding causes issues
                val = encoder.decode(bits, true);

                if (Math.abs(origval - val) > tolerance) {
                    if (mantissa == 23) {
                        exponent++;
                    } else
                        mantissa++;
                } else {
                    passed = true;
                    break;
                }

                encoder.reinit(exponent, mantissa);
            }

            if (mantissa < 0)
                mantissa = 0;

            output[0] = exponent;
            output[1] = mantissa;
            output[2] = signed;
if (ldebug) System.out.println("final params: exp: " + exponent + " mantissa: " + mantissa);

            return;
        }

if (ldebug) System.out.println("mantissa: " + (Integer.toBinaryString(mantissa)) + " val: " + mantissa);
        if (mantissa > 0) {
            int charPos = 23;
            int radix = 1 << 1;
            int mask = radix - 1;
            do {
                if ((mantissa & mask) == 1)
                    break;
                mantissa >>>= 1;

                charPos--;
            } while (mantissa != 0);

            mantissa = charPos;
if (ldebug) System.out.println("mantissa bits: " + mantissa);
        } else {
            // 0 mantissa, so skip checking
            output[0] = exponent;
            output[1] = 0;
            output[2] = signed;

if (ldebug) System.out.println("final params: exp: " + exponent + " mantissa: 0");
            return;
        }

        boolean passed = true;
        int mantissa_initial = mantissa;
        int exponent_initial = exponent;

        FloatPacker encoder = new FloatPacker(exponent, mantissa);
        int bits;
        float val;
        boolean orig = true;
        int hops = 0;

        if (!search) {
            output[0] = exponent;
            output[1] = mantissa;
            output[2] = signed;

            bits = (int) encoder.encode(origval, false);  // rounding causes issues
            val = encoder.decode(bits, true);

            if (Math.abs(origval - val) > tolerance) {
                System.out.println("FAIL: orig: " + origval + " err: " + (Math.abs(origval - val)));
            }

            return;
        }

        // TODO: Which direction should we test, base on tolerance?
        while(passed && mantissa > -1) {
            bits = (int) encoder.encode(origval, false);  // rounding causes issues
            val = encoder.decode(bits, true);

if(ldebug) System.out.println("mantissa: " + mantissa + " orig val: " + origval + " newval: " + val);
            if (Math.abs(origval - val) > tolerance) {
if(ldebug)                System.out.println("FAIL: err: " + (Math.abs(origval - val)));
                passed = false;

                if (orig == true) {
                    //System.out.println("***Failed to encode: " + origval + " using: " + exponent + " " + mantissa);
                    exponent++;
                    mantissa = 22;
                }
            } else {
if(ldebug)                System.out.println("PASS");

                mantissa--;
                hops++;

                encoder.reinit(exponent, mantissa);
            }

            orig = false;
        }

        mantissa++;

        output[0] = exponent;
        output[1] = mantissa;
        output[2] = signed;

if (ldebug) System.out.println("final params: exp: " + exponent + " mantissa: " + mantissa);

if (exponent > exponent_initial || mantissa > mantissa_initial)
    System.out.println("bigger then original: " + exponent + " mant: " + mantissa + " orig: " + exponent_initial + " " + mantissa_initial + " val: " + origval);
    }


    /**
     * Find the float parameters of exponent, mantissa and sign that will
     * express the array of float values within the tolerence specified.
     *
     * @param origvals The original value
     * @param tolerance The error tolerance
     * @param output The exponent, mantisa and sign bits needed
     */
    public static void findFloatParams(float[] origvals, float tolerance, int[] output) {
        int[] tmp = new int[3];
        int len = origvals.length;

        output[0] = 0;
        output[1] = 0;
        output[2] = 0;

        for(int i=0; i < len; i++) {
            findFloatParams(origvals[i], tolerance, tmp);
            if (tmp[0] > output[0])
                output[0] = tmp[0];
            if (tmp[1] > output[1])
                output[1] = tmp[1];
            if (tmp[2] > output[2])
                output[2] = tmp[2];
        }
    }

    /**
     * Find the float parameters of exponent, mantissa and sign that will
     * express the array of float values within the tolerence specified.
     *
     * @param origvals The original value
     * @param tolerance The error tolerance
     * @param output The exponent, mantisa and sign bits needed
     */
    public static void findFloatParamsOld(float[] origvals, float tolerance, int[] output) {
        int exponent = 6;
        int mantissa = 23;
        int signed = 0;

        float[] minmax = new float[3];

        findMinMax(origvals,minmax);
        exponent = exponentNeeded(minmax);
        mantissa = mantissaNeeded(origvals, exponent, tolerance);
        if (minmax[2] > 0)
            signed = 1;
    }

    private static void testSingleFloat(float origval, float tolerance) {
        FloatPacker encoder = new FloatPacker(1,1);
        float val;
        int bits;

        int[] output = new int[3];


        findFloatParams(origval, tolerance, output);

        System.out.println("val: " + origval + " exponent: " + output[0] + " mantissa: " + output[1]);
        encoder.reinit(output[0], output[1]);

        bits = (int) encoder.encode(origval, false);
        val = encoder.decode(bits, true);

        if (Math.abs(origval - val) > tolerance) {
            System.out.println("FAIL: err: " + (Math.abs(origval - val)) + " orig: " + origval + " decode: " + val + " exponent: " + output[0] + " mantissa: " + output[1]);
        } else {
            System.out.println("PASS:  orig: " + origval + " new: " + val);
        }
    }

    private static void testAllFloatsSize(int size, float start, int step, float tolerance) {
        FloatPacker encoder = new FloatPacker(1,1);
        float val;
        int bits;

        int[] output = new int[3];


        int numTested = 0;
        float[] array = new float[size];
        int startI = Float.floatToIntBits(start);

        while(numTested < size) {
            int i = startI + numTested * step;
            float var = Float.intBitsToFloat(i);

            if (Float.isNaN(var))
                continue;

            array[numTested] = var;

            numTested++;

        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(size*4);
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            quantizeFloatArrayDeflater(dos, array, tolerance);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        System.out.println("Original size: " + (size * 4) + " compressed: " + dos.size());
    }

    private static void testAllWholeFloatsSize(int size, float start, float tolerance) {
        FloatPacker encoder = new FloatPacker(1,1);
        float val;
        int bits;

        int[] output = new int[3];


        int numTested = 0;
        float[] array = new float[size];
        int startI = Float.floatToIntBits(start);

        while(numTested < size) {
            float var = start + numTested;

            if (Float.isNaN(var))
                continue;

            array[numTested] = var;

            numTested++;

        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(size*4);
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            quantizeFloatArrayDeflater(dos, array, tolerance);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        System.out.println("Original size: " + (size * 4) + " compressed: " + dos.size());
    }

    private static void testAllFloats(int step, float tolerance) {
        FloatPacker encoder = new FloatPacker(1,1);
        float val;
        int bits;

        int[] output = new int[3];


        boolean gonePositive = false;
        int numTested = 0;

        for(int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i = i + step) {

            if (gonePositive && i < 0)
                break;

            if (i > 0)
                gonePositive = true;

            numTested++;

            float var = Float.intBitsToFloat(i);

            if (i % 100000 == 0)
                System.out.println("Number: " + numTested + " Testing: " + var + " i: " + i);

            if (Float.isNaN(var))
                continue;

//            System.out.println("Testing: " + var);
            findFloatParams(var, tolerance, output);

            encoder.reinit(output[0], output[1]);

            bits = (int) encoder.encode(var, false);
            val = encoder.decode(bits, true);

            if (Math.abs(var - val) > tolerance) {
                System.out.println("FAIL: err: " + (Math.abs(var - val)) + " orig: " + var + " decode: " + val + " exponent: " + output[0] + " mantissa: " + output[1]);
            } else {
//                System.out.println("PASS");
            }
        }
    }

    public static void main(String[] args) {
//        float origval = 8.74213f;
//        float origval = 109.277f;
//        float origval = -118.625f;
        float origval = 0.25f;
//        float origval = 1.23456789f;
        float tolerance = 1e-7f;


        //testAllFloatsSize(5000, Integer.MIN_VALUE, 1007, 1f);
        testAllWholeFloatsSize(1000, -500, 1f);
    }

}
