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

package org.xj3d.sai.external.node.texturing;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.texturing.TextureCoordinateGenerator;

/** A concrete implementation of the TextureCoordinateGenerator node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITextureCoordinateGenerator extends SAINode implements TextureCoordinateGenerator {

/** The mode initializeOnly field */
private SFString mode;

/** Constructor */ 
public SAITextureCoordinateGenerator ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the mode String value. 
 * @return The mode String value.  */
public String getMode() {
  if ( mode == null ) { 
    mode = (SFString)getField( "mode" ); 
  }
  return( mode.getValue( ) );
}

/** Set the mode field. 
 * @param val The String to set.  */
public void setMode(String val) {
  if ( mode == null ) { 
    mode = (SFString)getField( "mode" ); 
  }
  mode.setValue( val );
}

}
