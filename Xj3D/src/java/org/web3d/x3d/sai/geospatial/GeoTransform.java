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

package org.web3d.x3d.sai.geospatial;

import org.web3d.x3d.sai.X3DGroupingNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D GeoTransform node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface GeoTransform extends X3DGroupingNode {

/** Return the geoCenter value in the argument double[]
 * @param val The double[] to initialize.  */
public void getGeoCenter(double[] val);

/** Set the geoCenter field. 
 * @param val The double[] to set.  */
public void setGeoCenter(double[] val);

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

/** Return the geoOrigin X3DNode value. 
 * @return The geoOrigin X3DNode value.  */
public X3DNode getGeoOrigin();

/** Set the geoOrigin field. 
 * @param val The X3DNode to set.  */
public void setGeoOrigin(X3DNode val);

/** Return the number of MFString items in the geoSystem field. 
 * @return the number of MFString items in the geoSystem field.  */
public int getNumGeoSystem();

/** Return the geoSystem value in the argument String[]
 * @param val The String[] to initialize.  */
public void getGeoSystem(String[] val);

/** Set the geoSystem field. 
 * @param val The String[] to set.  */
public void setGeoSystem(String[] val);

}
