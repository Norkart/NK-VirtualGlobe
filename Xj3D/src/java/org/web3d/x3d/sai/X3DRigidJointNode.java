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

import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3DRigidJointNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public interface X3DRigidJointNode extends X3DNode {

/** Return the number of MFString items in the forceOutput field. 
 * @return the number of MFString items in the forceOutput field.  */
public int getNumForceOutput();

/** Return the forceOutput value in the argument String[]
 * @param val The String[] to initialize.  */
public void getForceOutput(String[] val);

/** Set the forceOutput field. 
 * @param val The String[] to set.  */
public void setForceOutput(String[] val);

/** Return the body1 X3DNode value. 
 * @return The body1 X3DNode value.  */
public X3DNode getBody1();

/** Set the body1 field. 
 * @param val The X3DNode to set.  */
public void setBody1(X3DNode val);

/** Return the body2 X3DNode value. 
 * @return The body2 X3DNode value.  */
public X3DNode getBody2();

/** Set the body2 field. 
 * @param val The X3DNode to set.  */
public void setBody2(X3DNode val);

}
