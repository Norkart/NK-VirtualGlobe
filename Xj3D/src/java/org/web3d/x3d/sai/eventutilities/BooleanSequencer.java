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

/** Defines the requirements of an X3D BooleanSequencer node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface BooleanSequencer extends X3DSequencerNode {

/** Return the number of MFBool items in the keyValue field. 
 * @return the number of MFBool items in the keyValue field.  */
public int getNumKeyValue();

/** Return the keyValue value in the argument boolean[]
 * @param val The boolean[] to initialize.  */
public void getKeyValue(boolean[] val);

/** Set the keyValue field. 
 * @param val The boolean[] to set.  */
public void setKeyValue(boolean[] val);

/** Return the value boolean value. 
 * @return The value boolean value.  */
public boolean getValue();

}
