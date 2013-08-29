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

package org.web3d.vrml.renderer;

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
import org.web3d.vrml.nodes.proto.AbstractProto;
import org.web3d.vrml.nodes.proto.ExternalPrototypeDecl;

/**
 * A SAV interface for dealing with building a single extern proto.
 * <p>
 *
 * The builder is designed to create a single proto. However, that single proto
 * may well have nested protos as part of it, so we must deal with that too.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class CRExternProtoBuilder
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

    // Objects used during multiple runs

    /** Flag to indicate that one time initialisation process has happened */
    private boolean init;

    /** The error handler used for reporting errors */
    protected ErrorReporter errorReporter;

    /** The document locator to find error information */
    private Locator locator;

    /** Flag to indicate we are inside an extern proto declaration */
    private boolean inExternProtoDecl = false;

    /** Flag to indicate we are inside an extern proto URI */
    private boolean inExternProtoURI = false;

    /** Copy of the top child index for efficiency purposes */
    private int currentFieldIndex;

    /** Copy of the current working protype definition */
    private AbstractProto currentProto;

    // Variables replace or reset during each parsing run

    /** The root proto declaration */
    private AbstractProto rootProto;

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

    /** The working stack of currently defined PROTO declarations */
    private SimpleStack protoDeclStack;

    /** The working stack of currently building scripts. */
    private SimpleStack scriptStack;

    /** The working stack of def maps */
    private SimpleStack defMapStack;

    /** The working stack of proto decls maps */
    private SimpleStack protoMapStack;

    /** Flag indicating what mode - VRML97 or VRML3.0 that we're using */
    private boolean isVrml97;

    /** The major version of the spec this file belongs to. */
    private int majorVersion;

    /** The minor version of the spec this file belongs to. */
    private int minorVersion;

    /** Set of DEF names to ignore when we come to USE decls */
    private HashSet ignoreDefSet;

    /** The current world's root URL */
    private String worldURL;

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** Frame state manager for this event model instance */
    private FrameStateManager stateManager;

    /** Proto creator for this scene */
    private NodeTemplateToInstanceCreator protoCreator;

    /**
     * Create a new default instance of the scene builder. This uses the
     * default factory for nodes non-renderable nodes.
     *
     * @throws NullPointerException The factory reference is null
     */
    protected CRExternProtoBuilder(VRMLNodeFactory fac)
        throws NullPointerException {

        nodeFactory = fac;
        defMap = new HashMap();
        protoMap = new HashMap();
        externProtoMap = new HashMap();

        protoDeclStack = new SimpleStack();
        scriptStack = new SimpleStack();
        defMapStack = new SimpleStack();
        protoMapStack = new SimpleStack();

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

        rootProto = null;

        topOfStack = -1;
        currentFieldIndex = 0;

        worldURL = url;

        inExternProtoDecl = false;
        inExternProtoURI = false;

        int major = 3;
        int minor = 0;

        if(type.charAt(1) == 'V') {
            // we're in VRML model either 97 or 1.0.
            // Look at the 6th character to see the version number
            // ie "VRML V1.0" or "VRML V2.0"
            boolean is_20 = (version.charAt(1) == '2');

            if(is_20)
                major = 2;
        } else {
            // Parse the number string looking for the version minor number.
            int dot_index = version.indexOf('.');
            String minor_num = version.substring(dot_index + 1);

            // Should this look for a badly formatted number here or just
            // assume the parsing beforehad has correctly identified something
            // already dodgy?
            minor = Integer.parseInt(minor_num);
        }

        majorVersion = major;
        minorVersion = minor;

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
        throw new SAVException("Externprotos cannot contain PROFILE statements");
    }

    /**
     * A component declaration has been found in the code. A proto builder
     * should never encounter this situation.
     *
     * @param componentName The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void componentDecl(String componentName)
        throws SAVException, VRMLException {
        throw new SAVException("Externprotos cannot contain COMPONENT statements");
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
        // Ignored
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

        topOfStack = 0;
        currentProto = null;
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
        throw new SAVException("Externprotos cannot contain IMPORT statements");
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
        throw new SAVException("Externprotos cannot contain EXPORT statements");
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

        if(!inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                       "proto field declaration not in proto!");

        VRMLFieldDeclaration field =
            new VRMLFieldDeclaration(access, type, name);

        int index = currentProto.appendField(field);

        currentFieldIndex = index;

        if((value != null) && (currentProto instanceof VRMLProtoDeclare))
            throw new VRMLException("Externproto statement has a field value");

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
        if(inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                 "Starting proto body while still in declaration");

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
        if(inExternProtoDecl)
            throw new VRMLParseException(locator.getLineNumber(),
                                         locator.getColumnNumber(),
                                         "Nested PROTO declarations not allowed");

        CRExternPrototypeDecl proto = createDecl(name,
                                                 nodeFactory,
                                                 majorVersion,
                                                 minorVersion,
                                                 protoCreator);
        proto.setFrameStateManager(stateManager);

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
        ((ExternalPrototypeDecl)currentProto).setWorldUrl(worldURL);
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

    }

    //---------------------------------------------------------------
    // Misc Local methods
    //---------------------------------------------------------------

    /**
     * Create a declaration suitable for filling in as a new proto. There
     * is no need to set the frame state manager because that will be
     * done by this class.
     *
     * @param name The name applied to the proto decl in the file
     * @param fac The factory to use for creating the instances
     * @param majorVersion The major version number of this scene
     * @param minorVersion The minor version number of this scene
     * @param creator The node creator for generating instances of ourself
     */
    public abstract CRExternPrototypeDecl createDecl(String name,
                                                     VRMLNodeFactory fac,
                                                     int majorVersion,
                                                     int minorVersion,
                                                     NodeTemplateToInstanceCreator creator);

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
        inExternProtoDecl = false;
        inExternProtoURI = false;

        worldURL = null;

        // The rest of the fields are reset in the startDocument callback
        defMap.clear();
        protoMap.clear();
        externProtoMap.clear();
        globalDefMap.clear();
        globalProtoMap.clear();
        globalExternProtoMap.clear();
        protoDeclStack.clear();
        defMapStack.clear();
        protoMapStack.clear();
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
     * by the Java 3D Loader  Nodes defined as externals are inlines
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
     * @param useBehaviors true if BehaviorNodeTypes should enable their
     *   behaviors
     */
    public void setLoadRequirements(boolean useVrml97,
                                    boolean useBackgrounds,
                                    boolean useFogs,
                                    boolean useLights,
                                    boolean useAudio,
                                    boolean useViewpoints,
                                    boolean useExternals,
                                    boolean useBehaviors) {
        isVrml97 = useVrml97;
    }

    /**
     * Start a new proto definition within the current document context.
     * @param creator The proto creator to wrap the template with
     */
    public void newProto(NodeTemplateToInstanceCreator creator) {
        protoCreator = creator;
        rootProto = null;

        topOfStack = -1;
        currentFieldIndex = 0;

        inExternProtoDecl = false;
        inExternProtoURI = false;
    }

    /**
     * Set the frame state manager to be used by this proto instance.
     * used to pass in information about the contained scene for extern
     * proto handler.
     *
     * @param mgr The manager instance to use
     */
    public void setFrameStateManager(FrameStateManager mgr) {
        stateManager = mgr;
    }
}
