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

package org.web3d.x3d.sai.keydevicesensor;

import org.web3d.x3d.sai.X3DKeyDeviceSensorNode;

/** Defines the requirements of an X3D StringSensor node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface StringSensor extends X3DKeyDeviceSensorNode {

/** Return the deletionAllowed boolean value. 
 * @return The deletionAllowed boolean value.  */
public boolean getDeletionAllowed();

/** Set the deletionAllowed field. 
 * @param val The boolean to set.  */
public void setDeletionAllowed(boolean val);

/** Return the enteredText String value. 
 * @return The enteredText String value.  */
public String getEnteredText();

/** Return the finalText String value. 
 * @return The finalText String value.  */
public String getFinalText();

}
