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

package org.xj3d.sai.external.node.grouping;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.grouping.WorldInfo;

/** A concrete implementation of the WorldInfo node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIWorldInfo extends SAINode implements WorldInfo {

/** The title initializeOnly field */
private SFString title;

/** The info initializeOnly field */
private MFString info;

/** Constructor */ 
public SAIWorldInfo ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the title String value. 
 * @return The title String value.  */
public String getTitle() {
  if ( title == null ) { 
    title = (SFString)getField( "title" ); 
  }
  return( title.getValue( ) );
}

/** Set the title field. 
 * @param val The String to set.  */
public void setTitle(String val) {
  if ( title == null ) { 
    title = (SFString)getField( "title" ); 
  }
  title.setValue( val );
}

/** Return the number of MFString items in the info field. 
 * @return the number of MFString items in the info field.  */
public int getNumInfo() {
  if ( info == null ) { 
    info = (MFString)getField( "info" ); 
  }
  return( info.getSize( ) );
}

/** Return the info value in the argument String[]
 * @param val The String[] to initialize.  */
public void getInfo(String[] val) {
  if ( info == null ) { 
    info = (MFString)getField( "info" ); 
  }
  info.getValue( val );
}

/** Set the info field. 
 * @param val The String[] to set.  */
public void setInfo(String[] val) {
  if ( info == null ) { 
    info = (MFString)getField( "info" ); 
  }
  info.setValue( val.length, val );
}

}
