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
import java.util.* ;

/**
 * Prototype Huffman node.  Borrowed original impl from Sun HuffmanNode, need to decide
 * if copyright still stands for heavily modified code.
 * @version $Revision: 1.2 $
 */
public class HuffmanNode {
    public int tag, tagLength;
    protected int frequency;
    protected HuffmanNode child0, child1;

    public void addCount() {
        frequency++ ;
    }

    /**
     * Write the data for this node out to a stream.
     *
     * @param packer The place to write the bits
     * @param len The number of bits to use
     */
    public void writeData(BitPacker packer, int len) throws IOException {
        // No data for this node
        System.out.println("Writing untyped Huffman node?");
    }

    void addChildren(HuffmanNode child0, HuffmanNode child1) {
        this.child0 = child0 ;
        this.child1 = child1 ;
        this.frequency = child0.frequency + child1.frequency ;
    }

    void collectLeaves(int tag, int tagLength, Collection collection) {
        if (child0 == null) {
            this.tag = tag;
            this.tagLength = tagLength;
            collection.add(this);
        } else {
            child0.collectLeaves((tag << 1) | 0, tagLength + 1, collection);
            child1.collectLeaves((tag << 1) | 1, tagLength + 1, collection);
        }
    }

    /**
     * Sorts nodes in ascending order by frequency.
     */
    static class FrequencyComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            return ((HuffmanNode)o1).frequency - ((HuffmanNode)o2).frequency ;
        }
    }

    /**
     * Sorts nodes in descending order by tag bit length.
     */
    static class TagLengthComparator implements Comparator {
    public final int compare(Object o1, Object o2) {
        return ((HuffmanNode)o2).tagLength - ((HuffmanNode)o1).tagLength ;
    }
    }

    static FrequencyComparator frequencyComparator = new FrequencyComparator() ;
    static TagLengthComparator tagLengthComparator = new TagLengthComparator() ;

    public String toString() {
        return "HN." + hashCode() + " freq: " + frequency;
    }
}
