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

/** Defines the requirements of an X3D KeySensor node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface KeySensor extends X3DKeyDeviceSensorNode {

/** Return the keyPress String value. 
 * @return The keyPress String value.  */
public String getKeyPress();

/** Return the keyRelease String value. 
 * @return The keyRelease String value.  */
public String getKeyRelease();

/** Return the actionKeyPress int value. 
 * @return The actionKeyPress int value.  */
public int getActionKeyPress();

/** Return the actionKeyRelease int value. 
 * @return The actionKeyRelease int value.  */
public int getActionKeyRelease();

/** Return the shiftKey boolean value. 
 * @return The shiftKey boolean value.  */
public boolean getShiftKey();

/** Return the controlKey boolean value. 
 * @return The controlKey boolean value.  */
public boolean getControlKey();

/** Return the altKey boolean value. 
 * @return The altKey boolean value.  */
public boolean getAltKey();

}
