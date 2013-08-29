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

package org.web3d.x3d.sai.navigation;

import org.web3d.x3d.sai.X3DBindableNode;

/** Defines the requirements of an X3D NavigationInfo node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface NavigationInfo extends X3DBindableNode {

/** Return the number of MFFloat items in the avatarSize field. 
 * @return the number of MFFloat items in the avatarSize field.  */
public int getNumAvatarSize();

/** Return the avatarSize value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAvatarSize(float[] val);

/** Set the avatarSize field. 
 * @param val The float[] to set.  */
public void setAvatarSize(float[] val);

/** Return the headlight boolean value. 
 * @return The headlight boolean value.  */
public boolean getHeadlight();

/** Set the headlight field. 
 * @param val The boolean to set.  */
public void setHeadlight(boolean val);

/** Return the speed float value. 
 * @return The speed float value.  */
public float getSpeed();

/** Set the speed field. 
 * @param val The float to set.  */
public void setSpeed(float val);

/** Return the number of MFString items in the type field. 
 * @return the number of MFString items in the type field.  */
public int getNumType();

/** Return the type value in the argument String[]
 * @param val The String[] to initialize.  */
public void getType(String[] val);

/** Set the type field. 
 * @param val The String[] to set.  */
public void setType(String[] val);

/** Return the visibilityLimit float value. 
 * @return The visibilityLimit float value.  */
public float getVisibilityLimit();

/** Set the visibilityLimit field. 
 * @param val The float to set.  */
public void setVisibilityLimit(float val);

/** Return the number of MFString items in the transitionType field. 
 * @return the number of MFString items in the transitionType field.  */
public int getNumTransitionType();

/** Return the transitionType value in the argument String[]
 * @param val The String[] to initialize.  */
public void getTransitionType(String[] val);

/** Set the transitionType field. 
 * @param val The String[] to set.  */
public void setTransitionType(String[] val);

/** Return the number of MFFloat items in the transitionTime field. 
 * @return the number of MFFloat items in the transitionTime field.  */
public int getNumTransitionTime();

/** Return the transitionTime value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTransitionTime(float[] val);

/** Set the transitionTime field. 
 * @param val The float[] to set.  */
public void setTransitionTime(float[] val);

}
