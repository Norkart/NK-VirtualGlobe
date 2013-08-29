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

import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D TextureProperties node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TextureProperties extends X3DNode {

/** Return the boundaryColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBoundaryColor(float[] val);

/** Set the boundaryColor field. 
 * @param val The float[] to set.  */
public void setBoundaryColor(float[] val);

/** Return the boundaryWidth int value. 
 * @return The boundaryWidth int value.  */
public int getBoundaryWidth();

/** Set the boundaryWidth field. 
 * @param val The int to set.  */
public void setBoundaryWidth(int val);

/** Return the boundaryModeS String value. 
 * @return The boundaryModeS String value.  */
public String getBoundaryModeS();

/** Set the boundaryModeS field. 
 * @param val The String to set.  */
public void setBoundaryModeS(String val);

/** Return the boundaryModeT String value. 
 * @return The boundaryModeT String value.  */
public String getBoundaryModeT();

/** Set the boundaryModeT field. 
 * @param val The String to set.  */
public void setBoundaryModeT(String val);

/** Return the magnificationFilter String value. 
 * @return The magnificationFilter String value.  */
public String getMagnificationFilter();

/** Set the magnificationFilter field. 
 * @param val The String to set.  */
public void setMagnificationFilter(String val);

/** Return the minificationFilter String value. 
 * @return The minificationFilter String value.  */
public String getMinificationFilter();

/** Set the minificationFilter field. 
 * @param val The String to set.  */
public void setMinificationFilter(String val);

/** Return the generateMipMaps boolean value. 
 * @return The generateMipMaps boolean value.  */
public boolean getGenerateMipMaps();

/** Set the generateMipMaps field. 
 * @param val The boolean to set.  */
public void setGenerateMipMaps(boolean val);

/** Return the anisotropicMode String value. 
 * @return The anisotropicMode String value.  */
public String getAnisotropicMode();

/** Set the anisotropicMode field. 
 * @param val The String to set.  */
public void setAnisotropicMode(String val);

/** Return the anisotropicFilterDegree float value. 
 * @return The anisotropicFilterDegree float value.  */
public float getAnisotropicFilterDegree();

/** Set the anisotropicFilterDegree field. 
 * @param val The float to set.  */
public void setAnisotropicFilterDegree(float val);

}
