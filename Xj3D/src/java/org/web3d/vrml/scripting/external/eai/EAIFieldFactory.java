package org.web3d.vrml.scripting.external.eai;

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

import vrml.eai.field.EventOut;
import vrml.eai.field.EventIn;

import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * EAIFieldFactory produces the EventOut and EventIn subclass instances.
 * EAIFieldFactory is used to construct the appropriate EventOut and
 * EventIn instances as needed either for translation from the underlying
 * field and event implementation, and in response to Node.getEventOut and
 * Node.getEventIn calls.
 * <P>
 * The same factory is used to produce both the synchronous and stored
 * versions of each field.
 *
 * @author Brad Vender
 * @version 1.0
 */

interface EAIFieldFactory {

    /** Produce an eventIn.
     *  @param vrmlNode The originating node
     *  @param eventName The eventIn name
     */
    public EventIn getEventIn(VRMLNodeType vrmlNode, String eventName) 
    throws vrml.eai.field.InvalidEventInException, 
    vrml.eai.InvalidNodeException;

    /** Produce an asynchronous eventOut.
     *  These eventOut's respond with the current field value when queried,
     *  as opposed to the field value when created.
     *  @param vrmlNode The originating node
     *  @param eventName The eventIn name
     */
    public EventOut getEventOut(VRMLNodeType vrmlNode, String eventName)
    throws vrml.eai.field.InvalidEventOutException, 
    vrml.eai.InvalidNodeException;

    /** Produce an asynchronous eventOut.
     *  These eventOut's respond with the current field value when queried,
     *  as opposed to the field value when created.
     *  @param vrmlNode The originating node
     *  @param fieldID The field ID
     *  @param eventName The field name (for error reporting)
    */
    public EventOut getEventOut(
        VRMLNodeType vrmlNode, int fieldID, String eventName
    ) throws vrml.eai.field.InvalidEventOutException, 
    vrml.eai.InvalidNodeException;

    /** Produce an stored eventOut.
     *  These eventOut's respond with the value of the field at the time
     *  of creation, rather than the current field value.
     *  Note that this method is mainly for use by the event
     *  propogation system, since it uses the underlying fieldID's rather
     *  than the String fieldNames.
     *  @param vrmlNode The originating node
     *  @param fieldID The field ID
     *  @param eventName The field name (for error reporting)
     */
    public EventOut getStoredEventOut(
         VRMLNodeType vrmlNode, int fieldID, String eventName
    ) throws vrml.eai.field.InvalidEventOutException, 
    vrml.eai.InvalidNodeException;

}
