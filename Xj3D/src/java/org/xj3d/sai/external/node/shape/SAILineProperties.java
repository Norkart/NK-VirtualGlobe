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
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.shape.LineProperties;

/** A concrete implementation of the LineProperties node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAILineProperties extends SAINode implements LineProperties {

/** The linewidthScaleFactor inputOutput field */
private SFFloat linewidthScaleFactor;

/** The linetype inputOutput field */
private SFInt32 linetype;

/** The applied inputOutput field */
private SFBool applied;

/** Constructor */ 
public SAILineProperties ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the linewidthScaleFactor float value. 
 * @return The linewidthScaleFactor float value.  */
public float getLinewidthScaleFactor() {
  if ( linewidthScaleFactor == null ) { 
    linewidthScaleFactor = (SFFloat)getField( "linewidthScaleFactor" ); 
  }
  return( linewidthScaleFactor.getValue( ) );
}

/** Set the linewidthScaleFactor field. 
 * @param val The float to set.  */
public void setLinewidthScaleFactor(float val) {
  if ( linewidthScaleFactor == null ) { 
    linewidthScaleFactor = (SFFloat)getField( "linewidthScaleFactor" ); 
  }
  linewidthScaleFactor.setValue( val );
}

/** Return the linetype int value. 
 * @return The linetype int value.  */
public int getLinetype() {
  if ( linetype == null ) { 
    linetype = (SFInt32)getField( "linetype" ); 
  }
  return( linetype.getValue( ) );
}

/** Set the linetype field. 
 * @param val The int to set.  */
public void setLinetype(int val) {
  if ( linetype == null ) { 
    linetype = (SFInt32)getField( "linetype" ); 
  }
  linetype.setValue( val );
}

/** Return the applied boolean value. 
 * @return The applied boolean value.  */
public boolean getApplied() {
  if ( applied == null ) { 
    applied = (SFBool)getField( "applied" ); 
  }
  return( applied.getValue( ) );
}

/** Set the applied field. 
 * @param val The boolean to set.  */
public void setApplied(boolean val) {
  if ( applied == null ) { 
    applied = (SFBool)getField( "applied" ); 
  }
  applied.setValue( val );
}

}
