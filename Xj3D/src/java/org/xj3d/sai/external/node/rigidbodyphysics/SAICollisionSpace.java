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
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.CollisionSpace;

/** A concrete implementation of the CollisionSpace node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICollisionSpace extends SAINode implements CollisionSpace {

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** The collidables inputOutput field */
private MFNode collidables;

/** The useGeometry inputOutput field */
private SFBool useGeometry;

/** The enabled inputOutput field */
private SFBool enabled;

/** Constructor */ 
public SAICollisionSpace ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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

/** Return the number of MFNode items in the collidables field. 
 * @return the number of MFNode items in the collidables field.  */
public int getNumCollidables() {
  if ( collidables == null ) { 
    collidables = (MFNode)getField( "collidables" ); 
  }
  return( collidables.getSize( ) );
}

/** Return the collidables value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getCollidables(X3DNode[] val) {
  if ( collidables == null ) { 
    collidables = (MFNode)getField( "collidables" ); 
  }
  collidables.getValue( val );
}

/** Set the collidables field. 
 * @param val The X3DNode[] to set.  */
public void setCollidables(X3DNode[] val) {
  if ( collidables == null ) { 
    collidables = (MFNode)getField( "collidables" ); 
  }
  collidables.setValue( val.length, val );
}

/** Return the useGeometry boolean value. 
 * @return The useGeometry boolean value.  */
public boolean getUseGeometry() {
  if ( useGeometry == null ) { 
    useGeometry = (SFBool)getField( "useGeometry" ); 
  }
  return( useGeometry.getValue( ) );
}

/** Set the useGeometry field. 
 * @param val The boolean to set.  */
public void setUseGeometry(boolean val) {
  if ( useGeometry == null ) { 
    useGeometry = (SFBool)getField( "useGeometry" ); 
  }
  useGeometry.setValue( val );
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

}
