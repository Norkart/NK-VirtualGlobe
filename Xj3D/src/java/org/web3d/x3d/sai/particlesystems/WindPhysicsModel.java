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

package org.web3d.x3d.sai.particlesystems;

import org.web3d.x3d.sai.X3DParticlePhysicsModelNode;

/** Defines the requirements of an X3D WindPhysicsModel node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface WindPhysicsModel extends X3DParticlePhysicsModelNode {

/** Return the direction value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDirection(float[] val);

/** Set the direction field. 
 * @param val The float[] to set.  */
public void setDirection(float[] val);

/** Return the gustiness float value. 
 * @return The gustiness float value.  */
public float getGustiness();

/** Set the gustiness field. 
 * @param val The float to set.  */
public void setGustiness(float val);

/** Return the turbulence float value. 
 * @return The turbulence float value.  */
public float getTurbulence();

/** Set the turbulence field. 
 * @param val The float to set.  */
public void setTurbulence(float val);

/** Return the speed float value. 
 * @return The speed float value.  */
public float getSpeed();

/** Set the speed field. 
 * @param val The float to set.  */
public void setSpeed(float val);

}
