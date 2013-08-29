/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.parser.vrml97;

// Standard imports
import java.io.IOException;
import java.io.Reader;

// Application specific imports
import org.web3d.vrml.sav.*;

import org.web3d.util.ErrorReporter;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.BaseReader;
import org.web3d.vrml.parser.VRMLParserUtils;

/**
 * Implementation of a VRML97 reader that can be either strict or lenient in
 * the parsing.
 * <p>
 * When requested to parse, the reader will open the stream and check to see
 * that we have the right sort of parser. If the header does not contain
 * "#VRML V2.0 utf8" then it will generate an exception.
 * <p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class VRML97Reader extends BaseReader {

    /** The local relaxed parser instance */
    private VRML97RelaxedParser relaxedParser;

    // private VRML97StrictParser strictParser;

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    public VRML97Reader() {
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
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
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
                throw new VRMLParseException(1, 1, "Header missing #VRML statement");

            // Now check the values of each item
            if(!header[0].equals("#VRML"))
                throw new VRMLParseException(1, 1, "Invalid header. Not a vrml file");

            if((header[1] == null) || !header[1].equals("V2.0"))
                throw new VRMLParseException(1, 1, "Unsupported version " + header[1]);

            if((header[2] == null) || !header[2].equals("utf8"))
                throw new VRMLParseException(1, 1, "Unsupported encoding " + header[2]);
        } else {
            header = new String[4];
            header[0] = "#VRML";
            header[1] = "V2.0";
            header[2] = "utf8";
        }

        if(relaxedParser == null) {
            relaxedParser = new VRML97RelaxedParser(rdr);
            relaxedParser.initialize();
        } else
            relaxedParser.ReInit(rdr);

        relaxedParser.setContentHandler(contentHandler);
        relaxedParser.setRouteHandler(routeHandler);
        relaxedParser.setScriptHandler(scriptHandler);
        relaxedParser.setProtoHandler(protoHandler);
        relaxedParser.setErrorReporter(errorHandler);

        String base_url = is.getBaseURL();
        String real_url = is.getURL();
        if(real_url == null)
            real_url = "Unknown URL source. Base URL is " + base_url;

        relaxedParser.setDocumentUrl(real_url);

        Locator loc = relaxedParser.getDocumentLocator();

        if(contentHandler != null) {
            contentHandler.setDocumentLocator(loc);

            // Start document needs to reconstruct the header line to give
            contentHandler.startDocument(real_url,
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
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    private void parseStrictly(InputSource is)
        throws IOException, SAVException {

        throw new SAVNotSupportedException("Strict parsing not implemented yet");
    }
}
