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
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.DoubleAxisHingeJoint;

/** A concrete implementation of the DoubleAxisHingeJoint node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIDoubleAxisHingeJoint extends BaseNode implements DoubleAxisHingeJoint {

/** The forceOutput inputOutput field */
private MFString forceOutput;

/** The body1 inputOutput field */
private SFNode body1;

/** The body2 inputOutput field */
private SFNode body2;

/** The mustOutput inputOutput field */
private MFString mustOutput;

/** The anchorPoint inputOutput field */
private SFVec3f anchorPoint;

/** The axis1 inputOutput field */
private SFVec3f axis1;

/** The axis2 inputOutput field */
private SFVec3f axis2;

/** The body1AnchorPoint outputOnly field */
private SFVec3f body1AnchorPoint;

/** The body2AnchorPoint outputOnly field */
private SFVec3f body2AnchorPoint;

/** The desiredAngularVelocity1 inputOutput field */
private SFFloat desiredAngularVelocity1;

/** The desiredAngularVelocity2 inputOutput field */
private SFFloat desiredAngularVelocity2;

/** The body1Axis outputOnly field */
private SFVec3f body1Axis;

/** The body2Axis outputOnly field */
private SFVec3f body2Axis;

/** The minAngle1 inputOutput field */
private SFFloat minAngle1;

/** The maxAngle1 inputOutput field */
private SFFloat maxAngle1;

/** The maxTorque1 inputOutput field */
private SFFloat maxTorque1;

/** The maxTorque2 inputOutput field */
private SFFloat maxTorque2;

/** The hinge1Angle outputOnly field */
private SFFloat hinge1Angle;

/** The hinge2Angle outputOnly field */
private SFFloat hinge2Angle;

/** The hinge1AngleRate outputOnly field */
private SFFloat hinge1AngleRate;

/** The hinge2AngleRate outputOnly field */
private SFFloat hinge2AngleRate;

/** The stopBounce1 inputOutput field */
private SFFloat stopBounce1;

/** The stopErrorCorrection1 inputOutput field */
private SFFloat stopErrorCorrection1;

/** The stopConstantForceMix1 inputOutput field */
private SFFloat stopConstantForceMix1;

/** The suspensionForce inputOutput field */
private SFFloat suspensionForce;

/** The suspensionErrorCorrection inputOutput field */
private SFFloat suspensionErrorCorrection;

/** Constructor */ 
public SAIDoubleAxisHingeJoint ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFString items in the forceOutput field. 
 * @return the number of MFString items in the forceOutput field.  */
public int getNumForceOutput() {
  if ( forceOutput == null ) { 
    forceOutput = (MFString)getField( "forceOutput" ); 
  }
  return( forceOutput.getSize( ) );
}

/** Return the forceOutput value in the argument String[]
 * @param val The String[] to initialize.  */
public void getForceOutput(String[] val) {
  if ( forceOutput == null ) { 
    forceOutput = (MFString)getField( "forceOutput" ); 
  }
  forceOutput.getValue( val );
}

/** Set the forceOutput field. 
 * @param val The String[] to set.  */
public void setForceOutput(String[] val) {
  if ( forceOutput == null ) { 
    forceOutput = (MFString)getField( "forceOutput" ); 
  }
  forceOutput.setValue( val.length, val );
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

/** Return the number of MFString items in the mustOutput field. 
 * @return the number of MFString items in the mustOutput field.  */
public int getNumMustOutput() {
  if ( mustOutput == null ) { 
    mustOutput = (MFString)getField( "mustOutput" ); 
  }
  return( mustOutput.getSize( ) );
}

/** Return the mustOutput value in the argument String[]
 * @param val The String[] to initialize.  */
public void getMustOutput(String[] val) {
  if ( mustOutput == null ) { 
    mustOutput = (MFString)getField( "mustOutput" ); 
  }
  mustOutput.getValue( val );
}

/** Set the mustOutput field. 
 * @param val The String[] to set.  */
public void setMustOutput(String[] val) {
  if ( mustOutput == null ) { 
    mustOutput = (MFString)getField( "mustOutput" ); 
  }
  mustOutput.setValue( val.length, val );
}

/** Return the anchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAnchorPoint(float[] val) {
  if ( anchorPoint == null ) { 
    anchorPoint = (SFVec3f)getField( "anchorPoint" ); 
  }
  anchorPoint.getValue( val );
}

/** Set the anchorPoint field. 
 * @param val The float[] to set.  */
public void setAnchorPoint(float[] val) {
  if ( anchorPoint == null ) { 
    anchorPoint = (SFVec3f)getField( "anchorPoint" ); 
  }
  anchorPoint.setValue( val );
}

/** Return the axis1 value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAxis1(float[] val) {
  if ( axis1 == null ) { 
    axis1 = (SFVec3f)getField( "axis1" ); 
  }
  axis1.getValue( val );
}

/** Set the axis1 field. 
 * @param val The float[] to set.  */
public void setAxis1(float[] val) {
  if ( axis1 == null ) { 
    axis1 = (SFVec3f)getField( "axis1" ); 
  }
  axis1.setValue( val );
}

/** Return the axis2 value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAxis2(float[] val) {
  if ( axis2 == null ) { 
    axis2 = (SFVec3f)getField( "axis2" ); 
  }
  axis2.getValue( val );
}

/** Set the axis2 field. 
 * @param val The float[] to set.  */
public void setAxis2(float[] val) {
  if ( axis2 == null ) { 
    axis2 = (SFVec3f)getField( "axis2" ); 
  }
  axis2.setValue( val );
}

/** Return the body1AnchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody1AnchorPoint(float[] val) {
  if ( body1AnchorPoint == null ) { 
    body1AnchorPoint = (SFVec3f)getField( "body1AnchorPoint" ); 
  }
  body1AnchorPoint.getValue( val );
}

/** Return the body2AnchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody2AnchorPoint(float[] val) {
  if ( body2AnchorPoint == null ) { 
    body2AnchorPoint = (SFVec3f)getField( "body2AnchorPoint" ); 
  }
  body2AnchorPoint.getValue( val );
}

/** Return the desiredAngularVelocity1 float value. 
 * @return The desiredAngularVelocity1 float value.  */
public float getDesiredAngularVelocity1() {
  if ( desiredAngularVelocity1 == null ) { 
    desiredAngularVelocity1 = (SFFloat)getField( "desiredAngularVelocity1" ); 
  }
  return( desiredAngularVelocity1.getValue( ) );
}

/** Set the desiredAngularVelocity1 field. 
 * @param val The float to set.  */
public void setDesiredAngularVelocity1(float val) {
  if ( desiredAngularVelocity1 == null ) { 
    desiredAngularVelocity1 = (SFFloat)getField( "desiredAngularVelocity1" ); 
  }
  desiredAngularVelocity1.setValue( val );
}

/** Return the desiredAngularVelocity2 float value. 
 * @return The desiredAngularVelocity2 float value.  */
public float getDesiredAngularVelocity2() {
  if ( desiredAngularVelocity2 == null ) { 
    desiredAngularVelocity2 = (SFFloat)getField( "desiredAngularVelocity2" ); 
  }
  return( desiredAngularVelocity2.getValue( ) );
}

/** Set the desiredAngularVelocity2 field. 
 * @param val The float to set.  */
public void setDesiredAngularVelocity2(float val) {
  if ( desiredAngularVelocity2 == null ) { 
    desiredAngularVelocity2 = (SFFloat)getField( "desiredAngularVelocity2" ); 
  }
  desiredAngularVelocity2.setValue( val );
}

/** Return the body1Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody1Axis(float[] val) {
  if ( body1Axis == null ) { 
    body1Axis = (SFVec3f)getField( "body1Axis" ); 
  }
  body1Axis.getValue( val );
}

/** Return the body2Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody2Axis(float[] val) {
  if ( body2Axis == null ) { 
    body2Axis = (SFVec3f)getField( "body2Axis" ); 
  }
  body2Axis.getValue( val );
}

/** Return the minAngle1 float value. 
 * @return The minAngle1 float value.  */
public float getMinAngle1() {
  if ( minAngle1 == null ) { 
    minAngle1 = (SFFloat)getField( "minAngle1" ); 
  }
  return( minAngle1.getValue( ) );
}

/** Set the minAngle1 field. 
 * @param val The float to set.  */
public void setMinAngle1(float val) {
  if ( minAngle1 == null ) { 
    minAngle1 = (SFFloat)getField( "minAngle1" ); 
  }
  minAngle1.setValue( val );
}

/** Return the maxAngle1 float value. 
 * @return The maxAngle1 float value.  */
public float getMaxAngle1() {
  if ( maxAngle1 == null ) { 
    maxAngle1 = (SFFloat)getField( "maxAngle1" ); 
  }
  return( maxAngle1.getValue( ) );
}

/** Set the maxAngle1 field. 
 * @param val The float to set.  */
public void setMaxAngle1(float val) {
  if ( maxAngle1 == null ) { 
    maxAngle1 = (SFFloat)getField( "maxAngle1" ); 
  }
  maxAngle1.setValue( val );
}

/** Return the maxTorque1 float value. 
 * @return The maxTorque1 float value.  */
public float getMaxTorque1() {
  if ( maxTorque1 == null ) { 
    maxTorque1 = (SFFloat)getField( "maxTorque1" ); 
  }
  return( maxTorque1.getValue( ) );
}

/** Set the maxTorque1 field. 
 * @param val The float to set.  */
public void setMaxTorque1(float val) {
  if ( maxTorque1 == null ) { 
    maxTorque1 = (SFFloat)getField( "maxTorque1" ); 
  }
  maxTorque1.setValue( val );
}

/** Return the maxTorque2 float value. 
 * @return The maxTorque2 float value.  */
public float getMaxTorque2() {
  if ( maxTorque2 == null ) { 
    maxTorque2 = (SFFloat)getField( "maxTorque2" ); 
  }
  return( maxTorque2.getValue( ) );
}

/** Set the maxTorque2 field. 
 * @param val The float to set.  */
public void setMaxTorque2(float val) {
  if ( maxTorque2 == null ) { 
    maxTorque2 = (SFFloat)getField( "maxTorque2" ); 
  }
  maxTorque2.setValue( val );
}

/** Return the hinge1Angle float value. 
 * @return The hinge1Angle float value.  */
public float getHinge1Angle() {
  if ( hinge1Angle == null ) { 
    hinge1Angle = (SFFloat)getField( "hinge1Angle" ); 
  }
  return( hinge1Angle.getValue( ) );
}

/** Return the hinge2Angle float value. 
 * @return The hinge2Angle float value.  */
public float getHinge2Angle() {
  if ( hinge2Angle == null ) { 
    hinge2Angle = (SFFloat)getField( "hinge2Angle" ); 
  }
  return( hinge2Angle.getValue( ) );
}

/** Return the hinge1AngleRate float value. 
 * @return The hinge1AngleRate float value.  */
public float getHinge1AngleRate() {
  if ( hinge1AngleRate == null ) { 
    hinge1AngleRate = (SFFloat)getField( "hinge1AngleRate" ); 
  }
  return( hinge1AngleRate.getValue( ) );
}

/** Return the hinge2AngleRate float value. 
 * @return The hinge2AngleRate float value.  */
public float getHinge2AngleRate() {
  if ( hinge2AngleRate == null ) { 
    hinge2AngleRate = (SFFloat)getField( "hinge2AngleRate" ); 
  }
  return( hinge2AngleRate.getValue( ) );
}

/** Return the stopBounce1 float value. 
 * @return The stopBounce1 float value.  */
public float getStopBounce1() {
  if ( stopBounce1 == null ) { 
    stopBounce1 = (SFFloat)getField( "stopBounce1" ); 
  }
  return( stopBounce1.getValue( ) );
}

/** Set the stopBounce1 field. 
 * @param val The float to set.  */
public void setStopBounce1(float val) {
  if ( stopBounce1 == null ) { 
    stopBounce1 = (SFFloat)getField( "stopBounce1" ); 
  }
  stopBounce1.setValue( val );
}

/** Return the stopErrorCorrection1 float value. 
 * @return The stopErrorCorrection1 float value.  */
public float getStopErrorCorrection1() {
  if ( stopErrorCorrection1 == null ) { 
    stopErrorCorrection1 = (SFFloat)getField( "stopErrorCorrection1" ); 
  }
  return( stopErrorCorrection1.getValue( ) );
}

/** Set the stopErrorCorrection1 field. 
 * @param val The float to set.  */
public void setStopErrorCorrection1(float val) {
  if ( stopErrorCorrection1 == null ) { 
    stopErrorCorrection1 = (SFFloat)getField( "stopErrorCorrection1" ); 
  }
  stopErrorCorrection1.setValue( val );
}

/** Return the stopConstantForceMix1 float value. 
 * @return The stopConstantForceMix1 float value.  */
public float getStopConstantForceMix1() {
  if ( stopConstantForceMix1 == null ) { 
    stopConstantForceMix1 = (SFFloat)getField( "stopConstantForceMix1" ); 
  }
  return( stopConstantForceMix1.getValue( ) );
}

/** Set the stopConstantForceMix1 field. 
 * @param val The float to set.  */
public void setStopConstantForceMix1(float val) {
  if ( stopConstantForceMix1 == null ) { 
    stopConstantForceMix1 = (SFFloat)getField( "stopConstantForceMix1" ); 
  }
  stopConstantForceMix1.setValue( val );
}

/** Return the suspensionForce float value. 
 * @return The suspensionForce float value.  */
public float getSuspensionForce() {
  if ( suspensionForce == null ) { 
    suspensionForce = (SFFloat)getField( "suspensionForce" ); 
  }
  return( suspensionForce.getValue( ) );
}

/** Set the suspensionForce field. 
 * @param val The float to set.  */
public void setSuspensionForce(float val) {
  if ( suspensionForce == null ) { 
    suspensionForce = (SFFloat)getField( "suspensionForce" ); 
  }
  suspensionForce.setValue( val );
}

/** Return the suspensionErrorCorrection float value. 
 * @return The suspensionErrorCorrection float value.  */
public float getSuspensionErrorCorrection() {
  if ( suspensionErrorCorrection == null ) { 
    suspensionErrorCorrection = (SFFloat)getField( "suspensionErrorCorrection" ); 
  }
  return( suspensionErrorCorrection.getValue( ) );
}

/** Set the suspensionErrorCorrection field. 
 * @param val The float to set.  */
public void setSuspensionErrorCorrection(float val) {
  if ( suspensionErrorCorrection == null ) { 
    suspensionErrorCorrection = (SFFloat)getField( "suspensionErrorCorrection" ); 
  }
  suspensionErrorCorrection.setValue( val );
}

}
