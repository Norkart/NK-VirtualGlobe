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

import org.web3d.x3d.sai.X3DBindableNode;

/** Defines the requirements of an X3DViewpointNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface X3DViewpointNode extends X3DBindableNode {

/** Return the jump boolean value. 
 * @return The jump boolean value.  */
public boolean getJump();

/** Set the jump field. 
 * @param val The boolean to set.  */
public void setJump(boolean val);

/** Set the orientation field. 
 * @param val The float[] to set.  */
public void setOrientation(float[] val);

/** Return the description String value. 
 * @return The description String value.  */
public String getDescription();

/** Set the description field. 
 * @param val The String to set.  */
public void setDescription(String val);

/** Return the retainUserOffsets boolean value. 
 * @return The retainUserOffsets boolean value.  */
public boolean getRetainUserOffsets();

/** Set the retainUserOffsets field. 
 * @param val The boolean to set.  */
public void setRetainUserOffsets(boolean val);

}
