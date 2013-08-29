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
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.geospatial.GeoMetadata;

/** A concrete implementation of the GeoMetadata node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIGeoMetadata extends BaseNode implements GeoMetadata {

/** The data inputOutput field */
private MFNode data;

/** The summary inputOutput field */
private MFString summary;

/** The url inputOutput field */
private MFString url;

/** Constructor */ 
public SAIGeoMetadata ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFNode items in the data field. 
 * @return the number of MFNode items in the data field.  */
public int getNumData() {
  if ( data == null ) { 
    data = (MFNode)getField( "data" ); 
  }
  return( data.getSize( ) );
}

/** Return the data value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getData(X3DNode[] val) {
  if ( data == null ) { 
    data = (MFNode)getField( "data" ); 
  }
  data.getValue( val );
}

/** Set the data field. 
 * @param val The X3DNode[] to set.  */
public void setData(X3DNode[] val) {
  if ( data == null ) { 
    data = (MFNode)getField( "data" ); 
  }
  data.setValue( val.length, val );
}

/** Return the number of MFString items in the summary field. 
 * @return the number of MFString items in the summary field.  */
public int getNumSummary() {
  if ( summary == null ) { 
    summary = (MFString)getField( "summary" ); 
  }
  return( summary.getSize( ) );
}

/** Return the summary value in the argument String[]
 * @param val The String[] to initialize.  */
public void getSummary(String[] val) {
  if ( summary == null ) { 
    summary = (MFString)getField( "summary" ); 
  }
  summary.getValue( val );
}

/** Set the summary field. 
 * @param val The String[] to set.  */
public void setSummary(String[] val) {
  if ( summary == null ) { 
    summary = (MFString)getField( "summary" ); 
  }
  summary.setValue( val.length, val );
}

/** Return the number of MFString items in the url field. 
 * @return the number of MFString items in the url field.  */
public int getNumUrl() {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  return( url.getSize( ) );
}

/** Return the url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.getValue( val );
}

/** Set the url field. 
 * @param val The String[] to set.  */
public void setUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.setValue( val.length, val );
}

}
