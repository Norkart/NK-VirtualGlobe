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

package org.xj3d.sai.internal.node.rendering;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFVec3f;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.rendering.Normal;

/** A concrete implementation of the Normal node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAINormal extends BaseNode implements Normal {

/** The vector inputOutput field */
private MFVec3f vector;

/** Constructor */ 
public SAINormal ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFVec3f items in the vector field. 
 * @return the number of MFVec3f items in the vector field.  */
public int getNumVector() {
  if ( vector == null ) { 
    vector = (MFVec3f)getField( "vector" ); 
  }
  return( vector.getSize( ) );
}

/** Return the vector value in the argument float[]
 * @param val The float[] to initialize.  */
public void getVector(float[] val) {
  if ( vector == null ) { 
    vector = (MFVec3f)getField( "vector" ); 
  }
  vector.getValue( val );
}

/** Set the vector field. 
 * @param val The float[] to set.  */
public void setVector(float[] val) {
  if ( vector == null ) { 
    vector = (MFVec3f)getField( "vector" ); 
  }
  vector.setValue( val.length/3, val );
}

}
