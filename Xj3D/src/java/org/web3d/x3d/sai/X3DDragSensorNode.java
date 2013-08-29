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

package org.web3d.x3d.sai;

import org.web3d.x3d.sai.X3DPointingDeviceSensorNode;

/** Defines the requirements of an X3DDragSensorNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.3 $ */
public interface X3DDragSensorNode extends X3DPointingDeviceSensorNode {

/** Return the autoOffset boolean value. 
 * @return The autoOffset boolean value.  */
public boolean getAutoOffset();

/** Set the autoOffset field. 
 * @param val The boolean to set.  */
public void setAutoOffset(boolean val);

/** Return the trackPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTrackPoint(float[] val);

}
