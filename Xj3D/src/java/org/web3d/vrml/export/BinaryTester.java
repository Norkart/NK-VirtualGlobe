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
package org.web3d.vrml.export;

// External Imports
import java.io.*;
import java.util.HashMap;

// Local Imports
import org.web3d.util.SimpleStack;
import org.web3d.vrml.export.compressors.*;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.parser.FieldParserFactory;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.norender.NRNodeFactory;

public class BinaryTester {
    private static final int STATE_NODE = 0;
    private static final int STATE_FIELDS = 1;
    private int state = STATE_NODE;
    private DataInputStream dis;
    private HashMap dictionary;

    /** Number of bytes for the nodeOp(OP+NodeNum+DEF) */
    private int nodeOpBytes;
    private int useOpBytes;
    private int nodeNumBits;
    private int defNumBits;
    private int useOpBits;
    private int fieldOpBits;
    private int nodeOpBits;

    /** Scratch var for node ops */
    private byte[] nodeOp;
    private byte[] useOp;
    private byte[] fieldOp;
    private byte[] op;

    /** Number of bits for the field marker */
    private int fieldOpBytes;
    private int fieldNumBits;

    private String[] nodeDict;
    private String[] defDict;
    private String[] fieldDict;

    // A list of nodes and their fieldMaps(name, int)
    private HashMap[] nodeFieldDict;

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** The choosen FieldCompressors */
    private FieldDecompressor[] fieldMethods;

    /** The choosen FieldCompressors */
    private NodeCompressor[] nodeMethods;

    // Scratch variables for field reading.  They will be grown as needed.
    private String[] stringArray;
    private float[] floatArray;
    private int[] intArray;

    // The current node number
    private SimpleStack currentNode;

    public static void main(String args[]) {

        if (args.length < 1) {
            System.out.println("usage:  <filename>");
            return;
        }

        BinaryTester tester = new BinaryTester();

        try {
            File file = new File(args[0]);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

           tester.parseFile(dis);

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void parseFile(DataInputStream dis) throws IOException {
        floatArray = new float[4];
        stringArray = new String[1];
        intArray = new int[1];
        currentNode = new SimpleStack();
        initCompressors();
        this.dis = dis;
        readHeader();

        // TODO: use right profile
        nodeFactory = DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);
        nodeFactory.setProfile("Immersive");

        readOps();
    }

    public void readHeader() throws IOException {
        // Ignore spec version
        dis.readByte();
        nodeNumBits = dis.readByte();
        defNumBits = dis.readByte();
        fieldNumBits = dis.readByte();
        nodeOpBits = BinaryExporter.OP_BITS + nodeNumBits + defNumBits;
        useOpBits =  BinaryExporter.OP_BITS + defNumBits;
        fieldOpBits = BinaryExporter.FIELD_METHOD_BITS + fieldNumBits;

        nodeOpBytes = getNumberOfBytes(nodeOpBits);
        useOpBytes = getNumberOfBytes(nodeOpBits);
        fieldOpBytes = getNumberOfBytes(fieldOpBits);
System.out.println("nodeNumBits: " + nodeNumBits + " nodeOpBits: " + nodeOpBits + " defNumBits: " + defNumBits + " useOpBits: " + useOpBits + " fieldOpBits: " + fieldOpBits + " fieldnumBits: " + fieldNumBits);
System.out.println("nodeOpBytes: " + nodeOpBytes + " fieldOpBytes: " + fieldOpBytes);
        nodeOp = new byte[nodeOpBytes];
        useOp = new byte[useOpBytes];
        fieldOp = new byte[fieldOpBytes];

        int max = nodeOpBytes;
        if (useOpBytes > max)
            max = useOpBytes;
        if (fieldOpBytes > max)
            max = fieldOpBytes;

        op = new byte[max];

        // Read the node Dictionary
        int dictSize = dis.readInt();
        String key;
        System.out.println("Node Dictionary Size: " + dictSize);
        nodeDict = new String[dictSize+1];

        for(int i=0; i < dictSize; i++) {
            key = dis.readUTF();
            nodeDict[i+1] = key;
//System.out.println("nodeNum: " + i+ " key: " + key);
        }

        // Read the def Dictionary
        dictSize = dis.readInt();
        System.out.println("DEF Dictionary Size: " + dictSize);
        defDict = new String[dictSize+1];

        for(int i=0; i < dictSize; i++) {
            key = dis.readUTF();
            defDict[i+1] = key;
        }

        // Read the field Dictionary

        dictSize = dis.readInt();
        System.out.println("Field Dictionary Size: " + dictSize);
        nodeFieldDict = new HashMap[dictSize+1];
        int size;
        String fieldName;

System.out.println("Fields: " + dictSize);
        for(int i=0; i < dictSize; i++) {
            size = dis.readByte();
            nodeFieldDict[i+1] = new HashMap(size);

            for(int j=0; j < size; j++) {
                fieldName = dis.readUTF();
                nodeFieldDict[i+1].put(new Integer(j),fieldName);
                System.out.println("nodeNum: " + (i+1) + " fieldId: " + j + " name: " + fieldName);
            }
        }
    }

    public void readOps() throws IOException {
        int len;

        while(true) {
            len = dis.read(op);

            if (len < 0)
                break;

            BitUnpacker bup = new BitUnpacker(op);

            int op = bup.unpack(2);

            switch(op) {
                case BinaryExporter.OP_NODE:
                    readNode(bup);
                    break;
                default:
                    System.out.println("Unknown OP code: " + op);
            }
        }
   }

    /**
     * Read a USE from the stream.
     */
    public void readUSE(BitUnpacker bup) throws IOException {
        int defNum = bup.unpack(defNumBits);

        String defName = null;

        if (defNum != 0)
            defName = defDict[defNum];

        System.out.println("USE: " + defName);
    }

    /**
     * Read a node from the stream.  Returns false is it was an
     * end of field marker.  True if a node was read.
     */
    public boolean readNode(BitUnpacker bup) throws IOException {
        int nodeNum = bup.unpack(nodeNumBits);

        if (nodeNum == 0) {
System.out.println("rn eof");
            return false;
        }

        currentNode.push(new Integer(nodeNum));
        int defNum = bup.unpack(defNumBits);

        String name = nodeDict[nodeNum];
        String defName = null;

        if (defNum != 0)
            defName = defDict[defNum];

        System.out.println("Node: " + name + " defName: " + defName + " nodeNum: " + nodeNum);

        VRMLNode node = nodeFactory.createVRMLNode(name, false);

        readFields(node);

        currentNode.pop();
        return true;
    }

    public void readFields(VRMLNode node) throws IOException {
        int nodeId = ((Integer)currentNode.peek()).intValue();

        dis.read(fieldOp);
        BitUnpacker bup = new BitUnpacker(fieldOp);

        int fieldNum = bup.unpack(fieldNumBits);

        if (fieldNum == BinaryExporter.NODEOP_ENDFIELDS) {
System.out.println("EOFields");
            return;
        }

        String name = (String) nodeFieldDict[nodeId].get(new Integer(fieldNum-1));
//System.out.println("name: " + name + " fieldNum: " + fieldNum);
        int idx = node.getFieldIndex(name);
//        VRMLFieldDeclaration decl = node.getFieldDeclaration(idx);

        VRMLFieldDeclaration decl = node.getFieldDeclaration(idx);

        if (decl == null) {
//            System.out.println("No field: " + idx + " for: " + node);
            System.out.println("No field: " + (fieldNum-1) + " for: " + node);
        }

        System.out.print("SF: " + decl.getName() + " = ");
        int opCode;
        int fieldType = decl.getFieldType();
        int len;
        float fval;
        double dval;

        switch(fieldType) {
            case FieldConstants.SFNODE:
                dis.read(op);

                bup = new BitUnpacker(op);

                opCode = bup.unpack(2);
                if (opCode == BinaryExporter.OP_USE) {
                    readUSE(bup);
                    // TODO: Shouldn't this exit?
                } else if (opCode != BinaryExporter.OP_NODE)
                    System.out.println("ERROR: not a node");

                readNode(bup);
                break;
            case FieldConstants.MFNODE:
                boolean reading = true;

                while(reading) {
                    dis.read(op);

                    bup = new BitUnpacker(op);

                    opCode = bup.unpack(2);
                    if (opCode != BinaryExporter.OP_NODE) {
                        System.out.println("ERROR: not a node");

                    }

                    reading = readNode(bup);
                }
                break;
            case FieldConstants.SFSTRING:
                String sval = fieldMethods[fieldType].decompressString(dis,
                    FieldConstants.SFSTRING);
System.out.print(sval);
                break;
            case FieldConstants.MFSTRING:
                len = fieldMethods[fieldType].nextLength(dis);
                if (stringArray.length < len)
                    stringArray = new String[len];

                fieldMethods[fieldType].decompressString(dis,
                    fieldType, stringArray);

                for(int i=0; i < len; i++) {
                    System.out.print(stringArray[i] + " ");
                }
                break;
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFCOLOR:
                len = 3;
                fieldMethods[fieldType].decompressFloat(dis,
                    fieldType, floatArray);

                for(int i=0; i < len; i++) {
                    System.out.print(floatArray[i] + " ");
                }
                break;
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.SFROTATION:
                len = 4;
                fieldMethods[fieldType].decompressFloat(dis,
                    fieldType, floatArray);

                for(int i=0; i < len; i++) {
                    System.out.print(floatArray[i] + " ");
                }
                break;
            case FieldConstants.SFVEC2F:
                len = 2;
                fieldMethods[fieldType].decompressFloat(dis,
                    fieldType, floatArray);

                for(int i=0; i < len; i++) {
                    System.out.print(floatArray[i] + " ");
                }
                break;
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFFLOAT:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFVEC2F:
                len = fieldMethods[fieldType].nextLength(dis);
                if (floatArray.length < len)
                    floatArray = new float[len];

                fieldMethods[fieldType].decompressFloat(dis,
                    fieldType, floatArray);

                for(int i=0; i < len; i++) {
                    System.out.print(floatArray[i] + " ");
                }
                break;
            case FieldConstants.MFINT32:
                len = fieldMethods[fieldType].nextLength(dis);
                if (intArray.length < len)
                    intArray = new int[len];

                fieldMethods[fieldType].decompressInt(dis,
                    fieldType, intArray);

                for(int i=0; i < len; i++) {
                    System.out.print(intArray[i] + " ");
                }
                break;
            case FieldConstants.SFBOOL:
                boolean bval = fieldMethods[fieldType].decompressBoolean(dis,
                    fieldType);
System.out.print(bval);
                break;
            case FieldConstants.SFFLOAT:
                fval = fieldMethods[fieldType].decompressFloat(dis,
                    fieldType);
System.out.print(fval);
                break;
            case FieldConstants.SFDOUBLE:
                dval = fieldMethods[fieldType].decompressDouble(dis,
                    fieldType);
                break;
            case FieldConstants.SFINT32:
                int ival = fieldMethods[fieldType].decompressInt(dis,
                    fieldType);
                break;
            case FieldConstants.SFTIME:
                dval = fieldMethods[fieldType].decompressDouble(dis,
                    fieldType);
                break;
            default:
                System.out.println("Unhandled type in readFields: " + fieldType);
        }
System.out.println();
        readFields(node);
    }

    /**
     * Get the number of bytes need to store numBits.
     */
    private byte getNumberOfBytes(int numBits) {
        if (numBits <= 8)
            return 1;
        else if (numBits <= 16)
            return 2;
        else if (numBits <= 24)
            return 3;
        else if (numBits <= 32)  // Doubt it will ever go past this
            return 4;
        else {
            System.out.println("BinaryExporter:  More then 32 bits numBits!?");
            return 4;
        }
    }

    /**
     * Initialize the default compressors.  These will be the spec supplied defaults.
     */
    private void initCompressors() {
        fieldMethods = new FieldDecompressor[32+1];
        BinaryFieldEncoder bfe = new BinaryFieldEncoder();
        //RangeCompressor bfe = new RangeCompressor();

        registerFieldDecompressor(FieldConstants.SFINT32, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFBOOL, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFDOUBLE, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFFLOAT, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFCOLOR, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFCOLORRGBA, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFROTATION, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFSTRING, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFTIME, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFVEC3F, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFVEC2F, 0, bfe);
        registerFieldDecompressor(FieldConstants.SFIMAGE, 0, bfe);

        registerFieldDecompressor(FieldConstants.MFINT32, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFBOOL, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFDOUBLE, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFCOLOR, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFCOLORRGBA, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFFLOAT, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFSTRING, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFTIME, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFROTATION, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFVEC3F, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFVEC2F, 0, bfe);
        registerFieldDecompressor(FieldConstants.MFIMAGE, 0, bfe);

        // TODO: Find a good maximum for this or change data structures
        nodeMethods = new NodeCompressor[128];
    }

    /**
     * Register a class for a field/compression method.
     *
     * @param fieldType For what field type, defined in FieldConstants.
     * @param method For what compression method.
     * @param compressor What class implements the FieldCompressor interface.
     */
    public void registerFieldDecompressor(int fieldType,
                                        int method,
                                        FieldDecompressor compressor) {

        if (!compressor.canSupport(fieldType, method)) {
            System.out.println(compressor + " cannot support requested fieldType/method: " +
                fieldType + "/" + method);
            return;
        }

        fieldMethods[fieldType] = compressor;
    }

    /**
     * Register a class for a node/compression method.
     *
     * @param nodeNumber For what field type, defined in FieldConstants.
     * @param method For what compression method.
     * @param compressor What class implements the FieldCompressor interface.
     */
    public void registerNodeCompressor(int nodeNumber,
                                       int method,
                                       NodeCompressor compressor) {
        if (!compressor.canSupport(nodeNumber, method)) {
            System.out.println(compressor + " cannot support requested nodeNumber/method: " +
                nodeNumber + "/" + method);
            return;
        }

        nodeMethods[nodeNumber] = compressor;
    }
}
