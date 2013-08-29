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


/** Defines the requirements of an X3DPickableObject abstract node type
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface X3DPickableObject {

/** Return the pickable boolean value. 
 * @return The pickable boolean value.  */
public boolean getPickable();

/** Set the pickable field. 
 * @param val The boolean to set.  */
public void setPickable(boolean val);

/** Return the number of MFString items in the objectType field. 
 * @return the number of MFString items in the objectType field.  */
public int getNumObjectType();

/** Return the objectType value in the argument String[]
 * @param val The String[] to initialize.  */
public void getObjectType(String[] val);

/** Set the objectType field. 
 * @param val The String[] to set.  */
public void setObjectType(String[] val);

}
