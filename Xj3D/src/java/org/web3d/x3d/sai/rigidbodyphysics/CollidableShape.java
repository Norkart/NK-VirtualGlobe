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

/** Defines the requirements of an X3D CollidableShape node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface CollidableShape extends X3DNBodyCollidableNode {

/** Return the shape X3DNode value. 
 * @return The shape X3DNode value.  */
public X3DNode getShape();

/** Set the shape field. 
 * @param val The X3DNode to set.  */
public void setShape(X3DNode val);

}
