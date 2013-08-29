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

package org.xj3d.sai.internal.node.environmentalsensor;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.environmentalsensor.ProximitySensor;

/** A concrete implementation of the ProximitySensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIProximitySensor extends BaseNode implements ProximitySensor {

/** The center inputOutput field */
private SFVec3f center;

/** The size inputOutput field */
private SFVec3f size;

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The position_changed outputOnly field */
private SFVec3f position_changed;

/** The orientation_changed outputOnly field */
private SFRotation orientation_changed;

/** The centerOfRotation_changed outputOnly field */
private SFVec3f centerOfRotation_changed;

/** The enterTime outputOnly field */
private SFTime enterTime;

/** The exitTime outputOnly field */
private SFTime exitTime;

/** Constructor */ 
public SAIProximitySensor ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the center value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenter(float[] val) {
  if ( center == null ) { 
    center = (SFVec3f)getField( "center" ); 
  }
  center.getValue( val );
}

/** Set the center field. 
 * @param val The float[] to set.  */
public void setCenter(float[] val) {
  if ( center == null ) { 
    center = (SFVec3f)getField( "center" ); 
  }
  center.setValue( val );
}

/** Return the size value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSize(float[] val) {
  if ( size == null ) { 
    size = (SFVec3f)getField( "size" ); 
  }
  size.getValue( val );
}

/** Set the size field. 
 * @param val The float[] to set.  */
public void setSize(float[] val) {
  if ( size == null ) { 
    size = (SFVec3f)getField( "size" ); 
  }
  size.setValue( val );
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

/** Return the position_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPosition(float[] val) {
  if ( position_changed == null ) { 
    position_changed = (SFVec3f)getField( "position_changed" ); 
  }
  position_changed.getValue( val );
}

/** Return the orientation_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getOrientation(float[] val) {
  if ( orientation_changed == null ) { 
    orientation_changed = (SFRotation)getField( "orientation_changed" ); 
  }
  orientation_changed.getValue( val );
}

/** Return the centerOfRotation_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenterOfRotation(float[] val) {
  if ( centerOfRotation_changed == null ) { 
    centerOfRotation_changed = (SFVec3f)getField( "centerOfRotation_changed" ); 
  }
  centerOfRotation_changed.getValue( val );
}

/** Return the enterTime double value. 
 * @return The enterTime double value.  */
public double getEnterTime() {
  if ( enterTime == null ) { 
    enterTime = (SFTime)getField( "enterTime" ); 
  }
  return( enterTime.getValue( ) );
}

/** Return the exitTime double value. 
 * @return The exitTime double value.  */
public double getExitTime() {
  if ( exitTime == null ) { 
    exitTime = (SFTime)getField( "exitTime" ); 
  }
  return( exitTime.getValue( ) );
}

}
