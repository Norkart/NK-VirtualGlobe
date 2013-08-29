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

package org.web3d.parser.x3d;

// External imports
import java.io.IOException;
import java.io.Reader;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.ErrorReporter;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.BaseReader;
import org.web3d.vrml.parser.VRMLParserUtils;

/**
 * Implementation of an X3D reader that can be either strict or lenient in
 * the parsing and supports only VRML encoded files.
 * <p>
 * If the user needs support for XML-encoded files, they should use either
 * {@link X3DReader} for generalised X3D support, or
 * {@link org.web3d.x3d.jaxp.XMLReader} for XML-only support.
 * <p>
 * When requested to parse, the reader will open the stream and check to see
 * that we have the right sort of parser.
 * <p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class VRMLEncodedReader extends BaseReader {

    /** The local relaxed parser instance */
    private X3DRelaxedParser relaxedParser;

    // private X3DStrictParser strictParser;

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    public VRMLEncodedReader() {
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
    public void parse(InputSource is)
        throws IOException, VRMLException {

        String conformance = (String)properties.get(CONFORMANCE_PROP);

        if(conformance.equals(WEAK_CONFORMANCE))
            parseWeakly(is);
        else if(conformance.equals(STRICT_CONFORMANCE))
            parseStrictly(is);
    }

    /**
     * Convenience method to build and organise a weak parser.
     *
     * @param is The input source to be used
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not X3D UTF8
     *    encoded.
     */
    private void parseWeakly(InputSource is)
        throws IOException, VRMLException {

        Reader rdr = is.getCharacterStream();

        // Before we do anything, we'll sanity check the headers if we are
        // supposed to use them
        String[] header;

        if(!ignoreHeader) {
            header = VRMLParserUtils.parseFileHeader(rdr);
            if(header.length != 4)
                throw new VRMLParseException(1, 1, "Header missing #X3D statement");

            // Now check the values of each item
            if(!header[0].equals("#X3D"))
                throw new VRMLParseException(1, 1, "Invalid header. Not a X3D file");

            if((header[1] == null) || !(header[1].equals("V3.0") || header[1].equals("V3.1") || header[1].equals("V3.2")))
                throw new VRMLParseException(1, 1, "Unsupported version " + header[1]);

            if((header[2] == null) || !header[2].equals("utf8"))
                throw new VRMLParseException(1, 1, "Unsupported encoding " + header[2]);
        } else {
            header = new String[4];
            header[1] = "V3.0";
            header[2] = "utf8";
        }

        if(relaxedParser == null) {
            relaxedParser = new X3DRelaxedParser(rdr);
            relaxedParser.initialize();
        } else
            relaxedParser.ReInit(rdr);

        relaxedParser.setContentHandler(contentHandler);
        relaxedParser.setRouteHandler(routeHandler);
        relaxedParser.setScriptHandler(scriptHandler);
        relaxedParser.setProtoHandler(protoHandler);
        relaxedParser.setErrorReporter(errorHandler);

        Locator loc = relaxedParser.getDocumentLocator();

        if(contentHandler != null) {
            String base_url = is.getBaseURL();
            contentHandler.setDocumentLocator(loc);

            // Start document needs to reconstruct the header line to give
            contentHandler.startDocument(is.getURL(),
                                         base_url,
                                         header[2],
                                         header[0],
                                         header[1],
                                         header[3]);
        }

        try {
            relaxedParser.Scene();

            if(contentHandler != null)
                contentHandler.endDocument();
        } catch(ParseException pe) {
            throw new VRMLParseException(loc.getLineNumber(),
                                         loc.getColumnNumber(),
                                         pe.getMessage());
        }
    }

    /**
     * Convenience method to build and organise a strict parser.
     *
     * @param is The input source to be used
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not X3D UTF8
     *    encoded.
     */
    private void parseStrictly(InputSource is)
        throws IOException, SAVException {

        throw new SAVNotSupportedException("Strict parsing not implemented yet");
    }
}
