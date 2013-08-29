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

package org.xj3d.sai.external.node.particlesystems;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DAppearanceNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DParticleEmitterNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.particlesystems.ParticleSystem;

/** A concrete implementation of the ParticleSystem node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIParticleSystem extends SAINode implements ParticleSystem {

/** The geometry inputOutput field */
private SFNode geometry;

/** The appearance inputOutput field */
private SFNode appearance;

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** The geometryType initializeOnly field */
private SFString geometryType;

/** The enabled inputOutput field */
private SFBool enabled;

/** The maxParticles inputOutput field */
private SFInt32 maxParticles;

/** The particleLifetime inputOutput field */
private SFFloat particleLifetime;

/** The lifetimeVariation inputOutput field */
private SFFloat lifetimeVariation;

/** The emitter initializeOnly field */
private SFNode emitter;

/** The physics initializeOnly field */
private MFNode physics;

/** The colorRamp initializeOnly field */
private SFNode colorRamp;

/** The colorKey initializeOnly field */
private MFFloat colorKey;

/** The isActive outputOnly field */
private SFBool isActive;

/** The particleSize inputOutput field */
private SFVec2f particleSize;

/** The createParticles inputOutput field */
private SFBool createParticles;

/** The texCoordRamp initializeOnly field */
private SFNode texCoordRamp;

/** The texCoordKey initializeOnly field */
private MFFloat texCoordKey;

/** Constructor */ 
public SAIParticleSystem ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the geometry X3DNode value. 
 * @return The geometry X3DNode value.  */
public X3DNode getGeometry() {
  if ( geometry == null ) { 
    geometry = (SFNode)getField( "geometry" ); 
  }
  return( geometry.getValue( ) );
}

/** Set the geometry field. 
 * @param val The X3DGeometryNode to set.  */
public void setGeometry(X3DGeometryNode val) {
  if ( geometry == null ) { 
    geometry = (SFNode)getField( "geometry" ); 
  }
  geometry.setValue( val );
}

/** Set the geometry field. 
 * @param val The X3DProtoInstance to set.  */
public void setGeometry(X3DProtoInstance val) {
  if ( geometry == null ) { 
    geometry = (SFNode)getField( "geometry" ); 
  }
  geometry.setValue( val );
}

/** Return the appearance X3DNode value. 
 * @return The appearance X3DNode value.  */
public X3DNode getAppearance() {
  if ( appearance == null ) { 
    appearance = (SFNode)getField( "appearance" ); 
  }
  return( appearance.getValue( ) );
}

/** Set the appearance field. 
 * @param val The X3DAppearanceNode to set.  */
public void setAppearance(X3DAppearanceNode val) {
  if ( appearance == null ) { 
    appearance = (SFNode)getField( "appearance" ); 
  }
  appearance.setValue( val );
}

/** Set the appearance field. 
 * @param val The X3DProtoInstance to set.  */
public void setAppearance(X3DProtoInstance val) {
  if ( appearance == null ) { 
    appearance = (SFNode)getField( "appearance" ); 
  }
  appearance.setValue( val );
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

/** Return the geometryType String value. 
 * @return The geometryType String value.  */
public String getGeometryType() {
  if ( geometryType == null ) { 
    geometryType = (SFString)getField( "geometryType" ); 
  }
  return( geometryType.getValue( ) );
}

/** Set the geometryType field. 
 * @param val The String to set.  */
public void setGeometryType(String val) {
  if ( geometryType == null ) { 
    geometryType = (SFString)getField( "geometryType" ); 
  }
  geometryType.setValue( val );
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

/** Return the maxParticles int value. 
 * @return The maxParticles int value.  */
public int getMaxParticles() {
  if ( maxParticles == null ) { 
    maxParticles = (SFInt32)getField( "maxParticles" ); 
  }
  return( maxParticles.getValue( ) );
}

/** Set the maxParticles field. 
 * @param val The int to set.  */
public void setMaxParticles(int val) {
  if ( maxParticles == null ) { 
    maxParticles = (SFInt32)getField( "maxParticles" ); 
  }
  maxParticles.setValue( val );
}

/** Return the particleLifetime float value. 
 * @return The particleLifetime float value.  */
public float getParticleLifetime() {
  if ( particleLifetime == null ) { 
    particleLifetime = (SFFloat)getField( "particleLifetime" ); 
  }
  return( particleLifetime.getValue( ) );
}

/** Set the particleLifetime field. 
 * @param val The float to set.  */
public void setParticleLifetime(float val) {
  if ( particleLifetime == null ) { 
    particleLifetime = (SFFloat)getField( "particleLifetime" ); 
  }
  particleLifetime.setValue( val );
}

/** Return the lifetimeVariation float value. 
 * @return The lifetimeVariation float value.  */
public float getLifetimeVariation() {
  if ( lifetimeVariation == null ) { 
    lifetimeVariation = (SFFloat)getField( "lifetimeVariation" ); 
  }
  return( lifetimeVariation.getValue( ) );
}

/** Set the lifetimeVariation field. 
 * @param val The float to set.  */
public void setLifetimeVariation(float val) {
  if ( lifetimeVariation == null ) { 
    lifetimeVariation = (SFFloat)getField( "lifetimeVariation" ); 
  }
  lifetimeVariation.setValue( val );
}

/** Return the emitter X3DNode value. 
 * @return The emitter X3DNode value.  */
public X3DNode getEmitter() {
  if ( emitter == null ) { 
    emitter = (SFNode)getField( "emitter" ); 
  }
  return( emitter.getValue( ) );
}

/** Set the emitter field. 
 * @param val The X3DParticleEmitterNode to set.  */
public void setEmitter(X3DParticleEmitterNode val) {
  if ( emitter == null ) { 
    emitter = (SFNode)getField( "emitter" ); 
  }
  emitter.setValue( val );
}

/** Set the emitter field. 
 * @param val The X3DProtoInstance to set.  */
public void setEmitter(X3DProtoInstance val) {
  if ( emitter == null ) { 
    emitter = (SFNode)getField( "emitter" ); 
  }
  emitter.setValue( val );
}

/** Return the number of MFNode items in the physics field. 
 * @return the number of MFNode items in the physics field.  */
public int getNumPhysics() {
  if ( physics == null ) { 
    physics = (MFNode)getField( "physics" ); 
  }
  return( physics.getSize( ) );
}

/** Return the physics value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getPhysics(X3DNode[] val) {
  if ( physics == null ) { 
    physics = (MFNode)getField( "physics" ); 
  }
  physics.getValue( val );
}

/** Set the physics field. 
 * @param val The X3DNode[] to set.  */
public void setPhysics(X3DNode[] val) {
  if ( physics == null ) { 
    physics = (MFNode)getField( "physics" ); 
  }
  physics.setValue( val.length, val );
}

/** Return the colorRamp X3DNode value. 
 * @return The colorRamp X3DNode value.  */
public X3DNode getColorRamp() {
  if ( colorRamp == null ) { 
    colorRamp = (SFNode)getField( "colorRamp" ); 
  }
  return( colorRamp.getValue( ) );
}

/** Set the colorRamp field. 
 * @param val The X3DNode to set.  */
public void setColorRamp(X3DNode val) {
  if ( colorRamp == null ) { 
    colorRamp = (SFNode)getField( "colorRamp" ); 
  }
  colorRamp.setValue( val );
}

/** Return the number of MFFloat items in the colorKey field. 
 * @return the number of MFFloat items in the colorKey field.  */
public int getNumColorKey() {
  if ( colorKey == null ) { 
    colorKey = (MFFloat)getField( "colorKey" ); 
  }
  return( colorKey.getSize( ) );
}

/** Return the colorKey value in the argument float[]
 * @param val The float[] to initialize.  */
public void getColorKey(float[] val) {
  if ( colorKey == null ) { 
    colorKey = (MFFloat)getField( "colorKey" ); 
  }
  colorKey.getValue( val );
}

/** Set the colorKey field. 
 * @param val The float[] to set.  */
public void setColorKey(float[] val) {
  if ( colorKey == null ) { 
    colorKey = (MFFloat)getField( "colorKey" ); 
  }
  colorKey.setValue( val.length, val );
}

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the particleSize value in the argument float[]
 * @param val The float[] to initialize.  */
public void getParticleSize(float[] val) {
  if ( particleSize == null ) { 
    particleSize = (SFVec2f)getField( "particleSize" ); 
  }
  particleSize.getValue( val );
}

/** Set the particleSize field. 
 * @param val The float[] to set.  */
public void setParticleSize(float[] val) {
  if ( particleSize == null ) { 
    particleSize = (SFVec2f)getField( "particleSize" ); 
  }
  particleSize.setValue( val );
}

/** Return the createParticles boolean value. 
 * @return The createParticles boolean value.  */
public boolean getCreateParticles() {
  if ( createParticles == null ) { 
    createParticles = (SFBool)getField( "createParticles" ); 
  }
  return( createParticles.getValue( ) );
}

/** Set the createParticles field. 
 * @param val The boolean to set.  */
public void setCreateParticles(boolean val) {
  if ( createParticles == null ) { 
    createParticles = (SFBool)getField( "createParticles" ); 
  }
  createParticles.setValue( val );
}

/** Return the texCoordRamp X3DNode value. 
 * @return The texCoordRamp X3DNode value.  */
public X3DNode getTexCoordRamp() {
  if ( texCoordRamp == null ) { 
    texCoordRamp = (SFNode)getField( "texCoordRamp" ); 
  }
  return( texCoordRamp.getValue( ) );
}

/** Set the texCoordRamp field. 
 * @param val The X3DNode to set.  */
public void setTexCoordRamp(X3DNode val) {
  if ( texCoordRamp == null ) { 
    texCoordRamp = (SFNode)getField( "texCoordRamp" ); 
  }
  texCoordRamp.setValue( val );
}

/** Return the number of MFFloat items in the texCoordKey field. 
 * @return the number of MFFloat items in the texCoordKey field.  */
public int getNumTexCoordKey() {
  if ( texCoordKey == null ) { 
    texCoordKey = (MFFloat)getField( "texCoordKey" ); 
  }
  return( texCoordKey.getSize( ) );
}

/** Return the texCoordKey value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTexCoordKey(float[] val) {
  if ( texCoordKey == null ) { 
    texCoordKey = (MFFloat)getField( "texCoordKey" ); 
  }
  texCoordKey.getValue( val );
}

/** Set the texCoordKey field. 
 * @param val The float[] to set.  */
public void setTexCoordKey(float[] val) {
  if ( texCoordKey == null ) { 
    texCoordKey = (MFFloat)getField( "texCoordKey" ); 
  }
  texCoordKey.setValue( val.length, val );
}

}
