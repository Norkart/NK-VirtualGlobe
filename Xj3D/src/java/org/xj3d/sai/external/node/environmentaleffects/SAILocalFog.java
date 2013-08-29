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

package org.xj3d.sai.external.node.environmentaleffects;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.environmentaleffects.LocalFog;

/** A concrete implementation of the LocalFog node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAILocalFog extends SAINode implements LocalFog {

/** The enabled inputOutput field */
private SFBool enabled;

/** The color inputOutput field */
private SFColor color;

/** The fogType initializeOnly field */
private SFString fogType;

/** The visibilityRange inputOutput field */
private SFFloat visibilityRange;

/** Constructor */ 
public SAILocalFog ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the enabled boolean value. 
 * @return The enabled boolean value.  */
public boolean getEnabled() {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  return( enabled.getValue( ) );
}

/** Set the enabled field. 
 * @param val The boolean to set.  */
public void setEnabled(boolean val) {
  if ( enabled == null ) { 
    enabled = (SFBool)getField( "enabled" ); 
  }
  enabled.setValue( val );
}

/** Return the color value in the argument float[]
 * @param val The float[] to initialize.  */
public void getColor(float[] val) {
  if ( color == null ) { 
    color = (SFColor)getField( "color" ); 
  }
  color.getValue( val );
}

/** Set the color field. 
 * @param val The float[] to set.  */
public void setColor(float[] val) {
  if ( color == null ) { 
    color = (SFColor)getField( "color" ); 
  }
  color.setValue( val );
}

/** Return the fogType String value. 
 * @return The fogType String value.  */
public String getFogType() {
  if ( fogType == null ) { 
    fogType = (SFString)getField( "fogType" ); 
  }
  return( fogType.getValue( ) );
}

/** Set the fogType field. 
 * @param val The String to set.  */
public void setFogType(String val) {
  if ( fogType == null ) { 
    fogType = (SFString)getField( "fogType" ); 
  }
  fogType.setValue( val );
}

/** Return the visibilityRange float value. 
 * @return The visibilityRange float value.  */
public float getVisibilityRange() {
  if ( visibilityRange == null ) { 
    visibilityRange = (SFFloat)getField( "visibilityRange" ); 
  }
  return( visibilityRange.getValue( ) );
}

/** Set the visibilityRange field. 
 * @param val The float to set.  */
public void setVisibilityRange(float val) {
  if ( visibilityRange == null ) { 
    visibilityRange = (SFFloat)getField( "visibilityRange" ); 
  }
  visibilityRange.setValue( val );
}

}
