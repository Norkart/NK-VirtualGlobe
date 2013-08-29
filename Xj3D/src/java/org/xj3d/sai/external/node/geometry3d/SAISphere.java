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
import org.web3d.x3d.sai.geometry3d.Sphere;

/** A concrete implementation of the Sphere node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAISphere extends SAINode implements Sphere {

/** The solid initializeOnly field */
private SFBool solid;

/** The radius initializeOnly field */
private SFFloat radius;

/** Constructor */ 
public SAISphere ( 
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

}
