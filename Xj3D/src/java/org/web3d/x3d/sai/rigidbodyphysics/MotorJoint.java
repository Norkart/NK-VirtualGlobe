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

package org.web3d.x3d.sai.rigidbodyphysics;

import org.web3d.x3d.sai.X3DRigidJointNode;

/** Defines the requirements of an X3D MotorJoint node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface MotorJoint extends X3DRigidJointNode {

/** Return the autoCalc boolean value. 
 * @return The autoCalc boolean value.  */
public boolean getAutoCalc();

/** Set the autoCalc field. 
 * @param val The boolean to set.  */
public void setAutoCalc(boolean val);

/** Return the motor1Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMotor1Axis(float[] val);

/** Set the motor1Axis field. 
 * @param val The float[] to set.  */
public void setMotor1Axis(float[] val);

/** Return the motor2Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMotor2Axis(float[] val);

/** Set the motor2Axis field. 
 * @param val The float[] to set.  */
public void setMotor2Axis(float[] val);

/** Return the motor3Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMotor3Axis(float[] val);

/** Set the motor3Axis field. 
 * @param val The float[] to set.  */
public void setMotor3Axis(float[] val);

/** Return the axis1Angle float value. 
 * @return The axis1Angle float value.  */
public float getAxis1Angle();

/** Set the axis1Angle field. 
 * @param val The float to set.  */
public void setAxis1Angle(float val);

/** Return the axis2Angle float value. 
 * @return The axis2Angle float value.  */
public float getAxis2Angle();

/** Set the axis2Angle field. 
 * @param val The float to set.  */
public void setAxis2Angle(float val);

/** Return the axis3Angle float value. 
 * @return The axis3Angle float value.  */
public float getAxis3Angle();

/** Set the axis3Angle field. 
 * @param val The float to set.  */
public void setAxis3Angle(float val);

/** Return the stop1Bounce float value. 
 * @return The stop1Bounce float value.  */
public float getStop1Bounce();

/** Set the stop1Bounce field. 
 * @param val The float to set.  */
public void setStop1Bounce(float val);

/** Return the stop2Bounce float value. 
 * @return The stop2Bounce float value.  */
public float getStop2Bounce();

/** Set the stop2Bounce field. 
 * @param val The float to set.  */
public void setStop2Bounce(float val);

/** Return the stop3Bounce float value. 
 * @return The stop3Bounce float value.  */
public float getStop3Bounce();

/** Set the stop3Bounce field. 
 * @param val The float to set.  */
public void setStop3Bounce(float val);

/** Return the axis1Torque float value. 
 * @return The axis1Torque float value.  */
public float getAxis1Torque();

/** Set the axis1Torque field. 
 * @param val The float to set.  */
public void setAxis1Torque(float val);

/** Return the axis2Torque float value. 
 * @return The axis2Torque float value.  */
public float getAxis2Torque();

/** Set the axis2Torque field. 
 * @param val The float to set.  */
public void setAxis2Torque(float val);

/** Return the axis3Torque float value. 
 * @return The axis3Torque float value.  */
public float getAxis3Torque();

/** Set the axis3Torque field. 
 * @param val The float to set.  */
public void setAxis3Torque(float val);

/** Return the motor1Angle float value. 
 * @return The motor1Angle float value.  */
public float getMotor1Angle();

/** Return the motor2Angle float value. 
 * @return The motor2Angle float value.  */
public float getMotor2Angle();

/** Return the motor3Angle float value. 
 * @return The motor3Angle float value.  */
public float getMotor3Angle();

/** Return the motor1AngleRate float value. 
 * @return The motor1AngleRate float value.  */
public float getMotor1AngleRate();

/** Return the motor2AngleRate float value. 
 * @return The motor2AngleRate float value.  */
public float getMotor2AngleRate();

/** Return the motor3AngleRate float value. 
 * @return The motor3AngleRate float value.  */
public float getMotor3AngleRate();

/** Return the stop1ErrorCorrection float value. 
 * @return The stop1ErrorCorrection float value.  */
public float getStop1ErrorCorrection();

/** Set the stop1ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop1ErrorCorrection(float val);

/** Return the stop2ErrorCorrection float value. 
 * @return The stop2ErrorCorrection float value.  */
public float getStop2ErrorCorrection();

/** Set the stop2ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop2ErrorCorrection(float val);

/** Return the stop3ErrorCorrection float value. 
 * @return The stop3ErrorCorrection float value.  */
public float getStop3ErrorCorrection();

/** Set the stop3ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop3ErrorCorrection(float val);

/** Return the enabledAxes int value. 
 * @return The enabledAxes int value.  */
public int getEnabledAxes();

/** Set the enabledAxes field. 
 * @param val The int to set.  */
public void setEnabledAxes(int val);

}
