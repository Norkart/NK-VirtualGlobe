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

package org.web3d.x3d.sai.environmentaleffects;

import org.web3d.x3d.sai.X3DGeometricPropertyNode;

/** Defines the requirements of an X3D FogCoordinate node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface FogCoordinate extends X3DGeometricPropertyNode {

/** Return the number of MFFloat items in the depth field. 
 * @return the number of MFFloat items in the depth field.  */
public int getNumDepth();

/** Return the depth value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDepth(float[] val);

/** Set the depth field. 
 * @param val The float[] to set.  */
public void setDepth(float[] val);

}
