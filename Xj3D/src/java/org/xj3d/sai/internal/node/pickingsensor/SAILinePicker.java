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

package org.xj3d.sai.internal.node.pickingsensor;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.MFVec3f;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.pickingsensor.LinePicker;

/** A concrete implementation of the LinePicker node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAILinePicker extends BaseNode implements LinePicker {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The pickingGeometry inputOutput field */
private SFNode pickingGeometry;

/** The pickTarget inputOutput field */
private MFNode pickTarget;

/** The intersectionType initializeOnly field */
private SFString intersectionType;

/** The pickedGeometry outputOnly field */
private MFNode pickedGeometry;

/** The sortOrder initializeOnly field */
private SFString sortOrder;

/** The objectType inputOutput field */
private MFString objectType;

/** The pickedPoint outputOnly field */
private MFVec3f pickedPoint;

/** The pickedNormal outputOnly field */
private MFVec3f pickedNormal;

/** The pickedTextureCoordinate outputOnly field */
private MFVec3f pickedTextureCoordinate;

/** Constructor */ 
public SAILinePicker ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the pickingGeometry X3DNode value. 
 * @return The pickingGeometry X3DNode value.  */
public X3DNode getPickingGeometry() {
  if ( pickingGeometry == null ) { 
    pickingGeometry = (SFNode)getField( "pickingGeometry" ); 
  }
  return( pickingGeometry.getValue( ) );
}

/** Set the pickingGeometry field. 
 * @param val The X3DNode to set.  */
public void setPickingGeometry(X3DNode val) {
  if ( pickingGeometry == null ) { 
    pickingGeometry = (SFNode)getField( "pickingGeometry" ); 
  }
  pickingGeometry.setValue( val );
}

/** Return the number of MFNode items in the pickTarget field. 
 * @return the number of MFNode items in the pickTarget field.  */
public int getNumPickTarget() {
  if ( pickTarget == null ) { 
    pickTarget = (MFNode)getField( "pickTarget" ); 
  }
  return( pickTarget.getSize( ) );
}

/** Return the pickTarget value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getPickTarget(X3DNode[] val) {
  if ( pickTarget == null ) { 
    pickTarget = (MFNode)getField( "pickTarget" ); 
  }
  pickTarget.getValue( val );
}

/** Set the pickTarget field. 
 * @param val The X3DNode[] to set.  */
public void setPickTarget(X3DNode[] val) {
  if ( pickTarget == null ) { 
    pickTarget = (MFNode)getField( "pickTarget" ); 
  }
  pickTarget.setValue( val.length, val );
}

/** Return the intersectionType String value. 
 * @return The intersectionType String value.  */
public String getIntersectionType() {
  if ( intersectionType == null ) { 
    intersectionType = (SFString)getField( "intersectionType" ); 
  }
  return( intersectionType.getValue( ) );
}

/** Set the intersectionType field. 
 * @param val The String to set.  */
public void setIntersectionType(String val) {
  if ( intersectionType == null ) { 
    intersectionType = (SFString)getField( "intersectionType" ); 
  }
  intersectionType.setValue( val );
}

/** Return the number of MFNode items in the pickedGeometry field. 
 * @return the number of MFNode items in the pickedGeometry field.  */
public int getNumPickedGeometry() {
  if ( pickedGeometry == null ) { 
    pickedGeometry = (MFNode)getField( "pickedGeometry" ); 
  }
  return( pickedGeometry.getSize( ) );
}

/** Return the pickedGeometry value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getPickedGeometry(X3DNode[] val) {
  if ( pickedGeometry == null ) { 
    pickedGeometry = (MFNode)getField( "pickedGeometry" ); 
  }
  pickedGeometry.getValue( val );
}

/** Return the sortOrder String value. 
 * @return The sortOrder String value.  */
public String getSortOrder() {
  if ( sortOrder == null ) { 
    sortOrder = (SFString)getField( "sortOrder" ); 
  }
  return( sortOrder.getValue( ) );
}

/** Set the sortOrder field. 
 * @param val The String to set.  */
public void setSortOrder(String val) {
  if ( sortOrder == null ) { 
    sortOrder = (SFString)getField( "sortOrder" ); 
  }
  sortOrder.setValue( val );
}

/** Return the number of MFString items in the objectType field. 
 * @return the number of MFString items in the objectType field.  */
public int getNumObjectType() {
  if ( objectType == null ) { 
    objectType = (MFString)getField( "objectType" ); 
  }
  return( objectType.getSize( ) );
}

/** Return the objectType value in the argument String[]
 * @param val The String[] to initialize.  */
public void getObjectType(String[] val) {
  if ( objectType == null ) { 
    objectType = (MFString)getField( "objectType" ); 
  }
  objectType.getValue( val );
}

/** Set the objectType field. 
 * @param val The String[] to set.  */
public void setObjectType(String[] val) {
  if ( objectType == null ) { 
    objectType = (MFString)getField( "objectType" ); 
  }
  objectType.setValue( val.length, val );
}

/** Return the number of MFVec3f items in the pickedPoint field. 
 * @return the number of MFVec3f items in the pickedPoint field.  */
public int getNumPickedPoint() {
  if ( pickedPoint == null ) { 
    pickedPoint = (MFVec3f)getField( "pickedPoint" ); 
  }
  return( pickedPoint.getSize( ) );
}

/** Return the pickedPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPickedPoint(float[] val) {
  if ( pickedPoint == null ) { 
    pickedPoint = (MFVec3f)getField( "pickedPoint" ); 
  }
  pickedPoint.getValue( val );
}

/** Return the number of MFVec3f items in the pickedNormal field. 
 * @return the number of MFVec3f items in the pickedNormal field.  */
public int getNumPickedNormal() {
  if ( pickedNormal == null ) { 
    pickedNormal = (MFVec3f)getField( "pickedNormal" ); 
  }
  return( pickedNormal.getSize( ) );
}

/** Return the pickedNormal value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPickedNormal(float[] val) {
  if ( pickedNormal == null ) { 
    pickedNormal = (MFVec3f)getField( "pickedNormal" ); 
  }
  pickedNormal.getValue( val );
}

/** Return the number of MFVec3f items in the pickedTextureCoordinate field. 
 * @return the number of MFVec3f items in the pickedTextureCoordinate field.  */
public int getNumPickedTextureCoordinate() {
  if ( pickedTextureCoordinate == null ) { 
    pickedTextureCoordinate = (MFVec3f)getField( "pickedTextureCoordinate" ); 
  }
  return( pickedTextureCoordinate.getSize( ) );
}

/** Return the pickedTextureCoordinate value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPickedTextureCoordinate(float[] val) {
  if ( pickedTextureCoordinate == null ) { 
    pickedTextureCoordinate = (MFVec3f)getField( "pickedTextureCoordinate" ); 
  }
  pickedTextureCoordinate.getValue( val );
}

}
