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

package xj3d.filter;

// External imports
import java.io.*;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.net.content.VRMLFileNameMap;

import org.web3d.parser.vrml97.VRML97RelaxedParser;
import org.web3d.parser.x3d.X3DRelaxedParser;
import org.web3d.parser.x3d.X3DBinaryParser;
import org.web3d.parser.x3d.ParseException;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.parser.BaseReader;
import org.web3d.vrml.parser.VRMLParserUtils;

import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.x3d.jaxp.X3DErrorHandler;
import org.web3d.x3d.jaxp.X3DSAVAdapter;


/**
 * Implementation of a file reader that can handle more formats than just X3D
 * and VRML file parsing.
 * <p>
 * When requested to parse, the reader will first check the mimetype for any
 * specific parser and then look to the internals of the stream if that is
 * not useful for determining the stream contents.
 *
 * <p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class FileParserReader extends BaseReader {

    /** Name of the property specifying the namespace awareness */
    private static final String NAMESPACE_PROP = "useNamespaces";

    /** Name of the property to set the lexical handler in the XMLReader */
    private static final String LEXICAL_HANDLER_PROP =
    "http://xml.org/sax/properties/lexical-handler";


    /** Message when the header is completely missing */
    private static final String NO_HEADER_MSG =
        "Header missing #VRML or #X3D statement";

    /** Standard set of mimetypes for X3D/VRML97 parsing */
    private static final HashSet standardWeb3DTypes;

    /** The local relaxed VRML97 parser instance */
    private VRML97RelaxedParser relaxed97;

    // private VRML97StrictParser strict97;

    /** The local relaxed X3D parser instance */
    private X3DRelaxedParser relaxed03;

    /** The local binary X3D parser instance */
    private X3DBinaryParser binary03;

    // private X3DStrictParser strict03;

    /** The factory to generate SAX parser instances */
    private SAXParserFactory parserFactory;

    /** Adapter used to munge between SAX and SAV calls */
    private X3DSAVAdapter adapter;

    /** Common entity resolver instance */
    private X3DEntityResolver resolver;

    /** SAX Error handler for the system */
    private X3DErrorHandler x3dErrors;

    /** The assigned SAVAdapter contentHandler */
    private ContentHandler savContentHandler;

    /**
     * Collection of mime types registered to a string representing the
     * name of the filter class that will process it to a SAV stream.
     * These classes must implement the NonWeb3DFileParser interface.
     */
    private HashMap mimeToParserMap;

    /**
     * Static constructor to populate the list of standard mimetypes for
     * the Web3D file formats.
     */
    static {
        standardWeb3DTypes = new HashSet();
        standardWeb3DTypes.add("model/x3d+vrml");
        standardWeb3DTypes.add("model/x3d+binary");
        standardWeb3DTypes.add("model/x3d+xml");
        standardWeb3DTypes.add("model/world");
        standardWeb3DTypes.add("x-model/x-vrml");
        standardWeb3DTypes.add("model/vrml");
    }

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    FileParserReader() {
        properties.put(NAMESPACE_PROP, Boolean.FALSE);

        try {
            parserFactory = SAXParserFactory.newInstance();
        } catch(javax.xml.parsers.FactoryConfigurationError fce) {
            throw new FactoryConfigurationError("No SAX parser defined");
        }

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        adapter = new X3DSAVAdapter();
        x3dErrors = new X3DErrorHandler();
        resolver = new X3DEntityResolver();

        mimeToParserMap = new HashMap();
    }

    //---------------------------------------------------------------
    // Methods defined by BaseReader
    //---------------------------------------------------------------

    /**
     * Parse a VRML document represented by the given input source. This
     * method should not be called while currently processing a stream. If
     * multiple concurrent streams need to be parsed then multiple instances
     * of this interface should be used.
     *
     * @param input The input source to be used
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     * @throws SAVNotSupportedException The input file is not VRML97 UTF8
     *    encoded.
     */
    public void parse(InputSource input)
        throws IOException, VRMLException {

        String realURL = input.getURL();

        // Open the stream so that we can fetch the content type
        input.getCharacterStream();
        String contentType = input.getContentType();

        if (contentType == null) {
            VRMLFileNameMap fn_map = new VRMLFileNameMap();
            contentType = fn_map.getContentTypeFor(realURL);
        }

        if (realURL != null && realURL.endsWith(".x3db")) {
            contentType = "model/x3d+binary";
        }


        if(standardWeb3DTypes.contains(contentType))
            parseX3D(input, contentType);
        else {
            String classname = (String)mimeToParserMap.get(contentType);

            if(classname == null) {
                errorReporter.errorReport("No parser corresponding to the " +
                                          "MIME type " + contentType,
                                          null);
                return;
            }

            NonWeb3DFileParser parser = loadParserClass(classname);

            if(parser == null)
                return;

            parser.initialize();
            parser.setContentHandler(contentHandler);
            parser.setRouteHandler(routeHandler);
            parser.setScriptHandler(scriptHandler);
            parser.setProtoHandler(protoHandler);
            parser.setErrorReporter(errorReporter);
            parser.setDocumentUrl(realURL);

            parser.parse(input);
        }
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorReporter(ErrorReporter eh) {
        super.setErrorReporter(eh);
        x3dErrors.setErrorReporter(eh);
    }

    /**
     * Set the node handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param nh The node handler instance to use
     */
    public void setContentHandler(ContentHandler ch) {
        super.setContentHandler(ch);

        adapter.setContentHandler(ch);
        savContentHandler = ch;
    }

    /**
     * Set the script handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param sh The script handler instance to use
     */
    public void setScriptHandler(ScriptHandler sh) {
        super.setScriptHandler(sh);
        adapter.setScriptHandler(sh);
    }

    /**
     * Set the proto handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ph The proto handler instance to use
     */
    public void setProtoHandler(ProtoHandler ph) {
        super.setProtoHandler(ph);
        adapter.setProtoHandler(ph);
    }

    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param rh The route handler instance to use
     */
    public void setRouteHandler(RouteHandler rh) {
        super.setRouteHandler(rh);
        adapter.setRouteHandler(rh);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Allow registration of a mimetype to class name for the import
     * handler. Only a single class can be registered. If this is called
     * multiple times for the same MIME type string, the later will overwrite
     * any previously set value. The classname described must be a class
     * that implements the {@link NonWeb3DFileParser} interface. Setting a
     * null value for the classname will remove that importer from the
     * allowable collection.
     *
     * @param mimetype The mimetype string to register for
     * @param classname The fully qualified name of the class that could
     *    be loaded if this mimetype is encountered
     */
    void registerImporter(String mimetype, String classname) {
        mimeToParserMap.put(mimetype, classname);
    }

    /**
     * Check to see the classname of the importer registered for a specific
     * MIME type. If none is registered, null is returned.
     *
     * @param mimetype The MIME type string to check for
     * @return The name of the class registered or null
     */
    String getRegisteredImporter(String mimetype) {
        return (String)mimeToParserMap.get(mimetype);
    }

    /**
     * Handle the X3D/VRML97-specific file formats.
     *
     * @param input The input source to be used
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     */
    private void parseX3D(final InputSource input, String contentType)
        throws IOException, VRMLException {

        boolean type_found = false;
        boolean xml_parsing = false;
        PushbackReader pbr = null;
        boolean useFastInfoSet = false;

        if(contentType != null && contentType.equals("model/x3d+binary")) {
            useFastInfoSet = true;
        } else {
            // We need to first sniff the stream to work out what is being
            // given to us. The simplest way to do this is to look for the
            // first non-whitespace character. If it is '<' then we know we
            // have an XML file. If it is anything else, treat it as VRML
            // encoded. Do a quick check of the first character of the stream
            // as we're most likely to have the value there. Only on rare
            // occasions should we have any whitespace.

            try {
                Reader is = (Reader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run()  throws IOException {
                            return input.getCharacterStream();
                        }
                    }
                );

                pbr = new PushbackReader(is);
            } catch(PrivilegedActionException pae) {
                String msg = "IO Error while attempting to access file " +
                             input.getURL();

                IOException ioe = new IOException(msg);
                ioe.initCause(pae.getException());

                throw ioe;
            }

            int ch = 0;

            while(!type_found) {
                ch = pbr.read();

                switch(ch) {
                    case '<':
                        xml_parsing = true;
                        type_found = true;
                        break;

                    case '#':
                        xml_parsing = false;
                        type_found = true;
                        break;

                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        break;

                    default:
                        // anything other than whitespace must be the start of the
                        // stream, so we've found something to parse.
                        type_found = true;
                        break;
                }
            }

            // push back the last read character.
            if (ch!=-1)
                pbr.unread(ch);
        }

        String conformance = (String)properties.get(CONFORMANCE_PROP);

        if(xml_parsing) {
            boolean validate = conformance.equals(WEAK_CONFORMANCE);

            Boolean namespace = (Boolean)properties.get(NAMESPACE_PROP);

            parserFactory.setValidating(!validate);
            parserFactory.setNamespaceAware(namespace.booleanValue());

            org.xml.sax.XMLReader reader = null;

            try {
                SAXParser parser = parserFactory.newSAXParser();

/*
                Locator loc = savContentHandler.getDocumentLocator();

                if (loc != null)
                    adapter.setDocumentLocator(loc);
*/
                savContentHandler.startDocument(input.getURL(),
                                         input.getBaseURL(),
                                         "xml",
                                         "#X3D",
                                         "V3.0",
                                         null);


                reader = parser.getXMLReader();
                reader.setContentHandler(adapter);
                reader.setProperty(LEXICAL_HANDLER_PROP, adapter);
                reader.setErrorHandler(x3dErrors);
                reader.setEntityResolver(resolver);
            } catch(Exception e) {
                throw new IOException("Unable to configure factory as required");
            }

            // Convert our InputSource, to their InputSource....
            org.xml.sax.InputSource xis = new org.xml.sax.InputSource();
            xis.setCharacterStream(pbr);
            xis.setEncoding(input.getEncoding());

            try {
                reader.parse(xis);
            } catch(SAXException se) {
                Exception e = se.getException();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                if(e != null)
                    e.printStackTrace(pw);
                else
                    se.printStackTrace(pw);

                StringBuffer buf = new StringBuffer("SAX Error: ");
                buf.append(se.toString());

                buf.append("\nInternal stack trace");

                buf.append(sw.toString());

                throw new VRMLException(buf.toString());
            }
        } else {
            String base_url = input.getBaseURL();
            String real_url = input.getURL();

            if(real_url == null)
                real_url = "Unknown URL source. Base URL is " + base_url;

            if(conformance.equals(WEAK_CONFORMANCE))
                parseVRMLWeakly(pbr, useFastInfoSet, input, base_url, real_url);
            else if(conformance.equals(STRICT_CONFORMANCE))
                parseVRMLStrictly(pbr, base_url, real_url);
        }
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
    private void parseVRMLWeakly(Reader rdr,
                                 boolean useFastInfoSet,
                                 InputSource source,
                                 String baseURL,
                                 String realURL)
        throws IOException, VRMLException {

        // Before we do anything, we'll sanity check the headers if we are
        // supposed to use them
        String[] header;
        boolean vrml_97 = false;
        boolean binary = false;

        // TODO: It seems like we should be able to sniff the FastInfoset header, but
        //       what should we do when the rdr is null?  Also the underlying FI libraries
        //       expect that header so we need to reintroduce to the stream.

        if (rdr == null) {
            if (useFastInfoSet)
                binary = true;

            header = new String[4];
            header[0] = "#X3D";
            header[1] = "V3.0";
            header[2] = "binary";
        } else if(!ignoreHeader) {
            header = VRMLParserUtils.parseFileHeader(rdr);

            if(header.length != 4)
                throw new VRMLParseException(1, 1, NO_HEADER_MSG);

            // Now check the values of each item
            if(!header[0].equals("#VRML") && !header[0].equals("#X3D"))
                throw new VRMLParseException(1, 1, "Invalid header. Not a vrml file");

            vrml_97 = header[0].equals("#VRML");

            if((header[1] == null) ||
               (vrml_97 && !header[1].equals("V2.0")) ||
               (!vrml_97 && !header[1].equals("V3.0")))
                throw new VRMLParseException(1, 5, "Unsupported version " + header[1]);
            if((header[2] == null) || (!header[2].equals("utf8") && !header[2].equals("binary")))
                throw new VRMLParseException(1, 10, "Unsupported encoding " + header[2]);
        } else {
            // if we are going header-less then assume X3D 3.0 format
            vrml_97 = false;
            header = new String[4];
            header[0] = "#X3D";
            header[1] = "V3.0";
            header[2] = "utf8";
        }


        Locator loc = null;

        if(vrml_97) {
            if(relaxed97 == null) {
                relaxed97 = new VRML97RelaxedParser(rdr);
                relaxed97.initialize();
            } else
                relaxed97.ReInit(rdr);

            relaxed97.setContentHandler(contentHandler);
            relaxed97.setRouteHandler(routeHandler);
            relaxed97.setScriptHandler(scriptHandler);
            relaxed97.setProtoHandler(protoHandler);
            relaxed97.setErrorReporter(errorReporter);
            relaxed97.setDocumentUrl(realURL);

            loc = relaxed97.getDocumentLocator();
        }

        if (binary) {
            InputStream is = source.getByteStream();
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

            loc = binary03.getDocumentLocator();
        } else {
            if(relaxed03 == null) {
                relaxed03 = new X3DRelaxedParser(rdr);
                relaxed03.initialize();
            } else
                relaxed03.ReInit(rdr);

            relaxed03.setContentHandler(contentHandler);
            relaxed03.setRouteHandler(routeHandler);
            relaxed03.setScriptHandler(scriptHandler);
            relaxed03.setProtoHandler(protoHandler);
            relaxed03.setErrorReporter(errorReporter);
            relaxed03.setDocumentUrl(realURL);

            loc = relaxed03.getDocumentLocator();
        }

        if(contentHandler != null) {
            contentHandler.setDocumentLocator(loc);

            // Start document needs to reconstruct the header line to give
            contentHandler.startDocument(realURL,
                                         baseURL,
                                         header[2],
                                         header[0],
                                         header[1],
                                         header[3]);
        }

        try {
            if(vrml_97)
                relaxed97.Scene();
            else if (binary) {
                binary03.Scene();
            } else
                relaxed03.Scene();

            if(contentHandler != null)
                contentHandler.endDocument();
        } catch(org.web3d.parser.vrml97.ParseException pe1) {
            throw new VRMLParseException(loc.getLineNumber(),
                                         loc.getColumnNumber(),
                                         pe1.getMessage());
        } catch(org.web3d.parser.x3d.ParseException pe2) {
            throw new VRMLParseException(loc.getLineNumber(),
                                         loc.getColumnNumber(),
                                         pe2.getMessage());
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
    protected void parseVRMLStrictly(Reader rdr, String baseURL, String realURL)
        throws IOException, SAVException {

        throw new SAVNotSupportedException("Strict parsing not implemented yet");
    }

    /**
     * Load the named class file and check everything to make sure it
     * implements the right interface.
     */
    private NonWeb3DFileParser loadParserClass(final String classname) {
        NonWeb3DFileParser ret_val = null;

        try {
            ret_val = (NonWeb3DFileParser)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        Class cls = Class.forName(classname);
                        return cls.newInstance();
                    }
                }
            );

        } catch(PrivilegedActionException pae) {
            String msg = "Error attempting to create external parser " +
                         classname;

            errorReporter.errorReport(msg, pae.getException());
        } catch(ClassCastException cce) {
            String msg = "Unable to cast named class " + classname +
               " to an instance of xj3d.filter.NonWeb3DFileParser";

            errorReporter.errorReport(msg, null);
        }

        return ret_val;
    }
}

