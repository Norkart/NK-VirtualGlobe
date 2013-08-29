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

import org.web3d.x3d.sai.X3DBackgroundNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D TextureBackground node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TextureBackground extends X3DBackgroundNode {

/** Return the number of MFNode items in the backTexture field. 
 * @return the number of MFNode items in the backTexture field.  */
public int getNumBackTexture();

/** Return the backTexture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getBackTexture(X3DNode[] val);

/** Set the backTexture field. 
 * @param val The X3DNode[] to set.  */
public void setBackTexture(X3DNode[] val);

/** Return the number of MFNode items in the frontTexture field. 
 * @return the number of MFNode items in the frontTexture field.  */
public int getNumFrontTexture();

/** Return the frontTexture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getFrontTexture(X3DNode[] val);

/** Set the frontTexture field. 
 * @param val The X3DNode[] to set.  */
public void setFrontTexture(X3DNode[] val);

/** Return the number of MFNode items in the leftTexture field. 
 * @return the number of MFNode items in the leftTexture field.  */
public int getNumLeftTexture();

/** Return the leftTexture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getLeftTexture(X3DNode[] val);

/** Set the leftTexture field. 
 * @param val The X3DNode[] to set.  */
public void setLeftTexture(X3DNode[] val);

/** Return the number of MFNode items in the rightTexture field. 
 * @return the number of MFNode items in the rightTexture field.  */
public int getNumRightTexture();

/** Return the rightTexture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getRightTexture(X3DNode[] val);

/** Set the rightTexture field. 
 * @param val The X3DNode[] to set.  */
public void setRightTexture(X3DNode[] val);

/** Return the number of MFNode items in the bottomTexture field. 
 * @return the number of MFNode items in the bottomTexture field.  */
public int getNumBottomTexture();

/** Return the bottomTexture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getBottomTexture(X3DNode[] val);

/** Set the bottomTexture field. 
 * @param val The X3DNode[] to set.  */
public void setBottomTexture(X3DNode[] val);

/** Return the number of MFNode items in the topTexture field. 
 * @return the number of MFNode items in the topTexture field.  */
public int getNumTopTexture();

/** Return the topTexture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getTopTexture(X3DNode[] val);

/** Set the topTexture field. 
 * @param val The X3DNode[] to set.  */
public void setTopTexture(X3DNode[] val);

}
