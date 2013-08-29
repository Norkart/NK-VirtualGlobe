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

package org.web3d.x3d.sai.layering;

import org.web3d.x3d.sai.X3DLayerNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D Layer node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Layer extends X3DLayerNode {

/** Set the addChildren field. 
 * @param val The X3DNode[] to set.  */
public void addChildren(X3DNode[] val);

/** Set the removeChildren field. 
 * @param val The X3DNode[] to set.  */
public void removeChildren(X3DNode[] val);

/** Return the number of MFNode items in the children field. 
 * @return the number of MFNode items in the children field.  */
public int getNumChildren();

/** Return the children value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getChildren(X3DNode[] val);

/** Set the children field. 
 * @param val The X3DNode[] to set.  */
public void setChildren(X3DNode[] val);

}
