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

import org.web3d.x3d.sai.X3DBindableNode;

/** Defines the requirements of an X3DBackgroundNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.5 $ */
public interface X3DBackgroundNode extends X3DBindableNode {

/** Return the number of MFFloat items in the groundAngle field. 
 * @return the number of MFFloat items in the groundAngle field.  */
public int getNumGroundAngle();

/** Return the groundAngle value in the argument float[]
 * @param val The float[] to initialize.  */
public void getGroundAngle(float[] val);

/** Set the groundAngle field. 
 * @param val The float[] to set.  */
public void setGroundAngle(float[] val);

/** Return the number of MFColor items in the groundColor field. 
 * @return the number of MFColor items in the groundColor field.  */
public int getNumGroundColor();

/** Return the groundColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getGroundColor(float[] val);

/** Set the groundColor field. 
 * @param val The float[] to set.  */
public void setGroundColor(float[] val);

/** Return the number of MFFloat items in the skyAngle field. 
 * @return the number of MFFloat items in the skyAngle field.  */
public int getNumSkyAngle();

/** Return the skyAngle value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSkyAngle(float[] val);

/** Set the skyAngle field. 
 * @param val The float[] to set.  */
public void setSkyAngle(float[] val);

/** Return the number of MFColor items in the skyColor field. 
 * @return the number of MFColor items in the skyColor field.  */
public int getNumSkyColor();

/** Return the skyColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSkyColor(float[] val);

/** Set the skyColor field. 
 * @param val The float[] to set.  */
public void setSkyColor(float[] val);

/** Return the transparency float value. 
 * @return The transparency float value.  */
public float getTransparency();

/** Set the transparency field. 
 * @param val The float to set.  */
public void setTransparency(float val);

}
