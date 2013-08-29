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

package org.web3d.x3d.sai.eventutilities;

import org.web3d.x3d.sai.X3DSequencerNode;

/** Defines the requirements of an X3D IntegerSequencer node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface IntegerSequencer extends X3DSequencerNode {

/** Return the number of MFInt32 items in the keyValue field. 
 * @return the number of MFInt32 items in the keyValue field.  */
public int getNumKeyValue();

/** Return the keyValue value in the argument int[]
 * @param val The int[] to initialize.  */
public void getKeyValue(int[] val);

/** Set the keyValue field. 
 * @param val The int[] to set.  */
public void setKeyValue(int[] val);

/** Return the value int value. 
 * @return The value int value.  */
public int getValue();

}
