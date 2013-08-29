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
 * A FieldCompressor that works by compressing the range of data.  Floats are
 * converted to ints before range compression.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class RangeCompressor extends BinaryFieldEncoder {
    /**
     * Compress this field and deposit the output to the bitstream
     *
     * @param dos The stream to output the result
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data
     */
    public void compress(DataOutputStream dos, int fieldType, int[] data)
        throws IOException {

        CompressionTools.rangeCompressIntArray(dos,false,1,data);
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
                for(int i=0; i < data.length; i++) {
                   dos.writeFloat(data[i]);
                }
                break;
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFFLOAT:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFVEC2F:
                CompressionTools.compressFloatArray(dos, false,1,data);
                break;
            default:
                System.out.println("Unhandled datatype in compress float[]: " + fieldType);
        }
    }
}
