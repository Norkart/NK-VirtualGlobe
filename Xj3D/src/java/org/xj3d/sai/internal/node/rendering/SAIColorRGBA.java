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
import org.web3d.x3d.sai.MFColorRGBA;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.rendering.ColorRGBA;

/** A concrete implementation of the ColorRGBA node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIColorRGBA extends BaseNode implements ColorRGBA {

/** The color inputOutput field */
private MFColorRGBA color;

/** Constructor */ 
public SAIColorRGBA ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the number of MFColorRGBA items in the color field. 
 * @return the number of MFColorRGBA items in the color field.  */
public int getNumColor() {
  if ( color == null ) { 
    color = (MFColorRGBA)getField( "color" ); 
  }
  return( color.getSize( ) );
}

/** Return the color value in the argument float[]
 * @param val The float[] to initialize.  */
public void getColor(float[] val) {
  if ( color == null ) { 
    color = (MFColorRGBA)getField( "color" ); 
  }
  color.getValue( val );
}

/** Set the color field. 
 * @param val The float[] to set.  */
public void setColor(float[] val) {
  if ( color == null ) { 
    color = (MFColorRGBA)getField( "color" ); 
  }
  color.setValue( val.length/4, val );
}

}
