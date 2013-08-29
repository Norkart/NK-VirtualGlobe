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

package org.web3d.x3d.sai.networking;

import org.web3d.x3d.sai.X3DNetworkSensorNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D LoadSensor node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface LoadSensor extends X3DNetworkSensorNode {

/** Return the number of MFNode items in the watchList field. 
 * @return the number of MFNode items in the watchList field.  */
public int getNumWatchList();

/** Return the watchList value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getWatchList(X3DNode[] val);

/** Set the watchList field. 
 * @param val The X3DNode[] to set.  */
public void setWatchList(X3DNode[] val);

/** Return the timeOut double value. 
 * @return The timeOut double value.  */
public double getTimeOut();

/** Set the timeOut field. 
 * @param val The double to set.  */
public void setTimeOut(double val);

/** Return the loadTime double value. 
 * @return The loadTime double value.  */
public double getLoadTime();

/** Return the isLoaded boolean value. 
 * @return The isLoaded boolean value.  */
public boolean getIsLoaded();

/** Return the progress float value. 
 * @return The progress float value.  */
public float getProgress();

}
