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

package org.web3d.x3d.sai.navigation;

import org.web3d.x3d.sai.X3DGroupingNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DSensorNode;

/** Defines the requirements of an X3D Collision node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Collision extends X3DGroupingNode, X3DSensorNode {

/** Return the collide boolean value. 
 * @return The collide boolean value.  */
public boolean getCollide();

/** Set the collide field. 
 * @param val The boolean to set.  */
public void setCollide(boolean val);

/** Return the proxy X3DNode value. 
 * @return The proxy X3DNode value.  */
public X3DNode getProxy();

/** Set the proxy field. 
 * @param val The X3DNode to set.  */
public void setProxy(X3DNode val);

/** Return the collideTime double value. 
 * @return The collideTime double value.  */
public double getCollideTime();

}
