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

package org.web3d.x3d.sai.pickingsensor;

import org.web3d.x3d.sai.X3DPickingNode;

/** Defines the requirements of an X3D LinePicker node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface LinePicker extends X3DPickingNode {

/** Return the number of MFVec3f items in the pickedPoint field. 
 * @return the number of MFVec3f items in the pickedPoint field.  */
public int getNumPickedPoint();

/** Return the pickedPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPickedPoint(float[] val);

/** Return the number of MFVec3f items in the pickedNormal field. 
 * @return the number of MFVec3f items in the pickedNormal field.  */
public int getNumPickedNormal();

/** Return the pickedNormal value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPickedNormal(float[] val);

/** Return the number of MFVec3f items in the pickedTextureCoordinate field. 
 * @return the number of MFVec3f items in the pickedTextureCoordinate field.  */
public int getNumPickedTextureCoordinate();

/** Return the pickedTextureCoordinate value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPickedTextureCoordinate(float[] val);

}
