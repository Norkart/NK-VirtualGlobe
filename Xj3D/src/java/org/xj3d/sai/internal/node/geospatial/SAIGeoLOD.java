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
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3d;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.geospatial.GeoLOD;

/** A concrete implementation of the GeoLOD node interface
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public class SAIGeoLOD extends BaseNode implements GeoLOD {

/** The children outputOnly field */
private MFNode children;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** The rootNode initializeOnly field */
private MFNode rootNode;

/** The rootUrl initializeOnly field */
private MFString rootUrl;

/** The child1Url initializeOnly field */
private MFString child1Url;

/** The child2Url initializeOnly field */
private MFString child2Url;

/** The child3Url initializeOnly field */
private MFString child3Url;

/** The child4Url initializeOnly field */
private MFString child4Url;

/** The geoOrigin initializeOnly field */
private SFNode geoOrigin;

/** The geoSystem initializeOnly field */
private MFString geoSystem;

/** The center initializeOnly field */
private SFVec3d center;

/** The range initializeOnly field */
private SFFloat range;

/** The level_changed outputOnly field */
private SFInt32 level_changed;

/** Constructor */ 
public SAIGeoLOD ( 
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

/** Return the number of MFNode items in the rootNode field. 
 * @return the number of MFNode items in the rootNode field.  */
public int getNumRootNode() {
  if ( rootNode == null ) { 
    rootNode = (MFNode)getField( "rootNode" ); 
  }
  return( rootNode.getSize( ) );
}

/** Return the rootNode value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getRootNode(X3DNode[] val) {
  if ( rootNode == null ) { 
    rootNode = (MFNode)getField( "rootNode" ); 
  }
  rootNode.getValue( val );
}

/** Set the rootNode field. 
 * @param val The X3DNode[] to set.  */
public void setRootNode(X3DNode[] val) {
  if ( rootNode == null ) { 
    rootNode = (MFNode)getField( "rootNode" ); 
  }
  rootNode.setValue( val.length, val );
}

/** Return the number of MFString items in the rootUrl field. 
 * @return the number of MFString items in the rootUrl field.  */
public int getNumRootUrl() {
  if ( rootUrl == null ) { 
    rootUrl = (MFString)getField( "rootUrl" ); 
  }
  return( rootUrl.getSize( ) );
}

/** Return the rootUrl value in the argument String[]
 * @param val The String[] to initialize.  */
public void getRootUrl(String[] val) {
  if ( rootUrl == null ) { 
    rootUrl = (MFString)getField( "rootUrl" ); 
  }
  rootUrl.getValue( val );
}

/** Set the rootUrl field. 
 * @param val The String[] to set.  */
public void setRootUrl(String[] val) {
  if ( rootUrl == null ) { 
    rootUrl = (MFString)getField( "rootUrl" ); 
  }
  rootUrl.setValue( val.length, val );
}

/** Return the number of MFString items in the child1Url field. 
 * @return the number of MFString items in the child1Url field.  */
public int getNumChild1Url() {
  if ( child1Url == null ) { 
    child1Url = (MFString)getField( "child1Url" ); 
  }
  return( child1Url.getSize( ) );
}

/** Return the child1Url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getChild1Url(String[] val) {
  if ( child1Url == null ) { 
    child1Url = (MFString)getField( "child1Url" ); 
  }
  child1Url.getValue( val );
}

/** Set the child1Url field. 
 * @param val The String[] to set.  */
public void setChild1Url(String[] val) {
  if ( child1Url == null ) { 
    child1Url = (MFString)getField( "child1Url" ); 
  }
  child1Url.setValue( val.length, val );
}

/** Return the number of MFString items in the child2Url field. 
 * @return the number of MFString items in the child2Url field.  */
public int getNumChild2Url() {
  if ( child2Url == null ) { 
    child2Url = (MFString)getField( "child2Url" ); 
  }
  return( child2Url.getSize( ) );
}

/** Return the child2Url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getChild2Url(String[] val) {
  if ( child2Url == null ) { 
    child2Url = (MFString)getField( "child2Url" ); 
  }
  child2Url.getValue( val );
}

/** Set the child2Url field. 
 * @param val The String[] to set.  */
public void setChild2Url(String[] val) {
  if ( child2Url == null ) { 
    child2Url = (MFString)getField( "child2Url" ); 
  }
  child2Url.setValue( val.length, val );
}

/** Return the number of MFString items in the child3Url field. 
 * @return the number of MFString items in the child3Url field.  */
public int getNumChild3Url() {
  if ( child3Url == null ) { 
    child3Url = (MFString)getField( "child3Url" ); 
  }
  return( child3Url.getSize( ) );
}

/** Return the child3Url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getChild3Url(String[] val) {
  if ( child3Url == null ) { 
    child3Url = (MFString)getField( "child3Url" ); 
  }
  child3Url.getValue( val );
}

/** Set the child3Url field. 
 * @param val The String[] to set.  */
public void setChild3Url(String[] val) {
  if ( child3Url == null ) { 
    child3Url = (MFString)getField( "child3Url" ); 
  }
  child3Url.setValue( val.length, val );
}

/** Return the number of MFString items in the child4Url field. 
 * @return the number of MFString items in the child4Url field.  */
public int getNumChild4Url() {
  if ( child4Url == null ) { 
    child4Url = (MFString)getField( "child4Url" ); 
  }
  return( child4Url.getSize( ) );
}

/** Return the child4Url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getChild4Url(String[] val) {
  if ( child4Url == null ) { 
    child4Url = (MFString)getField( "child4Url" ); 
  }
  child4Url.getValue( val );
}

/** Set the child4Url field. 
 * @param val The String[] to set.  */
public void setChild4Url(String[] val) {
  if ( child4Url == null ) { 
    child4Url = (MFString)getField( "child4Url" ); 
  }
  child4Url.setValue( val.length, val );
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

/** Return the center value in the argument double[]
 * @param val The double[] to initialize.  */
public void getCenter(double[] val) {
  if ( center == null ) { 
    center = (SFVec3d)getField( "center" ); 
  }
  center.getValue( val );
}

/** Set the center field. 
 * @param val The double[] to set.  */
public void setCenter(double[] val) {
  if ( center == null ) { 
    center = (SFVec3d)getField( "center" ); 
  }
  center.setValue( val );
}

/** Return the range float value. 
 * @return The range float value.  */
public float getRange() {
  if ( range == null ) { 
    range = (SFFloat)getField( "range" ); 
  }
  return( range.getValue( ) );
}

/** Set the range field. 
 * @param val The float to set.  */
public void setRange(float val) {
  if ( range == null ) { 
    range = (SFFloat)getField( "range" ); 
  }
  range.setValue( val );
}

/** Return the level_changed int value. 
 * @return The level_changed int value.  */
public int getLevel() {
  if ( level_changed == null ) { 
    level_changed = (SFInt32)getField( "level_changed" ); 
  }
  return( level_changed.getValue( ) );
}

}
