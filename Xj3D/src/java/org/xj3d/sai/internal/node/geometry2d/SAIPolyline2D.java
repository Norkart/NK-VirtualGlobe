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

package org.xj3d.sai.internal.node.geometry2d;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFVec2f;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.geometry2d.Polyline2D;

/** A concrete implementation of the Polyline2D node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPolyline2D extends BaseNode implements Polyline2D {

/** The lineSegments inputOutput field */
private MFVec2f lineSegments;

/** Constructor */ 
public SAIPolyline2D ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFVec2f items in the lineSegments field. 
 * @return the number of MFVec2f items in the lineSegments field.  */
public int getNumLineSegments() {
  if ( lineSegments == null ) { 
    lineSegments = (MFVec2f)getField( "lineSegments" ); 
  }
  return( lineSegments.getSize( ) );
}

/** Return the lineSegments value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLineSegments(float[] val) {
  if ( lineSegments == null ) { 
    lineSegments = (MFVec2f)getField( "lineSegments" ); 
  }
  lineSegments.getValue( val );
}

/** Set the lineSegments field. 
 * @param val The float[] to set.  */
public void setLineSegments(float[] val) {
  if ( lineSegments == null ) { 
    lineSegments = (MFVec2f)getField( "lineSegments" ); 
  }
  lineSegments.setValue( val.length/2, val );
}

}
