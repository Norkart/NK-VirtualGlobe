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
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.particlesystems.PolylineEmitter;

/** A concrete implementation of the PolylineEmitter node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPolylineEmitter extends SAINode implements PolylineEmitter {

/** The speed inputOutput field */
private SFFloat speed;

/** The mass initializeOnly field */
private SFFloat mass;

/** The surfaceArea initializeOnly field */
private SFFloat surfaceArea;

/** The variation initializeOnly field */
private SFFloat variation;

/** The coords inputOutput field */
private SFNode coords;

/** The coordIndex initializeOnly field */
private MFInt32 coordIndex;

/** The set_coordIndex inputOnly field */
private MFInt32 set_coordIndex;

/** The direction inputOutput field */
private SFVec3f direction;

/** Constructor */ 
public SAIPolylineEmitter ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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

/** Return the speed float value. 
 * @return The speed float value.  */
public float getSpeed() {
  if ( speed == null ) { 
    speed = (SFFloat)getField( "speed" ); 
  }
  return( speed.getValue( ) );
}

/** Set the speed field. 
 * @param val The float to set.  */
public void setSpeed(float val) {
  if ( speed == null ) { 
    speed = (SFFloat)getField( "speed" ); 
  }
  speed.setValue( val );
}

/** Return the mass float value. 
 * @return The mass float value.  */
public float getMass() {
  if ( mass == null ) { 
    mass = (SFFloat)getField( "mass" ); 
  }
  return( mass.getValue( ) );
}

/** Set the mass field. 
 * @param val The float to set.  */
public void setMass(float val) {
  if ( mass == null ) { 
    mass = (SFFloat)getField( "mass" ); 
  }
  mass.setValue( val );
}

/** Return the surfaceArea float value. 
 * @return The surfaceArea float value.  */
public float getSurfaceArea() {
  if ( surfaceArea == null ) { 
    surfaceArea = (SFFloat)getField( "surfaceArea" ); 
  }
  return( surfaceArea.getValue( ) );
}

/** Set the surfaceArea field. 
 * @param val The float to set.  */
public void setSurfaceArea(float val) {
  if ( surfaceArea == null ) { 
    surfaceArea = (SFFloat)getField( "surfaceArea" ); 
  }
  surfaceArea.setValue( val );
}

/** Return the variation float value. 
 * @return The variation float value.  */
public float getVariation() {
  if ( variation == null ) { 
    variation = (SFFloat)getField( "variation" ); 
  }
  return( variation.getValue( ) );
}

/** Set the variation field. 
 * @param val The float to set.  */
public void setVariation(float val) {
  if ( variation == null ) { 
    variation = (SFFloat)getField( "variation" ); 
  }
  variation.setValue( val );
}

/** Return the coords X3DNode value. 
 * @return The coords X3DNode value.  */
public X3DNode getCoords() {
  if ( coords == null ) { 
    coords = (SFNode)getField( "coords" ); 
  }
  return( coords.getValue( ) );
}

/** Set the coords field. 
 * @param val The X3DCoordinateNode to set.  */
public void setCoords(X3DCoordinateNode val) {
  if ( coords == null ) { 
    coords = (SFNode)getField( "coords" ); 
  }
  coords.setValue( val );
}

/** Set the coords field. 
 * @param val The X3DProtoInstance to set.  */
public void setCoords(X3DProtoInstance val) {
  if ( coords == null ) { 
    coords = (SFNode)getField( "coords" ); 
  }
  coords.setValue( val );
}

/** Return the direction value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDirection(float[] val) {
  if ( direction == null ) { 
    direction = (SFVec3f)getField( "direction" ); 
  }
  direction.getValue( val );
}

/** Set the direction field. 
 * @param val The float[] to set.  */
public void setDirection(float[] val) {
  if ( direction == null ) { 
    direction = (SFVec3f)getField( "direction" ); 
  }
  direction.setValue( val );
}

}
