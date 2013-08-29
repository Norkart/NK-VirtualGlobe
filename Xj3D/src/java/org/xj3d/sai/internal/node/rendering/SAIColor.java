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

package org.xj3d.sai.internal.node.rendering;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFColor;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.rendering.Color;

/** A concrete implementation of the Color node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIColor extends BaseNode implements Color {

/** The color inputOutput field */
private MFColor color;

/** Constructor */ 
public SAIColor ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFColor items in the color field. 
 * @return the number of MFColor items in the color field.  */
public int getNumColor() {
  if ( color == null ) { 
    color = (MFColor)getField( "color" ); 
  }
  return( color.getSize( ) );
}

/** Return the color value in the argument float[]
 * @param val The float[] to initialize.  */
public void getColor(float[] val) {
  if ( color == null ) { 
    color = (MFColor)getField( "color" ); 
  }
  color.getValue( val );
}

/** Set the color field. 
 * @param val The float[] to set.  */
public void setColor(float[] val) {
  if ( color == null ) { 
    color = (MFColor)getField( "color" ); 
  }
  color.setValue( val.length/3, val );
}

}
