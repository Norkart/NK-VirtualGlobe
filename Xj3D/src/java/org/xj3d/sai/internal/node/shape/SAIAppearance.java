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

package org.xj3d.sai.internal.node.shape;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMaterialNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureNode;
import org.web3d.x3d.sai.X3DTextureTransformNode;
import org.web3d.x3d.sai.shape.Appearance;

/** A concrete implementation of the Appearance node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIAppearance extends BaseNode implements Appearance {

/** The material inputOutput field */
private SFNode material;

/** The texture inputOutput field */
private SFNode texture;

/** The textureTransform inputOutput field */
private SFNode textureTransform;

/** The lineProperties inputOutput field */
private SFNode lineProperties;

/** The fillProperties inputOutput field */
private SFNode fillProperties;

/** The textureProperties inputOutput field */
private SFNode textureProperties;

/** Constructor */ 
public SAIAppearance ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the material X3DNode value. 
 * @return The material X3DNode value.  */
public X3DNode getMaterial() {
  if ( material == null ) { 
    material = (SFNode)getField( "material" ); 
  }
  return( material.getValue( ) );
}

/** Set the material field. 
 * @param val The X3DMaterialNode to set.  */
public void setMaterial(X3DMaterialNode val) {
  if ( material == null ) { 
    material = (SFNode)getField( "material" ); 
  }
  material.setValue( val );
}

/** Set the material field. 
 * @param val The X3DProtoInstance to set.  */
public void setMaterial(X3DProtoInstance val) {
  if ( material == null ) { 
    material = (SFNode)getField( "material" ); 
  }
  material.setValue( val );
}

/** Return the texture X3DNode value. 
 * @return The texture X3DNode value.  */
public X3DNode getTexture() {
  if ( texture == null ) { 
    texture = (SFNode)getField( "texture" ); 
  }
  return( texture.getValue( ) );
}

/** Set the texture field. 
 * @param val The X3DTextureNode to set.  */
public void setTexture(X3DTextureNode val) {
  if ( texture == null ) { 
    texture = (SFNode)getField( "texture" ); 
  }
  texture.setValue( val );
}

/** Set the texture field. 
 * @param val The X3DProtoInstance to set.  */
public void setTexture(X3DProtoInstance val) {
  if ( texture == null ) { 
    texture = (SFNode)getField( "texture" ); 
  }
  texture.setValue( val );
}

/** Return the textureTransform X3DNode value. 
 * @return The textureTransform X3DNode value.  */
public X3DNode getTextureTransform() {
  if ( textureTransform == null ) { 
    textureTransform = (SFNode)getField( "textureTransform" ); 
  }
  return( textureTransform.getValue( ) );
}

/** Set the textureTransform field. 
 * @param val The X3DTextureTransformNode to set.  */
public void setTextureTransform(X3DTextureTransformNode val) {
  if ( textureTransform == null ) { 
    textureTransform = (SFNode)getField( "textureTransform" ); 
  }
  textureTransform.setValue( val );
}

/** Set the textureTransform field. 
 * @param val The X3DProtoInstance to set.  */
public void setTextureTransform(X3DProtoInstance val) {
  if ( textureTransform == null ) { 
    textureTransform = (SFNode)getField( "textureTransform" ); 
  }
  textureTransform.setValue( val );
}

/** Return the lineProperties X3DNode value. 
 * @return The lineProperties X3DNode value.  */
public X3DNode getLineProperties() {
  if ( lineProperties == null ) { 
    lineProperties = (SFNode)getField( "lineProperties" ); 
  }
  return( lineProperties.getValue( ) );
}

/** Set the lineProperties field. 
 * @param val The X3DNode to set.  */
public void setLineProperties(X3DNode val) {
  if ( lineProperties == null ) { 
    lineProperties = (SFNode)getField( "lineProperties" ); 
  }
  lineProperties.setValue( val );
}

/** Return the fillProperties X3DNode value. 
 * @return The fillProperties X3DNode value.  */
public X3DNode getFillProperties() {
  if ( fillProperties == null ) { 
    fillProperties = (SFNode)getField( "fillProperties" ); 
  }
  return( fillProperties.getValue( ) );
}

/** Set the fillProperties field. 
 * @param val The X3DNode to set.  */
public void setFillProperties(X3DNode val) {
  if ( fillProperties == null ) { 
    fillProperties = (SFNode)getField( "fillProperties" ); 
  }
  fillProperties.setValue( val );
}

/** Return the textureProperties X3DNode value. 
 * @return The textureProperties X3DNode value.  */
public X3DNode getTextureProperties() {
  if ( textureProperties == null ) { 
    textureProperties = (SFNode)getField( "textureProperties" ); 
  }
  return( textureProperties.getValue( ) );
}

/** Set the textureProperties field. 
 * @param val The X3DNode to set.  */
public void setTextureProperties(X3DNode val) {
  if ( textureProperties == null ) { 
    textureProperties = (SFNode)getField( "textureProperties" ); 
  }
  textureProperties.setValue( val );
}

}
