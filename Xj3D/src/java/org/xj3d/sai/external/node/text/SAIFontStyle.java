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

package org.xj3d.sai.external.node.text;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.text.FontStyle;

/** A concrete implementation of the FontStyle node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIFontStyle extends SAINode implements FontStyle {

/** The family initializeOnly field */
private MFString family;

/** The horizontal initializeOnly field */
private SFBool horizontal;

/** The justify initializeOnly field */
private MFString justify;

/** The language initializeOnly field */
private SFString language;

/** The leftToRight initializeOnly field */
private SFBool leftToRight;

/** The size initializeOnly field */
private SFFloat size;

/** The spacing initializeOnly field */
private SFFloat spacing;

/** The style initializeOnly field */
private SFString style;

/** The topToBottom initializeOnly field */
private SFBool topToBottom;

/** Constructor */ 
public SAIFontStyle ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFString items in the family field. 
 * @return the number of MFString items in the family field.  */
public int getNumFamily() {
  if ( family == null ) { 
    family = (MFString)getField( "family" ); 
  }
  return( family.getSize( ) );
}

/** Return the family value in the argument String[]
 * @param val The String[] to initialize.  */
public void getFamily(String[] val) {
  if ( family == null ) { 
    family = (MFString)getField( "family" ); 
  }
  family.getValue( val );
}

/** Set the family field. 
 * @param val The String[] to set.  */
public void setFamily(String[] val) {
  if ( family == null ) { 
    family = (MFString)getField( "family" ); 
  }
  family.setValue( val.length, val );
}

/** Return the horizontal boolean value. 
 * @return The horizontal boolean value.  */
public boolean getHorizontal() {
  if ( horizontal == null ) { 
    horizontal = (SFBool)getField( "horizontal" ); 
  }
  return( horizontal.getValue( ) );
}

/** Set the horizontal field. 
 * @param val The boolean to set.  */
public void setHorizontal(boolean val) {
  if ( horizontal == null ) { 
    horizontal = (SFBool)getField( "horizontal" ); 
  }
  horizontal.setValue( val );
}

/** Return the number of MFString items in the justify field. 
 * @return the number of MFString items in the justify field.  */
public int getNumJustify() {
  if ( justify == null ) { 
    justify = (MFString)getField( "justify" ); 
  }
  return( justify.getSize( ) );
}

/** Return the justify value in the argument String[]
 * @param val The String[] to initialize.  */
public void getJustify(String[] val) {
  if ( justify == null ) { 
    justify = (MFString)getField( "justify" ); 
  }
  justify.getValue( val );
}

/** Set the justify field. 
 * @param val The String[] to set.  */
public void setJustify(String[] val) {
  if ( justify == null ) { 
    justify = (MFString)getField( "justify" ); 
  }
  justify.setValue( val.length, val );
}

/** Return the language String value. 
 * @return The language String value.  */
public String getLanguage() {
  if ( language == null ) { 
    language = (SFString)getField( "language" ); 
  }
  return( language.getValue( ) );
}

/** Set the language field. 
 * @param val The String to set.  */
public void setLanguage(String val) {
  if ( language == null ) { 
    language = (SFString)getField( "language" ); 
  }
  language.setValue( val );
}

/** Return the leftToRight boolean value. 
 * @return The leftToRight boolean value.  */
public boolean getLeftToRight() {
  if ( leftToRight == null ) { 
    leftToRight = (SFBool)getField( "leftToRight" ); 
  }
  return( leftToRight.getValue( ) );
}

/** Set the leftToRight field. 
 * @param val The boolean to set.  */
public void setLeftToRight(boolean val) {
  if ( leftToRight == null ) { 
    leftToRight = (SFBool)getField( "leftToRight" ); 
  }
  leftToRight.setValue( val );
}

/** Return the size float value. 
 * @return The size float value.  */
public float getSize() {
  if ( size == null ) { 
    size = (SFFloat)getField( "size" ); 
  }
  return( size.getValue( ) );
}

/** Set the size field. 
 * @param val The float to set.  */
public void setSize(float val) {
  if ( size == null ) { 
    size = (SFFloat)getField( "size" ); 
  }
  size.setValue( val );
}

/** Return the spacing float value. 
 * @return The spacing float value.  */
public float getSpacing() {
  if ( spacing == null ) { 
    spacing = (SFFloat)getField( "spacing" ); 
  }
  return( spacing.getValue( ) );
}

/** Set the spacing field. 
 * @param val The float to set.  */
public void setSpacing(float val) {
  if ( spacing == null ) { 
    spacing = (SFFloat)getField( "spacing" ); 
  }
  spacing.setValue( val );
}

/** Return the style String value. 
 * @return The style String value.  */
public String getStyle() {
  if ( style == null ) { 
    style = (SFString)getField( "style" ); 
  }
  return( style.getValue( ) );
}

/** Set the style field. 
 * @param val The String to set.  */
public void setStyle(String val) {
  if ( style == null ) { 
    style = (SFString)getField( "style" ); 
  }
  style.setValue( val );
}

/** Return the topToBottom boolean value. 
 * @return The topToBottom boolean value.  */
public boolean getTopToBottom() {
  if ( topToBottom == null ) { 
    topToBottom = (SFBool)getField( "topToBottom" ); 
  }
  return( topToBottom.getValue( ) );
}

/** Set the topToBottom field. 
 * @param val The boolean to set.  */
public void setTopToBottom(boolean val) {
  if ( topToBottom == null ) { 
    topToBottom = (SFBool)getField( "topToBottom" ); 
  }
  topToBottom.setValue( val );
}

}
