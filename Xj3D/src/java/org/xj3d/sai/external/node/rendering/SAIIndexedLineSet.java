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
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DMetadataObject;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.rendering.IndexedLineSet;

/** A concrete implementation of the IndexedLineSet node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIIndexedLineSet extends SAINode implements IndexedLineSet {

/** The coord inputOutput field */
private SFNode coord;

/** The color inputOutput field */
private SFNode color;

/** The colorPerVertex initializeOnly field */
private SFBool colorPerVertex;

/** The colorIndex initializeOnly field */
private MFInt32 colorIndex;

/** The set_colorIndex inputOnly field */
private MFInt32 set_colorIndex;

/** The coordIndex initializeOnly field */
private MFInt32 coordIndex;

/** The set_coordIndex inputOnly field */
private MFInt32 set_coordIndex;

/** Constructor */ 
public SAIIndexedLineSet ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFInt32 items in the colorIndex field. 
 * @return the number of MFInt32 items in the colorIndex field.  */
public int getNumColorIndex() {
  if ( colorIndex == null ) { 
    colorIndex = (MFInt32)getField( "colorIndex" ); 
  }
  return( colorIndex.getSize( ) );
}

/** Return the colorIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getColorIndex(int[] val) {
  if ( colorIndex == null ) { 
    colorIndex = (MFInt32)getField( "colorIndex" ); 
  }
  colorIndex.getValue( val );
}

/** Set the colorIndex field. 
 * @param val The int[] to set.  */
public void setColorIndex(int[] val) {
  if ( !isRealized( ) ) { 
    if ( colorIndex == null ) { 
      colorIndex = (MFInt32)getField( "colorIndex" ); 
    } 
    colorIndex.setValue( val.length, val ); 
  } else { 
    if ( set_colorIndex == null ) { 
      set_colorIndex = (MFInt32)getField( "set_colorIndex" ); 
    } 
    set_colorIndex.setValue( val.length, val ); 
  } 
}

/** Return the number of MFInt32 items in the coordIndex field. 
 * @return the number of MFInt32 items in the coordIndex field.  */
public int getNumCoordIndex() {
  if ( coordIndex == null ) { 
    coordIndex = (MFInt32)getField( "coordIndex" ); 
  }
  return( coordIndex.getSize( ) );
}

/** Return the coordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getCoordIndex(int[] val) {
  if ( coordIndex == null ) { 
    coordIndex = (MFInt32)getField( "coordIndex" ); 
  }
  coordIndex.getValue( val );
}

/** Set the coordIndex field. 
 * @param val The int[] to set.  */
public void setCoordIndex(int[] val) {
  if ( !isRealized( ) ) { 
    if ( coordIndex == null ) { 
      coordIndex = (MFInt32)getField( "coordIndex" ); 
    } 
    coordIndex.setValue( val.length, val ); 
  } else { 
    if ( set_coordIndex == null ) { 
      set_coordIndex = (MFInt32)getField( "set_coordIndex" ); 
    } 
    set_coordIndex.setValue( val.length, val ); 
  } 
}

/** Return the coord X3DNode value. 
 * @return The coord X3DNode value.  */
public X3DNode getCoord() {
  if ( coord == null ) { 
    coord = (SFNode)getField( "coord" ); 
  }
  return( coord.getValue( ) );
}

/** Set the coord field. 
 * @param val The X3DCoordinateNode to set.  */
public void setCoord(X3DCoordinateNode val) {
  if ( coord == null ) { 
    coord = (SFNode)getField( "coord" ); 
  }
  coord.setValue( val );
}

/** Set the coord field. 
 * @param val The X3DProtoInstance to set.  */
public void setCoord(X3DProtoInstance val) {
  if ( coord == null ) { 
    coord = (SFNode)getField( "coord" ); 
  }
  coord.setValue( val );
}

/** Return the color X3DNode value. 
 * @return The color X3DNode value.  */
public X3DNode getColor() {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  return( color.getValue( ) );
}

/** Set the color field. 
 * @param val The X3DColorNode to set.  */
public void setColor(X3DColorNode val) {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  color.setValue( val );
}

/** Set the color field. 
 * @param val The X3DProtoInstance to set.  */
public void setColor(X3DProtoInstance val) {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  color.setValue( val );
}

/** Return the colorPerVertex boolean value. 
 * @return The colorPerVertex boolean value.  */
public boolean getColorPerVertex() {
  if ( colorPerVertex == null ) { 
    colorPerVertex = (SFBool)getField( "colorPerVertex" ); 
  }
  return( colorPerVertex.getValue( ) );
}

/** Set the colorPerVertex field. 
 * @param val The boolean to set.  */
public void setColorPerVertex(boolean val) {
  if ( colorPerVertex == null ) { 
    colorPerVertex = (SFBool)getField( "colorPerVertex" ); 
  }
  colorPerVertex.setValue( val );
}

}
