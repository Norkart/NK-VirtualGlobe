/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// Standard imports
import java.util.EventObject;

// Application specific imports
// None

/**
 * The event that is generated when a field changes value.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class X3DFieldEvent extends EventObject {

    /** The timestamp, in VRML time, that this occurred at */
    protected double timestamp;

    /** User associated data */
    protected Object userData;

    /**
     * Construct a new event instance.
     *
     * @param src The source field that generated this event
     * @param ts The timestamp of the event, In VRML time.
     * @param data Any user associated data with this event
     */
    public X3DFieldEvent(Object src, double ts, Object data) {
        super(src);

        timestamp = ts;
        userData = data;
    }

    /**
     * Get the timestamp that this event occured at
     *
     * @return The time of this event, in VRML time coordinates.
     */
    public double getTime() {
        return timestamp;
    }

    /**
     * Get any user associated data with this eventIn/Out.
     *
     * @return A reference to user associated data
     */
    public Object getData() {
        return userData;
    }
}
