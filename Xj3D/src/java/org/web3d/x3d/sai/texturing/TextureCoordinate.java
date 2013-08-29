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

package org.web3d.x3d.sai.texturing;

import org.web3d.x3d.sai.X3DTextureCoordinateNode;

/** Defines the requirements of an X3D TextureCoordinate node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TextureCoordinate extends X3DTextureCoordinateNode {

/** Return the number of MFVec2f items in the point field. 
 * @return the number of MFVec2f items in the point field.  */
public int getNumPoint();

/** Return the point value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPoint(float[] val);

/** Set the point field. 
 * @param val The float[] to set.  */
public void setPoint(float[] val);

}
