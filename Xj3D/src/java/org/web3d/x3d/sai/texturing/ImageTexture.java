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

import org.web3d.x3d.sai.X3DTexture2DNode;

/** Defines the requirements of an X3D ImageTexture node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface ImageTexture extends X3DTexture2DNode {

/** Return the number of MFString items in the url field. 
 * @return the number of MFString items in the url field.  */
public int getNumUrl();

/** Return the url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getUrl(String[] val);

/** Set the url field. 
 * @param val The String[] to set.  */
public void setUrl(String[] val);

}
