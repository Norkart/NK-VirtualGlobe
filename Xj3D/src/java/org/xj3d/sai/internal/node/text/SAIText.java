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

package org.xj3d.sai.internal.node.text;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DFontStyleNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.text.Text;

/** A concrete implementation of the Text node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIText extends BaseNode implements Text {

/** The string inputOutput field */
private MFString string;

/** The fontStyle inputOutput field */
private SFNode fontStyle;

/** The length inputOutput field */
private MFFloat length;

/** The maxExtent inputOutput field */
private SFFloat maxExtent;

/** The solid initializeOnly field */
private SFBool solid;

/** Constructor */ 
public SAIText ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFString items in the string field. 
 * @return the number of MFString items in the string field.  */
public int getNumString() {
  if ( string == null ) { 
    string = (MFString)getField( "string" ); 
  }
  return( string.getSize( ) );
}

/** Return the string value in the argument String[]
 * @param val The String[] to initialize.  */
public void getString(String[] val) {
  if ( string == null ) { 
    string = (MFString)getField( "string" ); 
  }
  string.getValue( val );
}

/** Set the string field. 
 * @param val The String[] to set.  */
public void setString(String[] val) {
  if ( string == null ) { 
    string = (MFString)getField( "string" ); 
  }
  string.setValue( val.length, val );
}

/** Return the fontStyle X3DNode value. 
 * @return The fontStyle X3DNode value.  */
public X3DNode getFontStyle() {
  if ( fontStyle == null ) { 
    fontStyle = (SFNode)getField( "fontStyle" ); 
  }
  return( fontStyle.getValue( ) );
}

/** Set the fontStyle field. 
 * @param val The X3DFontStyleNode to set.  */
public void setFontStyle(X3DFontStyleNode val) {
  if ( fontStyle == null ) { 
    fontStyle = (SFNode)getField( "fontStyle" ); 
  }
  fontStyle.setValue( val );
}

/** Set the fontStyle field. 
 * @param val The X3DProtoInstance to set.  */
public void setFontStyle(X3DProtoInstance val) {
  if ( fontStyle == null ) { 
    fontStyle = (SFNode)getField( "fontStyle" ); 
  }
  fontStyle.setValue( val );
}

/** Return the number of MFFloat items in the length field. 
 * @return the number of MFFloat items in the length field.  */
public int getNumLength() {
  if ( length == null ) { 
    length = (MFFloat)getField( "length" ); 
  }
  return( length.getSize( ) );
}

/** Return the length value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLength(float[] val) {
  if ( length == null ) { 
    length = (MFFloat)getField( "length" ); 
  }
  length.getValue( val );
}

/** Set the length field. 
 * @param val The float[] to set.  */
public void setLength(float[] val) {
  if ( length == null ) { 
    length = (MFFloat)getField( "length" ); 
  }
  length.setValue( val.length, val );
}

/** Return the maxExtent float value. 
 * @return The maxExtent float value.  */
public float getMaxExtent() {
  if ( maxExtent == null ) { 
    maxExtent = (SFFloat)getField( "maxExtent" ); 
  }
  return( maxExtent.getValue( ) );
}

/** Set the maxExtent field. 
 * @param val The float to set.  */
public void setMaxExtent(float val) {
  if ( maxExtent == null ) { 
    maxExtent = (SFFloat)getField( "maxExtent" ); 
  }
  maxExtent.setValue( val );
}

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid() {
  if ( solid == null ) { 
    solid = (SFBool)getField( "solid" ); 
  }
  return( solid.getValue( ) );
}

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val) {
  if ( solid == null ) { 
    solid = (SFBool)getField( "solid" ); 
  }
  solid.setValue( val );
}

}
