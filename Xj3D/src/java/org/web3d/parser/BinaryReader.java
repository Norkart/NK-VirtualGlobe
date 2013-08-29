/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.parser;

// External imports
import java.io.*;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.xml.sax.SAXException;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.ErrorReporter;

import org.web3d.parser.x3d.X3DBinaryParser;
import org.web3d.parser.x3d.ParseException;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.BaseReader;

/**
 * Implementation of an X3D reader that will only handle binary file parsing.
 * <p>
 * When requested to parse, the reader will automatically assume the binary format.
 * If another format is given, it will generate a parse error.
 * <p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class BinaryReader extends BaseReader {

    /** The local binary X3D parser instance */
    private X3DBinaryParser binary03;

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    BinaryReader() {
    }

    //---------------------------------------------------------------
    // Methods required by BaseReader
    //---------------------------------------------------------------

    /**
     * Parse a VRML document represented by the given input source. This
     * method should not be called while currently processing a stream. If
     * multiple concurrent streams need to be parsed then multiple instances
     * of this interface should be used.
     *
     * @param is The input source to be used
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    public void parse(final InputSource input)
        throws IOException, VRMLException {

        InputStream is = null;

        String realURL = input.getURL();

        try {

            is = (InputStream)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run()  throws IOException {
                        return input.getByteStream();
                    }
                }
            );

        } catch(PrivilegedActionException pae) {

            String msg = "IO Error while attempting to access file " +
                         input.getURL();

            IOException ioe = new IOException(msg);
            ioe.initCause(pae.getException());

            throw ioe;

        }

        String conformance = (String)properties.get(CONFORMANCE_PROP);

        String base_url = input.getBaseURL();
        String real_url = input.getURL();

        if(real_url == null)
            real_url = "Unknown URL source. Base URL is " + base_url;

        if(conformance.equals(WEAK_CONFORMANCE))
            parseVRMLWeakly(is, base_url, real_url);
        else if(conformance.equals(STRICT_CONFORMANCE))
            parseVRMLStrictly(is, base_url, real_url);
    }

    /**
     * Convenience method to build and organise a weak parser.
     *
     * @param rdr The input source to be used
     * @param baseURL The URL to the base diretory of this stream
     * @param realURL the fully qualified URL to the stream
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    private void parseVRMLWeakly(InputStream is, String baseURL, String realURL)
        throws IOException, VRMLException {

        if(binary03 == null) {
            binary03 = new X3DBinaryParser(is);
            binary03.initialize();
        } else
            binary03.ReInit(is);

        binary03.setContentHandler(contentHandler);
        binary03.setRouteHandler(routeHandler);
        binary03.setScriptHandler(scriptHandler);

        binary03.setProtoHandler(protoHandler);
        binary03.setErrorReporter(errorReporter);
        binary03.setDocumentUrl(realURL);

        Locator loc = binary03.getDocumentLocator();

        try {
            binary03.Scene();

        } catch(org.web3d.parser.x3d.ParseException pe2) {
            VRMLParseException vpe = new VRMLParseException(loc.getLineNumber(),
                                         loc.getColumnNumber(),
                                         "Error in file: " + realURL + "\n" + pe2.getMessage());

            throw vpe;
        }
    }

    /**
     * Convenience method to build and organise a strict parser.
     *
     * @param rdr The input source to be used
     * @param baseURL The URL to the base diretory of this stream
     * @param realURL the fully qualified URL to the stream
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    protected void parseVRMLStrictly(InputStream is, String baseURL, String realURL)
        throws IOException, SAVException {

        throw new SAVNotSupportedException("Strict parsing not implemented yet");
    }
}

