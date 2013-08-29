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

package org.web3d.x3d.sai;

import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DNormalNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureCoordinateNode;

/** Defines the requirements of an X3DComposedGeometryNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.7 $ */
public interface X3DComposedGeometryNode extends X3DGeometryNode {

/** Return the number of MFNode items in the attrib field. 
 * @return the number of MFNode items in the attrib field.  */
public int getNumAttrib();

/** Return the attrib value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getAttrib(X3DNode[] val);

/** Set the attrib field. 
 * @param val The X3DNode[] to set.  */
public void setAttrib(X3DNode[] val);

/** Return the fogCoord X3DNode value. 
 * @return The fogCoord X3DNode value.  */
public X3DNode getFogCoord();

/** Set the fogCoord field. 
 * @param val The X3DNode to set.  */
public void setFogCoord(X3DNode val);

/** Return the coord X3DNode value. 
 * @return The coord X3DNode value.  */
public X3DNode getCoord();

/** Set the coord field. 
 * @param val The X3DCoordinateNode to set.  */
public void setCoord(X3DCoordinateNode val);

/** Set the coord field. 
 * @param val The X3DProtoInstance to set.  */
public void setCoord(X3DProtoInstance val);

/** Return the color X3DNode value. 
 * @return The color X3DNode value.  */
public X3DNode getColor();

/** Set the color field. 
 * @param val The X3DColorNode to set.  */
public void setColor(X3DColorNode val);

/** Set the color field. 
 * @param val The X3DProtoInstance to set.  */
public void setColor(X3DProtoInstance val);

/** Return the normal X3DNode value. 
 * @return The normal X3DNode value.  */
public X3DNode getNormal();

/** Set the normal field. 
 * @param val The X3DNormalNode to set.  */
public void setNormal(X3DNormalNode val);

/** Set the normal field. 
 * @param val The X3DProtoInstance to set.  */
public void setNormal(X3DProtoInstance val);

/** Return the texCoord X3DNode value. 
 * @return The texCoord X3DNode value.  */
public X3DNode getTexCoord();

/** Set the texCoord field. 
 * @param val The X3DTextureCoordinateNode to set.  */
public void setTexCoord(X3DTextureCoordinateNode val);

/** Set the texCoord field. 
 * @param val The X3DProtoInstance to set.  */
public void setTexCoord(X3DProtoInstance val);

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid();

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val);

/** Return the ccw boolean value. 
 * @return The ccw boolean value.  */
public boolean getCcw();

/** Set the ccw field. 
 * @param val The boolean to set.  */
public void setCcw(boolean val);

/** Return the colorPerVertex boolean value. 
 * @return The colorPerVertex boolean value.  */
public boolean getColorPerVertex();

/** Set the colorPerVertex field. 
 * @param val The boolean to set.  */
public void setColorPerVertex(boolean val);

/** Return the normalPerVertex boolean value. 
 * @return The normalPerVertex boolean value.  */
public boolean getNormalPerVertex();

/** Set the normalPerVertex field. 
 * @param val The boolean to set.  */
public void setNormalPerVertex(boolean val);

}
