/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.parser.x3d;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.io.*;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import com.sun.xml.fastinfoset.sax.AttributesHolder;
import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;
import org.jvnet.fastinfoset.sax.PrimitiveTypeContentHandler;
import org.jvnet.fastinfoset.sax.EncodingAlgorithmContentHandler;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.sav.*;

import org.web3d.util.*;

import org.web3d.x3d.jaxp.X3DSAVAdapter;
import org.web3d.vrml.export.compressors.NodeCompressor;
//import org.web3d.vrml.export.compressors.TestCompressor;

/**
 * Handles the reading of elements from a FastInfoSet stream and converts
 * it to an X3D world.
 *
 * This expects attributes of type TypeAttributes instead of just Attributes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
class FastInfosetElementReader extends X3DSAVAdapter
    implements PrimitiveTypeContentHandler, EncodingAlgorithmContentHandler {

    protected static final int BYTE_ALGORITHM_ID = 32;
    protected static final int DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID = 33;
    protected static final int QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID = 34;

    /** Message for a bad field access type */
    private static final String UNKNOWN_ACCESS_TYPE_MSG =
        "The field has declared an invalid access type: ";


    private BooleanStack inCompressorStack;

    private BooleanStack skipEEStack;

    private NodeCompressor currentCompressor;

    private String lastNodeName;

    private boolean skipEndElement;

    /** After the payload any metadata is ignored */
    private boolean pastPayload;

    /** Switch between methods, should go away */
    private boolean compressedAttWay = false;


    FastInfosetElementReader(String worldURL) {

        inCompressorStack = new BooleanStack();
        inCompressorStack.push(false);

        skipEEStack = new BooleanStack();
    }

    /**
     * Start the processing of a new element with the given collection of
     * attribute information.
     *
     * @param namespace The namespace for the element. Null if not used
     * @param name The local name of the element to create
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

        boolean inCompressor = inCompressorStack.peek();

        if(useIsCurrent)
            throw new SAXException(USE_WITH_KIDS_MSG);

        // Remove X3D: namespace prefix.  Need to handle generically
        if(qName.startsWith("X3D:"))
            qName = qName.substring(4);

        Integer el_type = (Integer)elementMap.get(qName);

        String value = null;
        AttributesHolder atts = (AttributesHolder) attribs;
        BinaryContentHandler bch = (BinaryContentHandler) contentHandler;

        // TODO: Would a HashMap be faster here?
        if (qName.equals("MetadataSet")) {
            String name = attribs.getValue("name");

            if (name.equals(".x3db")) {
                // Pop Parents inCompressor and replace with true
                inCompressorStack.pop();
                inCompressorStack.push(true);
                inCompressor = true;

                inCompressorStack.push(inCompressor);
                skipEEStack.push(true);
                pastPayload = false;
                return;
            } else if (pastPayload) {
                pastPayload = false;

                inCompressorStack.push(inCompressor);
                skipEEStack.push(true);

                return;
            }
        }

        if (inCompressor) {
            String name = attribs.getValue("name");

            if (name != null) {
                if (name.equals("encoding")) {
//                    currentCompressor = new TestCompressor();

                    inCompressorStack.push(inCompressor);
                    skipEEStack.push(true);
                    return;
                } else if (name.equals("payload")) {
                    int idx = attribs.getIndex("value");
                    int algo = atts.getAlgorithmIndex(idx);

                    if (algo == EncodingAlgorithmIndexes.INT) {
                        int[] ival = (int[]) atts.getAlgorithmData(idx);
                        currentCompressor.decompress(ival);
                        currentCompressor.fillData(lastNodeName, bch);
                    } else if (algo == DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID) {
                        int[] i4val = (int[]) atts.getAlgorithmData(idx);
                        currentCompressor.decompress(i4val);
                        currentCompressor.fillData(lastNodeName, bch);
                    } else {
                        errorReporter.errorReport("Data in wrong format! " +
                                                    atts.getAlgorithmIndex(idx),
                                                    null);
                    }

                    pastPayload = true;
                    inCompressorStack.push(inCompressor);
                    skipEEStack.push(true);
                    return;
                }
            }
        } else {
            lastNodeName = qName;
        }

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
                                                 " for: " + qName,
                                                 null);
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

            Object type;
            String att_name;
            Integer id_int;

/*
            if (inCompressor) {
                currentCompressor.fillData(qName, bch);
            }
*/
            // now process all the attributes!
            int num_attr = attribs.getLength();
            for(int i = 0; !useIsCurrent && i < num_attr; i++) {
                att_name = atts.getLocalName(i);

                if (att_name.equals("encoder")) {
                    // TODO: make hashmap
                    if (atts.getValue(i).equals("1")) {
                        inCompressor = true;

//                        currentCompressor = new TestCompressor();
                    }
                    continue;
                } else if (att_name.equals("data")) {
                    type = atts.getAlgorithmData(i);

                    if (atts.getAlgorithmIndex(i) == EncodingAlgorithmIndexes.INT) {
                            // TODO: Assume all INT's are arrays
                        int[] ival = (int[]) atts.getAlgorithmData(i);
                        currentCompressor.decompress(ival);
                        currentCompressor.fillData(qName, bch);

                    } else {
                        errorReporter.errorReport("Data in wrong format!", null);
                    }

                    continue;
                }

                // Check to see if the attribute is one of the reserved
                // set. If so, treat separately from the normal field
                // processing.
                id_int = (Integer)attributeMap.get(att_name);

                if (id_int != null) {
                    continue;
                }

                decodeField(atts,i,bch,att_name);
            }

            inCompressorStack.push(inCompressor);
            skipEEStack.push(false);

            return;
        }

        switch(el_type.intValue()) {
            case X3D_TAG:
                loadContainerProperties(Float.parseFloat(versionString.substring(1)));

                if(contentHandler != null) {
                    contentHandler.startDocument(null,  // TODO: Not sure how to get filename
                                                 worldURL,
                                                 XML_ENCODING,
                                                 "#X3D",
                                                 "V3.0",
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
                                            attribs.getValue("type"));
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

                // Force an automatic startCDATA in parser doesn't
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
                        bch.startField("url");
                        bch.fieldValue(value);
                    } else {
                        scriptUrlStack.push(false);
                    }

                    value = attribs.getValue("mustEvaluate");
                    if(value != null) {
                        bch.startField("mustEvaluate");
                        bch.fieldValue(value);
                    }

                    value = attribs.getValue("directOutput");
                    if(value != null) {
                        bch.startField("directOutput");
                        bch.fieldValue(value);
                    }
                }

                break;

            case FIELD_VALUE_TAG:
                if(checkForSceneTag)
                    throw new SAXException(NO_SCENE_TAG_MSG);

                if(contentHandler != null) {

                    String DEFName = attribs.getValue(USE_ATTR);
                    if (DEFName != null) {
                        contentHandler.startField(attribs.getValue(NAME_ATTR));
                        contentHandler.useDecl(DEFName);
                    } else {
                        String att_name;

                        int val_attr = attribs.getIndex(VALUE_ATTR);

                        decodeField(atts,val_attr,bch,attribs.getValue(NAME_ATTR));
                    }
                }

                declDepthStack.push(fieldDeclDepth);
                fieldDeclDepth = 0;

                break;

            default:
                errorReporter.errorReport("Unknown start element type " + qName, null);
        }

        inCompressorStack.push(inCompressor);
        skipEEStack.push(false);
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

        boolean inCompressor = inCompressorStack.pop();
        boolean skipEndElement = skipEEStack.pop();

        if (inCompressor && currentCompressor != null) {
            BinaryContentHandler bch = (BinaryContentHandler) contentHandler;

            currentCompressor.fillData(qName, bch);
        }

        if (!skipEndElement)
            super.endElement(namespace, name, qName);
    }

    // Methods implementing PrimitiveTypeContentHandler

    /**
     * Receive notification of character data as an array of boolean.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "boolean" encoding
     * algorithm, see subclause 10.7<p>.
     *
     * @param b the array of boolean
     * @param start the start position in the array
     * @param length the number of boolean to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void booleans(boolean [] b, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an array of byte.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "base64" encoding
     * algorithm, see subclause 10.3.
     *
     * <p>Such a notification may occur for binary data that would
     * normally require base 64 encoding and reported as character data
     * using the {@link org.xml.sax.ContentHandler#characters characters}
     * method <p>.
     *
     * @param b the array of byte
     * @param start the start position in the array
     * @param length the number of byte to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void bytes(byte[] b, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an array of short.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "short" encoding
     * algorithm, see subclause 10.4<p>.
     *
     * @param s the array of short
     * @param start the start position in the array
     * @param length the number of short to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void shorts(short[] s, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an array of int.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "int" encoding
     * algorithm, see subclause 10.5<p>.
     *
     * @param i the array of int
     * @param start the start position in the array
     * @param length the number of int to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void ints(int [] i, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an array of long.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "long" encoding
     * algorithm, see subclause 10.6<p>.
     *
     * @param l the array of long
     * @param start the start position in the array
     * @param length the number of long to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void longs(long [] l, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an array of float.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "float" encoding
     * algorithm, see subclause 10.8<p>.
     *
     * @param f the array of float
     * @param start the start position in the array
     * @param length the number of float to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void floats(float [] f, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an array of double.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "double" encoding
     * algorithm, see subclause 10.9<p>.
     *
     * @param d the array of double
     * @param start the start position in the array
     * @param length the number of double to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void doubles(double [] d, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an two array of UUID.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "uuid" encoding
     * algorithm, see subclause 10.10<p>.
     *
     * @param msblsb the array of long containing pairs of most signficant
     * bits and least significant bits of the UUIDs
     * @param start the start position in the array
     * @param length the number of long to read from the array. This will
     * be twice the number of UUIDs, which are pairs of two long values
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void uuids(long[] msblsb, int start, int length) throws SAXException {
    }

    /**
     * Receive notification of character data as an two array of UUID.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing data encoded using the "uuid" encoding
     * algorithm, see subclause 10.10<p>.
     *
     * @param msb the array of long of the most sigificant bits of
     * the UUIDs
     * @param lsb the array of long of the least sigificant bits of
     * the UUIDs
     * @param start the start position in the array
     * @param length the number of UUID to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     */
    public void uuids(long[] msb, long[] lsb, int start, int length) throws SAXException {
    }


    // Methods from EncodingAlgorithmContentHandler

    /**
     * Receive notification of encoding algorithm data as an array
     * of byte.
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing encoding algorithm data.<p>
     *
     * <p>The Parser will call the method of this interface to report each
     * encoding algorithm data. Parsers MUST return all contiguous
     * characters in a single chunk</p>
     *
     * <p>Parsers may return all contiguous bytes in a single chunk, or
     * they may split it into several chunks providing that the length of
     * each chunk is of the required length to successfully apply the
     * encoding algorithm to the chunk.</p>
     *
     * @param URI the URI of the encoding algorithm
     * @param algorithm the encoding algorithm index
     * @param b the array of byte
     * @param start the start position in the array
     * @param length the number of byte to read from the array
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     * @see org.jvnet.fastinfoset.EncodingAlgorithmIndexes
     */
    public void octets(String URI, int algorithm, byte[] b, int start, int length)  throws SAXException {
    }

    /**
     * Receive notification of encoding algorithm data as an object.
     *
     * <p>Such notifications will occur for a Fast Infoset SAX parser
     * when processing encoding algorithm data that is converted from an
     * array of byte to an object more suitable for processing.<p>
     *
     * @param URI the URI of the encoding algorithm
     * @param algorithm the encoding algorithm index
     * @param o the encoding algorithm object
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *            wrapping another exception
     * @see org.jvnet.fastinfoset.EncodingAlgorithmIndexes
     */
    public void object(String URI, int algorithm, Object o)  throws SAXException {
    }

    /**
     * Decode a field
     */
    private void decodeField(AttributesHolder atts,
                             int i,
                             BinaryContentHandler bch,
                             String att_name) {
        if (i < 0) {
            bch.startField(att_name);
            return;
        }

        Object type = atts.getAlgorithmData(i);

        if (type == null) {
            // No typed data, handle as a string

            bch.startField(att_name);

            bch.fieldValue(atts.getValue(i));
            bch.endField();
        } else {
            switch(atts.getAlgorithmIndex(i)) {
/*
                // Still not sure how to tell single from double
                case EncodingAlgorithmIndexes.BOOLEAN:
                    boolean[] fval = (boolean[]) atts.getAlgorithmData(i);

                    bch.startField(att_name);
                    bch.fieldValue(fval, fval.length);
                    bch.endField();
                    break;
*/
                case EncodingAlgorithmIndexes.FLOAT:
                    // TODO: Assume all FLOAT's are arrays
                    float[] fval = (float[]) atts.getAlgorithmData(i);
                    bch.startField(att_name);
                    bch.fieldValue(fval, fval.length);
                    break;

                case EncodingAlgorithmIndexes.INT:
                    // TODO: Assume all INT's are arrays
                    int[] ival = (int[]) atts.getAlgorithmData(i);

                    bch.startField(att_name);
                    bch.fieldValue(ival, ival.length);
                    break;

                case EncodingAlgorithmIndexes.SHORT:
                    // TODO: Assume all INT's are arrays
                    short[] sval = (short[]) atts.getAlgorithmData(i);
                    int alen = sval.length;
                    int[] i2val = new int[alen];

                    for(int j=0; j < alen; j++) {
                        i2val[j] = sval[j];
                    }
                    bch.startField(att_name);
                    bch.fieldValue(i2val, i2val.length);
                    break;

                case BYTE_ALGORITHM_ID:
                    // TODO: stop hardcoding number for BYTE, reference from tables
                    byte[] bval = (byte[]) atts.getAlgorithmData(i);
                    int blen = bval.length;
                    int[] i3val = new int[blen];

                    for(int j=0; j < blen; j++) {
                        i3val[j] = bval[j];
                    }

                    bch.startField(att_name);
                    bch.fieldValue(i3val, i3val.length);
                    break;

                case DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID:
                    int[] i4val = (int[]) atts.getAlgorithmData(i);
                    bch.startField(att_name);
                    bch.fieldValue(i4val, i4val.length);
                    break;

                case QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID:
                    float[] f2val = (float[]) atts.getAlgorithmData(i);
                    bch.startField(att_name);
                    bch.fieldValue(f2val, f2val.length);
                    break;

                default:
                    errorReporter.warningReport("Unhandled algorithm: " +
                                                atts.getAlgorithmIndex(i),
                                                null);
            }
        }
    }
}
