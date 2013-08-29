/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai;

/**
 * The exception that is thrown when an error occurs in the connection between
 * the external application and the VRML browser. Typically this might be a
 * network connection stopping or similar problem.
 *
 * @version 1.0 3 August 1998
 */
public class ConnectionException extends VrmlException
{
    /**
     * Construct a basic instance of this exception with no error message
     */
    public ConnectionException()
    {
        super();
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public ConnectionException(String msg)
    {
        super(msg);
    }
}
