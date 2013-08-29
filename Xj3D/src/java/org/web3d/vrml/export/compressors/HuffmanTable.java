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

// External imports
import java.io.*;
import java.util.* ;

// Local imports
// None

/**
 * Prototype Huffman node.  Borrowed original impl from Sun HuffmanNode, need to decide
 * if copyright still stands for heavily modified code.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class HuffmanTable {
    private HashMap values;
    private int totalBits;

    /** The maximum number of bits any node has */
    private int dataLength;
    private int maxTagLength;

    /** Decompression table.  Flat array for now */
    private HuffmanNode[][] dtable;

    /**
     * Create a new HuffmanTable with entries for all possible position,
     * normal, and color tokens.
     */
    HuffmanTable() {
        values = new HashMap();
        dataLength = 0;
    }

    /**
     * Set the length of each data element in bits.
     *
     * @param dataLength The length.
     */
    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    /**
     * Get the total number of bits needed to writeout the stream
     * used to create this table.
     *
     * @return The number of bits
     */
    public int streamBits() {
        return totalBits;
    }

    private void calcStreamBits(LinkedList nodes) {
        totalBits = 0;

        maxTagLength = 0;

        Iterator i = nodes.iterator() ;
        while(i.hasNext()) {
            HuffmanNode n = (HuffmanNode)i.next() ;
            totalBits += n.frequency * n.tagLength;
            if (n.tagLength > maxTagLength)
                maxTagLength = n.tagLength;
        }
    }

    /**
     * Add an entry.
     *
     * @param node The Huffman node to add.
     */
    void addEntry(HuffmanNode node) {
        HuffmanNode found = (HuffmanNode) values.get(node);

        if (found != null) {
            found.addCount();
        } else {
            node.addCount();
            values.put(node,node);
        }
    }

    public HuffmanNode getEntry(HuffmanNode node) {
        HuffmanNode found = (HuffmanNode) values.get(node);
        return found;
    }

    /**
     * Write the dictionary.
     *
     * @param dos The stream to write to.
     */
    public void writeDict(DataOutputStream dos) throws IOException {
        LinkedList nodeList = new LinkedList();
        nodeList.addAll(values.values());
        int size = values.size();

        int streamPos = dos.size();

        // Write number of entries
        dos.writeInt(size);

        // Write entry field size(bits)
        // TODO: This could be munged into 1 byte
        dos.writeByte(maxTagLength);
        dos.writeByte(dataLength);
//System.out.println("write  numEntries: " + size + " tagLen: " + maxTagLength + " dataLen: " + dataLength);
        BitPacker packer = new BitPacker(dataLength * size + maxTagLength * size + size);
//System.out.println("packer bits: " + (dataLength * size + maxTagLength * size + size));

        // Write values
        Iterator i = nodeList.iterator();
        HuffmanNode n;

        while(i.hasNext()) {
            n = (HuffmanNode)i.next();
            packer.pack(n.tag, maxTagLength);
            packer.pack(n.tagLength, 5);
            n.writeData(packer, dataLength);
        }

        byte[] result = new byte[packer.size()];
        packer.getResult(result);
        size = packer.size();

        // Number of bytes the table takes
        dos.writeInt(size);
        dos.write(result, 0, size);
//System.out.println("Dict entries: " + values.size() + " dataLength: " + dataLength + " DictSize: " + (dos.size() - streamPos));

        //print();
    }

    /**
     * Read the dictonary from a stream.
     *
     * @param dis The stream.
     */
    public void readDict(InputStream dis) throws IOException {
        clear();

        byte buff[] = new byte[4];

        int numEntries = readInt(dis, buff);
        maxTagLength = dis.read();
        dataLength = dis.read();

        int len = readInt(dis, buff);
//System.out.println("read  numEntries: " + numEntries + " tagLen: " + maxTagLength + " dataLen: " + dataLength);
        byte[] bits = new byte[len];
//System.out.println("dict size: " + bits.length);
        dis.read(bits);

        BitUnpacker unpacker = new BitUnpacker(bits);

        // TODO: Need to remove hard code creation of IntegerHuffmanNode
        IntegerHuffmanNode node;
        int val;
        int tag;
        int tagLength;

//        System.out.println("Decompression table: " + (maxTagLength) * (2 << maxTagLength));
        dtable = new HuffmanNode[maxTagLength][2 << maxTagLength];

        for(int i=0; i < numEntries; i++) {
            node = new IntegerHuffmanNode();
            tag = unpacker.unpack(maxTagLength);
            tagLength = unpacker.unpack(5);

            val = unpacker.unpack(dataLength);
//System.out.println("tag: " + tag + " tagLength: " + tagLength + " val: " + val);
            node.tag = tag;
            node.tagLength = tagLength;
            node.setValue(val);

            values.put(node, node);
            dtable[tagLength-1][tag] = node;
        }

        //print();
    }


    /**
     * Decode a tag into a HuffmanNode.
     *
     * @param tags The huffman tag
     * @param data An array to put the decoded node into
     */
    public void decode(byte[] tags, int[] data) {
        BitUnpacker unpacker = new BitUnpacker(tags);

        int tag = 0;
        int idx = 0;
        int len = data.length;
        IntegerHuffmanNode node;
        int tagLength = 0;

        // TODO: This could be sped up greatly.
        while(idx < len) {
            tagLength++;
            tag = (tag << 1) | (unpacker.unpack(1));

            node = (IntegerHuffmanNode) dtable[tagLength-1][tag];
//System.out.println("proposed tag: " + Integer.toBinaryString(tag) + "  currlen: " + tagLength + " found: " + (node != null));

            if (node != null && node.tagLength == tagLength) {
//System.out.println("found tag: " + tag + " = " + node.getValue());
                data[idx++] = node.getValue();
                tag = 0;
                tagLength = 0;
            }
        }
     }

    /**
     * Clear this HuffmanTable instance.
     */
    public void clear() {
        values.clear();
    }

    /**
     * Compute optimized tags for each position, color, and normal entry.
     */
    public void computeTags() {
        LinkedList nodeList = new LinkedList();
        nodeList.addAll(values.values());

        computeTags(nodeList) ;
        calcStreamBits(nodeList);
        //print("\nposition tokens and tags", nodeList) ;
    }

    //
    // Compute tags for a list of Huffman tokens.
    //
    private void computeTags(LinkedList nodes) {
        HuffmanNode node0, node1, node2 ;

        // Return if there's nothing to do.
        if (values.isEmpty())
            return ;

        Collections.sort(nodes, HuffmanNode.frequencyComparator) ;

        // Apply Huffman's algorithm to construct a binary tree with a
        // minimum total weighted path length.
        node0 = (HuffmanNode)nodes.removeFirst();
        while (nodes.size() > 0) {
            node1 = (HuffmanNode)nodes.removeFirst();
            node2 = new HuffmanNode() ;

            node2.addChildren(node0, node1) ;
            addNodeInOrder(nodes, node2, HuffmanNode.frequencyComparator);

            node0 = (HuffmanNode)nodes.removeFirst();
        }

        // node0 is the root of the resulting binary tree.  Traverse it
        // assigning tags and lengths to the leaf nodes.  The leaves are
        // collected into the now empty node list.
        node0.collectLeaves(0, 0, nodes) ;

        // Sort the nodes in descending order by tag length.
        Collections.sort(nodes, HuffmanNode.tagLengthComparator) ;
    }

    //
    // Insert a node into the correct place in a sorted list of nodes.
    //
    private void addNodeInOrder(LinkedList l, HuffmanNode node, Comparator c) {
        ListIterator i = l.listIterator(0) ;

        while (i.hasNext()) {
            HuffmanNode n = (HuffmanNode)i.next() ;
            if (c.compare(n, node) > 0) {
                n = (HuffmanNode)i.previous() ;
                break ;
            }
        }
        i.add(node) ;

    }

    /**
     * Print a collection of HuffmanNode objects to standard out.
     *
     * @param header descriptive string
     * @param nodes Collection of HuffmanNode objects to print
     */
    void print(String header, Collection nodes) {
        System.out.println(header + "\nentries: " + nodes.size() + "\n") ;

        Collections.sort((LinkedList)nodes, IntegerHuffmanNode.dataComparator);

        Iterator i = nodes.iterator() ;
        while(i.hasNext()) {
            HuffmanNode n = (HuffmanNode)i.next() ;
            System.out.println(n.toString() + "\n") ;
        }
    }

    /**
     * Print the contents of this instance to standard out.
     */
    public void print() {
        LinkedList nodeList = new LinkedList() ;
        nodeList.addAll(values.values());

        print("\nposition tokens and tags", nodeList) ;
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
}
