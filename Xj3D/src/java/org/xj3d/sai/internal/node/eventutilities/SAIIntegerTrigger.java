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

package org.xj3d.sai.internal.node.eventutilities;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.eventutilities.IntegerTrigger;

/** A concrete implementation of the IntegerTrigger node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIIntegerTrigger extends BaseNode implements IntegerTrigger {

/** The set_boolean inputOnly field */
private SFBool set_boolean;

/** The integerKey inputOutput field */
private SFInt32 integerKey;

/** The triggerValue outputOnly field */
private SFInt32 triggerValue;

/** Constructor */ 
public SAIIntegerTrigger ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Set the set_boolean field. 
 * @param val The boolean to set.  */
public void setBoolean(boolean val) {
  if ( set_boolean == null ) { 
    set_boolean = (SFBool)getField( "set_boolean" ); 
  }
  set_boolean.setValue( val );
}

/** Return the integerKey int value. 
 * @return The integerKey int value.  */
public int getIntegerKey() {
  if ( integerKey == null ) { 
    integerKey = (SFInt32)getField( "integerKey" ); 
  }
  return( integerKey.getValue( ) );
}

/** Set the integerKey field. 
 * @param val The int to set.  */
public void setIntegerKey(int val) {
  if ( integerKey == null ) { 
    integerKey = (SFInt32)getField( "integerKey" ); 
  }
  integerKey.setValue( val );
}

/** Return the triggerValue int value. 
 * @return The triggerValue int value.  */
public int getTriggerValue() {
  if ( triggerValue == null ) { 
    triggerValue = (SFInt32)getField( "triggerValue" ); 
  }
  return( triggerValue.getValue( ) );
}

}
