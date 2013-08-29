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

package org.web3d.x3d.sai.geometry2d;

import org.web3d.x3d.sai.X3DGeometryNode;

/** Defines the requirements of an X3D Rectangle2D node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Rectangle2D extends X3DGeometryNode {

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid();

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val);

/** Return the size value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSize(float[] val);

/** Set the size field. 
 * @param val The float[] to set.  */
public void setSize(float[] val);

}
