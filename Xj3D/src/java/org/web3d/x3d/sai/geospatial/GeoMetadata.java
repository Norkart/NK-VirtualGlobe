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

package org.web3d.x3d.sai.geospatial;

import org.web3d.x3d.sai.X3DInfoNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D GeoMetadata node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface GeoMetadata extends X3DInfoNode {

/** Return the number of MFNode items in the data field. 
 * @return the number of MFNode items in the data field.  */
public int getNumData();

/** Return the data value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getData(X3DNode[] val);

/** Set the data field. 
 * @param val The X3DNode[] to set.  */
public void setData(X3DNode[] val);

/** Return the number of MFString items in the summary field. 
 * @return the number of MFString items in the summary field.  */
public int getNumSummary();

/** Return the summary value in the argument String[]
 * @param val The String[] to initialize.  */
public void getSummary(String[] val);

/** Set the summary field. 
 * @param val The String[] to set.  */
public void setSummary(String[] val);

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
