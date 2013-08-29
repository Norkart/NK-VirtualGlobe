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

package org.web3d.x3d.jaxp;

// External imports
import java.io.*;

import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.FactoryConfigurationError;

/**
 * Common reader implementation that can be either strict or lenient in
 * the parsing.
 * <p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * <li>"useNamespaces": [true, false]. Boolean values. Defaults to false</li>
 * </ul>
 *
 * Weak parsing is defined to mean not using validation on the document. Strong
 * parsing will enforce validation.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class XMLReader implements VRMLReader {

    // Constants used for property names and values

    /** Name of the property specifying the conformance level */
    private static final String CONFORMANCE_PROP = "conformance";

    /** Name of the property specifying the namespace awareness */
    private static final String NAMESPACE_PROP = "useNamespaces";

    /** Name of the property to set the lexical handler in the XMLReader */
    private static final String LEXICAL_HANDLER_PROP =
    "http://xml.org/sax/properties/lexical-handler";


    /** Name of the property value specifying weak conformance */
    private static final String WEAK_CONFORMANCE = "weak";

    /** Name of the property value specifying strict conformance */
    private static final String STRICT_CONFORMANCE = "strict";

    /** Map of properties held by this reader */
    private HashMap properties;

    // Variables for our general use during parsing

    /** Reference to the registered error handler if we have one */
    private ErrorReporter errorReporter;

    /** Flag to ignore the header */
    private boolean ignoreHeader;

    /** The factory to generate SAX parser instances */
    private SAXParserFactory parserFactory;

    /** Adapter used to munge between SAX and SAV calls */
    private X3DSAVAdapter adapter;

    /** Common entity resolver instance */
    private X3DEntityResolver resolver;

    /** SAX Error handler for the system */
    private X3DErrorHandler errorHandler;

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    public XMLReader() throws FactoryConfigurationError {
        properties = new HashMap();
        properties.put(CONFORMANCE_PROP, WEAK_CONFORMANCE);
        properties.put(NAMESPACE_PROP, Boolean.FALSE);
        ignoreHeader = false;

        try {
            parserFactory = SAXParserFactory.newInstance();
        } catch(javax.xml.parsers.FactoryConfigurationError fce) {
            throw new FactoryConfigurationError("No SAX parser defined");
        }

        adapter = new X3DSAVAdapter();
        errorHandler = new X3DErrorHandler();
        resolver = new X3DEntityResolver();
    }

    //---------------------------------------------------------------
    // Methods required by VRMLReader
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
     * @throws SAVNotSupportedException The input file is not XML
     *    encoded.
     */
    public void parse(InputSource input)
        throws IOException, VRMLException {

        String conf = (String)properties.get(CONFORMANCE_PROP);

        boolean validate = conf.equals(WEAK_CONFORMANCE);
        Boolean namespace = (Boolean)properties.get(NAMESPACE_PROP);

        parserFactory.setValidating(!validate);
        parserFactory.setNamespaceAware(namespace.booleanValue());

        org.xml.sax.XMLReader reader = null;

        try {
            SAXParser parser = parserFactory.newSAXParser();
            reader = parser.getXMLReader();
            reader.setContentHandler(adapter);
            reader.setProperty(LEXICAL_HANDLER_PROP, adapter);
            reader.setErrorHandler(errorHandler);
            reader.setEntityResolver(resolver);
        } catch(Exception e) {
            throw new IOException("Unable to configure factory as required");
        }

        // Convert our InputSource, to their InputSource....
        org.xml.sax.InputSource xis = new org.xml.sax.InputSource();
        Reader rdr = input.getCharacterStream();
        if(rdr != null)
            xis.setCharacterStream(rdr);
        else
            xis.setByteStream(input.getByteStream());

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
            buf.append(sw.toString());
            throw new VRMLException(buf.toString());
        }
    }

    /**
     * Get the value of the named property. VRMLReaders are not required to
     * support any specific property names.
     *
     * @param prop The name of the property to get the value of
     * @return The value of the set property or null if not set
     * @throws SAVNotSupportedException The VRMLReader does not recognize
     *   or does not support this property name.
     */
    public Object getProperty(String prop)
        throws SAVNotSupportedException {

        if(!properties.containsKey(prop))
            throw new SAVNotSupportedException("Unknown property: " + prop);

        return properties.get(prop);
    }

    /**
     * Set the value of the named property to the given value. VRMLReaders are
     * not required to support any specific property names. Using a value of
     * null will clear the currently set property value.
     *
     * @param name The name of the property to set
     * @param value The value of this property
     * @throws SAVNotSupportedException The VRMLReader does not recognize
     *   or does not support this property name.
     */
    public void setProperty(String name, Object value)
        throws SAVNotSupportedException {

        if(!properties.containsKey(name))
            throw new SAVNotSupportedException("Unknown property: " + name);

        if(value == null)
            throw new SAVNotSupportedException("Null values not supported");

        properties.put(name, value);
    }

    /**
     * Tell the reader that it should not look for the header. This is needed
     * to deal with createVrmlFromString() calls that do not include the header
     * as part of the stream and would otherwise cause an error.
     *
     * @param enable true to stop looking for a header
     */
    public void setHeaderIgnore(boolean enable) {
        ignoreHeader = enable;
    }

    /**
     * Get the currently set {@link ErrorHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set error handler.
     */
    public ErrorReporter getErrorHandler() {
        return errorReporter;
    }

    /**
     * Set the error handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorHandler(ErrorHandler eh) {
        errorReporter = eh;
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorReporter(ErrorReporter eh) {
        errorReporter = eh;
        errorHandler.setErrorReporter(eh);
    }

    /**
     * Get the currently set {@link ContentHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set node handler.
     */
    public ContentHandler getContentHandler() {
        return adapter.getContentHandler();
    }

    /**
     * Set the content handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ch The content handler instance to use
     */
    public void setContentHandler(ContentHandler ch) {
        adapter.setContentHandler(ch);
    }

    /**
     * Get the currently set {@link ScriptHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set script handler.
     */
    public ScriptHandler getScriptHandler() {
        return adapter.getScriptHandler();
    }


    /**
     * Set the script handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param sh The script handler instance to use
     */
    public void setScriptHandler(ScriptHandler sh) {
        adapter.setScriptHandler(sh);
    }

    /**
     * Get the currently set {@link ProtoHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set proto handler.
     */
    public ProtoHandler getProtoHandler() {
        return adapter.getProtoHandler();
    }


    /**
     * Set the proto handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ph The proto handler instance to use
     */
    public void setProtoHandler(ProtoHandler ph) {
        adapter.setProtoHandler(ph);
    }

    /**
     * Get the currently set {@link RouteHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set route handler.
     */
    public RouteHandler getRouteHandler() {
        return adapter.getRouteHandler();
    }

    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param rh The route handler instance to use
     */
    public void setRouteHandler(RouteHandler rh) {
        adapter.setRouteHandler(rh);
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------
}
