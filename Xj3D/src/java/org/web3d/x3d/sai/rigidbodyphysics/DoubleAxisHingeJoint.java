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

/** Defines the requirements of an X3D DoubleAxisHingeJoint node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface DoubleAxisHingeJoint extends X3DRigidJointNode {

/** Return the number of MFString items in the mustOutput field. 
 * @return the number of MFString items in the mustOutput field.  */
public int getNumMustOutput();

/** Return the mustOutput value in the argument String[]
 * @param val The String[] to initialize.  */
public void getMustOutput(String[] val);

/** Set the mustOutput field. 
 * @param val The String[] to set.  */
public void setMustOutput(String[] val);

/** Return the anchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAnchorPoint(float[] val);

/** Set the anchorPoint field. 
 * @param val The float[] to set.  */
public void setAnchorPoint(float[] val);

/** Return the axis1 value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAxis1(float[] val);

/** Set the axis1 field. 
 * @param val The float[] to set.  */
public void setAxis1(float[] val);

/** Return the axis2 value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAxis2(float[] val);

/** Set the axis2 field. 
 * @param val The float[] to set.  */
public void setAxis2(float[] val);

/** Return the body1AnchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody1AnchorPoint(float[] val);

/** Return the body2AnchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody2AnchorPoint(float[] val);

/** Return the desiredAngularVelocity1 float value. 
 * @return The desiredAngularVelocity1 float value.  */
public float getDesiredAngularVelocity1();

/** Set the desiredAngularVelocity1 field. 
 * @param val The float to set.  */
public void setDesiredAngularVelocity1(float val);

/** Return the desiredAngularVelocity2 float value. 
 * @return The desiredAngularVelocity2 float value.  */
public float getDesiredAngularVelocity2();

/** Set the desiredAngularVelocity2 field. 
 * @param val The float to set.  */
public void setDesiredAngularVelocity2(float val);

/** Return the body1Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody1Axis(float[] val);

/** Return the body2Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody2Axis(float[] val);

/** Return the minAngle1 float value. 
 * @return The minAngle1 float value.  */
public float getMinAngle1();

/** Set the minAngle1 field. 
 * @param val The float to set.  */
public void setMinAngle1(float val);

/** Return the maxAngle1 float value. 
 * @return The maxAngle1 float value.  */
public float getMaxAngle1();

/** Set the maxAngle1 field. 
 * @param val The float to set.  */
public void setMaxAngle1(float val);

/** Return the maxTorque1 float value. 
 * @return The maxTorque1 float value.  */
public float getMaxTorque1();

/** Set the maxTorque1 field. 
 * @param val The float to set.  */
public void setMaxTorque1(float val);

/** Return the maxTorque2 float value. 
 * @return The maxTorque2 float value.  */
public float getMaxTorque2();

/** Set the maxTorque2 field. 
 * @param val The float to set.  */
public void setMaxTorque2(float val);

/** Return the hinge1Angle float value. 
 * @return The hinge1Angle float value.  */
public float getHinge1Angle();

/** Return the hinge2Angle float value. 
 * @return The hinge2Angle float value.  */
public float getHinge2Angle();

/** Return the hinge1AngleRate float value. 
 * @return The hinge1AngleRate float value.  */
public float getHinge1AngleRate();

/** Return the hinge2AngleRate float value. 
 * @return The hinge2AngleRate float value.  */
public float getHinge2AngleRate();

/** Return the stopBounce1 float value. 
 * @return The stopBounce1 float value.  */
public float getStopBounce1();

/** Set the stopBounce1 field. 
 * @param val The float to set.  */
public void setStopBounce1(float val);

/** Return the stopErrorCorrection1 float value. 
 * @return The stopErrorCorrection1 float value.  */
public float getStopErrorCorrection1();

/** Set the stopErrorCorrection1 field. 
 * @param val The float to set.  */
public void setStopErrorCorrection1(float val);

/** Return the stopConstantForceMix1 float value. 
 * @return The stopConstantForceMix1 float value.  */
public float getStopConstantForceMix1();

/** Set the stopConstantForceMix1 field. 
 * @param val The float to set.  */
public void setStopConstantForceMix1(float val);

/** Return the suspensionForce float value. 
 * @return The suspensionForce float value.  */
public float getSuspensionForce();

/** Set the suspensionForce field. 
 * @param val The float to set.  */
public void setSuspensionForce(float val);

/** Return the suspensionErrorCorrection float value. 
 * @return The suspensionErrorCorrection float value.  */
public float getSuspensionErrorCorrection();

/** Set the suspensionErrorCorrection field. 
 * @param val The float to set.  */
public void setSuspensionErrorCorrection(float val);

}
