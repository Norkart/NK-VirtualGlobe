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

package org.xj3d.sai.internal.node.time;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.time.TimeSensor;

/** A concrete implementation of the TimeSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITimeSensor extends BaseNode implements TimeSensor {

/** The loop inputOutput field */
private SFBool loop;

/** The startTime inputOutput field */
private SFTime startTime;

/** The stopTime inputOutput field */
private SFTime stopTime;

/** The pauseTime inputOutput field */
private SFTime pauseTime;

/** The resumeTime inputOutput field */
private SFTime resumeTime;

/** The elapsedTime outputOnly field */
private SFTime elapsedTime;

/** The cycleInterval inputOutput field */
private SFTime cycleInterval;

/** The fraction_changed outputOnly field */
private SFFloat fraction_changed;

/** The time outputOnly field */
private SFTime time;

/** The cycleTime outputOnly field */
private SFTime cycleTime;

/** The isActive outputOnly field */
private SFBool isActive;

/** The isPaused outputOnly field */
private SFBool isPaused;

/** The enabled inputOutput field */
private SFBool enabled;

/** Constructor */ 
public SAITimeSensor ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the loop boolean value. 
 * @return The loop boolean value.  */
public boolean getLoop() {
  if ( loop == null ) { 
    loop = (SFBool)getField( "loop" ); 
  }
  return( loop.getValue( ) );
}

/** Set the loop field. 
 * @param val The boolean to set.  */
public void setLoop(boolean val) {
  if ( loop == null ) { 
    loop = (SFBool)getField( "loop" ); 
  }
  loop.setValue( val );
}

/** Return the startTime double value. 
 * @return The startTime double value.  */
public double getStartTime() {
  if ( startTime == null ) { 
    startTime = (SFTime)getField( "startTime" ); 
  }
  return( startTime.getValue( ) );
}

/** Set the startTime field. 
 * @param val The double to set.  */
public void setStartTime(double val) {
  if ( startTime == null ) { 
    startTime = (SFTime)getField( "startTime" ); 
  }
  startTime.setValue( val );
}

/** Return the stopTime double value. 
 * @return The stopTime double value.  */
public double getStopTime() {
  if ( stopTime == null ) { 
    stopTime = (SFTime)getField( "stopTime" ); 
  }
  return( stopTime.getValue( ) );
}

/** Set the stopTime field. 
 * @param val The double to set.  */
public void setStopTime(double val) {
  if ( stopTime == null ) { 
    stopTime = (SFTime)getField( "stopTime" ); 
  }
  stopTime.setValue( val );
}

/** Return the pauseTime double value. 
 * @return The pauseTime double value.  */
public double getPauseTime() {
  if ( pauseTime == null ) { 
    pauseTime = (SFTime)getField( "pauseTime" ); 
  }
  return( pauseTime.getValue( ) );
}

/** Set the pauseTime field. 
 * @param val The double to set.  */
public void setPauseTime(double val) {
  if ( pauseTime == null ) { 
    pauseTime = (SFTime)getField( "pauseTime" ); 
  }
  pauseTime.setValue( val );
}

/** Return the resumeTime double value. 
 * @return The resumeTime double value.  */
public double getResumeTime() {
  if ( resumeTime == null ) { 
    resumeTime = (SFTime)getField( "resumeTime" ); 
  }
  return( resumeTime.getValue( ) );
}

/** Set the resumeTime field. 
 * @param val The double to set.  */
public void setResumeTime(double val) {
  if ( resumeTime == null ) { 
    resumeTime = (SFTime)getField( "resumeTime" ); 
  }
  resumeTime.setValue( val );
}

/** Return the elapsedTime double value. 
 * @return The elapsedTime double value.  */
public double getElapsedTime() {
  if ( elapsedTime == null ) { 
    elapsedTime = (SFTime)getField( "elapsedTime" ); 
  }
  return( elapsedTime.getValue( ) );
}

/** Return the cycleInterval double value. 
 * @return The cycleInterval double value.  */
public double getCycleInterval() {
  if ( cycleInterval == null ) { 
    cycleInterval = (SFTime)getField( "cycleInterval" ); 
  }
  return( cycleInterval.getValue( ) );
}

/** Set the cycleInterval field. 
 * @param val The double to set.  */
public void setCycleInterval(double val) {
  if ( cycleInterval == null ) { 
    cycleInterval = (SFTime)getField( "cycleInterval" ); 
  }
  cycleInterval.setValue( val );
}

/** Return the fraction_changed float value. 
 * @return The fraction_changed float value.  */
public float getFraction() {
  if ( fraction_changed == null ) { 
    fraction_changed = (SFFloat)getField( "fraction_changed" ); 
  }
  return( fraction_changed.getValue( ) );
}

/** Return the time double value. 
 * @return The time double value.  */
public double getTime() {
  if ( time == null ) { 
    time = (SFTime)getField( "time" ); 
  }
  return( time.getValue( ) );
}

/** Return the cycleTime double value. 
 * @return The cycleTime double value.  */
public double getCycleTime() {
  if ( cycleTime == null ) { 
    cycleTime = (SFTime)getField( "cycleTime" ); 
  }
  return( cycleTime.getValue( ) );
}

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the isPaused boolean value. 
 * @return The isPaused boolean value.  */
public boolean getIsPaused() {
  if ( isPaused == null ) { 
    isPaused = (SFBool)getField( "isPaused" ); 
  }
  return( isPaused.getValue( ) );
}

/** Return the enabled boolean value. 
 * @return The enabled boolean value.  */
public boolean getEnabled() {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  return( enabled.getValue( ) );
}

/** Set the enabled field. 
 * @param val The boolean to set.  */
public void setEnabled(boolean val) {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  enabled.setValue( val );
}

}
