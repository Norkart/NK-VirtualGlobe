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
import java.io.DataInputStream;
import java.io.IOException;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * All classes capable of compressing a field must implement this interface
 *
 * @author Alan Hudson.
 * @version $Revision: 1.3 $
 */
public interface FieldCompressor {
    /** Can this fieldCompressor support this compression method
     *
     * @param fieldType What type of field, defined in FieldConstants.
     * @param method What method of compression.  0-127 defined by Web3D Consortium.
     */
    public boolean canSupport(int fieldType, int method);

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, int data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, int[] data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, boolean data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, boolean[] data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, float data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, float[] data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, float[][] data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, long data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, long[] data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, double data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, double[] data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, double[][] data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, String data)
        throws IOException;

    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, String[] data)
        throws IOException;
}
