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

package org.xj3d.sai.external.node.hanim;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.hanim.HAnimJoint;

/** A concrete implementation of the HAnimJoint node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIHAnimJoint extends SAINode implements HAnimJoint {

/** The children inputOutput field */
private MFNode children;

/** The addChildren inputOnly field */
private MFNode addChildren;

/** The removeChildren inputOnly field */
private MFNode removeChildren;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** The center inputOutput field */
private SFVec3f center;

/** The rotation inputOutput field */
private SFRotation rotation;

/** The scale inputOutput field */
private SFVec3f scale;

/** The scaleOrientation inputOutput field */
private SFRotation scaleOrientation;

/** The translation inputOutput field */
private SFVec3f translation;

/** The name inputOutput field */
private SFString name;

/** The limitOrientation inputOutput field */
private SFRotation limitOrientation;

/** The llimit inputOutput field */
private MFFloat llimit;

/** The skinCoordIndex inputOutput field */
private MFInt32 skinCoordIndex;

/** The skinCoordWeight inputOutput field */
private MFFloat skinCoordWeight;

/** The stiffness inputOutput field */
private MFFloat stiffness;

/** The ulimit inputOutput field */
private MFFloat ulimit;

/** Constructor */ 
public SAIHAnimJoint ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFNode items in the children field. 
 * @return the number of MFNode items in the children field.  */
public int getNumChildren() {
  if ( children == null ) { 
    children = (MFNode)getField( "children" ); 
  }
  return( children.getSize( ) );
}

/** Return the children value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getChildren(X3DNode[] val) {
  if ( children == null ) { 
    children = (MFNode)getField( "children" ); 
  }
  children.getValue( val );
}

/** Set the children field. 
 * @param val The X3DNode[] to set.  */
public void setChildren(X3DNode[] val) {
  if ( children == null ) { 
    children = (MFNode)getField( "children" ); 
  }
  children.setValue( val.length, val );
}

/** Set the addChildren field. 
 * @param val The X3DNode[] to set.  */
public void addChildren(X3DNode[] val) {
  if ( addChildren == null ) { 
    addChildren = (MFNode)getField( "addChildren" ); 
  }
  addChildren.setValue( val.length, val );
}

/** Set the removeChildren field. 
 * @param val The X3DNode[] to set.  */
public void removeChildren(X3DNode[] val) {
  if ( removeChildren == null ) { 
    removeChildren = (MFNode)getField( "removeChildren" ); 
  }
  removeChildren.setValue( val.length, val );
}

/** Return the bboxCenter value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBboxCenter(float[] val) {
  if ( bboxCenter == null ) { 
    bboxCenter = (SFVec3f)getField( "bboxCenter" ); 
  }
  bboxCenter.getValue( val );
}

/** Set the bboxCenter field. 
 * @param val The float[] to set.  */
public void setBboxCenter(float[] val) {
  if ( bboxCenter == null ) { 
    bboxCenter = (SFVec3f)getField( "bboxCenter" ); 
  }
  bboxCenter.setValue( val );
}

/** Return the bboxSize value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBboxSize(float[] val) {
  if ( bboxSize == null ) { 
    bboxSize = (SFVec3f)getField( "bboxSize" ); 
  }
  bboxSize.getValue( val );
}

/** Set the bboxSize field. 
 * @param val The float[] to set.  */
public void setBboxSize(float[] val) {
  if ( bboxSize == null ) { 
    bboxSize = (SFVec3f)getField( "bboxSize" ); 
  }
  bboxSize.setValue( val );
}

/** Return the center value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenter(float[] val) {
  if ( center == null ) { 
    center = (SFVec3f)getField( "center" ); 
  }
  center.getValue( val );
}

/** Set the center field. 
 * @param val The float[] to set.  */
public void setCenter(float[] val) {
  if ( center == null ) { 
    center = (SFVec3f)getField( "center" ); 
  }
  center.setValue( val );
}

/** Return the rotation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getRotation(float[] val) {
  if ( rotation == null ) { 
    rotation = (SFRotation)getField( "rotation" ); 
  }
  rotation.getValue( val );
}

/** Set the rotation field. 
 * @param val The float[] to set.  */
public void setRotation(float[] val) {
  if ( rotation == null ) { 
    rotation = (SFRotation)getField( "rotation" ); 
  }
  rotation.setValue( val );
}

/** Return the scale value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScale(float[] val) {
  if ( scale == null ) { 
    scale = (SFVec3f)getField( "scale" ); 
  }
  scale.getValue( val );
}

/** Set the scale field. 
 * @param val The float[] to set.  */
public void setScale(float[] val) {
  if ( scale == null ) { 
    scale = (SFVec3f)getField( "scale" ); 
  }
  scale.setValue( val );
}

/** Return the scaleOrientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScaleOrientation(float[] val) {
  if ( scaleOrientation == null ) { 
    scaleOrientation = (SFRotation)getField( "scaleOrientation" ); 
  }
  scaleOrientation.getValue( val );
}

/** Set the scaleOrientation field. 
 * @param val The float[] to set.  */
public void setScaleOrientation(float[] val) {
  if ( scaleOrientation == null ) { 
    scaleOrientation = (SFRotation)getField( "scaleOrientation" ); 
  }
  scaleOrientation.setValue( val );
}

/** Return the translation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTranslation(float[] val) {
  if ( translation == null ) { 
    translation = (SFVec3f)getField( "translation" ); 
  }
  translation.getValue( val );
}

/** Set the translation field. 
 * @param val The float[] to set.  */
public void setTranslation(float[] val) {
  if ( translation == null ) { 
    translation = (SFVec3f)getField( "translation" ); 
  }
  translation.setValue( val );
}

/** Return the name String value. 
 * @return The name String value.  */
public String getName() {
  if ( name == null ) { 
    name = (SFString)getField( "name" ); 
  }
  return( name.getValue( ) );
}

/** Set the name field. 
 * @param val The String to set.  */
public void setName(String val) {
  if ( name == null ) { 
    name = (SFString)getField( "name" ); 
  }
  name.setValue( val );
}

/** Return the limitOrientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLimitOrientation(float[] val) {
  if ( limitOrientation == null ) { 
    limitOrientation = (SFRotation)getField( "limitOrientation" ); 
  }
  limitOrientation.getValue( val );
}

/** Set the limitOrientation field. 
 * @param val The float[] to set.  */
public void setLimitOrientation(float[] val) {
  if ( limitOrientation == null ) { 
    limitOrientation = (SFRotation)getField( "limitOrientation" ); 
  }
  limitOrientation.setValue( val );
}

/** Return the number of MFFloat items in the llimit field. 
 * @return the number of MFFloat items in the llimit field.  */
public int getNumLlimit() {
  if ( llimit == null ) { 
    llimit = (MFFloat)getField( "llimit" ); 
  }
  return( llimit.getSize( ) );
}

/** Return the llimit value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLlimit(float[] val) {
  if ( llimit == null ) { 
    llimit = (MFFloat)getField( "llimit" ); 
  }
  llimit.getValue( val );
}

/** Set the llimit field. 
 * @param val The float[] to set.  */
public void setLlimit(float[] val) {
  if ( llimit == null ) { 
    llimit = (MFFloat)getField( "llimit" ); 
  }
  llimit.setValue( val.length, val );
}

/** Return the number of MFInt32 items in the skinCoordIndex field. 
 * @return the number of MFInt32 items in the skinCoordIndex field.  */
public int getNumSkinCoordIndex() {
  if ( skinCoordIndex == null ) { 
    skinCoordIndex = (MFInt32)getField( "skinCoordIndex" ); 
  }
  return( skinCoordIndex.getSize( ) );
}

/** Return the skinCoordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getSkinCoordIndex(int[] val) {
  if ( skinCoordIndex == null ) { 
    skinCoordIndex = (MFInt32)getField( "skinCoordIndex" ); 
  }
  skinCoordIndex.getValue( val );
}

/** Set the skinCoordIndex field. 
 * @param val The int[] to set.  */
public void setSkinCoordIndex(int[] val) {
  if ( skinCoordIndex == null ) { 
    skinCoordIndex = (MFInt32)getField( "skinCoordIndex" ); 
  }
  skinCoordIndex.setValue( val.length, val );
}

/** Return the number of MFFloat items in the skinCoordWeight field. 
 * @return the number of MFFloat items in the skinCoordWeight field.  */
public int getNumSkinCoordWeight() {
  if ( skinCoordWeight == null ) { 
    skinCoordWeight = (MFFloat)getField( "skinCoordWeight" ); 
  }
  return( skinCoordWeight.getSize( ) );
}

/** Return the skinCoordWeight value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSkinCoordWeight(float[] val) {
  if ( skinCoordWeight == null ) { 
    skinCoordWeight = (MFFloat)getField( "skinCoordWeight" ); 
  }
  skinCoordWeight.getValue( val );
}

/** Set the skinCoordWeight field. 
 * @param val The float[] to set.  */
public void setSkinCoordWeight(float[] val) {
  if ( skinCoordWeight == null ) { 
    skinCoordWeight = (MFFloat)getField( "skinCoordWeight" ); 
  }
  skinCoordWeight.setValue( val.length, val );
}

/** Return the number of MFFloat items in the stiffness field. 
 * @return the number of MFFloat items in the stiffness field.  */
public int getNumStiffness() {
  if ( stiffness == null ) { 
    stiffness = (MFFloat)getField( "stiffness" ); 
  }
  return( stiffness.getSize( ) );
}

/** Return the stiffness value in the argument float[]
 * @param val The float[] to initialize.  */
public void getStiffness(float[] val) {
  if ( stiffness == null ) { 
    stiffness = (MFFloat)getField( "stiffness" ); 
  }
  stiffness.getValue( val );
}

/** Set the stiffness field. 
 * @param val The float[] to set.  */
public void setStiffness(float[] val) {
  if ( stiffness == null ) { 
    stiffness = (MFFloat)getField( "stiffness" ); 
  }
  stiffness.setValue( val.length, val );
}

/** Return the number of MFFloat items in the ulimit field. 
 * @return the number of MFFloat items in the ulimit field.  */
public int getNumUlimit() {
  if ( ulimit == null ) { 
    ulimit = (MFFloat)getField( "ulimit" ); 
  }
  return( ulimit.getSize( ) );
}

/** Return the ulimit value in the argument float[]
 * @param val The float[] to initialize.  */
public void getUlimit(float[] val) {
  if ( ulimit == null ) { 
    ulimit = (MFFloat)getField( "ulimit" ); 
  }
  ulimit.getValue( val );
}

/** Set the ulimit field. 
 * @param val The float[] to set.  */
public void setUlimit(float[] val) {
  if ( ulimit == null ) { 
    ulimit = (MFFloat)getField( "ulimit" ); 
  }
  ulimit.setValue( val.length, val );
}

}
