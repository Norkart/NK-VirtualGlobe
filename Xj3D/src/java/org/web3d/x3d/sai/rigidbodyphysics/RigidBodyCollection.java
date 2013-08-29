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

import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D RigidBodyCollection node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface RigidBodyCollection extends X3DChildNode {

/** Set the contacts field. 
 * @param val The X3DNode[] to set.  */
public void setContacts(X3DNode[] val);

/** Return the autoDisable boolean value. 
 * @return The autoDisable boolean value.  */
public boolean getAutoDisable();

/** Set the autoDisable field. 
 * @param val The boolean to set.  */
public void setAutoDisable(boolean val);

/** Return the number of MFNode items in the bodies field. 
 * @return the number of MFNode items in the bodies field.  */
public int getNumBodies();

/** Return the bodies value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getBodies(X3DNode[] val);

/** Set the bodies field. 
 * @param val The X3DNode[] to set.  */
public void setBodies(X3DNode[] val);

/** Return the constantForceMix float value. 
 * @return The constantForceMix float value.  */
public float getConstantForceMix();

/** Set the constantForceMix field. 
 * @param val The float to set.  */
public void setConstantForceMix(float val);

/** Return the contactSurfaceThickness float value. 
 * @return The contactSurfaceThickness float value.  */
public float getContactSurfaceThickness();

/** Set the contactSurfaceThickness field. 
 * @param val The float to set.  */
public void setContactSurfaceThickness(float val);

/** Return the disableAngularSpeed float value. 
 * @return The disableAngularSpeed float value.  */
public float getDisableAngularSpeed();

/** Set the disableAngularSpeed field. 
 * @param val The float to set.  */
public void setDisableAngularSpeed(float val);

/** Return the disableLinearSpeed float value. 
 * @return The disableLinearSpeed float value.  */
public float getDisableLinearSpeed();

/** Set the disableLinearSpeed field. 
 * @param val The float to set.  */
public void setDisableLinearSpeed(float val);

/** Return the disableTime float value. 
 * @return The disableTime float value.  */
public float getDisableTime();

/** Set the disableTime field. 
 * @param val The float to set.  */
public void setDisableTime(float val);

/** Return the enabled boolean value. 
 * @return The enabled boolean value.  */
public boolean getEnabled();

/** Set the enabled field. 
 * @param val The boolean to set.  */
public void setEnabled(boolean val);

/** Return the errorCorrectionFactor float value. 
 * @return The errorCorrectionFactor float value.  */
public float getErrorCorrectionFactor();

/** Set the errorCorrectionFactor field. 
 * @param val The float to set.  */
public void setErrorCorrectionFactor(float val);

/** Return the gravity value in the argument float[]
 * @param val The float[] to initialize.  */
public void getGravity(float[] val);

/** Set the gravity field. 
 * @param val The float[] to set.  */
public void setGravity(float[] val);

/** Return the iterations int value. 
 * @return The iterations int value.  */
public int getIterations();

/** Set the iterations field. 
 * @param val The int to set.  */
public void setIterations(int val);

/** Return the number of MFNode items in the joints field. 
 * @return the number of MFNode items in the joints field.  */
public int getNumJoints();

/** Return the joints value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getJoints(X3DNode[] val);

/** Set the joints field. 
 * @param val The X3DNode[] to set.  */
public void setJoints(X3DNode[] val);

/** Return the maxCorrectionSpeed float value. 
 * @return The maxCorrectionSpeed float value.  */
public float getMaxCorrectionSpeed();

/** Set the maxCorrectionSpeed field. 
 * @param val The float to set.  */
public void setMaxCorrectionSpeed(float val);

/** Return the preferAccuracy boolean value. 
 * @return The preferAccuracy boolean value.  */
public boolean getPreferAccuracy();

/** Set the preferAccuracy field. 
 * @param val The boolean to set.  */
public void setPreferAccuracy(boolean val);

/** Return the collider X3DNode value. 
 * @return The collider X3DNode value.  */
public X3DNode getCollider();

/** Set the collider field. 
 * @param val The X3DNode to set.  */
public void setCollider(X3DNode val);

}
