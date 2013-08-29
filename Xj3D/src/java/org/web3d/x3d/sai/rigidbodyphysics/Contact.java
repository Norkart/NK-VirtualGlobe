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

import org.web3d.x3d.sai.X3DNBodyCollidableNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;

/** Defines the requirements of an X3D Contact node
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public interface Contact extends X3DNode {

/** Return the number of MFString items in the appliedParameters field. 
 * @return the number of MFString items in the appliedParameters field.  */
public int getNumAppliedParameters();

/** Return the appliedParameters value in the argument String[]
 * @param val The String[] to initialize.  */
public void getAppliedParameters(String[] val);

/** Set the appliedParameters field. 
 * @param val The String[] to set.  */
public void setAppliedParameters(String[] val);

/** Return the body1 X3DNode value. 
 * @return The body1 X3DNode value.  */
public X3DNode getBody1();

/** Set the body1 field. 
 * @param val The X3DNode to set.  */
public void setBody1(X3DNode val);

/** Return the body2 X3DNode value. 
 * @return The body2 X3DNode value.  */
public X3DNode getBody2();

/** Set the body2 field. 
 * @param val The X3DNode to set.  */
public void setBody2(X3DNode val);

/** Return the bounce float value. 
 * @return The bounce float value.  */
public float getBounce();

/** Set the bounce field. 
 * @param val The float to set.  */
public void setBounce(float val);

/** Return the contactNormal value in the argument float[]
 * @param val The float[] to initialize.  */
public void getContactNormal(float[] val);

/** Set the contactNormal field. 
 * @param val The float[] to set.  */
public void setContactNormal(float[] val);

/** Return the depth float value. 
 * @return The depth float value.  */
public float getDepth();

/** Set the depth field. 
 * @param val The float to set.  */
public void setDepth(float val);

/** Return the frictionCoefficients value in the argument float[]
 * @param val The float[] to initialize.  */
public void getFrictionCoefficients(float[] val);

/** Set the frictionCoefficients field. 
 * @param val The float[] to set.  */
public void setFrictionCoefficients(float[] val);

/** Return the frictionDirection value in the argument float[]
 * @param val The float[] to initialize.  */
public void getFrictionDirection(float[] val);

/** Set the frictionDirection field. 
 * @param val The float[] to set.  */
public void setFrictionDirection(float[] val);

/** Return the geometry1 X3DNode value. 
 * @return The geometry1 X3DNode value.  */
public X3DNode getGeometry1();

/** Set the geometry1 field. 
 * @param val The X3DNBodyCollidableNode to set.  */
public void setGeometry1(X3DNBodyCollidableNode val);

/** Set the geometry1 field. 
 * @param val The X3DProtoInstance to set.  */
public void setGeometry1(X3DProtoInstance val);

/** Return the geometry2 X3DNode value. 
 * @return The geometry2 X3DNode value.  */
public X3DNode getGeometry2();

/** Set the geometry2 field. 
 * @param val The X3DNBodyCollidableNode to set.  */
public void setGeometry2(X3DNBodyCollidableNode val);

/** Set the geometry2 field. 
 * @param val The X3DProtoInstance to set.  */
public void setGeometry2(X3DProtoInstance val);

/** Return the minBounceSpeed float value. 
 * @return The minBounceSpeed float value.  */
public float getMinBounceSpeed();

/** Set the minBounceSpeed field. 
 * @param val The float to set.  */
public void setMinBounceSpeed(float val);

/** Return the position value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPosition(float[] val);

/** Set the position field. 
 * @param val The float[] to set.  */
public void setPosition(float[] val);

/** Return the slipCoefficients value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSlipCoefficients(float[] val);

/** Set the slipCoefficients field. 
 * @param val The float[] to set.  */
public void setSlipCoefficients(float[] val);

/** Return the surfaceSpeed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSurfaceSpeed(float[] val);

/** Set the surfaceSpeed field. 
 * @param val The float[] to set.  */
public void setSurfaceSpeed(float[] val);

/** Return the softnessConstantForceMix float value. 
 * @return The softnessConstantForceMix float value.  */
public float getSoftnessConstantForceMix();

/** Set the softnessConstantForceMix field. 
 * @param val The float to set.  */
public void setSoftnessConstantForceMix(float val);

/** Return the softnessErrorCorrection float value. 
 * @return The softnessErrorCorrection float value.  */
public float getSoftnessErrorCorrection();

/** Set the softnessErrorCorrection field. 
 * @param val The float to set.  */
public void setSoftnessErrorCorrection(float val);

}
