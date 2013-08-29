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
 * The exception that is thrown when the user attempts to access a method in
 * the Browser interface after the reference has had the dispose method called.
 *
 * @version 1.0 7th March 1998
 */
public class InvalidBrowserException extends VrmlException
{
    /**
     * Construct a basic instance of this exception with no error message
     */
    public InvalidBrowserException()
    {
        super();
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public InvalidBrowserException(String msg)
    {
        super(msg);
    }
}
