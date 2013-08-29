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

package org.xj3d.sai.external.node.rigidbodyphysics;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.CollisionSensor;

/** A concrete implementation of the CollisionSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICollisionSensor extends SAINode implements CollisionSensor {

/** The isActive outputOnly field */
private SFBool isActive;

/** The collidables inputOutput field */
private SFNode collidables;

/** The enabled inputOutput field */
private SFBool enabled;

/** The contacts outputOnly field */
private MFNode contacts;

/** The intersections outputOnly field */
private MFNode intersections;

/** Constructor */ 
public SAICollisionSensor ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the collidables X3DNode value. 
 * @return The collidables X3DNode value.  */
public X3DNode getCollidables() {
  if ( collidables == null ) { 
    collidables = (SFNode)getField( "collidables" ); 
  }
  return( collidables.getValue( ) );
}

/** Set the collidables field. 
 * @param val The X3DNode to set.  */
public void setCollidables(X3DNode val) {
  if ( collidables == null ) { 
    collidables = (SFNode)getField( "collidables" ); 
  }
  collidables.setValue( val );
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

/** Return the number of MFNode items in the contacts field. 
 * @return the number of MFNode items in the contacts field.  */
public int getNumContacts() {
  if ( contacts == null ) { 
    contacts = (MFNode)getField( "contacts" ); 
  }
  return( contacts.getSize( ) );
}

/** Return the contacts value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getContacts(X3DNode[] val) {
  if ( contacts == null ) { 
    contacts = (MFNode)getField( "contacts" ); 
  }
  contacts.getValue( val );
}

/** Return the number of MFNode items in the intersections field. 
 * @return the number of MFNode items in the intersections field.  */
public int getNumIntersections() {
  if ( intersections == null ) { 
    intersections = (MFNode)getField( "intersections" ); 
  }
  return( intersections.getSize( ) );
}

/** Return the intersections value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getIntersections(X3DNode[] val) {
  if ( intersections == null ) { 
    intersections = (MFNode)getField( "intersections" ); 
  }
  intersections.getValue( val );
}

}
