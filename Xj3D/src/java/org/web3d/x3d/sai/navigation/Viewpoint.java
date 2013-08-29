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

package org.web3d.x3d.sai.navigation;

import org.web3d.x3d.sai.X3DViewpointNode;

/** Defines the requirements of an X3D Viewpoint node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Viewpoint extends X3DViewpointNode {

/** Return the fieldOfView float value. 
 * @return The fieldOfView float value.  */
public float getFieldOfView();

/** Set the fieldOfView field. 
 * @param val The float to set.  */
public void setFieldOfView(float val);

/** Return the position value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPosition(float[] val);

/** Set the position field. 
 * @param val The float[] to set.  */
public void setPosition(float[] val);

}
