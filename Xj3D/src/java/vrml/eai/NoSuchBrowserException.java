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
 * The exception that is thrown when the Browser factory is not able to locate
 * a browser given the arguments.
 *
 * @see BrowserFactory
 * @see BrowserFactoryImpl
 *
 * @version 1.0 23rd June 1998
 */
public class NoSuchBrowserException extends VrmlException
{
    /**
     * Construct a basic instance of this exception with no error message
     */
    public NoSuchBrowserException()
    {
        super();
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public NoSuchBrowserException(String msg)
    {
        super(msg);
    }
}
