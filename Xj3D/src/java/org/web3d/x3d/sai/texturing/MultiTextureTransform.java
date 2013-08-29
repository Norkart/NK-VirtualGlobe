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
import org.web3d.x3d.sai.X3DTextureTransformNode;

/** Defines the requirements of an X3D MultiTextureTransform node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface MultiTextureTransform extends X3DTextureTransformNode {

/** Return the number of MFNode items in the textureTransform field. 
 * @return the number of MFNode items in the textureTransform field.  */
public int getNumTextureTransform();

/** Return the textureTransform value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getTextureTransform(X3DNode[] val);

/** Set the textureTransform field. 
 * @param val The X3DNode[] to set.  */
public void setTextureTransform(X3DNode[] val);

}
