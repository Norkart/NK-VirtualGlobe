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

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DSensorNode;

/** Defines the requirements of an X3DPickingNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface X3DPickingNode extends X3DSensorNode {

/** Return the pickingGeometry X3DNode value. 
 * @return The pickingGeometry X3DNode value.  */
public X3DNode getPickingGeometry();

/** Set the pickingGeometry field. 
 * @param val The X3DNode to set.  */
public void setPickingGeometry(X3DNode val);

/** Return the number of MFNode items in the pickTarget field. 
 * @return the number of MFNode items in the pickTarget field.  */
public int getNumPickTarget();

/** Return the pickTarget value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getPickTarget(X3DNode[] val);

/** Set the pickTarget field. 
 * @param val The X3DNode[] to set.  */
public void setPickTarget(X3DNode[] val);

/** Return the intersectionType String value. 
 * @return The intersectionType String value.  */
public String getIntersectionType();

/** Set the intersectionType field. 
 * @param val The String to set.  */
public void setIntersectionType(String val);

/** Return the number of MFNode items in the pickedGeometry field. 
 * @return the number of MFNode items in the pickedGeometry field.  */
public int getNumPickedGeometry();

/** Return the pickedGeometry value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getPickedGeometry(X3DNode[] val);

/** Return the sortOrder String value. 
 * @return The sortOrder String value.  */
public String getSortOrder();

/** Set the sortOrder field. 
 * @param val The String to set.  */
public void setSortOrder(String val);

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
