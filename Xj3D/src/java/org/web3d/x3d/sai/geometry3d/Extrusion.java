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

import org.web3d.x3d.sai.X3DGeometryNode;

/** Defines the requirements of an X3D Extrusion node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Extrusion extends X3DGeometryNode {

/** Return the beginCap boolean value. 
 * @return The beginCap boolean value.  */
public boolean getBeginCap();

/** Set the beginCap field. 
 * @param val The boolean to set.  */
public void setBeginCap(boolean val);

/** Return the ccw boolean value. 
 * @return The ccw boolean value.  */
public boolean getCcw();

/** Set the ccw field. 
 * @param val The boolean to set.  */
public void setCcw(boolean val);

/** Return the convex boolean value. 
 * @return The convex boolean value.  */
public boolean getConvex();

/** Set the convex field. 
 * @param val The boolean to set.  */
public void setConvex(boolean val);

/** Return the creaseAngle float value. 
 * @return The creaseAngle float value.  */
public float getCreaseAngle();

/** Set the creaseAngle field. 
 * @param val The float to set.  */
public void setCreaseAngle(float val);

/** Return the number of MFVec2f items in the crossSection field. 
 * @return the number of MFVec2f items in the crossSection field.  */
public int getNumCrossSection();

/** Return the crossSection value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCrossSection(float[] val);

/** Set the crossSection field. 
 * @param val The float[] to set.  */
public void setCrossSection(float[] val);

/** Return the endCap boolean value. 
 * @return The endCap boolean value.  */
public boolean getEndCap();

/** Set the endCap field. 
 * @param val The boolean to set.  */
public void setEndCap(boolean val);

/** Return the number of MFRotation items in the orientation field. 
 * @return the number of MFRotation items in the orientation field.  */
public int getNumOrientation();

/** Return the orientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getOrientation(float[] val);

/** Set the orientation field. 
 * @param val The float[] to set.  */
public void setOrientation(float[] val);

/** Return the number of MFVec2f items in the scale field. 
 * @return the number of MFVec2f items in the scale field.  */
public int getNumScale();

/** Return the scale value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScale(float[] val);

/** Set the scale field. 
 * @param val The float[] to set.  */
public void setScale(float[] val);

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
public boolean getSolid();

/** Set the solid field. 
 * @param val The boolean to set.  */
public void setSolid(boolean val);

/** Return the number of MFVec3f items in the spine field. 
 * @return the number of MFVec3f items in the spine field.  */
public int getNumSpine();

/** Return the spine value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSpine(float[] val);

/** Set the spine field. 
 * @param val The float[] to set.  */
public void setSpine(float[] val);

}
