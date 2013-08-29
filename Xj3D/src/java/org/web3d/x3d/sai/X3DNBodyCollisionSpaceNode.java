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

import org.web3d.x3d.sai.X3DBoundedObject;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3DNBodyCollisionSpaceNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface X3DNBodyCollisionSpaceNode extends X3DNode, X3DBoundedObject {

/** Return the enabled boolean value. 
 * @return The enabled boolean value.  */
public boolean getEnabled();

/** Set the enabled field. 
 * @param val The boolean to set.  */
public void setEnabled(boolean val);

}
