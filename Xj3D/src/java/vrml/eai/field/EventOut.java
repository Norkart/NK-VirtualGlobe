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
import vrml.eai.event.VrmlEventListener;

/**
 * A VRML eventOut class. Represents the VRML read only access type.
 * <P>
 * The user can encapsulate data and associate that with this field. The user
 * can register themselves as listeners for the output of this event changing
 * by adding themselves as a VRMLEvent listener.
 *
 * @version 1.0 7th March 1998
 */
public abstract class EventOut extends BaseField
{
    /**
     * Construct an instance of this class.
     *
     * @param type The type of the field
     */
    protected EventOut(int type)
    {
        super(type);
    }

    /**
     * Add a listener for changes in this eventOut.
     *
     * @param l The listener to add
     */
    public abstract void addVrmlEventListener(VrmlEventListener l);

    /**
     * Remove a listener for changes in this eventOut.
     *
     * @param l The listener to remove
     */
    public abstract void removeVrmlEventListener(VrmlEventListener l);

    /**
     * Associate user data with this event. Whenever an event is generated
     * on this eventOut. this data will be available with the Event through
     * its getData method.
     *
     * @param data The data to associate with this eventOut instance
     */
    public abstract void setUserData(Object data);

    /**
     * Get the user data that is associated with this eventOut
     *
     * @return The user data, if any, associated with this eventOut
     */
    public abstract Object getUserData();
}
