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

package org.web3d.x3d.sai.grouping;

import org.web3d.x3d.sai.X3DGroupingNode;

/** Defines the requirements of an X3D Switch node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Switch extends X3DGroupingNode {

/** Return the whichChoice int value. 
 * @return The whichChoice int value.  */
public int getWhichChoice();

/** Set the whichChoice field. 
 * @param val The int to set.  */
public void setWhichChoice(int val);

}
