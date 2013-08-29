/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Prototype Huffman node.  Borrowed original impl from Sun HuffmanNode, need to decide
 * if copyright still stands for heavily modified code.
 * @version $Revision: 1.2 $
 */
class ByteHuffmanNode extends HuffmanNode {
    byte data;
    int dataLength;

    protected ByteHuffmanNode() {
    }

    protected ByteHuffmanNode(byte data) {
        this.data = data;
    }

    public void setValue(byte value) {
        data = value;
    }

    public byte getValue() {
        return data;
    }

    /**
     * Write the data for this node out to a stream.
     *
     * @param packer The place to write the bits
     * @param len The number of bits to use
     */
    public void writeData(BitPacker packer, int len) throws IOException {
        packer.pack(data, len);
    }

    void collectLeaves(int tag, int tagLength, Collection collection) {
        this.tag = tag;
        this.tagLength = tagLength;
        collection.add(this);
    }


    public String toString() {
    return
        "data: " + data +
        "\ntag 0x" + Integer.toBinaryString((int)tag) + " tag length " + tagLength +
        "\nfrequency: " + frequency ;
    }

    public int hashCode() {
        return data;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ByteHuffmanNode) {
            return data == ((ByteHuffmanNode)obj).getValue();
        }
        return false;
    }

}
