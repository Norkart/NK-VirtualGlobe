/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;   // Do not use *
import java.util.Iterator;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

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
 * A content handler which collects stats about the scenegraph.
 * Also collects proto definitions.
 * Also collected DEF table
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public class StatisticsCollector implements StringContentHandler,
    ProtoHandler, ScriptHandler, Comparator {

    /** The number of nodes in the file */
    private int totalNodeCount;

    /** The number of fields in the file */
    private int totalFieldCount;

    /** A mapping from nodeName to its node number */
    private HashMap nodeNum;

    /** A mapping from a defName to an assigned number */
    private HashMap defNum;
    private int lastDef;

    /** The last node number used.  Starts at 0 */
    private int lastNode;

    /** The mapping of proto names (key) to node instances (value) */
    protected HashMap protoMap;

    /** The mapping of externproto names (key) to node instances (value) */
    protected HashMap externProtoMap;

    /** Are we parsing a document */
    private boolean parsing;

    /** A mapping between nodename and number of occurances */
    private HashMap counts;

    /** A Node to field name mapping(Integer, HashSet) */
    private HashMap nodeFieldMap;

    /** Counts for each fieldType */
    private int[] fieldCounts;

    /** The maxium number of fields a node has */
    private int maxFieldCount;

    private int externProtoCount;
    private int protoCount;

    /** A stack of field count values(MutableInteger) */
    private SimpleStack fieldCountStack;

    /** A stack of field names used */
    private SimpleStack fieldNamesStack;

    /** A stack of node numbers assigned */
    private SimpleStack nodeNumStack;

    /**
     * Create a stats collector.
     *
     */
    public StatisticsCollector() {
        nodeNum = new HashMap(128);
        defNum = new HashMap(128);
        lastDef = 0;
        lastNode = 0;

        counts = new HashMap(128);
        fieldCounts = new int[31];
        fieldCountStack = new SimpleStack();
        fieldNamesStack = new SimpleStack();
        nodeNumStack = new SimpleStack();
        nodeFieldMap = new HashMap(128);
        totalNodeCount = 0;
        totalFieldCount = 0;
        parsing = false;
    }

    public HashMap getNodeTable() {
        return nodeNum;
    }

    public HashMap getFieldTable() {
        return nodeFieldMap;
    }

    public HashMap getDEFTable() {
        return defNum;
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

        Integer num = (Integer) nodeNum.get(name);
        if (num == null) {
            lastNode++;
            num = new Integer(lastNode);
            nodeNum.put(name, num);
        }

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

        totalNodeCount++;

        if (defName != null) {
            // TODO: Necessary for reading VRML files with duplicate def names
            if (defNum.get(defName) == null) {
                lastDef++;
                defNum.put(defName, new Integer(lastDef));
            }
        }

        fieldCountStack.push(new MutableInteger(0));
        fieldNamesStack.push(new HashSet());

        nodeNumStack.push(num);
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
        MutableInteger cnt = (MutableInteger) fieldCountStack.pop();
        int fieldCount = cnt.intValue();

        if (fieldCount > maxFieldCount) {
            maxFieldCount = fieldCount;
        }

        HashSet names = (HashSet) fieldNamesStack.pop();

        Integer node = (Integer) nodeNumStack.pop();
        if (node == null)
            System.out.println("***Error in end node no nodeNumStack entry");

        HashSet priorNames = (HashSet) nodeFieldMap.get(node);
        if (priorNames == null)
            nodeFieldMap.put(node, names);
        else {
            priorNames.addAll(names);
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
        totalFieldCount++;

        HashSet set = (HashSet) fieldNamesStack.peek();
        if (!set.contains(name)) {
            set.add(name);
            MutableInteger cnt = (MutableInteger) fieldCountStack.peek();
            cnt.inc();
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

        protoCount++;
        Integer num = (Integer) nodeNum.get(name);
        if (num == null) {
            nodeNum.put(name, new Integer(lastNode++));
        }
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

        externProtoCount++;
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
        System.out.println("Total Node Count: " + totalNodeCount);
        System.out.println("Total Field Count: " + totalFieldCount);
        System.out.println("Number of native Node Types: " + counts.size());
        System.out.println("Number of PROTO Node Types: " + protoCount + " total: " + (counts.size() + protoCount));
        System.out.println("Maximum number of fields in a node: " + maxFieldCount);
        System.out.println("DEFed nodes: " + lastDef);
        System.out.println();

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

    /**
     * Get the number of node types used in this file.  This is the
     * number of native nodes used.
     */
    public int getNativeNodeTypeCount() {
        return counts.size();
    }

    /**
     * Get the number of protos(internal and external) defined in this file.
     */
    public int getProtoTypeCount() {
        return protoCount + externProtoCount;
    }

    /**
     * Get the maximum number of fields used in a node(non script).
     */
     public int getMaxFieldCount() {
        return maxFieldCount;
     }

    /**
     * Get the number of DEF names.
     */
     public int getDEFCount() {
         return lastDef;
     }

    //-----------------------------------------------------------------------
    //Local Methods
    //-----------------------------------------------------------------------
}

/**
 * Mutable Integer for HashMap usage.
 */
class MutableInteger {
    private int val;

    public MutableInteger(int val) {
        this.val = val;
    }

    public int intValue() {
        return val;
    }

    public void inc() {
        val++;
    }
}
