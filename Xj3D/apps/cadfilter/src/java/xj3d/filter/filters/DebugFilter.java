/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
// None

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.AbstractFilter;

/**
 * A debug filter that prints out everything that passes through it and then
 * passes the information onwards to the next in the chain.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DebugFilter extends AbstractFilter {

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
        System.out.println("setDocumentLocator(" + loc + ")");

        super.setDocumentLocator(loc);
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

        System.out.println("startDocument(" + uri + ", " + url + ", "  +
            encoding + ", " + type + ", " + version + ", " + comment + ")");


        super.startDocument(uri, url, encoding, type, version, comment);
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

        System.out.println("profileDecl(" + profileName + ")");

        super.profileDecl(profileName);
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentInfo The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    public void componentDecl(String componentInfo)
        throws SAVException, VRMLException {

        System.out.println("componentDecl(" + componentInfo + ")");

        super.componentDecl(componentInfo);
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

        System.out.println("metaDecl(" + key + ", " + value + ")");

        super.metaDecl(key, value);
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

        System.out.println("importDecl(" + inline + ", " + exported +
                           ", " + imported + ")");

        super.importDecl(inline, exported, imported);
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

        System.out.println("exportDecl(" + defName + ", " + exported + ")");

        super.exportDecl(defName, exported);
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

       System.out.println("endDocument()");

       super.endDocument();
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

        System.out.println("startNode(" + name + ", " + defName + ")");

        super.startNode(name, defName);
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
        System.out.println("endNode()");

        super.endNode();
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
        System.out.println("startField(" + name + ")");

        super.startField(name);
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

        System.out.println("useDecl(" + defName + ")");

        super.useDecl(defName);
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
        System.out.println("endField()");

        super.endField();
    }

    //---------------------------------------------------------------
    // Methods defined by StringContentHandler
    //---------------------------------------------------------------

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

        System.out.println("fieldValue(String)");

        if(contentHandler instanceof StringContentHandler)
            ((StringContentHandler)contentHandler).fieldValue(value);
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

        System.out.println("fieldValue(String[])");

        if(contentHandler instanceof StringContentHandler)
            ((StringContentHandler)contentHandler).fieldValue(values);
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

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

        System.out.println("fieldValue(int)");

        if(contentHandler instanceof BinaryContentHandler)
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

        System.out.println("fieldValue(int[], int)");

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        System.out.println("fieldValue(boolean)");

        if(contentHandler instanceof BinaryContentHandler)
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

        System.out.println("fieldValue(boolean[], int)");

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        System.out.println("fieldValue(float)");

        if(contentHandler instanceof BinaryContentHandler)
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

        System.out.println("fieldValue(float[], int)");

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        System.out.println("fieldValue(long)");

        if(contentHandler instanceof BinaryContentHandler)
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

        System.out.println("fieldValue(long[], int)");

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        System.out.println("fieldValue(double)");

        if(contentHandler instanceof BinaryContentHandler)
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

        System.out.println("fieldValue(double[], int)");

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
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

        System.out.println("fieldValue(String[], int)");

        if(contentHandler instanceof BinaryContentHandler)
            ((BinaryContentHandler)contentHandler).fieldValue(value, len);
    }

    //---------------------------------------------------------------
    // Methods defined by ProtoHandler
    //---------------------------------------------------------------

    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void startProtoDecl(String name) throws SAVException, VRMLException {

        System.out.println("startProtoDecl(" + name + ")");

        super.startProtoDecl(name);
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void endProtoDecl() throws SAVException, VRMLException {
        System.out.println("endProtoDecl()");

        super.endProtoDecl();
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
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException {

        System.out.println("protoFieldDecl(" + access + ", " + type + ", " +
                           name + ", " + value + ")");

        super.protoFieldDecl(access, type, name, value);
    }

    /**
     * Notification of a field value uses an IS statement. If we are running
     * in VRML97 mode, this will throw an exception if the field access types
     * do not match.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void protoIsDecl(String fieldName) throws SAVException, VRMLException {
        System.out.println("protoIsDecl(" + fieldName + ")");
        super.protoIsDecl(fieldName);
    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scenegraph information.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void startProtoBody() throws SAVException, VRMLException {
        System.out.println("startProtoBody()");

        super.startProtoBody();
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void endProtoBody() throws SAVException, VRMLException {
        System.out.println("endProtoBody()");
        super.endProtoBody();
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void startExternProtoDecl(String name) throws SAVException, VRMLException {
        System.out.println("startExternProtoDecl(" + name + ")");
        super.startExternProtoDecl(name);
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void endExternProtoDecl() throws SAVException, VRMLException {
        System.out.println("endExternProtoDecl()");

        super.endExternProtoDecl();
    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list of URIs. The calling application is required to interpet the
     * incoming strings. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
        System.out.println("externProtoURI()");

        super.externProtoURI(values);
    }

    //---------------------------------------------------------------
    // Methods defined by ScriptHandler
    //---------------------------------------------------------------

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
        System.out.println("startScriptDecl()");
        super.startScriptDecl();
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
        System.out.println("endScriptDecl()");
        super.endScriptDecl();
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

        System.out.println("scriptFieldDecl( " + access + ", " + type + ", " +
                           name + ", " + value + ")");

        super.scriptFieldDecl(access, type, name, value);
    }

    //---------------------------------------------------------------
    // Methods defined by RouteHandler
    //---------------------------------------------------------------

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

        System.out.println("routeDecl(" + srcNodeName + ", " + srcFieldName +
                           ", " + destNodeName + ", " + destFieldName + ")");

        super.routeDecl(srcNodeName,
                        srcFieldName,
                        destNodeName,
                        destFieldName);
    }
}
