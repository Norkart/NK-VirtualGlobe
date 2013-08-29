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

// Exteranl imports
import java.io.*;
import java.util.*;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.net.URL;
import java.net.MalformedURLException;

// Local imports
import org.web3d.util.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ExternalPrototypeDecl;
import org.web3d.vrml.nodes.proto.AbstractProto;

import org.web3d.vrml.parser.FieldParserFactory;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.norender.NRNodeFactory;

import org.web3d.x3d.jaxp.X3DConstants;

/**
 * X3D XML encoding Expoter.
 *
 * WARNING: Do not change the location of a push or pop unless you really test the results.
 *
 *
 * This class must track when to drop a </field> tag.  It ends when:
 * <ul>
 * <li>a value is specified</li>
 * <li>another fieldDecl is found on the same level</li>
 * <li>a field is found on the same level</li>
 * <li>the script/protoInterface ends</li>
 * </ul>
 * <p>
 *     Maintain a level variable
 *         inc on each new node
 *         dec on each end of node
 * <p>
 *     Maintain a stack of level variables tracking fieldDecl
 *         push on a fieldDecl
 *         pop on a endFieldDecl(above rules)
 * <p>
 *     To handle the fact that IS elements must be declared first we have
 *     a stack of StringBuffers where IS information is kept per node.
 *         push on startNode
 *         pop and prepend on endNode
 *<p>
 *     To handle ProtoInstance fieldValues being fieldValue element instead of attribute
 *     we need to know when we are inside a protoInstance.
 *        push on startNode whether this is a protoInstance
 *        pop on endNode
 *
 *
 * TODO:
 *    convert VRML97 Collision.collide to isActive
 *    fix double loop on converting training.wrl
 *    fix ' exporting in MountHood_VRML conversion
 *    fix containerField for things like CADFace/shape
 *
 * @author Alan Hudson
 * @version $Revision: 1.20 $
 */
public class X3DXMLExporter extends Exporter {
    /** Name of the property file holding the default container fields */
    protected static final String CONTAINER_PROPS_FILE =
        "XMLcontainerfields.properties";

    /** String constant of the containerField attribute */
    protected static final String CONTAINER_ATTR = "containerField";

    // How many spaces should we indent per level
    public static final int INDENT_SIZE=4;

    /** The output stream */
    private PrintWriter pw;

    /** A mapping from nodeName to its node number */
    private HashMap nodeNum;

    /** A mapping from a defName to an assigned number */
    private HashMap defTable;
    private HashMap defRemapTable;
    private HashMap defNumTable;

    private int lastDef;

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** Field Parser */
    protected static VRMLFieldReader fieldParser;

    /** Node or Abstract Proto */
    private SimpleStack currentNode;

    /** The current node Name */
    private String currentNodeName;

    private SimpleStack currentField;
    /** What type of field are we in, 0=MFNode, 1=SFNode, (not nodes)2=SF*, 3=MF* */
    private IntStack inFieldType;
    /** A count of the number of fields processed for this node */
    private IntStack fieldCnt;
    /** A count of the number of nodes processed for this MFNode */
    private IntStack nodeCnt;
    /** A stack of IS connections */
    private SimpleStack currentIS;

    /** Are we inside a cdata output block(url) */
    private boolean inCData;

    /** The current ident level */
    private int ilevel;

    /** How many levels of nodes are we in */
    private int nlevel;

    /** A stack tracking fieldDecalaration levels */
    private IntStack fieldDeclCnt;

    /** String representation of n ident spaces */
    private String istring;

    /** Are we upgrading from VRML to X3D */
    private boolean upgrading;

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

    /** Is the current node a protoInstance */
    private BooleanStack isProto;

    /** Copy of the current working script definition */
    protected VRMLScriptNodeType currentScript;

    /** The current nodes attributes as a StringBuffer */
    private SimpleStack attribs;

    /** A buffer containg the current nodes attributes */
    private StringBuffer currentAttribs;

    /** The current node's SFNode/MFNode children as a StringBuffer */
    private SimpleStack nodes;

    /** A buffer containg the current nodes nodes */
    private StringBuffer currentNodes;

    /** Was the last token a field, for IS nodeCnt popping */
    private boolean lastStartField;

    /** Was the last token an IS */
    private boolean lastIS;

    /** Did we get a field value for the last field */
    private boolean gotFieldValue;

    /** The major version */
    private int major;

    /** The minor version */
    private int minor;

    /** Property for container fields */
    private Properties containerFields;

    /** The number of significant digits when printing floats, -1 not to change */
    protected int sigDigits;

    /**
     * Public Constructor.
     *
     * @param os The stream to send output to.
     * @param major The major version number of the spec to output
     * @param minor The minor version number of the spec to output
     * @param reporter The error reporter to write messages to
     */
    public X3DXMLExporter(OutputStream os,
                          int major,
                          int minor,
                          int sigDigits,
                          ErrorReporter reporter) {

        super(major, minor, reporter);

        pw = new PrintWriter(os,false);

        currentNode = new SimpleStack();
        currentField = new SimpleStack();
        currentIS = new SimpleStack();
        attribs = new SimpleStack();
        nodes = new SimpleStack();
        fieldCnt = new IntStack();
        nodeCnt = new IntStack();
        inFieldType = new IntStack();
        protoDeclStack = new SimpleStack();
        protoMapStack = new SimpleStack();
        fieldDeclCnt = new IntStack();
        isProto = new BooleanStack();
        containerFields = new Properties();

        nodeFactory = DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);

        nodeFactory.setSpecVersion(major, minor);
        nodeFactory.setProfile("Immersive");

        FieldParserFactory fac =
            FieldParserFactory.getFieldParserFactory();

        fieldParser = fac.newFieldParser(major, minor);

        ilevel = 0;
        nlevel = 0;
        inCData=false;
        upgrading = false;
        lastStartField = false;
        lastIS = false;
        gotFieldValue = true;
        StringBuffer buff = new StringBuffer();
        for(int i=0; i < INDENT_SIZE; i++) {
            buff.append(' ');
        }

        istring = buff.toString();
        nodeMap = new HashMap(50);
        protoMap = new HashMap();
        externProtoMap = new HashMap();
        defTable = new HashMap();
        defRemapTable = new HashMap();
        defNumTable = new HashMap();
    }

    /**
     * Public Constructor.
     *
     * @param os The stream to send output to.
     * @param major The major version number of the spec to output
     * @param minor The minor version number of the spec to output
     * @param reporter The error reporter to write messages to
     */
    public X3DXMLExporter(OutputStream os,
                          int major,
                          int minor,
                          ErrorReporter reporter) {

        this(os, major, minor, -1, reporter);
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

        throws SAVException, VRMLException{

        if(version.equals("V2.0")) {
            upgrading = true;
            errorReporter.messageReport("Upgrading content from VRML to X3D");
        }

        // Parse the number string looking for the version minor number.
        int dot_index = version.indexOf('.');
        String major_num = version.substring(1,dot_index);
        String minor_num = version.substring(dot_index + 1);

        // Should this look for a badly formatted number here or just
        // assume the parsing beforehad has correctly identified something
        // already dodgy?
        major = Integer.parseInt(major_num);
        int new_minor = Integer.parseInt(minor_num);

        if (containerFields.size() == 0 || new_minor != minor)
            loadContainerProperties(version.substring(1));
        minor = new_minor;

        nodeFactory.setSpecVersion(major, minor);

        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.print("<!DOCTYPE X3D PUBLIC ");
        if (upgrading) {
            String publicId = getPublicId(major,minor);
            String systemId = getSystemId(major,minor);

            // Finish Doctype
            pw.print("\"");
            pw.print(publicId);
            pw.print("\" \"");
            pw.print(systemId);
            pw.println("\">\n<X3D profile=\"Immersive\">\n");
            pw.println("<Scene>");
        }
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
        throws SAVException, VRMLException{

        nodeFactory.setProfile(profileName);

        String publicId = getPublicId(major,minor);
        String systemId = getSystemId(major,minor);

        // Finish Doctype
        pw.print("\"");
        pw.print(publicId);
        pw.print("\" \"");
        pw.print(systemId);
        pw.print("\">\n<X3D profile=\"");
        pw.print(profileName);
        pw.print("\">\n");

        pw.println("<Scene>");
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
        throws SAVException, VRMLException{

        // Parse componentInfo in the form of componentName:level
        int pos = componentName.indexOf(":");

        if (pos < 0) {
            errorReporter.fatalErrorReport("Invalid component info: " +
                                     componentName, null);
            return;
        }

        String compName = componentName.substring(0,pos);
        String compLevel = componentName.substring(pos+1);

        nodeFactory.addComponent(compName, Integer.parseInt(compLevel));

        pw.print("<component name=\"");
        pw.print(compName);
        pw.print("\" level=\"");
        pw.print(compLevel);
        pw.println("\" />");
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
        throws SAVException, VRMLException{

        // Preserve META information as comments
        pw.print("<meta name=\"");
        pw.print(key);
        pw.print("=\"");
        pw.print(value);
        pw.println("\"");
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
        throws SAVException, VRMLException{

        // TODO: Change to XML
        pw.print("IMPORT ");
        pw.print(inline);
        pw.print(".");
        pw.print(exported);
        pw.print(" AS ");
        pw.println(imported);
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
        throws SAVException, VRMLException{

        // TODO: Change to XML
        pw.print("EXPORT ");
        pw.print(defName);
        pw.print(" AS ");
        pw.print(exported);
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
        pw.println("</Scene>");
        pw.println("</X3D>");
        pw.flush();
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
        throws SAVException, VRMLException{

        VRMLNode node;
        lastStartField = false;

        currentNodes = new StringBuffer();
        currentNodeName = name;
        currentAttribs = new StringBuffer();
        nlevel++;

        if (!inFieldType.isEmpty()) {
            int type = inFieldType.peek();

            int nCnt = nodeCnt.pop();
            nodeCnt.push(++nCnt);
        }
        lastIS=false;
        node = (VRMLNode) nodeMap.get(name);

        if(protoMap.containsKey(name) || externProtoMap.containsKey(name)) {
            isProto.push(true);
        } else {
            isProto.push(false);
        }

        if (node == null) {
            if(protoMap.containsKey(name) || externProtoMap.containsKey(name)) {
                PrototypeDecl proto_def = null;

                proto_def = (PrototypeDecl)protoMap.get(name);

                if(proto_def == null) {
                    ExternalPrototypeDecl eproto_def = (ExternalPrototypeDecl)externProtoMap.get(name);
                    currentNode.push(eproto_def);
                } else {
                    currentNode.push(proto_def);
                }
            } else {
                node = nodeFactory.createVRMLNode(name, false);
                nodeMap.put(name, node);
                currentNode.push(node);
            }
        } else {
            currentNode.push(node);
        }

        fieldCnt.push(0);
        ilevel++;

        if (defName != null) {
            if (defTable.get(defName) != null) {
                Integer num = (Integer) defNumTable.get(defName);

                if (num != null)
                    num = new Integer(num.intValue() + 1);
                else
                    num = new Integer(1);

                defNumTable.put(defName, num);
                String newName = defName + "_DUP" + num.intValue();
                errorReporter.warningReport("Duplicate DEF detected, renamed to: " +
                                      newName, null);
                defRemapTable.put(defName, newName);
                defName = newName;
            } else
                defTable.put(defName, name);

            currentAttribs.append(" DEF=\"");
            currentAttribs.append(defName);
            currentAttribs.append("\"");
        }
        nodes.push(currentNodes);
        attribs.push(currentAttribs);
        currentIS.push(new StringBuffer());
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void endNode()
        throws SAVException, VRMLException{
        ilevel--;
        nlevel--;

        Object node = currentNode.pop();

        int cnt = fieldCnt.pop();

        if (cnt > 0 && !lastIS && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (type==0) {
                    indent();
                    ilevel--;
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                }
            }
        }

        indent();
        String name=null;

        currentAttribs = (StringBuffer) attribs.pop();
        currentNodes = (StringBuffer) nodes.pop();

        if (node instanceof VRMLNodeType) {
            name = ((VRMLNodeType)node).getVRMLNodeName();
        } else if (node instanceof PrototypeDecl) {
            // Replace protoInstances with the ProtoInstance tag
            name = ((PrototypeDecl)node).getVRMLNodeName();
            currentAttribs.append(" name=\"");
            currentAttribs.append(name);
            currentAttribs.append("\"");
            name = "ProtoInstance";
        } else {
            name = ((ExternalPrototypeDecl)node).getVRMLNodeName();
        }

        StringBuffer cis = (StringBuffer) currentIS.pop();

        if (cis.length() > 0) {
            currentNodes.insert(0,"</IS>");
            currentNodes.insert(0, cis.toString());
            currentNodes.insert(0,"<IS>");
        }
        currentNodes.insert(0,">");
        currentNodes.insert(0,currentAttribs.toString());
        currentNodes.insert(0,name);
        currentNodes.insert(0,"<");

        currentNodes.append("</");
        currentNodes.append(name);
        currentNodes.append(">");

        StringBuffer parent=null;

        if (!nodes.isEmpty())
            parent = (StringBuffer) nodes.pop();

        if (parent == null) {
            pw.println(currentNodes.toString());
        } else {
            parent.append(currentNodes.toString());
            nodes.push(parent);
        }

        isProto.pop();
        lastIS=false;
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
    public void startField(String name)
        throws SAVException, VRMLException{

        lastStartField=true;
        int cnt = fieldCnt.pop();
        int flevel=0;

        if (!fieldDeclCnt.isEmpty())
            flevel = fieldDeclCnt.peek();

        if (flevel == nlevel) {
            fieldDeclCnt.pop();
            currentNodes = (StringBuffer) nodes.pop();
            currentAttribs = (StringBuffer) attribs.pop();

            currentNodes.insert(0,">");
            currentNodes.insert(0,currentAttribs.toString());
            currentNodes.insert(0,"field");
            currentNodes.insert(0,"<");

            currentNodes.append("</field>");

            StringBuffer parent = null;
            if (!nodes.isEmpty())
                parent = (StringBuffer) nodes.pop();
            if (parent == null) {
                pw.println(currentNodes.toString());
            } else {
                parent.append(currentNodes.toString());
                nodes.push(parent);
            }
        }

        if (cnt > 0 && !lastIS && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (type==0) {
                    indent();
                    ilevel--;
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                }
            }
        }
        fieldCnt.push(++cnt);
        lastIS=false;
        gotFieldValue=false;

        indent();
        Object node = currentNode.peek();

        int idx;
        if (node instanceof VRMLNodeType) {
            idx = ((VRMLNodeType)node).getFieldIndex(name);
        } else if (node instanceof PrototypeDecl) {
            idx = ((PrototypeDecl)node).getFieldIndex(name);
        } else {
            idx = ((ExternalPrototypeDecl)node).getFieldIndex(name);
        }

        currentField.push(new Integer(idx));

        int type=-1;
        if (isField(FieldConstants.MFNODE)) {
            type=0;
            ilevel++;
            indent();
        } else if (isField(FieldConstants.SFNODE)) {
            type=1;
        } else if (isSFField()) {
            type=2;
        } else {
            type=3;
        }

        inFieldType.push(type);
        if (type > 1) {
            // TODO: Change to hash lookup
            if (name.equals("url") && ((VRMLNodeType)node).getVRMLNodeName().equals("Script")) {
                inCData=true;
                currentNodes = (StringBuffer) nodes.peek();
                currentAttribs = (StringBuffer) attribs.peek();
            } else {
                inCData=false;
                currentNodes = (StringBuffer) nodes.peek();
                currentAttribs = (StringBuffer) attribs.peek();
            }
        }

        nodeCnt.push(0);
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
    public void fieldValue(String value)
        throws SAVException, VRMLException{

        gotFieldValue=true;

        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek()).intValue();
        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNodeType)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((PrototypeDecl)node).getFieldDeclaration(idx);
        } else {
            decl = ((ExternalPrototypeDecl)node).getFieldDeclaration(idx);
        }

        String name = decl.getName();

        if (!inCData) {
            boolean ip = isProto.peek();

            if (!ip) {
                currentAttribs.append("\n");
                currentAttribs.append(name);
                currentAttribs.append("=");

                if (value == null) {
                    currentAttribs.append("\"");
                    currentAttribs.append("\"");
                } else if (value != null) {
                    if (isField(FieldConstants.SFBOOL)) {
                        currentAttribs.append("\"");
                        currentAttribs.append(value.toLowerCase());
                        currentAttribs.append("\"");
                    } else if (isField(FieldConstants.SFSTRING) ||
                               isField(FieldConstants.MFSTRING)) {
                        if (value.startsWith("\"")) {
                            currentAttribs.append(value);
                        } else {
                            currentAttribs.append("\"");
                            currentAttribs.append(value);
                            currentAttribs.append("\"");
                        }
                    } else {
                        currentAttribs.append("\"");
                        currentAttribs.append(value);
                        currentAttribs.append("\"");
                    }
                }
            } else {
                // Handle ProtoInstance fieldValues as fieldValue elements
                currentNodes.append("<fieldValue name=\"");
                currentNodes.append(name);

                currentNodes.append("\" value=");
                if (value != null) {
                    if (isField(FieldConstants.SFBOOL)) {
                        currentNodes.append("\"");
                        currentNodes.append(value.toLowerCase());
                        currentNodes.append("\"");
                    } else if (isField(FieldConstants.SFSTRING) ||
                               isField(FieldConstants.MFSTRING)) {
                        if (value.startsWith("\"")) {
                            currentAttribs.append(value);
                        } else {
                            currentAttribs.append("\"");
                            currentAttribs.append(value);
                            currentAttribs.append("\"");
                        }
                    } else {
                        currentNodes.append("\"");
                        currentNodes.append(value);
                        currentNodes.append("\"");
                    }
                }

                currentNodes.append("/>\n");
            }
        } else {
            currentNodes.append("<![CDATA[\n");
            // Remove enclosing quotes.  TODO: is this still needed?
            value = value.substring(1,value.length()-1);
            currentNodes.append(value);
            currentNodes.append("\n]]>");
        }
        currentField.pop();
        nodeCnt.pop();
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
    public void fieldValue(String[] values)
        throws SAVException, VRMLException{

        gotFieldValue=true;

        int idx = ((Integer)currentField.peek()).intValue();

        if (!inCData) {
            boolean ip = isProto.peek();

            Object node = currentNode.peek();

            VRMLFieldDeclaration decl;

            if (node instanceof VRMLNodeType) {
                decl = ((VRMLNodeType)node).getFieldDeclaration(idx);
            } else if (node instanceof PrototypeDecl) {
                decl = ((PrototypeDecl)node).getFieldDeclaration(idx);
            } else {
                decl = ((ExternalPrototypeDecl)node).getFieldDeclaration(idx);
            }

            if (!ip) {
                currentAttribs.append("\n");
                currentAttribs.append(decl.getName());
                currentAttribs.append("=");

                currentAttribs.append("'");
                for(int i=0; i < values.length; i++) {
                    //currentAttribs.append("\"");
                    currentAttribs.append(values[i]);
                    //currentAttribs.append("\"");
                    currentAttribs.append(" ");
                }
                currentAttribs.append("'\n");
            } else {
                // Handle ProtoInstance fieldValues as fieldValue elements
                currentNodes.append("<fieldValue name=\"");
                currentNodes.append(decl.getName());
                currentNodes.append("\" value=");
                currentNodes.append("'");
                for(int i=0; i < values.length; i++) {
                    currentNodes.append("\"");
                    currentNodes.append(values[i]);
                    currentNodes.append("\"");
                }
                currentNodes.append("'\n/>");
            }
        } else {
            currentNodes.append("<![CDATA[\n");

            for(int i=0; i < values.length; i++) {
                values[i] = values[i].substring(1,values[i].length()-1);

                currentNodes.append(values[i]);
            }
            currentNodes.append("\n]]>");
        }
        currentField.pop();
        nodeCnt.pop();
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
    public void useDecl(String defName)
        throws SAVException, VRMLException{

        int nCnt = nodeCnt.pop();

        nodeCnt.push(++nCnt);

        currentNodes.append("<");
        String defType = (String) defTable.get(defName);
        currentNodes.append(defType);

        String newName = (String) defRemapTable.get(defName);
        if (newName != null)
            defName = newName;


        currentNodes.append(" USE=\"");
        currentNodes.append(defName);
        currentNodes.append("\"");
        currentNodes.append("/>");
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
        // Ingored, handled by startField, endNode, endProtoDecl, endScriptDecl
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
        throws SAVException, VRMLException{

        StringBuffer buff = new StringBuffer();
        buff.append("<ROUTE fromNode=\"");
        buff.append(srcNode);
        buff.append("\" fromField=\"");
        buff.append(srcField);
        buff.append("\" toNode=\"");
        buff.append(destNode);
        buff.append("\" toField=\"");
        buff.append(destField);
        buff.append("\"/>\n");

        if (!nodes.isEmpty()) {
            currentNodes = (StringBuffer) nodes.peek();
            currentNodes.append(buff.toString());
        } else {
            pw.print(buff.toString());
        }
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
        currentScript = (VRMLScriptNodeType)currentNode.peek();
        fieldCnt.push(0);
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
        int cnt = fieldCnt.pop();

        int flevel=0;

        if (!fieldDeclCnt.isEmpty())
            flevel = fieldDeclCnt.peek();

        if (flevel == nlevel) {
            fieldDeclCnt.pop();
            currentNodes = (StringBuffer) nodes.pop();
            currentAttribs = (StringBuffer) attribs.pop();

            currentNodes.insert(0,">");
            currentNodes.insert(0,currentAttribs.toString());
            currentNodes.insert(0,"field");
            currentNodes.insert(0,"<");

            currentNodes.append("</field>");

            StringBuffer parent = null;
            if (!nodes.isEmpty())
                parent = (StringBuffer) nodes.pop();
            if (parent == null) {
                pw.println(currentNodes.toString());
            } else {
                parent.append(currentNodes.toString());
                nodes.push(parent);
            }
        }

        // TODO: I think this isEmpty check will fail for nested
        if (cnt > 0 && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                }
            }
        }
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
        throws SAVException, VRMLException{

        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        currentScript.appendField(field);

        printFieldDecl(access, type, name, currentScript.getFieldIndex(name),
            value);
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
        throws SAVException, VRMLException{

        indent();
        ilevel++;
        nlevel++;

        currentNodes = new StringBuffer();
        currentAttribs = new StringBuffer();
        currentAttribs.append(" name=\"");
        currentAttribs.append(name);
        currentAttribs.append("\"");
        nodes.push(currentNodes);
        attribs.push(currentAttribs);

        currentNodes = new StringBuffer();
        currentAttribs = new StringBuffer();
        nodes.push(currentNodes);
        attribs.push(currentAttribs);

        PrototypeDecl proto = new PrototypeDecl(name,
                                                majorVersion,
                                                minorVersion,
                                                null);

        protoMap.put(name, proto);
        protoDeclStack.push(proto);

        currentProto = proto;
        fieldCnt.push(0);
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
        throws SAVException, VRMLException{

        int flevel=0;

        if (!fieldDeclCnt.isEmpty())
            flevel = fieldDeclCnt.peek();

        if (flevel == nlevel) {
            fieldDeclCnt.pop();
            currentNodes = (StringBuffer) nodes.pop();
            currentAttribs = (StringBuffer) attribs.pop();

            currentNodes.insert(0,">");
            currentNodes.insert(0,currentAttribs.toString());
            currentNodes.insert(0,"field");
            currentNodes.insert(0,"<");

            currentNodes.append("</field>");

            StringBuffer parent = null;
            if (!nodes.isEmpty())
                parent = (StringBuffer) nodes.pop();
            if (parent == null) {
                pw.println(currentNodes.toString());
            } else {
                parent.append(currentNodes.toString());
                nodes.push(parent);
            }
        }

        int cnt = fieldCnt.pop();

        if (cnt > 0 && !inFieldType.isEmpty()) {
            int type = inFieldType.pop();

            if (type<2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                }
            }
        }

        // Handle the ProtoInterface "node"
        currentNodes = (StringBuffer) nodes.pop();
        currentAttribs = (StringBuffer) attribs.pop();

        currentNodes.insert(0,">");
        currentNodes.insert(0,currentAttribs.toString());
        currentNodes.insert(0,"ProtoInterface");
        currentNodes.insert(0,"<");

        currentNodes.append("</ProtoInterface>\n");

        StringBuffer parent = (StringBuffer) nodes.pop();

        parent.append(currentNodes.toString());
        nodes.push(parent);

        ilevel--;
        nlevel--;
        indent();
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
        throws SAVException, VRMLException{

        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        currentProto.appendField(field);

        printFieldDecl(access, type, name, currentProto.getFieldIndex(name),
        value);
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
        throws SAVException, VRMLException{

        int type = inFieldType.pop();
        int idx=-1;

        if (lastStartField || type<2) {
            nodeCnt.pop();
            idx = ((Integer) currentField.pop()).intValue();
        }

        if (lastStartField) {
            StringBuffer cis = (StringBuffer) currentIS.peek();
            cis.append("<connect nodeField=\"");

            Object node = currentNode.peek();
            VRMLFieldDeclaration decl;

            if (node instanceof VRMLNodeType) {
                decl = ((VRMLNodeType)node).getFieldDeclaration(idx);
            } else if (node instanceof PrototypeDecl) {
                decl = ((PrototypeDecl)node).getFieldDeclaration(idx);
            } else {
                decl = ((ExternalPrototypeDecl)node).getFieldDeclaration(idx);
            }


            cis.append(decl.getName());
            cis.append("\" protoField=\"");
            cis.append(fieldName);
            cis.append("\"/>\n");
        } else {
            currentNodes.append("<IS>");
            currentNodes.append("<connect nodeField=\"");

            Object node = currentNode.peek();
            VRMLFieldDeclaration decl;

            if (node instanceof VRMLNodeType) {
                decl = ((VRMLNodeType)node).getFieldDeclaration(idx);
            } else if (node instanceof PrototypeDecl) {
                decl = ((PrototypeDecl)node).getFieldDeclaration(idx);
            } else {
                decl = ((ExternalPrototypeDecl)node).getFieldDeclaration(idx);
            }

            currentNodes.append(decl.getName());
            currentNodes.append("\" protoField=\"");
            currentNodes.append(fieldName);
            currentNodes.append("\"/>\n");
            currentNodes.append("</IS>");
        }

        lastIS = true;
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
    public void startProtoBody() throws SAVException, VRMLException{

        indent();
        ilevel++;

        protoMapStack.push(protoMap);

        protoMap = new HashMap();

        currentNodes = new StringBuffer();
        currentAttribs = new StringBuffer();
        currentNodes.append("<ProtoBody>");
        nodes.push(currentNodes);
        attribs.push(currentAttribs);
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
    public void endProtoBody() throws SAVException, VRMLException{
        ilevel--;
        indent();

        if(protoMapStack.size() > 0) {
            // Now replace the data structures for this level
            protoMap = (HashMap)protoMapStack.pop();
        } else {
            protoMap = new HashMap();
        }

        // Handle the ProtoBody "node"
        StringBuffer parent = (StringBuffer) nodes.pop();
        parent.append("</ProtoBody>\n");
        currentAttribs = (StringBuffer) attribs.pop();


        currentNodes = (StringBuffer) nodes.pop();
        currentAttribs = (StringBuffer) attribs.pop();

        currentNodes.append(parent.toString());
        currentNodes.insert(0,">");
        currentNodes.insert(0,currentAttribs.toString());
        currentNodes.insert(0,"ProtoDeclare");
        currentNodes.insert(0,"<");

        currentNodes.append("</ProtoDeclare>\n");

        parent = null;
        if (!nodes.isEmpty())
            parent = (StringBuffer) nodes.pop();
        if (parent == null) {
            pw.println(currentNodes.toString());
        } else {
            parent.append(currentNodes.toString());
            nodes.push(parent);
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
        throws SAVException, VRMLException{

        indent();
        // TODO: Need to change to XML
        pw.print("EXTERNPROTO ");
        pw.print(name);
        pw.println(" [");
        ilevel++;

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
        throws SAVException, VRMLException{

        indent();
        pw.print("] ");
        ilevel--;
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
        pw.print("\"");

        // Not sure multiple URLs is valid?
        for(int i=0; i < values.length; i++) {
            pw.print(values[i]);
        }
        pw.println("\"\n");
    }

    //-----------------------------------------------------------------------
    //Local Methods
    //-----------------------------------------------------------------------

    /**
     * Indent to the current level.
     */
    private void indent() {
        for(int i=0; i < ilevel; i++) {
            //pw.print(istring);
        }
    }

    /**
     * Print a field declaration.
     */
    private void printFieldDecl(int access, String type, String name,
        int idx, Object value) {

        int typeNum;

        int flevel=0;

        if (!fieldDeclCnt.isEmpty())
            flevel = fieldDeclCnt.peek();

        if (flevel == nlevel) {
            fieldDeclCnt.pop();
            currentNodes = (StringBuffer) nodes.pop();
            currentAttribs = (StringBuffer) attribs.pop();

            currentNodes.insert(0,">");
            currentNodes.insert(0,currentAttribs.toString());
            currentNodes.insert(0,"field");
            currentNodes.insert(0,"<");

            currentNodes.append("</field>\n");

            StringBuffer parent = null;
            if (!nodes.isEmpty())
                parent = (StringBuffer) nodes.pop();
            if (parent == null) {
                pw.println(currentNodes.toString());
            } else {
                parent.append(currentNodes.toString());
                nodes.push(parent);
            }
        }

        fieldDeclCnt.push(nlevel);

        int cnt = fieldCnt.pop();

        if (cnt > 0 && !lastIS && !inFieldType.isEmpty()) {
            typeNum = inFieldType.pop();
            if (typeNum != 2) {
                int nCnt = nodeCnt.pop();
                currentField.pop();

                if (typeNum == 0) {
                    indent();
                    ilevel--;
                }
            } else {
                if (!gotFieldValue && !lastIS) {
                    nodeCnt.pop();
                    currentField.pop();
                }
            }
        }
        fieldCnt.push(++cnt);

        indent();

        currentNodes = new StringBuffer();
        currentAttribs = new StringBuffer();
        nodes.push(currentNodes);
        attribs.push(currentAttribs);

        currentAttribs.append(" name=\"");
        currentAttribs.append(name);
        currentAttribs.append("\"");
        currentAttribs.append(" accessType=");

        switch(access) {
            case FieldConstants.FIELD:
                currentAttribs.append("\"initializeOnly\"");
                break;
            case FieldConstants.EXPOSEDFIELD:
                currentAttribs.append("\"inputOutput\"");
                break;
            case FieldConstants.EVENTIN:
                currentAttribs.append("\"inputOnly\"");
                break;
            case FieldConstants.EVENTOUT:
                currentAttribs.append("\"outputOnly\"");
                break;
            default:
                errorReporter.warningReport("Unknown field type in X3DXMLExporter: " +
                                      access, null);
        }

        currentAttribs.append(" type=\"");
        currentAttribs.append(type);
        currentAttribs.append("\"");

        // TODO: Change to hash lookup
        if (type.equals("MFNode"))
            typeNum=0;
        else if (type.equals("SFNode"))
            typeNum=1;
        else {
            typeNum=2;
        }

        inFieldType.push(typeNum);
        if (typeNum!=2) {
            nodeCnt.push(0);

            currentField.push(new Integer(idx));
            flevel++;
        }

        if (value instanceof String) {
            // TODO: Change to hashmap
            if (type.equals("SFString")) {
                currentAttribs.append(" value=");
                currentAttribs.append(value);
            } else {
                currentAttribs.append(" value=");
                currentAttribs.append("\"");
                currentAttribs.append(value);
                currentAttribs.append("\"");
            }
        } else if (value instanceof String[]) {
            currentAttribs.append(" value='");
            for(int i=0; i < ((String[])value).length; i++) {
                pw.print(((String[])value)[i]);
            }
            currentAttribs.append("'");
        }

        pw.println();
        lastIS=false;
        gotFieldValue=true;
    }

    /**
     * Is the current field a MFField?
     */
    private boolean isMFField() {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek()).intValue();
        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNodeType)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((PrototypeDecl)node).getFieldDeclaration(idx);
        } else {
            decl = ((ExternalPrototypeDecl)node).getFieldDeclaration(idx);
        }

        if (decl == null) {
            errorReporter.errorReport("No decl for: " + node + " idx: " + idx, null);
        }

        switch(decl.getFieldType()) {
            case FieldConstants.MFINT32:
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFFLOAT:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFVEC2F:
            case FieldConstants.MFIMAGE:
            case FieldConstants.MFSTRING:
            case FieldConstants.MFNODE:
                return true;
        }

        return false;
    }

    /**
     * Is the current field a simple SF* field, ie not SFNode?
     */
    private boolean isSFField() {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek()).intValue();

        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNodeType)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((PrototypeDecl)node).getFieldDeclaration(idx);
        } else {
            decl = ((ExternalPrototypeDecl)node).getFieldDeclaration(idx);
        }

        switch(decl.getFieldType()) {
            case FieldConstants.SFINT32:
            case FieldConstants.SFCOLOR:
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.SFFLOAT:
            case FieldConstants.SFROTATION:
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFVEC2F:
            case FieldConstants.SFIMAGE:
            case FieldConstants.SFSTRING:
                return true;
        }
        return false;
    }

    /**
     * Is the current field of the type requested.
     *
     * @param const The FieldConstants constant
     */
    private boolean isField(int fconst) {
        Object node = currentNode.peek();
        int idx = ((Integer)currentField.peek()).intValue();

        VRMLFieldDeclaration decl;

        if (node instanceof VRMLNodeType) {
            decl = ((VRMLNodeType)node).getFieldDeclaration(idx);
        } else if (node instanceof PrototypeDecl) {
            decl = ((PrototypeDecl)node).getFieldDeclaration(idx);
        } else {
            decl = ((ExternalPrototypeDecl)node).getFieldDeclaration(idx);
        }

        return (fconst == decl.getFieldType());
    }

    /**
     * Get the publicId for this spec version
     *
     * @param major The major version
     * @param minor The minor version
     * @return The speced public id
     */
    private String getPublicId(int major, int minor) {
        switch(minor) {
            case 0:
                return X3DConstants.GENERAL_PUBLIC_ID_3_0;
            case 1:
                return X3DConstants.GENERAL_PUBLIC_ID_3_1;
            case 2:
                return X3DConstants.GENERAL_PUBLIC_ID_3_2;
            default:
                throw new UnsupportedSpecVersionException("Unhandled minor version: " + minor);
        }
    }

    /**
     * Get the publicId for this spec version
     *
     * @param major The major version
     * @param minor The minor version
     * @return The speced public id
     */
    private String getSystemId(int major, int minor) {
        switch(minor) {
            case 0:
                return X3DConstants.GENERAL_SYSTEM_ID_3_0;
            case 1:
                return X3DConstants.GENERAL_SYSTEM_ID_3_1;
            case 2:
                return X3DConstants.GENERAL_SYSTEM_ID_3_2;
            default:
                throw new UnsupportedSpecVersionException("Unhandled minor version: " + minor);
        }
    }

    /**
     * Load the properties files for the given spec version.
     *
     * @param specVersion The spec version in floating point format
     */
    protected void loadContainerProperties(final String specVersion) {
        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    String filename = "config/" + specVersion + "/" +
                                                         CONTAINER_PROPS_FILE;

                    try {
                        ClassLoader cl = ClassLoader.getSystemClassLoader();
                        InputStream is =
                            cl.getSystemResourceAsStream(filename);

                        // WebStart fallback
                        if(is == null) {
                            cl = X3DXMLExporter.class.getClassLoader();
                            is = cl.getResourceAsStream(filename);
                        }

                        if(is != null)
                            containerFields.load(is);
                        else
                            errorReporter.errorReport("Couldn't find container properties file: " +
                                                filename, null);

                    } catch(IOException ioe) {
                        errorReporter.errorReport("Error reading container properties file", null);
                    }
                    return null;
                }
            }
        );
    }
}
