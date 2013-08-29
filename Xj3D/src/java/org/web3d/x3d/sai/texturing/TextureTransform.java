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

import org.web3d.x3d.sai.X3DTextureTransformNode;

/** Defines the requirements of an X3D TextureTransform node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TextureTransform extends X3DTextureTransformNode {

/** Return the center value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenter(float[] val);

/** Set the center field. 
 * @param val The float[] to set.  */
public void setCenter(float[] val);

/** Return the rotation float value. 
 * @return The rotation float value.  */
public float getRotation();

/** Set the rotation field. 
 * @param val The float to set.  */
public void setRotation(float val);

/** Return the scale value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScale(float[] val);

/** Set the scale field. 
 * @param val The float[] to set.  */
public void setScale(float[] val);

/** Return the translation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTranslation(float[] val);

/** Set the translation field. 
 * @param val The float[] to set.  */
public void setTranslation(float[] val);

}
