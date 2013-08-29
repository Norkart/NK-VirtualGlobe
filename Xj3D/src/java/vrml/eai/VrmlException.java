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
 * The basic exception that is thrown by any VRML method call that wishes to
 * throw events. Based on RuntimeException so that the user has the choice of
 * deciding whether to catch the exception or not.
 *
 * @version 1.0 30 April 1998
 */
public class VrmlException extends RuntimeException
{
    /**
     * Construct a basic instance of this exception with no error message
     */
    public VrmlException()
    {
        super();
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public VrmlException(String msg)
    {
        super(msg);
    }
}
