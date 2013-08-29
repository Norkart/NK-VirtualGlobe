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
import org.web3d.x3d.sai.SFColorRGBA;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.texturing.TextureProperties;

/** A concrete implementation of the TextureProperties node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITextureProperties extends BaseNode implements TextureProperties {

/** The boundaryColor initializeOnly field */
private SFColorRGBA boundaryColor;

/** The boundaryWidth initializeOnly field */
private SFInt32 boundaryWidth;

/** The boundaryModeS initializeOnly field */
private SFString boundaryModeS;

/** The boundaryModeT initializeOnly field */
private SFString boundaryModeT;

/** The magnificationFilter initializeOnly field */
private SFString magnificationFilter;

/** The minificationFilter initializeOnly field */
private SFString minificationFilter;

/** The generateMipMaps initializeOnly field */
private SFBool generateMipMaps;

/** The anisotropicMode initializeOnly field */
private SFString anisotropicMode;

/** The anisotropicFilterDegree initializeOnly field */
private SFFloat anisotropicFilterDegree;

/** Constructor */ 
public SAITextureProperties ( 
  VRMLNodeType node, 
  ReferenceQueue refQueue, 
  FieldFactory fac, 
  FieldAccessListener fal, 
  BaseNodeFactory bnf ) { 
    super( node, refQueue, fac, fal, bnf ); 
}

/** Return the boundaryColor value in the argument float[]
 * @param val The float[] to initialize.  */
public void getBoundaryColor(float[] val) {
  if ( boundaryColor == null ) { 
    boundaryColor = (SFColorRGBA)getField( "boundaryColor" ); 
  }
  boundaryColor.getValue( val );
}

/** Set the boundaryColor field. 
 * @param val The float[] to set.  */
public void setBoundaryColor(float[] val) {
  if ( boundaryColor == null ) { 
    boundaryColor = (SFColorRGBA)getField( "boundaryColor" ); 
  }
  boundaryColor.setValue( val );
}

/** Return the boundaryWidth int value. 
 * @return The boundaryWidth int value.  */
public int getBoundaryWidth() {
  if ( boundaryWidth == null ) { 
    boundaryWidth = (SFInt32)getField( "boundaryWidth" ); 
  }
  return( boundaryWidth.getValue( ) );
}

/** Set the boundaryWidth field. 
 * @param val The int to set.  */
public void setBoundaryWidth(int val) {
  if ( boundaryWidth == null ) { 
    boundaryWidth = (SFInt32)getField( "boundaryWidth" ); 
  }
  boundaryWidth.setValue( val );
}

/** Return the boundaryModeS String value. 
 * @return The boundaryModeS String value.  */
public String getBoundaryModeS() {
  if ( boundaryModeS == null ) { 
    boundaryModeS = (SFString)getField( "boundaryModeS" ); 
  }
  return( boundaryModeS.getValue( ) );
}

/** Set the boundaryModeS field. 
 * @param val The String to set.  */
public void setBoundaryModeS(String val) {
  if ( boundaryModeS == null ) { 
    boundaryModeS = (SFString)getField( "boundaryModeS" ); 
  }
  boundaryModeS.setValue( val );
}

/** Return the boundaryModeT String value. 
 * @return The boundaryModeT String value.  */
public String getBoundaryModeT() {
  if ( boundaryModeT == null ) { 
    boundaryModeT = (SFString)getField( "boundaryModeT" ); 
  }
  return( boundaryModeT.getValue( ) );
}

/** Set the boundaryModeT field. 
 * @param val The String to set.  */
public void setBoundaryModeT(String val) {
  if ( boundaryModeT == null ) { 
    boundaryModeT = (SFString)getField( "boundaryModeT" ); 
  }
  boundaryModeT.setValue( val );
}

/** Return the magnificationFilter String value. 
 * @return The magnificationFilter String value.  */
public String getMagnificationFilter() {
  if ( magnificationFilter == null ) { 
    magnificationFilter = (SFString)getField( "magnificationFilter" ); 
  }
  return( magnificationFilter.getValue( ) );
}

/** Set the magnificationFilter field. 
 * @param val The String to set.  */
public void setMagnificationFilter(String val) {
  if ( magnificationFilter == null ) { 
    magnificationFilter = (SFString)getField( "magnificationFilter" ); 
  }
  magnificationFilter.setValue( val );
}

/** Return the minificationFilter String value. 
 * @return The minificationFilter String value.  */
public String getMinificationFilter() {
  if ( minificationFilter == null ) { 
    minificationFilter = (SFString)getField( "minificationFilter" ); 
  }
  return( minificationFilter.getValue( ) );
}

/** Set the minificationFilter field. 
 * @param val The String to set.  */
public void setMinificationFilter(String val) {
  if ( minificationFilter == null ) { 
    minificationFilter = (SFString)getField( "minificationFilter" ); 
  }
  minificationFilter.setValue( val );
}

/** Return the generateMipMaps boolean value. 
 * @return The generateMipMaps boolean value.  */
public boolean getGenerateMipMaps() {
  if ( generateMipMaps == null ) { 
    generateMipMaps = (SFBool)getField( "generateMipMaps" ); 
  }
  return( generateMipMaps.getValue( ) );
}

/** Set the generateMipMaps field. 
 * @param val The boolean to set.  */
public void setGenerateMipMaps(boolean val) {
  if ( generateMipMaps == null ) { 
    generateMipMaps = (SFBool)getField( "generateMipMaps" ); 
  }
  generateMipMaps.setValue( val );
}

/** Return the anisotropicMode String value. 
 * @return The anisotropicMode String value.  */
public String getAnisotropicMode() {
  if ( anisotropicMode == null ) { 
    anisotropicMode = (SFString)getField( "anisotropicMode" ); 
  }
  return( anisotropicMode.getValue( ) );
}

/** Set the anisotropicMode field. 
 * @param val The String to set.  */
public void setAnisotropicMode(String val) {
  if ( anisotropicMode == null ) { 
    anisotropicMode = (SFString)getField( "anisotropicMode" ); 
  }
  anisotropicMode.setValue( val );
}

/** Return the anisotropicFilterDegree float value. 
 * @return The anisotropicFilterDegree float value.  */
public float getAnisotropicFilterDegree() {
  if ( anisotropicFilterDegree == null ) { 
    anisotropicFilterDegree = (SFFloat)getField( "anisotropicFilterDegree" ); 
  }
  return( anisotropicFilterDegree.getValue( ) );
}

/** Set the anisotropicFilterDegree field. 
 * @param val The float to set.  */
public void setAnisotropicFilterDegree(float val) {
  if ( anisotropicFilterDegree == null ) { 
    anisotropicFilterDegree = (SFFloat)getField( "anisotropicFilterDegree" ); 
  }
  anisotropicFilterDegree.setValue( val );
}

}
