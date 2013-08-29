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

package org.web3d.x3d.jaxp;

// External imports
import java.io.IOException;
import java.io.InputStream;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

// Local imports
import org.web3d.util.*;
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;

/**
 * Interface adapter between XML input from a SAX source to the SAV source
 * used by the other parsers.
 * <p>
 * The implementation is a simplistic way for quickly getting XML content into
 * the normal VRML input sources by adapting the SAX feeds to SAV feeds.
 *
 * @author Justin Couch
 * @version $Revision: 1.45 $
 */
public class X3DSAVAdapter
    implements LexicalHandler, org.xml.sax.ContentHandler {

    /** Name of the property file holding the default container fields */
    protected static final String CONTAINER_PROPS_FILE = "XMLcontainerfields.properties";

    /** Message because the DTD name is wrong */
    protected static final String INVALID_DTD_NAME_MSG =
        "The DTD name provided is wrong: ";

    /** Message because the PUBLIC ID is wrong */
    protected static final String INVALID_PUBLIC_ID_MSG =
        "The PUBLIC ID provided is wrong";

    /** Message because the SYSTEM ID is wrong */
    protected static final String INVALID_SYSTEM_ID_MSG =
        "The SYSTEM ID provided is wrong";

    /** Message when transitional content detected */
    protected static final String TRANSITIONAL_HEADER_MSG =
        "The file uses the old, out of date transitional header. Please " +
        "upgrade the content to the correct header required. Soon we may " +
        "refuse to load this content.";

    /** Message when transitional content detected */
    protected static final String UNKNOWN_DTD_MSG =
        "The file uses an unknown DTD.  This content is not validated " +
        "against the X3D standard DTD.";

    /** Message for a bad field access type */
    protected static final String UNKNOWN_ACCESS_TYPE_MSG =
        "The field has declared an invalid access type: ";

    /** Message when no profile was given */
    protected static final String NO_PROFILE_MSG =
        "No profile defined and is a required attribute for a valid X3D file.";

    /** Message when there was no scene tag provided */
    protected static final String NO_SCENE_TAG_MSG =
        "No Scene element was defined before starting the file body.";

    /** Message when no profile was given */
    protected static final String USE_WITH_KIDS_MSG =
        "A USE has been declared and there are child elements provided";

    /** Message when we couldn't find any container properties file */
    private static final String NO_CONTAINER_PROPS_MSG =
        "Couldn't find container properties file";

    /** Message when we couldn't read the container properties file */
    private static final String CONTAINER_PROPS_READ_ERR =
        "Error reading container properties file";

    /**
     * When we get an element with an ID as a stock element type, but
     * have no idea what it is. Basically we should never see this message,
     * but it is there for safety.
     */
    private static final String UNKNOWN_ELEMENT_MSG =
        "Encountered an element claiming to be a standard element for " +
        "X3D, but we can't map it to anything useful. This is bad. " +
        "The element name is: ";

    /** Keyword describing the XML encoding for the start doc call */
    protected static final String XML_ENCODING = "xml";

    // Internal IDs for each important element type in the document.
    // This lists the important structural elements. If it is not
    // one of these, we assume it is the name of a node.
    protected static final int X3D_TAG = 1;
    protected static final int COMPONENT_TAG = 2;
    protected static final int SCENE_TAG = 3;
    protected static final int PROTO_DECL_TAG = 4;
    protected static final int EXTERNPROTO_DECL_TAG = 5;
    protected static final int IS_TAG = 6;
    protected static final int FIELD_TAG = 7;
    protected static final int META_TAG = 8;
    protected static final int PROTO_INSTANCE_TAG = 9;
    protected static final int IMPORT_TAG = 10;
    protected static final int EXPORT_TAG = 11;
    protected static final int ROUTE_TAG = 12;
    protected static final int FIELD_VALUE_TAG = 13;
    protected static final int SCRIPT_TAG = 14;
    protected static final int CONNECT_TAG = 15;
    protected static final int HEAD_TAG = 16;
    protected static final int PROTO_INTERFACE_TAG = 17;
    protected static final int PROTO_BODY_TAG = 18;

    /** String constant of the name attribute */
    protected static final String NAME_ATTR = "name";

    /** String constant of the version attribute */
    protected static final String VERSION_ATTR = "version";

    /** String constant of the value attribute */
    protected static final String VALUE_ATTR = "value";

    /** String constant of the DEF attribute */
    protected static final String DEF_ATTR = "DEF";

    /** String constant of the USE attribute */
    protected static final String USE_ATTR = "USE";

    /** String constant of the AS attribute */
    protected static final String AS_ATTR = "AS";

    /** String constant of the Class attribute */
    protected static final String CLASS_ATTR = "class";

    /** String constant of the containerField attribute */
    protected static final String CONTAINER_ATTR = "containerField";

    // Constants for looking up the attributes for node processing */
    protected static final int NAME_ATTR_ID = 1;
    protected static final int DEF_ATTR_ID = 2;
    protected static final int USE_ATTR_ID = 3;
    protected static final int CLASS_ATTR_ID = 4;
    protected static final int CONTAINER_ATTR_ID = 5;

    // Global constant maps

    /** Map the public ID to the spec version */
    protected static HashMap specVersionMap;

    /** Map the spec version to version string */
    protected static HashMap specStringMap;

    /** Set of permitted public IDs */
    protected static HashSet allowedPublicIDs;

    /** Set of permitted system IDs */
    protected static HashSet allowedSystemIDs;

    /** Set of permitted schema IDs */
    protected static HashSet systemSchemaIDs;

    /** Mapping of field access type strings to constant integers */
    protected static HashMap fieldAccessMap;

    /** Mapping of element names strings to constants */
    protected static HashMap elementMap;

    /** Mapping of reserved attribute names strings to constants */
    protected static HashMap attributeMap;

    // Local class variables

    /** The version string defined by the spec */
    protected String versionString;

    /** Property for container fields */
    protected Properties containerFields;

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** The document SAX locator to find error information */
    protected Locator saxLocator;

    /** The SAV locator to find error information */
    protected org.web3d.vrml.sav.Locator savLocator;

    /** Reference to the registered content handler if we have one */
    protected ContentHandler contentHandler;

    /** Reference to the registered route handler if we have one */
    protected RouteHandler routeHandler;

    /** Reference to the registered script handler if we have one */
    protected ScriptHandler scriptHandler;

    /** Reference to the registered proto handler if we have one */
    protected ProtoHandler protoHandler;

    /** World URL, for passing on to the listener */
    protected String worldURL;

    /** The full URL of the current world */
    protected String fullURL;

    /** Flag to say the previous startNode() was actually a USE */
    protected boolean useIsCurrent;

    /**
     * The buffer to put all the scripting string into as we're building up
     * CDATA information.
     */
    protected StringBuffer characterDataBuffer;

    /** Stack holding proto and script declaration semantics */
    protected BooleanStack scriptFlagStack;

    /** Flag to say we're in the processing of a script fields right now */
    protected boolean inScript;

    /** Flag to say we're in the processing of any field element right now */
    protected int fieldDeclDepth;

    /**
     * Stack of protoBodyDepth counts as we traverse the script and
     * proto declarations.
     */
    protected IntStack depthCountStack;

    /**
     * Stack of fieldDeclDepth counts as we traverse the script and
     * proto instance declarations, particularly field value calls
     */
    protected IntStack declDepthStack;

    /** Should we attempt to makeup for missing lexical events */
    protected boolean overrideLex;

    /** Flag to say we need to check next for a valid scene tag */
    protected boolean checkForSceneTag;

    /** Stored extern proto url to smooth model differences */
    protected String epUrl;

    /** Stack of flags on whether a Script had a url.  Used to ignore CDATA. */
    protected BooleanStack scriptUrlStack;

    /**
     * The ID string that is associated with the X3D namespace from the
     * incoming document. This will be defined in the X3D tag as one of the
     * attributes.
     */
    protected String x3dNamespaceId;

    /**
     * A set of prefixes that we know about for the namespace IDs. These will
     * be extracted from the xmlns: values in the X3D tag.
     */
    protected HashSet namespacePrefixes;

    /**
     * Flag indicating if the X3D namespace is set as the one that does not
     * need to be prepended. This can be determined from the
     * <code>xsd:noNamespaceSchemaLocation</code> attribute on the X3D tag.
     * We look for one of the matching schema locations.
     */
    protected boolean checkForX3DNamespace;

    /**
     * Set up the mapping of the element names to the internal ids
     */
    static {
        specVersionMap = new HashMap();
        specStringMap = new HashMap();

        allowedPublicIDs = new HashSet();
        allowedSystemIDs = new HashSet();

        // Temporary OLD IDs. Once 19776-2 is approved as IS, these should be
        // deleted from the code.
        allowedSystemIDs.add(X3DConstants.OLD_SYSTEM_ID);
        allowedSystemIDs.add(X3DConstants.TRANS_SYSTEM_ID);

        // V3.0 definitions.
        Float spec_ver = new Float(3.0f);
        specStringMap.put(spec_ver, "V3.0");

        specVersionMap.put(X3DConstants.GENERAL_PUBLIC_ID_3_0, spec_ver);
        specVersionMap.put(X3DConstants.INTERCHANGE_PUBLIC_ID_3_0, spec_ver);
        specVersionMap.put(X3DConstants.INTERACTIVE_PUBLIC_ID_3_0, spec_ver);
        specVersionMap.put(X3DConstants.IMMERSIVE_PUBLIC_ID_3_0, spec_ver);
        specVersionMap.put(X3DConstants.FULL_PUBLIC_ID_3_0, spec_ver);
        specVersionMap.put(X3DConstants.TRANS_PUBLIC_ID, spec_ver);

        allowedPublicIDs = new HashSet();
        allowedPublicIDs.add(X3DConstants.GENERAL_PUBLIC_ID_3_0);
        allowedPublicIDs.add(X3DConstants.INTERCHANGE_PUBLIC_ID_3_0);
        allowedPublicIDs.add(X3DConstants.INTERACTIVE_PUBLIC_ID_3_0);
        allowedPublicIDs.add(X3DConstants.IMMERSIVE_PUBLIC_ID_3_0);
        allowedPublicIDs.add(X3DConstants.FULL_PUBLIC_ID_3_0);
        allowedPublicIDs.add(X3DConstants.OLD_PUBLIC_ID);
        allowedPublicIDs.add(X3DConstants.TRANS_PUBLIC_ID);

        allowedSystemIDs.add(X3DConstants.GENERAL_SYSTEM_ID_3_0);

        // V3.1 definitions.
        spec_ver = new Float(3.1f);
        specStringMap.put(spec_ver, "V3.1");

        specVersionMap.put(X3DConstants.GENERAL_PUBLIC_ID_3_1, spec_ver);
        specVersionMap.put(X3DConstants.INTERCHANGE_PUBLIC_ID_3_1, spec_ver);
        specVersionMap.put(X3DConstants.CAD_INTERCHANGE_PUBLIC_ID_3_1, spec_ver);
        specVersionMap.put(X3DConstants.INTERACTIVE_PUBLIC_ID_3_1, spec_ver);
        specVersionMap.put(X3DConstants.IMMERSIVE_PUBLIC_ID_3_1, spec_ver);
        specVersionMap.put(X3DConstants.FULL_PUBLIC_ID_3_1, spec_ver);

        allowedPublicIDs.add(X3DConstants.GENERAL_PUBLIC_ID_3_1);
        allowedPublicIDs.add(X3DConstants.INTERCHANGE_PUBLIC_ID_3_1);
        allowedPublicIDs.add(X3DConstants.CAD_INTERCHANGE_PUBLIC_ID_3_1);
        allowedPublicIDs.add(X3DConstants.INTERACTIVE_PUBLIC_ID_3_1);
        allowedPublicIDs.add(X3DConstants.IMMERSIVE_PUBLIC_ID_3_1);
        allowedPublicIDs.add(X3DConstants.FULL_PUBLIC_ID_3_1);
        allowedPublicIDs.add(X3DConstants.OLD_PUBLIC_ID);
        allowedPublicIDs.add(X3DConstants.TRANS_PUBLIC_ID);

        allowedSystemIDs.add(X3DConstants.GENERAL_SYSTEM_ID_3_1);

        // V3.2 definitions.
        spec_ver = new Float(3.2f);
        specStringMap.put(spec_ver, "V3.2");

        specVersionMap.put(X3DConstants.GENERAL_PUBLIC_ID_3_2, spec_ver);
        specVersionMap.put(X3DConstants.INTERCHANGE_PUBLIC_ID_3_2, spec_ver);
        specVersionMap.put(X3DConstants.CAD_INTERCHANGE_PUBLIC_ID_3_2, spec_ver);
        specVersionMap.put(X3DConstants.INTERACTIVE_PUBLIC_ID_3_2, spec_ver);
        specVersionMap.put(X3DConstants.IMMERSIVE_PUBLIC_ID_3_2, spec_ver);
        specVersionMap.put(X3DConstants.FULL_PUBLIC_ID_3_2, spec_ver);

        allowedPublicIDs.add(X3DConstants.GENERAL_PUBLIC_ID_3_2);
        allowedPublicIDs.add(X3DConstants.INTERCHANGE_PUBLIC_ID_3_2);
        allowedPublicIDs.add(X3DConstants.CAD_INTERCHANGE_PUBLIC_ID_3_2);
        allowedPublicIDs.add(X3DConstants.INTERACTIVE_PUBLIC_ID_3_2);
        allowedPublicIDs.add(X3DConstants.IMMERSIVE_PUBLIC_ID_3_2);
        allowedPublicIDs.add(X3DConstants.FULL_PUBLIC_ID_3_2);
        allowedPublicIDs.add(X3DConstants.OLD_PUBLIC_ID);
        allowedPublicIDs.add(X3DConstants.TRANS_PUBLIC_ID);

        allowedSystemIDs.add(X3DConstants.GENERAL_SYSTEM_ID_3_2);

        systemSchemaIDs = new HashSet();
        systemSchemaIDs.add(X3DConstants.SCHEMA_ID_3_0);
        systemSchemaIDs.add(X3DConstants.SCHEMA_ID_3_1);
        systemSchemaIDs.add(X3DConstants.SCHEMA_ID_3_2);

        fieldAccessMap = new HashMap();
        fieldAccessMap.put("inputOnly", new Integer(FieldConstants.EVENTIN));
        fieldAccessMap.put("outputOnly", new Integer(FieldConstants.EVENTOUT));
        fieldAccessMap.put("inputOutput", new Integer(FieldConstants.EXPOSEDFIELD));
        fieldAccessMap.put("initializeOnly", new Integer(FieldConstants.FIELD));

        elementMap = new HashMap();
        elementMap.put("X3D", new Integer(X3D_TAG));
        elementMap.put("head", new Integer(HEAD_TAG));
        elementMap.put("component", new Integer(COMPONENT_TAG));
        elementMap.put("Scene", new Integer(SCENE_TAG));
        elementMap.put("ProtoDeclare", new Integer(PROTO_DECL_TAG));
        elementMap.put("ProtoInterface", new Integer(PROTO_INTERFACE_TAG));
        elementMap.put("ProtoBody", new Integer(PROTO_BODY_TAG));
        elementMap.put("ExternProtoDeclare", new Integer(EXTERNPROTO_DECL_TAG));
        elementMap.put("IS", new Integer(IS_TAG));
        elementMap.put("field", new Integer(FIELD_TAG));
        elementMap.put("meta", new Integer(META_TAG));
        elementMap.put("ProtoInstance", new Integer(PROTO_INSTANCE_TAG));
        elementMap.put("IMPORT", new Integer(IMPORT_TAG));
        elementMap.put("EXPORT", new Integer(EXPORT_TAG));
        elementMap.put("ROUTE", new Integer(ROUTE_TAG));
        elementMap.put("fieldValue", new Integer(FIELD_VALUE_TAG));
        elementMap.put("Script", new Integer(SCRIPT_TAG));
        elementMap.put("connect", new Integer(CONNECT_TAG));

        attributeMap = new HashMap();
//        attributeMap.put(NAME_ATTR, new Integer(NAME_ATTR_ID));
        attributeMap.put(DEF_ATTR, new Integer(DEF_ATTR_ID));
        attributeMap.put(USE_ATTR, new Integer(USE_ATTR_ID));
        attributeMap.put(CLASS_ATTR, new Integer(CLASS_ATTR_ID));
        attributeMap.put(CONTAINER_ATTR, new Integer(CONTAINER_ATTR_ID));
    }

    /**
     * Construct a default instance of this class.
     */
    public X3DSAVAdapter() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        scriptFlagStack = new BooleanStack();
        scriptUrlStack = new BooleanStack();
        depthCountStack = new IntStack();
        declDepthStack = new IntStack();
        characterDataBuffer = new StringBuffer();
        containerFields = new Properties();

        namespacePrefixes = new HashSet();

        scriptFlagStack.push(false);
        inScript = false;
        fieldDeclDepth = 0;
        overrideLex = false;
        checkForSceneTag = false;
        checkForX3DNamespace = false;
    }

    /**
     * Set the loading state.  Generally this is information that is gotten from
     * the lexicalHandler.  Seems XSLT transformation is keeping the lex handler to
     * itself.
     *
     * @param worldURL the worldURL
     * @param fullURL the full URL
     * @param overrideLex Enable if lexicalHandling not working.
     */
     public void setLoadState(String worldURL,
                              String fullURL,
                              boolean overrideLex) {

        this.worldURL = worldURL;
        this.fullURL = fullURL;
        this.overrideLex = overrideLex;
     }

    //----------------------------------------------------------
    // Methods defined by LexicalHandler
    //----------------------------------------------------------

    /**
     * Report an XML comment anywhere in the document.
     */
    public void comment(char[] ch, int start, int length) {
    }

    /**
     * Report the start of DTD declarations, if any. There are two
     * formal doctypes supported. The first type is the required type by the
     * X3D specification:
     *
     * <pre>
     *   &lt;!DOCTYPE X3D PUBLIC &quot;-//Web3D//DTD X3D 3.0//EN&quot;
     *    &quot;http://www.web3d.org/specification/x3d/x3d-3_0.dtd&quot;&gt;
     * </pre>
     *
     * The second type are the transitional DOCTYPEs used during development
     * of the X3D specification.
     *
     * <pre>
     *   &lt;!DOCTYPE X3D PUBLIC
     *    &quot;http://www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd&quot;
     *    &quot;/www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd&quot;&gt;
     * </pre>
     *
     * <pre>
     *   &lt;!DOCTYPE X3D PUBLIC
     *    &quot;http://www.web3d.org/specifications/x3d-3.0.dtd&quot;
     *    &quot;/www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd&quot;&gt;
     * </pre>

     * It is advised that these forms are never used, it is for transitional
     * content only. When this header is detected, a warning message is
     * generated on the output, but parsing continues normally.
     *
     * @param name The DTD name string
     * @param publicId The Public ID used for the content
     * @param systemId The system ID used for the content
     */
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException {

        if(!name.equals(X3DConstants.DTD_NAME))
            throw new SAXNotSupportedException(INVALID_DTD_NAME_MSG + name);

        boolean warned = false;

        if(allowedPublicIDs.contains(publicId)) {
            if(X3DConstants.OLD_PUBLIC_ID.equals(publicId)) {
                warned = true;
                errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
            } else if(X3DConstants.TRANS_PUBLIC_ID.equals(publicId)) {
                warned = true;
                errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
            }

            if(allowedSystemIDs.contains(systemId)) {
                if(X3DConstants.OLD_SYSTEM_ID.equals(systemId) && !warned)
                    errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
                else if(X3DConstants.TRANS_SYSTEM_ID.equals(systemId) && !warned)
                    errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
            } else
                throw new SAXException(INVALID_SYSTEM_ID_MSG);

            Float version = (Float)specVersionMap.get(publicId);
            versionString = (String)specStringMap.get(version);

        } else {
            errorReporter.warningReport(UNKNOWN_DTD_MSG, null);

            // Can't error out as a custom DTD could be used
            //
            // Spec language: A DOCTYPE or schema declaration is optional. The
            // reference DTD and location is specified in this document. To
            // allow for extensible tag sets (supersets of the base specification),
            // authors may point to a document definition other than the ones
            // listed in this specification. However, the alternate definition
            // shall specify an equivalent document to the base X3D DTD/Schema
            // (i.e., the tag instances shall look exactly the same regardless
            // of the source DTD/s chema).

            //throw new SAXException(INVALID_PUBLIC_ID_MSG);

            // No clue, will need to wait for X3D tag
            versionString = "V3.1";
        }

        // So everything is OK, now find the spec version and load the right
        // config file.
    }

    /**
     * Report the end of DTD declarations. Does nothing.
     */
    public void endDTD() {
    }

    /**
     * Report the start of a CDATA section. Script data for the URLs are
     * sent this way.
     */
    public void startCDATA() {
        if (overrideLex)
            return;

/*
        // This test is wrong, Metadata might have CDATA sections as well
        if(!inScript) {
            String msg = "CDATA section not in script!" +
                          saxLocator.getLineNumber();
            errorReporter.warningReport(msg, null);
            return;
        }
*/
/*
        characterDataBuffer.setLength(0);
        characterDataBuffer.append('\"');
*/
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA() {
        // Need to handle if other nodes besides scripts have CDATA
    }

    /**
     * Report the beginning of some internal and external XML entities.
     */
    public void startEntity(String name) {
    }

    /**
     * Report the end of an entity.
     */
    public void endEntity(String name) {
    }

    //----------------------------------------------------------
    // Methods defined by sax.ContentHandler
    //----------------------------------------------------------

    /**
     * Set the saxLocator used to report error information back to the user.
     * Setting a value of null will clear the current saxLocator.
     *
     * @param loc The saxLocator to use or null to clear
     */
    public void setDocumentLocator(Locator loc) {

        savLocator = new LocatorAdapter(loc);
        if(contentHandler != null)
            contentHandler.setDocumentLocator(savLocator);

        if(errorReporter instanceof ErrorHandler) {
            ErrorHandler handler = (ErrorHandler)errorReporter;
            handler.setDocumentLocator(savLocator);
        }

        saxLocator = loc;

        useIsCurrent = false;
    }

    /**
     * Start a new document to be parsed. If the instance is currently in the
     * middle of parsing a document, this will throw an exception. Does nothing
     * because the document start is assumed to be sent to the SAV content
     * handler after the DTD has been read.
     *
     * @throws SAXException The document is currently being parsed
     */
    public void startDocument() throws SAXException {
        // Default to the basic spec unless someone resets it later with the
        // appropriate doctype tag.
        versionString = "V3.1";
    }

    /**
     * End the current document that is being parsed. Cleans up any internal
     * structures currently in use but leaves the Document object about to
     * work with.
     *
     * @throws SAXException Not thrown in this implementation
     */
    public void endDocument() throws SAXException {
        // Need this here as the SAX code is self contained for document
        // handling.

        if(contentHandler != null && !overrideLex)
            contentHandler.endDocument();
    }

    /**
     * Start the prefix mapping for a particular element or attribute.
     * Not implemented yet
     *
     * @param prefix The prefix string used
     * @param uri The namespace URI the prefix is mapped to
     * @throws SAXException Not thrown in this implementation
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {

    }

    /**
     * End the mapping for the named prefix
     *
     * @param prefix The prefix to stop the URI mapping for
     * @throws SAXException Not thrown in this implementation
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * Start the processing of a new element with the given collection of
     * attribute information.
     *
     * @param namespace The namespace for the element. Null if not used
     * @param localName The local name of the element to create
     * @param qName The qualified name of the element including prefix
     * @param attribs The collection of attributes to use
     * @throws SAXException The element can't be found in the underlying
     *     factory
     */
    public void startElement(String namespace,
                             String localName,
                             String qName,
                             Attributes attribs)
        throws SAXException {

//System.out.println("SE: " + localName + " qName: " + qName);
/*
System.out.println("Attribs: " + qName);
int len = attribs.getLength();

for(int i=0; i < len; i++) {
   System.out.println(attribs.getQName(i) + " = " + attribs.getValue(i));
}
System.out.println();
*/
        if(useIsCurrent)
            throw new SAXException(USE_WITH_KIDS_MSG);

        int colon_idx = qName.indexOf(':');
        if(colon_idx != -1) {
            if(checkForX3DNamespace && qName.startsWith(x3dNamespaceId))
                qName = qName.substring(x3dNamespaceId.length());
            else {
                String ns = qName.substring(0, colon_idx);

                if(namespacePrefixes.contains(ns))
                    return;
            }
        }

        Integer el_type = (Integer)elementMap.get(qName);

        String value = null;

        if(el_type == null) {
            if(checkForSceneTag)
                throw new SAXException(NO_SCENE_TAG_MSG);

            // We're obviously in a new node, so go look for
            // the container field attribute to do a "startField"
            // call. However, no point doing anything if we don't have a
            // content handler to call. Need to weed out the one case were
            // we are the root node of a proto declaration body.

            if(fieldDeclDepth != 0) {
                String field_name = attribs.getValue(CONTAINER_ATTR);

                // No container field name defined? Look one up. If that
                // fails, guess and put in "children" since that is the
                // most commonly used default.
                if(field_name == null) {
                    field_name = containerFields.getProperty(qName);

                    if(field_name == null)
                        field_name = "children";
                }

                if(contentHandler != null) {
                    try {
                        contentHandler.startField(field_name);
                    } catch(InvalidFieldException ife) {
                       errorReporter.errorReport("No field: " + field_name +
                                                  " for: " + qName, ife);
                    }
                }
            }

            value = attribs.getValue(USE_ATTR);
            fieldDeclDepth++;

            if(value != null) {
                if(contentHandler != null)
                    contentHandler.useDecl(value);
                useIsCurrent = true;
            } else {
                value = attribs.getValue(DEF_ATTR);

                if(contentHandler != null)
                    contentHandler.startNode(qName, value);
            }

            // now process all the attributes!
            int num_attr = attribs.getLength();
            for(int i = 0; !useIsCurrent && i < num_attr; i++) {

                String name = attribs.getQName(i);

                // Is it an attribute prefixed by one of the namespaces?
                // if so, then ignore it and go to the next attribute
                colon_idx = name.indexOf(':');
                if(colon_idx != -1) {
                     String ns = name.substring(0, colon_idx);
                     if(namespacePrefixes.contains(ns))
                        continue;
                }

                // Check to see if the attribute is one of the reserved
                // set. If so, treat separately from the normal field
                // processing.
                //
                // If we don't have a string content handler, then do not
                // pass this information along. Ideally we want this to
                // automatically convert to the binary form if we have a binary
                // content handler, but that's too much work for me right now
                // trying to get this darn project finished.... JC
                //
                if(contentHandler instanceof StringContentHandler) {
                    StringContentHandler sch =
                        (StringContentHandler)contentHandler;

                    Integer id_int = (Integer)attributeMap.get(name);
                    if(id_int == null && contentHandler != null) {
                        String field_value = attribs.getValue(i);

                        // zero length string? Could well be an eventOut or eventIn,
                        // so ignore it.
                        if(field_value.length() == 0)
                            continue;

                        sch.startField(name);
                        sch.fieldValue(field_value);
                    }
                }
            }

            return;
        }

        switch(el_type.intValue()) {
            case X3D_TAG:
                value = attribs.getValue(VERSION_ATTR);

                if (value != null)
                    versionString = "V" + value;
                else
                    value = versionString.substring(1);

                loadContainerProperties(Float.parseFloat(value));

                if(contentHandler != null) {
                    contentHandler.startDocument(fullURL,
                                                 worldURL,
                                                 XML_ENCODING,
                                                 "#X3D",
                                                 versionString,
                                                 null);
                }

                value = attribs.getValue("profile");

                if(value == null)
                    throw new SAXException(NO_PROFILE_MSG);

                if(contentHandler != null)
                    contentHandler.profileDecl(value);

                // Setup cData
                if(overrideLex)
                    characterDataBuffer.append("\"");

                useIsCurrent = false;
                checkForSceneTag = true;

                // Now go looking for anything that starts with a xmlns:
                int num_attribs = attribs.getLength();
                for(int i = 0; i < num_attribs; i++) {
                    String attrib = attribs.getQName(i);
                    if(attrib.startsWith("xmlns:")) {
                        String space = attrib.substring(6);
                        // Ignore the Schema definition
                        if(!space.equals("xsd")) {
                            namespacePrefixes.add(space);

                            String ns_uri = attribs.getValue(i);

                            // TODO:
                            // This fixed URI right now is subject to
                            // specification. We have a single fixed URI, but
                            // this may be changed by the X3D spec process to
                            // be something that is relative to the spec version
                            // of the containing document (eg the DTD).
                            if(ns_uri.equals(X3DConstants.X3D_NAMESPACE_URI)) {
                                x3dNamespaceId = space;
                                checkForX3DNamespace = true;
                            }

                        }
                    } else if(attrib.equals("xsd:noNamespaceSchemaLocation")) {
                        // If there is a pre-defined non-namespace schema, and
                        // it is not the X3D schema, then we want to automatically
                        // turn on namespace checks for all elements.
                        String ns_val = attribs.getValue(i);
                        checkForX3DNamespace = !systemSchemaIDs.contains(ns_val);
                    }
                }

                break;

            case HEAD_TAG:
                // do nothing
                break;

            case COMPONENT_TAG:
                value = attribs.getValue(NAME_ATTR) + ':' +
                        attribs.getValue("level");
                if(contentHandler != null)
                    contentHandler.componentDecl(value);
                break;

            case SCENE_TAG:
                checkForSceneTag = false;
                break;

            case PROTO_DECL_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(protoHandler != null)
                    protoHandler.startProtoDecl(attribs.getValue(NAME_ATTR));

                scriptFlagStack.push(false);
                inScript = false;
                break;

            case PROTO_INTERFACE_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                // Don't do anything from here. We've already started the
                // proto decl handling in the PROTO_DECL_TAG.
                break;

            case PROTO_BODY_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(protoHandler != null) {
                    protoHandler.endProtoDecl();
                    protoHandler.startProtoBody();
                }

                break;

            case EXTERNPROTO_DECL_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(protoHandler != null) {
                    value = attribs.getValue(NAME_ATTR);
                    protoHandler.startExternProtoDecl(value);

                    epUrl = attribs.getValue("url");

                }

                scriptFlagStack.push(false);
                inScript = false;
                break;

            case IS_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                break;

            case CONNECT_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(contentHandler != null)
                    contentHandler.startField(attribs.getValue("nodeField"));

                if(protoHandler != null)
                    protoHandler.protoIsDecl(attribs.getValue("protoField"));
                break;

            case FIELD_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                int access_type =
                    processFieldAccess(attribs.getValue("accessType"), attribs.getValue(NAME_ATTR));

                // perhaps this should do some prior checking of the
                // access type to make sure that the user don't do anything
                // dumb like set a value for eventIn/Out.
                boolean is_used = false;
                depthCountStack.push(fieldDeclDepth);
                fieldDeclDepth = 0;

                if((value = attribs.getValue("USE")) != null)
                    is_used = true;
                else
                    value = attribs.getValue(VALUE_ATTR);

                if(inScript) {
                    if(is_used) {
                        if(scriptHandler != null) {
                            scriptHandler.scriptFieldDecl(access_type,
                                                          attribs.getValue("type"),
                                                          attribs.getValue(NAME_ATTR),
                                                          null);
                        }

                        if(contentHandler != null)
                            contentHandler.useDecl(value);
                    } else {
                        if (value != null && value.length() == 0) {
                            value = null;
                        }

                        if(scriptHandler != null)
                            scriptHandler.scriptFieldDecl(access_type,
                                                          attribs.getValue("type"),
                                                          attribs.getValue(NAME_ATTR),
                                                          value);
                    }
                } else {
                    if(is_used) {
                        if(protoHandler != null) {
                            protoHandler.protoFieldDecl(access_type,
                                                        attribs.getValue("type"),
                                                        attribs.getValue(NAME_ATTR),
                                                        null);
                        }

                        if(contentHandler != null)
                            contentHandler.useDecl(value);
                    } else {
                        if (value != null && value.length() == 0) {
                            value = null;
                        }

                        if(protoHandler != null)
                            protoHandler.protoFieldDecl(access_type,
                                                        attribs.getValue("type"),
                                                        attribs.getValue(NAME_ATTR),
                                                        value);
                    }
                }
                break;

            case META_TAG:
                if(contentHandler != null) {
                    contentHandler.metaDecl(attribs.getValue(NAME_ATTR),
                                            attribs.getValue("content"));
                }
                break;

            case PROTO_INSTANCE_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);


                if(contentHandler != null) {
                    String field_name = attribs.getValue(CONTAINER_ATTR);

                    // No container field name defined? Look one up. If that
                    // fails, guess and put in "children" since that is the
                    // most commonly used default.
                    if(field_name == null) {
                        field_name = containerFields.getProperty(localName);

                        if(field_name == null)
                            field_name = "children";
                    }

                    if(fieldDeclDepth != 0) {
                        contentHandler.startField(field_name);
                    }

                    fieldDeclDepth++;
                    contentHandler.startNode(attribs.getValue(NAME_ATTR),
                                             attribs.getValue(DEF_ATTR));
                }

/*
                // All of the above was commented out, restored for now?
                if (contentHandler != null)
                    contentHandler.startNode(attribs.getValue(NAME_ATTR),
                                             attribs.getValue(DEF_ATTR));
*/
                inScript = false;
                break;

            case IMPORT_TAG:
                if(contentHandler != null)
                    contentHandler.importDecl(attribs.getValue("inlineDEF"),
                                              attribs.getValue("exportedDEF"),
                                              attribs.getValue(AS_ATTR));
                break;

            case EXPORT_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(contentHandler != null)
                    contentHandler.exportDecl(attribs.getValue("localDEF"),
                                              attribs.getValue(AS_ATTR));
                break;

            case ROUTE_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(routeHandler != null)
                    routeHandler.routeDecl(attribs.getValue("fromNode"),
                                           attribs.getValue("fromField"),
                                           attribs.getValue("toNode"),
                                           attribs.getValue("toField"));
                break;

            case SCRIPT_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                // Force an automatic startScript in case parser doesn't
                startScript();

                scriptFlagStack.push(true);
                inScript = true;
                is_used = false;

                // Clear any CData garbage
                characterDataBuffer.setLength(0);
                characterDataBuffer.append('\"');

                String field_name = attribs.getValue(CONTAINER_ATTR);

                // No container field name defined? Look one up. If that
                // fails, guess and put in "children" since that is the
                // most commonly used default.
                if(field_name == null) {
                    field_name = containerFields.getProperty(localName);

                    if(field_name == null)
                        field_name = "children";
                }

                if(fieldDeclDepth != 0) {
                    if(contentHandler != null)
                        contentHandler.startField(field_name);
                }

                fieldDeclDepth++;
                value = attribs.getValue(USE_ATTR);

                if(value != null) {
                    if(contentHandler != null)
                        contentHandler.useDecl(value);
                    is_used = true;
                    useIsCurrent = true;
                } else {
                    value = attribs.getValue(DEF_ATTR);
                    is_used = false;
                    if(contentHandler != null)
                        contentHandler.startNode(qName, value);
                }

                if(scriptHandler != null && !is_used)
                    scriptHandler.startScriptDecl();

                // The definite fields of a script need to be passed through.
                // Only pass through if you're not in a USE though.
                if(!is_used && contentHandler != null) {
                    value = attribs.getValue("url");
                    if(value != null) {
                        scriptUrlStack.push(true);
                        if(contentHandler instanceof StringContentHandler) {
                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.startField("url");
                            sch.fieldValue(value);
                        }
                    } else {
                        scriptUrlStack.push(false);
                    }

                    value = attribs.getValue("mustEvaluate");
                    if(value != null) {
                        if(contentHandler instanceof StringContentHandler) {
                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.startField("mustEvaluate");
                            sch.fieldValue(value);
                        }
                    }

                    value = attribs.getValue("directOutput");
                    if(value != null) {
                        if(contentHandler instanceof StringContentHandler) {
                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.startField("directOutput");
                            sch.fieldValue(value);
                        }
                    }
                }

                break;

            case FIELD_VALUE_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(contentHandler != null) {
                    contentHandler.startField(attribs.getValue(NAME_ATTR));

                    value = attribs.getValue(USE_ATTR);
                    if(value != null) {
                        contentHandler.useDecl(value);
                    } else {
                        value = attribs.getValue(VALUE_ATTR);
                        if((value != null && value.length() > 0) &&
                           (contentHandler instanceof StringContentHandler)) {

                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.fieldValue(value);
                        }
                    }
                }

                declDepthStack.push(fieldDeclDepth);
                fieldDeclDepth = 0;
                break;

            default:
                errorReporter.errorReport(UNKNOWN_ELEMENT_MSG + qName,
                                          null);
        }
    }

    /**
     * End the element processing.
     *
     * @param namespace The namespace for the element. Null if not used
     * @param name The local name of the element to create
     * @param qName The qualified name of the element including prefix
     * @throws SAXException Not thrown in this implementation
     */
    public void endElement(String namespace, String name, String qName)
        throws SAXException {

        int colon_idx = qName.indexOf(':');
        if(colon_idx != -1) {
            if(checkForX3DNamespace && qName.startsWith(x3dNamespaceId))
                qName = qName.substring(x3dNamespaceId.length());
            else {
                String ns = qName.substring(0, colon_idx);

                if(namespacePrefixes.contains(ns))
                    return;
            }
        }

        Integer el_type = (Integer)elementMap.get(qName);

        String value;

        // Handle the case where we don't receive start/endCData events

        if(overrideLex && characterDataBuffer.length() > 1) {
            if(inScript && (contentHandler != null)) {
                characterDataBuffer.append('\"');

                // TODO: Cover up inScript bug?  Only set URL's if they have a value
                // Sometimes we are trying to set URL on something other then a script
                String url = characterDataBuffer.toString();

                int len = url.length();
                url = url.substring(1,len-2);
                url = url.trim();

                if (url.length() > 1) {
                    url = "\"" + url + "\"";
                    if(contentHandler instanceof StringContentHandler) {
                        StringContentHandler sch =
                            (StringContentHandler)contentHandler;
                        sch.startField("url");
                        sch.fieldValue(characterDataBuffer.toString());
                    }
                }

                characterDataBuffer.setLength(0);
                characterDataBuffer.append('\"');
            }
        }

        if(el_type == null) {
            fieldDeclDepth--;

            if(contentHandler != null) {
                if(useIsCurrent) {
                    contentHandler.endField();
                    useIsCurrent = false;
                } else {
                    contentHandler.endNode();
                }
            }
            return;
        }

        switch(el_type.intValue()) {

            case PROTO_DECL_TAG:
                inScript = scriptFlagStack.pop();
                break;

            case PROTO_INTERFACE_TAG:
                break;

            case PROTO_BODY_TAG:
                if(protoHandler != null)
                    protoHandler.endProtoBody();
                break;

            case EXTERNPROTO_DECL_TAG:
                if(protoHandler != null) {
                    // Make sure externProtoURI call is after proto decl is done

                    protoHandler.endExternProtoDecl();

                    // This need to be parsed.  Do we already have a MFString parsing service available?
                    // TODO; Generates garbage, might change to String.split after 1.4 is required

                    StringTokenizer st = new StringTokenizer(epUrl, "\"");
                    ArrayList list = new ArrayList();

                    while(st.hasMoreTokens()) {
                        list.add(st.nextToken());
                    }

                    int len = list.size();
                    String values[] = new String[len];

                    for(int i=0; i < len; i++) {
                        values[i] = (String) list.get(i);
                    }

                    protoHandler.externProtoURI(values);
                }

                inScript = scriptFlagStack.pop();
                break;

            case IS_TAG:
                break;

            case CONNECT_TAG:
                break;

            case FIELD_TAG:
                fieldDeclDepth = depthCountStack.pop();;
                break;

            case PROTO_INSTANCE_TAG:

                if(contentHandler != null)
                    contentHandler.endNode();

                // TODO: This seems dodgy
                inScript = true;
                fieldDeclDepth--;

                break;

            case SCRIPT_TAG:

                if (!useIsCurrent) {
                    boolean urlValue = scriptUrlStack.pop();

                    // Ignore CDATA if url provided
                    if (!urlValue)
                        endScript();

                    if(scriptHandler != null)
                        scriptHandler.endScriptDecl();
                }
                inScript = scriptFlagStack.pop();

                fieldDeclDepth--;

                if(contentHandler != null) {
                    if(useIsCurrent) {
                        contentHandler.endField();

                        useIsCurrent = false;
                    } else {
                        contentHandler.endNode();
                    }
                }
                break;

            case X3D_TAG:
                if(overrideLex) {
                    if(contentHandler != null)
                        contentHandler.endDocument();
                }
                break;

            case HEAD_TAG:
                checkForSceneTag = true;
                break;

            // do nothing with these elements.
            case COMPONENT_TAG:
            case SCENE_TAG:
            case META_TAG:
            case IMPORT_TAG:
            case EXPORT_TAG:
            case ROUTE_TAG:
                break;

            case FIELD_VALUE_TAG:
                fieldDeclDepth = declDepthStack.pop();
                break;

            default:
                errorReporter.errorReport(UNKNOWN_ELEMENT_MSG + qName,
                                          null);
        }
    }

    /**
     * Notification of character data to be added to the document. If we are
     * currently parsing a CDATA section then we just append the string to the
     * current string.
     *
     * @param ch The characters to add as a comment
     * @param start The start position in the character array
     * @param length The number of characters to use as a comment
     * @throws SAXException The node containing the characters in is invalid
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {

        characterDataBuffer.append(ch, start, length);
    }

    /**
     * Notification of ignorable whitespace. If we are set to ignore whitespace
     * then this method does nothing. If we do not ignore it then we add it to
     * the current CDATA section if we are processing a CDATA, else append it
     * as data to the current top node
     *
     * @param ch The characters to add as a comment
     * @param start The start position in the character array
     * @param length The number of characters to use as a comment
     * @throws SAXException The node containing the characters in is invalid
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
    }

    /**
     * Notification that a processing instruction has been added. Ignored.
     *
     * @param target The target instruction name
     * @param data Data associated with the instruction
     * @throws SAXException The instruction is invalid
     */
    public void processingInstruction(String target, String data)
        throws SAXException {
    }

    /**
     * Notification of an entity that was skipped during the processing.
     * Not implemented yet.
     *
     * @param name The name of the entity that was skipped
     * @throws SAXException Not thrown in this implementation
     */
    public void skippedEntity(String name) throws SAXException {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the currently set {@link ContentHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set node handler.
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Set the node handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ch The content handler instance to use
     */
    public void setContentHandler(ContentHandler ch) {
        contentHandler = ch;

        if(savLocator != null)
            contentHandler.setDocumentLocator(savLocator);
    }

    /**
     * Get the currently set {@link ScriptHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set script handler.
     */
    public ScriptHandler getScriptHandler() {
        return scriptHandler;
    }

    /**
     * Set the script handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param sh The script handler instance to use
     */
    public void setScriptHandler(ScriptHandler sh) {
        scriptHandler = sh;
    }

    /**
     * Get the currently set {@link ProtoHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set proto handler.
     */
    public ProtoHandler getProtoHandler() {
        return protoHandler;
    }

    /**
     * Set the proto handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ph The proto handler instance to use
     */
    public void setProtoHandler(ProtoHandler ph) {
        protoHandler = ph;
    }

    /**
     * Get the currently set {@link RouteHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set route handler.
     */
    public RouteHandler getRouteHandler() {
        return routeHandler;
    }

    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param rh The route handler instance to use
     */
    public void setRouteHandler(RouteHandler rh) {
        routeHandler = rh;
    }

    /**
     * Turn the field access type string into one of the standard contents
     *
     * @param type A string representing the access type
     * @param fieldName The name of the field to process
     * @return The access type as an int from FieldConstants constants
     */
    protected int processFieldAccess(String type, String fieldName)
        throws SAXException {

        Integer field_int = (Integer)fieldAccessMap.get(type);

        if(field_int == null)
            throw new SAXException(UNKNOWN_ACCESS_TYPE_MSG + type + " for field: " + fieldName);

        return field_int.intValue();
    }

    /**
     * Load the properties files for the given spec version.
     *
     * @param specVersion The spec version in floating point format
     */
    protected void loadContainerProperties(final float specVersion) {
        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    try {
                        ClassLoader cl = ClassLoader.getSystemClassLoader();
                        InputStream is =
                            cl.getSystemResourceAsStream("config/" +
                                                         specVersion +
                                                         "/" +
                                                         CONTAINER_PROPS_FILE);
                        // WebStart fallback
                        if (is==null)
                            is = X3DSAVAdapter.class.getClassLoader().getResourceAsStream("config/" +
                                                         specVersion +
                                                         "/" +
                                                         CONTAINER_PROPS_FILE);


                        if(is != null)
                            containerFields.load(is);
                        else
                            errorReporter.warningReport(NO_CONTAINER_PROPS_MSG,
                                                        null);

                    } catch(IOException ioe) {
                        errorReporter.errorReport(CONTAINER_PROPS_READ_ERR,
                                                  ioe);
                    }
                    return null;
                }
            }
        );
    }

    /**
     * Start of a script declaration.
     */
    protected void startScript() {
        characterDataBuffer.setLength(0);
        characterDataBuffer.append('\"');
    }

    /**
     * End a script.  Sends url data from the CDATA block.
     */
    protected void endScript() {
        characterDataBuffer.append('\"');

        if(contentHandler instanceof StringContentHandler) {
            StringContentHandler sch =
                (StringContentHandler)contentHandler;
            sch.startField("url");
            sch.fieldValue(characterDataBuffer.toString());
        }

        characterDataBuffer.setLength(0);
        characterDataBuffer.append('\"');
    }
}
