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

package org.xj3d.sai.external.node.rigidbodyphysics;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.SliderJoint;

/** A concrete implementation of the SliderJoint node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAISliderJoint extends SAINode implements SliderJoint {

/** The forceOutput inputOutput field */
private MFString forceOutput;

/** The body1 inputOutput field */
private SFNode body1;

/** The body2 inputOutput field */
private SFNode body2;

/** The axis inputOutput field */
private SFVec3f axis;

/** The minSeparation inputOutput field */
private SFFloat minSeparation;

/** The maxSeparation inputOutput field */
private SFFloat maxSeparation;

/** The separation outputOnly field */
private SFFloat separation;

/** The separationRate outputOnly field */
private SFFloat separationRate;

/** The stopBounce inputOutput field */
private SFFloat stopBounce;

/** The stopErrorCorrection inputOutput field */
private SFFloat stopErrorCorrection;

/** Constructor */ 
public SAISliderJoint ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFString items in the forceOutput field. 
 * @return the number of MFString items in the forceOutput field.  */
public int getNumForceOutput() {
  if ( forceOutput == null ) { 
    forceOutput = (MFString)getField( "forceOutput" ); 
  }
  return( forceOutput.getSize( ) );
}

/** Return the forceOutput value in the argument String[]
 * @param val The String[] to initialize.  */
public void getForceOutput(String[] val) {
  if ( forceOutput == null ) { 
    forceOutput = (MFString)getField( "forceOutput" ); 
  }
  forceOutput.getValue( val );
}

/** Set the forceOutput field. 
 * @param val The String[] to set.  */
public void setForceOutput(String[] val) {
  if ( forceOutput == null ) { 
    forceOutput = (MFString)getField( "forceOutput" ); 
  }
  forceOutput.setValue( val.length, val );
}

/** Return the body1 X3DNode value. 
 * @return The body1 X3DNode value.  */
public X3DNode getBody1() {
  if ( body1 == null ) { 
    body1 = (SFNode)getField( "body1" ); 
  }
  return( body1.getValue( ) );
}

/** Set the body1 field. 
 * @param val The X3DNode to set.  */
public void setBody1(X3DNode val) {
  if ( body1 == null ) { 
    body1 = (SFNode)getField( "body1" ); 
  }
  body1.setValue( val );
}

/** Return the body2 X3DNode value. 
 * @return The body2 X3DNode value.  */
public X3DNode getBody2() {
  if ( body2 == null ) { 
    body2 = (SFNode)getField( "body2" ); 
  }
  return( body2.getValue( ) );
}

/** Set the body2 field. 
 * @param val The X3DNode to set.  */
public void setBody2(X3DNode val) {
  if ( body2 == null ) { 
    body2 = (SFNode)getField( "body2" ); 
  }
  body2.setValue( val );
}

/** Return the axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAxis(float[] val) {
  if ( axis == null ) { 
    axis = (SFVec3f)getField( "axis" ); 
  }
  axis.getValue( val );
}

/** Set the axis field. 
 * @param val The float[] to set.  */
public void setAxis(float[] val) {
  if ( axis == null ) { 
    axis = (SFVec3f)getField( "axis" ); 
  }
  axis.setValue( val );
}

/** Return the minSeparation float value. 
 * @return The minSeparation float value.  */
public float getMinSeparation() {
  if ( minSeparation == null ) { 
    minSeparation = (SFFloat)getField( "minSeparation" ); 
  }
  return( minSeparation.getValue( ) );
}

/** Set the minSeparation field. 
 * @param val The float to set.  */
public void setMinSeparation(float val) {
  if ( minSeparation == null ) { 
    minSeparation = (SFFloat)getField( "minSeparation" ); 
  }
  minSeparation.setValue( val );
}

/** Return the maxSeparation float value. 
 * @return The maxSeparation float value.  */
public float getMaxSeparation() {
  if ( maxSeparation == null ) { 
    maxSeparation = (SFFloat)getField( "maxSeparation" ); 
  }
  return( maxSeparation.getValue( ) );
}

/** Set the maxSeparation field. 
 * @param val The float to set.  */
public void setMaxSeparation(float val) {
  if ( maxSeparation == null ) { 
    maxSeparation = (SFFloat)getField( "maxSeparation" ); 
  }
  maxSeparation.setValue( val );
}

/** Return the separation float value. 
 * @return The separation float value.  */
public float getSeparation() {
  if ( separation == null ) { 
    separation = (SFFloat)getField( "separation" ); 
  }
  return( separation.getValue( ) );
}

/** Return the separationRate float value. 
 * @return The separationRate float value.  */
public float getSeparationRate() {
  if ( separationRate == null ) { 
    separationRate = (SFFloat)getField( "separationRate" ); 
  }
  return( separationRate.getValue( ) );
}

/** Return the stopBounce float value. 
 * @return The stopBounce float value.  */
public float getStopBounce() {
  if ( stopBounce == null ) { 
    stopBounce = (SFFloat)getField( "stopBounce" ); 
  }
  return( stopBounce.getValue( ) );
}

/** Set the stopBounce field. 
 * @param val The float to set.  */
public void setStopBounce(float val) {
  if ( stopBounce == null ) { 
    stopBounce = (SFFloat)getField( "stopBounce" ); 
  }
  stopBounce.setValue( val );
}

/** Return the stopErrorCorrection float value. 
 * @return The stopErrorCorrection float value.  */
public float getStopErrorCorrection() {
  if ( stopErrorCorrection == null ) { 
    stopErrorCorrection = (SFFloat)getField( "stopErrorCorrection" ); 
  }
  return( stopErrorCorrection.getValue( ) );
}

/** Set the stopErrorCorrection field. 
 * @param val The float to set.  */
public void setStopErrorCorrection(float val) {
  if ( stopErrorCorrection == null ) { 
    stopErrorCorrection = (SFFloat)getField( "stopErrorCorrection" ); 
  }
  stopErrorCorrection.setValue( val );
}

}
