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

package org.xj3d.sai.external.node.rendering;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFColorRGBA;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.rendering.ColorRGBA;

/** A concrete implementation of the ColorRGBA node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIColorRGBA extends SAINode implements ColorRGBA {

/** The color inputOutput field */
private MFColorRGBA color;

/** Constructor */ 
public SAIColorRGBA ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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
