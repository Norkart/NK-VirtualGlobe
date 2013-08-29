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

/** Defines the requirements of an X3D MetadataString node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface MetadataString extends X3DNode, X3DMetadataObject {

/** Return the number of MFString items in the value field. 
 * @return the number of MFString items in the value field.  */
public int getNumValue();

/** Return the value value in the argument String[]
 * @param val The String[] to initialize.  */
public void getValue(String[] val);

/** Set the value field. 
 * @param val The String[] to set.  */
public void setValue(String[] val);

}
