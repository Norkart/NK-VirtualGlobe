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

package org.xj3d.sai.internal.node.navigation;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.navigation.LOD;

/** A concrete implementation of the LOD node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAILOD extends BaseNode implements LOD {

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

/** The center initializeOnly field */
private SFVec3f center;

/** The range initializeOnly field */
private MFFloat range;

/** The level_changed outputOnly field */
private SFInt32 level_changed;

/** The forceTransitions initializeOnly field */
private SFBool forceTransitions;

/** Constructor */ 
public SAILOD ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the number of MFFloat items in the range field. 
 * @return the number of MFFloat items in the range field.  */
public int getNumRange() {
  if ( range == null ) { 
    range = (MFFloat)getField( "range" ); 
  }
  return( range.getSize( ) );
}

/** Return the range value in the argument float[]
 * @param val The float[] to initialize.  */
public void getRange(float[] val) {
  if ( range == null ) { 
    range = (MFFloat)getField( "range" ); 
  }
  range.getValue( val );
}

/** Set the range field. 
 * @param val The float[] to set.  */
public void setRange(float[] val) {
  if ( range == null ) { 
    range = (MFFloat)getField( "range" ); 
  }
  range.setValue( val.length, val );
}

/** Return the level_changed int value. 
 * @return The level_changed int value.  */
public int getLevel() {
  if ( level_changed == null ) { 
    level_changed = (SFInt32)getField( "level_changed" ); 
  }
  return( level_changed.getValue( ) );
}

/** Return the forceTransitions boolean value. 
 * @return The forceTransitions boolean value.  */
public boolean getForceTransitions() {
  if ( forceTransitions == null ) { 
    forceTransitions = (SFBool)getField( "forceTransitions" ); 
  }
  return( forceTransitions.getValue( ) );
}

/** Set the forceTransitions field. 
 * @param val The boolean to set.  */
public void setForceTransitions(boolean val) {
  if ( forceTransitions == null ) { 
    forceTransitions = (SFBool)getField( "forceTransitions" ); 
  }
  forceTransitions.setValue( val );
}

}
