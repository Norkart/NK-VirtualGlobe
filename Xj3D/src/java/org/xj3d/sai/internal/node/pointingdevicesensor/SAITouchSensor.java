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

package org.xj3d.sai.internal.node.pointingdevicesensor;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.pointingdevicesensor.TouchSensor;

/** A concrete implementation of the TouchSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITouchSensor extends BaseNode implements TouchSensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The hitNormal_changed outputOnly field */
private SFVec3f hitNormal_changed;

/** The hitPoint_changed outputOnly field */
private SFVec3f hitPoint_changed;

/** The hitTexCoord_changed outputOnly field */
private SFVec2f hitTexCoord_changed;

/** The isOver outputOnly field */
private SFBool isOver;

/** The touchTime outputOnly field */
private SFTime touchTime;

/** The description inputOutput field */
private SFString description;

/** Constructor */ 
public SAITouchSensor ( 
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

/** Return the hitNormal_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getHitNormal(float[] val) {
  if ( hitNormal_changed == null ) { 
    hitNormal_changed = (SFVec3f)getField( "hitNormal_changed" ); 
  }
  hitNormal_changed.getValue( val );
}

/** Return the hitPoint_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getHitPoint(float[] val) {
  if ( hitPoint_changed == null ) { 
    hitPoint_changed = (SFVec3f)getField( "hitPoint_changed" ); 
  }
  hitPoint_changed.getValue( val );
}

/** Return the hitTexCoord_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getHitTexCoord(float[] val) {
  if ( hitTexCoord_changed == null ) { 
    hitTexCoord_changed = (SFVec2f)getField( "hitTexCoord_changed" ); 
  }
  hitTexCoord_changed.getValue( val );
}

/** Return the isOver boolean value. 
 * @return The isOver boolean value.  */
public boolean getIsOver() {
  if ( isOver == null ) { 
    isOver = (SFBool)getField( "isOver" ); 
  }
  return( isOver.getValue( ) );
}

/** Return the touchTime double value. 
 * @return The touchTime double value.  */
public double getTouchTime() {
  if ( touchTime == null ) { 
    touchTime = (SFTime)getField( "touchTime" ); 
  }
  return( touchTime.getValue( ) );
}

/** Return the description String value. 
 * @return The description String value.  */
public String getDescription() {
  if ( description == null ) { 
    description = (SFString)getField( "description" ); 
  }
  return( description.getValue( ) );
}

/** Set the description field. 
 * @param val The String to set.  */
public void setDescription(String val) {
  if ( description == null ) { 
    description = (SFString)getField( "description" ); 
  }
  description.setValue( val );
}

}
