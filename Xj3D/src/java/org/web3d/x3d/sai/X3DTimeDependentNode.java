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

/** Defines the requirements of an X3DTimeDependentNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.4 $ */
public interface X3DTimeDependentNode extends X3DChildNode {

/** Return the loop boolean value. 
 * @return The loop boolean value.  */
public boolean getLoop();

/** Set the loop field. 
 * @param val The boolean to set.  */
public void setLoop(boolean val);

/** Return the startTime double value. 
 * @return The startTime double value.  */
public double getStartTime();

/** Set the startTime field. 
 * @param val The double to set.  */
public void setStartTime(double val);

/** Return the stopTime double value. 
 * @return The stopTime double value.  */
public double getStopTime();

/** Set the stopTime field. 
 * @param val The double to set.  */
public void setStopTime(double val);

/** Return the pauseTime double value. 
 * @return The pauseTime double value.  */
public double getPauseTime();

/** Set the pauseTime field. 
 * @param val The double to set.  */
public void setPauseTime(double val);

/** Return the resumeTime double value. 
 * @return The resumeTime double value.  */
public double getResumeTime();

/** Set the resumeTime field. 
 * @param val The double to set.  */
public void setResumeTime(double val);

/** Return the elapsedTime double value. 
 * @return The elapsedTime double value.  */
public double getElapsedTime();

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive();

/** Return the isPaused boolean value. 
 * @return The isPaused boolean value.  */
public boolean getIsPaused();

}
