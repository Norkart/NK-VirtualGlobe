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

package org.xj3d.sai.internal.node.environmentaleffects;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.environmentaleffects.FogCoordinate;

/** A concrete implementation of the FogCoordinate node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIFogCoordinate extends BaseNode implements FogCoordinate {

/** The depth inputOutput field */
private MFFloat depth;

/** Constructor */ 
public SAIFogCoordinate ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFFloat items in the depth field. 
 * @return the number of MFFloat items in the depth field.  */
public int getNumDepth() {
  if ( depth == null ) { 
    depth = (MFFloat)getField( "depth" ); 
  }
  return( depth.getSize( ) );
}

/** Return the depth value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDepth(float[] val) {
  if ( depth == null ) { 
    depth = (MFFloat)getField( "depth" ); 
  }
  depth.getValue( val );
}

/** Set the depth field. 
 * @param val The float[] to set.  */
public void setDepth(float[] val) {
  if ( depth == null ) { 
    depth = (MFFloat)getField( "depth" ); 
  }
  depth.setValue( val.length, val );
}

}
