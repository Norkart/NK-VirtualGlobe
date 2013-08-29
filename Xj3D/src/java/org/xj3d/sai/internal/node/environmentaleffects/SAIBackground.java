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

package org.xj3d.sai.internal.node.environmentaleffects;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFColor;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.environmentaleffects.Background;

/** A concrete implementation of the Background node interface
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public class SAIBackground extends BaseNode implements Background {

/** The set_bind inputOnly field */
private SFBool set_bind;

/** The bindTime outputOnly field */
private SFTime bindTime;

/** The isBound outputOnly field */
private SFBool isBound;

/** The groundAngle inputOutput field */
private MFFloat groundAngle;

/** The groundColor inputOutput field */
private MFColor groundColor;

/** The skyAngle inputOutput field */
private MFFloat skyAngle;

/** The skyColor inputOutput field */
private MFColor skyColor;

/** The backUrl inputOutput field */
private MFString backUrl;

/** The frontUrl inputOutput field */
private MFString frontUrl;

/** The leftUrl inputOutput field */
private MFString leftUrl;

/** The rightUrl inputOutput field */
private MFString rightUrl;

/** The bottomUrl inputOutput field */
private MFString bottomUrl;

/** The topUrl inputOutput field */
private MFString topUrl;

/** The transparency inputOutput field */
private SFFloat transparency;

/** Constructor */ 
public SAIBackground ( 
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

/** Return the number of MFFloat items in the groundAngle field. 
 * @return the number of MFFloat items in the groundAngle field.  */
public int getNumGroundAngle() {
  if ( groundAngle == null ) { 
    groundAngle = (MFFloat)getField( "groundAngle" ); 
  }
  return( groundAngle.getSize( ) );
}

/** Return the groundAngle value in the argument float[]
 * @param val The float[] to initialize.  */
public void getGroundAngle(float[] val) {
  if ( groundAngle == null ) { 
    groundAngle = (MFFloat)getField( "groundAngle" ); 
  }
  groundAngle.getValue( val );
}

/** Set the groundAngle field. 
 * @param val The float[] to set.  */
public void setGroundAngle(float[] val) {
  if ( groundAngle == null ) { 
    groundAngle = (MFFloat)getField( "groundAngle" ); 
  }
  groundAngle.setValue( val.length, val );
}

/** Return the number of MFColor items in the groundColor field. 
 * @return the number of MFColor items in the groundColor field.  */
public int getNumGroundColor() {
  if ( groundColor == null ) { 
    groundColor = (MFColor)getField( "groundColor" ); 
  }
  return( groundColor.getSize( ) );
}

/** Return the groundColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getGroundColor(float[] val) {
  if ( groundColor == null ) { 
    groundColor = (MFColor)getField( "groundColor" ); 
  }
  groundColor.getValue( val );
}

/** Set the groundColor field. 
 * @param val The float[] to set.  */
public void setGroundColor(float[] val) {
  if ( groundColor == null ) { 
    groundColor = (MFColor)getField( "groundColor" ); 
  }
  groundColor.setValue( val.length/3, val );
}

/** Return the number of MFFloat items in the skyAngle field. 
 * @return the number of MFFloat items in the skyAngle field.  */
public int getNumSkyAngle() {
  if ( skyAngle == null ) { 
    skyAngle = (MFFloat)getField( "skyAngle" ); 
  }
  return( skyAngle.getSize( ) );
}

/** Return the skyAngle value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSkyAngle(float[] val) {
  if ( skyAngle == null ) { 
    skyAngle = (MFFloat)getField( "skyAngle" ); 
  }
  skyAngle.getValue( val );
}

/** Set the skyAngle field. 
 * @param val The float[] to set.  */
public void setSkyAngle(float[] val) {
  if ( skyAngle == null ) { 
    skyAngle = (MFFloat)getField( "skyAngle" ); 
  }
  skyAngle.setValue( val.length, val );
}

/** Return the number of MFColor items in the skyColor field. 
 * @return the number of MFColor items in the skyColor field.  */
public int getNumSkyColor() {
  if ( skyColor == null ) { 
    skyColor = (MFColor)getField( "skyColor" ); 
  }
  return( skyColor.getSize( ) );
}

/** Return the skyColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSkyColor(float[] val) {
  if ( skyColor == null ) { 
    skyColor = (MFColor)getField( "skyColor" ); 
  }
  skyColor.getValue( val );
}

/** Set the skyColor field. 
 * @param val The float[] to set.  */
public void setSkyColor(float[] val) {
  if ( skyColor == null ) { 
    skyColor = (MFColor)getField( "skyColor" ); 
  }
  skyColor.setValue( val.length/3, val );
}

/** Return the number of MFString items in the backUrl field. 
 * @return the number of MFString items in the backUrl field.  */
public int getNumBackUrl() {
  if ( backUrl == null ) { 
    backUrl = (MFString)getField( "backUrl" ); 
  }
  return( backUrl.getSize( ) );
}

/** Return the backUrl value in the argument String[]
 * @param val The String[] to initialize.  */
public void getBackUrl(String[] val) {
  if ( backUrl == null ) { 
    backUrl = (MFString)getField( "backUrl" ); 
  }
  backUrl.getValue( val );
}

/** Set the backUrl field. 
 * @param val The String[] to set.  */
public void setBackUrl(String[] val) {
  if ( backUrl == null ) { 
    backUrl = (MFString)getField( "backUrl" ); 
  }
  backUrl.setValue( val.length, val );
}

/** Return the number of MFString items in the frontUrl field. 
 * @return the number of MFString items in the frontUrl field.  */
public int getNumFrontUrl() {
  if ( frontUrl == null ) { 
    frontUrl = (MFString)getField( "frontUrl" ); 
  }
  return( frontUrl.getSize( ) );
}

/** Return the frontUrl value in the argument String[]
 * @param val The String[] to initialize.  */
public void getFrontUrl(String[] val) {
  if ( frontUrl == null ) { 
    frontUrl = (MFString)getField( "frontUrl" ); 
  }
  frontUrl.getValue( val );
}

/** Set the frontUrl field. 
 * @param val The String[] to set.  */
public void setFrontUrl(String[] val) {
  if ( frontUrl == null ) { 
    frontUrl = (MFString)getField( "frontUrl" ); 
  }
  frontUrl.setValue( val.length, val );
}

/** Return the number of MFString items in the leftUrl field. 
 * @return the number of MFString items in the leftUrl field.  */
public int getNumLeftUrl() {
  if ( leftUrl == null ) { 
    leftUrl = (MFString)getField( "leftUrl" ); 
  }
  return( leftUrl.getSize( ) );
}

/** Return the leftUrl value in the argument String[]
 * @param val The String[] to initialize.  */
public void getLeftUrl(String[] val) {
  if ( leftUrl == null ) { 
    leftUrl = (MFString)getField( "leftUrl" ); 
  }
  leftUrl.getValue( val );
}

/** Set the leftUrl field. 
 * @param val The String[] to set.  */
public void setLeftUrl(String[] val) {
  if ( leftUrl == null ) { 
    leftUrl = (MFString)getField( "leftUrl" ); 
  }
  leftUrl.setValue( val.length, val );
}

/** Return the number of MFString items in the rightUrl field. 
 * @return the number of MFString items in the rightUrl field.  */
public int getNumRightUrl() {
  if ( rightUrl == null ) { 
    rightUrl = (MFString)getField( "rightUrl" ); 
  }
  return( rightUrl.getSize( ) );
}

/** Return the rightUrl value in the argument String[]
 * @param val The String[] to initialize.  */
public void getRightUrl(String[] val) {
  if ( rightUrl == null ) { 
    rightUrl = (MFString)getField( "rightUrl" ); 
  }
  rightUrl.getValue( val );
}

/** Set the rightUrl field. 
 * @param val The String[] to set.  */
public void setRightUrl(String[] val) {
  if ( rightUrl == null ) { 
    rightUrl = (MFString)getField( "rightUrl" ); 
  }
  rightUrl.setValue( val.length, val );
}

/** Return the number of MFString items in the bottomUrl field. 
 * @return the number of MFString items in the bottomUrl field.  */
public int getNumBottomUrl() {
  if ( bottomUrl == null ) { 
    bottomUrl = (MFString)getField( "bottomUrl" ); 
  }
  return( bottomUrl.getSize( ) );
}

/** Return the bottomUrl value in the argument String[]
 * @param val The String[] to initialize.  */
public void getBottomUrl(String[] val) {
  if ( bottomUrl == null ) { 
    bottomUrl = (MFString)getField( "bottomUrl" ); 
  }
  bottomUrl.getValue( val );
}

/** Set the bottomUrl field. 
 * @param val The String[] to set.  */
public void setBottomUrl(String[] val) {
  if ( bottomUrl == null ) { 
    bottomUrl = (MFString)getField( "bottomUrl" ); 
  }
  bottomUrl.setValue( val.length, val );
}

/** Return the number of MFString items in the topUrl field. 
 * @return the number of MFString items in the topUrl field.  */
public int getNumTopUrl() {
  if ( topUrl == null ) { 
    topUrl = (MFString)getField( "topUrl" ); 
  }
  return( topUrl.getSize( ) );
}

/** Return the topUrl value in the argument String[]
 * @param val The String[] to initialize.  */
public void getTopUrl(String[] val) {
  if ( topUrl == null ) { 
    topUrl = (MFString)getField( "topUrl" ); 
  }
  topUrl.getValue( val );
}

/** Set the topUrl field. 
 * @param val The String[] to set.  */
public void setTopUrl(String[] val) {
  if ( topUrl == null ) { 
    topUrl = (MFString)getField( "topUrl" ); 
  }
  topUrl.setValue( val.length, val );
}

/** Return the transparency float value. 
 * @return The transparency float value.  */
public float getTransparency() {
  if ( transparency == null ) { 
    transparency = (SFFloat)getField( "transparency" ); 
  }
  return( transparency.getValue( ) );
}

/** Set the transparency field. 
 * @param val The float to set.  */
public void setTransparency(float val) {
  if ( transparency == null ) { 
    transparency = (SFFloat)getField( "transparency" ); 
  }
  transparency.setValue( val );
}

}
