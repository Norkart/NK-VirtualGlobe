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

package org.web3d.x3d.sai.interpolation;

import org.web3d.x3d.sai.X3DInterpolatorNode;

/** Defines the requirements of an X3D CoordinateInterpolator2D node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface CoordinateInterpolator2D extends X3DInterpolatorNode {

/** Return the number of MFVec2f items in the keyValue field. 
 * @return the number of MFVec2f items in the keyValue field.  */
public int getNumKeyValue();

/** Return the keyValue value in the argument float[]
 * @param val The float[] to initialize.  */
public void getKeyValue(float[] val);

/** Set the keyValue field. 
 * @param val The float[] to set.  */
public void setKeyValue(float[] val);

/** Return the number of MFVec2f items in the value field. 
 * @return the number of MFVec2f items in the value field.  */
public int getNumValue();

/** Return the value value in the argument float[]
 * @param val The float[] to initialize.  */
public void getValue(float[] val);

}
