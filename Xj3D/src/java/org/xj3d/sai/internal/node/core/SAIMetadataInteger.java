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

package org.xj3d.sai.internal.node.core;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.core.MetadataInteger;

/** A concrete implementation of the MetadataInteger node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIMetadataInteger extends BaseNode implements MetadataInteger {

/** The name inputOutput field */
private SFString name;

/** The reference inputOutput field */
private SFString reference;

/** The value inputOutput field */
private MFInt32 value;

/** Constructor */ 
public SAIMetadataInteger ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the name String value. 
 * @return The name String value.  */
public String getName() {
  if ( name == null ) { 
    name = (SFString)getField( "name" ); 
  }
  return( name.getValue( ) );
}

/** Set the name field. 
 * @param val The String to set.  */
public void setName(String val) {
  if ( name == null ) { 
    name = (SFString)getField( "name" ); 
  }
  name.setValue( val );
}

/** Return the reference String value. 
 * @return The reference String value.  */
public String getReference() {
  if ( reference == null ) { 
    reference = (SFString)getField( "reference" ); 
  }
  return( reference.getValue( ) );
}

/** Set the reference field. 
 * @param val The String to set.  */
public void setReference(String val) {
  if ( reference == null ) { 
    reference = (SFString)getField( "reference" ); 
  }
  reference.setValue( val );
}

/** Return the number of MFInt32 items in the value field. 
 * @return the number of MFInt32 items in the value field.  */
public int getNumValue() {
  if ( value == null ) { 
    value = (MFInt32)getField( "value" ); 
  }
  return( value.getSize( ) );
}

/** Return the value value in the argument int[]
 * @param val The int[] to initialize.  */
public void getValue(int[] val) {
  if ( value == null ) { 
    value = (MFInt32)getField( "value" ); 
  }
  value.getValue( val );
}

/** Set the value field. 
 * @param val The int[] to set.  */
public void setValue(int[] val) {
  if ( value == null ) { 
    value = (MFInt32)getField( "value" ); 
  }
  value.setValue( val.length, val );
}

}
