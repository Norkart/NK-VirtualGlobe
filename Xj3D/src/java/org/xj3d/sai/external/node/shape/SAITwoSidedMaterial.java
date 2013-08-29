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

package org.xj3d.sai.external.node.shape;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.shape.TwoSidedMaterial;

/** A concrete implementation of the TwoSidedMaterial node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITwoSidedMaterial extends SAINode implements TwoSidedMaterial {

/** The ambientIntensity inputOutput field */
private SFFloat ambientIntensity;

/** The backAmbientIntensity inputOutput field */
private SFFloat backAmbientIntensity;

/** The diffuseColor inputOutput field */
private SFColor diffuseColor;

/** The backDiffuseColor inputOutput field */
private SFColor backDiffuseColor;

/** The emissiveColor inputOutput field */
private SFColor emissiveColor;

/** The backEmissiveColor inputOutput field */
private SFColor backEmissiveColor;

/** The shininess inputOutput field */
private SFFloat shininess;

/** The backShininess inputOutput field */
private SFFloat backShininess;

/** The specularColor inputOutput field */
private SFColor specularColor;

/** The backSpecularColor inputOutput field */
private SFColor backSpecularColor;

/** The transparency inputOutput field */
private SFFloat transparency;

/** The backTransparency inputOutput field */
private SFFloat backTransparency;

/** The separateBackColor inputOutput field */
private SFBool separateBackColor;

/** Constructor */ 
public SAITwoSidedMaterial ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the ambientIntensity float value. 
 * @return The ambientIntensity float value.  */
public float getAmbientIntensity() {
  if ( ambientIntensity == null ) { 
    ambientIntensity = (SFFloat)getField( "ambientIntensity" ); 
  }
  return( ambientIntensity.getValue( ) );
}

/** Set the ambientIntensity field. 
 * @param val The float to set.  */
public void setAmbientIntensity(float val) {
  if ( ambientIntensity == null ) { 
    ambientIntensity = (SFFloat)getField( "ambientIntensity" ); 
  }
  ambientIntensity.setValue( val );
}

/** Return the backAmbientIntensity float value. 
 * @return The backAmbientIntensity float value.  */
public float getBackAmbientIntensity() {
  if ( backAmbientIntensity == null ) { 
    backAmbientIntensity = (SFFloat)getField( "backAmbientIntensity" ); 
  }
  return( backAmbientIntensity.getValue( ) );
}

/** Set the backAmbientIntensity field. 
 * @param val The float to set.  */
public void setBackAmbientIntensity(float val) {
  if ( backAmbientIntensity == null ) { 
    backAmbientIntensity = (SFFloat)getField( "backAmbientIntensity" ); 
  }
  backAmbientIntensity.setValue( val );
}

/** Return the diffuseColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDiffuseColor(float[] val) {
  if ( diffuseColor == null ) { 
    diffuseColor = (SFColor)getField( "diffuseColor" ); 
  }
  diffuseColor.getValue( val );
}

/** Set the diffuseColor field. 
 * @param val The float[] to set.  */
public void setDiffuseColor(float[] val) {
  if ( diffuseColor == null ) { 
    diffuseColor = (SFColor)getField( "diffuseColor" ); 
  }
  diffuseColor.setValue( val );
}

/** Return the backDiffuseColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBackDiffuseColor(float[] val) {
  if ( backDiffuseColor == null ) { 
    backDiffuseColor = (SFColor)getField( "backDiffuseColor" ); 
  }
  backDiffuseColor.getValue( val );
}

/** Set the backDiffuseColor field. 
 * @param val The float[] to set.  */
public void setBackDiffuseColor(float[] val) {
  if ( backDiffuseColor == null ) { 
    backDiffuseColor = (SFColor)getField( "backDiffuseColor" ); 
  }
  backDiffuseColor.setValue( val );
}

/** Return the emissiveColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getEmissiveColor(float[] val) {
  if ( emissiveColor == null ) { 
    emissiveColor = (SFColor)getField( "emissiveColor" ); 
  }
  emissiveColor.getValue( val );
}

/** Set the emissiveColor field. 
 * @param val The float[] to set.  */
public void setEmissiveColor(float[] val) {
  if ( emissiveColor == null ) { 
    emissiveColor = (SFColor)getField( "emissiveColor" ); 
  }
  emissiveColor.setValue( val );
}

/** Return the backEmissiveColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBackEmissiveColor(float[] val) {
  if ( backEmissiveColor == null ) { 
    backEmissiveColor = (SFColor)getField( "backEmissiveColor" ); 
  }
  backEmissiveColor.getValue( val );
}

/** Set the backEmissiveColor field. 
 * @param val The float[] to set.  */
public void setBackEmissiveColor(float[] val) {
  if ( backEmissiveColor == null ) { 
    backEmissiveColor = (SFColor)getField( "backEmissiveColor" ); 
  }
  backEmissiveColor.setValue( val );
}

/** Return the shininess float value. 
 * @return The shininess float value.  */
public float getShininess() {
  if ( shininess == null ) { 
    shininess = (SFFloat)getField( "shininess" ); 
  }
  return( shininess.getValue( ) );
}

/** Set the shininess field. 
 * @param val The float to set.  */
public void setShininess(float val) {
  if ( shininess == null ) { 
    shininess = (SFFloat)getField( "shininess" ); 
  }
  shininess.setValue( val );
}

/** Return the backShininess float value. 
 * @return The backShininess float value.  */
public float getBackShininess() {
  if ( backShininess == null ) { 
    backShininess = (SFFloat)getField( "backShininess" ); 
  }
  return( backShininess.getValue( ) );
}

/** Set the backShininess field. 
 * @param val The float to set.  */
public void setBackShininess(float val) {
  if ( backShininess == null ) { 
    backShininess = (SFFloat)getField( "backShininess" ); 
  }
  backShininess.setValue( val );
}

/** Return the specularColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSpecularColor(float[] val) {
  if ( specularColor == null ) { 
    specularColor = (SFColor)getField( "specularColor" ); 
  }
  specularColor.getValue( val );
}

/** Set the specularColor field. 
 * @param val The float[] to set.  */
public void setSpecularColor(float[] val) {
  if ( specularColor == null ) { 
    specularColor = (SFColor)getField( "specularColor" ); 
  }
  specularColor.setValue( val );
}

/** Return the backSpecularColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBackSpecularColor(float[] val) {
  if ( backSpecularColor == null ) { 
    backSpecularColor = (SFColor)getField( "backSpecularColor" ); 
  }
  backSpecularColor.getValue( val );
}

/** Set the backSpecularColor field. 
 * @param val The float[] to set.  */
public void setBackSpecularColor(float[] val) {
  if ( backSpecularColor == null ) { 
    backSpecularColor = (SFColor)getField( "backSpecularColor" ); 
  }
  backSpecularColor.setValue( val );
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

/** Return the backTransparency float value. 
 * @return The backTransparency float value.  */
public float getBackTransparency() {
  if ( backTransparency == null ) { 
    backTransparency = (SFFloat)getField( "backTransparency" ); 
  }
  return( backTransparency.getValue( ) );
}

/** Set the backTransparency field. 
 * @param val The float to set.  */
public void setBackTransparency(float val) {
  if ( backTransparency == null ) { 
    backTransparency = (SFFloat)getField( "backTransparency" ); 
  }
  backTransparency.setValue( val );
}

/** Return the separateBackColor boolean value. 
 * @return The separateBackColor boolean value.  */
public boolean getSeparateBackColor() {
  if ( separateBackColor == null ) { 
    separateBackColor = (SFBool)getField( "separateBackColor" ); 
  }
  return( separateBackColor.getValue( ) );
}

/** Set the separateBackColor field. 
 * @param val The boolean to set.  */
public void setSeparateBackColor(boolean val) {
  if ( separateBackColor == null ) { 
    separateBackColor = (SFBool)getField( "separateBackColor" ); 
  }
  separateBackColor.setValue( val );
}

}
