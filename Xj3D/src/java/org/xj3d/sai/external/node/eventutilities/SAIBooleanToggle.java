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
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.eventutilities.BooleanToggle;

/** A concrete implementation of the BooleanToggle node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIBooleanToggle extends SAINode implements BooleanToggle {

/** The set_boolean inputOnly field */
private SFBool set_boolean;

/** The toggle inputOutput field */
private SFBool toggle;

/** Constructor */ 
public SAIBooleanToggle ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Set the set_boolean field. 
 * @param val The boolean to set.  */
public void setBoolean(boolean val) {
  if ( set_boolean == null ) { 
    set_boolean = (SFBool)getField( "set_boolean" ); 
  }
  set_boolean.setValue( val );
}

/** Return the toggle boolean value. 
 * @return The toggle boolean value.  */
public boolean getToggle() {
  if ( toggle == null ) { 
    toggle = (SFBool)getField( "toggle" ); 
  }
  return( toggle.getValue( ) );
}

/** Set the toggle field. 
 * @param val The boolean to set.  */
public void setToggle(boolean val) {
  if ( toggle == null ) { 
    toggle = (SFBool)getField( "toggle" ); 
  }
  toggle.setValue( val );
}

}
