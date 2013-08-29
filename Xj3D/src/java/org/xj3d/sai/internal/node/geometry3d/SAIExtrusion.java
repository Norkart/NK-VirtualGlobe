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
import org.web3d.x3d.sai.MFRotation;
import org.web3d.x3d.sai.MFVec2f;
import org.web3d.x3d.sai.MFVec3f;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.geometry3d.Extrusion;

/** A concrete implementation of the Extrusion node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIExtrusion extends BaseNode implements Extrusion {

/** The beginCap initializeOnly field */
private SFBool beginCap;

/** The ccw initializeOnly field */
private SFBool ccw;

/** The convex initializeOnly field */
private SFBool convex;

/** The creaseAngle initializeOnly field */
private SFFloat creaseAngle;

/** The crossSection initializeOnly field */
private MFVec2f crossSection;

/** The endCap initializeOnly field */
private SFBool endCap;

/** The orientation initializeOnly field */
private MFRotation orientation;

/** The scale initializeOnly field */
private MFVec2f scale;

/** The solid initializeOnly field */
private SFBool solid;

/** The spine initializeOnly field */
private MFVec3f spine;

/** The set_crossSection inputOnly field */
private MFVec2f set_crossSection;

/** The set_orientation inputOnly field */
private MFRotation set_orientation;

/** The set_scale inputOnly field */
private MFVec2f set_scale;

/** The set_spine inputOnly field */
private MFVec3f set_spine;

/** Constructor */ 
public SAIExtrusion ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFVec2f items in the crossSection field. 
 * @return the number of MFVec2f items in the crossSection field.  */
public int getNumCrossSection() {
  if ( crossSection == null ) { 
    crossSection = (MFVec2f)getField( "crossSection" ); 
  }
  return( crossSection.getSize( ) );
}

/** Return the crossSection value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCrossSection(float[] val) {
  if ( crossSection == null ) { 
    crossSection = (MFVec2f)getField( "crossSection" ); 
  }
  crossSection.getValue( val );
}

/** Set the crossSection field. 
 * @param val The float[] to set.  */
public void setCrossSection(float[] val) {
  if ( !isRealized( ) ) { 
    if ( crossSection == null ) { 
      crossSection = (MFVec2f)getField( "crossSection" ); 
    } 
    crossSection.setValue( val.length/2, val ); 
  } else { 
    if ( set_crossSection == null ) { 
      set_crossSection = (MFVec2f)getField( "set_crossSection" ); 
    } 
    set_crossSection.setValue( val.length/2, val ); 
  } 
}

/** Return the number of MFRotation items in the orientation field. 
 * @return the number of MFRotation items in the orientation field.  */
public int getNumOrientation() {
  if ( orientation == null ) { 
    orientation = (MFRotation)getField( "orientation" ); 
  }
  return( orientation.getSize( ) );
}

/** Return the orientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getOrientation(float[] val) {
  if ( orientation == null ) { 
    orientation = (MFRotation)getField( "orientation" ); 
  }
  orientation.getValue( val );
}

/** Set the orientation field. 
 * @param val The float[] to set.  */
public void setOrientation(float[] val) {
  if ( !isRealized( ) ) { 
    if ( orientation == null ) { 
      orientation = (MFRotation)getField( "orientation" ); 
    } 
    orientation.setValue( val.length/4, val ); 
  } else { 
    if ( set_orientation == null ) { 
      set_orientation = (MFRotation)getField( "set_orientation" ); 
    } 
    set_orientation.setValue( val.length/4, val ); 
  } 
}

/** Return the number of MFVec2f items in the scale field. 
 * @return the number of MFVec2f items in the scale field.  */
public int getNumScale() {
  if ( scale == null ) { 
    scale = (MFVec2f)getField( "scale" ); 
  }
  return( scale.getSize( ) );
}

/** Return the scale value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScale(float[] val) {
  if ( scale == null ) { 
    scale = (MFVec2f)getField( "scale" ); 
  }
  scale.getValue( val );
}

/** Set the scale field. 
 * @param val The float[] to set.  */
public void setScale(float[] val) {
  if ( !isRealized( ) ) { 
    if ( scale == null ) { 
      scale = (MFVec2f)getField( "scale" ); 
    } 
    scale.setValue( val.length/2, val ); 
  } else { 
    if ( set_scale == null ) { 
      set_scale = (MFVec2f)getField( "set_scale" ); 
    } 
    set_scale.setValue( val.length/2, val ); 
  } 
}

/** Return the number of MFVec3f items in the spine field. 
 * @return the number of MFVec3f items in the spine field.  */
public int getNumSpine() {
  if ( spine == null ) { 
    spine = (MFVec3f)getField( "spine" ); 
  }
  return( spine.getSize( ) );
}

/** Return the spine value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSpine(float[] val) {
  if ( spine == null ) { 
    spine = (MFVec3f)getField( "spine" ); 
  }
  spine.getValue( val );
}

/** Set the spine field. 
 * @param val The float[] to set.  */
public void setSpine(float[] val) {
  if ( !isRealized( ) ) { 
    if ( spine == null ) { 
      spine = (MFVec3f)getField( "spine" ); 
    } 
    spine.setValue( val.length/3, val ); 
  } else { 
    if ( set_spine == null ) { 
      set_spine = (MFVec3f)getField( "set_spine" ); 
    } 
    set_spine.setValue( val.length/3, val ); 
  } 
}

/** Return the beginCap boolean value. 
 * @return The beginCap boolean value.  */
public boolean getBeginCap() {
  if ( beginCap == null ) { 
    beginCap = (SFBool)getField( "beginCap" ); 
  }
  return( beginCap.getValue( ) );
}

/** Set the beginCap field. 
 * @param val The boolean to set.  */
public void setBeginCap(boolean val) {
  if ( beginCap == null ) { 
    beginCap = (SFBool)getField( "beginCap" ); 
  }
  beginCap.setValue( val );
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

/** Return the endCap boolean value. 
 * @return The endCap boolean value.  */
public boolean getEndCap() {
  if ( endCap == null ) { 
    endCap = (SFBool)getField( "endCap" ); 
  }
  return( endCap.getValue( ) );
}

/** Set the endCap field. 
 * @param val The boolean to set.  */
public void setEndCap(boolean val) {
  if ( endCap == null ) { 
    endCap = (SFBool)getField( "endCap" ); 
  }
  endCap.setValue( val );
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

}
