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

package org.web3d.x3d.sai.environmentaleffects;

import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DFogObject;

/** Defines the requirements of an X3D LocalFog node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface LocalFog extends X3DChildNode, X3DFogObject {

/** Return the enabled boolean value. 
 * @return The enabled boolean value.  */
public boolean getEnabled();

/** Set the enabled field. 
 * @param val The boolean to set.  */
public void setEnabled(boolean val);

}
