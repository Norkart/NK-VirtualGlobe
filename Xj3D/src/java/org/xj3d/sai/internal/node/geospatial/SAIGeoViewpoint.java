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

package org.xj3d.sai.internal.node.geospatial;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3d;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.geospatial.GeoViewpoint;

/** A concrete implementation of the GeoViewpoint node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIGeoViewpoint extends BaseNode implements GeoViewpoint {

/** The centerOfRotation inputOutput field */
private SFVec3f centerOfRotation;

/** The set_bind inputOnly field */
private SFBool set_bind;

/** The bindTime outputOnly field */
private SFTime bindTime;

/** The isBound outputOnly field */
private SFBool isBound;

/** The fieldOfView inputOutput field */
private SFFloat fieldOfView;

/** The jump inputOutput field */
private SFBool jump;

/** The orientation inputOnly field */
private SFRotation orientation;

/** The set_position inputOnly field */
private SFVec3d set_position;

/** The headlight inputOutput field */
private SFBool headlight;

/** The navType inputOutput field */
private MFString navType;

/** The description inputOutput field */
private SFString description;

/** The geoOrigin initializeOnly field */
private SFNode geoOrigin;

/** The geoSystem initializeOnly field */
private MFString geoSystem;

/** The set_orientation initializeOnly field */
private SFRotation set_orientation;

/** The position initializeOnly field */
private SFVec3d position;

/** The speedFactor initializeOnly field */
private SFFloat speedFactor;

/** The retainUserOffsets inputOutput field */
private SFBool retainUserOffsets;

/** Constructor */ 
public SAIGeoViewpoint ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the orientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getOrientation(float[] val) {
  if ( set_orientation == null ) { 
    set_orientation = (SFRotation)getField( "set_orientation" ); 
  }
  set_orientation.getValue( val );
}

/** Set the orientation field. 
 * @param val The float[] to set.  */
public void setOrientation(float[] val) {
  if ( !isRealized( ) ) { 
    if ( set_orientation == null ) { 
      set_orientation = (SFRotation)getField( "set_orientation" ); 
    } 
    set_orientation.setValue( val ); 
  } else { 
    if ( orientation == null ) { 
      orientation = (SFRotation)getField( "orientation" ); 
    } 
    orientation.setValue( val ); 
  } 
}

/** Return the position value in the argument double[]
 * @param val The double[] to initialize.  */
public void getPosition(double[] val) {
  if ( position == null ) { 
    position = (SFVec3d)getField( "position" ); 
  }
  position.getValue( val );
}

/** Set the position field. 
 * @param val The double[] to set.  */
public void setPosition(double[] val) {
  if ( !isRealized( ) ) { 
    if ( position == null ) { 
      position = (SFVec3d)getField( "position" ); 
    } 
    position.setValue( val ); 
  } else { 
    if ( set_position == null ) { 
      set_position = (SFVec3d)getField( "set_position" ); 
    } 
    set_position.setValue( val ); 
  } 
}

/** Return the centerOfRotation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenterOfRotation(float[] val) {
  if ( centerOfRotation == null ) { 
    centerOfRotation = (SFVec3f)getField( "centerOfRotation" ); 
  }
  centerOfRotation.getValue( val );
}

/** Set the centerOfRotation field. 
 * @param val The float[] to set.  */
public void setCenterOfRotation(float[] val) {
  if ( centerOfRotation == null ) { 
    centerOfRotation = (SFVec3f)getField( "centerOfRotation" ); 
  }
  centerOfRotation.setValue( val );
}

/** Set the set_bind field. 
 * @param val The boolean to set.  */
public void setBind(boolean val) {
  if ( set_bind == null ) { 
    set_bind = (SFBool)getField( "set_bind" ); 
  }
  set_bind.setValue( val );
}

/** Return the bindTime double value. 
 * @return The bindTime double value.  */
public double getBindTime() {
  if ( bindTime == null ) { 
    bindTime = (SFTime)getField( "bindTime" ); 
  }
  return( bindTime.getValue( ) );
}

/** Return the isBound boolean value. 
 * @return The isBound boolean value.  */
public boolean getIsBound() {
  if ( isBound == null ) { 
    isBound = (SFBool)getField( "isBound" ); 
  }
  return( isBound.getValue( ) );
}

/** Return the fieldOfView float value. 
 * @return The fieldOfView float value.  */
public float getFieldOfView() {
  if ( fieldOfView == null ) { 
    fieldOfView = (SFFloat)getField( "fieldOfView" ); 
  }
  return( fieldOfView.getValue( ) );
}

/** Set the fieldOfView field. 
 * @param val The float to set.  */
public void setFieldOfView(float val) {
  if ( fieldOfView == null ) { 
    fieldOfView = (SFFloat)getField( "fieldOfView" ); 
  }
  fieldOfView.setValue( val );
}

/** Return the jump boolean value. 
 * @return The jump boolean value.  */
public boolean getJump() {
  if ( jump == null ) { 
    jump = (SFBool)getField( "jump" ); 
  }
  return( jump.getValue( ) );
}

/** Set the jump field. 
 * @param val The boolean to set.  */
public void setJump(boolean val) {
  if ( jump == null ) { 
    jump = (SFBool)getField( "jump" ); 
  }
  jump.setValue( val );
}

/** Return the headlight boolean value. 
 * @return The headlight boolean value.  */
public boolean getHeadlight() {
  if ( headlight == null ) { 
    headlight = (SFBool)getField( "headlight" ); 
  }
  return( headlight.getValue( ) );
}

/** Set the headlight field. 
 * @param val The boolean to set.  */
public void setHeadlight(boolean val) {
  if ( headlight == null ) { 
    headlight = (SFBool)getField( "headlight" ); 
  }
  headlight.setValue( val );
}

/** Return the number of MFString items in the navType field. 
 * @return the number of MFString items in the navType field.  */
public int getNumNavType() {
  if ( navType == null ) { 
    navType = (MFString)getField( "navType" ); 
  }
  return( navType.getSize( ) );
}

/** Return the navType value in the argument String[]
 * @param val The String[] to initialize.  */
public void getNavType(String[] val) {
  if ( navType == null ) { 
    navType = (MFString)getField( "navType" ); 
  }
  navType.getValue( val );
}

/** Set the navType field. 
 * @param val The String[] to set.  */
public void setNavType(String[] val) {
  if ( navType == null ) { 
    navType = (MFString)getField( "navType" ); 
  }
  navType.setValue( val.length, val );
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

/** Return the geoOrigin X3DNode value. 
 * @return The geoOrigin X3DNode value.  */
public X3DNode getGeoOrigin() {
  if ( geoOrigin == null ) { 
    geoOrigin = (SFNode)getField( "geoOrigin" ); 
  }
  return( geoOrigin.getValue( ) );
}

/** Set the geoOrigin field. 
 * @param val The X3DNode to set.  */
public void setGeoOrigin(X3DNode val) {
  if ( geoOrigin == null ) { 
    geoOrigin = (SFNode)getField( "geoOrigin" ); 
  }
  geoOrigin.setValue( val );
}

/** Return the number of MFString items in the geoSystem field. 
 * @return the number of MFString items in the geoSystem field.  */
public int getNumGeoSystem() {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  return( geoSystem.getSize( ) );
}

/** Return the geoSystem value in the argument String[]
 * @param val The String[] to initialize.  */
public void getGeoSystem(String[] val) {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  geoSystem.getValue( val );
}

/** Set the geoSystem field. 
 * @param val The String[] to set.  */
public void setGeoSystem(String[] val) {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  geoSystem.setValue( val.length, val );
}

/** Return the speedFactor float value. 
 * @return The speedFactor float value.  */
public float getSpeedFactor() {
  if ( speedFactor == null ) { 
    speedFactor = (SFFloat)getField( "speedFactor" ); 
  }
  return( speedFactor.getValue( ) );
}

/** Set the speedFactor field. 
 * @param val The float to set.  */
public void setSpeedFactor(float val) {
  if ( speedFactor == null ) { 
    speedFactor = (SFFloat)getField( "speedFactor" ); 
  }
  speedFactor.setValue( val );
}

/** Return the retainUserOffsets boolean value. 
 * @return The retainUserOffsets boolean value.  */
public boolean getRetainUserOffsets() {
  if ( retainUserOffsets == null ) { 
    retainUserOffsets = (SFBool)getField( "retainUserOffsets" ); 
  }
  return( retainUserOffsets.getValue( ) );
}

/** Set the retainUserOffsets field. 
 * @param val The boolean to set.  */
public void setRetainUserOffsets(boolean val) {
  if ( retainUserOffsets == null ) { 
    retainUserOffsets = (SFBool)getField( "retainUserOffsets" ); 
  }
  retainUserOffsets.setValue( val );
}

}
