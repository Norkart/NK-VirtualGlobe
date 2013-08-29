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

package org.web3d.x3d.sai.rigidbodyphysics;

import org.web3d.x3d.sai.X3DNBodyCollidableNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;

/** Defines the requirements of an X3D CollidableOffset node
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public interface CollidableOffset extends X3DNBodyCollidableNode {

/** Return the collidable X3DNode value. 
 * @return The collidable X3DNode value.  */
public X3DNode getCollidable();

/** Set the collidable field. 
 * @param val The X3DNBodyCollidableNode to set.  */
public void setCollidable(X3DNBodyCollidableNode val);

/** Set the collidable field. 
 * @param val The X3DProtoInstance to set.  */
public void setCollidable(X3DProtoInstance val);

}
