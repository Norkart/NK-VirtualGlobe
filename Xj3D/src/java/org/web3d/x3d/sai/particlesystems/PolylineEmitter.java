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

import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DParticleEmitterNode;
import org.web3d.x3d.sai.X3DProtoInstance;

/** Defines the requirements of an X3D PolylineEmitter node
 * @author Rex Melton
 * @version $Revision: 1.2 $ */
public interface PolylineEmitter extends X3DParticleEmitterNode {

/** Return the coords X3DNode value. 
 * @return The coords X3DNode value.  */
public X3DNode getCoords();

/** Set the coords field. 
 * @param val The X3DCoordinateNode to set.  */
public void setCoords(X3DCoordinateNode val);

/** Set the coords field. 
 * @param val The X3DProtoInstance to set.  */
public void setCoords(X3DProtoInstance val);

/** Return the number of MFInt32 items in the coordIndex field. 
 * @return the number of MFInt32 items in the coordIndex field.  */
public int getNumCoordIndex();

/** Return the coordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
public void getCoordIndex(int[] val);

/** Set the coordIndex field. 
 * @param val The int[] to set.  */
public void setCoordIndex(int[] val);

/** Return the direction value in the argument float[]
 * @param val The float[] to initialize.  */
public void getDirection(float[] val);

/** Set the direction field. 
 * @param val The float[] to set.  */
public void setDirection(float[] val);

}
