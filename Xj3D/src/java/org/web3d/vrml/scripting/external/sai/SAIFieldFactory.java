/***************************************************************************** 
 *                        Web3d.org Copyright (c) 2007 
 *                               Java Source 
 * 
 * This source is licensed under the GNU LGPL v2.1 
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information 
 * 
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it. 
 * 
 ****************************************************************************/ 

package org.web3d.vrml.scripting.external.sai;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.InvalidFieldException;
import org.web3d.x3d.sai.InvalidNodeException;

/**
 * SAIFieldFactory produces the field wrappers.
 * SAIFieldFactory is used to construct the appropriate X3DField
 * instances as needed either for translation from the underlying
 * field and event implementation, and in response to X3DNode.getField
 * calls.
 * <P>
 * The same factory is used to produce both the synchronous and stored
 * versions of each field.
 *
 * @author Brad Vender
 * @version 1.0
 */

public interface SAIFieldFactory {

    /** Produce an asynchronous field.
     *  These fields respond with the current field value when queried,
     *  as opposed to the field value when created.
     *  @param vrmlNode The originating node
     *  @param fieldID The field ID
     *  @param eventName The field name (for error reporting)
    */
    public X3DField getField(VRMLNodeType vrmlNode, int fieldID,
        String eventName
    ) throws InvalidFieldException, InvalidNodeException;

    /** Produce an asynchronous field.
     *  These fields respond with the current field value when queried,
     *  as opposed to the field value when created.
     *  @param vrmlNode The originating node
     *  @param eventName The name of the field
     */
    public X3DField getField(VRMLNodeType vrmlNode, String eventName) 
    throws InvalidFieldException, InvalidNodeException;

    /** Produce an stored field.
     *  These fields respond with the value of the field at the time
     *  of creation, rather than the current field value.
     *  Note that this method is mainly for use by the event
     *  propogation system, since it uses the underlying fieldID's rather
     *  than the String fieldNames.
     *  @param vrmlNode The originating node
     *  @param fieldID The field ID
     *  @param eventName The field name (for error reporting)
     *  @param isInput Is this an input or output buffer
     */
    public X3DField getStoredField(VRMLNodeType vrmlNode, int fieldID,
         String eventName, boolean isInput
    ) throws InvalidFieldException, InvalidNodeException;

}
