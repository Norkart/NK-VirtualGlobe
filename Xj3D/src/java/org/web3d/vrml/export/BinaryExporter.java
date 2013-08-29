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
package org.web3d.vrml.export;

// External imports
import java.io.*;
import java.util.*;

import java.net.URL;
import java.net.MalformedURLException;

// Local imports
import org.web3d.util.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.export.compressors.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ExternalPrototypeDecl;
import org.web3d.vrml.nodes.proto.AbstractProto;

import org.web3d.vrml.parser.FieldParserFactory;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.norender.NRNodeFactory;

/**
 * An experimental binary format writer.
 *
 * Format:
 *      table describing compression method per fieldType
 *         # of table entries(8)
 *         fieldType(8), compression method(8)
 *
 *      table describing compression method for individial nodes
 *         # of table entries(16)
 *         nodeNumber(16), compression method(8)
 *
 *      scenegraph structure
 *         PROTO definitions
 *         Nodes
 *             NodeNumber(8 or 16)
 *             DEF #(16) or 0(8) for no DEF name.  1st bit(0 = no def)
 *             Fields
 *                 fieldNumber(8) 0=end of node  (Internal Xj3D index + 1)
 *                 field encoding by type
 *
 * Compression method numbers:
 *    Seperate set of assigned numbers for fieldType and node
 *    Consortium assigned numbers are 0-127, the rest are locally defineable
 *
 * FieldType Compression methods implemented
 *      All field types  0  Straight IEEE Encoding
 *      MFInt32          1  Squash Range
 *      MFFloat          1  Convert to Int, squash range
 *
 * Goals:
 *    Enable experimentation with compression methods.
 *    Avoid having experimenters change this file
 *    Enable app writers to choose different compression methods.
 *
 * Design:
 *    Have a properties file saying what class implements the compression method
 *    fieldType, method, class
 *    nodeNumber, method, class
 *
 * Each compressor class must implement either the FieldCompressor or NodeCompressor interface
 *
 * ----------------------------------
 * Proposed New Structure
 *
 * Header
 *   Binary spec version
 *   Profile
 *      length(8)
 *      data
 *   Component
 *      number of components(8)
 *         length(8)
 *         data
 *   Number of Bits used for Node Number(max 16)
 *   Number of Bits used for DEF lookup(max 32)
 *   Number of Bits used for field name
 *
 *   Node Dictionary Table
 *      num entries(32)
 *      values(UTF8)
 *   DEF Dictionary Table
 *      num entries(32)
 *      values(UTF8)
 *   Field Table
 *      num entries(32)   -- Number of field table entries
 *      num fields for this node
 *      values(UTF8)
 *
 *   Table describing compression method per fieldType
 *         # of table entries(8)
 *         fieldType(8),
 *         number of methods(8)  Cannot exceed 8 entries
 *            compression method(8)  -- Indexed into X3D Binary Spec
 *
 *   Table describing compression method for individial nodes
 *         # of table entries(16)
 *         nodeNumber(16), compression method(8)
 *
 *
 *   Proto Definitions, assign NodeNum to each Prototype.  Retain nested structures.
 *      Structure TBD
 *
 *   Import/Export Statements
 *      Structure TBD
 *
 * Main File
 *
 * Sequence of OP Codes and Data
 * OpCode(2 bits, 00 = Node, 01=USE, 10 = Comment, 11 = ROUTE
 *
 * Node
 *   NodeNum(variable)
 *   Def lookup(variable)  --> Byte Alignment(OP+NodeNum+DEF divisible by 8)
 *   Field Name Id(variable)  -->Look for end of fields marker
 *   Field Method used(3 bits) --> Byte Alignment(FieldNameID+FieldMethod)
 *      Field Method Params  --> Byte Aligned
 *   Field Data --> Byte Aligned
 *
 * USE
 *   Def lookup(variable)  --> Byte Aligned(OP+DEF)
 *
 * Comment
 *    length(14) -->  Byte Aligned(OP+length)
 *    data
 *
 * ROUTE
 *    fromNode(Def Lookup, variable)
 *    fromField(FieldNameID,variable)
 *    toNode(Def Lookup, variable)
 *    toField(FieldNameID, variable)  --> Byte Alignment(OP+fromNode+fromField+toNode+toField)
 *
 * NodeNum 0 is reserved as end of fields marker.
 * NodeNum 1 is reserved for Script nodes.  They will have their own format.
 * FieldNum 0 is reserved for end of fields marker
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public class BinaryExporter extends Exporter implements Comparator {
    private static final int SPEC_VERSION = 1;

    /** The number of Bits used for the OPCode */
    public static final int OP_BITS = 2;

    /** The number of Bits used for the FieldMethod */
    public static final int FIELD_METHOD_BITS = 3;

    // OpCodes
    public static final int OP_NODE=0;
    public static final int OP_USE=1;
    public static final int OP_COMMENT=2;
    public static final int OP_ROUTE=3;

    // Reserved NODE_OPCODES
    public static final int NODEOP_ENDFIELDS = 0;
    public static final int NODEOP_DYNAMIC = 1;

    /** The number of nodes in the file */
    private int nodeCount;

    /** The number of fields in the file */
    private int fieldCount;

    /** The output stream */
    private DataOutputStream dos;

    /** A mapping from nodeName to its node number */
    private HashMap nodeNum;

    /** A mapping from a defName to an assigned number */
    private HashMap defNum;

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** Field Parser */
    protected static VRMLFieldReader fieldParser;

    private SimpleStack currentNode;
    private SimpleStack currentField;
    private BooleanStack inMFNode;
    private IntStack fieldCnt;

    /** A mapping of node name to node.  Stores only one instance of each node. */
    private HashMap nodeMap;

    /** The mapping of proto names (key) to node instances (value) */
    protected HashMap protoMap;

    /** The mapping of externproto names (key) to node instances (value) */
    protected HashMap externProtoMap;

    /** The working stack of currently defined PROTO declarations */
    private SimpleStack protoDeclStack;

    /** The working stack of proto decls maps */
    private SimpleStack protoMapStack;

    /** Copy of the current working protype definition */
    private AbstractProto currentProto;

    /** The choosen FieldCompressors */
    private FieldCompressor[] fieldMethods;

    /** The choosen FieldCompressors */
    private NodeCompressor[] nodeMethods;

    /** The current SceneGraphCompressor active */
    private SceneGraphCompressor sgCompressor;

    /** A stack of sgCompressor's used to detect the end */
    private SimpleStack sgCompressorStack;

    /** Are we parsing a document */
    private boolean parsing;

    /** A mapping between nodename and number of occurances */
    private HashMap counts;

    /** Counts for each fieldType */
    private int[] fieldCounts;

    /** Collected stats */
    private StatisticsCollector stats;

    /** Number of bytes for the nodeOp(OP+NodeNum+DEF) */
    private int nodeOpBytes;
    private int useOpBytes;
    private int nodeNumBits;
    private int defNumBits;

    private int nodeOpBits;
    private int useOpBits;
    private int fieldOpBits;

    /** Scratch var for node ops */
    private byte[] nodeOp;
    private byte[] useOp;
    private byte[] fieldOp;

    /** Number of bits for the field marker */
    private int fieldOpBytes;
    private int fieldNumBits;

    private HashMap nodeDict;
    private HashMap defDict;
    private HashMap nodeFieldDict;

    // Maps nodeNum to fieldNames(integer, hashmap)
    HashMap nodeFieldMap;

    /**
     * Create a new exporter for binary file formats.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param stats The first pass stats.
     */
    public BinaryExporter(OutputStream os, int major, int minor, StatisticsCollector stats) {
        super(major, minor);
        this.stats = stats;

        dos = new DataOutputStream(os);

        nodeDict = stats.getNodeTable();
        defDict = stats.getDEFTable();
        nodeFieldDict = stats.getFieldTable();

        // Reserve two for endoffield and ScriptNodeTypes
        nodeNumBits = numBits(2 + stats.getNativeNodeTypeCount() + stats.getProtoTypeCount());
        defNumBits = numBits(stats.getDEFCount());

        nodeOpBits = OP_BITS + nodeNumBits + defNumBits;
        nodeOpBytes = getNumberOfBytes(nodeOpBits);

        useOpBits = OP_BITS + defNumBits;
        useOpBytes = getNumberOfBytes(useOpBits);

        // TODO: Make these the same to avoid pushback on decode.
        useOpBytes = nodeOpBytes;

        // Reserve 0 for endoffield marker, +1 for starting idxs at 0
        fieldNumBits = numBits(2 + stats.getMaxFieldCount());
        //fieldNumBits = numBits(1 + fieldDict.size());
        fieldOpBits = FIELD_METHOD_BITS + fieldNumBits;
        fieldOpBytes = getNumberOfBytes(fieldOpBits);

        writeHeader();

        nodeNum = new HashMap(128);
        defNum = new HashMap(128);

        currentNode = new SimpleStack();
        currentField = new SimpleStack();
        fieldCnt = new IntStack();
        inMFNode = new BooleanStack();
        protoDeclStack = new SimpleStack();
        protoMapStack = new SimpleStack();
        sgCompressorStack = new SimpleStack();

        nodeFactory = DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);
        nodeFactory.setSpecVersion(3,0);
        nodeFactory.setProfile("Immersive");

        counts = new HashMap(128);
        fieldCounts = new int[31];
        nodeCount = 0;
        fieldCount = 0;
        parsing = false;
        sgCompressor = null;

        FieldParserFactory fac =
            FieldParserFactory.getFieldParserFactory();

        fieldParser = fac.newFieldParser(major, minor);

        populateTables();
        initCompressors();

        nodeOp = new byte[nodeOpBytes];
        useOp = new byte[useOpBytes];
        fieldOp = new byte[fieldOpBytes];
    }

    /**
     * Register a class for a field/compression method.
     *
     * @param fieldType For what field type, defined in FieldConstants.
     * @param method For what compression method.
     * @param compressor What class implements the FieldCompressor interface.
     */
    public void registerFieldCompressor(int fieldType,
                                        int method,
                                        FieldCompressor compressor) {
        if (parsing) {
            errorReporter.warningReport("Cannot set fieldCompressor at this time.",
                                  null);
            return;
        }

        if (!compressor.canSupport(fieldType, method)) {
            errorReporter.warningReport(compressor +
                                  " cannot support requested fieldType/method: " +
                                  fieldType + "/" + method, null);
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
        if (parsing) {
            errorReporter.warningReport("Cannot set nodeCompressor at this time.", null);
            return;
        }

        if (!compressor.canSupport(nodeNumber, method)) {
            errorReporter.warningReport(compressor +
                                  " cannot support requested nodeNumber/method: " +
                                  nodeNumber + "/" + method, null);
            return;
        }

        nodeMethods[nodeNumber] = compressor;
    }

    //----------------------------------------------------------
    // ContentHandler methods
    //----------------------------------------------------------

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <CODE>startDocument()</CODE> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    public void setDocumentLocator(Locator loc) {
    }

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The VRML version of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        parsing = true;
    }

    /**
     * A profile declaration has been found in the code. IAW the X3D
     * specification, this method will only ever be called once in the lifetime
     * of the parser for this document. The name is the name of the profile
     * for the document to use.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentName The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void componentDecl(String componentName)
        throws SAVException, VRMLException {
    }

    /**
     * A META declaration has been found in the code. There may be zero
     * or more meta declarations in the file, appearing just after the
     * component declaration. Each meta declaration has a key and value
     * strings. No information is to be implied from this. It is for extra
     * data only.
     *
     * @param key The value of the key string
     * @param value The value of the value string
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void metaDecl(String key, String value)
        throws SAVException, VRMLException {
    }

    /**
     * An IMPORT declaration has been found in the document. All three
     * parameters will always be provided, regardless of whether the AS keyword
     * has been used or not. The parser implementation will automatically set
     * the local import name as needed.
     *
     * @param inline The name of the inline DEF nodes
     * @param exported The exported name from the inlined file
     * @param imported The local name to use for the exported name
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void importDecl(String inline, String exported, String imported)
        throws SAVException, VRMLException {
    }

    /**
     * An EXPORT declaration has been found in the document. Both paramters
     * will always be provided regardless of whether the AS keyword has been
     * used. The parser implementation will automatically set the exported
     * name as needed.
     *
     * @param defName The DEF name of the nodes to be exported
     * @param exported The name to be exported as
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void exportDecl(String defName, String exported)
        throws SAVException, VRMLException {
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endDocument() throws SAVException, VRMLException {
        // Write out the DEF table for size comparisons.  When we goto 2 pass then put at front

        try {
            dos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        Integer count = (Integer) counts.get(name);
        if (count != null) {
            int val = count.intValue();
            val++;
            count = new Integer(val);
            counts.put(name, count);
        } else {
            count = new Integer(1);
            counts.put(name, count);
        }

        nodeCount++;
        VRMLNode node;

        // TODO: Stop creating new BitPackers
        BitPacker packer = new BitPacker(nodeOpBytes);
        packer.pack(OP_NODE,OP_BITS);

        // TODO: Optimize by only creating one type of each node
        node = nodeFactory.createVRMLNode(name, false);

        int idx = ((Integer)nodeDict.get(name)).intValue();

        try {

            if (node == null) {
                // Need to handle proto
                currentNode.push(null);
                //dos.writeShort(1024);
                return;
            }

            currentNode.push(node);

            packer.pack(idx, nodeNumBits);
            //dos.writeByte(idx);

            if (defName != null) {
                int lastDef = ((Integer)defDict.get(defName)).intValue();
                packer.pack(lastDef, defNumBits);
            } else {
                packer.pack(0, defNumBits);
            }

            packer.getResult(nodeOp);
            for(int i=0; i < nodeOpBytes; i++) {
                dos.writeByte(nodeOp[i]);
            }
        } catch(IOException ioe) {
           ioe.printStackTrace();
        }

        if (sgCompressor != null) {
            sgCompressor.startNode(name, defName);
            sgCompressorStack.push(null);
        } else {
            if (nodeMethods[idx] != null) {
                nodeMethods[idx].reinit(dos,fieldParser);
                nodeMethods[idx].startNode(name, defName);

                if (nodeMethods[idx] instanceof SceneGraphCompressor) {
                    sgCompressor = (SceneGraphCompressor) nodeMethods[idx];
                    sgCompressorStack.push(sgCompressor);
                } else {
                    sgCompressorStack.push(null);
                }
            } else {
                sgCompressorStack.push(null);
            }
        }
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endNode() throws SAVException, VRMLException {
        VRMLNodeType node = (VRMLNodeType) currentNode.pop();

        SceneGraphCompressor sgc = (SceneGraphCompressor) sgCompressorStack.pop();
        if (sgc != null) {
            // Finished with a compressor
            sgCompressor = null;
        }

        if (sgc != null) {
            sgc.endNode();
            return;
        } else {
            int idx = ((Integer)nodeNum.get(node.getVRMLNodeName())).intValue();
            if (nodeMethods[idx] != null) {
                nodeMethods[idx].endNode();
                return;
            }
        }

        try {
            // Indicate end of fields
            for(int i=0; i < fieldOpBytes; i++)
                dos.writeByte(NODEOP_ENDFIELDS);
        } catch(IOException ioe) {
           ioe.printStackTrace();
        }
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startField(String name) throws SAVException, VRMLException {
        fieldCount++;
        VRMLNodeType node = (VRMLNodeType) currentNode.peek();
        try {
            if (node == null) {
                errorReporter.errorReport("no node for field: " + name, null);
                return;
            }

            int fieldIdx = node.getFieldIndex(name);
//            int dictIdx = ((Integer)fieldDict.get(name)).intValue();
            Integer nodeId = (Integer) nodeDict.get(node.getVRMLNodeName());

            HashMap names = (HashMap)nodeFieldMap.get(nodeId);

            if (names == null)
                errorReporter.errorReport("Error, no fieldNames for: " +
                                    node.getVRMLNodeName(), null);

            int dictIdx = ((Integer)names.get(name)).intValue() + 1;

            if (fieldIdx < 0) {
                errorReporter.errorReport("***Unknown field: " + name +
                                    " for node: " + node);
            }

            currentField.push(new Integer(fieldIdx));

            if (sgCompressor != null) {
                sgCompressor.startField(name);
                return;
            } else {
                int nidx = ((Integer)nodeNum.get(node.getVRMLNodeName())).intValue();
                if (nodeMethods[nidx] != null) {
                    nodeMethods[nidx].startField(name);
                    return;
                }
            }

            BitPacker packer = new BitPacker(fieldOpBytes);
            packer.pack(dictIdx,fieldNumBits);
//            packer.pack(fieldIdx+1,fieldNumBits);
            packer.pack(0, FIELD_METHOD_BITS);
            packer.getResult(fieldOp);

            for(int i=0; i < fieldOpBytes; i++) {
                dos.writeByte(fieldOp[i]);
            }
        } catch(IOException ioe) {
           ioe.printStackTrace();
        }
    }

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String value) throws SAVException, VRMLException {
        VRMLNodeType node = (VRMLNodeType) currentNode.peek();
        int idx = ((Integer)currentField.peek()).intValue();
        VRMLFieldDeclaration decl = node.getFieldDeclaration(idx);
        float[] fval;
        int[] ival;
        double dval;

        if (sgCompressor != null) {
            sgCompressor.fieldValue(value);
            return;
        } else {
            int nidx = ((Integer)nodeNum.get(node.getVRMLNodeName())).intValue();
            if (nodeMethods[nidx] != null) {
                nodeMethods[nidx].fieldValue(value);
                return;
            }
        }

        if (decl == null) {
            errorReporter.errorReport("No field decl for index: " + idx, null);
            return;
        }

        try {
            fieldCounts[decl.getFieldType()]++;

            switch(decl.getFieldType()) {
                case FieldConstants.SFBOOL:
                    boolean bval = fieldParser.SFBool(value);
                    fieldMethods[FieldConstants.SFBOOL].compress(dos, FieldConstants.SFBOOL, bval);
                    break;
                case FieldConstants.SFDOUBLE:
                    dval = fieldParser.SFDouble(value);
                    fieldMethods[FieldConstants.SFDOUBLE].compress(dos, FieldConstants.SFDOUBLE, dval);
                    break;
                case FieldConstants.SFFLOAT:
                    float sfval = fieldParser.SFFloat(value);
                    fieldMethods[FieldConstants.SFFLOAT].compress(dos, FieldConstants.SFFLOAT, sfval);
                    break;
                case FieldConstants.SFCOLOR:
                    fval = fieldParser.SFColor(value);
                    fieldMethods[FieldConstants.SFCOLOR].compress(dos, FieldConstants.SFCOLOR, fval);
                    break;
                case FieldConstants.SFCOLORRGBA:
                    fval = fieldParser.SFColorRGBA(value);
                    fieldMethods[FieldConstants.SFCOLORRGBA].compress(dos, FieldConstants.SFCOLORRGBA, fval);
                    break;
                case FieldConstants.SFINT32:
                    int sival = fieldParser.SFInt32(value);
                    fieldMethods[FieldConstants.SFINT32].compress(dos, FieldConstants.SFINT32, sival);
                    break;
                case FieldConstants.SFROTATION:
                    fval = fieldParser.SFRotation(value);
                    fieldMethods[FieldConstants.SFROTATION].compress(dos, FieldConstants.SFROTATION, fval);
                    break;
                case FieldConstants.SFSTRING:
                    String ssval = fieldParser.SFString(value);
                    fieldMethods[FieldConstants.SFSTRING].compress(dos, FieldConstants.SFSTRING, ssval);
                    break;
                case FieldConstants.SFTIME:
                    dval = fieldParser.SFTime(value);
                    fieldMethods[FieldConstants.SFTIME].compress(dos, FieldConstants.SFTIME, dval);
                    break;
                case FieldConstants.SFVEC3F:
                    fval = fieldParser.SFVec3f(value);
                    fieldMethods[FieldConstants.SFVEC3F].compress(dos, FieldConstants.SFVEC3F, fval);
                    break;
                case FieldConstants.SFVEC2F:
                    fval = fieldParser.SFVec2f(value);
                    fieldMethods[FieldConstants.SFVEC2F].compress(dos, FieldConstants.SFVEC2F, fval);
                    break;
                case FieldConstants.MFINT32:
                    // TODO: Need to consider the bytes as unsigned when reading in
                    ival = fieldParser.MFInt32(value);
                    fieldMethods[FieldConstants.MFINT32].compress(dos, FieldConstants.MFINT32, ival);
                    break;
                case FieldConstants.MFCOLOR:
                    fval = fieldParser.MFColor(value);
                    fieldMethods[FieldConstants.MFCOLOR].compress(dos, FieldConstants.MFCOLOR, fval);
                    break;
                case FieldConstants.MFCOLORRGBA:
                    fval = fieldParser.MFColorRGBA(value);
                    fieldMethods[FieldConstants.MFCOLORRGBA].compress(dos, FieldConstants.MFCOLORRGBA, fval);
                    break;
                case FieldConstants.MFFLOAT:
                    fval = fieldParser.MFFloat(value);
                    fieldMethods[FieldConstants.MFFLOAT].compress(dos, FieldConstants.MFFLOAT, fval);
                    break;
                case FieldConstants.MFSTRING:
                    String[] sval = fieldParser.MFString(value);
                    fieldMethods[FieldConstants.MFSTRING].compress(dos, FieldConstants.MFSTRING, sval);
                    break;
                case FieldConstants.MFROTATION:
                    fval = fieldParser.MFRotation(value);
                    fieldMethods[FieldConstants.MFROTATION].compress(dos, FieldConstants.MFROTATION, fval);
                    break;
                case FieldConstants.MFVEC3F:
                    fval = fieldParser.MFVec3f(value);
                    fieldMethods[FieldConstants.MFVEC3F].compress(dos, FieldConstants.MFVEC3F, fval);
                    break;
                case FieldConstants.MFVEC2F:
                    fval = fieldParser.MFVec2f(value);
                    fieldMethods[FieldConstants.MFVEC3F].compress(dos, FieldConstants.MFVEC2F, fval);
                    break;
                default:
                   errorReporter.errorReport("Unhandled field type: " + decl.getFieldTypeString());
                   break;
            }
        } catch(IOException ioe) {
           ioe.printStackTrace();
        }
    }

    /**
     * The value of an MFField where the underlying parser knows about how the
     * values are broken up. The parser is not required to support this
     * callback, but implementors of this interface should understand it. The
     * most likely time we will have this method called is for MFString or
     * URL lists. If called, it is guaranteed to split the strings along the
     * SF node type boundaries.
     *
     * @param values The list of string representing the values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] values) throws SAVException, VRMLException {
        VRMLNodeType node = (VRMLNodeType) currentNode.peek();
        int idx = ((Integer)currentField.peek()).intValue();
        VRMLFieldDeclaration decl = node.getFieldDeclaration(idx);
        float[] fval;
        int[] ival;
        double dval;

        if (sgCompressor != null) {
            sgCompressor.fieldValue(values);
            return;
        } else {
            int nidx = ((Integer)nodeNum.get(node.getVRMLNodeName())).intValue();
            if (nodeMethods[nidx] != null) {
                nodeMethods[nidx].fieldValue(values);
                return;
            }
        }

        if (decl == null) {
            errorReporter.errorReport("No field decl for index: " + idx, null);
            return;
        }

        int fieldType = decl.getFieldType();
        try {
            fieldCounts[fieldType]++;

            switch(decl.getFieldType()) {
                case FieldConstants.SFINT32:
                case FieldConstants.SFFLOAT:
                case FieldConstants.SFTIME:
                case FieldConstants.SFDOUBLE:
                case FieldConstants.SFLONG:
                case FieldConstants.SFBOOL:
                case FieldConstants.SFSTRING:
                    errorReporter.warningReport("Field not parsable as String[] " + decl);
                    break;

                case FieldConstants.MFINT32:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFInt32(values));
                    break;
                case FieldConstants.MFTIME:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFTime(values));
                    break;
                case FieldConstants.MFDOUBLE:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFDouble(values));
                    break;
                case FieldConstants.MFLONG:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFLong(values));
                    break;
                case FieldConstants.SFROTATION:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.SFRotation(values));
                    break;
                case FieldConstants.MFROTATION:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFRotation(values));
                    break;
                case FieldConstants.MFBOOL:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFRotation(values));
                    break;
                case FieldConstants.MFFLOAT:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFFloat(values));
                    break;
                case FieldConstants.SFVEC2F:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.SFVec2f(values));
                    break;
                case FieldConstants.SFVEC3F:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.SFVec3f(values));
                    break;
                case FieldConstants.SFCOLOR:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.SFColor(values));
                    break;
                case FieldConstants.MFVEC2F:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFVec2f(values));
                    break;
                case FieldConstants.MFVEC3F:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFVec3f(values));
                    break;
                case FieldConstants.MFCOLOR:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFColor(values));
                    break;
                case FieldConstants.SFVEC3D:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.SFVec3d(values));
                    break;
                case FieldConstants.MFVEC3D:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFVec3d(values));
                    break;
                case FieldConstants.MFSTRING:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFString(values));
                    break;
                case FieldConstants.SFIMAGE:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.SFImage(values));
                    break;
                case FieldConstants.MFIMAGE:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFImage(values));
                    break;
                case FieldConstants.SFCOLORRGBA:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.SFColorRGBA(values));
                    break;
                case FieldConstants.MFCOLORRGBA:
                    fieldMethods[fieldType].compress(dos, fieldType, fieldParser.MFColorRGBA(values));
                    break;
                case FieldConstants.SFNODE:
                case FieldConstants.MFNODE:
                    // in either case, this will be the value "NULL", so just
                    // ignore it completely.
                    break;

                default:
                    errorReporter.errorReport("Unknown field type provided!" + fieldType);
            }
        } catch(IOException ioe) {
           ioe.printStackTrace();
        }

    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void useDecl(String defName) throws SAVException, VRMLException {
        // TODO: Stop creating new BitPackers
        BitPacker packer = new BitPacker(nodeOpBytes);
        packer.pack(OP_USE,OP_BITS);

        int idx = ((Integer)defDict.get(defName)).intValue();
        packer.pack(idx, defNumBits);

        try {
            packer.getResult(useOp);
            for(int i=0; i < useOpBytes; i++) {
                dos.writeByte(useOp[i]);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endField() throws SAVException, VRMLException {
        int idx = ((Integer)currentField.pop()).intValue();
        VRMLNodeType node = (VRMLNodeType) currentNode.peek();
/*
        VRMLFieldDeclaration decl = node.getFieldDeclaration(idx);

        // TODO: Seems like XML and UTF8 handle endFields different.  XML is
        // generating them more often.
System.out.println("decl: " + decl);
        if (decl.getFieldType() == FieldConstants.MFNODE) {
            try {
    System.out.println("EOF2");
                // Indicate end of fields
                for(int i=0; i < nodeOpBytes; i++)
                    dos.writeByte(NODEOP_ENDFIELDS);
            } catch(IOException ioe) {
               ioe.printStackTrace();
            }
        }
*/
    }

    //-----------------------------------------------------------------------
    //Methods for interface RouteHandler
    //-----------------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNode The name of the DEF of the source node
     * @param srcField The name of the field to route values from
     * @param destNode The name of the DEF of the destination node
     * @param destField The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void routeDecl(String srcNode,
                          String srcField,
                          String destNode,
                          String destField)
        throws SAVException, VRMLException {

    }

    //----------------------------------------------------------
    // ScriptHandler methods
    //----------------------------------------------------------

    /**
     * Notification of the start of a script declaration. All calls between
     * now and the corresponding {@link #endScriptDecl} call belong to this
     * script node. This method will be called <I>after</I> the ContentHandler
     * <CODE>startNode()</CODE> method call. All DEF information is contained
     * in that method call and this just signifies the start of script
     * processing so that we know to treat the field parsing callbacks a
     * little differently.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startScriptDecl() throws SAVException, VRMLException {
    }

    /**
     * Notification of the end of a script declaration. This is guaranteed to
     * be called before the ContentHandler <CODE>endNode()</CODE> callback.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endScriptDecl() throws SAVException, VRMLException {
    }

    /**
     * Notification of a script's field declaration. This is used for all
     * fields except <CODE>url</CODE>, <CODE>mustEvaluate</CODE> and
     * <CODE>directOutput</CODE> fields. These fields use the normal field
     * callbacks of {@link ContentHandler}.
     * <p>
     * If the current parsing is in a proto and the field "value" is defined
     * with an IS statement then the value returned here is null. There will
     * be a subsequent call to the ProtoHandlers <CODE>protoIsDecl()</CODE>
     * method with the name of the field included.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void scriptFieldDecl(int access,
                                String type,
                                String name,
                                Object value)
        throws SAVException, VRMLException {

    }

    //----------------------------------------------------------
    // ProtoHandler methods
    //----------------------------------------------------------
    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startProtoDecl(String name)
        throws SAVException, VRMLException {

        PrototypeDecl proto = new PrototypeDecl(name,
                                                majorVersion,
                                                minorVersion,
                                                null);

        protoMap.put(name, proto);
        protoDeclStack.push(proto);

        currentProto = proto;
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #startProtoBody()} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endProtoDecl()
        throws SAVException, VRMLException {
    }

    /**
     * Notification of a proto's field declaration. This is used for both
     * external and ordinary protos. Externprotos don't allow the declaration
     * of a value for the field. In this case, the parameter value will be
     * null.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException {

        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        currentProto.appendField(field);
    }

    /**
     * Notification of a field value uses an IS statement.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void protoIsDecl(String fieldName)
        throws SAVException, VRMLException {
    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scenegraph information.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startProtoBody() throws SAVException, VRMLException {

        protoMapStack.push(protoMap);

        protoMap = new HashMap();
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endProtoBody() throws SAVException, VRMLException {
        if(protoMapStack.size() > 0) {
            // Now replace the data structures for this level
            protoMap = (HashMap)protoMapStack.pop();
        } else {
            protoMap = new HashMap();
        }
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void startExternProtoDecl(String name)
        throws SAVException, VRMLException {

        ExternalPrototypeDecl proto = new ExternalPrototypeDecl(name,
                                                                majorVersion,
                                                                minorVersion,
                                                                null);

        // by spec, a new proto will trash the previous definition. Do it now.
        externProtoMap.put(name, proto);
        currentProto = proto;
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #externProtoURI} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endExternProtoDecl()
        throws SAVException, VRMLException {

    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list as an array of URI strings. The calling application is required to
     * interpet the incoming string. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
    }

    //-------------------------------------------------------------------
    // Methods for the Comparator interface
    //-------------------------------------------------------------------
    public int compare(Object a, Object b) {
        Integer aval = (Integer) ((Map.Entry)a).getValue();
        Integer bval = (Integer) ((Map.Entry)b).getValue();

        return bval.intValue() - aval.intValue();

    }

    public boolean equals(Object a) {
        return (a==this);
    }


    public void printStats() {
        errorReporter.messageReport("Node Count: " + nodeCount);
        errorReporter.messageReport("Field Count: " + fieldCount);
        errorReporter.messageReport("Node Type Count: " + counts.size());
        errorReporter.messageReport();

        Set entrySet = counts.entrySet();
        Iterator itr2 = entrySet.iterator();
        String key;
        Integer val;
        Map.Entry entry[] = new Map.Entry[entrySet.size()];
        int i=0;

        while(itr2.hasNext()) {
            entry[i++] = (Map.Entry) itr2.next();
            //System.out.println(entry.getValue() + " " + entry.getKey());
        }

        Arrays.sort(entry,this);
        for(i=0; i < entry.length; i++) {
           System.out.println(entry[i]);
        }
/*
        System.out.println("SFINT32: " + fieldCounts[1]);
        System.out.println("MFINT32: " + fieldCounts[2]);
        System.out.println("SFFLOAT: " + fieldCounts[3]);
        System.out.println("MFFLOAT: " + fieldCounts[4]);
        System.out.println("SFDOUBLE: " + fieldCounts[5]);
        System.out.println("MFDOUBLE: " + fieldCounts[6]);
        System.out.println("SFTIME: " + fieldCounts[7]);
        System.out.println("MFTIME: " + fieldCounts[8]);
        System.out.println("SFNODE: " + fieldCounts[9]);
        System.out.println("MFNODE: " + fieldCounts[10]);
        System.out.println("SFVEC2F: " + fieldCounts[11]);
        System.out.println("MFVEC2F: " + fieldCounts[12]);
        System.out.println("SFVEC3F: " + fieldCounts[13]);
        System.out.println("MFVEC3F: " + fieldCounts[14]);
        System.out.println("SFVEC3D: " + fieldCounts[15]);
        System.out.println("MFVEC3D: " + fieldCounts[16]);
        System.out.println("SFIMAGE: " + fieldCounts[17]);
        System.out.println("MFIMAGE: " + fieldCounts[18]);
        System.out.println("SFLONG: " + fieldCounts[19]);
        System.out.println("MFLONG: " + fieldCounts[20]);
        System.out.println("SFBOOL: " + fieldCounts[21]);
        System.out.println("MFBOOL: " + fieldCounts[22]);
        System.out.println("SFSTRING: " + fieldCounts[23]);
        System.out.println("MFSTRING: " + fieldCounts[24]);
        System.out.println("SFROTATION: " + fieldCounts[25]);
        System.out.println("MFROTATION: " + fieldCounts[26]);
        System.out.println("SFCOLOR: " + fieldCounts[27]);
        System.out.println("MFCOLOR: " + fieldCounts[28]);
        System.out.println("SFCOLORRGBA: " + fieldCounts[29]);
        System.out.println("MFCOLORRGBA: " + fieldCounts[30]);

        CompressionTools.printStats();
*/
    }

    //-----------------------------------------------------------------------
    //Local Methods
    //-----------------------------------------------------------------------

    /**
     * Initialize the default compressors.  These will be the spec supplied defaults.
     */
    private void initCompressors() {
        fieldMethods = new FieldCompressor[32+1];
        BinaryFieldEncoder bfe = new BinaryFieldEncoder();
        //RangeCompressor bfe = new RangeCompressor();

        registerFieldCompressor(FieldConstants.SFINT32, 0, bfe);
        registerFieldCompressor(FieldConstants.SFBOOL, 0, bfe);
        registerFieldCompressor(FieldConstants.SFDOUBLE, 0, bfe);
        registerFieldCompressor(FieldConstants.SFFLOAT, 0, bfe);
        registerFieldCompressor(FieldConstants.SFCOLOR, 0, bfe);
        registerFieldCompressor(FieldConstants.SFCOLORRGBA, 0, bfe);
        registerFieldCompressor(FieldConstants.SFROTATION, 0, bfe);
        registerFieldCompressor(FieldConstants.SFSTRING, 0, bfe);
        registerFieldCompressor(FieldConstants.SFTIME, 0, bfe);
        registerFieldCompressor(FieldConstants.SFVEC3F, 0, bfe);
        registerFieldCompressor(FieldConstants.SFVEC2F, 0, bfe);
        registerFieldCompressor(FieldConstants.SFIMAGE, 0, bfe);

        registerFieldCompressor(FieldConstants.MFINT32, 0, bfe);
        registerFieldCompressor(FieldConstants.MFBOOL, 0, bfe);
        registerFieldCompressor(FieldConstants.MFDOUBLE, 0, bfe);
        registerFieldCompressor(FieldConstants.MFCOLOR, 0, bfe);
        registerFieldCompressor(FieldConstants.MFCOLORRGBA, 0, bfe);
        registerFieldCompressor(FieldConstants.MFFLOAT, 0, bfe);
        registerFieldCompressor(FieldConstants.MFSTRING, 0, bfe);
        registerFieldCompressor(FieldConstants.MFTIME, 0, bfe);
        registerFieldCompressor(FieldConstants.MFROTATION, 0, bfe);
        registerFieldCompressor(FieldConstants.MFVEC3F, 0, bfe);
        registerFieldCompressor(FieldConstants.MFVEC2F, 0, bfe);
        registerFieldCompressor(FieldConstants.MFIMAGE, 0, bfe);

        // TODO: Find a good maximum for this or change data structures
        nodeMethods = new NodeCompressor[128];
/*
        NormalCompressor nc = new NormalCompressor();
        PositionInterpolatorCompressor pic = new PositionInterpolatorCompressor();
        OrientationInterpolatorCompressor oic = new OrientationInterpolatorCompressor();
        CoordinateInterpolatorCompressor cic = new CoordinateInterpolatorCompressor();
        IndexedFaceSetCompressor ifsc = new IndexedFaceSetCompressor();
        CoordinateCompressor cc = new CoordinateCompressor();
        TextureCoordinateCompressor tcc = new TextureCoordinateCompressor();

        registerNodeCompressor(((Integer)nodeNum.get("Normal")).intValue(),0,nc);
        registerNodeCompressor(((Integer)nodeNum.get("PositionInterpolator")).intValue(),0,pic);
        registerNodeCompressor(((Integer)nodeNum.get("OrientationInterpolator")).intValue(),0,oic);
        registerNodeCompressor(((Integer)nodeNum.get("CoordinateInterpolator")).intValue(),0,cic);
        registerNodeCompressor(((Integer)nodeNum.get("IndexedFaceSet")).intValue(),0,ifsc);
        registerNodeCompressor(((Integer)nodeNum.get("Coordinate")).intValue(),0,cc);
        registerNodeCompressor(((Integer)nodeNum.get("TextureCoordinate")).intValue(),0,tcc);
*/
    }

    private void populateTables() {
        byte lastField=1;
        nodeNum.put("Anchor",new Integer(2));
        nodeNum.put("Appearance",new Integer(3));
        nodeNum.put("AudioClip",new Integer(4));
        nodeNum.put("Background",new Integer(5));
        nodeNum.put("Billboard",new Integer(6));
        nodeNum.put("Box",new Integer(7));
        nodeNum.put("Arc2D",new Integer(8));
        nodeNum.put("ArcClose2D",new Integer(9));
        nodeNum.put("Circle2D",new Integer(10));
        nodeNum.put("Disk2D",new Integer(11));
        nodeNum.put("Polyline2D",new Integer(12));
        nodeNum.put("Polypoint2D",new Integer(13));
        nodeNum.put("Rectangle2D",new Integer(14));
        nodeNum.put("TriangleSet2D",new Integer(15));
        nodeNum.put("Collision",new Integer(16));
        nodeNum.put("ColorInterpolator",new Integer(17));
        nodeNum.put("Color",new Integer(18));
        nodeNum.put("Cone",new Integer(19));
        nodeNum.put("Coordinate",new Integer(20));
        nodeNum.put("CoordinateInterpolator",new Integer(21));
        nodeNum.put("Cylinder",new Integer(22));
        nodeNum.put("CylinderSensor",new Integer(23));
        nodeNum.put("DirectionalLight",new Integer(24));
        nodeNum.put("ElevationGrid",new Integer(25));
        nodeNum.put("Extrusion",new Integer(26));
        nodeNum.put("FillProperties",new Integer(27));
        nodeNum.put("Fog",new Integer(28));
        nodeNum.put("FontStyle",new Integer(29));
        nodeNum.put("Group",new Integer(30));
        nodeNum.put("ImageTexture",new Integer(31));
        nodeNum.put("IndexedFaceSet",new Integer(32));
        nodeNum.put("IndexedLineSet",new Integer(33));
        nodeNum.put("Inline",new Integer(34));
        nodeNum.put("KeySensor",new Integer(35));
        nodeNum.put("LineProperties",new Integer(36));
        nodeNum.put("LoadSensor",new Integer(37));
        nodeNum.put("LOD",new Integer(38));
        nodeNum.put("Material",new Integer(39));
        nodeNum.put("MovieTexture",new Integer(40));
        nodeNum.put("MultiTexture",new Integer(41));
        nodeNum.put("NavigationInfo",new Integer(42));
        nodeNum.put("Normal",new Integer(43));
        nodeNum.put("NormalInterpolator",new Integer(44));
        nodeNum.put("OrientationInterpolator",new Integer(45));
        nodeNum.put("PixelTexture",new Integer(46));
        nodeNum.put("PlaneSensor",new Integer(47));
        nodeNum.put("PointLight",new Integer(48));
        nodeNum.put("PointSet",new Integer(49));
        nodeNum.put("PositionInterpolator",new Integer(50));
        nodeNum.put("ProximitySensor",new Integer(51));
        nodeNum.put("ScalarInterpolator",new Integer(52));
        nodeNum.put("Script",new Integer(53));
        nodeNum.put("Shape",new Integer(54));
        nodeNum.put("Sound",new Integer(55));
        nodeNum.put("Sphere",new Integer(56));
        nodeNum.put("SphereSensor",new Integer(57));
        nodeNum.put("SpotLight",new Integer(58));
        nodeNum.put("StringSensor",new Integer(59));
        nodeNum.put("Switch",new Integer(60));
        nodeNum.put("Text",new Integer(61));
        nodeNum.put("TextureCoordinate",new Integer(62));
        nodeNum.put("TextureTransform",new Integer(63));
        nodeNum.put("TimeSensor",new Integer(64));
        nodeNum.put("TouchSensor",new Integer(65));
        nodeNum.put("Transform",new Integer(66));
        nodeNum.put("TriangleFanSet",new Integer(67));
        nodeNum.put("TriangleSet",new Integer(68));
        nodeNum.put("TriangleStripSet",new Integer(69));
        nodeNum.put("Viewpoint",new Integer(70));
        nodeNum.put("VisibilitySensor",new Integer(71));
        nodeNum.put("WorldInfo",new Integer(72));
        nodeNum.put("EspduTransform",new Integer(73));
        nodeNum.put("ReceiverPdu",new Integer(74));
        nodeNum.put("SignalPdu",new Integer(75));
        nodeNum.put("TransmitterPdu",new Integer(76));
        nodeNum.put("GeoCoordinate",new Integer(77));
        nodeNum.put("GeoElevationGrid",new Integer(78));
        nodeNum.put("GeoInline",new Integer(79));
        nodeNum.put("GeoLocation",new Integer(80));
        nodeNum.put("GeoLOD",new Integer(81));
        nodeNum.put("GeoMetadata",new Integer(82));
        nodeNum.put("GeoOrigin",new Integer(83));
        nodeNum.put("GeoPositionInterpolator",new Integer(84));
        nodeNum.put("GeoTouchSensor",new Integer(85));
        nodeNum.put("GeoViewpoint",new Integer(86));
        nodeNum.put("Displacer",new Integer(87));
        nodeNum.put("Humanoid",new Integer(88));
        nodeNum.put("Joint",new Integer(89));
        nodeNum.put("Segment",new Integer(90));
        nodeNum.put("Site",new Integer(91));
        nodeNum.put("Contour2D",new Integer(92));
        nodeNum.put("CoordinateDeformer",new Integer(93));
        nodeNum.put("NurbsCurve",new Integer(94));
        nodeNum.put("NurbsCurve2D",new Integer(95));
        nodeNum.put("NurbsGroup",new Integer(96));
        nodeNum.put("NurbsPositionInterpolator",new Integer(97));
        nodeNum.put("NurbsSurface",new Integer(98));
        nodeNum.put("NurbsTextureSurface",new Integer(99));
        nodeNum.put("ContourPolyline2D",new Integer(100));
        nodeNum.put("TrimmedSurface",new Integer(101));
    }

    /**
     * Determine the number of bits needed to store this value.
     *
     * @param val The maximum value
     * @return The number of bits to store 0-val.
     */
    public int numBits(int val) {
        int nbits=0;

        while(val > 0) {
            val >>= 1;
            nbits++;
        }

        return nbits;
    }

    private void writeHeader() {
        int lastSize;

        try {
            dos.writeByte(SPEC_VERSION);
            dos.writeByte(nodeNumBits);
            dos.writeByte(defNumBits);
            dos.writeByte(fieldNumBits);

            lastSize = dos.size();

            dos.writeInt(nodeDict.size());
            Set keys = nodeDict.entrySet();
            Iterator itr = keys.iterator();
            String[] dictNames = new String[nodeDict.size() + 1];

            Map.Entry entry;
            while(itr.hasNext()) {
                entry = (Map.Entry) itr.next();
                dictNames[((Integer)entry.getValue()).intValue()] = (String)entry.getKey();
            }

            int size = dictNames.length;
            for(int i=1; i < size; i++) {
                dos.writeUTF(dictNames[i]);
            }

            lastSize = dos.size();

            keys = defDict.entrySet();
            itr = keys.iterator();

            dos.writeInt(defDict.size());
            dictNames = new String[defDict.size() + 1];

            while(itr.hasNext()) {
                entry = (Map.Entry) itr.next();
                dictNames[((Integer)entry.getValue()).intValue()] = (String)entry.getKey();
            }

            size = dictNames.length;

            for(int i=1; i < size; i++) {
                dos.writeUTF(dictNames[i]);
            }

            lastSize = dos.size();

            keys = nodeFieldDict.entrySet();
            itr = keys.iterator();

            dos.writeInt(nodeFieldDict.size());
            String[][] nodeFieldNames = new String[nodeFieldDict.size() + 1][];
            org.web3d.util.HashSet fields;
            HashMap fieldMap;
            nodeFieldMap = new HashMap();
            Integer nodeNum;

            while(itr.hasNext()) {
                entry = (Map.Entry) itr.next();

                fields = (org.web3d.util.HashSet) entry.getValue();
                nodeNum = (Integer)entry.getKey();

                int nn = nodeNum.intValue();
                nodeFieldNames[nn] = new String[fields.size()];
                fields.toArray(nodeFieldNames[nn]);
                fieldMap = new HashMap();
                for(int i=0; i < nodeFieldNames[nn].length; i++) {
                    fieldMap.put(nodeFieldNames[nn][i],new Integer(i));
                }
                nodeFieldMap.put(nodeNum, fieldMap);
            }

            size = nodeFieldNames.length;
            int len;

            for(int i=1; i < size; i++) {
                len = nodeFieldNames[i].length;
                dos.writeByte(len);
                for(int j=0; j < len; j++) {
                    dos.writeUTF(nodeFieldNames[i][j]);
                }
            }

            lastSize = dos.size();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
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
            errorReporter.errorReport("BinaryExporter:  More then 32 bits numBits!?", null);
            return 4;
        }
    }
}
