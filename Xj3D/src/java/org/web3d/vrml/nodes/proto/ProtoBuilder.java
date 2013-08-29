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

package org.web3d.vrml.nodes.proto;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.sav.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.util.SimpleStack;
import org.web3d.vrml.parser.FieldParserFactory;
import org.web3d.vrml.parser.VRMLFieldReader;

/**
 * A SAV interface for dealing with building a single proto.
 * <p>
 *
 * The builder is designed to create a single proto. However, that single proto
 * may well have nested protos as part of it, so we must deal with that too.
 * <p>
 *
 * The proto must be locally resident in this file, not an externproto.
 *
 * @author Justin Couch
 * @version $Revision: 1.62 $
 */
public class ProtoBuilder
    implements StringContentHandler,
               BinaryContentHandler,
               ProtoHandler,
               ScriptHandler,
               RouteHandler {

    // Various standard constants

    /** The initial size of the stack for element processing */
    private static final int STACK_START_SIZE = 50;

    /** The increment size of the stack if it get overflowed */
    private static final int STACK_INCREMENT = 25;

    /** Name of the profile used for VRML97 compatibility */
    private static final String VRML97_PROFILE_STRING = "VRML97";

    /** Error when an import statement is found in an VRML97 file */
    private static final String VRML_IMPORT_ERROR =
        "IMPORT statements are not permitted in VRML97 files";

    /** Error when an export statement is found in an VRML97 file */
    private static final String VRML_EXPORT_ERROR =
        "EXPORT statements are not permitted in VRML97 files";

    /** Error when attempting to nest proto declaration statements */
    private static final String NESTED_PROTO_ERROR =
        "Nested PROTO declarations not allowed";

    // Objects used during multiple runs

    /** Flag to indicate that one time initialisation process has happened */
    private boolean init;

    /** The error handler used for reporting errors */
    private ErrorReporter errorReporter;

    /** The document locator to find error information */
    private Locator locator;

    /** Field Parser for the current scene */
    private VRMLFieldReader fieldParser;

    // Flags used during parsing. We use a collection of booleans rather than
    // int flags and a switch statement. Booleans are simpler to understand
    // down in the individual methods and provide just the same speed. So the
    // code is a little more verbose, but easier to understand.

    /** Flag to indicate we are inside a script at the moment */
    private boolean inScript = false;

    /** Flag to indicate we are inside a proto declaration at the moment */
    private boolean inProtoDecl = false;

    /** Flag to indicate we are inside a proto body at the moment */
    private boolean inProtoBody = false;

    /** Flag to indicate we are inside an EXTERNPROTO declaration ATM. */
    private boolean inExternProtoDecl = false;

    /** Flag to indicate we are processing an EXTERNPROTO uri */
    private boolean inExternProtoURI = false;

    /** Counter when we ignore a node type we don't know about */
    private int ignoreNodeCounter;

    /** Copy of the top child index for efficiency purposes */
    private int currentFieldIndex;

    /** Copy of the top node for efficiency purposes */
    private VRMLNodeType currentNode;

    /** Copy of the current working protype definition */
    private AbstractProto currentProto;

    /** Copy of the current working script definition */
    private VRMLScriptNodeType currentScript;

    // Variables replace or reset during each parsing run

    /** The root proto declaration */
    private AbstractProto rootProto;

    /** The stack of nodes used during processing the proto */
    private VRMLNodeType[] nodeStack;

    /** The stack of child indexes used during processing */
    private int[] childIndexStack;

    /** Counter to the top item in the stack array */
    private int topOfStack;

    /**
     * The mapping of global def names (key) to node instances (value). Used
     * for reference only on the field declarations.
     */
    private Map globalDefMap;

    /** The mapping of global protos for reference in field decls. */
    private Map globalProtoMap;

    /** The mapping of externproto names (key) to node instances (value) */
    private Map globalExternProtoMap;

    /** The mapping of def names (key) to node instances (value) */
    private HashMap defMap;

    /** The mapping of proto names (key) to node instances (value) */
    private HashMap protoMap;

    /** The mapping of externproto names (key) to node instances (value) */
    private HashMap externProtoMap;

    /**
     * The mapping of imported node names (import foo.bar AS importname) to
     * their ImportProxyNode instance.
     */
    private Map importProxyMap;

    /**
     * The mapping of export node names (import foo.exportname AS bar) to
     * their ImportProxyNode instance. Two level deep map. First level is by
     * the DEF name of the appropriate Inline. This resolves to another Map
     * that is keyed by the export name to the ImportProxyNode instance.
     */
    private Map exportProxyMap;

    /** The working stack of currently defined PROTO declarations */
    private SimpleStack protoDeclStack;

    /** The working stack of currently building scripts. */
    private SimpleStack scriptStack;

    /** The working stack of def maps */
    private SimpleStack defMapStack;

    /** The working stack of proto decls maps */
    private SimpleStack protoMapStack;

    /** The working stack of import proxy maps */
    private SimpleStack importMapStack;

    /** The working stack of export proxy maps */
    private SimpleStack exportMapStack;

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** Flag indicating what mode - VRML97 or VRML3.0 that we're using */
    private boolean isVrml97;

    /** Set of classes that we don't want to load */
    private HashSet dontLoadTypes;

    /** Set of DEF names to ignore when we come to USE decls */
    private HashSet ignoreDefSet;

    /** The current world's root URL */
    private String worldURL;

    /** The major version of the spec this file belongs to. */
    private int majorVersion;

    /** The minor version of the spec this file belongs to. */
    private int minorVersion;

    /** Proto creator for this scene */
    private NodeTemplateToInstanceCreator protoCreator;

    /**
     * Create a new default instance of the scene builder. This uses the
     * default factory for nodes non-renderable nodes.
     *
     * @param fac The factory used to create internal nodes representations
     *    with.
     * @throws NullPointerException The factory reference is null
     */
    public ProtoBuilder(VRMLNodeFactory fac) throws NullPointerException {

        if(fac == null)
            throw new NullPointerException("The factory is null");

        nodeFactory = fac;

        defMap = new HashMap();
        protoMap = new HashMap();
        externProtoMap = new HashMap();
        importProxyMap = new HashMap();
        exportProxyMap = new HashMap();

        protoDeclStack = new SimpleStack();
        scriptStack = new SimpleStack();
        defMapStack = new SimpleStack();
        protoMapStack = new SimpleStack();
        importMapStack = new SimpleStack();
        exportMapStack = new SimpleStack();

        dontLoadTypes = new HashSet();
        ignoreDefSet = new HashSet();

        nodeStack = new VRMLNodeType[STACK_START_SIZE];
        childIndexStack = new int[STACK_START_SIZE];

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
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
        locator = loc;
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
     * @param version The VRML version of this document
     * @param type The bytes of the first part of the file header
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

        rootProto = null;

        topOfStack = -1;
        currentFieldIndex = 0;

        worldURL = url;

        inScript = false;
        inProtoDecl = false;
        inProtoBody = false;
        inExternProtoDecl = false;
        inExternProtoURI = false;

        int major = 3;
        int minor = 0;
        int real_enc = SceneMetaData.VRML_ENCODING;


        if(type.charAt(1) == 'V') {
            // we're in VRML model either 97 or 1.0.
            nodeFactory.setSpecVersion(2, 0);
            nodeFactory.setProfile(VRML97_PROFILE_STRING);
            major = 2;
        } else {
            real_enc = encoding.equals("utf8") ?
                       SceneMetaData.VRML_ENCODING :
                       SceneMetaData.XML_ENCODING;

            // Parse the number string looking for the version minor number.
            int dot_index = version.indexOf('.');
            String minor_num = version.substring(dot_index + 1);

            // Should this look for a badly formatted number here or just
            // assume the parsing beforehad has correctly identified something
            // already dodgy?
            minor = Integer.parseInt(minor_num);

            nodeFactory.setSpecVersion(major, minor);
        }

        if((major != majorVersion) || (minor != minorVersion)) {
            FieldParserFactory fac =
                FieldParserFactory.getFieldParserFactory();

            fieldParser = fac.newFieldParser(major, minor);
        }

        majorVersion = major;
        minorVersion = minor;

        if((majorVersion == 3) && (real_enc == SceneMetaData.XML_ENCODING)) {
            fieldParser.setCaseSensitive(false);
        } else {
            fieldParser.setCaseSensitive(true);
        }

        if(locator == null)
            locator = new DefaultLocator();

        if(!init) {
            globalDefMap = new HashMap();
            globalProtoMap = new HashMap();
            globalExternProtoMap = new HashMap();
            init = true;
        }
    }

    /**
     * A profile declaration has been found in the code. A proto builder
     * should never encounter this situation.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {

        nodeFactory.setProfile(profileName);
    }

    /**
     * A component declaration has been found in the code. A proto builder
     * should never encounter this situation.
     *
     * @param componentInfo The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void componentDecl(String componentInfo)
        throws SAVException, VRMLException {

        // Strip the string to the component name and level.
        int colon = componentInfo.indexOf(':');
        String name = componentInfo;
        int level = VRMLNodeFactory.ANY_LEVEL;

        if(colon != -1) {
            name = componentInfo.substring(0, colon);
            String tmp = componentInfo.substring(colon + 1);

            try {
                level = Integer.parseInt(tmp);
            } catch(NumberFormatException nfe) {
                throw new VRMLParseException(locator.getLineNumber(),
                                             locator.getColumnNumber(),
                                             "Component level is malformed");
            }
        }

        ComponentInfo info = nodeFactory.addComponent(name, level);
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

        // Not used
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
        if(majorVersion < 3)
            throw new VRMLException(VRML_IMPORT_ERROR);

        // Look in the DEF map for the name.
        VRMLNodeType node = (VRMLNodeType)defMap.get(inline);

        if(node == null)
            throw new VRMLException("DEF name not known for Inline referenced by import " +
                                    inline);
        // What happens if the DEF is actually a proto that is wrapping an
        // Inline as the primary type? Spec is not clear on whether this is a
        // legal construct or not.
        if(node.getPrimaryType() != TypeConstants.InlineNodeType)
            throw new VRMLException("Import statement refers to a DEF name " +
                                    "that is not an Inline node" + inline);

        ImportNodeProxy n = new ImportNodeProxy(imported, inline, exported);
        n.setErrorReporter(errorReporter);
        ((PrototypeDecl)currentProto).addImportDecl(imported, n);

        importProxyMap.put(imported, n);

        Map exports = (Map)exportProxyMap.get(inline);
        if(exports == null) {
            exports = new HashMap();
            exportProxyMap.put(inline, exports);
            ((VRMLInlineNodeType)node).setImportNodes(exports);
        }

        exports.put(exported, n);
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

        if(majorVersion < 3)
            throw new VRMLException(VRML_EXPORT_ERROR);

        throw new VRMLException("Protos cannot contain EXPORT statements");
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endDocument() throws SAVException, VRMLException {

        // Make sure we clean up any stray references.
        for(int i = 0; i < topOfStack; i++)
            nodeStack[i] = null;

        topOfStack = 0;
        currentNode = null;
        currentProto = null;
        currentScript = null;
        currentFieldIndex = -1;
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if(inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                         "Node declaration in externproto decl");

        // If one of our parent nodes is something we don't understand then we
        // need to ignore it. We do this as a first thing and don't even
        // attempt to process this child node. If we did, we would run into all
        // sorts of problems, such as having nested ignores. Basically, once we
        // start to ignore it, _everything_ is ignored until we get back to the
        // same level and we start processing the next sibling node.
        if(ignoreNodeCounter > 0) {
            // Don't increment unless we are already ignoring nodes
            ignoreNodeCounter++;

            if(defName != null) {
                defMap.remove(defName);
                ignoreDefSet.add(defName);
            }

            return;
        }

        VRMLNodeType node = null;

        // Try local definitions for protos and extern protos. If these fail,
        // try global definitions. If these fail, try the node factory.
        // If all these fail, then mark this as being an
        // ignorable node and exit. Not sure if this is currently spec
        // compliant as to proto v externproto asking first
        VRMLNodeTemplate proto_def = null;

        if(protoMap.containsKey(name)) {
            proto_def = (VRMLNodeTemplate)protoMap.get(name);
        } else if(externProtoMap.containsKey(name)) {
            proto_def = (VRMLNodeTemplate)externProtoMap.get(name);
        } else if(globalProtoMap.containsKey(name)) {
            proto_def = (VRMLNodeTemplate)globalProtoMap.get(name);
        } else {
            proto_def = (VRMLNodeTemplate)globalExternProtoMap.get(name);
        }

        if(proto_def != null) {
            node = new ProtoInstancePlaceHolder(proto_def,
                                                majorVersion,
                                                minorVersion,
                                                false,
                                                protoCreator);
        } else {
            node = (VRMLNodeType)nodeFactory.createVRMLNode(name, false);
        }

        // Check to see if this is on the ignore list. If it is, we just null
        // the reference and treat it like we didn't know the node exists. We
        // Still register it as a DEF so that when we come to the USE
        // declaration we can ignore it there without generating an error. It
        // also means removing the declaration from the list of real decls as
        // the DEF/USE spec says that re-defining the name changes all uses
        // after that point to the new definition.
        if(node != null) {
            Class cls = node.getClass();
            boolean dont_load = false;

            if(dontLoadTypes.contains(cls)) {
               dont_load = true;
            } else if(node instanceof VRMLProtoInstance) {
                // check the proto's type for matches
                VRMLProtoInstance proto = (VRMLProtoInstance)node;
                VRMLNode impl = proto.getImplementationNode();

                // The proto impl node may be null if this is an extern proto
                // that is being represented.
                if((impl != null) && dontLoadTypes.contains(impl.getClass()))
                    dont_load = true;
            }

            if(dont_load) {
                ignoreNodeCounter = 1;

                if(defName != null) {
                    defMap.remove(defName);
                    ignoreDefSet.add(defName);
                }

                return;
            }
         } else {
            // how do we deal with nodes that are not known? Will need to
            // generate an error here somewhere.
            ignoreNodeCounter = 1;

            if(defName != null) {
                defMap.remove(defName);
                ignoreDefSet.add(defName);
            }

            return;
        }

        if(defName != null) {
            if((majorVersion > 2) && defMap.containsKey(defName))
                throw new VRMLException("DEF name \"" + defName +
                                         "\" already declared in this scope. " +
                                         "X3D requires unique DEF names per file");
            node.setDEF();
            ignoreDefSet.remove(defName);
            defMap.put(defName, node);
        }

        if((inProtoDecl) && (nodeStack[topOfStack] == null)) {
            ((VRMLProtoDeclare)currentProto).addFieldNodeValue(currentFieldIndex,
                                                               node);
        } else {
            // OK, so it all checks out, let's pop it onto the stack
            nodeStack[topOfStack].setValue(currentFieldIndex, node);
        }

        topOfStack++;
        nodeStack[topOfStack] = node;
        currentNode = node;

        // don't assign the childIndex here because we don't do that until
        // we get the field call. It is possible that this is an empty node
        // declaration so the next thing we get will be the request to end
        // node. The child index will never have been used. Besides, at this
        // point, we can't work out what the next child field that will be
        // read will be.
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endNode() throws SAVException, VRMLException {

        // We should *never* get this, but protection anyway
        if(inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                         "End Node declaration in externproto");

        if(ignoreNodeCounter > 0) {
            ignoreNodeCounter--;
            return;
        }

        // Pop the object off the top of the stack now.
        nodeStack[topOfStack] = null;
        topOfStack--;

        // If this is the last close of the node in the proto declaration,
        // don't do anything. We don't even set the currentFieldIndex because
        // it should be set in the next call, which is either the end of the
        // proto decl or the next field decl.

        if(topOfStack >= 0) {
            currentNode = nodeStack[topOfStack];
            currentFieldIndex = childIndexStack[topOfStack];
        } else {
            currentNode = null;
            currentFieldIndex = -1;
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startField(String name) throws SAVException, VRMLException {
        // If ignoring this node, forget about any processing
        if(ignoreNodeCounter > 0)
            return;

        // set the current field index to this new field name.
        int index  = nodeStack[topOfStack].getFieldIndex(name);

        // Unknown field? What should we do?
        if(index == -1) {
            throw new InvalidFieldException("Unknown field name: " + name +
                                            " for node: " +
                                            currentNode.getVRMLNodeName());
        }

        currentFieldIndex = index;
        childIndexStack[topOfStack] = index;
    }

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void fieldValue(String value) throws SAVException, VRMLException {
        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        // check for the field existing first. This should never have a null
        // returned, but it's a good safety valve check for a while.
        VRMLFieldDeclaration decl =
            currentNode.getFieldDeclaration(currentFieldIndex);

        if(decl == null)
            throw new InvalidFieldException("Invalid field: " +
                                            currentNode.getVRMLNodeName() +
                                            " " + currentFieldIndex);

        int ft = decl.getFieldType();

        if (ft == FieldConstants.SFBOOL || ft == FieldConstants.MFBOOL) {
            value = value.toUpperCase();
        }

        // Now parse the string if we have to. We then use the setValue call
        // because that will ensure that all the IS referenced fields will be
        // updated with these values as well.
        try {
            parseField(currentNode,
                       currentFieldIndex,
                       decl.getFieldType(),
                       value);
        } catch(FieldException fe) {
            fe.setFieldName(decl.getName());
            throw fe;
        } catch(Exception e) {
            errorReporter.messageReport("Problem in PROTO: " + currentProto.getVRMLNodeName() + "  field: " + decl + "  value: " + value);
            VRMLException ve = new VRMLException("Internal error in parsing field: " + decl);
            ve.initCause(e);
            throw ve;
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void fieldValue(String[] values) throws SAVException, VRMLException {
        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0)  || inProtoDecl)
            return;

        // if there is a zero length array, no point passing that on.
        if(!inProtoDecl && (values.length != 0)) {
            VRMLFieldDeclaration decl =
                currentNode.getFieldDeclaration(currentFieldIndex);

            if(decl == null)
                throw new InvalidFieldException("Invalid field: " +
                                                currentNode.getVRMLNodeName() +
                                                " " + currentFieldIndex);

            try {
                parseField(currentNode,
                           currentFieldIndex,
                           decl.getFieldType(),
                           values);
            } catch(FieldException fe) {
                fe.setFieldName(decl.getName());
                throw fe;
            } catch(Exception e) {
                StringBuffer vals = new StringBuffer();

                for(int i=0; i < values.length; i++) {
                    vals.append(values[i]);
                    vals.append("\n");
                }
                errorReporter.messageReport("Problem in PROTO: " + currentProto.getVRMLNodeName() + "  field: " + decl + "  value:   " + vals.toString());
                VRMLException ve = new VRMLException("Internal error in parsing field: " + decl);
                ve.initCause(e);
                throw ve;
            }
        }
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void useDecl(String defName) throws SAVException, VRMLException {

        VRMLNodeType node;

        if(importProxyMap.containsKey(defName))
            throw new InvalidFieldValueException(
                "Line: " + locator.getLineNumber() +
                " Column: " + locator.getColumnNumber() +
                " Illegal to USE an IMPORTed node: " + defName);

        if(inProtoDecl) {
            node = (VRMLNodeType)globalDefMap.get(defName);
        } else {
            // If ignoring this node, forget about any processing
            if((ignoreNodeCounter > 0) || ignoreDefSet.contains(defName))
                return;

            node = (VRMLNodeType)defMap.get(defName);
        }

        if(node == null)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                         "USE declares an unknown source " +
                                         "node DEF \"" + defName + '\"');

        if((inProtoDecl) && (topOfStack == 0)) {
            if(currentProto.getPrimaryType() != TypeConstants.ScriptNodeType) {
                if(node instanceof VRMLBindableNodeType)
                    throw new VRMLParseException(locator.getLineNumber(),
                                                 locator.getColumnNumber(),
                                                 "USE references a bindable node \"" +
                                                 defName + '\"');

                if((node instanceof VRMLGroupingNodeType) &&
                   ((VRMLGroupingNodeType)node).containsBindableNodes())
                    throw new VRMLParseException(locator.getLineNumber(),
                                                 locator.getColumnNumber(),
                                     "USE references a grouping node that contains " +
                                     " bindable node(s).");
            }

            ((VRMLProtoDeclare)currentProto).addFieldNodeValue(currentFieldIndex,
                                                               node);
        } else {
            // OK, so it all checks out, let's pop it onto the stack
            if(!(nodeStack[topOfStack] instanceof VRMLScriptNodeType)) {
                if(node instanceof VRMLBindableNodeType)
                    throw new VRMLParseException(locator.getLineNumber(),
                                                 locator.getColumnNumber(),
                                                 "USE references a bindable node \"" +
                                                 defName + '\"');

                if((node instanceof VRMLGroupingNodeType) &&
                   ((VRMLGroupingNodeType)node).containsBindableNodes())
                    throw new VRMLParseException(locator.getLineNumber(),
                                                 locator.getColumnNumber(),
                                                "USE references a grouping node " +
                                                "that contains bindable node(s). \"" +
                                                defName + '\"');
            }
            nodeStack[topOfStack].setValue(currentFieldIndex, node);
        }
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endField() throws SAVException, VRMLException {
        // do we need anything here?
    }

    //----------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //----------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(int value)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value);
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(int[] value, int len)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value, len);
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(boolean value)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value);
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(boolean[] value, int len)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value, len);
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value);
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(float[] value, int len)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value, len);
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(long value)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value);
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(long[] value, int len)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value, len);
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(double value)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value);
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(double[] value, int len)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value, len);
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void fieldValue(String[] value, int len)
        throws SAVException, VRMLException {

        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || inProtoDecl)
            return;

        currentNode.setValue(currentFieldIndex, value, len);
    }

    //----------------------------------------------------------
    // Methods defined by ProtoHandler
    //----------------------------------------------------------

    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startProtoDecl(String name) throws SAVException, VRMLException {

        // Don't allow nesting of Protos within a proto declaration. The next
        // bit straight after a proto declaration is fine, but within the
        // acutal declaration is not.
        if(inProtoDecl || inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                         NESTED_PROTO_ERROR);

        PrototypeDecl proto = new PrototypeDecl(name,
                                                majorVersion,
                                                minorVersion,
                                                protoCreator);

        // by spec, a new proto will trash the previous definition. Do it now.
        // Problem with current proto is that what if this is the start of a
        // proto body that allows another definition. We will probably need
        // some form of proto declaration stack to deal with this too. At the
        // moment, things would just fall to pieces once this def has been
        // popped.

        if(currentProto != null)
            ((PrototypeDecl)currentProto).addPrototypeDecl(proto);

        protoMap.put(name, proto);
        protoDeclStack.push(currentProto);

        currentProto = proto;

        if(rootProto == null) {
            topOfStack = 0;
            rootProto = proto;
        } else {
            topOfStack++;
        }

        inProtoDecl = true;
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endProtoDecl() throws SAVException, VRMLException {
        inProtoDecl = false;
        topOfStack--;
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
     * @param value The default value of the field. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException {

        if(!inProtoDecl && !inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                       "proto field declaration not in proto!");

        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        int index = currentProto.appendField(field);

        currentFieldIndex = index;

        if((value != null) && (currentProto instanceof VRMLProtoDeclare)) {
            VRMLProtoDeclare p_decl = (VRMLProtoDeclare)currentProto;

            try {
                if(value instanceof String)
                    parseField(p_decl,
                               index,
                               field.getFieldType(),
                               (String)value);
                else
                    parseField(p_decl,
                               index,
                               field.getFieldType(),
                               (String[])value);
            } catch(FieldException fe) {
                fe.setFieldName(field.getName());
                throw fe;
            }
        }

        childIndexStack[topOfStack] = index;
    }

    /**
     * Notification of a field value uses an IS statement. If we are running
     * in VRML97 mode, this will throw an exception if the field access types
     * do not match.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void protoIsDecl(String fieldName) throws SAVException, VRMLException {

        if (ignoreNodeCounter > 0)
            return;

        if(!(currentProto instanceof PrototypeDecl))
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                       "proto IS declaration not in proto!");

        ((PrototypeDecl)currentProto).addIS(fieldName,
                                            currentNode,
                                            currentFieldIndex);

    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scenegraph information.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startProtoBody() throws SAVException, VRMLException {
        if(inProtoDecl || inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                 "Starting proto body while still in declaration");

        // We set the base item on the stack to be the proto body group. The
        // field will always be the "children" of any grouping node
        topOfStack++;
        currentNode = (VRMLNodeType)((PrototypeDecl)currentProto).getBodyGroup();

        nodeStack[topOfStack] = currentNode;

        currentFieldIndex = currentNode.getFieldIndex("children");
        childIndexStack[topOfStack] = currentFieldIndex;

        // Now build new data structures for this level
        protoMapStack.push(protoMap);
        defMapStack.push(defMap);
        importMapStack.push(importProxyMap);
        exportMapStack.push(exportProxyMap);

        protoMap = new HashMap();
        defMap = new HashMap();
        exportProxyMap = new HashMap();
        importProxyMap = new HashMap();

        inProtoBody = true;
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endProtoBody() throws SAVException, VRMLException {

        // now that we are at the end of the body, pop this one off the stack
        // and replace it with the previous proto we were working on (if any).
        currentNode.setupFinished();

        PrototypeDecl current_proto = (PrototypeDecl)currentProto;
        current_proto.setDEFMap(defMap);

        currentProto = (AbstractProto)protoDeclStack.pop();

        nodeStack[topOfStack] = null;
        topOfStack--;

        if(topOfStack >= 0) {
            currentNode = nodeStack[topOfStack];
            currentFieldIndex = childIndexStack[topOfStack];

            // Now replace the data structures for this level
            protoMap = (HashMap)protoMapStack.pop();
            defMap = (HashMap)defMapStack.pop();
            importProxyMap = (HashMap)importMapStack.pop();
            exportProxyMap = (HashMap)exportMapStack.pop();
        } else {
            currentNode = null;
            currentFieldIndex = -1;

            inProtoBody = false;
            defMap = new HashMap();
            protoMap = new HashMap();
            importProxyMap = new HashMap();
            exportProxyMap = new HashMap();
        }
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startExternProtoDecl(String name) throws SAVException, VRMLException {
        if(inProtoDecl || inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                         "Nested PROTO declarations not allowed");
        ExternalPrototypeDecl proto =
            new ExternalPrototypeDecl(name, majorVersion, minorVersion, protoCreator);

        // by spec, a new proto will trash the previous definition. Do it now.
        externProtoMap.put(name, proto);
        currentProto = proto;
        rootProto = proto;

        topOfStack = 0;

        inExternProtoDecl = true;
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endExternProtoDecl() throws SAVException, VRMLException {
        inExternProtoDecl = false;
        //currentProto = null;

        // we should be heading straight to the URI now..
        inExternProtoURI = true;
    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list of URIs. The calling application is required to interpet the
     * incoming strings. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
        if(!inExternProtoURI)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                  "EXTERNPROTO URI declaration not allowed here");
        ((ExternalPrototypeDecl)currentProto).setUrl(values, values.length);

        currentProto = null;
    }

    //----------------------------------------------------------
    // Methods defined by ScriptHandler
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void startScriptDecl() throws SAVException, VRMLException {

        currentScript = (VRMLScriptNodeType)currentNode;
        scriptStack.push(currentScript);

        inScript = true;
    }

    /**
     * Notification of the end of a script declaration. This is guaranteed to
     * be called before the ContentHandler <CODE>endNode()</CODE> callback.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void endScriptDecl() throws SAVException, VRMLException {

        currentScript = (VRMLScriptNodeType)scriptStack.pop();

        if(currentScript == null)
            inScript = false;
    }

    /**
     * Notification of a script's field declaration. This is used for all
     * fields except <CODE>url</CODE>, <CODE>mustEvaluate</CODE> and
     * <CODE>directOutput</CODE> fields. These fields use the normal field
     * callbacks of {@link ContentHandler}.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void scriptFieldDecl(int access,
                                String type,
                                String name,
                                Object value)
        throws SAVException, VRMLException {

        if(!inScript) {
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                         "Script declaration not in script!");
        }
        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        int index = currentScript.appendField(field);

        // Only pass through fields and exposed fields. Also, if the field
        // coming in is a SF/MFNode then the value will be null so ignore
        // that too. We could check with endsWith() for the node type name,
        // but it's not worth the speed penalty

        int field_type = field.getFieldType();
        boolean is_node_field = ((field_type == FieldConstants.SFNODE) ||
                                 (field_type == FieldConstants.MFNODE));


        if(!is_node_field &&
           ((access == FieldConstants.FIELD) ||
            (access == FieldConstants.EXPOSEDFIELD))) {

            try {
                if(value instanceof String)
                    parseField(currentScript,
                               index,
                               field.getFieldType(),
                               (String)value);
                else if(value instanceof String[])
                    parseField(currentScript,
                               index,
                               field.getFieldType(),
                               (String[])value);
            } catch(FieldException fe) {
                fe.setFieldName(field.getName());
                throw fe;
            }
        }

        currentFieldIndex = index;
        childIndexStack[topOfStack] = index;
    }

    //----------------------------------------------------------
    // Methods defined by RouteHandler
    //----------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNodeName The name of the DEF of the source node
     * @param srcFieldName The name of the field to route values from
     * @param destNodeName The name of the DEF of the destination node
     * @param destFieldName The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    public void routeDecl(String srcNodeName,
                          String srcFieldName,
                          String destNodeName,
                          String destFieldName)
        throws SAVException, VRMLException {

        // If the node is on the ignore list, quietly forget about it.
        if(ignoreDefSet.contains(srcNodeName) ||
           ignoreDefSet.contains(destNodeName)) {
            return;
        }

        VRMLNodeType src_node = (VRMLNodeType)defMap.get(srcNodeName);
        boolean imported_route = false;

        if(src_node == null) {
            // perhaps it is an imported node?
            if((majorVersion < 3) || !importProxyMap.containsKey(srcNodeName))
                throw new InvalidFieldConnectionException(
                                 "ROUTE declares an unknown source node DEF: " +
                                 srcNodeName);

            imported_route = true;
            src_node = (VRMLNodeType)importProxyMap.get(srcNodeName);
        }

        VRMLNodeType dest_node = (VRMLNodeType)defMap.get(destNodeName);

        if(dest_node == null) {
            if((majorVersion < 3) || !importProxyMap.containsKey(destNodeName))
                throw new InvalidFieldConnectionException(
                               "ROUTE declares an unknown destination node DEF: " +
                               destNodeName);

            imported_route = true;
            dest_node = (VRMLNodeType)importProxyMap.get(destNodeName);
        }

        int src_index = src_node.getFieldIndex(srcFieldName);

        if(src_index == -1)
            throw new InvalidFieldConnectionException(
                             "ROUTE declares an unknown source node field DEF: " +
                             srcNodeName + "." +srcFieldName);

        int dest_index = dest_node.getFieldIndex(destFieldName);

        if(dest_index == -1)
            throw new InvalidFieldConnectionException(
                           "ROUTE declares an unknown destination node field DEF: " +
                           destNodeName + "." + destFieldName);

        // Check the field type to make sure they match
        VRMLFieldDeclaration src_decl =
            src_node.getFieldDeclaration(src_index);
        VRMLFieldDeclaration dest_decl =
            dest_node.getFieldDeclaration(dest_index);

        if(!imported_route && (src_decl.getFieldType() != dest_decl.getFieldType()))
            throw new InvalidFieldConnectionException("The source field(" +
                srcNodeName + "." + src_decl.getName() + ") type " +
                src_decl.getFieldTypeString() + " and destination field(" +
                destNodeName + "." + dest_decl.getName() + ") type "  +
                dest_decl.getFieldTypeString() + " don't match");

        // So, we've made it through all the validity checking hoops. Now lets
        // just create an instance and register the thing....
        ProtoROUTE route =
            new ProtoROUTE(src_node, src_index, dest_node, dest_index);

        ((PrototypeDecl)currentProto).addRouteDecl(route);
    }

    //---------------------------------------------------------------
    // Misc Local methods
    //---------------------------------------------------------------

    /**
     * Set the error handler instance used by this instance of the builder. The
     * handler is used to report errors at the higher level. A value of null
     * will clear the current instance and return to the default handling.
     *
     * @param reporter The instance to use or null to clear
     */
    public void setErrorReporter(ErrorReporter reporter) {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        nodeFactory.setErrorReporter(errorReporter);
    }

    /**
     * Get the scene that was last built by this scene handler. If none of the
     * methods have been called yet, this will return a null reference. The
     * scene instance returned by this builder will not have had any external
     * references resolved. Externprotos, scripts, Inlines and all other nodes
     * that reference part of their data as a URL will need to be loaded
     * separately.
     *
     * @return The last built scene
     */
    public AbstractProto getPrototype() {
        return rootProto;
    }

    /**
     * Reset the builder. This is used to make sure that the builder has been
     * reset after a parsing run just in case the last parsing run exited
     * abnormally and left us in an odd state. Sometimes this can prevent us
     * from parsing again. This method should be called just before the
     * <code>VRMLReader.parse()</code> method is called.
     * <p>
     * The flags set about what to load are <i>not</i> reset by this method.
     */
    public void reset() {
        inScript = false;
        inProtoDecl = false;
        inProtoBody = false;
        inExternProtoDecl = false;
        inExternProtoURI = false;

        protoCreator = null;
        worldURL = null;

        dontLoadTypes.clear();
        ignoreDefSet.clear();

        // The rest of the fields are reset in the startDocument callback
        defMap.clear();
        protoMap.clear();
        externProtoMap.clear();
        importProxyMap.clear();
        exportProxyMap.clear();

        protoDeclStack.clear();
        defMapStack.clear();
        protoMapStack.clear();
        importMapStack.clear();
        exportMapStack.clear();
    }

    /**
     * Set the global DEF map to be used within fields. This is maintained
     * between resets so it should be cleared when necessary.
     *
     * @param defs The new DEF map to use
     * @param protos The new proto map to use
     * @param externProtos The new externproto map to use
     */
    public void setGlobals(Map defs, Map protos, Map externProtos) {
        if(defs == null)
            globalDefMap = new HashMap();
        else
            globalDefMap = defs;

        if(protos == null)
            globalProtoMap = new HashMap();
        else
            globalProtoMap = protos;

        if(externProtos == null)
            globalExternProtoMap = new HashMap();
        else
            globalExternProtoMap = externProtos;

        init = true;
    }

    /**
     * Set the flags about what to load. The flags defined here are matched
     * by the Java 3D Loader interface. Nodes defined as externals are inlines
     * and extern protos.
     *
     * @param useVrml97 true if the semantics should be VRML97 capable rather
     *    than the looser VRML 3.0.
     * @param useBackgrounds true if BackgroundNodeTypes should be loaded
     * @param useFogs true if FogNodeTypes should be loaded
     * @param useLights true if LightNodeTypes should be loaded
     * @param useAudio true if AudioClipNodeTypes should be loaded
     * @param useViewpoints true if ViewpointNodeTypes should loaded
     * @param useExternals true if ExternalNodeTypes should be loaded
     */
    public void setLoadRequirements(boolean useVrml97,
                                    boolean useBackgrounds,
                                    boolean useFogs,
                                    boolean useLights,
                                    boolean useAudio,
                                    boolean useViewpoints,
                                    boolean useExternals) {
        isVrml97 = useVrml97;

        if(!useBackgrounds)
            dontLoadTypes.add(VRMLBackgroundNodeType.class);

        if(!useFogs)
            dontLoadTypes.add(VRMLFogNodeType.class);

        if(!useLights)
            dontLoadTypes.add(VRMLLightNodeType.class);

        if(!useAudio)
            dontLoadTypes.add(VRMLAudioClipNodeType.class);

        if(!useViewpoints)
            dontLoadTypes.add(VRMLViewpointNodeType.class);

        if(!useExternals) {
            dontLoadTypes.add(VRMLInlineNodeType.class);
            // dontLoadTypes.add(VRMLExternProtoInstance.class);
        }
    }

    /**
     * Start a new proto definition within the current document context.
     *
     * @param creator The proto creator to wrap the template with
     */
    public void newProto(NodeTemplateToInstanceCreator creator) {
        protoCreator = creator;
        rootProto = null;

        topOfStack = -1;
        currentFieldIndex = 0;

        inScript = false;
        inProtoDecl = false;
        inProtoBody = false;
        inExternProtoDecl = false;
        inExternProtoURI = false;
    }

    /**
     * Resize the stack if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeStack() {

        if((topOfStack + 1) == nodeStack.length) {
            int old_size = nodeStack.length;
            int new_size = old_size + STACK_INCREMENT;

            VRMLNodeType[] tmp_nodes = new VRMLNodeType[new_size];
            int[] tmp_index = new int[new_size];

            System.arraycopy(nodeStack, 0, tmp_nodes, 0, old_size);
            System.arraycopy(childIndexStack, 0, tmp_nodes, 0, old_size);

            nodeStack = tmp_nodes;
            childIndexStack = tmp_index;
        }
    }

    /**
     * Convenience method to parse a field string and set it in the destination
     * node.
     *
     * @param node The node to set the field value in
     * @param index The field index for the value
     * @param type The type of the field (from the fieldDecl)
     * @param value The string to parse as the value
     */
    private void parseField(VRMLNodeType node,
                            int index,
                            int fieldType,
                            String value) {
        if(fieldType != FieldConstants.SFSTRING) {
            value = value.trim();

            if(value.length() == 0)
                return;
        }

        switch(fieldType) {
            case FieldConstants.SFINT32:
                node.setValue(index, fieldParser.SFInt32(value));
                break;

            case FieldConstants.MFINT32:
                int[] i_val = fieldParser.MFInt32(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFFLOAT:
                node.setValue(index, fieldParser.SFFloat(value));
                break;

            case FieldConstants.SFTIME:
                node.setValue(index, fieldParser.SFTime(value));
                break;

            case FieldConstants.SFDOUBLE:
                node.setValue(index, fieldParser.SFDouble(value));
                break;

            case FieldConstants.MFTIME:
                double[] d_val = fieldParser.MFTime(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFDOUBLE:
                d_val = fieldParser.MFDouble(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFLONG:
                node.setValue(index, fieldParser.SFLong(value));
                break;

            case FieldConstants.MFLONG:
                long[] l_val = fieldParser.MFLong(value);
                node.setValue(index, l_val, l_val.length);
                break;

            case FieldConstants.SFBOOL:
                node.setValue(index, fieldParser.SFBool(value));
                break;

            case FieldConstants.SFROTATION:
                float[] f_val = fieldParser.SFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFROTATION:
                f_val = fieldParser.MFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFBOOL:
                boolean[] b_val = fieldParser.MFBool(value);
                node.setValue(index, b_val, b_val.length);
                break;

            case FieldConstants.MFFLOAT:
                f_val = fieldParser.MFFloat(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC2F:
                f_val = fieldParser.SFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3F:
                f_val = fieldParser.SFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC4F:
                f_val = fieldParser.SFVec4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC2F:
                f_val = fieldParser.MFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC3F:
                f_val = fieldParser.MFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC4F:
                f_val = fieldParser.MFVec4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3D:
                d_val = fieldParser.SFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFVEC4D:
                d_val = fieldParser.SFVec4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC2D:
            	d_val = fieldParser.MFVec2d(value);
            	node.setValue(index, d_val, d_val.length);
            	break;
                
            case FieldConstants.MFVEC3D:
                d_val = fieldParser.MFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC4D:
                d_val = fieldParser.MFVec4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFSTRING:
                node.setValue(index, fieldParser.SFString(value));
                break;

            case FieldConstants.MFSTRING:
                String[] s_val = fieldParser.MFString(value);
                node.setValue(index, s_val, s_val.length);
                break;

            case FieldConstants.SFIMAGE:
                i_val = fieldParser.SFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.MFIMAGE:
                i_val = fieldParser.MFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFCOLOR:
                f_val = fieldParser.SFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLOR:
                f_val = fieldParser.MFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFCOLORRGBA:
                f_val = fieldParser.SFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLORRGBA:
                f_val = fieldParser.MFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3F:
                f_val = fieldParser.SFMatrix3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX4F:
                f_val = fieldParser.SFMatrix4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX3F:
                f_val = fieldParser.MFMatrix3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX4F:
                f_val = fieldParser.MFMatrix4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3D:
                d_val = fieldParser.SFMatrix3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFMATRIX4D:
                d_val = fieldParser.SFMatrix4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX3D:
                d_val = fieldParser.MFMatrix3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX4D:
                d_val = fieldParser.MFMatrix4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
                // in either case, this will be the value "NULL", so just
                // ignore it completely.
                break;

            default:
                errorReporter.warningReport("Unknown field type " +
                                            "provided to ProtoBuilder in parseField(VRMLNodeType,int,int,String)! " +
                                            fieldType, null);
        }
    }

    /**
     * Second convenience method to parse a field string and set it in the
     * destination proto declaration (other version sets it in a node).
     *
     * @param proto The proto declaration to set the field value in
     * @param index The field index for the value
     * @param type The type of the field (from the fieldDecl)
     * @param value The string to parse as the value
     */
    private void parseField(VRMLProtoDeclare proto,
                            int index,
                            int fieldType,
                            String value) {
        if(fieldType != FieldConstants.SFSTRING) {
            value = value.trim();

            if(value.length() == 0)
                return;
        }

        switch(fieldType) {
            case FieldConstants.SFINT32:
                proto.setValue(index, fieldParser.SFInt32(value));
                break;

            case FieldConstants.MFINT32:
                int[] i_val = fieldParser.MFInt32(value);
                proto.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFFLOAT:
                proto.setValue(index, fieldParser.SFFloat(value));
                break;

            case FieldConstants.SFTIME:
                proto.setValue(index, fieldParser.SFTime(value));
                break;

            case FieldConstants.SFDOUBLE:
                proto.setValue(index, fieldParser.SFDouble(value));
                break;

            case FieldConstants.MFTIME:
                double[] d_val = fieldParser.MFTime(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFDOUBLE:
                d_val = fieldParser.MFDouble(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFLONG:
                proto.setValue(index, fieldParser.SFLong(value));
                break;

            case FieldConstants.MFLONG:
                long[] l_val = fieldParser.MFLong(value);
                proto.setValue(index, l_val, l_val.length);
                break;

            case FieldConstants.SFBOOL:
                proto.setValue(index, fieldParser.SFBool(value));
                break;

            case FieldConstants.SFROTATION:
                float[] f_val = fieldParser.SFRotation(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFROTATION:
                f_val = fieldParser.MFRotation(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFBOOL:
                boolean[] b_val = fieldParser.MFBool(value);
                proto.setValue(index, b_val, b_val.length);
                break;

            case FieldConstants.MFFLOAT:
                f_val = fieldParser.MFFloat(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC2F:
                f_val = fieldParser.SFVec2f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3F:
                f_val = fieldParser.SFVec3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFCOLOR:
                f_val = fieldParser.SFColor(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC2F:
                f_val = fieldParser.MFVec2f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC3F:
                f_val = fieldParser.MFVec3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLOR:
                f_val = fieldParser.MFColor(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3D:
                d_val = fieldParser.SFVec3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC2D:
            	d_val = fieldParser.MFVec2d(value);
            	proto.setValue(index, d_val, d_val.length);
            	break;
                
            case FieldConstants.MFVEC3D:
                d_val = fieldParser.MFVec3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFSTRING:
                if(((String)value).length() > 0)
                    proto.setValue(index, fieldParser.SFString(value));
                break;

            case FieldConstants.MFSTRING:
                if(((String)value).length() > 0) {
                    String[] s_val = fieldParser.MFString(value);
                    proto.setValue(index, s_val, s_val.length);
                }
                break;

            case FieldConstants.SFIMAGE:
                i_val = fieldParser.SFImage(value);
                proto.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.MFIMAGE:
                i_val = fieldParser.MFImage(value);
                proto.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFCOLORRGBA:
                f_val = fieldParser.SFColorRGBA(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLORRGBA:
                f_val = fieldParser.MFColorRGBA(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3F:
                f_val = fieldParser.SFMatrix3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX4F:
                f_val = fieldParser.SFMatrix4f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX3F:
                f_val = fieldParser.MFMatrix3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX4F:
                f_val = fieldParser.MFMatrix4f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3D:
                d_val = fieldParser.SFMatrix3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFMATRIX4D:
                d_val = fieldParser.SFMatrix4d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX3D:
                d_val = fieldParser.MFMatrix3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX4D:
                d_val = fieldParser.MFMatrix4d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
                // in either case, this will be the value "NULL", so just
                // ignore it completely.
                break;

            default:
                errorReporter.warningReport("Unknown field type " +
                                            "provided to ProtoBuilder in parseField(VRMLProtoDeclare,int,int,String)! " +
                                            fieldType, null);

        }
    }

    /**
     * Convenience method to parse a field from an array of strings and set
     * it in the destination node. Not all fields can be handled by this form.
     *
     * @param node The node to set the field value in
     * @param index The field index for the value
     * @param type The type of the field (from the fieldDecl)
     * @param value The string to parse as the value
     */
    private void parseField(VRMLNodeType node,
                            int index,
                            int fieldType,
                            String[] value) {

        // If the length is zero, ignore the parse request
        if(value.length == 0)
            return;

        switch(fieldType) {
            case FieldConstants.SFINT32:
            case FieldConstants.SFFLOAT:
            case FieldConstants.SFTIME:
            case FieldConstants.SFDOUBLE:
            case FieldConstants.SFLONG:
            case FieldConstants.SFBOOL:
            case FieldConstants.SFSTRING:
                errorReporter.warningReport("Field not parsable as String[]. " +
                                            node.getVRMLNodeName() + " index: " +
                                            index + " val: " + value[0], null);
                break;

            case FieldConstants.MFINT32:
                int[] i_val = fieldParser.MFInt32(value);
                node.setValue(index, i_val, i_val.length);
                break;


            case FieldConstants.MFTIME:
                double[] d_val = fieldParser.MFTime(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFDOUBLE:
                d_val = fieldParser.MFDouble(value);
                node.setValue(index, d_val, d_val.length);
                break;


            case FieldConstants.MFLONG:
                long[] l_val = fieldParser.MFLong(value);
                node.setValue(index, l_val, l_val.length);
                break;

            case FieldConstants.SFROTATION:
                float[] f_val = fieldParser.SFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFROTATION:
                f_val = fieldParser.MFRotation(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFBOOL:
                boolean[] b_val = fieldParser.MFBool(value);
                node.setValue(index, b_val, b_val.length);
                break;

            case FieldConstants.MFFLOAT:
                f_val = fieldParser.MFFloat(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC2F:
                f_val = fieldParser.SFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3F:
                f_val = fieldParser.SFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC4F:
                f_val = fieldParser.SFVec4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC2F:
                f_val = fieldParser.MFVec2f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC3F:
                f_val = fieldParser.MFVec3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC4F:
                f_val = fieldParser.MFVec4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3D:
                d_val = fieldParser.SFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFVEC4D:
                d_val = fieldParser.SFVec4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC2D:
            	d_val = fieldParser.MFVec2d(value);
            	node.setValue(index, d_val, d_val.length);
            	break;
                
            case FieldConstants.MFVEC3D:
                d_val = fieldParser.MFVec3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC4D:
                d_val = fieldParser.MFVec4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFSTRING:
                String[] s_val = fieldParser.MFString(value);
                node.setValue(index, s_val, s_val.length);
                break;

            case FieldConstants.SFIMAGE:
                i_val = fieldParser.SFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.MFIMAGE:
                i_val = fieldParser.MFImage(value);
                node.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFCOLOR:
                f_val = fieldParser.SFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLOR:
                f_val = fieldParser.MFColor(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFCOLORRGBA:
                f_val = fieldParser.SFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLORRGBA:
                f_val = fieldParser.MFColorRGBA(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3F:
                f_val = fieldParser.SFMatrix3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX4F:
                f_val = fieldParser.SFMatrix4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX3F:
                f_val = fieldParser.MFMatrix3f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX4F:
                f_val = fieldParser.MFMatrix4f(value);
                node.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3D:
                d_val = fieldParser.SFMatrix3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFMATRIX4D:
                d_val = fieldParser.SFMatrix4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX3D:
                d_val = fieldParser.MFMatrix3d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX4D:
                d_val = fieldParser.MFMatrix4d(value);
                node.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
                // in either case, this will be the value "NULL", so just
                // ignore it completely.
                break;

            default:
                errorReporter.warningReport("Unknown field type " +
                                            "provided to ProtoBuilder in parseField(VRMLNodeType,int,int,String[])! " +
                                            fieldType, null);
        }
    }

    /**
     * Convenience method to parse a field from an array of strings and set
     * it in the destination node. Not all fields can be handled by this form.
     *
     * @param proto The proto declaration to set the field value in
     * @param index The field index for the value
     * @param type The type of the field (from the fieldDecl)
     * @param value The string to parse as the value
     */
    private void parseField(VRMLProtoDeclare proto,
                            int index,
                            int fieldType,
                            String[] value) {

        // If the length is zero, ignore the parse request
        if(value.length == 0)
            return;

        switch(fieldType) {
            case FieldConstants.SFINT32:
            case FieldConstants.SFFLOAT:
            case FieldConstants.SFTIME:
            case FieldConstants.SFDOUBLE:
            case FieldConstants.SFLONG:
            case FieldConstants.SFBOOL:
            case FieldConstants.SFSTRING:
                errorReporter.warningReport("Field not parsable as String[]. " +
                                            proto.getVRMLNodeName() + " index: " +
                                            index + " val: " + value[0], null);
                break;

            case FieldConstants.MFINT32:
                int[] i_val = fieldParser.MFInt32(value);
                proto.setValue(index, i_val, i_val.length);
                break;


            case FieldConstants.MFTIME:
                double[] d_val = fieldParser.MFTime(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFDOUBLE:
                d_val = fieldParser.MFDouble(value);
                proto.setValue(index, d_val, d_val.length);
                break;


            case FieldConstants.MFLONG:
                long[] l_val = fieldParser.MFLong(value);
                proto.setValue(index, l_val, l_val.length);
                break;

            case FieldConstants.SFROTATION:
                float[] f_val = fieldParser.SFRotation(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFROTATION:
                f_val = fieldParser.MFRotation(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFBOOL:
                boolean[] b_val = fieldParser.MFBool(value);
                proto.setValue(index, b_val, b_val.length);
                break;

            case FieldConstants.MFFLOAT:
                f_val = fieldParser.MFFloat(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC2F:
                f_val = fieldParser.SFVec2f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3F:
                f_val = fieldParser.SFVec3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC4F:
                f_val = fieldParser.SFVec4f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC2F:
                f_val = fieldParser.MFVec2f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC3F:
                f_val = fieldParser.MFVec3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFVEC4F:
                f_val = fieldParser.MFVec4f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFVEC3D:
                d_val = fieldParser.SFVec3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFVEC4D:
                d_val = fieldParser.SFVec4d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC4D:
                d_val = fieldParser.MFVec4d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC3D:
                d_val = fieldParser.MFVec3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFVEC2D:
            	d_val = fieldParser.MFVec2d(value);
            	proto.setValue(index, d_val, d_val.length);
            	break;
                
            case FieldConstants.MFSTRING:
                String[] s_val = fieldParser.MFString(value);
                proto.setValue(index, s_val, s_val.length);
                break;

            case FieldConstants.SFIMAGE:
                i_val = fieldParser.SFImage(value);
                proto.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.MFIMAGE:
                i_val = fieldParser.MFImage(value);
                proto.setValue(index, i_val, i_val.length);
                break;

            case FieldConstants.SFCOLOR:
                f_val = fieldParser.SFColor(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLOR:
                f_val = fieldParser.MFColor(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFCOLORRGBA:
                f_val = fieldParser.SFColorRGBA(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFCOLORRGBA:
                f_val = fieldParser.MFColorRGBA(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3F:
                f_val = fieldParser.SFMatrix3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX4F:
                f_val = fieldParser.SFMatrix4f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX3F:
                f_val = fieldParser.MFMatrix3f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.MFMATRIX4F:
                f_val = fieldParser.MFMatrix4f(value);
                proto.setValue(index, f_val, f_val.length);
                break;

            case FieldConstants.SFMATRIX3D:
                d_val = fieldParser.SFMatrix3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFMATRIX4D:
                d_val = fieldParser.SFMatrix4d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX3D:
                d_val = fieldParser.MFMatrix3d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.MFMATRIX4D:
                d_val = fieldParser.MFMatrix4d(value);
                proto.setValue(index, d_val, d_val.length);
                break;

            case FieldConstants.SFNODE:
            case FieldConstants.MFNODE:
                // in either case, this will be the value "NULL", so just
                // ignore it completely.
                break;

            default:
                errorReporter.warningReport("Unknown field type " +
                                            "provided to ProtoBuilder in parseField(VRMLProtoDeclar,int,int,String[])! " +
                                            fieldType, null);
        }
    }
}
