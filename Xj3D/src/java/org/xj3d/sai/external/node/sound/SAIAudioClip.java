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

package org.xj3d.sai.external.node.sound;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.sound.AudioClip;

/** A concrete implementation of the AudioClip node interface
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public class SAIAudioClip extends SAINode implements AudioClip {

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

/** The description inputOutput field */
private SFString description;

/** The pitch inputOutput field */
private SFFloat pitch;

/** The url inputOutput field */
private MFString url;

/** The duration_changed outputOnly field */
private SFTime duration_changed;

/** The isActive outputOnly field */
private SFBool isActive;

/** The isPaused outputOnly field */
private SFBool isPaused;

/** Constructor */ 
public SAIAudioClip ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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

/** Return the description String value. 
 * @return The description String value.  */
public String getDescription() {
  if ( description == null ) { 
    description = (SFString)getField( "description" ); 
  }
  return( description.getValue( ) );
}

/** Set the description field. 
 * @param val The String to set.  */
public void setDescription(String val) {
  if ( description == null ) { 
    description = (SFString)getField( "description" ); 
  }
  description.setValue( val );
}

/** Return the pitch float value. 
 * @return The pitch float value.  */
public float getPitch() {
  if ( pitch == null ) { 
    pitch = (SFFloat)getField( "pitch" ); 
  }
  return( pitch.getValue( ) );
}

/** Set the pitch field. 
 * @param val The float to set.  */
public void setPitch(float val) {
  if ( pitch == null ) { 
    pitch = (SFFloat)getField( "pitch" ); 
  }
  pitch.setValue( val );
}

/** Return the number of MFString items in the url field. 
 * @return the number of MFString items in the url field.  */
public int getNumUrl() {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  return( url.getSize( ) );
}

/** Return the url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.getValue( val );
}

/** Set the url field. 
 * @param val The String[] to set.  */
public void setUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.setValue( val.length, val );
}

/** Return the duration_changed double value. 
 * @return The duration_changed double value.  */
public double getDuration() {
  if ( duration_changed == null ) { 
    duration_changed = (SFTime)getField( "duration_changed" ); 
  }
  return( duration_changed.getValue( ) );
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

}
