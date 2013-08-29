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

package org.xj3d.sai.external.node.geometry2d;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFVec2f;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.geometry2d.Polypoint2D;

/** A concrete implementation of the Polypoint2D node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPolypoint2D extends SAINode implements Polypoint2D {

/** The point initializeOnly field */
private MFVec2f point;

/** Constructor */ 
public SAIPolypoint2D ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFVec2f items in the point field. 
 * @return the number of MFVec2f items in the point field.  */
public int getNumPoint() {
  if ( point == null ) { 
    point = (MFVec2f)getField( "point" ); 
  }
  return( point.getSize( ) );
}

/** Return the point value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPoint(float[] val) {
  if ( point == null ) { 
    point = (MFVec2f)getField( "point" ); 
  }
  point.getValue( val );
}

/** Set the point field. 
 * @param val The float[] to set.  */
public void setPoint(float[] val) {
  if ( point == null ) { 
    point = (MFVec2f)getField( "point" ); 
  }
  point.setValue( val.length/2, val );
}

}
