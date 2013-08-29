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

package org.xj3d.sai.external.node.texturing;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.texturing.TextureTransform;

/** A concrete implementation of the TextureTransform node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITextureTransform extends SAINode implements TextureTransform {

/** The center inputOutput field */
private SFVec2f center;

/** The rotation inputOutput field */
private SFFloat rotation;

/** The scale inputOutput field */
private SFVec2f scale;

/** The translation inputOutput field */
private SFVec2f translation;

/** Constructor */ 
public SAITextureTransform ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the center value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenter(float[] val) {
  if ( center == null ) { 
    center = (SFVec2f)getField( "center" ); 
  }
  center.getValue( val );
}

/** Set the center field. 
 * @param val The float[] to set.  */
public void setCenter(float[] val) {
  if ( center == null ) { 
    center = (SFVec2f)getField( "center" ); 
  }
  center.setValue( val );
}

/** Return the rotation float value. 
 * @return The rotation float value.  */
public float getRotation() {
  if ( rotation == null ) { 
    rotation = (SFFloat)getField( "rotation" ); 
  }
  return( rotation.getValue( ) );
}

/** Set the rotation field. 
 * @param val The float to set.  */
public void setRotation(float val) {
  if ( rotation == null ) { 
    rotation = (SFFloat)getField( "rotation" ); 
  }
  rotation.setValue( val );
}

/** Return the scale value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScale(float[] val) {
  if ( scale == null ) { 
    scale = (SFVec2f)getField( "scale" ); 
  }
  scale.getValue( val );
}

/** Set the scale field. 
 * @param val The float[] to set.  */
public void setScale(float[] val) {
  if ( scale == null ) { 
    scale = (SFVec2f)getField( "scale" ); 
  }
  scale.setValue( val );
}

/** Return the translation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTranslation(float[] val) {
  if ( translation == null ) { 
    translation = (SFVec2f)getField( "translation" ); 
  }
  translation.getValue( val );
}

/** Set the translation field. 
 * @param val The float[] to set.  */
public void setTranslation(float[] val) {
  if ( translation == null ) { 
    translation = (SFVec2f)getField( "translation" ); 
  }
  translation.setValue( val );
}

}
