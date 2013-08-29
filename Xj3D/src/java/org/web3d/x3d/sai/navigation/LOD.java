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

/** Defines the requirements of an X3D LOD node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface LOD extends X3DGroupingNode {

/** Return the center value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenter(float[] val);

/** Set the center field. 
 * @param val The float[] to set.  */
public void setCenter(float[] val);

/** Return the number of MFFloat items in the range field. 
 * @return the number of MFFloat items in the range field.  */
public int getNumRange();

/** Return the range value in the argument float[]
 * @param val The float[] to initialize.  */
public void getRange(float[] val);

/** Set the range field. 
 * @param val The float[] to set.  */
public void setRange(float[] val);

/** Return the level int value. 
 * @return The level int value.  */
public int getLevel();

/** Return the forceTransitions boolean value. 
 * @return The forceTransitions boolean value.  */
public boolean getForceTransitions();

/** Set the forceTransitions field. 
 * @param val The boolean to set.  */
public void setForceTransitions(boolean val);

}
