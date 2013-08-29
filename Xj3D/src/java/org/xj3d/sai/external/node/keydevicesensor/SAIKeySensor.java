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

package org.xj3d.sai.external.node.keydevicesensor;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.keydevicesensor.KeySensor;

/** A concrete implementation of the KeySensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIKeySensor extends SAINode implements KeySensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The keyPress outputOnly field */
private SFString keyPress;

/** The keyRelease outputOnly field */
private SFString keyRelease;

/** The actionKeyPress outputOnly field */
private SFInt32 actionKeyPress;

/** The actionKeyRelease outputOnly field */
private SFInt32 actionKeyRelease;

/** The shiftKey outputOnly field */
private SFBool shiftKey;

/** The controlKey outputOnly field */
private SFBool controlKey;

/** The altKey outputOnly field */
private SFBool altKey;

/** Constructor */ 
public SAIKeySensor ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the enabled boolean value. 
 * @return The enabled boolean value.  */
public boolean getEnabled() {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  return( enabled.getValue( ) );
}

/** Set the enabled field. 
 * @param val The boolean to set.  */
public void setEnabled(boolean val) {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  enabled.setValue( val );
}

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the keyPress String value. 
 * @return The keyPress String value.  */
public String getKeyPress() {
  if ( keyPress == null ) { 
    keyPress = (SFString)getField( "keyPress" ); 
  }
  return( keyPress.getValue( ) );
}

/** Return the keyRelease String value. 
 * @return The keyRelease String value.  */
public String getKeyRelease() {
  if ( keyRelease == null ) { 
    keyRelease = (SFString)getField( "keyRelease" ); 
  }
  return( keyRelease.getValue( ) );
}

/** Return the actionKeyPress int value. 
 * @return The actionKeyPress int value.  */
public int getActionKeyPress() {
  if ( actionKeyPress == null ) { 
    actionKeyPress = (SFInt32)getField( "actionKeyPress" ); 
  }
  return( actionKeyPress.getValue( ) );
}

/** Return the actionKeyRelease int value. 
 * @return The actionKeyRelease int value.  */
public int getActionKeyRelease() {
  if ( actionKeyRelease == null ) { 
    actionKeyRelease = (SFInt32)getField( "actionKeyRelease" ); 
  }
  return( actionKeyRelease.getValue( ) );
}

/** Return the shiftKey boolean value. 
 * @return The shiftKey boolean value.  */
public boolean getShiftKey() {
  if ( shiftKey == null ) { 
    shiftKey = (SFBool)getField( "shiftKey" ); 
  }
  return( shiftKey.getValue( ) );
}

/** Return the controlKey boolean value. 
 * @return The controlKey boolean value.  */
public boolean getControlKey() {
  if ( controlKey == null ) { 
    controlKey = (SFBool)getField( "controlKey" ); 
  }
  return( controlKey.getValue( ) );
}

/** Return the altKey boolean value. 
 * @return The altKey boolean value.  */
public boolean getAltKey() {
  if ( altKey == null ) { 
    altKey = (SFBool)getField( "altKey" ); 
  }
  return( altKey.getValue( ) );
}

}
