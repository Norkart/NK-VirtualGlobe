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

package org.web3d.x3d.sai.text;

import org.web3d.x3d.sai.X3DFontStyleNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;

/** Defines the requirements of an X3D Text node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Text extends X3DGeometryNode {

/** Return the number of MFString items in the string field. 
 * @return the number of MFString items in the string field.  */
public int getNumString();

/** Return the string value in the argument String[]
 * @param val The String[] to initialize.  */
public void getString(String[] val);

/** Set the string field. 
 * @param val The String[] to set.  */
public void setString(String[] val);

/** Return the fontStyle X3DNode value. 
 * @return The fontStyle X3DNode value.  */
public X3DNode getFontStyle();

/** Set the fontStyle field. 
 * @param val The X3DFontStyleNode to set.  */
public void setFontStyle(X3DFontStyleNode val);

/** Set the fontStyle field. 
 * @param val The X3DProtoInstance to set.  */
public void setFontStyle(X3DProtoInstance val);

/** Return the number of MFFloat items in the length field. 
 * @return the number of MFFloat items in the length field.  */
public int getNumLength();

/** Return the length value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLength(float[] val);

/** Set the length field. 
 * @param val The float[] to set.  */
public void setLength(float[] val);

/** Return the maxExtent float value. 
 * @return The maxExtent float value.  */
public float getMaxExtent();

/** Set the maxExtent field. 
 * @param val The float to set.  */
public void setMaxExtent(float val);

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid();

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val);

}
