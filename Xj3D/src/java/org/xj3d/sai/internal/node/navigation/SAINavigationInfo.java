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
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.navigation.NavigationInfo;

/** A concrete implementation of the NavigationInfo node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAINavigationInfo extends BaseNode implements NavigationInfo {

/** The set_bind inputOnly field */
private SFBool set_bind;

/** The bindTime outputOnly field */
private SFTime bindTime;

/** The isBound outputOnly field */
private SFBool isBound;

/** The avatarSize inputOutput field */
private MFFloat avatarSize;

/** The headlight inputOutput field */
private SFBool headlight;

/** The speed inputOutput field */
private SFFloat speed;

/** The type inputOutput field */
private MFString type;

/** The visibilityLimit inputOutput field */
private SFFloat visibilityLimit;

/** The transitionType inputOutput field */
private MFString transitionType;

/** The transitionTime inputOutput field */
private MFFloat transitionTime;

/** Constructor */ 
public SAINavigationInfo ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the number of MFFloat items in the avatarSize field. 
 * @return the number of MFFloat items in the avatarSize field.  */
public int getNumAvatarSize() {
  if ( avatarSize == null ) { 
    avatarSize = (MFFloat)getField( "avatarSize" ); 
  }
  return( avatarSize.getSize( ) );
}

/** Return the avatarSize value in the argument float[]
 * @param val The float[] to initialize.  */
public void getAvatarSize(float[] val) {
  if ( avatarSize == null ) { 
    avatarSize = (MFFloat)getField( "avatarSize" ); 
  }
  avatarSize.getValue( val );
}

/** Set the avatarSize field. 
 * @param val The float[] to set.  */
public void setAvatarSize(float[] val) {
  if ( avatarSize == null ) { 
    avatarSize = (MFFloat)getField( "avatarSize" ); 
  }
  avatarSize.setValue( val.length, val );
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

/** Return the speed float value. 
 * @return The speed float value.  */
public float getSpeed() {
  if ( speed == null ) { 
    speed = (SFFloat)getField( "speed" ); 
  }
  return( speed.getValue( ) );
}

/** Set the speed field. 
 * @param val The float to set.  */
public void setSpeed(float val) {
  if ( speed == null ) { 
    speed = (SFFloat)getField( "speed" ); 
  }
  speed.setValue( val );
}

/** Return the number of MFString items in the type field. 
 * @return the number of MFString items in the type field.  */
public int getNumType() {
  if ( type == null ) { 
    type = (MFString)getField( "type" ); 
  }
  return( type.getSize( ) );
}

/** Return the type value in the argument String[]
 * @param val The String[] to initialize.  */
public void getType(String[] val) {
  if ( type == null ) { 
    type = (MFString)getField( "type" ); 
  }
  type.getValue( val );
}

/** Set the type field. 
 * @param val The String[] to set.  */
public void setType(String[] val) {
  if ( type == null ) { 
    type = (MFString)getField( "type" ); 
  }
  type.setValue( val.length, val );
}

/** Return the visibilityLimit float value. 
 * @return The visibilityLimit float value.  */
public float getVisibilityLimit() {
  if ( visibilityLimit == null ) { 
    visibilityLimit = (SFFloat)getField( "visibilityLimit" ); 
  }
  return( visibilityLimit.getValue( ) );
}

/** Set the visibilityLimit field. 
 * @param val The float to set.  */
public void setVisibilityLimit(float val) {
  if ( visibilityLimit == null ) { 
    visibilityLimit = (SFFloat)getField( "visibilityLimit" ); 
  }
  visibilityLimit.setValue( val );
}

/** Return the number of MFString items in the transitionType field. 
 * @return the number of MFString items in the transitionType field.  */
public int getNumTransitionType() {
  if ( transitionType == null ) { 
    transitionType = (MFString)getField( "transitionType" ); 
  }
  return( transitionType.getSize( ) );
}

/** Return the transitionType value in the argument String[]
 * @param val The String[] to initialize.  */
public void getTransitionType(String[] val) {
  if ( transitionType == null ) { 
    transitionType = (MFString)getField( "transitionType" ); 
  }
  transitionType.getValue( val );
}

/** Set the transitionType field. 
 * @param val The String[] to set.  */
public void setTransitionType(String[] val) {
  if ( transitionType == null ) { 
    transitionType = (MFString)getField( "transitionType" ); 
  }
  transitionType.setValue( val.length, val );
}

/** Return the number of MFFloat items in the transitionTime field. 
 * @return the number of MFFloat items in the transitionTime field.  */
public int getNumTransitionTime() {
  if ( transitionTime == null ) { 
    transitionTime = (MFFloat)getField( "transitionTime" ); 
  }
  return( transitionTime.getSize( ) );
}

/** Return the transitionTime value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTransitionTime(float[] val) {
  if ( transitionTime == null ) { 
    transitionTime = (MFFloat)getField( "transitionTime" ); 
  }
  transitionTime.getValue( val );
}

/** Set the transitionTime field. 
 * @param val The float[] to set.  */
public void setTransitionTime(float[] val) {
  if ( transitionTime == null ) { 
    transitionTime = (MFFloat)getField( "transitionTime" ); 
  }
  transitionTime.setValue( val.length, val );
}

}
