/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.sai;

// Standard imports
// None

// Application specific imports
import org.web3d.x3d.sai.X3DFieldEvent;

/**
 * Local variant of the field event that allows data to be reset.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SAIFieldEvent extends X3DFieldEvent {

    /**
     * Construct a new default event instance.
     *
     * @param src Anything non-null. Gets overwritten anyway
     */
    SAIFieldEvent(Object src) {
        super(src, 0, null);
    }


    /**
     * Construct a new event instance.
     *
     * @param src The source field that generated this event
     * @param ts The timestamp of the event, In VRML time.
     * @param data Any user associated data with this event
     */
    void update(Object src, double ts, Object data) {
        source = src;
        timestamp = ts;
        userData = data;
    }
}
