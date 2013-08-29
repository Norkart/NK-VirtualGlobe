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

package org.xj3d.sai.internal.node.rigidbodyphysics;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.CollisionCollection;

/** A concrete implementation of the CollisionCollection node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICollisionCollection extends BaseNode implements CollisionCollection {

/** The collidables inputOutput field */
private MFNode collidables;

/** The enabled inputOutput field */
private SFBool enabled;

/** The bounce inputOutput field */
private SFFloat bounce;

/** The minBounceSpeed inputOutput field */
private SFFloat minBounceSpeed;

/** The frictionCoefficients inputOutput field */
private SFVec2f frictionCoefficients;

/** The slipCoefficients inputOutput field */
private SFVec2f slipCoefficients;

/** The surfaceSpeed inputOutput field */
private SFVec2f surfaceSpeed;

/** The appliedParameters inputOutput field */
private MFString appliedParameters;

/** The softnessConstantForceMix inputOutput field */
private SFFloat softnessConstantForceMix;

/** The softnessErrorCorrection inputOutput field */
private SFFloat softnessErrorCorrection;

/** Constructor */ 
public SAICollisionCollection ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFNode items in the collidables field. 
 * @return the number of MFNode items in the collidables field.  */
public int getNumCollidables() {
  if ( collidables == null ) { 
    collidables = (MFNode)getField( "collidables" ); 
  }
  return( collidables.getSize( ) );
}

/** Return the collidables value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getCollidables(X3DNode[] val) {
  if ( collidables == null ) { 
    collidables = (MFNode)getField( "collidables" ); 
  }
  collidables.getValue( val );
}

/** Set the collidables field. 
 * @param val The X3DNode[] to set.  */
public void setCollidables(X3DNode[] val) {
  if ( collidables == null ) { 
    collidables = (MFNode)getField( "collidables" ); 
  }
  collidables.setValue( val.length, val );
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

/** Return the bounce float value. 
 * @return The bounce float value.  */
public float getBounce() {
  if ( bounce == null ) { 
    bounce = (SFFloat)getField( "bounce" ); 
  }
  return( bounce.getValue( ) );
}

/** Set the bounce field. 
 * @param val The float to set.  */
public void setBounce(float val) {
  if ( bounce == null ) { 
    bounce = (SFFloat)getField( "bounce" ); 
  }
  bounce.setValue( val );
}

/** Return the minBounceSpeed float value. 
 * @return The minBounceSpeed float value.  */
public float getMinBounceSpeed() {
  if ( minBounceSpeed == null ) { 
    minBounceSpeed = (SFFloat)getField( "minBounceSpeed" ); 
  }
  return( minBounceSpeed.getValue( ) );
}

/** Set the minBounceSpeed field. 
 * @param val The float to set.  */
public void setMinBounceSpeed(float val) {
  if ( minBounceSpeed == null ) { 
    minBounceSpeed = (SFFloat)getField( "minBounceSpeed" ); 
  }
  minBounceSpeed.setValue( val );
}

/** Return the frictionCoefficients value in the argument float[]
 * @param val The float[] to initialize.  */
public void getFrictionCoefficients(float[] val) {
  if ( frictionCoefficients == null ) { 
    frictionCoefficients = (SFVec2f)getField( "frictionCoefficients" ); 
  }
  frictionCoefficients.getValue( val );
}

/** Set the frictionCoefficients field. 
 * @param val The float[] to set.  */
public void setFrictionCoefficients(float[] val) {
  if ( frictionCoefficients == null ) { 
    frictionCoefficients = (SFVec2f)getField( "frictionCoefficients" ); 
  }
  frictionCoefficients.setValue( val );
}

/** Return the slipCoefficients value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSlipCoefficients(float[] val) {
  if ( slipCoefficients == null ) { 
    slipCoefficients = (SFVec2f)getField( "slipCoefficients" ); 
  }
  slipCoefficients.getValue( val );
}

/** Set the slipCoefficients field. 
 * @param val The float[] to set.  */
public void setSlipCoefficients(float[] val) {
  if ( slipCoefficients == null ) { 
    slipCoefficients = (SFVec2f)getField( "slipCoefficients" ); 
  }
  slipCoefficients.setValue( val );
}

/** Return the surfaceSpeed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSurfaceSpeed(float[] val) {
  if ( surfaceSpeed == null ) { 
    surfaceSpeed = (SFVec2f)getField( "surfaceSpeed" ); 
  }
  surfaceSpeed.getValue( val );
}

/** Set the surfaceSpeed field. 
 * @param val The float[] to set.  */
public void setSurfaceSpeed(float[] val) {
  if ( surfaceSpeed == null ) { 
    surfaceSpeed = (SFVec2f)getField( "surfaceSpeed" ); 
  }
  surfaceSpeed.setValue( val );
}

/** Return the number of MFString items in the appliedParameters field. 
 * @return the number of MFString items in the appliedParameters field.  */
public int getNumAppliedParameters() {
  if ( appliedParameters == null ) { 
    appliedParameters = (MFString)getField( "appliedParameters" ); 
  }
  return( appliedParameters.getSize( ) );
}

/** Return the appliedParameters value in the argument String[]
 * @param val The String[] to initialize.  */
public void getAppliedParameters(String[] val) {
  if ( appliedParameters == null ) { 
    appliedParameters = (MFString)getField( "appliedParameters" ); 
  }
  appliedParameters.getValue( val );
}

/** Set the appliedParameters field. 
 * @param val The String[] to set.  */
public void setAppliedParameters(String[] val) {
  if ( appliedParameters == null ) { 
    appliedParameters = (MFString)getField( "appliedParameters" ); 
  }
  appliedParameters.setValue( val.length, val );
}

/** Return the softnessConstantForceMix float value. 
 * @return The softnessConstantForceMix float value.  */
public float getSoftnessConstantForceMix() {
  if ( softnessConstantForceMix == null ) { 
    softnessConstantForceMix = (SFFloat)getField( "softnessConstantForceMix" ); 
  }
  return( softnessConstantForceMix.getValue( ) );
}

/** Set the softnessConstantForceMix field. 
 * @param val The float to set.  */
public void setSoftnessConstantForceMix(float val) {
  if ( softnessConstantForceMix == null ) { 
    softnessConstantForceMix = (SFFloat)getField( "softnessConstantForceMix" ); 
  }
  softnessConstantForceMix.setValue( val );
}

/** Return the softnessErrorCorrection float value. 
 * @return The softnessErrorCorrection float value.  */
public float getSoftnessErrorCorrection() {
  if ( softnessErrorCorrection == null ) { 
    softnessErrorCorrection = (SFFloat)getField( "softnessErrorCorrection" ); 
  }
  return( softnessErrorCorrection.getValue( ) );
}

/** Set the softnessErrorCorrection field. 
 * @param val The float to set.  */
public void setSoftnessErrorCorrection(float val) {
  if ( softnessErrorCorrection == null ) { 
    softnessErrorCorrection = (SFFloat)getField( "softnessErrorCorrection" ); 
  }
  softnessErrorCorrection.setValue( val );
}

}
