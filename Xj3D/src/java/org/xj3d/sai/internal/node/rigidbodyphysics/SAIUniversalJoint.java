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

package org.xj3d.sai.internal.node.rigidbodyphysics;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.UniversalJoint;

/** A concrete implementation of the UniversalJoint node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIUniversalJoint extends BaseNode implements UniversalJoint {

/** The forceOutput inputOutput field */
private MFString forceOutput;

/** The body1 inputOutput field */
private SFNode body1;

/** The body2 inputOutput field */
private SFNode body2;

/** The anchorPoint inputOutput field */
private SFVec3f anchorPoint;

/** The axis1 inputOutput field */
private SFVec3f axis1;

/** The axis2 inputOutput field */
private SFVec3f axis2;

/** The body1AnchorPoint outputOnly field */
private SFVec3f body1AnchorPoint;

/** The body2AnchorPoint outputOnly field */
private SFVec3f body2AnchorPoint;

/** The body1Axis outputOnly field */
private SFVec3f body1Axis;

/** The body2Axis outputOnly field */
private SFVec3f body2Axis;

/** The stop1Bounce inputOutput field */
private SFFloat stop1Bounce;

/** The stop2Bounce inputOutput field */
private SFFloat stop2Bounce;

/** The stop1ErrorCorrection inputOutput field */
private SFFloat stop1ErrorCorrection;

/** The stop2ErrorCorrection inputOutput field */
private SFFloat stop2ErrorCorrection;

/** Constructor */ 
public SAIUniversalJoint ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the anchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAnchorPoint(float[] val) {
  if ( anchorPoint == null ) { 
    anchorPoint = (SFVec3f)getField( "anchorPoint" ); 
  }
  anchorPoint.getValue( val );
}

/** Set the anchorPoint field. 
 * @param val The float[] to set.  */
public void setAnchorPoint(float[] val) {
  if ( anchorPoint == null ) { 
    anchorPoint = (SFVec3f)getField( "anchorPoint" ); 
  }
  anchorPoint.setValue( val );
}

/** Return the axis1 value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAxis1(float[] val) {
  if ( axis1 == null ) { 
    axis1 = (SFVec3f)getField( "axis1" ); 
  }
  axis1.getValue( val );
}

/** Set the axis1 field. 
 * @param val The float[] to set.  */
public void setAxis1(float[] val) {
  if ( axis1 == null ) { 
    axis1 = (SFVec3f)getField( "axis1" ); 
  }
  axis1.setValue( val );
}

/** Return the axis2 value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAxis2(float[] val) {
  if ( axis2 == null ) { 
    axis2 = (SFVec3f)getField( "axis2" ); 
  }
  axis2.getValue( val );
}

/** Set the axis2 field. 
 * @param val The float[] to set.  */
public void setAxis2(float[] val) {
  if ( axis2 == null ) { 
    axis2 = (SFVec3f)getField( "axis2" ); 
  }
  axis2.setValue( val );
}

/** Return the body1AnchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody1AnchorPoint(float[] val) {
  if ( body1AnchorPoint == null ) { 
    body1AnchorPoint = (SFVec3f)getField( "body1AnchorPoint" ); 
  }
  body1AnchorPoint.getValue( val );
}

/** Return the body2AnchorPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody2AnchorPoint(float[] val) {
  if ( body2AnchorPoint == null ) { 
    body2AnchorPoint = (SFVec3f)getField( "body2AnchorPoint" ); 
  }
  body2AnchorPoint.getValue( val );
}

/** Return the body1Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody1Axis(float[] val) {
  if ( body1Axis == null ) { 
    body1Axis = (SFVec3f)getField( "body1Axis" ); 
  }
  body1Axis.getValue( val );
}

/** Return the body2Axis value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBody2Axis(float[] val) {
  if ( body2Axis == null ) { 
    body2Axis = (SFVec3f)getField( "body2Axis" ); 
  }
  body2Axis.getValue( val );
}

/** Return the stop1Bounce float value. 
 * @return The stop1Bounce float value.  */
public float getStop1Bounce() {
  if ( stop1Bounce == null ) { 
    stop1Bounce = (SFFloat)getField( "stop1Bounce" ); 
  }
  return( stop1Bounce.getValue( ) );
}

/** Set the stop1Bounce field. 
 * @param val The float to set.  */
public void setStop1Bounce(float val) {
  if ( stop1Bounce == null ) { 
    stop1Bounce = (SFFloat)getField( "stop1Bounce" ); 
  }
  stop1Bounce.setValue( val );
}

/** Return the stop2Bounce float value. 
 * @return The stop2Bounce float value.  */
public float getStop2Bounce() {
  if ( stop2Bounce == null ) { 
    stop2Bounce = (SFFloat)getField( "stop2Bounce" ); 
  }
  return( stop2Bounce.getValue( ) );
}

/** Set the stop2Bounce field. 
 * @param val The float to set.  */
public void setStop2Bounce(float val) {
  if ( stop2Bounce == null ) { 
    stop2Bounce = (SFFloat)getField( "stop2Bounce" ); 
  }
  stop2Bounce.setValue( val );
}

/** Return the stop1ErrorCorrection float value. 
 * @return The stop1ErrorCorrection float value.  */
public float getStop1ErrorCorrection() {
  if ( stop1ErrorCorrection == null ) { 
    stop1ErrorCorrection = (SFFloat)getField( "stop1ErrorCorrection" ); 
  }
  return( stop1ErrorCorrection.getValue( ) );
}

/** Set the stop1ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop1ErrorCorrection(float val) {
  if ( stop1ErrorCorrection == null ) { 
    stop1ErrorCorrection = (SFFloat)getField( "stop1ErrorCorrection" ); 
  }
  stop1ErrorCorrection.setValue( val );
}

/** Return the stop2ErrorCorrection float value. 
 * @return The stop2ErrorCorrection float value.  */
public float getStop2ErrorCorrection() {
  if ( stop2ErrorCorrection == null ) { 
    stop2ErrorCorrection = (SFFloat)getField( "stop2ErrorCorrection" ); 
  }
  return( stop2ErrorCorrection.getValue( ) );
}

/** Set the stop2ErrorCorrection field. 
 * @param val The float to set.  */
public void setStop2ErrorCorrection(float val) {
  if ( stop2ErrorCorrection == null ) { 
    stop2ErrorCorrection = (SFFloat)getField( "stop2ErrorCorrection" ); 
  }
  stop2ErrorCorrection.setValue( val );
}

}
