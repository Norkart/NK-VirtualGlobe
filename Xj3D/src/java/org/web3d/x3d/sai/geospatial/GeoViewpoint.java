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

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DViewpointNode;

/** Defines the requirements of an X3D GeoViewpoint node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface GeoViewpoint extends X3DViewpointNode {

/** Return the fieldOfView float value. 
 * @return The fieldOfView float value.  */
public float getFieldOfView();

/** Set the fieldOfView field. 
 * @param val The float to set.  */
public void setFieldOfView(float val);

/** Return the headlight boolean value. 
 * @return The headlight boolean value.  */
public boolean getHeadlight();

/** Set the headlight field. 
 * @param val The boolean to set.  */
public void setHeadlight(boolean val);

/** Return the number of MFString items in the navType field. 
 * @return the number of MFString items in the navType field.  */
public int getNumNavType();

/** Return the navType value in the argument String[]
 * @param val The String[] to initialize.  */
public void getNavType(String[] val);

/** Set the navType field. 
 * @param val The String[] to set.  */
public void setNavType(String[] val);

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

/** Return the orientation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getOrientation(float[] val);

/** Set the orientation field. 
 * @param val The float[] to set.  */
public void setOrientation(float[] val);

/** Return the position value in the argument double[]
 * @param val The double[] to initialize.  */
public void getPosition(double[] val);

/** Set the position field. 
 * @param val The double[] to set.  */
public void setPosition(double[] val);

/** Return the speedFactor float value. 
 * @return The speedFactor float value.  */
public float getSpeedFactor();

/** Set the speedFactor field. 
 * @param val The float to set.  */
public void setSpeedFactor(float val);

}
