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

package org.xj3d.sai.external.node.dis;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.dis.DISEntityTypeMapping;

/** A concrete implementation of the DISEntityTypeMapping node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIDISEntityTypeMapping extends SAINode implements DISEntityTypeMapping {

/** The domain initializeOnly field */
private SFInt32 domain;

/** The country initializeOnly field */
private SFInt32 country;

/** The category initializeOnly field */
private SFInt32 category;

/** The subCategory initializeOnly field */
private SFInt32 subCategory;

/** The specific initializeOnly field */
private SFInt32 specific;

/** The kind initializeOnly field */
private SFInt32 kind;

/** The extra initializeOnly field */
private SFInt32 extra;

/** The url initializeOnly field */
private MFString url;

/** Constructor */ 
public SAIDISEntityTypeMapping ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the domain int value. 
 * @return The domain int value.  */
public int getDomain() {
  if ( domain == null ) { 
    domain = (SFInt32)getField( "domain" ); 
  }
  return( domain.getValue( ) );
}

/** Set the domain field. 
 * @param val The int to set.  */
public void setDomain(int val) {
  if ( domain == null ) { 
    domain = (SFInt32)getField( "domain" ); 
  }
  domain.setValue( val );
}

/** Return the country int value. 
 * @return The country int value.  */
public int getCountry() {
  if ( country == null ) { 
    country = (SFInt32)getField( "country" ); 
  }
  return( country.getValue( ) );
}

/** Set the country field. 
 * @param val The int to set.  */
public void setCountry(int val) {
  if ( country == null ) { 
    country = (SFInt32)getField( "country" ); 
  }
  country.setValue( val );
}

/** Return the category int value. 
 * @return The category int value.  */
public int getCategory() {
  if ( category == null ) { 
    category = (SFInt32)getField( "category" ); 
  }
  return( category.getValue( ) );
}

/** Set the category field. 
 * @param val The int to set.  */
public void setCategory(int val) {
  if ( category == null ) { 
    category = (SFInt32)getField( "category" ); 
  }
  category.setValue( val );
}

/** Return the subCategory int value. 
 * @return The subCategory int value.  */
public int getSubCategory() {
  if ( subCategory == null ) { 
    subCategory = (SFInt32)getField( "subCategory" ); 
  }
  return( subCategory.getValue( ) );
}

/** Set the subCategory field. 
 * @param val The int to set.  */
public void setSubCategory(int val) {
  if ( subCategory == null ) { 
    subCategory = (SFInt32)getField( "subCategory" ); 
  }
  subCategory.setValue( val );
}

/** Return the specific int value. 
 * @return The specific int value.  */
public int getSpecific() {
  if ( specific == null ) { 
    specific = (SFInt32)getField( "specific" ); 
  }
  return( specific.getValue( ) );
}

/** Set the specific field. 
 * @param val The int to set.  */
public void setSpecific(int val) {
  if ( specific == null ) { 
    specific = (SFInt32)getField( "specific" ); 
  }
  specific.setValue( val );
}

/** Return the kind int value. 
 * @return The kind int value.  */
public int getKind() {
  if ( kind == null ) { 
    kind = (SFInt32)getField( "kind" ); 
  }
  return( kind.getValue( ) );
}

/** Set the kind field. 
 * @param val The int to set.  */
public void setKind(int val) {
  if ( kind == null ) { 
    kind = (SFInt32)getField( "kind" ); 
  }
  kind.setValue( val );
}

/** Return the extra int value. 
 * @return The extra int value.  */
public int getExtra() {
  if ( extra == null ) { 
    extra = (SFInt32)getField( "extra" ); 
  }
  return( extra.getValue( ) );
}

/** Set the extra field. 
 * @param val The int to set.  */
public void setExtra(int val) {
  if ( extra == null ) { 
    extra = (SFInt32)getField( "extra" ); 
  }
  extra.setValue( val );
}

/** Return the number of MFString items in the url field. 
 * @return the number of MFString items in the url field.  */
public int getNumUrl() {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  return( url.getSize( ) );
}

/** Return the url value in the argument String[]
 * @param val The String[] to initialize.  */
public void getUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.getValue( val );
}

/** Set the url field. 
 * @param val The String[] to set.  */
public void setUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.setValue( val.length, val );
}

}
