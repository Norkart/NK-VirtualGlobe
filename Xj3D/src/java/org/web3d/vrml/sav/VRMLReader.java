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

package org.web3d.vrml.sav;

// Standard imports
import java.io.IOException;

// Application specific imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLException;

/**
 * Representation of a class capable of reading a VRML source and providing
 * event notification of the items in that file.
 * <p>
 * The class acts as a serial stream of events as the VRML file is parsed.
 * If a given handler is not registered, events of these types will be
 * ignored.
 * <p>
 * All SAV interfaces are assumed to be Synchronous. The parse methods shall
 * not return until parsing is complete and readers shall wait for an event
 * handler callback to return before reporting the next event.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public interface VRMLReader {

    /**
     * Parse a VRML document represented by the given input source. This
     * method should not be called while currently processing a stream. If
     * multiple concurrent streams need to be parsed then multiple instances
     * of this interface should be used.
     *
     * @param is The input source to be used
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     */
    public void parse(InputSource is)
        throws IOException, VRMLException;

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
        throws SAVNotSupportedException;

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
        throws SAVNotSupportedException;

    /**
     * Tell the reader that it should not look for the header. This is needed
     * to deal with createVrmlFromString() calls that do not include the header
     * as part of the stream and would otherwise cause an error.
     *
     * @param enable true to stop looking for a header
     */
    public void setHeaderIgnore(boolean enable);

    /**
     * Get the currently set {@link ErrorHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set error handler.
     */
    public ErrorReporter getErrorHandler();

    /**
     * Set the error handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorHandler(ErrorHandler eh);

    /**
     * Set the error handler to the given instance. If the value is null it
     * will clear the currently set instance. If the reporter is also an
     * instance of ErrorHandler, the actions will be the same as for setting
     * an error handler.
     *
     * @param eh The error reporter instance to use
     */
    public void setErrorReporter(ErrorReporter eh);


    /**
     * Get the currently set {@link ContentHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set node handler.
     */
    public ContentHandler getContentHandler();

    /**
     * Set the node handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param nh The node handler instance to use
     */
    public void setContentHandler(ContentHandler nh);

    /**
     * Get the currently set {@link ScriptHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set script handler.
     */
    public ScriptHandler getScriptHandler();

    /**
     * Set the script handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param sh The script handler instance to use
     */
    public void setScriptHandler(ScriptHandler sh);

    /**
     * Get the currently set {@link ProtoHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set proto handler.
     */
    public ProtoHandler getProtoHandler();

    /**
     * Set the proto handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param ph The proto handler instance to use
     */
    public void setProtoHandler(ProtoHandler ph);

    /**
     * Get the currently set {@link RouteHandler} instance. If nothing is set
     * it will return null.
     *
     * @return The currently set route handler.
     */
    public RouteHandler getRouteHandler();

    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param rh The route handler instance to use
     */
    public void setRouteHandler(RouteHandler rh);
}
