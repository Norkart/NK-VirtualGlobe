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

package org.xj3d.sai.external.node.geometry3d;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.geometry3d.Cylinder;

/** A concrete implementation of the Cylinder node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICylinder extends SAINode implements Cylinder {

/** The solid initializeOnly field */
private SFBool solid;

/** The radius initializeOnly field */
private SFFloat radius;

/** The height initializeOnly field */
private SFFloat height;

/** The bottom initializeOnly field */
private SFBool bottom;

/** The side initializeOnly field */
private SFBool side;

/** The top initializeOnly field */
private SFBool top;

/** Constructor */ 
public SAICylinder ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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

/** Return the radius float value. 
 * @return The radius float value.  */
public float getRadius() {
  if ( radius == null ) { 
    radius = (SFFloat)getField( "radius" ); 
  }
  return( radius.getValue( ) );
}

/** Set the radius field. 
 * @param val The float to set.  */
public void setRadius(float val) {
  if ( radius == null ) { 
    radius = (SFFloat)getField( "radius" ); 
  }
  radius.setValue( val );
}

/** Return the height float value. 
 * @return The height float value.  */
public float getHeight() {
  if ( height == null ) { 
    height = (SFFloat)getField( "height" ); 
  }
  return( height.getValue( ) );
}

/** Set the height field. 
 * @param val The float to set.  */
public void setHeight(float val) {
  if ( height == null ) { 
    height = (SFFloat)getField( "height" ); 
  }
  height.setValue( val );
}

/** Return the bottom boolean value. 
 * @return The bottom boolean value.  */
public boolean getBottom() {
  if ( bottom == null ) { 
    bottom = (SFBool)getField( "bottom" ); 
  }
  return( bottom.getValue( ) );
}

/** Set the bottom field. 
 * @param val The boolean to set.  */
public void setBottom(boolean val) {
  if ( bottom == null ) { 
    bottom = (SFBool)getField( "bottom" ); 
  }
  bottom.setValue( val );
}

/** Return the side boolean value. 
 * @return The side boolean value.  */
public boolean getSide() {
  if ( side == null ) { 
    side = (SFBool)getField( "side" ); 
  }
  return( side.getValue( ) );
}

/** Set the side field. 
 * @param val The boolean to set.  */
public void setSide(boolean val) {
  if ( side == null ) { 
    side = (SFBool)getField( "side" ); 
  }
  side.setValue( val );
}

/** Return the top boolean value. 
 * @return The top boolean value.  */
public boolean getTop() {
  if ( top == null ) { 
    top = (SFBool)getField( "top" ); 
  }
  return( top.getValue( ) );
}

/** Set the top field. 
 * @param val The boolean to set.  */
public void setTop(boolean val) {
  if ( top == null ) { 
    top = (SFBool)getField( "top" ); 
  }
  top.setValue( val );
}

}
