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

/** Defines the requirements of an X3D TextureCoordinateGenerator node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TextureCoordinateGenerator extends X3DTextureCoordinateNode {

/** Return the mode String value. 
 * @return The mode String value.  */
public String getMode();

/** Set the mode field. 
 * @param val The String to set.  */
public void setMode(String val);

}
