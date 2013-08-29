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

package org.xj3d.sai.external.node.navigation;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.navigation.Viewpoint;

/** A concrete implementation of the Viewpoint node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIViewpoint extends SAINode implements Viewpoint {

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

/** The orientation inputOutput field */
private SFRotation orientation;

/** The position inputOutput field */
private SFVec3f position;

/** The description inputOutput field */
private SFString description;

/** The centerOfRotation inputOutput field */
private SFVec3f centerOfRotation;

/** The retainUserOffsets inputOutput field */
private SFBool retainUserOffsets;

/** Constructor */ 
public SAIViewpoint ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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

/** Return the orientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getOrientation(float[] val) {
  if ( orientation == null ) { 
    orientation = (SFRotation)getField( "orientation" ); 
  }
  orientation.getValue( val );
}

/** Set the orientation field. 
 * @param val The float[] to set.  */
public void setOrientation(float[] val) {
  if ( orientation == null ) { 
    orientation = (SFRotation)getField( "orientation" ); 
  }
  orientation.setValue( val );
}

/** Return the position value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPosition(float[] val) {
  if ( position == null ) { 
    position = (SFVec3f)getField( "position" ); 
  }
  position.getValue( val );
}

/** Set the position field. 
 * @param val The float[] to set.  */
public void setPosition(float[] val) {
  if ( position == null ) { 
    position = (SFVec3f)getField( "position" ); 
  }
  position.setValue( val );
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
