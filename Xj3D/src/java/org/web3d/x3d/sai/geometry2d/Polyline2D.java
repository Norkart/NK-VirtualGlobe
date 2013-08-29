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

package org.web3d.x3d.sai.geometry2d;

import org.web3d.x3d.sai.X3DGeometryNode;

/** Defines the requirements of an X3D Polyline2D node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface Polyline2D extends X3DGeometryNode {

/** Return the number of MFVec2f items in the lineSegments field. 
 * @return the number of MFVec2f items in the lineSegments field.  */
public int getNumLineSegments();

/** Return the lineSegments value in the argument float[]
 * @param val The float[] to initialize.  */
public void getLineSegments(float[] val);

/** Set the lineSegments field. 
 * @param val The float[] to set.  */
public void setLineSegments(float[] val);

}
