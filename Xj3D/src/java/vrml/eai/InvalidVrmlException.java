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
 * The exception that is thrown when a the string passed to
 * createVrmlFromString does not contain legal VRML.
 *
 * @version 1.0 7th March 1998
 */
public class InvalidVrmlException extends VrmlException
{
    /**
     * Construct a basic instance of this exception with no error message
     */
    public InvalidVrmlException()
    {
        super();
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public InvalidVrmlException(String msg)
    {
        super(msg);
    }
}
