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
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.particlesystems.ExplosionEmitter;

/** A concrete implementation of the ExplosionEmitter node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIExplosionEmitter extends BaseNode implements ExplosionEmitter {

/** The speed inputOutput field */
private SFFloat speed;

/** The mass initializeOnly field */
private SFFloat mass;

/** The surfaceArea initializeOnly field */
private SFFloat surfaceArea;

/** The variation inputOutput field */
private SFFloat variation;

/** The position inputOutput field */
private SFVec3f position;

/** Constructor */ 
public SAIExplosionEmitter ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the mass float value. 
 * @return The mass float value.  */
public float getMass() {
  if ( mass == null ) { 
    mass = (SFFloat)getField( "mass" ); 
  }
  return( mass.getValue( ) );
}

/** Set the mass field. 
 * @param val The float to set.  */
public void setMass(float val) {
  if ( mass == null ) { 
    mass = (SFFloat)getField( "mass" ); 
  }
  mass.setValue( val );
}

/** Return the surfaceArea float value. 
 * @return The surfaceArea float value.  */
public float getSurfaceArea() {
  if ( surfaceArea == null ) { 
    surfaceArea = (SFFloat)getField( "surfaceArea" ); 
  }
  return( surfaceArea.getValue( ) );
}

/** Set the surfaceArea field. 
 * @param val The float to set.  */
public void setSurfaceArea(float val) {
  if ( surfaceArea == null ) { 
    surfaceArea = (SFFloat)getField( "surfaceArea" ); 
  }
  surfaceArea.setValue( val );
}

/** Return the variation float value. 
 * @return The variation float value.  */
public float getVariation() {
  if ( variation == null ) { 
    variation = (SFFloat)getField( "variation" ); 
  }
  return( variation.getValue( ) );
}

/** Set the variation field. 
 * @param val The float to set.  */
public void setVariation(float val) {
  if ( variation == null ) { 
    variation = (SFFloat)getField( "variation" ); 
  }
  variation.setValue( val );
}

/** Return the position value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPosition(float[] val) {
  if ( position == null ) { 
    position = (SFVec3f)getField( "position" ); 
  }
  position.getValue( val );
}

/** Set the position field. 
 * @param val The float[] to set.  */
public void setPosition(float[] val) {
  if ( position == null ) { 
    position = (SFVec3f)getField( "position" ); 
  }
  position.setValue( val );
}

}
