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

package org.xj3d.sai.external.node.geospatial;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.MFVec3d;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.geospatial.GeoCoordinate;

/** A concrete implementation of the GeoCoordinate node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIGeoCoordinate extends SAINode implements GeoCoordinate {

/** The point inputOutput field */
private MFVec3d point;

/** The geoOrigin initializeOnly field */
private SFNode geoOrigin;

/** The geoSystem initializeOnly field */
private MFString geoSystem;

/** Constructor */ 
public SAIGeoCoordinate ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFVec3d items in the point field. 
 * @return the number of MFVec3d items in the point field.  */
public int getNumPoint() {
  if ( point == null ) { 
    point = (MFVec3d)getField( "point" ); 
  }
  return( point.getSize( ) );
}

/** Return the point value in the argument double[]
 * @param val The double[] to initialize.  */
public void getPoint(double[] val) {
  if ( point == null ) { 
    point = (MFVec3d)getField( "point" ); 
  }
  point.getValue( val );
}

/** Set the point field. 
 * @param val The double[] to set.  */
public void setPoint(double[] val) {
  if ( point == null ) { 
    point = (MFVec3d)getField( "point" ); 
  }
  point.setValue( val.length/3, val );
}

/** Return the geoOrigin X3DNode value. 
 * @return The geoOrigin X3DNode value.  */
public X3DNode getGeoOrigin() {
  if ( geoOrigin == null ) { 
    geoOrigin = (SFNode)getField( "geoOrigin" ); 
  }
  return( geoOrigin.getValue( ) );
}

/** Set the geoOrigin field. 
 * @param val The X3DNode to set.  */
public void setGeoOrigin(X3DNode val) {
  if ( geoOrigin == null ) { 
    geoOrigin = (SFNode)getField( "geoOrigin" ); 
  }
  geoOrigin.setValue( val );
}

/** Return the number of MFString items in the geoSystem field. 
 * @return the number of MFString items in the geoSystem field.  */
public int getNumGeoSystem() {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  return( geoSystem.getSize( ) );
}

/** Return the geoSystem value in the argument String[]
 * @param val The String[] to initialize.  */
public void getGeoSystem(String[] val) {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  geoSystem.getValue( val );
}

/** Set the geoSystem field. 
 * @param val The String[] to set.  */
public void setGeoSystem(String[] val) {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  geoSystem.setValue( val.length, val );
}

}
