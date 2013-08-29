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
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DViewportNode;

/** Defines the requirements of an X3DLayerNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface X3DLayerNode extends X3DNode {

/** Return the isPickable boolean value. 
 * @return The isPickable boolean value.  */
public boolean getIsPickable();

/** Set the isPickable field. 
 * @param val The boolean to set.  */
public void setIsPickable(boolean val);

/** Return the viewport X3DNode value. 
 * @return The viewport X3DNode value.  */
public X3DNode getViewport();

/** Set the viewport field. 
 * @param val The X3DViewportNode to set.  */
public void setViewport(X3DViewportNode val);

/** Set the viewport field. 
 * @param val The X3DProtoInstance to set.  */
public void setViewport(X3DProtoInstance val);

}
