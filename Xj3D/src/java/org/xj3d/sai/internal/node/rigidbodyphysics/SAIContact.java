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
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNBodyCollidableNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.rigidbodyphysics.Contact;

/** A concrete implementation of the Contact node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIContact extends BaseNode implements Contact {

/** The appliedParameters inputOutput field */
private MFString appliedParameters;

/** The body1 inputOutput field */
private SFNode body1;

/** The body2 inputOutput field */
private SFNode body2;

/** The bounce inputOutput field */
private SFFloat bounce;

/** The contactNormal inputOutput field */
private SFVec3f contactNormal;

/** The depth inputOutput field */
private SFFloat depth;

/** The frictionCoefficients inputOutput field */
private SFVec2f frictionCoefficients;

/** The frictionDirection inputOutput field */
private SFVec3f frictionDirection;

/** The geometry1 inputOutput field */
private SFNode geometry1;

/** The geometry2 inputOutput field */
private SFNode geometry2;

/** The minBounceSpeed inputOutput field */
private SFFloat minBounceSpeed;

/** The position inputOutput field */
private SFVec3f position;

/** The slipCoefficients inputOutput field */
private SFVec2f slipCoefficients;

/** The surfaceSpeed inputOutput field */
private SFVec2f surfaceSpeed;

/** The softnessConstantForceMix inputOutput field */
private SFFloat softnessConstantForceMix;

/** The softnessErrorCorrection inputOutput field */
private SFFloat softnessErrorCorrection;

/** Constructor */ 
public SAIContact ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the body1 X3DNode value. 
 * @return The body1 X3DNode value.  */
public X3DNode getBody1() {
  if ( body1 == null ) { 
    body1 = (SFNode)getField( "body1" ); 
  }
  return( body1.getValue( ) );
}

/** Set the body1 field. 
 * @param val The X3DNode to set.  */
public void setBody1(X3DNode val) {
  if ( body1 == null ) { 
    body1 = (SFNode)getField( "body1" ); 
  }
  body1.setValue( val );
}

/** Return the body2 X3DNode value. 
 * @return The body2 X3DNode value.  */
public X3DNode getBody2() {
  if ( body2 == null ) { 
    body2 = (SFNode)getField( "body2" ); 
  }
  return( body2.getValue( ) );
}

/** Set the body2 field. 
 * @param val The X3DNode to set.  */
public void setBody2(X3DNode val) {
  if ( body2 == null ) { 
    body2 = (SFNode)getField( "body2" ); 
  }
  body2.setValue( val );
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

/** Return the contactNormal value in the argument float[]
 * @param val The float[] to initialize.  */
public void getContactNormal(float[] val) {
  if ( contactNormal == null ) { 
    contactNormal = (SFVec3f)getField( "contactNormal" ); 
  }
  contactNormal.getValue( val );
}

/** Set the contactNormal field. 
 * @param val The float[] to set.  */
public void setContactNormal(float[] val) {
  if ( contactNormal == null ) { 
    contactNormal = (SFVec3f)getField( "contactNormal" ); 
  }
  contactNormal.setValue( val );
}

/** Return the depth float value. 
 * @return The depth float value.  */
public float getDepth() {
  if ( depth == null ) { 
    depth = (SFFloat)getField( "depth" ); 
  }
  return( depth.getValue( ) );
}

/** Set the depth field. 
 * @param val The float to set.  */
public void setDepth(float val) {
  if ( depth == null ) { 
    depth = (SFFloat)getField( "depth" ); 
  }
  depth.setValue( val );
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

/** Return the frictionDirection value in the argument float[]
 * @param val The float[] to initialize.  */
public void getFrictionDirection(float[] val) {
  if ( frictionDirection == null ) { 
    frictionDirection = (SFVec3f)getField( "frictionDirection" ); 
  }
  frictionDirection.getValue( val );
}

/** Set the frictionDirection field. 
 * @param val The float[] to set.  */
public void setFrictionDirection(float[] val) {
  if ( frictionDirection == null ) { 
    frictionDirection = (SFVec3f)getField( "frictionDirection" ); 
  }
  frictionDirection.setValue( val );
}

/** Return the geometry1 X3DNode value. 
 * @return The geometry1 X3DNode value.  */
public X3DNode getGeometry1() {
  if ( geometry1 == null ) { 
    geometry1 = (SFNode)getField( "geometry1" ); 
  }
  return( geometry1.getValue( ) );
}

/** Set the geometry1 field. 
 * @param val The X3DNBodyCollidableNode to set.  */
public void setGeometry1(X3DNBodyCollidableNode val) {
  if ( geometry1 == null ) { 
    geometry1 = (SFNode)getField( "geometry1" ); 
  }
  geometry1.setValue( val );
}

/** Set the geometry1 field. 
 * @param val The X3DProtoInstance to set.  */
public void setGeometry1(X3DProtoInstance val) {
  if ( geometry1 == null ) { 
    geometry1 = (SFNode)getField( "geometry1" ); 
  }
  geometry1.setValue( val );
}

/** Return the geometry2 X3DNode value. 
 * @return The geometry2 X3DNode value.  */
public X3DNode getGeometry2() {
  if ( geometry2 == null ) { 
    geometry2 = (SFNode)getField( "geometry2" ); 
  }
  return( geometry2.getValue( ) );
}

/** Set the geometry2 field. 
 * @param val The X3DNBodyCollidableNode to set.  */
public void setGeometry2(X3DNBodyCollidableNode val) {
  if ( geometry2 == null ) { 
    geometry2 = (SFNode)getField( "geometry2" ); 
  }
  geometry2.setValue( val );
}

/** Set the geometry2 field. 
 * @param val The X3DProtoInstance to set.  */
public void setGeometry2(X3DProtoInstance val) {
  if ( geometry2 == null ) { 
    geometry2 = (SFNode)getField( "geometry2" ); 
  }
  geometry2.setValue( val );
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
