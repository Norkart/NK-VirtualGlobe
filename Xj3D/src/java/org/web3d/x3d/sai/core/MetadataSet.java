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

package org.web3d.x3d.sai.core;

import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D MetadataSet node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface MetadataSet extends X3DNode, X3DMetadataObject {

/** Return the number of MFNode items in the value field. 
 * @return the number of MFNode items in the value field.  */
public int getNumValue();

/** Return the value value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getValue(X3DNode[] val);

/** Set the value field. 
 * @param val The X3DNode[] to set.  */
public void setValue(X3DNode[] val);

}
