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

/** Defines the requirements of an X3D CustomViewport node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface CustomViewport extends X3DNode {

/** Return the fixedX boolean value. 
 * @return The fixedX boolean value.  */
public boolean getFixedX();

/** Set the fixedX field. 
 * @param val The boolean to set.  */
public void setFixedX(boolean val);

/** Return the fixedY boolean value. 
 * @return The fixedY boolean value.  */
public boolean getFixedY();

/** Set the fixedY field. 
 * @param val The boolean to set.  */
public void setFixedY(boolean val);

/** Return the fixedWidth boolean value. 
 * @return The fixedWidth boolean value.  */
public boolean getFixedWidth();

/** Set the fixedWidth field. 
 * @param val The boolean to set.  */
public void setFixedWidth(boolean val);

/** Return the fixedHeight boolean value. 
 * @return The fixedHeight boolean value.  */
public boolean getFixedHeight();

/** Set the fixedHeight field. 
 * @param val The boolean to set.  */
public void setFixedHeight(boolean val);

/** Return the x float value. 
 * @return The x float value.  */
public float getX();

/** Set the x field. 
 * @param val The float to set.  */
public void setX(float val);

/** Return the y float value. 
 * @return The y float value.  */
public float getY();

/** Set the y field. 
 * @param val The float to set.  */
public void setY(float val);

/** Return the width float value. 
 * @return The width float value.  */
public float getWidth();

/** Set the width field. 
 * @param val The float to set.  */
public void setWidth(float val);

/** Return the height float value. 
 * @return The height float value.  */
public float getHeight();

/** Set the height field. 
 * @param val The float to set.  */
public void setHeight(float val);

}
