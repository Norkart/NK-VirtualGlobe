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

package org.web3d.x3d.sai.dis;

import org.web3d.x3d.sai.X3DGroupingNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DSensorNode;

/** Defines the requirements of an X3D EspduTransform node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface EspduTransform extends X3DGroupingNode, X3DSensorNode {

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

/** Return the marking String value. 
 * @return The marking String value.  */
public String getMarking();

/** Set the marking field. 
 * @param val The String to set.  */
public void setMarking(String val);

/** Return the siteID int value. 
 * @return The siteID int value.  */
public int getSiteID();

/** Set the siteID field. 
 * @param val The int to set.  */
public void setSiteID(int val);

/** Return the applicationID int value. 
 * @return The applicationID int value.  */
public int getApplicationID();

/** Set the applicationID field. 
 * @param val The int to set.  */
public void setApplicationID(int val);

/** Return the entityID int value. 
 * @return The entityID int value.  */
public int getEntityID();

/** Set the entityID field. 
 * @param val The int to set.  */
public void setEntityID(int val);

/** Return the readInterval double value. 
 * @return The readInterval double value.  */
public double getReadInterval();

/** Set the readInterval field. 
 * @param val The double to set.  */
public void setReadInterval(double val);

/** Return the writeInterval double value. 
 * @return The writeInterval double value.  */
public double getWriteInterval();

/** Set the writeInterval field. 
 * @param val The double to set.  */
public void setWriteInterval(double val);

/** Return the networkMode String value. 
 * @return The networkMode String value.  */
public String getNetworkMode();

/** Set the networkMode field. 
 * @param val The String to set.  */
public void setNetworkMode(String val);

/** Return the address String value. 
 * @return The address String value.  */
public String getAddress();

/** Set the address field. 
 * @param val The String to set.  */
public void setAddress(String val);

/** Return the port int value. 
 * @return The port int value.  */
public int getPort();

/** Set the port field. 
 * @param val The int to set.  */
public void setPort(int val);

/** Return the articulationParameterCount int value. 
 * @return The articulationParameterCount int value.  */
public int getArticulationParameterCount();

/** Set the articulationParameterCount field. 
 * @param val The int to set.  */
public void setArticulationParameterCount(int val);

/** Return the number of MFFloat items in the articulationParameterArray field. 
 * @return the number of MFFloat items in the articulationParameterArray field.  */
public int getNumArticulationParameterArray();

/** Return the articulationParameterArray value in the argument float[]
 * @param val The float[] to initialize.  */
public void getArticulationParameterArray(float[] val);

/** Set the articulationParameterArray field. 
 * @param val The float[] to set.  */
public void setArticulationParameterArray(float[] val);

/** Return the articulationParameterValue0 float value. 
 * @return The articulationParameterValue0 float value.  */
public float getArticulationParameterValue0();

/** Return the articulationParameterValue1 float value. 
 * @return The articulationParameterValue1 float value.  */
public float getArticulationParameterValue1();

/** Return the articulationParameterValue2 float value. 
 * @return The articulationParameterValue2 float value.  */
public float getArticulationParameterValue2();

/** Return the articulationParameterValue3 float value. 
 * @return The articulationParameterValue3 float value.  */
public float getArticulationParameterValue3();

/** Return the articulationParameterValue4 float value. 
 * @return The articulationParameterValue4 float value.  */
public float getArticulationParameterValue4();

/** Return the articulationParameterValue5 float value. 
 * @return The articulationParameterValue5 float value.  */
public float getArticulationParameterValue5();

/** Return the articulationParameterValue6 float value. 
 * @return The articulationParameterValue6 float value.  */
public float getArticulationParameterValue6();

/** Return the articulationParameterValue7 float value. 
 * @return The articulationParameterValue7 float value.  */
public float getArticulationParameterValue7();

/** Set the articulationParameterValue0 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue0(float val);

/** Set the articulationParameterValue1 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue1(float val);

/** Set the articulationParameterValue2 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue2(float val);

/** Set the articulationParameterValue3 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue3(float val);

/** Set the articulationParameterValue4 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue4(float val);

/** Set the articulationParameterValue5 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue5(float val);

/** Set the articulationParameterValue6 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue6(float val);

/** Set the articulationParameterValue7 field. 
 * @param val The float to set.  */
public void setArticulationParameterValue7(float val);

/** Return the timestamp double value. 
 * @return The timestamp double value.  */
public double getTimestamp();

/** Return the detonationResult int value. 
 * @return The detonationResult int value.  */
public int getDetonationResult();

/** Set the detonationResult field. 
 * @param val The int to set.  */
public void setDetonationResult(int val);

/** Return the detonationLocation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDetonationLocation(float[] val);

/** Set the detonationLocation field. 
 * @param val The float[] to set.  */
public void setDetonationLocation(float[] val);

/** Return the detonationRelativeLocation value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDetonationRelativeLocation(float[] val);

/** Set the detonationRelativeLocation field. 
 * @param val The float[] to set.  */
public void setDetonationRelativeLocation(float[] val);

/** Return the isDetonated boolean value. 
 * @return The isDetonated boolean value.  */
public boolean getIsDetonated();

/** Return the detonateTime double value. 
 * @return The detonateTime double value.  */
public double getDetonateTime();

/** Return the eventApplicationID int value. 
 * @return The eventApplicationID int value.  */
public int getEventApplicationID();

/** Set the eventApplicationID field. 
 * @param val The int to set.  */
public void setEventApplicationID(int val);

/** Return the eventEntityID int value. 
 * @return The eventEntityID int value.  */
public int getEventEntityID();

/** Set the eventEntityID field. 
 * @param val The int to set.  */
public void setEventEntityID(int val);

/** Return the eventSiteID int value. 
 * @return The eventSiteID int value.  */
public int getEventSiteID();

/** Set the eventSiteID field. 
 * @param val The int to set.  */
public void setEventSiteID(int val);

/** Return the fired1 boolean value. 
 * @return The fired1 boolean value.  */
public boolean getFired1();

/** Set the fired1 field. 
 * @param val The boolean to set.  */
public void setFired1(boolean val);

/** Return the fired2 boolean value. 
 * @return The fired2 boolean value.  */
public boolean getFired2();

/** Set the fired2 field. 
 * @param val The boolean to set.  */
public void setFired2(boolean val);

/** Return the fireMissionIndex int value. 
 * @return The fireMissionIndex int value.  */
public int getFireMissionIndex();

/** Set the fireMissionIndex field. 
 * @param val The int to set.  */
public void setFireMissionIndex(int val);

/** Return the firingRange float value. 
 * @return The firingRange float value.  */
public float getFiringRange();

/** Set the firingRange field. 
 * @param val The float to set.  */
public void setFiringRange(float val);

/** Return the firingRate int value. 
 * @return The firingRate int value.  */
public int getFiringRate();

/** Set the firingRate field. 
 * @param val The int to set.  */
public void setFiringRate(int val);

/** Return the munitionApplicationID int value. 
 * @return The munitionApplicationID int value.  */
public int getMunitionApplicationID();

/** Set the munitionApplicationID field. 
 * @param val The int to set.  */
public void setMunitionApplicationID(int val);

/** Return the munitionEndPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMunitionEndPoint(float[] val);

/** Set the munitionEndPoint field. 
 * @param val The float[] to set.  */
public void setMunitionEndPoint(float[] val);

/** Return the munitionEntityID int value. 
 * @return The munitionEntityID int value.  */
public int getMunitionEntityID();

/** Set the munitionEntityID field. 
 * @param val The int to set.  */
public void setMunitionEntityID(int val);

/** Return the munitionSiteID int value. 
 * @return The munitionSiteID int value.  */
public int getMunitionSiteID();

/** Set the munitionSiteID field. 
 * @param val The int to set.  */
public void setMunitionSiteID(int val);

/** Return the munitionStartPoint value in the argument float[]
 * @param val The float[] to initialize.  */
public void getMunitionStartPoint(float[] val);

/** Set the munitionStartPoint field. 
 * @param val The float[] to set.  */
public void setMunitionStartPoint(float[] val);

/** Return the firedTime double value. 
 * @return The firedTime double value.  */
public double getFiredTime();

/** Set the firedTime field. 
 * @param val The double to set.  */
public void setFiredTime(double val);

/** Return the number of MFString items in the geoSystem field. 
 * @return the number of MFString items in the geoSystem field.  */
public int getNumGeoSystem();

/** Return the geoSystem value in the argument String[]
 * @param val The String[] to initialize.  */
public void getGeoSystem(String[] val);

/** Set the geoSystem field. 
 * @param val The String[] to set.  */
public void setGeoSystem(String[] val);

/** Return the geoOrigin X3DNode value. 
 * @return The geoOrigin X3DNode value.  */
public X3DNode getGeoOrigin();

/** Set the geoOrigin field. 
 * @param val The X3DNode to set.  */
public void setGeoOrigin(X3DNode val);

/** Return the entityCategory int value. 
 * @return The entityCategory int value.  */
public int getEntityCategory();

/** Set the entityCategory field. 
 * @param val The int to set.  */
public void setEntityCategory(int val);

/** Return the entityDomain int value. 
 * @return The entityDomain int value.  */
public int getEntityDomain();

/** Set the entityDomain field. 
 * @param val The int to set.  */
public void setEntityDomain(int val);

/** Return the entityExtra int value. 
 * @return The entityExtra int value.  */
public int getEntityExtra();

/** Set the entityExtra field. 
 * @param val The int to set.  */
public void setEntityExtra(int val);

/** Return the entityKind int value. 
 * @return The entityKind int value.  */
public int getEntityKind();

/** Set the entityKind field. 
 * @param val The int to set.  */
public void setEntityKind(int val);

/** Return the entitySpecific int value. 
 * @return The entitySpecific int value.  */
public int getEntitySpecific();

/** Set the entitySpecific field. 
 * @param val The int to set.  */
public void setEntitySpecific(int val);

/** Return the entityCountry int value. 
 * @return The entityCountry int value.  */
public int getEntityCountry();

/** Set the entityCountry field. 
 * @param val The int to set.  */
public void setEntityCountry(int val);

/** Return the entitySubCategory int value. 
 * @return The entitySubCategory int value.  */
public int getEntitySubCategory();

/** Set the entitySubCategory field. 
 * @param val The int to set.  */
public void setEntitySubCategory(int val);

/** Return the appearance int value. 
 * @return The appearance int value.  */
public int getAppearance();

/** Set the appearance field. 
 * @param val The int to set.  */
public void setAppearance(int val);

/** Return the linearVelocity value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLinearVelocity(float[] val);

/** Set the linearVelocity field. 
 * @param val The float[] to set.  */
public void setLinearVelocity(float[] val);

/** Return the linearAcceleration value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLinearAcceleration(float[] val);

/** Set the linearAcceleration field. 
 * @param val The float[] to set.  */
public void setLinearAcceleration(float[] val);

/** Return the forceID int value. 
 * @return The forceID int value.  */
public int getForceID();

/** Set the forceID field. 
 * @param val The int to set.  */
public void setForceID(int val);

/** Return the number of MFString items in the xmppParams field. 
 * @return the number of MFString items in the xmppParams field.  */
public int getNumXmppParams();

/** Return the xmppParams value in the argument String[]
 * @param val The String[] to initialize.  */
public void getXmppParams(String[] val);

/** Set the xmppParams field. 
 * @param val The String[] to set.  */
public void setXmppParams(String[] val);

}
