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

package org.web3d.x3d.sai.environmentalsensor;

import org.web3d.x3d.sai.X3DEnvironmentalSensorNode;

/** Defines the requirements of an X3D ProximitySensor node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface ProximitySensor extends X3DEnvironmentalSensorNode {

/** Return the position value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPosition(float[] val);

/** Return the orientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getOrientation(float[] val);

/** Return the centerOfRotation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenterOfRotation(float[] val);

}
