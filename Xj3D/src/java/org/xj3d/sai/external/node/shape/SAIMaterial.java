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
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.shape.Material;

/** A concrete implementation of the Material node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIMaterial extends SAINode implements Material {

/** The ambientIntensity inputOutput field */
private SFFloat ambientIntensity;

/** The diffuseColor inputOutput field */
private SFColor diffuseColor;

/** The emissiveColor inputOutput field */
private SFColor emissiveColor;

/** The shininess inputOutput field */
private SFFloat shininess;

/** The specularColor inputOutput field */
private SFColor specularColor;

/** The transparency inputOutput field */
private SFFloat transparency;

/** Constructor */ 
public SAIMaterial ( 
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
