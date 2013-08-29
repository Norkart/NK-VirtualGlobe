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

package org.xj3d.sai.external.node.dis;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.dis.EspduTransform;

/** A concrete implementation of the EspduTransform node interface
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public class SAIEspduTransform extends SAINode implements EspduTransform {

/** The enabled inputOutput field */
private SFBool enabled;

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

/** The marking inputOutput field */
private SFString marking;

/** The siteID inputOutput field */
private SFInt32 siteID;

/** The applicationID inputOutput field */
private SFInt32 applicationID;

/** The entityID inputOutput field */
private SFInt32 entityID;

/** The readInterval inputOutput field */
private SFTime readInterval;

/** The writeInterval inputOutput field */
private SFTime writeInterval;

/** The networkMode inputOutput field */
private SFString networkMode;

/** The address inputOutput field */
private SFString address;

/** The port inputOutput field */
private SFInt32 port;

/** The articulationParameterCount inputOutput field */
private SFInt32 articulationParameterCount;

/** The articulationParameterArray inputOutput field */
private MFFloat articulationParameterArray;

/** The articulationParameterValue0_changed outputOnly field */
private SFFloat articulationParameterValue0_changed;

/** The articulationParameterValue1_changed outputOnly field */
private SFFloat articulationParameterValue1_changed;

/** The articulationParameterValue2_changed outputOnly field */
private SFFloat articulationParameterValue2_changed;

/** The articulationParameterValue3_changed outputOnly field */
private SFFloat articulationParameterValue3_changed;

/** The articulationParameterValue4_changed outputOnly field */
private SFFloat articulationParameterValue4_changed;

/** The articulationParameterValue5_changed outputOnly field */
private SFFloat articulationParameterValue5_changed;

/** The articulationParameterValue6_changed outputOnly field */
private SFFloat articulationParameterValue6_changed;

/** The articulationParameterValue7_changed outputOnly field */
private SFFloat articulationParameterValue7_changed;

/** The isActive outputOnly field */
private SFBool isActive;

/** The set_articulationParameterValue0 inputOnly field */
private SFFloat set_articulationParameterValue0;

/** The set_articulationParameterValue1 inputOnly field */
private SFFloat set_articulationParameterValue1;

/** The set_articulationParameterValue2 inputOnly field */
private SFFloat set_articulationParameterValue2;

/** The set_articulationParameterValue3 inputOnly field */
private SFFloat set_articulationParameterValue3;

/** The set_articulationParameterValue4 inputOnly field */
private SFFloat set_articulationParameterValue4;

/** The set_articulationParameterValue5 inputOnly field */
private SFFloat set_articulationParameterValue5;

/** The set_articulationParameterValue6 inputOnly field */
private SFFloat set_articulationParameterValue6;

/** The set_articulationParameterValue7 inputOnly field */
private SFFloat set_articulationParameterValue7;

/** The timestamp outputOnly field */
private SFTime timestamp;

/** The detonationResult inputOutput field */
private SFInt32 detonationResult;

/** The detonationLocation inputOutput field */
private SFVec3f detonationLocation;

/** The detonationRelativeLocation inputOutput field */
private SFVec3f detonationRelativeLocation;

/** The isDetonated outputOnly field */
private SFBool isDetonated;

/** The detonateTime outputOnly field */
private SFTime detonateTime;

/** The eventApplicationID inputOutput field */
private SFInt32 eventApplicationID;

/** The eventEntityID inputOutput field */
private SFInt32 eventEntityID;

/** The eventSiteID inputOutput field */
private SFInt32 eventSiteID;

/** The fired1 inputOutput field */
private SFBool fired1;

/** The fired2 inputOutput field */
private SFBool fired2;

/** The fireMissionIndex inputOutput field */
private SFInt32 fireMissionIndex;

/** The firingRange inputOutput field */
private SFFloat firingRange;

/** The firingRate inputOutput field */
private SFInt32 firingRate;

/** The munitionApplicationID inputOutput field */
private SFInt32 munitionApplicationID;

/** The munitionEndPoint inputOutput field */
private SFVec3f munitionEndPoint;

/** The munitionEntityID inputOutput field */
private SFInt32 munitionEntityID;

/** The munitionSiteID inputOutput field */
private SFInt32 munitionSiteID;

/** The munitionStartPoint inputOutput field */
private SFVec3f munitionStartPoint;

/** The firedTime inputOutput field */
private SFTime firedTime;

/** The geoSystem inputOutput field */
private MFString geoSystem;

/** The geoOrigin initializeOnly field */
private SFNode geoOrigin;

/** The entityCategory inputOutput field */
private SFInt32 entityCategory;

/** The entityDomain inputOutput field */
private SFInt32 entityDomain;

/** The entityExtra inputOutput field */
private SFInt32 entityExtra;

/** The entityKind inputOutput field */
private SFInt32 entityKind;

/** The entitySpecific inputOutput field */
private SFInt32 entitySpecific;

/** The entityCountry inputOutput field */
private SFInt32 entityCountry;

/** The entitySubCategory inputOutput field */
private SFInt32 entitySubCategory;

/** The appearance inputOutput field */
private SFInt32 appearance;

/** The linearVelocity inputOutput field */
private SFVec3f linearVelocity;

/** The linearAcceleration inputOutput field */
private SFVec3f linearAcceleration;

/** The forceID inputOutput field */
private SFInt32 forceID;

/** The xmppParams inputOutput field */
private MFString xmppParams;

/** Constructor */ 
public SAIEspduTransform ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the articulationParameterValue0 float value. 
 * @return The articulationParameterValue0 float value.  */
public float getArticulationParameterValue0() {
  if ( articulationParameterValue0_changed == null ) { 
    articulationParameterValue0_changed = (SFFloat)getField( "articulationParameterValue0_changed" ); 
  }
  return( articulationParameterValue0_changed.getValue( ) );
}

/** Set the articulationParameterValue0 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue0(float val) {
  if ( set_articulationParameterValue0 == null ) { 
    set_articulationParameterValue0 = (SFFloat)getField( "set_articulationParameterValue0" ); 
  }
  set_articulationParameterValue0.setValue( val );
}

/** Return the articulationParameterValue1 float value. 
 * @return The articulationParameterValue1 float value.  */
public float getArticulationParameterValue1() {
  if ( articulationParameterValue1_changed == null ) { 
    articulationParameterValue1_changed = (SFFloat)getField( "articulationParameterValue1_changed" ); 
  }
  return( articulationParameterValue1_changed.getValue( ) );
}

/** Set the articulationParameterValue1 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue1(float val) {
  if ( set_articulationParameterValue1 == null ) { 
    set_articulationParameterValue1 = (SFFloat)getField( "set_articulationParameterValue1" ); 
  }
  set_articulationParameterValue1.setValue( val );
}

/** Return the articulationParameterValue2 float value. 
 * @return The articulationParameterValue2 float value.  */
public float getArticulationParameterValue2() {
  if ( articulationParameterValue2_changed == null ) { 
    articulationParameterValue2_changed = (SFFloat)getField( "articulationParameterValue2_changed" ); 
  }
  return( articulationParameterValue2_changed.getValue( ) );
}

/** Set the articulationParameterValue2 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue2(float val) {
  if ( set_articulationParameterValue2 == null ) { 
    set_articulationParameterValue2 = (SFFloat)getField( "set_articulationParameterValue2" ); 
  }
  set_articulationParameterValue2.setValue( val );
}

/** Return the articulationParameterValue3 float value. 
 * @return The articulationParameterValue3 float value.  */
public float getArticulationParameterValue3() {
  if ( articulationParameterValue3_changed == null ) { 
    articulationParameterValue3_changed = (SFFloat)getField( "articulationParameterValue3_changed" ); 
  }
  return( articulationParameterValue3_changed.getValue( ) );
}

/** Set the articulationParameterValue3 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue3(float val) {
  if ( set_articulationParameterValue3 == null ) { 
    set_articulationParameterValue3 = (SFFloat)getField( "set_articulationParameterValue3" ); 
  }
  set_articulationParameterValue3.setValue( val );
}

/** Return the articulationParameterValue4 float value. 
 * @return The articulationParameterValue4 float value.  */
public float getArticulationParameterValue4() {
  if ( articulationParameterValue4_changed == null ) { 
    articulationParameterValue4_changed = (SFFloat)getField( "articulationParameterValue4_changed" ); 
  }
  return( articulationParameterValue4_changed.getValue( ) );
}

/** Set the articulationParameterValue4 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue4(float val) {
  if ( set_articulationParameterValue4 == null ) { 
    set_articulationParameterValue4 = (SFFloat)getField( "set_articulationParameterValue4" ); 
  }
  set_articulationParameterValue4.setValue( val );
}

/** Return the articulationParameterValue5 float value. 
 * @return The articulationParameterValue5 float value.  */
public float getArticulationParameterValue5() {
  if ( articulationParameterValue5_changed == null ) { 
    articulationParameterValue5_changed = (SFFloat)getField( "articulationParameterValue5_changed" ); 
  }
  return( articulationParameterValue5_changed.getValue( ) );
}

/** Set the articulationParameterValue5 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue5(float val) {
  if ( set_articulationParameterValue5 == null ) { 
    set_articulationParameterValue5 = (SFFloat)getField( "set_articulationParameterValue5" ); 
  }
  set_articulationParameterValue5.setValue( val );
}

/** Return the articulationParameterValue6 float value. 
 * @return The articulationParameterValue6 float value.  */
public float getArticulationParameterValue6() {
  if ( articulationParameterValue6_changed == null ) { 
    articulationParameterValue6_changed = (SFFloat)getField( "articulationParameterValue6_changed" ); 
  }
  return( articulationParameterValue6_changed.getValue( ) );
}

/** Set the articulationParameterValue6 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue6(float val) {
  if ( set_articulationParameterValue6 == null ) { 
    set_articulationParameterValue6 = (SFFloat)getField( "set_articulationParameterValue6" ); 
  }
  set_articulationParameterValue6.setValue( val );
}

/** Return the articulationParameterValue7 float value. 
 * @return The articulationParameterValue7 float value.  */
public float getArticulationParameterValue7() {
  if ( articulationParameterValue7_changed == null ) { 
    articulationParameterValue7_changed = (SFFloat)getField( "articulationParameterValue7_changed" ); 
  }
  return( articulationParameterValue7_changed.getValue( ) );
}

/** Set the articulationParameterValue7 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue7(float val) {
  if ( set_articulationParameterValue7 == null ) { 
    set_articulationParameterValue7 = (SFFloat)getField( "set_articulationParameterValue7" ); 
  }
  set_articulationParameterValue7.setValue( val );
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

/** Return the marking String value. 
 * @return The marking String value.  */
public String getMarking() {
  if ( marking == null ) { 
    marking = (SFString)getField( "marking" ); 
  }
  return( marking.getValue( ) );
}

/** Set the marking field. 
 * @param val The String to set.  */
public void setMarking(String val) {
  if ( marking == null ) { 
    marking = (SFString)getField( "marking" ); 
  }
  marking.setValue( val );
}

/** Return the siteID int value. 
 * @return The siteID int value.  */
public int getSiteID() {
  if ( siteID == null ) { 
    siteID = (SFInt32)getField( "siteID" ); 
  }
  return( siteID.getValue( ) );
}

/** Set the siteID field. 
 * @param val The int to set.  */
public void setSiteID(int val) {
  if ( siteID == null ) { 
    siteID = (SFInt32)getField( "siteID" ); 
  }
  siteID.setValue( val );
}

/** Return the applicationID int value. 
 * @return The applicationID int value.  */
public int getApplicationID() {
  if ( applicationID == null ) { 
    applicationID = (SFInt32)getField( "applicationID" ); 
  }
  return( applicationID.getValue( ) );
}

/** Set the applicationID field. 
 * @param val The int to set.  */
public void setApplicationID(int val) {
  if ( applicationID == null ) { 
    applicationID = (SFInt32)getField( "applicationID" ); 
  }
  applicationID.setValue( val );
}

/** Return the entityID int value. 
 * @return The entityID int value.  */
public int getEntityID() {
  if ( entityID == null ) { 
    entityID = (SFInt32)getField( "entityID" ); 
  }
  return( entityID.getValue( ) );
}

/** Set the entityID field. 
 * @param val The int to set.  */
public void setEntityID(int val) {
  if ( entityID == null ) { 
    entityID = (SFInt32)getField( "entityID" ); 
  }
  entityID.setValue( val );
}

/** Return the readInterval double value. 
 * @return The readInterval double value.  */
public double getReadInterval() {
  if ( readInterval == null ) { 
    readInterval = (SFTime)getField( "readInterval" ); 
  }
  return( readInterval.getValue( ) );
}

/** Set the readInterval field. 
 * @param val The double to set.  */
public void setReadInterval(double val) {
  if ( readInterval == null ) { 
    readInterval = (SFTime)getField( "readInterval" ); 
  }
  readInterval.setValue( val );
}

/** Return the writeInterval double value. 
 * @return The writeInterval double value.  */
public double getWriteInterval() {
  if ( writeInterval == null ) { 
    writeInterval = (SFTime)getField( "writeInterval" ); 
  }
  return( writeInterval.getValue( ) );
}

/** Set the writeInterval field. 
 * @param val The double to set.  */
public void setWriteInterval(double val) {
  if ( writeInterval == null ) { 
    writeInterval = (SFTime)getField( "writeInterval" ); 
  }
  writeInterval.setValue( val );
}

/** Return the networkMode String value. 
 * @return The networkMode String value.  */
public String getNetworkMode() {
  if ( networkMode == null ) { 
    networkMode = (SFString)getField( "networkMode" ); 
  }
  return( networkMode.getValue( ) );
}

/** Set the networkMode field. 
 * @param val The String to set.  */
public void setNetworkMode(String val) {
  if ( networkMode == null ) { 
    networkMode = (SFString)getField( "networkMode" ); 
  }
  networkMode.setValue( val );
}

/** Return the address String value. 
 * @return The address String value.  */
public String getAddress() {
  if ( address == null ) { 
    address = (SFString)getField( "address" ); 
  }
  return( address.getValue( ) );
}

/** Set the address field. 
 * @param val The String to set.  */
public void setAddress(String val) {
  if ( address == null ) { 
    address = (SFString)getField( "address" ); 
  }
  address.setValue( val );
}

/** Return the port int value. 
 * @return The port int value.  */
public int getPort() {
  if ( port == null ) { 
    port = (SFInt32)getField( "port" ); 
  }
  return( port.getValue( ) );
}

/** Set the port field. 
 * @param val The int to set.  */
public void setPort(int val) {
  if ( port == null ) { 
    port = (SFInt32)getField( "port" ); 
  }
  port.setValue( val );
}

/** Return the articulationParameterCount int value. 
 * @return The articulationParameterCount int value.  */
public int getArticulationParameterCount() {
  if ( articulationParameterCount == null ) { 
    articulationParameterCount = (SFInt32)getField( "articulationParameterCount" ); 
  }
  return( articulationParameterCount.getValue( ) );
}

/** Set the articulationParameterCount field. 
 * @param val The int to set.  */
public void setArticulationParameterCount(int val) {
  if ( articulationParameterCount == null ) { 
    articulationParameterCount = (SFInt32)getField( "articulationParameterCount" ); 
  }
  articulationParameterCount.setValue( val );
}

/** Return the number of MFFloat items in the articulationParameterArray field. 
 * @return the number of MFFloat items in the articulationParameterArray field.  */
public int getNumArticulationParameterArray() {
  if ( articulationParameterArray == null ) { 
    articulationParameterArray = (MFFloat)getField( "articulationParameterArray" ); 
  }
  return( articulationParameterArray.getSize( ) );
}

/** Return the articulationParameterArray value in the argument float[]
 * @param val The float[] to initialize.  */
public void getArticulationParameterArray(float[] val) {
  if ( articulationParameterArray == null ) { 
    articulationParameterArray = (MFFloat)getField( "articulationParameterArray" ); 
  }
  articulationParameterArray.getValue( val );
}

/** Set the articulationParameterArray field. 
 * @param val The float[] to set.  */
public void setArticulationParameterArray(float[] val) {
  if ( articulationParameterArray == null ) { 
    articulationParameterArray = (MFFloat)getField( "articulationParameterArray" ); 
  }
  articulationParameterArray.setValue( val.length, val );
}

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the timestamp double value. 
 * @return The timestamp double value.  */
public double getTimestamp() {
  if ( timestamp == null ) { 
    timestamp = (SFTime)getField( "timestamp" ); 
  }
  return( timestamp.getValue( ) );
}

/** Return the detonationResult int value. 
 * @return The detonationResult int value.  */
public int getDetonationResult() {
  if ( detonationResult == null ) { 
    detonationResult = (SFInt32)getField( "detonationResult" ); 
  }
  return( detonationResult.getValue( ) );
}

/** Set the detonationResult field. 
 * @param val The int to set.  */
public void setDetonationResult(int val) {
  if ( detonationResult == null ) { 
    detonationResult = (SFInt32)getField( "detonationResult" ); 
  }
  detonationResult.setValue( val );
}

/** Return the detonationLocation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDetonationLocation(float[] val) {
  if ( detonationLocation == null ) { 
    detonationLocation = (SFVec3f)getField( "detonationLocation" ); 
  }
  detonationLocation.getValue( val );
}

/** Set the detonationLocation field. 
 * @param val The float[] to set.  */
public void setDetonationLocation(float[] val) {
  if ( detonationLocation == null ) { 
    detonationLocation = (SFVec3f)getField( "detonationLocation" ); 
  }
  detonationLocation.setValue( val );
}

/** Return the detonationRelativeLocation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDetonationRelativeLocation(float[] val) {
  if ( detonationRelativeLocation == null ) { 
    detonationRelativeLocation = (SFVec3f)getField( "detonationRelativeLocation" ); 
  }
  detonationRelativeLocation.getValue( val );
}

/** Set the detonationRelativeLocation field. 
 * @param val The float[] to set.  */
public void setDetonationRelativeLocation(float[] val) {
  if ( detonationRelativeLocation == null ) { 
    detonationRelativeLocation = (SFVec3f)getField( "detonationRelativeLocation" ); 
  }
  detonationRelativeLocation.setValue( val );
}

/** Return the isDetonated boolean value. 
 * @return The isDetonated boolean value.  */
public boolean getIsDetonated() {
  if ( isDetonated == null ) { 
    isDetonated = (SFBool)getField( "isDetonated" ); 
  }
  return( isDetonated.getValue( ) );
}

/** Return the detonateTime double value. 
 * @return The detonateTime double value.  */
public double getDetonateTime() {
  if ( detonateTime == null ) { 
    detonateTime = (SFTime)getField( "detonateTime" ); 
  }
  return( detonateTime.getValue( ) );
}

/** Return the eventApplicationID int value. 
 * @return The eventApplicationID int value.  */
public int getEventApplicationID() {
  if ( eventApplicationID == null ) { 
    eventApplicationID = (SFInt32)getField( "eventApplicationID" ); 
  }
  return( eventApplicationID.getValue( ) );
}

/** Set the eventApplicationID field. 
 * @param val The int to set.  */
public void setEventApplicationID(int val) {
  if ( eventApplicationID == null ) { 
    eventApplicationID = (SFInt32)getField( "eventApplicationID" ); 
  }
  eventApplicationID.setValue( val );
}

/** Return the eventEntityID int value. 
 * @return The eventEntityID int value.  */
public int getEventEntityID() {
  if ( eventEntityID == null ) { 
    eventEntityID = (SFInt32)getField( "eventEntityID" ); 
  }
  return( eventEntityID.getValue( ) );
}

/** Set the eventEntityID field. 
 * @param val The int to set.  */
public void setEventEntityID(int val) {
  if ( eventEntityID == null ) { 
    eventEntityID = (SFInt32)getField( "eventEntityID" ); 
  }
  eventEntityID.setValue( val );
}

/** Return the eventSiteID int value. 
 * @return The eventSiteID int value.  */
public int getEventSiteID() {
  if ( eventSiteID == null ) { 
    eventSiteID = (SFInt32)getField( "eventSiteID" ); 
  }
  return( eventSiteID.getValue( ) );
}

/** Set the eventSiteID field. 
 * @param val The int to set.  */
public void setEventSiteID(int val) {
  if ( eventSiteID == null ) { 
    eventSiteID = (SFInt32)getField( "eventSiteID" ); 
  }
  eventSiteID.setValue( val );
}

/** Return the fired1 boolean value. 
 * @return The fired1 boolean value.  */
public boolean getFired1() {
  if ( fired1 == null ) { 
    fired1 = (SFBool)getField( "fired1" ); 
  }
  return( fired1.getValue( ) );
}

/** Set the fired1 field. 
 * @param val The boolean to set.  */
public void setFired1(boolean val) {
  if ( fired1 == null ) { 
    fired1 = (SFBool)getField( "fired1" ); 
  }
  fired1.setValue( val );
}

/** Return the fired2 boolean value. 
 * @return The fired2 boolean value.  */
public boolean getFired2() {
  if ( fired2 == null ) { 
    fired2 = (SFBool)getField( "fired2" ); 
  }
  return( fired2.getValue( ) );
}

/** Set the fired2 field. 
 * @param val The boolean to set.  */
public void setFired2(boolean val) {
  if ( fired2 == null ) { 
    fired2 = (SFBool)getField( "fired2" ); 
  }
  fired2.setValue( val );
}

/** Return the fireMissionIndex int value. 
 * @return The fireMissionIndex int value.  */
public int getFireMissionIndex() {
  if ( fireMissionIndex == null ) { 
    fireMissionIndex = (SFInt32)getField( "fireMissionIndex" ); 
  }
  return( fireMissionIndex.getValue( ) );
}

/** Set the fireMissionIndex field. 
 * @param val The int to set.  */
public void setFireMissionIndex(int val) {
  if ( fireMissionIndex == null ) { 
    fireMissionIndex = (SFInt32)getField( "fireMissionIndex" ); 
  }
  fireMissionIndex.setValue( val );
}

/** Return the firingRange float value. 
 * @return The firingRange float value.  */
public float getFiringRange() {
  if ( firingRange == null ) { 
    firingRange = (SFFloat)getField( "firingRange" ); 
  }
  return( firingRange.getValue( ) );
}

/** Set the firingRange field. 
 * @param val The float to set.  */
public void setFiringRange(float val) {
  if ( firingRange == null ) { 
    firingRange = (SFFloat)getField( "firingRange" ); 
  }
  firingRange.setValue( val );
}

/** Return the firingRate int value. 
 * @return The firingRate int value.  */
public int getFiringRate() {
  if ( firingRate == null ) { 
    firingRate = (SFInt32)getField( "firingRate" ); 
  }
  return( firingRate.getValue( ) );
}

/** Set the firingRate field. 
 * @param val The int to set.  */
public void setFiringRate(int val) {
  if ( firingRate == null ) { 
    firingRate = (SFInt32)getField( "firingRate" ); 
  }
  firingRate.setValue( val );
}

/** Return the munitionApplicationID int value. 
 * @return The munitionApplicationID int value.  */
public int getMunitionApplicationID() {
  if ( munitionApplicationID == null ) { 
    munitionApplicationID = (SFInt32)getField( "munitionApplicationID" ); 
  }
  return( munitionApplicationID.getValue( ) );
}

/** Set the munitionApplicationID field. 
 * @param val The int to set.  */
public void setMunitionApplicationID(int val) {
  if ( munitionApplicationID == null ) { 
    munitionApplicationID = (SFInt32)getField( "munitionApplicationID" ); 
  }
  munitionApplicationID.setValue( val );
}

/** Return the munitionEndPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMunitionEndPoint(float[] val) {
  if ( munitionEndPoint == null ) { 
    munitionEndPoint = (SFVec3f)getField( "munitionEndPoint" ); 
  }
  munitionEndPoint.getValue( val );
}

/** Set the munitionEndPoint field. 
 * @param val The float[] to set.  */
public void setMunitionEndPoint(float[] val) {
  if ( munitionEndPoint == null ) { 
    munitionEndPoint = (SFVec3f)getField( "munitionEndPoint" ); 
  }
  munitionEndPoint.setValue( val );
}

/** Return the munitionEntityID int value. 
 * @return The munitionEntityID int value.  */
public int getMunitionEntityID() {
  if ( munitionEntityID == null ) { 
    munitionEntityID = (SFInt32)getField( "munitionEntityID" ); 
  }
  return( munitionEntityID.getValue( ) );
}

/** Set the munitionEntityID field. 
 * @param val The int to set.  */
public void setMunitionEntityID(int val) {
  if ( munitionEntityID == null ) { 
    munitionEntityID = (SFInt32)getField( "munitionEntityID" ); 
  }
  munitionEntityID.setValue( val );
}

/** Return the munitionSiteID int value. 
 * @return The munitionSiteID int value.  */
public int getMunitionSiteID() {
  if ( munitionSiteID == null ) { 
    munitionSiteID = (SFInt32)getField( "munitionSiteID" ); 
  }
  return( munitionSiteID.getValue( ) );
}

/** Set the munitionSiteID field. 
 * @param val The int to set.  */
public void setMunitionSiteID(int val) {
  if ( munitionSiteID == null ) { 
    munitionSiteID = (SFInt32)getField( "munitionSiteID" ); 
  }
  munitionSiteID.setValue( val );
}

/** Return the munitionStartPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMunitionStartPoint(float[] val) {
  if ( munitionStartPoint == null ) { 
    munitionStartPoint = (SFVec3f)getField( "munitionStartPoint" ); 
  }
  munitionStartPoint.getValue( val );
}

/** Set the munitionStartPoint field. 
 * @param val The float[] to set.  */
public void setMunitionStartPoint(float[] val) {
  if ( munitionStartPoint == null ) { 
    munitionStartPoint = (SFVec3f)getField( "munitionStartPoint" ); 
  }
  munitionStartPoint.setValue( val );
}

/** Return the firedTime double value. 
 * @return The firedTime double value.  */
public double getFiredTime() {
  if ( firedTime == null ) { 
    firedTime = (SFTime)getField( "firedTime" ); 
  }
  return( firedTime.getValue( ) );
}

/** Set the firedTime field. 
 * @param val The double to set.  */
public void setFiredTime(double val) {
  if ( firedTime == null ) { 
    firedTime = (SFTime)getField( "firedTime" ); 
  }
  firedTime.setValue( val );
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

/** Return the entityCategory int value. 
 * @return The entityCategory int value.  */
public int getEntityCategory() {
  if ( entityCategory == null ) { 
    entityCategory = (SFInt32)getField( "entityCategory" ); 
  }
  return( entityCategory.getValue( ) );
}

/** Set the entityCategory field. 
 * @param val The int to set.  */
public void setEntityCategory(int val) {
  if ( entityCategory == null ) { 
    entityCategory = (SFInt32)getField( "entityCategory" ); 
  }
  entityCategory.setValue( val );
}

/** Return the entityDomain int value. 
 * @return The entityDomain int value.  */
public int getEntityDomain() {
  if ( entityDomain == null ) { 
    entityDomain = (SFInt32)getField( "entityDomain" ); 
  }
  return( entityDomain.getValue( ) );
}

/** Set the entityDomain field. 
 * @param val The int to set.  */
public void setEntityDomain(int val) {
  if ( entityDomain == null ) { 
    entityDomain = (SFInt32)getField( "entityDomain" ); 
  }
  entityDomain.setValue( val );
}

/** Return the entityExtra int value. 
 * @return The entityExtra int value.  */
public int getEntityExtra() {
  if ( entityExtra == null ) { 
    entityExtra = (SFInt32)getField( "entityExtra" ); 
  }
  return( entityExtra.getValue( ) );
}

/** Set the entityExtra field. 
 * @param val The int to set.  */
public void setEntityExtra(int val) {
  if ( entityExtra == null ) { 
    entityExtra = (SFInt32)getField( "entityExtra" ); 
  }
  entityExtra.setValue( val );
}

/** Return the entityKind int value. 
 * @return The entityKind int value.  */
public int getEntityKind() {
  if ( entityKind == null ) { 
    entityKind = (SFInt32)getField( "entityKind" ); 
  }
  return( entityKind.getValue( ) );
}

/** Set the entityKind field. 
 * @param val The int to set.  */
public void setEntityKind(int val) {
  if ( entityKind == null ) { 
    entityKind = (SFInt32)getField( "entityKind" ); 
  }
  entityKind.setValue( val );
}

/** Return the entitySpecific int value. 
 * @return The entitySpecific int value.  */
public int getEntitySpecific() {
  if ( entitySpecific == null ) { 
    entitySpecific = (SFInt32)getField( "entitySpecific" ); 
  }
  return( entitySpecific.getValue( ) );
}

/** Set the entitySpecific field. 
 * @param val The int to set.  */
public void setEntitySpecific(int val) {
  if ( entitySpecific == null ) { 
    entitySpecific = (SFInt32)getField( "entitySpecific" ); 
  }
  entitySpecific.setValue( val );
}

/** Return the entityCountry int value. 
 * @return The entityCountry int value.  */
public int getEntityCountry() {
  if ( entityCountry == null ) { 
    entityCountry = (SFInt32)getField( "entityCountry" ); 
  }
  return( entityCountry.getValue( ) );
}

/** Set the entityCountry field. 
 * @param val The int to set.  */
public void setEntityCountry(int val) {
  if ( entityCountry == null ) { 
    entityCountry = (SFInt32)getField( "entityCountry" ); 
  }
  entityCountry.setValue( val );
}

/** Return the entitySubCategory int value. 
 * @return The entitySubCategory int value.  */
public int getEntitySubCategory() {
  if ( entitySubCategory == null ) { 
    entitySubCategory = (SFInt32)getField( "entitySubCategory" ); 
  }
  return( entitySubCategory.getValue( ) );
}

/** Set the entitySubCategory field. 
 * @param val The int to set.  */
public void setEntitySubCategory(int val) {
  if ( entitySubCategory == null ) { 
    entitySubCategory = (SFInt32)getField( "entitySubCategory" ); 
  }
  entitySubCategory.setValue( val );
}

/** Return the appearance int value. 
 * @return The appearance int value.  */
public int getAppearance() {
  if ( appearance == null ) { 
    appearance = (SFInt32)getField( "appearance" ); 
  }
  return( appearance.getValue( ) );
}

/** Set the appearance field. 
 * @param val The int to set.  */
public void setAppearance(int val) {
  if ( appearance == null ) { 
    appearance = (SFInt32)getField( "appearance" ); 
  }
  appearance.setValue( val );
}

/** Return the linearVelocity value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLinearVelocity(float[] val) {
  if ( linearVelocity == null ) { 
    linearVelocity = (SFVec3f)getField( "linearVelocity" ); 
  }
  linearVelocity.getValue( val );
}

/** Set the linearVelocity field. 
 * @param val The float[] to set.  */
public void setLinearVelocity(float[] val) {
  if ( linearVelocity == null ) { 
    linearVelocity = (SFVec3f)getField( "linearVelocity" ); 
  }
  linearVelocity.setValue( val );
}

/** Return the linearAcceleration value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLinearAcceleration(float[] val) {
  if ( linearAcceleration == null ) { 
    linearAcceleration = (SFVec3f)getField( "linearAcceleration" ); 
  }
  linearAcceleration.getValue( val );
}

/** Set the linearAcceleration field. 
 * @param val The float[] to set.  */
public void setLinearAcceleration(float[] val) {
  if ( linearAcceleration == null ) { 
    linearAcceleration = (SFVec3f)getField( "linearAcceleration" ); 
  }
  linearAcceleration.setValue( val );
}

/** Return the forceID int value. 
 * @return The forceID int value.  */
public int getForceID() {
  if ( forceID == null ) { 
    forceID = (SFInt32)getField( "forceID" ); 
  }
  return( forceID.getValue( ) );
}

/** Set the forceID field. 
 * @param val The int to set.  */
public void setForceID(int val) {
  if ( forceID == null ) { 
    forceID = (SFInt32)getField( "forceID" ); 
  }
  forceID.setValue( val );
}

/** Return the number of MFString items in the xmppParams field. 
 * @return the number of MFString items in the xmppParams field.  */
public int getNumXmppParams() {
  if ( xmppParams == null ) { 
    xmppParams = (MFString)getField( "xmppParams" ); 
  }
  return( xmppParams.getSize( ) );
}

/** Return the xmppParams value in the argument String[]
 * @param val The String[] to initialize.  */
public void getXmppParams(String[] val) {
  if ( xmppParams == null ) { 
    xmppParams = (MFString)getField( "xmppParams" ); 
  }
  xmppParams.getValue( val );
}

/** Set the xmppParams field. 
 * @param val The String[] to set.  */
public void setXmppParams(String[] val) {
  if ( xmppParams == null ) { 
    xmppParams = (MFString)getField( "xmppParams" ); 
  }
  xmppParams.setValue( val.length, val );
}

}
