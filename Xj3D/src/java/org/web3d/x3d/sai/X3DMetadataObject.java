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


/** Defines the requirements of an X3DMetadataObject abstract node type
 * @author Rex Melton
 * @version $Revision: 1.3 $ */
public interface X3DMetadataObject {

/** Return the name String value. 
 * @return The name String value.  */
public String getName();

/** Set the name field. 
 * @param val The String to set.  */
public void setName(String val);

/** Return the reference String value. 
 * @return The reference String value.  */
public String getReference();

/** Set the reference field. 
 * @param val The String to set.  */
public void setReference(String val);

}
