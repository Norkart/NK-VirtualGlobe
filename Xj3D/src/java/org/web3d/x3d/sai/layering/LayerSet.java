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

package org.web3d.x3d.sai.layering;

import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D LayerSet node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface LayerSet extends X3DNode {

/** Return the number of MFNode items in the layers field. 
 * @return the number of MFNode items in the layers field.  */
public int getNumLayers();

/** Return the layers value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
public void getLayers(X3DNode[] val);

/** Set the layers field. 
 * @param val The X3DNode[] to set.  */
public void setLayers(X3DNode[] val);

/** Return the activeLayer int value. 
 * @return The activeLayer int value.  */
public int getActiveLayer();

/** Set the activeLayer field. 
 * @param val The int to set.  */
public void setActiveLayer(int val);

/** Return the number of MFInt32 items in the order field. 
 * @return the number of MFInt32 items in the order field.  */
public int getNumOrder();

/** Return the order value in the argument int[]
 * @param val The int[] to initialize.  */
public void getOrder(int[] val);

/** Set the order field. 
 * @param val The int[] to set.  */
public void setOrder(int[] val);

}
