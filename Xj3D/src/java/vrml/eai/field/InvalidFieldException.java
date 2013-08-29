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

package vrml.eai.field;

import vrml.eai.VrmlException;

/**
 * The exception that is thrown when a reference to any field is not valid.
 * Generally used as a base class to more specific invalid field methods.
 * <P>
 * An eventOut may be invalid for a number of reasons:
 * <UL>
 * <LI>The user may have typed in the wrong name through a typo.
 * <LI>The name may not correspond to a field in that node at all.
 * <LI>The name given refers to a valid field but the field cannot be
 *     accessed as an eventOut.
 * </UL>
 *
 * @version 1.0 7th March 1998
 * @see InvalidEventInException
 * @see InvalidEventOutException
 */
public class InvalidFieldException extends VrmlException
{
    /**
     * Construct a basic instance of this exception with no error message
     */
    public InvalidFieldException()
    {
        super();
    }

    /**
     * Constructs a new exception with a particular message
     *
     * @param msg The message to use
     */
    public InvalidFieldException(String msg)
    {
        super(msg);
    }
}
