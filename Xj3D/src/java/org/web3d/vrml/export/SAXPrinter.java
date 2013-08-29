/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.fastinfoset.sax.AttributesHolder;
import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;

import org.web3d.x3d.jaxp.X3DConstants;
import org.web3d.vrml.lang.UnsupportedSpecVersionException;

/**
 * Converts a SAX stream of events to an X3D textual representation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class SAXPrinter extends DefaultHandler {
    // TODO: Commonize these constants
    protected static final int BYTE_ALGORITHM_ID = 32;
    protected static final int DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID = 33;
    protected static final int QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID = 34;

    /** The stream to write to */
    private Writer out;

    /** A temporary buffer for character data */
    private StringBuffer textBuffer;

    /** The major version of the spec this file belongs to. */
    protected int majorVersion;

    /** The minor version of the spec this file belongs to. */
    protected int minorVersion;

    /** Should we print the DOC type */
    private boolean printDocType;

    public SAXPrinter(Writer out, int majorVersion, int minorVersion,
        boolean printDocType) {

        this.out = out;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.printDocType = printDocType;
    }

    //----------------------------------------------------------
    // SAXDocumentHandler methods
    //----------------------------------------------------------
    /**
     * Handles startDocument event.
     *
     */
    public void startDocument() throws SAXException {
        print("<?xml version='1.0' encoding='UTF-8'?>");
        printNewLine();

        String publicId = getPublicId(majorVersion,minorVersion);
        String systemId = getSystemId(majorVersion,minorVersion);

        if (printDocType) {
            print("<!DOCTYPE X3D PUBLIC \"");
            print(publicId);
            print("\" \"");
            print(systemId);
            print("\">\n");
        }

    }

    /**
     * Handles endDocument event
     */
    public void endDocument() throws SAXException {
        try {
            printNewLine();
            out.flush();
        } catch (IOException e) {
            throw new SAXException("SAX error", e);
        }
    }

    /**
     * Handles characters event.
     *
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters .
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        String s = new String(ch, start, length);

        if (textBuffer == null) {
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }
    }

    /**
     * Handles startElement event.
     *
     * @param namespaceURI The namespace URI.
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name, or the empty string if qualified names are not available.
     * @param attributes The specified or defaulted attributes.
     */
    public void startElement(String namespaceURI, String localName,
        String qName, Attributes attributes) throws SAXException {

        flushText();

        String name = null;

        if (localName.equals("")) {
            name = qName;
        } else {
            name = localName;
        }

        print("<" + name);

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String aName = attributes.getLocalName(i);

                if ("".equals(aName)) {
                    aName = attributes.getQName(i);
                }

                String value = attributes.getValue(i);


                if (value == null) {
                    AttributesHolder atts = (AttributesHolder) attributes;
                    Object type = atts.getAlgorithmData(i);
                    String strValue = "unknown";

                    switch(atts.getAlgorithmIndex(i)) {
                        case BYTE_ALGORITHM_ID:
                            byte[] bval = (byte[]) atts.getAlgorithmData(i);
                            break;
                        case DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID:
                            int[] i4val = (int[]) atts.getAlgorithmData(i);
                            break;
                        case QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID:
                            float[] f2val = (float[]) atts.getAlgorithmData(i);
                            break;
                        default:
                            System.out.println("Unhandled algorithm in SAXPrinter: " + atts.getAlgorithmIndex(i));
                    }
                    print(" " + aName + "='" + strValue + "'");

                } else {
                    print(" " + aName + "='" + value + "'");
                }
            }

        }
        print(">");
    }

    /**
     * Handles endElement event.
     *
     * @param namespaceURI namespace URI
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name, or the empty string if qualified names are not available.
     */
    public void endElement(String namespaceURI, String localName, String qName )
        throws SAXException {

        flushText();

        String name = localName;

        if ("".equals(name)) {
            name = qName;
        }

        print("</" + name + ">");
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Display a string.
     */
    private void print(String s) throws SAXException {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    /**
     * Display a newLine.
     */
    private void printNewLine() throws SAXException {
        try {
            out.write(System.getProperty("line.separator"));
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    /**
     * Flush the text accumulated in the character buffer.
     */
    private void flushText() throws SAXException {
        if (textBuffer == null) {
            return;
        }

        print(textBuffer.toString());
        textBuffer = null;
    }

    /**
     * Get the publicId for this spec version
     *
     * @param major The major version
     * @param minor The minor version
     * @return The speced public id
     */
    protected String getPublicId(int major, int minor) {
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
    protected String getSystemId(int major, int minor) {
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
}
