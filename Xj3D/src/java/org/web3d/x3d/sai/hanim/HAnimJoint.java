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

package org.web3d.x3d.sai.hanim;

import org.web3d.x3d.sai.X3DGroupingNode;

/** Defines the requirements of an X3D HAnimJoint node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface HAnimJoint extends X3DGroupingNode {

/** Return the center value in the argument float[]
 * @param val The float[] to initialize.  */
public void getCenter(float[] val);

/** Set the center field. 
 * @param val The float[] to set.  */
public void setCenter(float[] val);

/** Return the rotation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getRotation(float[] val);

/** Set the rotation field. 
 * @param val The float[] to set.  */
public void setRotation(float[] val);

/** Return the scale value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScale(float[] val);

/** Set the scale field. 
 * @param val The float[] to set.  */
public void setScale(float[] val);

/** Return the scaleOrientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getScaleOrientation(float[] val);

/** Set the scaleOrientation field. 
 * @param val The float[] to set.  */
public void setScaleOrientation(float[] val);

/** Return the translation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getTranslation(float[] val);

/** Set the translation field. 
 * @param val The float[] to set.  */
public void setTranslation(float[] val);

/** Return the name String value. 
 * @return The name String value.  */
public String getName();

/** Set the name field. 
 * @param val The String to set.  */
public void setName(String val);

/** Return the limitOrientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLimitOrientation(float[] val);

/** Set the limitOrientation field. 
 * @param val The float[] to set.  */
public void setLimitOrientation(float[] val);

/** Return the number of MFFloat items in the llimit field. 
 * @return the number of MFFloat items in the llimit field.  */
public int getNumLlimit();

/** Return the llimit value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLlimit(float[] val);

/** Set the llimit field. 
 * @param val The float[] to set.  */
public void setLlimit(float[] val);

/** Return the number of MFInt32 items in the skinCoordIndex field. 
 * @return the number of MFInt32 items in the skinCoordIndex field.  */
public int getNumSkinCoordIndex();

/** Return the skinCoordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getSkinCoordIndex(int[] val);

/** Set the skinCoordIndex field. 
 * @param val The int[] to set.  */
public void setSkinCoordIndex(int[] val);

/** Return the number of MFFloat items in the skinCoordWeight field. 
 * @return the number of MFFloat items in the skinCoordWeight field.  */
public int getNumSkinCoordWeight();

/** Return the skinCoordWeight value in the argument float[]
 * @param val The float[] to initialize.  */
public void getSkinCoordWeight(float[] val);

/** Set the skinCoordWeight field. 
 * @param val The float[] to set.  */
public void setSkinCoordWeight(float[] val);

/** Return the number of MFFloat items in the stiffness field. 
 * @return the number of MFFloat items in the stiffness field.  */
public int getNumStiffness();

/** Return the stiffness value in the argument float[]
 * @param val The float[] to initialize.  */
public void getStiffness(float[] val);

/** Set the stiffness field. 
 * @param val The float[] to set.  */
public void setStiffness(float[] val);

/** Return the number of MFFloat items in the ulimit field. 
 * @return the number of MFFloat items in the ulimit field.  */
public int getNumUlimit();

/** Return the ulimit value in the argument float[]
 * @param val The float[] to initialize.  */
public void getUlimit(float[] val);

/** Set the ulimit field. 
 * @param val The float[] to set.  */
public void setUlimit(float[] val);

}
