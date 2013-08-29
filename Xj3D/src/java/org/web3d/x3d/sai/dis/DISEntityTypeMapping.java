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

import org.web3d.x3d.sai.X3DInfoNode;

/** Defines the requirements of an X3D DISEntityTypeMapping node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface DISEntityTypeMapping extends X3DInfoNode {

/** Return the domain int value. 
 * @return The domain int value.  */
public int getDomain();

/** Set the domain field. 
 * @param val The int to set.  */
public void setDomain(int val);

/** Return the country int value. 
 * @return The country int value.  */
public int getCountry();

/** Set the country field. 
 * @param val The int to set.  */
public void setCountry(int val);

/** Return the category int value. 
 * @return The category int value.  */
public int getCategory();

/** Set the category field. 
 * @param val The int to set.  */
public void setCategory(int val);

/** Return the subCategory int value. 
 * @return The subCategory int value.  */
public int getSubCategory();

/** Set the subCategory field. 
 * @param val The int to set.  */
public void setSubCategory(int val);

/** Return the specific int value. 
 * @return The specific int value.  */
public int getSpecific();

/** Set the specific field. 
 * @param val The int to set.  */
public void setSpecific(int val);

/** Return the kind int value. 
 * @return The kind int value.  */
public int getKind();

/** Set the kind field. 
 * @param val The int to set.  */
public void setKind(int val);

/** Return the extra int value. 
 * @return The extra int value.  */
public int getExtra();

/** Set the extra field. 
 * @param val The int to set.  */
public void setExtra(int val);

/** Return the number of MFString items in the url field. 
 * @return the number of MFString items in the url field.  */
public int getNumUrl();

/** Return the url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getUrl(String[] val);

/** Set the url field. 
 * @param val The String[] to set.  */
public void setUrl(String[] val);

}
