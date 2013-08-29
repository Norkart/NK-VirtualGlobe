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

import org.web3d.x3d.sai.X3DNBodyCollisionSpaceNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D CollisionSpace node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface CollisionSpace extends X3DNBodyCollisionSpaceNode {

/** Return the number of MFNode items in the collidables field. 
 * @return the number of MFNode items in the collidables field.  */
public int getNumCollidables();

/** Return the collidables value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getCollidables(X3DNode[] val);

/** Set the collidables field. 
 * @param val The X3DNode[] to set.  */
public void setCollidables(X3DNode[] val);

/** Return the useGeometry boolean value. 
 * @return The useGeometry boolean value.  */
public boolean getUseGeometry();

/** Set the useGeometry field. 
 * @param val The boolean to set.  */
public void setUseGeometry(boolean val);

}
