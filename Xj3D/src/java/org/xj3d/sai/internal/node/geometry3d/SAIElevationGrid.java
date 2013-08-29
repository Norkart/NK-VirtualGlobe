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

package org.xj3d.sai.internal.node.geometry3d;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DNormalNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureCoordinateNode;
import org.web3d.x3d.sai.geometry3d.ElevationGrid;

/** A concrete implementation of the ElevationGrid node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIElevationGrid extends BaseNode implements ElevationGrid {

/** The set_height inputOnly field */
private MFFloat set_height;

/** The color inputOutput field */
private SFNode color;

/** The normal inputOutput field */
private SFNode normal;

/** The texCoord inputOutput field */
private SFNode texCoord;

/** The ccw initializeOnly field */
private SFBool ccw;

/** The colorPerVertex initializeOnly field */
private SFBool colorPerVertex;

/** The creaseAngle initializeOnly field */
private SFFloat creaseAngle;

/** The height initializeOnly field */
private MFFloat height;

/** The normalPerVertex initializeOnly field */
private SFBool normalPerVertex;

/** The solid initializeOnly field */
private SFBool solid;

/** The xDimension initializeOnly field */
private SFInt32 xDimension;

/** The xSpacing initializeOnly field */
private SFFloat xSpacing;

/** The zDimension initializeOnly field */
private SFInt32 zDimension;

/** The zSpacing initializeOnly field */
private SFFloat zSpacing;

/** Constructor */ 
public SAIElevationGrid ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFFloat items in the height field. 
 * @return the number of MFFloat items in the height field.  */
public int getNumHeight() {
  if ( height == null ) { 
    height = (MFFloat)getField( "height" ); 
  }
  return( height.getSize( ) );
}

/** Return the height value in the argument float[]
 * @param val The float[] to initialize.  */
public void getHeight(float[] val) {
  if ( height == null ) { 
    height = (MFFloat)getField( "height" ); 
  }
  height.getValue( val );
}

/** Set the height field. 
 * @param val The float[] to set.  */
public void setHeight(float[] val) {
  if ( !isRealized( ) ) { 
    if ( height == null ) { 
      height = (MFFloat)getField( "height" ); 
    } 
    height.setValue( val.length, val ); 
  } else { 
    if ( set_height == null ) { 
      set_height = (MFFloat)getField( "set_height" ); 
    } 
    set_height.setValue( val.length, val ); 
  } 
}

/** Return the color X3DNode value. 
 * @return The color X3DNode value.  */
public X3DNode getColor() {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  return( color.getValue( ) );
}

/** Set the color field. 
 * @param val The X3DColorNode to set.  */
public void setColor(X3DColorNode val) {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  color.setValue( val );
}

/** Set the color field. 
 * @param val The X3DProtoInstance to set.  */
public void setColor(X3DProtoInstance val) {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  color.setValue( val );
}

/** Return the normal X3DNode value. 
 * @return The normal X3DNode value.  */
public X3DNode getNormal() {
  if ( normal == null ) { 
    normal = (SFNode)getField( "normal" ); 
  }
  return( normal.getValue( ) );
}

/** Set the normal field. 
 * @param val The X3DNormalNode to set.  */
public void setNormal(X3DNormalNode val) {
  if ( normal == null ) { 
    normal = (SFNode)getField( "normal" ); 
  }
  normal.setValue( val );
}

/** Set the normal field. 
 * @param val The X3DProtoInstance to set.  */
public void setNormal(X3DProtoInstance val) {
  if ( normal == null ) { 
    normal = (SFNode)getField( "normal" ); 
  }
  normal.setValue( val );
}

/** Return the texCoord X3DNode value. 
 * @return The texCoord X3DNode value.  */
public X3DNode getTexCoord() {
  if ( texCoord == null ) { 
    texCoord = (SFNode)getField( "texCoord" ); 
  }
  return( texCoord.getValue( ) );
}

/** Set the texCoord field. 
 * @param val The X3DTextureCoordinateNode to set.  */
public void setTexCoord(X3DTextureCoordinateNode val) {
  if ( texCoord == null ) { 
    texCoord = (SFNode)getField( "texCoord" ); 
  }
  texCoord.setValue( val );
}

/** Set the texCoord field. 
 * @param val The X3DProtoInstance to set.  */
public void setTexCoord(X3DProtoInstance val) {
  if ( texCoord == null ) { 
    texCoord = (SFNode)getField( "texCoord" ); 
  }
  texCoord.setValue( val );
}

/** Return the ccw boolean value. 
 * @return The ccw boolean value.  */
public boolean getCcw() {
  if ( ccw == null ) { 
    ccw = (SFBool)getField( "ccw" ); 
  }
  return( ccw.getValue( ) );
}

/** Set the ccw field. 
 * @param val The boolean to set.  */
public void setCcw(boolean val) {
  if ( ccw == null ) { 
    ccw = (SFBool)getField( "ccw" ); 
  }
  ccw.setValue( val );
}

/** Return the colorPerVertex boolean value. 
 * @return The colorPerVertex boolean value.  */
public boolean getColorPerVertex() {
  if ( colorPerVertex == null ) { 
    colorPerVertex = (SFBool)getField( "colorPerVertex" ); 
  }
  return( colorPerVertex.getValue( ) );
}

/** Set the colorPerVertex field. 
 * @param val The boolean to set.  */
public void setColorPerVertex(boolean val) {
  if ( colorPerVertex == null ) { 
    colorPerVertex = (SFBool)getField( "colorPerVertex" ); 
  }
  colorPerVertex.setValue( val );
}

/** Return the creaseAngle float value. 
 * @return The creaseAngle float value.  */
public float getCreaseAngle() {
  if ( creaseAngle == null ) { 
    creaseAngle = (SFFloat)getField( "creaseAngle" ); 
  }
  return( creaseAngle.getValue( ) );
}

/** Set the creaseAngle field. 
 * @param val The float to set.  */
public void setCreaseAngle(float val) {
  if ( creaseAngle == null ) { 
    creaseAngle = (SFFloat)getField( "creaseAngle" ); 
  }
  creaseAngle.setValue( val );
}

/** Return the normalPerVertex boolean value. 
 * @return The normalPerVertex boolean value.  */
public boolean getNormalPerVertex() {
  if ( normalPerVertex == null ) { 
    normalPerVertex = (SFBool)getField( "normalPerVertex" ); 
  }
  return( normalPerVertex.getValue( ) );
}

/** Set the normalPerVertex field. 
 * @param val The boolean to set.  */
public void setNormalPerVertex(boolean val) {
  if ( normalPerVertex == null ) { 
    normalPerVertex = (SFBool)getField( "normalPerVertex" ); 
  }
  normalPerVertex.setValue( val );
}

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid() {
  if ( solid == null ) { 
    solid = (SFBool)getField( "solid" ); 
  }
  return( solid.getValue( ) );
}

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val) {
  if ( solid == null ) { 
    solid = (SFBool)getField( "solid" ); 
  }
  solid.setValue( val );
}

/** Return the xDimension int value. 
 * @return The xDimension int value.  */
public int getXDimension() {
  if ( xDimension == null ) { 
    xDimension = (SFInt32)getField( "xDimension" ); 
  }
  return( xDimension.getValue( ) );
}

/** Set the xDimension field. 
 * @param val The int to set.  */
public void setXDimension(int val) {
  if ( xDimension == null ) { 
    xDimension = (SFInt32)getField( "xDimension" ); 
  }
  xDimension.setValue( val );
}

/** Return the xSpacing float value. 
 * @return The xSpacing float value.  */
public float getXSpacing() {
  if ( xSpacing == null ) { 
    xSpacing = (SFFloat)getField( "xSpacing" ); 
  }
  return( xSpacing.getValue( ) );
}

/** Set the xSpacing field. 
 * @param val The float to set.  */
public void setXSpacing(float val) {
  if ( xSpacing == null ) { 
    xSpacing = (SFFloat)getField( "xSpacing" ); 
  }
  xSpacing.setValue( val );
}

/** Return the zDimension int value. 
 * @return The zDimension int value.  */
public int getZDimension() {
  if ( zDimension == null ) { 
    zDimension = (SFInt32)getField( "zDimension" ); 
  }
  return( zDimension.getValue( ) );
}

/** Set the zDimension field. 
 * @param val The int to set.  */
public void setZDimension(int val) {
  if ( zDimension == null ) { 
    zDimension = (SFInt32)getField( "zDimension" ); 
  }
  zDimension.setValue( val );
}

/** Return the zSpacing float value. 
 * @return The zSpacing float value.  */
public float getZSpacing() {
  if ( zSpacing == null ) { 
    zSpacing = (SFFloat)getField( "zSpacing" ); 
  }
  return( zSpacing.getValue( ) );
}

/** Set the zSpacing field. 
 * @param val The float to set.  */
public void setZSpacing(float val) {
  if ( zSpacing == null ) { 
    zSpacing = (SFFloat)getField( "zSpacing" ); 
  }
  zSpacing.setValue( val );
}

}
