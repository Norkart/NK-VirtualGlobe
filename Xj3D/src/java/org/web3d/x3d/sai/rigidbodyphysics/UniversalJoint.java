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

/** Defines the requirements of an X3D UniversalJoint node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface UniversalJoint extends X3DRigidJointNode {

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

/** Return the body1Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody1Axis(float[] val);

/** Return the body2Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody2Axis(float[] val);

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

}
