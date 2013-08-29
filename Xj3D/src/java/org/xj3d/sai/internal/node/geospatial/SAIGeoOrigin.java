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

package org.xj3d.sai.internal.node.geospatial;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3d;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.geospatial.GeoOrigin;

/** A concrete implementation of the GeoOrigin node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIGeoOrigin extends BaseNode implements GeoOrigin {

/** The geoSystem inputOutput field */
private MFString geoSystem;

/** The geoCoords inputOutput field */
private SFVec3d geoCoords;

/** The rotateYUp initializeOnly field */
private SFBool rotateYUp;

/** Constructor */ 
public SAIGeoOrigin ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
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

/** Return the geoCoords value in the argument double[]
 * @param val The double[] to initialize.  */
public void getGeoCoords(double[] val) {
  if ( geoCoords == null ) { 
    geoCoords = (SFVec3d)getField( "geoCoords" ); 
  }
  geoCoords.getValue( val );
}

/** Set the geoCoords field. 
 * @param val The double[] to set.  */
public void setGeoCoords(double[] val) {
  if ( geoCoords == null ) { 
    geoCoords = (SFVec3d)getField( "geoCoords" ); 
  }
  geoCoords.setValue( val );
}

/** Return the rotateYUp boolean value. 
 * @return The rotateYUp boolean value.  */
public boolean getRotateYUp() {
  if ( rotateYUp == null ) { 
    rotateYUp = (SFBool)getField( "rotateYUp" ); 
  }
  return( rotateYUp.getValue( ) );
}

/** Set the rotateYUp field. 
 * @param val The boolean to set.  */
public void setRotateYUp(boolean val) {
  if ( rotateYUp == null ) { 
    rotateYUp = (SFBool)getField( "rotateYUp" ); 
  }
  rotateYUp.setValue( val );
}

}
