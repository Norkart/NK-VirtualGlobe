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


/** Defines the requirements of an X3DFogObject abstract node type
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface X3DFogObject {

/** Return the color value in the argument float[]
 * @param val The float[] to initialize.  */
public void getColor(float[] val);

/** Set the color field. 
 * @param val The float[] to set.  */
public void setColor(float[] val);

/** Return the fogType String value. 
 * @return The fogType String value.  */
public String getFogType();

/** Set the fogType field. 
 * @param val The String to set.  */
public void setFogType(String val);

/** Return the visibilityRange float value. 
 * @return The visibilityRange float value.  */
public float getVisibilityRange();

/** Set the visibilityRange field. 
 * @param val The float to set.  */
public void setVisibilityRange(float val);

}
