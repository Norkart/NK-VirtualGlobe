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

package org.web3d.x3d.sai.geometry3d;

import org.web3d.x3d.sai.X3DGeometryNode;

/** Defines the requirements of an X3D Cylinder node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Cylinder extends X3DGeometryNode {

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid();

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val);

/** Return the radius float value. 
 * @return The radius float value.  */
public float getRadius();

/** Set the radius field. 
 * @param val The float to set.  */
public void setRadius(float val);

/** Return the height float value. 
 * @return The height float value.  */
public float getHeight();

/** Set the height field. 
 * @param val The float to set.  */
public void setHeight(float val);

/** Return the bottom boolean value. 
 * @return The bottom boolean value.  */
public boolean getBottom();

/** Set the bottom field. 
 * @param val The boolean to set.  */
public void setBottom(boolean val);

/** Return the side boolean value. 
 * @return The side boolean value.  */
public boolean getSide();

/** Set the side field. 
 * @param val The boolean to set.  */
public void setSide(boolean val);

/** Return the top boolean value. 
 * @return The top boolean value.  */
public boolean getTop();

/** Set the top field. 
 * @param val The boolean to set.  */
public void setTop(boolean val);

}
