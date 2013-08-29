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

package org.xj3d.sai.internal.node.particlesystems;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.particlesystems.WindPhysicsModel;

/** A concrete implementation of the WindPhysicsModel node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIWindPhysicsModel extends BaseNode implements WindPhysicsModel {

/** The enabled inputOutput field */
private SFBool enabled;

/** The direction inputOutput field */
private SFVec3f direction;

/** The gustiness inputOutput field */
private SFFloat gustiness;

/** The turbulence inputOutput field */
private SFFloat turbulence;

/** The speed inputOutput field */
private SFFloat speed;

/** Constructor */ 
public SAIWindPhysicsModel ( 
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

/** Return the direction value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDirection(float[] val) {
  if ( direction == null ) { 
    direction = (SFVec3f)getField( "direction" ); 
  }
  direction.getValue( val );
}

/** Set the direction field. 
 * @param val The float[] to set.  */
public void setDirection(float[] val) {
  if ( direction == null ) { 
    direction = (SFVec3f)getField( "direction" ); 
  }
  direction.setValue( val );
}

/** Return the gustiness float value. 
 * @return The gustiness float value.  */
public float getGustiness() {
  if ( gustiness == null ) { 
    gustiness = (SFFloat)getField( "gustiness" ); 
  }
  return( gustiness.getValue( ) );
}

/** Set the gustiness field. 
 * @param val The float to set.  */
public void setGustiness(float val) {
  if ( gustiness == null ) { 
    gustiness = (SFFloat)getField( "gustiness" ); 
  }
  gustiness.setValue( val );
}

/** Return the turbulence float value. 
 * @return The turbulence float value.  */
public float getTurbulence() {
  if ( turbulence == null ) { 
    turbulence = (SFFloat)getField( "turbulence" ); 
  }
  return( turbulence.getValue( ) );
}

/** Set the turbulence field. 
 * @param val The float to set.  */
public void setTurbulence(float val) {
  if ( turbulence == null ) { 
    turbulence = (SFFloat)getField( "turbulence" ); 
  }
  turbulence.setValue( val );
}

/** Return the speed float value. 
 * @return The speed float value.  */
public float getSpeed() {
  if ( speed == null ) { 
    speed = (SFFloat)getField( "speed" ); 
  }
  return( speed.getValue( ) );
}

/** Set the speed field. 
 * @param val The float to set.  */
public void setSpeed(float val) {
  if ( speed == null ) { 
    speed = (SFFloat)getField( "speed" ); 
  }
  speed.setValue( val );
}

}
