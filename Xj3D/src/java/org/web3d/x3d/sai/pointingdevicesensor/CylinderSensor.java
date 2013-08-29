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

package org.web3d.x3d.sai.pointingdevicesensor;

import org.web3d.x3d.sai.X3DDragSensorNode;

/** Defines the requirements of an X3D CylinderSensor node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface CylinderSensor extends X3DDragSensorNode {

/** Return the maxAngle float value. 
 * @return The maxAngle float value.  */
public float getMaxAngle();

/** Set the maxAngle field. 
 * @param val The float to set.  */
public void setMaxAngle(float val);

/** Return the minAngle float value. 
 * @return The minAngle float value.  */
public float getMinAngle();

/** Set the minAngle field. 
 * @param val The float to set.  */
public void setMinAngle(float val);

/** Return the diskAngle float value. 
 * @return The diskAngle float value.  */
public float getDiskAngle();

/** Set the diskAngle field. 
 * @param val The float to set.  */
public void setDiskAngle(float val);

/** Return the rotation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getRotation(float[] val);

/** Return the offset float value. 
 * @return The offset float value.  */
public float getOffset();

/** Set the offset field. 
 * @param val The float to set.  */
public void setOffset(float val);

}
