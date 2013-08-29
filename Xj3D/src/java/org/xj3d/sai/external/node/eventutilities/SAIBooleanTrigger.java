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

package org.xj3d.sai.external.node.eventutilities;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.eventutilities.BooleanTrigger;

/** A concrete implementation of the BooleanTrigger node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIBooleanTrigger extends SAINode implements BooleanTrigger {

/** The set_triggerTime inputOnly field */
private SFTime set_triggerTime;

/** The triggerTrue outputOnly field */
private SFBool triggerTrue;

/** Constructor */ 
public SAIBooleanTrigger ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Set the set_triggerTime field. 
 * @param val The double to set.  */
public void setTriggerTime(double val) {
  if ( set_triggerTime == null ) { 
    set_triggerTime = (SFTime)getField( "set_triggerTime" ); 
  }
  set_triggerTime.setValue( val );
}

/** Return the triggerTrue boolean value. 
 * @return The triggerTrue boolean value.  */
public boolean getTriggerTrue() {
  if ( triggerTrue == null ) { 
    triggerTrue = (SFBool)getField( "triggerTrue" ); 
  }
  return( triggerTrue.getValue( ) );
}

}
