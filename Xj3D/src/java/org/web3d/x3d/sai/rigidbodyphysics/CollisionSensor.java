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

package org.web3d.x3d.sai.rigidbodyphysics;

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DSensorNode;

/** Defines the requirements of an X3D CollisionSensor node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface CollisionSensor extends X3DSensorNode {

/** Return the collidables X3DNode value. 
 * @return The collidables X3DNode value.  */
public X3DNode getCollidables();

/** Set the collidables field. 
 * @param val The X3DNode to set.  */
public void setCollidables(X3DNode val);

/** Return the number of MFNode items in the contacts field. 
 * @return the number of MFNode items in the contacts field.  */
public int getNumContacts();

/** Return the contacts value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getContacts(X3DNode[] val);

/** Return the number of MFNode items in the intersections field. 
 * @return the number of MFNode items in the intersections field.  */
public int getNumIntersections();

/** Return the intersections value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getIntersections(X3DNode[] val);

}
