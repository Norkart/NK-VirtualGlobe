/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender;

// External imports
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ProtoBuilder;
import org.web3d.vrml.nodes.proto.ExternalPrototypeDecl;
import org.web3d.vrml.renderer.CRVRMLScene;
import org.web3d.vrml.renderer.DefaultLocator;
import org.web3d.vrml.renderer.DefaultNodeFactory;

import org.xj3d.core.loading.SceneBuilder;

/**
 * A Java3D file loader implementation for reading VRML utf8 files and
 * building a null renderer scenegraph with them.
 * <p>
 *
 * If the user asks for no behaviors, then we will still load nodes that
 * use behaviors, but will disable their use. For example, a LOD will still
 * need to have all of the geometry loaded, just not shown or activated
 * because the LOD's internal behavior is disabled.
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public class NRVRMLSceneBuilder implements SceneBuilder {

    // Various standard constants

    // Objects used during multiple runs

    /** The error handler used for reporting errors */
    private ErrorReporter errorReporter;

    /** The document locator to find error information */
    private Locator locator;

    /** Current content handler reference */
    private StringContentHandler contentHandler;

    /** Current proto handler reference */
    private ProtoHandler protoHandler;

    /** Current script handler reference */
    private ScriptHandler scriptHandler;

    /** Current route handler reference */
    private RouteHandler routeHandler;

    /** The main scene builder */
    private NRMainSceneBuilder mainSceneBuilder;

    /** Builder for proto content */
    private ProtoBuilder protoBuilder;

    /** Builder for proto content */
    private NRExternProtoBuilder externProtoBuilder;

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** Flag indicating what mode - VRML97 or VRML3.0 that we're using */
    private boolean isVrml97;

    /** The mapping of proto names (key) to node instances (value) */
    private HashMap protoMap;

    /** The mapping of externproto names (key) to node instances (value) */
    private HashMap externProtoMap;

    /**
     * Flag to indicate this is currently parsing a document and should not
     * start another until this is finished.
     */
    private boolean isParsing;

    /** The current world's root URL */
    private String worldURL;

    /**
     * Nesting counter for proto declarations so we know when to swap the
     * SAV callback notifications.
     */
    private int protoNestings;

    /** The version number of this file.  2.0 for VRML97, 3.0 for X3D */
    private String version;

    /**
     * Create a new default instance of the scene builder. This uses the
     * default factory for nodes.
     */
    public NRVRMLSceneBuilder() {
        this(null);
    }

    /**
     * Create a scene builder with the given node factory. The factory should
     * be producing nodes that conform to the
     * {@link org.web3d.vrml.renderer.norender.nodes.NRVRMLNode} interface as we
     * expect some of the capabilities to be there. Using any other form of
     * factory is asking for errors. If the factory reference is null then the
     * default factory will be used.
     *
     * @param fac The factory instance to use.
     */
    public NRVRMLSceneBuilder(VRMLNodeFactory fac) {

        nodeFactory = (fac != null) ? fac : NRNodeFactory.getNRNodeFactory();

        HashMap defs = new HashMap();
        protoMap = new HashMap();
        externProtoMap = new HashMap();

        mainSceneBuilder = new NRMainSceneBuilder(nodeFactory);
        mainSceneBuilder.setGlobals(defs, protoMap, externProtoMap);

        VRMLNodeFactory null_fac =
            DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);

        protoBuilder = new ProtoBuilder(null_fac);
        protoBuilder.setGlobals(defs, protoMap, externProtoMap);

        externProtoBuilder = new NRExternProtoBuilder(nodeFactory);
        externProtoBuilder.setGlobals(defs, protoMap, externProtoMap);

        isParsing = false;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by SceneBuilder
    //----------------------------------------------------------

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

        mainSceneBuilder.setErrorReporter(errorReporter);
        externProtoBuilder.setErrorReporter(errorReporter);
        protoBuilder.setErrorReporter(errorReporter);
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
    public VRMLScene getScene() {
        return mainSceneBuilder.getScene();
    }

    /**
     * Release any references to the scene that was last built by this scene handler.
	 * This should be called by the loader after a reference to the scene has been 
	 * retrieved using the getScene() method.
     */
    public void releaseScene() {
		mainSceneBuilder.releaseScene();
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
        isParsing = false;

        mainSceneBuilder.reset();
        protoBuilder.reset();
        externProtoBuilder.reset();
    }

    /**
     * Change the builder to recognise only VRML97 content or allow any
     * version to be loaded.
     *
     * @param enabled true to restrict content to VRML97 only
     */
    public void allowVRML97Only(boolean enabled) {
        isVrml97 = enabled;
    }

    /**
     * Set the frame state manager to use for the builder after
     * this point. Set a value of null it will clear the currently set items.
     *
     * @param fsm The state manager to use
     */
    public void setFrameStateManager(FrameStateManager fsm) {
        mainSceneBuilder.setFrameStateManager(fsm);
        externProtoBuilder.setFrameStateManager(fsm);
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

        mainSceneBuilder.setDocumentLocator(loc);
        protoBuilder.setDocumentLocator(loc);
        externProtoBuilder.setDocumentLocator(loc);
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

        if(isParsing)
            throw new SAVException("Currently parsing a document");

        isParsing = true;

        if(locator == null)
            setDocumentLocator(new DefaultLocator());

        // Remove the leading V on the version
        this.version = version.substring(1);

        mainSceneBuilder.startDocument(uri,url, encoding, type, version, comment);
        protoBuilder.startDocument(uri,url, encoding, type, version, comment);
        externProtoBuilder.startDocument(uri,url, encoding, type, version, comment);

        protoNestings = 0;

        contentHandler = mainSceneBuilder;
        protoHandler = mainSceneBuilder;
        scriptHandler = mainSceneBuilder;
        routeHandler = mainSceneBuilder;
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
        mainSceneBuilder.profileDecl(profileName);
        protoBuilder.profileDecl(profileName);
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
        mainSceneBuilder.componentDecl(componentInfo);
        protoBuilder.componentDecl(componentInfo);
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

        contentHandler.metaDecl(key, value);
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
        contentHandler.importDecl(inline, exported, imported);
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
        contentHandler.exportDecl(defName, exported);
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

        mainSceneBuilder.endDocument();
        protoBuilder.endDocument();
        externProtoBuilder.endDocument();

        isParsing = false;
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

        contentHandler.startNode(name, defName);
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
        contentHandler.endNode();
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
        contentHandler.startField(name);
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
        contentHandler.fieldValue(value);
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
        contentHandler.fieldValue(values);
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
        contentHandler.useDecl(defName);
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
        contentHandler.endField();
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

        ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value,len);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value,len);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value,len);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value,len);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value,len);
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

        ((BinaryContentHandler)contentHandler).fieldValue(value,len);
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
        if(protoNestings == 0) {
            contentHandler = protoBuilder;
            scriptHandler = protoBuilder;
            routeHandler = protoBuilder;
            protoHandler = protoBuilder;

            protoBuilder.newProto(mainSceneBuilder.getProtoCreator());
        }

        protoNestings++;
        protoHandler.startProtoDecl(name);
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
        protoHandler.endProtoDecl();
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
        protoHandler.protoFieldDecl(access, type, name, value);
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
        protoHandler.protoIsDecl(fieldName);
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
        protoHandler.startProtoBody();
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
        protoHandler.endProtoBody();

        protoNestings--;

        if(protoNestings == 0) {
            contentHandler = mainSceneBuilder;
            protoHandler = mainSceneBuilder;
            scriptHandler = mainSceneBuilder;
            routeHandler = mainSceneBuilder;

            PrototypeDecl proto = (PrototypeDecl)protoBuilder.getPrototype();

            protoMap.put(proto.getVRMLNodeName(), proto);
            CRVRMLScene scene = mainSceneBuilder.getScene();
            scene.addProto(proto);
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
        if(protoNestings != 0) {
            //if we are nested pass the extern decl to the the protoBuilder
            contentHandler = protoBuilder;
            scriptHandler = protoBuilder;
            routeHandler = protoBuilder;
            protoHandler = protoBuilder;

            protoBuilder.newProto(mainSceneBuilder.getProtoCreator());
        }
        else {
            //we are not nested so build a NR specific one
            contentHandler = externProtoBuilder;
            scriptHandler = externProtoBuilder;
            routeHandler = externProtoBuilder;
            protoHandler = externProtoBuilder;

            externProtoBuilder.newProto(mainSceneBuilder.getProtoCreator());
        }
        protoNestings++;
        protoHandler.startExternProtoDecl(name);
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
        protoHandler.endExternProtoDecl();
        protoNestings--;
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
        protoHandler.externProtoURI(values);

        if (protoNestings == 0) {
            contentHandler = mainSceneBuilder;
            protoHandler = mainSceneBuilder;
            scriptHandler = mainSceneBuilder;
            routeHandler = mainSceneBuilder;

            ExternalPrototypeDecl proto = (ExternalPrototypeDecl)
                externProtoBuilder.getPrototype();

            externProtoMap.put(proto.getVRMLNodeName(), proto);
            CRVRMLScene scene = mainSceneBuilder.getScene();
            scene.addExternProto(proto);
        }

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
        scriptHandler.startScriptDecl();
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
        scriptHandler.endScriptDecl();
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

        scriptHandler.scriptFieldDecl(access, type, name, value);
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

        routeHandler.routeDecl(srcNodeName,
                               srcFieldName,
                               destNodeName,
                               destFieldName);
    }

    //----------------------------------------------------------
    // Public convenience methods.
    //----------------------------------------------------------

    /**
     * Set the flags about what to load. The flags defined here are matched
     * by the Java 3D Loader Nodes defined as externals are inlines
     * and extern protos. This method should be called after the
     * {@link #allowVRML97Only(boolean)} method as it uses that flag.
     *
     * @param useBackgrounds true if BackgroundNodeTypes should be loaded
     * @param useFogs true if FogNodeTypes should be loaded
     * @param useLights true if LightNodeTypes should be loaded
     * @param useAudio true if AudioClipNodeTypes should be loaded
     * @param useViewpoints true if ViewpointNodeTypes should loaded
     * @param useExternals true if ExternalNodeTypes should be loaded
     */
    public void setLoadRequirements(boolean useBackgrounds,
                                    boolean useFogs,
                                    boolean useLights,
                                    boolean useAudio,
                                    boolean useViewpoints,
                                    boolean useExternals) {

        mainSceneBuilder.setLoadRequirements(isVrml97,
                                             useBackgrounds,
                                             useFogs,
                                             useLights,
                                             useAudio,
                                             useViewpoints,
                                             useExternals);

        protoBuilder.setLoadRequirements(isVrml97,
                                         useBackgrounds,
                                         useFogs,
                                         useLights,
                                         useAudio,
                                         useViewpoints,
                                         useExternals);
    }
}
