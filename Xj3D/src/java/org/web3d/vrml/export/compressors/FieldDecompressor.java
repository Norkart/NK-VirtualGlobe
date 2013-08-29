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

// External imports
import java.io.DataInputStream;
import java.io.IOException;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * All classes capable of decompressing a field must implement this interface
 *
 * @author Alan Hudson.
 * @version $Revision: 1.4 $
 */
public interface FieldDecompressor {

    /**
     * Can this fieldCompressor support this compression method
     *
     * @param fieldType What type of field, defined in FieldConstants.
     * @param method What method of compression.  0-127 defined by Web3D Consortium.
     */
    public boolean canSupport(int fieldType, int method);

    /**
     * Get the length of variable length field.
     *
     * @return The length of the upcoming field in number of type units.
     */
    public int nextLength(DataInputStream dis) throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public int decompressInt(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressInt(DataInputStream dis, int fieldType, int[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public boolean decompressBoolean(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressBoolean(DataInputStream dis, int fieldType, boolean[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public float decompressFloat(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressFloat(DataInputStream dis, int fieldType, float[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressFloat(DataInputStream dis, int fieldType, float[][] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public long decompressLong(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressLong(DataInputStream dis, int fieldType, long[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public double decompressDouble(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressDouble(DataInputStream dis, int fieldType, double[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressDouble(DataInputStream dis, int fieldType, double[][] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     */
    public String decompressString(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     */
    public void decompressString(DataInputStream dis, int fieldType, String[] data)
        throws IOException;
}
