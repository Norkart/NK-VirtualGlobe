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

import org.web3d.x3d.sai.X3DBoundedObject;
import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D HAnimHumanoid node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface HAnimHumanoid extends X3DChildNode, X3DBoundedObject {

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

/** Return the number of MFString items in the info field. 
 * @return the number of MFString items in the info field.  */
public int getNumInfo();

/** Return the info value in the argument String[]
 * @param val The String[] to initialize.  */
public void getInfo(String[] val);

/** Set the info field. 
 * @param val The String[] to set.  */
public void setInfo(String[] val);

/** Return the number of MFNode items in the joints field. 
 * @return the number of MFNode items in the joints field.  */
public int getNumJoints();

/** Return the joints value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getJoints(X3DNode[] val);

/** Set the joints field. 
 * @param val The X3DNode[] to set.  */
public void setJoints(X3DNode[] val);

/** Return the number of MFNode items in the segments field. 
 * @return the number of MFNode items in the segments field.  */
public int getNumSegments();

/** Return the segments value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSegments(X3DNode[] val);

/** Set the segments field. 
 * @param val The X3DNode[] to set.  */
public void setSegments(X3DNode[] val);

/** Return the number of MFNode items in the sites field. 
 * @return the number of MFNode items in the sites field.  */
public int getNumSites();

/** Return the sites value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSites(X3DNode[] val);

/** Set the sites field. 
 * @param val The X3DNode[] to set.  */
public void setSites(X3DNode[] val);

/** Return the number of MFNode items in the skeleton field. 
 * @return the number of MFNode items in the skeleton field.  */
public int getNumSkeleton();

/** Return the skeleton value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSkeleton(X3DNode[] val);

/** Set the skeleton field. 
 * @param val The X3DNode[] to set.  */
public void setSkeleton(X3DNode[] val);

/** Return the number of MFNode items in the skin field. 
 * @return the number of MFNode items in the skin field.  */
public int getNumSkin();

/** Return the skin value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getSkin(X3DNode[] val);

/** Set the skin field. 
 * @param val The X3DNode[] to set.  */
public void setSkin(X3DNode[] val);

/** Return the skinCoord X3DNode value. 
 * @return The skinCoord X3DNode value.  */
public X3DNode getSkinCoord();

/** Set the skinCoord field. 
 * @param val The X3DNode to set.  */
public void setSkinCoord(X3DNode val);

/** Return the skinNormal X3DNode value. 
 * @return The skinNormal X3DNode value.  */
public X3DNode getSkinNormal();

/** Set the skinNormal field. 
 * @param val The X3DNode to set.  */
public void setSkinNormal(X3DNode val);

/** Return the version String value. 
 * @return The version String value.  */
public String getVersion();

/** Set the version field. 
 * @param val The String to set.  */
public void setVersion(String val);

/** Return the number of MFNode items in the viewpoints field. 
 * @return the number of MFNode items in the viewpoints field.  */
public int getNumViewpoints();

/** Return the viewpoints value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getViewpoints(X3DNode[] val);

/** Set the viewpoints field. 
 * @param val The X3DNode[] to set.  */
public void setViewpoints(X3DNode[] val);

}
