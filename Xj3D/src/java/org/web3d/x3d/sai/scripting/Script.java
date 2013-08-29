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

package org.web3d.x3d.sai.scripting;

import org.web3d.x3d.sai.X3DScriptNode;

/** Defines the requirements of an X3D Script node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Script extends X3DScriptNode {

/** Return the mustEvaluate boolean value. 
 * @return The mustEvaluate boolean value.  */
public boolean getMustEvaluate();

/** Set the mustEvaluate field. 
 * @param val The boolean to set.  */
public void setMustEvaluate(boolean val);

/** Return the directOutput boolean value. 
 * @return The directOutput boolean value.  */
public boolean getDirectOutput();

/** Set the directOutput field. 
 * @param val The boolean to set.  */
public void setDirectOutput(boolean val);

}
