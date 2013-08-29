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

package org.web3d.x3d.sai;

import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3DParticleEmitterNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface X3DParticleEmitterNode extends X3DNode {

/** Return the speed float value. 
 * @return The speed float value.  */
public float getSpeed();

/** Set the speed field. 
 * @param val The float to set.  */
public void setSpeed(float val);

/** Return the mass float value. 
 * @return The mass float value.  */
public float getMass();

/** Set the mass field. 
 * @param val The float to set.  */
public void setMass(float val);

/** Return the surfaceArea float value. 
 * @return The surfaceArea float value.  */
public float getSurfaceArea();

/** Set the surfaceArea field. 
 * @param val The float to set.  */
public void setSurfaceArea(float val);

/** Return the variation float value. 
 * @return The variation float value.  */
public float getVariation();

/** Set the variation field. 
 * @param val The float to set.  */
public void setVariation(float val);

}
