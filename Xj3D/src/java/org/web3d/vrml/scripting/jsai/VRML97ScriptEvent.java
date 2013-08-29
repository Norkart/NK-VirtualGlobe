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

package org.web3d.vrml.scripting.jsai;

// Standard imports
// none

// Application specific imports
import vrml.Event;
import vrml.ConstField;

/**
 * A wrapper class for VRML events that are about to be sent into the script.
 * <p>
 *
 * This class is designed to be reused rather than creating a new instance
 * each time.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class VRML97ScriptEvent extends Event {

    /**
     * Re-initialise the event class with the new details. Replaces the old
     * details with the new ones so that it can be re-used.
     *
     * @param field The name of the field that has changed
     * @param time The timestamp of the event
     * @param val The value of the field
     */
    void reInit(String field, double time, ConstField val) {
        name = field;
        timestamp = time;
        value = val;
    }

    /**
     * Clone the object to create an identical copy.
     *
     * @return A complete copy of the event.
     */
    public Object clone() {
        VRML97ScriptEvent cl = new VRML97ScriptEvent();
        cl.reInit(name, timestamp, value);

        return cl;
    }
}

