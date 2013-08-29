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
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFImage;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.texturing.PixelTexture;

/** A concrete implementation of the PixelTexture node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPixelTexture extends BaseNode implements PixelTexture {

/** The textureProperties initializeOnly field */
private SFNode textureProperties;

/** The repeatS initializeOnly field */
private SFBool repeatS;

/** The repeatT initializeOnly field */
private SFBool repeatT;

/** The image inputOutput field */
private SFImage image;

/** Constructor */ 
public SAIPixelTexture ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the textureProperties X3DNode value. 
 * @return The textureProperties X3DNode value.  */
public X3DNode getTextureProperties() {
  if ( textureProperties == null ) { 
    textureProperties = (SFNode)getField( "textureProperties" ); 
  }
  return( textureProperties.getValue( ) );
}

/** Set the textureProperties field. 
 * @param val The X3DNode to set.  */
public void setTextureProperties(X3DNode val) {
  if ( textureProperties == null ) { 
    textureProperties = (SFNode)getField( "textureProperties" ); 
  }
  textureProperties.setValue( val );
}

/** Return the repeatS boolean value. 
 * @return The repeatS boolean value.  */
public boolean getRepeatS() {
  if ( repeatS == null ) { 
    repeatS = (SFBool)getField( "repeatS" ); 
  }
  return( repeatS.getValue( ) );
}

/** Set the repeatS field. 
 * @param val The boolean to set.  */
public void setRepeatS(boolean val) {
  if ( repeatS == null ) { 
    repeatS = (SFBool)getField( "repeatS" ); 
  }
  repeatS.setValue( val );
}

/** Return the repeatT boolean value. 
 * @return The repeatT boolean value.  */
public boolean getRepeatT() {
  if ( repeatT == null ) { 
    repeatT = (SFBool)getField( "repeatT" ); 
  }
  return( repeatT.getValue( ) );
}

/** Set the repeatT field. 
 * @param val The boolean to set.  */
public void setRepeatT(boolean val) {
  if ( repeatT == null ) { 
    repeatT = (SFBool)getField( "repeatT" ); 
  }
  repeatT.setValue( val );
}

}
