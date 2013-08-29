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

package org.web3d.vrml.parser;

// External imports
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;

import org.web3d.util.ErrorReporter;
import org.web3d.util.DefaultErrorReporter;

/**
 * Common reader implementation that can be either strict or lenient in
 * the parsing.
 * <p>
 * This parser supports the following properties:
 * <ul>
 * <li>"conformance": ["weak", "strict"]. String values. Defaults to weak</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class BaseReader implements VRMLReader {

    // Constants used for property names and values

    /** Name of the property specifying the conformance level */
    protected static final String CONFORMANCE_PROP = "conformance";

    /** Name of the property value specifying weak conformance */
    protected static final String WEAK_CONFORMANCE = "weak";

    /** Name of the property value specifying strict conformance */
    protected static final String STRICT_CONFORMANCE = "strict";

    /** Map of properties held by this reader */
    protected HashMap properties;

    // Variables for our general use during parsing

    /** Reference to the registered content handler if we have one */
    protected ContentHandler contentHandler;

    /** Reference to the registered route handler if we have one */
    protected RouteHandler routeHandler;

    /** Reference to the registered script handler if we have one */
    protected ScriptHandler scriptHandler;

    /** Reference to the registered proto handler if we have one */
    protected ProtoHandler protoHandler;

    /** Reference to the registered error handler if we have one */
    protected ErrorReporter errorReporter;

    /** Reference to the registered error handler if we have one */
    protected ErrorHandler errorHandler;

    /** Flag to ignore the header */
    protected boolean ignoreHeader;

    /**
     * Create a new instance of the reader. Does not initialise anything until
     * we know what sort of input file we have.
     */
    protected BaseReader() {
        properties = new HashMap();
        properties.put(CONFORMANCE_PROP, WEAK_CONFORMANCE);
        ignoreHeader = false;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //---------------------------------------------------------------
    // Methods required by VRMLReader
    //---------------------------------------------------------------

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
        errorHandler = eh;
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param ep The error reporter instance to use
     */
    public void setErrorReporter(ErrorReporter ep) {
        if(ep == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = ep;
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

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------
}
