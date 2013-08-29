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

package org.web3d.x3d.sai;

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DTextureNode;

/** Defines the requirements of an X3DTexture2DNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.5 $ */
public interface X3DTexture2DNode extends X3DTextureNode {

/** Return the textureProperties X3DNode value. 
 * @return The textureProperties X3DNode value.  */
public X3DNode getTextureProperties();

/** Set the textureProperties field. 
 * @param val The X3DNode to set.  */
public void setTextureProperties(X3DNode val);

/** Return the repeatS boolean value. 
 * @return The repeatS boolean value.  */
public boolean getRepeatS();

/** Set the repeatS field. 
 * @param val The boolean to set.  */
public void setRepeatS(boolean val);

/** Return the repeatT boolean value. 
 * @return The repeatT boolean value.  */
public boolean getRepeatT();

/** Set the repeatT field. 
 * @param val The boolean to set.  */
public void setRepeatT(boolean val);

}
