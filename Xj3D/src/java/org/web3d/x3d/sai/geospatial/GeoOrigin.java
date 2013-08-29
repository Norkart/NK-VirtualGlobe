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

/** Defines the requirements of an X3D GeoOrigin node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface GeoOrigin extends X3DNode {

/** Return the number of MFString items in the geoSystem field. 
 * @return the number of MFString items in the geoSystem field.  */
public int getNumGeoSystem();

/** Return the geoSystem value in the argument String[]
 * @param val The String[] to initialize.  */
public void getGeoSystem(String[] val);

/** Set the geoSystem field. 
 * @param val The String[] to set.  */
public void setGeoSystem(String[] val);

/** Return the geoCoords value in the argument double[]
 * @param val The double[] to initialize.  */
public void getGeoCoords(double[] val);

/** Set the geoCoords field. 
 * @param val The double[] to set.  */
public void setGeoCoords(double[] val);

/** Return the rotateYUp boolean value. 
 * @return The rotateYUp boolean value.  */
public boolean getRotateYUp();

/** Set the rotateYUp field. 
 * @param val The boolean to set.  */
public void setRotateYUp(boolean val);

}
