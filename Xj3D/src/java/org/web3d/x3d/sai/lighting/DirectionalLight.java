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

package org.web3d.x3d.sai.lighting;

import org.web3d.x3d.sai.X3DLightNode;

/** Defines the requirements of an X3D DirectionalLight node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface DirectionalLight extends X3DLightNode {

/** Return the direction value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDirection(float[] val);

/** Set the direction field. 
 * @param val The float[] to set.  */
public void setDirection(float[] val);

}