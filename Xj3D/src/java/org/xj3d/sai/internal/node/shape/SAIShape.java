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
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DAppearanceNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.shape.Shape;

/** A concrete implementation of the Shape node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIShape extends BaseNode implements Shape {

/** The appearance inputOutput field */
private SFNode appearance;

/** The geometry inputOutput field */
private SFNode geometry;

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** Constructor */ 
public SAIShape ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

}
