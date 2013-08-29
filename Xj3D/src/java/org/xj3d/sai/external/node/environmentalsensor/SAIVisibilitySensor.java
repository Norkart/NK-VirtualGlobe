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

package org.xj3d.sai.external.node.environmentalsensor;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.environmentalsensor.VisibilitySensor;

/** A concrete implementation of the VisibilitySensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIVisibilitySensor extends SAINode implements VisibilitySensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The center inputOutput field */
private SFVec3f center;

/** The size inputOutput field */
private SFVec3f size;

/** The enterTime outputOnly field */
private SFTime enterTime;

/** The exitTime outputOnly field */
private SFTime exitTime;

/** Constructor */ 
public SAIVisibilitySensor ( 
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
