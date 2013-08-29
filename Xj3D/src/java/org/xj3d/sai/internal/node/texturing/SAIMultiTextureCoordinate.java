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

package org.xj3d.sai.internal.node.texturing;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.texturing.MultiTextureCoordinate;

/** A concrete implementation of the MultiTextureCoordinate node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIMultiTextureCoordinate extends BaseNode implements MultiTextureCoordinate {

/** The texCoord inputOutput field */
private MFNode texCoord;

/** Constructor */ 
public SAIMultiTextureCoordinate ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFNode items in the texCoord field. 
 * @return the number of MFNode items in the texCoord field.  */
public int getNumTexCoord() {
  if ( texCoord == null ) { 
    texCoord = (MFNode)getField( "texCoord" ); 
  }
  return( texCoord.getSize( ) );
}

/** Return the texCoord value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getTexCoord(X3DNode[] val) {
  if ( texCoord == null ) { 
    texCoord = (MFNode)getField( "texCoord" ); 
  }
  texCoord.getValue( val );
}

/** Set the texCoord field. 
 * @param val The X3DNode[] to set.  */
public void setTexCoord(X3DNode[] val) {
  if ( texCoord == null ) { 
    texCoord = (MFNode)getField( "texCoord" ); 
  }
  texCoord.setValue( val.length, val );
}

}
