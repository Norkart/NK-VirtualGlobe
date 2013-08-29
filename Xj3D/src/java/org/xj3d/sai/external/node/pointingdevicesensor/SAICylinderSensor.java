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

package org.xj3d.sai.external.node.pointingdevicesensor;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.pointingdevicesensor.CylinderSensor;

/** A concrete implementation of the CylinderSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICylinderSensor extends SAINode implements CylinderSensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The autoOffset inputOutput field */
private SFBool autoOffset;

/** The trackPoint_changed outputOnly field */
private SFVec3f trackPoint_changed;

/** The description inputOutput field */
private SFString description;

/** The isOver outputOnly field */
private SFBool isOver;

/** The maxAngle inputOutput field */
private SFFloat maxAngle;

/** The minAngle inputOutput field */
private SFFloat minAngle;

/** The diskAngle inputOutput field */
private SFFloat diskAngle;

/** The rotation_changed outputOnly field */
private SFRotation rotation_changed;

/** The offset inputOutput field */
private SFFloat offset;

/** Constructor */ 
public SAICylinderSensor ( 
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

/** Return the autoOffset boolean value. 
 * @return The autoOffset boolean value.  */
public boolean getAutoOffset() {
  if ( autoOffset == null ) { 
    autoOffset = (SFBool)getField( "autoOffset" ); 
  }
  return( autoOffset.getValue( ) );
}

/** Set the autoOffset field. 
 * @param val The boolean to set.  */
public void setAutoOffset(boolean val) {
  if ( autoOffset == null ) { 
    autoOffset = (SFBool)getField( "autoOffset" ); 
  }
  autoOffset.setValue( val );
}

/** Return the trackPoint_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTrackPoint(float[] val) {
  if ( trackPoint_changed == null ) { 
    trackPoint_changed = (SFVec3f)getField( "trackPoint_changed" ); 
  }
  trackPoint_changed.getValue( val );
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

/** Return the isOver boolean value. 
 * @return The isOver boolean value.  */
public boolean getIsOver() {
  if ( isOver == null ) { 
    isOver = (SFBool)getField( "isOver" ); 
  }
  return( isOver.getValue( ) );
}

/** Return the maxAngle float value. 
 * @return The maxAngle float value.  */
public float getMaxAngle() {
  if ( maxAngle == null ) { 
    maxAngle = (SFFloat)getField( "maxAngle" ); 
  }
  return( maxAngle.getValue( ) );
}

/** Set the maxAngle field. 
 * @param val The float to set.  */
public void setMaxAngle(float val) {
  if ( maxAngle == null ) { 
    maxAngle = (SFFloat)getField( "maxAngle" ); 
  }
  maxAngle.setValue( val );
}

/** Return the minAngle float value. 
 * @return The minAngle float value.  */
public float getMinAngle() {
  if ( minAngle == null ) { 
    minAngle = (SFFloat)getField( "minAngle" ); 
  }
  return( minAngle.getValue( ) );
}

/** Set the minAngle field. 
 * @param val The float to set.  */
public void setMinAngle(float val) {
  if ( minAngle == null ) { 
    minAngle = (SFFloat)getField( "minAngle" ); 
  }
  minAngle.setValue( val );
}

/** Return the diskAngle float value. 
 * @return The diskAngle float value.  */
public float getDiskAngle() {
  if ( diskAngle == null ) { 
    diskAngle = (SFFloat)getField( "diskAngle" ); 
  }
  return( diskAngle.getValue( ) );
}

/** Set the diskAngle field. 
 * @param val The float to set.  */
public void setDiskAngle(float val) {
  if ( diskAngle == null ) { 
    diskAngle = (SFFloat)getField( "diskAngle" ); 
  }
  diskAngle.setValue( val );
}

/** Return the rotation_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getRotation(float[] val) {
  if ( rotation_changed == null ) { 
    rotation_changed = (SFRotation)getField( "rotation_changed" ); 
  }
  rotation_changed.getValue( val );
}

/** Return the offset float value. 
 * @return The offset float value.  */
public float getOffset() {
  if ( offset == null ) { 
    offset = (SFFloat)getField( "offset" ); 
  }
  return( offset.getValue( ) );
}

/** Set the offset field. 
 * @param val The float to set.  */
public void setOffset(float val) {
  if ( offset == null ) { 
    offset = (SFFloat)getField( "offset" ); 
  }
  offset.setValue( val );
}

}
