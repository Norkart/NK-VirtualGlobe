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

package vrml.eai.event;

import vrml.eai.field.BaseField;

/**
 * The event that is generated when an eventOut changes a value.
 *
 * @version 1.0 7th march
 */
public class VrmlEvent
{
    // who generated this event
    private BaseField source;

    // The timestamp, in VRML time, that this occurred at
    private double timestamp;

    // User associated data
    private Object userData;

    /**
     * Construct a new event instance.
     *
     * @param src The source field that generated this event
     * @param ts The timestamp of the event, In VRML time.
     * @param data Any user associated data with this event
     */
    public VrmlEvent(BaseField src, double ts, Object data)
    {
        source = src;
        timestamp = ts;
        userData = data;
    }

    /**
     * Get the source field of this event.
     *
     * @return A reference to the eventIn/Out that generated this event.
     */
    public BaseField getSource()
    {
        return source;
    }

    /**
     * Get the timestamp that this event occured at
     *
     * @return The time of this event, in VRML time coordinates.
     */
    public double getTime()
    {
        return timestamp;
    }

    /**
     * Get any user associated data with this eventIn/Out.
     *
     * @return A reference to user associated data
     */
    public Object getData()
    {
        return userData;
    }
}