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
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.hanim.HAnimSegment;

/** A concrete implementation of the HAnimSegment node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIHAnimSegment extends BaseNode implements HAnimSegment {

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

/** The centerOfMass inputOutput field */
private SFVec3f centerOfMass;

/** The coord inputOutput field */
private SFNode coord;

/** The displacers inputOutput field */
private MFNode displacers;

/** The mass inputOutput field */
private SFFloat mass;

/** The momentsOfInertia inputOutput field */
private MFFloat momentsOfInertia;

/** The name inputOutput field */
private SFString name;

/** Constructor */ 
public SAIHAnimSegment ( 
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

/** Return the centerOfMass value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenterOfMass(float[] val) {
  if ( centerOfMass == null ) { 
    centerOfMass = (SFVec3f)getField( "centerOfMass" ); 
  }
  centerOfMass.getValue( val );
}

/** Set the centerOfMass field. 
 * @param val The float[] to set.  */
public void setCenterOfMass(float[] val) {
  if ( centerOfMass == null ) { 
    centerOfMass = (SFVec3f)getField( "centerOfMass" ); 
  }
  centerOfMass.setValue( val );
}

/** Return the coord X3DNode value. 
 * @return The coord X3DNode value.  */
public X3DNode getCoord() {
  if ( coord == null ) { 
    coord = (SFNode)getField( "coord" ); 
  }
  return( coord.getValue( ) );
}

/** Set the coord field. 
 * @param val The X3DCoordinateNode to set.  */
public void setCoord(X3DCoordinateNode val) {
  if ( coord == null ) { 
    coord = (SFNode)getField( "coord" ); 
  }
  coord.setValue( val );
}

/** Set the coord field. 
 * @param val The X3DProtoInstance to set.  */
public void setCoord(X3DProtoInstance val) {
  if ( coord == null ) { 
    coord = (SFNode)getField( "coord" ); 
  }
  coord.setValue( val );
}

/** Return the number of MFNode items in the displacers field. 
 * @return the number of MFNode items in the displacers field.  */
public int getNumDisplacers() {
  if ( displacers == null ) { 
    displacers = (MFNode)getField( "displacers" ); 
  }
  return( displacers.getSize( ) );
}

/** Return the displacers value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getDisplacers(X3DNode[] val) {
  if ( displacers == null ) { 
    displacers = (MFNode)getField( "displacers" ); 
  }
  displacers.getValue( val );
}

/** Set the displacers field. 
 * @param val The X3DNode[] to set.  */
public void setDisplacers(X3DNode[] val) {
  if ( displacers == null ) { 
    displacers = (MFNode)getField( "displacers" ); 
  }
  displacers.setValue( val.length, val );
}

/** Return the mass float value. 
 * @return The mass float value.  */
public float getMass() {
  if ( mass == null ) { 
    mass = (SFFloat)getField( "mass" ); 
  }
  return( mass.getValue( ) );
}

/** Set the mass field. 
 * @param val The float to set.  */
public void setMass(float val) {
  if ( mass == null ) { 
    mass = (SFFloat)getField( "mass" ); 
  }
  mass.setValue( val );
}

/** Return the number of MFFloat items in the momentsOfInertia field. 
 * @return the number of MFFloat items in the momentsOfInertia field.  */
public int getNumMomentsOfInertia() {
  if ( momentsOfInertia == null ) { 
    momentsOfInertia = (MFFloat)getField( "momentsOfInertia" ); 
  }
  return( momentsOfInertia.getSize( ) );
}

/** Return the momentsOfInertia value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMomentsOfInertia(float[] val) {
  if ( momentsOfInertia == null ) { 
    momentsOfInertia = (MFFloat)getField( "momentsOfInertia" ); 
  }
  momentsOfInertia.getValue( val );
}

/** Set the momentsOfInertia field. 
 * @param val The float[] to set.  */
public void setMomentsOfInertia(float[] val) {
  if ( momentsOfInertia == null ) { 
    momentsOfInertia = (MFFloat)getField( "momentsOfInertia" ); 
  }
  momentsOfInertia.setValue( val.length, val );
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

}
