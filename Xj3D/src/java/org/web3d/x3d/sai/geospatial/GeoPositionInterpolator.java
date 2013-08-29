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

import org.web3d.x3d.sai.X3DInterpolatorNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D GeoPositionInterpolator node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface GeoPositionInterpolator extends X3DInterpolatorNode {

/** Return the number of MFVec3d items in the keyValue field. 
 * @return the number of MFVec3d items in the keyValue field.  */
public int getNumKeyValue();

/** Return the keyValue value in the argument double[]
 * @param val The double[] to initialize.  */
public void getKeyValue(double[] val);

/** Set the keyValue field. 
 * @param val The double[] to set.  */
public void setKeyValue(double[] val);

/** Return the value value in the argument float[]
 * @param val The float[] to initialize.  */
public void getValue(float[] val);

/** Return the geovalue value in the argument double[]
 * @param val The double[] to initialize.  */
public void getGeovalue(double[] val);

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
