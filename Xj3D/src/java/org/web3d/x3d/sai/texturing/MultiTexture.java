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
import org.web3d.x3d.sai.X3DTextureNode;

/** Defines the requirements of an X3D MultiTexture node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface MultiTexture extends X3DTextureNode {

/** Return the number of MFString items in the mode field. 
 * @return the number of MFString items in the mode field.  */
public int getNumMode();

/** Return the mode value in the argument String[]
 * @param val The String[] to initialize.  */
public void getMode(String[] val);

/** Set the mode field. 
 * @param val The String[] to set.  */
public void setMode(String[] val);

/** Return the number of MFNode items in the texture field. 
 * @return the number of MFNode items in the texture field.  */
public int getNumTexture();

/** Return the texture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getTexture(X3DNode[] val);

/** Set the texture field. 
 * @param val The X3DNode[] to set.  */
public void setTexture(X3DNode[] val);

/** Return the color value in the argument float[]
 * @param val The float[] to initialize.  */
public void getColor(float[] val);

/** Set the color field. 
 * @param val The float[] to set.  */
public void setColor(float[] val);

/** Return the alpha float value. 
 * @return The alpha float value.  */
public float getAlpha();

/** Set the alpha field. 
 * @param val The float to set.  */
public void setAlpha(float val);

/** Return the number of MFString items in the function field. 
 * @return the number of MFString items in the function field.  */
public int getNumFunction();

/** Return the function value in the argument String[]
 * @param val The String[] to initialize.  */
public void getFunction(String[] val);

/** Set the function field. 
 * @param val The String[] to set.  */
public void setFunction(String[] val);

/** Return the number of MFString items in the source field. 
 * @return the number of MFString items in the source field.  */
public int getNumSource();

/** Return the source value in the argument String[]
 * @param val The String[] to initialize.  */
public void getSource(String[] val);

/** Set the source field. 
 * @param val The String[] to set.  */
public void setSource(String[] val);

}
