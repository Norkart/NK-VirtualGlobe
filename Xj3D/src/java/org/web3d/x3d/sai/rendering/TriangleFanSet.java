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

package org.web3d.x3d.sai.rendering;

import org.web3d.x3d.sai.X3DComposedGeometryNode;

/** Defines the requirements of an X3D TriangleFanSet node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface TriangleFanSet extends X3DComposedGeometryNode {

/** Return the number of MFInt32 items in the fanCount field. 
 * @return the number of MFInt32 items in the fanCount field.  */
public int getNumFanCount();

/** Return the fanCount value in the argument int[]
 * @param val The int[] to initialize.  */
public void getFanCount(int[] val);

/** Set the fanCount field. 
 * @param val The int[] to set.  */
public void setFanCount(int[] val);

}
