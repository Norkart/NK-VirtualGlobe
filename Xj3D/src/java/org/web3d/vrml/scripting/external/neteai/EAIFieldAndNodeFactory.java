package org.web3d.vrml.scripting.external.neteai;

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

import java.io.DataInputStream;
import java.io.IOException;

import vrml.eai.Node;
import vrml.eai.field.EventOut;
import vrml.eai.field.EventIn;

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

interface EAIFieldAndNodeFactory {

    /** Construct a node wrapper for a node.
     * @param nodeID The network node ID
     * @param nodeName The type name of the node
     * @return the new node
     */
    public Node createNode(int nodeID);
    
    /** Produce an eventIn.
     *  @param fieldID The network ID of this field
     *  @param type The type of this field (see vrml.eai.field.BaseField)
     *  @param eventName The eventIn name
     */
    public EventIn generateEventIn(int fieldID, int FieldType);

   /** Produce an asynchronous eventOut.
     *  These eventOut's respond with the current field value when queried,
     *  as opposed to the field value when created.
     *  @param fieldID The network ID of this field
     *  @param type The type of this field (see vrml.eai.field.BaseField)
     *  @param eventName The eventIn name
    */
    public EventOut getEventOut(
            int fieldID, int fieldType
    );

    /** Calculate the network ID of a node
     * @param node The node
     * @return The network ID
     */
    public int getNodeID(Node node);
    
    /** Produce an stored eventOut.
     *  These eventOut's respond with the value of the field at the time
     *  of creation, rather than the current field value.
     *  Note that this method is mainly for use by the event
     *  propogation system, since it uses the underlying fieldID's rather
     *  than the String fieldNames.
     *  @param fieldID The network ID of this field
     *  @param type The type of this field (see vrml.eai.field.BaseField)
     *  @param eventName The eventIn name
     *  @param source The stream containing field value
     * @throws IOException
     */
    public EventOut getStoredEventOut(
            int fieldID, int fieldType, DataInputStream source
    ) throws IOException;

}
