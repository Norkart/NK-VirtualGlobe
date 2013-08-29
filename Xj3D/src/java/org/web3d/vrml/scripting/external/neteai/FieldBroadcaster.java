/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

import java.io.DataInputStream;
import java.io.IOException;

import vrml.eai.event.VrmlEventListener;

/**
 * FieldBroadcaster is the object responsible for turning
 * field changed messages from the network into client side field changed
 * events.
 */
public interface FieldBroadcaster {

    /**
     * Generate the broadcast of a field changed event for a given
     * field ID.  Reads the field value off the network and then
     * adds the field to the work list.
     * @param fieldID The network ID of the field which changed
     * @param dis The input stream to read the field value from
     */
    void generateFieldBroadcast(int fieldID, DataInputStream dis) throws IOException;

    /**
     * Remove a listener for a given field ID
     * @param fieldID The network field ID to remove a listener from
     * @param l The listener for the field
     * @return Was that the last listener for this field ID?
     */
    boolean removeVrmlEventListener(int fieldID, VrmlEventListener l);

    /**
     * Add a listener for a given field ID.  Need to specify
     * the field type so that the broadcast system knows what to generate.
     * @param fieldID The network field ID to add a listener for
     * @param fieldType The type of the field
     * @param l The listener for the field
     * @return Was that the first listener for this field ID?
     */
    boolean addVrmlEventListener(int fieldID, int fieldType, VrmlEventListener l);

    /**
     * Get the user data associated with a field
     * @param fieldID The network ID of the field
     * @return The user data associated with the field
     */
    Object getUserData(int fieldID);

    /**
     * Set the user data associated with a field
     * @param fieldID The network ID of the field
     * @param data The user data associated the the field
     */
    void setUserData(int fieldID, Object data);

}
