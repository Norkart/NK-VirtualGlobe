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

/** Defines the requirements of an X3D ScalarInterpolator node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface ScalarInterpolator extends X3DInterpolatorNode {

/** Return the number of MFFloat items in the keyValue field. 
 * @return the number of MFFloat items in the keyValue field.  */
public int getNumKeyValue();

/** Return the keyValue value in the argument float[]
 * @param val The float[] to initialize.  */
public void getKeyValue(float[] val);

/** Set the keyValue field. 
 * @param val The float[] to set.  */
public void setKeyValue(float[] val);

/** Return the value float value. 
 * @return The value float value.  */
public float getValue();

}
