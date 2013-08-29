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

import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.EventIn;
import vrml.eai.field.EventOut;

/**
 * FieldAndNodeRequestProcessor is the interface
 * abstracting the class which handles all of the
 * EAI field and node requests.  The callers are
 * expected to supply the appropriate network
 * IDs in place of the implicit node and field
 * objects.
 */
public interface FieldAndNodeRequestProcessor {

    /** Notify server that node is no longer referenced.
     *  Does not check for remaining instances.
     * @param nodeID Network ID of the node to dispose
     */
    public abstract void disposeNode(int nodeID);

    /**
     * Get an eventIn for a given field name on a given
     * node
     * @param nodeID The network ID of the node
     * @param fieldName The name of the field
     * @return The eventIn object
     */
    public abstract EventIn getEventIn(int nodeID, String fieldName);

    /**
     * Get an eventOut for a given field name on a given
     * node
     * @param nodeID The network ID of the node
     * @param fieldName The name of the field
     * @return The eventOut object
     */
    public abstract EventOut getEventOut(int nodeID, String fieldName);

    /** Transmit a getNode request to the server and wait for reply.
     * @param nodeName The nodeName to request
     * @return The ID for the node if found.
     */
    public abstract int getNode(String nodeName);

    /** Returns the type name for a given node.
     * @param nodeID The network ID of the node.
     * @return The type name of the node.
     */
    public abstract String getNodeType(int nodeID);
    
    /** Transmit a setFieldValue request.  The buffer holds
     *  the field value to transmit using writeFieldValue.
     * @param fieldID The network field ID
     * @param buffer The buffer holding the value
     */
    public void setFieldValue(int fieldID, EventWrapper buffer);
    
    /** Transmit a getFieldValue request.  The buffer will
     *  receive the field value using readFieldValue.
     * @param fieldID The network field ID
     * @param buffer The buffer to receive the value
     */
    public void getFieldValue(int fieldID, EventWrapper buffer);
    
    /** Get the user data associated with a field.
     *  This is implemented on the client side.
     * @param fieldID The network field ID
     * @return The user data object
     */
    public Object getUserData(int fieldID);
    
    /** Set the user data associated with a field.
     *  This is implemented on the client side.
     *  @param fieldID The network field ID
     *  @param data The user data object
     */
    public void setUserData(int fieldID, Object data);

    /**
     * Remove a listener from the listeners for a field.  Will turn off event
     * notifications if this is the last listener for that field.
     * @param fieldID The network ID of the field.
     * @param l The listener to remove
     */
    public abstract void removeVrmlEventListener(int fieldID, VrmlEventListener l);

    /**
     * Add a listener to event changes for a field.  Turns on event notifications
     * as needed.  Need to pass the field type so that the broadcast system knows
     * what to generate when updates arrive.
     * @param fieldID The network ID of the field
     * @param fieldType The type of the field
     * @param l The listener to add.
     */
    public abstract void addVrmlEventListener(int fieldID, int fieldType, VrmlEventListener l);

    /**
     * Get the number of elements in this field's value
     * @param fieldID The network ID of this field
     * @return The number of elements in the field's value
     */
    public abstract int getNumFieldValues(int fieldID);

    /**
     * Return the number of components in an SFImage field
     * @param fieldID The network field ID
     * @return The number of components in the image
     */
    public abstract int getImageComponents(int fieldID);

    /**
     * Return the height on an SFImage field
     * @param fieldID The network field ID
     * @return The height of the image
     */
    public abstract int getImageHeight(int fieldID);

    /**
     * Return the width of an SFImage field
     * @param fieldID The network field ID
     * @return The width of the image
     */
    public abstract int getImageWidth(int fieldID);
    
}