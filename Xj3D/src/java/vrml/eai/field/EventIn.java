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

import vrml.eai.Node;

/**
 * A VRML eventIn class. Represents the VRML write only access type.
 * <P>
 * The user can associate data and also listen for events on this eventIn.
 * These events are notified to the listener at the time that they arrive at
 * the field. This allows the addition of extra features like monitoring a
 * particular field for certain values being set (eg for debugging purposes)
 * without having to know every single node that has a ROUTE to this eventIn.
 *
 * @version 1.0 7th March 1998
 */
public abstract class EventIn extends BaseField
{
    /**
     * Construct an instance of this class.
     *
     * @param type The type of the field
     */
    protected EventIn(int type)
    {
        super(type);
    }

    /**
     * Associate user data with this event. Whenever an event is generated
     * on this eventIn. this data will be available with the Event through
     * its getData method.
     *
     * @param data The data to associate with this eventIn instance
     */
    public abstract void setUserData(Object data);

    /**
     * Get the user data that is associated with this eventIn
     *
     * @return The user data, if any, associated with this eventIn
     */
    public abstract Object getUserData();
}






