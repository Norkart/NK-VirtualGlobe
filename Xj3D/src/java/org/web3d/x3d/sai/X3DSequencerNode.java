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

import org.web3d.x3d.sai.X3DChildNode;

/** Defines the requirements of an X3DSequencerNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.4 $ */
public interface X3DSequencerNode extends X3DChildNode {

/** Set the previous field. 
 * @param val The boolean to set.  */
public void setPrevious(boolean val);

/** Set the next field. 
 * @param val The boolean to set.  */
public void setNext(boolean val);

/** Set the fraction field. 
 * @param val The float to set.  */
public void setFraction(float val);

/** Return the number of MFFloat items in the key field. 
 * @return the number of MFFloat items in the key field.  */
public int getNumKey();

/** Return the key value in the argument float[]
 * @param val The float[] to initialize.  */
public void getKey(float[] val);

/** Set the key field. 
 * @param val The float[] to set.  */
public void setKey(float[] val);

}
