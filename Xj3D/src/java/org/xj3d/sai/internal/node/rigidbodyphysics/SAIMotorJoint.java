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
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.MotorJoint;

/** A concrete implementation of the MotorJoint node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIMotorJoint extends BaseNode implements MotorJoint {

/** The forceOutput inputOutput field */
private MFString forceOutput;

/** The body1 inputOutput field */
private SFNode body1;

/** The body2 inputOutput field */
private SFNode body2;

/** The autoCalc initializeOnly field */
private SFBool autoCalc;

/** The motor1Axis inputOutput field */
private SFVec3f motor1Axis;

/** The motor2Axis inputOutput field */
private SFVec3f motor2Axis;

/** The motor3Axis inputOutput field */
private SFVec3f motor3Axis;

/** The axis1Angle inputOutput field */
private SFFloat axis1Angle;

/** The axis2Angle inputOutput field */
private SFFloat axis2Angle;

/** The axis3Angle inputOutput field */
private SFFloat axis3Angle;

/** The stop1Bounce inputOutput field */
private SFFloat stop1Bounce;

/** The stop2Bounce inputOutput field */
private SFFloat stop2Bounce;

/** The stop3Bounce inputOutput field */
private SFFloat stop3Bounce;

/** The axis1Torque inputOutput field */
private SFFloat axis1Torque;

/** The axis2Torque inputOutput field */
private SFFloat axis2Torque;

/** The axis3Torque inputOutput field */
private SFFloat axis3Torque;

/** The motor1Angle outputOnly field */
private SFFloat motor1Angle;

/** The motor2Angle outputOnly field */
private SFFloat motor2Angle;

/** The motor3Angle outputOnly field */
private SFFloat motor3Angle;

/** The motor1AngleRate outputOnly field */
private SFFloat motor1AngleRate;

/** The motor2AngleRate outputOnly field */
private SFFloat motor2AngleRate;

/** The motor3AngleRate outputOnly field */
private SFFloat motor3AngleRate;

/** The stop1ErrorCorrection inputOutput field */
private SFFloat stop1ErrorCorrection;

/** The stop2ErrorCorrection inputOutput field */
private SFFloat stop2ErrorCorrection;

/** The stop3ErrorCorrection inputOutput field */
private SFFloat stop3ErrorCorrection;

/** The enabledAxes inputOutput field */
private SFInt32 enabledAxes;

/** Constructor */ 
public SAIMotorJoint ( 
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

/** Return the autoCalc boolean value. 
 * @return The autoCalc boolean value.  */
public boolean getAutoCalc() {
  if ( autoCalc == null ) { 
    autoCalc = (SFBool)getField( "autoCalc" ); 
  }
  return( autoCalc.getValue( ) );
}

/** Set the autoCalc field. 
 * @param val The boolean to set.  */
public void setAutoCalc(boolean val) {
  if ( autoCalc == null ) { 
    autoCalc = (SFBool)getField( "autoCalc" ); 
  }
  autoCalc.setValue( val );
}

/** Return the motor1Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMotor1Axis(float[] val) {
  if ( motor1Axis == null ) { 
    motor1Axis = (SFVec3f)getField( "motor1Axis" ); 
  }
  motor1Axis.getValue( val );
}

/** Set the motor1Axis field. 
 * @param val The float[] to set.  */
public void setMotor1Axis(float[] val) {
  if ( motor1Axis == null ) { 
    motor1Axis = (SFVec3f)getField( "motor1Axis" ); 
  }
  motor1Axis.setValue( val );
}

/** Return the motor2Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMotor2Axis(float[] val) {
  if ( motor2Axis == null ) { 
    motor2Axis = (SFVec3f)getField( "motor2Axis" ); 
  }
  motor2Axis.getValue( val );
}

/** Set the motor2Axis field. 
 * @param val The float[] to set.  */
public void setMotor2Axis(float[] val) {
  if ( motor2Axis == null ) { 
    motor2Axis = (SFVec3f)getField( "motor2Axis" ); 
  }
  motor2Axis.setValue( val );
}

/** Return the motor3Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMotor3Axis(float[] val) {
  if ( motor3Axis == null ) { 
    motor3Axis = (SFVec3f)getField( "motor3Axis" ); 
  }
  motor3Axis.getValue( val );
}

/** Set the motor3Axis field. 
 * @param val The float[] to set.  */
public void setMotor3Axis(float[] val) {
  if ( motor3Axis == null ) { 
    motor3Axis = (SFVec3f)getField( "motor3Axis" ); 
  }
  motor3Axis.setValue( val );
}

/** Return the axis1Angle float value. 
 * @return The axis1Angle float value.  */
public float getAxis1Angle() {
  if ( axis1Angle == null ) { 
    axis1Angle = (SFFloat)getField( "axis1Angle" ); 
  }
  return( axis1Angle.getValue( ) );
}

/** Set the axis1Angle field. 
 * @param val The float to set.  */
public void setAxis1Angle(float val) {
  if ( axis1Angle == null ) { 
    axis1Angle = (SFFloat)getField( "axis1Angle" ); 
  }
  axis1Angle.setValue( val );
}

/** Return the axis2Angle float value. 
 * @return The axis2Angle float value.  */
public float getAxis2Angle() {
  if ( axis2Angle == null ) { 
    axis2Angle = (SFFloat)getField( "axis2Angle" ); 
  }
  return( axis2Angle.getValue( ) );
}

/** Set the axis2Angle field. 
 * @param val The float to set.  */
public void setAxis2Angle(float val) {
  if ( axis2Angle == null ) { 
    axis2Angle = (SFFloat)getField( "axis2Angle" ); 
  }
  axis2Angle.setValue( val );
}

/** Return the axis3Angle float value. 
 * @return The axis3Angle float value.  */
public float getAxis3Angle() {
  if ( axis3Angle == null ) { 
    axis3Angle = (SFFloat)getField( "axis3Angle" ); 
  }
  return( axis3Angle.getValue( ) );
}

/** Set the axis3Angle field. 
 * @param val The float to set.  */
public void setAxis3Angle(float val) {
  if ( axis3Angle == null ) { 
    axis3Angle = (SFFloat)getField( "axis3Angle" ); 
  }
  axis3Angle.setValue( val );
}

/** Return the stop1Bounce float value. 
 * @return The stop1Bounce float value.  */
public float getStop1Bounce() {
  if ( stop1Bounce == null ) { 
    stop1Bounce = (SFFloat)getField( "stop1Bounce" ); 
  }
  return( stop1Bounce.getValue( ) );
}

/** Set the stop1Bounce field. 
 * @param val The float to set.  */
public void setStop1Bounce(float val) {
  if ( stop1Bounce == null ) { 
    stop1Bounce = (SFFloat)getField( "stop1Bounce" ); 
  }
  stop1Bounce.setValue( val );
}

/** Return the stop2Bounce float value. 
 * @return The stop2Bounce float value.  */
public float getStop2Bounce() {
  if ( stop2Bounce == null ) { 
    stop2Bounce = (SFFloat)getField( "stop2Bounce" ); 
  }
  return( stop2Bounce.getValue( ) );
}

/** Set the stop2Bounce field. 
 * @param val The float to set.  */
public void setStop2Bounce(float val) {
  if ( stop2Bounce == null ) { 
    stop2Bounce = (SFFloat)getField( "stop2Bounce" ); 
  }
  stop2Bounce.setValue( val );
}

/** Return the stop3Bounce float value. 
 * @return The stop3Bounce float value.  */
public float getStop3Bounce() {
  if ( stop3Bounce == null ) { 
    stop3Bounce = (SFFloat)getField( "stop3Bounce" ); 
  }
  return( stop3Bounce.getValue( ) );
}

/** Set the stop3Bounce field. 
 * @param val The float to set.  */
public void setStop3Bounce(float val) {
  if ( stop3Bounce == null ) { 
    stop3Bounce = (SFFloat)getField( "stop3Bounce" ); 
  }
  stop3Bounce.setValue( val );
}

/** Return the axis1Torque float value. 
 * @return The axis1Torque float value.  */
public float getAxis1Torque() {
  if ( axis1Torque == null ) { 
    axis1Torque = (SFFloat)getField( "axis1Torque" ); 
  }
  return( axis1Torque.getValue( ) );
}

/** Set the axis1Torque field. 
 * @param val The float to set.  */
public void setAxis1Torque(float val) {
  if ( axis1Torque == null ) { 
    axis1Torque = (SFFloat)getField( "axis1Torque" ); 
  }
  axis1Torque.setValue( val );
}

/** Return the axis2Torque float value. 
 * @return The axis2Torque float value.  */
public float getAxis2Torque() {
  if ( axis2Torque == null ) { 
    axis2Torque = (SFFloat)getField( "axis2Torque" ); 
  }
  return( axis2Torque.getValue( ) );
}

/** Set the axis2Torque field. 
 * @param val The float to set.  */
public void setAxis2Torque(float val) {
  if ( axis2Torque == null ) { 
    axis2Torque = (SFFloat)getField( "axis2Torque" ); 
  }
  axis2Torque.setValue( val );
}

/** Return the axis3Torque float value. 
 * @return The axis3Torque float value.  */
public float getAxis3Torque() {
  if ( axis3Torque == null ) { 
    axis3Torque = (SFFloat)getField( "axis3Torque" ); 
  }
  return( axis3Torque.getValue( ) );
}

/** Set the axis3Torque field. 
 * @param val The float to set.  */
public void setAxis3Torque(float val) {
  if ( axis3Torque == null ) { 
    axis3Torque = (SFFloat)getField( "axis3Torque" ); 
  }
  axis3Torque.setValue( val );
}

/** Return the motor1Angle float value. 
 * @return The motor1Angle float value.  */
public float getMotor1Angle() {
  if ( motor1Angle == null ) { 
    motor1Angle = (SFFloat)getField( "motor1Angle" ); 
  }
  return( motor1Angle.getValue( ) );
}

/** Return the motor2Angle float value. 
 * @return The motor2Angle float value.  */
public float getMotor2Angle() {
  if ( motor2Angle == null ) { 
    motor2Angle = (SFFloat)getField( "motor2Angle" ); 
  }
  return( motor2Angle.getValue( ) );
}

/** Return the motor3Angle float value. 
 * @return The motor3Angle float value.  */
public float getMotor3Angle() {
  if ( motor3Angle == null ) { 
    motor3Angle = (SFFloat)getField( "motor3Angle" ); 
  }
  return( motor3Angle.getValue( ) );
}

/** Return the motor1AngleRate float value. 
 * @return The motor1AngleRate float value.  */
public float getMotor1AngleRate() {
  if ( motor1AngleRate == null ) { 
    motor1AngleRate = (SFFloat)getField( "motor1AngleRate" ); 
  }
  return( motor1AngleRate.getValue( ) );
}

/** Return the motor2AngleRate float value. 
 * @return The motor2AngleRate float value.  */
public float getMotor2AngleRate() {
  if ( motor2AngleRate == null ) { 
    motor2AngleRate = (SFFloat)getField( "motor2AngleRate" ); 
  }
  return( motor2AngleRate.getValue( ) );
}

/** Return the motor3AngleRate float value. 
 * @return The motor3AngleRate float value.  */
public float getMotor3AngleRate() {
  if ( motor3AngleRate == null ) { 
    motor3AngleRate = (SFFloat)getField( "motor3AngleRate" ); 
  }
  return( motor3AngleRate.getValue( ) );
}

/** Return the stop1ErrorCorrection float value. 
 * @return The stop1ErrorCorrection float value.  */
public float getStop1ErrorCorrection() {
  if ( stop1ErrorCorrection == null ) { 
    stop1ErrorCorrection = (SFFloat)getField( "stop1ErrorCorrection" ); 
  }
  return( stop1ErrorCorrection.getValue( ) );
}

/** Set the stop1ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop1ErrorCorrection(float val) {
  if ( stop1ErrorCorrection == null ) { 
    stop1ErrorCorrection = (SFFloat)getField( "stop1ErrorCorrection" ); 
  }
  stop1ErrorCorrection.setValue( val );
}

/** Return the stop2ErrorCorrection float value. 
 * @return The stop2ErrorCorrection float value.  */
public float getStop2ErrorCorrection() {
  if ( stop2ErrorCorrection == null ) { 
    stop2ErrorCorrection = (SFFloat)getField( "stop2ErrorCorrection" ); 
  }
  return( stop2ErrorCorrection.getValue( ) );
}

/** Set the stop2ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop2ErrorCorrection(float val) {
  if ( stop2ErrorCorrection == null ) { 
    stop2ErrorCorrection = (SFFloat)getField( "stop2ErrorCorrection" ); 
  }
  stop2ErrorCorrection.setValue( val );
}

/** Return the stop3ErrorCorrection float value. 
 * @return The stop3ErrorCorrection float value.  */
public float getStop3ErrorCorrection() {
  if ( stop3ErrorCorrection == null ) { 
    stop3ErrorCorrection = (SFFloat)getField( "stop3ErrorCorrection" ); 
  }
  return( stop3ErrorCorrection.getValue( ) );
}

/** Set the stop3ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop3ErrorCorrection(float val) {
  if ( stop3ErrorCorrection == null ) { 
    stop3ErrorCorrection = (SFFloat)getField( "stop3ErrorCorrection" ); 
  }
  stop3ErrorCorrection.setValue( val );
}

/** Return the enabledAxes int value. 
 * @return The enabledAxes int value.  */
public int getEnabledAxes() {
  if ( enabledAxes == null ) { 
    enabledAxes = (SFInt32)getField( "enabledAxes" ); 
  }
  return( enabledAxes.getValue( ) );
}

/** Set the enabledAxes field. 
 * @param val The int to set.  */
public void setEnabledAxes(int val) {
  if ( enabledAxes == null ) { 
    enabledAxes = (SFInt32)getField( "enabledAxes" ); 
  }
  enabledAxes.setValue( val );
}

}
