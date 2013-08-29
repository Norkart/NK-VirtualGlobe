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
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.sound.Sound;

/** A concrete implementation of the Sound node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAISound extends SAINode implements Sound {

/** The direction inputOutput field */
private SFVec3f direction;

/** The intensity inputOutput field */
private SFFloat intensity;

/** The location inputOutput field */
private SFVec3f location;

/** The maxBack inputOutput field */
private SFFloat maxBack;

/** The maxFront inputOutput field */
private SFFloat maxFront;

/** The minBack inputOutput field */
private SFFloat minBack;

/** The minFront inputOutput field */
private SFFloat minFront;

/** The priority inputOutput field */
private SFFloat priority;

/** The source inputOutput field */
private SFNode source;

/** The spatialize initializeOnly field */
private SFBool spatialize;

/** Constructor */ 
public SAISound ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the direction value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDirection(float[] val) {
  if ( direction == null ) { 
    direction = (SFVec3f)getField( "direction" ); 
  }
  direction.getValue( val );
}

/** Set the direction field. 
 * @param val The float[] to set.  */
public void setDirection(float[] val) {
  if ( direction == null ) { 
    direction = (SFVec3f)getField( "direction" ); 
  }
  direction.setValue( val );
}

/** Return the intensity float value. 
 * @return The intensity float value.  */
public float getIntensity() {
  if ( intensity == null ) { 
    intensity = (SFFloat)getField( "intensity" ); 
  }
  return( intensity.getValue( ) );
}

/** Set the intensity field. 
 * @param val The float to set.  */
public void setIntensity(float val) {
  if ( intensity == null ) { 
    intensity = (SFFloat)getField( "intensity" ); 
  }
  intensity.setValue( val );
}

/** Return the location value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLocation(float[] val) {
  if ( location == null ) { 
    location = (SFVec3f)getField( "location" ); 
  }
  location.getValue( val );
}

/** Set the location field. 
 * @param val The float[] to set.  */
public void setLocation(float[] val) {
  if ( location == null ) { 
    location = (SFVec3f)getField( "location" ); 
  }
  location.setValue( val );
}

/** Return the maxBack float value. 
 * @return The maxBack float value.  */
public float getMaxBack() {
  if ( maxBack == null ) { 
    maxBack = (SFFloat)getField( "maxBack" ); 
  }
  return( maxBack.getValue( ) );
}

/** Set the maxBack field. 
 * @param val The float to set.  */
public void setMaxBack(float val) {
  if ( maxBack == null ) { 
    maxBack = (SFFloat)getField( "maxBack" ); 
  }
  maxBack.setValue( val );
}

/** Return the maxFront float value. 
 * @return The maxFront float value.  */
public float getMaxFront() {
  if ( maxFront == null ) { 
    maxFront = (SFFloat)getField( "maxFront" ); 
  }
  return( maxFront.getValue( ) );
}

/** Set the maxFront field. 
 * @param val The float to set.  */
public void setMaxFront(float val) {
  if ( maxFront == null ) { 
    maxFront = (SFFloat)getField( "maxFront" ); 
  }
  maxFront.setValue( val );
}

/** Return the minBack float value. 
 * @return The minBack float value.  */
public float getMinBack() {
  if ( minBack == null ) { 
    minBack = (SFFloat)getField( "minBack" ); 
  }
  return( minBack.getValue( ) );
}

/** Set the minBack field. 
 * @param val The float to set.  */
public void setMinBack(float val) {
  if ( minBack == null ) { 
    minBack = (SFFloat)getField( "minBack" ); 
  }
  minBack.setValue( val );
}

/** Return the minFront float value. 
 * @return The minFront float value.  */
public float getMinFront() {
  if ( minFront == null ) { 
    minFront = (SFFloat)getField( "minFront" ); 
  }
  return( minFront.getValue( ) );
}

/** Set the minFront field. 
 * @param val The float to set.  */
public void setMinFront(float val) {
  if ( minFront == null ) { 
    minFront = (SFFloat)getField( "minFront" ); 
  }
  minFront.setValue( val );
}

/** Return the priority float value. 
 * @return The priority float value.  */
public float getPriority() {
  if ( priority == null ) { 
    priority = (SFFloat)getField( "priority" ); 
  }
  return( priority.getValue( ) );
}

/** Set the priority field. 
 * @param val The float to set.  */
public void setPriority(float val) {
  if ( priority == null ) { 
    priority = (SFFloat)getField( "priority" ); 
  }
  priority.setValue( val );
}

/** Return the source X3DNode value. 
 * @return The source X3DNode value.  */
public X3DNode getSource() {
  if ( source == null ) { 
    source = (SFNode)getField( "source" ); 
  }
  return( source.getValue( ) );
}

/** Set the source field. 
 * @param val The X3DNode to set.  */
public void setSource(X3DNode val) {
  if ( source == null ) { 
    source = (SFNode)getField( "source" ); 
  }
  source.setValue( val );
}

/** Return the spatialize boolean value. 
 * @return The spatialize boolean value.  */
public boolean getSpatialize() {
  if ( spatialize == null ) { 
    spatialize = (SFBool)getField( "spatialize" ); 
  }
  return( spatialize.getValue( ) );
}

/** Set the spatialize field. 
 * @param val The boolean to set.  */
public void setSpatialize(boolean val) {
  if ( spatialize == null ) { 
    spatialize = (SFBool)getField( "spatialize" ); 
  }
  spatialize.setValue( val );
}

}
