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

import org.web3d.x3d.sai.X3DChildNode;

/** Defines the requirements of an X3DLightNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.5 $ */
public interface X3DLightNode extends X3DChildNode {

/** Return the ambientIntensity float value. 
 * @return The ambientIntensity float value.  */
public float getAmbientIntensity();

/** Set the ambientIntensity field. 
 * @param val The float to set.  */
public void setAmbientIntensity(float val);

/** Return the color value in the argument float[]
 * @param val The float[] to initialize.  */
public void getColor(float[] val);

/** Set the color field. 
 * @param val The float[] to set.  */
public void setColor(float[] val);

/** Return the intensity float value. 
 * @return The intensity float value.  */
public float getIntensity();

/** Set the intensity field. 
 * @param val The float to set.  */
public void setIntensity(float val);

/** Return the on boolean value. 
 * @return The on boolean value.  */
public boolean getOn();

/** Set the on field. 
 * @param val The boolean to set.  */
public void setOn(boolean val);

/** Return the global boolean value. 
 * @return The global boolean value.  */
public boolean getGlobal();

/** Set the global field. 
 * @param val The boolean to set.  */
public void setGlobal(boolean val);

}
