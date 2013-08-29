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

package org.web3d.x3d.sai.shape;

import org.web3d.x3d.sai.X3DAppearanceChildNode;

/** Defines the requirements of an X3D LineProperties node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface LineProperties extends X3DAppearanceChildNode {

/** Return the linewidthScaleFactor float value. 
 * @return The linewidthScaleFactor float value.  */
public float getLinewidthScaleFactor();

/** Set the linewidthScaleFactor field. 
 * @param val The float to set.  */
public void setLinewidthScaleFactor(float val);

/** Return the linetype int value. 
 * @return The linetype int value.  */
public int getLinetype();

/** Set the linetype field. 
 * @param val The int to set.  */
public void setLinetype(int val);

/** Return the applied boolean value. 
 * @return The applied boolean value.  */
public boolean getApplied();

/** Set the applied field. 
 * @param val The boolean to set.  */
public void setApplied(boolean val);

}
