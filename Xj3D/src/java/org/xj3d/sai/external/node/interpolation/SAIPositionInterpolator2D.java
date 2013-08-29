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

package org.xj3d.sai.external.node.interpolation;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFVec2f;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.interpolation.PositionInterpolator2D;

/** A concrete implementation of the PositionInterpolator2D node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPositionInterpolator2D extends SAINode implements PositionInterpolator2D {

/** The key inputOutput field */
private MFFloat key;

/** The keyValue inputOutput field */
private MFVec2f keyValue;

/** The set_fraction inputOnly field */
private SFFloat set_fraction;

/** The value_changed outputOnly field */
private SFVec2f value_changed;

/** Constructor */ 
public SAIPositionInterpolator2D ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFFloat items in the key field. 
 * @return the number of MFFloat items in the key field.  */
public int getNumKey() {
  if ( key == null ) { 
    key = (MFFloat)getField( "key" ); 
  }
  return( key.getSize( ) );
}

/** Return the key value in the argument float[]
 * @param val The float[] to initialize.  */
public void getKey(float[] val) {
  if ( key == null ) { 
    key = (MFFloat)getField( "key" ); 
  }
  key.getValue( val );
}

/** Set the key field. 
 * @param val The float[] to set.  */
public void setKey(float[] val) {
  if ( key == null ) { 
    key = (MFFloat)getField( "key" ); 
  }
  key.setValue( val.length, val );
}

/** Return the number of MFVec2f items in the keyValue field. 
 * @return the number of MFVec2f items in the keyValue field.  */
public int getNumKeyValue() {
  if ( keyValue == null ) { 
    keyValue = (MFVec2f)getField( "keyValue" ); 
  }
  return( keyValue.getSize( ) );
}

/** Return the keyValue value in the argument float[]
 * @param val The float[] to initialize.  */
public void getKeyValue(float[] val) {
  if ( keyValue == null ) { 
    keyValue = (MFVec2f)getField( "keyValue" ); 
  }
  keyValue.getValue( val );
}

/** Set the keyValue field. 
 * @param val The float[] to set.  */
public void setKeyValue(float[] val) {
  if ( keyValue == null ) { 
    keyValue = (MFVec2f)getField( "keyValue" ); 
  }
  keyValue.setValue( val.length/2, val );
}

/** Set the set_fraction field. 
 * @param val The float to set.  */
public void setFraction(float val) {
  if ( set_fraction == null ) { 
    set_fraction = (SFFloat)getField( "set_fraction" ); 
  }
  set_fraction.setValue( val );
}

/** Return the value_changed value in the argument float[]
 * @param val The float[] to initialize.  */
public void getValue(float[] val) {
  if ( value_changed == null ) { 
    value_changed = (SFVec2f)getField( "value_changed" ); 
  }
  value_changed.getValue( val );
}

}
