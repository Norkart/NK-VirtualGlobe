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

import org.web3d.x3d.sai.X3DTouchSensorNode;

/** Defines the requirements of an X3D TouchSensor node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TouchSensor extends X3DTouchSensorNode {

/** Return the hitNormal value in the argument float[]
 * @param val The float[] to initialize.  */
public void getHitNormal(float[] val);

/** Return the hitPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getHitPoint(float[] val);

/** Return the hitTexCoord value in the argument float[]
 * @param val The float[] to initialize.  */
public void getHitTexCoord(float[] val);

}
