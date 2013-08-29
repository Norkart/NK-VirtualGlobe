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

package org.xj3d.sai.internal.node.keydevicesensor;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.keydevicesensor.StringSensor;

/** A concrete implementation of the StringSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIStringSensor extends BaseNode implements StringSensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The deletionAllowed inputOutput field */
private SFBool deletionAllowed;

/** The enteredText outputOnly field */
private SFString enteredText;

/** The finalText outputOnly field */
private SFString finalText;

/** Constructor */ 
public SAIStringSensor ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the deletionAllowed boolean value. 
 * @return The deletionAllowed boolean value.  */
public boolean getDeletionAllowed() {
  if ( deletionAllowed == null ) { 
    deletionAllowed = (SFBool)getField( "deletionAllowed" ); 
  }
  return( deletionAllowed.getValue( ) );
}

/** Set the deletionAllowed field. 
 * @param val The boolean to set.  */
public void setDeletionAllowed(boolean val) {
  if ( deletionAllowed == null ) { 
    deletionAllowed = (SFBool)getField( "deletionAllowed" ); 
  }
  deletionAllowed.setValue( val );
}

/** Return the enteredText String value. 
 * @return The enteredText String value.  */
public String getEnteredText() {
  if ( enteredText == null ) { 
    enteredText = (SFString)getField( "enteredText" ); 
  }
  return( enteredText.getValue( ) );
}

/** Return the finalText String value. 
 * @return The finalText String value.  */
public String getFinalText() {
  if ( finalText == null ) { 
    finalText = (SFString)getField( "finalText" ); 
  }
  return( finalText.getValue( ) );
}

}
