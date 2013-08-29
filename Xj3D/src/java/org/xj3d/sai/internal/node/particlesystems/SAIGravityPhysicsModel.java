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
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.particlesystems.GravityPhysicsModel;

/** A concrete implementation of the GravityPhysicsModel node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIGravityPhysicsModel extends BaseNode implements GravityPhysicsModel {

/** The enabled inputOutput field */
private SFBool enabled;

/** The gravity inputOutput field */
private SFVec3f gravity;

/** Constructor */ 
public SAIGravityPhysicsModel ( 
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

/** Return the gravity value in the argument float[]
 * @param val The float[] to initialize.  */
public void getGravity(float[] val) {
  if ( gravity == null ) { 
    gravity = (SFVec3f)getField( "gravity" ); 
  }
  gravity.getValue( val );
}

/** Set the gravity field. 
 * @param val The float[] to set.  */
public void setGravity(float[] val) {
  if ( gravity == null ) { 
    gravity = (SFVec3f)getField( "gravity" ); 
  }
  gravity.setValue( val );
}

}
