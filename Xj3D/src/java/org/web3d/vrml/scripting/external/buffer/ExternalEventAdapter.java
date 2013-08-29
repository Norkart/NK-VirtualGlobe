package org.web3d.vrml.scripting.external.buffer;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

/**
 * ExternalEventAdapter is an adapter between the underlying event model and
 * the EAI or SAI callbacks.  The particular details are handled by the
 * appropriate implementing classes.
 * <P>
 * The purpose of having this interface is to support having the event
 * changed callbacks occuring in the same thread as the event model, or
 * occuring outside the event model's thread.
 * <P>
*/
public interface ExternalEventAdapter {

    /** Add a listener for one of the fields of this node.
     *  Implementors are allowed to restrict and enforce that
     *  the event listener conforms to various interfaces.
     *  @param fieldID The ID of the field.
     *  @param listener The listener to add.
     */
    public void addListener(int fieldID, Object listener);

    /** Broadcast an eventOutChanged event for a given field.
     * @param fieldID The field which changed.
     * @param timestamp When the change occurred.
     */
    public void generateBroadcast(int fieldID, double timestamp);

    /** Remove a listener for one of the fields of this node.
     *  Implementors are allowed to restrict and enforce that the
     *  event listener conforms to various interfaces.
     * @param fieldID The ID of the field.
     * @param listener The listener to remove.
     */
    public void removeListener(int fieldID, Object listener);


}

