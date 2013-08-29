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

package org.web3d.x3d.sai.layering;

import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D FixedViewport node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface FixedViewport extends X3DNode {

/** Return the x int value. 
 * @return The x int value.  */
public int getX();

/** Set the x field. 
 * @param val The int to set.  */
public void setX(int val);

/** Return the y int value. 
 * @return The y int value.  */
public int getY();

/** Set the y field. 
 * @param val The int to set.  */
public void setY(int val);

/** Return the width int value. 
 * @return The width int value.  */
public int getWidth();

/** Set the width field. 
 * @param val The int to set.  */
public void setWidth(int val);

/** Return the height int value. 
 * @return The height int value.  */
public int getHeight();

/** Set the height field. 
 * @param val The int to set.  */
public void setHeight(int val);

}
