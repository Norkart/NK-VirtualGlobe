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

package org.xj3d.sai.internal.node.hanim;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.hanim.HAnimHumanoid;

/** A concrete implementation of the HAnimHumanoid node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIHAnimHumanoid extends BaseNode implements HAnimHumanoid {

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

/** The info inputOutput field */
private MFString info;

/** The joints inputOutput field */
private MFNode joints;

/** The segments inputOutput field */
private MFNode segments;

/** The sites inputOutput field */
private MFNode sites;

/** The skeleton inputOutput field */
private MFNode skeleton;

/** The skin inputOutput field */
private MFNode skin;

/** The skinCoord inputOutput field */
private SFNode skinCoord;

/** The skinNormal inputOutput field */
private SFNode skinNormal;

/** The version inputOutput field */
private SFString version;

/** The viewpoints inputOutput field */
private MFNode viewpoints;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** Constructor */ 
public SAIHAnimHumanoid ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the number of MFString items in the info field. 
 * @return the number of MFString items in the info field.  */
public int getNumInfo() {
  if ( info == null ) { 
    info = (MFString)getField( "info" ); 
  }
  return( info.getSize( ) );
}

/** Return the info value in the argument String[]
 * @param val The String[] to initialize.  */
public void getInfo(String[] val) {
  if ( info == null ) { 
    info = (MFString)getField( "info" ); 
  }
  info.getValue( val );
}

/** Set the info field. 
 * @param val The String[] to set.  */
public void setInfo(String[] val) {
  if ( info == null ) { 
    info = (MFString)getField( "info" ); 
  }
  info.setValue( val.length, val );
}

/** Return the number of MFNode items in the joints field. 
 * @return the number of MFNode items in the joints field.  */
public int getNumJoints() {
  if ( joints == null ) { 
    joints = (MFNode)getField( "joints" ); 
  }
  return( joints.getSize( ) );
}

/** Return the joints value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getJoints(X3DNode[] val) {
  if ( joints == null ) { 
    joints = (MFNode)getField( "joints" ); 
  }
  joints.getValue( val );
}

/** Set the joints field. 
 * @param val The X3DNode[] to set.  */
public void setJoints(X3DNode[] val) {
  if ( joints == null ) { 
    joints = (MFNode)getField( "joints" ); 
  }
  joints.setValue( val.length, val );
}

/** Return the number of MFNode items in the segments field. 
 * @return the number of MFNode items in the segments field.  */
public int getNumSegments() {
  if ( segments == null ) { 
    segments = (MFNode)getField( "segments" ); 
  }
  return( segments.getSize( ) );
}

/** Return the segments value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSegments(X3DNode[] val) {
  if ( segments == null ) { 
    segments = (MFNode)getField( "segments" ); 
  }
  segments.getValue( val );
}

/** Set the segments field. 
 * @param val The X3DNode[] to set.  */
public void setSegments(X3DNode[] val) {
  if ( segments == null ) { 
    segments = (MFNode)getField( "segments" ); 
  }
  segments.setValue( val.length, val );
}

/** Return the number of MFNode items in the sites field. 
 * @return the number of MFNode items in the sites field.  */
public int getNumSites() {
  if ( sites == null ) { 
    sites = (MFNode)getField( "sites" ); 
  }
  return( sites.getSize( ) );
}

/** Return the sites value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSites(X3DNode[] val) {
  if ( sites == null ) { 
    sites = (MFNode)getField( "sites" ); 
  }
  sites.getValue( val );
}

/** Set the sites field. 
 * @param val The X3DNode[] to set.  */
public void setSites(X3DNode[] val) {
  if ( sites == null ) { 
    sites = (MFNode)getField( "sites" ); 
  }
  sites.setValue( val.length, val );
}

/** Return the number of MFNode items in the skeleton field. 
 * @return the number of MFNode items in the skeleton field.  */
public int getNumSkeleton() {
  if ( skeleton == null ) { 
    skeleton = (MFNode)getField( "skeleton" ); 
  }
  return( skeleton.getSize( ) );
}

/** Return the skeleton value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSkeleton(X3DNode[] val) {
  if ( skeleton == null ) { 
    skeleton = (MFNode)getField( "skeleton" ); 
  }
  skeleton.getValue( val );
}

/** Set the skeleton field. 
 * @param val The X3DNode[] to set.  */
public void setSkeleton(X3DNode[] val) {
  if ( skeleton == null ) { 
    skeleton = (MFNode)getField( "skeleton" ); 
  }
  skeleton.setValue( val.length, val );
}

/** Return the number of MFNode items in the skin field. 
 * @return the number of MFNode items in the skin field.  */
public int getNumSkin() {
  if ( skin == null ) { 
    skin = (MFNode)getField( "skin" ); 
  }
  return( skin.getSize( ) );
}

/** Return the skin value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSkin(X3DNode[] val) {
  if ( skin == null ) { 
    skin = (MFNode)getField( "skin" ); 
  }
  skin.getValue( val );
}

/** Set the skin field. 
 * @param val The X3DNode[] to set.  */
public void setSkin(X3DNode[] val) {
  if ( skin == null ) { 
    skin = (MFNode)getField( "skin" ); 
  }
  skin.setValue( val.length, val );
}

/** Return the skinCoord X3DNode value. 
 * @return The skinCoord X3DNode value.  */
public X3DNode getSkinCoord() {
  if ( skinCoord == null ) { 
    skinCoord = (SFNode)getField( "skinCoord" ); 
  }
  return( skinCoord.getValue( ) );
}

/** Set the skinCoord field. 
 * @param val The X3DNode to set.  */
public void setSkinCoord(X3DNode val) {
  if ( skinCoord == null ) { 
    skinCoord = (SFNode)getField( "skinCoord" ); 
  }
  skinCoord.setValue( val );
}

/** Return the skinNormal X3DNode value. 
 * @return The skinNormal X3DNode value.  */
public X3DNode getSkinNormal() {
  if ( skinNormal == null ) { 
    skinNormal = (SFNode)getField( "skinNormal" ); 
  }
  return( skinNormal.getValue( ) );
}

/** Set the skinNormal field. 
 * @param val The X3DNode to set.  */
public void setSkinNormal(X3DNode val) {
  if ( skinNormal == null ) { 
    skinNormal = (SFNode)getField( "skinNormal" ); 
  }
  skinNormal.setValue( val );
}

/** Return the version String value. 
 * @return The version String value.  */
public String getVersion() {
  if ( version == null ) { 
    version = (SFString)getField( "version" ); 
  }
  return( version.getValue( ) );
}

/** Set the version field. 
 * @param val The String to set.  */
public void setVersion(String val) {
  if ( version == null ) { 
    version = (SFString)getField( "version" ); 
  }
  version.setValue( val );
}

/** Return the number of MFNode items in the viewpoints field. 
 * @return the number of MFNode items in the viewpoints field.  */
public int getNumViewpoints() {
  if ( viewpoints == null ) { 
    viewpoints = (MFNode)getField( "viewpoints" ); 
  }
  return( viewpoints.getSize( ) );
}

/** Return the viewpoints value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getViewpoints(X3DNode[] val) {
  if ( viewpoints == null ) { 
    viewpoints = (MFNode)getField( "viewpoints" ); 
  }
  viewpoints.getValue( val );
}

/** Set the viewpoints field. 
 * @param val The X3DNode[] to set.  */
public void setViewpoints(X3DNode[] val) {
  if ( viewpoints == null ) { 
    viewpoints = (MFNode)getField( "viewpoints" ); 
  }
  viewpoints.setValue( val.length, val );
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

}
