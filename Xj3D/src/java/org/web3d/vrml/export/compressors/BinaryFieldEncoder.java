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
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * A field compressor that just encodes the data in binary form.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class BinaryFieldEncoder implements FieldCompressor, FieldDecompressor {
    /** The next length to read */
    private int len;

    /** Can this fieldCompressor support this compression method
     *
     * @param fieldType What type of field, defined in FieldConstants.
     * @param method What method of compression.  0-127 defined by Web3D Consortium.
     */
    public boolean canSupport(int fieldType, int method) {
        // This class can encode all field types
        return true;
    }

    /**
     * Get the length of variable length field.
     *
     * @return The length of the upcoming field in number of type units.
     */
    public int nextLength(DataInputStream dis) throws IOException {
        len = dis.readInt();
        return len;
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, int data)
        throws IOException {
        dos.writeInt(data);
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, int[] data)
        throws IOException {

        dos.writeInt(data.length);
        for(int i=0; i < data.length; i++) {
            dos.writeInt(data[i]);
        }
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, boolean data)
        throws IOException {
        dos.writeBoolean(data);
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, boolean[] data)
        throws IOException {
        dos.writeInt(data.length);
        for(int i=0; i < data.length; i++) {
            dos.writeBoolean(data[i]);
        }
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, float data)
        throws IOException {

        dos.writeFloat(data);
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, float[] data)
        throws IOException {


        switch(fieldType) {
            case FieldConstants.SFCOLOR:
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFROTATION:
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.SFVEC2F:
                break;
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFFLOAT:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFVEC2F:
                dos.writeInt(data.length);
                break;
            default:
                System.out.println("Unhandled datatype in compress float[]: " + fieldType);
        }

        for(int i=0; i < data.length; i++) {
           dos.writeFloat(data[i]);
        }
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, float[][] data)
        throws IOException {
        dos.writeInt(data.length * data[0].length);
        for(int i=0; i < data.length; i++) {
            for(int j=0; j < data[0].length; j++) {
                dos.writeFloat(data[i][j]);
            }
        }
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, long data)
        throws IOException {
        dos.writeLong(data);
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, long[] data)
        throws IOException {

        dos.writeInt(data.length);

        for(int i=0; i < data.length; i++) {
           dos.writeLong(data[i]);
        }
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, double data)
        throws IOException {
        dos.writeDouble(data);
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, double[] data)
        throws IOException {

        dos.writeInt(data.length);
        for(int i=0; i < data.length; i++) {
           dos.writeDouble(data[i]);
        }
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, double[][] data)
        throws IOException {
        dos.writeInt(data.length * data[0].length);
        for(int i=0; i < data.length; i++) {
            for(int j=0; j < data[0].length; j++) {
                dos.writeDouble(data[i][j]);
            }
        }
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, String data)
        throws IOException {

        dos.writeUTF(data);
    }

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, String[] data)
        throws IOException {
        dos.writeInt(data.length);
        for(int i=0; i < data.length; i++) {
            dos.writeUTF(data[i]);
        }
    }

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return data The field data
     */
    public int decompressInt(DataInputStream dis, int fieldType)
        throws IOException {

        return dis.readInt();
    }

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressInt(DataInputStream dis, int fieldType, int[] data)
        throws IOException {

        for(int i=0; i < len; i++) {
            data[i] = dis.readInt();
        }
    }

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public boolean decompressBoolean(DataInputStream dis, int fieldType)
        throws IOException {

        return dis.readBoolean();
    }

    /**
     * Decompress this field. If the array is too small it will be realloacted.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressBoolean(DataInputStream dis, int fieldType, boolean[] data)
        throws IOException {

        for(int i=0; i < len; i++) {
            data[i] = dis.readBoolean();
        }
    }

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public float decompressFloat(DataInputStream dis, int fieldType)
        throws IOException {
        return dis.readFloat();
    }

    /**
     * Decompress this field.If the array is too small it will be realloacted.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressFloat(DataInputStream dis, int fieldType, float[] data)
        throws IOException {

        switch(fieldType) {
            case FieldConstants.SFCOLOR:
            case FieldConstants.SFVEC3F:
                len = 3;
                break;
            case FieldConstants.SFROTATION:
            case FieldConstants.SFCOLORRGBA:
                len = 4;
                break;
            case FieldConstants.SFVEC2F:
                len = 2;
                break;
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFFLOAT:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFVEC2F:
                // Should have already been read
                break;
            default:
                System.out.println("Unhandled datatype in compress float[]: " + fieldType);
        }

        for(int i=0; i < len; i++) {
            data[i] = dis.readFloat();
        }
    }

    /**
     * Decompress this field. If the array is too small it will be realloacted.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressFloat(DataInputStream dis, int fieldType, float[][] data)
        throws IOException {

        // TODO: Is it important to keep the original structure here?
        for(int i=0; i < len; i++) {
            data[0][i] = dis.readFloat();
        }
    }

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public long decompressLong(DataInputStream dis, int fieldType)
        throws IOException {

        return dis.readLong();
    }

    /**
     * Decompress this field. If the array is too small it will be realloacted.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressLong(DataInputStream dis, int fieldType, long[] data)
        throws IOException {

        for(int i=0; i < len; i++) {
            data[i] = dis.readLong();
        }
    }

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public double decompressDouble(DataInputStream dis, int fieldType)
        throws IOException {

        return dis.readDouble();
    }

    /**
     * Decompress this field. If the array is too small it will be realloacted.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressDouble(DataInputStream dis, int fieldType, double[] data)
        throws IOException {

        for(int i=0; i < len; i++) {
            data[i] = dis.readDouble();
        }
    }

    /**
     * Decompress this field. If the array is too small it will be realloacted.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressDouble(DataInputStream dis, int fieldType, double[][] data)
        throws IOException {

        // TODO: Is it important to keep the original structure here?
        for(int i=0; i < len; i++) {
            data[0][i] = dis.readDouble();
        }
    }

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public String decompressString(DataInputStream dis, int fieldType)
        throws IOException {


        return dis.readUTF();
    }

    /**
     * Decompress this field. If the array is too small it will be realloacted.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void decompressString(DataInputStream dis, int fieldType, String[] data)
        throws IOException {

        for(int i=0; i < len; i++) {
            data[i] = dis.readUTF();
        }
    }
}
