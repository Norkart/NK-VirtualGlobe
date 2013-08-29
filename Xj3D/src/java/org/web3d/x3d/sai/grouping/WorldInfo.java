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

import org.web3d.x3d.sai.X3DInfoNode;

/** Defines the requirements of an X3D WorldInfo node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface WorldInfo extends X3DInfoNode {

/** Return the title String value. 
 * @return The title String value.  */
public String getTitle();

/** Set the title field. 
 * @param val The String to set.  */
public void setTitle(String val);

/** Return the number of MFString items in the info field. 
 * @return the number of MFString items in the info field.  */
public int getNumInfo();

/** Return the info value in the argument String[]
 * @param val The String[] to initialize.  */
public void getInfo(String[] val);

/** Set the info field. 
 * @param val The String[] to set.  */
public void setInfo(String[] val);

}
