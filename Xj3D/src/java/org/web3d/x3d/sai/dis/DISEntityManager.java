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

package org.web3d.x3d.sai.dis;

import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D DISEntityManager node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface DISEntityManager extends X3DChildNode {

/** Return the siteID int value. 
 * @return The siteID int value.  */
public int getSiteID();

/** Set the siteID field. 
 * @param val The int to set.  */
public void setSiteID(int val);

/** Return the applicationID int value. 
 * @return The applicationID int value.  */
public int getApplicationID();

/** Set the applicationID field. 
 * @param val The int to set.  */
public void setApplicationID(int val);

/** Return the address String value. 
 * @return The address String value.  */
public String getAddress();

/** Set the address field. 
 * @param val The String to set.  */
public void setAddress(String val);

/** Return the port int value. 
 * @return The port int value.  */
public int getPort();

/** Set the port field. 
 * @param val The int to set.  */
public void setPort(int val);

/** Return the number of MFNode items in the addedEntities field. 
 * @return the number of MFNode items in the addedEntities field.  */
public int getNumAddedEntities();

/** Return the addedEntities value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getAddedEntities(X3DNode[] val);

/** Return the number of MFNode items in the removedEntities field. 
 * @return the number of MFNode items in the removedEntities field.  */
public int getNumRemovedEntities();

/** Return the removedEntities value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getRemovedEntities(X3DNode[] val);

/** Return the number of MFNode items in the mapping field. 
 * @return the number of MFNode items in the mapping field.  */
public int getNumMapping();

/** Return the mapping value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getMapping(X3DNode[] val);

/** Set the mapping field. 
 * @param val The X3DNode[] to set.  */
public void setMapping(X3DNode[] val);

}
