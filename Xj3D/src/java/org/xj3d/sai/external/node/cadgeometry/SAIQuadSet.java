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

package org.xj3d.sai.external.node.cadgeometry;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DNormalNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureCoordinateNode;
import org.web3d.x3d.sai.cadgeometry.QuadSet;

/** A concrete implementation of the QuadSet node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIQuadSet extends SAINode implements QuadSet {

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

/** Constructor */ 
public SAIQuadSet ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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

}
