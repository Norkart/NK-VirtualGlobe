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

package org.web3d.x3d.sai.shape;

import org.web3d.x3d.sai.X3DMaterialNode;

/** Defines the requirements of an X3D TwoSidedMaterial node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TwoSidedMaterial extends X3DMaterialNode {

/** Return the ambientIntensity float value. 
 * @return The ambientIntensity float value.  */
public float getAmbientIntensity();

/** Set the ambientIntensity field. 
 * @param val The float to set.  */
public void setAmbientIntensity(float val);

/** Return the backAmbientIntensity float value. 
 * @return The backAmbientIntensity float value.  */
public float getBackAmbientIntensity();

/** Set the backAmbientIntensity field. 
 * @param val The float to set.  */
public void setBackAmbientIntensity(float val);

/** Return the diffuseColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDiffuseColor(float[] val);

/** Set the diffuseColor field. 
 * @param val The float[] to set.  */
public void setDiffuseColor(float[] val);

/** Return the backDiffuseColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBackDiffuseColor(float[] val);

/** Set the backDiffuseColor field. 
 * @param val The float[] to set.  */
public void setBackDiffuseColor(float[] val);

/** Return the emissiveColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getEmissiveColor(float[] val);

/** Set the emissiveColor field. 
 * @param val The float[] to set.  */
public void setEmissiveColor(float[] val);

/** Return the backEmissiveColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBackEmissiveColor(float[] val);

/** Set the backEmissiveColor field. 
 * @param val The float[] to set.  */
public void setBackEmissiveColor(float[] val);

/** Return the shininess float value. 
 * @return The shininess float value.  */
public float getShininess();

/** Set the shininess field. 
 * @param val The float to set.  */
public void setShininess(float val);

/** Return the backShininess float value. 
 * @return The backShininess float value.  */
public float getBackShininess();

/** Set the backShininess field. 
 * @param val The float to set.  */
public void setBackShininess(float val);

/** Return the specularColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSpecularColor(float[] val);

/** Set the specularColor field. 
 * @param val The float[] to set.  */
public void setSpecularColor(float[] val);

/** Return the backSpecularColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBackSpecularColor(float[] val);

/** Set the backSpecularColor field. 
 * @param val The float[] to set.  */
public void setBackSpecularColor(float[] val);

/** Return the transparency float value. 
 * @return The transparency float value.  */
public float getTransparency();

/** Set the transparency field. 
 * @param val The float to set.  */
public void setTransparency(float val);

/** Return the backTransparency float value. 
 * @return The backTransparency float value.  */
public float getBackTransparency();

/** Set the backTransparency field. 
 * @param val The float to set.  */
public void setBackTransparency(float val);

/** Return the separateBackColor boolean value. 
 * @return The separateBackColor boolean value.  */
public boolean getSeparateBackColor();

/** Set the separateBackColor field. 
 * @param val The boolean to set.  */
public void setSeparateBackColor(boolean val);

}
