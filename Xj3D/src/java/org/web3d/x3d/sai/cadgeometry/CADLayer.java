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

package org.web3d.x3d.sai.cadgeometry;

import org.web3d.x3d.sai.X3DGroupingNode;

/** Defines the requirements of an X3D CADLayer node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface CADLayer extends X3DGroupingNode {

/** Return the name String value. 
 * @return The name String value.  */
public String getName();

/** Set the name field. 
 * @param val The String to set.  */
public void setName(String val);

/** Return the number of MFBool items in the visible field. 
 * @return the number of MFBool items in the visible field.  */
public int getNumVisible();

/** Return the visible value in the argument boolean[]
 * @param val The boolean[] to initialize.  */
public void getVisible(boolean[] val);

/** Set the visible field. 
 * @param val The boolean[] to set.  */
public void setVisible(boolean[] val);

}
