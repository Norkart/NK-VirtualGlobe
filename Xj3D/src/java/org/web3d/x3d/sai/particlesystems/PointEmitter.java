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

import org.web3d.x3d.sai.X3DParticleEmitterNode;

/** Defines the requirements of an X3D PointEmitter node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface PointEmitter extends X3DParticleEmitterNode {

/** Return the direction value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDirection(float[] val);

/** Set the direction field. 
 * @param val The float[] to set.  */
public void setDirection(float[] val);

/** Return the position value in the argument float[]
 * @param val The float[] to initialize.  */
public void getPosition(float[] val);

/** Set the position field. 
 * @param val The float[] to set.  */
public void setPosition(float[] val);

}
