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

package org.web3d.x3d.sai.eventutilities;

import org.web3d.x3d.sai.X3DChildNode;

/** Defines the requirements of an X3D BooleanFilter node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface BooleanFilter extends X3DChildNode {

/** Set the boolean field. 
 * @param val The boolean to set.  */
public void setBoolean(boolean val);

/** Return the inputFalse boolean value. 
 * @return The inputFalse boolean value.  */
public boolean getInputFalse();

/** Return the inputTrue boolean value. 
 * @return The inputTrue boolean value.  */
public boolean getInputTrue();

/** Return the inputNegate boolean value. 
 * @return The inputNegate boolean value.  */
public boolean getInputNegate();

}
