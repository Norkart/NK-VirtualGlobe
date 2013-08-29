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
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DNormalNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureCoordinateNode;
import org.web3d.x3d.sai.geometry3d.IndexedFaceSet;

/** A concrete implementation of the IndexedFaceSet node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIIndexedFaceSet extends BaseNode implements IndexedFaceSet {

/** The attrib inputOutput field */
private MFNode attrib;

/** The fogCoord inputOutput field */
private SFNode fogCoord;

/** The coord inputOutput field */
private SFNode coord;

/** The color inputOutput field */
private SFNode color;

/** The normal inputOutput field */
private SFNode normal;

/** The texCoord inputOutput field */
private SFNode texCoord;

/** The solid initializeOnly field */
private SFBool solid;

/** The ccw initializeOnly field */
private SFBool ccw;

/** The colorPerVertex initializeOnly field */
private SFBool colorPerVertex;

/** The normalPerVertex initializeOnly field */
private SFBool normalPerVertex;

/** The colorIndex initializeOnly field */
private MFInt32 colorIndex;

/** The set_colorIndex inputOnly field */
private MFInt32 set_colorIndex;

/** The coordIndex initializeOnly field */
private MFInt32 coordIndex;

/** The set_coordIndex inputOnly field */
private MFInt32 set_coordIndex;

/** The texCoordIndex initializeOnly field */
private MFInt32 texCoordIndex;

/** The set_texCoordIndex inputOnly field */
private MFInt32 set_texCoordIndex;

/** The normalIndex initializeOnly field */
private MFInt32 normalIndex;

/** The set_normalIndex inputOnly field */
private MFInt32 set_normalIndex;

/** The creaseAngle initializeOnly field */
private SFFloat creaseAngle;

/** The convex initializeOnly field */
private SFBool convex;

/** Constructor */ 
public SAIIndexedFaceSet ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFInt32 items in the colorIndex field. 
 * @return the number of MFInt32 items in the colorIndex field.  */
public int getNumColorIndex() {
  if ( colorIndex == null ) { 
    colorIndex = (MFInt32)getField( "colorIndex" ); 
  }
  return( colorIndex.getSize( ) );
}

/** Return the colorIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getColorIndex(int[] val) {
  if ( colorIndex == null ) { 
    colorIndex = (MFInt32)getField( "colorIndex" ); 
  }
  colorIndex.getValue( val );
}

/** Set the colorIndex field. 
 * @param val The int[] to set.  */
public void setColorIndex(int[] val) {
  if ( !isRealized( ) ) { 
    if ( colorIndex == null ) { 
      colorIndex = (MFInt32)getField( "colorIndex" ); 
    } 
    colorIndex.setValue( val.length, val ); 
  } else { 
    if ( set_colorIndex == null ) { 
      set_colorIndex = (MFInt32)getField( "set_colorIndex" ); 
    } 
    set_colorIndex.setValue( val.length, val ); 
  } 
}

/** Return the number of MFInt32 items in the coordIndex field. 
 * @return the number of MFInt32 items in the coordIndex field.  */
public int getNumCoordIndex() {
  if ( coordIndex == null ) { 
    coordIndex = (MFInt32)getField( "coordIndex" ); 
  }
  return( coordIndex.getSize( ) );
}

/** Return the coordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getCoordIndex(int[] val) {
  if ( coordIndex == null ) { 
    coordIndex = (MFInt32)getField( "coordIndex" ); 
  }
  coordIndex.getValue( val );
}

/** Set the coordIndex field. 
 * @param val The int[] to set.  */
public void setCoordIndex(int[] val) {
  if ( !isRealized( ) ) { 
    if ( coordIndex == null ) { 
      coordIndex = (MFInt32)getField( "coordIndex" ); 
    } 
    coordIndex.setValue( val.length, val ); 
  } else { 
    if ( set_coordIndex == null ) { 
      set_coordIndex = (MFInt32)getField( "set_coordIndex" ); 
    } 
    set_coordIndex.setValue( val.length, val ); 
  } 
}

/** Return the number of MFInt32 items in the texCoordIndex field. 
 * @return the number of MFInt32 items in the texCoordIndex field.  */
public int getNumTexCoordIndex() {
  if ( texCoordIndex == null ) { 
    texCoordIndex = (MFInt32)getField( "texCoordIndex" ); 
  }
  return( texCoordIndex.getSize( ) );
}

/** Return the texCoordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getTexCoordIndex(int[] val) {
  if ( texCoordIndex == null ) { 
    texCoordIndex = (MFInt32)getField( "texCoordIndex" ); 
  }
  texCoordIndex.getValue( val );
}

/** Set the texCoordIndex field. 
 * @param val The int[] to set.  */
public void setTexCoordIndex(int[] val) {
  if ( !isRealized( ) ) { 
    if ( texCoordIndex == null ) { 
      texCoordIndex = (MFInt32)getField( "texCoordIndex" ); 
    } 
    texCoordIndex.setValue( val.length, val ); 
  } else { 
    if ( set_texCoordIndex == null ) { 
      set_texCoordIndex = (MFInt32)getField( "set_texCoordIndex" ); 
    } 
    set_texCoordIndex.setValue( val.length, val ); 
  } 
}

/** Return the number of MFInt32 items in the normalIndex field. 
 * @return the number of MFInt32 items in the normalIndex field.  */
public int getNumNormalIndex() {
  if ( normalIndex == null ) { 
    normalIndex = (MFInt32)getField( "normalIndex" ); 
  }
  return( normalIndex.getSize( ) );
}

/** Return the normalIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getNormalIndex(int[] val) {
  if ( normalIndex == null ) { 
    normalIndex = (MFInt32)getField( "normalIndex" ); 
  }
  normalIndex.getValue( val );
}

/** Set the normalIndex field. 
 * @param val The int[] to set.  */
public void setNormalIndex(int[] val) {
  if ( !isRealized( ) ) { 
    if ( normalIndex == null ) { 
      normalIndex = (MFInt32)getField( "normalIndex" ); 
    } 
    normalIndex.setValue( val.length, val ); 
  } else { 
    if ( set_normalIndex == null ) { 
      set_normalIndex = (MFInt32)getField( "set_normalIndex" ); 
    } 
    set_normalIndex.setValue( val.length, val ); 
  } 
}

/** Return the number of MFNode items in the attrib field. 
 * @return the number of MFNode items in the attrib field.  */
public int getNumAttrib() {
  if ( attrib == null ) { 
    attrib = (MFNode)getField( "attrib" ); 
  }
  return( attrib.getSize( ) );
}

/** Return the attrib value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getAttrib(X3DNode[] val) {
  if ( attrib == null ) { 
    attrib = (MFNode)getField( "attrib" ); 
  }
  attrib.getValue( val );
}

/** Set the attrib field. 
 * @param val The X3DNode[] to set.  */
public void setAttrib(X3DNode[] val) {
  if ( attrib == null ) { 
    attrib = (MFNode)getField( "attrib" ); 
  }
  attrib.setValue( val.length, val );
}

/** Return the fogCoord X3DNode value. 
 * @return The fogCoord X3DNode value.  */
public X3DNode getFogCoord() {
  if ( fogCoord == null ) { 
    fogCoord = (SFNode)getField( "fogCoord" ); 
  }
  return( fogCoord.getValue( ) );
}

/** Set the fogCoord field. 
 * @param val The X3DNode to set.  */
public void setFogCoord(X3DNode val) {
  if ( fogCoord == null ) { 
    fogCoord = (SFNode)getField( "fogCoord" ); 
  }
  fogCoord.setValue( val );
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

/** Return the convex boolean value. 
 * @return The convex boolean value.  */
public boolean getConvex() {
  if ( convex == null ) { 
    convex = (SFBool)getField( "convex" ); 
  }
  return( convex.getValue( ) );
}

/** Set the convex field. 
 * @param val The boolean to set.  */
public void setConvex(boolean val) {
  if ( convex == null ) { 
    convex = (SFBool)getField( "convex" ); 
  }
  convex.setValue( val );
}

}
