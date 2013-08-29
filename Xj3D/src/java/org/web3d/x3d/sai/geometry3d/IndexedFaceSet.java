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

package org.web3d.x3d.sai.geometry3d;

import org.web3d.x3d.sai.X3DComposedGeometryNode;

/** Defines the requirements of an X3D IndexedFaceSet node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface IndexedFaceSet extends X3DComposedGeometryNode {

/** Return the number of MFInt32 items in the colorIndex field. 
 * @return the number of MFInt32 items in the colorIndex field.  */
public int getNumColorIndex();

/** Return the colorIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getColorIndex(int[] val);

/** Set the colorIndex field. 
 * @param val The int[] to set.  */
public void setColorIndex(int[] val);

/** Return the number of MFInt32 items in the coordIndex field. 
 * @return the number of MFInt32 items in the coordIndex field.  */
public int getNumCoordIndex();

/** Return the coordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getCoordIndex(int[] val);

/** Set the coordIndex field. 
 * @param val The int[] to set.  */
public void setCoordIndex(int[] val);

/** Return the number of MFInt32 items in the texCoordIndex field. 
 * @return the number of MFInt32 items in the texCoordIndex field.  */
public int getNumTexCoordIndex();

/** Return the texCoordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getTexCoordIndex(int[] val);

/** Set the texCoordIndex field. 
 * @param val The int[] to set.  */
public void setTexCoordIndex(int[] val);

/** Return the number of MFInt32 items in the normalIndex field. 
 * @return the number of MFInt32 items in the normalIndex field.  */
public int getNumNormalIndex();

/** Return the normalIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getNormalIndex(int[] val);

/** Set the normalIndex field. 
 * @param val The int[] to set.  */
public void setNormalIndex(int[] val);

/** Return the creaseAngle float value. 
 * @return The creaseAngle float value.  */
public float getCreaseAngle();

/** Set the creaseAngle field. 
 * @param val The float to set.  */
public void setCreaseAngle(float val);

/** Return the convex boolean value. 
 * @return The convex boolean value.  */
public boolean getConvex();

/** Set the convex field. 
 * @param val The boolean to set.  */
public void setConvex(boolean val);

}
