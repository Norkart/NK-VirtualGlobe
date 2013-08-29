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

package org.web3d.x3d.sai.lighting;

import org.web3d.x3d.sai.X3DLightNode;

/** Defines the requirements of an X3D PointLight node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface PointLight extends X3DLightNode {

/** Return the attenuation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAttenuation(float[] val);

/** Set the attenuation field. 
 * @param val The float[] to set.  */
public void setAttenuation(float[] val);

/** Return the location value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLocation(float[] val);

/** Set the location field. 
 * @param val The float[] to set.  */
public void setLocation(float[] val);

/** Return the radius float value. 
 * @return The radius float value.  */
public float getRadius();

/** Set the radius field. 
 * @param val The float to set.  */
public void setRadius(float val);

}
